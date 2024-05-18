package net.finmath.lch.initialmargin.swapclear.libormodels;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.HashMap;
import java.util.Map;

import net.finmath.lch.initialmargin.swapclear.aggregationscheme.SchemeStorage.CurveName;
import net.finmath.exception.CalculationException;
import net.finmath.marketdata.model.AnalyticModel;
import net.finmath.marketdata.model.AnalyticModelFromCurvesAndVols;
import net.finmath.marketdata.model.curves.Curve;
import net.finmath.marketdata.model.curves.DiscountCurve;
import net.finmath.marketdata.model.curves.DiscountCurveInterpolation;
import net.finmath.marketdata.model.curves.ForwardCurve;
import net.finmath.marketdata.model.curves.ForwardCurveFromDiscountCurve;
import net.finmath.marketdata.model.curves.CurveInterpolation.ExtrapolationMethod;
import net.finmath.marketdata.model.curves.CurveInterpolation.InterpolationEntity;
import net.finmath.marketdata.model.curves.CurveInterpolation.InterpolationMethod;
import net.finmath.montecarlo.BrownianMotion;
import net.finmath.montecarlo.BrownianMotionFromMersenneRandomNumbers;
import net.finmath.montecarlo.RandomVariableFromArrayFactory;
import net.finmath.montecarlo.interestrate.CalibrationProduct;
import net.finmath.montecarlo.interestrate.LIBORMarketModel;
import net.finmath.montecarlo.interestrate.LIBORModelMonteCarloSimulationModel;
import net.finmath.montecarlo.interestrate.LIBORMonteCarloSimulationFromLIBORModel;
import net.finmath.montecarlo.interestrate.models.LIBORMarketModelFromCovarianceModel;
import net.finmath.montecarlo.interestrate.models.covariance.AbstractLIBORCovarianceModelParametric;
import net.finmath.montecarlo.interestrate.models.covariance.BlendedLocalVolatilityModel;
import net.finmath.montecarlo.interestrate.models.covariance.LIBORCorrelationModel;
import net.finmath.montecarlo.interestrate.models.covariance.LIBORCorrelationModelExponentialDecay;
import net.finmath.montecarlo.interestrate.models.covariance.LIBORCovarianceModelFromVolatilityAndCorrelation;
import net.finmath.montecarlo.interestrate.models.covariance.LIBORVolatilityModel;
import net.finmath.montecarlo.interestrate.models.covariance.LIBORVolatilityModelPiecewiseConstant;
import net.finmath.montecarlo.process.EulerSchemeFromProcessModel;
import net.finmath.time.TimeDiscretizationFromArray;



public class ModelFactory {
	
	// date up to which historical zero rate data is available
	public static final LocalDateTime REFERENCE_DATE =  LocalDate.of(2024,  Month.FEBRUARY, 5).atStartOfDay();
		
		
	public static LIBORModelMonteCarloSimulationModel getModel(CurveName curveName) throws CalculationException {
		switch (curveName) {
		case EUR_EURIBOR_3M:
			final int numberOfPaths = 1000;
			final int numberOfFactors = 1;
			final double[] zeroRateFixings = new double[] {0.0, 0.2493151, 0.4986301, 0.7479452, 1.0000000, 1.4986301, 2.0000000, 3.0000000, 4.0000000, 5.0000000, 6.0000000, 7.0000000, 8.0000000, 9.0000000, 10.0000000, 12.0000000, 15.0000000, 20.0000000, 25.0000000, 30.0000000, 35.0000000, 40.0000000, 50.0000000, 60.0000000};	
			final double[] zeroRates = new double[]{0.0, 0.039679454, 0.038092025, 0.036301049, 0.034411283, 0.03127043, 0.029134774, 0.026978123, 0.026084691, 0.02578301, 0.025724468, 0.025808707, 0.025986709, 0.026228892, 0.026532052, 0.02712804, 0.02767065, 0.027235979, 0.026004347, 0.024754381, 0.023677185, 0.022869038, 0.021163335, 0.019995148};
			final double liborRateTimeHorizon = 40.0;
			final double liborPeriodLength = 0.25;
			final double simulationTimeStep = 0.0625;
			double[] volatility = new double[] {0.018188757085591742, 0.023669136470620522, 0.018838312991427623, 0.017398721686694832, 0.016157134065584788, 0.012580974937947433, 0.03715426781325657, 0.005, 0.011789390800298272, 0.023835171110197697, 0.017407408943930395, 0.01647740433369315, 0.014694957571607822, 0.012702232360660029, -0.011628112608719269, 0.014145444499685772, 0.021064553112378974, 0.01704151002264071, 0.016478221525674207, 0.013046920133543867, 0.011028286318128655, 0.003453754464521545, -2.3004198146720653E-4, 0.027332804579421253, 0.014488506318889972, 0.013199204327977335, 0.008914059002711453, 0.008878468292101021, 0.012214531788801015, 0.004927497343985883, 0.030667639991621958, 0.01396130678001358, 0.0152163862643317, 0.008136714960180556, 0.002349377408815333, 0.005, -0.007070862361627261, -0.006351041032061278, 0.026574244710620715, 0.01169447786782754, 0.0042076388094646954, 0.005, 0.005, 0.005, 0.005, 0.005, 0.005, 0.005};
			double displacementParameter =  0.5077918637651763;
			final AnalyticModel curveModel = getAnalyticModel(curveName, zeroRateFixings, zeroRates);
			return createLIBORMarketModel(numberOfPaths, numberOfFactors, liborRateTimeHorizon, liborPeriodLength, simulationTimeStep, curveModel, curveName, volatility, displacementParameter); 
		// standard EUR curve should be used for Base & Floor IM
		// case EUR_EURIBOR: 
		default:
			throw new IllegalArgumentException("Curve " + curveName.toString() + " currently not implemented.");
		}

	}
	
	
	public static LIBORModelMonteCarloSimulationModel createLIBORMarketModel(
			final int numberOfPaths, final int numberOfFactors, 
			final double liborRateTimeHorzion, final double liborPeriodLength, final double simulationTimeStep, 
			final AnalyticModel curveModel, CurveName curveName, double[] volatility, double displacementParameter) throws CalculationException {

		final ForwardCurve forwardCurve = curveModel.getForwardCurve("ForwardCurveFromDiscountCurve("+ curveName.toString() +",3M)");
		final DiscountCurve discountCurve = curveModel.getDiscountCurve(CurveName.EUR_EURIBOR_3M.toString());
		
		/*
		 * Create a simulation time discretization
		 */
		// If simulation time is below libor time, exceptions will be hard to track.
		double lastTime = liborRateTimeHorzion;
		double dt = simulationTimeStep;
		TimeDiscretizationFromArray timeDiscretizationFromArray = new TimeDiscretizationFromArray(0.0, (int) (lastTime / dt), dt);

		/*
		 * Create the libor tenor structure and the initial values
		 */
		TimeDiscretizationFromArray liborPeriodDiscretization = new TimeDiscretizationFromArray(0.0, (int) (liborRateTimeHorzion / liborPeriodLength), liborPeriodLength);

		/*
		 * Create Brownian motions
		 */
		final BrownianMotion brownianMotion = new BrownianMotionFromMersenneRandomNumbers(timeDiscretizationFromArray, numberOfFactors, numberOfPaths, 31415 /* seed */);

		// Create a volatility model: Piecewise constant volatility
		LIBORVolatilityModel volatilityModel = new LIBORVolatilityModelPiecewiseConstant(new RandomVariableFromArrayFactory(), timeDiscretizationFromArray, liborPeriodDiscretization, new TimeDiscretizationFromArray(0.00, 1.0, 2.0, 5.0, 10.0, 20.0, 30.0, 40.0), new TimeDiscretizationFromArray(0.00, 1.0, 2.0, 5.0, 10.0, 20.0, 30.0, 40.0), volatility, false);

		// Create a correlation model
		LIBORCorrelationModel correlationModel = new LIBORCorrelationModelExponentialDecay(timeDiscretizationFromArray, liborPeriodDiscretization, numberOfFactors, 0.05, false);

		// Create a covariance model
		AbstractLIBORCovarianceModelParametric covarianceModelParametric = new LIBORCovarianceModelFromVolatilityAndCorrelation(timeDiscretizationFromArray, liborPeriodDiscretization, volatilityModel, correlationModel);

		// Create blended local volatility model with fixed parameter 0.0 (that is "lognormal").
		AbstractLIBORCovarianceModelParametric covarianceModelBlended = new BlendedLocalVolatilityModel(new RandomVariableFromArrayFactory(), covarianceModelParametric, displacementParameter, false);

		// Set model properties
		final Map<String, String> properties = new HashMap<>();

		// Choose the simulation measure
		properties.put("measure", LIBORMarketModelFromCovarianceModel.Measure.SPOT.name());

		// Choose log normal model
		properties.put("stateSpace", LIBORMarketModelFromCovarianceModel.StateSpace.NORMAL.name());

		// Empty array of calibration items - hence, model will use given covariance
		final CalibrationProduct[] calibrationItems = new CalibrationProduct[0];

		/*
		 * Create corresponding LIBOR Market Model
		 */		
		final LIBORMarketModel liborMarketModel = new LIBORMarketModelFromCovarianceModel(
				liborPeriodDiscretization, curveModel, forwardCurve, discountCurve, 
				new RandomVariableFromArrayFactory(), covarianceModelBlended, calibrationItems, properties);

		final EulerSchemeFromProcessModel process = new EulerSchemeFromProcessModel(liborMarketModel, brownianMotion);

		return new LIBORMonteCarloSimulationFromLIBORModel(process);
	}

	
	public static AnalyticModel getAnalyticModel(CurveName curveName, double[] zeroRateFixings, double[] zeroRates) {
		// Create a discount curve
		final DiscountCurveInterpolation discountCurveInterpolation = DiscountCurveInterpolation.createDiscountCurveFromZeroRates(
				curveName.toString() /* name */,
				REFERENCE_DATE.toLocalDate()	/* referenceDate */,
				zeroRateFixings	/* maturities */,
				zeroRates		/* rates */,
				InterpolationMethod.LINEAR,
				ExtrapolationMethod.CONSTANT,
				InterpolationEntity.LOG_OF_VALUE
				);
		
		// Single curve setup
		final ForwardCurve	forwardCurveFromDiscountCurve = new ForwardCurveFromDiscountCurve(
				discountCurveInterpolation.getName(),
				REFERENCE_DATE.toLocalDate(),
				"3M");
		
		return new AnalyticModelFromCurvesAndVols(new Curve[] { discountCurveInterpolation, forwardCurveFromDiscountCurve });
	}
	
}
