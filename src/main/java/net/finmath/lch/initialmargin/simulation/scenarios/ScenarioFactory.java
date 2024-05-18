package net.finmath.lch.initialmargin.simulation.scenarios;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Arrays;

import net.finmath.lch.initialmargin.simulation.modeldata.RandomVariableSeries;
import net.finmath.lch.initialmargin.simulation.modeldata.ZeroRateModel;
import net.finmath.lch.initialmargin.simulation.ratetransformations.RateTransformationEWMA;
import net.finmath.lch.initialmargin.simulation.ratetransformations.RateTransformationMidVolatilityScaling;
import net.finmath.lch.initialmargin.simulation.ratetransformations.RateTransformationReturns;
import net.finmath.lch.initialmargin.simulation.ratetransformations.RateTransformationReturns.ReturnType;
import net.finmath.exception.CalculationException;



public class ScenarioFactory {
	
	public enum Simulation {EXPECTED_SHORTFALL_4, EXPECTED_SHORTFALL_6, VALUE_AT_RISK}
	
	
	private ScenarioBuilder				scenarioBuilder;
	private final Simulation			simulation;
	
	
	public ScenarioFactory (ZeroRateModel zeroRateModel, Simulation simulation) {
		this.simulation = simulation;
		switch (simulation) {
		case EXPECTED_SHORTFALL_4:
		case EXPECTED_SHORTFALL_6:
			this.scenarioBuilder = new ScenarioBuilder(zeroRateModel, Arrays.asList(
					new RateTransformationReturns(5, ReturnType.ABSOLUTE),
					new RateTransformationEWMA(60, 0.992),
					new RateTransformationMidVolatilityScaling("Returns")));
			break;
		case VALUE_AT_RISK:
			this.scenarioBuilder = new ScenarioBuilder(zeroRateModel, Arrays.asList(
					new RateTransformationReturns(5, ReturnType.ABSOLUTE)));
			break;
		default:
			throw new IllegalArgumentException("Unsupported simulation type: " + simulation);
		}
	}


	// TODO: add constant window option
	public RandomVariableSeries getTenorPointScenarios(LocalDateTime evaluationDate, double tenorFixing) throws CalculationException {
		switch (simulation) {
		case EXPECTED_SHORTFALL_4:
			// Basis-Risk Add-On IM: scenarios from 01.01.2008 up to today
			return scenarioBuilder.getTenorPointScenarios(evaluationDate, tenorFixing); 
		case EXPECTED_SHORTFALL_6:
			// Base IM: rolling window with 2500 scenarios 
			return scenarioBuilder.getTenorPointScenarios(evaluationDate, tenorFixing, 2500); 
		case VALUE_AT_RISK:
			// Floor IM: rolling window with 2500 scenarios + fix window from 01.01.2008 to 30.06.2010
			return scenarioBuilder.getTenorPointScenarios(evaluationDate, tenorFixing, 2500, LocalDate.of(2008, Month.JANUARY, 01).atStartOfDay(), LocalDate.of(2010, Month.JUNE, 30).atStartOfDay()); 
		default:
			throw new IllegalArgumentException("Unsupported simulation type: " + simulation);
		}
	}

}
