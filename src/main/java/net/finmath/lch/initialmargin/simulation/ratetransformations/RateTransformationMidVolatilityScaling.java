package net.finmath.lch.initialmargin.simulation.ratetransformations;


import java.time.LocalDateTime;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import net.finmath.exception.CalculationException;
import net.finmath.stochastic.RandomVariable;



/**
 * Transformation method to perform a mid volatility scaling r^*_t = r_t/2 * (1 + vol_N/vol_t).
 * Used by LCH in order to apply a historical scenario to today's market environment.
 * Returns are scaled to reflect halfway (i.e. the mid-point) between the current volatility and historic. 
 * 
 * @author Raphael Prandtl
 */
public class RateTransformationMidVolatilityScaling extends AbstractRateTransformation {
	
    private final String 						transformationId; 
	
    
    /**
     * Performs a mid volatility scaling transformation.
     * The primary data set is assumed to be the dispersion data and the one the must be provided the return series.
     * 
     * @param transformationId The transformation ID of the return data set.
     */
	public RateTransformationMidVolatilityScaling(final String transformationId) {
		super("MidVolatilityScaling");    // unique TransformationMethod ID
		this.transformationId = transformationId;
	}
	
	
	@Override
	public void applyTransformation(TransformationContext context) throws CalculationException {
		NavigableMap<LocalDateTime, RandomVariable> primaryData = context.getPrimaryData();
		NavigableMap<LocalDateTime, RandomVariable> additionalData = context.getTransformationResult(transformationId);
	    if (additionalData == null) {
	        throw new CalculationException("Additional data set for transformation not found.");
	    }
			
		NavigableMap<LocalDateTime, RandomVariable> midVolatilityData = new TreeMap<>();
	    RandomVariable latestDispersion = primaryData.lastEntry().getValue();
	    for (Map.Entry<LocalDateTime, RandomVariable> entry : primaryData.entrySet()) {
	    	LocalDateTime currentDate = entry.getKey();
	    	RandomVariable currentReturn = additionalData.get(currentDate);
	    	RandomVariable currentDispersion = entry.getValue();
			RandomVariable midVolatilityScaling = currentReturn.mult(0.5).mult(latestDispersion.div(currentDispersion).add(1.0));
			midVolatilityData.put(currentDate, midVolatilityScaling);
		}
	    context.addTransformationResult(this.getId(), midVolatilityData);
	    context.setPrimaryData(midVolatilityData);
	}


}


