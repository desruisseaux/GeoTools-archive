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
package org.geotools.referencing.operation.projection;

// J2SE dependencies and extensions
import java.util.Collection;

// OpenGIS dependencies
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterNotFoundException;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.PlanarProjection;

// Geotools dependencies
import org.geotools.metadata.iso.citation.CitationImpl;
import org.geotools.referencing.NamedIdentifier;
import org.geotools.resources.cts.ResourceKeys;
import org.geotools.resources.cts.Resources;


/**
 * Orthographic Projection. This is a perspective azimuthal (planar) projection
 * that is neither conformal nor equal-area. It resembles a globe and only 
 * one hemisphere can be seen at a time, since it is 
 * a perspectiove projection from infinite distance. While not useful for 
 * accurate measurements, this projection is useful for pictorial views of the
 * world. Only the spherical form is given here.
 * <p>
 * 
 * NOTE: formulae used below are from a port, to java, of the 
 *       'proj' package of the USGS survey. USGS work is acknowledged here.
 * <p>
 *
 * <strong>References:</strong><ul>
 *   <li> Proj-4.4.7 available at <A HREF="http://www.remotesensing.org/proj">www.remotesensing.org/proj</A><br>
 *        Relevant files are: {@code PJ_ortho.c}, {@code pj_fwd.c} and {@code pj_inv.c}.</li>
 *   <li> John P. Snyder (Map Projections - A Working Manual,
 *        U.S. Geological Survey Professional Paper 1395, 1987)</li>
 * </ul>
 *
 * @see <A HREF="http://mathworld.wolfram.com/OrthographicProjection.html">Orthographic projection on mathworld.wolfram.com</A>
 * @see <A HREF="http://www.remotesensing.org/geotiff/proj_list/orthographic.html">"Orthographic" on www.remotesensing.org</A>
 *
 * @version $Id$
 * @author Rueben Schulz
 *
 * @since 2.1
 */
public abstract class Orthographic extends MapProjection {
    /**
     * Creates a transform from the specified group of parameter values.
     *
     * @param  parameters The group of parameter values.
     * @param  expected The expected parameter descriptors.
     * @return The created math transform.
     * @throws ParameterNotFoundException if a required parameter was not found.
     */
    Orthographic(final ParameterValueGroup parameters, final Collection expected) 
            throws ParameterNotFoundException
    {
        //Fetch parameters 
        super(parameters, expected);
    }
    
    /**
     * {@inheritDoc}
     */
    public ParameterDescriptorGroup getParameterDescriptors() {
        return Provider.PARAMETERS;
    }
    
    /**
     * Compares the specified object with this map projection for equality.
     */
    public boolean equals(final Object object) {
        if (object == this) {
            // Slight optimization
            return true;
        }
        // Relevant parameters are already compared in MapProjection
        return super.equals(object);
    }
    
    
    
    
    //////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////
    ////////                                                                          ////////
    ////////                                 PROVIDER                                 ////////
    ////////                                                                          ////////
    //////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////

    /**
     * The {@link org.geotools.referencing.operation.MathTransformProvider}
     * for a {@link Orthographic} projection.
     *
     * @see org.geotools.referencing.operation.DefaultMathTransformFactory
     *
     * @version $Id$
     * @author Rueben Schulz
     */
    public static final class Provider extends AbstractProvider {
        /**
         * The parameters group.
         */
        static final ParameterDescriptorGroup PARAMETERS = createDescriptorGroup(new NamedIdentifier[] {
                new NamedIdentifier(CitationImpl.OGC,      "Orthographic"),
                new NamedIdentifier(CitationImpl.GEOTIFF,  "CT_Orthographic"),
                new NamedIdentifier(CitationImpl.ESRI,     "Orthographic"),
                new NamedIdentifier(CitationImpl.GEOTOOLS, Resources.formatInternational(
                                                           ResourceKeys.ORTHOGRAPHIC_PROJECTION))
            }, new ParameterDescriptor[] {
                SEMI_MAJOR,       SEMI_MINOR,
                CENTRAL_MERIDIAN, LATITUDE_OF_ORIGIN,
                SCALE_FACTOR,     
                FALSE_EASTING,    FALSE_NORTHING
            });
            
        /**
         * Constructs a new provider. 
         */
        public Provider() {
            super(PARAMETERS);
        }    

        /**
         * Returns the operation type for this map projection.
         */
        protected Class getOperationType() {
            return PlanarProjection.class;
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
            
            //values here are in degrees (the standard units for this parameter)
            final double latitudeOfOrigin = Math.abs(
                MapProjection.doubleValue(descriptors, Provider.LATITUDE_OF_ORIGIN, parameters));
            
            if (isSpherical(parameters)) {
                // Polar case.
                if (Math.abs(latitudeOfOrigin - Math.PI/2) < EPS) {
                    return new OrthographicPolar(parameters, descriptors);
                }
                // Equatorial case.
                else if (latitudeOfOrigin < EPS) {
                    return new OrthographicEquatorial(parameters, descriptors);
                }
                // Generic (oblique) case.
                else {
                    return new OrthographicOblique(parameters, descriptors);
                }
            } else {
                throw new UnsupportedOperationException(Resources.format(
                    ResourceKeys.ERROR_ELLIPTICAL_NOT_SUPPORTED));
            }
        }    
    }
}
