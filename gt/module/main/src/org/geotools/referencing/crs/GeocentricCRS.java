/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Managment Committee (PMC)
 * (C) 2001, Institut de Recherche pour le Développement
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
import java.util.Map;
import java.util.Collections;
import javax.units.NonSI;
import javax.units.Unit;

// OpenGIS direct dependencies
import org.opengis.referencing.datum.GeodeticDatum;
import org.opengis.referencing.cs.CartesianCS;
import org.opengis.referencing.cs.SphericalCS;
import org.opengis.referencing.cs.AxisDirection;
import org.opengis.referencing.cs.CoordinateSystemAxis;

// Geotools dependencies
import org.geotools.referencing.Info;
import org.geotools.referencing.wkt.Formatter;


/**
 * A 3D coordinate reference system with the origin at the approximate centre of mass of the earth.
 * A geocentric CRS deals with the earth's curvature by taking a 3D spatial view, which obviates
 * the need to model the earth's curvature.
 *
 * <TABLE CELLPADDING='6' BORDER='1'>
 * <TR BGCOLOR="#EEEEFF"><TH NOWRAP>Used with CS type(s)</TH></TR>
 * <TR><TD>
 *   {@link org.geotools.referencing.cs.CartesianCS Cartesian},
 *   {@link org.geotools.referencing.cs.SphericalCS Spherical}
 * </TD></TR></TABLE>
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class GeocentricCRS extends CoordinateReferenceSystem implements org.opengis.referencing.crs.GeocentricCRS
{
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 6784642848287659827L;
    
    /**
     * The default geocentric CRS with a
     * {@linkplain org.geotools.referencing.cs.CartesianCS#GEOCENTRIC cartesian coordinate system}.
     * Prime meridian is Greenwich, geodetic datum is WGS84 and linear units are metres.
     * The <var>X</var> axis points towards the prime meridian.
     * The <var>Y</var> axis points East.
     * The <var>Z</var> axis points North.
     */
    public static final GeocentricCRS CARTESIAN = new GeocentricCRS("Cartesian",
                        org.geotools.referencing.cs.CartesianCS.GEOCENTRIC,
                        org.geotools.referencing.datum.GeodeticDatum.WGS84);
    
    /**
     * The default geocentric CRS with a
     * {@linkplain org.geotools.referencing.cs.SphericalCS#GEOCENTRIC spherical coordinate system}.
     * Prime meridian is Greenwich, geodetic datum is WGS84 and linear units are metres.
     */
    public static final GeocentricCRS SPHERICAL = new GeocentricCRS("Spherical",
                        org.geotools.referencing.cs.SphericalCS.GEOCENTRIC,
                        org.geotools.referencing.datum.GeodeticDatum.WGS84);

    /**
     * Constructs a geocentric CRS from a name.
     *
     * @param name The name.
     * @param coordinateSystem The coordinate system.
     * @param datum The datum.
     */
    public GeocentricCRS(final String        name,
                         final CartesianCS   coordinateSystem,
                         final GeodeticDatum datum)
    {
        this(Collections.singletonMap("name", name), coordinateSystem, datum);
    }

    /**
     * Constructs a geocentric CRS from a name.
     *
     * @param name The name.
     * @param coordinateSystem The coordinate system.
     * @param datum The datum.
     */
    public GeocentricCRS(final String        name,
                         final SphericalCS   coordinateSystem,
                         final GeodeticDatum datum)
    {
        this(Collections.singletonMap("name", name), coordinateSystem, datum);
    }

    /**
     * Constructs a geographic CRS from a set of properties.
     * The properties are given unchanged to the super-class constructor.
     *
     * @param properties Set of properties. Should contains at least <code>"name"</code>.
     * @param coordinateSystem The coordinate system.
     * @param datum The datum.
     */
    public GeocentricCRS(final Map           properties,
                         final CartesianCS   coordinateSystem,
                         final GeodeticDatum datum)
    {
        super(properties, coordinateSystem, datum);
    }

    /**
     * Constructs a geographic CRS from a set of properties.
     * The properties are given unchanged to the super-class constructor.
     *
     * @param properties Set of properties. Should contains at least <code>"name"</code>.
     * @param coordinateSystem The coordinate system.
     * @param datum The datum.
     */
    public GeocentricCRS(final Map           properties,
                         final SphericalCS   coordinateSystem,
                         final GeodeticDatum datum)
    {
        super(properties, coordinateSystem, datum);
    }
    
    /**
     * Returns a hash value for this geocentric CRS.
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
     * @return The WKT element name, which is "GEOCCS"
     */
    protected String formatWKT(final Formatter formatter) {
        formatter.append(datum);
        formatter.append(((GeodeticDatum)datum).getPrimeMeridian());
        formatter.append(getUnit());
        final int dimension = coordinateSystem.getDimension();
        for (int i=0; i<dimension; i++) {
            formatter.append(coordinateSystem.getAxis(i));
        }
        return "GEOCCS";
    }
}
