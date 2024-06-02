package net.finmath.lch.initialmargin.swapclear.sensitivities;


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
    protected void calculateDeltaSensitivity(SensitivityMatrix sensitivityMatrix, SensitivityComponent component) {
        forwardSensitivities.calculateDeltaSensitivity(sensitivityMatrix, component);
        discountSensitivities.calculateDeltaSensitivity(sensitivityMatrix, component);
    }

    
    @Override
    protected void calculateGammaSensitivity(SensitivityMatrix sensitivityMatrix, SensitivityComponent component) {
        forwardSensitivities.calculateGammaSensitivity(sensitivityMatrix, component);
        discountSensitivities.calculateGammaSensitivity(sensitivityMatrix, component);
    }
    
}
