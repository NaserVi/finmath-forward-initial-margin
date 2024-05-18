package net.finmath.lch.initialmargin.simulation.modeldata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A unmodifiable collection of sorted curve fixing points with their corresponding names, e.g., "1w", "1m",...
 * Used for tenor point sampling of interest rate risk and scenario creation.
 * For implementation examples following the LCH methodology check {@link lch.initialmargin.simulation.TenorGridFactory}.
 * 
 * @author Raphael Prandtl
 *
 */
public class TenorGrid {
	
	private final Map<Double, String>					fixedGrid;
	
	
	public TenorGrid(LinkedHashMap<Double, String> fixedGrid) {
        this.fixedGrid = Collections.unmodifiableMap(new LinkedHashMap<>(fixedGrid));
	}
	
	
	/**
	 * @return The fixings of the tenor grid in increasing order
	 */
	public ArrayList<Double> getFixings() {
	    return new ArrayList<>(fixedGrid.keySet());
	}
	
	
	/**
	 * @return The names of the tenor point fixings
	 */
	public ArrayList<String> getTenors() {
	    return new ArrayList<>(fixedGrid.values());
	}
	
	
	/**
	 * @param fixingDay The fixing of the tenor point
	 * @return The name of the tenor point if the fixing day is available, null otherwise
	 */
    public String getTenor(double fixingDay) {
        return fixedGrid.get(fixingDay);
    }

    
    /**
     * @param tenor The name of the tenor point
     * @return True if the tenor is part of the tenor grid
     */
    public boolean containsTenor(String tenor) {
        return fixedGrid.containsValue(tenor);
    }

	
}
