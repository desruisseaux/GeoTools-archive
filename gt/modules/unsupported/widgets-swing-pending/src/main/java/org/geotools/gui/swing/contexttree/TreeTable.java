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
import org.geotools.gui.swing.contexttree.column.TreeTableColumn;
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


/**
 *
 * @author johann sorel
 */
public class TreeTable extends JXTreeTable {

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
                
                
                
    private final List<Object> buffer = new ArrayList<Object>();
    private final String COPY_NAME = "-" + TextBundle.getResource().getString("copy") + "- ";

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
        initKeySupport();
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

    ////////////////////////////////////////////////////////////////////////////
    // CUT/COPY/PASTE/DUPLICATE/DELETE  ////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////

    private String getCopyName(String name) {
        return COPY_NAME + name;
    }

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

    public void duplicateSelection() {
        TreePath[] selections = getTreeSelectionModel().getSelectionPaths();

        if (canDuplicateSelection()) {
            FacilitiesFactory ff = new FacilitiesFactory();

            if (onlyMapLayers(selections)) {

                for (TreePath tp : selections) {
                    MapLayer layer = (MapLayer) ((ContextTreeNode) tp.getLastPathComponent()).getUserObject();
                    MapContext parent = (MapContext) ((ContextTreeNode) ((ContextTreeNode) tp.getLastPathComponent()).getParent()).getUserObject();
                    MapLayer copylayer = ff.duplicateLayer(layer);
                    copylayer.setTitle(getCopyName(layer.getTitle()));

                    parent.addLayer(copylayer);
                    parent.moveLayer(parent.indexOf(copylayer), parent.indexOf(layer));
                }

            } else if (onlyMapContexts(selections)) {

                for (TreePath tp : selections) {
                    MapContext context = (MapContext) ((ContextTreeNode) tp.getLastPathComponent()).getUserObject();
                    MapContext copycontext = ff.duplicateContext(context);
                    copycontext.setTitle(getCopyName(context.getTitle()));

                    getTreeTableModel().addMapContext(copycontext);
                }
            }
        }
    }

    public boolean canDuplicateSelection() {
        TreePath[] selections = getTreeSelectionModel().getSelectionPaths();

        if (hasSelection(selections)) {
            return (onlyMapContexts(selections) || onlyMapLayers(selections));
        } else {
            return false;
        }
    }

    public boolean canDeleteSelection() {
        return hasSelection();
    }

    public boolean canCopySelection() {
        return canDuplicateSelection();
    }

    public boolean canCutSelection() {
        return canDuplicateSelection();
    }

    public void deleteSelection() {

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
        }

    }

    public void copySelectionInBuffer() {
        TreePath[] selections = getTreeSelectionModel().getSelectionPaths();

        if (hasSelection(selections)) {
            buffer.clear();

            if (onlyMapLayers(selections)) {

                for (TreePath tp : selections) {
                    MapLayer layer = (MapLayer) ((ContextTreeNode) tp.getLastPathComponent()).getUserObject();
                    buffer.add(layer);
                }

            } else if (onlyMapContexts(selections)) {

                for (TreePath tp : selections) {
                    MapContext context = (MapContext) ((ContextTreeNode) tp.getLastPathComponent()).getUserObject();
                    buffer.add(context);
                }
            }


        }

    }

    public void cutSelectionInBuffer() {
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

            } else if (onlyMapContexts(selections)) {

                for (TreePath tp : selections) {
                    MapContext context = (MapContext) ((ContextTreeNode) tp.getLastPathComponent()).getUserObject();

                    buffer.add(context);
                    getTreeTableModel().removeMapContext(context);
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

    public void pasteBuffer() {
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
                                    copy.setTitle(getCopyName(layer.getTitle()));
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
                                    copy.setTitle(getCopyName(layer.getTitle()));
                                    context.addLayer(copy);
                                    context.moveLayer(context.indexOf(copy), context.indexOf(layer));
                                }

                            }

                        }
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
            }


        }



    }

    public Object[] getBuffer() {
        return buffer.toArray();
    }

    @Override
    public ContextTreeModel getTreeTableModel() {
        return (ContextTreeModel) super.getTreeTableModel();
    }
}




