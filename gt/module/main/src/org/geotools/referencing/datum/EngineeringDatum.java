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
package org.geotools.referencing.datum;

// J2SE dependencies
import java.util.Map;
import java.util.Locale;
import java.util.Collections;
import java.util.HashMap;

// Geotools dependencies
import org.geotools.referencing.IdentifiedObject;
import org.geotools.referencing.wkt.Formatter;
import org.geotools.resources.cts.Resources;
import org.geotools.resources.cts.ResourceKeys;


/**
 * Defines the origin of an engineering coordinate reference system. An engineering datum is used
 * in a region around that origin. This origin can be fixed with respect to the earth (such as a
 * defined point at a construction site), or be a defined point on a moving vehicle (such as on a
 * ship or satellite).
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class EngineeringDatum extends Datum implements org.opengis.referencing.datum.EngineeringDatum {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 1498304918725248637L;

    /**
     * An engineering datum for unknow coordinate reference system. Such CRS are usually
     * assumed cartesian, but will not have any transformation path to other CRS.
     *
     * @see org.geotools.referencing.crs.EngineeringCRS#CARTESIAN_2D
     * @see org.geotools.referencing.crs.EngineeringCRS#CARTESIAN_3D
     */
    public static final EngineeringDatum UNKNOW;
    static {
        final Map properties = new HashMap(4);
        properties.put( NAME_PROPERTY, "Unknow");
        properties.put(ALIAS_PROPERTY, Resources.formatInternational(ResourceKeys.UNKNOW));
        UNKNOW = new EngineeringDatum(properties);
    }

    /**
     * Construct an engineering datum from a name.
     *
     * @param name The datum name.
     */
    public EngineeringDatum(final String name) {
        this(Collections.singletonMap(NAME_PROPERTY, name));
    }

    /**
     * Construct an engineering datum from a set of properties. The properties map is
     * given unchanged to the {@linkplain Datum#Datum(Map) super-class constructor}.
     *
     * @param properties Set of properties. Should contains at least <code>"name"</code>.
     */
    public EngineeringDatum(final Map properties) {
        super(properties);
    }
    
    /**
     * Compare this datum with the specified object for equality.
     *
     * @param  object The object to compare to <code>this</code>.
     * @param  compareMetadata <code>true</code> for performing a strict comparaison, or
     *         <code>false</code> for comparing only properties relevant to transformations.
     * @return <code>true</code> if both objects are equal.
     */
    public boolean equals(final IdentifiedObject object, final boolean compareMetadata) {
        if (object == this) {
            return true; // Slight optimization.
        }
        return super.equals(object, compareMetadata);
    }
    
    /**
     * Format the inner part of a
     * <A HREF="http://geoapi.sourceforge.net/snapshot/javadoc/org/opengis/referencing/doc-files/WKT.html"><cite>Well
     * Known Text</cite> (WKT)</A> element.
     *
     * @param  formatter The formatter to use.
     * @return The WKT element name, which is "LOCAL_DATUM"
     */
    protected String formatWKT(final Formatter formatter) {
        super.formatWKT(formatter);
        return "LOCAL_DATUM";
    }
}
