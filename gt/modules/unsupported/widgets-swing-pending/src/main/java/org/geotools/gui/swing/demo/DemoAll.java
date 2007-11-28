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

import java.awt.GridLayout;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.gui.swing.contexttree.JContextTree;
import org.geotools.gui.swing.contexttree.JContextTreePopup;
import org.geotools.gui.swing.contexttree.TreeContextEvent;
import org.geotools.gui.swing.contexttree.TreeContextListener;
import org.geotools.gui.swing.contexttree.column.OpacityTreeTableColumn;
import org.geotools.gui.swing.contexttree.column.SelectionTreeTableColumn;
import org.geotools.gui.swing.contexttree.column.StyleTreeTableColumn;
import org.geotools.gui.swing.contexttree.column.VisibleTreeTableColumn;
import org.geotools.gui.swing.contexttree.popup.ContextActiveItem;
import org.geotools.gui.swing.contexttree.popup.ContextPropertyItem;
import org.geotools.gui.swing.contexttree.popup.CopyItem;
import org.geotools.gui.swing.contexttree.popup.CutItem;
import org.geotools.gui.swing.contexttree.popup.DeleteItem;
import org.geotools.gui.swing.contexttree.popup.DuplicateItem;
import org.geotools.gui.swing.contexttree.popup.LayerFeatureItem;
import org.geotools.gui.swing.contexttree.popup.LayerPropertyItem;
import org.geotools.gui.swing.contexttree.popup.LayerVisibilityItem;
import org.geotools.gui.swing.contexttree.popup.LayerZoomItem;
import org.geotools.gui.swing.contexttree.popup.PasteItem;
import org.geotools.gui.swing.contexttree.popup.SeparatorItem;
import org.geotools.gui.swing.datachooser.DataPanel;
import org.geotools.gui.swing.datachooser.JDataChooser;
import org.geotools.gui.swing.datachooser.JDataChooser;
import org.geotools.gui.swing.datachooser.JDatabaseDataPanel;
import org.geotools.gui.swing.datachooser.JFileDataPanel;
import org.geotools.gui.swing.datachooser.JServerDataPanel;
import org.geotools.gui.swing.icon.IconBundle;
import org.geotools.gui.swing.map.map2d.DefaultEditableMap2D;
import org.geotools.gui.swing.map.map2d.DefaultSelectableMap2D;
import org.geotools.gui.swing.map.map2d.SelectableMap2D;
import org.geotools.gui.swing.misc.Render.RandomStyleFactory;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.DefaultMapLayer;
import org.geotools.map.MapContext;
import org.geotools.map.MapLayer;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.renderer.shape.ShapefileRenderer;
import org.geotools.styling.Style;


/**
 *
 * @author johann sorel
 */
public class DemoAll extends javax.swing.JFrame {

    private final RandomStyleFactory RANDOM_STYLE_FACTORY = new RandomStyleFactory();
    private final DefaultEditableMap2D map;
    private final OpacityTreeTableColumn colOpacity = new OpacityTreeTableColumn();
    private final VisibleTreeTableColumn colVisible = new VisibleTreeTableColumn();
    private final StyleTreeTableColumn colStyle = new StyleTreeTableColumn();
    private final SelectionTreeTableColumn colSelection = new SelectionTreeTableColumn(null);
    
    private int nb = 1;

    /** Creates new form DemoSwingGeowidgets */
    public DemoAll() {
                
        initComponents();
        setLocationRelativeTo(null);

        map = new DefaultEditableMap2D(new ShapefileRenderer());
        map.getComponent().setOpaque(false);
        
        final MapContext context = buildContext();
        initTree(tree,map);
                        
        pan_mappane.setLayout(new GridLayout(1, 1));
        pan_mappane.add(map);
        
        
        tree.addContext(context);

        gui_map2dcontrol.setMap(map);
        gui_map2dinfo.setMap(map);
        gui_map2dedit.setMap(map);
        
        
        tree.addTreeContextListener(new TreeContextListener() {

            public void contextAdded(TreeContextEvent event) {
            }

            public void contextRemoved(TreeContextEvent event) {
            }

            public void contextActivated(TreeContextEvent event) {
                if(event.getMapContext() != null){
                    map.setContext(event.getMapContext());
                }
            }

            public void contextMoved(TreeContextEvent event) {
            }
        });
        
        
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                map.setContext(context);
                
                try {
            map.setMapArea(map.getContext().getLayerBounds());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
            }
        });

    }

    private MapContext buildContext() {
        MapContext context = null;
        MapLayer layer;

        try {
            context = new DefaultMapContext(DefaultGeographicCRS.WGS84);
            HashMap hash = new HashMap();
            hash.put("url", DemoAll.class.getResource("/org/geotools/gui/swing/demo/shape/test_polygon.shp"));
            DataStore store = DataStoreFinder.getDataStore(hash);
            FeatureSource fs = store.getFeatureSource(store.getTypeNames()[0]);
            Style style = RANDOM_STYLE_FACTORY.createRandomVectorStyle(fs);
            layer = new DefaultMapLayer(fs, style);
            layer.setTitle("demo_polygon.shp");
            context.addLayer(layer);

            hash = new HashMap();
            hash.put("url", DemoAll.class.getResource("/org/geotools/gui/swing/demo/shape/test_ligne.shp"));
            store = DataStoreFinder.getDataStore(hash);
            fs = store.getFeatureSource(store.getTypeNames()[0]);
            style = RANDOM_STYLE_FACTORY.createRandomVectorStyle(fs);
            layer = new DefaultMapLayer(fs, style);
            layer.setTitle("demo_line.shp");
            context.addLayer(layer);

            hash = new HashMap();
            hash.put("url", DemoAll.class.getResource("/org/geotools/gui/swing/demo/shape/test_point.shp"));
            store = DataStoreFinder.getDataStore(hash);
            fs = store.getFeatureSource(store.getTypeNames()[0]);
            style = RANDOM_STYLE_FACTORY.createRandomVectorStyle(fs);
            layer = new DefaultMapLayer(fs, style);
            layer.setTitle("demo_point.shp");
            context.addLayer(layer);
            context.setTitle("DemoContext");
        } catch (Exception ex) {
            ex.printStackTrace();
        }


        return context;
    }

    private void initTree(JContextTree tree,SelectableMap2D map){
        JContextTreePopup popup = tree.getPopupMenu();        
                
        popup.addItem(new LayerVisibilityItem());           //layer         
        popup.addItem(new SeparatorItem() );        
        popup.addItem(new LayerZoomItem(null));              //layer
        popup.addItem(new LayerFeatureItem());              //layer
        popup.addItem(new ContextActiveItem(tree));         //context
        popup.addItem(new SeparatorItem() );
        popup.addItem(new CutItem(tree));                   //all
        popup.addItem(new CopyItem(tree));                  //all
        popup.addItem(new PasteItem(tree));                 //all
        popup.addItem(new DuplicateItem(tree));             //all        
        popup.addItem(new SeparatorItem() );        
        popup.addItem(new DeleteItem(tree));                //all
        popup.addItem(new SeparatorItem() );        
        popup.addItem(new LayerPropertyItem());             //layer
        popup.addItem(new ContextPropertyItem());           //context
                
        popup.setMap(map);        
        colSelection.setMap(map);
        
        tree.addColumn(colVisible);
        tree.addColumn(colOpacity);
        tree.addColumn(colStyle);
        tree.addColumn(colSelection);
        
        tree.revalidate();
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
        jSplitPane1 = new javax.swing.JSplitPane();
        jpanel8 = new javax.swing.JPanel();
        pan_mappane = new javax.swing.JPanel();
        gui_map2dcontrol = new org.geotools.gui.swing.map.map2d.control.JMap2DControlBar();
        gui_map2dinfo = new org.geotools.gui.swing.map.map2d.control.JMap2DInfoBar();
        gui_map2dedit = new org.geotools.gui.swing.map.map2d.control.JMap2DEditBar();
        jPanel4 = new javax.swing.JPanel();
        tree = new org.geotools.gui.swing.contexttree.JContextTree();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem4 = new javax.swing.JMenuItem();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        jMenuItem3 = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JSeparator();
        chk_file = new javax.swing.JCheckBoxMenuItem();
        chk_database = new javax.swing.JCheckBoxMenuItem();
        chk_server = new javax.swing.JCheckBoxMenuItem();
        jMenu4 = new javax.swing.JMenu();
        jMenu5 = new javax.swing.JMenu();
        guiChkVisible = new javax.swing.JCheckBoxMenuItem();
        guiChkOpacity = new javax.swing.JCheckBoxMenuItem();
        guiChkStyle = new javax.swing.JCheckBoxMenuItem();
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

        jSplitPane1.setDividerLocation(300);

        jpanel8.setBackground(new java.awt.Color(102, 102, 102));

        pan_mappane.setBackground(new java.awt.Color(255, 255, 255));

        org.jdesktop.layout.GroupLayout pan_mappaneLayout = new org.jdesktop.layout.GroupLayout(pan_mappane);
        pan_mappane.setLayout(pan_mappaneLayout);
        pan_mappaneLayout.setHorizontalGroup(
            pan_mappaneLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 544, Short.MAX_VALUE)
        );
        pan_mappaneLayout.setVerticalGroup(
            pan_mappaneLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 409, Short.MAX_VALUE)
        );

        org.jdesktop.layout.GroupLayout jpanel8Layout = new org.jdesktop.layout.GroupLayout(jpanel8);
        jpanel8.setLayout(jpanel8Layout);
        jpanel8Layout.setHorizontalGroup(
            jpanel8Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(gui_map2dinfo, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 544, Short.MAX_VALUE)
            .add(jpanel8Layout.createSequentialGroup()
                .add(gui_map2dcontrol, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 317, Short.MAX_VALUE)
                .add(gui_map2dedit, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
            .add(pan_mappane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jpanel8Layout.setVerticalGroup(
            jpanel8Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jpanel8Layout.createSequentialGroup()
                .add(jpanel8Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(gui_map2dcontrol, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(gui_map2dedit, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(pan_mappane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(gui_map2dinfo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 24, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        jSplitPane1.setRightComponent(jpanel8);

        org.jdesktop.layout.GroupLayout jPanel4Layout = new org.jdesktop.layout.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(tree, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 299, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(tree, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 470, Short.MAX_VALUE)
        );

        jSplitPane1.setLeftComponent(jPanel4);

        jMenu1.setText("File");

        jMenuItem4.setText("New Context");
        jMenuItem4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                actionNewContext(evt);
            }
        });
        jMenu1.add(jMenuItem4);

        jMenuItem1.setText("Exit");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitAction(evt);
            }
        });
        jMenu1.add(jMenuItem1);

        jMenuBar1.add(jMenu1);

        jMenu2.setText("Data");

        jMenuItem3.setText("Add Data");
        jMenuItem3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dataChooserAction(evt);
            }
        });
        jMenu2.add(jMenuItem3);
        jMenu2.add(jSeparator3);

        chk_file.setSelected(true);
        chk_file.setText("FileDataPanel");
        jMenu2.add(chk_file);

        chk_database.setSelected(true);
        chk_database.setText("DatabaseDataPanel");
        jMenu2.add(chk_database);

        chk_server.setSelected(true);
        chk_server.setText("ServerDataPanel");
        jMenu2.add(chk_server);

        jMenuBar1.add(jMenu2);

        jMenu4.setText("GUI");

        jMenu5.setText("Context Tree");

        guiChkVisible.setSelected(true);
        guiChkVisible.setText("Visible Column");
        guiChkVisible.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                guiChkVisibleActionPerformed(evt);
            }
        });
        jMenu5.add(guiChkVisible);

        guiChkOpacity.setSelected(true);
        guiChkOpacity.setText("Opacity Column");
        guiChkOpacity.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                guiChkOpacityActionPerformed(evt);
            }
        });
        jMenu5.add(guiChkOpacity);

        guiChkStyle.setSelected(true);
        guiChkStyle.setText("Style Column");
        guiChkStyle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                guiChkStyleActionPerformed(evt);
            }
        });
        jMenu5.add(guiChkStyle);

        jMenu4.add(jMenu5);

        jMenuBar1.add(jMenu4);

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
            .add(jSplitPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 850, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jSplitPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 472, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

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
                lst.add(new JServerDataPanel());
            }

            JDataChooser jdc = new JDataChooser(null,lst);
            
            JDataChooser.ACTION ret = jdc.showDialog();

            if (ret == JDataChooser.ACTION.APPROVE) {
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

    private void actionNewContext(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_actionNewContext
        DefaultMapContext context;

        context = new DefaultMapContext(DefaultGeographicCRS.WGS84);
        context.setTitle("Context " + nb);
        tree.addContext(context);
        nb++;
        
    }//GEN-LAST:event_actionNewContext

    private void guiChkVisibleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_guiChkVisibleActionPerformed
        
        if(guiChkVisible.isSelected()){
            tree.addColumn(colVisible);
        }else{
            tree.removeColumn(colVisible);
        }
}//GEN-LAST:event_guiChkVisibleActionPerformed

    private void guiChkOpacityActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_guiChkOpacityActionPerformed
         if(guiChkOpacity.isSelected()){
            tree.addColumn(colOpacity);
        }else{
            tree.removeColumn(colOpacity);
        }
}//GEN-LAST:event_guiChkOpacityActionPerformed

    private void guiChkStyleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_guiChkStyleActionPerformed
         if(guiChkStyle.isSelected()){
            tree.addColumn(colStyle);
        }else{
            tree.removeColumn(colStyle);
        }
}//GEN-LAST:event_guiChkStyleActionPerformed

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
    private javax.swing.JCheckBoxMenuItem guiChkOpacity;
    private javax.swing.JCheckBoxMenuItem guiChkStyle;
    private javax.swing.JCheckBoxMenuItem guiChkVisible;
    private org.geotools.gui.swing.map.map2d.control.JMap2DControlBar gui_map2dcontrol;
    private org.geotools.gui.swing.map.map2d.control.JMap2DEditBar gui_map2dedit;
    private org.geotools.gui.swing.map.map2d.control.JMap2DInfoBar gui_map2dinfo;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JList jList1;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenu jMenu4;
    private javax.swing.JMenu jMenu5;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JMenuItem jMenuItem4;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSplitPane jSplitPane1;
    private org.jdesktop.swingx.JXImagePanel jXImagePanel1;
    private javax.swing.JPanel jpanel8;
    private javax.swing.JPanel pan_mappane;
    private org.geotools.gui.swing.contexttree.JContextTree tree;
    // End of variables declaration//GEN-END:variables
}
