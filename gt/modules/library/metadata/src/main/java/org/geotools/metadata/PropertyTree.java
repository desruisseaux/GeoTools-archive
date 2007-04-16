/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2007, GeoTools Project Managment Committee (PMC)
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
package org.geotools.metadata;

// J2SE dependencies
import java.lang.reflect.Constructor;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Iterator;
import java.util.Locale;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

// OpenGIS dependencies
import org.opengis.util.CodeList;
import org.opengis.util.InternationalString;

// Geotools dependencies
import org.geotools.resources.Utilities;
import org.geotools.resources.XMath;


/**
 * Represents the metadata property as a tree made from {@linkplain TreeNode tree nodes}.
 * Note that while {@link TreeNode} is defined in the {@link javax.swing.tree} package,
 * it can be seen as a data structure independent of Swing.
 * <p>
 * Note: this method is called {@code PropertyTree} because it may implements
 * {@link javax.swing.tree.TreeModel} in some future Geotools implementation.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux (Geomatys)
 */
final class PropertyTree {
    /**
     * The default number of significant digits (may or may not be fraction digits).
     */
    private static final int PRECISION = 12;

    /**
     * The expected standard implemented by the metadata.
     */
    private final MetadataStandard standard;

    /**
     * The locale to use for {@linkplain Date date}, {@linkplain Number number}
     * and {@linkplain InternationalString international string} formatting.
     */
    private final Locale locale;

    /**
     * The object to use for formatting numbers.
     * Will be created only when first needed.
     */
    private transient NumberFormat numberFormat;

    /**
     * The object to use for formatting dates.
     * Will be created only when first needed.
     */
    private transient DateFormat dateFormat;

    /**
     * Creates a new tree builder using the default locale.
     *
     * @param standard The expected standard implemented by the metadata.
     */
    public PropertyTree(final MetadataStandard standard) {
        this(standard, Locale.getDefault());
    }

    /**
     * Creates a new tree builder.
     *
     * @param standard The expected standard implemented by the metadata.
     * @param locale   The locale to use for {@linkplain Date date}, {@linkplain Number number}
     *                 and {@linkplain InternationalString international string} formatting.
     */
    public PropertyTree(final MetadataStandard standard, final Locale locale) {
        this.standard = standard;
        this.locale   = locale;
    }

    /**
     * Creates a tree for the specified metadata.
     */
    public MutableTreeNode asTree(final Object metadata) {
        final String name = Utilities.getShortName(standard.getInterface(metadata.getClass()));
        final DefaultMutableTreeNode root = createTreeNode(localize(name), metadata, true);
        append(root, metadata);
        return root;
    }

    /**
     * Appends the specified value to a branch. The value may be a metadata
     * (treated {@linkplain AbstractMetadata#asMap as a Map} - see below),
     * a collection or a singleton.
     * <p>
     * Map or metadata are constructed as a sub tree where every nodes is a
     * property name, and the childs are the value(s) for that property.
     */
    private void append(final DefaultMutableTreeNode branch, final Object value) {
        if (value instanceof Map) {
            appendMap(branch, (Map) value);
            return;
        }
        if (value instanceof AbstractMetadata) {
            appendMap(branch, ((AbstractMetadata) value).asMap());
            return;
        }
        if (value != null) {
            final PropertyAccessor accessor = standard.getAccessorOptional(value.getClass());
            if (accessor != null) {
                appendMap(branch, new PropertyMap(value, accessor));
                return;
            }
        }
        if (value instanceof Collection) {
            for (final Iterator it=((Collection) value).iterator(); it.hasNext();) {
                final Object element = it.next();
                if (!PropertyAccessor.isEmpty(element)) {
                    append(branch, element);
                }
            }
            return;
        }
        final String asText;
        if (value instanceof CodeList) {
            asText = localize((CodeList) value);
        } else if (value instanceof Date) {
            asText = format((Date) value);
        } else if (value instanceof Number) {
            asText = format((Number) value);
        } else if (value instanceof InternationalString) {
            asText = ((InternationalString) value).toString(locale);
        } else {
            asText = String.valueOf(value);
        }
        branch.add(createTreeNode(asText, value, false));
    }

    /**
     * Appends the specified map (usually a metadata) to a branch. Each map keys
     * is a child in the specified {@code branch}, and each value is a child of
     * the map key. There is often only one value for a map key, but not always;
     * some are collections, which are formatted as many childs for the same key.
     */
    private void appendMap(final DefaultMutableTreeNode branch, final Map asMap) {
        for (final Iterator it=asMap.entrySet().iterator(); it.hasNext();) {
            final Map.Entry entry = (Map.Entry) it.next();
            final Object value = entry.getValue();
            if (!PropertyAccessor.isEmpty(value)) {
                final String name = localize((String) entry.getKey());
                final DefaultMutableTreeNode child = createTreeNode(name, value, true);
                append(child, value);
                branch.add(child);
            }
        }
    }

    /**
     * Formats the specified number.
     */
    private String format(final Number value) {
        if (numberFormat == null) {
            numberFormat = NumberFormat.getNumberInstance(locale);
            numberFormat.setMinimumFractionDigits(0);
        }
        int precision = 0;
        if (!XMath.isInteger(value.getClass())) {
            precision = PRECISION;
            final double v = Math.abs(value.doubleValue());
            if (v > 0) {
                final int digits = (int) XMath.log10(v);
                if (Math.abs(digits) >= PRECISION) {
                    // TODO: Switch to exponential notation when a convenient API will be available in J2SE.
                    return value.toString();
                }
                if (digits >= 0) {
                    precision -= digits;
                }
                precision = Math.max(0, PRECISION - precision);
            }
        }
        numberFormat.setMaximumFractionDigits(precision);
        return numberFormat.format(value);
    }

    /**
     * Formats the specified date.
     */
    private String format(final Date value) {
        if (dateFormat == null) {
            dateFormat = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, locale);
        }
        return dateFormat.format(value);
    }

    /**
     * Localize the specified property name. In current version, this is merely
     * a hook for future development. For now we reformat the programatic name.
     */
    private String localize(String name) {
        name = name.trim();
        final int length = name.length();
        if (length != 0) {
            final StringBuffer buffer = new StringBuffer();
            buffer.append(Character.toUpperCase(name.charAt(0)));
            boolean previousIsUpper = true;
            int base = 1;
            for (int i=1; i<length; i++) {
                final boolean currentIsUpper = Character.isUpperCase(name.charAt(i));
                if (currentIsUpper != previousIsUpper) {
                    /*
                     * When a case change is detected (lower case to upper case as in "someName",
                     * or "someURL", or upper case to lower case as in "HTTPProxy"), then insert
                     * a space just before the upper case letter.
                     */
                    int split = i;
                    if (previousIsUpper) {
                        split--;
                    }
                    if (split > base) {
                        buffer.append(name.substring(base, split)).append(' ');
                        base = split;
                    }
                }
                previousIsUpper = currentIsUpper;
            }
            final String candidate = buffer.append(name.substring(base)).toString();
            if (!candidate.equals(name)) {
                // Holds a reference to this new String object only if it worth it.
                name = candidate;
            }
        }
        return name;
    }

    /**
     * Localize the specified property name. In current version, this is merely
     * a hook for future development.  For now we reformat the programatic name.
     */
    private String localize(final CodeList code) {
        return code.name().trim().replace('_', ' ').toLowerCase(locale);
    }

    /**
     * Returns a string representation of the specified tree node.
     */
    public static String toString(final TreeNode node) {
        final StringBuffer buffer = new StringBuffer();
        toString(node, buffer, 0, System.getProperty("line.separator", "\n"));
        return buffer.toString();
    }

    /**
     * Append a string representation of the specified node to the specified buffer.
     */
    private static void toString(final TreeNode     node,
                                 final StringBuffer buffer,
                                 final int          indent,
                                 final String       lineSeparator)
    {
        final int count = node.getChildCount();
        if (count == 0) {
            if (node.isLeaf()) {
                /*
                 * If the node has no child and is a leaf, then it is some value like a number,
                 * a date or a string.  We just display this value, which is usually part of a
                 * collection. If the node has no child and is NOT a leaf, then it is an empty
                 * metadata and we just ommit it.
                 */
                buffer.append(Utilities.spaces(indent)).append(node).append(lineSeparator);
            }
            return;
        }
        buffer.append(Utilities.spaces(indent)).append(node).append(':');
        if (count == 1) {
            final TreeNode child = node.getChildAt(0);
            if (child.isLeaf()) {
                buffer.append(' ').append(child).append(lineSeparator);
                return;
            }
        }
        for (int i=0; i<count; i++) {
            final TreeNode child = node.getChildAt(i);
            if (i == 0) {
                buffer.append(lineSeparator);
            }
            toString(child, buffer, indent+2, lineSeparator);
        }
    }


    // -------- TreeNode workaround ---------------------------------------------------------------
    /**
     * Constructor for {@link org.geotools.gui.swing.tree.NamedTreeNode}.
     */
    private static Constructor treeNodeConstructor;

    /**
     * Set to {@code true} if {@link #treeNodeConstructor} can't be obtained.
     */
    private static boolean noNamedTreeNode = false;

    /**
     * Creates an initially empty tree node.
     *
     * @param name   The value to be returned by {@link TreeNode#toString}.
     * @param object The user object to be returned by the tree node. May
     *               or may not be the same than {@code name}.
     * @param allowsChildren if children are allowed.
     */
    private static DefaultMutableTreeNode createTreeNode(final String name,
                                                         final Object object,
                                                         final boolean allowsChildren)
    {
        /*
         * If the "modules/extension/swing-widgets" JAR is in the classpath,  then create an
         * instance of NamedTreeNode (see org.geotools.swing.tree javadoc for an explanation
         * about why the NamedTreeNode workaround is needed).  We use reflection because the
         * swing-widgets module is optional,  so we fallback on the standard Swing object if
         * we can't create an instance of NamedTreeNode.   We will attempt to use reflection
         * only once in order to avoid a overhead if the swing-widgets module is not available.
         *
         * The swing-widgets module contains a "NamedTreeNodeTest" for making sure that the
         * NamedTreeNode instances are properly created.
         *
         * Note: No need to sychronize; this is not a big deal if we make the attempt twice.
         */
        if (!noNamedTreeNode) try {
            if (treeNodeConstructor == null) {
                treeNodeConstructor = Class.forName("org.geotools.gui.swing.tree.NamedTreeNode").
                        getConstructor(new Class[] {String.class, Object.class, Boolean.TYPE});
            }
            return (DefaultMutableTreeNode) treeNodeConstructor.newInstance(
                    new Object[] {name, object, Boolean.valueOf(allowsChildren)});
        } catch (Exception e) {
            /*
             * There is a large amount of checked and unchecked exceptions that the above code
             * may thrown. We catch all of them because a reasonable fallback exists (creation
             * of the default Swing object below).  Note that none of the unchecked exceptions
             * (IllegalArgumentException, NullPointerException...) should occurs, except maybe
             * SecurityException. Maybe we could let the unchecked exceptions propagate...
             */
            noNamedTreeNode = true;
        }
        return new DefaultMutableTreeNode(name, allowsChildren);
    }
}
