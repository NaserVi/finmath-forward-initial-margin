package net.finmath.lch.initialmargin.simulation.modeldata;

import java.time.LocalDateTime;
import java.util.NavigableMap;
import java.util.TreeMap;

import net.finmath.lch.initialmargin.swapclear.aggregationscheme.SchemeStorage.Currency;
import net.finmath.lch.initialmargin.swapclear.aggregationscheme.SchemeStorage.CurveName;
import net.finmath.exception.CalculationException;
import net.finmath.montecarlo.interestrate.LIBORModelMonteCarloSimulationModel;
import net.finmath.stochastic.RandomVariable;
import net.finmath.stochastic.Scalar;
import net.finmath.time.FloatingpointDate;

/**
 * The ZeroRateModel to calculate (forward) initial margin for swaps.
 * It is a time series of StochasticCurve, which are either from the historical input data or LMM generated data.
 * The model provides discount factors and forward rates calculated from the zero rate curves. 
 * LCH uses a standardized zero curve to calculate interest rate risk and scenarios for the historical simulations.
 * If rates/discount factors from historical date are called (evaluationDate < referenceDate) the StochasticCurve internal inter-/extrapolation scheme is used.
 * If rates/discount factors from model data are called (evaluationDate >= referenceDate) the data is directly retrieve from the input model.
 * 
 * @author Raphael Prandtl
 */
public class ZeroRateModel {
	
	
    private final LocalDateTime referenceDate; // date up to which zero rate history is available. Has to be equal to LIBOR model reference date
    private final LIBORModelMonteCarloSimulationModel processModel;
    private final NavigableMap<LocalDateTime, StochasticCurve> zeroCurves;
	
	private static final double DAILY_STEP_SIZE = 1/365.0; 


	public ZeroRateModel(final Currency currency, final CurveName curveName, final LIBORModelMonteCarloSimulationModel processModel, final String inputDataFilePath) throws CalculationException {
		this.processModel = processModel;
		this.referenceDate = processModel.getReferenceDate();
		this.zeroCurves = new ZeroCurveHistory(inputDataFilePath).buildZeroCurveHistory();
	}		
	
	
	/**
	 * Returns the historical or model generated zero rate for the given evaluationDate.
	 * If the evaluationDate < referenceDate of the model it returns the historical rate otherwise the model generated rate
	 * 
	 * @param evaluationDate Date for which the rate should be returned
	 * @param periodLength Maturity of the zero rate
	 * @param curveshifts If provided, adds a shift to the rate before returning it
	 * @param pathOrState The path or state of the rate if only one simulation path should be considered
	 * @return The zero rate of the model
	 * @throws CalculationException
	 */
	public RandomVariable getZeroRate(LocalDateTime evaluationDate, double periodLength, StochasticCurve curveshifts, Integer pathOrState) throws CalculationException {
		// Case: evaluation date is before reference date -> call zero rates from history
		if (evaluationDate.isBefore(referenceDate)) {
			// Case: evaluation date is before first available data point
			if (evaluationDate.isBefore(zeroCurves.firstKey())) {
				throw new IllegalArgumentException("Evaluation date is before beginning of available data. First entry at: " + zeroCurves.firstKey());
			}
			// Case: evaluation date is not available, e.g. weekend or holiday 
			if (zeroCurves.get(evaluationDate) == null) {
				throw new IllegalArgumentException("No data available for " + evaluationDate + ". Check for weekend or holiday.");
			}
		}
		// Case: evaluation date is after reference date  -> call zero rates from model
		double modelTime = FloatingpointDate.getFloatingPointDateFromDate(referenceDate, evaluationDate);
		// Case: date is called for the first time -> create a new ZeroCurve for this date
		if ((evaluationDate.isAfter(referenceDate) || evaluationDate.isEqual(referenceDate)) && zeroCurves.get(evaluationDate) == null) {
			setUpZeroCurve(evaluationDate);
		}
		// Case: curve already available but zero rate not available for periodLength
		//TODO: check with extrapolation for coarser grid and short periodLengths e.g. get first LIBOR period length
		if((evaluationDate.isAfter(referenceDate) || evaluationDate.isEqual(referenceDate)) && !zeroCurves.get(evaluationDate).containsRate(periodLength)) {
			addZeroRateFromProcessModel(evaluationDate, modelTime, periodLength, DAILY_STEP_SIZE); 
		}
		// Case: evaluation date is available in historical data set -> call to stochastic curve and internal interpolation if needed
		// Case: evaluation date is after reference date and zero rate is available for periodLength
		if (curveshifts == null) {
			return zeroCurves.get(evaluationDate).getRate(periodLength, pathOrState);
		}
		return zeroCurves.get(evaluationDate).getRate(periodLength, pathOrState).add(curveshifts.getRate(periodLength, pathOrState));
	}
		
	
	/**
	 * Returns the historical or model generated discount factor for the given evaluationDate.
	 * @param evaluationDate Date for which the rate should be returned
	 * @param periodLength Maturity of the zero rate
	 * @param curveshifts If provided, adds a shift to the rate before calculating the discount factor
	 * @param pathOrState The path or state of the rate if only one simulation path should be considered
	 * @return The discount factor calculated from the rate of the model
	 * @throws CalculationException
	 */
	public RandomVariable getDiscountFactor(LocalDateTime evaluationDate, double periodLength, StochasticCurve curveshifts, Integer pathOrState) throws CalculationException {
		if (periodLength == 0.0) {
			return new Scalar(1.0);
		}
		return getZeroRate(evaluationDate, periodLength, curveshifts, pathOrState).mult(-periodLength).exp();
	}
	
	
	/**
	 * Returns the historical or model generated forward rate for the given evaluationDate.
	 * @param evaluationDate Date for which the rate should be returned
	 * @param periodLength Maturity of the zero rate
	 * @param curveshifts If provided, adds a shift to both rates used for calculating the discount factors
	 * @param pathOrState The path or state of the rate if only one simulation path should be considered
	 * @return The forward rate calculated from the rates of the model
	 * @throws CalculationException
	 */
	public RandomVariable getForwardRate(LocalDateTime evaluationDate, double periodStart, double periodEnd, StochasticCurve curveshifts, Integer pathOrState) throws CalculationException {
		RandomVariable discountFactorPeriodStart = getDiscountFactor(evaluationDate, periodStart, curveshifts, pathOrState);
		RandomVariable discountFactorPeriodEnd = getDiscountFactor(evaluationDate, periodEnd, curveshifts, pathOrState);
		return discountFactorPeriodStart.div(discountFactorPeriodEnd).sub(1.0).div(periodEnd - periodStart);
	}
	
	
	/**
	 * Returns a time series of random variables use for scenario creation / sampling.
	 * First call takes the longest time due to interpolation of complete history but for every new call only one rate has to be added
	 * @param evaluationDate
	 * @param periodLength The tenor point fixing for which data should be returned  
	 * @return The time series of zero rates (historical + model data)
	 * @throws CalculationException
	 */
	public NavigableMap<LocalDateTime, RandomVariable> getRateSeries(LocalDateTime evaluationDate, double periodLength) throws CalculationException {
		// check if periodLength exceeds minimal or maximal periodLenght available in history.
		// Necessary for scenario sampling of "xM" curves, e.g. 3M LIBOR. Since historical data set starts with 3M fixing and if sensitivities for 1d point call the respective scenarios for 1d 
		// we need a constant extrapolation of the 3M point and adjust the model rates fixing to "continue" with this fixing
		if (periodLength < zeroCurves.firstEntry().getValue().getFixingSmallest()) {
			periodLength = zeroCurves.firstEntry().getValue().getFixingSmallest();
		}
		if (periodLength > zeroCurves.firstEntry().getValue().getFixingGreatest()) {
			periodLength = zeroCurves.firstEntry().getValue().getFixingGreatest();
		}
		NavigableMap<LocalDateTime, RandomVariable> rateSeries = new TreeMap<>();
		// Returns the rate series from the beginning of available data until the cutoff date (inclusive)
		// Scenarios == curveShifts -> curveShifts always null + always sampled on all realizations
		NavigableMap<LocalDateTime, StochasticCurve> zeroCurveHeadMap = zeroCurves.headMap(evaluationDate, true);
		for (LocalDateTime date : zeroCurveHeadMap.keySet()) {
			rateSeries.put(date, this.getZeroRate(date, periodLength, null, null));
		}
		return rateSeries;
	}
	
	
	/**
	 * 
	 * @return The maturity of the LIBOR model
	 */
	public double getModelMaturity() {
		return processModel.getLiborPeriodDiscretization().getAsArrayList().get(processModel.getLiborPeriodDiscretization().getNumberOfTimes() - 1);
	}
	
	
	/**
	 * 
	 * @return The reference date (start date) of the LIBOR model
	 */
	public LocalDateTime getReferenceDate() {
		return referenceDate;
	}
	
	
	/**
	 * 
	 * @return The number of paths used in the LIBOR model
	 */
	public int getNumberOfPaths() {
		return processModel.getNumberOfPaths();
	}
	
	
	// Case: First call to new evaluation date -> new zero curve instantiated
	private void setUpZeroCurve(LocalDateTime evaluationDate) throws CalculationException {
		if (evaluationDate.isBefore(processModel.getReferenceDate())) {
			throw new IllegalArgumentException("Evaluation date is between end of available historical data and beginning of model data.");
		}
		zeroCurves.put(evaluationDate, new StochasticCurve());
	}
	
	
	// Time step interpolation to fit model discretization to daily scenarios
	private void addZeroRateFromProcessModel(LocalDateTime evaluationDate, double time, double periodLength, double dailyStepSize) throws CalculationException {
        int timeIndexNearestLessOrEqual = processModel.getTimeDiscretization().getTimeIndexNearestLessOrEqual(time);
        double timeNearestLessOrEqual = processModel.getTimeDiscretization().getTime(timeIndexNearestLessOrEqual);
        // if distance to next less time is smaller than half length of a day step size we evaluate at the grid point
        if (Math.abs(timeNearestLessOrEqual - time) < dailyStepSize/2.0) {
        	zeroCurves.get(evaluationDate).addRate(periodLength, getZeroRateOnTimeGrid(timeNearestLessOrEqual, periodLength));
        }
        int timeIndexNearestGreaterOrEqual  = timeIndexNearestLessOrEqual + 1;
        double timeNearestGreaterOrEqual = processModel.getTimeDiscretization().getTime(timeIndexNearestGreaterOrEqual);
        // if nearest greater time exceeds model end use constant extrapolation: timeNearestGreaterOrEqual + periodLength > modelMaturity 
        if (timeIndexNearestGreaterOrEqual + periodLength > processModel.getLiborPeriodDiscretization().getAsArrayList().get(processModel.getLiborPeriodDiscretization().getNumberOfTimes() - 1)) {
        	zeroCurves.get(evaluationDate).addRate(periodLength, getZeroRateOnTimeGrid(timeNearestLessOrEqual, periodLength));
        }
        // if distance to next greater time is smaller than half length of a day step size we evaluate at the grid point 
        if (Math.abs(timeNearestGreaterOrEqual - time) < dailyStepSize/2.0) {
        	zeroCurves.get(evaluationDate).addRate(periodLength, getZeroRateOnTimeGrid(timeNearestGreaterOrEqual, periodLength));
        }
        // If not we interpolate the  zero rates linearly between the closest lower and upper grid points for the given time 
        RandomVariable zeroRateNearestLessOrEqual 	 = getZeroRateOnTimeGrid(timeNearestLessOrEqual, periodLength);
        RandomVariable zeroRateNearestGreaterOrEqual = getZeroRateOnTimeGrid(timeNearestGreaterOrEqual, periodLength);
    	// lowerTime < upperTime always ensured due to daily increase in every iteration
        double weight = (time - timeNearestLessOrEqual) / (timeNearestGreaterOrEqual - timeNearestLessOrEqual);
        zeroCurves.get(evaluationDate).addRate(periodLength, zeroRateNearestLessOrEqual.mult(1 - weight).add(zeroRateNearestGreaterOrEqual.mult(weight)));
	}
	
	
	private RandomVariable getZeroRateOnTimeGrid(double time, double periodLength) throws CalculationException {
		return getForwardBondLibor(time, periodLength).log().div(-periodLength);
	}
	
	
	private RandomVariable getForwardBondLibor(double time, double periodLength) throws CalculationException {
		return processModel.getForwardRate(time, time, time + periodLength).mult(periodLength).add(1.0).invert();
	}
	
	
	/*  
	 *  Adjustment factor alpha(t) = E^Q[1/N(t)] * P^OIS(t;0)^-1 
	 *  P^OIS(T;t) = P^L(T;t) * alpha(t)/alpha(T) 
	 *  P^L(T;t) = (L(t;t,T)*(T-t)+1)^-1
	 *  alpha(t)/alpha(T) = P^L(t;0)/P^L(T;0) * P^OIS(T;0)/P^OIS(t;0)
	 *  P^L(t;0)/P^L(T;0) = L(0;t,T)*(T-t)+1
	 */	
//	private RandomVariable getForwardBondOIS(double time, double periodLength) throws CalculationException {
//		RandomVariable forwardAtZero = processModel.getForwardRate(0.0, time, time + periodLength).mult(periodLength).add(1.0);
//		RandomVariable forwardAtTime = processModel.getForwardRate(time, time, time + periodLength).mult(periodLength).add(1.0);
//		double oisDiscountFactors = processModel.getModel().getDiscountCurve().getDiscountFactor(time + periodLength)/processModel.getModel().getDiscountCurve().getDiscountFactor(time);
//		return forwardAtZero.mult(oisDiscountFactors).div(forwardAtTime);
//	}

}
