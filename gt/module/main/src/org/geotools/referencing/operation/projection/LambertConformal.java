/*
 * Geotools - OpenSource mapping toolkit
 * (C) 2003, 2004 Geotools Project Managment Committee (PMC)
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
import java.util.Collection;
import java.awt.geom.Point2D;
import javax.units.NonSI;

// OpenGIS dependencies
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterNotFoundException;
import org.opengis.referencing.operation.MathTransform;

// Geotools dependencies
import org.geotools.measure.Latitude;
import org.geotools.referencing.Identifier;
import org.geotools.metadata.citation.Citation;
import org.geotools.resources.cts.ResourceKeys;
import org.geotools.resources.cts.Resources;


/**
 * Lambert Conical Conformal Projection.  Areas and shapes are deformed
 * as one moves away from standard parallels.  The angles are true in
 * a limited area.  This projection is used for the charts of North America.
 * It uses a default central latitude of 40°N.
 * <br><br>
 *
 * This implementation provides transforms for three cases of the lambert conic 
 * conformal projection:
 * <ul>
 *   <li><code>Lambert_Conformal_Conic_1SP</code> (EPSG code 9801)</li>
 *   <li><code>Lambert_Conformal_Conic_2SP</code> (EPSG code 9802)</li>
 *   <li><code>Lambert_Conic_Conformal_2SP_Belgium</code> (EPSG code 9803)</li>
 * </ul>
 *
 * For the 1SP case the latitude of origin is used as the standard parallel (SP). 
 * To use a 1SP with a latitude of origin different from the SP, use the 2SP
 * and set both the SP1 and SP2 to the single SP. 
 * <br><br>
 *
 * <strong>References:</strong><ul>
 *   <li>John P. Snyder (Map Projections - A Working Manual,<br>
 *       U.S. Geological Survey Professional Paper 1395, 1987)</li>
 *   <li>"Coordinate Conversions and Transformations including Formulas",<br>
 *       EPSG Guidence Note Number 7, Version 19.</li>
 * </ul>
 *
 * @see <A HREF="http://mathworld.wolfram.com/LambertConformalConicProjection.html">Lambert conformal conic projection on MathWorld</A>
 * @see <A HREF="http://www.remotesensing.org/geotiff/proj_list/lambert_conic_conformal_1sp.html">lambert_conic_conformal_1sp</A>
 * @see <A HREF="http://www.remotesensing.org/geotiff/proj_list/lambert_conic_conformal_2sp.html">lambert_conic_conformal_2sp</A>
 * @see <A HREF="http://www.remotesensing.org/geotiff/proj_list/lambert_conic_conformal_2sp_belgium.html">lambert_conic_conformal_2sp_belgium</A>
 *
 * @version $Id$
 * @author André Gosselin
 * @author Martin Desruisseaux
 * @author Rueben Schulz
 */
public class LambertConformal extends MapProjection{
    /** 
     * Constant for the belgium 2SP case. This is 29.2985 seconds, given 
     * here in radians.
     */
    private static final double BELGE_A = 0.00014204313635987700;

    /**
     * Standards parallels in radians, for {@link #getParameterValues} implementation.
     */
    protected final double phi1, phi2;
    
    /**
     * Internal variables for computation.
     */
    private final double n,F,rho0;
    
    /**
     * <code>true</code> for 2SP, or <code>false</code> for 1SP projection.
     */
    private final boolean sp2;
    
    /**
     * <code>true</code> for Belgium 2SP.
     */
    private final boolean belgium;
    
    
    /**
     * The {@link org.geotools.referencing.operation.MathTransformProvider}
     * for a {@link LambertConformal} 1SP projection.
     *
     * @see org.geotools.referencing.operation.MathTransformFactory
     *
     * @version $Id$
     * @author Martin Desruisseaux
     * @author Rueben Schulz
     */
    public static final class Provider1SP extends Provider {       
        /**
         * The parameters group.
         */
        static final ParameterDescriptorGroup PARAMETERS = createDescriptorGroup(new Identifier[] {
                new Identifier(Citation.OPEN_GIS, "Lambert_Conformal_Conic_1SP"),
                new Identifier(Citation.EPSG,     "Lambert Conic Conformal (1SP)"),
                new Identifier(Citation.EPSG,     "9801"),
                new Identifier(Citation.GEOTIFF,  "CT_LambertConfConic_1SP"),
                new Identifier(Citation.GEOTOOLS, Resources.formatInternational(
                                                  ResourceKeys.LAMBERT_CONFORMAL_PROJECTION))
            }, new ParameterDescriptor[] {
                SEMI_MAJOR,          SEMI_MINOR,
                CENTRAL_MERIDIAN,    LATITUDE_OF_ORIGIN,
                SCALE_FACTOR,
                FALSE_EASTING,       FALSE_NORTHING
            });
         
        /**
         * Construct a new provider. 
         */
        public Provider1SP() {
            super(PARAMETERS);
        }     
            
        /**
         * Creates a transform from the specified group of parameter values.
         *
         * @param  parameters The group of parameter values.
         * @return The created math transform.
         * @throws ParameterNotFoundException if a required parameter was not found.
         */
        protected MathTransform createMathTransform(final ParameterValueGroup parameters) 
                throws ParameterNotFoundException
        {
            final Collection descriptors = PARAMETERS.descriptors();
            return new LambertConformal(parameters, descriptors, false, false);
        }
    }


    /**
     * The {@link org.geotools.referencing.operation.MathTransformProvider}
     * for a {@link LambertConformal} 2SP projection.
     *
     * @see org.geotools.referencing.operation.MathTransformFactory
     *
     * @version $Id$
     * @author Martin Desruisseaux
     * @author Rueben Schulz
     */
    public static class Provider2SP extends Provider {
        /**
         * The operation parameter descriptor for the {@link #phi1 standard parallel 1}
         * parameter value. Valid values range is from -90 to 90°. Default value is 0.
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
         * parameter value. Valid values range is from -90 to 90°. Default value is 0.
         */
        public static final ParameterDescriptor STANDARD_PARALLEL_2 = createDescriptor(
                new Identifier[] {
                    new Identifier(Citation.OPEN_GIS, "standard_parallel_2"),
                    new Identifier(Citation.EPSG,     "Latitude of 2nd standard parallel"),
                    new Identifier(Citation.GEOTIFF,  "StdParallel2")
                },
                0, -90, 90, NonSI.DEGREE_ANGLE);
        
        /**
         * The parameters group.
         * @task REVISIT: ESRI also included the scale factor as a parameter
         */
        static final ParameterDescriptorGroup PARAMETERS = createDescriptorGroup(new Identifier[] {
                new Identifier(Citation.OPEN_GIS, "Lambert_Conformal_Conic_2SP"),
                new Identifier(Citation.EPSG,     "Lambert Conic Conformal (2SP)"),
                new Identifier(Citation.EPSG,     "9802"),
                new Identifier(Citation.GEOTIFF,  "CT_LambertConfConic_2SP"),
                new Identifier(Citation.GEOTIFF,  "CT_LambertConfConic"),
                new Identifier(Citation.ESRI,     "Lambert_Conformal_Conic"),
                new Identifier(Citation.GEOTOOLS, Resources.formatInternational(
                                                  ResourceKeys.LAMBERT_CONFORMAL_PROJECTION))
            }, new ParameterDescriptor[] {
                SEMI_MAJOR,          SEMI_MINOR,
                CENTRAL_MERIDIAN,    LATITUDE_OF_ORIGIN,
                STANDARD_PARALLEL_1, STANDARD_PARALLEL_2,
                FALSE_EASTING,       FALSE_NORTHING
            });
        
        /**
         * Construct a new provider. 
         */
        public Provider2SP() {
            super(PARAMETERS);
        }
        
        /**
         * Construct a new provider. 
         */
        protected Provider2SP(final ParameterDescriptorGroup params) {
            super(params);
        }
           
        /**
         * Creates a transform from the specified group of parameter values.
         *
         * @param  parameters The group of parameter values.
         * @return The created math transform.
         * @throws ParameterNotFoundException if a required parameter was not found.
         */
        protected MathTransform createMathTransform(final ParameterValueGroup parameters) 
                throws ParameterNotFoundException
        {
            final Collection descriptors = PARAMETERS.descriptors();
            return new LambertConformal(parameters, descriptors, true, false);
        }
     }


    /**
     * The {@link org.geotools.referencing.operation.MathTransformProvider}
     * for a {@link LambertConformal} 2SP Belgium projection.
     *
     * @see org.geotools.referencing.operation.MathTransformFactory
     *
     * @version $Id$
     * @author Rueben Schulz
     */
     public static final class Provider2SP_Belgium extends Provider2SP {
        /**
         * The parameters group.
         */
        static final ParameterDescriptorGroup PARAMETERS = createDescriptorGroup(new Identifier[] {
                new Identifier(Citation.OPEN_GIS, "Lambert_Conformal_Conic_2SP_Belgium"),
                new Identifier(Citation.EPSG,     "Lambert Conic Conformal (2SP Belgium)"),
                new Identifier(Citation.EPSG,     "9803"),
                new Identifier(Citation.GEOTOOLS, Resources.formatInternational(
                                                  ResourceKeys.LAMBERT_CONFORMAL_PROJECTION))
            }, new ParameterDescriptor[] {
                SEMI_MAJOR,          SEMI_MINOR,
                CENTRAL_MERIDIAN,    LATITUDE_OF_ORIGIN,
                STANDARD_PARALLEL_1, STANDARD_PARALLEL_2,
                FALSE_EASTING,       FALSE_NORTHING
            });
         
        /**
         * Construct a new provider. 
         */
        public Provider2SP_Belgium() {
            super(PARAMETERS);
        }
           
        /**
         * Creates a transform from the specified group of parameter values.
         *
         * @param  parameters The group of parameter values.
         * @return The created math transform.
         * @throws ParameterNotFoundException if a required parameter was not found.
         */
        protected MathTransform createMathTransform(final ParameterValueGroup parameters) 
                throws ParameterNotFoundException
        {
            final Collection descriptors = PARAMETERS.descriptors();
            return new LambertConformal(parameters, descriptors, true, true);
        }
    }
     
    
    /**
     * Construct a new map projection from the supplied parameters.
     *
     * @param  parameters The parameter values in standard units.
     * @param  expected The expected parameter descriptors.
     * @param  sp2 <code>true</code> for 2SP, or <code>false</code> for 1SP.
     * @param  belgium <code>true</code> for the Belgium 2SP case.
     * @throws ParameterNotFoundException if a mandatory parameter is missing.
     *
     * @task REVISIT: set phi2 = phi1 if no SP2 value is given by user (an 1sp projection)
     */
    public LambertConformal(final ParameterValueGroup parameters, final Collection expected,
                            final boolean sp2, final boolean belgium) 
    {
        //Fetch parameters 
        super(parameters, expected);
        this.sp2         = sp2;
        this.belgium     = belgium;
        if (sp2) {
            phi1 = doubleValue(expected, Provider2SP.STANDARD_PARALLEL_1, parameters);
            phi2 = doubleValue(expected, Provider2SP.STANDARD_PARALLEL_2, parameters);
        } else {
            if (belgium) {
                throw new IllegalArgumentException();
            }
            // EPSG says the 1SP case uses the latitude of origin as the SP
            phi1 = phi2 = latitudeOfOrigin;
        }
        // Compute constants
        if (Math.abs(phi1 + phi2) < EPS) {
            throw new IllegalArgumentException(Resources.format(
                    ResourceKeys.ERROR_ANTIPODE_LATITUDES_$2,
                    new Latitude(Math.toDegrees(phi1)),
                    new Latitude(Math.toDegrees(phi2))));
        }
        final double  cosphi1 = Math.cos(phi1);
        final double  sinphi1 = Math.sin(phi1);
        final boolean  secant = Math.abs(phi1-phi2) > EPS; // Should be 'true' for 2SP case.
        if (isSpherical) {
            if (secant) {
                n = Math.log(cosphi1 / Math.cos(phi2)) /
                    Math.log(Math.tan((Math.PI/4) + 0.5*phi2) /
                             Math.tan((Math.PI/4) + 0.5*phi1));
            } else {
                n = sinphi1;
            }
            F = cosphi1 * Math.pow(Math.tan((Math.PI/4) + 0.5*phi1), n) / n;
            if (Math.abs(Math.abs(latitudeOfOrigin) - (Math.PI/2)) >= EPS) {
                rho0 = F * Math.pow(Math.tan((Math.PI/4) + 0.5*latitudeOfOrigin), -n);
            } else {
                rho0 = 0.0;
            }
        } else {
            final double m1 = msfn(sinphi1, cosphi1);
            final double t1 = tsfn(phi1, sinphi1);
            if (secant) {
                final double sinphi2 = Math.sin(phi2);
                final double m2 = msfn(sinphi2, Math.cos(phi2));
                final double t2 = tsfn(phi2, sinphi2);
                n = Math.log(m1/m2) / Math.log(t1/t2);
            } else {
                n = sinphi1;
            }
            F = m1 * Math.pow(t1, -n) / n;
            if (Math.abs(Math.abs(latitudeOfOrigin) - (Math.PI/2)) >= EPS) {
                rho0 = F * Math.pow(tsfn(latitudeOfOrigin, Math.sin(latitudeOfOrigin)), n);
            } else {
                rho0 = 0.0;
            }
        } 
    }
    
    /**
     * {@inheritDoc}
     */
    public ParameterDescriptorGroup getParameterDescriptors() {
        return (belgium) ? 
                    Provider2SP_Belgium.PARAMETERS :
                    ((sp2) ? Provider2SP.PARAMETERS : Provider1SP.PARAMETERS);
    }
    
    /**
     * {@inheritDoc}
     */
    public ParameterValueGroup getParameterValues() {
        final ParameterValueGroup values = super.getParameterValues();
        if (sp2) {
            final Collection expected = getParameterDescriptors().descriptors();
            set(expected, Provider2SP.STANDARD_PARALLEL_1, values, phi1);
            set(expected, Provider2SP.STANDARD_PARALLEL_2, values, phi2);
        }
        return values;
    }
    
    /**
     * Transforms the specified (<var>x</var>,<var>y</var>) coordinate (units in radians)
     * and stores the result in <code>ptDst</code> (linear distance on a unit sphere).
     */
    protected Point2D transformNormalized(double x, double y, Point2D ptDst) 
            throws ProjectionException 
    {
        double rho;
        //Snyder p. 108
        if (Math.abs(Math.abs(y) - (Math.PI/2)) < EPS) {
            if (y*n <= 0) {
                throw new ProjectionException(Resources.format(
                        ResourceKeys.ERROR_POLE_PROJECTION_$1,
                        new Latitude(Math.toDegrees(y))));
            } else {
                rho = 0;
            }
        } else if (isSpherical) {
            rho = F * Math.pow(Math.tan((Math.PI/4) + 0.5*y), -n);
        } else {
            rho = F * Math.pow(tsfn(y, Math.sin(y)), n);
        }
        
        x *= n;
        if (belgium) {
            x -= BELGE_A;
        }
        y = rho0 - rho * Math.cos(x);
        x =        rho * Math.sin(x);
        
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
        double theta;
        y = rho0 - y;
        double rho = Math.sqrt(x*x + y*y);  // Zero when the latitude is 90 degrees.
        if (rho > EPS) {
            if (n < 0) {
                rho = -rho;
                x = -x;
                y = -y;
            }
            theta = Math.atan2(x, y);
            if (belgium) {
                theta += BELGE_A;
            }
            x = theta/n;
            if (isSpherical) {
                y = 2.0 * Math.atan(Math.pow(F/rho, 1.0/n)) - (Math.PI/2);
            } else {
                y = cphi2(Math.pow(rho/F, 1.0/n));
            }
        } else {
            x = 0.0;
            y = n < 0 ? -(Math.PI/2) : (Math.PI/2);
        }
        if (ptDst != null) {
            ptDst.setLocation(x,y);
            return ptDst;
        }
        return new Point2D.Double(x,y);
    }
    
    /**
     * Returns a hash value for this projection.
     */
    public int hashCode() {
        /*
         * This code should be computed fast. Consequently, we do not use all fields
         * in this object.  Two <code>LambertConformal</code> objects with different
         * {@link #phi1} and {@link #phi2} should compute a F value different enough.
         */
        final long code = Double.doubleToLongBits(F);
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
            final LambertConformal that = (LambertConformal) object;
            return (this.sp2 == that.sp2) && (this.belgium == that.belgium) &&
                   equals(this.n,      that.n)    &&
                   equals(this.F,      that.F)    &&
                   equals(this.rho0,   that.rho0) &&
                   equals(this.phi1,   that.phi1) &&
                   equals(this.phi2,   that.phi2);
        }
        return false;
    }
}
