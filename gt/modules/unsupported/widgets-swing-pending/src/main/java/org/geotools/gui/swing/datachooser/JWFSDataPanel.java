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

import java.awt.Component;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;
import javax.swing.event.EventListenerList;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.data.wfs.WFSDataStoreFactory;
import org.geotools.gui.swing.datachooser.model.DBModel;
import org.geotools.gui.swing.datachooser.model.KeyModel;
import org.geotools.gui.swing.icon.IconBundle;
import org.geotools.gui.swing.misc.Render.RandomStyleFactory;
import org.geotools.map.DefaultMapLayer;
import org.geotools.map.MapLayer;
import org.geotools.styling.Style;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

/**
 *
 * @author johann sorel
 */
public class JWFSDataPanel extends javax.swing.JPanel implements DataPanel {

    private static ResourceBundle BUNDLE = ResourceBundle.getBundle("org/geotools/gui/swing/datachooser/Bundle");

    private DataStore store;
    private EventListenerList listeners = new EventListenerList();
    
    
    /** Creates new form DefaultShapeTypeChooser */
    public JWFSDataPanel() {
        initComponents();
        
        tab_table.setTableHeader(null);
        tab_table.setModel(new DBModel(tab_table));
        
        WFSDataStoreFactory factory = new WFSDataStoreFactory();

        KeyModel model = new KeyModel(tab_key);
        model.setParam(factory.getParametersInfo());
        tab_key.setModel(model);
        tab_key.revalidate();
        tab_key.repaint();
        
        
        
        
        

//        Param[] params = factory.getParametersInfo();
//        for (Param pm : params) {
//            //une description de la clé
//            String desc = pm.description;
//            System.out.println(desc);
//            //la clé
//            String key = pm.key;
//            System.out.println(key);
//            //Vrai si cette clé est obligatoire
//            boolean needed = pm.required;
//            //un exemple de valeur possible
//            Object value = pm.sample;
//            //la classe nécessaire
//            Class classe = pm.type;
//        }

    }

    public Map getProperties() {
        return ((KeyModel) tab_key.getModel()).getProperties();
    }

//    public void parseProperties(Map map) {
//
//        if (map.containsKey("dbtype")) {
//            Object type = map.get("dbtype");
//
//            if (type.equals("postgis")) {
//                jcb_dbtype.setSelectedIndex(0);
//                
//                PostgisDataStoreFactory pdsf = new PostgisDataStoreFactory();
//                
//                KeyModel model = new KeyModel(tab_key);
//                model.setParam(pdsf.getParametersInfo());
//                model.parse(map);
//                tab_key.setModel(model);
//                tab_key.revalidate();
//                tab_key.repaint();
//            } else if (type.equals("oracle")) {
//                jcb_dbtype.setSelectedIndex(1);
//                OracleDataStoreFactory pdsf = new OracleDataStoreFactory();
//
//                KeyModel model = new KeyModel(tab_key);
//                model.setParam(pdsf.getParametersInfo());
//                model.parse(map);
//                tab_key.setModel(model);
//                tab_key.revalidate();
//                tab_key.repaint();
//            }
//
//        }
//
//    }

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

        jScrollPane1 = new javax.swing.JScrollPane();
        tab_key = new javax.swing.JTable();
        jScrollPane2 = new javax.swing.JScrollPane();
        tab_table = new org.jdesktop.swingx.JXTable();
        but_refresh = new javax.swing.JButton();
        but_add = new javax.swing.JButton();

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
        jScrollPane1.setViewportView(tab_key);

        tab_table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jScrollPane2.setViewportView(tab_table);

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/geotools/gui/swing/datachooser/Bundle"); // NOI18N
        but_refresh.setText(bundle.getString("connect")); // NOI18N
        but_refresh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                but_refreshactionRefresh(evt);
            }
        });

        but_add.setIcon(IconBundle.getResource().getIcon("16_data_add"));
        but_add.setText(bundle.getString("add")); // NOI18N
        but_add.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        but_add.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                but_addactionAdd(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 267, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(but_refresh))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 231, Short.MAX_VALUE)
                    .add(but_add))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 245, Short.MAX_VALUE)
                    .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 245, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(but_add)
                    .add(but_refresh))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void but_refreshactionRefresh(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_but_refreshactionRefresh
        try {
            store = DataStoreFinder.getDataStore(getProperties());
            refreshTable();
        } catch (IOException ex) {
            store = null;
            System.out.println(ex);
        }
    }//GEN-LAST:event_but_refreshactionRefresh

    private void but_addactionAdd(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_but_addactionAdd
        ArrayList<MapLayer> layers = new ArrayList<MapLayer>();
        RandomStyleFactory rsf = new RandomStyleFactory();
        
        if (store != null) {
            
            for (int i = 0; i < tab_table.getSelectedRows().length; i++) {
                try {
                    DBModel model = (DBModel) tab_table.getModel();
                    String name = (String) model.getValueAt(tab_table.getSelectedRows()[i], 0);
                    FeatureSource<SimpleFeatureType, SimpleFeature> fs = store.getFeatureSource(name);
                    Style style = rsf.createRandomVectorStyle(fs);
                    
                    MapLayer layer = new DefaultMapLayer(fs, style);
                    layer.setTitle( "WFS -" + name);
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
    }//GEN-LAST:event_but_addactionAdd
    public ImageIcon getIcon16() {
        return IconBundle.getResource().getIcon("16_web");
    }

    public ImageIcon getIcon48() {
        return IconBundle.getResource().getIcon("48_web");
    }

    public String getTitle() {
        return BUNDLE.getString("server");
    }

    public Component getChooserComponent() {
        return this;
    }

    public List<MapLayer> read() {
        ArrayList<MapLayer> lst = new ArrayList<MapLayer>();
        return lst;
    }

    public void addListener(DataListener listener) {

    }

    public void removeListener(DataListener listener) {

    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton but_add;
    private javax.swing.JButton but_refresh;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTable tab_key;
    private org.jdesktop.swingx.JXTable tab_table;
    // End of variables declaration//GEN-END:variables
}
