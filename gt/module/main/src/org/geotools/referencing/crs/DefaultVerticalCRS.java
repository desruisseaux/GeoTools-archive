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
import java.util.Collections;
import java.util.Map;

// OpenGIS dependencies
import org.opengis.referencing.cs.VerticalCS;
import org.opengis.referencing.crs.VerticalCRS;
import org.opengis.referencing.datum.VerticalDatum;

// Geotools dependencies
import org.geotools.referencing.wkt.Formatter;
import org.geotools.referencing.cs.DefaultVerticalCS;
import org.geotools.referencing.AbstractReferenceSystem;
import org.geotools.referencing.AbstractIdentifiedObject;
import org.geotools.referencing.datum.DefaultVerticalDatum;


/**
 * A 1D coordinate reference system used for recording heights or depths. Vertical CRSs make use
 * of the direction of gravity to define the concept of height or depth, but the relationship with
 * gravity may not be straightforward. By implication, ellipsoidal heights (h) cannot be captured
 * in a vertical coordinate reference system. Ellipsoidal heights cannot exist independently, but
 * only as inseparable part of a 3D coordinate tuple defined in a geographic 3D coordinate
 * reference system.
 *
 * <TABLE CELLPADDING='6' BORDER='1'>
 * <TR BGCOLOR="#EEEEFF"><TH NOWRAP>Used with CS type(s)</TH></TR>
 * <TR><TD>
 *   {@link VerticalCS Vertical}
 * </TD></TR></TABLE>
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class DefaultVerticalCRS extends AbstractSingleCRS implements VerticalCRS {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 3565878468719941800L;
    
    /**
     * Default vertical coordinate reference system using ellipsoidal datum.
     * Ellipsoidal heights are measured along the normal to the ellipsoid
     * used in the definition of horizontal datum.
     *
     * @todo Localize name.
     */
    public static final DefaultVerticalCRS ELLIPSOIDAL_HEIGHT = new DefaultVerticalCRS("Ellipsoidal height",
                        DefaultVerticalDatum.ELLIPSOIDAL, DefaultVerticalCS.ELLIPSOIDAL_HEIGHT);

    /**
     * Constructs a vertical CRS from a name.
     *
     * @param name The name.
     * @param datum The datum.
     * @param cs The coordinate system.
     */
    public DefaultVerticalCRS(final String         name,
                              final VerticalDatum datum,
                              final VerticalCS       cs)
    {
        this(Collections.singletonMap(NAME_PROPERTY, name), datum, cs);
    }

    /**
     * Constructs a vertical CRS from a set of properties. The properties are given unchanged to
     * the {@linkplain AbstractReferenceSystem#AbstractReferenceSystem(Map) super-class constructor}.
     *
     * @param properties Set of properties. Should contains at least <code>"name"</code>.
     * @param datum The datum.
     * @param cs The coordinate system.
     */
    public DefaultVerticalCRS(final Map      properties,
                              final VerticalDatum datum,
                              final VerticalCS       cs)
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
     * @return The WKT element name, which is "VERT_CS"
     */
    protected String formatWKT(final Formatter formatter) {
        super.formatWKT(formatter);
        return "VERT_CS";
    }
}
