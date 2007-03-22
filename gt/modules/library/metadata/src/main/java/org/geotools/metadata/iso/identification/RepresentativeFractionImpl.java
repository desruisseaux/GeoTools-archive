package org.geotools.metadata.iso.identification;

import org.opengis.metadata.identification.RepresentativeFraction;

/**
 * Set this up as a number - because it is.
 * 
 * @author Jody
 */
public class RepresentativeFractionImpl extends Number implements RepresentativeFraction {
    private static final long serialVersionUID = 7228422109144637537L;
    int denominator;
    
    public RepresentativeFractionImpl( int denominator ){
        this.denominator = denominator;
    }
    
    public static RepresentativeFraction fromDouble( double value ){
        return new RepresentativeFractionImpl( (int) (1.0 / value) ); // flip!
    }
    public double doubleValue() {
        return 1.0 / (double) denominator;
    }

    public float floatValue() {
        return 1.0f / (float) denominator;
    }

    public int intValue() {
        return 0;
    }

    public long longValue() {
        return 0;
    }

    public int getDenominator() {
        return denominator;
    }

    public boolean equals( Object obj ) {
        if( obj == null || !(obj instanceof RepresentativeFraction) ){
            return false;
        }
        RepresentativeFraction other = (RepresentativeFraction) obj;
        return denominator == other.getDenominator();
    }
    public int hashCode() {
        return denominator;
    }
}
