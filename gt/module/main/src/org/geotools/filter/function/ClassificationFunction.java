/*
 * ClassificationFunction.java
 *
 * Created on October 27, 2004, 11:27 AM
 */

package org.geotools.filter.function;

import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.filter.Expression;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.FilterFactoryFinder;
import org.geotools.filter.FunctionExpression;
import org.geotools.filter.FunctionExpressionImpl;
import org.geotools.filter.LiteralExpression;
import org.geotools.util.ProgressListener;

/**
 *
 * @author James Macgill
 * @author Cory Horner, Refractions Research Inc.
 */
public abstract class ClassificationFunction extends FunctionExpressionImpl implements FunctionExpression {

	public static final int MODE_NULL_KEEP = 1 << 1;
	public static final int MODE_NULL_IGNORE = 1 << 2;
	public static final int MODE_NULL_ISOLATE = 1 << 3;
	public static final int MODE_NaN_KEEP = 1 << 4;
	public static final int MODE_NaN_IGNORE = 1 << 5;
	public static final int MODE_NaN_ISOLATE = 1 << 6;
	
    FeatureCollection fc = null;
    int classNum;
    Expression expr; 
    ProgressListener progress;
    int mode = MODE_NaN_IGNORE | MODE_NULL_IGNORE;
    
    /** Creates a new instance of ClassificationFunction */
    public ClassificationFunction() {
    }
    
    public int getArgCount() {
        return 2;
    }
    
    public int getNumberOfClasses(){
        return classNum;
    }
    
    public void setNumberOfClasses(int i){
        classNum = i;
    }
    
    public FeatureCollection getCollection() {
    	return fc;
    }
    
    public void setCollection (FeatureCollection fc) {
    	this.fc = fc;
    }
    
    public Expression getExpression(){
        return expr;
    }
    
    public void setExpression(Expression e){
        expr = e;
    }
    
    public ProgressListener getProgressListener() {
    	return progress;
    }
    
    public void setProgressListener(ProgressListener progress) {
    	this.progress = progress;
    }
    
    public Expression[] getArgs(){
        Expression[] ret = new Expression[2];
        ret[0] = expr;
        FilterFactory ff = FilterFactoryFinder.createFilterFactory();
        ret[1] = ff.createLiteralExpression(classNum);
        return ret;
    }
    
    public int getMode() {
    	return mode;
    }
    
    /**
     * Sets the mode from ClassificationFunction.MODE_... static constants.<br>
     * Usage:<br>
     * setMode(ClassificationFunction.MODE_NaN_IGNORE | ClassificationFunction.MODE_NULL_IGNORE);<br>
     * only one of MODE_NaN_ and MODE_NULL_ may be used.<br>
     * NOTE: this is not fully implemented yet.
     * 
     * @param mode flag(s) dictating the classifier operation
     */
    public void setMode(int mode) {
    	this.mode = mode;
    }
    
    public boolean isModeSet(int mode) {
    	return ((this.mode & mode) != 0);
    }
    
    public abstract String getName();
    
    public void setArgs(Expression[] args){
        expr = args[0];
        classNum = ((Number) ((LiteralExpression) args[1]).getLiteral()).intValue();
    }
    
    public abstract Object getValue(Feature feature);
    
	public Object getValue(int index) {
		return null;
	}

    /**
	 * Determines the number of decimal places to truncate the interval at
	 * (public for testing purposes only).
	 * 
	 * @param slotWidth
	 * @return
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
	 * @return
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
