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
package org.geotools.referencing.wkt;

// Geotools dependencies
import org.geotools.resources.Utilities;


/**
 * Base class for all object formattable as
 * <A HREF="http://geoapi.sourceforge.net/snapshot/javadoc/org/opengis/referencing/doc-files/WKT.html"><cite>Well
 * Known Text</cite> (WKT)</A>.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class Formattable {
    /**
     * Default constructor.
     */
    protected Formattable() {
    }

    /**
     * Returns a string representation for this object. The default implementation returns
     * an extension of the <cite>Well Known Text</cite> (WKT) format. The extension consist
     * in usage of classname where no WKT keyword is defined. For example the
     * <A HREF="http://geoapi.sourceforge.net/snapshot/javadoc/org/opengis/referencing/doc-files/WKT.html">WKT
     * specification</A> do not defines any keyword for
     * {@linkplain org.geotools.referencing.cs.CoordinateSystem coordinate system} objects. If this
     * object is an instance of {@link org.geotools.referencing.cs.CartesianCS}, then the WKT will
     * be formatted as <code>"CartesianCS[AXIS["</code>...<code>"], AXIS["</code>...<code>"],
     * </code><i>etc.</i><code>]"</code>.
     */
    public String toString() {
        final Formatter formatter = new Formatter(null);
        formatter.append(this);
        return formatter.toString();
    }

    /**
     * Returns a
     * <A HREF="http://geoapi.sourceforge.net/snapshot/javadoc/org/opengis/referencing/doc-files/WKT.html"><cite>Well
     * Known Text</cite> (WKT)</A> for this object using a default {@linkplain Formatter formatter}.
     *
     * @param  indentation The amount of spaces to use in indentation for WKT formatting.
     * @return The Well Know Text for this object.
     *
     * @throws UnformattableObjectException If an object can't be formatted as WKT.
     *         A formatting may fails because an object is too complex for the WKT format
     *         capability (for example an {@linkplain org.geotools.referencing.crs.EngineeringCRS
     *         engineering CRS} with different unit for each axis), or because only some specific
     *         implementations can be formatted as WKT.
     */
    public String toWKT(final int indentation) throws UnformattableObjectException {
        final Formatter formatter = new Formatter(null, indentation);
        formatter.append(this);
        if (formatter.isInvalidWKT()) {
            // TODO localize.
            throw new UnformattableObjectException("Not a valid WKT format.");
        }
        return formatter.toString();
    }
     
    /**
     * Format the inner part of a
     * <A HREF="http://geoapi.sourceforge.net/snapshot/javadoc/org/opengis/referencing/doc-files/WKT.html"><cite>Well
     * Known Text</cite> (WKT)</A> element. This method is automatically invoked by
     * {@link Formatter#append(Formattable)}. Element name and authority code must not be
     * formatted here. For example for a <code>GEOGCS</code> element
     * ({@link org.geotools.referencing.crs.GeographicCRS}), the formatter will invokes
     * this method for completing the WKT at the insertion point show below:
     *
     * <pre>
     * &nbsp;    GEOGCS["WGS 84", AUTHORITY["EPSG","4326"]]
     * &nbsp;                   |
     * &nbsp;           (insertion point)
     * </pre>
     *
     * The default implementation does nothing.
     * Subclasses must override this method for proper WKT formatting.
     *
     * @param  formatter The formatter to use.
     * @return The WKT element name (e.g. "GEOGCS").
     *
     * @see #toWKT
     * @see #toString
     */
    protected String formatWKT(final Formatter formatter) {
        return Utilities.getShortClassName(this);
    }
}
