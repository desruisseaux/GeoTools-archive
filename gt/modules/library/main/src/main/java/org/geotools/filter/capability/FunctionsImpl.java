package org.geotools.filter.capability;

import org.opengis.filter.capability.Function;
import org.opengis.filter.capability.Functions;

/**
 * Implementation of the Functions interface.
 * 
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public class FunctionsImpl implements Functions {

    Function[] functionNames;
    
    public FunctionsImpl( Function[] functionNames ) {
        if ( functionNames == null ) {
            functionNames = new Function[]{};
        }
        
        this.functionNames = functionNames;
    }
    
    public Function[] getFunctionNames() {
        return functionNames;
    }
    
    public Function getFunctionName(String name) {
        if ( name == null ) {
            return null;
        }
        
        for ( int i = 0; i < functionNames.length; i++ ) {
            if ( name.equals( functionNames[i].getName() ) ) {
                return functionNames[i];
            }
        }
        
        return null;
    }


}
