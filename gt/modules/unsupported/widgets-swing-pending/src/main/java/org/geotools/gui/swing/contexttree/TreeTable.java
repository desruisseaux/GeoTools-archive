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
import java.awt.Component;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPopupMenu;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableCellEditor;
import javax.swing.tree.TreePath;
import org.geotools.gui.swing.contexttree.column.TreeTableColumn;
import org.geotools.gui.swing.contexttree.renderer.DefaultHeaderRenderer;
import org.geotools.gui.swing.contexttree.renderer.TreeNodeProvider;
import org.geotools.gui.swing.i18n.TextBundle;
import org.geotools.map.MapContext;
import org.geotools.map.MapLayer;
import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jdesktop.swingx.renderer.DefaultTreeRenderer;


/**
 *
 * @author johann sorel
 */
public class TreeTable extends JXTreeTable {

    private List<Object> buffer = new ArrayList<Object>();

    public TreeTable() {
        super(new ContextTreeModel());

        setComponentPopupMenu(new JContextTreePopup(this));
        setColumnControlVisible(true);
        setTreeCellRenderer(new DefaultTreeRenderer(new TreeNodeProvider(this)));


        getColumnModel().getColumn(0).setHeaderRenderer(new DefaultHeaderRenderer(null, null, TextBundle.getResource().getString("col_tree")));

        setHighlighters(new Highlighter[]{HighlighterFactory.createAlternateStriping(Color.white, HighlighterFactory.QUICKSILVER, 1)});



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


        //drag and drop 
        initDragAndDrop();
    }

    private void initDragAndDrop() {

        setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        MultiContextTreeTransferHandler handler = new MultiContextTreeTransferHandler();
        setTransferHandler(handler);
        setDropTarget(new MultiContextTreeDrop(handler));
        setDragEnabled(true);

    }

    ////////////////////////////////////////////////////////////////////////////
    // CUT/COPY/PASTE/DUPLICATE  ///////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////

    
    public boolean hasSelection() {
        TreePath[] selections = getTreeSelectionModel().getSelectionPaths();
        
        if (selections != null) {
            if (selections.length > 0) {
                return true;
            }
        }
        return false;
    }
    
    private boolean hasSelection(TreePath[] selections) {

        if (selections != null) {
            if (selections.length > 0) {
                return true;
            }
        }
        return false;
    }

    
    private boolean onlyMapContexts(List<Object> lst){
        
        for(Object obj : lst){
            
            if( !(obj instanceof MapContext)){
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
    
    private boolean onlyMapLayers(List<Object> lst){
        
        for(Object obj : lst){
            
            if( !(obj instanceof MapLayer)){
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

    public void duplicateSelection() {
        TreePath[] selections = getTreeSelectionModel().getSelectionPaths();

        if (hasSelection(selections)) {
            
            if(onlyMapLayers(selections)){
                //a faire
            }else if(onlyMapContexts(selections)){
                
            }
        }
    }

    public boolean canDuplicateSelection() {
        TreePath[] selections = getTreeSelectionModel().getSelectionPaths();

        if (hasSelection(selections)) {
            return onlyMapContexts(selections) || onlyMapLayers(selections);
        } else {
            return false;
        }
    }

    public void copySelectionInBuffer() {
        TreePath[] selections = getTreeSelectionModel().getSelectionPaths();

        if (hasSelection(selections)) {
            buffer.clear();

            if (selections != null) {
                for (TreePath tp : selections) {
                    ContextTreeNode node = (ContextTreeNode) tp.getLastPathComponent();
                    buffer.add(node.getUserObject());
                }
            }
        }

    }

    public void cutSelectionInBuffer() {
        TreePath[] selections = getTreeSelectionModel().getSelectionPaths();

        if (hasSelection(selections)) {
            buffer.clear();

            if (selections != null) {
                for (TreePath tp : selections) {
                    ContextTreeNode node = (ContextTreeNode) tp.getLastPathComponent();
                    buffer.add(node.getUserObject());
                }
            }
        }
    }

    public boolean isBufferEmpty() {
        if (buffer.size() == 0) {
            return true;
        } else {
            return false;
        }
    }

    public boolean canPasteBuffer() {
        if(isBufferEmpty()){
            return false;
        }else{
            
            if(onlyMapContexts(buffer)){
                return true;
            }else if(onlyMapLayers(buffer)){
                TreePath[] selections = getTreeSelectionModel().getSelectionPaths();
                    
                if(hasSelection(selections)){
                    if(selections.length == 1){
                        return true;
                    }
                }
            }
            
        }
        return false;
    }

    public void pasteBuffer() {
        TreePath[] selections = getTreeSelectionModel().getSelectionPaths();

        if (hasSelection(selections)) {

            if (selections != null) {
                if (selections.length > 0) {

                    ContextTreeNode node = (ContextTreeNode) selections[0].getLastPathComponent();

                    if (node.getUserObject() instanceof MapLayer) {

                    } else if (node.getUserObject() instanceof MapContext) {
                        MapContext context = (MapContext) node.getUserObject();

                        for (Object obj : buffer) {
                            if (obj instanceof MapLayer) {
                                context.addLayer((MapLayer) obj);
                            }
                        }

                    }

                }
            }
        }
        buffer.clear();
    }

    public Object[] getBuffer() {
        return buffer.toArray();
    }

    @Override
    public ContextTreeModel getTreeTableModel() {
        return (ContextTreeModel) super.getTreeTableModel();
    }
}




