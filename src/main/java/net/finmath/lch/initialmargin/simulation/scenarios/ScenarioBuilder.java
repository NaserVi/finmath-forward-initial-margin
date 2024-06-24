package net.finmath.lch.initialmargin.simulation.scenarios;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.NavigableMap;

import net.finmath.lch.initialmargin.simulation.modeldata.RandomVariableSeries;
import net.finmath.lch.initialmargin.simulation.modeldata.ZeroRateModel;
import net.finmath.lch.initialmargin.simulation.ratetransformations.RateTransformation;
import net.finmath.lch.initialmargin.simulation.ratetransformations.TransformationContext;
import net.finmath.exception.CalculationException;
import net.finmath.stochastic.RandomVariable;


public class ScenarioBuilder {


	private final ZeroRateModel						zeroRateModel;
    private final List<RateTransformation> 			rateTransformations; 
    
    
    public ScenarioBuilder(ZeroRateModel zeroRateModel, List<RateTransformation> rateTransformations) {
        this.zeroRateModel = zeroRateModel;
    	this.rateTransformations = rateTransformations;
    }
    

    // For moving scenario window with additional stress period
    public RandomVariableSeries getTenorPointScenarios(LocalDateTime evaluationDate, double tenorFixing, int numberOfMovingScenarios, LocalDateTime startDateFixedScenarios, LocalDateTime endDateFixedScenarios) throws CalculationException {
    	NavigableMap<LocalDateTime, RandomVariable> scenarios = getTransformedRateSeries(evaluationDate, tenorFixing);
    	RandomVariableSeries randomVariableSeries = new RandomVariableSeries(getRatesForLastNDays(scenarios, evaluationDate, numberOfMovingScenarios));
    	randomVariableSeries.addSeries(getRatesFromTo(scenarios, startDateFixedScenarios, endDateFixedScenarios));
    	return randomVariableSeries;
    }
    
    
    // For moving scenario window with fixed length
    public RandomVariableSeries getTenorPointScenarios(LocalDateTime evaluationDate, double tenorFixing, int numberOfScenarios) throws CalculationException {
		NavigableMap<LocalDateTime, RandomVariable> scenarios =  getTransformedRateSeries(evaluationDate, tenorFixing);
    	return new RandomVariableSeries(getRatesForLastNDays(scenarios, evaluationDate, numberOfScenarios));
    }
  
    
    // For moving scenario window that starts at beginning of available data and extends over time
    public RandomVariableSeries getTenorPointScenarios(LocalDateTime evaluationDate, double tenorFixing) throws CalculationException {
    	return new RandomVariableSeries(new HashMap<>(getTransformedRateSeries(evaluationDate, tenorFixing)));
    }
       	
	
	private NavigableMap<LocalDateTime, RandomVariable> getTransformedRateSeries(LocalDateTime evaluationDate, double tenorFixing) throws CalculationException {
		NavigableMap<LocalDateTime, RandomVariable> rateSeries = zeroRateModel.getRateSeries(evaluationDate, tenorFixing);
		TransformationContext context = new TransformationContext(rateSeries);
        // store the raw data as initial data input
        context.addTransformationResult("InitialDataInput", rateSeries);
        // apply the transformation methods one after the other and store the ID and result to the pipeline
        for (RateTransformation method : rateTransformations) {
            try {
				method.applyTransformation(context);
			} catch (CalculationException e) {
				e.printStackTrace();
				throw new RuntimeException("Transformation error while applying " + method.getId(), e);
			}
            context.addTransformationResult(method.getId(), context.getPrimaryData());
        }
		return context.getPrimaryData();
	}
	
	
    // Switch to HashMap to increase performance in future calculations since ordering not longer necessary
	private HashMap<LocalDateTime, RandomVariable> getRatesForLastNDays(NavigableMap<LocalDateTime, RandomVariable> rateSeries, LocalDateTime cutOffDate, int numberOfDays) {
		NavigableMap<LocalDateTime, RandomVariable> curvesUntil = rateSeries.headMap(cutOffDate, true);
		ArrayList<LocalDateTime> dates = new ArrayList<LocalDateTime>(curvesUntil.descendingKeySet());
	    if (numberOfDays >= dates.size()) {
	        throw new IllegalArgumentException("Number of days exceeds the available data. Available: " + dates.size() + ".");
	    }
	    LocalDateTime startDate = dates.get(numberOfDays - 1);
		return new HashMap<>(rateSeries.subMap(startDate, true, cutOffDate, true));
	}
	
	
	private HashMap<LocalDateTime, RandomVariable> getRatesFromTo(NavigableMap<LocalDateTime, RandomVariable> rateSeries, LocalDateTime startDate, LocalDateTime endDate) {
		return new HashMap<>(rateSeries.subMap(startDate, true, endDate, true));
	}

}
