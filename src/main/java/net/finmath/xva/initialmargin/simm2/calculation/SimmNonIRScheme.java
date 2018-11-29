package net.finmath.xva.initialmargin.simm2.calculation;

import net.finmath.sensitivities.simm2.RiskClass;
import net.finmath.sensitivities.simm2.SimmCoordinate;
import net.finmath.stochastic.RandomVariableInterface;
import net.finmath.stochastic.Scalar;
import net.finmath.xva.initialmargin.simm2.specs.ParameterSet;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represents a product that returns the initial margin to be posted at a fixed time according to SIMM.
 * This product will consider the non-IR Delta and Vega risk contributions to the total margin.
 */
public class SimmNonIRScheme {
	private final ParameterSet parameter;

	public SimmNonIRScheme(ParameterSet parameter) {
		this.parameter = parameter;
	}

	/**
	 * Calculates the weighted sensitivity of a trade.
	 *
	 * @param coordinate The coordinate describing what kind of sensitivity (with respect to which risk factor) x is.
	 * @param x          The value of the sensitivity as a random variable.
	 * @return The {@link WeightedSensitivity} object representing the computation result.
	 */
	public WeightedSensitivity getWeightedSensitivity(SimmCoordinate coordinate, RandomVariableInterface x) {
		//Additional weighting:
		//Vega: For non-IR, non-CR we have a non-trivial volatility weight to multiply
		//This is not included in the risk weight (the risk weight is excluded from the concentration factor)
		RandomVariableInterface a = x.mult(parameter.getAdditionalWeight(coordinate));

		final double riskWeight = parameter.getRiskWeight(coordinate);
		final double threshold = parameter.getConcentrationThreshold(coordinate);
		//not for credit -- here we have to sum up all sensitivities belonging to the same issuer/seniority; todo.
		final RandomVariableInterface concentrationRiskFactor = a.abs().div(threshold).sqrt().cap(1.0);

		return new WeightedSensitivity(coordinate, concentrationRiskFactor, a.mult(riskWeight).mult(concentrationRiskFactor));
	}

	/**
	 * Calculates the result of a bucket aggregation, i. e. the figures K including the constituents' weighted sensitivities.
	 *
	 * @param weightedSensitivities The set of the weighted sensitivities of the trades in the bucket.
	 * @return A {@link BucketResult} containing the whole thing.
	 */
	public BucketResult getBucketAggregation(String bucketName, Set<WeightedSensitivity> weightedSensitivities) {
		RandomVariableInterface k = weightedSensitivities.stream().
				flatMap(w -> weightedSensitivities.stream().map(v -> w.getCrossTermNonIR(v, parameter))).
				reduce(new Scalar(0.0), RandomVariableInterface::add).sqrt();

		return new BucketResult(bucketName, weightedSensitivities, k);
	}

	/**
	 * Calculates the resulting delta margin according to ISDA SIMM v2.0 B.8 (d)
	 *
	 * @param results A collection of per-bucket results.
	 * @return The delta margin.
	 */
	public RandomVariableInterface getMargin(Collection<BucketResult> results, RiskClass riskClass) {
		RandomVariableInterface kResidual = results.stream().
				filter(bK -> bK.getBucketName().equalsIgnoreCase("residual")).
				findAny().map(BucketResult::getK).orElse(new Scalar(0.0));

		return results.stream().
				filter(bK -> !bK.getBucketName().equalsIgnoreCase("residual")).
				flatMap(bK1 -> results.stream().
						filter(bK -> !bK.getBucketName().equalsIgnoreCase("residual")).
						map(bK2 -> {

							if (bK1.getBucketName().equalsIgnoreCase(bK2.getBucketName())) {
								return bK1.getK().squared();
							}
							return bK1.getS().mult(bK2.getS()).
									mult(parameter.getCrossBucketCorrelation(riskClass, bK1.getBucketName(), bK2.getBucketName()));
						})).
				reduce(new Scalar(0.0), RandomVariableInterface::add).
				sqrt().add(kResidual);
	}

	/**
	 * Strips the vertices of the gradient summing the sensitivities of different vertices together.
	 * Vertex-level sensitivities are neither needed for vega margins nor for delta margins (since this scheme does not handle IR delta).
	 * @param gradient The gradient which may contain coordinates with vertices.
	 * @return A stream of vertex-less coordinates paired with sensitivities.
	 */
	private Stream<Pair<SimmCoordinate, RandomVariableInterface>> stripVertices(Map<SimmCoordinate, RandomVariableInterface> gradient) {
		return gradient.entrySet().stream().
				map(z -> Pair.of(z.getKey().stripVertex(), z.getValue())).
				collect(Collectors.groupingBy(Pair::getKey)).entrySet().stream().
				map(group -> Pair.of(
						group.getKey(),
						group.getValue().stream().map(Pair::getValue).reduce(new Scalar(0.0), RandomVariableInterface::add)
						));
	}

	public RandomVariableInterface getMargin(RiskClass riskClass, Map<SimmCoordinate, RandomVariableInterface> gradient) {

		Set<BucketResult> bucketResults = stripVertices(gradient).
				map(z -> Pair.of(
						z.getKey().getSimmBucket(),
						getWeightedSensitivity(z.getKey(), z.getValue()))).
				collect(Collectors.groupingBy(Pair::getKey, Collectors.mapping(Pair::getValue, Collectors.toSet()))).
				entrySet().stream().
				map(bucketWS -> getBucketAggregation(bucketWS.getKey(), bucketWS.getValue())).
				collect(Collectors.toSet());

		return getMargin(bucketResults, riskClass);
	}
}