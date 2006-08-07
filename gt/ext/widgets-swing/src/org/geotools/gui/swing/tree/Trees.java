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
package org.geotools.gui.swing.tree;

// J2SE dependencies
import java.util.List;
import java.util.ArrayList;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeModel;
import javax.swing.tree.DefaultTreeModel;

// Geotools dependencies
import org.geotools.resources.XArray;


/**
 * Convenience static methods for trees operations.
 *
 * @since 2.0
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public final class Trees {
    /**
     * Interdit la création d'objets de cette classe.
     */
    private Trees() {
    }

    /**
     * Returns the path to the specified
     * {@linkplain org.geotools.gui.swing.tree.TreeNode#getUserObject user object}. For each tree
     * node which are actually instance of Geotools {@link org.geotools.gui.swing.tree.TreeNode},
     * this method compares the specified {@code value} against the user object returned by the
     * {@link org.geotools.gui.swing.tree.TreeNode#getUserObject} method.
     *
     * @param  model The tree model to inspect.
     * @param  value User object to compare to
     *         {@link org.geotools.gui.swing.tree.TreeNode#getUserObject}.
     * @return The paths to the specified value, or an empty array if none.
     */
    public static TreePath[] getPathsToUserObject(final TreeModel model, final Object value) {
        final List paths = new ArrayList(8);
        final Object[] path = new Object[8];
        path[0] = model.getRoot();
        getPathsToUserObject(model, value, path, 1, paths);
        return (TreePath[]) paths.toArray(new TreePath[paths.size()]);
    }

    /**
     * Implémentation de la recherche des chemins. Cette
     * méthode s'appele elle-même d'une façon récursive.
     *
     * @param  model  Modèle dans lequel rechercher le chemin.
     * @param  value  Objet à rechercher dans
     *                {@link org.geotools.gui.swing.tree.TreeNode#getUserObject}.
     * @param  path   Chemin parcouru jusqu'à maintenant.
     * @param  length Longueur valide de {@code path}.
     * @param  list   Liste dans laquelle ajouter les {@link TreePath} trouvés.
     * @return {@code path}, ou un nouveau tableau s'il a fallu l'agrandir.
     */
    private static Object[] getPathsToUserObject(final TreeModel model, final Object value,
                                                 Object[] path, final int length, final List list)
    {
        final Object parent = path[length-1];
        if (parent instanceof org.geotools.gui.swing.tree.TreeNode) {
            final Object nodeValue = ((org.geotools.gui.swing.tree.TreeNode)parent).getUserObject();
            if (nodeValue==value || (value!=null && value.equals(nodeValue))) {
                list.add(new TreePath(XArray.resize(path, length)));
            }
        }
        final int count = model.getChildCount(parent);
        for (int i=0; i<count; i++) {
            if (length >= path.length) {
                path = XArray.resize(path, length << 1);
            }
            path[length] = model.getChild(parent, i);
            path = getPathsToUserObject(model, value, path, length+1, list);
        }
        return path;
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
                                      final StringBuffer buffer, final int level, boolean[] last)
    {
        for (int i=0; i<level; i++) {
            if (i != level-1) {
                buffer.append(last[i] ? '\u00A0' : '\u2502');
                buffer.append("\u00A0\u00A0\u00A0");
            } else {
                buffer.append(last[i] ? '\u2514': '\u251C');
                buffer.append("\u2500\u2500\u2500");
            }
        }
        buffer.append(node);
        buffer.append('\n');
        if (level >= last.length) {
            last = XArray.resize(last, level*2);
        }
        final int count=model.getChildCount(node);
        for (int i=0; i<count; i++) {
            last[level] = (i == count-1);
            last=toString(model, model.getChild(node,i), buffer, level+1, last);
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
        final StringBuffer buffer = new StringBuffer();
        toString(tree, root, buffer, 0, new boolean[64]);
        return buffer.toString();
    }

    /**
     * Returns a graphical representation of the specified tree model. This representation can
     * be printed to the {@linkplain System#out standard output stream} (for example) if it uses
     * a monospaced font and supports unicode.
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
     *
     * @param  node The root node of the tree to format.
     * @return A string representation of the tree, or {@code null} if it doesn't contain any node.
     */
    public static String toString(final TreeNode node) {
        return toString(new DefaultTreeModel(node, true));
    }
}
