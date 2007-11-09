/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.geotools.gui.swing.contexttree;

import javax.swing.tree.TreePath;
import org.geotools.gui.swing.contexttree.ContextTreeNode;
import org.geotools.map.MapContext;

final class DADMetaTransfer{
    
    public int origine = 0;
    public MapContext origine_parent = null;
    public ContextTreeNode draggedNode;
    public TreePath dragPath;
    
}