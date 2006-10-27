/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004-2006, GeoTools Project Managment Committee (PMC)
 *    (C) 2004, Institut de Recherche pour le Développement
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
package org.geotools.metadata.iso.extent;

// J2SE dependencies
import java.util.Locale;
import java.awt.geom.Rectangle2D;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;

// OpenGIS dependencies
import org.opengis.metadata.extent.GeographicBoundingBox;
import org.opengis.referencing.operation.TransformException;
import org.opengis.spatialschema.geometry.Envelope;

// Geotools dependencies
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.i18n.ErrorKeys;


/**
 * Geographic position of the dataset. This is only an approximate
 * so specifying the co-ordinate reference system is unnecessary.
 *
 * @since 2.1
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Touraïvane
 */
public class GeographicBoundingBoxImpl extends GeographicExtentImpl
        implements GeographicBoundingBox
{
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -3278089380004172514L;

    /**
     * The method for constructing a bounding box from an envelope.
     * Will be obtained only when first needed.
     */
    private static Method constructor;

    /**
     * The method for constructing a string representation of this box.
     * Will be obtained only when first needed.
     */
    private static Method toString;

    /**
     * A bounding box ranging from 180°W to 180°E and 90°S to 90°N.
     *
     * @since 2.2
     */
    public static final GeographicBoundingBox WORLD;
    static {
        final GeographicBoundingBoxImpl world = new GeographicBoundingBoxImpl(-180, 180, -90, 90);
        world.freeze();
        WORLD = world;
    }

    /**
     * The western-most coordinate of the limit of the dataset extent.
     * The value is expressed in longitude in decimal degrees (positive east).
     */
    private double westBoundLongitude;

    /**
     * The eastern-most coordinate of the limit of the dataset extent.
     * The value is expressed in longitude in decimal degrees (positive east).
     */
    private double eastBoundLongitude;

    /**
     * The southern-most coordinate of the limit of the dataset extent.
     * The value is expressed in latitude in decimal degrees (positive north).
     */
    private double southBoundLatitude;

    /**
     * The northern-most, coordinate of the limit of the dataset extent.
     * The value is expressed in latitude in decimal degrees (positive north).
     */
    private double northBoundLatitude;

    /**
     * Constructs an initially empty geographic bounding box.
     */
    public GeographicBoundingBoxImpl() {
    }

    /**
     * Constructs a geographic bounding box initialized to the same values than the specified one.
     *
     * @since 2.2
     */
    public GeographicBoundingBoxImpl(final GeographicBoundingBox box) {
        super(box);
        setWestBoundLongitude(box.getWestBoundLongitude());
        setEastBoundLongitude(box.getEastBoundLongitude());
        setSouthBoundLatitude(box.getSouthBoundLatitude());
        setNorthBoundLatitude(box.getNorthBoundLatitude());
    }

    /**
     * Constructs a geographic bounding box from the specified envelope. If the envelope contains
     * a CRS, then the bounding box will be projected to the {@linkplain DefaultGeographicCRS#WGS84
     * WGS 84} CRS. Otherwise, the envelope is assumed already in WGS 84 CRS.
     * <p>
     * <strong>Note:</strong> This method is available only if the referencing module is
     * on the classpath.
     *
     * @param  envelope The envelope to use for initializing this geographic bounding box.
     * @throws UnsupportedOperationException if the referencing module is not on the classpath.
     * @throws TransformException if the envelope can't be transformed.
     *
     * @since 2.2
     */
    public GeographicBoundingBoxImpl(final Envelope envelope) throws TransformException {
        super(true);
        if (constructor == null) {
            // No need to synchronize.
            constructor = getMethod("copy",  new Class[] {
                            Envelope.class, GeographicBoundingBoxImpl.class});
        }
        try {
            invoke(constructor, new Object[] {envelope, this});
        } catch (InvocationTargetException exception) {
            final Throwable cause = exception.getTargetException();
            if (cause instanceof TransformException) {
                throw (TransformException) cause;
            }
            throw new UndeclaredThrowableException(cause);
        }
    }

    /**
     * Constructs a geographic bounding box from the specified rectangle.
     * The rectangle is assumed in {@linkplain DefaultGeographicCRS#WGS84 WGS 84} CRS.
     */
    public GeographicBoundingBoxImpl(final Rectangle2D bounds) {
        this(bounds.getMinX(), bounds.getMaxX(), bounds.getMinY(), bounds.getMaxY());
    }

    /**
     * Creates a geographic bounding box initialized to the specified values.
     */
    public GeographicBoundingBoxImpl(final double westBoundLongitude,
                                     final double eastBoundLongitude,
                                     final double southBoundLatitude,
                                     final double northBoundLatitude)
    {
        super(true);
        setWestBoundLongitude( westBoundLongitude);
        setEastBoundLongitude( eastBoundLongitude);
        setSouthBoundLatitude(southBoundLatitude );
        setNorthBoundLatitude(northBoundLatitude );
    }

    /**
     * Returns the western-most coordinate of the limit of the
     * dataset extent. The value is expressed in longitude in
     * decimal degrees (positive east).
     *
     * @return The western-most longitude between -180 and +180°.
     */
    public double getWestBoundLongitude() {
        return westBoundLongitude;
    }
    
    /**
     * Set the western-most coordinate of the limit of the
     * dataset extent. The value is expressed in longitude in
     * decimal degrees (positive east).
     */
    public synchronized void setWestBoundLongitude(final double newValue) {
        checkWritePermission();
        westBoundLongitude = newValue;
    }

    /**
     * Returns the eastern-most coordinate of the limit of the
     * dataset extent. The value is expressed in longitude in
     * decimal degrees (positive east).
     *
     * @return The eastern-most longitude between -180 and +180°.
     */
    public double getEastBoundLongitude() {
        return eastBoundLongitude;
    }

    /**
     * Set the eastern-most coordinate of the limit of the
     * dataset extent. The value is expressed in longitude in
     * decimal degrees (positive east).
     */
    public synchronized void setEastBoundLongitude(final double newValue) {
        checkWritePermission();
        eastBoundLongitude = newValue;
    }

    /**
     * Returns the southern-most coordinate of the limit of the
     * dataset extent. The value is expressed in latitude in
     * decimal degrees (positive north).
     *
     * @return The southern-most latitude between -90 and +90°.
     */
    public double getSouthBoundLatitude()  {
        return southBoundLatitude;
    }

    /**
     * Set the southern-most coordinate of the limit of the
     * dataset extent. The value is expressed in latitude in
     * decimal degrees (positive north).
     */
    public synchronized void setSouthBoundLatitude(final double newValue) {
        checkWritePermission();
        southBoundLatitude = newValue;
    }

    /**
     * Returns the northern-most, coordinate of the limit of the
     * dataset extent. The value is expressed in latitude in
     * decimal degrees (positive north).
     *
     * @return The northern-most latitude between -90 and +90°.
     */
    public double getNorthBoundLatitude()   {
        return northBoundLatitude;
    }

    /**
     * Set the northern-most, coordinate of the limit of the
     * dataset extent. The value is expressed in latitude in
     * decimal degrees (positive north).
     */
    public synchronized void setNorthBoundLatitude(final double newValue) {
        checkWritePermission();
        northBoundLatitude = newValue;
    }

    /**
     * Adds a geographic bounding box to this box. If the {@linkplain #getInclusion inclusion}
     * status is the same for this box and the box to be added, then the resulting bounding box
     * is the union of the two boxes. If the {@linkplain #getInclusion inclusion} status are
     * opposite (<cite>exclusion</cite>), then this method attempt to exclude the some area of
     * specified box from this box. The resulting bounding box is smaller if the exclusion can
     * be performed without ambiguity.
     *
     * @since 2.2
     */
    public synchronized void add(final GeographicBoundingBox box) {
        checkWritePermission();
        final double xmin = box.getWestBoundLongitude();
        final double xmax = box.getEastBoundLongitude();
        final double ymin = box.getSouthBoundLatitude();
        final double ymax = box.getNorthBoundLatitude();
        if (getInclusion() == box.getInclusion()) {
            if (xmin < westBoundLongitude) westBoundLongitude = xmin;
            if (xmax > eastBoundLongitude) eastBoundLongitude = xmax;
            if (ymin < southBoundLatitude) southBoundLatitude = ymin;
            if (ymax > northBoundLatitude) northBoundLatitude = ymax;
        } else {
            if (ymin <= southBoundLatitude && ymax >= northBoundLatitude) {
                if (xmin > westBoundLongitude) westBoundLongitude = xmin;
                if (xmax < eastBoundLongitude) eastBoundLongitude = xmax;
            }
            if (xmin <= westBoundLongitude && xmax >= eastBoundLongitude) {
                if (ymin > southBoundLatitude) southBoundLatitude = ymin;
                if (ymax < northBoundLatitude) northBoundLatitude = ymax;
            }
        }
    }

    /**
     * Declares this metadata and all its attributes as unmodifiable.
     */
    protected void freeze() {
        super.freeze();
    }

    /**
     * Compares this geographic bounding box with the specified object for equality.
     */
    public synchronized boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (object!=null && object.getClass().equals(getClass())) {
            final GeographicBoundingBoxImpl that = (GeographicBoundingBoxImpl) object;
            return Double.doubleToLongBits(this.southBoundLatitude) ==
                   Double.doubleToLongBits(that.southBoundLatitude) &&
                   Double.doubleToLongBits(this.northBoundLatitude) ==
                   Double.doubleToLongBits(that.northBoundLatitude) &&
                   Double.doubleToLongBits(this.eastBoundLongitude) ==
                   Double.doubleToLongBits(that.eastBoundLongitude) &&
                   Double.doubleToLongBits(this.westBoundLongitude) ==
                   Double.doubleToLongBits(that.westBoundLongitude);
        }
        return false;
    }

    /**
     * Returns a hash code value for this extent.
     */
    public synchronized int hashCode() {
        long code = serialVersionUID;
        code ^=           Double.doubleToLongBits(southBoundLatitude);
        code  = 37*code + Double.doubleToLongBits(northBoundLatitude);
        code  = 37*code + Double.doubleToLongBits(eastBoundLongitude);
        code  = 37*code + Double.doubleToLongBits(westBoundLongitude);
        return (int)code ^ (int)(code >>> 32);
    }

    /**
     * Returns a string representation of this extent using a default angle pattern.
     */
    public String toString() {
        return toString(this, "DD°MM'SS.s\"", null);
    }

    /**
     * Returns a string representation of the specified extent using the specified angle pattern
     * and locale. See {@link AngleFormat} for a description of angle patterns.
     *
     * @param box     The bounding box to format.
     * @param pattern The angle pattern (e.g. {@code DD°MM'SS.s"}.
     * @param locale  The locale, or {@code null} for the default one.
     *
     * @since 2.2
     */
    public static String toString(final GeographicBoundingBox box,
                                  final String                pattern,
                                  final Locale                locale)
    {
        if (toString == null) {
            // No need to synchronize.
            toString = getMethod("toString",  new Class[] {
                        GeographicBoundingBox.class, String.class, Locale.class});
        }
        try {
            return String.valueOf(invoke(toString, new Object[] {box, pattern, locale}));
        } catch (InvocationTargetException exception) {
            throw new UndeclaredThrowableException(exception.getTargetException());
        }
    }

    /**
     * Returns a helper method which depends on the referencing module. We use reflection
     * since we can't have a direct dependency to this module.
     */
    private static Method getMethod(final String name, final Class[] arguments) {
        try {
            return Class.forName("org.geotools.resources.BoundingBoxes").getMethod(name, arguments);
        } catch (ClassNotFoundException exception) {
            // Simplify when we will be allowed to compile for J2SE 1.5.
            UnsupportedOperationException e = new UnsupportedOperationException(Errors.format(
                    ErrorKeys.MISSING_MODULE_$1, "referencing"));
            e.initCause(exception);
            throw e;
        } catch (NoSuchMethodException exception) {
            // Should never happen if we didn't broke our BoundingBoxes helper class.
            throw new AssertionError(exception);
        }
    }

    /**
     * Invokes the specified method with the specified arguments.
     */
    private static Object invoke(final Method method, final Object[] arguments)
            throws InvocationTargetException
    {
        try {
            return method.invoke(null, arguments);
        } catch (IllegalAccessException exception) {
            // Should never happen if our BoundingBoxes helper class is not broken.
            throw new AssertionError(exception);
        } catch (InvocationTargetException exception) {
            final Throwable cause = exception.getTargetException();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            if (cause instanceof Error) {
                throw (Error) cause;
            }
            throw exception;
        }
    }
}
