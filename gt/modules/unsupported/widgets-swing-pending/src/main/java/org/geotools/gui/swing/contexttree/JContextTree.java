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

import org.geotools.gui.swing.contexttree.draganddrop.MultiContextTreeDrop;
import org.geotools.gui.swing.contexttree.draganddrop.MultiContextTreeTransferHandler;
import java.awt.Color;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableCellEditor;
import javax.swing.tree.TreePath;
import org.geotools.gui.swing.JMapPane;
import org.geotools.gui.swing.contexttree.column.OpacityTreeTableColumn;
import org.geotools.gui.swing.contexttree.column.StyleTreeTableColumn;
import org.geotools.gui.swing.contexttree.column.TreeTableColumn;
import org.geotools.gui.swing.contexttree.column.VisibleTreeTableColumn;
import org.geotools.gui.swing.contexttree.renderer.DefaultHeaderRenderer;
import org.geotools.gui.swing.contexttree.renderer.TreeNodeProvider;
import org.geotools.gui.swing.i18n.TextBundle;
import org.geotools.gui.swing.misc.FacilitiesFactory;
import org.geotools.map.MapContext;
import org.geotools.map.MapLayer;
import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jdesktop.swingx.renderer.DefaultTreeRenderer;
import org.jdesktop.swingx.treetable.TreeTableModel;


/**
 *
 * @author johann sorel
 */
public class JContextTree extends JXTreeTable {

    /**
     * Default copy action used for Key Input
     */
    public final Action COPY_ACTION = new AbstractAction() {

                public void actionPerformed(ActionEvent e) {
                    copySelectionInBuffer();
                }
                };
    /**
     * Default cut action used for Key Input
     */
    public final Action CUT_ACTION = new AbstractAction() {

                public void actionPerformed(ActionEvent e) {
                    cutSelectionInBuffer();
                }
                };
    /**
     * Default paste action used for Key Input
     */
    public final Action PASTE_ACTION = new AbstractAction() {

                public void actionPerformed(ActionEvent e) {
                    pasteBuffer();
                }
                };
    /**
     * Default delete action used for Key Input
     */
    public final Action DELETE_ACTION = new AbstractAction() {

                public void actionPerformed(ActionEvent e) {
                    deleteSelection();
                }
                };
    /**
     * Default duplicate action used for Key Input
     */
    public final Action DUPLICATE_ACTION = new AbstractAction() {

                public void actionPerformed(ActionEvent e) {
                    duplicateSelection();
                }
                };
                
    /**
     * the buffer containing the cutted/copied datas
     */            
    private final List<Object> buffer = new ArrayList<Object>();
    
    /**
     * String added to layer name use when paste/duplicate
     */
    private String COPY_NAME = "-" + TextBundle.getResource().getString("a_copy") + "- ";

    /**
     * Tree widget to manage MapContexts and MapLayers
     * 
     */
    public JContextTree() {
        super(new ContextTreeModel());

        setComponentPopupMenu(new JContextTreePopup(this));
        setColumnControlVisible(true);
        setTreeCellRenderer(new DefaultTreeRenderer(new TreeNodeProvider(this)));

        getColumnModel().getColumn(0).setHeaderRenderer(new DefaultHeaderRenderer(null, null, TextBundle.getResource().getString("col_tree")));
        setHighlighters(new Highlighter[]{HighlighterFactory.createAlternateStriping(Color.white, HighlighterFactory.QUICKSILVER, 1)});

        initCellEditAcceleration();
        initDragAndDrop();
        initKeySupport();
    }
    
    /**
     * add mouse listener to set cell in edit mode when mouseover
     */
    private void initCellEditAcceleration() {
        //listener to set cell in edit mode on mouse over
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
    }

    private void initDragAndDrop() {
        setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        MultiContextTreeTransferHandler handler = new MultiContextTreeTransferHandler();
        setTransferHandler(handler);
        setDropTarget(new MultiContextTreeDrop(handler));
        setDragEnabled(true);

    }

    private void initKeySupport() {
        InputMap inputMap = getInputMap();
        ActionMap actionMap = getActionMap();

        KeyStroke copyKeys = KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_MASK);
        KeyStroke cutKeys = KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.CTRL_MASK);
        KeyStroke pasteKeys = KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.CTRL_MASK);
        KeyStroke deleteKeys = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0);
        KeyStroke duplicateKeys = KeyStroke.getKeyStroke(KeyEvent.VK_D, KeyEvent.CTRL_MASK);
        String copycode = "copy";
        String cutcode = "cut";
        String pastecode = "paste";
        String deletecode = "delete";
        String duplicatecode = "duplicate";

        inputMap.put(copyKeys, copycode);
        inputMap.put(cutKeys, cutcode);
        inputMap.put(pasteKeys, pastecode);
        inputMap.put(deleteKeys, deletecode);
        inputMap.put(duplicateKeys, duplicatecode);
        actionMap.put(copycode, COPY_ACTION);
        actionMap.put(cutcode, CUT_ACTION);
        actionMap.put(pastecode, PASTE_ACTION);
        actionMap.put(deletecode, DELETE_ACTION);
        actionMap.put(duplicatecode, DUPLICATE_ACTION);

    }
    
    /**
     * get the tree model. dont play with the model, too much things are linked    
     * @return the tree model
     * @deprecated 
     */
    @Override
    @Deprecated
    public ContextTreeModel getTreeTableModel() {
        return (ContextTreeModel) super.getTreeTableModel();
    }
    
    /**
     * set the tree model. dont play with the model, too much things are linked
     * @param contexttreemodel the new model, <b>MUST</b> be a ContextTreeModel.
     * 
     * @deprecated 
     */
    @Override
    @Deprecated
    public void setTreeTableModel(TreeTableModel contexttreemodel){
        if(contexttreemodel != null){
            if(contexttreemodel instanceof ContextTreeModel){
                super.setTreeTableModel(contexttreemodel);
            }else{
            }
        }        
    }
    
    
    
    
////////////////////////////////////////////////////////////////////////////////
// STATIC CONSTRUCTORS /////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////

    /**
     * create a default JContextTree, with default columns
     * and default JContextTreePopup items
     * 
     * @param map
     * @return default JContextTree
     */
    public static JContextTree createDefaultTree(JMapPane map) {
        JContextTree tree = new JContextTree();


        tree.addColumnModel(new VisibleTreeTableColumn());
        tree.addColumnModel(new OpacityTreeTableColumn());
        tree.addColumnModel(new StyleTreeTableColumn());
        ((JContextTreePopup) tree.getComponentPopupMenu()).activeDefaultPopups();
        ((JContextTreePopup) tree.getComponentPopupMenu()).setMapPane(map);
        tree.revalidate();

        return tree;
    }
        
////////////////////////////////////////////////////////////////////////////////
// CUT/COPY/PASTE/DUPLICATE/DELETE  ////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////
    
    private boolean hasSelection(TreePath[] selections) {

        if (selections != null) {
            if (selections.length > 0) {
                return true;
            }
        }
        return false;
    }

    private boolean onlyMapContexts(List<Object> lst) {

        for (Object obj : lst) {

            if (!(obj instanceof MapContext)) {
                return false;
            }
        }

        return true;
    }

    private boolean onlyMapContexts(TreePath[] paths) {

        for (TreePath path : paths) {

            if (!(((ContextTreeNode) path.getLastPathComponent()).getUserObject() instanceof MapContext)) {
                return false;
            }
        }
        return true;
    }

    private boolean onlyMapLayers(List<Object> lst) {

        for (Object obj : lst) {

            if (!(obj instanceof MapLayer)) {
                return false;
            }
        }

        return true;
    }

    private boolean onlyMapLayers(TreePath[] paths) {

        for (TreePath path : paths) {

            if (!(((ContextTreeNode) path.getLastPathComponent()).getUserObject() instanceof MapLayer)) {
                return false;
            }
        }
        return true;
    }
    
    /**
     *  prefix string used when pasting/duplicating datas
     * 
     * @param prefix if null, prefix will be an empty string
     */
    public void setPrefixString(String prefix){
        if(prefix != null){
            COPY_NAME = prefix;
        }else{
            COPY_NAME = "";
        }
    }
        
    /**
     * prefix used when pasting/duplicating datas
     * 
     * @return String 
     */
    public String getPrefixString() {
        return COPY_NAME;
    }

    /**
     * 
     * @return true if ther is something selected
     */
    public boolean hasSelection() {
        TreePath[] selections = getTreeSelectionModel().getSelectionPaths();

        if (selections != null) {
            if (selections.length > 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Duplicate was is actually selected in the tree. nothing happens
     * if selection isn't composed of only 1 type of datas. (only layers or only contexts )
     * 
     * @return true if duplication succeed
     */
    public boolean duplicateSelection() {
        TreePath[] selections = getTreeSelectionModel().getSelectionPaths();

        if (canDuplicateSelection()) {
            FacilitiesFactory ff = new FacilitiesFactory();

            if (onlyMapLayers(selections)) {

                for (TreePath tp : selections) {
                    MapLayer layer = (MapLayer) ((ContextTreeNode) tp.getLastPathComponent()).getUserObject();
                    MapContext parent = (MapContext) ((ContextTreeNode) ((ContextTreeNode) tp.getLastPathComponent()).getParent()).getUserObject();
                    MapLayer copylayer = ff.duplicateLayer(layer);
                    copylayer.setTitle(COPY_NAME + layer.getTitle());

                    parent.addLayer(copylayer);
                    parent.moveLayer(parent.indexOf(copylayer), parent.indexOf(layer));
                }
                return true;
                

            } else if (onlyMapContexts(selections)) {

                for (TreePath tp : selections) {
                    MapContext context = (MapContext) ((ContextTreeNode) tp.getLastPathComponent()).getUserObject();
                    MapContext copycontext = ff.duplicateContext(context);
                    copycontext.setTitle(COPY_NAME + context.getTitle());

                    getTreeTableModel().addMapContext(copycontext);
                }
                
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 
     * @return true if tree buffer is empty
     */
    public boolean isBufferEmpty() {
        if (buffer.size() == 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 
     * @return true is paste can succeed
     */
    public boolean canPasteBuffer() {
        if (isBufferEmpty()) {
            return false;
        } else {

            if (onlyMapContexts(buffer)) {
                return true;
            } else if (onlyMapLayers(buffer)) {
                TreePath[] selections = getTreeSelectionModel().getSelectionPaths();

                if (hasSelection(selections)) {
                    if (selections.length == 1) {
                        return true;
                    }
                }
            }

        }
        return false;
    }
    
    /**
     * 
     * @return true if duplication can succeed
     */
    public boolean canDuplicateSelection() {
        TreePath[] selections = getTreeSelectionModel().getSelectionPaths();

        if (hasSelection(selections)) {
            return (onlyMapContexts(selections) || onlyMapLayers(selections));
        } else {
            return false;
        }
    }

    /**
     * 
     * @return true if delete can succeed
     */
    public boolean canDeleteSelection() {
        return hasSelection();
    }

    /**
     * 
     * @return true if copy can succeed
     */
    public boolean canCopySelection() {
        return canDuplicateSelection();
    }

    /**
     * 
     * @return true if cut can succeed
     */
    public boolean canCutSelection() {
        return canDuplicateSelection();
    }

    /**
     * delete what is actually selected
     * 
     * @return true if delete suceed
     */
    public boolean deleteSelection() {

        if (canDeleteSelection()) {
            TreePath[] selections = getTreeSelectionModel().getSelectionPaths();

            for (int i = selections.length - 1; i >= 0; i--) {
                TreePath tp = selections[i];

                if (((ContextTreeNode) tp.getLastPathComponent()).getUserObject() instanceof MapLayer) {
                    MapLayer layer = (MapLayer) ((ContextTreeNode) tp.getLastPathComponent()).getUserObject();
                    MapContext parent = (MapContext) ((ContextTreeNode) ((ContextTreeNode) tp.getLastPathComponent()).getParent()).getUserObject();

                    parent.removeLayer(layer);

                } else if (((ContextTreeNode) tp.getLastPathComponent()).getUserObject() instanceof MapContext) {
                    MapContext context = (MapContext) ((ContextTreeNode) tp.getLastPathComponent()).getUserObject();
                    getTreeTableModel().removeMapContext(context);
                }
            }
            
            return true;
        }
        
        return false;

    }

    /**
     * copy what is actually selected in the tree buffer
     * 
     * @return true if copy succeed
     */
    public boolean copySelectionInBuffer() {
        TreePath[] selections = getTreeSelectionModel().getSelectionPaths();

        if (hasSelection(selections)) {
            buffer.clear();

            if (onlyMapLayers(selections)) {

                for (TreePath tp : selections) {
                    MapLayer layer = (MapLayer) ((ContextTreeNode) tp.getLastPathComponent()).getUserObject();
                    buffer.add(layer);
                }

                return true;
            } else if (onlyMapContexts(selections)) {

                for (TreePath tp : selections) {
                    MapContext context = (MapContext) ((ContextTreeNode) tp.getLastPathComponent()).getUserObject();
                    buffer.add(context);
                }
                
                return true;
            }


        }
        
        return false;

    }

    /**
     * copy what is actually selected in the tree buffer and cut it from the tree.
     * 
     * @return true if cut succeed
     */
    public boolean cutSelectionInBuffer() {
        TreePath[] selections = getTreeSelectionModel().getSelectionPaths();

        if (hasSelection(selections)) {
            buffer.clear();


            if (onlyMapLayers(selections)) {

                for (TreePath tp : selections) {
                    MapLayer layer = (MapLayer) ((ContextTreeNode) tp.getLastPathComponent()).getUserObject();
                    MapContext parent = (MapContext) ((ContextTreeNode) ((ContextTreeNode) tp.getLastPathComponent()).getParent()).getUserObject();

                    buffer.add(layer);
                    parent.removeLayer(layer);
                }
                return true;
            } else if (onlyMapContexts(selections)) {

                for (TreePath tp : selections) {
                    MapContext context = (MapContext) ((ContextTreeNode) tp.getLastPathComponent()).getUserObject();

                    buffer.add(context);
                    getTreeTableModel().removeMapContext(context);
                }
                return true;
            }

        }
        return false;
    }
 
    /**
     * paste at the selected node what is in the buffer
     * 
     * @return true if paste succeed
     */
    public boolean pasteBuffer() {
        TreePath[] selections = getTreeSelectionModel().getSelectionPaths();


        if (!isBufferEmpty()) {
            FacilitiesFactory ff = new FacilitiesFactory();


            if (onlyMapLayers(buffer)) {

                if (hasSelection(selections)) {

                    if (selections.length == 1) {


                        if (((ContextTreeNode) selections[0].getLastPathComponent()).getUserObject() instanceof MapLayer) {
                            MapLayer insertlayer = (MapLayer) ((ContextTreeNode) selections[0].getLastPathComponent()).getUserObject();
                            MapContext parent = (MapContext) ((ContextTreeNode) ((ContextTreeNode) selections[0].getLastPathComponent()).getParent()).getUserObject();

                            for (Object obj : buffer) {
                                MapLayer layer = (MapLayer) obj;

                                if (parent.indexOf(layer) == -1) {
                                    parent.addLayer(layer);
                                    parent.moveLayer(parent.indexOf(layer), parent.indexOf(insertlayer));
                                } else {
                                    MapLayer copy = ff.duplicateLayer(layer);
                                    copy.setTitle(COPY_NAME + layer.getTitle());
                                    parent.addLayer(copy);
                                    parent.moveLayer(parent.indexOf(copy), parent.indexOf(insertlayer));
                                }
                            }


                        } else if (((ContextTreeNode) selections[0].getLastPathComponent()).getUserObject() instanceof MapContext) {
                            MapContext context = (MapContext) ((ContextTreeNode) selections[0].getLastPathComponent()).getUserObject();


                            for (Object obj : buffer) {
                                MapLayer layer = (MapLayer) obj;

                                if (context.indexOf(layer) == -1) {
                                    context.addLayer(layer);
                                } else {
                                    MapLayer copy = ff.duplicateLayer(layer);
                                    copy.setTitle(COPY_NAME + layer.getTitle());
                                    context.addLayer(copy);
                                    context.moveLayer(context.indexOf(copy), context.indexOf(layer));
                                }

                            }

                        }
                        return true;
                    }
                }


            } else if (onlyMapContexts(buffer)) {

                for (Object obj : buffer) {
                    MapContext context = (MapContext) obj;

                    if (getTreeTableModel().getMapContextIndex(context) == -1) {
                        getTreeTableModel().addMapContext(context);
                    } else {
                        getTreeTableModel().addMapContext(ff.duplicateContext(context));
                    }

                }

                buffer.clear();
                return true;
            }


        }

        return false;

    }

    /**
     * get a Array of the objects in the buffer
     * 
     * @return object array, can be MapLayers or MapContexts or empty array
     */
    public Object[] getBuffer() {
        return buffer.toArray();
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
// LISTENERS MANAGEMENT ////////////////////////////////////////////////////////
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




