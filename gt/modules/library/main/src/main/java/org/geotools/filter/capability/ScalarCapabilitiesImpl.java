package org.geotools.filter.capability;

import org.opengis.filter.capability.ArithmeticOperators;
import org.opengis.filter.capability.ComparisonOperators;
import org.opengis.filter.capability.ScalarCapabilities;

/**
 * /**
 * Implementation of the ScalarCapabilities interface.
 * 
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public class ScalarCapabilitiesImpl implements ScalarCapabilities {

    ArithmeticOperators arithmeticOperators;
    ComparisonOperators comparisonOperators;
    boolean logicalOperators;
    
    public ScalarCapabilitiesImpl( ComparisonOperators comparisonOperators, 
        ArithmeticOperators arithmeticOperators, boolean logicalOperators ) {
        this.arithmeticOperators = arithmeticOperators;
        this.comparisonOperators = comparisonOperators;
        this.logicalOperators = logicalOperators;
    }

    
    public ArithmeticOperators getArithmeticOperators() {
        return arithmeticOperators;
    }

    public ComparisonOperators getComparisonOperators() {
        return comparisonOperators;
    }

    public boolean hasLogicalOperators() {
        return logicalOperators;
    }

}
