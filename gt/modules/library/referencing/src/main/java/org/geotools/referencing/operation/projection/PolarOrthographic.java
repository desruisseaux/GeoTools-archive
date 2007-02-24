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

// OpenGIS dependencies
import org.opengis.parameter.ParameterNotFoundException;
import org.opengis.parameter.ParameterValueGroup;

// Geotools dependencies
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.i18n.ErrorKeys;


/**
 * The polar case of the {@link Orthographic} projection. Only the spherical
 * form is given here.
 *
 * @since 2.4
 * @source $URL$
 * @version $Id$
 * @author Rueben Schulz
 */
public class PolarOrthographic extends Orthographic {
    /**
     * Maximum difference allowed when comparing real numbers.
     */
    private static final double EPSILON = 1E-6;

    /**
     * {@code true} if this projection is for the north pole, or {@code false}
     * if it is for the south pole.
     */
    private final boolean northPole;

    /**
     * Constructs a polar orthographic projection.
     *
     * @param  parameters The parameter values in standard units.
     * @throws ParameterNotFoundException if a mandatory parameter is missing.
     */
    protected PolarOrthographic(final ParameterValueGroup parameters) 
            throws ParameterNotFoundException
    {
        super(parameters);
        ensureLatitudeEquals(Provider.LATITUDE_OF_ORIGIN, latitudeOfOrigin, Math.PI/2);
        northPole = (latitudeOfOrigin > 0);
        latitudeOfOrigin = (northPole) ? Math.PI/2.0 : -Math.PI/2.0;
        ensureSpherical();
    }

    /**
     * Transforms the specified (<var>&lambda;</var>,<var>&phi;</var>) coordinates
     * (units in radians) and stores the result in {@code ptDst} (linear distance
     * on a unit sphere).
     */
    protected Point2D transformNormalized(double x, double y, final Point2D ptDst)
            throws ProjectionException
    {
        if (Math.abs(y - latitudeOfOrigin) - EPSILON > Math.PI/2.0) {
            throw new ProjectionException(Errors.format(ErrorKeys.POINT_OUTSIDE_HEMISPHERE));
        }
        double cosphi = Math.cos(y);
        double coslam = Math.cos(x);
        if (northPole) {
            coslam = -coslam;
        }
        y = cosphi * coslam;
        x = cosphi * Math.sin(x);

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
        final double rho = Math.sqrt(x*x + y*y);
        double sinc = rho;
        if (sinc > 1.0) {
            if ((sinc - 1.0) > EPSILON) {
                throw new ProjectionException(Errors.format(ErrorKeys.POINT_OUTSIDE_HEMISPHERE));
            }
            sinc = 1.0;
        }
        if (rho <= EPSILON) {
            y = latitudeOfOrigin;
            x = 0.0;
        } else {
            double phi;
            if (northPole) {
                y = -y;
                phi = Math.acos(sinc);   // equivalent to asin(cos(c)) over the range [0:1]
            } else {
                phi = -Math.acos(sinc);
            }
            x = Math.atan2(x, y);
            y = phi;
        }
        if (ptDst != null) {
            ptDst.setLocation(x,y);
            return ptDst;
        }
        return new Point2D.Double(x,y);
    }
}
