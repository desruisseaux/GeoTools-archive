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

// J2SE dependencies
import java.util.prefs.Preferences;

// GeoAPI dependencies
import org.opengis.parameter.GeneralParameterValue;

// Geotools dependencies
import org.geotools.resources.Arguments;
import org.geotools.resources.Utilities;


/**
 * Base class for all object formattable as
 * <A HREF="http://geoapi.sourceforge.net/snapshot/javadoc/org/opengis/referencing/doc-files/WKT.html"><cite>Well
 * Known Text</cite> (WKT)</A>.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @see <A HREF="http://geoapi.sourceforge.net/snapshot/javadoc/org/opengis/referencing/doc-files/WKT.html">Well Know Text specification</A>
 * @see <A HREF="http://gdal.velocet.ca/~warmerda/wktproblems.html">OGC WKT Coordinate System Issues</A>
 */
public class Formattable {
    /**
     * The "Indentation" preference name.
     * Note: this string is also hard-coded in AffineTransform2D.
     */
    private static final String INDENTATION = "Indentation";

    /**
     * Default constructor.
     */
    protected Formattable() {
    }

    /**
     * Returns a string representation for this object. The default implementation returns
     * a string similar to the <cite>Well Known Text</cite> (WKT) format. The difference
     * consist in usage of classnames instead of WKT keywords. For example the
     * <A HREF="http://geoapi.sourceforge.net/snapshot/javadoc/org/opengis/referencing/doc-files/WKT.html">WKT
     * specification</A> do not defines any keyword for
     * {@linkplain org.geotools.referencing.cs.CoordinateSystem coordinate system} objects. If this
     * object is an instance of {@link org.geotools.referencing.cs.CartesianCS}, then the WKT will
     * be formatted as <code>"CartesianCS[AXIS["</code>...<code>"], AXIS["</code>...<code>"],
     * </code><i>etc.</i><code>]"</code>.
     */
    public String toString() {
        final Formatter formatter = new Formatter(null, 2);
        formatter.usesClassname = true;
        formatter.append(this);
        return formatter.toString();
    }

    /**
     * Returns a
     * <A HREF="http://geoapi.sourceforge.net/snapshot/javadoc/org/opengis/referencing/doc-files/WKT.html"><cite>Well
     * Known Text</cite> (WKT)</A> using a default indentation. The default indentation is read from
     * {@linkplain Preferences user preferences}. It can be set from the command line using the
     * following syntax:
     *
     * <blockquote>
     * <code>java org.geotools.referencing.wkt.Formattable -identation=</code><var>&lt;preferred
     * indentation&gt;</var>
     * </blockquote>
     *
     * @return The Well Know Text for this object.
     * @throws UnformattableObjectException If this object can't be formatted as WKT.
     *         A formatting may fails because an object is too complex for the WKT format
     *         capability (for example an {@linkplain org.geotools.referencing.crs.EngineeringCRS
     *         engineering CRS} with different unit for each axis), or because only some specific
     *         implementations can be formatted as WKT.
     */
    public String toWKT() throws UnformattableObjectException {
        int indentation = 2;
        try {
            indentation = Preferences.userNodeForPackage(Formattable.class)
                                     .getInt(INDENTATION, indentation);
        } catch (SecurityException ignore) {
            // Ignore. Will fallback on the default indentation.
        }
        return toWKT(indentation);
    }

    /**
     * Returns a
     * <A HREF="http://geoapi.sourceforge.net/snapshot/javadoc/org/opengis/referencing/doc-files/WKT.html"><cite>Well
     * Known Text</cite> (WKT)</A> for this object using a default {@linkplain Formatter formatter}.
     *
     * @param  indentation The amount of spaces to use in indentation for WKT formatting,
     *         or 0 for formatting the whole WKT on a single line.
     * @return The Well Know Text for this object.
     * @throws UnformattableObjectException If this object can't be formatted as WKT.
     *         A formatting may fails because an object is too complex for the WKT format
     *         capability (for example an {@linkplain org.geotools.referencing.crs.EngineeringCRS
     *         engineering CRS} with different unit for each axis), or because only some specific
     *         implementations can be formatted as WKT.
     */
    public String toWKT(final int indentation) throws UnformattableObjectException {
        final Formatter formatter = new Formatter(null, indentation);
        if (this instanceof GeneralParameterValue) {
            // Special processing for parameter values, which is formatted
            // directly in 'Formatter'. Note that in GeoAPI, this interface
            // doesn't share the same parent interface than other interfaces.
            formatter.append((GeneralParameterValue) this);
        } else {
            formatter.append(this);
        }
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

    /**
     * Set the preferred indentation from the command line. This indentation is used by
     * {@link #toWKT()}. This method can be invoked from the command line using the following
     * syntax:
     *
     * <blockquote>
     * <code>java org.geotools.referencing.wkt.Formattable -identation=</code><var>&lt;preferred
     * indentation&gt;</var>
     * </blockquote>
     */
    public static void main(final String[] args) {
        final Arguments arguments = new Arguments(args);
        final int indentation = arguments.getRequiredInteger(INDENTATION);
        arguments.getRemainingArguments(0);
        Preferences.userNodeForPackage(Formattable.class).putInt(INDENTATION, indentation);
    }
}
