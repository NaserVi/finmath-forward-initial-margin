package net.finmath.lch.initialmargin.swapclear.sensitivities;

import java.util.Map;

import net.finmath.lch.initialmargin.swapclear.sensitivities.DiscountFactor.DiscountDate;
import net.finmath.stochastic.RandomVariable;


/**
 * Class to store all components that are needed for the calculation of swap sensitivities w.r.t. the discount factors.
 */
public class SensitivityComponent {
	
	private Map<DiscountDate, DiscountFactor> 									discountFactors;
	private RandomVariable														rate;
	private double																dayCountFraction;
	private RandomVariable 														notional;
	private boolean																payer;

	
	public SensitivityComponent(Map<DiscountDate, DiscountFactor> discountFactors, RandomVariable rate, double dayCountFraction, RandomVariable notional, boolean payer) {
		this.discountFactors = discountFactors;
		this.rate = rate;
		this.dayCountFraction = dayCountFraction;
		this.notional = notional;
		this.payer = payer;
	}
	
	
	public DiscountFactor getDiscountFactor(DiscountDate discountDate) {
		return discountFactors.get(discountDate);
	}
	
	
	public RandomVariable getRate() {
		return rate;
	}
	
	
	public double getDayCountFraction() {
		return dayCountFraction;
	}
	
	
	public RandomVariable getNotional() {
		return notional;
	}
	
	
	public boolean isPayer() {
		return payer;
	}
	
	
}
