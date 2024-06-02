package net.finmath.lch.initialmargin.swapclear.evaluation;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.TreeMap;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

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
import net.finmath.lch.initialmargin.swapclear.evaluation.MarginValuationAdjustment.InitialMargin;
import net.finmath.lch.initialmargin.swapclear.libormodels.ModelFactory;
import net.finmath.lch.initialmargin.swapclear.products.LchSwap;
import net.finmath.lch.initialmargin.swapclear.products.LocalPortfolio;
import net.finmath.lch.initialmargin.swapclear.products.components.LchSwapLeg;
import net.finmath.lch.initialmargin.swapclear.sensitivities.DiscountSensitivities;
import net.finmath.lch.initialmargin.swapclear.sensitivities.ForwardAndDiscountSensitivities;
import net.finmath.lch.initialmargin.swapclear.sensitivities.ForwardSensitivities;
import net.finmath.lch.initialmargin.swapclear.sensitivities.Sensitivities;
import net.finmath.lch.initialmargin.swapclear.sensitivities.SensitivityComponentsForSwapLeg;
import net.finmath.lch.initialmargin.swapclear.sensitivities.SensitivityMatrix;
import net.finmath.exception.CalculationException;
import net.finmath.montecarlo.RandomVariableFromDoubleArray;
import net.finmath.montecarlo.interestrate.LIBORModelMonteCarloSimulationModel;
import net.finmath.stochastic.RandomVariable;
import net.finmath.stochastic.Scalar;
import net.finmath.time.FloatingpointDate;
import net.finmath.time.businessdaycalendar.BusinessdayCalendar;



public class InitialMarginExperiments {
	
	
	// Sensitivity buckets of a 5y swap
	private static final double[] BUCKETS = new double[] {1/365.0, 7/365.0, 14/365.0, 21/365.0, 30/365.0, 60/365.0, 91/365.0, 121/365.0, 152/365.0, 182/365.0, 
			212/365.0, 243/365.0, 273/365.0, 304/365.0, 334/365.0, 365/365.0, 456/365.0, 547/365.0, 638/365.0, 730/365.0, 1095/365.0, 1460/365.0, 1825/365.0, 2190/365.0};
	
	/*
	 * Last date of available zero rate data
	 * LMM's of ModelFactory are calibrated to the same date
	 */
	private static final LocalDateTime REFERENCE_DATE = ModelFactory.REFERENCE_DATE; 

	
	public enum SensitivityType 	{FORWARD, DISCOUNT, FORWARD_AND_DISCOUNT};
	public enum SensitivityOrder 	{DELTA, GAMMA};

	
	public static void main (String[] args) throws CalculationException {
		
//		getSwapValuePaths("D:/Finanzmathematik/Masterthesis/Data/EUR_EURIBOR_3M/5y_Swap_Value_Comparison.xlsx", "Value under ZRM", true);
//		getSwapValuePaths("D:/Finanzmathematik/Masterthesis/Data/EUR_EURIBOR_3M/5y_Swap_Value_Comparison.xlsx", "Value under LMM", false);
//		
//		getSensitivitiesAggregated("D:/Finanzmathematik/Masterthesis/Data/EUR_EURIBOR_3M/5y_Swap_Aggregated_Sensitivities.xlsx", "Discount Delta", SensitivityType.DISCOUNT, SensitivityOrder.DELTA);
//		getSensitivitiesAggregated("D:/Finanzmathematik/Masterthesis/Data/EUR_EURIBOR_3M/5y_Swap_Aggregated_Sensitivities.xlsx", "Discount Gamma", SensitivityType.DISCOUNT, SensitivityOrder.GAMMA);
//		getSensitivitiesAggregated("D:/Finanzmathematik/Masterthesis/Data/EUR_EURIBOR_3M/5y_Swap_Aggregated_Sensitivities.xlsx", "Forward Delta", SensitivityType.FORWARD, SensitivityOrder.DELTA);
//		getSensitivitiesAggregated("D:/Finanzmathematik/Masterthesis/Data/EUR_EURIBOR_3M/5y_Swap_Aggregated_Sensitivities.xlsx", "Forward Gamma", SensitivityType.FORWARD, SensitivityOrder.GAMMA);
//
//		getSensitivitiesBucketWise("D:/Finanzmathematik/Masterthesis/Data/EUR_EURIBOR_3M/5y_Swap_Discount_Delta_Buckets.xlsx", "Sensitivity Buckets", SensitivityType.DISCOUNT, SensitivityOrder.DELTA);
//		getSensitivitiesBucketWise("D:/Finanzmathematik/Masterthesis/Data/EUR_EURIBOR_3M/5y_Swap_Discount_Gamma_Buckets.xlsx", "Sensitivity Buckets", SensitivityType.DISCOUNT, SensitivityOrder.GAMMA);
//		getSensitivitiesBucketWise("D:/Finanzmathematik/Masterthesis/Data/EUR_EURIBOR_3M/5y_Swap_Forward_Delta_Buckets.xlsx", "Sensitivity Buckets", SensitivityType.FORWARD, SensitivityOrder.DELTA);
//		getSensitivitiesBucketWise("D:/Finanzmathematik/Masterthesis/Data/EUR_EURIBOR_3M/5y_Swap_Forward_Gamma_Buckets.xlsx", "Sensitivity Buckets", SensitivityType.FORWARD, SensitivityOrder.GAMMA);
//

//		getInitialMarginPaths("D:/Finanzmathematik/Masterthesis/Data/EUR_EURIBOR_3M/Results/5y_Swap_Floor_Initial_Margin.xlsx", "Average+Fixed", InitialMargin.FLOOR, false, false);
//		getInitialMarginPaths("D:/Finanzmathematik/Masterthesis/Data/EUR_EURIBOR_3M/Results/5y_Swap_Floor_Initial_Margin.xlsx", "Average+Moving", InitialMargin.FLOOR, false, true);
//		getInitialMarginPaths("D:/Finanzmathematik/Masterthesis/Data/EUR_EURIBOR_3M/Results/5y_Swap_Floor_Initial_Margin.xlsx", "Pathwise+Fixed", InitialMargin.FLOOR, true, false);
//		getInitialMarginPaths("D:/Finanzmathematik/Masterthesis/Data/EUR_EURIBOR_3M/Results/5y_Swap_Floor_Initial_Margin.xlsx", "Pathwise+Moving", InitialMargin.FLOOR, true, true);
//
//		getInitialMarginPaths("D:/Finanzmathematik/Masterthesis/Data/EUR_EURIBOR_3M/Results/5y_Swap_Base_Initial_Margin.xlsx", "Average+Fixed", InitialMargin.BASE, false, false);
//		getInitialMarginPaths("D:/Finanzmathematik/Masterthesis/Data/EUR_EURIBOR_3M/Results/5y_Swap_Base_Initial_Margin.xlsx", "Average+Moving", InitialMargin.BASE, false, true);
//		getInitialMarginPaths("D:/Finanzmathematik/Masterthesis/Data/EUR_EURIBOR_3M/Results/5y_Swap_Base_Initial_Margin.xlsx", "Pathwise+Fixed", InitialMargin.BASE, true, false);
//		getInitialMarginPaths("D:/Finanzmathematik/Masterthesis/Data/EUR_EURIBOR_3M/Results/5y_Swap_Base_Initial_Margin.xlsx", "Pathwise+Moving", InitialMargin.BASE, true, true);
//
//		
//		getWorstScenarioDates("D:/Finanzmathematik/Masterthesis/Data/EUR_EURIBOR_3M/5y_Swap_Worst_Scenario_Dates_Base.xlsx", "Base IM Pathwise", Simulation.EXPECTED_SHORTFALL_6, true, true);
//		getWorstScenarioDates("D:/Finanzmathematik/Masterthesis/Data/EUR_EURIBOR_3M/5y_Swap_Worst_Scenario_Dates_Base.xlsx", "Base IM Approximation", Simulation.EXPECTED_SHORTFALL_6, false, true);
//		getWorstScenarioDates("D:/Finanzmathematik/Masterthesis/Data/EUR_EURIBOR_3M/5y_Swap_Worst_Scenario_Dates_Floor.xlsx", "Floor IM Pathwise", Simulation.VALUE_AT_RISK, true, true);
//		getWorstScenarioDates("D:/Finanzmathematik/Masterthesis/Data/EUR_EURIBOR_3M/5y_Swap_Worst_Scenario_Dates_Floor.xlsx", "Floor IM Approximation", Simulation.VALUE_AT_RISK, false, true);
//
//		getIMWithFullRevaluation("D:/Finanzmathematik/Masterthesis/Data/EUR_EURIBOR_3M/5y_Swap_Initial_Margin_Full_Revalue.xlsx", "Base IM Pathwise", Simulation.EXPECTED_SHORTFALL_6, true);
//		getIMWithFullRevaluation("D:/Finanzmathematik/Masterthesis/Data/EUR_EURIBOR_3M/5y_Swap_Initial_Margin_Full_Revalue.xlsx", "Base IM Approximation", Simulation.EXPECTED_SHORTFALL_6, false);
//		getIMWithFullRevaluation("D:/Finanzmathematik/Masterthesis/Data/EUR_EURIBOR_3M/5y_Swap_Initial_Margin_Full_Revalue.xlsx", "Floor IM Pathwise", Simulation.VALUE_AT_RISK, true);
//		getIMWithFullRevaluation("D:/Finanzmathematik/Masterthesis/Data/EUR_EURIBOR_3M/5y_Swap_Initial_Margin_Full_Revalue.xlsx", "Floor IM Approximation", Simulation.VALUE_AT_RISK, false);
//		
	}
	
	
	
	
	public static void getInitialMarginPaths(String filePath, String sheetName, InitialMargin type, boolean pathWiseEvaluation, boolean movingScenarioWindow) throws CalculationException {
		final LIBORModelMonteCarloSimulationModel liborModel = ModelFactory.getModel(CurveName.EUR_EURIBOR_3M);
		final ZeroRateModel zeroRateModel = new ZeroRateModel(Currency.EUR, CurveName.EUR_EURIBOR_3M, liborModel, "D:/Finanzmathematik/Masterthesis/Data/EUR_EURIBOR_3M/EUR_EURIBOR_3M_Input_Data.xlsx");
		
	    LchSwap swap = PortfolioFactory.create5YPayerSwap();
	    LocalPortfolio localPortfolio = PortfolioFactory.getSwapPortfolio(swap).getLocalPortfolio(Currency.EUR); //, swap10Y, swap10YPlusSpread);		
	    
	    ArrayList<LocalDateTime> dates = getDates(localPortfolio.getLastPaymentDate(), SchemeStorage.getLocalCalendar(Currency.EUR));
	    
		try {
            // Check if file exists
            if (!Files.exists(Paths.get(filePath))) {
                System.out.println("File does not exist.");
                return;
            }
            FileInputStream fileIn = new FileInputStream(filePath);
            Workbook workbook = WorkbookFactory.create(fileIn);
            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
            	// Create a new sheet if it doesn't exist
                sheet = workbook.createSheet(sheetName); 
            }
            CellStyle dateStyle = workbook.createCellStyle();
            CreationHelper createHelper = workbook.getCreationHelper();
            dateStyle.setDataFormat(createHelper.createDataFormat().getFormat("yyyy-MM-dd"));

            for (int i = 0; i < dates.size(); i++) {
                Row row = sheet.createRow(i);
                Cell dateCell = row.createCell(0);
                dateCell.setCellValue(dates.get(i));
                dateCell.setCellStyle(dateStyle);

                double[] paths = null;
        		switch (type) {
        		case FLOOR:
        			paths = localPortfolio.getFloorInitialMargin(dates.get(i), zeroRateModel, pathWiseEvaluation, movingScenarioWindow).getRealizations();
        			break;
        		case BASE:
        			paths = localPortfolio.getBaseInitialMargin(dates.get(i), zeroRateModel, pathWiseEvaluation, movingScenarioWindow).getRealizations();
        			break;
        		case PAIRS:
        			paths = localPortfolio.getBaseInitialMargin(dates.get(i), zeroRateModel, pathWiseEvaluation, movingScenarioWindow).getRealizations();
        			break;
        		default:
        			throw new IllegalArgumentException("Initial Margin type not defined.");
        		}
        		if (paths == null) {
        			row.createCell(1).setCellValue(0);
        		} else {
	                for (int j = 0; j < paths.length; j++) {
	                    row.createCell(j + 1).setCellValue(paths[j]);
	                }
                }	
            }
            // Close the input stream
            fileIn.close();

            // Write the changes back to the file
            FileOutputStream fileOut = new FileOutputStream(filePath);
            workbook.write(fileOut);
            fileOut.close();
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }		
	}
	
	
	public static void getSwapValuePaths(String filePath, String sheetName, boolean valueUnderZeroModel) throws CalculationException {
		final LIBORModelMonteCarloSimulationModel model = ModelFactory.getModel(CurveName.EUR_EURIBOR_3M);
		final ZeroRateModel zeroRateModel = new ZeroRateModel(Currency.EUR, CurveName.EUR_EURIBOR_3M, model, "D:/Finanzmathematik/Masterthesis/Data/EUR_EURIBOR_3M/EUR_EURIBOR_3M_Input_Data.xlsx");

	    LchSwap swap = PortfolioFactory.create5YPayerSwap();
	    ArrayList<LocalDateTime> dates = getDates(swap.getLastPaymentDate(), SchemeStorage.getLocalCalendar(Currency.EUR));

		try {
            // Check if file exists
            if (!Files.exists(Paths.get(filePath))) {
                System.out.println("File does not exist.");
                return;
            }
            FileInputStream fileIn = new FileInputStream(filePath);
            Workbook workbook = WorkbookFactory.create(fileIn);
            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
            	// Create a new sheet if it doesn't exist
                sheet = workbook.createSheet(sheetName); 
            }
            CellStyle dateStyle = workbook.createCellStyle();
            CreationHelper createHelper = workbook.getCreationHelper();
            dateStyle.setDataFormat(createHelper.createDataFormat().getFormat("yyyy-MM-dd"));

            for (int i = 0; i < dates.size(); i++) {
                Row row = sheet.createRow(i);
                Cell dateCell = row.createCell(0);
                dateCell.setCellValue(dates.get(i));
                dateCell.setCellStyle(dateStyle);

                double[] paths;
                if (valueUnderZeroModel) {
                    paths = swap.getValue(dates.get(i), zeroRateModel, null, zeroRateModel, null, null).getRealizations();
                } else {
                	paths = swap.getValue(FloatingpointDate.getFloatingPointDateFromDate(REFERENCE_DATE, dates.get(i)), model).getRealizations();
                }
                for (int j = 0; j < paths.length; j++) {
                    row.createCell(j + 1).setCellValue(paths[j]);
                }
            }
            // Close the input stream
            fileIn.close();

            // Write the changes back to the file
            FileOutputStream fileOut = new FileOutputStream(filePath);
            workbook.write(fileOut);
            fileOut.close();
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }		
	}
	
	
	public static void getSensitivitiesAggregated(String filePath, String sheetName, SensitivityType type, SensitivityOrder order) throws CalculationException {
		final LIBORModelMonteCarloSimulationModel model = ModelFactory.getModel(CurveName.EUR_EURIBOR_3M);
		final ZeroRateModel zeroRateModel = new ZeroRateModel(Currency.EUR, CurveName.EUR_EURIBOR_3M, model, "D:/Finanzmathematik/Masterthesis/Data/EUR_EURIBOR_3M/EUR_EURIBOR_3M_Input_Data.xlsx");

	    LchSwap swap = PortfolioFactory.create5YPayerSwap();
	    LocalPortfolio localPortfolio = PortfolioFactory.getSwapPortfolio(swap).getLocalPortfolio(Currency.EUR);
	    ArrayList<LocalDateTime> dates = getDates(swap.getLastPaymentDate(), SchemeStorage.getLocalCalendar(Currency.EUR));

		SensitivityComponentsForSwapLeg sensitivityComponents = new SensitivityComponentsForSwapLeg(localPortfolio.getSwaps(), zeroRateModel, zeroRateModel);
	    
		try {
            // Check if file exists
            if (!Files.exists(Paths.get(filePath))) {
                System.out.println("File does not exist.");
                return;
            }
            FileInputStream fileIn = new FileInputStream(filePath);
            Workbook workbook = WorkbookFactory.create(fileIn);
            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
            	// Create a new sheet if it doesn't exist
                sheet = workbook.createSheet(sheetName); 
            }
            CellStyle dateStyle = workbook.createCellStyle();
            CreationHelper createHelper = workbook.getCreationHelper();
            dateStyle.setDataFormat(createHelper.createDataFormat().getFormat("yyyy-MM-dd"));

            for (int i = 0; i < dates.size(); i++) {
                Row row = sheet.createRow(i);
                Cell dateCell = row.createCell(0);
                dateCell.setCellValue(dates.get(i));
                dateCell.setCellStyle(dateStyle);

                double bpFactor = 10000;
                if (order.equals(SensitivityOrder.GAMMA)) {
                	bpFactor *= 10000;
                }
                
                SensitivityMatrix sensitivityMatrix = getSensitivityCurve(dates.get(i), zeroRateModel, sensitivityComponents, type, order);
                
				RandomVariable sensitivity = new RandomVariableFromDoubleArray(0.0);
				for (Map.Entry<Double, TreeMap<Double, RandomVariable>> sensitivityRow : sensitivityMatrix.getSensitivityMatrix().entrySet()) {
					for (Map.Entry<Double, RandomVariable> sensitivityColumn : sensitivityRow.getValue().entrySet()) {
						if (sensitivityColumn.getValue() != null) {
							sensitivity = sensitivity.add(sensitivityColumn.getValue());
						}
					}
				}
				double[] paths = sensitivity.mult(bpFactor).getRealizations(); 
				for (int j = 0; j < paths.length; j++) { // 100 paths
					row.createCell(j + 1).setCellValue(paths[j]);
				}
            }
            // Close the input stream
            fileIn.close();

            // Write the changes back to the file
            FileOutputStream fileOut = new FileOutputStream(filePath);
            workbook.write(fileOut);
            fileOut.close();
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	
	public static void getSensitivitiesBucketWise(String filePath, String sheetName, SensitivityType type, SensitivityOrder order) throws CalculationException {
		final LIBORModelMonteCarloSimulationModel model = ModelFactory.getModel(CurveName.EUR_EURIBOR_3M);
		final ZeroRateModel zeroRateModel = new ZeroRateModel(Currency.EUR, CurveName.EUR_EURIBOR_3M, model, "D:/Finanzmathematik/Masterthesis/Data/EUR_EURIBOR_3M/EUR_EURIBOR_3M_Input_Data.xlsx");

	    LchSwap swap = PortfolioFactory.create5YPayerSwap();
	    LocalPortfolio localPortfolio = PortfolioFactory.getSwapPortfolio(swap).getLocalPortfolio(Currency.EUR);
	    ArrayList<LocalDateTime> dates = getDates(swap.getLastPaymentDate(), SchemeStorage.getLocalCalendar(Currency.EUR));

		SensitivityComponentsForSwapLeg sensitivityComponents = new SensitivityComponentsForSwapLeg(localPortfolio.getSwaps(), zeroRateModel, zeroRateModel);	    
		
		try {
            // Check if file exists
            if (!Files.exists(Paths.get(filePath))) {
                System.out.println("File does not exist.");
                return;
            }
            FileInputStream fileIn = new FileInputStream(filePath);
            Workbook workbook = WorkbookFactory.create(fileIn);
            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
            	// Create a new sheet if it doesn't exist
                sheet = workbook.createSheet(sheetName); 
            }
            CellStyle dateStyle = workbook.createCellStyle();
            CreationHelper createHelper = workbook.getCreationHelper();
            dateStyle.setDataFormat(createHelper.createDataFormat().getFormat("yyyy-MM-dd"));

            for (int i = 0; i < dates.size(); i++) {
                Row row = sheet.createRow(i+1);
                Cell dateCell = row.createCell(0);
                dateCell.setCellValue(dates.get(i));
                dateCell.setCellStyle(dateStyle);

                int index = 0;
                double bpFactor = 10000;
                if (order.equals(SensitivityOrder.GAMMA)) {
                	bpFactor *= 10000;
                }
                
                SensitivityMatrix sensitivityMatrix = getSensitivityCurve(dates.get(i), zeroRateModel, sensitivityComponents, type, order);
				for (Map.Entry<Double, TreeMap<Double, RandomVariable>> sensitivityRow : sensitivityMatrix.getSensitivityMatrix().entrySet()) {
					for (double bucket : BUCKETS) {
						if (sensitivityRow.getValue().get(bucket) == null) {
							for (int j = 0; j < 100; j++) { // 100 paths
			                    row.createCell(index + j + 1).setCellValue(0);
							}
						} else {
							double[] pathsPerBucket = sensitivityRow.getValue().get(bucket).mult(bpFactor).getRealizations(); 
							for (int j = 0; j < pathsPerBucket.length; j++) {
								row.createCell(index+ j + 1).setCellValue(pathsPerBucket[j]);
							}
						}
						index += 101;
					}				
				}              
            }
            // Close the input stream
            fileIn.close();

            // Write the changes back to the file
            FileOutputStream fileOut = new FileOutputStream(filePath);
            workbook.write(fileOut);
            fileOut.close();
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }	
	}
	
	
	public static void getWorstScenarioDates(String filePath, String sheetName, Simulation simulation, boolean pathWiseEvaluation, boolean movingScenarioWindow) throws CalculationException {
		final LIBORModelMonteCarloSimulationModel model = ModelFactory.getModel(CurveName.EUR_EURIBOR_3M);
		final ZeroRateModel zeroRateModel = new ZeroRateModel(Currency.EUR, CurveName.EUR_EURIBOR_3M, model, "D:/Finanzmathematik/Masterthesis/Data/EUR_EURIBOR_3M/EUR_EURIBOR_3M_Input_Data.xlsx");

	    LchSwap swap = PortfolioFactory.create5YPayerSwap();
	    LocalPortfolio localPortfolio = PortfolioFactory.getSwapPortfolio(swap).getLocalPortfolio(Currency.EUR);
	    ArrayList<LocalDateTime> dates = getDates(swap.getLastPaymentDate(), SchemeStorage.getLocalCalendar(Currency.EUR));

		SensitivityComponentsForSwapLeg sensitivityComponents = new SensitivityComponentsForSwapLeg(localPortfolio.getSwaps(), zeroRateModel, zeroRateModel);	    
		ForwardAndDiscountSensitivities sensitivities = new ForwardAndDiscountSensitivities(sensitivityComponents, TenorGridFactory.getTenorGrid(GridType.INITIAL_MARGIN_RISK_GRID));
		
		try {
            // Check if file exists
            if (!Files.exists(Paths.get(filePath))) {
                System.out.println("File does not exist.");
                return;
            }
            FileInputStream fileIn = new FileInputStream(filePath);
            Workbook workbook = WorkbookFactory.create(fileIn);
            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
            	// Create a new sheet if it doesn't exist
                sheet = workbook.createSheet(sheetName); 
            }
            CellStyle dateStyle = workbook.createCellStyle();
            CreationHelper createHelper = workbook.getCreationHelper();
            dateStyle.setDataFormat(createHelper.createDataFormat().getFormat("yyyy-MM-dd"));

            if (pathWiseEvaluation) {
                Row row = sheet.createRow(0);
                int pathIndex = 0;
				for(int paths = 0; paths < 100; paths++) {
					row.createCell(pathIndex+1).setCellValue(paths+1);
					pathIndex += 21;
				}
            }
            for (int i = 0; i < dates.size(); i++) {
                Row row = sheet.createRow(i+1);
                Cell dateCell = row.createCell(0);
                dateCell.setCellValue(dates.get(i));
                dateCell.setCellStyle(dateStyle);
				
        		RandomVariableSeries portfolioPnLs = getPnL(dates.get(i), zeroRateModel, sensitivities, simulation, movingScenarioWindow);
				if (pathWiseEvaluation) {
					int index = 0;
					for (int pathNumber = 0; pathNumber < zeroRateModel.getNumberOfPaths(); pathNumber++) {
						List<LocalDateTime> worstScenarioDates = portfolioPnLs.getNLowestValues(20, pathNumber);
						for (int j = 0; j < worstScenarioDates.size(); j++) {
							row.createCell(index + j + 1).setCellValue(worstScenarioDates.get(j));
						}
						index += 21;
					}
				} else {
	        		List<LocalDateTime> worstScenarioDates = portfolioPnLs.getNLowestValues(20, null);
					for (int j = 0; j < worstScenarioDates.size(); j++) {
						row.createCell(j + 1).setCellValue(worstScenarioDates.get(j));
					}
				}
            }
            // Close the input stream
            fileIn.close();

            // Write the changes back to the file
            FileOutputStream fileOut = new FileOutputStream(filePath);
            workbook.write(fileOut);
            fileOut.close();
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }		
	}
	
	
	// Revalues portfolio under all scenarios to check if PnL actually filters for the 20 worst scenarios
	public static void getIMWithFullRevaluation(String filePath, String sheetName, Simulation simulation, boolean pathWiseEvaluation) throws CalculationException {
		final LIBORModelMonteCarloSimulationModel model = ModelFactory.getModel(CurveName.EUR_EURIBOR_3M);
		final ZeroRateModel zeroRateModel = new ZeroRateModel(Currency.EUR, CurveName.EUR_EURIBOR_3M, model, "D:/Finanzmathematik/Masterthesis/Data/EUR_EURIBOR_3M/EUR_EURIBOR_3M_Input_Data.xlsx");

	    LchSwap swap = PortfolioFactory.create5YPayerSwap();
	    LocalPortfolio localPortfolio = PortfolioFactory.getSwapPortfolio(swap).getLocalPortfolio(Currency.EUR);
	    ArrayList<LocalDateTime> dates = getDates(swap.getLastPaymentDate(), SchemeStorage.getLocalCalendar(Currency.EUR));

		SensitivityComponentsForSwapLeg sensitivityComponents = new SensitivityComponentsForSwapLeg(localPortfolio.getSwaps(), zeroRateModel, zeroRateModel);	    
		ForwardAndDiscountSensitivities sensitivities = new ForwardAndDiscountSensitivities(sensitivityComponents, TenorGridFactory.getTenorGrid(GridType.INITIAL_MARGIN_RISK_GRID));
	
		try {
            // Check if file exists
            if (!Files.exists(Paths.get(filePath))) {
                System.out.println("File does not exist.");
                return;
            }
            FileInputStream fileIn = new FileInputStream(filePath);
            Workbook workbook = WorkbookFactory.create(fileIn);
            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
            	// Create a new sheet if it doesn't exist
                sheet = workbook.createSheet(sheetName); 
            }
            CellStyle dateStyle = workbook.createCellStyle();
            CreationHelper createHelper = workbook.getCreationHelper();
            dateStyle.setDataFormat(createHelper.createDataFormat().getFormat("yyyy-MM-dd"));

            if (pathWiseEvaluation) {
                Row row = sheet.createRow(0);
                int pathIndex = 0;
				for(int paths = 0; paths < 100; paths++) {
					row.createCell(pathIndex+1).setCellValue(paths+1);
					pathIndex += 21;
				}
            }
            for (int i = 0; i < dates.size(); i++) {
                Row row = sheet.createRow(i+1);
                Cell dateCell = row.createCell(0);
                dateCell.setCellValue(dates.get(i));
                dateCell.setCellStyle(dateStyle);
				
				double[] initialMarginRealizations = new double[zeroRateModel.getNumberOfPaths()];
				if (pathWiseEvaluation) {
					for (int pathNumber = 0; pathNumber < zeroRateModel.getNumberOfPaths(); pathNumber++) {
						List<RandomVariable> worstLosses = getLossesUnderFullRevaluation(dates.get(i), localPortfolio, zeroRateModel, sensitivities, simulation, pathNumber);
						initialMarginRealizations[pathNumber] = getInitialMarginFromLosses(worstLosses, simulation).get(pathNumber);
					}			
				} else {
	        		List<RandomVariable> worstLosses = getLossesUnderFullRevaluation(dates.get(i), localPortfolio, zeroRateModel, sensitivities, simulation, null);
	        		initialMarginRealizations = getInitialMarginFromLosses(worstLosses, simulation).getRealizations();
				}
				for (int j = 0; j < initialMarginRealizations.length; j++) {
					row.createCell(j + 1).setCellValue(initialMarginRealizations[j]);
				}
            }
            // Close the input stream
            fileIn.close();

            // Write the changes back to the file
            FileOutputStream fileOut = new FileOutputStream(filePath);
            workbook.write(fileOut);
            fileOut.close();
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }		
	}
		
	
	private static SensitivityMatrix getSensitivityCurve(LocalDateTime evaluationDate, ZeroRateModel zeroRateModel, SensitivityComponentsForSwapLeg sensitivityComponents, SensitivityType type, SensitivityOrder order) throws CalculationException {
		Sensitivities sensitivities;
		switch (type) {
		case FORWARD:
			sensitivities = new ForwardSensitivities(sensitivityComponents, TenorGridFactory.getTenorGrid(GridType.INITIAL_MARGIN_RISK_GRID)); // Risk is sampled on the "fixed grid in zero space"
			break;
		case DISCOUNT:
			sensitivities = new DiscountSensitivities(sensitivityComponents, TenorGridFactory.getTenorGrid(GridType.INITIAL_MARGIN_RISK_GRID)); // Risk is sampled on the "fixed grid in zero space"
			break;
		case FORWARD_AND_DISCOUNT:
			sensitivities = new ForwardAndDiscountSensitivities(sensitivityComponents, TenorGridFactory.getTenorGrid(GridType.INITIAL_MARGIN_RISK_GRID)); // Risk is sampled on the "fixed grid in zero space"
			break;
		default:
			throw new IllegalArgumentException("Invalid sensitivity type");
		}

		switch (order) {
		case DELTA:
			return sensitivities.getDeltaSensitivities(evaluationDate);
		case GAMMA:
			return sensitivities.getGammaSensitivities(evaluationDate);
		default:
			throw new IllegalArgumentException("Invalid sensitivity order");
		}
	}
	
	
	private static RandomVariableSeries getPnL(LocalDateTime evaluationDate, ZeroRateModel zeroRateModel, ForwardAndDiscountSensitivities sensitivities, Simulation simulation, boolean movingScenarioWindow) throws CalculationException {	
		// if movingScenarioWindow == false we restrict the scenario window to historical scenarios only -> the scenarios are not moving with the model
		if(!movingScenarioWindow) {
			evaluationDate = zeroRateModel.getReferenceDate();
		}
		ScenarioFactory scenarios = new ScenarioFactory(zeroRateModel, simulation);	
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
		return pnLSeries;
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
	
	
	private static List<RandomVariable> getLossesUnderFullRevaluation(LocalDateTime evaluationDate, LocalPortfolio localPortfolio, ZeroRateModel zeroRateModel, ForwardAndDiscountSensitivities sensitivities, Simulation simulation, Integer pathOrState) throws CalculationException {
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
	
	
	public static ArrayList<LocalDateTime> getDates(double years, BusinessdayCalendar calendar) throws CalculationException {
		LocalDateTime lastDate = FloatingpointDate.getDateFromFloatingPointDate(REFERENCE_DATE, years + 5/365.0); // Additional 5 days
		ArrayList<LocalDateTime> dates = new ArrayList<>();
		for (LocalDateTime date = REFERENCE_DATE; date.isBefore(lastDate) ; date = date.plusDays(1)) {
			if (calendar.isBusinessday(date.toLocalDate())) {
				dates.add(date);
			}
		}
		return dates;
	}
	
	
}
