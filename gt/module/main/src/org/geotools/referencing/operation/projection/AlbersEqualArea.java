/*
 * Geotools - OpenSource mapping toolkit
 * (C) 2003, 2004 Geotools Project Managment Committee (PMC)
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
 *****************************************************************************
 * Some parts Copyright (c) 1995, Gerald Evenden
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
 ******************************************************************************
 */
package org.geotools.referencing.operation.projection;

// J2SE dependencies and extensions
import java.awt.geom.Point2D;
import java.util.Collection;
import javax.units.NonSI;

// OpenGIS dependencies
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterNotFoundException;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.operation.ConicProjection;
import org.opengis.referencing.operation.MathTransform;

// Geotools dependencies
import org.geotools.measure.Latitude;
import org.geotools.metadata.citation.Citation;
import org.geotools.referencing.Identifier;
import org.geotools.resources.cts.ResourceKeys;
import org.geotools.resources.cts.Resources;


/**
 * Albers Equal Area Projection (EPSG code 9822). This is a conic projection
 * with parallels being unequally spaced arcs of concentric circles, more
 * closely spaced at north and south edges of the map. Merideans
 * are equally spaced radii of the same circles and intersect parallels at right 
 * angles. As the name implies, this projection minimizes distortion in areas.
 * <br><br>
 *
 * The "standard_parallel_2" parameter is optional and will be given the 
 * same value as "standard_parallel_1" if not set (creating a 1 standard parallel
 * projection). 
 * <br><br>
 *
 * NOTE: formulae used below are from a port, to java, of the 
 *       'proj4' package of the USGS survey. USGS work is acknowledged here.
 * <br><br>
 *
 * <strong>References:</strong><ul>
 *   <li> Proj-4.4.7 available at <A HREF="http://www.remotesensing.org/proj">www.remotesensing.org/proj</A><br>
 *        Relevent files are: PJ_aea.c, pj_fwd.c and pj_inv.c </li>
 *   <li> John P. Snyder (Map Projections - A Working Manual,
 *        U.S. Geological Survey Professional Paper 1395, 1987)</li>
 *   <li> "Coordinate Conversions and Transformations including Formulas",
 *        EPSG Guidence Note Number 7, Version 19.</li>
 * </ul>
 *
 * @see <A HREF="http://mathworld.wolfram.com/AlbersEqual-AreaConicProjection.html/">Albers Equal-Area Conic Projection on MathWorld</A>
 * @see <A HREF="http://www.remotesensing.org/geotiff/proj_list/albers_equal_area_conic.html"> "Albers_Conic_Equal_Area" on www.remotesensing.org</A>
 * @see <A HREF="http://srmwww.gov.bc.ca/gis/bceprojection.html">British Columbia Albers Standard Projection</A>
 *
 * @version $Id$
 * @author Rueben Schulz
 */
public class AlbersEqualArea extends MapProjection {       
    /**
     * Constants used by the spherical and elliptical Albers projection. 
     */
    private final double n, c, rho0;
    
    /**
     * An error condition indicating itteration will not converge for the 
     * inverse ellipse. See Snyder (14-20)
     */
    private final double ec;
    
    /**
     * Standards parallel 1 in radians, for {@link #getParameterValues} implementation.
     */
    private final double phi1;

    /**
     * Standards parallel 2 in radians, for {@link #getParameterValues} implementation.
     */
    private double phi2;
    
    /**
     * The {@link org.geotools.referencing.operation.MathTransformProvider}
     * for a {@link AlbersEqualArea} projection.
     *
     * @see org.geotools.referencing.operation.MathTransformFactory
     *
     * @version $Id$
     * @author Rueben Schulz
     */
    public static final class Provider extends AbstractProvider {
        /**
         * The operation parameter descriptor for the {@link #phi1 standard parallel 1}
         * parameter value. Valid values range is from -90 to 90�. Default value is 0.
         */
        public static final ParameterDescriptor STANDARD_PARALLEL_1 = createDescriptor(
                new Identifier[] {
                    new Identifier(Citation.OPEN_GIS, "standard_parallel_1"),
                    new Identifier(Citation.EPSG,     "Latitude of 1st standard parallel"),
                    new Identifier(Citation.GEOTIFF,  "StdParallel1")
                },
                0, -90, 90, NonSI.DEGREE_ANGLE);
                
        /**
         * The operation parameter descriptor for the {@link #phi2 standard parallel 2}
         * parameter value. Valid values range is from -90 to 90�. Default value is 0.
         */
        public static final ParameterDescriptor STANDARD_PARALLEL_2 = createOptionalDescriptor(
                new Identifier[] {
                    new Identifier(Citation.OPEN_GIS, "standard_parallel_2"),
                    new Identifier(Citation.EPSG,     "Latitude of 2nd standard parallel"),
                    new Identifier(Citation.GEOTIFF,  "StdParallel2")
                },
                -90, 90, NonSI.DEGREE_ANGLE);

        /**
         * The parameters group.
         */
        static final ParameterDescriptorGroup PARAMETERS = createDescriptorGroup(new Identifier[] {
                new Identifier(Citation.OPEN_GIS, "Albers_Conic_Equal_Area"),
                new Identifier(Citation.EPSG,     "Albers Equal Area"),
                new Identifier(Citation.EPSG,     "9822"),
                new Identifier(Citation.GEOTIFF,  "CT_AlbersEqualArea"),
                new Identifier(Citation.ESRI,     "Albers"),
                new Identifier(Citation.ESRI,     "Albers Equal Area Conic"),
                new Identifier(Citation.GEOTOOLS, Resources.formatInternational(
                                                  ResourceKeys.ALBERS_EQUAL_AREA_PROJECTION))
            }, new ParameterDescriptor[] {
                SEMI_MAJOR,          SEMI_MINOR,
                CENTRAL_MERIDIAN,    LATITUDE_OF_ORIGIN,
                STANDARD_PARALLEL_1, STANDARD_PARALLEL_2,
                FALSE_EASTING,       FALSE_NORTHING
            });

        /**
         * Construct a new provider. 
         */
        public Provider() {
            super(PARAMETERS);
        }

        /**
         * Returns the operation type for this map projection.
         */
        protected Class getOperationType() {
            return ConicProjection.class;
        }

        /**
         * Creates a transform from the specified group of parameter values.
         *
         * @param  parameters The group of parameter values.
         * @return The created math transform.
         * @throws ParameterNotFoundException if a required parameter was not found.
         */
        public MathTransform createMathTransform(final ParameterValueGroup parameters)
                throws ParameterNotFoundException
        {
            final Collection descriptors = PARAMETERS.descriptors();
            return new AlbersEqualArea(parameters, descriptors);
        }
    }
    
    
    /**
     * Construct a new map projection from the supplied parameters.
     *
     * @param  parameters The parameter values in standard units.
     * @param  expected The expected parameter descriptors.
     * @throws ParameterNotFoundException if a mandatory parameter is missing.
     */
    AlbersEqualArea(final ParameterValueGroup parameters, final Collection expected) 
            throws ParameterNotFoundException
    {
        //Fetch parameters 
        super(parameters, expected);

        phi1 = doubleValue(expected, Provider.STANDARD_PARALLEL_1, parameters);
        ensureLatitudeInRange(       Provider.STANDARD_PARALLEL_1, phi1, true);
        phi2 = doubleValue(expected, Provider.STANDARD_PARALLEL_2, parameters);
        if (Double.isNaN(phi2)) {
            phi2 = phi1;
        }
        ensureLatitudeInRange(Provider.STANDARD_PARALLEL_2, phi2, true);

	//Compute Constants
        if (Math.abs(phi1 + phi2) < EPS) 
            throw new IllegalArgumentException(Resources.format(
                    ResourceKeys.ERROR_ANTIPODE_LATITUDES_$2,
                    new Latitude(Math.toDegrees(phi1)),
                    new Latitude(Math.toDegrees(phi2))));
         
        double  sinphi = Math.sin(phi1);
        double  cosphi = Math.cos(phi1);
        double  n      = sinphi;
        boolean secant = (Math.abs(phi1 - phi2) >= EPS);
        if (isSpherical) {
            if (secant) {
                n = 0.5 * (n + Math.sin(phi2));
            }           
            c    = cosphi * cosphi + n*2 * sinphi;
            rho0 = Math.sqrt(c - n*2 * Math.sin(latitudeOfOrigin)) /n;
            ec   = Double.NaN;
        } else {
            double m1 = msfn(sinphi, cosphi);
            double q1 = qsfn(sinphi);
            if (secant) { /* secant cone */
                sinphi    = Math.sin(phi2);
                cosphi    = Math.cos(phi2);
                double m2 = msfn(sinphi, cosphi);
                double q2 = qsfn(sinphi);
                n = (m1 * m1 - m2 * m2) / (q2 - q1);
            }
            c = m1 * m1 + n * q1;
            rho0 = Math.sqrt(c - n * qsfn(Math.sin(latitudeOfOrigin))) /n;
            ec = 1.0 - .5 * (1.0-excentricitySquared) * 
                 Math.log((1.0 - excentricity) / (1.0 + excentricity)) / excentricity;
        }
        this.n = n;
    }
    
    /**
     * {@inheritDoc}
     */
    public ParameterDescriptorGroup getParameterDescriptors() {
        return Provider.PARAMETERS;
    }
    
    /**
     * {@inheritDoc}
     */
    public ParameterValueGroup getParameterValues() {
        final ParameterValueGroup values = super.getParameterValues();
        final Collection expected = getParameterDescriptors().descriptors();
        set(expected, Provider.STANDARD_PARALLEL_1, values, phi1);
        set(expected, Provider.STANDARD_PARALLEL_2, values, phi2);
        return values;
    }
    
    /**
     * Transforms the specified (<var>x</var>,<var>y</var>) coordinate (units in radians)
     * and stores the result in <code>ptDst</code> (linear distance on a unit sphere).
     */
    protected Point2D transformNormalized(double x, double y, Point2D ptDst) 
            throws ProjectionException 
    {
        x *= n;
        double rho;
        if (isSpherical) {
            rho = c - n*2 * Math.sin(y);
        } else {
            rho = c - n * qsfn(Math.sin(y));
        }

        if (rho < 0.0) {
            if (Math.abs(rho) < EPS) {
                rho = 0.0;
            } else {
                //TODO: can remove if someone can prove this condition will never happen
                throw new ProjectionException("Tolerance condition error");
            }
        }
        rho = Math.sqrt(rho) / n;
        y   = rho0 - rho * Math.cos(x);
        x   =        rho * Math.sin(x);

        if (ptDst != null) {
            ptDst.setLocation(x,y);
            return ptDst;
        }
        return new Point2D.Double(x,y);
    }
    
    /**
     * Transforms the specified (<var>x</var>,<var>y</var>) coordinate
     * and stores the result in <code>ptDst</code>.
     */
    protected Point2D inverseTransformNormalized(double x, double y, Point2D ptDst) 
            throws ProjectionException 
    {
        y = rho0 - y;
        double rho = Math.sqrt(x*x + y*y);
        if (rho > EPS) {
            if (n < 0.0) {
                rho = -rho;
                x   = -x;
                y   = -y;
            }
            x = Math.atan2(x, y) / n;
            y =  rho*n;
            if (isSpherical) {
                y = (c - y * y) / (n*2);
                if (Math.abs(y) <= 1.0){
                    y = Math.asin(y);
                }
                else {
                    y = (y < 0.0) ? -Math.PI/2.0 : Math.PI/2.0;
                }     
            } else {
                y = (c - y*y) / n;
                if (Math.abs(ec - Math.abs(y)) > EPS) {
                    y = phi1(y);
                } else {
                    y = (y < 0.0) ? -Math.PI/2.0 : Math.PI/2.0;
                } 
            }   
        } else {
            x = 0.0;
            y = n > 0.0 ? Math.PI/2.0 : - Math.PI/2.0;
        }

        if (ptDst != null) {
            ptDst.setLocation(x,y);
            return ptDst;
        }
        return new Point2D.Double(x,y);
    }
    
    /**
     * Iteratively solves equation (3-16) from Snyder.
     *
     * @param qs arcsin(q/2), used in the first step of itteration
     * @return the latitude
     */
    private double phi1(double qs) throws ProjectionException {
        final double tone_es = 1 - excentricitySquared;
        double phi = Math.asin(0.5 * qs);
        if (excentricity < EPS) {
            return phi;
        }
        for (int i=0; i<MAX_ITER; i++) {
            final double sinpi = Math.sin(phi);
            final double cospi = Math.cos(phi);
            final double con   = excentricity * sinpi;
            final double com   = 1.0 - con*con;
            final double dphi  = 0.5 * com*com / cospi * 
                                 (qs/tone_es - sinpi / com + 0.5/excentricity * 
                                 Math.log((1. - con) / (1. + con)));
            phi += dphi;
            if (Math.abs(dphi) <= TOL) {
                return phi;
            }
        } 
        throw new ProjectionException(Resources.format(ResourceKeys.ERROR_NO_CONVERGENCE));
    }
    
    /** 
     * Calculates q, Snyder equation (3-12)
     *
     * @param sinphi sin of the latitude q is calculated for
     * @return q from Snyder equation (3-12)
     */
    private double qsfn(double sinphi) {
        final double one_es = 1 - excentricitySquared;
        if (excentricity >= EPS) {
            final double con = excentricity * sinphi;
            return (one_es * (sinphi / (1. - con*con) -
                   (0.5/excentricity) * Math.log((1.-con) / (1.+con))));
        } else {
            return sinphi + sinphi;
        }
    }
    
    /**
     * Returns a hash value for this projection.
     */
    public int hashCode() {
        final long code = Double.doubleToLongBits(c);
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
            final AlbersEqualArea that = (AlbersEqualArea) object;
            return equals(this.n    , that.n   ) &&
                   equals(this.c    , that.c   ) &&
                   equals(this.rho0 , that.rho0) &&
                   equals(this.phi1 , that.phi1) &&
                   equals(this.phi2 , that.phi2);
        }
        return false;
    }
}
