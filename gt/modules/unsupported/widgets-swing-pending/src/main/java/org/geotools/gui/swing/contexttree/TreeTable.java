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

import java.awt.event.KeyEvent;
import org.geotools.gui.swing.contexttree.draganddrop.MultiContextTreeDrop;
import org.geotools.gui.swing.contexttree.draganddrop.MultiContextTreeTransferHandler;
import java.awt.Color;
import java.awt.Point;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragSource;
import java.awt.dnd.MouseDragGestureRecognizer;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableCellEditor;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import org.geotools.gui.swing.contexttree.column.TreeTableColumn;
import org.geotools.gui.swing.contexttree.renderer.DefaultHeaderRenderer;
import org.geotools.gui.swing.contexttree.renderer.TreeNodeProvider;
import org.geotools.gui.swing.i18n.TextBundle;
import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jdesktop.swingx.renderer.DefaultTreeRenderer;


/**
 *
 * @author johann sorel
 */
public class TreeTable extends JXTreeTable {

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
                        
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        setDragEnabled(true);
        
//        DragSource dragSource = new DragSource();
//        dragSource.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_NONE, new GestureListener(this));
                        
        MultiContextTreeTransferHandler handler = new MultiContextTreeTransferHandler();
        setTransferHandler(handler);
        setDropTarget(new MultiContextTreeDrop(handler));
        

//        CombineListener listener = new CombineListener(this);
//        addKeyListener(listener);
//        addMouseListener(listener);
       
    }

    @Override
    public ContextTreeModel getTreeTableModel() {
        return (ContextTreeModel) super.getTreeTableModel();
    }

}
//class CombineListener implements MouseListener, KeyListener {
//
//    private Integer activekey = null;
//    private ContextTreeTable tree = null;
//
//    public CombineListener(ContextTreeTable tree) {
//        this.tree = tree;
//    }
//
//    public void mouseClicked(MouseEvent e) {
//
//        if (activekey != null) {
//            if (activekey.intValue() == KeyEvent.VK_CONTROL) {
//                Point point = e.getPoint();
//                int row = tree.rowAtPoint(point);
//                
//                System.out.println("la");
//                
//                if (row >= 0) {
//                    TreeSelectionModel selectmodel = tree.getTreeSelectionModel();
//                    TreePath[] selected = selectmodel.getSelectionPaths();
//                    TreePath[] selection = new TreePath[selected.length + 1];
//                    
//                    for(int i=0;i<selected.length;i++){
//                        selection[i] = selected[i];
//                    }
//                    int last = selection.length-1 ;
//                    selection[last] = tree.getPathForRow(row);
//                    
//                    selectmodel.setSelectionPaths(selection);
//                    System.out.println("la1");
//                }
//
//            }else{
//                
//            }
//        }
//    }
//
//    public void mousePressed(MouseEvent e) {
//    }
//
//    public void mouseReleased(MouseEvent e) {
//    }
//
//    public void mouseEntered(MouseEvent e) {
//    }
//
//    public void mouseExited(MouseEvent e) {
//    }
//
//    public void keyPressed(KeyEvent e) {
//        activekey = e.getKeyCode();
//    }
//
//    public void keyReleased(KeyEvent e) {
//        activekey = null;
//    }
//
//    public void keyTyped(KeyEvent e) {
//    }
//}
//
//
//class GestureListener extends MouseDragGestureRecognizer{
//    
//    public GestureListener{
//        super();
//        
//        
//    }
//    
//}
