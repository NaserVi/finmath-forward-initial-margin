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
    	// Filter discount factors for selected discounting method and payment date
		DiscountFactor discountFactorPayment 		= component.getDiscountFactor(DiscountDate.PAYMENT);
    	DiscountFactor discountFactorPeriodStart 	= component.getDiscountFactor(DiscountDate.PERIOD_START);
    	DiscountFactor discountFactorPeriodEnd 		= component.getDiscountFactor(DiscountDate.PERIOD_END);
		// get constant factors plus the discount factor for the payment date of the period
    	RandomVariable coefficient = getConstantCoefficient(component.getNotional(), component.getDayCountFraction(), component.isPayer());
    	coefficient = coefficient.mult(discountFactorPayment.getDiscountFactor());
    	// we need the additional day count fraction between the period start and period end
    	double dayCountFraction = discountFactorPeriodEnd.getTimeToMaturity() - discountFactorPeriodStart.getTimeToMaturity();
    	// independent of derivative order one sensitivity for the period start bond and one for the period end bond
        RandomVariable sensitivityPeriodStart = discountFactorPeriodStart.getDerivative(derivativeOrder).mult(Math.pow(ZERO_RATE_SHIFT, derivativeOrder)).div(discountFactorPeriodEnd.getDiscountFactor()).div(dayCountFraction).mult(coefficient);
        RandomVariable sensitivityPeriodEnd = discountFactorPeriodStart.getDiscountFactor().mult(discountFactorPeriodEnd.getInverseDerivative(derivativeOrder).mult(Math.pow(ZERO_RATE_SHIFT, derivativeOrder))).div(dayCountFraction).mult(coefficient);
		mapSensitivityToMatrix(sensitivityMatrix, sensitivityPeriodStart, discountFactorPeriodStart.getTimeToMaturity(), discountFactorPeriodStart.getTimeToMaturity());
		mapSensitivityToMatrix(sensitivityMatrix, sensitivityPeriodEnd, discountFactorPeriodEnd.getTimeToMaturity(), discountFactorPeriodEnd.getTimeToMaturity());
		// one additional sensitivity for the cross-gamma case
		if (derivativeOrder == 2) {
			RandomVariable crossGammaSensitivity = discountFactorPeriodStart.getDerivative(1).mult(discountFactorPeriodEnd.getInverseDerivative(1)).mult(Math.pow(ZERO_RATE_SHIFT, 2)).div(dayCountFraction).mult(coefficient);
			// We construct an upper triangle matrix since the derivative order doesn't matter and thus gamma matrix is symmetric -> add sensitivity twice to one maturity combination
			mapSensitivityToMatrix(sensitivityMatrix, crossGammaSensitivity.mult(2), discountFactorPeriodStart.getTimeToMaturity(), discountFactorPeriodEnd.getTimeToMaturity());
		}
	}
	
   
}
