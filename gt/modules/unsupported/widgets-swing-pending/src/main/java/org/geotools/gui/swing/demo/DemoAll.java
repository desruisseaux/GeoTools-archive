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

import java.awt.BorderLayout;
import java.awt.ComponentOrientation;
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

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.gui.swing.JMapPane;
import org.geotools.gui.swing.contexttree.TreeEvent;
import org.geotools.gui.swing.contexttree.TreeListener;
import org.geotools.gui.swing.contexttree.JContextTree;
import org.geotools.gui.swing.control.JLightMapPaneControl;
import org.geotools.gui.swing.control.JMapPaneInfoPanel;
import org.geotools.gui.swing.datachooser.DataPanel;
import org.geotools.gui.swing.datachooser.JDataChooser;
import org.geotools.gui.swing.datachooser.JDataChooser;
import org.geotools.gui.swing.datachooser.JDatabaseDataPanel;
import org.geotools.gui.swing.datachooser.JFileDataPanel;
import org.geotools.gui.swing.datachooser.ServerDataPanel;
import org.geotools.gui.swing.icon.IconBundle;
import org.geotools.gui.swing.misc.Render.RandomStyleFactory;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.DefaultMapLayer;
import org.geotools.map.MapContext;
import org.geotools.map.MapLayer;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.renderer.shape.ShapefileRenderer;
import org.geotools.styling.Style;
import org.jdesktop.swingx.JXTitledPanel;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;


/**
 *
 * @author johann sorel
 */
public class DemoAll extends javax.swing.JFrame {

    private final RandomStyleFactory RANDOM_STYLE_FACTORY = new RandomStyleFactory();
    private final ImageIcon ICO_CHECK = IconBundle.getResource().getIcon("16_check");
    private final ImageIcon ICO_ERROR = IconBundle.getResource().getIcon("16_error");
    private final ImageIcon ICO_WARNING = IconBundle.getResource().getIcon("16_warning");
    private final ImageIcon ICO_INFORMATION = IconBundle.getResource().getIcon("16_information");
    
    private MapContext _context;
    private MapLayer _layer;
    private JContextTree tree;
    private JMapPane map;
    private JLightMapPaneControl lightcontrol;
    private JMapPaneInfoPanel infopanel;
    private JDataChooser.STATE state = JDataChooser.STATE.BUTTONED;
    private int nb = 1;

    /** Creates new form DemoSwingGeowidgets */
    public DemoAll() {
        initComponents();
        setLocationRelativeTo(null);


        Map hash;
        DataStore store;
        FeatureSource fs;
        Style style;
        MapLayer layer;
        try {

            //_context = new DefaultMapContext(CRS.decode("EPSG:4326"));
            _context = new DefaultMapContext(DefaultGeographicCRS.WGS84);
            hash = new HashMap();
            hash.put("url", DemoAll.class.getResource("/org/geotools/gui/swing/demo/shape/test_polygon.shp"));
            store = DataStoreFinder.getDataStore(hash);
            fs = store.getFeatureSource(store.getTypeNames()[0]);
            style = RANDOM_STYLE_FACTORY.createRandomVectorStyle(fs);
            layer = new DefaultMapLayer(fs, style);
            layer.setTitle("demo_polygon.shp");
            _context.addLayer(layer);
            _layer = layer;

            hash = new HashMap();
            hash.put("url", DemoAll.class.getResource("/org/geotools/gui/swing/demo/shape/test_ligne.shp"));
            store = DataStoreFinder.getDataStore(hash);
            fs = store.getFeatureSource(store.getTypeNames()[0]);
            style = RANDOM_STYLE_FACTORY.createRandomVectorStyle(fs);
            layer = new DefaultMapLayer(fs, style);
            layer.setTitle("demo_line.shp");
            _context.addLayer(layer);

            hash = new HashMap();
            hash.put("url", DemoAll.class.getResource("/org/geotools/gui/swing/demo/shape/test_point.shp"));
            store = DataStoreFinder.getDataStore(hash);
            fs = store.getFeatureSource(store.getTypeNames()[0]);
            style = RANDOM_STYLE_FACTORY.createRandomVectorStyle(fs);
            layer = new DefaultMapLayer(fs, style);
            layer.setTitle("demo_point.shp");
            _context.addLayer(layer);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        _context.setTitle("DemoContext");


        

        /***********************JMAPPANE***************************************/
        map = new JMapPane();
        map.setOpaque(false);
        map.setRenderer(new ShapefileRenderer());
        map.setContext(_context);
        pan_jmappane.setLayout(new GridLayout(1, 1));
        pan_jmappane.add(map);

        try {
            map.setMapArea(map.getContext().getLayerBounds());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        map.revalidate();

        /************************JCONTEXTTREE**********************************/
        titled_jcontexttree.setLeftDecoration(new JLabel(ICO_INFORMATION));
        tree = JContextTree.createDefaultTree(map);
        tree.addMapContext(_context);
        
        JScrollPane pane = new JScrollPane(tree);
        pane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        pane.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        
        tree.expandAll();
        pan_jcontexttree.setLayout(new GridLayout(1, 1));
        pan_jcontexttree.add(pane);
        
        /************************JLIGHTMAPPANECONTROL**************************/
        pan_lightmappanecontrol.setLeftDecoration(new JLabel(ICO_INFORMATION));
        lightcontrol = new JLightMapPaneControl();
        lightcontrol.setMapPane(map);
        pan_lightmappanecontrol.add(lightcontrol);


        /************************JMAPPANEINFOPANEL*****************************/
        pan_mappaneinfo.setLeftDecoration(new JLabel(ICO_INFORMATION));
        infopanel = new JMapPaneInfoPanel();
        infopanel.setMapPane(map);
        pan_mappaneinfo.add(infopanel);

        /************************LISTENER**************************************/
        pan_listener.setLayout(new GridLayout(1, 1));
        ContextTreeListener ecouteur = new ContextTreeListener(map);
        pan_listener.add(ecouteur);
        tree.addTreeListener(ecouteur);


    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        group_jdatachooser = new javax.swing.ButtonGroup();
        dia_about = new javax.swing.JDialog();
        jXImagePanel1 = new org.jdesktop.swingx.JXImagePanel();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList();
        jPanel5 = new javax.swing.JPanel();
        lbl_check = new javax.swing.JLabel();
        lbl_checking = new javax.swing.JLabel();
        lbl_working = new javax.swing.JLabel();
        lbl_stop = new javax.swing.JLabel();
        jSplitPane1 = new javax.swing.JSplitPane();
        pan_jmappane = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        titled_jcontexttree = new org.jdesktop.swingx.JXTitledPanel();
        pan_jcontexttree = new javax.swing.JPanel();
        pan_listener = new javax.swing.JPanel();
        jButton2 = new javax.swing.JButton();
        pan_lightmappanecontrol = new org.jdesktop.swingx.JXTitledPanel();
        pan_mappaneinfo = new org.jdesktop.swingx.JXTitledPanel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        jMenuItem3 = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JSeparator();
        jRadioButtonMenuItem1 = new javax.swing.JRadioButtonMenuItem();
        jRadioButtonMenuItem2 = new javax.swing.JRadioButtonMenuItem();
        jSeparator4 = new javax.swing.JSeparator();
        chk_file = new javax.swing.JCheckBoxMenuItem();
        chk_database = new javax.swing.JCheckBoxMenuItem();
        chk_server = new javax.swing.JCheckBoxMenuItem();
        jMenu3 = new javax.swing.JMenu();
        jMenuItem2 = new javax.swing.JMenuItem();

        jXImagePanel1.setImage(IconBundle.getResource().getIcon("about").getImage());
        jXImagePanel1.setStyle(org.jdesktop.swingx.JXImagePanel.Style.SCALED_KEEP_ASPECT_RATIO);

        org.jdesktop.layout.GroupLayout jXImagePanel1Layout = new org.jdesktop.layout.GroupLayout(jXImagePanel1);
        jXImagePanel1.setLayout(jXImagePanel1Layout);
        jXImagePanel1Layout.setHorizontalGroup(
            jXImagePanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 137, Short.MAX_VALUE)
        );
        jXImagePanel1Layout.setVerticalGroup(
            jXImagePanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 203, Short.MAX_VALUE)
        );

        jLabel1.setText("GT Swing Widget Team :");

        jList1.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Johann Sorel" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane1.setViewportView(jList1);

        org.jdesktop.layout.GroupLayout dia_aboutLayout = new org.jdesktop.layout.GroupLayout(dia_about.getContentPane());
        dia_about.getContentPane().setLayout(dia_aboutLayout);
        dia_aboutLayout.setHorizontalGroup(
            dia_aboutLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(dia_aboutLayout.createSequentialGroup()
                .addContainerGap()
                .add(jXImagePanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(18, 18, 18)
                .add(dia_aboutLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel1)
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 225, Short.MAX_VALUE))
                .addContainerGap())
        );
        dia_aboutLayout.setVerticalGroup(
            dia_aboutLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(dia_aboutLayout.createSequentialGroup()
                .addContainerGap()
                .add(dia_aboutLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(dia_aboutLayout.createSequentialGroup()
                        .add(jLabel1)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 183, Short.MAX_VALUE))
                    .add(jXImagePanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel5.setBackground(new java.awt.Color(255, 255, 255));
        jPanel5.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true));

        lbl_check.setIcon(ICO_CHECK);
        lbl_check.setText("Working");

        lbl_checking.setIcon(ICO_INFORMATION);
        lbl_checking.setText("Working but will be improved");

        lbl_working.setIcon(ICO_WARNING);
        lbl_working.setText("In work");

        lbl_stop.setIcon(ICO_ERROR);
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
                .addContainerGap(379, Short.MAX_VALUE))
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

        jSplitPane1.setDividerLocation(300);

        pan_jmappane.setBackground(new java.awt.Color(255, 255, 255));

        org.jdesktop.layout.GroupLayout pan_jmappaneLayout = new org.jdesktop.layout.GroupLayout(pan_jmappane);
        pan_jmappane.setLayout(pan_jmappaneLayout);
        pan_jmappaneLayout.setHorizontalGroup(
            pan_jmappaneLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 454, Short.MAX_VALUE)
        );
        pan_jmappaneLayout.setVerticalGroup(
            pan_jmappaneLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 340, Short.MAX_VALUE)
        );

        jSplitPane1.setRightComponent(pan_jmappane);

        titled_jcontexttree.setTitle(" JContextTree");

        org.jdesktop.layout.GroupLayout pan_jcontexttreeLayout = new org.jdesktop.layout.GroupLayout(pan_jcontexttree);
        pan_jcontexttree.setLayout(pan_jcontexttreeLayout);
        pan_jcontexttreeLayout.setHorizontalGroup(
            pan_jcontexttreeLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 295, Short.MAX_VALUE)
        );
        pan_jcontexttreeLayout.setVerticalGroup(
            pan_jcontexttreeLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 203, Short.MAX_VALUE)
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
            .add(0, 299, Short.MAX_VALUE)
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
            .add(jButton2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 299, Short.MAX_VALUE)
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

        pan_lightmappanecontrol.setTitle("JLightMapPaneControl");

        org.jdesktop.layout.GroupLayout pan_lightmappanecontrolLayout = new org.jdesktop.layout.GroupLayout(pan_lightmappanecontrol.getContentContainer());
        pan_lightmappanecontrol.getContentContainer().setLayout(pan_lightmappanecontrolLayout);
        pan_lightmappanecontrolLayout.setHorizontalGroup(
            pan_lightmappanecontrolLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 756, Short.MAX_VALUE)
        );
        pan_lightmappanecontrolLayout.setVerticalGroup(
            pan_lightmappanecontrolLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 30, Short.MAX_VALUE)
        );

        pan_mappaneinfo.setTitle("JMapPaneInfoPanel");

        org.jdesktop.layout.GroupLayout pan_mappaneinfoLayout = new org.jdesktop.layout.GroupLayout(pan_mappaneinfo.getContentContainer());
        pan_mappaneinfo.getContentContainer().setLayout(pan_mappaneinfoLayout);
        pan_mappaneinfoLayout.setHorizontalGroup(
            pan_mappaneinfoLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 756, Short.MAX_VALUE)
        );
        pan_mappaneinfoLayout.setVerticalGroup(
            pan_mappaneinfoLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 35, Short.MAX_VALUE)
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

        jMenu2.setText("Data");

        jMenuItem3.setIcon(ICO_CHECK);
        jMenuItem3.setText("Add Data");
        jMenuItem3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dataChooserAction(evt);
            }
        });
        jMenu2.add(jMenuItem3);
        jMenu2.add(jSeparator3);

        group_jdatachooser.add(jRadioButtonMenuItem1);
        jRadioButtonMenuItem1.setText("tabbed");
        jRadioButtonMenuItem1.setIcon(ICO_CHECK);
        jRadioButtonMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                actionDataTabbed(evt);
            }
        });
        jMenu2.add(jRadioButtonMenuItem1);

        group_jdatachooser.add(jRadioButtonMenuItem2);
        jRadioButtonMenuItem2.setSelected(true);
        jRadioButtonMenuItem2.setText("buttoned");
        jRadioButtonMenuItem2.setIcon(ICO_CHECK);
        jRadioButtonMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                actionDataButtoned(evt);
            }
        });
        jMenu2.add(jRadioButtonMenuItem2);
        jMenu2.add(jSeparator4);

        chk_file.setSelected(true);
        chk_file.setText("FileDataPanel");
        chk_file.setIcon(ICO_INFORMATION);
        jMenu2.add(chk_file);

        chk_database.setSelected(true);
        chk_database.setText("DatabaseDataPanel");
        chk_database.setIcon(ICO_INFORMATION);
        jMenu2.add(chk_database);

        chk_server.setSelected(true);
        chk_server.setText("ServerDataPanel");
        chk_server.setIcon(ICO_ERROR);
        jMenu2.add(chk_server);

        jMenuBar1.add(jMenu2);

        jMenu3.setText("?");

        jMenuItem2.setText("About");
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuAbout(evt);
            }
        });
        jMenu3.add(jMenuItem2);

        jMenuBar1.add(jMenu3);

        setJMenuBar(jMenuBar1);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel5, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .add(pan_lightmappanecontrol, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .add(pan_mappaneinfo, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .add(jSplitPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 760, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel5, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(pan_lightmappanecontrol, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jSplitPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 342, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(pan_mappaneinfo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
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

        if (tree.getActiveContext() != null) {
            List<DataPanel> lst = new ArrayList<DataPanel>();

            if (chk_file.isSelected()) {
                lst.add(new JFileDataPanel());
            }
            if (chk_database.isSelected()) {
                lst.add(new JDatabaseDataPanel());
            }
            if (chk_server.isSelected()) {
                lst.add(new ServerDataPanel());
            }

            JDataChooser jdc = new JDataChooser(null, true, state);
            jdc.setDataTypeChooser(lst);
            int ret = jdc.showDialog();

            if (ret == JDataChooser.ADD_EXIT) {
                List<MapLayer> layers = jdc.getLayers();
                for (MapLayer layer : layers) {
                    tree.getActiveContext().addLayer(layer);
                }
            }
        
        }
    }//GEN-LAST:event_dataChooserAction

    private void exitAction(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitAction
        System.exit(0);
    }//GEN-LAST:event_exitAction

    private void menuAbout(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuAbout
        dia_about.setLocationRelativeTo(null);
        dia_about.setSize(400, 200);
        dia_about.setVisible(true);
    }//GEN-LAST:event_menuAbout

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

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


        new DemoAll().setVisible(true);
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBoxMenuItem chk_database;
    private javax.swing.JCheckBoxMenuItem chk_file;
    private javax.swing.JCheckBoxMenuItem chk_server;
    private javax.swing.JDialog dia_about;
    private javax.swing.ButtonGroup group_jdatachooser;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JList jList1;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JRadioButtonMenuItem jRadioButtonMenuItem1;
    private javax.swing.JRadioButtonMenuItem jRadioButtonMenuItem2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JSplitPane jSplitPane1;
    private org.jdesktop.swingx.JXImagePanel jXImagePanel1;
    private javax.swing.JLabel lbl_check;
    private javax.swing.JLabel lbl_checking;
    private javax.swing.JLabel lbl_stop;
    private javax.swing.JLabel lbl_working;
    private javax.swing.JPanel pan_jcontexttree;
    private javax.swing.JPanel pan_jmappane;
    private org.jdesktop.swingx.JXTitledPanel pan_lightmappanecontrol;
    private javax.swing.JPanel pan_listener;
    private org.jdesktop.swingx.JXTitledPanel pan_mappaneinfo;
    private org.jdesktop.swingx.JXTitledPanel titled_jcontexttree;
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
        setLeftDecoration(new JLabel(IconBundle.getResource().getIcon("16_information")));
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

        if (map.getContext() != null) {
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