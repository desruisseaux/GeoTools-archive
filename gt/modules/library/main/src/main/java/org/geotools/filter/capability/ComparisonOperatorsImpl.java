package org.geotools.filter.capability;

import org.opengis.filter.capability.ComparisonOperators;
import org.opengis.filter.capability.Operator;

/**
 * Implementation of the ComparisonOperators interface.
 * 
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public class ComparisonOperatorsImpl implements ComparisonOperators {

    Operator[] operators;
    
    public ComparisonOperatorsImpl( Operator[] operators ) {
        if ( operators == null ) 
            operators = new Operator[]{};
        
        this.operators = operators;
    }
    
    public Operator[] getOperators() {
        return operators;
    }
    
    public Operator getOperator(String name) {
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
