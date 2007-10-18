package org.geotools.filter.capability;

import org.opengis.filter.capability.FunctionName;

/**
 * Implementation of the FunctionName interface.
 * 
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public class FunctionNameImpl extends OperatorImpl implements FunctionName {

    int argumentCount;
    
    public FunctionNameImpl( String name, int argumentCount ) {
        super( name );
        this.argumentCount = argumentCount;
    }
    
    public int getArgumentCount() {
        return argumentCount;
    }

}
