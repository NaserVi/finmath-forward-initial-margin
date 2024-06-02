package net.finmath.lch.initialmargin.simulation.modeldata;

import java.util.TreeMap;

import net.finmath.stochastic.RandomVariable;
import net.finmath.stochastic.Scalar;

/**
 * The stochastic curve (TreeMap<Double, RandomVariable>) represents an interest rate curve.
 * The key represents the fixing point on the curve and the value the attained paths of the random curve.
 * The curve is used for storing zero curves that are generated from historical data (Scalar) or from the LMM, and therefore, stochastic (RandomVariable). 
 * It uses an constant extrapolation and linear interpolation method on zero rates to obtain values that are not in the initial sampling set.
 * 
 * @author Raphael Prandtl
 *
 */
public class StochasticCurve {
	
	private TreeMap<Double, RandomVariable> curve = new TreeMap<>();

	
	public StochasticCurve(TreeMap<Double, RandomVariable> curve) {
		this.curve = curve;
	}
	
	
	public StochasticCurve() {
	}	
	
	
	/**
	 * @param periodLength The fixing point on the curve
	 * @param value The value / rate that will be added for the fixing point
	 */
	public void addRate(double periodLength, RandomVariable value) {
		curve.put(periodLength, value);
	}
	
	
	/**
	 * Returns the rate for the provided fixing.
	 * If the value is available it returns it directly from the map, otherwise inter-/extrapolation is used.
	 * 
	 * @param periodLength The fixing point on the curve
	 * @param pathOrState The path for which the rate should be returned
	 * @return The rate of the provided path, or the whole random variable if pathOrState == null
	 */
	public RandomVariable getRate(double periodLength, Integer pathOrState) {
		RandomVariable rate = getValue(periodLength);
		if (pathOrState != null) {
			return new Scalar(rate.get(pathOrState));
		}
		return rate;
	}
	

	/**
	 * @param periodLength The fixing point on the curve
	 * @return True if the value exists in the current curve sampling set 
	 */
	public boolean containsRate(double periodLength) {
		return curve.containsKey(periodLength);
	}
	
	
	/**
	 * @return The complete curve
	 */
	public TreeMap<Double, RandomVariable> getCurve() {
		return curve;
	}
	
	
	/**
	 * 
	 * @return The lowest fixing point in the current curve sampling set
	 */
	public double getFixingSmallest() {
		return curve.firstKey();
	}
	
	
	/**
	 * 
	 * @return The greatest fixing point in the current curve sampling set
	 */
	public double getFixingGreatest() {
		return curve.lastKey();
	}
	
	/*
	 *  Returns the value if it is in the current sampling set otherwise inter-/extrapolation.
	 *  The inter-/extrapolation is done for the complete random variable and afterwards path filtering is applied.
	 */
	private RandomVariable getValue(double periodLength) {
		if (containsRate(periodLength)) {
			return curve.get(periodLength);
		}
		RandomVariable zeroRate = getInterpolatedValue(periodLength);
		addRate(periodLength, zeroRate);
		return zeroRate;
	}
	
	
	// Internal interpolation for rate scenario shifts and historical data points
	private RandomVariable getInterpolatedValue(double periodLength) {
	    // constant extrapolation
	    if (periodLength <= curve.firstKey()) {
	        return curve.get(curve.firstKey());
	    }
	    
	    if (periodLength >= curve.lastKey()) {
	        return curve.get(curve.lastKey());
	    }
	 
        // linear interpolation of zero rates
        double lowerKey = curve.lowerKey(periodLength);
        double higherKey = curve.higherKey(periodLength);
        RandomVariable lowerValue = curve.get(lowerKey);
        RandomVariable higherValue = curve.get(higherKey);
            
        double weight = (periodLength - lowerKey) / (higherKey - lowerKey);
        return lowerValue.mult(1 - weight).add(higherValue.mult(weight));
	}

}
