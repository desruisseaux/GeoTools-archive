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
package org.geotools.referencing.factory;

// J2SE dependencies
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

// The following are just data structure, not dependencies to the whole Swing framework.
import javax.swing.tree.TreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.DefaultMutableTreeNode;

// OpenGIS dependencies
import org.opengis.referencing.AuthorityFactory;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.cs.CSAuthorityFactory;
import org.opengis.referencing.datum.DatumAuthorityFactory;
import org.opengis.referencing.operation.CoordinateOperationAuthorityFactory;

// Geotools dependencies
import org.geotools.factory.BufferedFactory;
import org.geotools.factory.OptionalFactory;
import org.geotools.resources.OptionalDependencies;
import org.geotools.resources.Utilities;
import org.geotools.resources.X364;
import org.opengis.metadata.citation.Citation;


/**
 * Build a tree of factory dependencies. Current version work only with
 * {@linkplain AuthorityFactory authority factories} extending the classes
 * defined in this package.
 *
 * @since 2.4
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class FactoryDependencies {
    /**
     * A list of interfaces that may be implemented by this class.
     * Used by {@link #createTree()}.
     */
    private static final Class[] TYPES = {
        CRSAuthorityFactory.class,
        CSAuthorityFactory.class,
        DatumAuthorityFactory.class,
        CoordinateOperationAuthorityFactory.class,
        BufferedFactory.class,
        OptionalFactory.class
    };

    /**
     * Labels for {@link #TYPES}.
     */
    private static final String[] TYPE_LABELS = {
        "crs", "cs", "datum", "operation", "buffered", "optional"
    };

    /**
     * The factory to format.
     */
    private final AuthorityFactory factory;

    /**
     * {@code true} for applying colors on a ANSI X3.64 (aka ECMA-48 and ISO/IEC 6429)
     * compliant output device.
     */
    private boolean colors;

    /**
     * Creates a new dependency tree for the specified factory.
     */
    public FactoryDependencies(final AuthorityFactory factory) {
        this.factory = factory;
    }

    /**
     * Returns {@code true} if syntax coloring is enabled.
     * By default, syntax coloring is disabled.
     */
    public boolean isColorEnabled() {
        return colors;
    }

    /**
     * Enables or disables syntax coloring on ANSI X3.64 (aka ECMA-48 and ISO/IEC 6429)
     * compatible terminal. By default, syntax coloring is disabled.
     */
    public void setColorEnabled(final boolean colors) {
        this.colors = colors;
    }

    /**
     * Prints the dependencies as a tree to the specified printer.
     */
    public void print(final PrintWriter out) {
        out.write(OptionalDependencies.toString(asTree()));
    }

    /**
     * Prints the dependencies as a tree to the specified writer.
     *
     * @param  out The writer where to print the tree.
     * @throws IOException if an error occured while writting to the stream.
     */
    public void print(final Writer out) throws IOException {
        out.write(OptionalDependencies.toString(asTree()));
    }

    /**
     * Returns the dependencies as a tree.
     */
    public TreeNode asTree() {
        return createTree(factory, new HashSet());
    }

    /**
     * Returns the dependencies for the specified factory.
     */
    private MutableTreeNode createTree(final AuthorityFactory factory, final Set/*<AuthorityFactory>*/ done) {
        final DefaultMutableTreeNode root = createNode(factory);
        if (factory instanceof AbstractAuthorityFactory) {
            final Collection dep = ((AbstractAuthorityFactory) factory).dependencies();
            if (dep != null) {
                for (final Iterator it=dep.iterator(); it.hasNext();) {
                    final Object element = it.next();
                    final MutableTreeNode child;
                    if (element instanceof AuthorityFactory) {
                        final AuthorityFactory candidate = (AuthorityFactory) element;
                        if (!done.add(candidate)) {
                            continue;
                        }
                        child = createTree(candidate, done);
                        if (!done.remove(candidate)) {
                            throw new AssertionError(); // Should never happen.
                        }
                    } else {
                        child = OptionalDependencies.createTreeNode(element.toString(), element, false);
                    }
                    root.add(child);
                }
            }
        }
        return root;
    }

    /**
     * Creates a single node for the specified factory.
     */
    private DefaultMutableTreeNode createNode(final AuthorityFactory factory) {
        final StringBuffer buffer =
                new StringBuffer(Utilities.getShortClassName(factory)).append('[');
        final Citation authority = factory.getAuthority();
        if (authority != null) {
            final Collection identifiers = authority.getIdentifiers();
            if (identifiers != null) {
                boolean next = false;
                for (final Iterator it=identifiers.iterator(); it.hasNext();) {
                    if (next) {
                        buffer.append(", ");
                    }
                    if (colors) buffer.append(X364.MAGENTA);
                    buffer.append('"').append(it.next()).append('"');
                    if (colors) buffer.append(X364.DEFAULT);
                    next = true;
                }            
            }
        }
        buffer.append(']');
        boolean hasFound = false;
        for (int i=0; i<TYPES.length; i++) {
            final Class type = TYPES[i];
            if (type.isInstance(factory)) {
                buffer.append(hasFound ? ", " : " (");
                if (colors) {
                    buffer.append(AuthorityFactory.class.isAssignableFrom(type) ? X364.GREEN : X364.CYAN);
                }
                buffer.append(TYPE_LABELS[i]);
                if (colors) buffer.append(X364.DEFAULT);
                hasFound = true;
            }
        }
        if (hasFound) {
            buffer.append(')');
        }
        return OptionalDependencies.createTreeNode(buffer.toString(), factory, true);
    }
}
