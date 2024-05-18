package net.finmath.lch.initialmargin.swapclear.sensitivities;


import net.finmath.lch.initialmargin.simulation.modeldata.StochasticCurve;
import net.finmath.lch.initialmargin.simulation.modeldata.TenorGrid;


/**
 * Calculates the sensitivities of a swap component w.r.t. the forward rate and discount factors, both mapped to the same curve.
 */
public class ForwardAndDiscountSensitivities extends AbstractSensitvities {

    private ForwardSensitivities forwardSensitivities;
    private DiscountSensitivities discountSensitivities;

    
    public ForwardAndDiscountSensitivities(SensitivityComponentsForSwapLeg sensitivityComponentsForSwapLeg, TenorGrid tenorGrid) {
        super(sensitivityComponentsForSwapLeg, tenorGrid);
        forwardSensitivities = new ForwardSensitivities(sensitivityComponentsForSwapLeg, tenorGrid);
        discountSensitivities = new DiscountSensitivities(sensitivityComponentsForSwapLeg, tenorGrid);
    }

    
    @Override
    protected void calculateDeltaSensitivity(StochasticCurve sensitivityCurve, SensitivityComponent component) {
        forwardSensitivities.calculateDeltaSensitivity(sensitivityCurve, component);
        discountSensitivities.calculateDeltaSensitivity(sensitivityCurve, component);
    }

    
    @Override
    protected void calculateGammaSensitivity(StochasticCurve sensitivityCurve, SensitivityComponent component) {
        forwardSensitivities.calculateGammaSensitivity(sensitivityCurve, component);
        discountSensitivities.calculateGammaSensitivity(sensitivityCurve, component);
    }
    
}
