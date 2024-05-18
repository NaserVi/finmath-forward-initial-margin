package net.finmath.lch.initialmargin.swapclear.products.components;

import net.finmath.exception.CalculationException;
import net.finmath.montecarlo.interestrate.TermStructureMonteCarloSimulationModel;
import net.finmath.stochastic.RandomVariable;

/**
 * Base class for notional classes.
 *
 * @author Christian Fries
 * @version 1.0
 */
public interface LchNotional {

	/**
	 * Returns the currency string of this notional.
	 *
	 * @return the currency
	 */
	String getCurrency();

	/**
	 * Calculates the notional at the start of a period, given a period.
	 * Example: The notional can be independent of the period (constant running notional) or depending on the period (accruing notional).
	 *
	 * @param period Period.
	 * @param model The model against we are evaluation.
	 * @return The notional for the given period as of period start.
	 * @throws net.finmath.exception.CalculationException Thrown if the valuation fails, specific cause may be available via the <code>cause()</code> method.
	 */
	RandomVariable getNotionalAtPeriodStart(LchAbstractPeriod period, TermStructureMonteCarloSimulationModel model) throws CalculationException;

	/**
	 * Calculates the notional at the end of a period, given a period.
	 * Example: The notional can be independent of the period (constant running notional) or depending on the period (accruing notional).
	 *
	 * @param period Period.
	 * @param model The model against we are evaluation.
	 * @return The notional for the given period as of period end.
	 * @throws net.finmath.exception.CalculationException Thrown if the valuation fails, specific cause may be available via the <code>cause()</code> method.
	 */
	RandomVariable getNotionalAtPeriodEnd(LchAbstractPeriod period, TermStructureMonteCarloSimulationModel model) throws CalculationException;
	
	
	// Added method for constant notional 
	RandomVariable getConstantNotional();

}
