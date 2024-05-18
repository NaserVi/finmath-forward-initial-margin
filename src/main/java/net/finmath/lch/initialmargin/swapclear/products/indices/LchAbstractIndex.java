package net.finmath.lch.initialmargin.swapclear.products.indices;
/*
 * Created on 03.09.2006
 *
 * (c) Copyright Christian P. Fries, Germany. Contact: email@christian-fries.de.
 */

import java.time.LocalDateTime;

import net.finmath.lch.initialmargin.simulation.modeldata.StochasticCurve;
import net.finmath.lch.initialmargin.simulation.modeldata.ZeroRateModel;
import net.finmath.exception.CalculationException;
import net.finmath.montecarlo.interestrate.TermStructureMonteCarloSimulationModel;
import net.finmath.montecarlo.interestrate.products.components.AbstractProductComponent;
import net.finmath.stochastic.RandomVariable;

/**
 * Base class for indices.
 *
 * Indices are small functions mapping time and a vector of
 * random variables to a random variable, where the time
 * is the fixing time of the index.
 *
 * @author Christian Fries
 * @version 1.0
 */
public abstract class LchAbstractIndex extends AbstractProductComponent {

	private static final long serialVersionUID = 7992943924779922710L;

	private final String name;

	/**
	 * Initialize name and currency of an index.
	 *
	 * @param name The name of an index. Used to map an index on a curve.
	 * @param currency The natural currency of an index. This more for compatibility purposes, since the information sould be contained in the name.
	 */
	public LchAbstractIndex(final String name, final String currency) {
		super(currency);
		this.name = name;
	}

	/**
	 * Initialize the name of an index.
	 *
	 * @param name The name of an index. Used to map an index on a curve.
	 */
	public LchAbstractIndex(final String name) {
		this(name, null);
	}

	/**
	 * Initialize an abstract index which does not have a dedicated name or currency,
	 * e.g. a function of other indicies.
	 */
	public LchAbstractIndex() {
		this(null, null);
	}

	@Override
	public abstract RandomVariable getValue(double fixingTime, TermStructureMonteCarloSimulationModel model) throws CalculationException;

	
	/**
	 * Added method to get the value of a swap leg under the shifted curve scenario
	 * 
	 * @param evaluationTime The observation time of the forward curve
	 * @param fixingTime The fixing time of the index w.r.t the observing time of the curve
	 * 
	 * @return The rate which is used for sensitivity calculations
	 */
	public abstract RandomVariable getValue(LocalDateTime evaluationDate, LocalDateTime fixingDate, ZeroRateModel curveModel, StochasticCurve curveShifts, Integer pathOrState) throws CalculationException;
	

	/**
	 * Added method to get the period length of the underlying index
	 * 
	 */
	public abstract double getPeriodLength();

	
	/**
	 * Added method to return the periodStartOffset as an act/365 daycount.
	 * TODO: Must be revised if number of indices increases (currently 2: Fixed and LIBOR)
	 * @return the periodStartOffset of the LIBORIndex
	 */
	public abstract double getPeriodStartOffset();
	
	/**
	 * Returns the name of the index.
	 *
	 * @return The name of the index.
	 */
	public String getName() {
		return name;
	}
}
