package org.geotools.filter;

import java.util.Iterator;
import java.util.List;

import org.geotools.factory.Hints;
import org.opengis.filter.expression.Function;

/**
 * Isolate function lookup code from Factory implementation(s).
 * <p>
 * This is done to look for two things:
 * <ul>
 * <li>org.geotools.filter.Function
 * <li>org.opengis.filter.expression.Function
 * </ul>
 * This is done as a proper utility class that accepts Hints.
 * 
 * @author Jody Garnett
 */
public class FunctionFinder {    
    public FunctionFinder( Hints hints ){
        // currently hints are not used, need help :-P
    }
   
    public Function findFunction( String name ){
        return findFunction( name, null );
    }

    public Function findFunction( String name, List/*<Expression>*/ parameters ){
        name = functionName(name);

        try {
            for( Iterator it = org.geotools.factory.FactoryFinder.factories(FunctionExpression.class); it.hasNext(); ){
                FunctionExpression function = (FunctionExpression) it.next();
                if( name.equalsIgnoreCase( function.getName() ) ){
                	if ( parameters != null ) {
                		function.setParameters( parameters );	
                	}
                	
                    return function;
                }
            }
            for( Iterator i = org.geotools.factory.FactoryFinder.factories(FunctionImpl.class); i.hasNext(); ){
                FunctionImpl function = (FunctionImpl) i.next();
                if( name.equalsIgnoreCase( function.getName() ) ){
                	function.setParameters( parameters );
                    return function;
                }
            }
        }
        catch( Exception e ){
            throw new RuntimeException("Unable to create class " + name + "Function", e );  
        }
        throw new RuntimeException("Unable to find function " + name );
    }
    
    private String functionName( String name ) {
        int index = -1;

        if ((index = name.indexOf("Function")) != -1) {
            name = name.substring(0, index);
        }

        name = name.toLowerCase().trim();
        char c = name.charAt(0);
        name = name.replaceFirst("" + c, "" + Character.toUpperCase(c));

        return name;
    }
}
