package net.finmath.xva.sensiproducts;

import java.time.LocalDateTime;
import java.util.Map;

import net.finmath.exception.CalculationException;
import net.finmath.montecarlo.BrownianMotion;
import net.finmath.montecarlo.MonteCarloSimulationModel;
import net.finmath.montecarlo.RandomVariableFromDoubleArray;
import net.finmath.montecarlo.interestrate.LIBORModelMonteCarloSimulationModel;
import net.finmath.montecarlo.interestrate.TermStructureModel;
import net.finmath.montecarlo.process.MonteCarloProcess;
import net.finmath.stochastic.RandomVariable;
import net.finmath.time.TimeDiscretization;

/**
 * A stub that only provides a time discretization and constants.
 */
class SimulationStub implements LIBORModelMonteCarloSimulationModel {
	private TimeDiscretization timeDiscretization;

	SimulationStub(TimeDiscretization timeDiscretization) {
		this.timeDiscretization = timeDiscretization;
	}

	/**
	 * Returns the numberOfPaths.
	 *
	 * @return Returns the numberOfPaths.
	 */
	@Override
	public int getNumberOfPaths() {
		return 1;
	}

	@Override
	public LocalDateTime getReferenceDate() {
		return null;
	}

	/**
	 * Returns the timeDiscretizationFromArray.
	 *
	 * @return Returns the timeDiscretizationFromArray.
	 */
	@Override
	public TimeDiscretization getTimeDiscretization() {
		return timeDiscretization;
	}

	/**
	 * Returns the time for a given time index.
	 *
	 * @param timeIndex Time index
	 * @return Returns the time for a given time index.
	 */
	@Override
	public double getTime(int timeIndex) {
		return timeDiscretization.getTime(timeIndex);
	}

	/**
	 * Returns the time index for a given time.
	 *
	 * @param time The time.
	 * @return Returns the time index for a given time.
	 */
	@Override
	public int getTimeIndex(double time) {
		return timeDiscretization.getTimeIndex(time);
	}

	/**
	 * Returns a random variable which is initialized to a constant,
	 * but has exactly the same number of paths or discretization points as the ones used by this <code>MonteCarloSimulationModel</code>.
	 *
	 * @param value The constant value to be used for initialized the random variable.
	 * @return A new random variable.
	 */
	@Override
	public RandomVariable getRandomVariableForConstant(double value) {
		return new RandomVariableFromDoubleArray(value);
	}

	/**
	 * This method returns the weights of a weighted Monte Carlo method (the probability density).
	 *
	 * @param timeIndex Time index at which the process should be observed
	 * @return A vector of positive weights which sums up to one
	 */
	@Override
	public RandomVariable getMonteCarloWeights(int timeIndex) {
		return null;
	}

	/**
	 * This method returns the weights of a weighted Monte Carlo method (the probability density).
	 *
	 * @param time Time at which the process should be observed
	 * @return A vector of positive weights which sums up to one
	 */
	@Override
	public RandomVariable getMonteCarloWeights(double time) {
		return null;
	}

	/**
	 * Create a clone of this simulation modifying some of its properties (if any).
	 *
	 * @param dataModified The data which should be changed in the new model
	 * @return Returns a clone of this model, with some data modified (then it is no longer a clone :-)
	 */
	@Override
	public MonteCarloSimulationModel getCloneWithModifiedData(Map<String, Object> dataModified) {
		return null;
	}

	/**
	 * @return Returns the numberOfFactors.
	 */
	@Override
	public int getNumberOfFactors() {
		return 1;
	}

	/**
	 * Returns the libor period discretization as time discretization representing start and end dates of periods.
	 *
	 * @return Returns the libor period discretization
	 */
	@Override
	public TimeDiscretization getLiborPeriodDiscretization() {
		return null;
	}

	/**
	 * @return The number of LIBORs in the LIBOR discretization
	 */
	@Override
	public int getNumberOfLibors() {
		return 1;
	}

	/**
	 * Returns the period start of the specified forward rate period.
	 *
	 * @param timeIndex The index corresponding to a given time (interpretation is start of period)
	 * @return The period start of the specified forward rate period.
	 */
	@Override
	public double getLiborPeriod(int timeIndex) {
		return 0;
	}

	/**
	 * Same as java.util.Arrays.binarySearch(liborPeriodDiscretization,time).
	 * Will return a negative value if the time is not found, but then -index-1 corresponds to the index of the smallest time greater than the given one.
	 *
	 * @param time The tenor time (fixing of the forward rate) for which the index is requested.
	 * @return The index corresponding to a given time (interpretation is start of period)
	 */
	@Override
	public int getLiborPeriodIndex(double time) {
		return 0;
	}

	/**
	 * Return the forward rate for a given simulation time index and a given forward rate index.
	 *
	 * @param timeIndex  Simulation time index.
	 * @param liborIndex TenorFromArray time index (index corresponding to the fixing of the forward rate).
	 * @return The forward rate as a random variable.
	 * @throws CalculationException Thrown if the valuation fails, specific cause may be available via the <code>cause()</code> method.
	 */
	@Override
	public RandomVariable getLIBOR(int timeIndex, int liborIndex) throws CalculationException {
		return null;
	}

	/**
	 * Return the forward rate curve for a given simulation time index.
	 *
	 * @param timeIndex Simulation time index.
	 * @return The forward rate curve for a given simulation time index.
	 * @throws CalculationException Thrown if the valuation fails, specific cause may be available via the <code>cause()</code> method.
	 */
	@Override
	public RandomVariable[] getLIBORs(int timeIndex) throws CalculationException {
		return new RandomVariable[0];
	}

	/**
	 * Returns the Brownian motion used to simulate the curve.
	 *
	 * @return The Brownian motion used to simulate the curve.
	 */
	@Override
	public BrownianMotion getBrownianMotion() {
		return null;
	}

	/**
	 * Return the forward rate for a given simulation time and a given period start and period end.
	 *
	 * @param time        Simulation time
	 * @param periodStart Start time of period
	 * @param periodEnd   End time of period
	 * @return The forward rate as a random variable as seen on simulation time for the specified period.
	 * @throws CalculationException Thrown if the valuation fails, specific cause may be available via the <code>cause()</code> method.
	 */
	@Override
	public RandomVariable getLIBOR(double time, double periodStart, double periodEnd) throws CalculationException {
		return null;
	}

	/**
	 * Return the numeraire at a given time.
	 *
	 * @param time Time at which the process should be observed
	 * @return The numeraire at the specified time as <code>RandomVariableFromDoubleArray</code>
	 * @throws CalculationException Thrown if the valuation fails, specific cause may be available via the <code>cause()</code> method.
	 */
	@Override
	public RandomVariable getNumeraire(double time) throws CalculationException {
		return null;
	}

	/**
	 * Returns the underlying model.
	 * <p>
	 * The model specifies the measure, the initial value, the drift, the factor loadings (covariance model), etc.
	 *
	 * @return The underlying model
	 */
	@Override
	public TermStructureModel getModel() {
		return null;
	}

	@Override
	public Map<String, RandomVariable> getModelParameters() {
		return null;
	}

	/**
	 * @return The implementation of the process
	 */
	@Override
	public MonteCarloProcess getProcess() {
		return null;
	}

	/**
	 * Return a clone of this model with a modified Brownian motion using a different seed.
	 *
	 * @param seed The seed
	 * @return Clone of this object, but having a different seed.
	 * @deprecated
	 */
	@Override
	public Object getCloneWithModifiedSeed(int seed) {
		return null;
	}

	@Override
	public RandomVariable getForwardRate(double time, double periodStart, double periodEnd)
			throws CalculationException {
		// TODO Auto-generated method stub
		return null;
	}
}
