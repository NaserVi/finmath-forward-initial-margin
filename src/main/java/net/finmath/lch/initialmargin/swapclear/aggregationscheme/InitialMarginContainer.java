package net.finmath.lch.initialmargin.swapclear.aggregationscheme;

import java.util.HashMap;

import net.finmath.lch.initialmargin.simulation.modeldata.RandomVariableSeries;
import net.finmath.lch.initialmargin.simulation.modeldata.StochasticCurve;
import net.finmath.lch.initialmargin.simulation.scenarios.CurveScenarios;
import net.finmath.lch.initialmargin.simulation.scenarios.ScenarioFactory.Simulation;


/**
 * Basic class to store and retrieve data needed during the calculation of Initial Margin
 */
public class InitialMarginContainer {
	
    private HashMap<Simulation, RandomVariableSeries>  		pnlSeries;
    private HashMap<Simulation, CurveScenarios>  			curveScenarios;
    private StochasticCurve 								deltaSensitivities;
    private StochasticCurve	 								gammaSensitivities;



    public InitialMarginContainer() {
    	this.pnlSeries = new HashMap<>();
    	this.curveScenarios = new HashMap<>();
    	this.deltaSensitivities = null;
    	this.gammaSensitivities = null;
    }
    
    
    public void setPnLSeries(Simulation simulation, RandomVariableSeries pnlSeries) {
    	this.pnlSeries.put(simulation, pnlSeries);
    }
    
    
    public void setCurveScenarios(Simulation simulation, CurveScenarios curveScenarios) {
    	this.curveScenarios.put(simulation, curveScenarios);
    }
    
    
    public void setDeltaSensitivities(StochasticCurve deltaSensitivities) {
    	this.deltaSensitivities = deltaSensitivities;
    }    
    
    
    public void setGammaSensitivities(StochasticCurve gammaSensitivities) {
    	this.gammaSensitivities = gammaSensitivities;
    }
    
    
    public RandomVariableSeries getPnlSeries(Simulation simulation) {
        return pnlSeries.get(simulation);
    }

    
    public CurveScenarios getCurveScenarios(Simulation simulation) {
        return curveScenarios.get(simulation);
    }

    
    public StochasticCurve getDeltaSensitivities() {
    	return deltaSensitivities;
    }
    
    
    public StochasticCurve getGammaSensitivities() {
    	return gammaSensitivities;
    }
    
}
