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
	 * @param maturity The time for which the closest fixing point should be returned
	 * @return The nearest smaller fixing or the smallest fixing if maturity < smallest available fixing
	 */
    public double getNearestSmallerFixing(double maturity) {
		ArrayList<Double> fixings = getFixings();
		int tenorIndex = Collections.binarySearch(fixings, maturity);
		
		if (tenorIndex >= 0) { // Exact match on grid
			return fixings.get(tenorIndex);
		} else {
			int insertionPoint = -tenorIndex - 1;
			
			if (insertionPoint > 0) {
				return fixings.get(insertionPoint - 1);
			} else {
				// maturity is smaller than the smallest fixing
				return fixings.get(0);
			}
		}
    }

	/**
	 * @param maturity The time for which the closest fixing point should be returned
	 * @return The nearest greater fixing or the greatest fixing if maturity > greatest available fixing
	 */
	public double getNearestGreaterFixing(double maturity) {
		ArrayList<Double> fixings = getFixings();
		int tenorIndex = Collections.binarySearch(fixings, maturity);
		
		if (tenorIndex >= 0) { // Exact match on grid
			return fixings.get(tenorIndex);
		} else {
			int insertionPoint = -tenorIndex - 1;

			if (insertionPoint < fixings.size()) {
				return fixings.get(insertionPoint);
			} else {
				// maturity is greater than the greatest fixing
				return fixings.get(fixings.size() - 1);
			}
		}
	}
	
	
	public double getFixingSmallest() {
		ArrayList<Double> fixings = getFixings();
		return fixings.get(0);
	}
	
   	
	public double getFixingGreatest() {
		ArrayList<Double> fixings = getFixings();
		return fixings.get(fixings.size() - 1);
	}
	
	
    /**
     * @param tenor The name of the tenor point
     * @return True if the tenor is part of the tenor grid
     */
    public boolean containsTenor(String tenor) {
        return fixedGrid.containsValue(tenor);
    }

	
}
