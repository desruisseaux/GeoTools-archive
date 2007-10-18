package org.geotools.filter.capability;

import org.opengis.filter.capability.ArithmeticOperators;
import org.opengis.filter.capability.Functions;

/**
 * Implementation of the ArithmeticOperators interface.
 * 
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public class ArithmeticOperatorsImpl implements ArithmeticOperators {

    boolean simpleArithmetic;
    Functions functions;
    
    public ArithmeticOperatorsImpl( boolean simpleArtithmetic, Functions functions ) {
        this.simpleArithmetic = simpleArtithmetic;
        this.functions = functions;
    }
    
    public boolean hasSimpleArithmetic() {
        return simpleArithmetic;
    }

    public Functions getFunctions() {
        return functions;
    }
}
