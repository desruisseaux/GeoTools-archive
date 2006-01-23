/*
 * MapScaleDenominatorImpl.java
 *
 * Created on 07 December 2004, 16:29
 */

package org.geotools.filter;

import org.geotools.feature.Feature;

/**
 * This class is actualy a place holder.  It resolves to 1.0 but should actualy be substituted for 
 * a literal that actualy contains the current map scale before use.
 * @author James
 * @source $URL$
 */
public class MapScaleDenominatorImpl extends DefaultExpression implements MapScaleDenominator  {
    
    /** Creates a new instance of MapScaleDenominatorImpl */
    public MapScaleDenominatorImpl() {
        
    }
    
    
    public Object getValue(Feature f){
        return new Double(1);
    }
    
    /**
     * Used by FilterVisitors to perform some action on this filter instance.
     * Typicaly used by Filter decoders, but may also be used by any thing
     * which needs infomration from filter structure. Implementations should
     * always call: visitor.visit(this); It is importatant that this is not
     * left to a parent class unless the  parents API is identical.
     *
     * @param visitor The visitor which requires access to this filter, the
     *        method must call visitor.visit(this);
     */
    public void accept(FilterVisitor visitor) {
        visitor.visit(this);
    }
    
    public String toString(){
        return MapScaleDenominator.EV_NAME;
    }
}
