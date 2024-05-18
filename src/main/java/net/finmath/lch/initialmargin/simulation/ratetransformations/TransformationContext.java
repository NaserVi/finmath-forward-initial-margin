package net.finmath.lch.initialmargin.simulation.ratetransformations;


import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.NavigableMap;

import net.finmath.stochastic.RandomVariable;


/**
 * Manages the transformation pipe line such that each transformation has access to the data it needs to operate on, 
 * and can access the results of previous transformations.
 * Holds the current state of the data (PrimaryData) and any intermediate results.
 *
 * @author Raphael Prandtl
 */
public class TransformationContext {
	
	    private NavigableMap<LocalDateTime, RandomVariable>							primaryData;
	    private Map<String, NavigableMap<LocalDateTime, RandomVariable>>			transformationResults;

	    
	    /**
	     * Sets up the transformation context and initialize it with the first data set of the transformation series.
	     * The transformation results are stored together with their unique ID.
	     * @param primaryData The most recent data set or result of the transformations.
	     */
	    public TransformationContext(NavigableMap<LocalDateTime, RandomVariable> primaryData) {
	        this.primaryData = primaryData;
	        this.transformationResults = new HashMap<String, NavigableMap<LocalDateTime, RandomVariable>>();
	    }

	    
	    /**
	     * 
	     * @return The current primary data set, i.e. the most recent result.
	     */
	    public NavigableMap<LocalDateTime, RandomVariable> getPrimaryData() {
	        return primaryData;
	    }

	    
	    /**
	     * Replaces the previous primary data with the new provided data set.
	     * @param primaryData New primary data set.
	     */
	    public void setPrimaryData(NavigableMap<LocalDateTime, RandomVariable> primaryData) {
	        this.primaryData = primaryData;
	    }

	    
	    /**
	     * Adds the result of a transformation together with the ID and count of the transformation to the data pipeline.
	     * @param transformationId The unique ID of the transformation.
	     * @param data The data set that will be added to the pipeline.
	     */
	    public void addTransformationResult(String transformationId, NavigableMap<LocalDateTime, RandomVariable> data) {
	    	transformationResults.put(transformationId, data);
	    }

	    
	    /**
	     * Retrieve a specific transformation result based on the ID and instance number.
	     * @param transformationId The unique ID of the transformation.
	     * @return The 2D RandomVariable array of this transformation.
	     */
	    public NavigableMap<LocalDateTime, RandomVariable> getTransformationResult(String transformationId) {
	    	return transformationResults.get(transformationId);
	    }
	    
	    
	    /**
	     * 
	     * @return The whole Map of the applied transformations.
	     */
	    public Map<String, NavigableMap<LocalDateTime, RandomVariable>> getTransformationResultMap() {
	        return new HashMap<>(transformationResults);
	    }
	    
	    	    
	}
