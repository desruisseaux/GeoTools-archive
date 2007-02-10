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

// J2SE dependencies and extensions
import java.awt.geom.Point2D;
import java.util.Collection;

// OpenGIS dependencies
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterNotFoundException;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.operation.CylindricalProjection;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.FactoryException;

// Geotools dependencies
import org.geotools.metadata.iso.citation.Citations;
import org.geotools.referencing.NamedIdentifier;
import org.geotools.resources.i18n.VocabularyKeys;
import org.geotools.resources.i18n.Vocabulary;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.i18n.Errors;


/**
 * Equidistant cylindrical projection (EPSG code 9823).  In the particular case where the
 * {@code standard_parallel_1} is 0°, this projection is also called
 * {@linkplain PlateCarree Plate Carree} or Equirectangular.
 *
 * This is used in, for example, <cite>WGS84 / Plate Carree</cite> (EPSG:32662).
 * 
 * <strong>References:</strong><ul>
 *   <li>John P. Snyder (Map Projections - A Working Manual,<br>
 *       U.S. Geological Survey Professional Paper 1395, 1987)</li>
 *   <li>"Coordinate Conversions and Transformations including Formulas",<br>
 *       EPSG Guidence Note Number 7 part 2, Version 24.</li>
 * </ul>
 *
 * @see <A HREF="http://mathworld.wolfram.com/CylindricalEquidistantProjection.html">Cylindrical Equidistant projection on MathWorld</A>
 * @see <A HREF="http://www.remotesensing.org/geotiff/proj_list/equirectangular.html">"Equirectangular" on RemoteSensing.org</A>
 *
 * @since 2.2
 * @source $URL$
 * @version $Id$
 * @author John Grange
 * @author Martin Desruisseaux
 */
public class EquidistantCylindrical extends MapProjection {
    /**
     * Cosinus of the standard_parallel_1 parameter.
     */
    private final double cosStandardParallel;

    /**
     * standard_parallel_1 parameter, used in getParameterValues(). 
     * Set to {@link Double#NaN} for the {@code Plate_Carree} case.
     */
    private final double standardParallel;

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
        super(parameters);
        final Collection expected = getParameterDescriptors().descriptors();
        if (expected.contains(Provider.STANDARD_PARALLEL)) {
            standardParallel = Math.abs(doubleValue(expected,
                                        Provider.STANDARD_PARALLEL, parameters));
            ensureLatitudeInRange(Provider.STANDARD_PARALLEL, standardParallel, false);
            cosStandardParallel = Math.cos(standardParallel);
        } else {
            // standard parallel is the equator (Plate Carree or Equirectangular)
            standardParallel = Double.NaN;
            cosStandardParallel = 1.0;
        }
        assert latitudeOfOrigin == 0 : latitudeOfOrigin;
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
        if (!Double.isNaN(standardParallel)) {
            final Collection expected = getParameterDescriptors().descriptors();
            set(expected, Provider.STANDARD_PARALLEL, values, standardParallel);
        }
        return values;
    }

    /**
     * Transforms the specified (<var>&lambda;</var>,<var>&phi;</var>) coordinates
     * (units in radians) and stores the result in {@code ptDst} (linear distance
     * on a unit sphere).
     */
    protected Point2D transformNormalized(double x, double y, final Point2D ptDst)
            throws ProjectionException
    {
        x *= cosStandardParallel;
        if (ptDst != null) {
            ptDst.setLocation(x,y);
            return ptDst;
        }
        return new Point2D.Double(x,y);
    }

    /**
     * Transforms the specified (<var>x</var>,<var>y</var>) coordinates
     * and stores the result in <code>ptDst</code>.
     */
    protected Point2D inverseTransformNormalized(double x, double y, final Point2D ptDst)
            throws ProjectionException
    {
        x /= cosStandardParallel;
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
        final long code = Double.doubleToLongBits(standardParallel);
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
            final EquidistantCylindrical that = (EquidistantCylindrical) object;
            return equals(this.standardParallel,  that.standardParallel);
        }
        return false;
    } 




    //////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////
    ////////                                                                          ////////
    ////////                                 PROVIDERS                                ////////
    ////////                                                                          ////////
    //////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////

    /**
     * The {@linkplain org.geotools.referencing.operation.MathTransformProvider math transform
     * provider} for an {@linkplain EquidistantCylindrical Equidistant Cylindrical} projection
     * (EPSG code 9823).
     *
     * @since 2.2
     * @version $Id$
     * @author John Grange
     *
     * @see org.geotools.referencing.operation.DefaultMathTransformFactory
     */
    public static class Provider extends AbstractProvider {
        /**
         * The parameters group. Note the EPSG includes a "Latitude of natural origin" parameter instead
         * of "standard_parallel_1". I have sided with ESRI and Snyder in this case.
         */
        static final ParameterDescriptorGroup PARAMETERS = createDescriptorGroup(new NamedIdentifier[] {
                new NamedIdentifier(Citations.OGC,      "Equidistant_Cylindrical"),
                new NamedIdentifier(Citations.EPSG,     "Equidistant Cylindrical"),
                new NamedIdentifier(Citations.ESRI,     "Equidistant_Cylindrical"),
                new NamedIdentifier(Citations.EPSG,     "9823"),
                new NamedIdentifier(Citations.GEOTOOLS, Vocabulary.formatInternational(
                                    VocabularyKeys.EQUIDISTANT_CYLINDRICAL_PROJECTION))
            }, new ParameterDescriptor[] {
                SEMI_MAJOR,       SEMI_MINOR,
                CENTRAL_MERIDIAN, STANDARD_PARALLEL,
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
                throws ParameterNotFoundException, FactoryException
        {
            if (isSpherical(parameters)) {
                return new EquidistantCylindrical(parameters);
            } else {
                throw new FactoryException(Errors.format(ErrorKeys.ELLIPTICAL_NOT_SUPPORTED));
            }
        }
    }
}
