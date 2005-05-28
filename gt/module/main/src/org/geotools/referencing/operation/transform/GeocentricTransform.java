/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Managment Committee (PMC)
 * (C) 2001, Institut de Recherche pour le Développement
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *
 *    This package contains documentation from OpenGIS specifications.
 *    OpenGIS consortium's work is fully acknowledged here.
 */
package org.geotools.referencing.operation.transform;

// J2SE dependencies and extensions
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Collections;
import javax.units.Converter;
import javax.units.SI;
import javax.units.Unit;

// OpenGIS dependencies
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterNotFoundException;
import org.opengis.parameter.ParameterValue;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.datum.Ellipsoid;
import org.opengis.referencing.operation.Conversion;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.OperationMethod;

// Geotools dependencies
import org.geotools.metadata.iso.citation.CitationImpl;
import org.geotools.parameter.ParameterReal;
import org.geotools.referencing.NamedIdentifier;
import org.geotools.referencing.operation.MathTransformProvider;
import org.geotools.resources.cts.ResourceKeys;
import org.geotools.resources.cts.Resources;


/**
 * Transforms three dimensional {@linkplain org.geotools.referencing.crs.DefaultGeographicCRS
 * geographic} points to {@linkplain org.geotools.referencing.crs.DefaultGeocentricCRS geocentric}
 * coordinate points. Input points must be longitudes, latitudes and heights above the ellipsoid.
 *
 * @version $Id$
 * @author Frank Warmerdam
 * @author Martin Desruisseaux
 */
public class GeocentricTransform extends AbstractMathTransform implements Serializable {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -3352045463953828140L;

    /**
     * Maximal error tolerance in metres during assertions, in metres. If assertions
     * are enabled (JDK 1.4 only), then every coordinates transformed with
     * {@link #inverseTransform} will be transformed again with {@link #mathTransform}.
     * If the distance between the resulting position and the original position
     * is greater than <code>MAX_ERROR</code>, then a {@link AssertionError} is thrown.
     */
    private static final double MAX_ERROR = 0.01;
    
    /**
     * Cosine of 67.5 degrees.
     */
    private static final double COS_67P5 = 0.38268343236508977;
    
    /**
     * Toms region 1 constant.
     */
    private static final double AD_C = 1.0026000;
    
    /**
     * Semi-major axis of ellipsoid in meters.
     */
    private final double a;
    
    /**
     * Semi-minor axis of ellipsoid in meters.
     */
    private final double b;
    
    /**
     * Square of semi-major axis (<var>a</var>˛).
     */
    private final double a2;
    
    /**
     * Square of semi-minor axis (<var>b</var>˛).
     */
    private final double b2;
    
    /**
     * Eccentricity squared.
     */
    private final double e2;
    
    /**
     * 2nd eccentricity squared.
     */
    private final double ep2;
    
    /**
     * <code>true</code> if geographic coordinates include an ellipsoidal
     * height (i.e. are 3-D), or <code>false</code> if they are strictly 2-D.
     */
    private final boolean hasHeight;
    
    /**
     * The inverse of this transform. Will be created only when needed.
     */
    private transient MathTransform inverse;
    
    /**
     * Constructs a transform from the specified ellipsoid.
     *
     * @param ellipsoid The ellipsoid.
     * @param hasHeight <code>true</code> if geographic coordinates
     *                  include an ellipsoidal height (i.e. are 3-D),
     *                  or <code>false</code> if they are only 2-D.
     */
    public GeocentricTransform(final Ellipsoid ellipsoid, final boolean hasHeight) {
        this(ellipsoid.getSemiMajorAxis(),
             ellipsoid.getSemiMinorAxis(),
             ellipsoid.getAxisUnit(), hasHeight);
    }
    
    /**
     * Constructs a transform from the specified parameters.
     *
     * @param semiMajor The semi-major axis length.
     * @param semiMinor The semi-minor axis length.
     * @param units     The axis units.
     * @param hasHeight <code>true</code> if geographic coordinates
     *                  include an ellipsoidal height (i.e. are 3-D),
     *                  or <code>false</code> if they are only 2-D.
     */
    public GeocentricTransform(final double  semiMajor,
                               final double  semiMinor,
                               final Unit    units,
                               final boolean hasHeight)
    {
        this.hasHeight = hasHeight;
        final Converter converter = units.getConverterTo(SI.METER);
        a   = converter.convert(semiMajor);
        b   = converter.convert(semiMinor);
        a2  = a*a;
        b2  = b*b;
        e2  = (a2 - b2) / a2;
        ep2 = (a2 - b2) / b2;
        checkArgument("a", a, Double.MAX_VALUE);
        checkArgument("b", b, a);
    }
    
    /**
     * Check an argument value. The argument must be greater
     * than 0 and finite, otherwise an exception is thrown.
     *
     * @param name  The argument name.
     * @param value The argument value.
     * @param max   The maximal legal argument value.
     */
    private static void checkArgument(final String name,
                                      final double value,
                                      final double max) throws IllegalArgumentException
    {
        if (!(value>=0 && value<=max)) {
            // Use '!' in order to trap NaN
            throw new IllegalArgumentException(Resources.format(
                    ResourceKeys.ERROR_ILLEGAL_ARGUMENT_$2, name, new Double(value)));
        }
    }

    /**
     * Returns the parameter descriptors for this math transform.
     */
    public ParameterDescriptorGroup getParameterDescriptors() {
        return Provider.PARAMETERS;
    }

    /**
     * Returns the parameter values for this math transform.
     *
     * @return A copy of the parameter values for this math transform.
     */
    public ParameterValueGroup getParameterValues() {
        return getParameterValues(getParameterDescriptors());
    }

    /**
     * Returns the parameter values using the specified descriptor.
     *
     * @param  The parameter descriptor.
     * @return A copy of the parameter values for this math transform.
     */
    private ParameterValueGroup getParameterValues(final ParameterDescriptorGroup descriptor) {
        final ParameterValue[] parameters = new ParameterValue[hasHeight ? 2 : 3];
        int index = 0;
        if (!hasHeight) {
            final ParameterValue p = new org.geotools.parameter.Parameter(Provider.DIM);
            p.setValue(2);
            parameters[index++] = p;
        }
        parameters[index++] = new ParameterReal(Provider.SEMI_MAJOR, a);
        parameters[index++] = new ParameterReal(Provider.SEMI_MINOR, b);
        return new org.geotools.parameter.ParameterGroup(descriptor, parameters);
    }
    
    /**
     * Gets the dimension of input points, which is 2 or 3.
     */
    public int getSourceDimensions() {
        return hasHeight ? 3 : 2;
    }
    
    /**
     * Gets the dimension of output points, which is 3.
     */
    public final int getTargetDimensions() {
        return 3;
    }
    
    /**
     * Converts geodetic coordinates (longitude, latitude, height) to geocentric
     * coordinates (x, y, z) according to the current ellipsoid parameters.
     */
    public void transform(double[] srcPts, int srcOff, double[] dstPts, int dstOff, int numPts) {
        transform(srcPts, srcOff, dstPts, dstOff, numPts, false);
    }
    
    /**
     * Implementation of geodetic to geocentric conversion. This implementation allows the caller
     * to use height in computation. This is used for assertion with {@link #checkTransform}.
     */
    private void transform(double[] srcPts, int srcOff,
                     final double[] dstPts, int dstOff,
                     int numPts, boolean hasHeight)
    {
        final int dimSource = getSourceDimensions();
        hasHeight |= (dimSource>=3);
        if (srcPts==dstPts && needCopy(srcOff, dimSource, dstOff, 3, numPts)) {
            // Source and destination arrays overlaps: copy in a temporary buffer.
            final double[] old = srcPts;
            srcPts = new double[numPts * (hasHeight ? 3 : 2)];
            System.arraycopy(old, srcOff, srcPts, 0, srcPts.length);
            srcOff = 0;
        }
        while (--numPts >= 0) {
            final double L = Math.toRadians(srcPts[srcOff++]); // Longitude
            final double P = Math.toRadians(srcPts[srcOff++]); // Latitude
            final double h = hasHeight ? srcPts[srcOff++] : 0; // Height above the ellipsoid (m)
            
            final double cosLat = Math.cos(P);
            final double sinLat = Math.sin(P);
            final double rn     = a / Math.sqrt(1 - e2 * (sinLat*sinLat));
            
            dstPts[dstOff++] = (rn + h) * cosLat * Math.cos(L); // X: Toward prime meridian
            dstPts[dstOff++] = (rn + h) * cosLat * Math.sin(L); // Y: Toward East
            dstPts[dstOff++] = (rn * (1-e2) + h) * sinLat;      // Z: Toward North
        }
    }
    
    /**
     * Converts geodetic coordinates (longitude, latitude, height) to geocentric
     * coordinates (x, y, z) according to the current ellipsoid parameters.
     */
    public void transform(float[] srcPts, int srcOff,
                    final float[] dstPts, int dstOff, int numPts)
    {
        final int dimSource = getSourceDimensions();
        final boolean hasHeight = (dimSource>=3);
        if (srcPts==dstPts && needCopy(srcOff, dimSource, dstOff, 3, numPts)) {
            // Source and destination arrays overlaps: copy in a temporary buffer.
            final float[] old = srcPts;
            srcPts = new float[numPts*dimSource];
            System.arraycopy(old, srcOff, srcPts, 0, srcPts.length);
            srcOff = 0;
        }
        while (--numPts >= 0) {
            final double L = Math.toRadians(srcPts[srcOff++]); // Longitude
            final double P = Math.toRadians(srcPts[srcOff++]); // Latitude
            final double h = hasHeight ? srcPts[srcOff++] : 0; // Height above the ellipsoid (m)
            
            final double cosLat = Math.cos(P);
            final double sinLat = Math.sin(P);
            final double rn     = a / Math.sqrt(1 - e2 * (sinLat*sinLat));
            
            dstPts[dstOff++] = (float) ((rn + h) * cosLat * Math.cos(L)); // X: Toward prime meridian
            dstPts[dstOff++] = (float) ((rn + h) * cosLat * Math.sin(L)); // Y: Toward East
            dstPts[dstOff++] = (float) ((rn * (1-e2) + h) * sinLat);      // Z: Toward North
        }
    }
    
    /**
     * Converts geocentric coordinates (x, y, z) to geodetic coordinates
     * (longitude, latitude, height), according to the current ellipsoid
     * parameters. The method used here is derived from "An Improved
     * Algorithm for Geocentric to Geodetic Coordinate Conversion", by
     * Ralph Toms, Feb 1996.
     */
    public void inverseTransform(double[] srcPts, int srcOff,
                           final double[] dstPts, int dstOff, final int numPts)
    {
        final int dimTarget = getSourceDimensions();
        if (srcPts==dstPts && needCopy(srcOff, 3, dstOff, dimTarget, numPts)) {
            // Source and destination arrays overlaps: copy in a temporary buffer.
            final double[] old = srcPts;
            srcPts = new double[numPts*3];
            System.arraycopy(old, srcOff, srcPts, 0, srcPts.length);
            srcOff = 0;
        }
        inverseTransform(null, srcPts, srcOff, null, dstPts, dstOff, numPts, dimTarget);
    }
    
    /**
     * Converts geocentric coordinates (x, y, z) to geodetic coordinates
     * (longitude, latitude, height), according to the current ellipsoid
     * parameters. The method used here is derived from "An Improved
     * Algorithm for Geocentric to Geodetic Coordinate Conversion", by
     * Ralph Toms, Feb 1996.
     */
    public void inverseTransform(float[] srcPts, int srcOff,
                           final float[] dstPts, int dstOff, final int numPts)
    {
        final int dimTarget = getSourceDimensions();
        if (srcPts==dstPts && needCopy(srcOff, 3, dstOff, dimTarget, numPts)) {
            // Source and destination arrays overlaps: copy in a temporary buffer.
            final float[] old = srcPts;
            srcPts = new float[numPts*3];
            System.arraycopy(old, srcOff, srcPts, 0, srcPts.length);
            srcOff = 0;
        }
        inverseTransform(srcPts, null, srcOff, dstPts, null, dstOff, numPts, dimTarget);
    }

    /**
     * Implementation of the inverse transformation.
     */
    private void inverseTransform(final float[] srcPts1, final double[] srcPts2, int srcOff,
                                  final float[] dstPts1, final double[] dstPts2, int dstOff,
                                  int numPts, final int dimTarget)
    {
        final boolean hasHeight = (dimTarget>=3);
        boolean   computeHeight = hasHeight;
        assert (computeHeight=true)==true; // Force computeHeight to true if assertions are enabled.
        while (--numPts >= 0) {
            final double x, y, z;
            if (srcPts2 != null) {
                x = srcPts2[srcOff++]; // Toward prime meridian
                y = srcPts2[srcOff++]; // Toward East
                z = srcPts2[srcOff++]; // Toward North
            } else {
                x = srcPts1[srcOff++]; // Toward prime meridian
                y = srcPts1[srcOff++]; // Toward East
                z = srcPts1[srcOff++]; // Toward North
            }
            // Note: The Java version of 'atan2' work correctly for x==0.
            //       No need for special handling like in the C version.
            //       No special handling neither for latitude. Formulas
            //       below are generic enough, considering that 'atan'
            //       work correctly with infinities (1/0).
            
            // Note: Variable names follow the notation used in Toms, Feb 1996
            final double      W2 = x*x + y*y;                       // square of distance from Z axis
            final double      W  = Math.sqrt(W2);                   // distance from Z axis
            final double      T0 = z * AD_C;                        // initial estimate of vertical component
            final double      S0 = Math.sqrt(T0*T0 + W2);           // initial estimate of horizontal component
            final double  sin_B0 = T0 / S0;                         // sin(B0), B0 is estimate of Bowring aux variable
            final double  cos_B0 = W / S0;                          // cos(B0)
            final double sin3_B0 = sin_B0 * sin_B0 * sin_B0;        // cube of sin(B0)
            final double      T1 = z + b * ep2 * sin3_B0;           // corrected estimate of vertical component
            final double     sum = W - a*e2*(cos_B0*cos_B0*cos_B0); // numerator of cos(phi1)
            final double      S1 = Math.sqrt(T1*T1 + sum * sum);    // corrected estimate of horizontal component
            final double  sin_p1 = T1 / S1;                         // sin(phi1), phi1 is estimated latitude
            final double  cos_p1 = sum / S1;                        // cos(phi1)
            
            final double longitude = Math.toDegrees(Math.atan2(y      , x     ));
            final double  latitude = Math.toDegrees(Math.atan(sin_p1 / cos_p1));
            final double    height;
            
            if (dstPts2 != null) {
                dstPts2[dstOff++] = longitude;
                dstPts2[dstOff++] = latitude;
            } else {
                dstPts1[dstOff++] = (float) longitude;
                dstPts1[dstOff++] = (float) latitude;
            }
            if (computeHeight) {
                final double rn = a/Math.sqrt(1-e2*(sin_p1*sin_p1)); // Earth radius at location
                if      (cos_p1 >= +COS_67P5) height = W / +cos_p1 - rn;
                else if (cos_p1 <= -COS_67P5) height = W / -cos_p1 - rn;
                else                          height = z / sin_p1 + rn*(e2 - 1.0);
                if (hasHeight) {
                    if (dstPts2 != null) {
                        dstPts2[dstOff++] = height;
                    } else {
                        dstPts1[dstOff++] = (float) height;
                    }
                }
                // If assertion are enabled, then transform the
                // result and compare it with the input array.
                double distance;
                assert MAX_ERROR > (distance=checkTransform(new double[]
                        {x,y,z, longitude, latitude, height})) : distance;
            }
        }
    }
    
    /**
     * Transform the last half if the specified array and returns
     * the distance with the first half. Array <code>points</code>
     * must have a length of 6.
     */
    private double checkTransform(final double[] points) {
        transform(points, 3, points, 3, 1, true);
        final double dx = points[0]-points[3];
        final double dy = points[1]-points[4];
        final double dz = points[2]-points[5];
        return Math.sqrt(dx*dx + dy*dy + dz*dz);
    }
    
    /**
     * Returns the inverse of this transform.
     */
    public MathTransform inverse() {
        if (inverse == null) {
            // No need to synchronize; this is not a big deal if this object is created twice.
            inverse = new Inverse();
        }
        return inverse;
    }
    
    /**
     * Returns a hash value for this transform.
     */
    public int hashCode() {
        final long code = Double.doubleToLongBits( a ) +
                          37*(Double.doubleToLongBits( b ) +
                          37*(Double.doubleToLongBits( a2) +
                          37*(Double.doubleToLongBits( b2) +
                          37*(Double.doubleToLongBits( e2) +
                          37*(Double.doubleToLongBits(ep2))))));
        return (int) code ^ (int) (code >>> 32) ^ (int)serialVersionUID;
    }
    
    /**
     * Compares the specified object with this math transform for equality.
     */
    public boolean equals(final Object object) {
        if (object == this) {
            // Slight optimization
            return true;
        }
        if (super.equals(object)) {
            final GeocentricTransform that = (GeocentricTransform) object;
            return Double.doubleToLongBits(this. a ) == Double.doubleToLongBits(that. a ) &&
                   Double.doubleToLongBits(this. b ) == Double.doubleToLongBits(that. b ) &&
                   Double.doubleToLongBits(this. a2) == Double.doubleToLongBits(that. a2) &&
                   Double.doubleToLongBits(this. b2) == Double.doubleToLongBits(that. b2) &&
                   Double.doubleToLongBits(this. e2) == Double.doubleToLongBits(that. e2) &&
                   Double.doubleToLongBits(this.ep2) == Double.doubleToLongBits(that.ep2) &&
                   this.hasHeight == that.hasHeight;
        }
        return false;
    }
    
    /**
     * Inverse of a geocentric transform.
     *
     * @version $Id$
     * @author Martin Desruisseaux
     */
    private final class Inverse extends AbstractMathTransform.Inverse implements Serializable {
        /**
         * Serial number for interoperability with different versions.
         */
        private static final long serialVersionUID = 6942084702259211803L;

        /**
         * Default constructor.
         */
        public Inverse() {
            GeocentricTransform.this.super();
        }
    
        /**
         * Returns the parameter descriptors for this math transform.
         */
        public ParameterDescriptorGroup getParameterDescriptors() {
            return ProviderInverse.PARAMETERS;
        }

        /**
         * Returns the parameter values for this math transform.
         *
         * @return A copy of the parameter values for this math transform.
         */
        public ParameterValueGroup getParameterValues() {
            return GeocentricTransform.this.getParameterValues(getParameterDescriptors());
        }

        /**
         * Inverse transform an array of points.
         */
        public void transform(final double[] source, final int srcOffset,
                              final double[] dest,   final int dstOffset, final int length)
        {
            GeocentricTransform.this.inverseTransform(source, srcOffset, dest, dstOffset, length);
        }
        
        /**
         * Inverse transform an array of points.
         */
        public void transform(final float[] source, final int srcOffset,
                              final float[] dest,   final int dstOffset, final int length)
        {
            GeocentricTransform.this.inverseTransform(source, srcOffset, dest, dstOffset, length);
        }

        /**
         * Restore reference to this object after deserialization.
         */
        private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
            in.defaultReadObject();
            GeocentricTransform.this.inverse = this;
        }
    }
    
    /**
     * The provider for {@link GeocentricTransform}. This provider will construct transforms
     * from {@linkplain org.geotools.referencing.crs.DefaultGeographicCRS geographic} to
     * {@linkplain org.geotools.referencing.crs.DefaultGeocentricCRS geocentric} coordinate
     * reference systems.
     *
     * @version $Id$
     * @author Martin Desruisseaux
     */
    public static class Provider extends MathTransformProvider {
        /**
         * Serial number for interoperability with different versions.
         */
        private static final long serialVersionUID = 7043216580786030251L;

        /**
         * The operation parameter descriptor for the "semi_major" parameter value.
         * Valid values range from 0 to infinity.
         */
        public static final ParameterDescriptor SEMI_MAJOR = createDescriptor(
                new NamedIdentifier[] {
                    new NamedIdentifier(CitationImpl.OGC,  "semi_major"),
                    new NamedIdentifier(CitationImpl.EPSG, "semi-major axis")   //epsg does not specifically define this parameter
                },
                Double.NaN, 0, Double.POSITIVE_INFINITY, SI.METER);

        /**
         * The operation parameter descriptor for the "semi_minor" parameter value.
         * Valid values range from 0 to infinity.
         */
        public static final ParameterDescriptor SEMI_MINOR = createDescriptor(
                new NamedIdentifier[] {
                    new NamedIdentifier(CitationImpl.OGC,  "semi_minor"),
                    new NamedIdentifier(CitationImpl.EPSG, "semi-minor axis")   //epsg does not specifically define this parameter
                },
                Double.NaN, 0, Double.POSITIVE_INFINITY, SI.METER);

        /**
         * The number of geographic dimension (2 or 3). This is a Geotools-specif argument.
         * The default value is 3, which is the value implied in OGC's WKT.
         */
        private static final ParameterDescriptor DIM =
                new org.geotools.parameter.ParameterDescriptor(
                    Collections.singletonMap(NAME_PROPERTY,
                                             new NamedIdentifier(CitationImpl.GEOTOOLS, "dim")),
                    3, 2, 3, false);

        /**
         * The parameters group.
         */
        static final ParameterDescriptorGroup PARAMETERS = createDescriptorGroup(
                        "Ellipsoid_To_Geocentric",
                        "Geographic/geocentric conversions", 
                        "9602",
                        ResourceKeys.GEOCENTRIC_TRANSFORM);

        /**
         * Constructs the parameters group.
         */
        static ParameterDescriptorGroup createDescriptorGroup(final String ogc,
                                                              final String epsgName,
                                                              final String epsgCode,
                                                              final int geotools)
        {
            return createDescriptorGroup(new NamedIdentifier[] {
                    new NamedIdentifier(CitationImpl.OGC,      ogc),
                    new NamedIdentifier(CitationImpl.EPSG,     epsgName),
                    new NamedIdentifier(CitationImpl.EPSG,     epsgCode),
                    new NamedIdentifier(CitationImpl.GEOTOOLS, Resources.formatInternational(geotools))
                }, new ParameterDescriptor[] {
                    SEMI_MAJOR, SEMI_MINOR, DIM
                });
        }

        /**
         * The provider for the 2D case. Will be constructed by {@link #getMethod}
         * when first needed.
         */
        transient Provider noHeight;

        /**
         * Constructs a provider with default parameters.
         */
        public Provider() {
            super(3, 3, PARAMETERS);
        }
        
        /**
         * Constructs a provider from a set of parameters.
         *
         * @param sourceDimensions Number of dimensions in the source CRS of this operation method.
         * @param targetDimensions Number of dimensions in the target CRS of this operation method.
         * @param parameters The set of parameters (never <code>null</code>).
         */
        Provider(final int sourceDimensions,
                 final int targetDimensions,
                 final ParameterDescriptorGroup parameters)
        {
            super(sourceDimensions, targetDimensions, parameters);
        }

        /**
         * Returns the operation type.
         */
        protected Class getOperationType() {
            return Conversion.class;
        }
        
        /**
         * Creates a transform from the specified group of parameter values.
         *
         * @param  values The group of parameter values.
         * @return The created math transform.
         * @throws ParameterNotFoundException if a required parameter was not found.
         */
        public MathTransform createMathTransform(final ParameterValueGroup values)
                throws ParameterNotFoundException
        {
            final int dimGeographic =    intValue(DIM,        values);
            final double  semiMajor = doubleValue(SEMI_MAJOR, values);
            final double  semiMinor = doubleValue(SEMI_MINOR, values);
            return new GeocentricTransform(semiMajor, semiMinor, SI.METER, dimGeographic!=2);
        }

        /**
         * Returns the operation method for the specified math transform. This method is invoked
         * automatically after <code>createMathTransform</code>. The default implementation returns
         * an operation with source dimensions that matches the math transform source dimensions.
         */
        protected OperationMethod getMethod(final MathTransform mt) {
            switch (mt.getSourceDimensions()) {
                case 2: {
                    if (noHeight == null) {
                        noHeight = new Provider(2, 3, PARAMETERS);
                    }
                    return noHeight;
                }
                case 3: return this;
                default: throw new IllegalArgumentException();
            }
        }
    }
    
    /**
     * The provider for inverse of {@link GeocentricTransform}. This provider will construct
     * transforms from {@linkplain org.geotools.referencing.crs.DefaultGeocentricCRS geocentric}
     * to {@linkplain org.geotools.referencing.crs.DefaultGeographicCRS geographic} coordinate
     * reference systems.
     *
     * @version $Id$
     * @author Martin Desruisseaux
     */
    public static class ProviderInverse extends Provider {
        /**
         * Serial number for interoperability with different versions.
         */
        private static final long serialVersionUID = -7356791540110076789L;

        /**
         * The parameters group.
         *
         * @todo The EPSG code seems to be the same than for the direct transform.
         */
        static final ParameterDescriptorGroup PARAMETERS = createDescriptorGroup(
                        "Geocentric_To_Ellipsoid",
                        "Geographic/geocentric conversions", 
                        "9602",
                        ResourceKeys.GEOCENTRIC_TRANSFORM);

        /**
         * Create a provider.
         */
        public ProviderInverse() {
            super(3, 3, PARAMETERS);
        }
        
        /**
         * Constructs a provider from a set of parameters.
         *
         * @param sourceDimensions Number of dimensions in the source CRS of this operation method.
         * @param targetDimensions Number of dimensions in the target CRS of this operation method.
         * @param parameters The set of parameters (never <code>null</code>).
         */
        ProviderInverse(final int sourceDimensions,
                        final int targetDimensions,
                        final ParameterDescriptorGroup parameters)
        {
            super(sourceDimensions, targetDimensions, parameters);
        }
        
        /**
         * Creates a transform from the specified group of parameter values.
         *
         * @param  values The group of parameter values.
         * @return The created math transform.
         * @throws ParameterNotFoundException if a required parameter was not found.
         */
        public MathTransform createMathTransform(final ParameterValueGroup values)
                throws ParameterNotFoundException
        {
            return ((GeocentricTransform) super.createMathTransform(values)).inverse();
        }

        /**
         * Returns the operation method for the specified math transform. This method is invoked
         * automatically after <code>createMathTransform</code>. The default implementation returns
         * an operation with target dimensions that matches the math transform target dimensions.
         */
        protected OperationMethod getMethod(final MathTransform mt) {
            switch (mt.getTargetDimensions()) {
                case 2: {
                    if (noHeight == null) {
                        noHeight = new ProviderInverse(3, 2, PARAMETERS);
                    }
                    return noHeight;
                }
                case 3: return this;
                default: throw new IllegalArgumentException();
            }
        }
    }
}
