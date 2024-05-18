package net.finmath.lch.initialmargin.simulation.modeldata;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import net.finmath.stochastic.RandomVariable;

/**
 * Class to store a time series of random variables. Each random variable is associated with a LocalDateTime and represents the end of date value of a zero rate.
 * The random variable will be a Scalar for historical data or a random variable with n-realizations for model generated data.
 * 
 * @author Raphael Prandtl
 *
 */
public class RandomVariableSeries {
	
	
	private final HashMap<LocalDateTime, RandomVariable> randomVariableSeries;
	
	
	public RandomVariableSeries(HashMap<LocalDateTime, RandomVariable> randomVariableSeries) {
		this.randomVariableSeries = randomVariableSeries;
	}

	
	public RandomVariableSeries() {
		this.randomVariableSeries = new HashMap<>();
	}
	
	
	/**
	 * Appends the provided random variable series to the existing one, overwrites duplicates.
	 * @param randomVariableSeries The values that should be added
	 */
	public void addSeries(HashMap<LocalDateTime, RandomVariable> randomVariableSeries) {
		this.randomVariableSeries.putAll(randomVariableSeries);
	}
	
	
	/**
	 * @return The complete time series of random variables.
	 */
	public HashMap<LocalDateTime, RandomVariable> getSeries() {
		return randomVariableSeries;
	}
	
	
	/**
	 * @param evaluationDate The date for which the random variable should be returned
	 * @return The value for the input date
	 */
	public RandomVariable getValue(LocalDateTime evaluationDate) {
		return randomVariableSeries.get(evaluationDate);
	}
	
	
	/**
	 * Performs for each date an addition of two tenor point series.
	 * Assumes both time series have the same keys, otherwise IllegalArgumentException is thrown.
	 * 
	 * @param randomVariableSeries The addend
	 * @throws IllegalArgumentException
	 */
	public void sum(RandomVariableSeries randomVariableSeries) {
		if (this.randomVariableSeries.isEmpty()) {
			this.randomVariableSeries.putAll(randomVariableSeries.getSeries());
		} else if (this.randomVariableSeries.keySet().equals(randomVariableSeries.getSeries().keySet())) {
			for (Map.Entry<LocalDateTime, RandomVariable> entry : randomVariableSeries.getSeries().entrySet()) {
				this.randomVariableSeries.merge(entry.getKey(), entry.getValue(), RandomVariable::add);
			}	
		} else {
			throw new IllegalArgumentException("Can't add rate series with different dates.");
		}
	}
	
	
	/**
	 * Filters the time series for the lowest values. 
	 * If no path or state is provided, the average of the random variable is use for comparison otherwise a path-wise comparison
	 * 
	 * @param numberOfLowestValues The number of how many values should be returned
	 * @param pathOrState The path fpr which the values should be returned. Null input equals average of all paths
	 * @return The lowest value of the time series
	 */
	public List<LocalDateTime> getNLowestValues(int numberOfLowestValues, Integer pathOrState) {
		PriorityQueue<LocalDateTime> worstDates;
		// if pathOrState == null we average the PnL on all paths as an approximation
		if (pathOrState == null) {
			worstDates = new PriorityQueue<>(Comparator.comparingDouble(date -> {
		    	return randomVariableSeries.get(date).mult(-1.0).getAverage();
		    }));
		// otherwise we compare the PnL pathWise
		} else {
			worstDates = new PriorityQueue<>(Comparator.comparingDouble(date -> {
		    	return randomVariableSeries.get(date).get(pathOrState) * (-1);
		    }));
		}
		for (LocalDateTime date : randomVariableSeries.keySet()) {
			worstDates.add(date);
			if (worstDates.size() > numberOfLowestValues) {
				worstDates.poll();
			}
		}
		return new ArrayList<>(worstDates);
	}
	

}
