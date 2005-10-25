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
 * Equidistant cylindrical projection. This is used in, for example, <cite>WGS84 / Plate
 * Carree</cite> (EPSG:32662). In the particular case where the {@linkplain #latitudeOfOrigin
 * latitude of origin} is 0°, this projection is also called Equirectangular.
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
        return (phi==1) ? Provider.EQUIRECTANGULAR_PARAMETERS
                        : Provider.EQUIDISTANT_PARAMETERS;
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
         * The parameters group, which mix "Equirectangular" and "Equidistant cylindrical"
         * together in order to allows flexible construction.
         */
        static final ParameterDescriptorGroup PARAMETERS;

        /**
         * The parameter group for the equidistant projection.
         */
        static final ParameterDescriptorGroup EQUIDISTANT_PARAMETERS;

        /**
         * The parameter group for the equirectangular projection.
         */
        static final ParameterDescriptorGroup EQUIRECTANGULAR_PARAMETERS;

        /**
         * Creates all parameter groups.
         */
        static {
            final NamedIdentifier GEOTIFF, OGC0, OGC, EPSG, CODE, GEOTOOLS;
            GEOTIFF  = new NamedIdentifier(CitationImpl.GEOTIFF, "CT_Equirectangular");
            OGC0     = new NamedIdentifier(CitationImpl.OGC,     "Equirectangular");
            OGC      = new NamedIdentifier(CitationImpl.OGC,     "Equidistant_Cylindrical");
            EPSG     = new NamedIdentifier(CitationImpl.EPSG,    "Equidistant Cylindrical");
            CODE     = new NamedIdentifier(CitationImpl.EPSG,    "9823");
            GEOTOOLS = new NamedIdentifier(CitationImpl.GEOTOOLS, Vocabulary.formatInternational(
                                           VocabularyKeys.EQUIDISTANT_CYLINDRICAL_PROJECTION));

            final ParameterDescriptor[] PARAM = new ParameterDescriptor[] {
                SEMI_MAJOR,          SEMI_MINOR,
                LATITUDE_OF_ORIGIN,  CENTRAL_MERIDIAN,
                FALSE_EASTING,       FALSE_NORTHING
            };
            final ParameterDescriptor[] PARAM0 = new ParameterDescriptor[] {
                SEMI_MAJOR,          SEMI_MINOR,
                                     CENTRAL_MERIDIAN,
                FALSE_EASTING,       FALSE_NORTHING
            };

            PARAMETERS = createDescriptorGroup(new NamedIdentifier[] {
                         OGC, OGC0, EPSG, CODE, GEOTIFF, GEOTOOLS}, PARAM);
            EQUIDISTANT_PARAMETERS = createDescriptorGroup(new NamedIdentifier[] {
                         OGC, EPSG, CODE, GEOTOOLS}, PARAM);
            EQUIRECTANGULAR_PARAMETERS = createDescriptorGroup(new NamedIdentifier[] {
                         OGC0, GEOTIFF}, PARAM0);
        }

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
