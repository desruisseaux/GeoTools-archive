/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Managment Committee (PMC)
 * (C) 2001, Institut de Recherche pour le D�veloppement
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
 *    This package contains documentation from OpenGIS specifications.
 *    OpenGIS consortium's work is fully acknowledged here.
 */
package org.geotools.referencing.crs;

// J2SE dependencies
import java.util.Collections;
import java.util.Map;

import javax.units.SI;

import org.geotools.referencing.IdentifiedObject;
import org.geotools.referencing.cs.CartesianCS;
import org.geotools.referencing.wkt.Formatter;
import org.geotools.resources.Utilities;
import org.geotools.resources.cts.ResourceKeys;
import org.geotools.resources.cts.Resources;
import org.geotools.util.NameFactory;
import org.opengis.referencing.cs.CoordinateSystem;
import org.opengis.referencing.datum.EngineeringDatum;
import org.opengis.util.GenericName;
import org.opengis.util.InternationalString;
 

/**
 * A contextually local coordinate reference system. It can be divided into two broad categories:
 * <ul>
 *   <li>earth-fixed systems applied to engineering activities on or near the surface of the
 *       earth;</li>
 *   <li>CRSs on moving platforms such as road vehicles, vessels, aircraft, or spacecraft.</li>
 * </ul>
 *
 * <TABLE CELLPADDING='6' BORDER='1'>
 * <TR BGCOLOR="#EEEEFF"><TH NOWRAP>Used with CS type(s)</TH></TR>
 * <TR><TD>
 *   {@link org.opengis.referencing.cs.CartesianCS    Cartesian},
 *   {@link org.opengis.referencing.cs.AffineCS       Affine},
 *   {@link org.opengis.referencing.cs.EllipsoidalCS  Ellipsoidal},
 *   {@link org.opengis.referencing.cs.SphericalCS    Spherical},
 *   {@link org.opengis.referencing.cs.CylindricalCS  Cylindrical},
 *   {@link org.opengis.referencing.cs.PolarCS        Polar},
 *   {@link org.opengis.referencing.cs.VerticalCS     Vertical},
 *   {@link org.opengis.referencing.cs.LinearCS       Linear}
 * </TD></TR></TABLE>
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class EngineeringCRS extends org.geotools.referencing.crs.SingleCRS
                         implements org.opengis.referencing.crs.EngineeringCRS
{
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 6695541732063382701L;

    /**
     * A cartesian local coordinate system.
     */
    private static final class Cartesian extends EngineeringCRS {
        /** Serial number for interoperability with different versions. */
        private static final long serialVersionUID = -1773381554353809683L;

        /** The alias to use for all Cartesian CS. */
        private static GenericName ALIAS = NameFactory.create(new InternationalString[] {
                       Resources.formatInternational(ResourceKeys.CARTESIAN)});

        /** Construct a coordinate system with the given name. */
        public Cartesian(final String name, final CoordinateSystem cs) {
            super(name, org.geotools.referencing.datum.EngineeringDatum.UNKNOW, cs);
        }

        /** Returns the localized name for "Cartesian". */
        public GenericName[] getAlias() {
            return new GenericName[] {ALIAS};
        }

        /**
         * Compares the specified object to this CRS for equality. This method is overriden
         * because, otherwise, <code>CARTESIAN_xD</code> and <code>GENERIC_xD</code> would
         * be considered equals when metadata are ignored.
         */
        public boolean equals(final IdentifiedObject object, final boolean compareMetadata) {
            if (super.equals(object, compareMetadata)) {
                if (compareMetadata) {
                    // No need to performs the check below if metadata were already compared.
                    return true;
                }
                final Cartesian that = (Cartesian) object;
                return Utilities.equals(this.getName().getCode(), that.getName().getCode());
            }
            return false;
        }
    }

    /**
     * A two-dimensional cartesian coordinate reference system with
     * {@linkplain org.geotools.referencing.cs.CoordinateSystemAxis#X x},
     * {@linkplain org.geotools.referencing.cs.CoordinateSystemAxis#Y y}
     * axis in {@linkplain SI#METER metres}. By default, this CRS has no transformation
     * path to any other CRS (i.e. a map using this CS can't be reprojected to a
     * {@linkplain GeographicCRS geographic coordinate reference system} for example).
     */
    public static final EngineeringCRS CARTESIAN_2D = new Cartesian("Cartesian",
                                                                    CartesianCS.GENERIC_2D);

    /**
     * A three-dimensional cartesian coordinate reference system with
     * {@linkplain org.geotools.referencing.cs.CoordinateSystemAxis#X x},
     * {@linkplain org.geotools.referencing.cs.CoordinateSystemAxis#Y y},
     * {@linkplain org.geotools.referencing.cs.CoordinateSystemAxis#Z z}
     * axis in {@linkplain SI#METER metres}. By default, this CRS has no transformation
     * path to any other CRS (i.e. a map using this CS can't be reprojected to a
     * {@linkplain GeographicCRS geographic coordinate reference system} for example).
     */
    public static final EngineeringCRS CARTESIAN_3D = new Cartesian("Cartesian",
                                                                    CartesianCS.GENERIC_3D);

    /**
     * A two-dimensional wildcard coordinate system with
     * {@linkplain org.geotools.referencing.cs.CoordinateSystemAxis#X x},
     * {@linkplain org.geotools.referencing.cs.CoordinateSystemAxis#Y y}
     * axis in {@linkplain SI#METER metres}. At the difference of {@link #CARTESIAN_2D},
     * this coordinate system is treated specially by the default {@linkplain
     * org.geotools.referencing.operation.CoordinateOperationFactory coordinate operation factory}
     * with loose transformation rules: if no transformation path were found (for example
     * through a {@linkplain DerivedCRS derived CRS}), then the transformation from this
     * CRS to any CRS with a compatible number of dimensions is assumed to be the identity
     * transform. This CRS is usefull as a kind of wildcard when no CRS were explicitly specified.
     */
    public static final EngineeringCRS GENERIC_2D = new Cartesian("Generic",
                                                                  CartesianCS.GENERIC_2D);

    /**
     * A three-dimensional wildcard coordinate system with
     * {@linkplain org.geotools.referencing.cs.CoordinateSystemAxis#X x},
     * {@linkplain org.geotools.referencing.cs.CoordinateSystemAxis#Y y},
     * {@linkplain org.geotools.referencing.cs.CoordinateSystemAxis#Z z}
     * axis in {@linkplain SI#METER metres}. At the difference of {@link #CARTESIAN_3D},
     * this coordinate system is treated specially by the default {@linkplain
     * org.geotools.referencing.operation.CoordinateOperationFactory coordinate operation factory}
     * with loose transformation rules: if no transformation path were found (for example
     * through a {@linkplain DerivedCRS derived CRS}), then the transformation from this
     * CRS to any CRS with a compatible number of dimensions is assumed to be the identity
     * transform. This CRS is usefull as a kind of wildcard when no CRS were explicitly specified.
     */
    public static final EngineeringCRS GENERIC_3D = new Cartesian("Generic",
                                                                  CartesianCS.GENERIC_3D);

    /**
     * Constructs an engineering CRS from a name.
     *
     * @param name The name.
     * @param datum The datum.
     * @param cs The coordinate system.
     */
    public EngineeringCRS(final String            name,
                          final EngineeringDatum datum,
                          final CoordinateSystem    cs)
    {
        this(Collections.singletonMap(NAME_PROPERTY, name), datum, cs);
    }

    /**
     * Constructs an engineering CRS from a set of properties. The properties are given unchanged
     * to the {@linkplain org.geotools.referencing.ReferenceSystem#ReferenceSystem(Map) super-class
     * constructor}.
     *
     * @param properties Set of properties. Should contains at least <code>"name"</code>.
     * @param datum The datum.
     * @param cs The coordinate system.
     */
    public EngineeringCRS(final Map         properties,
                          final EngineeringDatum datum,
                          final CoordinateSystem    cs)
    {
        super(properties, datum, cs);
    }
    
    /**
     * Returns a hash value for this derived CRS.
     *
     * @return The hash code value. This value doesn't need to be the same
     *         in past or future versions of this class.
     */
    public int hashCode() {
        return (int)serialVersionUID ^ super.hashCode();
    }
    
    /**
     * Format the inner part of a
     * <A HREF="http://geoapi.sourceforge.net/snapshot/javadoc/org/opengis/referencing/doc-files/WKT.html"><cite>Well
     * Known Text</cite> (WKT)</A> element.
     *
     * @param  formatter The formatter to use.
     * @return The WKT element name, which is "LOCAL_CS"
     */
    protected String formatWKT(final Formatter formatter) {
        super.formatWKT(formatter);
        return "LOCAL_CS";
    }
}
