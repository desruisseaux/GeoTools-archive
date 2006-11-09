/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004-2006, GeoTools Project Managment Committee (PMC)
 *    
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    reated on October 27, 2004, 11:27 AM
 */
package org.geotools.filter.function;

import java.util.Arrays;
import java.util.List;

import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.filter.Expression;
import org.opengis.filter.FilterFactory;
import org.geotools.filter.FilterFactoryFinder;
import org.geotools.filter.FunctionExpression;
import org.geotools.filter.FunctionExpressionImpl;
import org.geotools.filter.LiteralExpression;
import org.geotools.util.ProgressListener;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.expression.Literal;

/**
 * Parent for classifiers which break a feature collection into the specified number of classes.
 * 
 * @author James Macgill
 * @author Cory Horner, Refractions Research Inc.
 * @source $URL$
 */
public abstract class ClassificationFunction extends FunctionExpressionImpl implements FunctionExpression {

    FeatureCollection featureCollection = null;    
    ProgressListener progress;
    
    Expression[] arguments = new Expression[2];
    List parameters = Arrays.asList( arguments );
    
    /** Creates a new instance of ClassificationFunction */
    public ClassificationFunction() {
    }
    
    public int getArgCount() {
        return 2;
    }
    
    /**
     * Will return -1 if not a literal constant.
     * 
     * @return
     */
    public int getNumberOfClasses(){
        Expression expr = arguments[1];
        Object value = null;
        if( expr != null && expr instanceof Literal ){
            Literal literal = (Literal) expr;
            value = literal.getValue();
        }
        else if( featureCollection != null ){
            value = expr.evaluate( featureCollection );            
        }
        if( value != null && value instanceof Integer){
            return ((Integer)value).intValue();
        }
        return -1;        
    }
    public void setNumberOfClasses(int slots ){
        FilterFactory ff = CommonFactoryFinder.getFilterFactory( null );         
        arguments[1] = (Expression) ff.literal( slots );        
    }
    
    public FeatureCollection getCollection() {
    	return featureCollection;
    }
    
    public void setCollection (FeatureCollection fc) {
    	this.featureCollection = fc;
    }
        
    public ProgressListener getProgressListener() {
        return progress;
    }
    
    public void setProgressListener(ProgressListener progress) {
        this.progress = progress;
    }
    
    public Expression getExpression(){
        return arguments[0];
    }
    
    public void setExpression(Expression e){
        arguments[0] = e;
    }

    public Expression[] getArgs(){
        return arguments;
    }
    /**
     * Returns the function parameters.
     */
    public List getParameters() {
        return parameters;
    }
    
    /**
     * Sets the function paramters.
     */
    public void setParameters(List params) {
        parameters.set(0, params.get(0));
        parameters.set(1, params.get(1));        
    }
    public abstract String getName();
    
    public void setArgs(Expression[] args){
        arguments[0] = args[0];
        arguments[1] = args[1];        
    }
    
    public abstract Object evaluate(Feature feature);
    
	public Object getValue(int index) {
		return null;
	}

    /**
	 * Determines the number of decimal places to truncate the interval at
	 * (public for testing purposes only).
	 * 
	 * @param slotWidth
	 */
    protected int decimalPlaces(double slotWidth) {
    	int val = (new Double(Math.log(1.0/slotWidth)/2.0)).intValue();
    	if (val < 0) return 0;
    	else return val+1;
    }
    
    /**
	 * Truncates a double to a certain number of decimals places. Note:
	 * truncation at zero decimal places will still show up as x.0, since we're
	 * using the double type.
	 * 
	 * @param value
	 *            number to round-off
	 * @param decimalPlaces
	 *            number of decimal places to leave
	 * @return the rounded value
	 */
    protected double round(double value, int decimalPlaces) {
    	double divisor = Math.pow(10, decimalPlaces);
    	double newVal = value * divisor;
    	newVal =  (new Long(Math.round(newVal)).intValue())/divisor; 
    	return newVal;
    }
    
    /**
	 * Corrects a round off operation by incrementing or decrementing the
	 * decimal place (preferably the smallest one).  This should usually be used to adjust the bounds to include a value.  Example: 0.31-->0.44 where 0.44 is the maximum value and end of the range.  We could just make the , round(0.31, 1)=0.3; round(0.44 max value = 0.49
	 * 
	 * @param value
	 * @param decimalPlaces
	 * @param up
	 */
    protected double fixRound(double value, int decimalPlaces, boolean up) {
    	double divisor = Math.pow(10, decimalPlaces);
    	double newVal = value * divisor;
    	if (up) newVal++; //+0.001 (for 3 dec places)
    	else newVal--; //-0.001
    	newVal =  newVal/divisor; 
    	return newVal;
    }

}
