/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Managment Committee (PMC)
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
import java.util.Map;
import java.util.HashMap;
import java.util.Locale;
import java.awt.geom.Point2D;
import javax.units.NonSI;

// OpenGIS dependencies
import org.opengis.parameter.ParameterValue;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterNotFoundException;
import org.opengis.referencing.operation.MathTransform;

// Geotools dependencies
import org.geotools.measure.Latitude;
import org.geotools.referencing.Identifier;
import org.geotools.referencing.operation.MathTransformFactory;  // For Javadoc
import org.geotools.referencing.operation.MathTransformProvider; // For Javadoc
import org.geotools.metadata.citation.Citation;
import org.geotools.resources.cts.ResourceKeys;
import org.geotools.resources.cts.Resources;


/**
 * Mercator Cylindrical Projection. The parallels and the meridians are straight lines and
 * cross at right angles; this projection thus produces rectangular charts. The scale is true
 * along the equator (by default) or along two parallels equidistant of the equator (if a scale
 * factor other than 1 is used). This projection is used to represent areas close to the equator.
 * It is also often used for maritime navigation because all the straight lines on the chart are
 * <em>loxodrome</em> lines, i.e. a ship following this line would keep a constant azimuth on its
 * compass.
 * <br><br>
 *
 * This implementation handles both the 1 and 2 stardard parallel cases.
 * For <code>Mercator_1SP</code> (EPSG code 9804), the line of contact is the equator. 
 * For <code>Mercator_2SP</code> (EPSG code 9805) lines of contact are symmetrical 
 * about the equator.
 * <br><br>
 *
 * <strong>References:</strong><ul>
 *   <li>John P. Snyder (Map Projections - A Working Manual,<br>
 *       U.S. Geological Survey Professional Paper 1395, 1987)</li>
 *   <li>"Coordinate Conversions and Transformations including Formulas",<br>
 *       EPSG Guidence Note Number 7, Version 19.</li>
 * </ul>
 *
 * @see <A HREF="http://mathworld.wolfram.com/MercatorProjection.html">Mercator projection on MathWorld</A>
 * @see <A HREF="http://www.remotesensing.org/geotiff/proj_list/mercator_1sp.html">"mercator_1sp" on Remote Sensing</A>
 * @see <A HREF="http://www.remotesensing.org/geotiff/proj_list/mercator_2sp.html">"mercator_2sp" on Remote Sensing</A>
 * 
 * @version $Id$
 * @author André Gosselin
 * @author Martin Desruisseaux
 * @author Rueben Schulz
 */
public class Mercator extends MapProjection {
    /**
     * Standard Parallel used for the <code>Mercator_2SP</code> case.
     * Set to {@link Double#NaN} for the <code>Mercator_1SP</code> case.
     */
    protected final double standardParallel;

    /**
     * The {@link MathTransformProvider} for a {@link Mercator} 1SP projection.
     *
     * @see <A HREF="http://www.remotesensing.org/geotiff/proj_list/mercator_1sp.html">"mercator_1sp" on Remote Sensing</A>
     * @see MathTransformFactory
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
                new Identifier(Citation.OPEN_GIS, "Mercator_1SP"),
                new Identifier(Citation.EPSG,     "9804"),
                new Identifier(Citation.GEOTOOLS, Resources.formatInternational(
                                                  ResourceKeys.CYLINDRICAL_MERCATOR_PROJECTION))
            }, new ParameterDescriptor[] {
                SEMI_MAJOR,       SEMI_MINOR,
                CENTRAL_MERIDIAN, SCALE_FACTOR,
                FALSE_EASTING,    FALSE_NORTHING
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
        public MathTransform createMathTransform(final ParameterValueGroup parameters)
                throws ParameterNotFoundException
        {
            if (isSpherical(parameters)) {
                return new Spherical(parameters, false);
            } else {
                return new Mercator(parameters, false);
            }
        }
    }

    /**
     * The {@link MathTransformProvider} for a {@link Mercator} 2SP projection.
     *
     * @see <A HREF="http://www.remotesensing.org/geotiff/proj_list/mercator_2sp.html">"mercator_2sp" on Remote Sensing</A>
     * @see MathTransformFactory
     *
     * @version $Id$
     * @author Martin Desruisseaux
     * @author Rueben Schulz
     */
    public static final class Provider2SP extends Provider {
        /**
         * The operation parameter descriptor for the {@link #standardParallel standard parallel}
         * parameter value. Valid values range is from -90 to 90°. Default value is 0.
         */
        public static final ParameterDescriptor STANDARD_PARALLEL = createDescriptor(
                new Identifier[] {
                    new Identifier(Citation.OPEN_GIS, "standard_parallel_1"),
// TODO                    new Identifier(Citation.EPSG,     "")
                },
                0, -90, 90, NonSI.DEGREE_ANGLE);

        /**
         * The parameters group.
         */
        static final ParameterDescriptorGroup PARAMETERS = createDescriptorGroup(new Identifier[] {
                new Identifier(Citation.OPEN_GIS, "Mercator_2SP"),
                new Identifier(Citation.EPSG,     "9805"),
                new Identifier(Citation.GEOTIFF,  "CT_Mercator"),
                new Identifier(Citation.GEOTOOLS, Resources.formatInternational(
                                                  ResourceKeys.CYLINDRICAL_MERCATOR_PROJECTION))
            }, new ParameterDescriptor[] {
                SEMI_MAJOR,       SEMI_MINOR,
                CENTRAL_MERIDIAN, STANDARD_PARALLEL,
                FALSE_EASTING,    FALSE_NORTHING
            });

        /**
         * Construct a new provider. 
         */
        public Provider2SP() {
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
            if (isSpherical(parameters)) {
                return new Spherical(parameters, true);
            } else {
                return new Mercator(parameters, true);
            }
        }
    }


    /**
     * Construct a new map projection from the supplied parameters.
     *
     * @param  parameters The parameter values in standard units.
     * @throws ParameterNotFoundException if a mandatory parameter is missing.
     */
    protected Mercator(final ParameterValueGroup parameters)
            throws ParameterNotFoundException
    {
        this(parameters, parameters.getDescriptor().getName().getCode().endsWith("2SP"));
    }

    /**
     * Construct a new map projection from the supplied parameters.
     *
     * @param  parameters The parameter values in standard units.
     * @param  sp2 Indicates if this is a 1 or 2 standard parallel case of the mercator projection.
     * @throws ParameterNotFoundException if a mandatory parameter is missing.
     */
    Mercator(final ParameterValueGroup parameters, final boolean sp2)
            throws ParameterNotFoundException
    {
        //Fetch parameters 
        super(parameters);
        if (sp2) {
            // scaleFactor is not a parameter in the Mercator_2SP case and is computed from
            // the standard parallel.   The super-class constructor should have initialized
            // 'scaleFactor' to 1. We still use the '*=' operator rather than '=' in case a
            // user implementation still provides a scale factor for its custom projections.
            standardParallel = Math.abs(doubleValue(Provider2SP.STANDARD_PARALLEL, parameters));
            ensureLatitudeInRange(Provider2SP.STANDARD_PARALLEL, standardParallel, false);
            if (isSpherical) {
                scaleFactor *= Math.cos(standardParallel);
            }  else {
                scaleFactor *= msfn(Math.sin(standardParallel),
                                    Math.cos(standardParallel));
            }
            globalScale = scaleFactor*semiMajor;
        } else {
            // No standard parallel. Instead, uses the scale factor explicitely provided.
            standardParallel = Double.NaN;
        }
        assert latitudeOfOrigin == 0 : latitudeOfOrigin;
    }

    /**
     * {@inheritDoc}
     */
    public ParameterValueGroup getParameterValues() {
        final boolean sp1 = Double.isNaN(standardParallel);
        final ParameterDescriptorGroup descriptor = (sp1) ? Provider1SP.PARAMETERS
                                                          : Provider2SP.PARAMETERS;
        // TODO: remove the cast below once we will be allowed to use J2SE 1.5.
        final ParameterValueGroup values = (ParameterValueGroup) descriptor.createValue();
        set(Provider.SEMI_MAJOR,       values, semiMajor      );
        set(Provider.SEMI_MINOR,       values, semiMinor      );
        set(Provider.CENTRAL_MERIDIAN, values, centralMeridian);
        if (sp1) {
            set(Provider1SP.SCALE_FACTOR, values, scaleFactor);
        } else {
            set(Provider2SP.STANDARD_PARALLEL, values, standardParallel);
        }
        set(Provider.FALSE_EASTING,  values, falseEasting );
        set(Provider.FALSE_NORTHING, values, falseNorthing);
        return values;
    }
    
    /**
     * Transforms the specified (<var>x</var>,<var>y</var>) coordinate (units in radians)
     * and stores the result in <code>ptDst</code> (linear distance on a unit sphere).
     */
    protected Point2D transformNormalized(double x, double y, final Point2D ptDst)
            throws ProjectionException
    {
        if (Math.abs(y) > (Math.PI/2 - EPS)) {
            throw new ProjectionException(Resources.format(
                    ResourceKeys.ERROR_POLE_PROJECTION_$1, new Latitude(Math.toDegrees(y))));
        }

        y = - Math.log(tsfn(y, Math.sin(y)));

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
        y = Math.exp(-y);
        y = cphi2(y);

        if (ptDst != null) {
            ptDst.setLocation(x,y);
            return ptDst;
        }
        return new Point2D.Double(x,y);
    }


    /**
     * Provides the transform equations for the spherical case of the Mercator projection.
     *
     * @version $Id$
     * @author Martin Desruisseaux
     * @author Rueben Schulz
     */
    private static final class Spherical extends Mercator {
        /**
         * Construct a new map projection from the suplied parameters.
         *
         * @param  parameters The parameter values in standard units.
         * @param  sp2 Indicates if this is a 1 or 2 standard parallel case of the mercator projection.
         * @throws ParameterNotFoundException if a mandatory parameter is missing.
         */
        protected Spherical(final ParameterValueGroup parameters, final boolean sp2)
                throws ParameterNotFoundException
        {
            super(parameters, sp2);
            assert isSpherical;
	}

	/**
	 * Transforms the specified (<var>x</var>,<var>y</var>) coordinate
         * and stores the result in <code>ptDst</code> using equations for a Sphere.
	 */
        protected Point2D transformNormalized(double x, double y, Point2D ptDst)
                throws ProjectionException
        {
            if (Math.abs(y) > (Math.PI/2 - EPS)) {
                throw new ProjectionException(Resources.format(
                        ResourceKeys.ERROR_POLE_PROJECTION_$1, new Latitude(Math.toDegrees(y))));
            }
            // Compute using ellipsoidal formulas, for comparaison later.
            assert (ptDst = super.transformNormalized(x, y, ptDst)) != null;
          
            y = Math.log(Math.tan((Math.PI/4) + 0.5*y));

            assert Math.abs(ptDst.getX()-x) <= EPS*globalScale : x;
            assert Math.abs(ptDst.getY()-y) <= EPS*globalScale : y;
            if (ptDst != null) {
                ptDst.setLocation(x,y);
                return ptDst;
            }
            return new Point2D.Double(x,y);
        }

        /**
         * Transforms the specified (<var>x</var>,<var>y</var>) coordinate
         * and stores the result in <code>ptDst</code> using equations for a sphere.
         */
        protected Point2D inverseTransformNormalized(double x, double y, Point2D ptDst)
                throws ProjectionException
        {
            // Compute using ellipsoidal formulas, for comparaison later.
            assert (ptDst = super.inverseTransformNormalized(x, y, ptDst)) != null;

            y = (Math.PI/2) - 2.0*Math.atan(Math.exp(-y));

            assert Math.abs(ptDst.getX()-x) <= EPS : x;
            assert Math.abs(ptDst.getY()-y) <= EPS : y;
            if (ptDst != null) {
                ptDst.setLocation(x,y);
                return ptDst;
            }
            return new Point2D.Double(x,y);
	}
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
            final Mercator that = (Mercator) object;
            return equals(this.standardParallel,  that.standardParallel);
        }
        return false;
    }
}
