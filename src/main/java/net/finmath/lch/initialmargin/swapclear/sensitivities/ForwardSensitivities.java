package net.finmath.lch.initialmargin.swapclear.sensitivities;


import net.finmath.lch.initialmargin.simulation.modeldata.TenorGrid;
import net.finmath.lch.initialmargin.swapclear.sensitivities.DiscountFactor.DiscountDate;
import net.finmath.stochastic.RandomVariable;


/**
 * Calculates the sensitivities of a swap component w.r.t. the forward rate (the two discount factors entering into the rate)
 */
public class ForwardSensitivities extends AbstractSensitvities {
	
	
	public ForwardSensitivities(SensitivityComponentsForSwapLeg sensitivityComponentsForSwapLeg, TenorGrid tenorGrid) {
		super(sensitivityComponentsForSwapLeg, tenorGrid);
	}
	

	@Override
	protected void calculateDeltaSensitivity(SensitivityMatrix sensitivityMatrix, SensitivityComponent component) {
		calculateSensitivity(sensitivityMatrix, component, 1);
	}


	@Override
	protected void calculateGammaSensitivity(SensitivityMatrix sensitivityMatrix, SensitivityComponent component) {
		calculateSensitivity(sensitivityMatrix, component, 2);
	}
	
	
	private void calculateSensitivity(SensitivityMatrix sensitivityMatrix, SensitivityComponent component, int derivativeOrder) {
		// if the LIBOR is already fixed no sensitivity -> no discount factors for period start and end date
    	if (component.getDiscountFactor(DiscountDate.PERIOD_START) == null) {
    		return;
    	}
    	DiscountFactor discountFactorPeriodStart = component.getDiscountFactor(DiscountDate.PERIOD_START);
    	RandomVariable coefficient = component.getNotional();
    	if (component.isPayer()) {
    		coefficient = coefficient.mult(-1.0);
    	}
    	RandomVariable sensitivity = discountFactorPeriodStart.getDerivative(derivativeOrder).mult(Math.pow(ZERO_RATE_SHIFT, derivativeOrder)).mult(coefficient);
		if (derivativeOrder == 1) {
			mapDeltaToMatrix(sensitivityMatrix, sensitivity, discountFactorPeriodStart.getTimeToMaturity());
		} else {
	        mapGammaToMatrix(sensitivityMatrix, sensitivity, discountFactorPeriodStart.getTimeToMaturity(), discountFactorPeriodStart.getTimeToMaturity());
		}
	}
	
   
}
