package net.finmath.lch.initialmargin.simulation.ratetransformations;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;

import net.finmath.exception.CalculationException;
import net.finmath.stochastic.RandomVariable;



/**
 * A transformation method to calculate absolute and relative returns based on a return lag of a RandomVariable time series.
 * 
 * @author Raphael Prandtl
 */
public class RateTransformationReturns extends AbstractRateTransformation {
	
    public enum ReturnType {
        ABSOLUTE,
        RELATIVE
    }
	
	private final int 					marginPeriodOfRisk;
	private final ReturnType 			returnType;
	
	
	/**
	 * Performs a return transformation.
	 * The length (number of entries) of the initial data set is reduced by the return lag.
	 * 
	 * @param returnLag The lag between the data points.
	 * @param returnType Enum to calculate either absolute or relative returns w.r.t the lag.
	 */
	public RateTransformationReturns(final int marginPeriodOfRisk, final ReturnType returnType) {
		super("Returns"); 		// unique TransformationMethod ID
		this.marginPeriodOfRisk = marginPeriodOfRisk;
		this.returnType = returnType;
	}
	
	@Override
	public void applyTransformation(TransformationContext context) throws CalculationException {
		NavigableMap<LocalDateTime, RandomVariable> primaryData = context.getPrimaryData();
		if (primaryData.size() <= marginPeriodOfRisk) {
			throw new CalculationException("Not enough data points to apply return transformation. Reduce lag or increase data series.");
		}
		NavigableMap<LocalDateTime, RandomVariable> returnData = new TreeMap<>();
		List<LocalDateTime> dates = new ArrayList<>(primaryData.keySet());
		for (int i = 0; i < dates.size() - marginPeriodOfRisk; i++) {
			// The marginPeriodOfRisk is forward looking and determines the rate shift (return) that can happen over this time horizon
			// A scenario is the rate in x-days minus the current rate
			LocalDateTime scenarioDate = dates.get(i);
			LocalDateTime mprDate = dates.get(i + marginPeriodOfRisk);
			RandomVariable scenarioRate = primaryData.get(scenarioDate);
			RandomVariable mprRate = primaryData.get(mprDate);
			
			switch (returnType) {
			case ABSOLUTE:
				RandomVariable resultAbsolute = mprRate.sub(scenarioRate);
				returnData.put(scenarioDate, resultAbsolute);
				break;
			case RELATIVE: // case rate 0 missing
				RandomVariable resultRelative = mprRate.div(scenarioRate).sub(1.0);
				returnData.put(scenarioDate, resultRelative);
				break;
			}
		}
	    context.addTransformationResult(this.getId(), returnData);
	    context.setPrimaryData(returnData);
	}	
	
}
