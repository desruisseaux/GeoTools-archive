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
import java.lang.reflect.Array;
import java.text.Format;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*; // We use a lot of classes from this package.
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.LogRecord;
import javax.imageio.metadata.IIOMetadataNode;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

// Geotools dependencies
import org.geotools.util.LoggedFormat;
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.OptionalDependencies;
import org.geotools.util.UnsupportedImplementationException;


/**
 * Base class for {@linkplain GeographicMetadata geographic metadata} parsers. This class
 * provides convenience methods for encoding and decoding metadata information. A metadata
 * root {@linkplain Node node} is specified at construction time, together with a path to
 * the {@linkplain Element element} of interest. Example of valid paths:
 * <p>
 * <ul>
 *   <li>{@code "CoordinateReferenceSystem/Datum"}</li>
 *   <li>{@code "CoordinateReferenceSystem/CoordinateSystem"}</li>
 *   <li>{@code "GridGeometry/Envelope"}</li>
 * </ul>
 * <p>
 * In addition, some elements contains an arbitrary amount of childs. The path to child
 * elements can also be specified to the constructor. Examples (note that the constructor
 * expects paths relative to the parent; we show absolute paths below for completness):
 * <p>
 * <ul>
 *   <li>{@code "CoordinateReferenceSystem/CoordinateSystem/Axis"}</li>
 *   <li>{@code "GridGeometry/Envelope/CoordinateValues"}</li>
 *   <li>{@code "SampleDimensions/SampleDimension"}</li>
 * </ul>
 *
 * The {@code get} and {@code set} methods defined in this class will operate on the
 * <cite>selected</cite> {@linkplain Element element}, which may be either the one
 * specified at construction time, or one of its childs. The element can be selected
 * by {@link #selectParent} (the default) or {@link #selectChild}.
 * <p>
 * The example below creates an accessor for a node called {@code "CoordinateSystem"}
 * which is expected to have childs called {@code "Axis"}:
 *
 * <blockquote><pre>
 * MetadataAccessor accessor = new MetadataAccessor(metadata,
 *         "CoordinateReferenceSystem/CoordinateSystem", "Axis");
 *
 * accessor.selectParent();
 * String csName = accessor.getString("name");
 *
 * accessor.selectChild(0);
 * String firstAxisName = accessor.getString("name");
 * </pre></blockquote>
 *
 * @since 2.4
 * @version $Id$
 * @source $URL$
 * @author Martin Desruisseaux
 */
public class MetadataAccessor {
    /**
     * The separator between names in a node path.
     */
    private static final char SEPARATOR = '/';

    /**
     * The parent of child {@linkplain Element elements}.
     */
    protected final Node parent;

    /**
     * The {@linkplain #childs} path. This is the {@code childPath} parameter
     * given to the constructor.
     */
    private final String childPath;

    /**
     * The list of child elements. May be empty but never null.
     */
    private final List/*<Node>*/ childs;

    /**
     * The current element, or {@code null} if not yet selected.
     *
     * @see #selectChild
     * @see #currentElement()
     */
    private transient Element current;

    /**
     * Creates an accessor for the {@linkplain Element element} at the given path. Paths are
     * separated by the {@code '/'} character. See {@linkplain MetadataAccessor class javadoc}
     * for path examples.
     *
     * @param  metadata   The metadata node.
     * @param  parentPath The path to the {@linkplain Node node} of interest, or {@code null}
     *                    if {@code metadata} is directly the node of interest.
     * @param  childPath  The path (relative to {@code parentPath}) to the child
     *                    {@linkplain Element elements}, or {@code null} if none.
     */
    protected MetadataAccessor(final Node metadata, final String parentPath, final String childPath) {
        childs = new ArrayList(4);
        this.childPath = childPath;
        /*
         * Fetchs the parent node and ensure that we got a singleton. If there is more nodes than
         * expected, log a warning and pickup the first one. If there is no node, create a new one.
         */
        if (parentPath != null) {
            listChilds(metadata, parentPath, 0, childs, true);
            final int count = childs.size();
            switch (count) {
                default: {
                    warning("<init>", ErrorKeys.TOO_MANY_OCCURENCES_$2,
                            new Object[] {parentPath, new Integer(count)});
                    // Fall through for picking the first node.
                }
                case 1: {
                    parent = (Node) childs.get(0);
                    childs.clear();
                    break;
                }
                case 0: {
                    parent = appendChild(metadata, parentPath);
                    break;
                }
            }
        } else {
            parent = metadata;
        }
        /*
         * Computes a full path to children. Searching from 'metadata' root node using 'path'
         * should be identical to searching from 'parent' node using 'childPath', except in
         * case of badly formed metadata where the parent node appears more than once.
         */
        if (childPath != null) {
            final String path;
            if (parentPath != null) {
                path = parentPath + SEPARATOR + childPath;
            } else {
                path = childPath;
            }
            listChilds(metadata, path, 0, childs, false);
        }
        if (parent instanceof Element) {
            current = (Element) parent;
        }
    }

    /**
     * Adds to the {@link #childs} list the child nodes at the given {@code path}.
     * This method is for constructor implementation only and invokes itself recursively.
     *
     * @param  parent The parent metadata node.
     * @param  path   The path to the nodes or elements to insert into the list.
     * @param  base   The offset in {@code path} for the next element name.
     * @param  childs The list where to insert the nodes or elements.
     * @param  includeNodes {@code true} of adding nodes as well as elements.
     */
    private static void listChilds(final Node parent, final String path, final int base,
                                   final List/*<Node>*/ childs, final boolean includeNodes)
    {
        final int upper = path.indexOf(SEPARATOR, base);
        final String name = ((upper >= 0) ? path.substring(base, upper)
                                          : path.substring(base)).trim();
        final NodeList list = parent.getChildNodes();
        final int length = list.getLength();
        for (int i=0; i<length; i++) {
            final Node candidate = list.item(i);
            if (name.equals(candidate.getNodeName())) {
                if (upper >= 0) {
                    listChilds(candidate, path, upper+1, childs, includeNodes);
                } else if (includeNodes || (candidate instanceof Element)) {
                    // For the very last node, we may require an element.
                    childs.add(candidate);
                }
            }
        }
    }

    /**
     * Appends a child to the given parent.
     *
     * @param parent   The parent to add a child to.
     * @param path     The path of the child to add.
     * @return element The new child.
     */
    private static Node appendChild(Node parent, final String path) {
        int lower = 0;
search: for (int upper; (upper = path.indexOf(SEPARATOR, lower)) >= 0; lower=upper+1) {
            final String name = path.substring(lower, upper).trim();
            final NodeList list = parent.getChildNodes();
            final int length = list.getLength();
            for (int i=length; --i>=0;) {
                final Node candidate = list.item(i);
                if (name.equals(candidate.getNodeName())) {
                    parent = candidate;
                    continue search;
                }
            }
            parent = parent.appendChild(new IIOMetadataNode(name.intern()));
        }
        final String name = path.substring(lower).trim().intern();
        return parent.appendChild(new IIOMetadataNode(name));
    }

    /**
     * Returns the number of child {@linkplain Element elements}.
     * This is the upper value (exclusive) for {@link #selectChild}.
     *
     * @return The child {@linkplain Element elements} count.
     *
     * @see #selectChild
     * @see #createChild
     */
    public int childCount() {
        return childs.size();
    }

    /**
     * Adds a new child {@linkplain Element element} at the path given at construction time
     * and {@linkplain #selectChild select} it. The {@linkplain #childCount child count}
     * will be increased by 1.
     *
     * @see #childCount
     * @see #selectChild
     */
    public void createChild() {
        final int size = childs.size();
        final Node child = appendChild(parent, childPath);
        if (child instanceof Element) {
            childs.add((Element) child);
            selectChild(size);
        } else {
            throw new UnsupportedImplementationException(child.getClass());
        }
    }

    /**
     * Selects the {@linkplain Element element} at the given index. Every subsequent calls
     * to {@code get} or {@code set} methods will apply to this selected child element.
     *
     * @param index The index of the element to select.
     * @throws ArrayIndexOutOfBoundsException if the specified index is out of bounds.
     *
     * @see #childCount
     * @see #createChild
     * @see #selectParent
     */
    public void selectChild(final int index) throws ArrayIndexOutOfBoundsException {
        current = (Element) childs.get(index);
    }

    /**
     * Selects the {@linkplain #parent parent} element. Every subsequent calls
     * to {@code get} or {@code set} methods will apply to this parent element.
     *
     * @throws NoSuchElementException if there is no parent {@linkplain Element element}.
     *
     * @see #selectChild
     */
    public void selectParent() throws NoSuchElementException {
        if (parent instanceof Element) {
            current = (Element) parent;
        } else {
            throw new NoSuchElementException();
        }
    }

    /**
     * Returns the current element.
     *
     * @return The currently selected element.
     * @throws IllegalStateException if there is no selected element.
     *
     * @see #selectChild
     */
    private Element currentElement() throws IllegalStateException {
        if (current == null) {
            throw new IllegalStateException();
        }
        return current;
    }

    /**
     * Returns an attribute as a string for the {@linkplain #selectChild selected element},
     * or {@code null} if none. This method never returns an empty string.
     * <p>
     * Every {@code get} methods in this class invoke this method first. Consequently,
     * this method provides a single point for overriding if subclasses want to process
     * the attribute before parsing.
     *
     * @param attribute The attribute to fetch (e.g. {@code "name"}).
     * @return The attribute value (never an empty string), or {@code null} if none.
     */
    protected String getString(final String attribute) {
        String candidate = currentElement().getAttribute(attribute);
        if (candidate != null) {
            candidate = candidate.trim();
            if (candidate.length() == 0) {
                candidate = null;
            }
        }
        return candidate;
    }

    /**
     * Set the attribute to the specified value,
     * or remove the attribute if the value is null.
     * <p>
     * Every {@code set} methods in this class invoke this method last. Consequently,
     * this method provides a single point for overriding if subclasses want to process
     * the attribute after formatting.
     * 
     * @param attribute The attribute name.
     * @param value     The attribute value.
     */
    protected void setString(final String attribute, String value) {
        final Element element = currentElement();
        if (value == null || (value=value.trim()).length() == 0) {
            if (element.hasAttribute(attribute)) {
                element.removeAttribute(attribute);
            }
        } else {
            element.setAttribute(attribute, value);
        }
    }

    /**
     * Set the attribute to the specified enumeration value,
     * or remove the attribute if the value is null.
     * 
     * @param attribute The attribute name.
     * @param value     The attribute value.
     */
    protected void setEnum(final String attribute, String value) {
        if (value != null) {
            value = value.trim().replace('_', ' ').toLowerCase();
        }
        setString(attribute, value);
    }

    /**
     * Returns an attribute as an integer for the {@linkplain #selectChild selected element},
     * or {@code null} if none. If the attribute can't be parsed as an integer, then this method
     * logs a warning and returns {@code null}.
     *
     * @param attribute The attribute to fetch (e.g. {@code "minimum"}).
     * @return The attribute value, or {@code null} if none or unparseable.
     */
    protected Integer getInteger(final String attribute) {
        String value = getString(attribute);
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
                warning("getInteger", ErrorKeys.UNPARSABLE_NUMBER_$1, value);
            }
        }
        return null;
    }

    /**
     * Set the attribute to the specified integer value.
     *
     * @param attribute The attribute name.
     * @param value     The attribute value.
     */
    protected void setInteger(final String attribute, final int value) {
        setString(attribute, Integer.toString(value));
    }

    /**
     * Returns an attribute as an array of integers for the {@linkplain #selectChild selected
     * element}, or {@code null} if none. If an element can't be parsed as an integer, then this
     * method logs a warning and returns {@code null}.
     *
     * @param attribute The attribute to fetch (e.g. {@code "minimum"}).
     * @param unique {@code true} if duplicated values should be collapsed into unique values,
     *         or {@code false} for preserving duplicated values.
     * @return The attribute values, or {@code null} if none.
     */
    protected int[] getIntegers(final String attribute, final boolean unique) {
        return (int[]) getSequence(attribute, unique, true);
    }

    /**
     * Set the attribute to the specified array of values,
     * or remove the attribute if the array is {@code null}.
     *
     * @param attribute The attribute name.
     * @param value     The attribute value.
     */
    protected void setIntegers(final String attribute, final int[] values) {
        setSequence(attribute, values);
    }

    /**
     * Returns an attribute as a floating point for the {@linkplain #selectChild selected element},
     * or {@code null} if none. If the attribute can't be parsed as a floating point, then this
     * method logs a warning and returns {@code null}.
     *
     * @param attribute The attribute to fetch (e.g. {@code "minimum"}).
     * @return The attribute value, or {@code null} if none or unparseable.
     */
    protected Double getDouble(final String attribute) {
        final String value = getString(attribute);
        if (value != null) try {
            return Double.valueOf(value);
        } catch (NumberFormatException e) {
            warning("getDouble", ErrorKeys.UNPARSABLE_NUMBER_$1, value);
        }
        return null;
    }

    /**
     * Set the attribute to the specified floating point value,
     * or remove the attribute if the value is NaN.
     *
     * @param attribute The attribute name.
     * @param value     The attribute value.
     */
    protected void setDouble(final String attribute, final double value) {
        String text = null;
        if (!Double.isNaN(value) && !Double.isInfinite(value)) {
            text = Double.toString(value);
        }
        setString(attribute, text);
    }

    /**
     * Returns an attribute as an array of floating point for the {@linkplain #selectChild
     * selected element}, or {@code null} if none. If an element can't be parsed as a floating
     * point, then this method logs a warning and returns {@code null}.
     *
     * @param attribute The attribute to fetch (e.g. {@code "fillValues"}).
     * @param unique {@code true} if duplicated values should be collapsed into unique values,
     *         or {@code false} for preserving duplicated values.
     * @return The attribute values, or {@code null} if none.
     */
    protected double[] getDoubles(final String attribute, final boolean unique) {
        return (double[]) getSequence(attribute, unique, false);
    }

    /**
     * Set the attribute to the specified array of values,
     * or remove the attribute if the array is {@code null}.
     *
     * @param attribute The attribute name.
     * @param value     The attribute value.
     */
    protected void setDoubles(final String attribute, final double[] values) {
        setSequence(attribute, values);
    }

    /**
     * Implementation of {@link #getIntegers} and {@link #getDoubles} methods.
     *
     * @param  attribute The attribute to fetch  (e.g. {@code "fillValues"}).
     * @param  unique {@code true} if duplicated values should be collapsed into unique values,
     *         or {@code false} for preserving duplicated values.
     * @param  integers {@code true} for parsing as {@code int}, or {@code false} for parsing as
     *         {@code double}.
     * @return The attribute values, or {@code null} if none.
     */
    private Object getSequence(final String attribute, final boolean unique, final boolean integers) {
        final String sequence = getString(attribute);
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
            final Number number;
            try {
                if (integers) {
                    number = Integer.valueOf(token);
                } else {
                    number = Double.valueOf(token);
                }
            } catch (NumberFormatException e) {
                warning(integers ? "getIntegers" : "getDoubles", ErrorKeys.UNPARSABLE_NUMBER_$1, token);
                continue;
            }
            numbers.add(number);
        }
        int count = 0;
        final Object values;
        if (integers) {
            values = new int[numbers.size()];
        } else {
            values = new double[numbers.size()];
        }
        for (final Iterator it=numbers.iterator(); it.hasNext();) {
            Array.set(values, count++, it.next());
        }
        assert Array.getLength(values) == count;
        return values;
    }

    /**
     * Implementation of {@link #setIntegers} and {@link #setDoubles} methods.
     *
     * @param attribute The attribute name.
     * @param value     The attribute value.
     */
    private void setSequence(final String attribute, final Object values) {
        String text = null;
        if (values != null) {
            final StringBuffer buffer = new StringBuffer();
            final int length = Array.getLength(values);
            for (int i=0; i<length; i++) {
                if (i != 0) {
                    buffer.append(' ');
                }
                buffer.append(Array.get(values, i));
            }
            text = buffer.toString();
        }
        setString(attribute, text);
    }

    /**
     * Returns an attribute as a date for the {@linkplain #selectChild selected element},
     * or {@code null} if none. If the attribute can't be parsed as a date, then this method
     * logs a warning and returns {@code null}.
     *
     * @param attribute The attribute to fetch (e.g. {@code "origin"}).
     * @return The attribute value, or {@code null} if none or unparseable.
     */
    protected Date getDate(final String attribute) {
        final String value = getString(attribute);
        if (value != null) {
            final LoggedFormat format = (LoggedFormat) dateFormat.get();
            return (Date) format.parse(value);
        }
        return null;
    }

    /**
     * Set the attribute to the specified value, or remove the attribute if the value is null.
     *
     * @param attribute The attribute name.
     * @param value     The attribute value.
     */
    protected void setDate(final String attribute, final Date value) {
        String text = null;
        if (value != null) {
            final Format format = (Format) dateFormat.get();
            text = format.format(value);
        }
        setString(attribute, text);
    }

    /**
     * A parser and formatter for {@link Date} objects. We use one instance per thread in order to
     * avoid synchronization issues. The date format is part of {@link GeographicMetadataFormat}
     * definition.
     */
    private static final ThreadLocal/*<LoggedFormat<Date>>*/ dateFormat = new ThreadLocal() {
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
    private static void warning(final String method, final int key, final Object value) {
        final LogRecord record = Errors.getResources(null).getLogRecord(Level.WARNING, key, value);
        record.setSourceClassName(MetadataAccessor.class.getName());
        record.setSourceMethodName(method);
        Logger.getLogger("org.geotools.image.io.metadata").log(record);
    }

    /**
     * Returns a string representation of metadata, mostly for debugging purpose.
     */
    public String toString() {
        return OptionalDependencies.toString(OptionalDependencies.xmlToSwing(parent));
    }
}
