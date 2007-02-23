/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *
 *   (C) 2003-2006, Geotools Project Managment Committee (PMC)
 *   (C) 2001, Institut de Recherche pour le Développement
 *   (C) 2000, Frank Warmerdam
 *   (C) 1999, Fisheries and Oceans Canada
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
 *    This package contains formulas from the PROJ package of USGS.
 *    USGS's work is fully acknowledged here. This derived work has
 *    been relicensed under LGPL with Frank Warmerdam's permission.
 */
package org.geotools.referencing.operation.projection;

// J2SE dependencies
import java.util.Collection;
import java.awt.geom.Point2D;

// OpenGIS dependencies
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.parameter.ParameterNotFoundException;

// Geotools dependencies
import org.geotools.measure.Latitude;
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.i18n.ErrorKeys;


/**
 * Mercator Cylindrical Projection. The parallels and the meridians are straight lines and
 * cross at right angles; this projection thus produces rectangular charts. The scale is true
 * along the equator (by default) or along two parallels equidistant of the equator (if a scale
 * factor other than 1 is used). This projection is used to represent areas close to the equator.
 * It is also often used for maritime navigation because all the straight lines on the chart are
 * <em>loxodrome</em> lines, i.e. a ship following this line would keep a constant azimuth on its
 * compass.
 * <p>
 *
 * This implementation handles both the 1 and 2 stardard parallel cases.
 * For {@code Mercator_1SP} (EPSG code 9804), the line of contact is the equator. 
 * For {@code Mercator_2SP} (EPSG code 9805) lines of contact are symmetrical 
 * about the equator.
 * <p>
 *
 * <strong>References:</strong><ul>
 *   <li>John P. Snyder (Map Projections - A Working Manual,<br>
 *       U.S. Geological Survey Professional Paper 1395, 1987)</li>
 *   <li>"Coordinate Conversions and Transformations including Formulas",<br>
 *       EPSG Guidence Note Number 7, Version 19.</li>
 * </ul>
 *
 * @see <A HREF="http://mathworld.wolfram.com/MercatorProjection.html">Mercator projection on MathWorld</A>
 * @see <A HREF="http://www.remotesensing.org/geotiff/proj_list/mercator_1sp.html">"mercator_1sp" on RemoteSensing.org</A>
 * @see <A HREF="http://www.remotesensing.org/geotiff/proj_list/mercator_2sp.html">"mercator_2sp" on RemoteSensing.org</A>
 * 
 * @since 2.1
 * @source $URL$
 * @version $Id$
 * @author André Gosselin
 * @author Martin Desruisseaux
 * @author Rueben Schulz
 */
public abstract class Mercator extends MapProjection {
    /**
     * Maximum difference allowed when comparing real numbers.
     */
    private static final double EPSILON = 1E-6;
    
    /**
     * Standard Parallel used for the {@link Mercator2SP} case.
     * Set to {@link Double#NaN} for the {@link Mercator1SP} case.
     */
    protected final double standardParallel;

    /**
     * Constructs a new map projection from the supplied parameters.
     *
     * @param  parameters The parameter values in standard units.
     * @throws ParameterNotFoundException if a mandatory parameter is missing.
     */
    protected Mercator(final ParameterValueGroup parameters)
            throws ParameterNotFoundException
    {
        // Fetch parameters 
        super(parameters);
        final Collection expected = getParameterDescriptors().descriptors();
        if (expected.contains(AbstractProvider.STANDARD_PARALLEL)) {
            // scaleFactor is not a parameter in the Mercator_2SP case and is computed from
            // the standard parallel.   The super-class constructor should have initialized
            // 'scaleFactor' to 1. We still use the '*=' operator rather than '=' in case a
            // user implementation still provides a scale factor for its custom projections.
            standardParallel = Math.abs(doubleValue(expected,
                                        AbstractProvider.STANDARD_PARALLEL, parameters));
            ensureLatitudeInRange(AbstractProvider.STANDARD_PARALLEL, standardParallel, false);
            if (isSpherical) {
                scaleFactor *= Math.cos(standardParallel);
            }  else {
                scaleFactor *= msfn(Math.sin(standardParallel),
                                    Math.cos(standardParallel));
            }
            globalScale = scaleFactor*semiMajor;
        } else {
            // No standard parallel. Instead, uses the scale factor explicitely provided.
            standardParallel = Double.NaN;
        }
        assert latitudeOfOrigin == 0 : latitudeOfOrigin;
    }

    /**
     * {@inheritDoc}
     */
    public ParameterValueGroup getParameterValues() {
        final ParameterValueGroup values = super.getParameterValues();
        if (!Double.isNaN(standardParallel)) {
            final Collection expected = getParameterDescriptors().descriptors();
            set(expected, AbstractProvider.STANDARD_PARALLEL, values, standardParallel);
        }
        return values;
    }

    /**
     * Transforms the specified (<var>&lambda;</var>,<var>&phi;</var>) coordinates
     * (units in radians) and stores the result in {@code ptDst} (linear distance
     * on a unit sphere).
     */
    protected Point2D transformNormalized(double x, double y, final Point2D ptDst)
            throws ProjectionException
    {
        if (Math.abs(y) > (Math.PI/2 - EPSILON)) {
            throw new ProjectionException(Errors.format(
                    ErrorKeys.POLE_PROJECTION_$1, new Latitude(Math.toDegrees(y))));
        }

        y = - Math.log(tsfn(y, Math.sin(y)));

        if (ptDst != null) {
            ptDst.setLocation(x,y);
            return ptDst;
        }
        return new Point2D.Double(x,y);
    }
    
    /**
     * Transforms the specified (<var>x</var>,<var>y</var>) coordinates
     * and stores the result in {@code ptDst}.
     */
    protected Point2D inverseTransformNormalized(double x, double y, final Point2D ptDst)
            throws ProjectionException
    {
        y = Math.exp(-y);
        y = cphi2(y);

        if (ptDst != null) {
            ptDst.setLocation(x,y);
            return ptDst;
        }
        return new Point2D.Double(x,y);
    }


    /**
     * Provides the transform equations for the spherical case of the Mercator projection.
     *
     * @version $Id$
     * @author Martin Desruisseaux
     * @author Rueben Schulz
     */
    static abstract class Spherical extends Mercator {
        /**
         * Constructs a new map projection from the suplied parameters.
         *
         * @param  parameters The parameter values in standard units.
         * @throws ParameterNotFoundException if a mandatory parameter is missing.
         */
        protected Spherical(final ParameterValueGroup parameters)
                throws ParameterNotFoundException
        {
            super(parameters);
            ensureSpherical();
        }

        /**
         * Transforms the specified (<var>&lambda;</var>,<var>&phi;</var>) coordinates
         * (units in radians) and stores the result in {@code ptDst} (linear distance
         * on a unit sphere).
         */
        protected Point2D transformNormalized(double x, double y, Point2D ptDst)
                throws ProjectionException
        {
            if (Math.abs(y) > (Math.PI/2 - EPSILON)) {
                throw new ProjectionException(Errors.format(
                        ErrorKeys.POLE_PROJECTION_$1, new Latitude(Math.toDegrees(y))));
            }
            // Compute using ellipsoidal formulas, for comparaison later.
            assert (ptDst = super.transformNormalized(x, y, ptDst)) != null;
          
            y = Math.log(Math.tan((Math.PI/4) + 0.5*y));

            assert checkTransform(x, y, ptDst);
            if (ptDst != null) {
                ptDst.setLocation(x,y);
                return ptDst;
            }
            return new Point2D.Double(x,y);
        }

        /**
         * Transforms the specified (<var>x</var>,<var>y</var>) coordinates
         * and stores the result in {@code ptDst} using equations for a sphere.
         */
        protected Point2D inverseTransformNormalized(double x, double y, Point2D ptDst)
                throws ProjectionException
        {
            // Compute using ellipsoidal formulas, for comparaison later.
            assert (ptDst = super.inverseTransformNormalized(x, y, ptDst)) != null;

            y = (Math.PI/2) - 2.0*Math.atan(Math.exp(-y));

            assert checkInverseTransform(x, y, ptDst);
            if (ptDst != null) {
                ptDst.setLocation(x,y);
                return ptDst;
            }
            return new Point2D.Double(x,y);
        }      
    }


    /**
     * Returns a hash value for this projection.
     */
    public int hashCode() {
        final long code = Double.doubleToLongBits(standardParallel);
        return ((int)code ^ (int)(code >>> 32)) + 37*super.hashCode();
    }

    /**
     * Compares the specified object with this map projection for equality.
     */
    public boolean equals(final Object object) {
        if (object == this) {
            // Slight optimization
            return true;
        }
        if (super.equals(object)) {
            final Mercator that = (Mercator) object;
            return equals(this.standardParallel,  that.standardParallel);
        }
        return false;
    }
}
