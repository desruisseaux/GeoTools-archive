/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, 2004, Geotools Project Managment Committee (PMC)
 * (C) 2001, Institut de Recherche pour le D�veloppement
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
 *     FRANCE: Surveillance de l'Environnement Assist�e par Satellite
 *             Institut de Recherche pour le D�veloppement / US-Espace
 *             mailto:seasnet@teledetection.fr
 *
 *     CANADA: Observatoire du Saint-Laurent
 *             Institut Maurice-Lamontagne
 *             mailto:osl@osl.gc.ca
 *
 *    This package contains formulas from the PROJ package of USGS.
 *    USGS's work is fully acknowledged here.
 */
package org.geotools.ct.proj;

// J2SE dependencies
import java.util.Locale;

import org.geotools.cs.Projection;
import org.geotools.ct.MathTransform;
import org.geotools.ct.MissingParameterException;
import org.geotools.resources.i18n.VocabularyKeys;
import org.geotools.resources.i18n.Vocabulary;


/**
 * Stereographic Projection. The directions starting from the central point are true,
 * but the areas and the lengths become increasingly deformed as one moves away from
 * the center.  This projection is used to represent polar areas.  It can be adapted
 * for other areas having a circular form.
 * <br><br>
 *
 * This implementation, and its subclasses, provides transforms for four cases of the  
 * stereographic projection:
 * <ul>
 *   <li><code>"Oblique_Stereographic"</code> (EPSG code 9809), alias <code>"Double Stereographic"</code>
 *       in ESRI software</li>
 *   <li><code>"Stereographic"</code> in ESRI software (<strong>NOT</strong> EPSG code 9809)</li>
 *   <li><code>"Polar_Stereographic"</code> (EPSG code 9810, uses iteration for the inverse)</li>
 *   <li><code>"Polar_Stereographic_Series"</code> (EPSG code 9810), uses a series for the inverse.
 *        This is a little bit faster, but may be a little bit less accurate)</li>
 * </ul>   
 *
 * Both the <code>"Oblique_Stereographic"</code> and <code>"Stereographic"</code> 
 * projections are "double" projections involving two parts: 1) a conformal
 * transformation of the geographic coordinates to a sphere and 2) a spherical
 * Stereographic projection. The EPSG considers both methods to be valid, but 
 * considers them to be a different coordinate operation method.
 * <br><br>
 *
 * The <code>"Stereographic"</code> case uses the USGS equations of Snyder.
 * This uses a simplified conversion to the conformal sphere that computes 
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
 * (EPSG guidence note 7 and libproj) calculate the same values. Until these 
 * differences are resolved, please be careful when using this projection.
 * <br><br>
 *
 * If a <code>"latitude_of_origin"</code> parameter is supplied and is not consistent with the
 * projection classification (for example a latitude different from &plusmn;90� for the polar case),
 * then the oblique or polar case will be automatically inferred from the latitude. In other
 * words, the latitude of origin has precedence on the projection classification. If ommited,
 * then the default value is 90�N for <code>"Polar_Stereographic"</code> and 0� for
 * <code>"Oblique_Stereographic"</code>.
 * <br><br>
 *
 * The <code>"latitude_true_scale"</code> parameter is not specified by the EPSG and is
 * only used for the <code>"Polar_Stereographic"</code> case.
 * The <code>"Polar_Stereographic_Series"</code> does not include this parameter.
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
 * @author Andr� Gosselin
 * @author Martin Desruisseaux
 * @author Rueben Schulz
 *
 * @task TODO: Declares that <code>"Stereographic"</code> is an ESRI name.
 *             Add the <code>"Double Stereographic"</code> alias (from ESRI)
 *             for <code>"Oblique_Stereographic"</code>.
 *
 * @deprecated Replaced by {@link org.geotools.referencing.operation.projection.Stereographic}.
 */
public abstract class Stereographic extends PlanarProjection {
    /**
     * Informations about a {@link Stereographic} projection. The {@link #create} method infer
     * the kind of projection ({@link PolarStereographic} or {@link ObliqueStereographic} from
     * the latitude of origin. If the latitude of origin is not explicitely specified, then the
     * default value is 90�N for <code>"Polar_Stereographic"</code> and 0� for
     * <code>"Oblique_Stereographic"</code>.
     *
     * @version $Id$
     * @author Martin Desruisseaux
     * @author Rueben Schulz
     */
    static final class Provider extends org.geotools.ct.proj.Provider {
        /**
         * <code>true</code> for polar stereographic, or
         * <code>false</code> for equatorial and oblique
         * stereographic.
         */
        private final boolean polar;

        /**
         * <code>true</code> if using the EPSG oblique equations or a series for the 
         * inverse polar stereographic.
         */
        private final boolean EPSG;
                
        /**
         * Construct a provider for polar or oblique stereographic using USGS equations.
         *
         * @param polar <code>true</code> for polar stereographic, or
         *              <code>false</code> for equatorial and oblique
         *              stereographic.
         */
        public Provider(final boolean polar) {
            this(polar, false);
        }
        
        /**
         * Construct a provider for polar or oblique stereographic.
         *
         * @param polar <code>true</code> for polar stereographic, or
         *              <code>false</code> for equatorial and oblique
         *              stereographic.
         * @param EPSG <code>true</code> for EPSG oblique equations or to use a series
         *             for the polar inverse, or <code>false</code> for USGS equations.
         */
        public Provider(final boolean polar, final boolean EPSG) {
            super(EPSG ? (polar ? "Polar_Stereographic_Series" : "Oblique_Stereographic") :
                          (polar ? "Polar_Stereographic"        : "Stereographic"), 
                          VocabularyKeys.STEREOGRAPHIC_PROJECTION);
            if (polar && !EPSG) {
                //no default, allows default to be decided in PolarStereographic
                put("latitude_true_scale", Double.NaN, LATITUDE_RANGE);
            }
            this.polar = polar;
            this.EPSG  = EPSG;
        }

        /**
         * Create a new stereographic projection. The type of projection (polar or oblique) is
         * automatically inferred from the latitude of origin. If the latitude of origin is not
         * explicitely specified, then the default value is infered from the projection
         * classification.
         */
        public MathTransform create(final Projection parameters) throws MissingParameterException {
            final double latitudeOfOrigin = Math.abs(
                latitudeToRadians(parameters.getValue("latitude_of_origin", polar ? 90 : 0), true));
            final boolean isSpherical = isSpherical(parameters);
            // Polar case.
            if (Math.abs(latitudeOfOrigin - Math.PI/2) < EPS) {
                if (isSpherical) {
                    return new PolarStereographic.Spherical(parameters);
                } else {
                    if (EPSG) {
                        return new PolarStereographic.Series(parameters);
                    } else {
                        return new PolarStereographic(parameters);
                    }
                }
            }
            // Equatorial case.
            if (latitudeOfOrigin < EPS) {
                if (isSpherical) {
                    return new EquatorialStereographic.Spherical(parameters);
                } else if (!EPSG) {
                    return new EquatorialStereographic(parameters);
                }               
            }
            // Generic (oblique) case.
            if (isSpherical) {
                return new ObliqueStereographic.Spherical(parameters);
            } else {
                if (EPSG) { 
                    return new ObliqueStereographic.EPSG(parameters);
                } else {
                    return new ObliqueStereographic(parameters);
                }
            }               
        }
    }

    /**
     * Construct a stereographic transformation from the specified parameters.
     *
     * @param parameters The parameter values in standard units.
     * @throws MissingParameterException if a mandatory parameter is missing.
     */
    protected Stereographic(final Projection parameters) throws MissingParameterException {
        super(parameters);
    }

    /**
     * Returns a human readable name localized for the specified locale.
     */
    public String getName(final Locale locale) {
        return Vocabulary.getResources(locale).getString(VocabularyKeys.STEREOGRAPHIC_PROJECTION);
    }
}
