package net.finmath.initialmargin.isdasimm;

import net.finmath.montecarlo.BrownianMotion;
import net.finmath.montecarlo.RandomVariableFactory;
import net.finmath.montecarlo.interestrate.LIBORMarketModel;
import net.finmath.montecarlo.interestrate.LIBORModelInterface;
import net.finmath.montecarlo.interestrate.LIBORModelMonteCarloSimulation;
import net.finmath.montecarlo.interestrate.LIBORModelMonteCarloSimulationInterface;
import net.finmath.montecarlo.process.ProcessEulerScheme;
import net.finmath.time.TimeDiscretizationFromArray;

public class SIMMUnitTests {

	public LIBORModelMonteCarloSimulationInterface getDummySimulation() throws Exception {
		double lastTime = 30.0;
		double dt = 0.1;
		TimeDiscretizationFromArray timeDiscretizationFromArray = new TimeDiscretizationFromArray(0.0, (int) (lastTime / dt), dt);

		/*
		 * Create the libor tenor structure and the initial values
		 */
		final int numberOfPaths = 1;
		final int numberOfFactors = 1;

		double liborPeriodLength = 0.5;
		double liborRateTimeHorzion = 30.0;
		TimeDiscretizationFromArray liborPeriodDiscretization = new TimeDiscretizationFromArray(0.0, (int) (liborRateTimeHorzion / liborPeriodLength), liborPeriodLength);
		final BrownianMotion brownianMotion = new net.finmath.montecarlo.BrownianMotionLazyInit(timeDiscretizationFromArray, numberOfFactors, numberOfPaths, 31415 /* seed */);
		LIBORModelInterface liborMarketModelCalibrated = new LIBORMarketModel(
				liborPeriodDiscretization,
				null,
				null, null,
				new RandomVariableFactory(), // No AAD here
				null,
				null,
				null);
		ProcessEulerScheme process = new ProcessEulerScheme(brownianMotion);
		LIBORModelMonteCarloSimulationInterface simulationCalibrated = new LIBORModelMonteCarloSimulation(liborMarketModelCalibrated, process);
		return simulationCalibrated;
	}
}
