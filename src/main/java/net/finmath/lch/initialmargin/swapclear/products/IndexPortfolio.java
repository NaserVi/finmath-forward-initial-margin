package net.finmath.lch.initialmargin.swapclear.products;


import java.util.ArrayList;
import java.util.List;

import net.finmath.lch.initialmargin.swapclear.aggregationscheme.SchemeStorage.Currency;
import net.finmath.lch.initialmargin.swapclear.aggregationscheme.SchemeStorage.CurveName;
import net.finmath.lch.initialmargin.swapclear.products.components.LchSwapLeg;


/**
 * The IndexPortfolio stores swaps with the same underlying (curve) and the same currency.
 */
public class IndexPortfolio {
	
	private List<LchSwapLeg>									indexPortfolio;
	private final Currency 										currency;
	private final CurveName										curveName;
	
	

	public IndexPortfolio(Currency currency, CurveName curveName) {
		this.indexPortfolio = new ArrayList<>();
		this.currency = currency;
		this.curveName = curveName;
	}

	
	public void addSwap(LchSwap swap) {
    	if (currency.equals(swap.getLchCurrency()) && curveName.equals(swap.getCurveName())) {
    		indexPortfolio.add(swap.getLegPayer());
    		indexPortfolio.add(swap.getLegReceiver());
    	}
	}
	
	
	public List<LchSwapLeg> getSwaps() {
		return indexPortfolio;
	}
	
	
	/**
	 * @return The latest payment date of the portfolio
	 */
	public double getLastPaymentDate() {
		double lastPaymentDate = 0.0;
		for (LchSwapLeg leg : indexPortfolio) {
			lastPaymentDate = Math.max(lastPaymentDate, leg.getLastPaymentDate());
		}
		return lastPaymentDate;
	}
	
	
	public Currency getCurrency() {
		return currency;
	}
	
	
	public CurveName getCurveName() {
		return curveName;
	}
	
	
}
