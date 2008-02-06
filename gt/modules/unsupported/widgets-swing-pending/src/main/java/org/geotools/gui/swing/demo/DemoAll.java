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
import java.awt.Color;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import java.util.Map;
import javax.swing.JDialog;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.apache.commons.collections.map.SingletonMap;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.data.shapefile.ShapefileDataStore;
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
import org.geotools.gui.swing.contexttree.node.SourceGroup;
import org.geotools.gui.swing.contexttree.node.StyleGroup;
import org.geotools.gui.swing.contexttree.popup.RuleMaxScaleItem;
import org.geotools.gui.swing.contexttree.popup.RuleMinScaleItem;
import org.geotools.gui.swing.datachooser.DataPanel;
import org.geotools.gui.swing.datachooser.JDataChooser;
import org.geotools.gui.swing.datachooser.JDataChooser;
import org.geotools.gui.swing.datachooser.JDatabaseDataPanel;
import org.geotools.gui.swing.datachooser.JFileDataPanel;
import org.geotools.gui.swing.datachooser.JServerDataPanel;
import org.geotools.gui.swing.datachooser.JWFSDataPanel;
import org.geotools.gui.swing.datachooser.JWMSDataPanel;
import org.geotools.gui.swing.icon.IconBundle;
import org.geotools.gui.swing.map.map2d.JDefaultEditableMap2D;
import org.geotools.gui.swing.map.map2d.Map2D;
import org.geotools.gui.swing.map.map2d.SelectableMap2D;
import org.geotools.gui.swing.map.map2d.decoration.ColorDecoration;
import org.geotools.gui.swing.map.map2d.decoration.ImageDecoration;
import org.geotools.gui.swing.map.map2d.decoration.MiniMapDecoration;
import org.geotools.gui.swing.map.map2d.decoration.NavigationDecoration;
import org.geotools.gui.swing.misc.Render.RandomStyleFactory;
import org.geotools.gui.swing.toolbox.WidgetTool;
import org.geotools.gui.swing.toolbox.WidgetToolListener;
import org.geotools.gui.swing.toolbox.widgettool.clipping.ClippingTTDescriptor;
import org.geotools.gui.swing.toolbox.tooltree.JToolTree;
import org.geotools.gui.swing.toolbox.tooltree.ToolTreeListener;
import org.geotools.gui.swing.toolbox.WidgetToolDescriptor;
import org.geotools.gui.swing.toolbox.widgettool.shapecreation.ShapeCreationTTDescriptor;
import org.geotools.gui.swing.toolbox.widgettool.svg2mif.SVG2MIFTTDescriptor;
import org.geotools.gui.swing.toolbox.widgettool.vdem2csv.VDem2CSVTTDescriptor;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.DefaultMapLayer;
import org.geotools.map.MapContext;
import org.geotools.map.MapLayer;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.styling.Style;

/**
 *
 * @author johann sorel
 */
public class DemoAll extends javax.swing.JFrame {

    private final RandomStyleFactory RANDOM_STYLE_FACTORY = new RandomStyleFactory();
    private final JDefaultEditableMap2D map;
    private final OpacityTreeTableColumn colOpacity = new OpacityTreeTableColumn();
    private final VisibleTreeTableColumn colVisible = new VisibleTreeTableColumn();
    private final StyleTreeTableColumn colStyle = new StyleTreeTableColumn();
    private final SelectionTreeTableColumn colSelection = new SelectionTreeTableColumn(null);
    private final SourceGroup subsource = new SourceGroup();
    private final StyleGroup substyle = new StyleGroup();
    private final JToolTree tooltree = new JToolTree();
    private final ImageDecoration overBackImage = new ImageDecoration();
    private final ColorDecoration overBackColor = new ColorDecoration();
    private final NavigationDecoration overNavigation = new NavigationDecoration();
    private final MiniMapDecoration overMiniMap = new MiniMapDecoration();
    private final WidgetToolDescriptor shapeTool = new ShapeCreationTTDescriptor();
    private final WidgetToolDescriptor vdem2csvTool = new VDem2CSVTTDescriptor();
    private final WidgetToolDescriptor svg2mifTool = new SVG2MIFTTDescriptor();
    private final WidgetToolDescriptor clipTool = new ClippingTTDescriptor();
    private int nb = 1;

    /** Creates new form DemoSwingGeowidgets */
    public DemoAll() {

        initComponents();
        setLocationRelativeTo(null);

        map = new JDefaultEditableMap2D();

        final MapContext context = buildContext();
        initTree(tree, map);

        pan_mappane.setLayout(new GridLayout(1, 1));
        pan_mappane.add(map);

        tree.addContext(context);

        gui_map2dcontrol.setMap(map);
        gui_map2dinfo.setMap(map);
        gui_map2dedit.setMap(map);

        overBackImage.setImage(IconBundle.getResource().getIcon("about").getImage());
        overBackImage.setOpaque(true);
        overBackImage.setBackground(new Color(0.7f, 0.7f, 1f, 0.8f));
        overBackImage.setStyle(org.jdesktop.swingx.JXImagePanel.Style.CENTERED);
        map.setBackDecoration(overBackColor);
        map.addDecoration(overNavigation);

        tree.addTreeContextListener(new TreeContextListener() {

            public void contextAdded(TreeContextEvent event) {
            }

            public void contextRemoved(TreeContextEvent event) {
            }

            public void contextActivated(TreeContextEvent event) {
                if (event.getMapContext() != null) {
                    map.getRenderingStrategy().setContext(event.getMapContext());
                }
            }

            public void contextMoved(TreeContextEvent event) {
            }
        });

        pantoolbox.add(BorderLayout.CENTER, tooltree);


        tooltree.addTool(shapeTool);
        tooltree.addTool(svg2mifTool);
        tooltree.addTool(vdem2csvTool);
        tooltree.addTool(clipTool);

        tooltree.addToolTreeListener(new ToolTreeListener() {

            public void treeToolActivated(WidgetToolDescriptor tool) {
                MapContext[] contexts = tree.getContexts();
                List<MapLayer> layers = new ArrayList<MapLayer>();

                for (MapContext context : contexts) {
                    MapLayer[] lst = context.getLayers();
                    for (MapLayer layer : lst) {
                        layers.add(layer);
                    }
                }
                MapLayer[] lst = layers.toArray(new MapLayer[layers.size()]);


                Map parameters = new HashMap();
                parameters.put("layers", lst);

                JDialog dialog = new JDialog();
                dialog.setTitle(tool.getTitle());

                WidgetTool wt = tool.createTool(parameters);

                wt.addWidgetToolListener(new WidgetToolListener() {

                    public void objectCreated(Object obj) {
                        if (obj instanceof DataStore) {
                            RandomStyleFactory rsf = new RandomStyleFactory();

                            DataStore store = (DataStore) obj;

                            try {
                                String name = store.getTypeNames()[0];
                                FeatureSource source = store.getFeatureSource(name);
                                MapLayer layer = new DefaultMapLayer(source, rsf.createRandomVectorStyle(source));

                                if (tree.getActiveContext() != null) {
                                    tree.getActiveContext().addLayer(layer);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }
                    }
                });

                dialog.setContentPane(wt.getComponent());
                dialog.pack();
                dialog.setLocationRelativeTo(null);
                dialog.setModal(true);
                dialog.setVisible(true);
            }
        });

        map.getRenderingStrategy().setContext(context);


    }

    private MapContext buildContext() {
        MapContext context = null;
        MapLayer layer;

        try {
            context = new DefaultMapContext(DefaultGeographicCRS.WGS84);
            DataStore store = DataStoreFinder.getDataStore(new SingletonMap("url",DemoAll.class.getResource("/org/geotools/gui/swing/demo/shape/test_polygon.shp")));
            FeatureSource fs = store.getFeatureSource(store.getTypeNames()[0]);
            Style style = RANDOM_STYLE_FACTORY.createRandomVectorStyle(fs);
            layer = new DefaultMapLayer(fs, style);
            layer.setTitle("demo_polygon.shp");
            context.addLayer(layer);

            store = DataStoreFinder.getDataStore(new SingletonMap("url",DemoAll.class.getResource("/org/geotools/gui/swing/demo/shape/test_ligne.shp")));
            fs = store.getFeatureSource(store.getTypeNames()[0]);
            style = RANDOM_STYLE_FACTORY.createRandomVectorStyle(fs);
            layer = new DefaultMapLayer(fs, style);
            layer.setTitle("demo_line.shp");
            context.addLayer(layer);

            store = DataStoreFinder.getDataStore(new SingletonMap("url",DemoAll.class.getResource("/org/geotools/gui/swing/demo/shape/test_point.shp")));
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

    private void initTree(JContextTree tree, Map2D map) {
        JContextTreePopup popup = tree.getPopupMenu();

        popup.addItem(new LayerVisibilityItem());           //layer         
        popup.addItem(new SeparatorItem());
        popup.addItem(new LayerZoomItem(null));              //layer
        popup.addItem(new LayerFeatureItem());              //layer
        popup.addItem(new ContextActiveItem(tree));         //context
        popup.addItem(new SeparatorItem());
        popup.addItem(new CutItem(tree));                   //all
        popup.addItem(new CopyItem(tree));                  //all
        popup.addItem(new PasteItem(tree));                 //all
        popup.addItem(new DuplicateItem(tree));             //all        
        popup.addItem(new SeparatorItem());
        popup.addItem(new DeleteItem(tree));                //all
        popup.addItem(new SeparatorItem());
        popup.addItem(new LayerPropertyItem());             //layer
        popup.addItem(new ContextPropertyItem());           //context

        popup.addItem(new RuleMinScaleItem());
        popup.addItem(new RuleMaxScaleItem());


        popup.setMap(map);
        if(map instanceof SelectableMap2D){
            colSelection.setMap( (SelectableMap2D)map);
        }
        

        tree.addColumn(colVisible);
        tree.addColumn(colOpacity);
        tree.addColumn(colStyle);
        tree.addColumn(colSelection);

        tree.addSubNodeGroup(subsource);
        tree.addSubNodeGroup(substyle);

        tree.revalidate();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        group_jdatachooser = new javax.swing.ButtonGroup();
        dia_about = new javax.swing.JDialog();
        jXImagePanel1 = new org.jdesktop.swingx.JXImagePanel();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList();
        bg_backlayer = new javax.swing.ButtonGroup();
        jSplitPane1 = new javax.swing.JSplitPane();
        jpanel8 = new javax.swing.JPanel();
        pan_mappane = new javax.swing.JPanel();
        gui_map2dinfo = new org.geotools.gui.swing.map.map2d.control.JMap2DInfoBar();
        jPanel4 = new javax.swing.JPanel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        tree = new org.geotools.gui.swing.contexttree.JContextTree();
        pantoolbox = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jToolBar1 = new javax.swing.JToolBar();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        gui_map2dcontrol = new org.geotools.gui.swing.map.map2d.control.JMap2DControlBar();
        gui_map2dedit = new org.geotools.gui.swing.map.map2d.control.JMap2DEditBar();
        jToolBar2 = new javax.swing.JToolBar();
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
        jMenuItem7 = new javax.swing.JMenuItem();
        guiChkVisible = new javax.swing.JCheckBoxMenuItem();
        guiChkOpacity = new javax.swing.JCheckBoxMenuItem();
        guiChkStyle = new javax.swing.JCheckBoxMenuItem();
        guiChkSelection = new javax.swing.JCheckBoxMenuItem();
        jSeparator2 = new javax.swing.JSeparator();
        jMenuItem9 = new javax.swing.JMenuItem();
        guiChkSubSource = new javax.swing.JCheckBoxMenuItem();
        guiChkSubStyle = new javax.swing.JCheckBoxMenuItem();
        jSeparator4 = new javax.swing.JSeparator();
        jMenuItem8 = new javax.swing.JMenuItem();
        jMenu7 = new javax.swing.JMenu();
        jCheckBoxMenuItem1 = new javax.swing.JCheckBoxMenuItem();
        jSeparator5 = new javax.swing.JSeparator();
        guiChkShapeCreateTool = new javax.swing.JCheckBoxMenuItem();
        guiChkVdem2CSV = new javax.swing.JCheckBoxMenuItem();
        guiChkSVG2MIF = new javax.swing.JCheckBoxMenuItem();
        jMenu6 = new javax.swing.JMenu();
        jMenuItem5 = new javax.swing.JMenuItem();
        jRadioButtonMenuItem3 = new javax.swing.JRadioButtonMenuItem();
        jRadioButtonMenuItem1 = new javax.swing.JRadioButtonMenuItem();
        jRadioButtonMenuItem2 = new javax.swing.JRadioButtonMenuItem();
        jSeparator1 = new javax.swing.JSeparator();
        jMenuItem6 = new javax.swing.JMenuItem();
        gui_Chknavigationlayer = new javax.swing.JCheckBoxMenuItem();
        gui_Chkminimaplayer = new javax.swing.JCheckBoxMenuItem();
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
        setTitle("Swing Widgets Demo");
        setIconImage(IconBundle.getResource().getIcon("about").getImage());

        jSplitPane1.setDividerLocation(300);

        jpanel8.setBackground(new java.awt.Color(102, 102, 102));
        jpanel8.setLayout(new java.awt.BorderLayout());

        pan_mappane.setOpaque(false);

        org.jdesktop.layout.GroupLayout pan_mappaneLayout = new org.jdesktop.layout.GroupLayout(pan_mappane);
        pan_mappane.setLayout(pan_mappaneLayout);
        pan_mappaneLayout.setHorizontalGroup(
            pan_mappaneLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 514, Short.MAX_VALUE)
        );
        pan_mappaneLayout.setVerticalGroup(
            pan_mappaneLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 456, Short.MAX_VALUE)
        );

        jpanel8.add(pan_mappane, java.awt.BorderLayout.CENTER);

        gui_map2dinfo.setFloatable(false);
        jpanel8.add(gui_map2dinfo, java.awt.BorderLayout.SOUTH);

        jSplitPane1.setRightComponent(jpanel8);

        jTabbedPane1.addTab("ContextTree", tree);

        pantoolbox.setLayout(new java.awt.BorderLayout());
        jTabbedPane1.addTab("ToolBox", pantoolbox);

        org.jdesktop.layout.GroupLayout jPanel4Layout = new org.jdesktop.layout.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jTabbedPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 271, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jTabbedPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 488, Short.MAX_VALUE)
        );

        jSplitPane1.setLeftComponent(jPanel4);

        getContentPane().add(jSplitPane1, java.awt.BorderLayout.CENTER);

        jPanel2.setLayout(new java.awt.GridBagLayout());

        jToolBar1.setFloatable(false);
        jToolBar1.setRollover(true);

        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/geotools/gui/swing/icon/defaultset/crystalproject/16x16/actions/folder_new.png"))); // NOI18N
        jButton1.setBorderPainted(false);
        jButton1.setContentAreaFilled(false);
        jButton1.setMargin(new java.awt.Insets(2, 2, 2, 2));
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton1);

        jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/geotools/gui/swing/icon/defaultset/crystalproject/16x16/actions/edit_add.png"))); // NOI18N
        jButton2.setBorderPainted(false);
        jButton2.setContentAreaFilled(false);
        jButton2.setMargin(new java.awt.Insets(2, 2, 2, 2));
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton2);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        jPanel2.add(jToolBar1, gridBagConstraints);

        gui_map2dcontrol.setFloatable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        jPanel2.add(gui_map2dcontrol, gridBagConstraints);

        gui_map2dedit.setFloatable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        jPanel2.add(gui_map2dedit, gridBagConstraints);

        jToolBar2.setFloatable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        jPanel2.add(jToolBar2, gridBagConstraints);

        getContentPane().add(jPanel2, java.awt.BorderLayout.PAGE_START);

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

        jMenuItem7.setText("Columns-----");
        jMenuItem7.setEnabled(false);
        jMenu5.add(jMenuItem7);

        guiChkVisible.setSelected(true);
        guiChkVisible.setText("Visible");
        guiChkVisible.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                guiChkVisibleActionPerformed(evt);
            }
        });
        jMenu5.add(guiChkVisible);

        guiChkOpacity.setSelected(true);
        guiChkOpacity.setText("Opacity");
        guiChkOpacity.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                guiChkOpacityActionPerformed(evt);
            }
        });
        jMenu5.add(guiChkOpacity);

        guiChkStyle.setSelected(true);
        guiChkStyle.setText("Style");
        guiChkStyle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                guiChkStyleActionPerformed(evt);
            }
        });
        jMenu5.add(guiChkStyle);

        guiChkSelection.setSelected(true);
        guiChkSelection.setText("Selection");
        guiChkSelection.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                guiChkSelectionActionPerformed(evt);
            }
        });
        jMenu5.add(guiChkSelection);
        jMenu5.add(jSeparator2);

        jMenuItem9.setText("SubNode Groups-----");
        jMenuItem9.setEnabled(false);
        jMenu5.add(jMenuItem9);

        guiChkSubSource.setSelected(true);
        guiChkSubSource.setText("Source");
        guiChkSubSource.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                guiChkSubSourceActionPerformed(evt);
            }
        });
        jMenu5.add(guiChkSubSource);

        guiChkSubStyle.setSelected(true);
        guiChkSubStyle.setText("Style");
        guiChkSubStyle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                guiChkSubStyleActionPerformed(evt);
            }
        });
        jMenu5.add(guiChkSubStyle);
        jMenu5.add(jSeparator4);

        jMenuItem8.setText("Popup Items-----");
        jMenuItem8.setEnabled(false);
        jMenu5.add(jMenuItem8);

        jMenu4.add(jMenu5);

        jMenu7.setText("Tool Tree");

        jCheckBoxMenuItem1.setSelected(true);
        jCheckBoxMenuItem1.setText("Clip");
        jCheckBoxMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxMenuItem1ActionPerformed(evt);
            }
        });
        jMenu7.add(jCheckBoxMenuItem1);
        jMenu7.add(jSeparator5);

        guiChkShapeCreateTool.setSelected(true);
        guiChkShapeCreateTool.setText("Shapefile creation");
        guiChkShapeCreateTool.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                guiChkShapeCreateToolActionPerformed(evt);
            }
        });
        jMenu7.add(guiChkShapeCreateTool);

        guiChkVdem2CSV.setSelected(true);
        guiChkVdem2CSV.setText("VDem > CSV");
        guiChkVdem2CSV.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                guiChkVdem2CSVActionPerformed(evt);
            }
        });
        jMenu7.add(guiChkVdem2CSV);

        guiChkSVG2MIF.setSelected(true);
        guiChkSVG2MIF.setText("SVG > MIF");
        guiChkSVG2MIF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                guiChkSVG2MIFActionPerformed(evt);
            }
        });
        jMenu7.add(guiChkSVG2MIF);

        jMenu4.add(jMenu7);

        jMenu6.setText("Map2D");

        jMenuItem5.setText("BackDecoration-----");
        jMenuItem5.setEnabled(false);
        jMenu6.add(jMenuItem5);

        bg_backlayer.add(jRadioButtonMenuItem3);
        jRadioButtonMenuItem3.setText("None");
        jRadioButtonMenuItem3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonMenuItem3ActionPerformed(evt);
            }
        });
        jMenu6.add(jRadioButtonMenuItem3);

        bg_backlayer.add(jRadioButtonMenuItem1);
        jRadioButtonMenuItem1.setSelected(true);
        jRadioButtonMenuItem1.setText("Color");
        jRadioButtonMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonMenuItem1ActionPerformed(evt);
            }
        });
        jMenu6.add(jRadioButtonMenuItem1);

        bg_backlayer.add(jRadioButtonMenuItem2);
        jRadioButtonMenuItem2.setText("Image");
        jRadioButtonMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonMenuItem2ActionPerformed(evt);
            }
        });
        jMenu6.add(jRadioButtonMenuItem2);
        jMenu6.add(jSeparator1);

        jMenuItem6.setText("Decorations-----");
        jMenuItem6.setEnabled(false);
        jMenu6.add(jMenuItem6);

        gui_Chknavigationlayer.setSelected(true);
        gui_Chknavigationlayer.setText("Navigation");
        gui_Chknavigationlayer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gui_ChknavigationlayerActionPerformed(evt);
            }
        });
        jMenu6.add(gui_Chknavigationlayer);

        gui_Chkminimaplayer.setText("Minimap");
        gui_Chkminimaplayer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gui_ChkminimaplayerActionPerformed(evt);
            }
        });
        jMenu6.add(gui_Chkminimaplayer);

        jMenu4.add(jMenu6);

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

            JDataChooser jdc = new JDataChooser(null, lst);

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

        if (guiChkVisible.isSelected()) {
            tree.addColumn(colVisible);
        } else {
            tree.removeColumn(colVisible);
        }
}//GEN-LAST:event_guiChkVisibleActionPerformed

    private void guiChkOpacityActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_guiChkOpacityActionPerformed
        if (guiChkOpacity.isSelected()) {
            tree.addColumn(colOpacity);
        } else {
            tree.removeColumn(colOpacity);
        }
}//GEN-LAST:event_guiChkOpacityActionPerformed

    private void guiChkStyleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_guiChkStyleActionPerformed
        if (guiChkStyle.isSelected()) {
            tree.addColumn(colStyle);
        } else {
            tree.removeColumn(colStyle);
        }
}//GEN-LAST:event_guiChkStyleActionPerformed

    private void guiChkSelectionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_guiChkSelectionActionPerformed
        if (guiChkSelection.isSelected()) {
            tree.addColumn(colSelection);
        } else {
            tree.removeColumn(colSelection);
        }
    }//GEN-LAST:event_guiChkSelectionActionPerformed

    private void jRadioButtonMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonMenuItem2ActionPerformed
        map.setBackDecoration(overBackImage);
    }//GEN-LAST:event_jRadioButtonMenuItem2ActionPerformed

    private void jRadioButtonMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonMenuItem1ActionPerformed
        map.setBackDecoration(overBackColor);
    }//GEN-LAST:event_jRadioButtonMenuItem1ActionPerformed

    private void jRadioButtonMenuItem3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonMenuItem3ActionPerformed
        map.setBackDecoration(null);
    }//GEN-LAST:event_jRadioButtonMenuItem3ActionPerformed

    private void gui_ChknavigationlayerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gui_ChknavigationlayerActionPerformed
        if (gui_Chknavigationlayer.isSelected()) {
            map.addDecoration(overNavigation);
        } else {
            map.removeDecoration(overNavigation);
        }
}//GEN-LAST:event_gui_ChknavigationlayerActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        DefaultMapContext context;
        context = new DefaultMapContext(DefaultGeographicCRS.WGS84);
        context.setTitle("Context " + nb);
        tree.addContext(context);
        nb++;
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed


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

            lst.add(new JWFSDataPanel());
            lst.add(new JWMSDataPanel());

            JDataChooser jdc = new JDataChooser(null, lst);

            JDataChooser.ACTION ret = jdc.showDialog();

            if (ret == JDataChooser.ACTION.APPROVE) {
                List<MapLayer> layers = jdc.getLayers();
                for (MapLayer layer : layers) {
                    tree.getActiveContext().addLayer(layer);
                }
            }

        }
    }//GEN-LAST:event_jButton2ActionPerformed

    private void gui_ChkminimaplayerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gui_ChkminimaplayerActionPerformed
        if (gui_Chkminimaplayer.isSelected()) {
            map.addDecoration(overMiniMap);
        } else {
            map.removeDecoration(overMiniMap);
        }
}//GEN-LAST:event_gui_ChkminimaplayerActionPerformed

    private void guiChkSubStyleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_guiChkSubStyleActionPerformed
        if (guiChkSubStyle.isSelected()) {
            tree.addSubNodeGroup(substyle);
        } else {
            tree.removeSubNodeGroup(substyle);
        }
    }//GEN-LAST:event_guiChkSubStyleActionPerformed

    private void guiChkSubSourceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_guiChkSubSourceActionPerformed
        if (guiChkSubSource.isSelected()) {
            tree.addSubNodeGroup(subsource);
        } else {
            tree.removeSubNodeGroup(subsource);
        }
}//GEN-LAST:event_guiChkSubSourceActionPerformed

    private void guiChkSVG2MIFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_guiChkSVG2MIFActionPerformed
        if (guiChkSVG2MIF.isSelected()) {
            tooltree.addTool(svg2mifTool);
        } else {
            tooltree.removeTool(svg2mifTool);
        }        
    }//GEN-LAST:event_guiChkSVG2MIFActionPerformed

    private void guiChkVdem2CSVActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_guiChkVdem2CSVActionPerformed
        if (guiChkVdem2CSV.isSelected()) {
            tooltree.addTool(vdem2csvTool);
        } else {
            tooltree.removeTool(vdem2csvTool);
        }  
    }//GEN-LAST:event_guiChkVdem2CSVActionPerformed

    private void guiChkShapeCreateToolActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_guiChkShapeCreateToolActionPerformed
        if (guiChkShapeCreateTool.isSelected()) {
            tooltree.addTool(shapeTool);
        } else {
            tooltree.removeTool(shapeTool);
        }  
    }//GEN-LAST:event_guiChkShapeCreateToolActionPerformed

    private void jCheckBoxMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxMenuItem1ActionPerformed
        if (jCheckBoxMenuItem1.isSelected()) {
            tooltree.addTool(clipTool);
        } else {
            tooltree.removeTool(clipTool);
        }  
    }//GEN-LAST:event_jCheckBoxMenuItem1ActionPerformed

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
    private javax.swing.ButtonGroup bg_backlayer;
    private javax.swing.JCheckBoxMenuItem chk_database;
    private javax.swing.JCheckBoxMenuItem chk_file;
    private javax.swing.JCheckBoxMenuItem chk_server;
    private javax.swing.JDialog dia_about;
    private javax.swing.ButtonGroup group_jdatachooser;
    private javax.swing.JCheckBoxMenuItem guiChkOpacity;
    private javax.swing.JCheckBoxMenuItem guiChkSVG2MIF;
    private javax.swing.JCheckBoxMenuItem guiChkSelection;
    private javax.swing.JCheckBoxMenuItem guiChkShapeCreateTool;
    private javax.swing.JCheckBoxMenuItem guiChkStyle;
    private javax.swing.JCheckBoxMenuItem guiChkSubSource;
    private javax.swing.JCheckBoxMenuItem guiChkSubStyle;
    private javax.swing.JCheckBoxMenuItem guiChkVdem2CSV;
    private javax.swing.JCheckBoxMenuItem guiChkVisible;
    private javax.swing.JCheckBoxMenuItem gui_Chkminimaplayer;
    private javax.swing.JCheckBoxMenuItem gui_Chknavigationlayer;
    private org.geotools.gui.swing.map.map2d.control.JMap2DControlBar gui_map2dcontrol;
    private org.geotools.gui.swing.map.map2d.control.JMap2DEditBar gui_map2dedit;
    private org.geotools.gui.swing.map.map2d.control.JMap2DInfoBar gui_map2dinfo;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JCheckBoxMenuItem jCheckBoxMenuItem1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JList jList1;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenu jMenu4;
    private javax.swing.JMenu jMenu5;
    private javax.swing.JMenu jMenu6;
    private javax.swing.JMenu jMenu7;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JMenuItem jMenuItem4;
    private javax.swing.JMenuItem jMenuItem5;
    private javax.swing.JMenuItem jMenuItem6;
    private javax.swing.JMenuItem jMenuItem7;
    private javax.swing.JMenuItem jMenuItem8;
    private javax.swing.JMenuItem jMenuItem9;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JRadioButtonMenuItem jRadioButtonMenuItem1;
    private javax.swing.JRadioButtonMenuItem jRadioButtonMenuItem2;
    private javax.swing.JRadioButtonMenuItem jRadioButtonMenuItem3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JSeparator jSeparator5;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JToolBar jToolBar2;
    private org.jdesktop.swingx.JXImagePanel jXImagePanel1;
    private javax.swing.JPanel jpanel8;
    private javax.swing.JPanel pan_mappane;
    private javax.swing.JPanel pantoolbox;
    private org.geotools.gui.swing.contexttree.JContextTree tree;
    // End of variables declaration//GEN-END:variables
}
