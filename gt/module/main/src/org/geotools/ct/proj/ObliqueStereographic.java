/*
 * Geotools 2 - OpenSource mapping toolkit
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
 * Contacts:
 *     UNITED KINGDOM: James Macgill
 *             mailto:j.macgill@geog.leeds.ac.uk
 *
 *     FRANCE: Surveillance de l'Environnement Assistée par Satellite
 *             Institut de Recherche pour le Développement / US-Espace
 *             mailto:seasnet@teledetection.fr
 *
 *     CANADA: Observatoire du Saint-Laurent
 *             Institut Maurice-Lamontagne
 *             mailto:osl@osl.gc.ca
 *
 *    This package contains formulas from the PROJ package of USGS.
 *    USGS's work is fully acknowledged here.
 */
/*
** libproj -- library of cartographic projections
** Copyright (c) 2003   Gerald I. Evenden
**
** Permission is hereby granted, free of charge, to any person obtaining
** a copy of this software and associated documentation files (the
** "Software"), to deal in the Software without restriction, including
** without limitation the rights to use, copy, modify, merge, publish,
** distribute, sublicense, and/or sell copies of the Software, and to
** permit persons to whom the Software is furnished to do so, subject to
** the following conditions:
**
** The above copyright notice and this permission notice shall be
** included in all copies or substantial portions of the Software.
**
** THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
** EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
** MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
** IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
** CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
** TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
** SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package org.geotools.ct.proj;

// J2SE dependencies
import java.awt.geom.Point2D;

// Geotools dependencies
import org.geotools.cs.Projection;
import org.geotools.ct.MissingParameterException;

// Resources
import org.geotools.resources.cts.Resources;
import org.geotools.resources.cts.ResourceKeys;


/**
 * The USGS oblique/equatorial case of the {@linkplain Stereographic stereographic} 
 * projection. This is similar but <strong>NOT</strong> equal to EPSG code 9809.
 *
 * @version $Id$
 * @author André Gosselin
 * @author Martin Desruisseaux
 * @author Rueben Schulz
 *
 * @deprecated
 */
public class ObliqueStereographic extends Stereographic {
    /**
     * A constant used in the transformations.
     * This is <strong>not</strong> equal to the {@link #scaleFactor}.
     */
    final double k0;

    /**
     * Constants used for the oblique projections.
     */
    final double sinphi0, cosphi0, chi1, sinChi1, cosChi1;

    /**
     * Construct an oblique stereographic projection.
     *
     * @param  parameters The parameter values in standard units.
     * @throws MissingParameterException if a mandatory parameter is missing.
     */
    protected ObliqueStereographic(final Projection parameters) throws MissingParameterException {
        super(parameters);
        if (Math.abs(latitudeOfOrigin) < EPS) {    //Equitorial
            cosphi0 = 1.0;
            sinphi0 = 0.0;
            chi1    = 0.0;
            cosChi1 = 1.0;
            sinChi1 = 0.0;
            latitudeOfOrigin = 0;
        } else {                                   //Oblique
            cosphi0 = Math.cos(latitudeOfOrigin);
            sinphi0 = Math.sin(latitudeOfOrigin);
            chi1    = 2.0 * Math.atan(ssfn(latitudeOfOrigin, sinphi0)) - (Math.PI/2);
            cosChi1 = Math.cos(chi1);
            sinChi1 = Math.sin(chi1);
        }
        // part of (14 - 15)
        k0  = 2.0*msfn(sinphi0, cosphi0);
    }

    /**
     * Transforms the specified (<var>x</var>,<var>y</var>) coordinate (units in radians)
     * and stores the result in <code>ptDst</code> (linear distance on a unit sphere).
     *
     * @param  x The longitude in radians.
     * @param  y The latitude in radians.
     * @param  ptDst The destination point, or <code>null</code>.
     * @return The projected point in meters.
     * @throws ProjectionException if the projection failed.
     */
    protected Point2D transformNormalized(double x, double y, final Point2D ptDst)
            throws ProjectionException
    {
        final double chi = 2.0 * Math.atan(ssfn(y, Math.sin(y))) - (Math.PI/2);
        final double sinChi = Math.sin(chi);
        final double cosChi = Math.cos(chi);
        final double cosChi_coslon = cosChi*Math.cos(x);
        final double A = k0 / cosChi1 / (1 + sinChi1*sinChi + cosChi1*cosChi_coslon);
        x = A * cosChi*Math.sin(x);
        y = A * (cosChi1*sinChi - sinChi1*cosChi_coslon);

        if (ptDst != null) {
            ptDst.setLocation(x,y);
            return ptDst;
        }
        return new Point2D.Double(x,y);
    }

    /**
     * Transforms the specified (<var>x</var>,<var>y</var>) coordinate (linear distance
     * on a unit sphere) and stores the result in <code>ptDst</code> (units in radians).
     *
     * @param  x The <var>x</var> ordinate in meters.
     * @param  y The <var>y</var> ordinate in meters.
     * @param  ptDst The destination point, or <code>null</code>.
     * @return The geographic point in radians.
     * @throws ProjectionException if the projection failed.
     */
    protected Point2D inverseTransformNormalized(double x, double y, final Point2D ptDst)
            throws ProjectionException
    {
        final double rho = Math.sqrt(x*x + y*y);
        final double ce = 2.0 * Math.atan2(rho*cosChi1, k0);
        final double cosce = Math.cos(ce);
        final double since = Math.sin(ce);
        final double chi = (Math.abs(rho)>=EPS) ? 
            Math.asin(cosce*sinChi1 + (y*since*cosChi1 / rho)) : chi1;
        final double tp = Math.tan(Math.PI/4.0 + chi/2.0);
        
        //parts of (21-36) used to calculate longitude
        final double t = x*since;
        final double ct = rho*cosChi1*cosce - y*sinChi1*since;
        
        /*
         * Compute latitude using iterative technique (3-4).
         */
        final double halfe = e/2.0;
        double phi0 = chi;
        for (int i=MAX_ITER;;) {
            final double esinphi = e*Math.sin(phi0);
            final double phi = 2*Math.atan(tp*Math.pow((1+esinphi)/(1-esinphi), halfe))-(Math.PI/2);
            if (Math.abs(phi-phi0) < TOL) {
                // TODO: checking rho may be redundant
                x = (Math.abs(rho)<EPS) || (Math.abs(t)<EPS && Math.abs(ct)<EPS) ? 
                     0.0 : Math.atan2(t, ct);
                y = phi;
                break;
            }
            phi0 = phi;
            if (--i < 0) {
                throw new ProjectionException(Resources.format(ResourceKeys.ERROR_NO_CONVERGENCE));
            }
        }

        if (ptDst != null) {
            ptDst.setLocation(x,y);
            return ptDst;
        }
        return new Point2D.Double(x,y);
    }

    /**
     * Compute part of function (3-1) from Snyder
     */
    final double ssfn(double phi, double sinphi) {
        sinphi *= e;
        return Math.tan((Math.PI/4.0) + phi/2.0) *
               Math.pow((1-sinphi) / (1+sinphi), e/2.0);
    }

    /**
     * Returns a hash value for this map projection.
     */
    public int hashCode() {
        final long code = Double.doubleToLongBits(k0);
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
            final ObliqueStereographic that = (ObliqueStereographic) object;
            return equals(this.     k0,   that.     k0) &&
                   equals(this.sinphi0,   that.sinphi0) &&
                   equals(this.cosphi0,   that.cosphi0) &&
                   equals(this.   chi1,   that.   chi1) &&
                   equals(this.sinChi1,   that.sinChi1) &&
                   equals(this.cosChi1,   that.cosChi1);
        }
        return false;
    }




    /**
     * Provides the transform equations for the spherical case of the 
     * Stereographic projection.
     *
     * @version $Id$
     * @author Martin Desruisseaux
     * @author Rueben Schulz
     */
    static final class Spherical extends ObliqueStereographic {
        /**
         * A constant used in the transformations. This constant hides the <code>k0</code>
         * constant from the ellipsoidal case. The spherical and ellipsoidal <code>k0</code>
         * are not computed in the same way, and we preserve the ellipsoidal <code>k0</code>
         * in {@link Stereographic} in order to allow assertions to work.
         */
        private static final double k0 = 2;

        /**
         * Construct a new map projection from the suplied parameters.
         *
         * @param  parameters The parameter values in standard units.
         * @throws MissingParameterException if a mandatory parameter is missing.
         */
        protected Spherical(final Projection parameters) throws MissingParameterException {
            super(parameters);
            assert isSpherical;
        }

        /**
         * Transforms the specified (<var>x</var>,<var>y</var>) coordinate (units in radians)
         * and stores the result in <code>ptDst</code> (linear distance on a unit sphere).
         */
        protected Point2D transformNormalized(double x, double y, Point2D ptDst)
                throws ProjectionException
        {
            //Compute using ellipsoidal formulas, for comparaison later.
            assert (ptDst = super.transformNormalized(x, y, ptDst)) != null;
            
            final double coslat = Math.cos(y);
            final double sinlat = Math.sin(y);
            final double coslon = Math.cos(x);
            double f = 1.0 + sinphi0*sinlat + cosphi0*coslat*coslon; // (21-4)
            if (f < EPS) {
                throw new ProjectionException(Resources.format(
                          ResourceKeys.ERROR_VALUE_TEND_TOWARD_INFINITY));
            }
            f = k0/f;
            x = f * coslat * Math.sin(x);                           // (21-2)
            y = f * (cosphi0 * sinlat - sinphi0 * coslat * coslon); // (21-3)

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
         * and stores the result in <code>ptDst</code>.
         */
        protected Point2D inverseTransformNormalized(double x, double y, Point2D ptDst)
                throws ProjectionException
        {
            // Compute using ellipsoidal formulas, for comparaison later.
            assert (ptDst = super.inverseTransformNormalized(x, y, ptDst)) != null;

            final double rho = Math.sqrt(x*x + y*y);
            if (Math.abs(rho) < EPS) {
                y = latitudeOfOrigin;
                x = 0.0;
            } else {
                final double c = 2.0 * Math.atan(rho/k0);
                final double cosc = Math.cos(c);
                final double sinc = Math.sin(c);
                final double ct = rho*cosphi0*cosc - y*sinphi0*sinc; // (20-15)
                final double t  = x*sinc;                            // (20-15)
                y = Math.asin(cosc*sinphi0 + y*sinc*cosphi0/rho);    // (20-14)
                x = (Math.abs(ct)<EPS && Math.abs(t)<EPS) ? 
                     0.0 : Math.atan2(t, ct);
            }

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
     * Provides the transform equations for the Oblique Stereographic (EPSG code 9809).
     * The formulas used below are not from the EPSG, but rather those of the 
     * "Oblique Stereographic Alternative" in the <code>libproj4</code> package 
     * written by Gerald Evenden. His work is acknowledged here and greatly appreciated. 
     * <br><br>
     * 
     * The forward equations used in libproj4 are the same as those given in the 
     * UNB reports for the Double Stereographic. The inverse equations are similar,
     * but use different methods to itterate the for the lattitude.
     * <br><br>
     * 
     * <strong>References:</strong><ul>
     *   <li><code>libproj4</code> is available at
     *       <A HREF="http://members.bellatlantic.net/~vze2hc4d/proj4/">libproj4 Miscellanea</A><br>
     *        Relevent files are: <code>PJ_sterea.c</code>, <code>pj_gauss.c</code>,
     *        <code>pj_fwd.c</code>, <code>pj_inv.c</code> and <code>lib_proj.h</code></li>
     *   <li>Gerald Evenden. <A HREF="http://members.bellatlantic.net/~vze2hc4d/proj4/sterea.pdf">
     *       "Supplementary PROJ.4 Notes - Oblique Stereographic Alternative"</A></li>
     *   <li>"Coordinate Conversions and Transformations including Formulas",
     *       EPSG Guidence Note Number 7, Version 19.</li>
     *   <li>Krakiwsky, E.J., D.B. Thomson, and R.R. Steeves. 1977. A Manual 
     *       For Geodetic Coordinate Transformations in the Maritimes. 
     *       Geodesy and Geomatics Engineering, UNB. Technical Report No. 48.</li>
     *   <li>Thomson, D.B., M.P. Mepham and R.R. Steeves. 1977. 
     *       The Stereographic Double Projection. 
     *       Surveying Engineering, University of New Brunswick. Technical Report No. 46.</li>
     * </ul>
     *
     * @version $Id$
     * @author Rueben Schulz
     */
    static final class EPSG extends ObliqueStereographic { 
        /*
         * Contstants used in the forward and inverse gauss methods.
         */ 
        private final double C, K, ratexp;

        /*
         * Constants for the epsg stereographic transform.
         */
        private final double phic0, cosc0, sinc0, R2; 

        /*
         * The tolerance used for the inverse itteration. This is smaller
         * than the tolerance in the {@link MapProjection} superclass.
         */
        private static final double TOL = 1E-14;

        /**
         * Construct a new map projection from the supplied parameters.
         *
         * @param  parameters The parameter values in standard units.
         * @throws MissingParameterException if a mandatory parameter is missing.
         */
        protected EPSG(final Projection parameters) throws MissingParameterException {
            super(parameters);

            // Compute constants
            final double sphi = Math.sin(latitudeOfOrigin);
            double cphi = Math.cos(latitudeOfOrigin);  
            cphi *= cphi;
            R2 = 2.0*Math.sqrt(1. - es) / (1. - es * sphi * sphi);

            C = Math.sqrt(1. + es * cphi * cphi / (1. - es));
            phic0 = Math.asin(sphi / C);
            sinc0 = Math.sin(phic0);
            cosc0 = Math.cos(phic0);

            ratexp = 0.5 * C * e;
            K = Math.tan(.5 * phic0 + Math.PI/4) / 
                    (Math.pow(Math.tan(.5 * latitudeOfOrigin + Math.PI/4), C) *
                    srat(e * sphi, ratexp));
        }

        /**
         * Transforms the specified (<var>x</var>,<var>y</var>) coordinate
         * and stores the result in <code>ptDst</code>.
         */
        protected Point2D transformNormalized(double x, double y, Point2D ptDst)
                throws ProjectionException 
        {  
            y = 2. * Math.atan(K *Math.pow(Math.tan(.5 * y + Math.PI/4), C) *
                                  srat(e * Math.sin(y), ratexp)) - Math.PI/2;
            x *= C;
            double sinc = Math.sin(y);
            double cosc = Math.cos(y);
            double cosl = Math.cos(x);
            double k = R2 / (1. + sinc0 * sinc + cosc0 * cosc * cosl);
            x = k * cosc * Math.sin(x);
            y = k * (cosc0 * sinc - sinc0 * cosc * cosl);

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
            final double rho = Math.sqrt(x*x + y*y);
            
            if (Math.abs(rho) < EPS) {
                x = 0.0;
                y = phic0;
            } else {
                final double ce = 2. * Math.atan2(rho, R2);
                final double sinc = Math.sin(ce);
                final double cosc = Math.cos(ce);
                x = Math.atan2(x * sinc, rho * cosc0 * cosc - y * sinc0 * sinc);
                y = (cosc * sinc0) + (y * sinc * cosc0 / rho);

                if (Math.abs(y) >= 1.0) {
                    y = (y < 0.0) ? -Math.PI/2.0 : Math.PI/2.0;
                } else {
                    y = Math.asin(y);
                }           
            }
            
            // Begin pj_inv_gauss(...) method inlined
            x /= C;
            double num = Math.pow(Math.tan(.5 * y + Math.PI/4.0)/K, 1./C);
            for (int i=MAX_ITER;;) {
                double phi = 2.0 * Math.atan(num * srat(e * Math.sin(y), - 0.5 * e)) - Math.PI/2.0;
                if (Math.abs(phi - y) < TOL) {
                    break;
                }
                y = phi;
                if (--i < 0) {
                    throw new ProjectionException(Resources.format(ResourceKeys.ERROR_NO_CONVERGENCE));
                }
            }
            // End pj_inv_gauss(...) method inlined
            
            if (ptDst != null) {
                ptDst.setLocation(x,y);
                return ptDst;
            }
            return new Point2D.Double(x,y);
        }      

        /*
         * A simple function used by the transforms.
         */
        private static double srat(double esinp, double exp) {
            return Math.pow((1.-esinp)/(1.+esinp), exp);
        }
    }    
}
