/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2003-2006, GeoTools Project Managment Committee (PMC)
 *    (C) 2001, Institut de Recherche pour le D�veloppement
 *    (C) 1999, Fisheries and Oceans Canada
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
 */
package org.geotools.measure;

// J2SE dependencies
import java.io.Serializable;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.text.Format;
import java.text.ParseException;
import java.util.Locale;

// Geotools dependencies
import org.geotools.resources.ClassChanger;


/**
 * An angle in degrees. An angle is the amount of rotation needed to bring one line or plane
 * into coincidence with another, generally measured in degrees, sexagesimal degrees or grads.
 *
 * @since 2.0
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @see Latitude
 * @see Longitude
 * @see AngleFormat
 */
public class Angle implements Comparable, Serializable {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 1158747349433104534L;
    
    /**
     * A shared instance of {@link AngleFormat}.
     */
    private static Reference format;
    
    /**
     * Define how angle can be converted to {@link Number} objects.
     */
    static {
        ClassChanger.register(new ClassChanger(Angle.class, Double.class) {
            protected Number convert(final Comparable o) {
                return new Double(((Angle) o).theta);
            }
            
            protected Comparable inverseConvert(final Number value) {
                return new Angle(value.doubleValue());
            }
        });
    }
    
    /**
     * Angle value in degres.
     */
    private final double theta;
    
    /**
     * Contruct a new angle with the specified value.
     *
     * @param theta Angle in degrees.
     */
    public Angle(final double theta) {
        this.theta = theta;
    }
    
    /**
     * Constructs a newly allocated {@code Angle} object that represents the angle value
     * represented by the string. The string should represents an angle in either fractional
     * degrees (e.g. 45.5�) or degrees with minutes and seconds (e.g. 45�30').
     *
     * @param  string A string to be converted to an {@code Angle}.
     * @throws NumberFormatException if the string does not contain a parsable angle.
     */
    public Angle(final String string) throws NumberFormatException {
        try {
            final Angle theta = (Angle) getAngleFormat().parseObject(string);
            if (getClass().isAssignableFrom(theta.getClass())) {
                this.theta = theta.theta;
            } else {
                throw new NumberFormatException();
            }
        }
        catch (ParseException exception) {
            NumberFormatException e=new NumberFormatException(exception.getLocalizedMessage());
            e.initCause(exception);
            throw e;
        }
    }
    
    /**
     * Returns the angle value in degrees.
     */
    public double degrees() {
        return theta;
    }
    
    /**
     * Returns the angle value in radians.
     */
    public double radians() {
        return Math.toRadians(theta);
    }
    
    /**
     * Returns a hash code for this {@code Angle} object.
     */
    public int hashCode() {
        final long code = Double.doubleToLongBits(theta);
        return (int) code ^ (int) (code >>> 32);
    }
    
    /**
     * Compares the specified object with this angle for equality.
     */
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (object!=null && getClass().equals(object.getClass())) {
            final Angle that = (Angle) object;
            return Double.doubleToLongBits(this.theta) ==
                   Double.doubleToLongBits(that.theta);
        }  else {
            return false;
        }
    }
    
    /**
     * Compares two {@code Angle} objects numerically. The comparaison
     * is done as if by the {@link Double#compare(double,double)} method.
     */
    public int compareTo(final Object that) {
        return Double.compare(this.theta, ((Angle)that).theta);
    }
    
    /**
     * Returns a string representation of this {@code Angle} object.
     */
    public String toString() {
        return getAngleFormat().format(this, new StringBuffer(), null).toString();
    }
    
    /**
     * Returns a shared instance of {@link AngleFormat}. The return type is
     * {@link Format} in order to avoid class loading before necessary.
     */
    private static synchronized Format getAngleFormat() {
        if (format!=null) {
            final Format angleFormat = (Format) format.get();
            if (angleFormat!=null) {
                return angleFormat;
            }
        }
        final Format newFormat = new AngleFormat("D�MM.m'", Locale.US);
        format = new SoftReference(newFormat);
        return newFormat;
    }
}
