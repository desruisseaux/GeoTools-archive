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
package org.geotools.gui.swing.datachooser;

import com.sun.org.apache.bcel.internal.generic.LSTORE;
import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.event.EventListenerList;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.data.oracle.OracleDataStoreFactory;
import org.geotools.data.postgis.PostgisDataStoreFactory;
import org.geotools.gui.swing.datachooser.model.DBModel;
import org.geotools.gui.swing.datachooser.model.KeyModel;
import org.geotools.gui.swing.i18n.TextBundle;
import org.geotools.gui.swing.icon.IconBundle;
import org.geotools.gui.swing.misc.Render.RandomStyleFactory;
import org.geotools.map.DefaultMapLayer;
import org.geotools.map.MapLayer;
import org.geotools.styling.Style;

/**
 *
 * @author johann sorel
 */
public class JDatabaseDataPanel extends javax.swing.JPanel implements DataPanel {

    private DataStore store;
    private EventListenerList listeners = new EventListenerList();

    /** Creates new form DefaultShapeTypeChooser */
    public JDatabaseDataPanel() {
        initComponents();

        tab_table.setTableHeader(null);
        tab_table.setModel(new DBModel(tab_table));

        PostgisDataStoreFactory pdsf = new PostgisDataStoreFactory();

        KeyModel model = new KeyModel(tab_key);
        model.setParam(pdsf.getParametersInfo());
        tab_key.setModel(model);
        tab_key.revalidate();
        tab_key.repaint();

        jcb_dbtype.addItemListener(new ItemListener() {

                    public void itemStateChanged(ItemEvent e) {

                        if (e.getItem() != null) {

                            if (e.getItem().equals("postgis")) {
                                PostgisDataStoreFactory pdsf = new PostgisDataStoreFactory();

                                KeyModel model = new KeyModel(tab_key);
                                model.setParam(pdsf.getParametersInfo());
                                tab_key.setModel(model);
                                tab_key.revalidate();
                                tab_key.repaint();
                            } else {
                                OracleDataStoreFactory pdsf = new OracleDataStoreFactory();

                                KeyModel model = new KeyModel(tab_key);
                                model.setParam(pdsf.getParametersInfo());
                                tab_key.setModel(model);
                                tab_key.revalidate();
                                tab_key.repaint();
                            }
                        }
                    }
                });


    }

    public Map getProperties() {
        return ((KeyModel) tab_key.getModel()).getProperties();
    }

    public void parseProperties(Map map) {

        if (map.containsKey("dbtype")) {
            Object type = map.get("dbtype");

            if (type.equals("postgis")) {
                jcb_dbtype.setSelectedIndex(0);
                
                PostgisDataStoreFactory pdsf = new PostgisDataStoreFactory();

                KeyModel model = new KeyModel(tab_key);
                model.setParam(pdsf.getParametersInfo());
                model.parse(map);
                tab_key.setModel(model);
                tab_key.revalidate();
                tab_key.repaint();
            } else if (type.equals("oracle")) {
                jcb_dbtype.setSelectedIndex(1);
                OracleDataStoreFactory pdsf = new OracleDataStoreFactory();

                KeyModel model = new KeyModel(tab_key);
                model.setParam(pdsf.getParametersInfo());
                model.parse(map);
                tab_key.setModel(model);
                tab_key.revalidate();
                tab_key.repaint();
            }

        }


    }

    private void refreshTable() {

        if (store != null) {
            ((DBModel) tab_table.getModel()).clean();
            try {
                ((DBModel) tab_table.getModel()).add(store.getTypeNames());
            } catch (IOException ex) {
                System.out.println(ex);
            }
        }

    }

    private void fireEvent(MapLayer[] layers) {
        for (DataListener lst : listeners.getListeners(DataListener.class)) {
            lst.addLayers(layers);
        }

    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lbl_dbtype = new javax.swing.JLabel();
        but_refresh = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tab_table = new org.jdesktop.swingx.JXTable();
        jcb_dbtype = new javax.swing.JComboBox();
        but_add = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        tab_key = new javax.swing.JTable();

        lbl_dbtype.setText(TextBundle.getResource().getString("dbtype"));

        but_refresh.setText(TextBundle.getResource().getString("refresh"));
        but_refresh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                actionRefresh(evt);
            }
        });

        tab_table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jScrollPane1.setViewportView(tab_table);

        jcb_dbtype.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "postgis", "oracle" }));

        but_add.setIcon(IconBundle.getResource().getIcon("16_data_add"));
        but_add.setText(TextBundle.getResource().getString("add"));
        but_add.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        but_add.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                actionAdd(evt);
            }
        });

        tab_key.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane2.setViewportView(tab_key);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, but_refresh)
                    .add(jScrollPane2, 0, 182, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(lbl_dbtype)
                        .add(18, 18, 18)
                        .add(jcb_dbtype, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 130, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 185, Short.MAX_VALUE))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(92, 92, 92)
                        .add(but_add)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(lbl_dbtype)
                            .add(jcb_dbtype, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 187, Short.MAX_VALUE))
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 215, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(but_add)
                    .add(but_refresh))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void actionRefresh(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_actionRefresh

        try {
            store = DataStoreFinder.getDataStore(getProperties());
            refreshTable();
        } catch (IOException ex) {
            store = null;
            System.out.println(ex);
        }       
        
    }//GEN-LAST:event_actionRefresh

    private void actionAdd(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_actionAdd
        ArrayList<MapLayer> layers = new ArrayList<MapLayer>();
        RandomStyleFactory rsf = new RandomStyleFactory();

        if (store != null) {

            for (int i = 0; i < tab_table.getSelectedRows().length; i++) {
                try {
                    DBModel model = (DBModel) tab_table.getModel();
                    String name = (String) model.getValueAt(tab_table.getSelectedRows()[i], 0);
                    FeatureSource fs = store.getFeatureSource(name);
                    Style style = rsf.createRandomVectorStyle(fs);

                    MapLayer layer = new DefaultMapLayer(fs, style);
                    layer.setTitle(jcb_dbtype.getSelectedItem().toString() + "-" + name);
                    layers.add(layer);
                } catch (IOException ex) {
                    System.out.println(ex);
                }
            }

            if (layers.size() > 0) {
                MapLayer[] lys = new MapLayer[layers.size()];
                for (int i = 0; i < layers.size(); i++) {
                    lys[i] = layers.get(i);
                }
                fireEvent(lys);
            }
        }
    }//GEN-LAST:event_actionAdd

    public ImageIcon getIcon16() {
        return IconBundle.getResource().getIcon("16_database");
    }

    public ImageIcon getIcon48() {
        return IconBundle.getResource().getIcon("48_database");
    }

    public String getTitle() {
        return TextBundle.getResource().getString("database");
    }

    public Component getChooserComponent() {
        return this;
    }

    public void addListener(DataListener listener) {
        listeners.add(DataListener.class, listener);
    }

    public void removeListener(DataListener listener) {
        listeners.remove(DataListener.class, listener);
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton but_add;
    private javax.swing.JButton but_refresh;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JComboBox jcb_dbtype;
    private javax.swing.JLabel lbl_dbtype;
    private javax.swing.JTable tab_key;
    private org.jdesktop.swingx.JXTable tab_table;
    // End of variables declaration//GEN-END:variables
}