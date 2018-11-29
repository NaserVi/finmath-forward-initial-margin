package net.finmath.sensitivities;

import net.finmath.montecarlo.interestrate.LIBORModelMonteCarloSimulationInterface;
import net.finmath.stochastic.RandomVariableInterface;
import net.finmath.stochastic.Scalar;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Provides a fixed gradient throughout all times.
 */
public class GradientProductConstant<C> implements GradientProduct<C> {

	private Map<C, RandomVariableInterface> sensitivitiyMap;

	private GradientProductConstant(Map<C, RandomVariableInterface> sensitivityMap) {
		this.sensitivitiyMap = sensitivityMap;
	}

	public static <C> GradientProductConstant<C> fromDouble(Map<C, Double> sensitivities) {
		return fromRandom(
				sensitivities.entrySet().stream().
						map(e -> Pair.of(e.getKey(), new Scalar(e.getValue()))).
						collect(Collectors.toMap(Pair::getKey, Pair::getValue)));
	}

	public static <C> GradientProductConstant<C> fromRandom(Map<C, RandomVariableInterface> sensitivities) {
		return new GradientProductConstant(sensitivities);
	}

	/**
	 * Returns the constant sensitivities that were given upon construction.
	 *
	 * @param evaluationTime The time which will not be evaluated.
	 * @param model The model which will not be evaluated.
	 * @return The constant map from coordinates to sensitivity values.
	 */
	@Override
	public Map<C, RandomVariableInterface> getGradient(double evaluationTime, LIBORModelMonteCarloSimulationInterface model) {
		return sensitivitiyMap;
	}
}