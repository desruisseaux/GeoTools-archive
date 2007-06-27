/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2007, GeoTools Project Managment Committee (PMC)
 *    (C) 2007, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.image.io.metadata;

// J2SE dependencies
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.StringTokenizer;
import java.util.Locale;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.imageio.metadata.IIOMetadata;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

// Geotools dependencies
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.i18n.Errors;
import org.geotools.util.LoggedFormat;
import org.geotools.util.NumberRange;


/**
 * Base class for {@linkplain GeographicMetadata geographic metadata} parsers.
 * This class provides convenience methods for encoding and decoding metadata
 * information.
 *
 * @since 2.4
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class MetadataAccessor {
    /**
     * Creates a default accessor.
     */
    protected MetadataAccessor() {
    }

    /**
     * Returns a node attribute as a string, or {@code null} if none.
     * This method never returns an empty string.
     *
     * @param  node The node, usually obtained by a call to {@link #getElement}.
     * @param  attribute The attribute to fetch from the above node (e.g. {@code "name"}).
     * @return The attribute value (never an empty string), or {@code null} if none.
     */
    protected static String getString(final Element element, final String attribute) {
        if (element != null) {
            String candidate = element.getAttribute(attribute);
            if (candidate != null) {
                candidate = candidate.trim();
                if (candidate.length() != 0) {
                    return candidate;
                }
            }
        }
        return null;
    }

    /**
     * Returns a node attribute as an integer, or {@code null} if none. If the attribute can't
     * be parsed as an integer, then this method logs a warning and returns {@code null}.
     *
     * @param  node The node, usually obtained by a call to {@link #getElement}.
     * @param  attribute The attribute to fetch from the above node (e.g. {@code "minimum"}).
     * @return The attribute value, or {@code null} if none or unparseable.
     */
    protected static Integer getInteger(final Element node, final String attribute) {
        String value = getString(node, attribute);
        if (value != null) {
            // Remove the trailing ".0", if any.
            for (int i=value.length(); --i>=0;) {
                switch (value.charAt(i)) {
                    case '0': continue;
                    case '.': value = value.substring(0, i); break;
                    default : break;
                }
                break;
            }
            try {
                return Integer.valueOf(value);
            } catch (NumberFormatException e) {
                log("getInteger", ErrorKeys.UNPARSABLE_NUMBER_$1, value);
            }
        }
        return null;
    }

    /**
     * Returns a node attribute as a floating point, or {@code null} if none. If the attribute
     * can't be parsed as a floating point, then this method logs a warning and returns {@code null}.
     *
     * @param  node The node, usually obtained by a call to {@link #getElement}.
     * @param  attribute The attribute to fetch from the above node (e.g. {@code "minimum"}).
     * @return The attribute value, or {@code null} if none or unparseable.
     */
    protected static Double getDouble(final Element node, final String attribute) {
        final String value = getString(node, attribute);
        if (value != null) try {
            return Double.valueOf(value);
        } catch (NumberFormatException e) {
            log("getDouble", ErrorKeys.UNPARSABLE_NUMBER_$1, value);
        }
        return null;
    }

    /**
     * Returns a node attribute as an array of floating point, or {@code null} if none. If an
     * element can't be parsed as a floating point, then this method logs a warning and returns
     * {@code null}.
     *
     * @param  node The node, usually obtained by a call to {@link #getElement}.
     * @param  attribute The attribute to fetch from the above node (e.g. {@code "minimum"}).
     * @param  unique {@code true} if duplicated values should be collapsed into unique values,
     *         or {@code false} for preserving duplicated values.
     * @return The attribute values, or {@code null} if none.
     */
    protected static double[] getDoubles(final Element node, final String attribute, final boolean unique) {
        final String sequence = getString(node, attribute);
        if (sequence == null) {
            return null;
        }
        final Collection/*<Double>*/ numbers;
        if (unique) {
            numbers = new LinkedHashSet();
        } else {
            numbers = new ArrayList();
        }
        final StringTokenizer tokens = new StringTokenizer(sequence);
        while (tokens.hasMoreTokens()) {
            final String token = tokens.nextToken();
            final Double number;
            try {
                number = Double.valueOf(sequence);
            } catch (NumberFormatException e) {
                log("getDoubles", ErrorKeys.UNPARSABLE_NUMBER_$1, token);
                continue;
            }
            numbers.add(number);
        }
        int count = 0;
        final double[] values = new double[numbers.size()];
        for (final Iterator it=numbers.iterator(); it.hasNext();) {
            values[count++] = ((Double) it.next()).doubleValue();
        }
        if (count != values.length) {
            throw new AssertionError(); // Should never happen.
        }
        return values;
    }

    /**
     * Returns a node attribute as a date, or {@code null} if none. If the attribute can't
     * be parsed as a date, then this method logs a warning and returns {@code null}.
     *
     * @param  node The node, usually obtained by a call to {@link #getElement}.
     * @param  attribute The attribute to fetch from the above node (e.g. {@code "origin"}).
     * @return The attribute value, or {@code null} if none or unparseable.
     */
    protected static Date getDate(final Element node, final String attribute) {
        final String value = getString(node, attribute);
        if (value != null) {
            final LoggedFormat format = (LoggedFormat) dateFormat.get();
            return (Date) format.parse(value);
        }
        return null;
    }

    /**
     * A parser and formatter for {@link Date} objects. We use one instance per thread in order
     * to avoid synchronization issues. The parser is used by {@link GeographicMetadata} and by
     * {@link MetadataAccessor}. It is part of the {@link GeographicMetadataFormat}
     * definition.
     */
    static final ThreadLocal/*<LoggedFormat<Date>>*/ dateFormat = new ThreadLocal() {
        //@Override
        protected Object initialValue() {
            final DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
            format.setTimeZone(TimeZone.getTimeZone("UTC"));
            final LoggedFormat logged = LoggedFormat.getInstance(format, Date.class);
            logged.setLogger("org.geotools.image.io.metadata");
            logged.setCaller(MetadataAccessor.class, "getDate");
            return logged;
        }
    };

    /**
     * Convenience method for logging a warning. Do not allow overriding, because
     * it would not work for warnings emitted by the {@link #getDate} method.
     */
    static void log(final String method, final int key, final Object value) {
        final LogRecord record = Errors.getResources(null).getLogRecord(Level.WARNING, key, value);
        record.setSourceClassName(MetadataAccessor.class.getName());
        record.setSourceMethodName(method);
        Logger.getLogger("org.geotools.image.io.metadata").log(record);
    }
}
