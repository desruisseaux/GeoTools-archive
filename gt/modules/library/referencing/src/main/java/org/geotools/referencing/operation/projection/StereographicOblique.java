/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *
 *   (C) 2003-2006, Geotools Project Managment Committee (PMC)
 *   (C) 2003, Gerald I. Evenden
 *   (C) 2001, Institut de Recherche pour le Développement
 *   (C) 2000, Frank Warmerdam
 *   (C) 1999, Fisheries and Oceans Canada
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
 *    This package contains formulas from the PROJ package of USGS.
 *    USGS's work is fully acknowledged here. This derived work has
 *    been relicensed under LGPL with Frank Warmerdam's permission.
 */
package org.geotools.referencing.operation.projection;

// J2SE dependencies and extensions
import java.awt.geom.Point2D;

// OpenGIS dependencies
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterNotFoundException;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.operation.MathTransform;

// Geotools dependencies
import org.geotools.resources.XMath;
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.referencing.NamedIdentifier;
import org.geotools.metadata.iso.citation.Citations;


/**
 * The USGS oblique/equatorial case of the Stereographic projection. This is similar but
 * <strong>NOT</strong> equal to EPSG code 9809 ({@code "Oblique_Stereographic"} EPSG name).
 * The later is rather implemented by {@link StereographicDouble}.
 *
 * @since 2.1
 * @source $URL$
 * @version $Id$
 * @author André Gosselin
 * @author Martin Desruisseaux
 * @author Rueben Schulz
 */
public class StereographicOblique extends Stereographic {    
    /**
     * Maximum number of iterations for iterative computations.
     */
    private static final int MAXIMUM_ITERATIONS = 15;

    /**
     * Difference allowed in iterative computations.
     */
    private static final double ITERATION_TOLERANCE = 1E-10;

    /**
     * Maximum difference allowed when comparing real numbers.
     */
    private static final double EPSILON = 1E-6;

    /**
     * Constants used for the oblique projections. All those constants are completly determined by
     * {@link #latitudeOfOrigin}. Concequently, there is no need to test them in {@link #hashCode}
     * or {@link #equals} methods.
     */
    final double k0, sinphi0, cosphi0, chi1, sinChi1, cosChi1;

    /**
     * Constructs an oblique stereographic projection (USGS equations).
     *
     * @param  parameters The group of parameter values.
     * @throws ParameterNotFoundException if a required parameter was not found.
     *
     * @since 2.4
     */
    protected StereographicOblique(final ParameterValueGroup parameters) 
            throws ParameterNotFoundException
    {
        this(parameters, StereographicOblique.Provider.PARAMETERS);
    }

    /**
     * Constructs an oblique stereographic projection (USGS equations).
     *
     * @param  parameters The group of parameter values.
     * @param  descriptor The expected parameter descriptor.
     * @throws ParameterNotFoundException if a required parameter was not found.
     */
    StereographicOblique(final ParameterValueGroup parameters,
                         final ParameterDescriptorGroup descriptor) 
            throws ParameterNotFoundException
    {
        super(parameters, descriptor);
        if (Math.abs(latitudeOfOrigin) < EPSILON) { // Equatorial
            latitudeOfOrigin = 0;
            cosphi0 = 1.0;
            sinphi0 = 0.0;
            chi1    = 0.0;
            cosChi1 = 1.0;
            sinChi1 = 0.0;
        } else {                                    // Oblique
            cosphi0 = Math.cos(latitudeOfOrigin);
            sinphi0 = Math.sin(latitudeOfOrigin);
            chi1    = 2.0 * Math.atan(ssfn(latitudeOfOrigin, sinphi0)) - (Math.PI/2);
            cosChi1 = Math.cos(chi1);
            sinChi1 = Math.sin(chi1);
        }
        // part of (14 - 15)
        k0 = 2.0 * msfn(sinphi0, cosphi0);
    }

    /**
     * Transforms the specified (<var>&lambda;</var>,<var>&phi;</var>) coordinates
     * (units in radians) and stores the result in {@code ptDst} (linear distance
     * on a unit sphere).
     */
    protected Point2D transformNormalized(double x, double y, Point2D ptDst) 
            throws ProjectionException 
    {
        final double chi    = 2.0 * Math.atan(ssfn(y, Math.sin(y))) - (Math.PI/2);
        final double sinChi = Math.sin(chi);
        final double cosChi = Math.cos(chi);
        final double cosChi_cosLon = cosChi*Math.cos(x);
        final double A = k0 / cosChi1 / (1 + sinChi1*sinChi + cosChi1*cosChi_cosLon);
        x = A * cosChi * Math.sin(x);
        y = A * (cosChi1 * sinChi - sinChi1 * cosChi_cosLon);

        if (ptDst != null) {
            ptDst.setLocation(x,y);
            return ptDst;
        }
        return new Point2D.Double(x,y);
    }

    /**
     * Transforms the specified (<var>x</var>,<var>y</var>) coordinates
     * and stores the result in {@code ptDst}.
     */
    protected Point2D inverseTransformNormalized(double x, double y, Point2D ptDst) 
            throws ProjectionException 
    {
        final double  rho    = Math.sqrt(x*x + y*y);
        final double  ce     = 2.0 * Math.atan2(rho*cosChi1, k0);
        final double  cosce  = Math.cos(ce);
        final double  since  = Math.sin(ce);
        final boolean rhoIs0 = Math.abs(rho) < EPSILON;
        final double  chi    = rhoIs0 ? chi1 : Math.asin(cosce*sinChi1 + (y*since*cosChi1 / rho));
        final double  tp     = Math.tan(Math.PI/4.0 + chi/2.0);

        // parts of (21-36) used to calculate longitude
        final double t  = x*since;
        final double ct = rho*cosChi1*cosce - y*sinChi1*since;

        // Compute latitude using iterative technique (3-4)
        final double halfe = excentricity / 2.0;
        double phi0 = chi;
        for (int i=MAXIMUM_ITERATIONS;;) {
            final double esinphi = excentricity * Math.sin(phi0);
            final double phi = 2*Math.atan(tp*Math.pow((1+esinphi)/(1-esinphi), halfe))-(Math.PI/2);
            if (Math.abs(phi-phi0) < ITERATION_TOLERANCE) {
                // TODO: checking rho may be redundant
                x = rhoIs0 || (Math.abs(t)<EPSILON && Math.abs(ct)<EPSILON) ? 0.0 : Math.atan2(t, ct);
                y = phi;
                break;
            }
            phi0 = phi;
            if (--i < 0) {
                throw new ProjectionException(Errors.format(ErrorKeys.NO_CONVERGENCE));
            }
        }

        if (ptDst != null) {
            ptDst.setLocation(x,y);
            return ptDst;
        }
        return new Point2D.Double(x,y);
    }

    /**
     * Maximal error (in metres) tolerated for assertions, if enabled.
     */
    //@Override
    protected double getToleranceForAssertions(final double longitude, final double latitude) {
        final double delta = Math.abs(longitude - centralMeridian)/2 +
                             Math.abs(latitude  - latitudeOfOrigin);
        if (delta > 40) {
            return 0.5;
        }
        if (delta > 15) {
            return 0.1;
        }
        return super.getToleranceForAssertions(longitude, latitude);
    }

    /**
     * Computes part of function (3-1) from Snyder.
     */
    final double ssfn(double phi, double sinphi) {
        sinphi *= excentricity;
        return Math.tan((Math.PI/4.0) + phi/2.0) *
               Math.pow((1-sinphi) / (1+sinphi), excentricity/2.0);
    }


    /**
     * Provides the transform equations for the spherical case of the 
     * Stereographic projection.
     *
     * @version $Id$
     * @author Martin Desruisseaux
     * @author Rueben Schulz
     */
    static final class Spherical extends StereographicOblique {
        /**
         * A constant used in the transformations. This constant hides the {@code k0}
         * constant from the ellipsoidal case. The spherical and ellipsoidal {@code k0}
         * are not computed in the same way, and we preserve the ellipsoidal {@code k0}
         * in {@link Stereographic} in order to allow assertions to work.
         */
        private static final double k0 = 2;

        /**
         * Constructs a spherical oblique stereographic projection.
         *
         * @param  parameters The group of parameter values.
         * @param  descriptor The expected parameter descriptor.
         * @throws ParameterNotFoundException if a required parameter was not found.
         */
        Spherical(final ParameterValueGroup parameters, final ParameterDescriptorGroup descriptor) 
                throws ParameterNotFoundException 
        {
            super(parameters, descriptor);
            ensureSpherical();
        }

        /**
         * Transforms the specified (<var>&lambda;</var>,<var>&phi;</var>) coordinates
         * (units in radians) and stores the result in {@code ptDst} (linear distance
         * on a unit sphere).
         */
        protected Point2D transformNormalized(double x, double y, Point2D ptDst)
                throws ProjectionException 
        {
            // Compute using ellipsoidal formulas, for comparaison later.
            assert (ptDst = super.transformNormalized(x, y, ptDst)) != null;

            final double coslat = Math.cos(y);
            final double sinlat = Math.sin(y);
            final double coslon = Math.cos(x);
            double f = 1.0 + sinphi0*sinlat + cosphi0*coslat*coslon; // (21-4)
            if (f < EPSILON) {
                throw new ProjectionException(Errors.format(
                          ErrorKeys.VALUE_TEND_TOWARD_INFINITY));
            }
            f = k0 / f;
            x = f * coslat * Math.sin(x);                           // (21-2)
            y = f * (cosphi0 * sinlat - sinphi0 * coslat * coslon); // (21-3)

            assert checkTransform(x, y, ptDst);
            if (ptDst != null) {
                ptDst.setLocation(x,y);
                return ptDst;
            }
            return new Point2D.Double(x,y);
        }

        /**
         * Transforms the specified (<var>x</var>,<var>y</var>) coordinates
         * and stores the result in {@code ptDst}.
         */
        protected Point2D inverseTransformNormalized(double x, double y, Point2D ptDst)
                throws ProjectionException 
        {
            // Compute using ellipsoidal formulas, for comparaison later.
            assert (ptDst = super.inverseTransformNormalized(x, y, ptDst)) != null;

            final double rho = Math.sqrt(x*x + y*y);
            if (Math.abs(rho) < EPSILON) {
                y = latitudeOfOrigin;
                x = 0.0;
            } else {
                final double c    = 2.0 * Math.atan(rho/k0);
                final double cosc = Math.cos(c);
                final double sinc = Math.sin(c);
                final double ct   = rho*cosphi0*cosc - y*sinphi0*sinc; // (20-15)
                final double t    = x*sinc;                            // (20-15)
                y = Math.asin(cosc*sinphi0 + y*sinc*cosphi0/rho);      // (20-14)
                x = (Math.abs(ct)<EPSILON && Math.abs(t)<EPSILON) ? 0.0 : Math.atan2(t, ct);
            }

            assert checkInverseTransform(x, y, ptDst);
            if (ptDst != null) {
                ptDst.setLocation(x,y);
                return ptDst;
            }
            return new Point2D.Double(x,y);
        }
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
     * provider} for a {@linkplain Stereographic Stereographic} projections using USGS equations.
     *
     * @since 2.4
     * @version $Id$
     * @author Rueben Schulz
     *
     * @see org.geotools.referencing.operation.DefaultMathTransformFactory
     */
    public static final class Provider extends Stereographic.Provider {
        /**
         * The parameters group.
         */
        static final ParameterDescriptorGroup PARAMETERS = createDescriptorGroup(new NamedIdentifier[] {
                new NamedIdentifier(Citations.ESRI,     "Stereographic"),
                new NamedIdentifier(Citations.GEOTIFF,  "CT_Stereographic"),
                new NamedIdentifier(Citations.GEOTOOLS, NAME)
            }, new ParameterDescriptor[] {
                SEMI_MAJOR,          SEMI_MINOR,
                CENTRAL_MERIDIAN,    LATITUDE_OF_ORIGIN,
                SCALE_FACTOR,
                FALSE_EASTING,       FALSE_NORTHING
            });

        /**
         * Constructs a new provider. 
         */
        public Provider() {
            super(PARAMETERS);
        }

        /**
         * Creates the general case.
         */
        //@Override
        MathTransform createMathTransform(final ParameterValueGroup parameters,
                                          final ParameterDescriptorGroup descriptor)
                throws ParameterNotFoundException
        {
            return new StereographicOblique(parameters, descriptor);
        }
    }
}
