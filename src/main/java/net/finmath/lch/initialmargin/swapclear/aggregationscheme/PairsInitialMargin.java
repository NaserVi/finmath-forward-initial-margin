package net.finmath.lch.initialmargin.swapclear.aggregationscheme;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.TreeMap;

import net.finmath.lch.initialmargin.simulation.modeldata.RandomVariableSeries;
import net.finmath.lch.initialmargin.simulation.modeldata.TenorGridFactory;
import net.finmath.lch.initialmargin.simulation.modeldata.ZeroRateModel;
import net.finmath.lch.initialmargin.simulation.modeldata.TenorGridFactory.GridType;
import net.finmath.lch.initialmargin.simulation.scenarios.CurveScenarios;
import net.finmath.lch.initialmargin.simulation.scenarios.ScenarioFactory;
import net.finmath.lch.initialmargin.simulation.scenarios.ScenarioFactory.Simulation;
import net.finmath.lch.initialmargin.swapclear.products.LocalPortfolio;
import net.finmath.lch.initialmargin.swapclear.products.components.LchSwapLeg;
import net.finmath.lch.initialmargin.swapclear.sensitivities.ForwardAndDiscountSensitivities;
import net.finmath.lch.initialmargin.swapclear.sensitivities.Sensitivities;
import net.finmath.lch.initialmargin.swapclear.sensitivities.SensitivityComponentsForSwapLeg;
import net.finmath.lch.initialmargin.swapclear.sensitivities.SensitivityMatrix;
import net.finmath.exception.CalculationException;
import net.finmath.montecarlo.RandomVariableFromDoubleArray;
import net.finmath.stochastic.RandomVariable;
import net.finmath.stochastic.Scalar;
import net.finmath.time.FloatingpointDate;


/**
 * Calculation scheme of the LCH PAIRS initial margin methodology.
 * 
 */
public class PairsInitialMargin {
	
	private static final double DECIMAL_TO_BASIS_POINT_CONVERSION_FACTOR = 10000.0;
	private static final int NUMBER_OF_FULL_REVALUATIONS = 20;
	
	
	/**
	 * Base IM is calculated on a 10y window of historical scenarios.
	 * A currency-specific standardized zero curve, e.g. EUR_EURIBOR, is used for swap valuing and risk sampling/mapping.
	 * Initial Margin is calculated as an Expected-Shortfall on the 6 worst losses
	 * 
	 * @param evaluationDate Time for which IM will be calculated
	 * @param zeroRateModel Model under which IM is valued (risk, swap values, scenarios)
	 * @param localPortfolio Portfolio consisting of interest swaps
	 * @param pathWiseEvaluation If true scenarios and IM will be calculated on each path separately, otherwise as the average of all paths
	 * @param movingScenarioWindow If true the scenarios will move with the model evaluation date -> LMM of ZeroRateModel will generate additional scenarios
	 * @return The Base Initial Margin of the LCH methodology
	 * @throws CalculationException
	 */
	public static RandomVariable getBaseInitialMargin(LocalDateTime evaluationDate, ZeroRateModel zeroRateModel, LocalPortfolio localPortfolio, boolean pathWiseEvaluation, boolean movingScenarioWindow) throws CalculationException {
		// 1. Set up InitialMarginContainer to store intermediate results
		InitialMarginContainer dataContainer = new InitialMarginContainer();
		// 2. Get delta and gamma sensitivities and store them in the container
		getSensitivities(evaluationDate, zeroRateModel, localPortfolio, dataContainer);
		// 3. Calculate PnL with Taylor Expansion and store curve shifts and PnLs in dataContainer
		Simulation simulation = Simulation.EXPECTED_SHORTFALL_6;
		getPnLAndCurveScenarios(evaluationDate, zeroRateModel, localPortfolio, dataContainer, simulation, movingScenarioWindow);
		// 4. Full revalue of the swaps under the 20 worst scenarios, either path-wise or by approximation (average over all paths)
		if (pathWiseEvaluation) {
			double[] initialMarginRealizations = new double[zeroRateModel.getNumberOfPaths()];
			for (int pathNumber = 0; pathNumber < zeroRateModel.getNumberOfPaths(); pathNumber++) {
				// Get 20 worst losses on each path
				List<RandomVariable> worstLosses = getNWorstLosses(evaluationDate, localPortfolio, zeroRateModel, dataContainer, simulation, NUMBER_OF_FULL_REVALUATIONS, pathNumber);
				// Average the worst 6 losses on each path
				initialMarginRealizations[pathNumber] = getInitialMarginFromLosses(worstLosses, simulation).get(pathNumber);
			}
			double filtrationTime = FloatingpointDate.getFloatingPointDateFromDate(zeroRateModel.getReferenceDate(), evaluationDate);
			return new RandomVariableFromDoubleArray(filtrationTime, initialMarginRealizations);
		}
		// Approximation over all paths
		List<RandomVariable> worstLosses = getNWorstLosses(evaluationDate, localPortfolio, zeroRateModel, dataContainer, simulation, NUMBER_OF_FULL_REVALUATIONS, null);
		return getInitialMarginFromLosses(worstLosses, simulation);
	}
	
	

	/**
	 * Floor IM is calculated on a 10y window of historical scenarios plus fixed stress period January 2008 - June 2010.
	 * A currency-specific standardized zero curve, e.g. EUR_EURIBOR, is used for swap valuing and risk sampling/mapping.
	 * Initial Margin is calculated as a Value-at-Risk at 13th worst loss.
	 * 
	 * @param evaluationDate Time for which IM will be calculated
	 * @param zeroRateModel Model under which IM is valued (risk, swap values, scenarios)
	 * @param localPortfolio Portfolio consisting of interest swaps
	 * @param pathWiseEvaluation If true scenarios and IM will be calculated on each path separately, otherwise as the average of all paths
	 * @param movingScenarioWindow If true the scenarios will move with the model evaluation date -> LMM of ZeroRateModel will generate additional scenarios
	 * @return The Base Initial Margin of the LCH methodology
	 * @throws CalculationException
	 */
	public static RandomVariable getFloorInitialMargin(LocalDateTime evaluationDate, ZeroRateModel zeroRateModel, LocalPortfolio localPortfolio, boolean pathWiseEvaluation, boolean movingScenarioWindow) throws CalculationException {
		// 1. Set up InitialMarginContainer to store intermediate results
		InitialMarginContainer dataContainer = new InitialMarginContainer();
		// 2. Get delta and gamma sensitivities and store them in the container
		getSensitivities(evaluationDate, zeroRateModel, localPortfolio, dataContainer);
		// 3. Calculate PnL with Taylor Expansion and store curve shifts and PnLs in dataContainer
		Simulation simulation = Simulation.VALUE_AT_RISK;
		getPnLAndCurveScenarios(evaluationDate, zeroRateModel, localPortfolio, dataContainer, simulation, movingScenarioWindow);
		// 4. Full revalue of the swaps under the 20 worst scenarios, either path-wise or by approximation (average over all paths)
		if (pathWiseEvaluation) {
			double[] initialMarginRealizations = new double[zeroRateModel.getNumberOfPaths()];
			for (int pathNumber = 0; pathNumber < zeroRateModel.getNumberOfPaths(); pathNumber++) {
				// Get 20 worst losses on each path
				List<RandomVariable> worstLosses = getNWorstLosses(evaluationDate, localPortfolio, zeroRateModel, dataContainer, simulation, NUMBER_OF_FULL_REVALUATIONS, pathNumber);
				// The 13th worst loss on each path
				initialMarginRealizations[pathNumber] = getInitialMarginFromLosses(worstLosses, simulation).get(pathNumber);
			}
			double filtrationTime = FloatingpointDate.getFloatingPointDateFromDate(zeroRateModel.getReferenceDate(), evaluationDate);
			return new RandomVariableFromDoubleArray(filtrationTime, initialMarginRealizations);
		}
		// Approximation over all paths
		List<RandomVariable> worstLosses = getNWorstLosses(evaluationDate, localPortfolio, zeroRateModel, dataContainer, simulation, NUMBER_OF_FULL_REVALUATIONS, null);
		return getInitialMarginFromLosses(worstLosses, simulation);
	}
	
	
	/**
	 * Pairs IM is the maximum of the Base IM and Floor IM.
	 * 
	 * @param evaluationDate Time for which IM will be calculated
	 * @param zeroRateModel Model under which IM is valued (risk, swap values, scenarios)
	 * @param localPortfolio Portfolio consisting of interest swaps
	 * @param pathWiseEvaluation If true scenarios and IM will be calculated on each path separately, otherwise as the average of all paths
	 * @param movingScenarioWindow If true the scenarios will move with the model evaluation date -> LMM of ZeroRateModel will generate additional scenarios
	 * @return The Base Initial Margin of the LCH methodology
	 * @throws CalculationException
	 */
	public static RandomVariable getPairsInitialMargin(LocalDateTime evaluationDate, ZeroRateModel zeroRateModel, LocalPortfolio localPortfolio, boolean pathWiseEvaluation, boolean movingScenarioWindow) throws CalculationException {
		// 1. Set up InitialMarginContainer to store intermediate results
		InitialMarginContainer dataContainer = new InitialMarginContainer();
		// 2. Get delta and gamma sensitivities and store them in the container
		getSensitivities(evaluationDate, zeroRateModel, localPortfolio, dataContainer);
		// 3. Calculate PnL with Taylor Expansion and store curve shifts and PnLs in dataContainer
		// Split evaluation into Base and Floor Initial Margin due to different scenario transformations
		Simulation simulationBase = Simulation.EXPECTED_SHORTFALL_6;
		Simulation simulationFloor = Simulation.VALUE_AT_RISK;
		getPnLAndCurveScenarios(evaluationDate, zeroRateModel, localPortfolio, dataContainer, simulationBase, movingScenarioWindow);
		getPnLAndCurveScenarios(evaluationDate, zeroRateModel, localPortfolio, dataContainer, simulationFloor, movingScenarioWindow);
		// 4. Full revalue of the swaps under the 20 worst scenarios, either path-wise or by approximation (average over all paths)
		if (pathWiseEvaluation) {
			// Pairs IM is the maximum of Base and Floor IM
			double[] initialMarginRealizations = new double[zeroRateModel.getNumberOfPaths()];
			for (int pathNumber = 0; pathNumber < zeroRateModel.getNumberOfPaths(); pathNumber++) {
				// Get 20 worst losses on each path 
				List<RandomVariable> worstLossesBaseIM = getNWorstLosses(evaluationDate, localPortfolio, zeroRateModel, dataContainer, simulationBase, NUMBER_OF_FULL_REVALUATIONS, pathNumber);
				List<RandomVariable> worstLossesFloorIM = getNWorstLosses(evaluationDate, localPortfolio, zeroRateModel, dataContainer, simulationFloor, NUMBER_OF_FULL_REVALUATIONS, pathNumber);
				double baseInitialMargin = getInitialMarginFromLosses(worstLossesBaseIM, simulationBase).get(pathNumber);
				double floorInitialMargin = getInitialMarginFromLosses(worstLossesFloorIM, simulationFloor).get(pathNumber);
				initialMarginRealizations[pathNumber] = baseInitialMargin > floorInitialMargin ? baseInitialMargin : floorInitialMargin;
			}
			double filtrationTime = FloatingpointDate.getFloatingPointDateFromDate(zeroRateModel.getReferenceDate(), evaluationDate);
			return new RandomVariableFromDoubleArray(filtrationTime, initialMarginRealizations);
		}
		// Approximation over all paths, therefore pathNumber = null
		List<RandomVariable> worstLossesBaseIM = getNWorstLosses(evaluationDate, localPortfolio, zeroRateModel, dataContainer, simulationBase, NUMBER_OF_FULL_REVALUATIONS, null);
		List<RandomVariable> worstLossesFloorIM = getNWorstLosses(evaluationDate, localPortfolio, zeroRateModel, dataContainer, simulationFloor, NUMBER_OF_FULL_REVALUATIONS, null);
		RandomVariable baseInitialMargin = getInitialMarginFromLosses(worstLossesBaseIM, Simulation.EXPECTED_SHORTFALL_6);
		RandomVariable floorInitialMargin = getInitialMarginFromLosses(worstLossesFloorIM, Simulation.VALUE_AT_RISK);	
		return baseInitialMargin.getAverage() > floorInitialMargin.getAverage() ? baseInitialMargin : floorInitialMargin;
	}
	
	
	// Calculates the sensitivities of the swap portfolio and stores them in the IM data container
	private static void getSensitivities(LocalDateTime evaluationDate, ZeroRateModel zeroRateModel, LocalPortfolio localPortfolio, InitialMarginContainer initialMarginContainer) throws CalculationException {
		// 1. Get sensitivity components for all swap legs with same currency
		SensitivityComponentsForSwapLeg sensitivityComponents = new SensitivityComponentsForSwapLeg(localPortfolio.getSwaps(), zeroRateModel, zeroRateModel);
		// Pairs IM uses the same standardized curve for deriving sensitivities w.r.t forward rate changes and discount factor changes
		// -> Forward and discount risk can be combined and evaluated under the same scenarios
		// 2. Calculate delta and gamma sensitivities of the portfolio under the same curve
		Sensitivities sensitivities = new ForwardAndDiscountSensitivities(sensitivityComponents, TenorGridFactory.getTenorGrid(GridType.INITIAL_MARGIN_RISK_GRID)); // Risk is sampled on the "fixed grid in zero space"
		SensitivityMatrix delta = sensitivities.getDeltaSensitivities(evaluationDate);
		SensitivityMatrix gamma = sensitivities.getGammaSensitivities(evaluationDate);
		// Add results to the initial margin data container
		initialMarginContainer.setDeltaSensitivities(delta);
		initialMarginContainer.setGammaSensitivities(gamma);
	}
	
	
	
	/* TODO: Currency conversion to base currency
	 * Sensitivities are calculated with respect to the Zero Coupon Rate Curve.
	 * SwapClear separates the sensitivities into forward and discount sensitivities. 
	 * Currently: If sensitivities rely on the same curve, the discount factors are handled as constants when calculating forward sensitivities and vice versa.
	 */ 
	// Calculates the PNL of the swap portfolio (Taylor) and stores them in the IM data container together with the curve scenarios
	private static void getPnLAndCurveScenarios(LocalDateTime evaluationDate, ZeroRateModel zeroRateModel, LocalPortfolio localPortfolio, InitialMarginContainer initialMarginContainer, Simulation simulation, boolean movingScenarioWindow) throws CalculationException {	
		// if movingScenarioWindow == false we restrict the scenario window to historical scenarios only -> the scenarios are not moving with the model
		if(!movingScenarioWindow) {
			evaluationDate = zeroRateModel.getReferenceDate();
		}
		ScenarioFactory scenarios = new ScenarioFactory(zeroRateModel, simulation);	
		// Store tenor point scenarios to construct the shifted curves for full swap revaluation after PnL comparison
		CurveScenarios curveScenarios = new CurveScenarios();
		// Directly aggregated element-wise PnL calculations
		RandomVariableSeries pnLSeries = new RandomVariableSeries();
		// We map forward and discount sensitivities to the same matrix -> gamma matrix contains all necessary fixing points
		// Loop through all rows of the matrix
		for (Map.Entry<Double, TreeMap<Double, RandomVariable>> row : initialMarginContainer.getGammaSensitivities().getSensitivityMatrix().entrySet()) {
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
			pnLSeries.sum(getFirstOrderTaylorTerm(rowScenarios, initialMarginContainer.getDeltaSensitivities().getSensitivityMatrix().get(row.getKey()).get(row.getKey()))); // Delta
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
		// Add results to the initial margin data container
		initialMarginContainer.setCurveScenarios(simulation, curveScenarios);
		initialMarginContainer.setPnLSeries(simulation, pnLSeries);
	}
	
	
	/*
	 * The Taylor expansion for the PnL calculation is a vector operation.
	 * The entries of the vector are the pillar points on which the scenarios and sensitivities are sampled.
	 * This method equals the element-wise multiplication of the i-th entries
	 */
	private static RandomVariableSeries getFirstOrderTaylorTerm(RandomVariableSeries tenorPointScenario, RandomVariable sensitivity) {
		HashMap<LocalDateTime, RandomVariable> pnlOnTenorPoint = new HashMap<>();
		for (Map.Entry<LocalDateTime, RandomVariable> shift : tenorPointScenario.getSeries().entrySet()) {
			pnlOnTenorPoint.put(shift.getKey(), shift.getValue().mult(DECIMAL_TO_BASIS_POINT_CONVERSION_FACTOR).mult(sensitivity));
		}
		return new RandomVariableSeries(pnlOnTenorPoint);
	}

	
	private static RandomVariableSeries getSecondOrderTaylorTerm(RandomVariableSeries firstTenorPointScenario, RandomVariableSeries secondTenorPointScenario, RandomVariable sensitivity) {
		HashMap<LocalDateTime, RandomVariable> pnlOnTenorPoint = new HashMap<>();
		for (Map.Entry<LocalDateTime, RandomVariable> shift : firstTenorPointScenario.getSeries().entrySet()) {
			RandomVariable secondShift = secondTenorPointScenario.getSeries().get(shift.getKey());
			pnlOnTenorPoint.put(shift.getKey(), shift.getValue().mult(secondShift).mult(DECIMAL_TO_BASIS_POINT_CONVERSION_FACTOR * DECIMAL_TO_BASIS_POINT_CONVERSION_FACTOR).mult(sensitivity).mult(0.5));
		}
		return new RandomVariableSeries(pnlOnTenorPoint);
	}
	
	
	// Total revaluation of the portfolio under the n worst scenerios
	private static List<RandomVariable> getNWorstLosses(LocalDateTime evaluationDate, LocalPortfolio localPortfolio, ZeroRateModel zeroRateModel, InitialMarginContainer initialMarginContainer, Simulation simulation, int numberOfLosses, Integer pathOrState) throws CalculationException {
		List<RandomVariable> worstLosses = new ArrayList<RandomVariable>(Collections.nCopies(numberOfLosses, new Scalar(0.0)));
		List<LocalDateTime> worstDates = initialMarginContainer.getPnlSeries(simulation).getNLowestValues(numberOfLosses, pathOrState); 
		for (LchSwapLeg swapLeg : localPortfolio.getSwaps()) {
			RandomVariable netPresentValue = swapLeg.getValue(evaluationDate, zeroRateModel, null, pathOrState); // no shift
			// If the evaluationDate is after the last paymentDate of the portfolio, all payments have been made <-> all swaps have value zero 
			// -> swap has no sensitivity -> PnL calculation does not return any scenario -> call of shifted value results in an error due to missing scenarios
			double modelTime = FloatingpointDate.getFloatingPointDateFromDate(zeroRateModel.getReferenceDate(), evaluationDate);
			if (modelTime >= localPortfolio.getLastPaymentDate()) {
				return worstLosses;
			}
			for (int index = 0; index < numberOfLosses; index++) {
				// get shifted value for elements of worstDates on all paths
				RandomVariable shiftedNetPresentValue = swapLeg.getValue(evaluationDate, zeroRateModel, initialMarginContainer.getCurveScenarios(simulation).getCurveShifts(worstDates.get(index)), pathOrState); 
				// directly update / sum up the losses of all swap legs: NPV_s - NPV 
				worstLosses.set(index, worstLosses.get(index).add(shiftedNetPresentValue.sub(netPresentValue))); 
			}
		}
		return worstLosses;
	}

	
	// Aggregation of the worst losses
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
