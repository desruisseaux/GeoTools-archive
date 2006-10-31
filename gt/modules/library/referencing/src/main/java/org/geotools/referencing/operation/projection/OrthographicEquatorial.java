/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *
 *   (C) 2004-2006, Geotools Project Managment Committee (PMC)
 *   (C) 2000, Frank Warmerdam
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
import java.util.Collection;

// OpenGIS dependencies
import org.opengis.parameter.ParameterNotFoundException;
import org.opengis.parameter.ParameterValueGroup;

// Geotools dependencies
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.i18n.ErrorKeys;


/**
 * The equatorial case of the {@link Orthographic} projection. This is a 
 * simplification of the oblique case for {@link #latitudeOfOrigin} == 0.0. 
 * Only the spherical form is given here.
 *
 * @todo this code is identical to the oblique except for 6 lines.
 *       It could be moved to the oblique with an isEquatorial flag.
 *
 * @since 2.1
 * @source $URL$
 * @version $Id$
 * @author Rueben Schulz
 */
public class OrthographicEquatorial extends OrthographicOblique {
    /**
     * Maximum difference allowed when comparing real numbers.
     */
    private static final double EPSILON = 1E-6;
    
    /**
     * Constructs an equatorial orthographic projection.
     *
     * @param  parameters The parameter values in standard units.
     * @param  expected The expected parameter descriptors.
     * @throws ParameterNotFoundException if a mandatory parameter is missing.
     */
    OrthographicEquatorial(final ParameterValueGroup parameters, final Collection expected) 
            throws ParameterNotFoundException
    {
        super(parameters, expected);
        assert (latitudeOfOrigin < EPSILON) : latitudeOfOrigin;
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

        double cosphi = Math.cos(y);
        double coslam = Math.cos(x);

        if (cosphi * coslam < -EPSILON) {
            throw new ProjectionException(Errors.format(ErrorKeys.POINT_OUTSIDE_HEMISPHERE));
        }

        y = Math.sin(y);
        x = cosphi * Math.sin(x);   

        assert checkTransform(x, y, ptDst);
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
    protected Point2D inverseTransformNormalized(double x, double y, Point2D ptDst)
            throws ProjectionException
    {
        // Compute using oblique formulas, for comparaison later.
        assert (ptDst = super.inverseTransformNormalized(x, y, ptDst)) != null;

        final double rho = Math.sqrt(x*x + y*y);
        double sinc = rho;
        if (sinc > 1.0) {
            if ((sinc - 1.0) > EPSILON) {
                throw new ProjectionException(Errors.format(ErrorKeys.POINT_OUTSIDE_HEMISPHERE));
            }
            sinc = 1.0;
        }

        double cosc = Math.sqrt(1.0 - sinc * sinc); /* in this range OK */
        if (rho <= EPSILON) {
            y = latitudeOfOrigin;
            x = 0.0;
        } else {
            double phi = y * sinc / rho;
            x *= sinc;
            y = cosc * rho;

            //begin sinchk
            if (Math.abs(phi) >= 1.0) {
                phi = (phi < 0.0) ? -Math.PI/2.0 : Math.PI/2.0;
            }
            else {
                phi = Math.asin(phi);
            }
            //end sinchk

            if (y == 0.0) {
                if(x == 0.0) {
                    x = 0.0;
                } else {
                    x = (x < 0.0) ? -Math.PI/2.0 : Math.PI/2.0;
                }
            } else {
                x = Math.atan2(x, y);
            }
            y = phi;
        }

        assert checkInverseTransform(x, y, ptDst);
        if (ptDst != null) {
            ptDst.setLocation(x,y);
            return ptDst;
        }
        return new Point2D.Double(x,y);
    }
}
