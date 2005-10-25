/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2005, Geotools Project Managment Committee (PMC)
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
 */
package org.geotools.referencing.operation.projection;

// J2SE dependencies and extensions
import java.awt.geom.Point2D;

// OpenGIS dependencies
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterNotFoundException;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.operation.CylindricalProjection;
import org.opengis.referencing.operation.MathTransform;

// Geotools dependencies
import org.geotools.metadata.iso.citation.CitationImpl;
import org.geotools.referencing.NamedIdentifier;
import org.geotools.resources.i18n.VocabularyKeys;
import org.geotools.resources.i18n.Vocabulary;


/**
 * Equirectangular Projection. This is used in, for example, WGS84 / Plate Carree (EPSG:32662).
 * Multiple names are used for this projection, such as Equirectangular, Cylindrical Equidistant
 * and Plate Carree.
 *
 * @see <A HREF="http://mathworld.wolfram.com/CylindricalEquidistantProjection.html">Mercator projection on MathWorld</A>
 * @see <A HREF="http://www.remotesensing.org/geotiff/proj_list/equirectangular.html">Equirectangular</A>
 *
 * @since 2.2
 * @version $Id$
 * @author John Grange
 * @author Martin Desruisseaux
 */
public class EquidistantCylindrical extends MapProjection {
    /**
     * Cosinus of the {@linkplain #latitudeOfOrigin latitude of origin}.
     */
    private final double phi;

    /**
     * Constructs a new map projection from the supplied parameters.
     *
     * @param  parameters The parameter values in standard units.
     * @throws ParameterNotFoundException if a mandatory parameter is missing.
     */
    protected EquidistantCylindrical(final ParameterValueGroup parameters)
            throws ParameterNotFoundException
    {
        // Fetch parameters 
        super(parameters, Provider.PARAMETERS.descriptors());
        phi = Math.cos(latitudeOfOrigin);
    }

    /**
     * {@inheritDoc}
     */
    public ParameterDescriptorGroup getParameterDescriptors() {
        return Provider.PARAMETERS;
    }

    /**
     * Transforms the specified (<var>x</var>,<var>y</var>) coordinate (units in radians)
     * and stores the result in <code>ptDst</code> (linear distance on a unit sphere).
     */
    protected Point2D transformNormalized(double x, double y, final Point2D ptDst)
            throws ProjectionException
    {
        x *= phi;
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
    protected Point2D inverseTransformNormalized(double x, double y, final Point2D ptDst)
            throws ProjectionException
    {
        x /= phi;
        if (ptDst != null) {
            ptDst.setLocation(x,y);
            return ptDst;
        }
        return new Point2D.Double(x,y);
    }

    /**
     * The {@link org.geotools.referencing.operation.MathTransformProvider} for an
     * {@link org.geotools.referencing.operation.projection.EquidistantCylindrical}.
     *
     * @see org.geotools.referencing.operation.DefaultMathTransformFactory
     *
     * @since 2.2
     * @version $Id$
     * @author John Grange
     */
    public static final class Provider extends AbstractProvider {
        /**
         * The parameters group.
         */
        static final ParameterDescriptorGroup PARAMETERS = createDescriptorGroup(new NamedIdentifier[] {
                new NamedIdentifier(CitationImpl.OGC,      "Equirectangular"),
                new NamedIdentifier(CitationImpl.OGC,      "Equidistant_Cylindrical"),
                new NamedIdentifier(CitationImpl.EPSG,     "Equidistant Cylindrical"),
                new NamedIdentifier(CitationImpl.EPSG,     "9823"),
                new NamedIdentifier(CitationImpl.GEOTIFF,  "CT_Equirectangular"),
                new NamedIdentifier(CitationImpl.GEOTOOLS, Vocabulary.formatInternational(
                                    VocabularyKeys.EQUIDISTANT_CYLINDRICAL_PROJECTION))
            }, new ParameterDescriptor[] {
                SEMI_MAJOR,          SEMI_MINOR,
                LATITUDE_OF_ORIGIN,  CENTRAL_MERIDIAN,
                FALSE_EASTING,       FALSE_NORTHING
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
         * @throws org.opengis.parameter.ParameterNotFoundException if a required parameter was not found.
         */
        public MathTransform createMathTransform(final ParameterValueGroup parameters)
                throws ParameterNotFoundException
        {
            return new EquidistantCylindrical(parameters);
        }
    }
}
