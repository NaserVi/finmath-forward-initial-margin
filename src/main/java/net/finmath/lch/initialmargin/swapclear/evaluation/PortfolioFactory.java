package net.finmath.lch.initialmargin.swapclear.evaluation;

import java.time.LocalDate;
import java.time.Month;

import net.finmath.lch.initialmargin.swapclear.aggregationscheme.SchemeStorage;
import net.finmath.lch.initialmargin.swapclear.aggregationscheme.SchemeStorage.Currency;
import net.finmath.lch.initialmargin.swapclear.aggregationscheme.SchemeStorage.CurveName;
import net.finmath.lch.initialmargin.swapclear.libormodels.ModelFactory;
import net.finmath.lch.initialmargin.swapclear.products.GlobalPortfolio;
import net.finmath.lch.initialmargin.swapclear.products.LchSwap;
import net.finmath.lch.initialmargin.swapclear.products.components.LchNotional;
import net.finmath.lch.initialmargin.swapclear.products.components.LchNotionalFromConstant;
import net.finmath.lch.initialmargin.swapclear.products.components.LchSwapLeg;
import net.finmath.lch.initialmargin.swapclear.products.indices.LchAbstractIndex;
import net.finmath.lch.initialmargin.swapclear.products.indices.LchLIBORIndex;
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
import net.finmath.time.Schedule;
import net.finmath.time.ScheduleGenerator;
import net.finmath.time.TimeDiscretizationFromArray;
import net.finmath.time.businessdaycalendar.BusinessdayCalendar;

public class PortfolioFactory {
	
	
    public static GlobalPortfolio getSwapPortfolio(LchSwap... swaps) {
        GlobalPortfolio globalPortfolio = new GlobalPortfolio();
        for (LchSwap swap : swaps) {
            globalPortfolio.addSwap(swap);
        }
        return globalPortfolio;
    }
    
    public static void main(String args[]) {
		System.out.println(getParSwapRate(create15YPayerSwap()));
    }
	
	
	public static LchSwap create5YPayerSwap() {		
		// Create 5y EUR swap on 3M EURIBOR
		final Currency euro = Currency.EUR;
		final BusinessdayCalendar eurCalendar = SchemeStorage.getLocalCalendar(euro);
		
		final CurveName euribor3m = CurveName.EUR_EURIBOR_3M;
		final LocalDate	referenceDate = LocalDate.of(2024, Month.FEBRUARY, 05);
		final int			spotOffsetDays = 2;
		final String		forwardStartPeriod = "0D";
		final String		maturity = "5Y";
		final String		frequency = "quarterly";
		final String		daycountConvention = "30/360"; 
		
		final LchNotional notional = new LchNotionalFromConstant(1.0);
		final LchAbstractIndex floatIndex = new LchLIBORIndex(0.0, 0.25);
		final double spread = 0.025979;
		final Schedule schedule = ScheduleGenerator.createScheduleFromConventions(referenceDate, spotOffsetDays, forwardStartPeriod, maturity, frequency, daycountConvention, "first", "following", eurCalendar, -2, 0);
		
		final LchSwapLeg floatLeg_3M_EURIBOR = new LchSwapLeg(schedule, notional, floatIndex, 0.0, false /* isPayLeg*/, false /* isNotionalExchanged */);
		final LchSwapLeg fixLeg_3M_EURIBOR = new LchSwapLeg(schedule, notional, null, spread, true /* isPayLeg*/, false /* isNotionalExchanged */);
		final LchSwap swap_3M_EURIBOR = new LchSwap(euro, euribor3m, floatLeg_3M_EURIBOR, fixLeg_3M_EURIBOR);
		
		return swap_3M_EURIBOR;	
	}
	
	
	public static LchSwap create5YReceiverSwap() {		
		// Create 5y EUR swap on 3M EURIBOR
		final Currency euro = Currency.EUR;
		final BusinessdayCalendar eurCalendar = SchemeStorage.getLocalCalendar(euro);
		
		final CurveName euribor3m = CurveName.EUR_EURIBOR_3M;
		final LocalDate	referenceDate = LocalDate.of(2024, Month.FEBRUARY, 05);
		final int			spotOffsetDays = 2;
		final String		forwardStartPeriod = "0D";
		final String		maturity = "5Y";
		final String		frequency = "quarterly";
		final String		daycountConvention = "30/360"; 
		
		final LchNotional notional = new LchNotionalFromConstant(1.0);
		final LchAbstractIndex floatIndex = new LchLIBORIndex(0.0, 0.25);
		final double spread = 0.025979;
		final Schedule schedule = ScheduleGenerator.createScheduleFromConventions(referenceDate, spotOffsetDays, forwardStartPeriod, maturity, frequency, daycountConvention, "first", "following", eurCalendar, -2, 0);
		
		final LchSwapLeg fixLeg_3M_EURIBOR = new LchSwapLeg(schedule, notional, null, spread, false /* isPayLeg*/, false /* isNotionalExchanged */);
		final LchSwapLeg floatLeg_3M_EURIBOR = new LchSwapLeg(schedule, notional, floatIndex, 0.0, true /* isPayLeg*/, false /* isNotionalExchanged */);
		final LchSwap swap_3M_EURIBOR = new LchSwap(euro, euribor3m, fixLeg_3M_EURIBOR, floatLeg_3M_EURIBOR);
		
		return swap_3M_EURIBOR;	
	}	
	
	public static LchSwap create10YPayerSwap() {		
		// Create 5y EUR swap on 3M EURIBOR
		final Currency euro = Currency.EUR;
		final BusinessdayCalendar eurCalendar = SchemeStorage.getLocalCalendar(euro);
		
		final CurveName euribor3m = CurveName.EUR_EURIBOR_3M;
		final LocalDate	referenceDate = LocalDate.of(2024, Month.FEBRUARY, 05);
		final int			spotOffsetDays = 2;
		final String		forwardStartPeriod = "0D";
		final String		maturity = "10Y";
		final String		frequency = "quarterly";
		final String		daycountConvention = "30/360"; 
		
		final LchNotional notional = new LchNotionalFromConstant(1.0);
		final LchAbstractIndex floatIndex = new LchLIBORIndex(0.0, 0.25);
		final double spread = 0.0266132;
		final Schedule schedule = ScheduleGenerator.createScheduleFromConventions(referenceDate, spotOffsetDays, forwardStartPeriod, maturity, frequency, daycountConvention, "first", "following", eurCalendar, -2, 0);
		
		final LchSwapLeg floatLeg_3M_EURIBOR = new LchSwapLeg(schedule, notional, floatIndex, 0.00, false /* isPayLeg*/, false /* isNotionalExchanged */);
		final LchSwapLeg fixLeg_3M_EURIBOR = new LchSwapLeg(schedule, notional, null, spread, true /* isPayLeg*/, false /* isNotionalExchanged */);
		final LchSwap swap_3M_EURIBOR = new LchSwap(euro, euribor3m, floatLeg_3M_EURIBOR, fixLeg_3M_EURIBOR);
		
		return swap_3M_EURIBOR;	
	}
	
	public static LchSwap create10YReceiverSwap() {		
		// Create 5y EUR swap on 3M EURIBOR
		final Currency euro = Currency.EUR;
		final BusinessdayCalendar eurCalendar = SchemeStorage.getLocalCalendar(euro);
		
		final CurveName euribor3m = CurveName.EUR_EURIBOR_3M;
		final LocalDate	referenceDate = LocalDate.of(2024, Month.FEBRUARY, 05);
		final int			spotOffsetDays = 2;
		final String		forwardStartPeriod = "0D";
		final String		maturity = "10Y";
		final String		frequency = "quarterly";
		final String		daycountConvention = "30/360"; 
		
		final LchNotional notional = new LchNotionalFromConstant(1.0);
		final LchAbstractIndex floatIndex = new LchLIBORIndex(0.0, 0.25);
		final double spread = 0.0266132;
		final Schedule schedule = ScheduleGenerator.createScheduleFromConventions(referenceDate, spotOffsetDays, forwardStartPeriod, maturity, frequency, daycountConvention, "first", "following", eurCalendar, -2, 0);
		
		final LchSwapLeg floatLeg_3M_EURIBOR = new LchSwapLeg(schedule, notional, floatIndex, 0.00, true /* isPayLeg*/, false /* isNotionalExchanged */);
		final LchSwapLeg fixLeg_3M_EURIBOR = new LchSwapLeg(schedule, notional, null, spread, false /* isPayLeg*/, false /* isNotionalExchanged */);
		final LchSwap swap_3M_EURIBOR = new LchSwap(euro, euribor3m, fixLeg_3M_EURIBOR, floatLeg_3M_EURIBOR);
		
		return swap_3M_EURIBOR;	
	}
	
	public static LchSwap create15YPayerSwap() {		
		// Create 5y EUR swap on 3M EURIBOR
		final Currency euro = Currency.EUR;
		final BusinessdayCalendar eurCalendar = SchemeStorage.getLocalCalendar(euro);
		
		final CurveName euribor3m = CurveName.EUR_EURIBOR_3M;
		final LocalDate	referenceDate = LocalDate.of(2024, Month.FEBRUARY, 05);
		final int			spotOffsetDays = 2;
		final String		forwardStartPeriod = "0D";
		final String		maturity = "15Y";
		final String		frequency = "quarterly";
		final String		daycountConvention = "30/360"; 
		
		final LchNotional notional = new LchNotionalFromConstant(1.0);
		final LchAbstractIndex floatIndex = new LchLIBORIndex(0.0, 0.25);
		final double spread = 0.027603;
		final Schedule schedule = ScheduleGenerator.createScheduleFromConventions(referenceDate, spotOffsetDays, forwardStartPeriod, maturity, frequency, daycountConvention, "first", "following", eurCalendar, -2, 0);
		
		final LchSwapLeg floatLeg_3M_EURIBOR = new LchSwapLeg(schedule, notional, floatIndex, 0.0, false /* isPayLeg*/, false /* isNotionalExchanged */);
		final LchSwapLeg fixLeg_3M_EURIBOR = new LchSwapLeg(schedule, notional, null, spread, true /* isPayLeg*/, false /* isNotionalExchanged */);
		final LchSwap swap_3M_EURIBOR = new LchSwap(euro, euribor3m, floatLeg_3M_EURIBOR, fixLeg_3M_EURIBOR);
		
		return swap_3M_EURIBOR;	
	}
	
	public static LchSwap create15YReceiverSwap() {		
		// Create 5y EUR swap on 3M EURIBOR
		final Currency euro = Currency.EUR;
		final BusinessdayCalendar eurCalendar = SchemeStorage.getLocalCalendar(euro);
		
		final CurveName euribor3m = CurveName.EUR_EURIBOR_3M;
		final LocalDate	referenceDate = LocalDate.of(2024, Month.FEBRUARY, 05);
		final int			spotOffsetDays = 2;
		final String		forwardStartPeriod = "0D";
		final String		maturity = "15Y";
		final String		frequency = "quarterly";
		final String		daycountConvention = "30/360"; 
		
		final LchNotional notional = new LchNotionalFromConstant(1.0);
		final LchAbstractIndex floatIndex = new LchLIBORIndex(0.0, 0.25);
		final double spread = 0.027603;
		final Schedule schedule = ScheduleGenerator.createScheduleFromConventions(referenceDate, spotOffsetDays, forwardStartPeriod, maturity, frequency, daycountConvention, "first", "following", eurCalendar, -2, 0);
		
		final LchSwapLeg fixLeg_3M_EURIBOR = new LchSwapLeg(schedule, notional, null, spread, false /* isPayLeg*/, false /* isNotionalExchanged */);
		final LchSwapLeg floatLeg_3M_EURIBOR = new LchSwapLeg(schedule, notional, floatIndex, 0.0, true /* isPayLeg*/, false /* isNotionalExchanged */);
		final LchSwap swap_3M_EURIBOR = new LchSwap(euro, euribor3m, fixLeg_3M_EURIBOR, floatLeg_3M_EURIBOR);
		
		return swap_3M_EURIBOR;	
	}
	
	public static LchSwap create20YPayerSwap() {		
		// Create 5y EUR swap on 3M EURIBOR
		final Currency euro = Currency.EUR;
		final BusinessdayCalendar eurCalendar = SchemeStorage.getLocalCalendar(euro);
		
		final CurveName euribor3m = CurveName.EUR_EURIBOR_3M;
		final LocalDate	referenceDate = LocalDate.of(2024, Month.FEBRUARY, 05);
		final int			spotOffsetDays = 2;
		final String		forwardStartPeriod = "0D";
		final String		maturity = "20Y";
		final String		frequency = "quarterly";
		final String		daycountConvention = "30/360"; 
		
		final LchNotional notional = new LchNotionalFromConstant(1.0);
		final LchAbstractIndex floatIndex = new LchLIBORIndex(0.0, 0.25);
		final double spread = 0.04;
		final Schedule schedule = ScheduleGenerator.createScheduleFromConventions(referenceDate, spotOffsetDays, forwardStartPeriod, maturity, frequency, daycountConvention, "first", "following", eurCalendar, -2, 0);
		
		final LchSwapLeg floatLeg_3M_EURIBOR = new LchSwapLeg(schedule, notional, floatIndex, 0.0, false /* isPayLeg*/, false /* isNotionalExchanged */);
		final LchSwapLeg fixLeg_3M_EURIBOR = new LchSwapLeg(schedule, notional, null, spread, true /* isPayLeg*/, false /* isNotionalExchanged */);
		final LchSwap swap_3M_EURIBOR = new LchSwap(euro, euribor3m, floatLeg_3M_EURIBOR, fixLeg_3M_EURIBOR);
		
		return swap_3M_EURIBOR;	
	}

	public static double getParSwapRate(LchSwap swap) {	
		int numberOfPeriods = swap.getLegReceiver().getSchedule().getNumberOfPeriods();
		final double[]	swapTenor = new double[numberOfPeriods + 1];
		for (int periodStartIndex = 0; periodStartIndex < numberOfPeriods; periodStartIndex++) {
			swapTenor[periodStartIndex] = swap.getLegReceiver().getSchedule().getFixing(periodStartIndex);
		}
		swapTenor[numberOfPeriods] = swap.getLegReceiver().getSchedule().getPayment(numberOfPeriods -1);
		
		final AnalyticModel curveModel = getAnalyticModel(CurveName.EUR_EURIBOR_3M);
		// Create the forward curve (initial value of the LIBOR market model)
		final ForwardCurve forwardCurve = curveModel.getForwardCurve("ForwardCurveFromDiscountCurve(" + CurveName.EUR_EURIBOR_3M.toString() + ",3M)");
		final DiscountCurve discountCurve = curveModel.getDiscountCurve(CurveName.EUR_EURIBOR_3M.toString());

		return getParSwaprate(forwardCurve, discountCurve, swapTenor);
	}
	
	
	private static double getParSwaprate(final ForwardCurve forwardCurve, final DiscountCurve discountCurve, final double[] swapTenor) {
		return net.finmath.marketdata.products.Swap.getForwardSwapRate(new TimeDiscretizationFromArray(swapTenor), new TimeDiscretizationFromArray(swapTenor), forwardCurve, discountCurve);
	}
	
	
	public static AnalyticModel getAnalyticModel(CurveName curveName) {
		
		final double[] zeroRateFixings = new double[] {0.0, 0.2493151, 0.4986301, 0.7479452, 1.0000000, 1.4986301, 2.0000000, 3.0000000, 4.0000000, 5.0000000, 6.0000000, 7.0000000, 8.0000000, 9.0000000, 10.0000000, 12.0000000, 15.0000000, 20.0000000, 25.0000000, 30.0000000, 35.0000000, 40.0000000, 50.0000000, 60.0000000};	
		final double[] zeroRates = new double[]{0.0, 0.039679454, 0.038092025, 0.036301049, 0.034411283, 0.03127043, 0.029134774, 0.026978123, 0.026084691, 0.02578301, 0.025724468, 0.025808707, 0.025986709, 0.026228892, 0.026532052, 0.02712804, 0.02767065, 0.027235979, 0.026004347, 0.024754381, 0.023677185, 0.022869038, 0.021163335, 0.019995148};
	
		// Create a discount curve
		final DiscountCurveInterpolation discountCurveInterpolation = DiscountCurveInterpolation.createDiscountCurveFromZeroRates(
				curveName.toString() /* name */,
				ModelFactory.REFERENCE_DATE.toLocalDate()	/* referenceDate */,
				zeroRateFixings	/* maturities */,
				zeroRates		/* rates */,
				InterpolationMethod.LINEAR,
				ExtrapolationMethod.CONSTANT,
				InterpolationEntity.LOG_OF_VALUE
				);
		
		// Single curve setup
		final ForwardCurve	forwardCurveFromDiscountCurve = new ForwardCurveFromDiscountCurve(
				discountCurveInterpolation.getName(),
				ModelFactory.REFERENCE_DATE.toLocalDate(),
				"3M");
		
		AnalyticModel model = new AnalyticModelFromCurvesAndVols(new Curve[] { discountCurveInterpolation, forwardCurveFromDiscountCurve });
	
		return model;
	}
}
