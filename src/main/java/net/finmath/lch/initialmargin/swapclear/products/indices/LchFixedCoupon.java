package net.finmath.lch.initialmargin.swapclear.products.indices;

import java.time.LocalDateTime;

/*
 * Created on 06.12.2009
 *
 * (c) Copyright Christian P. Fries, Germany. Contact: email@christian-fries.de.
 */

import java.util.Set;

import net.finmath.lch.initialmargin.simulation.modeldata.StochasticCurve;
import net.finmath.lch.initialmargin.simulation.modeldata.ZeroRateModel;
import net.finmath.exception.CalculationException;
import net.finmath.montecarlo.RandomVariableFromDoubleArray;
import net.finmath.montecarlo.interestrate.TermStructureMonteCarloSimulationModel;
import net.finmath.stochastic.RandomVariable;

/**
 * A fixed coupon index paying constant coupon..
 *
 * @author Christian Fries
 * @version 1.0
 */
public class LchFixedCoupon extends LchAbstractIndex {

	private static final long serialVersionUID = 5375406324063846793L;

	private final RandomVariable coupon;

	/**
	 * Creates a fixed coupon index paying constant coupon.
	 *
	 * @param coupon The coupon.
	 */
	public LchFixedCoupon(final double coupon) {
		super();
		this.coupon = new RandomVariableFromDoubleArray(coupon);
	}

	@Override
	public RandomVariable getValue(final double evaluationTime, final TermStructureMonteCarloSimulationModel model) {
		return coupon;
	}

	
	@Override
	public RandomVariable getValue(LocalDateTime evaluationDate, LocalDateTime fixingDate, ZeroRateModel curveModel, StochasticCurve curveShifts, Integer pathOrState) throws CalculationException {
		return coupon;
	}
	
	
	@Override
	public double getPeriodStartOffset() {
		return 0.0;
	}

	
	@Override
	public double getPeriodLength() {
		return 0.0;
	}
	
	/**
	 * Returns the coupon.
	 *
	 * @return the coupon
	 */
	public RandomVariable getCoupon() {
		return coupon;
	}

	@Override
	public Set<String> queryUnderlyings() {
		return null;
	}

	@Override
	public String toString() {
		return "LchFixedCoupon [coupon=" + coupon + ", toString()="
				+ super.toString() + "]";
	}
}
