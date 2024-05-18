package net.finmath.lch.initialmargin.simulation.ratetransformations;

import net.finmath.exception.CalculationException;

/**
 * Interface for applying various transformations on a random variable time series TreeMap<LocalDateTime, RandomVariable>.
 * The transformations are used to create risk scenarios / curve shifts.
 *
 * @author Raphael Prandtl
 */
public interface RateTransformation {
	
	/**
	 * 
	 * @return The unique ID of the TransformationMethod. Each method has its own ID.
	 */
	String getId();
	
	/**
	 * Applies the transformation on the primary data set of the TransformationContext and stores the result as the new primary data set.
	 * Adds the data set to the transformation pipeline with the corresponding transformation ID. 
	 * @param context The TransformationContext, that stores all the primary data set, applied transformations and intermediate results.
	 */
	void applyTransformation(TransformationContext context) throws CalculationException;
	

}
