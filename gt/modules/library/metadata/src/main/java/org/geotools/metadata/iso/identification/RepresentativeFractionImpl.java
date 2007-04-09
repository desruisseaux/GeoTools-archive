/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2007, GeoTools Project Managment Committee (PMC)
 *   
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    This package contains documentation from OpenGIS specifications.
 *    OpenGIS consortium's work is fully acknowledged here.
 */
package org.geotools.metadata.iso.identification;

// OpenGIS dependencies
import org.opengis.metadata.identification.RepresentativeFraction;

// Geotools dependencies
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.i18n.Errors;


/**
 * A scale where {@linkplain #getDenominator denominator} = {@code 1 / scale}.
 * This implementation is set up as a {@linkplain Number number} - because it is.
 * 
 * @source $URL: http://svn.geotools.org/geotools/trunk/gt/modules/library/metadata/src/main/java/org/geotools/metadata/iso/identification/ResolutionImpl.java $
 * @version $Id: ResolutionImpl.java 24868 2007-03-22 07:58:09Z jgarnett $
 * @author Jody Garnett
 *
 * @since 2.4
 */
public class RepresentativeFractionImpl extends Number implements RepresentativeFraction {
    /**
     * Serial number for compatibility with different versions.
     */
    private static final long serialVersionUID = 7228422109144637537L;

    /**
     * The number below the line in a vulgar fraction.
     */
    private final long denominator;

    /**
     * Creates a new representative fraction from the specified denominator.
     */
    public RepresentativeFractionImpl(final long denominator) {
        this.denominator = denominator;
    }

    /**
     * Creates a representative fraction from a scale as a {@code double} value.
     * The {@linkplain #getDenominator denominator} will be set to {@code 1/scale}.
     *
     * @param scale The scale.
     *
     * @throws IllegalArgumentException if the condition {@code abs(scale) <= 1} is not meet.
     *
     * @todo Should we rename this method as {@code fromScale}?
     */
    public static RepresentativeFraction fromDouble(final double scale)
            throws IllegalArgumentException
    {
        if (Math.abs(scale) <= 1 || scale == Double.POSITIVE_INFINITY) {
            // Note: we accept positive infinity, but not negative infinity because
            //       we can't represent a negative zero using 'long' primitive type.
            return new RepresentativeFractionImpl(Math.round(1.0 / scale)); // flip!
        } else {
            throw new IllegalArgumentException(Errors.format(ErrorKeys.ILLEGAL_ARGUMENT_$2,
                    "scale", new Double(scale)));
        }
    }
    
    public double toScale(){
        return doubleValue();
    }
    /**
     * Returns the scale in a form usable for computation.
     *
     * @return <code>1.0 / {@linkplain #getDenominator() denominator}</code>
     */
    public double doubleValue() {
        return 1.0 / (double) denominator;
    }

    /**
     * Returns the scale as a {@code float} type.
     */
    public float floatValue() {
        return 1.0f / (float) denominator;
    }

    /**
     * Returns 0 since the scale is a fraction between 0 and 1. Such value can not
     * be represented as an integer.
     */
    public long longValue() {
        return 0;
    }

    /**
     * Returns 0 since the scale is a fraction between 0 and 1. Such value can not
     * be represented as an integer.
     */
    public int intValue() {
        return 0;
    }

    /**
     * Returns the number below the line in a vulgar fraction.
     */
    public int getDenominator() {
        if (denominator < Integer.MIN_VALUE || denominator > Integer.MAX_VALUE) {
            throw new IllegalStateException(); // API change required.
        }
        return (int) denominator;
    }

    /**
     * Compares this object with the specified value for equality.
     */
    public boolean equals(final Object object) {
        /*
         * Note: 'equals(Object)' and 'hashCode()' implementations are defined in the interface,
         * in order to ensure that the following requirements hold:
         *
         * - a.equals(b) == b.equals(a)   (reflexivity)
         * - a.equals(b) implies (a.hashCode() == b.hashCode())
         */
        if (object instanceof RepresentativeFraction) {
            final RepresentativeFraction that = (RepresentativeFraction) object;
            return denominator == that.getDenominator();
        }
        return false;
    }

    /**
     * Returns a hash value for this representative fraction.
     */
    public int hashCode() {
        return (int) denominator;
    }
}
