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

// J2SE dependencies
import java.io.IOException;
import java.io.Serializable;
import java.io.ObjectInputStream;
import javax.units.Converter;
import javax.units.Unit;
import javax.units.SI;

// OpenGIS dependencies
import org.opengis.referencing.datum.Ellipsoid;
import org.opengis.parameter.ParameterValue;
import org.opengis.parameter.OperationParameter;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.parameter.OperationParameterGroup;
import org.opengis.parameter.ParameterNotFoundException;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

// Geotools dependencies
import org.geotools.metadata.citation.Citation;
import org.geotools.referencing.Identifier;
import org.geotools.referencing.operation.MathTransformProvider;
import org.geotools.parameter.ParameterRealValue;
import org.geotools.resources.cts.Resources;
import org.geotools.resources.cts.ResourceKeys;


/**
 * Transforms three dimensional {@linkplain org.geotools.referencing.crs.GeographicCRS geographic}
 * points to {@linkplain org.geotools.referencing.crs.GeocentricCRS geocentric} coordinate points.
 * Input points must be longitudes, latitudes and heights above the ellipsoid.
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
     * {@link #inverseTransform} will be transformed again with {@link #transform}.
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
     * Square of semi-major axis (<var>a</var>²).
     */
    private final double a2;
    
    /**
     * Square of semi-minor axis (<var>b</var>²).
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
     * Construct a transform from the specified ellipsoid.
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
     * Construct a transform from the specified parameters.
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
     * The operation parameters. To be overrides by {@link Inverse} only.
     */
    OperationParameterGroup getParameters() {
        return Provider.PARAMETERS;
    }

    /**
     * Returns the parameters for this math transform.
     *
     * @return The parameters for this math transform.
     */
    public ParameterValueGroup getParameterValues() {
        return new org.geotools.parameter.ParameterValueGroup(getParameters(),
               new ParameterValue[] {
                   new ParameterRealValue(Provider.SEMI_MAJOR, a),
                   new ParameterRealValue(Provider.SEMI_MINOR, b)
               });
    }
    
    /**
     * Gets the dimension of input points, which is 2 or 3.
     */
    public int getDimSource() {
        return hasHeight ? 3 : 2;
    }
    
    /**
     * Gets the dimension of output points, which is 3.
     */
    public final int getDimTarget() {
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
     * Implementation of geodetic to geocentric conversion. This
     * implementation allows the caller to use height in computation.
     */
    private void transform(final double[] srcPts, int srcOff,
                           final double[] dstPts, int dstOff,
                           int numPts, boolean hasHeight)
    {
        int step = 0;
        final int dimSource = getDimSource();
        hasHeight |= (dimSource>=3);
        if (srcPts==dstPts && srcOff<dstOff && srcOff+numPts*dimSource>dstOff) {
            step = -dimSource;
            srcOff -= (numPts-1)*step;
            dstOff -= (numPts-1)*step;
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
            srcOff += step;
            dstOff += step;
        }
    }
    
    /**
     * Converts geodetic coordinates (longitude, latitude, height) to geocentric
     * coordinates (x, y, z) according to the current ellipsoid parameters.
     */
    public void transform(final float[] srcPts, int srcOff,
                          final float[] dstPts, int dstOff, int numPts)
    {
        int step = 0;
        final int dimSource = getDimSource();
        final boolean hasHeight = (dimSource>=3);
        if (srcPts==dstPts && srcOff<dstOff && srcOff+numPts*dimSource>dstOff) {
            step = -dimSource;
            srcOff -= (numPts-1)*step;
            dstOff -= (numPts-1)*step;
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
            srcOff += step;
            dstOff += step;
        }
    }
    
    /**
     * Converts geocentric coordinates (x, y, z) to geodetic coordinates
     * (longitude, latitude, height), according to the current ellipsoid
     * parameters. The method used here is derived from "An Improved
     * Algorithm for Geocentric to Geodetic Coordinate Conversion", by
     * Ralph Toms, Feb 1996.
     */
    public void inverseTransform(final double[] srcPts, int srcOff,
                                 final double[] dstPts, int dstOff, int numPts)
    {
        int step = 0;
        final int dimSource = getDimSource();
        final boolean hasHeight = (dimSource>=3);
        boolean   computeHeight = hasHeight;
        assert computeHeight=true; // Intentional side effect
        if (srcPts==dstPts && srcOff<dstOff && srcOff+numPts*dimSource>dstOff) {
            step    = -dimSource;
            srcOff -= (numPts-1)*step;
            dstOff -= (numPts-1)*step;
        }
        while (--numPts >= 0) {
            final double x = srcPts[srcOff++]; // Toward prime meridian
            final double y = srcPts[srcOff++]; // Toward East
            final double z = srcPts[srcOff++]; // Toward North
            
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
            
            dstPts[dstOff++] = longitude;
            dstPts[dstOff++] = latitude;
            if (computeHeight) {
                final double rn = a/Math.sqrt(1-e2*(sin_p1*sin_p1)); // Earth radius at location
                if      (cos_p1 >= +COS_67P5) height = W / +cos_p1 - rn;
                else if (cos_p1 <= -COS_67P5) height = W / -cos_p1 - rn;
                else                          height = z / sin_p1 + rn*(e2 - 1.0);
                if (hasHeight) {
                    dstPts[dstOff++] = height;
                }
                // If assertion are enabled, then transform the
                // result and compare it with the input array.
                double distance;
                assert MAX_ERROR > (distance=checkTransform(new double[]
                        {x,y,z, longitude, latitude, height})) : distance;
            }
            srcOff += step;
            dstOff += step;
        }
    }
    
    /**
     * Converts geocentric coordinates (x, y, z) to geodetic coordinates
     * (longitude, latitude, height), according to the current ellipsoid
     * parameters. The method used here is derived from "An Improved
     * Algorithm for Geocentric to Geodetic Coordinate Conversion", by
     * Ralph Toms, Feb 1996.
     */
    public void inverseTransform(final float[] srcPts, int srcOff,
                                 final float[] dstPts, int dstOff, int numPts)
    {
        int step = 0;
        final int dimSource = getDimSource();
        final boolean hasHeight = (dimSource>=3);
        boolean   computeHeight = hasHeight;
        assert computeHeight=true; // Intentional side effect
        if (srcPts==dstPts && srcOff<dstOff && srcOff+numPts*dimSource>dstOff) {
            step    = -dimSource;
            srcOff -= (numPts-1)*step;
            dstOff -= (numPts-1)*step;
        }
        while (--numPts >= 0) {
            final double x = srcPts[srcOff++]; // Toward prime meridian
            final double y = srcPts[srcOff++]; // Toward East
            final double z = srcPts[srcOff++]; // Toward North
            
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
            
            dstPts[dstOff++] = (float) longitude;
            dstPts[dstOff++] = (float) latitude;
            if (computeHeight) {
                final double rn = a/Math.sqrt(1-e2*(sin_p1*sin_p1)); // Earth radius at location
                if      (cos_p1 >= +COS_67P5) height = W / +cos_p1 - rn;
                else if (cos_p1 <= -COS_67P5) height = W / -cos_p1 - rn;
                else                          height = z / sin_p1 + rn*(e2 - 1.0);
                if (hasHeight) {
                    dstPts[dstOff++] = (float) height;
                }
                // If assertion are enabled, then transform the
                // result and compare it with the input array.
                double distance;
                assert MAX_ERROR > (distance=checkTransform(new double[]
                        {x,y,z, longitude, latitude, height})) : distance;
            }
            srcOff += step;
            dstOff += step;
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
        return (int) code ^ (int) (code >>> 32);
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
         * The operation parameters.
         */
        OperationParameterGroup getParameters() {
            return ProviderInverse.PARAMETERS;
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
     * from {@linkplain org.geotools.referencing.crs.GeographicCRS geographic} to
     * {@linkplain org.geotools.referencing.crs.GeocentricCRS geocentric} coordinate reference
     * systems.
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
        public static final OperationParameter SEMI_MAJOR = new org.geotools.parameter.OperationParameter(
                "semi_major", Double.NaN, 0, Double.POSITIVE_INFINITY, SI.METER);

        /**
         * The operation parameter descriptor for the "semi_minor" parameter value.
         * Valid values range from 0 to infinity.
         */
        public static final OperationParameter SEMI_MINOR = new org.geotools.parameter.OperationParameter(
                "semi_minor", Double.NaN, 0, Double.POSITIVE_INFINITY, SI.METER);

        /**
         * The number of geographic dimension (2 or 3). This is a Geotools-specif argument.
         * The default value is 3.
         */
        private static final OperationParameter DIM_GEOCS = new org.geotools.parameter.OperationParameter(
                "dim_geoCS", 3, 2, 3);

        /**
         * The parameters group.
         */
        static final OperationParameterGroup PARAMETERS = group("Ellipsoid_To_Geocentric", "9602");

        /**
         * Construct the parameters group.
         */
        static OperationParameterGroup group(final String ogc, final String epsg) {
            return group(
                     new Identifier[] {
                        new Identifier(Citation.OPEN_GIS, null,  ogc),
                        new Identifier(Citation.EPSG,    "EPSG", epsg)
                     }, new OperationParameter[] {
                        SEMI_MAJOR, SEMI_MINOR, DIM_GEOCS
                     });
        }

        /**
         * Constructs a provider.
         */
        public Provider() {
            this(PARAMETERS);
        }

        /**
         * Constructs a provider.
         */
        public Provider(final OperationParameterGroup parameters) {
            super(3, 3, parameters);
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
            final double  semiMajor = values.getValue("semi_major").doubleValue();
            final double  semiMinor = values.getValue("semi_minor").doubleValue();
            final int dimGeographic = values.getValue("dim_geoCS" ).intValue();
            return new GeocentricTransform(semiMajor, semiMinor, SI.METER, dimGeographic!=2);
        }

        /**
         * Returns the resources key for {@linkplain #getName localized name}.
         * This method is for internal purpose by Geotools implementation only.
         */
        protected int getLocalizationKey() {
            return ResourceKeys.GEOCENTRIC_TRANSFORM;
        }
    }
    
    /**
     * The provider for inverse of {@link GeocentricTransform}. This provider will construct
     * transforms from {@linkplain org.geotools.referencing.crs.GeocentricCRS geocentric} to
     * {@linkplain org.geotools.referencing.crs.GeographicCRS geographic} coordinate reference
     * systems.
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
         * NOTE: The EPSG code seems to be the same than for the direct transform.
         */
        static final OperationParameterGroup PARAMETERS = group("Geocentric_To_Ellipsoid", "9602");

        /**
         * Create a provider.
         */
        public ProviderInverse() {
            super(PARAMETERS);
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
    }
}
