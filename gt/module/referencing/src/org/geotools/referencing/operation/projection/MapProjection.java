/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Managment Committee (PMC)
 * (C) 2001, Institut de Recherche pour le Développement
 * (C) 1999, Fisheries and Oceans Canada
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
 *    This package contains formulas from the PROJ package of USGS.
 *    USGS's work is fully acknowledged here.
 */
package org.geotools.referencing.operation.projection;

// J2SE dependencies and extensions
import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.Collection;
import javax.units.NonSI;
import javax.units.SI;
import javax.units.Unit;

// OpenGIS dependencies
import org.opengis.parameter.InvalidParameterValueException;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterNotFoundException;
import org.opengis.parameter.ParameterValue;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.MathTransform2D;
import org.opengis.referencing.operation.Projection;
import org.opengis.referencing.operation.TransformException;

// Geotools dependencies
import org.geotools.measure.Latitude;
import org.geotools.measure.Longitude;
import org.geotools.metadata.iso.citation.Citations;
import org.geotools.referencing.NamedIdentifier;
import org.geotools.referencing.operation.MathTransformProvider;
import org.geotools.referencing.operation.transform.AbstractMathTransform;
import org.geotools.resources.XMath;
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.i18n.ErrorKeys;


/**
 * Base class for transformation services between ellipsoidal and cartographic projections.
 * This base class provides the basic feature needed for all methods (no need to overrides
 * methods). Subclasses must "only" implements the following methods:
 * <ul>
 *   <li>{@link #getParameterValues}</li>
 *   <li>{@link #transformNormalized}</li>
 *   <li>{@link #inverseTransformNormalized}</li>
 * </ul>
 * <p>
 * <strong>NOTE:</strong>Serialization of this class is appropriate for short-term storage
 * or RMI use, but will probably not be compatible with future version. For long term storage,
 * WKT (Well Know Text) or XML (not yet implemented) are more appropriate.
 *
 * @since 2.0
 * @version $Id$
 * @source $URL$
 * @author André Gosselin
 * @author Martin Desruisseaux
 * @author Rueben Schulz
 *
 * @see <A HREF="http://mathworld.wolfram.com/MapProjection.html">Map projections on MathWorld</A>
 * @see <A HREF="http://atlas.gc.ca/site/english/learningresources/carto_corner/map_projections.html">Map projections on the atlas of Canada</A>
 * @tutorial http://www.geotools.org/display/GEOTOOLS/How+to+add+new+projections
 */
public abstract class MapProjection extends AbstractMathTransform
                implements MathTransform2D, Serializable
{
    /**
     * Maximum difference allowed when comparing real numbers.
     */
    static final double EPS = 1.0E-6;
    
    /**
     * Difference allowed in iterative computations.
     */
    static final double TOL = 1E-10;
    
    /**
     * Maximum number of itterations for iterative computations.
     */
    static final int MAX_ITER = 15;
    
    /**
     * Ellipsoid excentricity, equals to <code>sqrt({@link #excentricitySquared})</code>.
     * Value 0 means that the ellipsoid is spherical.
     *
     * @see #excentricitySquared
     * @see #isSpherical
     */
    protected final double excentricity;
    
    /**
     * The square of excentricity: e² = (a²-b²)/a² where
     * <var>e</var> is the {@linkplain #excentricity excentricity},
     * <var>a</var> is the {@linkplain #semiMajor semi major} axis length and
     * <var>b</var> is the {@linkplain #semiMinor semi minor} axis length.
     *
     * @see #excentricity
     * @see #semiMajor
     * @see #semiMinor
     * @see #isSpherical
     */
    protected final double excentricitySquared;

    /**
     * {@code true} if this projection is spherical. Spherical model has identical
     * {@linkplain #semiMajor semi major} and {@linkplain #semiMinor semi minor} axis
     * length, and an {@linkplain #excentricity excentricity} zero.
     *
     * @see #excentricity
     * @see #semiMajor
     * @see #semiMinor
     */
    protected final boolean isSpherical;
    
    /**
     * Length of semi-major axis, in metres. This is named '<var>a</var>' or '<var>R</var>'
     * (Radius in spherical cases) in Snyder.
     *
     * @see #excentricity
     * @see #semiMinor
     */
    protected final double semiMajor;
    
    /**
     * Length of semi-minor axis, in metres. This is named '<var>b</var>' in Snyder.
     *
     * @see #excentricity
     * @see #semiMajor
     */
    protected final double semiMinor;
    
    /**
     * Central longitude in <u>radians</u>. Default value is 0, the Greenwich meridian.
     * This is called '<var>lambda0</var>' in Snyder.
     *
     * <strong>Consider this field as final</strong>. It is not final only
     * because some classes need to modify it at construction time.
     */
    protected double centralMeridian;
    
    /**
     * Latitude of origin in <u>radians</u>. Default value is 0, the equator.
     * This is called '<var>phi0</var>' in Snyder.
     *
     * <strong>Consider this field as final</strong>. It is not final only
     * because some classes need to modify it at construction time.
     */
    protected double latitudeOfOrigin;
    
    /**
     * The scale factor. Default value is 1. Named '<var>k</var>' in Snyder.
     *
     * <strong>Consider this field as final</strong>. It is not final only
     * because some classes need to modify it at construction time.
     */
    protected double scaleFactor;
    
    /**
     * False easting, in metres. Default value is 0.
     */
    protected final double falseEasting;
    
    /**
     * False northing, in metres. Default value is 0.
     */
    protected final double falseNorthing;
    
    /**
     * Global scale factor. Default value {@code globalScale} is equal
     * to {@link #semiMajor}&times;{@link #scaleFactor}.
     *
     * <strong>Consider this field as final</strong>. It is not final only
     * because some classes need to modify it at construction time.
     */
    protected double globalScale;
    
    /**
     * The inverse of this map projection. Will be created only when needed.
     */
    private transient MathTransform inverse;
    
    /**
     * Constructs a new map projection from the suplied parameters.
     *
     * @param  values The parameter values in standard units.
     *         The following parameter are recognized:
     *         <ul>
     *           <li>"semi_major" (mandatory: no default)</li>
     *           <li>"semi_minor" (mandatory: no default)</li>
     *           <li>"central_meridian"   (default to 0°)</li>
     *           <li>"latitude_of_origin" (default to 0°)</li>
     *           <li>"scale_factor"       (default to 1 )</li>
     *           <li>"false_easting"      (default to 0 )</li>
     *           <li>"false_northing"     (default to 0 )</li>
     *         </ul>
     * @throws ParameterNotFoundException if a mandatory parameter is missing.
     */
    protected MapProjection(final ParameterValueGroup values) throws ParameterNotFoundException {
        this(values, null);
    }

    /**
     * Constructor invoked by sub-classes when we can't rely on
     * {@link #getParameterDescriptors} before the construction
     * is completed. This is the case when the later method depends on
     * the value of some class's attribute, which has not yet been set.
     * An example is {@link Mercator#getParameterDescriptors}.
     *
     * This method is not public because it is not a very elegant hack, and
     * a work around exists. For example Mercator_1SP and Mercator_2SP could
     * be implemented by two separated classes, in which case {@link #getParameterDescriptors}
     * returns a constant and can be safely invoked in a constructor. We do
     * not always use this cleaner way in the projection package because it
     * is going to contains a lot of.. well... projections, and we will try
     * to reduce the amount of class declarations.
     */
    MapProjection(final ParameterValueGroup values, Collection expected)
            throws ParameterNotFoundException
    {
        if (expected == null) {
            expected = getParameterDescriptors().descriptors();
        }
        semiMajor           = doubleValue(expected, AbstractProvider.SEMI_MAJOR,         values);
        semiMinor           = doubleValue(expected, AbstractProvider.SEMI_MINOR,         values);
        centralMeridian     = doubleValue(expected, AbstractProvider.CENTRAL_MERIDIAN,   values);
        latitudeOfOrigin    = doubleValue(expected, AbstractProvider.LATITUDE_OF_ORIGIN, values);
        scaleFactor         = doubleValue(expected, AbstractProvider.SCALE_FACTOR,       values);
        falseEasting        = doubleValue(expected, AbstractProvider.FALSE_EASTING,      values);
        falseNorthing       = doubleValue(expected, AbstractProvider.FALSE_NORTHING,     values);
        isSpherical         = (semiMajor == semiMinor);
        excentricitySquared = 1.0 - (semiMinor*semiMinor)/(semiMajor*semiMajor);
        excentricity        = Math.sqrt(excentricitySquared);
        globalScale         = scaleFactor*semiMajor;
        ensureLongitudeInRange(AbstractProvider.CENTRAL_MERIDIAN,   centralMeridian,  true);
        ensureLatitudeInRange (AbstractProvider.LATITUDE_OF_ORIGIN, latitudeOfOrigin, true);
    }

    /**
     * Returns {@code true} if the specified parameter can apply to this map projection.
     * The set of expected parameters must be supplied. The default implementation just
     * invokes {@code expected.contains(param)}. Some subclasses will override this method
     * in order to handle {@link ModifiedParameterDescriptor} in a special way.
     *
     * @see #doubleValue
     * @see #set
     */
    boolean isExpectedParameter(final Collection expected, final ParameterDescriptor param) {
        return expected.contains(param);
    }

    /**
     * Returns the parameter value for the specified operation parameter. Values are
     * automatically converted into the standard units specified by the supplied
     * {@code param} argument, except {@link NonSI#DEGREE_ANGLE degrees} which
     * are converted to {@link SI#RADIAN radians}.
     *
     * @param  expected The value returned by {@code getParameterDescriptors().descriptors()}.
     * @param  param The parameter to look for.
     * @param  group The parameter value group to search into.
     * @return The requested parameter value, or {@code NaN} if {@code param} is
     *         {@linkplain MathTransformProvider#createOptionalDescriptor optional}
     *         and the user didn't provided any value.
     * @throws ParameterNotFoundException if the parameter is not found.
     *
     * @see MathTransformProvider#doubleValue
     */
    final double doubleValue(final Collection       expected,
                             final ParameterDescriptor param,
                             final ParameterValueGroup group)
            throws ParameterNotFoundException
    {
        if (isExpectedParameter(expected, param)) {
            /*
             * Gets the value supplied by the user. The conversion from
             * degrees to radians (if needed) is performed by AbstractProvider.
             */
            return AbstractProvider.doubleValue(param, group);
        }
        /*
         * The constructor asked for a parameter value that do not apply to the type of the
         * projection to be created. Returns a default value common to all projection types,
         * but this value should not be used in projection computations.
         */
        double v;
        final Object value = param.getDefaultValue();
        if (value instanceof Number) {
            v = ((Number) value).doubleValue();
            if (NonSI.DEGREE_ANGLE.equals(param.getUnit())) {
                v = Math.toRadians(v);
            }
        } else {
            v = Double.NaN;
        }
        return v;
    }

    /**
     * Ensures that the latitude is within allowed limits (&plusmn;&pi;/2).
     * This method is useful to check the validity of projection parameters,
     * like {@link #latitudeOfOrigin}.
     *
     * @param  y Latitude to check, in radians.
     * @param  edge {@code true} to accept latitudes of &plusmn;&pi;/2.
     * @throws IllegalArgumentException if the latitude is out of range.
     */
    static void ensureLatitudeInRange(final ParameterDescriptor name, double y, final boolean edge)
            throws IllegalArgumentException
    {
        if (edge ? (y>=Latitude.MIN_VALUE*Math.PI/180 && y<=Latitude.MAX_VALUE*Math.PI/180) :
                   (y> Latitude.MIN_VALUE*Math.PI/180 && y< Latitude.MAX_VALUE*Math.PI/180))
        {
            return;
        }
        y = Math.toDegrees(y);
        throw new InvalidParameterValueException(Errors.format(ErrorKeys.LATITUDE_OUT_OF_RANGE_$1,
                                                 new Latitude(y)), name.getName().getCode(), y);
    }
    
    /**
     * Ensures that the longitue is within allowed limits (&plusmn;&pi;).
     * This method is used to check the validity of projection parameters,
     * like {@link #centralMeridian}.
     *
     * @param  x Longitude to verify, in radians.
     * @param  edge {@code true} for accepting longitudes of &plusmn;&pi;.
     * @throws IllegalArgumentException if the longitude is out of range.
     */
    static void ensureLongitudeInRange(final ParameterDescriptor name, double x, final boolean edge)
            throws IllegalArgumentException
    {
        if (edge ? (x>=Longitude.MIN_VALUE*Math.PI/180 && x<=Longitude.MAX_VALUE*Math.PI/180) :
                   (x> Longitude.MIN_VALUE*Math.PI/180 && x< Longitude.MAX_VALUE*Math.PI/180))
        {
            return;
        }
        x = Math.toDegrees(x);
        throw new InvalidParameterValueException(Errors.format(ErrorKeys.LONGITUDE_OUT_OF_RANGE_$1,
                                                 new Longitude(x)), name.getName().getCode(), x);
    }

    /**
     * Set the value in a parameter group. This convenience method is used
     * by subclasses for {@link #getParameterValues} implementation. Values
     * are automatically converted from radians to degrees if needed.
     *
     * @param expected  The value returned by {@code getParameterDescriptors().descriptors()}.
     * @param param     One of the {@link AbstractProvider} constants.
     * @param group     The group in which to set the value.
     * @param value     The value to set.
     */
    final void set(final Collection       expected,
                   final ParameterDescriptor param,
                   final ParameterValueGroup group,
                   double value)
    {
        if (isExpectedParameter(expected, param)) {
            if (NonSI.DEGREE_ANGLE.equals(param.getUnit())) {
                /*
                 * Converts radians to degrees and try to fix rounding error
                 * (e.g. -61.500000000000014  -->  -61.5). This is necessary
                 * in order to avoid a bias when formatting a transform and
                 * parsing it again.
                 */
                value = Math.toDegrees(value);
                double old = value;
                value = XMath.fixRoundingError(value, 12);
                if (value == old) {
                    /*
                     * The attempt to fix rounding error failed. Try again with the
                     * assumption that the true value is a multiple of 1/3 of angle
                     * (e.g. 51.166666666666664  -->  51.166666666666666), which is
                     * common in the EPSG database.
                     */
                    old *= 3;
                    final double test = XMath.fixRoundingError(old, 12);
                    if (test != old) {
                        value = test/3;
                    }
                }
            }
            group.parameter(param.getName().getCode()).setValue(value);
        }
    }

    /**
     * Returns the parameter descriptors for this map projection.
     * This is used for a providing a default implementation of
     * {@link #getParameterValues}, as well as arguments checking.
     */
    public abstract ParameterDescriptorGroup getParameterDescriptors();

    /**
     * Returns the parameter values for this map projection.
     *
     * @return A copy of the parameter values for this map projection.
     */
    public ParameterValueGroup getParameterValues() {
        final ParameterDescriptorGroup descriptor = getParameterDescriptors();
        final Collection expected = descriptor.descriptors();
        // TODO: remove the cast below once we will be allowed to use J2SE 1.5.
        final ParameterValueGroup values = (ParameterValueGroup) descriptor.createValue();
        set(expected, AbstractProvider.SEMI_MAJOR,         values, semiMajor       );
        set(expected, AbstractProvider.SEMI_MINOR,         values, semiMinor       );
        set(expected, AbstractProvider.CENTRAL_MERIDIAN,   values, centralMeridian );
        set(expected, AbstractProvider.LATITUDE_OF_ORIGIN, values, latitudeOfOrigin);
        set(expected, AbstractProvider.SCALE_FACTOR,       values, scaleFactor     );
        set(expected, AbstractProvider.FALSE_EASTING,      values, falseEasting    );
        set(expected, AbstractProvider.FALSE_NORTHING,     values, falseNorthing   );
        return values;
    }
    
    /**
     * Returns the dimension of input points.
     */
    public final int getSourceDimensions() {
        return 2;
    }
    
    /**
     * Returns the dimension of output points.
     */
    public final int getTargetDimensions() {
        return 2;
    }
    
    
    
    
    //////////////////////////////////////////////////////////////////////////////////////////
    ////////                                                                          ////////
    ////////                          TRANSFORMATION METHODS                          ////////
    ////////             Includes an inner class for inverse projections.             ////////
    ////////                                                                          ////////
    //////////////////////////////////////////////////////////////////////////////////////////
    
    /**
     * Check point for private use by {@link #checkTransform}. This class is necessary in order
     * to avoid never-ending loop in {@code assert} statements (when an {@code assert}
     * calls {@code transform(...)}, which calls {@code inverse.transform(...)}, which
     * calls {@code transform(...)}, etc.).
     */
    private static final class CheckPoint extends Point2D.Double {
        public CheckPoint(final Point2D point) {
            super(point.getX(), point.getY());
        }
    }
    
    /**
     * Check if the transform of {@code point} is close enough to {@code target}.
     * "Close enough" means that the two points are separated by a distance shorter than
     * {@link #getToleranceForAssertions}. This method is used for assertions with J2SE 1.4.
     *
     * @param point   Point to transform, in degrees if {@code inverse} is false.
     * @param target  Point to compare to, in metres if {@code inverse} is false.
     * @param inverse {@code true} for an inverse transform instead of a direct one.
     * @return {@code true} if the two points are close enough.
     */
    private boolean checkTransform(Point2D point, final Point2D target, final boolean inverse) {
        if (!(point instanceof CheckPoint)) try {
            point = new CheckPoint(point);
            final double longitude;
            final double latitude;
            final double distance;
            if (inverse) {
                // Computes orthodromic distance (spherical model) in metres.
                point = ((MathTransform2D)inverse()).transform(point, point);
                final double y1 = Math.toRadians(point .getY());
                final double y2 = Math.toRadians(target.getY());
                final double dx = Math.toRadians(Math.abs(target.getX()-point.getX()) % 360);
                double rho = Math.sin(y1)*Math.sin(y2) + Math.cos(y1)*Math.cos(y2)*Math.cos(dx);
                if (rho>+1) {assert rho<=+(1+EPS) : rho; rho=+1;}
                if (rho<-1) {assert rho>=-(1+EPS) : rho; rho=-1;}
                distance  = Math.acos(rho)*semiMajor;
                longitude = point.getX();
                latitude  = point.getY();
            } else {
                // Computes cartesian distance in metres.
                longitude = point.getX();
                latitude  = point.getY();
                point     = transform(point, point);
                distance  = point.distance(target);
            }
            if (distance > getToleranceForAssertions(longitude, latitude)) {
                // Do not fail for NaN values.
                throw new AssertionError(Errors.format(ErrorKeys.PROJECTION_CHECK_FAILED_$3,
                          new Double   (distance),
                          new Longitude(longitude - Math.toDegrees(centralMeridian )),
                          new Latitude (latitude  - Math.toDegrees(latitudeOfOrigin))));
            }
        } catch (TransformException exception) {
            final AssertionError error = new AssertionError(exception.getLocalizedMessage());
            error.initCause(exception);
            throw error;
        }
        return true;
    }
    
    /**
     * Transforms the specified coordinate and stores the result in {@code ptDst}.
     * This method returns longitude as <var>x</var> values in the range {@code [-PI..PI]}
     * and latitude as <var>y</var> values in the range {@code [-PI/2..PI/2]}. It will be
     * checked by the caller, so this method doesn't need to performs this check.
     * <p>
     *
     * Input coordinates are also guarenteed to have the {@link #falseEasting} 
     * and {@link #falseNorthing} removed and be divided by {@link #globalScale}
     * before this method is invoked. After this method is invoked, the 
     * {@link #centralMeridian} is added to the {@code x} results 
     * in {@code ptDst}. This means that projections that implement this method 
     * are performed on an ellipse (or sphere) with a semiMajor axis of 1.0.
     * <p>
     *
     * In <A HREF="http://www.remotesensing.org/proj/">PROJ.4</A>, the same
     * standardization, described above, is handled by {@code pj_inv.c}.
     * Therefore when porting projections from PROJ.4, the inverse transform
     * equations can be used directly here with minimal change.
     * In the equations of Snyder, {@link #falseEasting}, {@link #falseNorthing}
     * and {@link #scaleFactor} are usually not given.
     * When implementing these equations here, you will not
     * need to add the {@link #centralMeridian} to the output longitude or remove the
     * {@link #semiMajor} ('<var>a</var>' or '<var>R</var>').
     *
     * @param x     The easting of the coordinate, linear distance on a unit sphere or ellipse.
     * @param y     The northing of the coordinate, linear distance on a unit sphere or ellipse.
     * @param ptDst the specified coordinate point that stores the result of transforming
     *              {@code ptSrc}, or {@code null}. Ordinates will be in
     *              <strong>radians</strong>.
     * @return      the coordinate point after transforming {@code x}, {@code y} 
     *              and storing the result in {@code ptDst}.
     * @throws ProjectionException if the point can't be transformed.
     *
     * @todo The {@code ptDst} argument will be removed and the return type changed if
     * RFE <A HREF="http://developer.java.sun.com/developer/bugParade/bugs/4222792.html">4222792</A>
     * is implemented efficiently in a future J2SE release (maybe J2SE 1.6?).
     */
    protected abstract Point2D inverseTransformNormalized(double x, double y, final Point2D ptDst)
            throws ProjectionException;
    
    /**
     * Transforms the specified coordinate and stores the result in {@code ptDst}.
     * This method is guaranteed to be invoked with values of <var>x</var> in the range
     * {@code [-PI..PI]} and values of <var>y</var> in the range {@code [-PI/2..PI/2]}.
     * <p>
     * 
     * Coordinates are also guaranteed to have the {@link #centralMeridian} 
     * removed from <var>x</var> before this method is invoked. After this method 
     * is invoked, the results in {@code ptDst} are multiplied by {@link #globalScale},
     * and the {@link #falseEasting} and {@link #falseNorthing} are added.
     * This means that projections that implement this method are performed on an
     * ellipse (or sphere) with a semiMajor axis of 1.0. 
     * <p>
     *
     * In <A HREF="http://www.remotesensing.org/proj/">PROJ.4</A>, the same
     * standardization, described above, is handled by {@code pj_fwd.c}.
     * Therefore when porting projections from PROJ.4, the forward transform equations can
     * be used directly here with minimal change. In the equations of Snyder,
     * {@link #falseEasting}, {@link #falseNorthing} and {@link #scaleFactor}
     * are usually not given. When implementing these equations here, you will not
     * need to remove the {@link #centralMeridian} from <var>x</var> or apply the
     * {@link #semiMajor} ('<var>a</var>' or '<var>R</var>').
     *
     * @param x     The longitude of the coordinate, in <strong>radians</strong>.
     * @param y     The  latitude of the coordinate, in <strong>radians</strong>.
     * @param ptDst the specified coordinate point that stores the result of transforming
     *              {@code ptSrc}, or {@code null}. Ordinates will be in a
     *              dimensionless unit, as a linear distance on a unit sphere or ellipse.
     * @return      the coordinate point after transforming {@code x}, {@code y}
     *              and storing the result in {@code ptDst}.
     * @throws ProjectionException if the point can't be transformed.
     *
     * @todo The {@code ptDst} argument will be removed and the return type changed if
     * RFE <A HREF="http://developer.java.sun.com/developer/bugParade/bugs/4222792.html">4222792</A>
     * is implemented efficiently in a future J2SE release (maybe J2SE 1.6?).
     */
    protected abstract Point2D transformNormalized(double x, double y, final Point2D ptDst)
            throws ProjectionException;
    
    /**
     * Transforms the specified {@code ptSrc} and stores the result in {@code ptDst}.
     * <p>
     *
     * This method standardizes the source {@code x} coordinate
     * by removing the {@link #centralMeridian}, before invoking
     * <code>{@link #transformNormalized transformNormalized}(x, y, ptDst)</code>.
     * It also multiplies by {@link #globalScale} and adds the {@link #falseEasting} and
     * {@link #falseNorthing} to the point returned by the {@code transformNormalized(...)}
     * call.
     *
     * @param ptSrc the specified coordinate point to be transformed. Ordinates must be in degrees.
     * @param ptDst the specified coordinate point that stores the result of transforming
     *              {@code ptSrc}, or {@code null}. Ordinates will be in metres.
     * @return      the coordinate point after transforming {@code ptSrc} and storing
     *              the result in {@code ptDst}.
     * @throws ProjectionException if the point can't be transformed.
     */
    public final Point2D transform(final Point2D ptSrc, Point2D ptDst) throws ProjectionException {
        final double x = ptSrc.getX();
        final double y = ptSrc.getY();
        if (x<Longitude.MIN_VALUE-EPS || x>Longitude.MAX_VALUE+EPS) { // Do not fail for NaN values.
            throw new PointOutsideEnvelopeException(Errors.format(
                    ErrorKeys.LONGITUDE_OUT_OF_RANGE_$1, new Longitude(x)));
        }
        if (y<Latitude.MIN_VALUE-EPS || y>Latitude.MAX_VALUE+EPS) { // Do not fail for NaN values.
            throw new PointOutsideEnvelopeException(Errors.format(
                    ErrorKeys.LATITUDE_OUT_OF_RANGE_$1, new Latitude(y)));
        }
        /*
         * Makes sure that the longitude before conversion stay within +/- PI radians. As a
         * special case, we do not check the range if no rotation were applied on the longitude.
         * This is because the user may have a big area ranging from -180° to +180°. With the
         * slight rounding errors related to map projections, the 180° longitude may be slightly
         * over the limit. Rolling the longitude would changes its sign. For example a bounding
         * box from 30° to +180° would become 30° to -180°, which is probably not what the user
         * wanted.
         */
        ptDst = transformNormalized(centralMeridian!=0 ?
                                    rollLongitude(Math.toRadians(x) - centralMeridian) :
                                    Math.toRadians(x), Math.toRadians(y), ptDst);
        ptDst.setLocation(globalScale*ptDst.getX() + falseEasting, 
                          globalScale*ptDst.getY() + falseNorthing);

        assert checkTransform(ptDst, (ptSrc!=ptDst) ? ptSrc : new Point2D.Double(x,y), true);
        return ptDst;
    }
    
    /**
     * Transforms a list of coordinate point ordinal values. Ordinates must be
     * (<var>longitude</var>,<var>latitude</var>) pairs in degrees.
     *
     * @throws ProjectionException if a point can't be transformed. This method try
     *         to transform every points even if some of them can't be transformed.
     *         Non-transformable points will have value {@link Double#NaN}. If more
     *         than one point can't be transformed, then this exception may be about
     *         an arbitrary point.
     */
    public final void transform(final double[] src,  int srcOffset,
                                final double[] dest, int dstOffset, int numPts)
        throws ProjectionException
    {
        /*
         * Vérifie s'il faudra parcourir le tableau en sens inverse.
         * Ce sera le cas si les tableaux source et destination se
         * chevauchent et que la destination est après la source.
         */
        final boolean reverse = (src==dest && srcOffset<dstOffset &&
                                 srcOffset+(2*numPts) > dstOffset);
        if (reverse) {
            srcOffset += 2*numPts;
            dstOffset += 2*numPts;
        }
        final Point2D.Double point = new Point2D.Double();
        ProjectionException firstException = null;
        while (--numPts>=0) {
            try {
                point.x = src[srcOffset++];
                point.y = src[srcOffset++];
                transform(point, point);
                dest[dstOffset++] = point.x;
                dest[dstOffset++] = point.y;
            } catch (ProjectionException exception) {
                dest[dstOffset++] = Double.NaN;
                dest[dstOffset++] = Double.NaN;
                if (firstException == null) {
                    firstException = exception;
                }
            }
            if (reverse) {
                srcOffset -= 4;
                dstOffset -= 4;
            }
        }
        if (firstException != null) {
            throw firstException;
        }
    }
    
    /**
     * Transforms a list of coordinate point ordinal values. Ordinates must be
     * (<var>longitude</var>,<var>latitude</var>) pairs in degrees.
     *
     * @throws ProjectionException if a point can't be transformed. This method try
     *         to transform every points even if some of them can't be transformed.
     *         Non-transformable points will have value {@link Float#NaN}. If more
     *         than one point can't be transformed, then this exception may be about
     *         an arbitrary point.
     */
    public final void transform(final float[] src,  int srcOffset,
                                final float[] dest, int dstOffset, int numPts)
        throws ProjectionException
    {
        final boolean reverse = (src==dest && srcOffset<dstOffset &&
                                 srcOffset+(2*numPts) > dstOffset);
        if (reverse) {
            srcOffset += 2*numPts;
            dstOffset += 2*numPts;
        }
        final Point2D.Double point = new Point2D.Double();
        ProjectionException firstException=null;
        while (--numPts>=0) {
            try {
                point.x = src[srcOffset++];
                point.y = src[srcOffset++];
                transform(point, point);
                dest[dstOffset++] = (float) point.x;
                dest[dstOffset++] = (float) point.y;
            } catch (ProjectionException exception) {
                dest[dstOffset++] = Float.NaN;
                dest[dstOffset++] = Float.NaN;
                if (firstException == null) {
                    firstException = exception;
                }
            }
            if (reverse) {
                srcOffset -= 4;
                dstOffset -= 4;
            }
        }
        if (firstException != null) {
            throw firstException;
        }
    }
    
    /**
     * Inverse of a map projection.  Will be created by {@link MapProjection#inverse()} only when
     * first required. Implementation of {@code transform(...)} methods are mostly identical
     * to {@code MapProjection.transform(...)}, except that they will invokes
     * {@link MapProjection#inverseTransformNormalized} instead of
     * {@link MapProjection#transformNormalized}.
     *
     * @version $Id$
     * @author Martin Desruisseaux
     */
    private final class Inverse extends AbstractMathTransform.Inverse implements MathTransform2D {
        /**
         * Default constructor.
         */
        public Inverse() {
            MapProjection.this.super();
        }

        /**
         * Inverse transforms the specified {@code ptSrc} and stores the result in {@code ptDst}.
         * <p>
         *
         * This method standardizes the {@code ptSrc} by removing the 
         * {@link #falseEasting} and {@link #falseNorthing} and dividing by 
         * {@link #globalScale} before invoking 
         * <code>{@link #inverseTransformNormalized inverseTransformNormalized}(x, y, ptDst)</code>.
         * It then adds the {@link #centralMeridian} to the {@code x} of the
         * point returned by the {@code inverseTransformNormalized} call.
         *
         * @param ptSrc the specified coordinate point to be transformed.
         *              Ordinates must be in metres.
         * @param ptDst the specified coordinate point that stores the
         *              result of transforming {@code ptSrc}, or
         *              {@code null}. Ordinates will be in degrees.
         * @return the coordinate point after transforming {@code ptSrc}
         *         and stroring the result in {@code ptDst}.
         * @throws ProjectionException if the point can't be transformed.
         */
        public final Point2D transform(final Point2D ptSrc, Point2D ptDst)
                throws ProjectionException
        {
            final double x0 = ptSrc.getX();
            final double y0 = ptSrc.getY();
            ptDst = inverseTransformNormalized((x0 - falseEasting )/globalScale,
                                               (y0 - falseNorthing)/globalScale, ptDst);
            /*
             * Makes sure that the longitude after conversion stay within +/- PI radians. As a
             * special case, we do not check the range if no rotation were applied on the longitude.
             * This is because the user may have a big area ranging from -180° to +180°. With the
             * slight rounding errors related to map projections, the 180° longitude may be slightly
             * over the limit. Rolling the longitude would changes its sign. For example a bounding
             * box from 30° to +180° would become 30° to -180°, which is probably not what the user
             * wanted.
             */
            final double x = Math.toDegrees(centralMeridian!=0 ?
                             rollLongitude(ptDst.getX() + centralMeridian) : ptDst.getX());
            final double y = Math.toDegrees(ptDst.getY());
            ptDst.setLocation(x,y);

            if (x<Longitude.MIN_VALUE-EPS || x>Longitude.MAX_VALUE+EPS) { // Accept NaN values.
                throw new PointOutsideEnvelopeException(Errors.format(
                        ErrorKeys.LONGITUDE_OUT_OF_RANGE_$1, new Longitude(x)));
            }
            if (y<Latitude.MIN_VALUE-EPS || y>Latitude.MAX_VALUE+EPS) { // Accept NaN values.
                throw new PointOutsideEnvelopeException(Errors.format(
                        ErrorKeys.LATITUDE_OUT_OF_RANGE_$1, new Latitude(y)));
            }
            assert checkTransform(ptDst, (ptSrc!=ptDst) ? ptSrc : new Point2D.Double(x0, y0), false);
            return ptDst;
        }

        /**
         * Inverse transforms a list of coordinate point ordinal values.
         * Ordinates must be (<var>x</var>,<var>y</var>) pairs in metres.
         *
         * @throws ProjectionException if a point can't be transformed. This method try
         *         to transform every points even if some of them can't be transformed.
         *         Non-transformable points will have value {@link Double#NaN}. If more
         *         than one point can't be transformed, then this exception may be about
         *         an arbitrary point.
         */
        public final void transform(final double[] src,  int srcOffset,
                                    final double[] dest, int dstOffset, int numPts)
                throws TransformException
        {
            /*
             * Vérifie s'il faudra parcourir le tableau en sens inverse.
             * Ce sera le cas si les tableaux source et destination se
             * chevauchent et que la destination est après la source.
             */
            final boolean reverse = (src==dest && srcOffset<dstOffset &&
                                     srcOffset+(2*numPts) > dstOffset);
            if (reverse) {
                srcOffset += 2*numPts;
                dstOffset += 2*numPts;
            }
            final Point2D.Double point = new Point2D.Double();
            ProjectionException firstException = null;
            while (--numPts>=0) {
                try {
                    point.x = src[srcOffset++];
                    point.y = src[srcOffset++];
                    transform(point, point);
                    dest[dstOffset++] = point.x;
                    dest[dstOffset++] = point.y;
                } catch (ProjectionException exception) {
                    dest[dstOffset++] = Double.NaN;
                    dest[dstOffset++] = Double.NaN;
                    if (firstException == null) {
                        firstException = exception;
                    }
                }
                if (reverse) {
                    srcOffset -= 4;
                    dstOffset -= 4;
                }
            }
            if (firstException != null) {
                throw firstException;
            }
        }

        /**
         * Inverse transforms a list of coordinate point ordinal values.
         * Ordinates must be (<var>x</var>,<var>y</var>) pairs in metres.
         *
         * @throws ProjectionException if a point can't be transformed. This method try
         *         to transform every points even if some of them can't be transformed.
         *         Non-transformable points will have value {@link Float#NaN}. If more
         *         than one point can't be transformed, then this exception may be about
         *         an arbitrary point.
         */
        public final void transform(final float[] src,  int srcOffset,
                                    final float[] dest, int dstOffset, int numPts)
                throws ProjectionException
        {
            final boolean reverse = (src==dest && srcOffset<dstOffset &&
                                     srcOffset+(2*numPts) > dstOffset);
            if (reverse) {
                srcOffset += 2*numPts;
                dstOffset += 2*numPts;
            }
            final Point2D.Double point = new Point2D.Double();
            ProjectionException firstException = null;
            while (--numPts>=0) {
                try {
                    point.x = src[srcOffset++];
                    point.y = src[srcOffset++];
                    transform(point, point);
                    dest[dstOffset++] = (float) point.x;
                    dest[dstOffset++] = (float) point.y;
                } catch (ProjectionException exception) {
                    dest[dstOffset++] = Float.NaN;
                    dest[dstOffset++] = Float.NaN;
                    if (firstException == null) {
                        firstException = exception;
                    }
                }
                if (reverse) {
                    srcOffset -= 4;
                    dstOffset -= 4;
                }
            }
            if (firstException!=null) {
                throw firstException;
            }
        }
    }
    
    /**
     * Returns the inverse of this map projection.
     */
    public final MathTransform inverse() {
        // No synchronization. Not a big deal if this method is invoked in
        // the same time by two threads resulting in two instances created.
        if (inverse == null) {
            inverse = new Inverse();
        }
        return inverse;
    }

    /**
     * Maximal error (in metres) tolerated for assertion, if enabled. When assertions are enabled,
     * every direct projection is followed by an inverse projection, and the result is compared to
     * the original coordinate. If a distance greater than the tolerance level is found, then an
     * {@link AssertionError} will be thrown. Subclasses should override this method if they need
     * to relax the tolerance level.
     *
     * @param  longitude The longitude in degrees.
     * @param  latitude The latitude in degrees.
     * @return The tolerance level for assertions, in meters.
     */
    protected double getToleranceForAssertions(final double longitude, final double latitude) {
        if (Math.abs(longitude - centralMeridian)/2 +
            Math.abs(latitude  - latitudeOfOrigin) > 40)
        {
            // When far from the valid area, use a larger tolerance.
            return 1;
        }
        // Be less strict when the point is near an edge.
        return (Math.abs(longitude) > 179) || (Math.abs(latitude) > 89) ? 1E-1 : 1E-6;
    }
    
    
    
    
    //////////////////////////////////////////////////////////////////////////////////////////
    ////////                                                                          ////////
    ////////      IMPLEMENTATION OF Object AND MathTransform2D STANDARD METHODS       ////////
    ////////                                                                          ////////
    //////////////////////////////////////////////////////////////////////////////////////////
    
    /**
     * Returns a hash value for this map projection.
     */
    public int hashCode() {
        long code =      Double.doubleToLongBits(semiMajor);
        code = code*37 + Double.doubleToLongBits(semiMinor);
        code = code*37 + Double.doubleToLongBits(centralMeridian);
        code = code*37 + Double.doubleToLongBits(latitudeOfOrigin);
        return (int) code ^ (int) (code >>> 32);
    }
    
    /**
     * Compares the specified object with this map projection for equality.
     */
    public boolean equals(final Object object) {
        // Do not check 'object==this' here, since this
        // optimization is usually done in subclasses.
        if (super.equals(object)) {
            final MapProjection that = (MapProjection) object;
            return equals(this.semiMajor,        that.semiMajor)        &&
                   equals(this.semiMinor,        that.semiMinor)        &&
                   equals(this.centralMeridian,  that.centralMeridian)  &&
                   equals(this.latitudeOfOrigin, that.latitudeOfOrigin) &&
                   equals(this.scaleFactor,      that.scaleFactor)      &&
                   equals(this.falseEasting,     that.falseEasting)     &&
                   equals(this.falseNorthing,    that.falseNorthing);
        }
        return false;
    }

    /**
     * Returns {@code true} if the two specified value are equals.
     * Two {@link Double#NaN NaN} values are considered equals.
     */
    static boolean equals(final double value1, final double value2) {
        return Double.doubleToLongBits(value1) == Double.doubleToLongBits(value2);
    }
    
    
    
    
    //////////////////////////////////////////////////////////////////////////////////////////
    ////////                                                                          ////////
    ////////                           FORMULAS FROM SNYDER                           ////////
    ////////                                                                          ////////
    //////////////////////////////////////////////////////////////////////////////////////////
    
    /**
     * Iteratively solve equation (7-9) from Snyder.
     */
    final double cphi2(final double ts) throws ProjectionException {
        final double eccnth = 0.5*excentricity;
        double phi = (Math.PI/2) - 2.0*Math.atan(ts);
        for (int i=0; i<MAX_ITER; i++) {
            final double con  = excentricity*Math.sin(phi);
            final double dphi = (Math.PI/2) - 2.0*Math.atan(ts * Math.pow((1-con)/(1+con), eccnth)) - phi;
            phi += dphi;
            if (Math.abs(dphi) <= TOL) {
                return phi;
            }
        }
        throw new ProjectionException(Errors.format(ErrorKeys.NO_CONVERGENCE));
    }
    
    /**
     * Compute function <code>f(s,c,e²) = c/sqrt(1 - s²&times;e²)</code> needed for the true scale
     * latitude (Snyder 14-15), where <var>s</var> and <var>c</var> are the sine and cosine of
     * the true scale latitude, and <var>e²</var> is the {@linkplain #excentricitySquared
     * eccentricity squared}.
     */
    final double msfn(final double s, final double c) {
        return c / Math.sqrt(1.0 - (s*s) * excentricitySquared);
    }
    
    /**
     * Compute function (15-9) and (9-13) from Snyder.
     * Equivalent to negative of function (7-7).
     */
    final double tsfn(final double phi, double sinphi) {
        sinphi *= excentricity;
        /*
         * NOTE: change sign to get the equivalent of Snyder (7-7).
         */
        return Math.tan(0.5 * ((Math.PI/2) - phi)) /
               Math.pow((1-sinphi)/(1+sinphi), 0.5*excentricity);
    }
    
    
    
    
    //////////////////////////////////////////////////////////////////////////////////////////
    ////////                                                                          ////////
    ////////                                 PROVIDER                                 ////////
    ////////                                                                          ////////
    //////////////////////////////////////////////////////////////////////////////////////////
    
    /**
     * The base provider for {@link MapProjection}s.
     *
     * @version $Id$
     * @author Martin Desruisseaux
     */
    public static abstract class AbstractProvider extends MathTransformProvider {
        /**
         * Serial number for interoperability with different versions.
         */
        private static final long serialVersionUID = 6280666068007678702L;

        /**
         * The operation parameter descriptor for the {@linkplain #semiMajor semi major} parameter
         * value. Valid values range is from 0 to infinity. This parameter is mandatory.
         *
         * @todo Would like to start range from 0 <u>exclusive</u>.
         */
        public static final ParameterDescriptor SEMI_MAJOR = createDescriptor(
                new NamedIdentifier[] {
                    new NamedIdentifier(Citations.OGC,  "semi_major"),
                    new NamedIdentifier(Citations.EPSG, "semi-major axis")   //epsg does not specifically define this parameter
                },
                Double.NaN, 0, Double.POSITIVE_INFINITY, SI.METER);

        /**
         * The operation parameter descriptor for the {@linkplain #semiMinor semi minor} parameter
         * value. Valid values range is from 0 to infinity. This parameter is mandatory.
         *
         * @todo Would like to start range from 0 <u>exclusive</u>.
         */
        public static final ParameterDescriptor SEMI_MINOR = createDescriptor(
                new NamedIdentifier[] {
                    new NamedIdentifier(Citations.OGC,  "semi_minor"),
                    new NamedIdentifier(Citations.EPSG, "semi-minor axis")   //epsg does not specifically define this parameter
                },
                Double.NaN, 0, Double.POSITIVE_INFINITY, SI.METER);

        /**
         * The operation parameter descriptor for the {@linkplain #centralMeridian central meridian}
         * parameter value. Valid values range is from -180 to 180°. Default value is 0.
         */
        public static final ParameterDescriptor CENTRAL_MERIDIAN = createDescriptor(
                new NamedIdentifier[] {
                    new NamedIdentifier(Citations.OGC,     "central_meridian"),
                    new NamedIdentifier(Citations.EPSG,    "Longitude of natural origin"),
                    new NamedIdentifier(Citations.EPSG,    "Longitude of false origin"),
                    new NamedIdentifier(Citations.ESRI,    "Longitude_Of_Origin"),
                    new NamedIdentifier(Citations.ESRI,    "Longitude_Of_Center"),  //ESRI uses this in orthographic (not to be confused with Longitude_Of_Center in oblique mercator)
                    new NamedIdentifier(Citations.GEOTIFF, "NatOriginLong")
                },
                0, -180, 180, NonSI.DEGREE_ANGLE);

        /**
         * The operation parameter descriptor for the {@linkplain #latitudeOfOrigin latitude of origin}
         * parameter value. Valid values range is from -90 to 90°. Default value is 0.
         */
        public static final ParameterDescriptor LATITUDE_OF_ORIGIN = createDescriptor(
                new NamedIdentifier[] {
                    new NamedIdentifier(Citations.OGC,  "latitude_of_origin"),
                    new NamedIdentifier(Citations.EPSG, "Latitude of false origin"),
                    new NamedIdentifier(Citations.EPSG, "Latitude of natural origin"),
                    new NamedIdentifier(Citations.ESRI, "Latitude_Of_Center"),  //ESRI uses this in orthographic 
                    new NamedIdentifier(Citations.GEOTIFF,  "NatOriginLat")
                },
                0, -90, 90, NonSI.DEGREE_ANGLE);

        /**
         * The operation parameter descriptor for the {@linkplain Mercator#standardParallel standard
         * parallel} parameter value. Valid values range is from -90 to 90°. Default value is 0.
         */
        public static final ParameterDescriptor STANDARD_PARALLEL = createDescriptor(
                new NamedIdentifier[] {
                    new NamedIdentifier(Citations.OGC,      "standard_parallel_1"),
                    new NamedIdentifier(Citations.EPSG,     "Latitude of 1st standard parallel"),
                    new NamedIdentifier(Citations.GEOTIFF,  "StdParallel1")
                },
                0, -90, 90, NonSI.DEGREE_ANGLE);

        /**
         * The operation parameter descriptor for the standard parallel 1 parameter value.
         * Valid values range is from -90 to 90°. Default value is 0.
         */
        public static final ParameterDescriptor STANDARD_PARALLEL_1 = createDescriptor(
                new NamedIdentifier[] {
                    new NamedIdentifier(Citations.OGC,      "standard_parallel_1"),
                    new NamedIdentifier(Citations.EPSG,     "Latitude of 1st standard parallel"),
                    new NamedIdentifier(Citations.GEOTIFF,  "StdParallel1")
                },
                0, -90, 90, NonSI.DEGREE_ANGLE);

        /**
         * The operation parameter descriptor for the standard parallel 2 parameter value.
         * Valid values range is from -90 to 90°. Default value is 0.
         */
        public static final ParameterDescriptor STANDARD_PARALLEL_2 = createOptionalDescriptor(
                new NamedIdentifier[] {
                    new NamedIdentifier(Citations.OGC,      "standard_parallel_2"),
                    new NamedIdentifier(Citations.EPSG,     "Latitude of 2nd standard parallel"),
                    new NamedIdentifier(Citations.GEOTIFF,  "StdParallel2")
                },
                -90, 90, NonSI.DEGREE_ANGLE);

        /**
         * The operation parameter descriptor for the {@link #scaleFactor scaleFactor}
         * parameter value. Valid values range is from 0 to infinity. Default value is 1.
         *
         * @todo Would like to start range from 0 <u>exclusive</u>.
         */
        public static final ParameterDescriptor SCALE_FACTOR = createDescriptor(
                new NamedIdentifier[] {
                    new NamedIdentifier(Citations.OGC,     "scale_factor"),
                    new NamedIdentifier(Citations.EPSG,    "Scale factor at natural origin"),
                    new NamedIdentifier(Citations.GEOTIFF, "ScaleAtNatOrigin"),
                    new NamedIdentifier(Citations.GEOTIFF, "ScaleAtCenter")
                },
                1, 0, Double.POSITIVE_INFINITY, Unit.ONE);

        /**
         * The operation parameter descriptor for the {@link #falseEasting falseEasting}
         * parameter value. Valid values range is unrestricted. Default value is 0.
         */
        public static final ParameterDescriptor FALSE_EASTING = createDescriptor(
                new NamedIdentifier[] {
                    new NamedIdentifier(Citations.OGC,     "false_easting"),
                    new NamedIdentifier(Citations.EPSG,    "False easting"),
                    new NamedIdentifier(Citations.EPSG,    "Easting at false origin"),
                    new NamedIdentifier(Citations.GEOTIFF, "FalseEasting")
                },
                0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, SI.METER);

        /**
         * The operation parameter descriptor for the {@link #falseNorthing falseNorthing}
         * parameter value. Valid values range is unrestricted. Default value is 0.
         */
        public static final ParameterDescriptor FALSE_NORTHING = createDescriptor(
                new NamedIdentifier[] {
                    new NamedIdentifier(Citations.OGC,     "false_northing"),
                    new NamedIdentifier(Citations.EPSG,    "False northing"),
                    new NamedIdentifier(Citations.EPSG,    "Northing at false origin"),
                    new NamedIdentifier(Citations.GEOTIFF, "FalseNorthing")
                },
                0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, SI.METER);

        /**
         * Constructs a math transform provider from a set of parameters. The provider
         * {@linkplain #getIdentifiers identifiers} will be the same than the parameter
         * ones.
         *
         * @param parameters The set of parameters (never {@code null}).
         */
        public AbstractProvider(final ParameterDescriptorGroup parameters) {
            super(2, 2, parameters);
        }

        /**
         * Returns the operation type for this map projection.
         */
        protected Class getOperationType() {
            return Projection.class;
        }

        /**
         * Returns {@code true} is the parameters use a spherical datum.
         */
        static boolean isSpherical(final ParameterValueGroup values) {
            try {
                return doubleValue(SEMI_MAJOR, values) ==
                       doubleValue(SEMI_MINOR, values);
            } catch (IllegalStateException exception) {
                // Probably could not find the requested values -- gobble error and be forgiving.
                // The error will probably be thrown at MapProjection construction time, which is
                // less surprising to some users.
                return false;
            }
        }

        /**
         * Returns the parameter value for the specified operation parameter in standard units.
         * Values are automatically converted into the standard units specified by the supplied
         * {@code param} argument, except {@link NonSI#DEGREE_ANGLE degrees} which are converted
         * to {@link SI#RADIAN radians}. This conversion is performed because the radians units
         * are standard for all internal computations in the map projection package. For example
         * they are the standard units for {@link MapProjection#latitudeOfOrigin latitudeOfOrigin}
         * and {@link MapProjection#centralMeridian centralMeridian} fields in the
         * {@link MapProjection} class.
         *
         * @param  param The parameter to look for.
         * @param  group The parameter value group to search into.
         * @return The requested parameter value.
         * @throws ParameterNotFoundException if the parameter is not found.
         */
        protected static double doubleValue(final ParameterDescriptor param,
                                            final ParameterValueGroup group)
                throws ParameterNotFoundException
        {
            double v = MathTransformProvider.doubleValue(param, group);
            if (NonSI.DEGREE_ANGLE.equals(param.getUnit())) {
                v = Math.toRadians(v);
            }
            return v;
        }
    }
}
