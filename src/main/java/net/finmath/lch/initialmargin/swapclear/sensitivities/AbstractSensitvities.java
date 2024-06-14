package net.finmath.lch.initialmargin.swapclear.sensitivities;

import java.time.LocalDateTime;
import java.util.List;

import net.finmath.lch.initialmargin.simulation.modeldata.TenorGrid;
import net.finmath.exception.CalculationException;
import net.finmath.stochastic.RandomVariable;


/**
 * Implementation of {@link net.finmath.lch.initialmargin.swapclear.sensitivities.Sensitivities}
 * Calculates sensitivitiy curves for swap legs
 */
public abstract class AbstractSensitvities implements Sensitivities {
	
	protected SensitivityComponentsForSwapLeg 				sensitivityComponentsForSwapLeg;
	protected TenorGrid										tenorGrid;
    protected enum Derivative								{DELTA, GAMMA};

    protected static final double ZERO_RATE_SHIFT = 0.0001; // 1 bps shift
	
    
	public AbstractSensitvities(SensitivityComponentsForSwapLeg sensitivityComponentsForSwapLeg, TenorGrid tenorGrid) {
		this.sensitivityComponentsForSwapLeg = sensitivityComponentsForSwapLeg;
		this.tenorGrid = tenorGrid; // Risk sampling always on "fixed grid in zero space"
	}


	@Override
	public SensitivityMatrix getDeltaSensitivities(LocalDateTime evaluationDate) throws CalculationException {
		return calculateSensitivityCurve(evaluationDate, Derivative.DELTA);
	}

	
	@Override
	public SensitivityMatrix getGammaSensitivities(LocalDateTime evaluationDate) throws CalculationException {
		return calculateSensitivityCurve(evaluationDate, Derivative.GAMMA);
	}
	
	
	// Here the sensitivity calculation is assigned to the type and the actual culculation is happening in the implementations, 
	protected SensitivityMatrix calculateSensitivityCurve(LocalDateTime evaluationDate, Derivative derivative) throws CalculationException {
		SensitivityMatrix sensitivityMatrix = new SensitivityMatrix();
	    List<SensitivityComponent> components = sensitivityComponentsForSwapLeg.getSensitivityComponents(evaluationDate);
	    for (SensitivityComponent component : components) {
	    	switch (derivative) {
			case DELTA:
				calculateDeltaSensitivity(sensitivityMatrix, component);
				break;
			case GAMMA:
				calculateGammaSensitivity(sensitivityMatrix, component);;
				break;
			default:
				throw new IllegalArgumentException("Derivative type not supported.");
			} 
	    }
	    return sensitivityMatrix;
	}


	protected abstract void calculateDeltaSensitivity(SensitivityMatrix sensitivityMatrix, SensitivityComponent component);

	
	protected abstract void calculateGammaSensitivity(SensitivityMatrix sensitivityMatrix, SensitivityComponent component);


	// Returns the zero rate independent swap components as a factor
	protected RandomVariable getConstantCoefficient(RandomVariable notional, double dayCountFraction, boolean isPayer) {
		RandomVariable coefficient = notional.mult(dayCountFraction);
		if (isPayer) {
			coefficient = coefficient.mult(-1.0);
		}
		return coefficient;
	}
	
	
	// We have always that maturitySecondOrder >= maturityFirstOrder
	protected void mapGammaToMatrix(SensitivityMatrix sensitivityMatrix, RandomVariable sensitivity, double maturityFirstOrder, double maturitySecondOrder) {	
		double lowerFixingFirstOrder = tenorGrid.getNearestSmallerFixing(maturityFirstOrder);
		double upperFixingFirstOrder = tenorGrid.getNearestGreaterFixing(maturityFirstOrder);
		double distanceFirstOrder = upperFixingFirstOrder - lowerFixingFirstOrder;
		
		double lowerFixingSecondOrder = tenorGrid.getNearestSmallerFixing(maturitySecondOrder);
		double upperFixingSecondOrder = tenorGrid.getNearestGreaterFixing(maturitySecondOrder);
		double distanceSecondOrder = upperFixingSecondOrder - lowerFixingSecondOrder;
		// if lower and upper fixings are the same then the distance is zero
		// the distance is zero either because both are smaller than the smallest or greater than the greatest fixing or maturity aligns exactly with bucket point
		if (distanceFirstOrder == 0) {
			// Case 1: the sensitivity is assigned to only one row
			// Determine the weighting of the columns
			if (distanceSecondOrder == 0) {
					// Case: Sensitivity is only assigned to one column
					sensitivityMatrix.addValue(lowerFixingFirstOrder, lowerFixingSecondOrder, sensitivity);
			} else {
				// Case: Sensitivity is assigned only to one row but weighted across columns
				double weightSecondOrder = (maturitySecondOrder - lowerFixingSecondOrder) / distanceSecondOrder;
				sensitivityMatrix.addValue(lowerFixingFirstOrder, lowerFixingSecondOrder, sensitivity.mult(1 - weightSecondOrder));
				sensitivityMatrix.addValue(lowerFixingFirstOrder, upperFixingSecondOrder, sensitivity.mult(weightSecondOrder));
			}
		} else {
			// Case 2: the sensitivity is assigned to two rows
			double weightFirstOrder = (maturityFirstOrder - lowerFixingFirstOrder) / distanceFirstOrder;
			// Determine the weighting of the columns
			if (distanceSecondOrder == 0) {
				// Case: Sensitivity is only assigned to one column but weighted across rows
				// If the second order fixing is smaller than the lower row we need to change row and column to maintain upper triangular form
				if (lowerFixingSecondOrder < upperFixingFirstOrder) {
					sensitivityMatrix.addValue(lowerFixingSecondOrder, upperFixingFirstOrder, sensitivity.mult(weightFirstOrder));
				} else {
					sensitivityMatrix.addValue(lowerFixingFirstOrder, lowerFixingSecondOrder, sensitivity.mult(1 - weightFirstOrder));
					sensitivityMatrix.addValue(upperFixingFirstOrder, lowerFixingSecondOrder, sensitivity.mult(weightFirstOrder));
				}
			} else {
				// Case: Sensitivity is weighted across rows and across columns
				double weightSecondOrder = (maturitySecondOrder - lowerFixingSecondOrder) / distanceSecondOrder;
				// the lower row and lower column entry are normally assigned
				sensitivityMatrix.addValue(lowerFixingFirstOrder, lowerFixingSecondOrder, sensitivity.mult((1 - weightFirstOrder) * (1 - weightSecondOrder)));
				// also the upper row and upper column entry are normally assigned
				sensitivityMatrix.addValue(upperFixingFirstOrder, upperFixingSecondOrder, sensitivity.mult(weightFirstOrder * weightSecondOrder));
				// if the first and second order maturity is equal we only assign to the lower row and upper column to maintain the upper triangular matrix
				if (maturityFirstOrder == maturitySecondOrder) {
					sensitivityMatrix.addValue(lowerFixingFirstOrder, upperFixingSecondOrder, sensitivity.mult(((1 - weightFirstOrder) * weightSecondOrder) + (weightFirstOrder * (1 - weightSecondOrder))));
				} else {
					sensitivityMatrix.addValue(upperFixingFirstOrder, lowerFixingSecondOrder, sensitivity.mult(weightFirstOrder * (1 - weightSecondOrder)));
					sensitivityMatrix.addValue(lowerFixingFirstOrder, upperFixingSecondOrder, sensitivity.mult((1 - weightFirstOrder) * weightSecondOrder));
				}
			}
		}
	}
	
	
	protected void mapDeltaToMatrix(SensitivityMatrix sensitivityMatrix, RandomVariable sensitivity, double maturity) {	
		// If maturity is smaller or greater than smallest or greatest fixing then lower = upper 
		double lowerFixing = tenorGrid.getNearestSmallerFixing(maturity);
		double upperFixing = tenorGrid.getNearestGreaterFixing(maturity);
		double distance = upperFixing - lowerFixing;
		// if lower and upper fixings are the same then the distance is zero
		// the distance is zero either because both are smaller than the smallest or greater than the greatest fixing or maturity aligns exactly with the bucket	
		if (distance == 0) {
			sensitivityMatrix.addValue(lowerFixing, lowerFixing, sensitivity);
		} else {
		// allocate the sensitivities on the diagonal entries
			double weight = (maturity - lowerFixing) / distance;
			sensitivityMatrix.addValue(lowerFixing, lowerFixing, sensitivity.mult(1 - weight));
			sensitivityMatrix.addValue(upperFixing, upperFixing, sensitivity.mult(weight));
		}
	}

	
}
