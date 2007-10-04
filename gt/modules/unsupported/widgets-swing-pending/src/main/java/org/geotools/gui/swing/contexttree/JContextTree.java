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

import org.geotools.gui.swing.contexttree.column.ColumnModel;
import org.geotools.gui.swing.contexttree.column.StyleColumnModel;
import org.geotools.gui.swing.contexttree.column.VisibleColumnModel;
import org.geotools.gui.swing.contexttree.renderer.ColumnHeader;
import org.geotools.gui.swing.contexttree.renderer.HeaderRenderer;
import org.geotools.gui.swing.contexttree.renderer.TreeNodeProvider;
import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.GridLayout;
import java.util.ArrayList;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.tree.TreePath;
import org.geotools.gui.swing.contexttree.column.OpacityColumnModel;
import org.geotools.map.MapContext;
import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jdesktop.swingx.renderer.DefaultTreeRenderer;
import org.geotools.gui.swing.i18n.TextBundle;

/**
 * Tree Component for easy MapContext and MapLayer management
 * @author johann sorel
 */
public class JContextTree extends JPanel {
    
    private ContextTreeModel model = new ContextTreeModel();
    private JXTreeTable tree = new JXTreeTable(model);
    private JContextTreePopup popup = new JContextTreePopup();
    private ArrayList<ColumnModel> columns = new ArrayList<ColumnModel>();
    
    /**
     * constructor
     */
    public JContextTree() {
       this(false);
    }
    
    public JContextTree(boolean complete){
        super(new GridLayout(1, 1));
        init();
        
        if(complete){
            addColumnModel( new VisibleColumnModel());
            addColumnModel( new OpacityColumnModel());
            addColumnModel( new StyleColumnModel());            
            getPopup().activeDefaultPopups();
        }
        
        
    }
    
    private void init(){
        tree.setComponentPopupMenu(popup);
        tree.setColumnControlVisible(true);
        tree.setTreeCellRenderer(new DefaultTreeRenderer(new TreeNodeProvider(this)));
        
        ColumnHeader head0 = new ColumnHeader(TextBundle.getResource().getString("col_tree"), new JLabel());
        tree.getColumnModel().getColumn(0).setHeaderValue(head0);
        tree.getColumnModel().getColumn(0).setHeaderRenderer(new HeaderRenderer());
        
        tree.setHighlighters(new Highlighter[]{HighlighterFactory.createAlternateStriping(Color.white, HighlighterFactory.QUICKSILVER, 1)});
        tree.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        ContextTreeTransferHandler handler = new ContextTreeTransferHandler();
        tree.setTransferHandler(handler);
        tree.setDropTarget(new ContextTreeDrop(handler));
        tree.setDragEnabled(true);
        
        popup.setTree(this,tree);
        
        JScrollPane pane = new JScrollPane(getTreeTable());
        pane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        pane.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        add(pane);
    }
    
    
    
    
    /**
     * get the default treepopup of the jcontexttree
     * @return JXMapContextTreePopup used by the tree
     */
    public JContextTreePopup getPopup() {
        return popup;
    }
    /**
     * model used by the jcontexttree, <b>use at your own risks</b>
     * @return JContextTreeModel
     * @deprecated 
     */
    @Deprecated
    private ContextTreeModel getModel() {
        return model;
    }
    /**
     * get the swinglabs jxtreetable. <b>use at your own risks</b>
     * @return JXTreeTable
     * @deprecated 
     */
    @Deprecated
    public JXTreeTable getTreeTable() {
        return tree;
    }
    
    
////////////////////////////////////////////////////////////////////////////////
// COLUMNS MANAGEMENT //////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////
    
    /**
     * add a new column in the model and update the treetable
     * @param model the new column model
     */
    public void addColumnModel(ColumnModel model){
        this.model.addColumnModel(model);
        columns.add(model);
        
        tree.getColumnModel().addColumn(model.getTableColumnExt());
        tree.revalidate();
    }
    /**
     * get the list of column
     * @return list of column models
     */
    public ArrayList<ColumnModel> getColumnModels() {
        return columns;
    }
    
////////////////////////////////////////////////////////////////////////////////
// MAPCONTEXT MANAGEMENT ///////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////
    
    /**
     * get the active context
     * @return return the active MapContext, if none return null
     */
    public MapContext getActiveContext() {
        return model.getActiveContext();
    }
    /**
     * active the context if in the tree
     * @param context the mapcontext to active
     */
    public void setActiveContext(MapContext context) {
        model.setActiveContext(context);
    }
    /**
     * add context to the Tree if not allready in it
     * @param context the context to add
     */
    public void addMapContext(MapContext context) {
        model.addMapContext(context);
        tree.expandPath(new TreePath(model.getRoot()));
    }
    /**
     * remove context from the tree
     * @param context target mapcontext to remove
     */
    public void removeMapContext(MapContext context){
        model.removeMapContext(context);
    }
    /**
     * count MapContext in the tree
     * @return number of mapcontext in the tree
     */
    public int getMapContextCount(){
        return model.getMapContextCount();
    }
    /**
     * return context at index i
     * @param i position of the mapcontext
     * @return the mapcontext a position i
     */
    public MapContext getMapContext(int i){
        return model.getMapContext(i);
    }
    /**
     * get the index of a mapcontext in the tree
     * @param context the mapcontext to find
     * @return index of context
     */
    public int getMapContextIndex(MapContext context){
        return model.getMapContextIndex(context);
    }
    /**
     * move a mapcontext
     * @param context the context to move
     * @param newplace new position of the child node
     */
    public void moveMapContext(MapContext context, int newplace ){
        ContextTreeNode moveNode = (ContextTreeNode) model.getMapContextNode(context);
        ContextTreeNode father = (ContextTreeNode) moveNode.getParent();
        model.moveMapContext(moveNode,father,newplace);
    }
    
////////////////////////////////////////////////////////////////////////////////
// LISTENERS ///////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////
    
    /**
     * add treeListener to Model
     * @param ker the new listener
     */
    public void addTreeListener(TreeListener ker ){
        model.addTreeListener(ker);
    }
    /**
     * remove treeListener from Model
     * @param ker the listner to remove
     */
    public void removeTreeListener( TreeListener ker ){
        model.removeTreeListener(ker);
    }
    /**
     * get treeListeners list
     * @return the listener's table
     */
    public TreeListener[] getTreeListeners(){
        return model.getTreeListeners();
    }
}
