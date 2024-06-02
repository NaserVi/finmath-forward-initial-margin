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
	
	
	// Map sensitivities to the provided tenor grid
	protected void mapSensitivityToMatrix(SensitivityMatrix sensitivityMatrix, RandomVariable sensitivity, double maturityFirstOrder, double maturitySecondOrder) {	
		// Possible fixing points
		// If maturity is smaller or greater than smallest or greatest fixing then lower = upper 
		double lowerFixingFirstOrder = tenorGrid.getNearestSmallerFixing(maturityFirstOrder);
		double upperFixingFirstOrder = tenorGrid.getNearestGreaterFixing(maturityFirstOrder);
		double lowerFixingSecondOrder = tenorGrid.getNearestSmallerFixing(maturitySecondOrder);
		double upperFixingSecondOrder = tenorGrid.getNearestGreaterFixing(maturitySecondOrder);

		double distanceFirstOrder = upperFixingFirstOrder - lowerFixingFirstOrder;
		double distanceSecondOrder = upperFixingSecondOrder - lowerFixingSecondOrder;
		
		// if lower and upper fixings are the same then the distance is zero
		// both distances are zero -> allocate complete sensitivity to one point
		if (distanceFirstOrder == 0 && distanceSecondOrder == 0) {
			sensitivityMatrix.addValue(lowerFixingFirstOrder, lowerFixingSecondOrder, sensitivity);
		// if first order distance is zero -> allocate only between second order points
		} else if (distanceFirstOrder == 0) {
			double weight = (maturitySecondOrder - lowerFixingSecondOrder) / distanceSecondOrder;
			sensitivityMatrix.addValue(lowerFixingFirstOrder, lowerFixingSecondOrder, sensitivity.mult(1 - weight));
			sensitivityMatrix.addValue(lowerFixingFirstOrder, upperFixingSecondOrder, sensitivity.mult(weight));
		// if second order distance is zero -> allocate only between first order points
		} else if (distanceSecondOrder == 0) {
			double weight = (maturityFirstOrder - lowerFixingFirstOrder) / distanceFirstOrder;
			sensitivityMatrix.addValue(lowerFixingFirstOrder, lowerFixingSecondOrder, sensitivity.mult(1 - weight));
			sensitivityMatrix.addValue(upperFixingFirstOrder, lowerFixingSecondOrder, sensitivity.mult(weight));
		// else allocate sensitivity to the nearest four points of the tenor grid
		} else {
			double weightFirstOrder = (maturityFirstOrder - lowerFixingFirstOrder) / distanceFirstOrder;
			double weightSecondOrder = (maturitySecondOrder - lowerFixingSecondOrder) / distanceSecondOrder;
			// Allocate sensitivities
			sensitivityMatrix.addValue(lowerFixingFirstOrder, lowerFixingSecondOrder, sensitivity.mult((1 - weightFirstOrder)  * (1 - weightSecondOrder)));
			sensitivityMatrix.addValue(lowerFixingFirstOrder, upperFixingSecondOrder, sensitivity.mult((1 - weightFirstOrder) * weightSecondOrder));
			sensitivityMatrix.addValue(upperFixingFirstOrder, lowerFixingSecondOrder, sensitivity.mult(weightFirstOrder * (1 - weightSecondOrder)));
			sensitivityMatrix.addValue(upperFixingFirstOrder, upperFixingSecondOrder, sensitivity.mult(weightFirstOrder * weightSecondOrder));
		}
	}
	
}
