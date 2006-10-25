/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2005, Geotools Project Managment Committee (PMC)
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

import org.geotools.metadata.iso.citation.Citations;
import org.geotools.referencing.NamedIdentifier;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterNotFoundException;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.operation.ConicProjection;
import org.opengis.referencing.operation.MathTransform;
import java.awt.geom.Point2D;
import java.util.Collection;
import javax.units.NonSI;
import javax.units.Unit;


/**
 * <p>Krovak Oblique Conformal Conic projection (EPSG: 9819) is used in the
 * Czech Republic and Slovakia under the name 'Krovak' projection. The
 * geographic coordinates on the  ellipsoid are first reduced to conformal
 * coordinates on the conformal (Gaussian) sphere. These spherical coordinates
 * are then projected onto the oblique cone and converted to grid coordinates.
 * The pseudo standard parallel is defined on the conformal sphere after its
 * rotation, to obtain the oblique  aspect of the projection. It is then the
 * parallel on this sphere at which the map projection is true to scale; on
 * the ellipsoid it maps as a complex curve.</p>
 *  <p>The compulsory parameters are just the ellipsoid characteristics.
 * All other parameters are optional and have defaults to match the common
 * usage with Krovak projection.</p>
 *  <p>In general the axis of Krovak projection are defined as westing and
 * southing (not easting and northing) and they are also reverted, so if the
 * value of projected coordinates should (and in y, x order in Krovak) be
 * positive the 'Axis' parameter for projection should be defined explicitly
 * like this (in wkt):<pre>PROJCS["S-JTSK (Ferro) / Krovak",  
 *         .                                                              
 *         .                                                              
 *         .
 *                                                                       
 *     PROJECTION["Krovak"]                                         
 *     PARAMETER["semi_major", 6377397.155],  
 *     PARAMETER["semi_minor", 6356078.963],                   
 *     UNIT["meter",1.0],                                  
 *     AXIS["x", WEST],                     
 *     AXIS["y", SOUTH]]                                              
 *     </pre>Axis in Krovak:
 * <pre>                                                              
 *   y<------------------+                                                                                                  
 *                       |                                             
 *    Czech. Rep.        | 
 *                       |                                                                   
 *                       x                              
   </pre>
   By default, the axis are 'easting,
 * northing' so the values of projected coordinates are negative and in (and in y, x order in Krovak
 * - it is cold Krovak GIS version).</p>
 *  <p><strong>References:</strong>
 *  <ul>
 *      <li>Proj-4.4.7 available at <A HREF="http://www.remotesensing.org/proj">www.remotesensing.org/proj</A><br>
 *      Relevant files is: PJ_krovak.c</li>
 *      <li>"Coordinate Conversions and Transformations including
 *      Formulas" available at, <A
 *      HREF="http://www.remotesensing.org/geotiff/proj_list/guid7.html">http://www.remotesensing.org/geotiff/proj_list/guid7.html</A></li>
 *  </ul>
 *  </p>
 *
 * @author jezekjan
 *
 * @see <A
 *      HREF="http://www.remotesensing.org/geotiff/proj_list/krovak.html">Krovak
 *      on RemoteSensing.org </A>
 * @see <A
 *      HREF="http://www.remotesensing.org/geotiff/proj_list/guid7.html">Krovak
 *      on "Coordinate Conversions and Transformations including Formulas"
 *      </A>
 */
public class Krovak extends MapProjection {
    /**  */
    private static final long serialVersionUID = -2278638897480282564L;

    /** Latitude of centre of the projection */
    private double lat_0;

    /** Longitude of centre of the projection */
    private double lon_0;

    /** Scale factor on the pseudo standard parallel */
    private final double k;

    /**
     * azimuth of the centre line passing through the centre of the
     * projection = co-latitude of the cone axis at point of intersection with
     * the ellipsoid.
     */
    private final double azim;

    /** Latitude of pseudo standard parallel */
    private final double s0;

    /** Semi-major axis */
    private final double major;

    /** Semi-minor axis */
    private final double minor;

    /** Useful variables calculated from parameters defined by user. */
    private final double alfa;

    /** Useful variables calculated from parameters defined by user. */
    private final double e2;

    /** Useful variables calculated from parameters defined by user. */
    private final double e;

    /** Useful variables calculated from parameters defined by user. */
    private final double u0;

    /** Useful variables calculated from parameters defined by user. */
    private final double k1;

    /** Useful variables calculated from parameters defined by user. */
    private final double radius;

    /** Useful variables calculated from parameters defined by user. */
    private final double n;

    /** Useful constant - 45° in rad */
    private final double s45 = 0.785398163397448;

    /**
     * lon_0 transformed on ellipsoid -if calculated from defaults
     * value should be 42°31'31.41725'' in rad.
     */
    private final double vk;

    Krovak(final ParameterValueGroup parameters)
        throws ParameterNotFoundException {
        super(parameters);

        final Collection expected = getParameterDescriptors()
                                        .descriptors();
        //Fetch parameters from user input.
        lat_0 = doubleValue(expected, Provider.LATITUDE_OF_ORIGIN, parameters);
        lon_0 = doubleValue(expected, Provider.CENTRAL_MERIDIAN, parameters);
        s0 = doubleValue(expected, Provider.PSEUDO_STANDARD_PARALLEL_1,
                parameters);
        k = doubleValue(expected, Provider.SCALE_FACTOR, parameters);
        azim = doubleValue(expected, Provider.AZIMUTH, parameters);
        major = doubleValue(expected, AbstractProvider.SEMI_MAJOR, parameters);
        minor = doubleValue(expected, AbstractProvider.SEMI_MINOR, parameters);

        //Calculates useful constants.
        n = Math.sin(s0);

        e2 = (Math.pow(major, 2) - Math.pow(minor, 2)) / Math.pow(major, 2);

        e = Math.sqrt(e2);

        alfa = Math.sqrt(1. + ((e2 * Math.pow(Math.cos(lat_0), 4)) / (1. - e2)));

        vk = alfa * lon_0;
        u0 = Math.asin(Math.sin(lat_0) / alfa);

        double g = Math.pow((1. - (e * Math.sin(lat_0))) / (1.
                + (e * Math.sin(lat_0))), (alfa * e) / 2.);

        k1 = (Math.pow(Math.tan((lat_0 / 2.) + s45), alfa) * g) / Math.tan((u0 / 2.)
                + s45);

        radius = (major * Math.sqrt(1 - e2)) / (1
            - (e2 * Math.pow(Math.sin(lat_0), 2)));
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
    protected Point2D inverseTransformNormalized(double x, double y,
        Point2D ptDst) throws ProjectionException {
        //x -> southing, y -> westing    	
        double yk = -x * major;
        double xk = -y * major;

        double ro = Math.sqrt((xk * xk) + (yk * yk));
        double eps = Math.atan2(yk, xk);

        double ro0 = k * radius * (1 / Math.tan(s0));
        double d = eps / Math.sin(s0);

        double s = 2. * (Math.atan(Math.pow(ro0 / ro, 1. / n) * Math.tan((s0 / 2.)
                    + s45)) - s45);

        double u = Math.asin((Math.cos(azim) * Math.sin(s))
                - (Math.sin(azim) * Math.cos(s) * Math.cos(d)));
        double deltav = Math.asin((Math.cos(s) * Math.sin(d)) / Math.cos(u));

        double lam = (vk - deltav) / alfa;
        double phi = 0;
        double fi1 = u;

        //iteration calculation
        while (Math.abs(fi1 - phi) > 0.00000000001) {
            fi1 = phi;
            phi = 2. * (Math.atan(Math.pow((1 / k1), -1. / alfa) * Math.pow(
                        Math.tan((u / 2.) + s45), 1. / alfa) * Math.pow(
                        (1. + (e * Math.sin(fi1))) / (1. - (e * Math.sin(fi1))),
                        e / 2.)) - s45);
        }

        if (ptDst != null) {
            ptDst.setLocation(lam, phi);

            return ptDst;
        }

        return new Point2D.Double(lam, phi);
    }

    /**
     * {@inheritDoc}
     */
    protected Point2D transformNormalized(double x, double y, Point2D ptDst)
        throws ProjectionException {
        double fi = y;
        double lam = x;
        double gfi = Math.pow(((1. - (e * Math.sin(fi))) / (1.
                + (e * Math.sin(fi)))), ((alfa * e) / 2.));

        double u = 2. * (Math.atan(((1 / k1) * Math.pow(Math.tan((fi / 2.)
                        + s45), alfa)) * gfi) - s45);

        double v = lam * alfa;

        double deltav = vk - v;
        double s = Math.asin((Math.cos(azim) * Math.sin(u))
                + (Math.sin(azim) * Math.cos(u) * Math.cos(deltav)));
        double d = Math.asin((Math.cos(u) * Math.sin(deltav)) / Math.cos(s));
        double eps = n * d;
        double ro0 = k * radius * (1 / Math.tan(s0));
        double ro = (ro0 * Math.pow(Math.tan((s0 / 2.) + s45), n)) / Math.pow(Math
                .tan((s / 2.) + s45), n);

        /* x and y are reverted  */
        y = -(ro * Math.cos(eps)) / major;
        x = -(ro * Math.sin(eps)) / major;

        if (ptDst != null) {
            ptDst.setLocation(x, y);

            return ptDst;
        }

        return new Point2D.Double(x, y);
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////
    ////////                                                                          ////////
    ////////                                 PROVIDER                                 ////////
    ////////                                                                          ////////
    //////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////
    /**
     * The {@link
     * org.geotools.referencing.operation.MathTransformProvider} for an {@link
     * Krovak krovak} projection.
     *
     * @author jezekjan
     *
     * @see org.geotools.referencing.operation.DefaultMathTransformFactory
     */
    public static class Provider extends AbstractProvider {
        public static final ParameterDescriptor SCALE_FACTOR = createDescriptor(new NamedIdentifier[] {
                    new NamedIdentifier(Citations.OGC, "scale_factor"),
                    new NamedIdentifier(Citations.EPSG,
                        "Scale factor at natural origin"),
                    new NamedIdentifier(Citations.GEOTIFF, "ScaleAtNatOrigin"),
                    new NamedIdentifier(Citations.GEOTIFF, "ScaleAtCenter")
                }, 0.9999, 0, Double.POSITIVE_INFINITY, Unit.ONE);

        /**
         * The operation parameter descriptor for the {@linkPlain
         * #centralMeridian central meridian} parameter value. Valid values
         * range is from -180 to 180. Default value is  Default value is 24°50' (= 42°50' from Ferro).
         */
        public static final ParameterDescriptor CENTRAL_MERIDIAN = createDescriptor(new NamedIdentifier[] {
                    new NamedIdentifier(Citations.OGC, "central_meridian"),
                    new NamedIdentifier(Citations.EPSG,
                        "Longitude of natural origin"),
                    new NamedIdentifier(Citations.EPSG,
                        "Longitude of false origin"),
                    new NamedIdentifier(Citations.ESRI, "Longitude_Of_Origin"),
                    new NamedIdentifier(Citations.ESRI, "Longitude_Of_Center"), //ESRI uses this in orthographic (not to be confused with Longitude_Of_Center in oblique mercator)
                new NamedIdentifier(Citations.GEOTIFF, "NatOriginLong")
                }, 42.5-17.66666666666667, -180, 180, NonSI.DEGREE_ANGLE);

        /**
         * The operation parameter descriptor for the {@linkPlain
         * #latitudeOfOrigin latitude of origin} parameter value. Valid values
         * range is from -90 to 90. Default value is 49.5.
         */
        public static final ParameterDescriptor LATITUDE_OF_ORIGIN = createDescriptor(new NamedIdentifier[] {
                    new NamedIdentifier(Citations.OGC, "latitude_of_origin"),
                    new NamedIdentifier(Citations.EPSG,
                        "Latitude of false origin"),
                    new NamedIdentifier(Citations.EPSG,
                        "Latitude of natural origin"),
                    new NamedIdentifier(Citations.ESRI, "Latitude_Of_Center"), //ESRI uses this in orthographic 
                new NamedIdentifier(Citations.GEOTIFF, "NatOriginLat")
                }, 49.5, -90, 90, NonSI.DEGREE_ANGLE);

        /**
         * The operation parameter descriptor for the {@linkPlain
         * #latitudeOfOrigin latitude of origin} parameter value. Valid values
         * range is from -90 to 90. Default value is 30.28813972222.
         */
        public static final ParameterDescriptor AZIMUTH = createDescriptor(new NamedIdentifier[] {
                    new NamedIdentifier(Citations.GEOTIFF, "Azimuth")
                }, 30.28813972222222, 0, 360, NonSI.DEGREE_ANGLE);

        /**
         * The operation parameter descriptor for the pseudo
         * standard parallel 1 parameter value. Valid values range is from -90
         * to 90. Default value is 78.5.
         */
        public static final ParameterDescriptor PSEUDO_STANDARD_PARALLEL_1 = createDescriptor(new NamedIdentifier[] {
                    new NamedIdentifier(Citations.OGC,
                        "pseudo_standard_parallel_1"),
                    new NamedIdentifier(Citations.EPSG,
                        "Latitude of Pseudo Standard Parallel"),
                }, 78.5, -90, 90, NonSI.DEGREE_ANGLE);

        /** The parameters group. */
        static final ParameterDescriptorGroup PARAMETERS = createDescriptorGroup(new NamedIdentifier[] {
                    new NamedIdentifier(Citations.OGC, "Krovak"),
                    new NamedIdentifier(Citations.GEOTIFF, "Krovak"),
                    new NamedIdentifier(Citations.EPSG,
                        "Krovak Oblique Conformal Conic"),
                    new NamedIdentifier(Citations.EPSG, "9819"),
                },
                new ParameterDescriptor[] {
                    SEMI_MAJOR, SEMI_MINOR, AZIMUTH, CENTRAL_MERIDIAN,
                    LATITUDE_OF_ORIGIN, FALSE_EASTING, FALSE_NORTHING,
                    SCALE_FACTOR, PSEUDO_STANDARD_PARALLEL_1
                });

/**
         * Constructs a new provider. 
         */
        public Provider() {
            super(PARAMETERS);
        }

        /**
         * Returns the operation type for this map projection.
         *
         * @return operation type for this map projection.
         */
        protected Class getOperationType() {
            return ConicProjection.class;
        }

        /**
         * Creates a transform from the specified group of
         * parameter values.
         *
         * @param parameters The group of parameter values.
         *
         * @return The created math transform.
         *
         * @throws ParameterNotFoundException if a required parameter was not
         *         found.
         */
        public MathTransform createMathTransform(
            final ParameterValueGroup parameters)
            throws ParameterNotFoundException {
            return new Krovak(parameters);
        }
    }
}
