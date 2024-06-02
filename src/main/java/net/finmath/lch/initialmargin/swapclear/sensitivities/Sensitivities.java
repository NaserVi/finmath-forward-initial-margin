package net.finmath.lch.initialmargin.swapclear.sensitivities;


import java.time.LocalDateTime;

import net.finmath.exception.CalculationException;

/**
 * Interface for the calculation of sensitivitiy curves
 */
public interface Sensitivities {
	
	/**
	 * Calculates the delta sensitivities mapped to a fixed grid of standard tenors
	 * @param evaluationDate The date of the evaluation
	 * @return The StochasticCurve with all delta sensitivities mapped to fixed tenor points
	 * @throws CalculationException
	 */
	public SensitivityMatrix getDeltaSensitivities(LocalDateTime evaluationDate) throws CalculationException;

	
	/**
	 * Calculates the gamma sensitivities mapped to a fixed grid of standard tenors
	 * @param evaluationDate The date of the evaluation
	 * @return The StochasticCurve with all sensitivities mapped to fixed tenor points
	 * @throws CalculationException
	 */
	public SensitivityMatrix getGammaSensitivities(LocalDateTime evaluationDate) throws CalculationException;
			

}
