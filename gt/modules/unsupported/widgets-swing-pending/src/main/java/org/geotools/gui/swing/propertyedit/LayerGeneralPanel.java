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
package org.geotools.gui.swing.propertyedit;

import java.awt.Component;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.Set;

import javax.swing.ImageIcon;

import org.geotools.data.DataStore;
import org.geotools.data.ServiceInfo;
import org.geotools.data.shapefile.indexed.IndexedShapefileDataStore;
import org.geotools.map.MapLayer;

/**
 *
 * @author johann sorel
 */
public class LayerGeneralPanel extends javax.swing.JPanel implements PropertyPanel {

    private MapLayer layer = null;
    private final String title;

    /** Creates new form LayerGeneralPanel */
    public LayerGeneralPanel() {

        initComponents();

        ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/geotools/gui/swing/propertyedit/Bundle");
        title = bundle.getString("general_title");
        
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        gui_jtf_name = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        jPanel1 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jtf_info_title = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jtf_info_source = new javax.swing.JTextArea();
        jLabel4 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jtf_info_schema = new javax.swing.JTextArea();
        jLabel5 = new javax.swing.JLabel();
        jScrollPane4 = new javax.swing.JScrollPane();
        jtf_info_description = new javax.swing.JTextArea();
        jLabel6 = new javax.swing.JLabel();
        jScrollPane5 = new javax.swing.JScrollPane();
        jtf_info_keyword = new javax.swing.JTextArea();
        jLabel7 = new javax.swing.JLabel();
        jScrollPane6 = new javax.swing.JScrollPane();
        jtf_info_publisher = new javax.swing.JTextArea();
        jXTitledSeparator1 = new org.jdesktop.swingx.JXTitledSeparator();

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/geotools/gui/swing/propertyedit/Bundle"); // NOI18N
        jLabel1.setText(bundle.getString("title")); // NOI18N

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        jLabel2.setText(bundle.getString("info_title")); // NOI18N

        jtf_info_title.setEditable(false);
        jtf_info_title.setOpaque(false);

        jLabel3.setText(bundle.getString("info_source")); // NOI18N

        jScrollPane2.setOpaque(false);

        jtf_info_source.setColumns(20);
        jtf_info_source.setEditable(false);
        jtf_info_source.setRows(3);
        jScrollPane2.setViewportView(jtf_info_source);

        jLabel4.setText(bundle.getString("info_schema")); // NOI18N

        jtf_info_schema.setColumns(20);
        jtf_info_schema.setEditable(false);
        jtf_info_schema.setRows(3);
        jScrollPane3.setViewportView(jtf_info_schema);

        jLabel5.setText(bundle.getString("info_description")); // NOI18N

        jtf_info_description.setColumns(20);
        jtf_info_description.setEditable(false);
        jtf_info_description.setRows(3);
        jScrollPane4.setViewportView(jtf_info_description);

        jLabel6.setText(bundle.getString("info_keyword")); // NOI18N

        jtf_info_keyword.setColumns(20);
        jtf_info_keyword.setEditable(false);
        jtf_info_keyword.setRows(3);
        jScrollPane5.setViewportView(jtf_info_keyword);

        jLabel7.setText(bundle.getString("info_publisher")); // NOI18N

        jtf_info_publisher.setColumns(20);
        jtf_info_publisher.setEditable(false);
        jtf_info_publisher.setRows(3);
        jScrollPane6.setViewportView(jtf_info_publisher);

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jScrollPane5, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 380, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 380, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jtf_info_title, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 380, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel2)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel3)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel4)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jScrollPane3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 380, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel5)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jScrollPane4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 380, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel7)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jScrollPane6, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 380, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel6))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel2)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jtf_info_title, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel3)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel4)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel5)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel7)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane6, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel6)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane5, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jScrollPane1.setViewportView(jPanel1);

        jXTitledSeparator1.setTitle(bundle.getString("informations")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 422, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(jLabel1)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(gui_jtf_name, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 381, Short.MAX_VALUE))
                    .add(jXTitledSeparator1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 422, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(gui_jtf_name, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(18, 18, 18)
                .add(jXTitledSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 291, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    private void parse() {
        if (layer != null) {
            gui_jtf_name.setText(layer.getTitle());

            DataStore store = (DataStore) layer.getFeatureSource().getDataStore();

            if (store instanceof IndexedShapefileDataStore) {
                ServiceInfo info = ((IndexedShapefileDataStore) store).getInfo();

                try {
                    jtf_info_title.setText(info.getTitle());
                } catch (Exception e) {
                    jtf_info_title.setText("Error : " + e.getMessage());
                }
                try {
                    jtf_info_source.setText(info.getSource().toString());
                } catch (Exception e) {
                    jtf_info_source.setText("Error : " + e.getMessage());
                }
                try {
                    jtf_info_schema.setText(info.getSchema().toString());
                } catch (Exception e) {
                    jtf_info_schema.setText("Error : " + e.getMessage());
                }
                try {
                    jtf_info_description.setText(info.getDescription());
                } catch (Exception e) {
                    jtf_info_description.setText("Error : " + e.getMessage());
                }
                try {
                    Set<String> set = info.getKeywords();
                    Iterator<String> ite = set.iterator();
                    while (ite.hasNext()) {
                        jtf_info_keyword.append(ite.next() + " ; ");
                    }
                } catch (Exception e) {
                    jtf_info_keyword.setText("Error : " + e.getMessage());
                }
                try {
                    jtf_info_publisher.setText(info.getPublisher().toString());
                } catch (Exception e) {
                    jtf_info_publisher.setText("Error : " + e.getMessage());
                }



            }

        } else {
            gui_jtf_name.setText("");
            jtf_info_description.setText("");
            jtf_info_keyword.setText("");
            jtf_info_publisher.setText("");
            jtf_info_schema.setText("");
            jtf_info_source.setText("");
            jtf_info_title.setText("");
        }
    }

    public void setTarget(Object target) {
        if (target instanceof MapLayer) {
            layer = (MapLayer) target;
        } else {
            layer = null;
        }
        parse();
    }

    public void apply() {
        if (layer != null) {
            layer.setTitle(gui_jtf_name.getText());
        }
    }

    public void reset() {
        parse();
    }

    public String getTitle() {
        return title;
    }

    public ImageIcon getIcon() {
        return null;
    }

    public String getToolTip() {
        return title;
    }

    public Component getPanel() {
        return this;
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField gui_jtf_name;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private org.jdesktop.swingx.JXTitledSeparator jXTitledSeparator1;
    private javax.swing.JTextArea jtf_info_description;
    private javax.swing.JTextArea jtf_info_keyword;
    private javax.swing.JTextArea jtf_info_publisher;
    private javax.swing.JTextArea jtf_info_schema;
    private javax.swing.JTextArea jtf_info_source;
    private javax.swing.JTextField jtf_info_title;
    // End of variables declaration//GEN-END:variables
}
