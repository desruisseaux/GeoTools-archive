package org.geotools.filter.capability;

import org.opengis.filter.capability.Operator;

/**
 * Implementation of the Operator interface.
 * 
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public class OperatorImpl implements Operator {

    String name;
    
    public OperatorImpl( String name ) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }

}
