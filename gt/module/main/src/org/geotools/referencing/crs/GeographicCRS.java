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
import org.opengis.referencing.cs.EllipsoidalCS;
import org.opengis.referencing.cs.AxisDirection;
import org.opengis.referencing.cs.CoordinateSystemAxis;

// Geotools dependencies
import org.geotools.referencing.Info;
import org.geotools.referencing.ReferenceSystem;  // For javadoc
import org.geotools.referencing.wkt.Formatter;


/**
 * A coordinate reference system based on an ellipsoidal approximation of the geoid; this provides
 * an accurate representation of the geometry of geographic features for a large portion of the
 * earth's surface.
 *
 * <TABLE CELLPADDING='6' BORDER='1'>
 * <TR BGCOLOR="#EEEEFF"><TH NOWRAP>Used with CS type(s)</TH></TR>
 * <TR><TD>
 *   {@link org.geotools.referencing.cs.EllipsoidalCS Ellipsoidal}
 * </TD></TR></TABLE>
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class GeographicCRS extends CoordinateReferenceSystem
                        implements org.opengis.referencing.crs.GeographicCRS
{
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 861224913438092335L;

    /**
     * A two-dimensional geographic coordinate reference system using WGS84 datum.
     * This CRS uses (<var>longitude</var>,<var>latitude</var>) ordinates with longitude values
     * increasing north and latitude values increasing east. Angular units are degrees and
     * prime meridian is Greenwich.
     */
    public static final GeographicCRS WGS84 = new GeographicCRS("WGS84",
                        org.geotools.referencing.datum.GeodeticDatum.WGS84,
                        org.geotools.referencing.cs.EllipsoidalCS.GEODETIC_2D);

    /**
     * A three-dimensional geographic coordinate reference system using WGS84 datum.
     * This CRS uses (<var>longitude</var>,<var>latitude</var>,<var>height</var>)
     * ordinates with longitude values increasing north, latitude values increasing
     * east and height above the ellipsoid in metre. Angular units are degrees and
     * prime meridian is Greenwich.
     */
    public static final GeographicCRS WGS84_3D = new GeographicCRS("WGS84",
                        org.geotools.referencing.datum.GeodeticDatum.WGS84,
                        org.geotools.referencing.cs.EllipsoidalCS.GEODETIC_3D);

    /**
     * Constructs a geographic CRS from a name.
     *
     * @param name The name.
     * @param datum The datum.
     * @param cs The coordinate system.
     */
    public GeographicCRS(final String         name,
                         final GeodeticDatum datum,
                         final EllipsoidalCS    cs)
    {
        this(Collections.singletonMap("name", name), datum, cs);
    }

    /**
     * Constructs a geographic CRS from a set of properties. The properties are given unchanged
     * to the {@linkplain ReferenceSystem#ReferenceSystem(Map) super-class constructor}.
     *
     * @param properties Set of properties. Should contains at least <code>"name"</code>.
     * @param datum The datum.
     * @param cs The coordinate system.
     */
    public GeographicCRS(final Map      properties,
                         final GeodeticDatum datum,
                         final EllipsoidalCS    cs)
    {
        super(properties, datum, cs);
    }
    
    /**
     * Returns a hash value for this geographic CRS.
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
     * @return The WKT element name, which is "GEOGCS"
     */
    protected String formatWKT(final Formatter formatter) {
        Unit unit = NonSI.DEGREE_ANGLE;
        final int dimension = coordinateSystem.getDimension();
        for (int i=dimension; --i>=0;) {
            final CoordinateSystemAxis axis = coordinateSystem.getAxis(i);
            final Unit candidate = axis.getUnit();
            if (NonSI.DEGREE_ANGLE.isCompatible(unit)) {
                unit = candidate;
                if (AxisDirection.EAST.equals(axis.getDirection().absolute())) {
                    break; // Found the longitude axis.
                }
            }
        }
        final Unit oldUnit = formatter.getContextualUnit();
        formatter.append(datum);
        formatter.setContextualUnit(unit);
        formatter.append(((GeodeticDatum)datum).getPrimeMeridian());
        formatter.setContextualUnit(oldUnit);
        formatter.append(unit);
        for (int i=0; i<dimension; i++) {
            formatter.append(coordinateSystem.getAxis(i));
        }
        if (!unit.equals(getUnit())) {
            formatter.setInvalidWKT();
        }
        return "GEOGCS";
    }
}
