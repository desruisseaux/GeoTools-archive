/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *   
 *   (C) 2003-2006, Geotools Project Managment Committee (PMC)
 *   (C) 2001, Institut de Recherche pour le Développement
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
 *    This package contains documentation from OpenGIS specifications.
 *    OpenGIS consortium's work is fully acknowledged here.
 */
package org.geotools.referencing.wkt;

// J2SE dependencies
import java.util.Locale;
import java.util.prefs.Preferences;

// OpenGIS dependencies
import org.opengis.metadata.citation.Citation;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.referencing.cs.CoordinateSystem;

// Geotools dependencies
import org.geotools.metadata.iso.citation.Citations;
import org.geotools.resources.Utilities;
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.i18n.ErrorKeys;


/**
 * Base class for all object formattable as
 * <A HREF="http://geoapi.sourceforge.net/snapshot/javadoc/org/opengis/referencing/doc-files/WKT.html"><cite>Well
 * Known Text</cite> (WKT)</A>.
 *
 * @since 2.0
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @see <A HREF="http://geoapi.sourceforge.net/snapshot/javadoc/org/opengis/referencing/doc-files/WKT.html">Well Know Text specification</A>
 * @see <A HREF="http://gdal.velocet.ca/~warmerda/wktproblems.html">OGC WKT Coordinate System Issues</A>
 */
public class Formattable {
    /**
     * The "Indentation" preference name.
     *
     * @todo this string is also hard-coded in AffineTransform2D, because we
     *       don't want to make it public (neither {@link #getIndentation}).
     */
    static final String INDENTATION = "Indentation";

    /**
     * The formatter for the {@link #toWKT()} method.
     * Will be constructed only when first needed.
     */
    private static Formatter FORMATTER;

    /**
     * Default constructor.
     */
    protected Formattable() {
    }

    /**
     * Returns a string representation for this object. The default implementation returns
     * the same string similar than {@link #toWKT()}, except that no exception is thrown if
     * the string contains non-standard keywords. For example the
     * <A HREF="http://geoapi.sourceforge.net/snapshot/javadoc/org/opengis/referencing/doc-files/WKT.html">WKT
     * specification</A> do not defines any keyword for {@linkplain CoordinateSystem coordinate
     * system} objects. If this object is an instance of
     * {@link org.geotools.referencing.cs.DefaultCartesianCS}, then the WKT will
     * be formatted as <code>"CartesianCS[AXIS["</code>...<code>"], AXIS["</code>...<code>"],
     * </code><i>etc.</i><code>]"</code>.
     */
    public String toString() {
        return toWKT(Citations.OGC, getIndentation(), false);
    }

    /**
     * Returns a
     * <A HREF="http://geoapi.sourceforge.net/snapshot/javadoc/org/opengis/referencing/doc-files/WKT.html"><cite>Well
     * Known Text</cite> (WKT)</A> using a default indentation. The default indentation is read from
     * {@linkplain Preferences user preferences}. It can be set from the command line using the
     * following syntax:
     *
     * <blockquote>
     * {@code java org.geotools.referencing.wkt.Formattable -identation=}<var>&lt;preferred
     * indentation&gt;</var>
     * </blockquote>
     *
     * @return The Well Know Text for this object.
     * @throws UnformattableObjectException If this object can't be formatted as WKT.
     *         A formatting may fails because an object is too complex for the WKT format capability
     *         (for example an {@linkplain org.geotools.referencing.crs.DefaultEngineeringCRS
     *         engineering CRS} with different unit for each axis), or because only some specific
     *         implementations can be formatted as WKT.
     */
    public String toWKT() throws UnformattableObjectException {
        return toWKT(getIndentation());
    }

    /**
     * Returns a
     * <A HREF="http://geoapi.sourceforge.net/snapshot/javadoc/org/opengis/referencing/doc-files/WKT.html"><cite>Well
     * Known Text</cite> (WKT)</A> for this object using the specified indentation.
     *
     * @param  indentation The amount of spaces to use in indentation for WKT formatting,
     *         or 0 for formatting the whole WKT on a single line.
     * @return The Well Know Text for this object.
     * @throws UnformattableObjectException If this object can't be formatted as WKT.
     *         A formatting may fails because an object is too complex for the WKT format capability
     *         (for example an {@linkplain org.geotools.referencing.crs.DefaultEngineeringCRS
     *         engineering CRS} with different unit for each axis), or because only some specific
     *         implementations can be formatted as WKT.
     */
    public String toWKT(final int indentation) throws UnformattableObjectException {
        return toWKT(Citations.OGC, indentation);
    }

    /**
     * Returns a
     * <A HREF="http://geoapi.sourceforge.net/snapshot/javadoc/org/opengis/referencing/doc-files/WKT.html"><cite>Well
     * Known Text</cite> (WKT)</A> for this object using the specified indentation and authority.
     *
     * @param  authority The authority to prefer when choosing WKT entities names.
     * @param  indentation The amount of spaces to use in indentation for WKT formatting,
     *         or 0 for formatting the whole WKT on a single line.
     * @return The Well Know Text for this object.
     * @throws UnformattableObjectException If this object can't be formatted as WKT.
     *         A formatting may fails because an object is too complex for the WKT format capability
     *         (for example an {@linkplain org.geotools.referencing.crs.DefaultEngineeringCRS
     *         engineering CRS} with different unit for each axis), or because only some specific
     *         implementations can be formatted as WKT.
     */
    public String toWKT(final Citation authority, final int indentation)
            throws UnformattableObjectException
    {
        return toWKT(authority, indentation, true);
    }

    /**
     * Returns a WKT for this object using the specified indentation and authority.
     * If {@code strict} is true, then an exception is thrown if the WKT contains
     * invalid keywords.
     */
    private String toWKT(final Citation authority, final int indentation, final boolean strict)
             throws UnformattableObjectException
    {
        if (authority == null) {
            throw new IllegalArgumentException(Errors.format(
                      ErrorKeys.NULL_ARGUMENT_$1, "authority"));
        }
        // No need to synchronize. This is not a big deal
        // if two formatters co-exist for a short time.
        Formatter formatter = FORMATTER;
        if (formatter             == null        ||
            formatter.indentation != indentation ||
            formatter.authority   != authority)
        {
            formatter = new Formatter(Symbols.DEFAULT, indentation);
            formatter.authority = authority;
            FORMATTER = formatter;
        }
        synchronized (formatter) {
            try {
                if (this instanceof GeneralParameterValue) {
                    // Special processing for parameter values, which is formatted
                    // directly in 'Formatter'. Note that in GeoAPI, this interface
                    // doesn't share the same parent interface than other interfaces.
                    formatter.append((GeneralParameterValue) this);
                } else {
                    formatter.append(this);
                }
                if (strict && formatter.isInvalidWKT()) {
                    throw new UnformattableObjectException(Errors.format(ErrorKeys.INVALID_WKT_FORMAT));
                }
                return formatter.toString();
            } finally {
                formatter.clear();
            }
        }
    }
     
    /**
     * Format the inner part of a
     * <A HREF="http://geoapi.sourceforge.net/snapshot/javadoc/org/opengis/referencing/doc-files/WKT.html"><cite>Well
     * Known Text</cite> (WKT)</A> element. This method is automatically invoked by
     * {@link Formatter#append(Formattable)}. Element name and authority code must not be
     * formatted here. For example for a {@code GEOGCS} element
     * ({@link org.geotools.referencing.crs.DefaultGeographicCRS}), the formatter will invokes
     * this method for completing the WKT at the insertion point show below:
     *
     * <pre>
     * &nbsp;    GEOGCS["WGS 84", AUTHORITY["EPSG","4326"]]
     * &nbsp;                   |
     * &nbsp;           (insertion point)
     * </pre>
     *
     * The default implementation declare that this object produces an invalid WKT.
     * Subclasses must override this method for proper WKT formatting.
     *
     * @param  formatter The formatter to use.
     * @return The WKT element name (e.g. "GEOGCS").
     *
     * @see #toWKT
     * @see #toString
     */
    protected String formatWKT(final Formatter formatter) {
        formatter.setInvalidWKT();
        Class type = getClass();
        Class[] interfaces = type.getInterfaces();
        for (int i=0; i<interfaces.length; i++) {
            final Class candidate = interfaces[i];
            if (candidate.getName().startsWith("org.opengis.referencing.")) {
                type = candidate;
                break;
            }
        }
        return Utilities.getShortName(type);
    }

    /**
     * Returns the default indentation.
     */
    static int getIndentation() {
        try {
            return Preferences.userNodeForPackage(Formattable.class).getInt(INDENTATION, 2);
        } catch (SecurityException ignore) {
            // Ignore. Will fallback on the default indentation.
            return 2;
        }
    }

    /**
     * Set the default value for indentation.
     *
     * @throws SecurityException if a security manager is present and
     *         it denies <code>RuntimePermission("preferences")</code>.
     */
    static void setIndentation(final int indentation) throws SecurityException {
        Preferences.userNodeForPackage(Formattable.class).putInt(INDENTATION, indentation);
    }
}
