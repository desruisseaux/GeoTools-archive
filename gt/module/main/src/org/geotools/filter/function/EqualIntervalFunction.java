/*
 * EqualRangesClassificationFunction.java
 *
 * Created on October 28, 2004, 3:13 PM
 */

package org.geotools.filter.function;

import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.filter.Expression;

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
        FeatureIterator it = fc.features();
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
    
    public Object getValue(Feature feature){
        FeatureCollection coll = feature.getParent();
        if (!(coll.equals(fc))){
            fc = coll;
            calculateMinAndMax();
        }
        int slot = calculateSlot(((Number) expr.getValue(feature)).doubleValue());        
        return new Integer(slot);
    }
    
    public void setExpression (Expression e){
        super.setExpression(e);
        if (fc != null){
            calculateMinAndMax();
        }
    }
    
}
