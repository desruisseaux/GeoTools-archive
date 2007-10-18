package org.geotools.filter.capability;

import org.opengis.filter.capability.Function;

/**
 * Implementation of the FunctionName interface.
 * 
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public class FunctionNameImpl extends OperatorImpl implements Function {

    int argumentCount;
    
    public FunctionNameImpl( String name, int argumentCount ) {
        super( name );
        this.argumentCount = argumentCount;
    }
    
    public int getArgumentCount() {
        return argumentCount;
    }

}
