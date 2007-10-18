package org.geotools.filter.capability;

import org.opengis.filter.capability.FunctionName;
import org.opengis.filter.capability.Functions;

/**
 * Implementation of the Functions interface.
 * 
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public class FunctionsImpl implements Functions {

    FunctionName[] functionNames;
    
    public FunctionsImpl( FunctionName[] functionNames ) {
        if ( functionNames == null ) {
            functionNames = new FunctionName[]{};
        }
        
        this.functionNames = functionNames;
    }
    
    public FunctionName[] getFunctionNames() {
        return functionNames;
    }
    
    public FunctionName getFunctionName(String name) {
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
