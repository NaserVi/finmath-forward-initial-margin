package net.finmath.lch.initialmargin.swapclear.sensitivities;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.finmath.lch.initialmargin.simulation.modeldata.StochasticCurve;
import net.finmath.lch.initialmargin.simulation.modeldata.TenorGrid;
import net.finmath.exception.CalculationException;
import net.finmath.stochastic.RandomVariable;
import net.finmath.stochastic.Scalar;


/**
 * Implementation of {@link lch.initialmargin.swapclear.sensitivities.Sensitivities}
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
	public StochasticCurve getDeltaSensitivities(LocalDateTime evaluationDate) throws CalculationException {
		return calculateSensitivityCurve(evaluationDate, Derivative.DELTA);
	}

	
	@Override
	public StochasticCurve getGammaSensitivities(LocalDateTime evaluationDate) throws CalculationException {
		return calculateSensitivityCurve(evaluationDate, Derivative.GAMMA);
	}
	
	
	// Here the sensitivity calculation is assigned to the type and the actual culculation is happening in the implementations, 
	protected StochasticCurve calculateSensitivityCurve(LocalDateTime evaluationDate, Derivative derivative) throws CalculationException {
	    StochasticCurve sensitivityCurve = new StochasticCurve();
	    List<SensitivityComponent> components = sensitivityComponentsForSwapLeg.getSensitivityComponents(evaluationDate);
	    for (SensitivityComponent component : components) {
	    	switch (derivative) {
			case DELTA:
				calculateDeltaSensitivity(sensitivityCurve, component);
				break;
			case GAMMA:
				calculateGammaSensitivity(sensitivityCurve, component);;
				break;
			default:
				throw new IllegalArgumentException("Derivative type not supported.");
			} 
	    }
	    return sensitivityCurve;
	}


	protected abstract void calculateDeltaSensitivity(StochasticCurve sensitivityCurve, SensitivityComponent component);

	
	protected abstract void calculateGammaSensitivity(StochasticCurve sensitivityCurve, SensitivityComponent component);


	// Returns the zero rate independent swap components as a factor
	protected RandomVariable getConstantCoefficient(RandomVariable notional, double dayCountFraction, boolean isPayer) {
		RandomVariable coefficient = notional.mult(dayCountFraction);
		if (isPayer) {
			coefficient = coefficient.mult(-1.0);
		}
		return coefficient;
	}
	

	// Mapps the sensitvities to the provided tenor grid
	protected void mapSensitivityToCurve(StochasticCurve sensitivityCurve, RandomVariable sensitivity, double timeToMaturity) {
		ArrayList<Double> tenorFixings = tenorGrid.getFixings(); 
		
		int tenorIndex = Collections.binarySearch(tenorFixings, timeToMaturity);
		if (tenorIndex < 0) { // No exact match on grid
			int upperTenorIndex 	= -tenorIndex - 1;
			if (upperTenorIndex >= tenorFixings.size()) {
				// Assign whole sensitivity to last pillar point if timeToMaturity > last fixing
				double upperTenorFixing = tenorFixings.get(tenorFixings.size() - 1);
				addSensitivity(sensitivityCurve, upperTenorFixing, sensitivity);
				return;
			}
			double upperTenorFixing = tenorFixings.get(upperTenorIndex);
			if (upperTenorIndex == 0) {
				// Assign whole sensitivity to first pillar point if timeToMaturity < first fixing
				addSensitivity(sensitivityCurve, upperTenorFixing, sensitivity);
				return;
			}
			// Linearly assign sensitivity to surrounding pillar points
			int lowerTenorIndex 	= upperTenorIndex - 1;
			double lowerTenorFixing = tenorFixings.get(lowerTenorIndex);
				
			double upperWeighting 	= (double) (timeToMaturity - lowerTenorFixing)/(upperTenorFixing - lowerTenorFixing);
			double lowerWeighting 	= 1 - upperWeighting;	
			addSensitivity(sensitivityCurve, upperTenorFixing, sensitivity.mult(upperWeighting));
			addSensitivity(sensitivityCurve, lowerTenorFixing, sensitivity.mult(lowerWeighting));
			
		} else {
			// Exact match on tenor grid
			double tenorFixing = tenorFixings.get(tenorIndex);
			addSensitivity(sensitivityCurve, tenorFixing, sensitivity);
		}
	}

	
	private void addSensitivity(StochasticCurve sensitivityCurve, double fixing, RandomVariable sensitivity) {
		RandomVariable currentSensitivity = sensitivityCurve.getCurve().getOrDefault(fixing, new Scalar(0.0));
		sensitivityCurve.addRate(fixing, currentSensitivity.add(sensitivity));
	}
	
	
}
