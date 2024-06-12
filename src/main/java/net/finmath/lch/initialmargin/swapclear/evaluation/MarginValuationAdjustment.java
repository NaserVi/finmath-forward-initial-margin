package net.finmath.lch.initialmargin.swapclear.evaluation;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.PriorityQueue;
import java.util.TreeMap;

import net.finmath.lch.initialmargin.simulation.modeldata.RandomVariableSeries;
import net.finmath.lch.initialmargin.simulation.modeldata.TenorGridFactory;
import net.finmath.lch.initialmargin.simulation.modeldata.ZeroRateModel;
import net.finmath.lch.initialmargin.simulation.modeldata.TenorGridFactory.GridType;
import net.finmath.lch.initialmargin.simulation.scenarios.CurveScenarios;
import net.finmath.lch.initialmargin.simulation.scenarios.ScenarioFactory;
import net.finmath.lch.initialmargin.simulation.scenarios.ScenarioFactory.Simulation;
import net.finmath.lch.initialmargin.swapclear.aggregationscheme.SchemeStorage;
import net.finmath.lch.initialmargin.swapclear.aggregationscheme.SchemeStorage.Currency;
import net.finmath.lch.initialmargin.swapclear.aggregationscheme.SchemeStorage.CurveName;
import net.finmath.lch.initialmargin.swapclear.libormodels.ModelFactory;
import net.finmath.lch.initialmargin.swapclear.products.LchSwap;
import net.finmath.lch.initialmargin.swapclear.products.LocalPortfolio;
import net.finmath.lch.initialmargin.swapclear.products.components.LchSwapLeg;
import net.finmath.lch.initialmargin.swapclear.sensitivities.ForwardAndDiscountSensitivities;
import net.finmath.lch.initialmargin.swapclear.sensitivities.SensitivityComponentsForSwapLeg;
import net.finmath.lch.initialmargin.swapclear.sensitivities.SensitivityMatrix;
import net.finmath.exception.CalculationException;

import net.finmath.montecarlo.RandomVariableFromDoubleArray;
import net.finmath.montecarlo.interestrate.LIBORModelMonteCarloSimulationModel;
import net.finmath.stochastic.RandomVariable;
import net.finmath.stochastic.Scalar;
import net.finmath.time.FloatingpointDate;


public class MarginValuationAdjustment {
	
	public enum InitialMargin  {BASE, FLOOR, PAIRS};
	
	/*
	 * Last date of available zero rate data
	 * LMM's of ModelFactory are calibrated to the same date
	 */
	private static final LocalDateTime REFERENCE_DATE = ModelFactory.REFERENCE_DATE; 

	private static final double NANOSECONDS_TO_SECONDS = 1E09;
	
	public static void main(String args[]) throws CalculationException {
		
		// Get the model under which swaps and MVA is valued
		final LIBORModelMonteCarloSimulationModel liborModel = ModelFactory.getModel(CurveName.EUR_EURIBOR_3M);
		
		// Create ZeroRateModel based on zero rate history and model data
		final ZeroRateModel zeroRateModel = new ZeroRateModel(Currency.EUR, CurveName.EUR_EURIBOR_3M, liborModel,"spreadsheets/EUR_EURIBOR_3M_Input_Data.xlsx");
		
		// Create a portfolio of swaps belonging to the same currency, but not necessarily same index 
		LchSwap swap1 = PortfolioFactory.create5YPayerSwap();
		LchSwap swap2 = PortfolioFactory.create5YReceiverSwap();

		LocalPortfolio swapPortfolio = PortfolioFactory.getSwapPortfolio(swap2).getLocalPortfolio(Currency.EUR); 
		LocalPortfolio swapPortfolio1 = PortfolioFactory.getSwapPortfolio(swap2).getLocalPortfolio(Currency.EUR); 
		LocalPortfolio swapPortfolio2 = PortfolioFactory.getSwapPortfolio(swap1, swap2).getLocalPortfolio(Currency.EUR);	

		// Calculate MVA of the portfolio 
		int daysPerStep = 1;
		double fundingSpread = 0.005;
		
//		System.out.println("5y Swaps");
//		getMVA(swapPortfolio, zeroRateModel, liborModel, REFERENCE_DATE, daysPerStep, fundingSpread, InitialMargin.FLOOR, false, false);
//		getMVA(swapPortfolio, zeroRateModel, liborModel, REFERENCE_DATE, daysPerStep, fundingSpread, InitialMargin.FLOOR, false, true);
//		getMVA(swapPortfolio, zeroRateModel, liborModel, REFERENCE_DATE, daysPerStep, fundingSpread, InitialMargin.FLOOR, true, false);
//		getMVA(swapPortfolio, zeroRateModel, liborModel, REFERENCE_DATE, daysPerStep, fundingSpread, InitialMargin.FLOOR, true, true);
//		
		getMVA(swapPortfolio, zeroRateModel, liborModel, REFERENCE_DATE, daysPerStep, fundingSpread, InitialMargin.BASE, false, false);
		getMVA(swapPortfolio, zeroRateModel, liborModel, REFERENCE_DATE, daysPerStep, fundingSpread, InitialMargin.BASE, false, true);
		getMVA(swapPortfolio, zeroRateModel, liborModel, REFERENCE_DATE, daysPerStep, fundingSpread, InitialMargin.BASE, true, false);
		getMVA(swapPortfolio, zeroRateModel, liborModel, REFERENCE_DATE, daysPerStep, fundingSpread, InitialMargin.BASE, true, true);
//		
//		getMVA(swapPortfolio, zeroRateModel, liborModel, REFERENCE_DATE, daysPerStep, fundingSpread, InitialMargin.PAIRS, false, false);
//		getMVA(swapPortfolio, zeroRateModel, liborModel, REFERENCE_DATE, daysPerStep, fundingSpread, InitialMargin.PAIRS, false, true);
//		getMVA(swapPortfolio, zeroRateModel, liborModel, REFERENCE_DATE, daysPerStep, fundingSpread, InitialMargin.PAIRS, true, false);
//		getMVA(swapPortfolio, zeroRateModel, liborModel, REFERENCE_DATE, daysPerStep, fundingSpread, InitialMargin.PAIRS, true, true);
//		
		
//		System.out.println("15y Swaps");
//		getMVA(swapPortfolio1, zeroRateModel, liborModel, REFERENCE_DATE, daysPerStep, fundingSpread, InitialMargin.FLOOR, false, false);
//		getMVA(swapPortfolio1, zeroRateModel, liborModel, REFERENCE_DATE, daysPerStep, fundingSpread, InitialMargin.FLOOR, false, true);
//		getMVA(swapPortfolio1, zeroRateModel, liborModel, REFERENCE_DATE, daysPerStep, fundingSpread, InitialMargin.FLOOR, true, false);
//		getMVA(swapPortfolio1, zeroRateModel, liborModel, REFERENCE_DATE, daysPerStep, fundingSpread, InitialMargin.FLOOR, true, true);
//		
//		getMVA(swapPortfolio1, zeroRateModel, liborModel, REFERENCE_DATE, daysPerStep, fundingSpread, InitialMargin.BASE, false, false);
//		getMVA(swapPortfolio1, zeroRateModel, liborModel, REFERENCE_DATE, daysPerStep, fundingSpread, InitialMargin.BASE, false, true);
//		getMVA(swapPortfolio1, zeroRateModel, liborModel, REFERENCE_DATE, daysPerStep, fundingSpread, InitialMargin.BASE, true, false);
//		getMVA(swapPortfolio1, zeroRateModel, liborModel, REFERENCE_DATE, daysPerStep, fundingSpread, InitialMargin.BASE, true, true);
//		
//		getMVA(swapPortfolio1, zeroRateModel, liborModel, REFERENCE_DATE, daysPerStep, fundingSpread, InitialMargin.PAIRS, false, false);
//		getMVA(swapPortfolio1, zeroRateModel, liborModel, REFERENCE_DATE, daysPerStep, fundingSpread, InitialMargin.PAIRS, false, true);
//		getMVA(swapPortfolio1, zeroRateModel, liborModel, REFERENCE_DATE, daysPerStep, fundingSpread, InitialMargin.PAIRS, true, false);
//		getMVA(swapPortfolio1, zeroRateModel, liborModel, REFERENCE_DATE, daysPerStep, fundingSpread, InitialMargin.PAIRS, true, true);		
//		
//		
//		System.out.println("Portfolio 2 Swaps");
//		getMVA(swapPortfolio2, zeroRateModel, liborModel, REFERENCE_DATE, daysPerStep, fundingSpread, InitialMargin.PAIRS, false, false);
//		getMVA(swapPortfolio2, zeroRateModel, liborModel, REFERENCE_DATE, daysPerStep, fundingSpread, InitialMargin.PAIRS, false, true);
//		getMVA(swapPortfolio2, zeroRateModel, liborModel, REFERENCE_DATE, daysPerStep, fundingSpread, InitialMargin.PAIRS, true, false);
//		getMVA(swapPortfolio2, zeroRateModel, liborModel, REFERENCE_DATE, daysPerStep, fundingSpread, InitialMargin.PAIRS, true, true);
//			
//		getMVA(swapPortfolio2, zeroRateModel, liborModel, REFERENCE_DATE, daysPerStep, fundingSpread, InitialMargin.BASE, false, false);
//		getMVA(swapPortfolio2, zeroRateModel, liborModel, REFERENCE_DATE, daysPerStep, fundingSpread, InitialMargin.BASE, false, true);
//		getMVA(swapPortfolio2, zeroRateModel, liborModel, REFERENCE_DATE, daysPerStep, fundingSpread, InitialMargin.BASE, true, false);
//		getMVA(swapPortfolio2, zeroRateModel, liborModel, REFERENCE_DATE, daysPerStep, fundingSpread, InitialMargin.BASE, true, true);
//		
//		getMVA(swapPortfolio2, zeroRateModel, liborModel, REFERENCE_DATE, daysPerStep, fundingSpread, InitialMargin.FLOOR, false, false);
//		getMVA(swapPortfolio2, zeroRateModel, liborModel, REFERENCE_DATE, daysPerStep, fundingSpread, InitialMargin.FLOOR, false, true);
//		getMVA(swapPortfolio2, zeroRateModel, liborModel, REFERENCE_DATE, daysPerStep, fundingSpread, InitialMargin.FLOOR, true, false);
//		getMVA(swapPortfolio2, zeroRateModel, liborModel, REFERENCE_DATE, daysPerStep, fundingSpread, InitialMargin.FLOOR, true, true);
//		


	}
	
	
//	public static void getMVA

	
	public static void getMVA(LocalPortfolio localPortfolio, ZeroRateModel zeroRateModel, LIBORModelMonteCarloSimulationModel liborModel, LocalDateTime startDate, int daysPerStep, double fundingSpread, InitialMargin type, boolean pathWiseEvaluation, boolean movingScenarioWindow) throws CalculationException {
		System.out.println("Calculating Margin Valuation Adjustment Exact:");
		String analysis = "Average";
		if (pathWiseEvaluation) {
			analysis = "Pathwise";
		}
		String window = "Fixed";
		if (movingScenarioWindow) {
			window = "Moving";
		}
		System.out.println("IM type: " + type.toString() + " | Evaluation: " + analysis + " | Scenarios: " + window);

		RandomVariable mva = new RandomVariableFromDoubleArray(0.0);
		LocalDateTime lastPaymentDate = FloatingpointDate.getDateFromFloatingPointDate(startDate, localPortfolio.getLastPaymentDate());
		double time = 0.0;
		double nextTime = 1/365.0; 
		long startTime = System.nanoTime();
		for (LocalDateTime date = startDate; date.isBefore(lastPaymentDate); date = date.plusDays(daysPerStep)) {
			if (SchemeStorage.getLocalCalendar(Currency.EUR).isBusinessday(date.toLocalDate())) {
				RandomVariable initialMargin;
				switch (type) {
				case BASE:
					initialMargin = localPortfolio.getBaseInitialMargin(date, zeroRateModel, pathWiseEvaluation, movingScenarioWindow);
					break;
				case FLOOR:
					initialMargin = localPortfolio.getFloorInitialMargin(date, zeroRateModel, pathWiseEvaluation, movingScenarioWindow);
					break;
				case PAIRS:
					initialMargin = localPortfolio.getPairsInitialMargin(date, zeroRateModel, pathWiseEvaluation, movingScenarioWindow);
					break;
				default:
					throw new IllegalArgumentException("Initial Margin type not defined.");
				}
//				System.out.println("Initial Margin as of " + date +": " + initialMargin.getAverage());
				// d(1/N^fd(t)) = 1/N^fd(t_i+1) - 1/N^fd(t_i)
				RandomVariable fundingNumeraire = liborModel.getNumeraire(nextTime).mult(Math.exp((nextTime) * fundingSpread)).invert();
				fundingNumeraire = fundingNumeraire.sub(liborModel.getNumeraire(time).mult(Math.exp(time * fundingSpread)).invert());
				mva = mva.add(fundingNumeraire.mult(initialMargin));
				time += daysPerStep/365.0;
			}
			nextTime += daysPerStep/365.0;
		}
		long endTime = System.nanoTime();


		System.out.println("Elapsed time: " + ((endTime - startTime)/NANOSECONDS_TO_SECONDS) + "seconds");
		System.out.println("MVA: " + (-mva.getAverage()));
		System.out.println();
	}
	
	
	public static void getMVAWithTaylorPnL(LocalPortfolio localPortfolio, ZeroRateModel zeroRateModel, LIBORModelMonteCarloSimulationModel liborModel, LocalDateTime startDate, int daysPerStep, double fundingSpread, Simulation simulation, boolean pathWiseEvaluation, boolean movingScenarioWindow) throws CalculationException {
		System.out.println("Calculating Margin Valuation Adjustment Taylor PnL:");
		String analysis = "Average";
		if (pathWiseEvaluation) {
			analysis = "Pathwise";
		}
		String window = "Fixed";
		if (movingScenarioWindow) {
			window = "Moving";
		}
		System.out.println("Simulation: " + simulation.toString() + " | Evaluation: " + analysis + " | Scenarios: " + window);

		SensitivityComponentsForSwapLeg sensitivityComponents = new SensitivityComponentsForSwapLeg(localPortfolio.getSwaps(), zeroRateModel, zeroRateModel);	    
		ForwardAndDiscountSensitivities sensitivities = new ForwardAndDiscountSensitivities(sensitivityComponents, TenorGridFactory.getTenorGrid(GridType.INITIAL_MARGIN_RISK_GRID));
		ScenarioFactory scenarios = new ScenarioFactory(zeroRateModel, simulation);	

		RandomVariable mva = new RandomVariableFromDoubleArray(0.0);
		LocalDateTime lastPaymentDate = FloatingpointDate.getDateFromFloatingPointDate(startDate, localPortfolio.getLastPaymentDate());
		double time = 0.0;
		double nextTime = 1/365.0; 
		long startTime = System.nanoTime();
		for (LocalDateTime date = startDate; date.isBefore(lastPaymentDate); date = date.plusDays(daysPerStep)) {
			if (SchemeStorage.getLocalCalendar(Currency.EUR).isBusinessday(date.toLocalDate())) {
				RandomVariable initialMargin = getInitialMarginWithTaylorPnL(date, zeroRateModel, sensitivities, scenarios, simulation, pathWiseEvaluation, movingScenarioWindow);
//				System.out.println("Initial Margin as of " + date +": " + initialMargin.getAverage());
				// d(1/N^fd(t)) = 1/N^fd(t_i+1) - 1/N^fd(t_i)
				RandomVariable fundingNumeraire = liborModel.getNumeraire(nextTime).mult(Math.exp((nextTime) * fundingSpread)).invert();
				fundingNumeraire = fundingNumeraire.sub(liborModel.getNumeraire(time).mult(Math.exp(time * fundingSpread)).invert());
				mva = mva.add(fundingNumeraire.mult(initialMargin));
				time += daysPerStep/365.0;
			}
			nextTime += daysPerStep/365.0;
		}
		long endTime = System.nanoTime();

		System.out.println("Elapsed time: " + ((endTime - startTime)/NANOSECONDS_TO_SECONDS) + "seconds");
		System.out.println("MVA: " + (-mva.getAverage()));
		System.out.println();
	}
	
	
	public static RandomVariable getInitialMarginWithTaylorPnL(LocalDateTime evaluationDate, ZeroRateModel zeroRateModel, ForwardAndDiscountSensitivities sensitivities, ScenarioFactory scenarios, Simulation simulation, boolean pathWiseEvaluation, boolean movingScenarioWindow) throws CalculationException {	
		SensitivityMatrix deltaMatrix = sensitivities.getDeltaSensitivities(evaluationDate);
		SensitivityMatrix gammaMatrix = sensitivities.getGammaSensitivities(evaluationDate);

		// Store tenor point scenarios to construct the shifted curves for full swap revaluation after PnL comparison
		CurveScenarios curveScenarios = new CurveScenarios();
		// Directly aggregated element-wise PnL calculations
		RandomVariableSeries pnLSeries = new RandomVariableSeries();
		// We map forward and discount sensitivities to the same matrix -> gamma matrix contains all necessary fixing points
		// Loop through all rows of the matrix
		for (Map.Entry<Double, TreeMap<Double, RandomVariable>> row : gammaMatrix.getSensitivityMatrix().entrySet()) {
			// if scenarios for row fixing is already contained in curveScenarios return it else created it
			RandomVariableSeries rowScenarios;
			if (curveScenarios.containsSeries(row.getKey())) {
				rowScenarios = curveScenarios.getSeries(row.getKey());
			} else {
				rowScenarios = scenarios.getTenorPointScenarios(evaluationDate, row.getKey());
				// Add the tenorPointScenario to the TenorGridSeries for later curve construction
				curveScenarios.addSeries(row.getKey(), rowScenarios);
			}
			// Delta sensitivities are always on the diagonal entries and fixings determined by the row entries -> just one delta sensitivity per row
			// Multiply the tenorPointScenario with the delta sensitivity and add it to the PnL results
			pnLSeries.sum(getFirstOrderTaylorTerm(rowScenarios, deltaMatrix.getSensitivityMatrix().get(row.getKey()).get(row.getKey()))); // Delta
			// Loop through all columns of the matrix	
			for (Map.Entry<Double, RandomVariable> column : row.getValue().entrySet()) {
				// if scenarios for column fixing is already contained in curveScenarios return it else created it
				RandomVariableSeries columnScenarios;
				if (curveScenarios.containsSeries(column.getKey())) {
					columnScenarios = curveScenarios.getSeries(column.getKey());
				} else {
					columnScenarios = scenarios.getTenorPointScenarios(evaluationDate, column.getKey());
					// Add the tenorPointScenario to the TenorGridSeries for later curve construction
					curveScenarios.addSeries(column.getKey(), columnScenarios);
				}
				// Gamma sensitivities on upper triangle matrix and rely on two scenario series
				// Multiply the tenorPointScenarios with the gamma sensitivity and add it to the PnL results
				pnLSeries.sum(getSecondOrderTaylorTerm(rowScenarios, columnScenarios, row.getValue().get(column.getKey()))); // Gamma
			}
		}
		RandomVariable initialMargin;
		if (pathWiseEvaluation) {
			double[] initialMarginRealizations = new double[zeroRateModel.getNumberOfPaths()];
			for (int pathNumber = 0; pathNumber < zeroRateModel.getNumberOfPaths(); pathNumber++) {
				List<RandomVariable> pnLs = getLosses(pnLSeries, pathNumber);
				initialMarginRealizations[pathNumber] = getInitialMarginFromLosses(pnLs, simulation).get(pathNumber);
			}
			double filtrationTime = FloatingpointDate.getFloatingPointDateFromDate(zeroRateModel.getReferenceDate(), evaluationDate);
			initialMargin = new RandomVariableFromDoubleArray(filtrationTime, initialMarginRealizations);	
		} else {
			List<RandomVariable> pnLs = getLosses(pnLSeries, null);
    		initialMargin = getInitialMarginFromLosses(pnLs, simulation);
		}
		return initialMargin;
	}
	
	
	public static void getMVAWithFullRevaluation(LocalPortfolio localPortfolio, ZeroRateModel zeroRateModel, LIBORModelMonteCarloSimulationModel liborModel, LocalDateTime startDate, int daysPerStep, double fundingSpread, Simulation simulation, boolean pathWiseEvaluation, boolean movingScenarioWindow) throws CalculationException {
		System.out.println("Calculating Margin Valuation Adjustment Full Revaluation:");
		String analysis = "Average";
		if (pathWiseEvaluation) {
			analysis = "Pathwise";
		}
		String window = "Fixed";
		if (movingScenarioWindow) {
			window = "Moving";
		}
		System.out.println("Simulation: " + simulation.toString() + " | Evaluation: " + analysis + " | Scenarios: " + window);

		SensitivityComponentsForSwapLeg sensitivityComponents = new SensitivityComponentsForSwapLeg(localPortfolio.getSwaps(), zeroRateModel, zeroRateModel);	    
		ForwardAndDiscountSensitivities sensitivities = new ForwardAndDiscountSensitivities(sensitivityComponents, TenorGridFactory.getTenorGrid(GridType.INITIAL_MARGIN_RISK_GRID));
	
		RandomVariable mva = new RandomVariableFromDoubleArray(0.0);
		LocalDateTime lastPaymentDate = FloatingpointDate.getDateFromFloatingPointDate(startDate, localPortfolio.getLastPaymentDate());
		double time = 0.0;
		double nextTime = 1/365.0; 
		long startTime = System.nanoTime();
		for (LocalDateTime date = startDate; date.isBefore(lastPaymentDate); date = date.plusDays(daysPerStep)) {
			if (SchemeStorage.getLocalCalendar(Currency.EUR).isBusinessday(date.toLocalDate())) {
				RandomVariable initialMargin = getInitialMarginWithFullRevaluation(date, zeroRateModel, localPortfolio, sensitivities, simulation, pathWiseEvaluation, movingScenarioWindow);
//				System.out.println("Initial Margin as of " + date +": " + initialMargin.getAverage());
				// d(1/N^fd(t)) = 1/N^fd(t_i+1) - 1/N^fd(t_i)
				RandomVariable fundingNumeraire = liborModel.getNumeraire(nextTime).mult(Math.exp((nextTime) * fundingSpread)).invert();
				fundingNumeraire = fundingNumeraire.sub(liborModel.getNumeraire(time).mult(Math.exp(time * fundingSpread)).invert());
				mva = mva.add(fundingNumeraire.mult(initialMargin));
				time += daysPerStep/365.0;
			}
			nextTime += daysPerStep/365.0;
		}
		long endTime = System.nanoTime();
		System.out.println("Elapsed time: " + ((endTime - startTime)/NANOSECONDS_TO_SECONDS) + "seconds");
		System.out.println("MVA: " + (-mva.getAverage()));
		System.out.println();
	}
	
	
	public static RandomVariable getInitialMarginWithFullRevaluation(LocalDateTime evaluationDate, ZeroRateModel zeroRateModel, LocalPortfolio localPortfolio, ForwardAndDiscountSensitivities sensitivities, Simulation simulation, boolean pathWiseEvaluation, boolean movingScenarioWindow) throws CalculationException {	
		RandomVariable initialMargin;
		if (pathWiseEvaluation) {
			double[] initialMarginRealizations = new double[zeroRateModel.getNumberOfPaths()];
			for (int pathNumber = 0; pathNumber < zeroRateModel.getNumberOfPaths(); pathNumber++) {
				List<RandomVariable> worstLosses = getLossesUnderFullRevaluation(evaluationDate, localPortfolio, zeroRateModel, sensitivities, simulation, pathNumber, movingScenarioWindow);
				initialMarginRealizations[pathNumber] = getInitialMarginFromLosses(worstLosses, simulation).get(pathNumber);
			}
			double filtrationTime = FloatingpointDate.getFloatingPointDateFromDate(zeroRateModel.getReferenceDate(), evaluationDate);
			initialMargin = new RandomVariableFromDoubleArray(filtrationTime, initialMarginRealizations);	
		} else {
    		List<RandomVariable> worstLosses = getLossesUnderFullRevaluation(evaluationDate, localPortfolio, zeroRateModel, sensitivities, simulation, null, movingScenarioWindow);
    		initialMargin = getInitialMarginFromLosses(worstLosses, simulation);
		}

		return initialMargin;
	}
	

	// Piecewise linear
	public static void getMVAWithInterpolation(LocalPortfolio localPortfolio, ZeroRateModel zeroRateModel, LIBORModelMonteCarloSimulationModel liborModel, LocalDateTime startDate, int pointsInBetween, int daysPerStep, double fundingSpread, InitialMargin type, boolean pathWiseEvaluation, boolean movingScenarioWindow) throws CalculationException {		
		System.out.println("Calculating Margin Valuation Adjustment by interpolation of " + (pointsInBetween) + " equidistant points:");
		String analysis = "Average";
		if (pathWiseEvaluation) {
			analysis = "Pathwise";
		}
		String window = "Fixed";
		if (movingScenarioWindow) {
			window = "Moving";
		}
		System.out.println("IM type: " + type.toString() + " | Evaluation: " + analysis + " | Scenarios: " + window);

		LocalDateTime lastPaymentDate = FloatingpointDate.getDateFromFloatingPointDate(startDate, localPortfolio.getLastPaymentDate());
		long startTime = System.nanoTime();
		double timeUntilLastPaymentDate = FloatingpointDate.getFloatingPointDateFromDate(startDate, lastPaymentDate);
		double timeBetweenIMEvaluations =  timeUntilLastPaymentDate / (pointsInBetween + 1);
		NavigableMap<LocalDateTime, RandomVariable> initialMargins = new TreeMap<>();
		for (int i = 0; i < pointsInBetween + 2; i++) {
			LocalDateTime date = startDate.plusDays(i * (int) (timeBetweenIMEvaluations * 365));
			while (!SchemeStorage.getLocalCalendar(Currency.EUR).isBusinessday(date.toLocalDate())) {
				date = date.minusDays(1);
			}
			
			switch (type) {
			case BASE:
				initialMargins.put(date, localPortfolio.getBaseInitialMargin(date, zeroRateModel, pathWiseEvaluation, movingScenarioWindow));
				break;
			case FLOOR:
				initialMargins.put(date, localPortfolio.getFloorInitialMargin(date, zeroRateModel, pathWiseEvaluation, movingScenarioWindow));
				break;
			case PAIRS:
				initialMargins.put(date, localPortfolio.getPairsInitialMargin(date, zeroRateModel, pathWiseEvaluation, movingScenarioWindow));
				break;
			default:
				throw new IllegalArgumentException("Initial Margin type not defined.");
			}
		}		
		RandomVariable mva = new RandomVariableFromDoubleArray(0.0);
		double time = 0.0;
		double nextTime = 1/365.0; 		
		for (LocalDateTime date = startDate; date.isBefore(lastPaymentDate); date = date.plusDays(daysPerStep)) {
			if (SchemeStorage.getLocalCalendar(Currency.EUR).isBusinessday(date.toLocalDate())) {
				LocalDateTime lowerKey = initialMargins.lowerKey(date);
				if (lowerKey == null) {
					lowerKey = initialMargins.firstKey();
				}
				LocalDateTime upperKey = initialMargins.higherKey(date);
				if (upperKey == null) {
					upperKey = initialMargins.lastKey();
				}
				RandomVariable initialMargin;
				// Case if evaluation date is after last date in initilMargins map
				if (date.isAfter(initialMargins.lastKey())) {
					initialMargin = new Scalar(0.0);
				} else {
					initialMargin = getInitialMarginWithInterpolation(date, lowerKey, upperKey, initialMargins.get(lowerKey), initialMargins.get(upperKey));
				}
//				System.out.println("Initial Margin as of " + date +": " + initialMargin.getAverage());
				// d(1/N^fd(t)) = 1/N^fd(t_i+1) - 1/N^fd(t_i)
				RandomVariable fundingNumeraire = liborModel.getNumeraire(nextTime).mult(Math.exp((nextTime) * fundingSpread)).invert();
				fundingNumeraire = fundingNumeraire.sub(liborModel.getNumeraire(time).mult(Math.exp(time * fundingSpread)).invert());
				mva = mva.add(fundingNumeraire.mult(initialMargin));
				time += daysPerStep/365.0;
			}
			nextTime += daysPerStep/365.0;
		}
		long endTime = System.nanoTime();
		System.out.println("Elapsed time: " + ((endTime - startTime)/NANOSECONDS_TO_SECONDS) + "seconds");
		System.out.println("MVA: " + (-mva.getAverage()));
		System.out.println();
	}
	
	
	// Assuming leftBoundary <= evaluationDate <= rightBoundary
	public static RandomVariable getInitialMarginWithInterpolation(LocalDateTime evaluationDate, LocalDateTime leftBoundary, LocalDateTime rightBoundary, RandomVariable leftValue, RandomVariable rightValue) {
		double distanceToLeftBoundary = FloatingpointDate.getFloatingPointDateFromDate(leftBoundary, evaluationDate);
		double totalDistance = FloatingpointDate.getFloatingPointDateFromDate(leftBoundary, rightBoundary);
		double weight = distanceToLeftBoundary / totalDistance;
		return leftValue.mult(1 - weight).add(rightValue.mult(weight));
	}
	
	
	private static List<RandomVariable> getLosses(RandomVariableSeries pnLSeries, Integer pathOrState) {
		if (pathOrState == null) {
			return new ArrayList<>(pnLSeries.getSeries().values());
		}
		List<RandomVariable> losses = new ArrayList<>(pnLSeries.getSeries().size());
		for (RandomVariable value : pnLSeries.getSeries().values()) {
			losses.add(new Scalar(value.get(pathOrState)));
		}
		return losses;
	}
		
	
	private static RandomVariableSeries getFirstOrderTaylorTerm(RandomVariableSeries tenorPointScenario, RandomVariable sensitivity) {
		HashMap<LocalDateTime, RandomVariable> pnlOnTenorPoint = new HashMap<>();
		for (Map.Entry<LocalDateTime, RandomVariable> shift : tenorPointScenario.getSeries().entrySet()) {
			pnlOnTenorPoint.put(shift.getKey(), shift.getValue().mult(10000).mult(sensitivity));
		}
		return new RandomVariableSeries(pnlOnTenorPoint);
	}

	
	private static RandomVariableSeries getSecondOrderTaylorTerm(RandomVariableSeries firstTenorPointScenario, RandomVariableSeries secondTenorPointScenario, RandomVariable sensitivity) {
		HashMap<LocalDateTime, RandomVariable> pnlOnTenorPoint = new HashMap<>();
		for (Map.Entry<LocalDateTime, RandomVariable> shift : firstTenorPointScenario.getSeries().entrySet()) {
			RandomVariable secondShift = secondTenorPointScenario.getSeries().get(shift.getKey());
			pnlOnTenorPoint.put(shift.getKey(), shift.getValue().mult(secondShift).mult(10000 * 10000).mult(sensitivity).mult(0.5));
		}
		return new RandomVariableSeries(pnlOnTenorPoint);
	}
	
	
	private static List<RandomVariable> getLossesUnderFullRevaluation(LocalDateTime evaluationDate, LocalPortfolio localPortfolio, ZeroRateModel zeroRateModel, ForwardAndDiscountSensitivities sensitivities, Simulation simulation, Integer pathOrState, boolean movingScenarioWindow) throws CalculationException {
		ScenarioFactory scenarios = new ScenarioFactory(zeroRateModel, simulation);	
		// Need sensitivity curve to call scenarios
		SensitivityMatrix gammaMatrix = sensitivities.getDeltaSensitivities(evaluationDate);
		CurveScenarios curveScenarios = new CurveScenarios();		
		// store one key for date array retrieval
		double keyFixing = 0.0;
		// Loop through all rows of the matrix
		for (Map.Entry<Double, TreeMap<Double, RandomVariable>> row : gammaMatrix.getSensitivityMatrix().entrySet()) {
			// if scenarios for row fixing is already contained in curveScenarios return it else created it
			if (!curveScenarios.containsSeries(row.getKey())) {
				// Add the tenorPointScenario to the TenorGridSeries for later curve construction
				curveScenarios.addSeries(row.getKey(), scenarios.getTenorPointScenarios(evaluationDate, row.getKey()));
			}	
			for (Map.Entry<Double, RandomVariable> column : row.getValue().entrySet()) {
				// if scenarios for column fixing is already contained in curveScenarios return it else created it
				if (!curveScenarios.containsSeries(column.getKey())) {
					curveScenarios.addSeries(column.getKey(), scenarios.getTenorPointScenarios(evaluationDate, column.getKey()));
				} 
			}
			keyFixing = row.getKey();
		}
		// value swaps under all scenario dates
		List<LocalDateTime> dates = new ArrayList<>(curveScenarios.getSeries(keyFixing).getSeries().keySet());
		List<RandomVariable> losses = new ArrayList<RandomVariable>(Collections.nCopies(dates.size(), new Scalar(0.0)));

		for (LchSwapLeg swapLeg : localPortfolio.getSwaps()) {
			RandomVariable netPresentValue = swapLeg.getValue(evaluationDate, zeroRateModel, null, pathOrState); // no shift
			// If the evaluationDate is after the last paymentDate of the portfolio, all payments have been made <-> all swaps have value zero 
			// -> swap has no sensitivity -> PnL calculation does not return any scenario -> call of shifted value results in an error due to missing scenarios
			double modelTime = FloatingpointDate.getFloatingPointDateFromDate(zeroRateModel.getReferenceDate(), evaluationDate);
			if (modelTime >= localPortfolio.getLastPaymentDate()) {
				return losses;
			}
			for (int index = 0; index < dates.size(); index++) {
				// get shifted value for elements of worstDates on all paths
				RandomVariable shiftedNetPresentValue = swapLeg.getValue(evaluationDate, zeroRateModel, curveScenarios.getCurveShifts(dates.get(index)), pathOrState); 
				// directly update / sum up the losses of all swap legs: NPV_s - NPV 
				losses.set(index, losses.get(index).add(shiftedNetPresentValue.sub(netPresentValue))); 
			}
		}
		return losses;
	}
	
	
	private static RandomVariable getInitialMarginFromLosses(List<RandomVariable> nWorstLosses, Simulation simulation) throws CalculationException {
	    int numberOfLosses;
	    switch (simulation) {
        	case EXPECTED_SHORTFALL_4:
        		numberOfLosses = 4; 
        		break;
	        case EXPECTED_SHORTFALL_6:
	        	numberOfLosses = 6; 
	            break;
	        case VALUE_AT_RISK:
	        	numberOfLosses = 13;
	            break;
	        default:
	            throw new IllegalArgumentException("Unsupported simulation type: " + simulation);
	    }
	    // In case of path-wise evaluation we pass a List of Scalars to the method, therefore the comparator works for both cases
	    PriorityQueue<RandomVariable> worstLosses = new PriorityQueue<>(Comparator.comparingDouble( losses -> {
	    	return losses.mult(-1.0).getAverage();
	    }));
	    for (RandomVariable loss : nWorstLosses) {
	        worstLosses.add(loss);
	        if (worstLosses.size() > numberOfLosses) {
	            worstLosses.poll(); 
	        }
	    }	    
	    RandomVariable initialMargin = new Scalar(0.0);
	    if (simulation == Simulation.EXPECTED_SHORTFALL_4 || simulation == Simulation.EXPECTED_SHORTFALL_6) {
	    	// ES: average of 4/6 worst losses
	        for (RandomVariable loss : worstLosses) {
	            initialMargin = initialMargin.add(loss);
	        }
	        initialMargin = initialMargin.div(numberOfLosses).abs(); 
	    } else if (simulation == Simulation.VALUE_AT_RISK) {
	    	// VaR: 13th worst loss
	        initialMargin = worstLosses.poll().abs();
	    }
		return initialMargin;
	}

}
