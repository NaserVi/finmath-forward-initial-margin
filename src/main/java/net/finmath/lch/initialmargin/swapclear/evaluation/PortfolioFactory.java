package net.finmath.lch.initialmargin.swapclear.evaluation;

import java.time.LocalDate;
import java.time.Month;

import net.finmath.lch.initialmargin.swapclear.aggregationscheme.SchemeStorage;
import net.finmath.lch.initialmargin.swapclear.aggregationscheme.SchemeStorage.Currency;
import net.finmath.lch.initialmargin.swapclear.aggregationscheme.SchemeStorage.CurveName;
import net.finmath.lch.initialmargin.swapclear.products.GlobalPortfolio;
import net.finmath.lch.initialmargin.swapclear.products.LchSwap;
import net.finmath.lch.initialmargin.swapclear.products.components.LchNotional;
import net.finmath.lch.initialmargin.swapclear.products.components.LchNotionalFromConstant;
import net.finmath.lch.initialmargin.swapclear.products.components.LchSwapLeg;
import net.finmath.lch.initialmargin.swapclear.products.indices.LchAbstractIndex;
import net.finmath.lch.initialmargin.swapclear.products.indices.LchLIBORIndex;
import net.finmath.time.Schedule;
import net.finmath.time.ScheduleGenerator;
import net.finmath.time.businessdaycalendar.BusinessdayCalendar;

public class PortfolioFactory {
	
	
    public static GlobalPortfolio getSwapPortfolio(LchSwap... swaps) {
        GlobalPortfolio globalPortfolio = new GlobalPortfolio();
        for (LchSwap swap : swaps) {
            globalPortfolio.addSwap(swap);
        }
        return globalPortfolio;
    }
	
	
	public static LchSwap create5YPayerSwap() {		
		// Create 5y EUR swap on 3M EURIBOR
		final Currency euro = Currency.EUR;
		final BusinessdayCalendar eurCalendar = SchemeStorage.getLocalCalendar(euro);
		
		final CurveName euribor3m = CurveName.EUR_EURIBOR_3M;
		final LocalDate	referenceDate = LocalDate.of(2024, Month.APRIL, 02);
		final int			spotOffsetDays = 2;
		final String		forwardStartPeriod = "0D";
		final String		maturity = "5Y";
		final String		frequency = "quarterly";
		final String		daycountConvention = "30/360"; 
		
		final LchNotional notional = new LchNotionalFromConstant(1.0);
		final LchAbstractIndex floatIndex = new LchLIBORIndex(0.0, 0.25);
		final double spread = 0.05;
		final Schedule schedule = ScheduleGenerator.createScheduleFromConventions(referenceDate, spotOffsetDays, forwardStartPeriod, maturity, frequency, daycountConvention, "first", "following", eurCalendar, -2, 0);
		
		final LchSwapLeg receiverLeg_3M_EURIBOR = new LchSwapLeg(schedule, notional, floatIndex, 0.0, true /* isPayLeg*/, false /* isNotionalExchanged */);
		final LchSwapLeg payerLeg_3M_EURIBOR = new LchSwapLeg(schedule, notional, null, spread, false /* isPayLeg*/, false /* isNotionalExchanged */);
		final LchSwap swap_3M_EURIBOR = new LchSwap(euro, euribor3m, receiverLeg_3M_EURIBOR, payerLeg_3M_EURIBOR);
		
		return swap_3M_EURIBOR;	
	}
	
	public static LchSwap create10YReceiverSwap() {		
		// Create 5y EUR swap on 3M EURIBOR
		final Currency euro = Currency.EUR;
		final BusinessdayCalendar eurCalendar = SchemeStorage.getLocalCalendar(euro);
		
		final CurveName euribor3m = CurveName.EUR_EURIBOR_3M;
		final LocalDate	referenceDate = LocalDate.of(2024, Month.APRIL, 2);
		final int			spotOffsetDays = 2;
		final String		forwardStartPeriod = "0D";
		final String		maturity = "10Y";
		final String		frequency = "quarterly";
		final String		daycountConvention = "30/360"; 
		
		final LchNotional notional = new LchNotionalFromConstant(1.0);
		final LchAbstractIndex floatIndex = new LchLIBORIndex(0.0, 0.25);
		final double spread = 0.02;
		final Schedule schedule = ScheduleGenerator.createScheduleFromConventions(referenceDate, spotOffsetDays, forwardStartPeriod, maturity, frequency, daycountConvention, "first", "following", eurCalendar, -2, 0);
		
		final LchSwapLeg receiverLeg_3M_EURIBOR = new LchSwapLeg(schedule, notional, null, spread, false /* isPayLeg*/, false /* isNotionalExchanged */);
		final LchSwapLeg payerLeg_3M_EURIBOR = new LchSwapLeg(schedule, notional, floatIndex, 0.0, true /* isPayLeg*/, false /* isNotionalExchanged */);
		final LchSwap swap_3M_EURIBOR = new LchSwap(euro, euribor3m, receiverLeg_3M_EURIBOR, payerLeg_3M_EURIBOR);
		
		return swap_3M_EURIBOR;	
	}
	
	public static LchSwap create10YPayerSwapWithFloatSpread() {		
		// Create 5y EUR swap on 3M EURIBOR
		final Currency euro = Currency.EUR;
		final BusinessdayCalendar eurCalendar = SchemeStorage.getLocalCalendar(euro);
		
		final CurveName euribor3m = CurveName.EUR_EURIBOR_3M;
		final LocalDate	referenceDate = LocalDate.of(2024, Month.APRIL, 2);
		final int			spotOffsetDays = 2;
		final String		forwardStartPeriod = "0D";
		final String		maturity = "10Y";
		final String		frequency = "quarterly";
		final String		daycountConvention = "30/360"; 
		
		final LchNotional notional = new LchNotionalFromConstant(1.0);
		final LchAbstractIndex floatIndex = new LchLIBORIndex(0.0, 0.25);
		final double payerSpread = 0.03;
		final Schedule schedule = ScheduleGenerator.createScheduleFromConventions(referenceDate, spotOffsetDays, forwardStartPeriod, maturity, frequency, daycountConvention, "first", "following", eurCalendar, -2, 0);
		
		final LchSwapLeg receiverLeg_3M_EURIBOR = new LchSwapLeg(schedule, notional, floatIndex, 0.02, true /* isPayLeg*/, false /* isNotionalExchanged */);
		final LchSwapLeg payerLeg_3M_EURIBOR = new LchSwapLeg(schedule, notional, null, payerSpread, false /* isPayLeg*/, false /* isNotionalExchanged */);
		final LchSwap swap_3M_EURIBOR = new LchSwap(euro, euribor3m, receiverLeg_3M_EURIBOR, payerLeg_3M_EURIBOR);
		
		return swap_3M_EURIBOR;	
	}
	
	public static LchSwap create15YPayerSwap() {		
		// Create 5y EUR swap on 3M EURIBOR
		final Currency euro = Currency.EUR;
		final BusinessdayCalendar eurCalendar = SchemeStorage.getLocalCalendar(euro);
		
		final CurveName euribor3m = CurveName.EUR_EURIBOR_3M;
		final LocalDate	referenceDate = LocalDate.of(2024, Month.APRIL, 2);
		final int			spotOffsetDays = 2;
		final String		forwardStartPeriod = "0D";
		final String		maturity = "15Y";
		final String		frequency = "quarterly";
		final String		daycountConvention = "30/360"; 
		
		final LchNotional notional = new LchNotionalFromConstant(1.0);
		final LchAbstractIndex floatIndex = new LchLIBORIndex(0.0, 0.25);
		final double spread = 0.06;
		final Schedule schedule = ScheduleGenerator.createScheduleFromConventions(referenceDate, spotOffsetDays, forwardStartPeriod, maturity, frequency, daycountConvention, "first", "following", eurCalendar, -2, 0);
		
		final LchSwapLeg receiverLeg_3M_EURIBOR = new LchSwapLeg(schedule, notional, floatIndex, 0.0, true /* isPayLeg*/, false /* isNotionalExchanged */);
		final LchSwapLeg payerLeg_3M_EURIBOR = new LchSwapLeg(schedule, notional, null, spread, false /* isPayLeg*/, false /* isNotionalExchanged */);
		final LchSwap swap_3M_EURIBOR = new LchSwap(euro, euribor3m, receiverLeg_3M_EURIBOR, payerLeg_3M_EURIBOR);
		
		return swap_3M_EURIBOR;	
	}
	
	public static LchSwap create20YPayerSwap() {		
		// Create 5y EUR swap on 3M EURIBOR
		final Currency euro = Currency.EUR;
		final BusinessdayCalendar eurCalendar = SchemeStorage.getLocalCalendar(euro);
		
		final CurveName euribor3m = CurveName.EUR_EURIBOR_3M;
		final LocalDate	referenceDate = LocalDate.of(2024, Month.APRIL, 2);
		final int			spotOffsetDays = 2;
		final String		forwardStartPeriod = "0D";
		final String		maturity = "20Y";
		final String		frequency = "quarterly";
		final String		daycountConvention = "30/360"; 
		
		final LchNotional notional = new LchNotionalFromConstant(1.0);
		final LchAbstractIndex floatIndex = new LchLIBORIndex(0.0, 0.25);
		final double spread = 0.04;
		final Schedule schedule = ScheduleGenerator.createScheduleFromConventions(referenceDate, spotOffsetDays, forwardStartPeriod, maturity, frequency, daycountConvention, "first", "following", eurCalendar, -2, 0);
		
		final LchSwapLeg receiverLeg_3M_EURIBOR = new LchSwapLeg(schedule, notional, floatIndex, 0.0, true /* isPayLeg*/, false /* isNotionalExchanged */);
		final LchSwapLeg payerLeg_3M_EURIBOR = new LchSwapLeg(schedule, notional, null, spread, false /* isPayLeg*/, false /* isNotionalExchanged */);
		final LchSwap swap_3M_EURIBOR = new LchSwap(euro, euribor3m, receiverLeg_3M_EURIBOR, payerLeg_3M_EURIBOR);
		
		return swap_3M_EURIBOR;	
	}

}
