package net.finmath.lch.initialmargin.simulation.modeldata;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.NavigableMap;
import java.util.TreeMap;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import net.finmath.exception.CalculationException;
import net.finmath.stochastic.Scalar;


/**
 * Used for transforming Excel-based input data (double) to stochastic curves (Scalar).
 * The input file consists of curve fixing points in the first column, dates in the first row in descending order, and for each date and fixing point the zero rate value of that date.
 * 
 * @author Raphael Prandtl
 *
 */
public class ZeroCurveHistory {

	private final String 					inputDataFilePath;
	
	
	public ZeroCurveHistory(final String inputDataFilePath) throws CalculationException {
		this.inputDataFilePath = inputDataFilePath;    
	}
	
	
	/**
	 * Transforms the input data to a time series of StochasticCurve.
	 * The historical data is of course not stochastic, the RandomVariable type is chosen to create a seamless transition of historical rates and model generated rates.
	 * @return The historical curve time series
	 * @throws CalculationException
	 */
	public NavigableMap<LocalDateTime, StochasticCurve> buildZeroCurveHistory() throws CalculationException { 
		NavigableMap<LocalDateTime, StochasticCurve> zeroCurves = new TreeMap<>();
		try (FileInputStream fileInput = new FileInputStream(new File(inputDataFilePath));
				Workbook workbook = WorkbookFactory.create(fileInput)) {
			
			Sheet sheet = workbook.getSheetAt(0);
	        ArrayList<LocalDateTime> dates = getDays(sheet);
	        // first row contains the date, following rows contain the rate series
	        for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
	            Row rateSeries = sheet.getRow(rowIndex);
	            double maturity = rateSeries.getCell(0).getNumericCellValue();
		        addRatesToCurves(zeroCurves, maturity, rateSeries, dates);		      
			}	

		} catch (IOException e) {
	        throw new CalculationException("Failed to build rate data from input file.", e);
		} 
		return zeroCurves;
	}


	private void addRatesToCurves(NavigableMap<LocalDateTime, StochasticCurve> zeroCurves, double maturity, Row rateSeries, ArrayList<LocalDateTime> dates) {
		for (int dayIndex = 0; dayIndex < dates.size(); dayIndex++) {
            double rate = rateSeries.getCell(dayIndex + 1).getNumericCellValue();
            zeroCurves.computeIfAbsent(dates.get(dayIndex), k -> new StochasticCurve()).addRate(maturity, new Scalar(rate));
        }
	}
	
	
	public ArrayList<LocalDateTime> getDays(Sheet sheet) {
		ArrayList<LocalDateTime> dates = new ArrayList<>();
		Row dateRow = sheet.getRow(0);
		// first column contains header
        for (int colIndex = 1; colIndex < dateRow.getLastCellNum(); colIndex++) {
            Cell dateCell = dateRow.getCell(colIndex);
            Date date = dateCell.getDateCellValue();
            LocalDateTime localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            dates.add(localDate); 
        }
        return dates;
	}
	
}
	