package net.finmath.lch.initialmargin.simulation.ratetransformations;

import net.finmath.exception.CalculationException;

/**
 * Abstract base class to implement the {@link lch.initialmargin.simulation.ratetransformations.RateTransformation} interface.
 * Manages for all transformation methods the unique ID handling.
 * 
 * @author Raphael Prandtl
 */
public abstract class AbstractRateTransformation implements RateTransformation {
	
	private final String ID;
	
	/**
	 * Ensures that all TransformationMethod classes must define a method ID.
	 * @param ID The ID of the transformation method. Specified in the respective classes.
	 */
	protected AbstractRateTransformation(String ID) {
		this.ID = ID;
	}

	 
	@Override
	public String getId() {
		return ID;
	}

	
	@Override
	public abstract void applyTransformation(TransformationContext context) throws CalculationException;

	
	
}
