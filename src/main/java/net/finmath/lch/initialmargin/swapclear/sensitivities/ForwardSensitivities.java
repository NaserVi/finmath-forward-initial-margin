package net.finmath.lch.initialmargin.swapclear.sensitivities;


import net.finmath.lch.initialmargin.simulation.modeldata.StochasticCurve;
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
	protected void calculateDeltaSensitivity(StochasticCurve sensitivityCurve, SensitivityComponent component) {
		calculateSensitivity(sensitivityCurve, component, 1);
	}


	@Override
	protected void calculateGammaSensitivity(StochasticCurve sensitivityCurve, SensitivityComponent component) {
		calculateSensitivity(sensitivityCurve, component, 2);
	}
	
	
	private void calculateSensitivity(StochasticCurve sensitivityCurve, SensitivityComponent component, int derivativeOrder) {
		// if the LIBOR is already fixed no sensitivity -> no discount factors for period start and end date
    	if (component.getDiscountFactor(DiscountDate.PERIOD_START) == null) {
    		return;
    	}
    	// Filter discount factors for selected discounting method and payment date
		DiscountFactor discountFactorPayment 		= component.getDiscountFactor(DiscountDate.PAYMENT);
    	DiscountFactor discountFactorPeriodStart 	= component.getDiscountFactor(DiscountDate.PERIOD_START);
    	DiscountFactor discountFactorPeriodEnd 		= component.getDiscountFactor(DiscountDate.PERIOD_END);
		// get constant factors plus the discount factor for the payment date of the period
    	RandomVariable coefficient = getConstantCoefficient(component.getNotional(), component.getDayCountFraction(), component.isPayer());
    	coefficient = coefficient.mult(discountFactorPayment.getDiscountFactor());
    	// need to calculate two sensitivities, one for the period start bond and one for the period end bond
    	// we need the additional day count fraction between the period start and period end
    	double dayCountFraction = discountFactorPeriodEnd.getTimeToMaturity() - discountFactorPeriodStart.getTimeToMaturity();
    	RandomVariable sensitivityPeriodStart = discountFactorPeriodStart.getDerivative(derivativeOrder).mult(Math.pow(ZERO_RATE_SHIFT, derivativeOrder)).div(discountFactorPeriodEnd.getDiscountFactor()).div(dayCountFraction).mult(coefficient);
    	RandomVariable sensitivityPeriodEnd = discountFactorPeriodStart.getDiscountFactor().mult(discountFactorPeriodEnd.getInverseDerivative(derivativeOrder).mult(Math.pow(ZERO_RATE_SHIFT, derivativeOrder))).div(dayCountFraction).mult(coefficient);

		mapSensitivityToCurve(sensitivityCurve, sensitivityPeriodStart, discountFactorPeriodStart.getTimeToMaturity());
		mapSensitivityToCurve(sensitivityCurve, sensitivityPeriodEnd, discountFactorPeriodEnd.getTimeToMaturity());
	}
	
   
}
