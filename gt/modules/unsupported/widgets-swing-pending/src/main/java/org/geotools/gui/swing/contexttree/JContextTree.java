/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2007, GeoTools Project Managment Committee (PMC)
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
package org.geotools.gui.swing.contexttree;

import java.awt.ComponentOrientation;
import java.awt.GridLayout;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.tree.TreePath;

import org.geotools.gui.swing.JMapPane;
import org.geotools.gui.swing.contexttree.column.TreeTableColumn;
import org.geotools.gui.swing.contexttree.column.OpacityTreeTableColumn;
import org.geotools.gui.swing.contexttree.column.StyleTreeTableColumn;
import org.geotools.gui.swing.contexttree.column.VisibleTreeTableColumn;
import org.geotools.map.MapContext;

/**
 * Tree Component for easy MapContext and MapLayer management
 * @author johann sorel
 */
public class JContextTree extends JPanel {

    private TreeTable tree = null;
    private ArrayList<TreeTableColumn> columns = new ArrayList<TreeTableColumn>();

    /**
     * constructor
     */
    public JContextTree() {
        super(new GridLayout(1, 1));
        tree = new TreeTable();
        init();
    }

    private void init() {
        JScrollPane pane = new JScrollPane(getTreeTable());
        pane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        pane.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        add(pane);
    }

    /**
     * get the swinglabs jxtreetable. 
     * @return JXTreeTable
     */
    public TreeTable getTreeTable() {
        return tree;
    }

    ////////////////////////////////////////////////////////////////////////////////
// COLUMNS MANAGEMENT //////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////

    /**
     * add a new column in the model and update the treetable
     * @param model the new column model
     */
    public void addColumnModel(TreeTableColumn model) {
        tree.getTreeTableModel().addColumnModel(model);

        tree.getColumnModel().addColumn(model);
        revalidate();
    }
    /**
     * get the list of column
     * @return list of column models
     */

    public TreeTableColumn[] getColumnModels() {
        return (TreeTableColumn[]) tree.getTreeTableModel().getColumnModels().toArray();
    }

    ////////////////////////////////////////////////////////////////////////////////
// MAPCONTEXT MANAGEMENT ///////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////

    /**
     * get the active context
     * @return return the active MapContext, if none return null
     */
    public MapContext getActiveContext() {
        return tree.getTreeTableModel().getActiveContext();
    }
    /**
     * active the context if in the tree
     * @param context the mapcontext to active
     */

    public void setActiveContext(MapContext context) {
        tree.getTreeTableModel().setActiveContext(context);
    }
    /**
     * add context to the Tree if not allready in it
     * @param context the context to add
     */

    public void addMapContext(MapContext context) {
        tree.getTreeTableModel().addMapContext(context);
        tree.expandPath(new TreePath(tree.getTreeTableModel().getRoot()));
    }
    /**
     * remove context from the tree
     * @param context target mapcontext to remove
     */

    public void removeMapContext(MapContext context) {
        tree.getTreeTableModel().removeMapContext(context);
    }
    /**
     * count MapContext in the tree
     * @return number of mapcontext in the tree
     */

    public int getMapContextCount() {
        return tree.getTreeTableModel().getMapContextCount();
    }
    /**
     * return context at index i
     * @param i position of the mapcontext
     * @return the mapcontext a position i
     */

    public MapContext getMapContext(int i) {
        return tree.getTreeTableModel().getMapContext(i);
    }
    /**
     * get the index of a mapcontext in the tree
     * @param context the mapcontext to find
     * @return index of context
     */

    public int getMapContextIndex(MapContext context) {
        return tree.getTreeTableModel().getMapContextIndex(context);
    }
    /**
     * move a mapcontext
     * @param context the context to move
     * @param newplace new position of the child node
     */

    public void moveMapContext(MapContext context, int newplace) {
        ContextTreeNode moveNode = (ContextTreeNode) tree.getTreeTableModel().getMapContextNode(context);
        ContextTreeNode father = (ContextTreeNode) moveNode.getParent();
        tree.getTreeTableModel().moveMapContext(moveNode, father, newplace);
    }

    ////////////////////////////////////////////////////////////////////////////////
// LISTENERS ///////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////

    /**
     * add treeListener to Model
     * @param ker the new listener
     */
    public void addTreeListener(TreeListener ker) {
        tree.getTreeTableModel().addTreeListener(ker);
    }
    /**
     * remove treeListener from Model
     * @param ker the listner to remove
     */

    public void removeTreeListener(TreeListener ker) {
        tree.getTreeTableModel().removeTreeListener(ker);
    }
    /**
     * get treeListeners list
     * @return the listener's table
     */

    public TreeListener[] getTreeListeners() {
        return tree.getTreeTableModel().getTreeListeners();
    }

    ////////////////////////////////////////////////////////////////////////////
    // STATIC CONSTRUCTORS /////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////

    public static JContextTree createDefaultTree(JMapPane map) {
        JContextTree tree = new JContextTree();


        tree.addColumnModel(new VisibleTreeTableColumn());
        tree.addColumnModel(new OpacityTreeTableColumn());
        tree.addColumnModel(new StyleTreeTableColumn());
        ((JContextTreePopup) tree.getTreeTable().getComponentPopupMenu()).activeDefaultPopups();
        ((JContextTreePopup) tree.getTreeTable().getComponentPopupMenu()).setMapPane(map);
        tree.revalidate();

        return tree;
    }
}
