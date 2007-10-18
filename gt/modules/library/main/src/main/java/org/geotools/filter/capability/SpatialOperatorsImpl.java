package org.geotools.filter.capability;

import org.opengis.filter.capability.SpatialOperator;
import org.opengis.filter.capability.SpatialOperators;

/**
 * Implementation of the SpatialOperators interface.
 * 
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public class SpatialOperatorsImpl implements SpatialOperators {

    SpatialOperator[] operators;
    
    public SpatialOperatorsImpl( SpatialOperator[] operators ) {
        if ( operators == null ) {
            operators = new SpatialOperator[]{};
        }
        this.operators = operators;
    }
    
    public SpatialOperator[] getOperators() {
        return operators;
    }
    
    public SpatialOperator getOperator(String name) {
        if ( name == null ) {
            return null;
        }
        
        for ( int i = 0; i < operators.length; i++ ) {
            if ( name.equals( operators[i].getName() ) ) {
                return operators[i];
            }
        }
        
        return null;
    }
}
