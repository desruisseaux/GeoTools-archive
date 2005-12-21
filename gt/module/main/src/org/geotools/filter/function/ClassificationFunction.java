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
 */
public abstract class ClassificationFunction extends FunctionExpressionImpl implements FunctionExpression {
    
    FeatureCollection fc = null;
    int classNum;
    Expression expr; 
    ProgressListener progress;
    
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
    
    public abstract String getName();
    
    public void setArgs(Expression[] args){
        expr = args[0];
        classNum = ((Number) ((LiteralExpression) args[1]).getLiteral()).intValue();
    }
    
    public abstract Object getValue(Feature feature);
    
	public Object getValue(int index) {
		return null;
	}

}
