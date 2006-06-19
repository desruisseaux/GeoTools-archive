/*
 * ClassificationExpression.java
 *
 * Created on November 4, 2003, 2:34 PM
 */

package edu.psu.geovista.geotools.filter;

/**
 *
 * @author  jfc173
 */


import org.geotools.filter.Expression;
import org.geotools.filter.FilterVisitor;
import org.geotools.filter.Filter;
import org.geotools.feature.*;
import java.util.Random;

public class ClassificationExpression implements Expression{
    
    private Filter filter;
    private Random r = new Random();
    
    /** Creates a new instance of ClassificationExpression */
    public ClassificationExpression() {
    }
    
    public void accept(FilterVisitor visitor) {
        visitor.visit(this);
    }
    
    public short getType() {
        //Placeholder to be filled later.
        return (short) 59;
    }
    
    public void setFilter(Filter f){
        filter = f;
        //stores the filter and then does absolutely nothing with it!
    }
    
    public Object getValue(Feature feature) {
        return new Integer(r.nextInt(5));
    }
    
}
