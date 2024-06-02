package net.finmath.lch.initialmargin.swapclear.aggregationscheme;

import java.util.HashMap;

import net.finmath.lch.initialmargin.simulation.modeldata.RandomVariableSeries;
import net.finmath.lch.initialmargin.simulation.scenarios.CurveScenarios;
import net.finmath.lch.initialmargin.simulation.scenarios.ScenarioFactory.Simulation;
import net.finmath.lch.initialmargin.swapclear.sensitivities.SensitivityMatrix;


/**
 * Basic class to store and retrieve data needed during the calculation of Initial Margin
 */
public class InitialMarginContainer {
	
    private HashMap<Simulation, RandomVariableSeries>  		pnlSeries;
    private HashMap<Simulation, CurveScenarios>  			curveScenarios;
    private SensitivityMatrix 								deltaSensitivities;
    private SensitivityMatrix	 							gammaSensitivities;



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
    
    
    public void setDeltaSensitivities(SensitivityMatrix deltaSensitivities) {
    	this.deltaSensitivities = deltaSensitivities;
    }    
    
    
    public void setGammaSensitivities(SensitivityMatrix gammaSensitivities) {
    	this.gammaSensitivities = gammaSensitivities;
    }
    
    
    public RandomVariableSeries getPnlSeries(Simulation simulation) {
        return pnlSeries.get(simulation);
    }

    
    public CurveScenarios getCurveScenarios(Simulation simulation) {
        return curveScenarios.get(simulation);
    }

    
    public SensitivityMatrix getDeltaSensitivities() {
    	return deltaSensitivities;
    }
    
    
    public SensitivityMatrix getGammaSensitivities() {
    	return gammaSensitivities;
    }
    
}
