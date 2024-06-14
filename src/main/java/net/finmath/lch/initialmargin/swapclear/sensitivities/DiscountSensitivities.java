package net.finmath.lch.initialmargin.swapclear.sensitivities;



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
	protected void calculateDeltaSensitivity(SensitivityMatrix sensitivityMatrix, SensitivityComponent component) {
    	calculateSensitivity(sensitivityMatrix, component, 1);
	}


	@Override
	protected void calculateGammaSensitivity(SensitivityMatrix sensitivityMatrix, SensitivityComponent component) {
    	calculateSensitivity(sensitivityMatrix, component, 2);
	}
	
	
	private void calculateSensitivity(SensitivityMatrix sensitivityMatrix, SensitivityComponent component, int derivativeOrder) {
    	// Filter discount factors for selected discounting method and payment date
		DiscountFactor discountFactor = component.getDiscountFactor(DiscountDate.PAYMENT);
		RandomVariable coefficient;
		// if no discount factor for period start -> fixed leg
		if (component.getDiscountFactor(DiscountDate.PERIOD_START) == null) {
			coefficient = getConstantCoefficient(component.getNotional(), component.getDayCountFraction(), component.isPayer()).mult(component.getRate());
		} else {
			coefficient = component.getNotional().mult(-1.0);
			if (component.isPayer()) {
				coefficient = coefficient.mult(-1.0);
			}
		}
		RandomVariable sensitivity = coefficient.mult(discountFactor.getDerivative(derivativeOrder).mult(Math.pow(ZERO_RATE_SHIFT, derivativeOrder)));
		// discount sensitivities do not have cross-gamma effects -> all mapped to the diagonal entries
		if(derivativeOrder == 1) {
			mapDeltaToMatrix(sensitivityMatrix, sensitivity, discountFactor.getTimeToMaturity());
		} else {
			mapGammaToMatrix(sensitivityMatrix, sensitivity, discountFactor.getTimeToMaturity(), discountFactor.getTimeToMaturity());
		}
	}
	

}
