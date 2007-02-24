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

// J2SE dependencies and extensions
import java.awt.geom.Point2D;

// OpenGIS dependencies
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterNotFoundException;

// Geotools dependencies
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.i18n.ErrorKeys;


/**
 * The USGS equatorial case of the {@linkplain Stereographic stereographic} projection.
 * This is a special case of oblique stereographic projection for 
 * {@linkplain #latitudeOfOrigin latitude of origin} == 0.0.
 *
 * @since 2.4
 * @source $URL$
 * @version $Id$
 * @author André Gosselin
 * @author Martin Desruisseaux
 * @author Rueben Schulz
 */
public class EquatorialStereographic extends StereographicUSGS {
    /**
     * Maximum difference allowed when comparing real numbers.
     */
    private static final double EPSILON = 1E-6;
    
    /**
     * A constant used in the transformations.
     * This is <strong>not</strong> equal to the {@link #scaleFactor}.
     */
    static final double k0 = 2;

    /**
     * Constructs an equatorial stereographic projection (EPSG equations).
     *
     * @param  parameters The group of parameter values.
     * @throws ParameterNotFoundException if a required parameter was not found.
     */
    protected EquatorialStereographic(final ParameterValueGroup parameters)
            throws ParameterNotFoundException 
    {
        this(parameters, Stereographic.Provider.PARAMETERS);
    }

    /**
     * Constructs an equatorial stereographic projection (USGS equations).
     *
     * @param  parameters The group of parameter values.
     * @param  descriptor The expected parameter descriptor.
     * @throws ParameterNotFoundException if a required parameter was not found.
     */
    EquatorialStereographic(final ParameterValueGroup parameters,
                            final ParameterDescriptorGroup descriptor) 
            throws ParameterNotFoundException
    {
        super(parameters, descriptor);
        assert super.k0 == k0 : super.k0;
        latitudeOfOrigin = 0.0;
    }

    /**
     * Transforms the specified (<var>&lambda;</var>,<var>&phi;</var>) coordinates
     * (units in radians) and stores the result in {@code ptDst} (linear distance
     * on a unit sphere).
     */
    protected Point2D transformNormalized(double x, double y, Point2D ptDst) 
            throws ProjectionException 
    {
        // Compute using oblique formulas, for comparaison later.
        assert (ptDst = super.transformNormalized(x, y, ptDst)) != null;

        final double chi = 2.0 * Math.atan(ssfn(y, Math.sin(y))) - (Math.PI/2);
        final double cosChi = Math.cos(chi);
        final double A = k0 / (1.0 + cosChi*Math.cos(x));    // typo in (12-29)
        x = A * cosChi*Math.sin(x);
        y = A * Math.sin(chi);

        assert checkTransform(x, y, ptDst);
        if (ptDst != null) {
            ptDst.setLocation(x,y);
            return ptDst;
        }
        return new Point2D.Double(x,y);
    }


    /**
     * Provides the transform equations for the spherical case of the 
     * equatorial stereographic projection.
     *
     * @version $Id$
     * @author Martin Desruisseaux
     * @author Rueben Schulz
     */
    static final class Spherical extends EquatorialStereographic {
        /**
         * Constructs a spherical equatorial stereographic projection (USGS equations).
         *
         * @param  parameters The group of parameter values.
         * @param  descriptor The expected parameter descriptor.
         * @throws ParameterNotFoundException if a required parameter was not found.
         */
        Spherical(final ParameterValueGroup parameters, final ParameterDescriptorGroup descriptor)
                throws ParameterNotFoundException
        {
            super(parameters, descriptor);
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
            //Compute using ellipsoidal formulas, for comparaison later.
            assert (ptDst = super.transformNormalized(x, y, ptDst)) != null;

            final double coslat = Math.cos(y);
            double f = 1.0 + coslat*Math.cos(x);
            if (f < EPSILON) {
                throw new ProjectionException(Errors.format(
                          ErrorKeys.VALUE_TEND_TOWARD_INFINITY));
            }
            f = k0 / f;                   // (21-14)
            x = f * coslat * Math.sin(x); // (21-2)
            y = f * Math.sin(y);          // (21-13)

            assert checkTransform(x, y, ptDst);
            if (ptDst != null) {
                ptDst.setLocation(x,y);
                return ptDst;
            }
            return new Point2D.Double(x,y);
        }

        /**
         * Transforms the specified (<var>x</var>,<var>y</var>) coordinate
         * and stores the result in {@code ptDst}.
         */
        protected Point2D inverseTransformNormalized(double x, double y,Point2D ptDst)
                throws ProjectionException 
        {
            // Compute using ellipsoidal formulas, for comparaison later.
            assert (ptDst = super.inverseTransformNormalized(x, y, ptDst)) != null;

            final double rho = Math.sqrt(x*x + y*y);
            if (Math.abs(rho) < EPSILON) {
                y = 0.0;                     // latitudeOfOrigin
                x = 0.0;
            } else {
                final double c = 2.0 * Math.atan(rho / k0);
                final double cosc = Math.cos(c);
                final double sinc = Math.sin(c);
                y = Math.asin(y * sinc/rho); // (20-14)  with phi1=0
                final double t  = x*sinc;
                final double ct = rho*cosc;
                x = (Math.abs(t) < EPSILON && Math.abs(ct) < EPSILON) ? 0.0 : Math.atan2(t, ct);
            }

            assert checkInverseTransform(x, y, ptDst);
            if (ptDst != null) {
                ptDst.setLocation(x,y);
                return ptDst;
            }
            return new Point2D.Double(x,y);
        }
    }
}
