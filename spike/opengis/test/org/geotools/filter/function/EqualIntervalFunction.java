/*
 * EqualRangesClassificationFunction.java
 *
 * Created on October 28, 2004, 3:13 PM
 */

package org.geotools.filter.function;

import java.util.Iterator;

import org.geotools.filter.Expression;
import org.opengis.feature.Attribute;
import org.opengis.feature.Feature;
import org.opengis.feature.FeatureCollection;

/**
 *
 * @author  jfc173
 */
public class EqualIntervalFunction extends ClassificationFunction{
    
    FeatureCollection fc = null;
    double min = 0;
    double max = 0;
    
    /** Creates a new instance of EqualRangesClassificationFunction */
    public EqualIntervalFunction() {
    }
    
    public String getName() {
        return "EqualInterval";
    }    
    
    private void calculateMinAndMax(){
        Iterator<Feature> it = fc.features();
        min = Double.POSITIVE_INFINITY;
        max = Double.NEGATIVE_INFINITY;
        while (it.hasNext()){
            Feature f = it.next();
            double value = ((Number) expr.getValue(f)).doubleValue();
            if (value > max){
                max = value;
            }
            if (value < min){
                min = value;
            }            
        }
    }
    
    public void setNumberOfClasses(int i){
        classNum = i;
    }
    
    protected int calculateSlot(double val) {
        if(val >= max) return classNum-1;

        double slotWidth = (max - min) / classNum;
        return (int) Math.floor((val - min) / slotWidth);
    }    
    
    public Object getValue(Attribute att){
    	throw new UnsupportedOperationException(
    			"Needs to define how to get the enclosing FeatureCollection for a given Attribute. " +
    			"May be XPath.get(\"/\")?");
    	/*
        FeatureCollection coll = feature.getParent();
        if (!(coll.equals(fc))){
            fc = coll;
            calculateMinAndMax();
        }
        int slot = calculateSlot(((Number) expr.getValue(feature)).doubleValue());        
        return new Integer(slot);
        */
    }
    
    public void setExpression (Expression e){
        super.setExpression(e);
        if (fc != null){
            calculateMinAndMax();
        }
    }
    
}
