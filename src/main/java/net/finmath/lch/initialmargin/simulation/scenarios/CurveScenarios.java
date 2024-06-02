package net.finmath.lch.initialmargin.simulation.scenarios;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import net.finmath.lch.initialmargin.simulation.modeldata.RandomVariableSeries;
import net.finmath.lch.initialmargin.simulation.modeldata.StochasticCurve;


/**
 * Collects individual tenor point time series and builds curve scenarios (collection of shifts for different tenor points).
 * Each time series represents the shifts (RandomVariableSeries), depending on the scenario type, of that tenor point (Key:Double).
 * 
 * @author Raphael Prandtl
 */
public class CurveScenarios {
	
	
	private final HashMap<Double, RandomVariableSeries> curveScenarios;
	
	
	public CurveScenarios(HashMap<Double, RandomVariableSeries> curveScenarios) {
		this.curveScenarios = curveScenarios;
	}

	
	public CurveScenarios() {
		this.curveScenarios = new HashMap<>();
	}
	
	
	/**
	 * 
	 * @param fixing The fixing for the tenor point series
	 * @param randomVariableSeries The time series of shifts
	 */
	public void addSeries(Double fixing, RandomVariableSeries randomVariableSeries) {
		curveScenarios.put(fixing, randomVariableSeries);
	}
	
	
	/**
	 * @param fixing The fixing of the randomVariableSeries
	 * @return The series if it is contained in the CurveScenarios
	 */
	public RandomVariableSeries getSeries(Double fixing) {
		return curveScenarios.get(fixing);
	}
	
	
	/**
	 * @param fixing The fixing of the randomVariableSeries
	 * @return True if the series is available
	 */
	public boolean containsSeries(Double fixing) {
		return curveScenarios.containsKey(fixing);
	}
	

	/**
	 * Builds a StochasticCurve for the available fixing points consisting of the shifts.
	 *
	 * @param evaluationDate The date for the curve shift
	 * @return The shifts as a curve (TreeMap<double, RandomVariable>)
	 */
	//Used for total and path-wise evaluation since path-wise filtering is done in ZeroRateModel
	public StochasticCurve getCurveShifts(LocalDateTime evaluationDate) {
		StochasticCurve curveShifts = new StochasticCurve();
		for (Map.Entry<Double, RandomVariableSeries> series : curveScenarios.entrySet()) {
			curveShifts.addRate(series.getKey(), series.getValue().getValue(evaluationDate));
		}
		return curveShifts;
	}
	
	
	/**
	 * 
	 * @return The complete time series of curve shifts on each fixing.
	 */
	public HashMap<Double, RandomVariableSeries> getCurveScenarios() {
		return curveScenarios;
	}
	
}
