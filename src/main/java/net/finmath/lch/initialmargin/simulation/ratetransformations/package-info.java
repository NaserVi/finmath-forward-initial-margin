/**
 * Contains methods to apply transformations on rate series. Each method has as input and output a NavigableMap<LocalDateTime, RandomVariable>.
 * When setting up a Transformation Context each transformation result will be stored and can be accessed with an identifier (ID) for reuse of later calculations.
 * 
 * @author Raphael Prandtl
 */
package net.finmath.lch.initialmargin.simulation.ratetransformations;