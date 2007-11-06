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

import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.TableCellEditor;
import javax.swing.tree.TreePath;
import org.geotools.gui.swing.contexttree.column.OpacityTreeTableColumn;
import org.geotools.gui.swing.contexttree.column.StyleTreeTableColumn;
import org.geotools.gui.swing.contexttree.column.TreeTableColumn;
import org.geotools.gui.swing.contexttree.column.VisibleTreeTableColumn;
import org.geotools.gui.swing.contexttree.renderer.DefaultHeaderRenderer;
import org.geotools.gui.swing.contexttree.renderer.TreeNodeProvider;
import org.geotools.gui.swing.i18n.TextBundle;
import org.geotools.map.MapContext;
import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jdesktop.swingx.renderer.DefaultTreeRenderer;

/**
 *
 * @author johann sorel
 */
public class ContextTreeTable extends JXTreeTable {

    
    
    public ContextTreeTable(){
        this(false);
    }
    
    public ContextTreeTable(boolean defaultRendering) {
        super(new ContextTreeModel());

        setComponentPopupMenu(new JContextTreePopup(this));
        setColumnControlVisible(true);
        setTreeCellRenderer(new DefaultTreeRenderer(new TreeNodeProvider(this)));


        getColumnModel().getColumn(0).setHeaderRenderer(new DefaultHeaderRenderer(null, null, TextBundle.getResource().getString("col_tree")));

        setHighlighters(new Highlighter[]{HighlighterFactory.createAlternateStriping(Color.white, HighlighterFactory.QUICKSILVER, 1)});
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        ContextTreeTransferHandler handler = new ContextTreeTransferHandler();
        setTransferHandler(handler);
        setDropTarget(new ContextTreeDrop(handler));
        setDragEnabled(true);

        this.addMouseMotionListener(new MouseMotionListener() {

                    public void mouseDragged(MouseEvent e) {
                    }

                    public void mouseMoved(MouseEvent e) {
                        Point p = e.getPoint();
                        if (p != null) {
                            int row = rowAtPoint(p);
                            int col = columnAtPoint(p);


                            if (row != editingRow || col != editingColumn) {


                                if (isEditing()) {
                                    TableCellEditor editor = cellEditor;
                                    if (!editor.stopCellEditing()) {
                                        editor.cancelCellEditing();
                                    }
                                }

                                if (!isEditing() && col >= 0 && row >= 0) {

                                    //we handle differently ContextTreeColumn
                                    if (getColumnExt(col) instanceof TreeTableColumn) {
                                        TreeTableColumn column = (TreeTableColumn) getColumnExt(col);
                                        if (isCellEditable(row, col) && column.isEditableOnMouseOver()) {
                                            editCellAt(row, col);
                                        }

                                    }

                                }
                            }

                        }

                    }
                });


        //build default rendering
        if (defaultRendering) {
            getTreeTableModel().addColumnModel(new VisibleTreeTableColumn());
            getTreeTableModel().addColumnModel(new OpacityTreeTableColumn());
            getTreeTableModel().addColumnModel(new StyleTreeTableColumn());
            ((JContextTreePopup) getComponentPopupMenu()).activeDefaultPopups();
        }

    }

    
    
    
    @Override
    public ContextTreeModel getTreeTableModel() {
        return (ContextTreeModel) super.getTreeTableModel();
    }
    
    
////////////////////////////////////////////////////////////////////////////////
// COLUMNS MANAGEMENT //////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////
    
    /**
     * add a new column in the model and update the treetable
     * @param model the new column model
     */
    public void addColumnModel(TreeTableColumn model) {
        getTreeTableModel().addColumnModel(model);

        getColumnModel().addColumn(model);
        revalidate();
    }
    /**
     * get the list of column
     * @return list of column models
     */

    public TreeTableColumn[] getColumnModels() {
        return (TreeTableColumn[]) getTreeTableModel().getColumnModels().toArray();
    }
    
////////////////////////////////////////////////////////////////////////////////
// MAPCONTEXT MANAGEMENT ///////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////

    /**
     * get the active context
     * @return return the active MapContext, if none return null
     */
    public MapContext getActiveContext() {
        return getTreeTableModel().getActiveContext();
    }
    /**
     * active the context if in the tree
     * @param context the mapcontext to active
     */

    public void setActiveContext(MapContext context) {
        getTreeTableModel().setActiveContext(context);
    }
    /**
     * add context to the Tree if not allready in it
     * @param context the context to add
     */

    public void addMapContext(MapContext context) {
        getTreeTableModel().addMapContext(context);
        expandPath(new TreePath(getTreeTableModel().getRoot()));
    }
    /**
     * remove context from the tree
     * @param context target mapcontext to remove
     */

    public void removeMapContext(MapContext context) {
        getTreeTableModel().removeMapContext(context);
    }
    /**
     * count MapContext in the tree
     * @return number of mapcontext in the tree
     */

    public int getMapContextCount() {
        return getTreeTableModel().getMapContextCount();
    }
    /**
     * return context at index i
     * @param i position of the mapcontext
     * @return the mapcontext a position i
     */

    public MapContext getMapContext(int i) {
        return getTreeTableModel().getMapContext(i);
    }
    /**
     * get the index of a mapcontext in the tree
     * @param context the mapcontext to find
     * @return index of context
     */

    public int getMapContextIndex(MapContext context) {
        return getTreeTableModel().getMapContextIndex(context);
    }
    /**
     * move a mapcontext
     * @param context the context to move
     * @param newplace new position of the child node
     */

    public void moveMapContext(MapContext context, int newplace) {
        ContextTreeNode moveNode = (ContextTreeNode) getTreeTableModel().getMapContextNode(context);
        ContextTreeNode father = (ContextTreeNode) moveNode.getParent();
        getTreeTableModel().moveMapContext(moveNode, father, newplace);
    }

////////////////////////////////////////////////////////////////////////////////
// LISTENERS ///////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////

    /**
     * add treeListener to Model
     * @param ker the new listener
     */
    public void addTreeListener(TreeListener ker) {
        getTreeTableModel().addTreeListener(ker);
    }
    /**
     * remove treeListener from Model
     * @param ker the listner to remove
     */

    public void removeTreeListener(TreeListener ker) {
        getTreeTableModel().removeTreeListener(ker);
    }
    /**
     * get treeListeners list
     * @return the listener's table
     */

    public TreeListener[] getTreeListeners() {
        return getTreeTableModel().getTreeListeners();
    }
    
        
}
