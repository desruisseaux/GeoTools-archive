/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2003-2006, Geotools Project Managment Committee (PMC)
 *    (C) 2001, Institut de Recherche pour le Développement
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
 */
package org.geotools.resources;

import java.lang.reflect.Constructor;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultMutableTreeNode;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;


/**
 * Bridges to optional dependencies (especially {@code widget-swing} module).
 *
 * @since 2.0
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public final class OptionalDependencies {
    /**
     * Constructor for {@link org.geotools.gui.swing.tree.NamedTreeNode}.
     */
    private static Constructor treeNodeConstructor;

    /**
     * Set to {@code true} if {@link #treeNodeConstructor} can't be obtained.
     */
    private static boolean noNamedTreeNode = false;

    /**
     * Interdit la création d'objets de cette classe.
     */
    private OptionalDependencies() {
    }

    /**
     * Creates an initially empty tree node.
     *
     * @param name   The value to be returned by {@link TreeNode#toString}.
     * @param object The user object to be returned by the tree node. May
     *               or may not be the same than {@code name}.
     * @param allowsChildren if children are allowed.
     */
    public static DefaultMutableTreeNode createTreeNode(final String name,
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

    /**
     * Creates a Swing root tree node from a XML root tree node. Together with
     * {@link #toString(TreeNode)}, this method provides a convenient way to print
     * the content of a XML document for debugging purpose.
     * <p>
     * This method should not be defined here, since this class is about optional dependencies.
     * It should be defined in {@link org.geotools.gui.swing.tree.Trees} instead. However we put
     * it here (for now) because it is used in some module that don't want to depend on widgets.
     */
    public static MutableTreeNode xmlToSwing(final Node node) {
        String label = node.getNodeName();
        final String value = node.getNodeValue();
        if (value != null) {
            label += "=\"" + value + '"';
        }
        final DefaultMutableTreeNode root = createTreeNode(label, node, true);
        final NamedNodeMap attributes = node.getAttributes();
        final int length = attributes.getLength();
        for (int i=0; i<length; i++) {
            final Node attribute = attributes.item(i);
            if (attribute != null) {
                label = attribute.getNodeName() + "=\"" + attribute.getNodeValue() + '"';
                root.add(createTreeNode(label, attribute, false));
            }
        }
        for (Node child=node.getFirstChild(); child!=null; child=child.getNextSibling()) {
            root.add(xmlToSwing(child));
        }
        return root;
    }

    /**
     * Construit une chaîne de caractères qui contiendra le
     * noeud spécifié ainsi que tous les noeuds enfants.
     *
     * @param model  Arborescence à écrire.
     * @param node   Noeud de l'arborescence à écrire.
     * @param buffer Buffer dans lequel écrire le noeud.
     * @param level  Niveau d'indentation (à partir de 0).
     * @param last   Indique si les niveaux précédents sont en train d'écrire leurs derniers items.
     * @return       Le tableau {@code last}, qui peut éventuellement avoir été agrandit.
     */
    private static boolean[] toString(final TreeModel model, final Object node,
                                      final StringBuilder buffer, final int level, boolean[] last,
                                      final String lineSeparator)
    {
        for (int i=0; i<level; i++) {
            if (i != level-1) {
                buffer.append(last[i] ? '\u00A0' : '\u2502')
                      .append("\u00A0\u00A0\u00A0");
            } else {
                buffer.append(last[i] ? '\u2514': '\u251C')
                      .append("\u2500\u2500\u2500");
            }
        }
        buffer.append(node).append(lineSeparator);
        if (level >= last.length) {
            last = XArray.resize(last, level*2);
        }
        final int count = model.getChildCount(node);
        for (int i=0; i<count; i++) {
            last[level] = (i == count-1);
            last = toString(model, model.getChild(node,i), buffer, level+1, last, lineSeparator);
        }
        return last;
    }

    /**
     * Returns a graphical representation  of the specified tree model. This representation can
     * be printed to the {@linkplain System#out standard output stream} (for example) if it uses
     * a monospaced font and supports unicode.
     *
     * @param  tree The tree to format.
     * @param  root First node to format.
     * @return A string representation of the tree, or {@code null} if it doesn't contain any node.
     */
    private static String toString(final TreeModel tree, final Object root) {
        if (root == null) {
            return null;
        }
        final StringBuilder buffer = new StringBuilder();
        toString(tree, root, buffer, 0, new boolean[64],
                 System.getProperty("line.separator", "\n"));
        return buffer.toString();
    }

    /**
     * Returns a graphical representation of the specified tree model. This representation can
     * be printed to the {@linkplain System#out standard output stream} (for example) if it uses
     * a monospaced font and supports unicode.
     * <p>
     * This method should not be defined here, since this class is about optional dependencies.
     * It should be defined in {@link org.geotools.gui.swing.tree.Trees} instead. However we put
     * it here (for now) because it is used in some module that don't want to depend on widgets.
     *
     * @param  tree The tree to format.
     * @return A string representation of the tree, or {@code null} if it doesn't contain any node.
     */
    public static String toString(final TreeModel tree) {
        return toString(tree, tree.getRoot());
    }

    /**
     * Returns a graphical representation of the specified tree. This representation can be
     * printed to the {@linkplain System#out standard output stream} (for example) if it uses
     * a monospaced font and supports unicode.
     * <p>
     * This method should not be defined here, since this class is about optional dependencies.
     * It should be defined in {@link org.geotools.gui.swing.tree.Trees} instead. However we put
     * it here (for now) because it is used in some module that don't want to depend on widgets.
     *
     * @param  node The root node of the tree to format.
     * @return A string representation of the tree, or {@code null} if it doesn't contain any node.
     */
    public static String toString(final TreeNode node) {
        return toString(new DefaultTreeModel(node, true));
    }
}
