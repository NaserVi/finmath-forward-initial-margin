package net.finmath.lch.initialmargin.swapclear.sensitivities;



import net.finmath.lch.initialmargin.simulation.modeldata.StochasticCurve;
import net.finmath.lch.initialmargin.simulation.modeldata.TenorGrid;
import net.finmath.lch.initialmargin.swapclear.sensitivities.DiscountFactor.DiscountDate;
import net.finmath.stochastic.RandomVariable;


/**
 * Calculates the sensitivities of a swap component w.r.t. the discount factor
 */
public class DiscountSensitivities extends AbstractSensitvities {
		
	
	public DiscountSensitivities(SensitivityComponentsForSwapLeg sensitivityComponentsForSwapLeg, TenorGrid tenorGrid) {
		super(sensitivityComponentsForSwapLeg, tenorGrid);
	}
	
	
	// Discount sensitivities are w.r.t the payment dates of the cash flows
	@Override
	protected void calculateDeltaSensitivity(StochasticCurve sensitivityCurve, SensitivityComponent component) {
    	calculateSensitivity(sensitivityCurve, component, 1);
	}


	@Override
	protected void calculateGammaSensitivity(StochasticCurve sensitivityCurve, SensitivityComponent component) {
    	calculateSensitivity(sensitivityCurve, component, 2);
	}
	
	
	private void calculateSensitivity(StochasticCurve sensitivityCurve, SensitivityComponent component, int derivativeOrder) {
    	// Filter discount factors for selected discounting method and payment date
		DiscountFactor discountFactor = component.getDiscountFactor(DiscountDate.PAYMENT);
		// get constant factors plus the time dependent rate of the period
    	RandomVariable coefficient = getConstantCoefficient(component.getNotional(), component.getDayCountFraction(), component.isPayer());
    	coefficient = coefficient.mult(component.getRate());
    	// multiply with derivative of the discount factor based on derivative order
		RandomVariable sensitivity = coefficient.mult(discountFactor.getDerivative(derivativeOrder).mult(Math.pow(ZERO_RATE_SHIFT, derivativeOrder)));
		mapSensitivityToCurve(sensitivityCurve, sensitivity, discountFactor.getTimeToMaturity());
	}
	

}
