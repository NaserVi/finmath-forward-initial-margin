package net.finmath.lch.initialmargin.swapclear.aggregationscheme;


import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import javafx.util.Pair;
import net.finmath.time.businessdaycalendar.BusinessdayCalendar;
import net.finmath.time.businessdaycalendar.BusinessdayCalendarExcludingTARGETHolidays;


/**
 * Storage of name conventions and static mappings for the Initial Margin scheme.
 */
public class SchemeStorage {
	
	
	public enum Currency {
		AUD, CAD, CHF, DKK, EUR, GBP, JPY, NZD, SEK, SGD, USD
		};
	
		
	public enum CurveName { 
		EUR_EURIBOR, EUR_EUROSTR, EUR_EURIBOR_1M, EUR_EURIBOR_3M, EUR_EURIBOR_6M, EUR_EURIBOR_12M, 
		EUR_EURIBOR_1x3, EUR_EURIBOR_1x6, EUR_EURIBOR_1x12, EUR_EURIBOR_3x6, EUR_EURIBOR_3x12, EUR_EURIBOR_6x12,
		
		}

	
	/*
	 * Retrieve the risk-free curve of each currency
	 */
	public static CurveName getOisCurve(Currency currency) {
		return CURRENCY_TO_OIS_CURVE.get(currency);
	}

	private static final Map<Currency, CurveName> CURRENCY_TO_OIS_CURVE;
	static {
		CURRENCY_TO_OIS_CURVE = new EnumMap<>(Currency.class);

		CURRENCY_TO_OIS_CURVE.put(Currency.EUR, CurveName.EUR_EUROSTR);
	}
	
	
	/*
	 * The standard curve of each currency which is used for the PAIRS model
	 */
	public static CurveName getStandardCurve(Currency currency) {
		return CURRENCY_TO_STANDARD_CURVE.get(currency);
	}
	
	private static final Map<Currency, CurveName> CURRENCY_TO_STANDARD_CURVE;
	static {
		CURRENCY_TO_STANDARD_CURVE = new EnumMap<>(Currency.class);
		
		CURRENCY_TO_STANDARD_CURVE.put(Currency.EUR, CurveName.EUR_EURIBOR);
	}
	
	
	/*
	 * Each currency has a standard / main spread curve which has the highest priority for spread netting
	 */
	public static CurveName getStandardSpreadCurve(Currency currency) {
		return CURRENCY_TO_STANDARD_SPREAD_CURVE.get(currency);
	}
	
	private static final Map<Currency, CurveName> CURRENCY_TO_STANDARD_SPREAD_CURVE;
	static {
		CURRENCY_TO_STANDARD_SPREAD_CURVE = new EnumMap<>(Currency.class);
		
		CURRENCY_TO_STANDARD_SPREAD_CURVE.put(Currency.EUR, CurveName.EUR_EURIBOR_6M);
	}
	

	/*
	 * Retrieve the spread curve of a curve pair
	 */
	public static CurveName getSpreadCurve(CurveName shorterCurve, CurveName longerCurve) {
		return CURVES_TO_SPREAD_CURVE.get(new Pair<>(shorterCurve, longerCurve));
	}

    private static final Map<Pair<CurveName, CurveName>, CurveName> CURVES_TO_SPREAD_CURVE;
	static {
		CURVES_TO_SPREAD_CURVE = new HashMap<>();
		
        CURVES_TO_SPREAD_CURVE.put(new Pair<>(CurveName.EUR_EURIBOR_1M, CurveName.EUR_EURIBOR_3M), CurveName.EUR_EURIBOR_1x3);
        CURVES_TO_SPREAD_CURVE.put(new Pair<>(CurveName.EUR_EURIBOR_1M, CurveName.EUR_EURIBOR_6M), CurveName.EUR_EURIBOR_1x6);
        CURVES_TO_SPREAD_CURVE.put(new Pair<>(CurveName.EUR_EURIBOR_1M, CurveName.EUR_EURIBOR_12M), CurveName.EUR_EURIBOR_1x12);
        CURVES_TO_SPREAD_CURVE.put(new Pair<>(CurveName.EUR_EURIBOR_3M, CurveName.EUR_EURIBOR_6M), CurveName.EUR_EURIBOR_3x6);
        CURVES_TO_SPREAD_CURVE.put(new Pair<>(CurveName.EUR_EURIBOR_3M, CurveName.EUR_EURIBOR_12M), CurveName.EUR_EURIBOR_3x12);
        CURVES_TO_SPREAD_CURVE.put(new Pair<>(CurveName.EUR_EURIBOR_6M, CurveName.EUR_EURIBOR_12M), CurveName.EUR_EURIBOR_6x12);

	}
	
	
	/*
	 * Retrieve a curve pair from a spread curve
	 */
	public static Pair<CurveName, CurveName> getCurvePair(CurveName spreadCurve) {
		return SPREAD_CURVE_TO_CURVE_PAIR.get(spreadCurve);
	}

    private static final Map<CurveName, Pair<CurveName, CurveName>> SPREAD_CURVE_TO_CURVE_PAIR;
	static {
		SPREAD_CURVE_TO_CURVE_PAIR = new HashMap<>();
		
		SPREAD_CURVE_TO_CURVE_PAIR.put(CurveName.EUR_EURIBOR_1x3, new Pair<>(CurveName.EUR_EURIBOR_1M, CurveName.EUR_EURIBOR_3M));
		SPREAD_CURVE_TO_CURVE_PAIR.put(CurveName.EUR_EURIBOR_1x6, new Pair<>(CurveName.EUR_EURIBOR_1M, CurveName.EUR_EURIBOR_6M));
		SPREAD_CURVE_TO_CURVE_PAIR.put(CurveName.EUR_EURIBOR_1x12, new Pair<>(CurveName.EUR_EURIBOR_1M, CurveName.EUR_EURIBOR_12M));
		SPREAD_CURVE_TO_CURVE_PAIR.put(CurveName.EUR_EURIBOR_3x6, new Pair<>(CurveName.EUR_EURIBOR_3M, CurveName.EUR_EURIBOR_6M));
		SPREAD_CURVE_TO_CURVE_PAIR.put(CurveName.EUR_EURIBOR_3x12, new Pair<>(CurveName.EUR_EURIBOR_3M, CurveName.EUR_EURIBOR_12M));
		SPREAD_CURVE_TO_CURVE_PAIR.put(CurveName.EUR_EURIBOR_6x12, new Pair<>(CurveName.EUR_EURIBOR_6M, CurveName.EUR_EURIBOR_12M));

	}
	
	
	/*
	 * The local calendar for which the indices in a specific currency are quoted 
	 */
	public static BusinessdayCalendar getLocalCalendar(Currency currency) {
		return CURRENCY_TO_CALENDAR.get(currency);
	}
	
	private static final Map<Currency, BusinessdayCalendar> CURRENCY_TO_CALENDAR;
	static {
		CURRENCY_TO_CALENDAR = new EnumMap<>(Currency.class);
		
		CURRENCY_TO_CALENDAR.put(Currency.EUR, new BusinessdayCalendarExcludingTARGETHolidays()); // Just for testing. Normally, storage of individual local calendars 
	}

}
