package net.finmath.lch.initialmargin.simulation.modeldata;

import java.util.LinkedHashMap;


/**
 * Implementations of the TenorGrid, used for sensitivity mapping and scenario sampling.
 * 
 * @author Raphael Raphael
 *
 */
public class TenorGridFactory {
		
	public enum GridType {INITIAL_MARGIN_RISK_GRID, INITIAL_MARGIN_SPREAD_GRID};
	
		
	// all fixings of the data sampling grids must be an element of the risk grid
    public static TenorGrid getTenorGrid(GridType gridType) {
        switch (gridType) {
        case INITIAL_MARGIN_RISK_GRID:
        	return createRiskCurve(
        			new double[]{1/365.0, 7/365.0, 14/365.0, 21/365.0, 30/365.0, 60/365.0, 91/365.0, 121/365.0, 152/365.0, 182/365.0, 
                				212/365.0, 243/365.0, 273/365.0, 304/365.0, 334/365.0, 365/365.0, 456/365.0, 547/365.0, 638/365.0, 730/365.0, 1095/365.0, 
                				1460/365.0, 1825/365.0, 2190/365.0, 2555/365.0, 2920/365.0, 3285/365.0, 3650/365.0, 4380/365.0, 5475/365.0, 7300/365.0, 
                				9125/365.0, 10950/365.0, 12775/365.0, 14600/365.0, 18250/365.0, 21900/365.0},
                	new String[]{"O/N", "1w", "2w", "3w", "1m", "2m", "3m", "4m", "5m", "6m", "7m", "8m", "9m", "10m", "11m", "1y", "15m", 
                			"18m", "21m", "2y", "3y", "4y", "5y", "6y", "7y", "8y", "9y", "10y", "12y", "15y", "20y", "25y", "30y", "35y", "40y", "50y", "60y"});
        case INITIAL_MARGIN_SPREAD_GRID:
            return createRiskCurve( 
            		new double[]{730/365.0, 1825/365.0, 3650/365.0, 10950/365.0},
            		new String[]{"2y", "5y", "10y", "30y"}); 
        default:
        	throw new IllegalArgumentException("Unsupported grid type: " + gridType);
        }
    }
    
    
    private static TenorGrid createRiskCurve(double[] fixings, String[] tenorPoints) {
        LinkedHashMap<Double, String> riskCurve = new LinkedHashMap<>();
        for (int i = 0; i < tenorPoints.length; i++) {
        	riskCurve.put(fixings[i], tenorPoints[i]);
        }
        return new TenorGrid(riskCurve);
    }
    
	
}
