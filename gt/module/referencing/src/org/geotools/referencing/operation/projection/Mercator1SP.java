/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *
 *   (C) 2005-2006, Geotools Project Managment Committee (PMC)
 *   
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.referencing.operation.projection;

// OpenGIS dependencies
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterNotFoundException;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.operation.CylindricalProjection;
import org.opengis.referencing.operation.MathTransform;

// Geotools dependencies
import org.geotools.metadata.iso.citation.Citations;
import org.geotools.referencing.NamedIdentifier;
import org.geotools.resources.i18n.VocabularyKeys;
import org.geotools.resources.i18n.Vocabulary;


/**
 * Mercator Cylindrical 1SP Projection.
 *
 * @see <A HREF="http://www.remotesensing.org/geotiff/proj_list/mercator_1sp.html">"mercator_1sp" on RemoteSensing.org</A>
 * 
 * @since 2.2
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Rueben Schulz
 */
public class Mercator1SP extends Mercator {
    /**
     * Constructs a new map projection from the supplied parameters.
     *
     * @param  parameters The parameter values in standard units.
     * @throws ParameterNotFoundException if a mandatory parameter is missing.
     */
    protected Mercator1SP(final ParameterValueGroup parameters)
            throws ParameterNotFoundException
    {
        super(parameters);
    }

    /**
     * {@inheritDoc}
     */
    public ParameterDescriptorGroup getParameterDescriptors() {
        return Provider.PARAMETERS;
    }


    /**
     * Provides the transform equations for the spherical case of the Mercator projection.
     *
     * @version $Id$
     * @author Martin Desruisseaux
     * @author Rueben Schulz
     */
    private static final class Spherical extends Mercator.Spherical {
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
        }

        /**
         * {@inheritDoc}
         */
        public ParameterDescriptorGroup getParameterDescriptors() {
            return Provider.PARAMETERS;
        }
    }


    /**
     * The {@link org.geotools.referencing.operation.MathTransformProvider} for a
     * {@linkplain Mercator1SP Mercator 1SP} projection.
     *
     * @see <A HREF="http://www.remotesensing.org/geotiff/proj_list/mercator_1sp.html">"mercator_1sp" on RemoteSensing.org</A>
     * @see org.geotools.referencing.operation.DefaultMathTransformFactory
     *
     * @version $Id$
     * @author Martin Desruisseaux
     * @author Rueben Schulz
     */
    public static class Provider extends AbstractProvider {
        /**
         * The parameters group.
         */
        static final ParameterDescriptorGroup PARAMETERS = createDescriptorGroup(new NamedIdentifier[] {
                new NamedIdentifier(Citations.OGC,      "Mercator_1SP"),
                new NamedIdentifier(Citations.EPSG,     "Mercator (1SP)"),
                new NamedIdentifier(Citations.EPSG,     "9804"),
                new NamedIdentifier(Citations.GEOTIFF,  "CT_Mercator"),
                new NamedIdentifier(Citations.GEOTOOLS, Vocabulary.formatInternational(
                                    VocabularyKeys.CYLINDRICAL_MERCATOR_PROJECTION))
            }, new ParameterDescriptor[] {
                SEMI_MAJOR,       SEMI_MINOR,
                CENTRAL_MERIDIAN, SCALE_FACTOR,
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
            return CylindricalProjection.class;
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
            if (isSpherical(parameters)) {
                return new Spherical(parameters);
            } else {
                return new Mercator1SP(parameters);
            }
        }
    }
}
