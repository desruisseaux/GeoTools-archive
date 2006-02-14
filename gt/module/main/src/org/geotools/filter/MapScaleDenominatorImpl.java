/*
 * MapScaleDenominatorImpl.java
 *
 * Created on 07 December 2004, 16:29
 */

package org.geotools.filter;

import org.geotools.feature.Feature;
import org.opengis.filter.expression.ExpressionVisitor;
import org.opengis.filter.expression.Literal;

/**
 * This class is actualy a place holder.  It resolves to 1.0 but should actualy be substituted for 
 * a literal that actualy contains the current map scale before use.
 * @author James
 * @source $URL$
 * 
 */
public class MapScaleDenominatorImpl extends DefaultExpression implements MapScaleDenominator, Literal {
    
    /** Creates a new instance of MapScaleDenominatorImpl */
    public MapScaleDenominatorImpl() {
        
    }
    
    public Object evaluate(Feature f){
        return getValue();
    }
    
    public Object getValue() {
    	return new Double(1);
    }
    
    public void setValue(Object constant) {
    	throw new UnsupportedOperationException();
    }
    
    public Object accept(ExpressionVisitor visitor, Object extraData) {
    	return visitor.visit(this,extraData);
    }
    
    public String toString(){
        return MapScaleDenominator.EV_NAME;
    }
}
