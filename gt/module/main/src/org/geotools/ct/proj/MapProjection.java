/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Managment Committee (PMC)
 * (C) 2001, Institut de Recherche pour le D�veloppement
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
package org.geotools.ct.proj;

// J2SE and JAI dependencies
import java.awt.geom.Point2D;
import java.util.Locale;
import java.util.Arrays;
import java.util.Collection;
import java.io.Serializable;
import javax.media.jai.ParameterListDescriptor;

// Geotools dependencies
import org.geotools.measure.Latitude;
import org.geotools.measure.Longitude;
import org.geotools.cs.Projection;

// Resources
import org.geotools.ct.MathTransform;
import org.geotools.ct.MathTransform2D;
import org.geotools.ct.TransformException;
import org.geotools.ct.AbstractMathTransform;
import org.geotools.ct.MissingParameterException;
import org.geotools.resources.cts.Resources;
import org.geotools.resources.cts.ResourceKeys;


/**
 * Base class for transformation services between ellipsoidal and cartographic projections.
 * This base class provides the basic feature needed for all methods (no need to overrides
 * methods). Subclasses must "only" implements the following methods:
 * <ul>
 *   <li>{@link #getName}</li>
 *   <li>{@link #transformNormalized}</li>
 *   <li>{@link #inverseTransformNormalized}</li>
 * </ul>
 * <br><br>
 * <strong>NOTE:</strong>Serialization of this class is appropriate for short-term storage
 * or RMI use, but will probably not be compatible with future version. For long term storage,
 * WKT (Well Know Text) or XML (not yet implemented) are more appropriate.
 *
 * @version $Id$
 * @author Andr� Gosselin
 * @author Martin Desruisseaux
 * @author Rueben Schulz
 *
 * @see <A HREF="http://mathworld.wolfram.com/MapProjection.html">Map projections on MathWorld</A>
 */
public abstract class MapProjection extends AbstractMathTransform implements MathTransform2D,
                                                                             Serializable
{
    /**
     * Maximal error (in metres) tolerated for assertion, if enabled. When assertions are enabled,
     * every direct projection is followed by an inverse projection, and the result is compared to
     * the original coordinate. If a distance greater than <code>MAX_ERROR</code> is found, then an
     * {@link AssertionError} will be thrown.
     */
    private static final double MAX_ERROR = 1;
    
    /**
     * Maximum difference allowed when comparing real numbers.
     */
    static final double EPS = 1.0E-6;
    
    /**
     * Difference allowed in iterative computations.
     */
    static final double TOL = 1E-10;
    
    /**
     * Classification string for this projection (e.g. "Transverse_Mercator").
     */
    private final String classification;
    
    /**
     * The parameter list descriptor.
     */
    private final ParameterListDescriptor descriptor;
    
    /**
     * Ellipsoid excentricity, equals to <code>sqrt({@link #es})</code>.
     * Value 0 means that the ellipsoid is spherical.
     *
     * @see #es
     * @see #isSpherical
     */
    protected final double e;
    
    /**
     * The square of excentricity: e� = (a�-b�)/a� where
     * <var>a</var> is the {@linkplain #semiMajor semi major} axis length and
     * <var>b</var> is the {@linkplain #semiMinor semi minor} axis length.
     *
     * @see #e
     * @see #semiMajor
     * @see #semiMinor
     * @see #isSpherical
     */
    protected final double es;

    /**
     * <code>true</code> if this projection is spherical. Spherical model has identical
     * {@linkplain #semiMajor semi major} and {@linkplain #semiMinor semi minor} axis
     * length, and an {@linkplain #e excentricity} zero.
     *
     * @see #e
     * @see #semiMajor
     * @see #semiMinor
     */
    protected final boolean isSpherical;
    
    /**
     * Length of semi-major axis, in metres. This is named '<var>a</var>' or '<var>R</var>'
     * (Radius in spherical cases) in Snyder.
     *
     * @see #e
     * @see #semiMinor
     */
    protected final double semiMajor;
    
    /**
     * Length of semi-minor axis, in metres. This is named '<var>b</var>' in Snyder.
     *
     * @see #e
     * @see #semiMajor
     */
    protected final double semiMinor;
    
    /**
     * Central longitude in <u>radians</u>. Default value is 0, the Greenwich meridian.
     * This is called '<var>lambda0</var>' in Snyder.
     */
    protected final double centralMeridian;
    
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
     * Global scale factor. Default value <code>globalScale</code> is equal
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
     * Construct a new map projection from the suplied parameters.
     *
     * @param  parameters The parameter values in standard units.
     *         The following parameter are recognized:
     *         <ul>
     *           <li>"semi_major" (mandatory: no default)</li>
     *           <li>"semi_minor" (mandatory: no default)</li>
     *           <li>"central_meridian"   (default to 0�)</li>
     *           <li>"latitude_of_origin" (default to 0�)</li>
     *           <li>"scale_factor"       (default to 1 )</li>
     *           <li>"false_easting"      (default to 0 )</li>
     *           <li>"false_northing"     (default to 0 )</li>
     *         </ul>
     * @throws MissingParameterException if a mandatory parameter is missing.
     */
    protected MapProjection(final Projection parameters) throws MissingParameterException {
        descriptor       =                    parameters.getParameters().getParameterListDescriptor();
        classification   =                    parameters.getClassName();
        semiMajor        =                    parameters.getValue("semi_major");
        semiMinor        =                    parameters.getValue("semi_minor");
        centralMeridian  = longitudeToRadians(parameters.getValue("central_meridian",   0), true);
        latitudeOfOrigin =  latitudeToRadians(parameters.getValue("latitude_of_origin", 0), true);
        scaleFactor      =                    parameters.getValue("scale_factor",       1);
        falseEasting     =                    parameters.getValue("false_easting",      0);
        falseNorthing    =                    parameters.getValue("false_northing",     0);
        isSpherical      = (semiMajor == semiMinor);
        es               = 1.0 - (semiMinor*semiMinor)/(semiMajor*semiMajor);
        e                = Math.sqrt(es);
        globalScale      = scaleFactor*semiMajor;
    }
    
    /**
     * Converts latitudes expressed in degrees to radians. This method
     * verifies that the latitude is within allowed limits (�90�).
     * This method is useful to check the validity of projection parameters,
     * like {@link #setCentralLongitude}.
     *
     * @param  y Latitude, to check, in degrees.
     * @param  edge <code>true</code> to accept latitudes of �90�.
     * @return Latitude in radians.
     * @throws IllegalArgumentException if the latitude is invalide.
     */
    static double latitudeToRadians(final double y, boolean edge) throws IllegalArgumentException {
        if (edge ? (y>=Latitude.MIN_VALUE && y<=Latitude.MAX_VALUE) :
                   (y> Latitude.MIN_VALUE && y< Latitude.MAX_VALUE))
        {
            return Math.toRadians(y);
        }
        throw new IllegalArgumentException(Resources.format(
                ResourceKeys.ERROR_LATITUDE_OUT_OF_RANGE_$1, new Latitude(y)));
    }
    
    /**
     * Converts longitudes expressed in degrees to radians. This method
     * verifies that the longitue is within allowed limits (�180�).
     * This method is used to check the validity of projection parameters,
     * like {@link #setCentralLongitude}.
     *
     * @param  x Longitude, to verify, in degrees.
     * @param  edge <code>true</code> for accepting longitudes of �180�.
     * @return Longitude in radians.
     * @throws IllegalArgumentException if a longitude is invalide.
     */
    static double longitudeToRadians(final double x, boolean edge) throws IllegalArgumentException {
        if (edge ? (x>=Longitude.MIN_VALUE && x<=Longitude.MAX_VALUE) :
                   (x> Longitude.MIN_VALUE && x< Longitude.MAX_VALUE))
        {
            return Math.toRadians(x);
        }
        throw new IllegalArgumentException(Resources.format(
                ResourceKeys.ERROR_LONGITUDE_OUT_OF_RANGE_$1, new Longitude(x)));
    }

    /**
     * Makes sure that the specified longitude stay within �180 degrees. This methpod should be
     * invoked after {@link #centralMeridian} had been added or removed to <var>x</var>. This
     * method may add or substract an amount of 360� to <var>x</var>.
     *
     * As a special case, we do not check the range if no rotation were applied on <var>x</var>.
     * This is because the user may have a big area ranging from -180� to +180�. With the slight
     * rounding errors related to map projections, the 180� longitude may be slightly over the
     * limit. Doing the check would changes its sign. For example a bounding box from 30� to +180�
     * would become 30� to -180�, which is probably not what the user wanted.
     *
     * @param  x The longitude.
     * @return The longitude in the range +/- 180�.
     */
    final double ensureInRange(double x) {
        if (centralMeridian != 0) {
            if (x > Math.PI) {
                x -= 2*Math.PI;
            } else if (x < -Math.PI) {
                x += 2*Math.PI;
            }
        }
        return x;
    }

    /**
     * Returns <code>true</code> if the classification for the specified projection
     * contains the specified word. The word must be delimited by "_" character or
     * end of line. Search is case-insensitive.
     */
    static boolean contains(final Projection projection, final String word) {
        final String name = projection.getClassName().trim();
        final int  length = word.length();
        int index = 0;
        while ((index = name.indexOf('_', index)) >= 0) {
            if (name.regionMatches(true, ++index, word, 0, length)) {
                return true;
            }
        }
        return true;
    }
    
    /**
     * Returns a human readable name localized for the specified locale.
     */
    public abstract String getName(final Locale locale);

    
    /**
     * Gets the dimension of input points.
     */
    public final int getDimSource() {
        return 2;
    }
    
    /**
     * Gets the dimension of output points.
     */
    public final int getDimTarget() {
        return 2;
    }
    
    
    
    
    //////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////
    ////////                                                                          ////////
    ////////                          TRANSFORMATION METHODS                          ////////
    ////////                                                                          ////////
    ////////             Includes an inner class for inverse projections.             ////////
    ////////                                                                          ////////
    //////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////
    
    /**
     * Check point for private use by {@link #checkTransform}. This class is necessary in order
     * to avoid never-ending loop in <code>assert</code> statements (when an <code>assert</code>
     * calls <code>transform(...)</code>, which calls <code>inverse.transform(...)</code>, which
     * calls <code>transform(...)</code>, etc.).
     */
    private static final class CheckPoint extends Point2D.Double {
        public CheckPoint(final Point2D point) {
            super(point.getX(), point.getY());
        }
    }
    
    /**
     * Check if the transform of <code>point</code> is close enough to <code>target</code>.
     * "Close enough" means that the two points are separated by a distance shorter than
     * {@link #MAX_ERROR}. This method is used for assertions with JDK 1.4.
     *
     * @param  point  Point to transform, in degrees if <code>inverse</code> is false.
     * @param  target Point to compare to, in metres if <code>inverse</code> is false.
     * @param inverse <code>true</code> for an inverse transform instead of a direct one.
     * @return <code>true</code> if the two points are close enough.
     * @throws ProjectionException if a transformation failed.
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
            // Be less strict when the point is near an edge.
            final boolean edge = (Math.abs(longitude) > 179) || (Math.abs(latitude) > 89);
            if (distance > (edge ? 5*MAX_ERROR : MAX_ERROR)) { // Do not fail for NaN values.
                throw new AssertionError(distance);
            }
        } catch (TransformException exception) {
            final AssertionError error = new AssertionError(exception.getLocalizedMessage());
            error.initCause(exception);
            throw error;
        }
        return true;
    }
    
    /**
     * Transforms the specified coordinate and stores the result in <code>ptDst</code>.
     * This method returns longitude as <var>x</var> values in the range <code>[-PI..PI]</code>
     * and latitude as <var>y</var> values in the range <code>[-PI/2..PI/2]</code>. It will be
     * checked by the caller, so this method doesn't need to performs this check.
     * <br><br>
     *
     * Input coordinates are also guarenteed to have the {@link #falseEasting} 
     * and {@link #falseNorthing} removed and be divided by {@link #globalScale}
     * before this method is invoked. After this method is invoked, the 
     * {@link #centralMeridian} is added to the <code>x</code> results 
     * in <code>ptDst</code>. This means that projections that implement this method 
     * are performed on an ellipse (or sphere) with a semiMajor axis of 1.0.
     * <br><br>
     *
     * In <A HREF="http://www.remotesensing.org/proj/">PROJ.4</A>, the same
     * standardization, described above, is handled by <code>pj_inv.c</code>.
     * Therefore when porting projections from PROJ.4, the inverse transform
     * equations can be used directly here with minimal change.
     * In the equations of Snyder, {@link #falseEasting}, {@link #falseNorthing}
     * and {@link #scaleFactor} are usually not given.
     * When implementing these equations here, you will not
     * need to add the {@link #centralMeridian} to the output longitude or remove the
     * {@link #majorAxis} ('<var>a</var>' or '<var>R</var>').
     *
     * @param x     The easting of the coordinate, linear distance on a unit sphere or ellipse.
     * @param y     The northing of the coordinate, linear distance on a unit sphere or ellipse.
     * @param ptDst the specified coordinate point that stores the result of transforming
     *              <code>ptSrc</code>, or <code>null</code>. Ordinates will be in
     *              <strong>radians</strong>.
     * @return      the coordinate point after transforming <code>x</code>, <code>y</code> 
     *              and storing the result in <code>ptDst</code>.
     * @throws ProjectionException if the point can't be transformed.
     *
     * @revisit TODO: The <code>ptDst</code> argument will be removed and the return type changed if
     * RFE <A HREF="http://developer.java.sun.com/developer/bugParade/bugs/4222792.html">4222792</A>
     * is implemented efficiently in a future J2SE release (maybe J2SE 1.5?).
     */
    protected abstract Point2D inverseTransformNormalized(double x, double y, final Point2D ptDst)
            throws ProjectionException;
    
    /**
     * Transforms the specified coordinate and stores the result in <code>ptDst</code>.
     * This method is guaranteed to be invoked with values of <var>x</var> in the range
     * <code>[-PI..PI]</code> and values of <var>y</var> in the range <code>[-PI/2..PI/2]</code>.
     * <br><br>
     * 
     * Coordinates are also guaranteed to have the {@link #centralMeridian} 
     * removed from <var>x</var> before this method is invoked. After this method 
     * is invoked, the results in <code>ptDst</code> are multiplied by {@link #globalScale},
     * and the {@link #falseEasting} and {@link #falseNorthing} are added.
     * This means that projections that implement this method are performed on an
     * ellipse (or sphere) with a semiMajor axis of 1.0. 
     * <br><br>
     *
     * In <A HREF="http://www.remotesensing.org/proj/">PROJ.4</A>, the same
     * standardization, described above, is handled by <code>pj_fwd.c</code>.
     * Therefore when porting projections from PROJ.4, the forward transform equations can
     * be used directly here with minimal change. In the equations of Snyder,
     * {@link #falseEasting}, {@link #falseNorthing} and {@link #scaleFactor}
     * are usually not given. When implementing these equations here, you will not
     * need to remove the {@link #centralMeridian} from <var>x</var> or apply the
     * {@link #majorAxis} ('<var>a</var>' or '<var>R</var>').
     *
     * @param x     The longitude of the coordinate, in <strong>radians</strong>.
     * @param y     The  latitude of the coordinate, in <strong>radians</strong>.
     * @param ptDst the specified coordinate point that stores the result of transforming
     *              <code>ptSrc</code>, or <code>null</code>. Ordinates will be in a
     *              dimensionless unit, as a linear distance on a unit sphere or ellipse.
     * @return      the coordinate point after transforming <code>x</code>, <code>y</code>
     *              and storing the result in <code>ptDst</code>.
     * @throws ProjectionException if the point can't be transformed.
     *
     * @revisit TODO: The <code>ptDst</code> argument will be removed and the return type changed if
     * RFE <A HREF="http://developer.java.sun.com/developer/bugParade/bugs/4222792.html">4222792</A>
     * is implemented efficiently in a future J2SE release (maybe J2SE 1.5?).
     */
    protected abstract Point2D transformNormalized(double x, double y, final Point2D ptDst)
            throws ProjectionException;
    
    /**
     * Transforms the specified <code>ptSrc</code> and stores the result in <code>ptDst</code>.
     * <br><br>
     *
     * This method standardizes the source <code>x</code> coordinate
     * by removing the {@link #centralMeridian}, before invoking
     * <code>{@link #transformNormalized transformNormalized}(x, y, ptDst)</code>.
     * It also multiplies by {@link #globalScale} and adds the {@link #falseEasting} and
     * {@link #falseNorthing} to the point returned by the <code>transformNormalized(...)</code>
     * call.
     *
     * @param ptSrc the specified coordinate point to be transformed. Ordinates must be in degrees.
     * @param ptDst the specified coordinate point that stores the result of transforming
     *              <code>ptSrc</code>, or <code>null</code>. Ordinates will be in metres.
     * @return      the coordinate point after transforming <code>ptSrc</code> and storing
     *              the result in <code>ptDst</code>.
     * @throws ProjectionException if the point can't be transformed.
     */
    public final Point2D transform(final Point2D ptSrc, Point2D ptDst) throws ProjectionException {
        final double x = ptSrc.getX();
        final double y = ptSrc.getY();

        if (x<Longitude.MIN_VALUE-EPS || x>Longitude.MAX_VALUE+EPS) { // Do not fail for NaN values.
            throw new PointOutsideEnvelopeException(Resources.format(
                    ResourceKeys.ERROR_LONGITUDE_OUT_OF_RANGE_$1, new Longitude(x)));
        }
        if (y<Latitude.MIN_VALUE-EPS || y>Latitude.MAX_VALUE+EPS) { // Do not fail for NaN values.
            throw new PointOutsideEnvelopeException(Resources.format(
                    ResourceKeys.ERROR_LATITUDE_OUT_OF_RANGE_$1, new Latitude(y)));
        }

        ptDst = transformNormalized(ensureInRange(Math.toRadians(x) - centralMeridian),
                                                  Math.toRadians(y), ptDst);
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
         * V�rifie s'il faudra parcourir le tableau en sens inverse.
         * Ce sera le cas si les tableaux source et destination se
         * chevauchent et que la destination est apr�s la source.
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
     * first required. Implementation of <code>transform(...)</code> methods are mostly identical
     * to <code>MapProjection.transform(...)</code>, except that they will invokes
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
         * Inverse transforms the specified <code>ptSrc</code>
         * and stores the result in <code>ptDst</code>.
         * <br><br>
         *
         * This method standardizes the <code>ptSrc</code> by removing the 
         * {@link #falseEasting} and {@link #falseNorthing} and dividing by 
         * {@link #globalScale} before invoking 
         * <code>{@link #inverseTransformNormalized inverseTransformNormalized}(x, y, ptDst)</code>.
         * It then adds the {@link #centralMeridian} to the <code>x</code> of the
         * point returned by the <code>inverseTransformNormalized</code> call.
         *
         * @param ptSrc the specified coordinate point to be transformed.
         *              Ordinates must be in metres.
         * @param ptDst the specified coordinate point that stores the
         *              result of transforming <code>ptSrc</code>, or
         *              <code>null</code>. Ordinates will be in degrees.
         * @return the coordinate point after transforming <code>ptSrc</code>
         *         and stroring the result in <code>ptDst</code>.
         * @throws ProjectionException if the point can't be transformed.
         */
        public final Point2D transform(final Point2D ptSrc, Point2D ptDst)
                throws ProjectionException
        {
            final double x0 = ptSrc.getX();
            final double y0 = ptSrc.getY();

            ptDst = inverseTransformNormalized((x0 - falseEasting )/globalScale,
                                               (y0 - falseNorthing)/globalScale, ptDst);
            final double x = Math.toDegrees(ensureInRange(ptDst.getX() + centralMeridian));
            final double y = Math.toDegrees(              ptDst.getY());
            ptDst.setLocation(x,y);

            if (x<Longitude.MIN_VALUE-EPS || x>Longitude.MAX_VALUE+EPS) { // Accept NaN values.
                throw new PointOutsideEnvelopeException(Resources.format(
                        ResourceKeys.ERROR_LONGITUDE_OUT_OF_RANGE_$1, new Longitude(x)));
            }
            if (y<Latitude.MIN_VALUE-EPS || y>Latitude.MAX_VALUE+EPS) { // Accept NaN values.
                throw new PointOutsideEnvelopeException(Resources.format(
                        ResourceKeys.ERROR_LATITUDE_OUT_OF_RANGE_$1, new Latitude(y)));
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
             * V�rifie s'il faudra parcourir le tableau en sens inverse.
             * Ce sera le cas si les tableaux source et destination se
             * chevauchent et que la destination est apr�s la source.
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
    
    
    
    
    //////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////
    ////////                                                                          ////////
    ////////      IMPLEMENTATION OF Object AND MathTransform2D STANDARD METHODS       ////////
    ////////                                                                          ////////
    //////////////////////////////////////////////////////////////////////////////////////////
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
     * Compares the specified object with
     * this map projection for equality.
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
     * Returns <code>true</code> if the two specified value are equals.
     * Two {@link Double#NaN NaN} values are considered equals.
     */
    static boolean equals(final double value1, final double value2) {
        return Double.doubleToLongBits(value1) == Double.doubleToLongBits(value2);
    }

    /**
     * Retourne une cha�ne de caract�res repr�sentant cette projection cartographique.
     * Cette cha�ne de caract�res contiendra entre autres le nom de la projection, les
     * coordonn�es du centre et celles de l'origine.
     *
     * @task REVISIT: part of the implementation is identical to the package-private method
     *       <code>AbstractMathTransform.paramMT(String)</code>.  We should consider moving
     *       it in a formatter class, probably close to WKTParser.
     */
    public final String toString() {
        final StringBuffer buffer = new StringBuffer("PARAM_MT[\"");
        buffer.append(classification);
        buffer.append('"');
        toString(buffer);
        buffer.append(']');
        return buffer.toString();
    }
    
    /**
     * Complete the WKT for this map projection.
     */
    void toString(final StringBuffer buffer) {
        final Collection names = Arrays.asList(descriptor.getParamNames());
        addParameter(names, buffer, "semi_major",         semiMajor);
        addParameter(names, buffer, "semi_minor",         semiMinor);
        addParameter(names, buffer, "central_meridian",   Math.toDegrees(centralMeridian));
        addParameter(names, buffer, "latitude_of_origin", Math.toDegrees(latitudeOfOrigin));
        addParameter(names, buffer, "scale_factor",       scaleFactor);
        addParameter(names, buffer, "false_easting",      falseEasting);
        addParameter(names, buffer, "false_northing",     falseNorthing);
    }
    
    /**
     * Add the <code>", PARAMETER["<name>", <value>]"</code> string
     * to the specified string buffer. This is a convenience method
     * for constructing WKT for "PARAM_MT".
     */
    private static void addParameter(final Collection   names,
                                     final StringBuffer buffer,
                                     final String       key,
                                     final double       value)
    {
        if (names.contains(key)) {
            addParameter(buffer, key, value);
        }
    }
    
    /**
     * Add the <code>", PARAMETER["<name>", <value>]"</code> string
     * to the specified string buffer. This is a convenience method
     * for constructing WKT for "PARAM_MT".
     *
     * @task REVISIT: part of the implementation is identical to the package-private method
     *       <code>AbstractMathTransform.addParameter(StringBuffer, String, double)</code>.
     *       We should consider moving it in a formatter class, probably close to WKTParser.
     */
    static void addParameter(final StringBuffer buffer, final String key, final double value) {
        buffer.append(", PARAMETER[\"");
        buffer.append(key);
        buffer.append("\",");
        buffer.append(value);
        buffer.append(']');
    }
    
    
    
    
    //////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////
    ////////                                                                          ////////
    ////////                           FORMULAS FROM SNYDER                           ////////
    ////////                                                                          ////////
    //////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////
    
    /**
     * Iteratively solve equation (7-9) from Snyder.
     */
    final double cphi2(final double ts) throws ProjectionException {
        final double eccnth = 0.5*e;
        double phi = (Math.PI/2) - 2.0*Math.atan(ts);
        for (int i=0; i<16; i++) {
            final double con  = e*Math.sin(phi);
            final double dphi = (Math.PI/2) - 2.0*Math.atan(ts * Math.pow((1-con)/(1+con), eccnth)) - phi;
            phi += dphi;
            if (Math.abs(dphi) <= TOL) {
                return phi;
            }
        }
        throw new ProjectionException(Resources.format(ResourceKeys.ERROR_NO_CONVERGENCE));
    }
    
    /**
     * Compute function <code>f(s,c,es) = c/sqrt(1 - s�*es)</code>
     * needed for the true scale latitude (Snyder 14-15), where
     * <var>s</var> and <var>c</var> are the sine and cosine of
     * the true scale latitude, and {@link #es} the eccentricity
     * squared.
     */
    final double msfn(final double s, final double c) {
        return c / Math.sqrt(1.0 - s*s*es);
    }
    
    /**
     * Compute function (15-9) from Snyder equivalent to negative of function (7-7).
     */
    final double tsfn(final double phi, double sinphi) {
        sinphi *= e;
        /*
         * NOTE: change sign to get the equivalent of Snyder (7-7).
         */
        return Math.tan(0.5 * ((Math.PI/2) - phi)) /
               Math.pow((1-sinphi)/(1+sinphi), 0.5*e);
    }
}
