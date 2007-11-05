/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geotools.gui.swing.contexttree;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import javax.swing.JLabel;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableCellEditor;
import org.geotools.gui.swing.contexttree.column.ContextTreeColumn;
import org.geotools.gui.swing.contexttree.renderer.ColumnHeader;
import org.geotools.gui.swing.contexttree.renderer.HeaderRenderer;
import org.geotools.gui.swing.contexttree.renderer.TreeNodeProvider;
import org.geotools.gui.swing.i18n.TextBundle;
import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jdesktop.swingx.renderer.DefaultTreeRenderer;

/**
 *
 * @author Administrateur
 */
public class TreeTable extends JXTreeTable {

    public TreeTable() {
        super(new ContextTreeModel());

        setComponentPopupMenu(new JContextTreePopup(this));
        setColumnControlVisible(true);
        setTreeCellRenderer(new DefaultTreeRenderer(new TreeNodeProvider(this)));

        ColumnHeader head0 = new ColumnHeader(TextBundle.getResource().getString("col_tree"), new JLabel());

        getColumnModel().getColumn(0).setHeaderValue(head0);
        getColumnModel().getColumn(0).setHeaderRenderer(new HeaderRenderer());

        setHighlighters(new Highlighter[]{HighlighterFactory.createAlternateStriping(Color.white, HighlighterFactory.QUICKSILVER, 1)});
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        ContextTreeTransferHandler handler = new ContextTreeTransferHandler();
        setTransferHandler(handler);
        setDropTarget(new ContextTreeDrop(handler));
        setDragEnabled(true);

        putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

        addMouseMotionListener(new MouseMotionListener() {

                    public void mouseDragged(MouseEvent e) {
                    }

                    public void mouseMoved(MouseEvent e) {
                        Point p = e.getPoint();
                        if (p != null) {
                            int row = rowAtPoint(p);
                            int col = columnAtPoint(p);

                            
                            if (row != editingRow || col != editingColumn) {
                                if (isEditing()) {
                                    TableCellEditor editor = getCellEditor();

                                    if (!editor.stopCellEditing()) {
                                        editor.cancelCellEditing();
                                    }
                                }

                                if (!isEditing() && col >= 0 && row >= 0) {

                                    //we handle differently ContextTreeColumn
                                    if (getColumnExt(col) instanceof ContextTreeColumn) {
                                        ContextTreeColumn column = (ContextTreeColumn) getColumnExt(col);
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

    @Override
    public ContextTreeModel getTreeTableModel() {
        return (ContextTreeModel) super.getTreeTableModel();
    }
    
    
//    @Override
//     public int getEditingColumn() {
//         if (lockedTable.hasFocus()) {
//             return lockedTable.getEditingColumn();
//         }
// 
//         return scrollTable.getEditingColumn() + frozenColumns;
//     }
//
//     
//    @Override
//     public int getEditingRow() {
//        this.
//        if (lockedTable.hasFocus()) {
//             return lockedTable.getEditingRow();
//         }
//         return scrollTable.getEditingRow();
//     }
    
    
    
}
