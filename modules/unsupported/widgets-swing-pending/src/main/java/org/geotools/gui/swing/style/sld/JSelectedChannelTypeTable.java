/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2002-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.gui.swing.style.sld;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractCellEditor;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import org.geotools.gui.swing.icon.IconBundle;
import org.geotools.gui.swing.style.StyleElementEditor;
import org.geotools.map.MapLayer;
import org.geotools.styling.SelectedChannelType;
import org.geotools.styling.SelectedChannelTypeImpl;


/**
 * SelectedChannnel type panel
 * 
 * @author Johann Sorel
 */
public class JSelectedChannelTypeTable extends javax.swing.JPanel implements StyleElementEditor<SelectedChannelType[]> {

    private MapLayer layer = null;
    private static final Icon ICO_UP = IconBundle.getResource().getIcon("16_uparrow");
    private static final Icon ICO_DOWN = IconBundle.getResource().getIcon("16_downarrow");
    private static final Icon ICO_NEW = IconBundle.getResource().getIcon("16_add_data");
    private static final Icon ICO_DELETE = IconBundle.getResource().getIcon("16_delete");
    private final SelectedModel model = new SelectedModel(new SelectedChannelType[]{});
    private final SelectedEditor editor = new SelectedEditor();

    /** Creates new form JFontsPanel */
    public JSelectedChannelTypeTable() {
        initComponents();
        init();
    }

    private void init() {
        tabFonts.setTableHeader(null);
        tabFonts.setModel(model);
        tabFonts.getColumnModel().getColumn(0).setCellEditor(editor);
        tabFonts.setDefaultRenderer(SelectedChannelType.class, new SelectedRenderer());
        tabFonts.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    public void setLayer(MapLayer layer) {
        editor.setLayer(layer);
        this.layer = layer;
    }

    public MapLayer getLayer() {
        return layer;
    }

    public void setEdited(SelectedChannelType[] fonts) {
        model.setSelecteds(fonts);
    }

    public SelectedChannelType[] getEdited() {
        return model.getSelecteds();
    }

    public void apply() {
    }

    public Component getComponent() {
        return this;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        tabFonts = new javax.swing.JTable();
        guiUp = new javax.swing.JButton();
        guiDown = new javax.swing.JButton();
        guiNew = new javax.swing.JButton();
        guiDelete = new javax.swing.JButton();

        setOpaque(false);

        jScrollPane1.setViewportView(tabFonts);

        guiUp.setIcon(ICO_UP);
        guiUp.setBorderPainted(false);
        guiUp.setContentAreaFilled(false);
        guiUp.setMargin(new java.awt.Insets(2, 2, 2, 2));
        guiUp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                guiUpActionPerformed(evt);
            }
        });

        guiDown.setIcon(ICO_DOWN);
        guiDown.setBorderPainted(false);
        guiDown.setContentAreaFilled(false);
        guiDown.setMargin(new java.awt.Insets(2, 2, 2, 2));
        guiDown.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                guiDownActionPerformed(evt);
            }
        });

        guiNew.setIcon(ICO_NEW);
        guiNew.setBorderPainted(false);
        guiNew.setContentAreaFilled(false);
        guiNew.setMargin(new java.awt.Insets(2, 2, 2, 2));
        guiNew.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                guiNewActionPerformed(evt);
            }
        });

        guiDelete.setIcon(ICO_DELETE);
        guiDelete.setBorderPainted(false);
        guiDelete.setContentAreaFilled(false);
        guiDelete.setMargin(new java.awt.Insets(2, 2, 2, 2));
        guiDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                guiDeleteActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 78, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(guiNew)
                    .add(guiUp)
                    .add(guiDown)
                    .add(guiDelete)))
        );

        layout.linkSize(new java.awt.Component[] {guiDelete, guiDown, guiNew, guiUp}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(guiUp)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(guiDown)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(guiNew)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(guiDelete)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 106, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
    private void guiUpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_guiUpActionPerformed
        int index = tabFonts.getSelectionModel().getMinSelectionIndex();

        if (index >= 0) {
            SelectedChannelType f = (SelectedChannelType) model.getValueAt(index, 0);
            model.moveUp(f);
        }
}//GEN-LAST:event_guiUpActionPerformed

    private void guiDownActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_guiDownActionPerformed
        int index = tabFonts.getSelectionModel().getMinSelectionIndex();

        if (index >= 0) {
            SelectedChannelType f = (SelectedChannelType) model.getValueAt(index, 0);
            model.moveDown(f);
        }
}//GEN-LAST:event_guiDownActionPerformed

    private void guiNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_guiNewActionPerformed
        model.newSelected();
}//GEN-LAST:event_guiNewActionPerformed

    private void guiDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_guiDeleteActionPerformed
        int index = tabFonts.getSelectionModel().getMinSelectionIndex();

        if (index >= 0) {
            model.deleteSelected(index);
        }
    }//GEN-LAST:event_guiDeleteActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton guiDelete;
    private javax.swing.JButton guiDown;
    private javax.swing.JButton guiNew;
    private javax.swing.JButton guiUp;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tabFonts;
    // End of variables declaration//GEN-END:variables
}
class SelectedModel extends AbstractTableModel {

    private List<SelectedChannelType> selecteds = new ArrayList<SelectedChannelType>();

    SelectedModel(SelectedChannelType[] selecteds) {
        for (SelectedChannelType s : selecteds) {
            this.selecteds.add(s);
        }
    }

    public void newSelected() {
        SelectedChannelType s = new SelectedChannelTypeImpl();

        selecteds.add(s);
        int last = selecteds.size() - 1;
        fireTableRowsInserted(last, last);
    }

    public void deleteSelected(int index) {
        selecteds.remove(index);
        fireTableRowsDeleted(index, index);
    }

    public void moveUp(SelectedChannelType s) {
        int index = selecteds.indexOf(s);
        if (index != 0) {
            selecteds.remove(s);
            selecteds.add(index - 1, s);
            fireTableDataChanged();
        }
    }

    public void moveDown(SelectedChannelType s) {
        int index = selecteds.indexOf(s);
        if (index != selecteds.size() - 1) {
            selecteds.remove(s);
            selecteds.add(index + 1, s);
            fireTableDataChanged();
        }
    }

    public void setSelecteds(SelectedChannelType[] selecteds) {
        this.selecteds.clear();

        if (selecteds != null) {
            for (SelectedChannelType s : selecteds) {
                this.selecteds.add(s);
            }
        }
        fireTableDataChanged();
    }

    public SelectedChannelType[] getSelecteds() {
        return selecteds.toArray(new SelectedChannelType[selecteds.size()]);
    }

    public int getRowCount() {
        return selecteds.size();
    }

    public int getColumnCount() {
        return 1;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return true;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return SelectedChannelType.class;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        return selecteds.get(rowIndex);
    }
}

class SelectedRenderer extends DefaultTableCellRenderer {

    private String text = "";

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

        JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, text, isSelected, hasFocus, row, column);

        SelectedChannelType s = (SelectedChannelType) value;
        lbl.setText(s.getChannelName());
        return lbl;
    }
}

class SelectedEditor extends AbstractCellEditor implements TableCellEditor {
    private MapLayer layer = null;
    private JSelectedChannelTypePane editpane = new JSelectedChannelTypePane();
    private JButton but = new JButton("");
    private SelectedChannelType selected = null;

    public SelectedEditor() {
        super();
        but.setBorderPainted(false);

        but.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (selected != null) {
                    JDialog dia = new JDialog();

                    //panneau d'edition           
                    editpane.setEdited(selected);

                    dia.setContentPane(editpane);
                    dia.setLocationRelativeTo(but);
                    dia.pack();
                    dia.setModal(true);
                    dia.setVisible(true);

                    selected = editpane.getEdited();
                }
            }
        });
    }

    public void setLayer(MapLayer layer) {
        this.layer = layer;
    }

    public MapLayer getLayer() {
        return layer;
    }

    public Object getCellEditorValue() {
        return selected;
    }

//    public boolean isCellEditable(EventObject e) {
//        return true;
//    }
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {

        if (value != null && value instanceof SelectedChannelType) {
            selected = (SelectedChannelType) value;            
            but.setText(selected.getChannelName());
        } else {
            selected = null;
        }
        return but;
    }

//    public boolean shouldSelectCell(EventObject anEvent) {
//        return true;
//    }
//
//    public boolean stopCellEditing() {
//        return true;
//    }
//
//    public void cancelCellEditing() {
//    }
//
//    public void addCellEditorListener(CellEditorListener l) {        
//    }
//    
//    public void removeCellEditorListener(CellEditorListener l) {
//    }
}
