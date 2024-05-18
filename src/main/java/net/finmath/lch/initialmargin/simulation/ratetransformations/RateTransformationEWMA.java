package net.finmath.lch.initialmargin.simulation.ratetransformations;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;

import net.finmath.exception.CalculationException;
import net.finmath.stochastic.RandomVariable;
import net.finmath.stochastic.Scalar;

/**
 * A transformation method to calculate the dispersion of a rate time series based on an exponential weighted moving average.
 * The EWMA is an iterative formula and requires a ‘seed volatility’ (classic standard deviation) to begin the series. 
 * 
 * @author Raphael Prandtl
 */
public class RateTransformationEWMA extends AbstractRateTransformation {
	
	private final int 				volatilitySeedPeriod;
	private final double 			decayFactor;
	
	
	/**
     * Performs an EWMA dispersion transformation.
     * 
     * @param volatilitySeedPeriod The number of entries to include in the initial standard deviation calculation
	 * @param decayFactor The EWMA decay factor 𝜆 determines the relative weighting of historic to recent data points. 
	 * A high 𝜆 implies a relatively high weighting of historic data points, and low weighting of new data points.
	 */
	public RateTransformationEWMA(final int volatilitySeedPeriod, final double decayFactor) {
		super("EWMA");    // unique TransformationMethod ID
		this.volatilitySeedPeriod = volatilitySeedPeriod;
		this.decayFactor = decayFactor;
	}
	
	
	public void applyTransformation(TransformationContext context) throws CalculationException{
		if ( volatilitySeedPeriod == 0 || volatilitySeedPeriod == 1) {
			throw new CalculationException("volatilitySeedPeriod must be strictly greater than 1.");
		}
		
		NavigableMap<LocalDateTime, RandomVariable> primaryData = context.getPrimaryData();
		if (primaryData.size() < volatilitySeedPeriod) {
			throw new CalculationException("volatilitySeedPeriod can't be greater than number of available rates.");
		}
		
		// "Plant" volatility seed at the beginning of the data set
		NavigableMap<LocalDateTime, RandomVariable> ewmaData = new TreeMap<>();
		List<LocalDateTime> dates = new ArrayList<>(primaryData.keySet());
		RandomVariable sum = new Scalar(0.0);
		RandomVariable sumOfSquares = new Scalar(0.0);

		for (int i = 0; i < volatilitySeedPeriod; i++) {
			LocalDateTime date = dates.get(i);
	    	RandomVariable rate = primaryData.get(date);
	        sum = sum.add(rate);
	        sumOfSquares = sumOfSquares.add(rate.squared());
	    }
		
    	RandomVariable numerator = sumOfSquares.sub(sum.squared().div(volatilitySeedPeriod)); 
	    double normalizationFactor = 1.0 / (volatilitySeedPeriod - 1);
	    RandomVariable volatilitySeed = numerator.mult(normalizationFactor);

	    ewmaData.put(dates.get(0), volatilitySeed.sqrt());
	    
	    // EWMA Transformation based on volatility seed
	    double oneMinusDecayFactor = 1.0 - decayFactor; 
	    RandomVariable previousDispersionSquared = volatilitySeed;

	    for (int i = 1; i < primaryData.size(); i++) {
	    	LocalDateTime currentDate = dates.get(i);
	        RandomVariable currentReturnSquared = primaryData.get(currentDate).squared();
	        RandomVariable currentDispersionSquared = previousDispersionSquared.mult(decayFactor).add(currentReturnSquared.mult(oneMinusDecayFactor));
	        ewmaData.put(currentDate, currentDispersionSquared.sqrt());
	        previousDispersionSquared = currentDispersionSquared;
	    }
	    context.addTransformationResult(this.getId(), ewmaData);
	    context.setPrimaryData(ewmaData);
	}
	

}
