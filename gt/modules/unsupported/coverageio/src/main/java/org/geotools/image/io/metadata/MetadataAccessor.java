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
 * Provides convenience methods for decoding metadata information. The metadata object is
 * typically an instance of {@link GeographicMetadata}, but doesn't have to. However the
 * metadata must be organized in nodes following the {@linkplain GeographicMetadataFormat
 * geographic metadata format} defined in this package.
 *
 * @since 2.4
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class MetadataAccessor {
    /**
     * The separator between names in a node path.
     */
    private static final char SEPARATOR = '/';

    /**
     * The metadata to decode as a XML tree.
     */
    protected final Node metadata;

    /**
     * Creates an accessor for the specified metadata.
     *
     * @throws IllegalArgumentException if the specified metadata doesn't support
     *         the {@value GeographicMetadataFormat#FORMAT_NAME} format.
     */
    public MetadataAccessor(final IIOMetadata metadata) throws IllegalArgumentException {
        this.metadata = metadata.getAsTree(GeographicMetadataFormat.FORMAT_NAME);
    }

    /**
     * Returns the ranges of valid values for each sample dimensions. The length of the returned
     * array is the same than the number of {@code "SampleDimensions/SampleDimension"} elements.
     * The ranges use {@link Integer} type. Note that range {@linkplain NumberRange#getMinValue
     * minimum value}, {@linkplain NumberRange#getMaxValue maximum value} or both may be null if
     * no {@code "minValue"} or {@code "maxValue"} attribute were found for a node.
     */
    public NumberRange[] getValidRanges() {
        final List/*<Element>*/ elements = getElements("SampleDimensions/SampleDimension");
        final NumberRange[] ranges = new NumberRange[elements.size()];
        for (int i=0; i<ranges.length; i++) {
            final Element element = (Element) elements.get(i);
            final Integer minimum = getInteger(element, "minValue");
            final Integer maximum = getInteger(element, "maxValue");
            // Note: minimum and/or maximum may be null, in which case the range in unbounded.
            ranges[i] = new NumberRange(Integer.class, minimum, true, maximum, true);
        }
        return ranges;
    }

    /**
     * Returns child nodes at the given path. This method is for {@link #getElements(String)}
     * implementation only, and invokes itself recursively.
     */
    private static void getElements(final Node parent, final String path, final int base,
            final List/*<Element>*/ elements)
    {
        final int upper = path.indexOf(SEPARATOR, base);
        final String name = ((upper >= 0) ? path.substring(base, upper) : path.substring(base)).trim();
        final NodeList list = parent.getChildNodes();
        final int length = list.getLength();
        for (int i=0; i<length; i++) {
            Node candidate = list.item(i);
            if (name.equals(candidate.getNodeName())) {
                if (upper >= 0) {
                    getElements(candidate, path, upper+1, elements);
                } else if (candidate instanceof Element) {
                    // For the very last node, we require an element.
                    elements.add((Element) candidate);
                }
            }
        }
    }

    /**
     * Returns child nodes at the given path, or an empty list if none. Paths are separated
     * by the {@code '/'} character. Examples of valid paths that may have many elements are:
     * <ul>
     *   <li>{@code "CoordinateReferenceSystem/CoordinateSystem/Axis"}</li>
     *   <li>{@code "GridGeometry/Envelope/CoordinateValues"}</li>
     *   <li>{@code "SampleDimensions/SampleDimension"}</li>
     * </ul>
     */
    protected List/*<Element>*/ getElements(final String path) {
        final List elements = new ArrayList();
        getElements(metadata, path, 0, elements);
        return elements;
    }

    /**
     * Returns a child node of the given path, or {@code null} if none. If more than one node exist
     * for the given name, the first one is returned and a warning is logged. Examples of valid
     * paths that usually have only one element are:
     * <ul>
     *   <li>{@code "CoordinateReferenceSystem/Datum"}</li>
     *   <li>{@code "CoordinateReferenceSystem/CoordinateSystem"}</li>
     *   <li>{@code "GridGeometry/Envelope"}</li>
     * </ul>
     */
    protected Element getElement(final String path) {
        final List elements = getElements(path);
        final int count = elements.size();
        switch (count) {
            default: {
                log("getElement", ErrorKeys.TOO_MANY_OCCURENCES_$2,
                        new Object[] {path, new Integer(count)});
                // Fall through
            }
            case 1: return (Element) elements.get(0);
            case 0: return null;
        }
    }

    /**
     * Returns a node attribute as a string, or {@code null} if none.
     * This method never returns an empty string.
     *
     * @param  node The node, usually obtained by a call to {@link #getElement}.
     * @param  attribute The attribute to fetch from the above node (e.g. {@code "name"}).
     * @return The attribute value (never an empty string), or {@code null} if none.
     */
    protected String getString(final Element element, final String attribute) {
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
    protected Integer getInteger(final Element node, final String attribute) {
        final String value = getString(node, attribute);
        if (value != null) try {
            return Integer.valueOf(value);
        } catch (NumberFormatException e) {
            log("getInteger", ErrorKeys.UNPARSABLE_NUMBER_$1, value);
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
    protected Double getDouble(final Element node, final String attribute) {
        final String value = getString(node, attribute);
        if (value != null) try {
            return Double.valueOf(value);
        } catch (NumberFormatException e) {
            log("getDouble", ErrorKeys.UNPARSABLE_NUMBER_$1, value);
        }
        return null;
    }

    /**
     * Returns a node attribute as a date, or {@code null} if none. If the attribute can't
     * be parsed as a date, then this method logs a warning and returns {@code null}.
     *
     * @param  node The node, usually obtained by a call to {@link #getElement}.
     * @param  attribute The attribute to fetch from the above node (e.g. {@code "origin"}).
     * @return The attribute value, or {@code null} if none or unparseable.
     */
    protected Date getDate(final Element node, final String attribute) {
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
     * {@link MetadataAccessor}. It is part of the {@link GeographicMetadataFormat} definition.
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
    private static void log(final String method, final int key, final Object value) {
        final LogRecord record = Errors.getResources(null).getLogRecord(Level.WARNING, key, value);
        record.setSourceClassName(MetadataAccessor.class.getName());
        record.setSourceMethodName(method);
        Logger.getLogger("org.geotools.image.io.metadata").log(record);
    }
}
