/*
 * Geotools - OpenSource mapping toolkit
 * (C) 2003, 2004, Geotools Project Managment Committee (PMC)
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

import javax.units.NonSI;

import org.geotools.metadata.citation.Citation;
import org.geotools.referencing.Identifier;
import org.geotools.resources.cts.ResourceKeys;
import org.geotools.resources.cts.Resources;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterNotFoundException;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.operation.MathTransform;

/**
 * Stereographic Projection. The directions starting from the central point are true,
 * but the areas and the lengths become increasingly deformed as one moves away from
 * the center.  This projection is used to represent polar areas.  It can be adapted
 * for other areas having a circular form.
 * <br><br>
 *
 * This implementation, and its subclasses, provides transforms for six cases of the  
 * stereographic projection:
 * <ul>
 *   <li><code>"Oblique_Stereographic"</code> (EPSG code 9809), alias <code>"Double_Stereographic"</code>
 *       in ESRI software</li>
 *   <li><code>"Stereographic"</code> in ESRI software (<strong>NOT</strong> EPSG code 9809)</li>
 *   <li><code>"Polar_Stereographic"</code> (EPSG code 9810, uses a series calculation for the inverse)</li>
 *   <li><code>"Polar Stereographic (variant B)"</code> (EPSG code 9829, uses a series calculation for the inverse)</li>
 *   <li><code>"Stereographic_North_Pole"</code> in ESRI software (uses itteration for the inverse)</li>
 *   <li><code>"Stereographic_South_Pole"</code> in ESRI software (uses itteration for the inverse)</li>     
 * </ul>   
 *
 * Both the <code>"Oblique_Stereographic"</code> and <code>"Stereographic"</code> 
 * projections are "double" projections involving two parts: 1) a conformal
 * transformation of the geographic coordinates to a sphere and 2) a spherical
 * Stereographic projection. The EPSG considers both methods to be valid, but 
 * considers them to be a different coordinate operation methods.
 * <br><br>
 *
 * The <code>"Stereographic"</code> case uses the USGS equations of Snyder.
 * This employs a simplified conversion to the conformal sphere that computes 
 * the conformal latitude of each point on the sphere.
 * <br><br>
 *
 * The <code>"Oblique_Stereographic"</code> case uses equations from the EPSG.
 * This uses a more generalized form of the conversion to the conformal sphere; using only 
 * a single conformal sphere at the origin point. Since this is a "double" projection,
 * it is sometimes called the "Double Stereographic". The <code>"Oblique_Stereographic"</code>
 * is used in New Brunswick (Canada) and the Netherlands.
 * <br><br>
 *
 * The <code>"Stereographic"</code> and <code>"Double Stereographic"</code> names are used in
 * ESRI's ArcGIS 8.x product. The <code>"Oblique_Stereographic"</code> name is the EPSG name
 * for the later only.
 * <br><br>
 *
 * <strong>WARNING:<strong> Tests points calculated with ArcGIS's "Double Stereographic" are
 * not always equal to points calculated with the <code>"Oblique_Stereographic"</code>.
 * However, where there are differences, two different implementations of these equations
 * (EPSG guidence note 7 and libproj) calculate the same values as we do. Until these 
 * differences are resolved, please be careful when using this projection.
 * <br><br>
 *
 * If a <code>"latitude_of_origin"</code> parameter is supplied and is not consistent with the
 * projection classification (for example a latitude different from &plusmn;90° for the polar case),
 * then the oblique or polar case will be automatically inferred from the latitude. In other
 * words, the latitude of origin has precedence on the projection classification. If ommited,
 * then the default value is 90°N for <code>"Polar_Stereographic"</code> and 0° for
 * <code>"Oblique_Stereographic"</code>.
 * <br><br>
 *
 * Polar projections that use the series equations for the inverse calculation will
 * be little bit faster, but may be a little bit less accurate. If a polar 
 * "latitude_of_origin" is used for the "Oblique_Stereographic" or "Stereographic",
 * the itterative equations will be used for inverse polar calculations.
 * <br><br>
 *
 * The "Polar Stereographic (variant B)", "Stereographic_North_Pole", and 
 * "Stereographic_South_Pole" cases include a "standard_parallel_1" paramter.
 * This parameter sets the latitude with a scale factor equal to the supplied
 * scale factor. The "Polar Stereographic (variant B)" recieves its "lattitude_of_origin"
 * paramater value from the hemisphere of the "standard_parallel_1" value.
 * <br><br>
 *
 * <strong>References:</strong><ul>
 *   <li>John P. Snyder (Map Projections - A Working Manual,<br>
 *       U.S. Geological Survey Professional Paper 1395, 1987)</li>
 *   <li>"Coordinate Conversions and Transformations including Formulas",<br>
 *       EPSG Guidence Note Number 7, Version 19.</li>
 *   <li>Gerald Evenden. <A HREF="http://members.bellatlantic.net/~vze2hc4d/proj4/sterea.pdf">
 *       "Supplementary PROJ.4 Notes - Oblique Stereographic Alternative"</A></li>
 *   <li>Krakiwsky, E.J., D.B. Thomson, and R.R. Steeves. 1977. A Manual 
 *       For Geodetic Coordinate Transformations in the Maritimes. 
 *       Geodesy and Geomatics Engineering, UNB. Technical Report No. 48.</li>
 *   <li>Thomson, D.B., M.P. Mepham and R.R. Steeves. 1977. 
 *       The Stereographic Double Projection. 
 *       Geodesy and Geomatics Engineereng, UNB. Technical Report No. 46.</li>
 * </ul>
 *
 * @see <A HREF="http://mathworld.wolfram.com/StereographicProjection.html">Stereographic projection on MathWorld</A>
 * @see <A HREF="http://www.remotesensing.org/geotiff/proj_list/polar_stereographic.html">Polar_Stereographic</A>
 * @see <A HREF="http://www.remotesensing.org/geotiff/proj_list/oblique_stereographic.html">Oblique_Stereographic</A>
 * @see <A HREF="http://www.remotesensing.org/geotiff/proj_list/stereographic.html">Stereographic</A>
 * @see <A HREF="http://www.remotesensing.org/geotiff/proj_list/random_issues.html#stereographic">Some Random Stereographic Issues</A>
 *
 * @version $Id$
 * @author André Gosselin
 * @author Martin Desruisseaux
 * @author Rueben Schulz
 */
public abstract class Stereographic extends MapProjection {
        
    /** Projection mode for switch statement. */
    protected static final short EPSG = 0;
    /** Projection mode for switch statement. */
    protected static final short USGS = 1;
    /** Projection mode for switch statement. */
    protected static final short POLAR_A = 2;
    /** Projection mode for switch statement. */
    protected static final short POLAR_B = 3;
    /** Projection mode for switch statement. */
    protected static final short POLAR_NORTH = 4;
    /** Projection mode for switch statement. */
    protected static final short POLAR_SOUTH = 5;
    
    /**
     * The type of stereographic projection, used for wkt parameters.
     */
    protected short stereoType;
    
    /**
     * The {@link org.geotools.referencing.operation.MathTransformProvider}
     * for a {@link Stereographic} Oblique projection.
     *
     * @see org.geotools.referencing.operation.MathTransformFactory
     *
     * @version $Id$
     * @author Rueben Schulz
     */
    public static final class Provider_Oblique extends Provider {
        /**
         * The parameters group.
         */
        static final ParameterDescriptorGroup PARAMETERS = createDescriptorGroup(new Identifier[] {
                new Identifier(Citation.OPEN_GIS, "Oblique_Stereographic"),
                new Identifier(Citation.EPSG,     "Oblique Stereographic"),
                new Identifier(Citation.EPSG,     "Roussilhe"),
                new Identifier(Citation.EPSG,     "9809"),
                new Identifier(Citation.GEOTIFF,  "CT_ObliqueStereographic"),
                new Identifier(Citation.ESRI,     "Double_Stereographic"),
                new Identifier(Citation.GEOTOOLS, Resources.formatInternational(
                                                  ResourceKeys.STEREOGRAPHIC_PROJECTION))
            }, new ParameterDescriptor[] {
                SEMI_MAJOR,          SEMI_MINOR,
                CENTRAL_MERIDIAN,    LATITUDE_OF_ORIGIN,
                SCALE_FACTOR,
                FALSE_EASTING,       FALSE_NORTHING
            });
     
        /**
         * Construct a new provider. 
         */
        public Provider_Oblique() {
            super(PARAMETERS);
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
            final double latitudeOfOrigin = Math.abs(
                doubleValue(Provider.LATITUDE_OF_ORIGIN, parameters));
            if (isSpherical(parameters)) {
                // Polar case.
                if (Math.abs(latitudeOfOrigin - Math.PI/2) < EPS) {
                    return new StereographicPolar.Spherical(parameters, descriptors, Double.NaN, EPSG);
                }
                // Equatorial case.
                else if (latitudeOfOrigin < EPS) {
                    return new StereographicEquatorial.Spherical(parameters, descriptors, EPSG);
                }
                // Generic (oblique) case.
                else {
                    return new StereographicOblique.Spherical(parameters, descriptors, EPSG);
                }
            } else {
                // Polar case.
                if (Math.abs(latitudeOfOrigin - Math.PI/2) < EPS) {
                    return new StereographicPolar(parameters, descriptors, Double.NaN, EPSG);
                }
                // Generic (oblique) case.
                else {
                    return new StereographicOblique.EPSG(parameters, descriptors, EPSG);
                }
            }
        }
    }
    
    /**
     * The {@link org.geotools.referencing.operation.MathTransformProvider}
     * for a {@link Stereographic} Polar projection. This provider uses the
     * series equations for the inverse elliptical calculations.
     *
     * @see org.geotools.referencing.operation.MathTransformFactory
     *
     * @version $Id$
     * @author Rueben Schulz
     */
    public static final class Provider_Polar_A extends Provider {
        /**
         * The parameters group.
         */
        static final ParameterDescriptorGroup PARAMETERS = createDescriptorGroup(new Identifier[] {
                new Identifier(Citation.OPEN_GIS, "Polar_Stereographic"),
                new Identifier(Citation.EPSG,     "Polar Stereographic (variant A)"),
                new Identifier(Citation.EPSG,     "9810"),
                new Identifier(Citation.GEOTIFF,  "CT_PolarStereographic"),
                new Identifier(Citation.GEOTOOLS, Resources.formatInternational(
                                                  ResourceKeys.STEREOGRAPHIC_PROJECTION))
            }, new ParameterDescriptor[] {
                SEMI_MAJOR,          SEMI_MINOR,
                CENTRAL_MERIDIAN,    LATITUDE_OF_ORIGIN,
                SCALE_FACTOR,
                FALSE_EASTING,       FALSE_NORTHING
            });
     
        /**
         * Construct a new provider. 
         */
        public Provider_Polar_A() {
            super(PARAMETERS);
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
            if (isSpherical(parameters)) {
                return new StereographicPolar.Spherical(parameters, descriptors, Double.NaN, POLAR_A);
            } else {
                return new StereographicPolar.Series(parameters, descriptors, Double.NaN, POLAR_A);
            }
        }
    }
    
    /**
     * The {@link org.geotools.referencing.operation.MathTransformProvider}
     * for a {@link Stereographic} Polar (Variant B) projection. This provider 
     * includes a "Standard_Parallel_1" parameter and determines the hemisphere
     * of the projection from the Standard_Parallel_1 value. It also uses the
     * series equations for the inverse elliptical calculations.
     *
     * @see org.geotools.referencing.operation.MathTransformFactory
     *
     * @version $Id$
     * @author Rueben Schulz
     */
    public static class Provider_Polar_B extends Provider {
        /**
         * The operation parameter descriptor for the latitudeTrueScale
         * parameter value. Valid values range is from -90 to 90°. 
         * Default value is Double.NaN.
         */
        public static final ParameterDescriptor LATITUDE_TRUE_SCALE = createDescriptor(
                new Identifier[] {
                    new Identifier(Citation.ESRI, "Standard_Parallel_1"),
                    new Identifier(Citation.EPSG, "Latitude of standard parallel")  
                },
                Double.NaN, -90, 90, NonSI.DEGREE_ANGLE);
                
        /**
         * The parameters group.
         */
        static final ParameterDescriptorGroup PARAMETERS = createDescriptorGroup(new Identifier[] {
                new Identifier(Citation.EPSG,     "Polar Stereographic (variant B)"),
                new Identifier(Citation.EPSG,     "9829"),
                new Identifier(Citation.GEOTOOLS, Resources.formatInternational(
                                                  ResourceKeys.STEREOGRAPHIC_PROJECTION))
            }, new ParameterDescriptor[] {
                SEMI_MAJOR,          SEMI_MINOR,
                CENTRAL_MERIDIAN,    LATITUDE_TRUE_SCALE,
                FALSE_EASTING,       FALSE_NORTHING
            });
     
        /**
         * Construct a new provider. 
         */
        public Provider_Polar_B() {
            super(PARAMETERS);
        }
        
        /**
         * Construct a new provider. 
         */
        protected Provider_Polar_B(final ParameterDescriptorGroup params) {
            super(params);
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
            final double latitudeTrueScale = doubleValue(LATITUDE_TRUE_SCALE, parameters);
            final double latitudeOfOrigin = (latitudeTrueScale < 0.0) ? -Math.PI/2.0 : Math.PI/2.0;

            if (isSpherical(parameters)) {
                return new StereographicPolar.Spherical(parameters, descriptors, latitudeOfOrigin, POLAR_B);
            } else {
                return new StereographicPolar.Series(parameters, descriptors, latitudeOfOrigin, POLAR_B);
            }
        }
    }
    
    /**
     * The {@link org.geotools.referencing.operation.MathTransformProvider}
     * for a {@link Stereographic} North Polar projection. This provider sets 
     * the "latitude_of_origin" parameter to +90.0 degrees and uses the
     * itterative equations for the inverse elliptical calculations.
     *
     * @see org.geotools.referencing.operation.MathTransformFactory
     *
     * @version $Id$
     * @author Rueben Schulz
     */
    public static class Provider_North_Pole extends Provider_Polar_B {             
        /**
         * The parameters group.
         */
        static final ParameterDescriptorGroup PARAMETERS = createDescriptorGroup(new Identifier[] {
                new Identifier(Citation.ESRI,     "Stereographic_North_Pole"),
                new Identifier(Citation.GEOTOOLS, Resources.formatInternational(
                                                  ResourceKeys.STEREOGRAPHIC_PROJECTION))
            }, new ParameterDescriptor[] {
                SEMI_MAJOR,          SEMI_MINOR,
                CENTRAL_MERIDIAN,    LATITUDE_TRUE_SCALE,
                SCALE_FACTOR,
                FALSE_EASTING,       FALSE_NORTHING
            });
            
            
        /**
         * Construct a new provider. 
         */
        public Provider_North_Pole() {
            super(PARAMETERS);
        }
        
        /**
         * Construct a new provider. 
         */
        protected Provider_North_Pole(final ParameterDescriptorGroup params) {
            super(params);
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
            if (isSpherical(parameters)) {
                return new StereographicPolar.Spherical(parameters, descriptors, Math.PI/2.0, POLAR_NORTH);
            } else {
                return new StereographicPolar(parameters, descriptors, Math.PI/2.0, POLAR_NORTH);
            }
        }     
    }
       
    /**
     * The {@link org.geotools.referencing.operation.MathTransformProvider}
     * for a {@link Stereographic} North Polar projection. This provider sets 
     * the "latitude_of_origin" parameter to -90.0 degrees and uses the
     * itterative equations for the inverse elliptical calculations.
     *
     * @see org.geotools.referencing.operation.MathTransformFactory
     *
     * @version $Id$
     * @author Rueben Schulz
     */
    public static final class Provider_South_Pole extends Provider_North_Pole {
        /**
         * The parameters group.
         */
        static final ParameterDescriptorGroup PARAMETERS = createDescriptorGroup(new Identifier[] {
                new Identifier(Citation.ESRI,     "Stereographic_South_Pole"),
                new Identifier(Citation.GEOTOOLS, Resources.formatInternational(
                                                  ResourceKeys.STEREOGRAPHIC_PROJECTION))
            }, new ParameterDescriptor[] {
                SEMI_MAJOR,          SEMI_MINOR,
                CENTRAL_MERIDIAN,    LATITUDE_TRUE_SCALE,
                SCALE_FACTOR,
                FALSE_EASTING,       FALSE_NORTHING
            });
              
        /**
         * Construct a new provider. 
         */
        public Provider_South_Pole() {
            super(PARAMETERS);
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
            if (isSpherical(parameters)) {
                return new StereographicPolar.Spherical(parameters, descriptors, -Math.PI/2.0, POLAR_SOUTH);
            } else {
                return new StereographicPolar(parameters, descriptors, -Math.PI/2.0, POLAR_SOUTH);
            }
        }  
    }
    
    /**
     * The {@link org.geotools.referencing.operation.MathTransformProvider}
     * for a {@link Stereographic} (USGS equations) projection.
     *
     * @see org.geotools.referencing.operation.MathTransformFactory
     *
     * @version $Id$
     * @author Rueben Schulz
     */
    public static final class Provider_USGS extends Provider{
        /**
         * The parameters group.
         */
        static final ParameterDescriptorGroup PARAMETERS = createDescriptorGroup(new Identifier[] {
                new Identifier(Citation.ESRI,     "Stereographic"),
                new Identifier(Citation.GEOTIFF,  "CT_Stereographic"),
                new Identifier(Citation.GEOTOOLS, Resources.formatInternational(
                                                  ResourceKeys.STEREOGRAPHIC_PROJECTION))
            }, new ParameterDescriptor[] {
                SEMI_MAJOR,          SEMI_MINOR,
                CENTRAL_MERIDIAN,    LATITUDE_OF_ORIGIN,
                SCALE_FACTOR,
                FALSE_EASTING,       FALSE_NORTHING
            });
     
        /**
         * Construct a new provider. 
         */
        public Provider_USGS() {
            super(PARAMETERS);
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
            final double latitudeOfOrigin = Math.abs(
                doubleValue(Provider.LATITUDE_OF_ORIGIN, parameters));
            if (isSpherical(parameters)) {
                // Polar case.
                if (Math.abs(latitudeOfOrigin - Math.PI/2) < EPS) {
                    return new StereographicPolar.Spherical(parameters, descriptors, Double.NaN, USGS);
                }
                // Equatorial case.
                else if (latitudeOfOrigin < EPS) {
                    return new StereographicEquatorial.Spherical(parameters, descriptors, USGS);
                }
                // Generic (oblique) case.
                else {
                    return new StereographicOblique.Spherical(parameters, descriptors, USGS);
                }
            } else {
                // Polar case.
                if (Math.abs(latitudeOfOrigin - Math.PI/2) < EPS) {
                    return new StereographicPolar(parameters, descriptors, Double.NaN, USGS);
                }
                // Equatorial case.
                else if (latitudeOfOrigin < EPS) {
                    return new StereographicEquatorial(parameters, descriptors, USGS);
                }
                // Generic (oblique) case.
                else {
                    return new StereographicOblique(parameters, descriptors, USGS);
                }
            }
        }
    }
    
    
    /**
     * Creates a transform from the specified group of parameter values.
     *
     * @param  parameters The group of parameter values.
     * @param  expected The expected parameter descriptors.
     * @return The created math transform.
     * @throws ParameterNotFoundException if a required parameter was not found.
     */
    Stereographic(final ParameterValueGroup parameters, final Collection expected) 
            throws ParameterNotFoundException
    {
        //Fetch parameters 
        super(parameters, expected);
    }
    
    /**
     * {@inheritDoc}
     */
    public ParameterDescriptorGroup getParameterDescriptors() {
        switch (stereoType) {
            case EPSG: 
                return Provider_Oblique.PARAMETERS;
            case USGS:
                return Provider_USGS.PARAMETERS;
            case POLAR_A:
                return Provider_Polar_A.PARAMETERS;
            case POLAR_B:
                return Provider_Polar_B.PARAMETERS;
            case POLAR_NORTH:
                return Provider_North_Pole.PARAMETERS;
            case POLAR_SOUTH:
                return Provider_South_Pole.PARAMETERS;
            default:
                assert false; return null;
        }
    }
} 
