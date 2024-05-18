package net.finmath.lch.initialmargin.swapclear.libormodels;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.junit.Assert;

import net.finmath.exception.CalculationException;
import net.finmath.marketdata.calibration.ParameterObject;
import net.finmath.marketdata.calibration.Solver;
import net.finmath.marketdata.model.AnalyticModel;
import net.finmath.marketdata.model.AnalyticModelFromCurvesAndVols;
import net.finmath.marketdata.model.curves.Curve;
import net.finmath.marketdata.model.curves.DiscountCurve;
import net.finmath.marketdata.model.curves.DiscountCurveFromForwardCurve;
import net.finmath.marketdata.model.curves.DiscountCurveInterpolation;
import net.finmath.marketdata.model.curves.ForwardCurve;
import net.finmath.marketdata.model.curves.ForwardCurveFromDiscountCurve;
import net.finmath.marketdata.model.curves.CurveInterpolation.ExtrapolationMethod;
import net.finmath.marketdata.model.curves.CurveInterpolation.InterpolationEntity;
import net.finmath.marketdata.model.curves.CurveInterpolation.InterpolationMethod;
import net.finmath.marketdata.products.AnalyticProduct;
import net.finmath.marketdata.products.Deposit;
import net.finmath.marketdata.products.Swap;
import net.finmath.montecarlo.BrownianMotion;
import net.finmath.montecarlo.BrownianMotionFromMersenneRandomNumbers;
import net.finmath.montecarlo.interestrate.CalibrationProduct;
import net.finmath.montecarlo.interestrate.TermStructureModel;
import net.finmath.montecarlo.interestrate.TermStructureMonteCarloSimulationFromTermStructureModel;
import net.finmath.montecarlo.interestrate.TermStructureMonteCarloSimulationModel;
import net.finmath.montecarlo.interestrate.models.LIBORMarketModelWithTenorRefinement;
import net.finmath.montecarlo.interestrate.models.covariance.AbstractLIBORCovarianceModelParametric;
import net.finmath.montecarlo.interestrate.models.covariance.BlendedLocalVolatilityModel;
import net.finmath.montecarlo.interestrate.models.covariance.LIBORCorrelationModel;
import net.finmath.montecarlo.interestrate.models.covariance.LIBORCorrelationModelExponentialDecay;
import net.finmath.montecarlo.interestrate.models.covariance.LIBORCovarianceModelExponentialForm5Param;
import net.finmath.montecarlo.interestrate.models.covariance.LIBORCovarianceModelFromVolatilityAndCorrelation;
import net.finmath.montecarlo.interestrate.models.covariance.LIBORVolatilityModel;
import net.finmath.montecarlo.interestrate.models.covariance.LIBORVolatilityModelPiecewiseConstant;
import net.finmath.montecarlo.interestrate.models.covariance.TermStructCovarianceModelFromLIBORCovarianceModelParametric;
import net.finmath.montecarlo.interestrate.models.covariance.TermStructureCovarianceModelParametric;
import net.finmath.montecarlo.interestrate.models.covariance.TermStructureTenorTimeScaling;
import net.finmath.montecarlo.interestrate.models.covariance.TermStructureTenorTimeScalingPicewiseConstant;
import net.finmath.montecarlo.interestrate.products.AbstractTermStructureMonteCarloProduct;
import net.finmath.montecarlo.interestrate.products.SwaptionSimple;
import net.finmath.montecarlo.process.EulerSchemeFromProcessModel;
import net.finmath.optimizer.OptimizerFactory;
import net.finmath.optimizer.OptimizerFactoryLevenbergMarquardt;
import net.finmath.optimizer.SolverException;
import net.finmath.time.Schedule;
import net.finmath.time.ScheduleGenerator;
import net.finmath.time.TimeDiscretization;
import net.finmath.time.TimeDiscretizationFromArray;
import net.finmath.time.TimeDiscretizationFromArray.ShortPeriodLocation;
import net.finmath.time.businessdaycalendar.BusinessdayCalendarExcludingTARGETHolidays;
import net.finmath.time.daycount.DayCountConvention_ACT_365;

public class LIBORMarketModelWithTenorRefinmentCalibration {

	public static final LocalDateTime REFERENCE_DATE =  LocalDate.of(2024,  Month.FEBRUARY, 5).atStartOfDay();
	
	private static DecimalFormat formatterValue		= new DecimalFormat(" ##0.0000%;-##0.0000%", new DecimalFormatSymbols(Locale.ENGLISH));
	private static DecimalFormat formatterDeviation	= new DecimalFormat(" 0.00000E00;-0.00000E00", new DecimalFormatSymbols(Locale.ENGLISH));

	public static void main(String[] args) throws CalculationException, SolverException {
		
		calibrateModel(1000, 1);
		
	}
	
	public static void calibrateModel(int numberOfPaths, int numberOfFactors) throws CalculationException, SolverException {
		/*
		 * Calibration test
		 */
		System.out.println("Calibration to Swaptions:");

//		final AnalyticModel curveModel = getAnalyticModel(curveName);
//		// Create the forward curve (initial value of the LIBOR market model)
//		final ForwardCurve forwardCurve = curveModel.getForwardCurve("ForwardCurveFromDiscountCurve(" + curveName.toString() + ",null)");
//		final DiscountCurve discountCurve = curveModel.getDiscountCurve(curveName.toString());
		
		final AnalyticModel curveModel = getCalibratedCurve();

		// Create the forward curve (initial value of the LIBOR market model)
		final ForwardCurve forwardCurve = curveModel.getForwardCurve("ForwardCurveFromDiscountCurve(discountCurve-EUR,3M)");

		final DiscountCurve discountCurve = curveModel.getDiscountCurve("discountCurve-EUR");

		/*
		 * Create a set of calibration products.
		 */
		final ArrayList<String>			calibrationItemNames	= new ArrayList<>();
		final ArrayList<CalibrationProduct>	calibrationProducts		= new ArrayList<>();

		final double	swapPeriodLength	= 0.5;

		final String[] atmExpiries = {
				"1M", "1M", "1M", "1M", "1M", "1M", "1M", "1M", "1M", "1M", "1M", "1M", "1M", "1M",
				"3M", "3M", "3M","3M", "3M", "3M", "3M", "3M", "3M", "3M", "3M", "3M", "3M", "3M",
				"6M", "6M", "6M", "6M", "6M", "6M", "6M", "6M", "6M", "6M", "6M", "6M", "6M", "6M", 
				"1Y", "1Y", "1Y", "1Y", "1Y", "1Y", "1Y", "1Y", "1Y", "1Y", "1Y", "1Y", "1Y", "1Y", 
				"2Y", "2Y", "2Y", "2Y", "2Y", "2Y", "2Y", "2Y", "2Y", "2Y", "2Y", "2Y", "2Y", "2Y", 
				"3Y", "3Y", "3Y", "3Y", "3Y", "3Y", "3Y", "3Y", "3Y", "3Y", "3Y", "3Y", "3Y", "3Y",
				"4Y", "4Y", "4Y", "4Y", "4Y", "4Y", "4Y", "4Y", "4Y", "4Y", "4Y", "4Y", "4Y", "4Y", 
				"5Y", "5Y", "5Y", "5Y", "5Y", "5Y", "5Y", "5Y", "5Y", "5Y", "5Y", "5Y", "5Y", "5Y",
				"7Y", "7Y", "7Y", "7Y", "7Y", "7Y", "7Y", "7Y", "7Y", "7Y", "7Y", "7Y", "7Y", "7Y", 
				"10Y", "10Y", "10Y", "10Y", "10Y", "10Y", "10Y", "10Y", "10Y", "10Y", "10Y", "10Y", "10Y", "10Y",
				"15Y", "15Y", "15Y", "15Y", "15Y", "15Y", "15Y", "15Y", "15Y", "15Y", "15Y", "15Y", "15Y", "15Y", 
				"20Y", "20Y", "20Y", "20Y", "20Y", "20Y", "20Y", "20Y", "20Y", "20Y", "20Y", "20Y", "20Y", "20Y",
				"25Y", "25Y", "25Y", "25Y", "25Y", "25Y", "25Y", "25Y", "25Y", "25Y", "25Y", "25Y", "25Y", "25Y",
				"30Y", "30Y", "30Y", "30Y", "30Y", "30Y", "30Y", "30Y", "30Y", "30Y", "30Y", "30Y", "30Y", "30Y"};
		final String[] atmTenors = {
				"1Y", "2Y", "3Y", "4Y", "5Y", "6Y", "7Y", "8Y", "9Y", "10Y", "15Y", "20Y", "25Y", "30Y", 
				"1Y", "2Y", "3Y", "4Y", "5Y", "6Y", "7Y", "8Y", "9Y", "10Y", "15Y", "20Y", "25Y", "30Y", 
				"1Y", "2Y", "3Y", "4Y", "5Y", "6Y", "7Y", "8Y", "9Y", "10Y", "15Y", "20Y", "25Y", "30Y", 
				"1Y", "2Y", "3Y", "4Y", "5Y", "6Y", "7Y", "8Y", "9Y", "10Y", "15Y", "20Y", "25Y", "30Y", 
				"1Y", "2Y", "3Y", "4Y", "5Y", "6Y", "7Y", "8Y", "9Y", "10Y", "15Y", "20Y", "25Y", "30Y", 
				"1Y", "2Y", "3Y", "4Y", "5Y", "6Y", "7Y", "8Y", "9Y", "10Y", "15Y", "20Y", "25Y", "30Y", 
				"1Y", "2Y", "3Y", "4Y", "5Y", "6Y", "7Y", "8Y", "9Y", "10Y", "15Y", "20Y", "25Y", "30Y", 
				"1Y", "2Y", "3Y", "4Y", "5Y", "6Y", "7Y", "8Y", "9Y", "10Y", "15Y", "20Y", "25Y", "30Y", 
				"1Y", "2Y", "3Y", "4Y", "5Y", "6Y", "7Y", "8Y", "9Y", "10Y", "15Y", "20Y", "25Y", "30Y", 
				"1Y", "2Y", "3Y", "4Y", "5Y", "6Y", "7Y", "8Y", "9Y", "10Y", "15Y", "20Y", "25Y", "30Y", 
				"1Y", "2Y", "3Y", "4Y", "5Y", "6Y", "7Y", "8Y", "9Y", "10Y", "15Y", "20Y", "25Y", "30Y", 
				"1Y", "2Y", "3Y", "4Y", "5Y", "6Y", "7Y", "8Y", "9Y", "10Y", "15Y", "20Y", "25Y", "30Y", 
				"1Y", "2Y", "3Y", "4Y", "5Y", "6Y", "7Y", "8Y", "9Y", "10Y", "15Y", "20Y", "25Y", "30Y", 
				"1Y", "2Y", "3Y", "4Y", "5Y", "6Y", "7Y", "8Y", "9Y", "10Y", "15Y", "20Y", "25Y", "30Y"};
		final double[] atmNormalVolatilities = {
				0.0091724, 0.0100630, 0.0097358, 0.0094046, 0.0090794, 0.0089036, 0.0087360, 0.0085685, 0.0084274, 0.0082822, 0.0079780, 0.0077245, 0.0075979, 0.0074987,
				0.0094961, 0.0104041, 0.0100989, 0.0097817, 0.0094661, 0.0093113, 0.0091580, 0.0090079, 0.0088609, 0.0087187, 0.0084518, 0.0081820, 0.0080339, 0.0079026,
				0.0100884, 0.0105405, 0.0102296, 0.0099164, 0.0096008, 0.0094413, 0.0093093, 0.0091484, 0.0090163, 0.0088842, 0.0086488, 0.0084034, 0.0082631, 0.0081498,
				0.0102156, 0.0103432, 0.0100760, 0.0098498, 0.0096765, 0.0095448, 0.0094336, 0.0093376, 0.0092308, 0.0091241, 0.0088811, 0.0086422, 0.0084847, 0.0083463,
				0.0100316, 0.0100104, 0.0097870, 0.0095870, 0.0094337, 0.0093227, 0.0092252, 0.0091594, 0.0090990, 0.0090374, 0.0086704, 0.0084787, 0.0083057, 0.0082119,
				0.0097211, 0.0096769, 0.0094825, 0.0093009, 0.0091439, 0.0090816, 0.0090050, 0.0089767, 0.0089323, 0.0088732, 0.0084803, 0.0082537, 0.0080932, 0.0079809,
				0.0094096, 0.0093518, 0.0091791, 0.0090044, 0.0089131, 0.0088557, 0.0087960, 0.0087642, 0.0087166, 0.0086609, 0.0082646, 0.0079982, 0.0078413, 0.0077103,
				0.0090900, 0.0090467, 0.0088999, 0.0087431, 0.0086835, 0.0086140, 0.0085665, 0.0085319, 0.0085017, 0.0084392, 0.0079901, 0.0077213, 0.0075501, 0.0074313,
				0.0087277, 0.0086903, 0.0085357, 0.0083898, 0.0083206, 0.0082405, 0.0081632, 0.0080939, 0.0080576, 0.0080132, 0.0075556, 0.0072706, 0.0070946, 0.0069734,
				0.0083182, 0.0083051, 0.0081041, 0.0079816, 0.0078467, 0.0077446, 0.0076425, 0.0075540, 0.0074741, 0.0073925, 0.0069140, 0.0066435, 0.0064752, 0.0063422,
				0.0076842, 0.0076659, 0.0074754, 0.0073211, 0.0071126, 0.0069723, 0.0068434, 0.0067227, 0.0066231, 0.0065666, 0.0060735, 0.0058558, 0.0056846, 0.0055513,
				0.0071250, 0.0071144, 0.0069248, 0.0067171, 0.0065200, 0.0063901, 0.0062637, 0.0061494, 0.0060528, 0.0059467, 0.0054564, 0.0052853, 0.0051208, 0.0049822,
				0.0067113, 0.0066629, 0.0064988, 0.0062471, 0.0060306, 0.0058922, 0.0057502, 0.0056278, 0.0055224, 0.0054431, 0.0050252, 0.0048565, 0.0046912, 0.0045590,
				0.0063820, 0.0063749, 0.0061893, 0.0059698, 0.0057145, 0.0055718, 0.0054252, 0.0052809, 0.0051233, 0.0050618, 0.0046247, 0.0044867, 0.0043391, 0.0042097};

		final BusinessdayCalendarExcludingTARGETHolidays cal = new BusinessdayCalendarExcludingTARGETHolidays();
		final DayCountConvention_ACT_365 modelDC = new DayCountConvention_ACT_365();
		for(int i=0; i<atmNormalVolatilities.length; i++ ) {

			final LocalDate exerciseDate = cal.getDateFromDateAndOffsetCode(REFERENCE_DATE.toLocalDate(), atmExpiries[i]);
			final LocalDate tenorEndDate = cal.getDateFromDateAndOffsetCode(exerciseDate, atmTenors[i]);
			double	exercise		= modelDC.getDaycountFraction(REFERENCE_DATE.toLocalDate(), exerciseDate);
			double	tenor			= modelDC.getDaycountFraction(exerciseDate, tenorEndDate);

			exercise = Math.round(exercise/0.25)*0.25;
			tenor = Math.round(tenor/0.25)*0.25;

			if(exercise < 1.0) {
				continue;
			}

			final int numberOfPeriods = (int)Math.round(tenor / swapPeriodLength);

			final double	moneyness			= 0.0;
			final double	targetVolatility	= atmNormalVolatilities[i];

			final String	targetVolatilityType = "VOLATILITYNORMAL";

			final double	weight = 1.0;

			//			if(exercise != 1.0 && (exercise+tenor < 30 || exercise+tenor >= 40)) weight = 0.01;
			//			if((exercise+tenor < 30 || exercise+tenor >= 40)) weight = 0.01;

			calibrationProducts.add(createCalibrationItem(weight, exercise, swapPeriodLength, numberOfPeriods, moneyness, targetVolatility, targetVolatilityType, forwardCurve, discountCurve));
			calibrationItemNames.add(atmExpiries[i]+"\t"+atmTenors[i]);
		}

		/*
		 * Create a simulation time discretization
		 */
		// If simulation time is below libor time, exceptions will be hard to track.
		final double lastTime	= 40.0;
		final double dt		= 0.25;
		final TimeDiscretizationFromArray timeDiscretizationFromArray = new TimeDiscretizationFromArray(0.0, (int) (lastTime / dt), dt);
		final TimeDiscretization liborPeriodDiscretization = timeDiscretizationFromArray;

		/*
		 * Create Brownian motions
		 */
		final BrownianMotion brownianMotion = new BrownianMotionFromMersenneRandomNumbers(timeDiscretizationFromArray, numberOfFactors, numberOfPaths, 31415 /* seed */);
		//		final BrownianMotion brownianMotion = new net.finmath.montecarlo.BrownianMotionCudaWithRandomVariableCuda(timeDiscretizationFromArray, numberOfFactors, numberOfPaths, 31415 /* seed */);

		TermStructureMonteCarloSimulationModel simulationCalibrated = null;
		
		final TimeDiscretization optionMaturityDiscretization = new TimeDiscretizationFromArray(0.0, 0.25, 0.50, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 15.0, 20.0, 25.0, 30.0, 40.0);

		TimeDiscretization timeToMaturityDiscretization = new TimeDiscretizationFromArray(0.00, 40, 1.0);
		final ArrayList<Double> timeToMaturityList = timeToMaturityDiscretization.getAsArrayList();
		timeToMaturityList.add(0.25);
		timeToMaturityList.add(0.50);
		timeToMaturityList.add(0.75);
		timeToMaturityDiscretization = new TimeDiscretizationFromArray(timeToMaturityList);

//		final double[] paramTimeScalingVolParam = { 0.089999984104462, 0.089993900990014, 0.0899901381500183, 0.0899845802184755, -0.00190194626729436, -0.00418576153191772, -0.00441813992248022, -0.00424528297453292, 0.00137189236415217, -0.00424012554558316, -0.00432221274145632, -0.00483647112245279, -0.00350345393803074, 0.00410390432565215, -0.00337651325449009, -0.00223245717648495, 0.00144781460237176, 0.00157384764851954, 0.000885233023712288, 0.000887727811942866, 0.00203665671181731, 0.00745080464532293, -0.0050077709913031, -0.0032360208915442, -0.00608705649088229, -0.00238780165327092, -0.00182870700628789, 0.00745598857460763, 0.00144844839078437, 0.0014903601419369, 0.00158601875086631, 0.00162150201575499, 0.00141187051230204, -0.00523312245340903, 0.0036498310707617, 0.00947164189040023, 0.00680900408902559, -0.000944907659866203, -0.00700426166316745, 0.0135575386635909, 0.0185659945912829, 0.0311569532311878, 0.0311491952246717, 0.0295444715838745, 0.0119027392354009, 0.0105503815107718, 0.00981187720991385, 0.0108686092521016, -0.00102609863041806, -0.00132404828297197, -0.00136659746062036, -0.000492807134885282, -0.00684581203436721, -0.007173834474506, -0.00716186758490906, -0.00794955864124858, -0.00804588974458283, -0.00798414268222274, -0.0077513603346857, -0.0085821316837881, 0.00483146372085457, 0.00198405481146835, 0.00280073798027928, 0.000214532758988497, -0.000606143302083523, 0.0123661513483746, 0.015763826965351, -0.0011732066931269, -0.00558653548706701, -0.00458230269486747, -0.00531797169359265, -0.00671516758264544, -0.00899400774658346, -0.00721681741356519, -0.00581835092251395, -0.00657903985460052, -0.00834198448965509, -0.0047904979265391, -0.00679363669166775, -0.00863808953844284, -0.00135870327632147, -0.00216134597150485, 0.00563484601100739, 0.0028891889237083, -0.00322788036100675, -0.00320578170793069, -0.00332017825431748, -0.00333796003083108, -0.00738260779548298, -0.00558566964708603, -0.00648562153961535, -0.00682239785630116, -0.00562208147942271, -0.00386778437264297, -0.00544935588956036, -0.00313993895400358, -0.00610512461105912, -0.00804633461716719, -0.00598433956038178, -0.00518802497554446, -0.00158767012422657, 0.00233171469230825, -0.00900000000000005, -0.00900000000000005, -0.00734040441362481, -0.00769899523590027, -0.00900000000000005, -0.00899242077496552, -0.0070058160828367, -0.008586826201944, -0.00746747216087698, -0.00899909885981117, -0.00900000000000005, 0.000598073267528036, -0.00900000000000005, -0.00899982203573159, -0.00892659602533228, -0.00894688063213806, -0.00892911465011963, -0.00900000000000005, -0.00900000000000005, 0.00330669971399458, 0.00280840467593776, 0.00226621963642401, -0.00509745871525581, -0.00509885341076341, -0.00508907670217468, -0.00508074462799044, -0.00547879989150885, -0.00548091270161478, -0.00549441668043954, -0.00549625007676212, 0.00344414029016832, 0.00344522046529704, 0.00360607833290572, 0.00361465017196252, -0.00282130455451834, -0.00281457297773471, -0.00241092871079786, -0.00242527597238421, -0.00813534074291396, -0.00814126990976035, -0.00813025770034016, -0.00813892371500514, -0.00882396620502618, -0.0088297104920116, -0.00883612513126053, -0.00883042201502831, -0.00900000000000005, -0.00900000000000005, -0.00900000000000005, -0.00900000000000005, -0.00868628673246206, -0.00867012748975867, -0.0086912365589896, -0.00868687066957818, -0.00892331104406707, -0.00891576800786055, -0.0089161919184329, -0.00891980289420473, 0.00399780701070502, -0.49444126851566, -0.494435363513558, -0.494432553650804, -0.00157162236828548, -1.0992842756501E-07, 0.00272063643065594, 0.00452985810651097, 0.00549556049936806, 0.010991441414052, 0.0033474290063268, 0.00197733376151605, 0.00142219063203068, -0.00188288841850331, 0.00399843012476706, 0.00399379216572144, 0.00399458175772137, 0.0039971511939114, -0.000139025663220937, 0.00400315391709513, 0.00399030728704296, 0.00400148539584672, 0.00400591414162075, -0.000406592063214253, 0.00400197814251451, 0.00400602207829323, 0.00398776446932046, 0.00400556894803626, -0.000266370310796592, 0.00400056550853854, 0.00399304195663744, 0.00400527909708902, 0.00400121588526432, -0.000929427453239216, 0.00401105627197073, 0.00399766713487625, 0.00400013819560811, 0.00399622054236744, -0.00112420144779585, 0.00400802693288153, 0.00399932667960264, 0.00399648950953365, 0.00399573250122974, 0.00399424624289481, 0.00400216759709767, 1.10646555687144, 1.1064589736536, -0.000183472135494369, -0.000172993390802076, 0.00393408908741404, 0.00733956560440598, 0.00861480187466026, 0.0115934582051673, 0.00974304003593672, 0.0127365325344546, 0.0153987434538555, 0.000725046076883778, 0.00399840170459536, 0.00399422017558321, 0.00401026169985445, 0.00400762949863013, -0.00021029126404189, 0.00400859984450043, 0.00400679024797902, 0.00399663219188377, 0.00399790837755902, -0.000228518151922764, 0.00400006011119813, 0.00399769112283165, 0.00400553399930391, 0.00400162107476618, -0.000313082716650621, 0.00400456292147056, 0.00400556968611433, 0.00400710530036603, 0.00400606294385858, -0.000554947624728905, 0.00398904569272785, 0.00399589266442923, 0.00400340781910198, 0.00399741813587968, -0.00437608771398979, 0.00400330296163454, 0.00399530784908118, 0.00399533951401766, 0.00401481261039315, 0.00400048819256143, 0.00400367416024569, 0.00389234920206552, 0.0037521667485533, 0.00384664174489545, 0.00486940225863406, 0.0044068222621924, 0.00326066030660317, 0.00363875022535542, 0.00177095962023269, 0.00458638200652051, 0.00338891727970305, 0.00148434141913449, 0.00347731515803139, -0.000150100738647379, 0.00399957477319647, 0.00399470099720745, 0.00400421187177353, 0.00173882579289788, 0.00400354292405236, 0.00399889648078441, 0.00401179116205208, 0.0039982703765957, 0.00184672898920138, 0.00400930833812459, 0.00400019630145089, 0.00399990110508255, 0.00401240326399964, 0.00489917875743417, 0.00400551781725645, 0.00399082291481226, 0.00400004193694341, 0.00401312185941792, 0.000203135587026336, 0.0039995495373251, 0.00399836672469344, 0.00400691498748744, 0.00400293211471976, -0.000696076651915027, 0.00399916077540136, 0.00399308501871636, 0.00399901046641418, 0.00400247100334813, 0.00398855232134889, 0.00399680411411167, -0.000913309187310931, 0.00923578351251054, 0.00565112096116737, 0.00433051150215994, 0.00318602582607875, 0.00341003341852321, 0.00421255887301701, 0.00453322303618831, 0.00462286524899222, 0.00473119716680763, -0.000143174718108278, 0.0101437528977208, -0.000201776972129086, 0.00399235104769332, 0.0039865400408277, 0.0019279212352188, -0.000177113086952985, 0.00400715958323341, 0.00400094279126635, 0.00400946124702011, 0.00134856050417628, -0.000268532793612923, 0.00400729868163326, 0.00400653049788387, 0.00399007195125664, 0.00590525535263523, -0.00194353869264568, 0.00399871791866517, 0.00400130118780169, 0.00400417116305861, 0.000610252068293993, -0.000658835336057235, 0.00400172899208264, 0.00400421144376383, 0.00400958732940494, -0.000850420437484669, -0.00203608813573197, 0.00399157310080955, 0.00399903174894247, 0.00400422026915309, 0.00399169734265986, 0.00400533483896996, 0.00399419664259638, -0.00422803682938843, 0.0197084555975708, -0.000257593581581099, 0.00285166866777017, 0.00431279319786545, 0.00498496514789032, 0.00520380834306612, 0.00513177431355671, 0.00756916987994435, -0.000150255252009589, -0.000161352954684722, 0.0235210099733678, -0.00446925775867501, 0.00399332548653183, -0.000147760885851023, -0.000115562257972509, 0.00399280420714138, 0.00400379600851552, 0.00399446397969584, 0.00195911180272385, -0.00243256388308474, 0.00400346605430943, 0.00400710482820076, 0.00400124996517238, -0.000132541770384485, -0.00148826895690495, 0.00399368178444763, 0.00400336715447685, 0.00399082194221145, 0.00291659809737315, -0.000997885402463235, 0.00399742042610546, 0.00400825592823958, 0.0040098736062748, -0.000580220365986495, -0.00155453164551143, 0.00400014563648007, 0.00400999864235201, 0.00399469503601508, 0.00400214756002654, 0.00398799876007819, 0.00400902506774626, 0.00492262585008514, 0.00277239054147526, 0.00494114891687885, 0.0065649394799405, 0.00727776153467931, 0.00790252348029478, 0.00695721366990139, 0.00795020828580415, 0.00150054473476394, 0.00648605765855751, 0.00837910943709391, 0.0503804515639715, 0.133603540055207, 0.00118074955960002, -0.000106384002954701, 0.00399836593326441, 0.00399975066350749, 0.00399971285379956, 0.00297815318540599, -0.000617569350050213, 0.00399729439950767, 0.00399684067077337, 0.00400426817060598, 0.00468809256081054, -0.000779672522481478, 0.0039983612720994, 0.00399404890081026, 0.0040052127607445, -0.000169466645079028, 0.00883652411877378, 0.00400355399099511, 0.00400303373148152, 0.00399891042225148, 0.0203126128852496, -0.00152706291335349, 0.00399781345209943, 0.00400160667802596, 0.00398755092520426, 0.00399656380252021, 0.0040065239538153, 0.00400208193358509, -0.00162699486450058, 0.0071920195408265, 0.011707060950816, 0.00650686908601564, 0.0052076550962668, 0.0062544184902681, 0.0052908532345961, 0.00241469318064073, 0.00222474971498637, 0.00484788868126099, -0.000141868322674157, 0.0550729366816687, 0.00277821696305189, -0.000122669337107752, 0.00400017267854154, 0.00399348330852289, 0.00399176498059539, -0.00103891696621039, 0.00420400979621641, 0.00400231524279263, 0.00399742020931394, 0.00400220918297029, -0.000227923751275513, 0.0075325070103828, 0.00400537031815308, 0.00400204547204036, 0.00399112808742699, -0.00046611510135596, 0.00390523645274286, 0.00400431064682218, 0.00398457478368309, 0.00399895872686772, -0.0050611782462024, -0.00112000274369474, 0.00400526191452558, 0.00399347179978057, 0.00399330079541019, 0.00400648127725935, 0.00400027795693062, 0.00399168552925152, 0.395067162378213, 0.132072956741606, 0.122754417939007, -0.000784196354458672, -0.000138235987104404, -0.000311900849230872, -0.000162042552188412, 0.00588356086811242, -3.53939689548158E-05, 0.066038021449398, 0.119325688692816, 0.000906737263871121, -0.000466309868929055, 0.00400061739921156, 0.00399513155195567, 0.00401036898056119, 0.00454105388651191, -0.00101137382471542, 0.00399738898788433, 0.00399497494402889, 0.00400176967863601, 0.00403892033066293, -0.0011872796669467, 0.0039980410438325, 0.00399447871671042, 0.00399433725267537, 0.00277273799570392, -0.000134031849509847, 0.0040079675265262, 0.00400770484702454, 0.00400433907258317, -0.000163643351428294, -0.000700705942142426, 0.00399994669038151, 0.00400662301876197, 0.00399698763069894, 0.0040045319961418, 0.00400617855369554, 0.00400214462362018, -0.000363542302689785, 0.00893214697607924, 0.0070464782835267, 0.00367976945506546, 0.00350338431221762, 0.00184027356053214, 0.0025221124976663, 0.00296684233761205, 0.0148423342382019, 0.0175859170330717, 0.0223329216288011, 0.01887797690199, -0.000247208346609389, 0.00399601535296208, 0.00399235601880748, 0.00084025610702231, -0.000424856316275215, 0.00399155251086519, 0.00401253333980356, 0.00399421004887061, -0.000214548549358204, -0.000646941159642577, 0.00401716178748541, 0.00399104212050509, 0.00399880774696967, -0.000241494328561845, -0.00242416774214484, 0.00399611863726235, 0.00399517086295241, 0.00400683242425706, -0.000334639655188627, -0.0039064039194833, 0.00399856168092896, 0.00400096082849641, 0.00400912112765976, 0.0039998414026744, 0.00399039750001721, 0.00400099640449435, -0.1099837964771, -0.109992724444491, -0.109982130083171, -0.0133631553656129, 0.00232518938304419, -0.000136640315476804, 0.0107353281194772, -0.000130294176112994, 0.138390042682926, -0.000132434303359212, 0.0210006430642306, 0.023084426210275, 0.172903333461268, 0.00399544822814204, -0.00348744652863597, 0.00412224730156018, 0.00399966724976868, 0.0039957643035143, 0.00399392009350188, 0.0240772359623621, -0.000488155517869532, 0.00399592978039879, 0.0039945597546101, 0.00401348404954144, -0.000599754212627483, -0.000745773028253532, 0.00399402869504536, 0.0039926803829258, 0.00400048116720232, -0.00065478589618655, -0.000158698520876535, 0.00400223156030028, 0.00399112005153314, 0.00399086095225361, 0.00399522588766668, 0.00399936374449912, 0.00399587987802305, 2.12272740176158, 2.25987913179547, 2.06323161986756, -0.000191063835124816, -0.000133332203657637, -0.0038183660917851, -0.000167037606899628, -0.000957237414739019, 0.00789026592113766, -0.000291587678961229, 0.0121200458837448, 0.138776553946212, 0.0529798181862248, 0.00861624640983701, 0.00659314981759569, 0.00399453979500096, 0.00400948094275374, 0.00400293967934368, 0.0181373891364167, -0.000183887927706963, 0.00400276465289603, 0.0039956313171267, 0.00400842205967217, 0.0161415498752904, -0.000115242887143754, 0.00400243459918743, 0.0040056495939698, 0.00400049928357782, 0.262447109876817, -0.00255194050012678, 0.00400487633013106, 0.00400303035185663, 0.00399860419723923, 0.00400192782997921, 0.0039909282840045, 0.00399928813321253, 0.0204772265101831, 0.0240188966562148, 0.0118544490210018, 0.00645311318916334, 0.0301017508284892, 0.100872004994125, 0.230535326396742, 0.034300629876816, 0.00787847919541187, 0.0322838852873132, 0.107294370184529, 0.1036153884209, -0.000128620412551977, -0.000930224875503104, 0.00400476051855667, 0.00400541342922024, 0.00400543624016981, -0.00287911155593107, 0.000998972972493756, 0.00399745965300019, 0.00401111709921132, 0.00400369664569743, -0.00127272245202417, 0.00570340038128389, 0.00399333692621244, 0.00400614158893996, 0.00399206214794711, -0.00209770779169251, -0.00181059704266888, 0.00400578848064937, 0.00399705125314804, 0.00399758938088308, 0.00400750543112501, 0.00400117956224315, 0.00398404819314136, -0.000235431722605949, -0.000289292192424331, -0.00429710724460976, -0.000375550335955473, -0.00185284868248381, -0.000136703019546342, -0.000180918925305957, 4.25478459443189E-05, -0.00025528090814467, 0.0204601903521506, 0.00619183289714929, 0.0100805530923725, 0.00516721617628374, -0.000295708021707873, 0.00780654577675436, 0.00139290080196226, 0.00576136809411688, 0.00139411475532367, -0.00380109298866226, 0.00310557423509122, -0.000120722330837101, 0.00419903403910578, -0.000161175784422497, -0.00156662410736716, -0.000276097439916945, -0.000117442859907528, 0.0494671574014293, -0.00718835627261953, 0.00399851965168418, 0.00401190837952466, 0.00400046777564649, 0.00399480087060629, 0.00400584058570769, 0.00400366423364113, 0.0957676093689149, 0.0884847080503522, 0.0845253137912114, -0.000338574360196422, 0.0091092601082336, 0.0238923996167416, -0.000204205856107072, 0.0125222501519829, 0.0160639233073764, 0.0117649125168953, 0.0230107053736233, -0.000439750014219888, 0.0462792733656938, -0.000119355962821415, -0.000159608913319949, 0.00307538346688976, -0.000328344480056022, 0.000300470644286628, -0.00441702839225111, -0.000583707014139153, -0.00024462290746606, -0.00274451299918907, -0.000337133910062902, 0.00400337294585505, 0.00400371580320374, 0.00399127654149084, 0.0040064423450648, 0.00400691109083355, 0.00401868428269433, 0.0220804167549368, -0.000504612030822202, -0.000780331256426177, -0.000473492077473064, -0.000156333078463891, -0.00545762407740487, 0.0164847202518829, 0.0124618287699428, 0.0303514569854341, 0.0462991269495574, -0.000107265204768476, 0.0178558518867034, 0.00778034365730244, 0.0107502111616525, 0.0398244686141763, -0.000207874137143602, 0.0303335606057633, 0.0562231598862078, 0.00399258154063248, 0.00399088760461249, 0.00400142119514921, 0.00400857366132433, 0.00400269036461805, 0.00400187480121562, 0.80194315711381, -0.00794602438985766, -0.0442260460173735, -0.00012057451027344, -0.000198620086347637, -0.000213891717274347, 0.000326016980683264, -0.000121825380544764, 0.00132024127437892, -0.000225713972805136, 0.00122350909091257, -0.000147251264995611, 0.0517474847755777, 0.00400374927699573, 0.00398662227835639, 0.0040041452676069, 0.00399513349920769, 0.00400601687723186, 0.00400444498317761, 0.00399481456922337, 0.00400802003555727, 0.00400640833329286, 0.00398824559189416, 0.00399382035565428, 0.00399703875652569, 0.00400315418170189, 0.00400146333002795, 0.00399968680594135, 0.00399288512227374, 0.00399868258926832, 0.00399417814824357, 0.00400187122505196, 0.00399916195565957 };
		
		final LIBORVolatilityModel volatilityModel = new LIBORVolatilityModelPiecewiseConstant(timeDiscretizationFromArray, liborPeriodDiscretization, optionMaturityDiscretization, timeToMaturityDiscretization, 0.40 / 100, true);

		final LIBORCorrelationModel correlationModel = new LIBORCorrelationModelExponentialDecay(timeDiscretizationFromArray, liborPeriodDiscretization, numberOfFactors, 0.05, false);
		// Create a covariance model
		AbstractLIBORCovarianceModelParametric covarianceModelParametric = new LIBORCovarianceModelFromVolatilityAndCorrelation(timeDiscretizationFromArray, liborPeriodDiscretization, volatilityModel, correlationModel);

		final TimeDiscretization tenorTimeScalingDiscretization = new TimeDiscretizationFromArray(0.0, 40.0, 0.25, ShortPeriodLocation.SHORT_PERIOD_AT_START);
		final double[] tenorTimeScalings = new double[tenorTimeScalingDiscretization.getNumberOfTimes()];
		Arrays.fill(tenorTimeScalings, 0.0);
		final TermStructureTenorTimeScaling tenorTimeScalingModel = new TermStructureTenorTimeScalingPicewiseConstant(tenorTimeScalingDiscretization, tenorTimeScalings);

		TermStructureCovarianceModelParametric termStructureCovarianceModel;
		termStructureCovarianceModel = new TermStructCovarianceModelFromLIBORCovarianceModelParametric(tenorTimeScalingModel, covarianceModelParametric);

//		termStructureCovarianceModel = termStructureCovarianceModel.getCloneWithModifiedParameters(paramTimeScalingVolParam);
		// Set model properties
		final Map<String, Object> properties = new HashMap<>();

		// accuracy 1E-12 takes fairly long
		final Double accuracy = 4E-3; //1E-12;
		final int maxIterations = 400;
		final int numberOfThreads = 6;
		final OptimizerFactory optimizerFactory = new OptimizerFactoryLevenbergMarquardt(maxIterations, accuracy, numberOfThreads);
		final double[] parameterStandardDeviation = new double[termStructureCovarianceModel.getParameter().length];
		final double[] parameterLowerBound = new double[termStructureCovarianceModel.getParameter().length];
		final double[] parameterUpperBound = new double[termStructureCovarianceModel.getParameter().length];
		Arrays.fill(parameterStandardDeviation, 0.0020/100.0);
		Arrays.fill(parameterLowerBound, Double.NEGATIVE_INFINITY);
		Arrays.fill(parameterUpperBound, Double.POSITIVE_INFINITY);

		// Set calibration properties (should use our brownianMotion for calibration - needed to have to right correlation).
		final Map<String, Object> calibrationParameters = new HashMap<>();
		calibrationParameters.put("accuracy", accuracy);
		calibrationParameters.put("brownianMotion", brownianMotion);
		calibrationParameters.put("parameterStep", 1E-6);
		calibrationParameters.put("optimizerFactory", optimizerFactory);
		properties.put("calibrationParameters", calibrationParameters);

		System.out.println("Number of covariance parameters: " + termStructureCovarianceModel.getParameter().length);

		/*
		 * Create corresponding LIBOR Market Model
		 */
		final TimeDiscretization liborPeriodDiscretizationFine = new TimeDiscretizationFromArray(0.0, 40.0, 1/365.0, ShortPeriodLocation.SHORT_PERIOD_AT_START); //daily
		final TimeDiscretization liborPeriodDiscretizationMedium = new TimeDiscretizationFromArray(0.0, 40.0, 1/12, ShortPeriodLocation.SHORT_PERIOD_AT_START); //monthly
		final TimeDiscretization liborPeriodDiscretizationCoarse = new TimeDiscretizationFromArray(0.0, 40.0, 1.0, ShortPeriodLocation.SHORT_PERIOD_AT_START); //yearly
		final TimeDiscretization liborPeriodDiscretizationCoarse2 = new TimeDiscretizationFromArray(0.0, 40.0, 5.0, ShortPeriodLocation.SHORT_PERIOD_AT_START);
		final TermStructureModel liborMarketModelCalibrated = new LIBORMarketModelWithTenorRefinement(
				new TimeDiscretization[] {liborPeriodDiscretizationFine, liborPeriodDiscretizationMedium, liborPeriodDiscretizationCoarse, liborPeriodDiscretizationCoarse2},
				new Integer[] {22, 25, 13, 9},
				curveModel,
				forwardCurve, new DiscountCurveFromForwardCurve(forwardCurve),
				termStructureCovarianceModel,
				calibrationProducts.toArray(new CalibrationProduct[0]), properties);

		
		System.out.println("\nCalibrated parameters are:");
		final double[] param = ((LIBORMarketModelWithTenorRefinement) liborMarketModelCalibrated).getCovarianceModel().getParameter();
		for (final double p : param) {
			System.out.println(p);
		}
		final EulerSchemeFromProcessModel process = new EulerSchemeFromProcessModel(liborMarketModelCalibrated, brownianMotion);
		simulationCalibrated = new TermStructureMonteCarloSimulationFromTermStructureModel(liborMarketModelCalibrated, process);
		
		System.out.println("\nValuation on calibrated model:");
		double deviationSum			= 0.0;
		double deviationSquaredSum	= 0.0;
		for (int i = 0; i < calibrationProducts.size(); i++) {
			final AbstractTermStructureMonteCarloProduct calibrationProduct = calibrationProducts.get(i).getProduct();
			try {
				final double valueModel = calibrationProduct.getValue(simulationCalibrated);
				final double valueTarget = calibrationProducts.get(i).getTargetValue().getAverage();
				final double error = valueModel-valueTarget;
				deviationSum += error;
				deviationSquaredSum += error*error;
				System.out.println(calibrationItemNames.get(i) + "\t" + "Model: " + formatterValue.format(valueModel) + "\t Target: " + formatterValue.format(valueTarget) + "\t Deviation: " + formatterDeviation.format(valueModel-valueTarget));// + "\t" + calibrationProduct.toString());
			}
			catch(final Exception e) {
			}
		}
		final double averageDeviation = deviationSum/calibrationProducts.size();
		System.out.println("Mean Deviation:" + formatterValue.format(averageDeviation));
		System.out.println("RMS Error.....:" + formatterValue.format(Math.sqrt(deviationSquaredSum/calibrationProducts.size())));
		System.out.println("__________________________________________________________________________________________\n");

		Assert.assertTrue(Math.abs(averageDeviation) < 1E-2);
	}
	

	public static AnalyticModel getCalibratedCurve() throws SolverException {
		final String[] maturity					= { "1D", "7D", "14D", "21D", "1M", "2M", "3M", "4M", "5M", "6M", "7M", "8M", "9M", "10M", "11M", "12M", "15M", "18M", "21M", "2Y", "3Y", "4Y", "5Y", "6Y", "7Y", "8Y", "9Y", "10Y", "12Y", "15Y", "20Y", "25Y", "30Y", "40Y", "50Y"};
		final String[] frequency				= { "tenor", "tenor", "tenor", "tenor", "tenor", "tenor", "tenor", "tenor", "tenor", "tenor", "tenor", "tenor", "tenor", "tenor", "tenor", "annual", "annual", "annual", "annual", "annual", "annual", "annual", "annual", "annual", "annual", "annual", "annual", "annual", "annual", "annual", "annual", "annual", "annual", "annual", "annual" };
		final String[] frequencyFloat			= { "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "quarterly", "quarterly", "quarterly", "quarterly", "quarterly", "quarterly", "quarterly", "quarterly", "semiannual", "semiannual", "semiannual", "semiannual", "semiannual", "semiannual", "semiannual", "semiannual", "semiannual", "semiannual", "semiannual", "semiannual" };
		final String[] daycountConventions		= { "ACT/360", "ACT/360", "ACT/360", "ACT/360", "ACT/360", "ACT/360", "ACT/360", "ACT/360", "ACT/360", "ACT/360", "ACT/360", "ACT/360", "ACT/360", "ACT/360", "ACT/360", "E30/360", "E30/360", "E30/360", "E30/360", "E30/360", "E30/360", "E30/360", "E30/360", "E30/360", "E30/360", "E30/360", "E30/360", "E30/360", "E30/360", "E30/360", "E30/360", "E30/360", "E30/360", "E30/360", "E30/360" };
		final String[] daycountConventionsFloat	= { "ACT/360", "ACT/360", "ACT/360", "ACT/360", "ACT/360", "ACT/360", "ACT/360", "ACT/360", "ACT/360", "ACT/360", "ACT/360", "ACT/360", "ACT/360", "ACT/360", "ACT/360", "ACT/360", "ACT/360", "ACT/360", "ACT/360", "ACT/360", "ACT/360", "ACT/360", "ACT/360", "ACT/360", "ACT/360", "ACT/360", "ACT/360", "ACT/360", "ACT/360", "ACT/360", "ACT/360", "ACT/360", "ACT/360", "ACT/360", "ACT/360" };
		final double[] rates					= { 0.03907, 0.03864, 0.03871, 0.03879, 0.03887, 0.03905, 0.03922, 0.03891, 0.03833, 0.03779, 0.03739, 0.03679, 0.03614, 0.03561, 0.03501, 0.03496, 0.03300, 0.03159, 0.03046, 0.02958, 0.02742, 0.02652, 0.02621, 0.02679, 0.02677, 0.026845, 0.02697, 0.02713, 0.02747, 0.027685, 0.027055, 0.02597, 0.024945, 0.02331, 0.02189 };

		final HashMap<String, Object> parameters = new HashMap<>();

		parameters.put("referenceDate", LocalDate.of(2024, Month.FEBRUARY, 05));
		parameters.put("currency", "EUR");
		parameters.put("forwardCurveTenor", "3M");
		parameters.put("maturities", maturity);
		parameters.put("fixLegFrequencies", frequency);
		parameters.put("floatLegFrequencies", frequencyFloat);
		parameters.put("fixLegDaycountConventions", daycountConventions);
		parameters.put("floatLegDaycountConventions", daycountConventionsFloat);
		parameters.put("rates", rates);

		return getCalibratedCurve(null, parameters);
	}
	

	private static AnalyticModel getCalibratedCurve(final AnalyticModel model2, final Map<String, Object> parameters) throws SolverException {

		final LocalDate	referenceDate		= (LocalDate) parameters.get("referenceDate");
		final String	currency			= (String) parameters.get("currency");
		final String	forwardCurveTenor	= (String) parameters.get("forwardCurveTenor");
		final String[]	maturities			= (String[]) parameters.get("maturities");
		final String[]	frequency			= (String[]) parameters.get("fixLegFrequencies");
		final String[]	frequencyFloat		= (String[]) parameters.get("floatLegFrequencies");
		final String[]	daycountConventions	= (String[]) parameters.get("fixLegDaycountConventions");
		final String[]	daycountConventionsFloat	= (String[]) parameters.get("floatLegDaycountConventions");
		final double[]	rates						= (double[]) parameters.get("rates");

		Assert.assertEquals(maturities.length, frequency.length);
		Assert.assertEquals(maturities.length, daycountConventions.length);
		Assert.assertEquals(maturities.length, rates.length);

		Assert.assertEquals(frequency.length, frequencyFloat.length);
		Assert.assertEquals(daycountConventions.length, daycountConventionsFloat.length);

		final int		spotOffsetDays = 2;
		final String	forwardStartPeriod = "0D";

		final String curveNameDiscount = "discountCurve-" + currency;

		/*
		 * We create a forward curve by referencing the same discount curve, since
		 * this is a single curve setup.
		 *
		 * Note that using an independent NSS forward curve with its own NSS parameters
		 * would result in a problem where both, the forward curve and the discount curve
		 * have free parameters.
		 */
		final ForwardCurve forwardCurve		= new ForwardCurveFromDiscountCurve(curveNameDiscount, referenceDate, forwardCurveTenor);

		// Create a collection of objective functions (calibration products)
		final Vector<AnalyticProduct> calibrationProducts = new Vector<>();
		final double[] curveMaturities	= new double[rates.length+1];
		final double[] curveValue			= new double[rates.length+1];
		final boolean[] curveIsParameter	= new boolean[rates.length+1];
		curveMaturities[0] = 0.0;
		curveValue[0] = 1.0;
		curveIsParameter[0] = false;
		
		// The first 15 products are cash deposits, followed by 20 swaps
		// The first instrument of the deposits is an OIS product -> no spot off set
		final Schedule scheduleESTR = ScheduleGenerator.createScheduleFromConventions(referenceDate, 0, forwardStartPeriod, maturities[0], frequency[0], daycountConventions[0], "first", "following", new BusinessdayCalendarExcludingTARGETHolidays(), 0, 0);
		curveMaturities[1] = scheduleESTR.getPayment(scheduleESTR.getNumberOfPeriods()-1);
		curveValue[1] = 1.0;
		curveIsParameter[1] = true;
		calibrationProducts.add(new Deposit(scheduleESTR, rates[0], curveNameDiscount));
		
		// Cash deposits
		for (int i=1; i < 15; i++) {
			final Schedule scheduleDeposit = ScheduleGenerator.createScheduleFromConventions(referenceDate, spotOffsetDays, forwardStartPeriod, maturities[i], frequency[i], daycountConventions[i], "first", "following", new BusinessdayCalendarExcludingTARGETHolidays(), -2, 0);		
			curveMaturities[i+1] = scheduleDeposit.getPayment(scheduleDeposit.getNumberOfPeriods()-1);
			curveValue[i+1] = 1.0;
			curveIsParameter[i+1] = true;
			calibrationProducts.add(new Deposit(scheduleDeposit, rates[i], curveNameDiscount));
		}
		// Swaps
		for(int i=15; i<rates.length; i++) {
			final Schedule schedulePay = ScheduleGenerator.createScheduleFromConventions(referenceDate, spotOffsetDays, forwardStartPeriod, maturities[i], frequency[i], daycountConventions[i], "first", "following", new BusinessdayCalendarExcludingTARGETHolidays(), -2, 0);
			final Schedule scheduleRec = ScheduleGenerator.createScheduleFromConventions(referenceDate, spotOffsetDays, forwardStartPeriod, maturities[i], frequencyFloat[i], daycountConventionsFloat[i], "first", "following", new BusinessdayCalendarExcludingTARGETHolidays(), -2, 0);
			curveMaturities[i+1] = Math.max(schedulePay.getPayment(schedulePay.getNumberOfPeriods()-1),scheduleRec.getPayment(scheduleRec.getNumberOfPeriods()-1));
			curveValue[i+1] = 1.0;
			curveIsParameter[i+1] = true;
			calibrationProducts.add(new Swap(schedulePay, null, rates[i], curveNameDiscount, scheduleRec, forwardCurve.getName(), 0.0, curveNameDiscount));
		}

		final InterpolationMethod interpolationMethod = InterpolationMethod.LINEAR;

		// Create a discount curve
		final DiscountCurveInterpolation discountCurveInterpolation = DiscountCurveInterpolation.createDiscountCurveFromDiscountFactors(
				curveNameDiscount								/* name */,
				referenceDate	/* referenceDate */,
				curveMaturities	/* maturities */,
				curveValue		/* discount factors */,
				curveIsParameter,
				interpolationMethod ,
				ExtrapolationMethod.CONSTANT,
				InterpolationEntity.LOG_OF_VALUE
				);

		/*
		 * Model consists of the two curves, but only one of them provides free parameters.
		 */
		AnalyticModel model = new AnalyticModelFromCurvesAndVols(new Curve[] { discountCurveInterpolation, forwardCurve });

		/*
		 * Create a collection of curves to calibrate
		 */
		final Set<ParameterObject> curvesToCalibrate = new HashSet<>();
		curvesToCalibrate.add(discountCurveInterpolation);

		/*
		 * Calibrate the curve
		 */
		final Solver solver = new Solver(model, calibrationProducts);
		final AnalyticModel calibratedModel = solver.getCalibratedModel(curvesToCalibrate);
		System.out.println("Solver reported acccurary....: " + solver.getAccuracy());

		Assert.assertEquals("Calibration accurarcy", 0.0, solver.getAccuracy(), 1E-3);

		// Get best parameters
		final double[] parametersBest = calibratedModel.getDiscountCurve(discountCurveInterpolation.getName()).getParameter();

		// Test calibration
		model			= calibratedModel;

		double squaredErrorSum = 0.0;
		for(final AnalyticProduct c : calibrationProducts) {
			final double value = c.getValue(0.0, model);
			final double valueTaget = 0.0;
			final double error = value - valueTaget;
			squaredErrorSum += error*error;
		}
		final double rms = Math.sqrt(squaredErrorSum/calibrationProducts.size());

		System.out.println("Independent checked acccurary: " + rms);

		System.out.println("Calibrated discount curve: ");
		for(int i=0; i<curveMaturities.length; i++) {
			final double maturity = curveMaturities[i];
			System.out.println(maturity + "\t" + calibratedModel.getDiscountCurve(discountCurveInterpolation.getName()).getDiscountFactor(maturity));
		}
		return model;
	}
	
	
	private static CalibrationProduct createCalibrationItem(final double weight, final double exerciseDate, final double swapPeriodLength, final int numberOfPeriods, final double moneyness, final double targetVolatility, final String targetVolatilityType, final ForwardCurve forwardCurve, final DiscountCurve discountCurve) throws CalculationException {

		final double[]	fixingDates			= new double[numberOfPeriods];
		final double[]	paymentDates		= new double[numberOfPeriods];
		final double[]	swapTenor			= new double[numberOfPeriods + 1];

		for (int periodStartIndex = 0; periodStartIndex < numberOfPeriods; periodStartIndex++) {
			fixingDates[periodStartIndex] = exerciseDate + periodStartIndex * swapPeriodLength;
			paymentDates[periodStartIndex] = exerciseDate + (periodStartIndex + 1) * swapPeriodLength;
			swapTenor[periodStartIndex] = exerciseDate + periodStartIndex * swapPeriodLength;
		}
		swapTenor[numberOfPeriods] = exerciseDate + numberOfPeriods * swapPeriodLength;

		// Swaptions swap rate
		final double swaprate = moneyness + getParSwaprate(forwardCurve, discountCurve, swapTenor);

		// Set swap rates for each period
		final double[] swaprates = new double[numberOfPeriods];
		Arrays.fill(swaprates, swaprate);

		/*
		 * We use Monte-Carlo calibration on implied volatility.
		 * Alternatively you may change here to Monte-Carlo valuation on price or
		 * use an analytic approximation formula, etc.
		 */
		final SwaptionSimple swaptionMonteCarlo = new SwaptionSimple(swaprate, swapTenor, SwaptionSimple.ValueUnit.valueOf(targetVolatilityType));
		//		double targetValuePrice = AnalyticFormulas.blackModelSwaptionValue(swaprate, targetVolatility, fixingDates[0], swaprate, getSwapAnnuity(discountCurve, swapTenor));
		return new CalibrationProduct(swaptionMonteCarlo, targetVolatility, weight);
	}
	
	
	private static double getParSwaprate(final ForwardCurve forwardCurve, final DiscountCurve discountCurve, final double[] swapTenor) {
		return net.finmath.marketdata.products.Swap.getForwardSwapRate(new TimeDiscretizationFromArray(swapTenor), new TimeDiscretizationFromArray(swapTenor), forwardCurve, discountCurve);
	}
	
	
//	private static AnalyticModel getAnalyticModel(CurveName curveName) {
//	
//	final double[] zeroRateFixings = new double[] {0.0, 0.002739726, 0.019178082, 0.038356164, 0.057534247, 0.082191781, 0.164383562, 0.249315068, 0.331506849, 0.416438356, 0.498630137, 0.580821918, 0.665753425, 0.747945205, 0.832876712, 0.915068493, 1.000000000, 1.249315068, 1.498630137, 1.747945205, 2.000000000, 3.000000000, 4.000000000, 5.000000000, 6.000000000, 7.000000000, 8.000000000, 9.000000000, 10.000000000, 12.000000000, 15.000000000, 20.000000000, 25.000000000, 30.000000000, 40.000000000, 50.000000000};	
//	final double[] zeroRates = new double[]{0.0, 0.0397177825405, 0.0393333258188, 0.0393440343793, 0.0393873440268, 0.0394531891821, 0.0395606835007, 0.0396734623729, 0.0393169775975, 0.0386948852936, 0.0381018412216, 0.0376683034316, 0.0369968850608, 0.0363154480771, 0.0357470311341, 0.0350901722155, 0.0344190909065, 0.0325222647440, 0.0309894567502, 0.0297626328201, 0.0291230280961, 0.0269777858225, 0.0260844375829, 0.0257828071929, 0.0263899156066, 0.0263827692307, 0.0264673045947, 0.0266046891310, 0.0267812343890, 0.0271602095920, 0.0273963745838, 0.0266170086584, 0.0252534028964, 0.0239580063895, 0.0218879650233, 0.0200451152775};
//
//	// Create a discount curve
//	final DiscountCurveInterpolation discountCurveInterpolation = DiscountCurveInterpolation.createDiscountCurveFromZeroRates(
//			curveName.toString() /* name */,
//			REFERENCE_DATE.toLocalDate()	/* referenceDate */,
//			zeroRateFixings	/* maturities */,
//			zeroRates		/* rates */,
//			InterpolationMethod.LINEAR,
//			ExtrapolationMethod.CONSTANT,
//			InterpolationEntity.LOG_OF_VALUE
//			);
//	
//	// Single curve setup
//	final ForwardCurve	forwardCurveFromDiscountCurve = new ForwardCurveFromDiscountCurve(
//			discountCurveInterpolation.getName(),
//			REFERENCE_DATE.toLocalDate(),
//			null);
//	
//	AnalyticModel model = new AnalyticModelFromCurvesAndVols(new Curve[] { discountCurveInterpolation, forwardCurveFromDiscountCurve });
//
//	return model;
//}
	
	
}
