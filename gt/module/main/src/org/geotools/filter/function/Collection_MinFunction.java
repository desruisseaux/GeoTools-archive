/*
 * CollectionMinFunction.java
 *
 * Created on May 11, 2005, 6:21 PM
 */

package org.geotools.filter.function;

import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.filter.Expression;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.FunctionExpression;
import org.geotools.filter.FunctionExpressionImpl;

/**
 *
 * @author James
 */
public class Collection_MinFunction extends FunctionExpressionImpl implements FunctionExpression{
    
    
    FeatureCollection fc = null;
    double min = 0;
    Expression expr;
    
    /** Creates a new instance of EqualRangesClassificationFunction */
    public Collection_MinFunction() {
    }
    
    public String getName() {
        return "Collection_Min";
    }    

    public int getArgCount() {
        return 1;
    }
    
    private void calculateMin(){
        FeatureIterator it = fc.features();
        min = Double.POSITIVE_INFINITY;
        while (it.hasNext()){
            Feature f = it.next();
            double value = ((Number) expr.getValue(f)).doubleValue();
           
            if (value < min){
                min = value;
            }            
        }
    }
    
    public void setArgs(Expression[] args){
        expr = args[0];
    }
    
    public Object getValue(Feature feature){
        FeatureCollection coll = feature.getParent();
        if (!(coll.equals(fc))){
            fc = coll;
            calculateMin();
        }
        return new Double(min);
    }
    
    public void setExpression (Expression e){
        expr = e;
        if (fc != null){
            calculateMin();
        }
    }
   
    
    public Expression[] getArgs(){
        Expression[] ret = new Expression[1];
        ret[0] = expr;
        return ret;
    }
    
    /**
     * Return this function as a string.
     *
     * @return String representation of this min function.
     */
    public String toString() {
        return "Collection_Min( " + expr + ")";
    }
    
}

