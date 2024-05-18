package net.finmath.lch.initialmargin.swapclear.products;

import java.util.HashMap;
import java.util.Map;

import net.finmath.lch.initialmargin.swapclear.aggregationscheme.SchemeStorage.Currency;



/**
 * The GlobalPortfolio stores all swaps. Collection of LocalPortfolio.
 */
public class GlobalPortfolio {
	
    private Map<Currency, LocalPortfolio> 		globalPortfolio;

    
    public GlobalPortfolio() {
    	this.globalPortfolio = new HashMap<>();
    }
    
    
    public void addSwap(LchSwap swap) {
    	Currency currency = swap.getLchCurrency(); 
    	globalPortfolio.computeIfAbsent(currency, k -> new LocalPortfolio(currency)).addSwap(swap);
    }
    
    
    public Map<Currency, LocalPortfolio> getLocalPortfolios() {
    	return globalPortfolio;
    }
    
    
    public LocalPortfolio getLocalPortfolio(Currency currency) {
    	return globalPortfolio.get(currency);
    }
    
    
}
