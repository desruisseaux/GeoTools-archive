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

package org.geotools.gui.swing.demo;

import org.geotools.gui.swing.contexttree.JContextTree;
import org.geotools.gui.swing.contexttree.TreeEvent;
import org.geotools.gui.swing.contexttree.TreeListener;
import org.geotools.gui.swing.control.JLightMapPaneControl;
import org.geotools.gui.swing.datachooser.DataPanel;
import org.geotools.gui.swing.datachooser.DatabaseDataPanel;
import org.geotools.gui.swing.datachooser.FileDataPanel;
import org.geotools.gui.swing.datachooser.JDataChooser;
import org.geotools.gui.swing.datachooser.ServerDataPanel;
import org.geotools.gui.swing.icon.IconBundle;
import org.geotools.gui.swing.misc.Render.RandomStyleFactory;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.gui.swing.JMapPane;
import org.geotools.gui.swing.propertyedit.ContextCRSPropertyPanel;
import org.geotools.gui.swing.propertyedit.JPropertyDialog;
import org.geotools.gui.swing.propertyedit.LayerFeaturePropertyPanel;
import org.geotools.gui.swing.propertyedit.LayerFilterPropertyPanel;
import org.geotools.gui.swing.propertyedit.LayerStylePropertyPanel;
import org.geotools.gui.swing.propertyedit.PropertyPanel;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.DefaultMapLayer;
import org.geotools.map.MapContext;
import org.geotools.map.MapLayer;
import org.geotools.referencing.CRS;
import org.geotools.renderer.shape.ShapefileRenderer;
import org.geotools.styling.Style;
import org.jdesktop.swingx.JXTitledPanel;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;

/**
 *
 * @author johann sorel
 */
public class DemoSwingGeowidgets extends javax.swing.JFrame {
    
    private MapContext _context;
    private MapLayer _layer;
    private JContextTree tree;
    private JMapPane map;
    private JLightMapPaneControl lightcontrol;
    private JDataChooser.STATE state = JDataChooser.STATE.BUTTONED;
    private int nb = 1;
    
    
    /** Creates new form DemoSwingGeowidgets */
    public DemoSwingGeowidgets() {
        initComponents();
        setLocationRelativeTo(null);

        
        Map hash;
        DataStore store;
        FeatureSource fs;
        Style style;
        MapLayer layer;
        try {
            _context = new DefaultMapContext(CRS.decode("EPSG:4326"));
            
            hash = new HashMap();
            hash.put("url", DemoSwingGeowidgets.class.getResource("/org/geotools/gui/swing/demo/shape/test_polygon.shp"));
            store = DataStoreFinder.getDataStore( hash );
            fs = store.getFeatureSource(store.getTypeNames()[0]);
            style = RandomStyleFactory.createRandomVectorStyle(fs);
            layer = new DefaultMapLayer(fs,style);
            layer.setTitle( "demo_polygon.shp" );
            _context.addLayer(layer);
            _layer = layer;
            
            hash = new HashMap();
            hash.put("url", DemoSwingGeowidgets.class.getResource("/org/geotools/gui/swing/demo/shape/test_ligne.shp"));
            store = DataStoreFinder.getDataStore( hash );
            fs = store.getFeatureSource(store.getTypeNames()[0]);
            style = RandomStyleFactory.createRandomVectorStyle(fs);
            layer = new DefaultMapLayer(fs,style);
            layer.setTitle( "demo_line.shp" );
            _context.addLayer(layer);
            
            hash = new HashMap();
            hash.put("url", DemoSwingGeowidgets.class.getResource("/org/geotools/gui/swing/demo/shape/test_point.shp"));
            store = DataStoreFinder.getDataStore( hash );
            fs = store.getFeatureSource(store.getTypeNames()[0]);
            style = RandomStyleFactory.createRandomVectorStyle(fs);
            layer = new DefaultMapLayer(fs,style);
            layer.setTitle( "demo_point.shp" );
            _context.addLayer(layer);
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        _context.setTitle("DemoContext");
        
        /************************LABEL*****************************************/
        lbl_check.setIcon(IconBundle.getResource().getIcon("CP22_actions_ok"));
        lbl_checking.setIcon(IconBundle.getResource().getIcon("CP22_actions_spellcheck"));
        lbl_working.setIcon(IconBundle.getResource().getIcon("CP22_apps_service_manager"));
        lbl_stop.setIcon(IconBundle.getResource().getIcon("CP22_actions_stop"));
        
        /************************JCONTEXTTREE**********************************/
        titled_jcontexttree.setLeftDecoration( new JLabel(IconBundle.getResource().getIcon("CP22_actions_spellcheck"))  );
        tree = new JContextTree(true);
        tree.addMapContext(_context);
        tree.getTreeTable().expandAll();
        pan_jcontexttree.setLayout(new GridLayout(1,1));
        pan_jcontexttree.add(tree);
        
        /***********************JMAPPANE***************************************/
        map = new JMapPane();
        map.setOpaque(false);
        map.setRenderer( new ShapefileRenderer());
        map.setContext(_context);
        pan_jmappane.setLayout(new GridLayout(1,1));
        pan_jmappane.add(map);
        
        try {
            map.setMapArea(map.getContext().getLayerBounds());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        map.revalidate();
        
        /************************JLIGHTMAPPANECONTROL**************************/
        pan_lightmappanecontrol.setLeftDecoration( new JLabel(IconBundle.getResource().getIcon("CP22_actions_spellcheck"))  );
        lightcontrol = new JLightMapPaneControl();
        lightcontrol.setMapPane(map);
        pan_lightmappanecontrol.add(lightcontrol);
        
        /************************JDATACHOOSER**********************************/
        titled_jdatachooser.setLeftDecoration( new JLabel(IconBundle.getResource().getIcon("CP22_apps_service_manager"))  );
        
        /************************DATAPANEL*************************************/
        titled_datapanel.setLeftDecoration( new JLabel(IconBundle.getResource().getIcon("CP22_actions_spellcheck"))  );
        lbl_filedatapanel.setIcon(IconBundle.getResource().getIcon("CP22_apps_service_manager"));
        lbl_databasedatapanel.setIcon(IconBundle.getResource().getIcon("CP22_actions_stop"));
        lbl_serverdatapanel.setIcon(IconBundle.getResource().getIcon("CP22_actions_stop"));
        
        /************************LISTENER**************************************/
        pan_listener.setLayout(new GridLayout(1,1));
        ContextTreeListener ecouteur = new ContextTreeListener( map );
        pan_listener.add( ecouteur );
        tree.addTreeListener( ecouteur );
        
        /************************JPropertyDialog**********************************/
        titled_jpropertydialog.setLeftDecoration( new JLabel(IconBundle.getResource().getIcon("CP22_actions_spellcheck"))  );
        
        /************************PROPERTYPANEL*********************************/
        titled_propertypanel.setLeftDecoration( new JLabel(IconBundle.getResource().getIcon("CP22_actions_spellcheck"))  );
        lbl_contextcrspropertypanel.setIcon(IconBundle.getResource().getIcon("CP22_actions_stop"));
        lbl_layerfeaturepropertypanel.setIcon(IconBundle.getResource().getIcon("CP22_apps_service_manager"));
        lbl_layerfilterpropertypanel.setIcon(IconBundle.getResource().getIcon("CP22_actions_stop"));
        lbl_layerstylepropertypanel.setIcon(IconBundle.getResource().getIcon("CP22_actions_stop"));
        
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        group_jdatachooser = new javax.swing.ButtonGroup();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jSplitPane1 = new javax.swing.JSplitPane();
        jPanel2 = new javax.swing.JPanel();
        pan_lightmappanecontrol = new org.jdesktop.swingx.JXTitledPanel();
        pan_jmappane = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        titled_jcontexttree = new org.jdesktop.swingx.JXTitledPanel();
        pan_jcontexttree = new javax.swing.JPanel();
        pan_listener = new javax.swing.JPanel();
        jButton2 = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        titled_jdatachooser = new org.jdesktop.swingx.JXTitledPanel();
        jButton1 = new javax.swing.JButton();
        jrb_tabbed = new javax.swing.JRadioButton();
        jrb_buttoned = new javax.swing.JRadioButton();
        chk_file = new javax.swing.JCheckBox();
        chk_database = new javax.swing.JCheckBox();
        chk_server = new javax.swing.JCheckBox();
        titled_datapanel = new org.jdesktop.swingx.JXTitledPanel();
        lbl_filedatapanel = new javax.swing.JLabel();
        lbl_databasedatapanel = new javax.swing.JLabel();
        lbl_serverdatapanel = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        titled_jpropertydialog = new org.jdesktop.swingx.JXTitledPanel();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        titled_propertypanel = new org.jdesktop.swingx.JXTitledPanel();
        jSeparator2 = new javax.swing.JSeparator();
        lbl_contextcrspropertypanel = new javax.swing.JLabel();
        lbl_layerfeaturepropertypanel = new javax.swing.JLabel();
        lbl_layerfilterpropertypanel = new javax.swing.JLabel();
        lbl_layerstylepropertypanel = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        lbl_check = new javax.swing.JLabel();
        lbl_checking = new javax.swing.JLabel();
        lbl_working = new javax.swing.JLabel();
        lbl_stop = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jSplitPane1.setDividerLocation(200);

        pan_lightmappanecontrol.setTitle("JLightMapPaneControl");

        org.jdesktop.layout.GroupLayout pan_lightmappanecontrolLayout = new org.jdesktop.layout.GroupLayout(pan_lightmappanecontrol.getContentContainer());
        pan_lightmappanecontrol.getContentContainer().setLayout(pan_lightmappanecontrolLayout);
        pan_lightmappanecontrolLayout.setHorizontalGroup(
            pan_lightmappanecontrolLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 368, Short.MAX_VALUE)
        );
        pan_lightmappanecontrolLayout.setVerticalGroup(
            pan_lightmappanecontrolLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 0, Short.MAX_VALUE)
        );

        pan_jmappane.setBackground(new java.awt.Color(255, 255, 255));

        org.jdesktop.layout.GroupLayout pan_jmappaneLayout = new org.jdesktop.layout.GroupLayout(pan_jmappane);
        pan_jmappane.setLayout(pan_jmappaneLayout);
        pan_jmappaneLayout.setHorizontalGroup(
            pan_jmappaneLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 352, Short.MAX_VALUE)
        );
        pan_jmappaneLayout.setVerticalGroup(
            pan_jmappaneLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 345, Short.MAX_VALUE)
        );

        org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(pan_lightmappanecontrol, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .add(jPanel2Layout.createSequentialGroup()
                .add(10, 10, 10)
                .add(pan_jmappane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .add(pan_lightmappanecontrol, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(pan_jmappane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        jSplitPane1.setRightComponent(jPanel2);

        titled_jcontexttree.setTitle(" JContextTree");

        org.jdesktop.layout.GroupLayout pan_jcontexttreeLayout = new org.jdesktop.layout.GroupLayout(pan_jcontexttree);
        pan_jcontexttree.setLayout(pan_jcontexttreeLayout);
        pan_jcontexttreeLayout.setHorizontalGroup(
            pan_jcontexttreeLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 195, Short.MAX_VALUE)
        );
        pan_jcontexttreeLayout.setVerticalGroup(
            pan_jcontexttreeLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 256, Short.MAX_VALUE)
        );

        org.jdesktop.layout.GroupLayout titled_jcontexttreeLayout = new org.jdesktop.layout.GroupLayout(titled_jcontexttree.getContentContainer());
        titled_jcontexttree.getContentContainer().setLayout(titled_jcontexttreeLayout);
        titled_jcontexttreeLayout.setHorizontalGroup(
            titled_jcontexttreeLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(pan_jcontexttree, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        titled_jcontexttreeLayout.setVerticalGroup(
            titled_jcontexttreeLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(pan_jcontexttree, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        org.jdesktop.layout.GroupLayout pan_listenerLayout = new org.jdesktop.layout.GroupLayout(pan_listener);
        pan_listener.setLayout(pan_listenerLayout);
        pan_listenerLayout.setHorizontalGroup(
            pan_listenerLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 199, Short.MAX_VALUE)
        );
        pan_listenerLayout.setVerticalGroup(
            pan_listenerLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 76, Short.MAX_VALUE)
        );

        jButton2.setText("Add MapContext");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                actionAddContext(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel4Layout = new org.jdesktop.layout.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, pan_listener, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .add(jButton2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 199, Short.MAX_VALUE)
            .add(titled_jcontexttree, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel4Layout.createSequentialGroup()
                .add(titled_jcontexttree, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jButton2)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(pan_listener, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        jSplitPane1.setLeftComponent(jPanel4);

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jSplitPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 578, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jSplitPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 395, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("for the map", jPanel1);

        titled_jdatachooser.setTitle("JDataChooser");

        jButton1.setText("Show");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dataChooserAction(evt);
            }
        });

        group_jdatachooser.add(jrb_tabbed);
        jrb_tabbed.setText("tabbed");
        jrb_tabbed.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jrb_tabbed.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jrb_tabbed.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                actionDataTabbed(evt);
            }
        });

        group_jdatachooser.add(jrb_buttoned);
        jrb_buttoned.setSelected(true);
        jrb_buttoned.setText("buttoned");
        jrb_buttoned.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jrb_buttoned.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jrb_buttoned.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                actionDataButtoned(evt);
            }
        });

        chk_file.setSelected(true);
        chk_file.setText("FileDataPanel");
        chk_file.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        chk_file.setMargin(new java.awt.Insets(0, 0, 0, 0));

        chk_database.setText("DatabaseDataPanel");
        chk_database.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        chk_database.setMargin(new java.awt.Insets(0, 0, 0, 0));

        chk_server.setText("ServerDataPanel");
        chk_server.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        chk_server.setMargin(new java.awt.Insets(0, 0, 0, 0));

        org.jdesktop.layout.GroupLayout titled_jdatachooserLayout = new org.jdesktop.layout.GroupLayout(titled_jdatachooser.getContentContainer());
        titled_jdatachooser.getContentContainer().setLayout(titled_jdatachooserLayout);
        titled_jdatachooserLayout.setHorizontalGroup(
            titled_jdatachooserLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(titled_jdatachooserLayout.createSequentialGroup()
                .addContainerGap()
                .add(titled_jdatachooserLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(titled_jdatachooserLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, chk_server, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, jrb_buttoned)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, jrb_tabbed))
                    .add(chk_database)
                    .add(chk_file)
                    .add(jButton1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 534, Short.MAX_VALUE))
                .addContainerGap())
        );
        titled_jdatachooserLayout.setVerticalGroup(
            titled_jdatachooserLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(titled_jdatachooserLayout.createSequentialGroup()
                .addContainerGap()
                .add(jrb_tabbed)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jrb_buttoned)
                .add(27, 27, 27)
                .add(chk_file)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(chk_database)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(chk_server)
                .add(24, 24, 24)
                .add(jButton1)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        titled_datapanel.setTitle("DataPanel");

        lbl_filedatapanel.setText("FileDataPanel");

        lbl_databasedatapanel.setText("DatabaseDataPanel");

        lbl_serverdatapanel.setText("ServerDataPanel");

        org.jdesktop.layout.GroupLayout titled_datapanelLayout = new org.jdesktop.layout.GroupLayout(titled_datapanel.getContentContainer());
        titled_datapanel.getContentContainer().setLayout(titled_datapanelLayout);
        titled_datapanelLayout.setHorizontalGroup(
            titled_datapanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(titled_datapanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(titled_datapanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(lbl_filedatapanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 534, Short.MAX_VALUE)
                    .add(lbl_databasedatapanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 534, Short.MAX_VALUE)
                    .add(lbl_serverdatapanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 534, Short.MAX_VALUE))
                .addContainerGap())
        );
        titled_datapanelLayout.setVerticalGroup(
            titled_datapanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(titled_datapanelLayout.createSequentialGroup()
                .add(49, 49, 49)
                .add(lbl_filedatapanel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(lbl_databasedatapanel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(lbl_serverdatapanel)
                .addContainerGap(23, Short.MAX_VALUE))
        );

        org.jdesktop.layout.GroupLayout jPanel3Layout = new org.jdesktop.layout.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, titled_datapanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, titled_jdatachooser, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .add(titled_jdatachooser, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(titled_datapanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("for Data", jPanel3);

        titled_jpropertydialog.setTitle("JPropertyDialog");

        jButton3.setText("MapContext");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                actionPropertyDialogContext(evt);
            }
        });

        jButton4.setText("MapLayer");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                actionPropertyDialogLayer(evt);
            }
        });

        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);

        org.jdesktop.layout.GroupLayout titled_jpropertydialogLayout = new org.jdesktop.layout.GroupLayout(titled_jpropertydialog.getContentContainer());
        titled_jpropertydialog.getContentContainer().setLayout(titled_jpropertydialogLayout);
        titled_jpropertydialogLayout.setHorizontalGroup(
            titled_jpropertydialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(titled_jpropertydialogLayout.createSequentialGroup()
                .addContainerGap()
                .add(jButton3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 258, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jButton4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 262, Short.MAX_VALUE)
                .addContainerGap())
        );
        titled_jpropertydialogLayout.setVerticalGroup(
            titled_jpropertydialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, titled_jpropertydialogLayout.createSequentialGroup()
                .addContainerGap()
                .add(titled_jpropertydialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jButton3)
                    .add(jButton4)
                    .add(jSeparator1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 217, Short.MAX_VALUE))
                .addContainerGap())
        );

        titled_propertypanel.setTitle("PropertyPanel");

        jSeparator2.setOrientation(javax.swing.SwingConstants.VERTICAL);

        lbl_contextcrspropertypanel.setText("ContextCRSPropertyPanel");

        lbl_layerfeaturepropertypanel.setText("LayerFeaturePropertyPanel");

        lbl_layerfilterpropertypanel.setText("LayerFilterPropertyPanel");

        lbl_layerstylepropertypanel.setText("LayerStylePropertyPanel");

        org.jdesktop.layout.GroupLayout titled_propertypanelLayout = new org.jdesktop.layout.GroupLayout(titled_propertypanel.getContentContainer());
        titled_propertypanel.getContentContainer().setLayout(titled_propertypanelLayout);
        titled_propertypanelLayout.setHorizontalGroup(
            titled_propertypanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(titled_propertypanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(lbl_contextcrspropertypanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 258, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jSeparator2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(titled_propertypanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(lbl_layerfeaturepropertypanel)
                    .add(lbl_layerfilterpropertypanel)
                    .add(lbl_layerstylepropertypanel))
                .addContainerGap(143, Short.MAX_VALUE))
        );
        titled_propertypanelLayout.setVerticalGroup(
            titled_propertypanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(titled_propertypanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(titled_propertypanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(titled_propertypanelLayout.createSequentialGroup()
                        .add(lbl_layerfeaturepropertypanel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(lbl_layerfilterpropertypanel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(lbl_layerstylepropertypanel))
                    .add(lbl_contextcrspropertypanel)
                    .add(jSeparator2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 54, Short.MAX_VALUE))
                .addContainerGap())
        );

        org.jdesktop.layout.GroupLayout jPanel6Layout = new org.jdesktop.layout.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, titled_propertypanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, titled_jpropertydialog, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .add(titled_jpropertydialog, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(titled_propertypanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("to edit", jPanel6);

        jPanel5.setBackground(new java.awt.Color(255, 255, 255));
        jPanel5.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true));

        lbl_check.setText("Working");

        lbl_checking.setText("Working but not much tests");

        lbl_working.setText("In work");

        lbl_stop.setText("Not yet begin");

        org.jdesktop.layout.GroupLayout jPanel5Layout = new org.jdesktop.layout.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .add(lbl_check)
                .add(30, 30, 30)
                .add(lbl_checking)
                .add(31, 31, 31)
                .add(lbl_working)
                .add(31, 31, 31)
                .add(lbl_stop)
                .addContainerGap(207, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(lbl_check, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(jPanel5Layout.createSequentialGroup()
                        .add(0, 0, 0)
                        .add(jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(lbl_checking, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, lbl_working, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(lbl_stop, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );

        jMenu1.setText("File");

        jMenuItem1.setText("Exit");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitAction(evt);
            }
        });
        jMenu1.add(jMenuItem1);

        jMenuBar1.add(jMenu1);

        setJMenuBar(jMenuBar1);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jTabbedPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 583, Short.MAX_VALUE)
            .add(jPanel5, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel5, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jTabbedPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 423, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    
    private void actionPropertyDialogContext(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_actionPropertyDialogContext
        ArrayList<PropertyPanel> lst = new ArrayList<PropertyPanel>();
        lst.add(new ContextCRSPropertyPanel());
        JPropertyDialog.showDialog(lst, _context);
    }//GEN-LAST:event_actionPropertyDialogContext
    
    private void actionPropertyDialogLayer(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_actionPropertyDialogLayer
        ArrayList<PropertyPanel> lst = new ArrayList<PropertyPanel>();
        lst.add(new LayerFilterPropertyPanel());
        lst.add(new LayerStylePropertyPanel());
        lst.add(new LayerFeaturePropertyPanel());
        JPropertyDialog.showDialog(lst, _layer);
        
    }//GEN-LAST:event_actionPropertyDialogLayer
    
    private void actionAddContext(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_actionAddContext
        DefaultMapContext context;
        try {
            context = new DefaultMapContext(CRS.decode("EPSG:4326"));
            context.setTitle("Context " + nb);
            tree.addMapContext(context);
            nb++;
        } catch (NoSuchAuthorityCodeException ex) {
            ex.printStackTrace();
        } catch (FactoryException ex) {
            ex.printStackTrace();
        }
        
    }//GEN-LAST:event_actionAddContext
    
    private void actionDataButtoned(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_actionDataButtoned
        state = JDataChooser.STATE.BUTTONED;
    }//GEN-LAST:event_actionDataButtoned
    
    private void actionDataTabbed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_actionDataTabbed
        state = JDataChooser.STATE.TABBED;
    }//GEN-LAST:event_actionDataTabbed
    
    private void dataChooserAction(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dataChooserAction
        List<DataPanel> lst = new ArrayList<DataPanel>();        
        if(chk_file.isSelected()) lst.add(new FileDataPanel());
        if(chk_database.isSelected()) lst.add(new DatabaseDataPanel());
        if(chk_server.isSelected()) lst.add(new ServerDataPanel());        
        JDataChooser.showDialog(lst,state);
        
    }//GEN-LAST:event_dataChooserAction
    
    private void exitAction(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitAction
        System.exit(0);
    }//GEN-LAST:event_exitAction
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
                
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        } catch (InstantiationException ex) {
            ex.printStackTrace();
        } catch (UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
        }
        
        
        
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new DemoSwingGeowidgets().setVisible(true);
            }
        });
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox chk_database;
    private javax.swing.JCheckBox chk_file;
    private javax.swing.JCheckBox chk_server;
    private javax.swing.ButtonGroup group_jdatachooser;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JRadioButton jrb_buttoned;
    private javax.swing.JRadioButton jrb_tabbed;
    private javax.swing.JLabel lbl_check;
    private javax.swing.JLabel lbl_checking;
    private javax.swing.JLabel lbl_contextcrspropertypanel;
    private javax.swing.JLabel lbl_databasedatapanel;
    private javax.swing.JLabel lbl_filedatapanel;
    private javax.swing.JLabel lbl_layerfeaturepropertypanel;
    private javax.swing.JLabel lbl_layerfilterpropertypanel;
    private javax.swing.JLabel lbl_layerstylepropertypanel;
    private javax.swing.JLabel lbl_serverdatapanel;
    private javax.swing.JLabel lbl_stop;
    private javax.swing.JLabel lbl_working;
    private javax.swing.JPanel pan_jcontexttree;
    private javax.swing.JPanel pan_jmappane;
    private org.jdesktop.swingx.JXTitledPanel pan_lightmappanecontrol;
    private javax.swing.JPanel pan_listener;
    private org.jdesktop.swingx.JXTitledPanel titled_datapanel;
    private org.jdesktop.swingx.JXTitledPanel titled_jcontexttree;
    private org.jdesktop.swingx.JXTitledPanel titled_jdatachooser;
    private org.jdesktop.swingx.JXTitledPanel titled_jpropertydialog;
    private org.jdesktop.swingx.JXTitledPanel titled_propertypanel;
    // End of variables declaration//GEN-END:variables
    
}




class ContextTreeListener extends JXTitledPanel implements TreeListener {
    
    private JTextArea txt = new JTextArea();
    private JMapPane map;
    private String message = "";
    
    /** Creates a new instance of JXTreeTableFrameListener 
     * @param map 
     */
    public ContextTreeListener(JMapPane map) {
        super();
        this.map = map;
        setTitle("TreeListener");
        setLeftDecoration( new JLabel(IconBundle.getResource().getIcon("CP22_actions_spellcheck"))  );
        txt.setFont(new Font("Arial", Font.PLAIN, 8));
        setPreferredSize(new Dimension(100, 120));
        add(BorderLayout.CENTER, new JScrollPane(txt));
    }
    
    public void ContextAdded(TreeEvent event) {
        message = "ADDED index: ";
        message += event.getFromIndex();
        message += "  who: " + event.getMapContext().getTitle();
        txt.setText(txt.getText() + message + "\n");
    }
    
    public void ContextRemoved(TreeEvent event) {
        message = "REMOVED index: ";
        message += event.getFromIndex();
        message += "  who: " + event.getMapContext().getTitle();
        txt.setText(txt.getText() + message + "\n");
    }
    
    public void ContextActivated(TreeEvent event) {
        message = "ACTIVATED index: ";
        message += event.getFromIndex();
        message += "  who: " + ((event.getMapContext() != null) ? event.getMapContext().getTitle() : "null");
        txt.setText(txt.getText() + message + "\n");
        
        map.setContext(event.getMapContext());
        
        if(map.getContext() != null){
            try {
                map.setMapArea(map.getContext().getLayerBounds());
                map.setReset(true);
                map.repaint();
                map.revalidate();
            } catch (IOException ex) {
                Logger.getLogger(ContextTreeListener.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
       
    }
    
    public void ContextMoved(TreeEvent event) {
        message = "MOVED indexes: ";
        message += event.getFromIndex() + " > " + event.getToIndex();
        message += "  who: " + event.getMapContext().getTitle();
        txt.setText(txt.getText() + message + "\n");
    }
}
