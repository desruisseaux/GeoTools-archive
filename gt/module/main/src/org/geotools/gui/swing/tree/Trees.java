/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Management Committee (PMC)
 * (C) 2001, Institut de Recherche pour le D�veloppement
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
 *
 * Contacts:
 *     UNITED KINGDOM: James Macgill
 *             mailto:j.macgill@geog.leeds.ac.uk
 *
 *     FRANCE: Surveillance de l'Environnement Assist�e par Satellite
 *             Institut de Recherche pour le D�veloppement / US-Espace
 *             mailto:seasnet@teledetection.fr
 */
package org.geotools.gui.swing.tree;

// J2SE dependencies
import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.geotools.resources.XArray;


/**
 * Convenience static methods for trees operations.
 *
 * @version $Id: Trees.java,v 1.1 2003/05/29 16:04:31 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public final class Trees {
    /**
     * Interdit la cr�ation d'objets de cette classe.
     */
    private Trees() {
    }

    /**
     * Retourne les chemins vers l'objet sp�cifi�. Cette m�thode suppose que l'arborescence
     * est constitu�e de noeuds {@link org.geotools.gui.swing.tree.TreeNode} et comparera
     * <code>value</code> avec les objets retourn�s par la m�thode
     * {@link org.geotools.gui.swing.tree.TreeNode#getUserObject}. Les noeuds qui ne sont
     * pas des objets {@link org.geotools.gui.swing.tree.TreeNode} ne seront pas
     * compar�s � <code>value</code>.
     *
     * @param  model Mod�le dans lequel rechercher le chemin.
     * @param  value Objet � rechercher dans
     *         {@link org.geotools.gui.swing.tree.TreeNode#getUserObject}.
     * @return Chemins vers l'objet sp�cifi�. Ce tableau peut avoir une
     *         longueur de 0, mais ne sera jamais <code>null</code>.
     */
    public static TreePath[] getPathsToUserObject(final TreeModel model, final Object value) {
        final List paths = new ArrayList(8);
        final Object[] path = new Object[8];
        path[0] = model.getRoot();
        getPathsToUserObject(model, value, path, 1, paths);
        return (TreePath[]) paths.toArray(new TreePath[paths.size()]);
    }

    /**
     * Impl�mentation de la recherche des chemins. Cette
     * m�thode s'appele elle-m�me d'une fa�on r�cursive.
     *
     * @param  model  Mod�le dans lequel rechercher le chemin.
     * @param  value  Objet � rechercher dans
     *                {@link org.geotools.gui.swing.tree.TreeNode#getUserObject}.
     * @param  path   Chemin parcouru jusqu'� maintenant.
     * @param  length Longueur valide de <code>path</code>.
     * @param  list   Liste dans laquelle ajouter les {@link TreePath} trouv�s.
     * @return <code>path</code>, ou un nouveau tableau s'il a fallu l'agrandir.
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
     * Construit une cha�ne de caract�res qui contiendra le
     * noeud sp�cifi� ainsi que tous les noeuds enfants.
     *
     * @param model  Arborescence � �crire.
     * @param node   Noeud de l'arborescence � �crire.
     * @param buffer Buffer dans lequel �crire le noeud.
     * @param level  Niveau d'indentation (� partir de 0).
     * @param last   Indique si les niveaux pr�c�dents sont
     *               en train d'�crire leurs derniers items.
     * @return       Le tableau <code>last</code>, qui peut
     *               �ventuellement avoir �t� agrandit.
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
     * Retourne une cha�ne de caract�res qui contiendra une
     * repr�sentation graphique de l'arborescence sp�cifi�e.
     * Cette arborescence appara�tre correctement si elle
     * est �crite avec une police mono-espac�e.
     *
     * @param  tree Arborescence � �crire.
     * @param  root Noeud � partir d'o� commencer � tracer l'arborescence.
     * @return Cha�ne de caract�res repr�sentant l'arborescence, ou
     *         <code>null</code> si <code>root</code> �tait nul.
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
     * Retourne une cha�ne de caract�res qui contiendra une
     * repr�sentation graphique de l'arborescence sp�cifi�e.
     * Cette arborescence appara�tre correctement si elle
     * est �crite avec une police mono-espac�e.
     *
     * @param  tree Arborescence � �crire.
     * @return Cha�ne de caract�res repr�sentant l'arborescence, ou
     *         <code>null</code> si l'arborescence ne contenait aucun noeud.
     */
    public static String toString(final TreeModel tree) {
        return toString(tree, tree.getRoot());
    }

    /**
     * Retourne une cha�ne de caract�res qui contiendra une
     * repr�sentation graphique de l'arborescence sp�cifi�e.
     * Cette arborescence appara�tre correctement si elle
     * est �crite avec une police mono-espac�e.
     *
     * @param  node Noeud � partir d'o� �crire l'arborescence.
     * @return Cha�ne de caract�res repr�sentant l'arborescence.
     */
    public static String toString(final TreeNode node) {
        return toString(new DefaultTreeModel(node, true));
    }
}
