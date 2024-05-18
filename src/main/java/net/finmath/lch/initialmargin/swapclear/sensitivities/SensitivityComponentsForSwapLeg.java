package net.finmath.lch.initialmargin.swapclear.sensitivities;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.finmath.lch.initialmargin.simulation.modeldata.ZeroRateModel;
import net.finmath.lch.initialmargin.swapclear.products.components.LchPeriod;
import net.finmath.lch.initialmargin.swapclear.products.components.LchProductCollection;
import net.finmath.lch.initialmargin.swapclear.products.components.LchSwapLeg;
import net.finmath.lch.initialmargin.swapclear.sensitivities.DiscountFactor.DiscountDate;
import net.finmath.exception.CalculationException;
import net.finmath.montecarlo.interestrate.products.components.AbstractProductComponent;
import net.finmath.stochastic.RandomVariable;
import net.finmath.time.FloatingpointDate;


/**
 * Collects the sensitivity components of swap legs, i.e., for each period the components are stored as a SensitivityComponent
 */
public class SensitivityComponentsForSwapLeg {
	
	private final List<LchSwapLeg>						swapLegs;
	private final ZeroRateModel 						forwardCurveModel;
	private final ZeroRateModel 						discountCurveModel;

	private List<SensitivityComponent> 					sensitivityComponents;
	private LocalDateTime								lastUsedEvaluationDate = null;
    private final Object 								sensitivityComponentsLock = new Object();

    
	public SensitivityComponentsForSwapLeg(final List<LchSwapLeg> swapLegs, final ZeroRateModel forwardCurveModel, final ZeroRateModel discountCurveModel) {
		this.swapLegs = swapLegs;
		this.forwardCurveModel = forwardCurveModel;
		this.discountCurveModel = discountCurveModel;
	}
	
	
	/**
	 * Calculates the discount factors and stores them together with their maturity and coefficients
	 * Discounting.LIBOR for single curve setup -> discount factors and LIBOR bonds are all derived from the same curve
	 * 
	 * @param evaluationTime
	 * @throws CalculationException
	 */
	public List<SensitivityComponent> getSensitivityComponents(LocalDateTime evaluationDate) throws CalculationException {
		synchronized (sensitivityComponentsLock) {
			if (lastUsedEvaluationDate == null || !evaluationDate.equals(lastUsedEvaluationDate)) {
				doCalculateSensitivityComponents(evaluationDate);
			} 
		}
		return sensitivityComponents;
	}
	
	
	private void doCalculateSensitivityComponents(LocalDateTime evaluationDate) throws CalculationException {		
		sensitivityComponents = new ArrayList<>();
		// Product components (Period) of the swap leg -> times, index, etc.
		for (LchSwapLeg leg : swapLegs) {
			double scheduleOffSet = FloatingpointDate.getFloatingPointDateFromDate(leg.getReferenceDate(), evaluationDate);
			LchProductCollection swapComponents = leg.getComponents();
			// TODO: double casting correction
			for (AbstractProductComponent component : swapComponents.getProducts()) {
				LchPeriod period = (LchPeriod) component;
				// without coupon flow no payment -> no sensitivity
				if (!period.isCouponFlow()) {
					continue;
				}
				// if payment date <= evaluation time -> no sensitivity 
				double paymentDate = period.getPaymentDate();
				if (paymentDate <= scheduleOffSet) { 
					continue;
				}
				// collect all components of a swap period necessary for the sensitivity calculation
				LocalDateTime fixingDate = FloatingpointDate.getDateFromFloatingPointDate(leg.getReferenceDate(), period.getFixingDate());
				// Rate for sensitivity calculation -> needed under current (not-shifted) curve and for all paths
				RandomVariable rate = period.getLchIndex().getValue(evaluationDate, fixingDate, forwardCurveModel, null, null); 
				RandomVariable notional = period.getNotional().getConstantNotional();
				double dayCountFraction = period.getDaycountFraction();
				boolean isPayerLeg = period.isPayer();
				// calculate all necessary discount factors for the period
				Map<DiscountDate, DiscountFactor> discountFactors = getDiscountFactors(evaluationDate, scheduleOffSet, period);
				
				SensitivityComponent sensitivityComponent = new SensitivityComponent(discountFactors, rate, dayCountFraction, notional, isPayerLeg);
				sensitivityComponents.add(sensitivityComponent);
			}
		}
		lastUsedEvaluationDate = evaluationDate;
	}	
	
	
	private Map<DiscountDate, DiscountFactor> getDiscountFactors(LocalDateTime evaluationDate, double scheduleOffSet, LchPeriod period) throws CalculationException {
		Map<DiscountDate, DiscountFactor> discountFactors = new HashMap<>();
		double paymentTime = period.getPaymentDate();
		// for all indices we need discount factors w.r.t. the payment dates of the cash flows
		// Either with LIBOR discounting in a single curve set up or OIS discounting in a multi-curve set up
		if (forwardCurveModel.equals(discountCurveModel)) {
			discountFactors.put(DiscountDate.PAYMENT, getDiscountFactor(evaluationDate, paymentTime - scheduleOffSet, forwardCurveModel));
		} else {
			discountFactors.put(DiscountDate.PAYMENT, getDiscountFactor(evaluationDate, paymentTime - scheduleOffSet, discountCurveModel));
		}
		// for float index payments we need additionally discount factors w.r.t. the period start (fixing + periodStartOffSet) and period end (fixing + periodStartOffSet + period length) 
		// only indices that pay a floating rate do have a periodLength > 0
		double periodLength = period.getLchIndex().getPeriodLength();
		if (periodLength > 0) {
			// If the floating rate is already fixed it does not contribute discount factors (zero rates) to the sensitivities
			double fixingTime = period.getFixingDate();
			if (scheduleOffSet < fixingTime) { // EOD valuation -> for "=" already fixed
				double periodStartOffSet =  period.getLchIndex().getPeriodStartOffset();
				// We need the floating rate discount factors only for LIBOR discounting 
				discountFactors.put(DiscountDate.PERIOD_START, getDiscountFactor(evaluationDate, (fixingTime + periodStartOffSet) - scheduleOffSet, forwardCurveModel));
				// the additional discount factor for the period end (fixing + offset + period length) 
				discountFactors.put(DiscountDate.PERIOD_END, getDiscountFactor(evaluationDate, (fixingTime + periodStartOffSet + periodLength) - scheduleOffSet, forwardCurveModel));
			}
		}
		return discountFactors;
	}
	
	
	private DiscountFactor getDiscountFactor(LocalDateTime evaulationDate, double maturity, ZeroRateModel curveModel) throws CalculationException {
		// Discount factor for sensitivity calculation -> needed under current (not-shifted) curve and for all paths
		RandomVariable liborBond = curveModel.getDiscountFactor(evaulationDate, maturity, null, null); 
		DiscountFactor discountFactor = new DiscountFactor(liborBond, maturity);
		return discountFactor;
	}


}
