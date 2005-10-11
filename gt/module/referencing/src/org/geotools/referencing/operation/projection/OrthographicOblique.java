/*
 * Geotools - OpenSource mapping toolkit
 * (C) 2004 Geotools Project Managment Committee (PMC)
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
/* 
 * Some parts Copyright (c) 2000, Frank Warmerdam
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
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
 * The oblique case of the {@link Orthographic} projection. Only the spherical
 * form is given here.
 *
 * @since 2.1
 * @version $Id$
 * @author Rueben Schulz
 */
public class OrthographicOblique extends Orthographic {
    /**
     * The sine of the {@link #latitudeOfOrigin}.
     */
    private final double sinphi0; 
    
    /**
     * The cosine of the {@link #latitudeOfOrigin}.
     */
    private final double cosphi0;
    
    /**
     * Constructs an oblique orthographic projection.
     *
     * @param  parameters The parameter values in standard units.
     * @param  expected The expected parameter descriptors.
     * @throws ParameterNotFoundException if a mandatory parameter is missing.
     */
    OrthographicOblique(final ParameterValueGroup parameters, final Collection expected) 
            throws ParameterNotFoundException
    {
        super(parameters, expected);
        sinphi0 = Math.sin(latitudeOfOrigin);
        cosphi0 = Math.cos(latitudeOfOrigin);
        assert isSpherical;
    }
    
    /**
     * Transforms the specified (<var>x</var>,<var>y</var>) coordinate (units in radians)
     * and stores the result in {@code ptDst} (linear distance on a unit sphere).
     */
    protected Point2D transformNormalized(double x, double y, final Point2D ptDst)
            throws ProjectionException
    {
        double cosphi = Math.cos(y);
        double coslam = Math.cos(x);
        double sinphi = Math.sin(y);
        
        if (sinphi0*sinphi + cosphi0*cosphi*coslam < - EPS) {
            throw new ProjectionException(Errors.format(ErrorKeys.POINT_OUTSIDE_HEMISPHERE));
        }
        
        y = cosphi0 * sinphi - sinphi0 * cosphi * coslam;      
        x = cosphi * Math.sin(x);
        
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
    protected Point2D inverseTransformNormalized(double x, double y, final Point2D ptDst)
            throws ProjectionException
    {
        final double rho = Math.sqrt(x*x + y*y);
        double sinc = rho;
        if (sinc > 1.0) {
            if ((sinc - 1.0) > EPS) {
                throw new ProjectionException(Errors.format(ErrorKeys.POINT_OUTSIDE_HEMISPHERE));
            }
            sinc = 1.0;
        }
        
        double cosc = Math.sqrt(1.0 - sinc * sinc); /* in this range OK */
        if (rho <= EPS) {
            y = latitudeOfOrigin;
            x = 0.0;
        } else {
            double phi = (cosc * sinphi0) + (y * sinc * cosphi0 / rho);
            y = (cosc - sinphi0 * phi) * rho;       //rather clever; equivalent to part of (20-15)
            x *= sinc * cosphi0;
            
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
        
        if (ptDst != null) {
            ptDst.setLocation(x,y);
            return ptDst;
        }
        return new Point2D.Double(x,y);
    }
}
