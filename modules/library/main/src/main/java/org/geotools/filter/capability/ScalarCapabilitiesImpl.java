package org.geotools.filter.capability;

import org.opengis.filter.capability.ArithmeticOperators;
import org.opengis.filter.capability.ComparisonOperators;
import org.opengis.filter.capability.ScalarCapabilities;

/**
 * Implementation of the ScalarCapabilities interface.
 * 
 * @author Justin Deoliveira, The Open Planning Project
 */
public class ScalarCapabilitiesImpl implements ScalarCapabilities {

    ArithmeticOperatorsImpl arithmeticOperators;
    ComparisonOperatorsImpl comparisonOperators;
    boolean logicalOperators;

    public ScalarCapabilitiesImpl() {
        arithmeticOperators = new ArithmeticOperatorsImpl();
        comparisonOperators = new ComparisonOperatorsImpl();
        logicalOperators = false;
    }

    public ScalarCapabilitiesImpl( ComparisonOperators comparisonOperators,
            ArithmeticOperators arithmeticOperators, boolean logicalOperators ) {
        this.arithmeticOperators = toArithmeticOperatorsImpl(arithmeticOperators);
        this.comparisonOperators = toComparisonOperatorsImpl(comparisonOperators);
        this.logicalOperators = logicalOperators;
    }

    public ScalarCapabilitiesImpl( ScalarCapabilities copy ) {
        arithmeticOperators = copy.getArithmeticOperators() == null ?
                new ArithmeticOperatorsImpl() :
                    new ArithmeticOperatorsImpl( copy.getArithmeticOperators() );
                
        comparisonOperators = copy.getComparisonOperators() == null ?
                new ComparisonOperatorsImpl() :
                    new ComparisonOperatorsImpl( copy.getComparisonOperators());
                
        logicalOperators = copy.hasLogicalOperators();
    }

    public void setArithmeticOperators( ArithmeticOperatorsImpl arithmeticOperators ) {
        this.arithmeticOperators = arithmeticOperators;
    }

    public ArithmeticOperatorsImpl getArithmeticOperators() {
        return arithmeticOperators;
    }

    public void setComparisonOperators( ComparisonOperatorsImpl comparisonOperators ) {
        this.comparisonOperators = comparisonOperators;
    }
    public ComparisonOperatorsImpl getComparisonOperators() {
        return comparisonOperators;
    }

    public void setLogicalOperators( boolean logicalOperators ) {
        this.logicalOperators = logicalOperators;
    }
    public boolean hasLogicalOperators() {
        return logicalOperators;
    }

    public static ComparisonOperatorsImpl toComparisonOperatorsImpl(
            ComparisonOperators comparisonOperators ) {
        if (comparisonOperators == null) {
            return new ComparisonOperatorsImpl();
        }
        if (comparisonOperators instanceof ComparisonOperatorsImpl) {
            return (ComparisonOperatorsImpl) comparisonOperators;
        } else {
            return new ComparisonOperatorsImpl(comparisonOperators);
        }
    }
    private static ArithmeticOperatorsImpl toArithmeticOperatorsImpl(
            ArithmeticOperators arithmeticOperators ) {
        if (arithmeticOperators == null) {
            return new ArithmeticOperatorsImpl();
        } else if (arithmeticOperators instanceof ArithmeticOperatorsImpl) {
            return (ArithmeticOperatorsImpl) arithmeticOperators;
        } else {
            return new ArithmeticOperatorsImpl(arithmeticOperators);
        }
    }
    public void addAll( ScalarCapabilities copy ) {
        if( copy == null ) return;
        if( copy.getArithmeticOperators() != null ){
            arithmeticOperators.addAll( copy.getArithmeticOperators() );
        }
        if( copy.getComparisonOperators() != null){
            comparisonOperators.addAll( copy.getComparisonOperators() );    
        }        
        if( copy.hasLogicalOperators() == true ){
            logicalOperators = true;
        }
    }
}
