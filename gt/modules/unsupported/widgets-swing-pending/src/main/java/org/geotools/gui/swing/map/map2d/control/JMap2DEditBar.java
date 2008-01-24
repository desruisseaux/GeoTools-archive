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
package org.geotools.gui.swing.map.map2d.control;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.Polygon;
import java.awt.Component;
import java.awt.FlowLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.border.EmptyBorder;

import org.geotools.gui.swing.icon.IconBundle;
import org.geotools.gui.swing.map.MapConstants;
import org.geotools.gui.swing.map.map2d.EditableMap2D;
import org.geotools.gui.swing.map.map2d.Map2D;
import org.geotools.gui.swing.map.map2d.event.Map2DActionStateEvent;
import org.geotools.gui.swing.map.map2d.event.Map2DContextEvent;
import org.geotools.gui.swing.map.map2d.event.Map2DEditLayerEvent;
import org.geotools.gui.swing.map.map2d.event.Map2DEditStateEvent;
import org.geotools.gui.swing.map.map2d.listener.EditableMap2DListener;
import org.geotools.gui.swing.map.map2d.listener.Map2DListener;
import org.geotools.gui.swing.map.map2d.event.Map2DMapAreaEvent;
import org.geotools.gui.swing.map.map2d.listener.NavigableMap2DListener;
import org.geotools.gui.swing.map.map2d.listener.StrategyListener;
import org.geotools.gui.swing.map.map2d.strategy.RenderingStrategy;
import org.geotools.map.MapContext;
import org.geotools.map.MapLayer;
import org.geotools.map.event.MapLayerListEvent;
import org.geotools.map.event.MapLayerListListener;

/**
 * JMap2DEditBar is a JPanel to handle edition state for an EditableMap2D
 * Layer selection, edition, line, polygon, point ...
 * 
 * @author johann sorel
 */
public class JMap2DEditBar extends JPanel implements Map2DListener,StrategyListener, NavigableMap2DListener, EditableMap2DListener {

    private final ContextListener contextListener = new ContextListener();
    private EditableMap2D map = null;
    private MapContext editionContext = null;
    private MapLayer editionLayer = null;
    private final int largeur = 4;
    private final JComboBox gui_layers = new JComboBox();
    private final JToggleButton gui_edit = buildToggleButton(IconBundle.getResource().getIcon("16_edit"));
    private final JButton gui_editing = buildButton(null);
    private final JButton gui_config = buildButton(IconBundle.getResource().getIcon("16_map2d_optimize"));
    private final JToggleButton gui_geom = buildToggleButton(IconBundle.getResource().getIcon("16_edit_geom"));
    private final JToggleButton gui_s_point = buildToggleButton(IconBundle.getResource().getIcon("16_edit_single_point"));
    private final JToggleButton gui_m_point = buildToggleButton(IconBundle.getResource().getIcon("16_edit_multi_point"));
    private final JToggleButton gui_s_line = buildToggleButton(IconBundle.getResource().getIcon("16_edit_single_line"));
    private final JToggleButton gui_m_line = buildToggleButton(IconBundle.getResource().getIcon("16_edit_multi_line"));
    private final JToggleButton gui_s_poly = buildToggleButton(IconBundle.getResource().getIcon("16_edit_single_polygon"));
    private final JToggleButton gui_m_poly = buildToggleButton(IconBundle.getResource().getIcon("16_edit_multi_polygon"));
    private final JToggleButton gui_none = new JToggleButton();
    private boolean isEditing = false;
    
    private ItemListener listen = new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                int selected = gui_layers.getSelectedIndex();

                if (selected >= 1) {
                    editionLayer = editionContext.getLayer(selected - 1);

                    if (map != null) {
                        map.setEditedMapLayer(editionLayer);
                        map.setEditState(MapConstants.EDIT_STATE.NONE);
                        lock(true);
                    }
                    

                } else {
                    map.setEditState(MapConstants.EDIT_STATE.NONE);
                    map.setEditedMapLayer(null);
                    editionLayer = null;
                }

            }
        };
    

    /**
     * Creates a new instance of JMap2DEditBar
     */
    public JMap2DEditBar() {
        this(null);
    }

    /**
     * Creates a new instance of JMap2DEditBar
     * @param pane : related Map2D or null
     */
    public JMap2DEditBar(Map2D pane) {
        super(new FlowLayout(FlowLayout.LEFT, 0, 0));
        getInsets().set(0, 0, 0, 0);
        setMap(pane);
        init();
        lock(true);
    }

    private void init() {

        gui_editing.setText("       ");
        gui_editing.setEnabled(false);
        gui_editing.setBorder(new EmptyBorder(largeur, largeur, largeur, largeur));

        ButtonGroup bg = new ButtonGroup();

        bg.add(gui_geom);
        bg.add(gui_s_point);
        bg.add(gui_m_point);
        bg.add(gui_s_line);
        bg.add(gui_m_line);
        bg.add(gui_s_poly);
        bg.add(gui_m_poly);
        bg.add(gui_none);

        add(gui_config);
        add(gui_edit);
        
        add(gui_layers);
        add(gui_editing);
        add(gui_geom);
        add(gui_s_point);
        add(gui_m_point);
        add(gui_s_line);
        add(gui_m_line);
        add(gui_s_poly);
        add(gui_m_poly);


        gui_layers.setRenderer(new listRenderer());

        gui_edit.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                if (gui_edit.isSelected()) {
                    if (map != null) {
                        map.setActionState(MapConstants.ACTION_STATE.EDIT);
                        map.setEditedMapLayer(editionLayer);
                    }

                } else {
                    if (map != null) {
                        map.setActionState(MapConstants.ACTION_STATE.NONE);
                        map.setEditedMapLayer(null);
                    }
                }


            }
        });

        gui_geom.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (map != null) {
                    map.setEditState(MapConstants.EDIT_STATE.EDIT);
                }
            }
        });

        gui_s_point.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (map != null) {
                    map.setEditState(MapConstants.EDIT_STATE.POINT);
                }
            }
        });

        gui_m_point.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (map != null) {
                    map.setEditState(MapConstants.EDIT_STATE.MULTI_POINT);
                }
            }
        });

        gui_s_line.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (map != null) {
                    map.setEditState(MapConstants.EDIT_STATE.LINE);
                }
            }
        });

        gui_m_line.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (map != null) {
                    map.setEditState(MapConstants.EDIT_STATE.MULTI_LINE);
                }
            }
        });

        gui_s_poly.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (map != null) {
                    map.setEditState(MapConstants.EDIT_STATE.POLYGON);
                }
            }
        });

        gui_m_poly.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (map != null) {
                    map.setEditState(MapConstants.EDIT_STATE.MULTI_POLYGON);
                }
            }
        });


    }

    private void initComboBox() {
        gui_layers.removeItemListener(listen);
        
        gui_layers.removeAllItems();

        gui_layers.addItem("-");

        if (editionContext != null) {
            gui_layers.setEnabled(true);
            MapLayer[] layers = editionContext.getLayers();

            for (MapLayer layer : layers) {
                gui_layers.addItem(layer);
            }

        } else {
            gui_layers.setEnabled(false);
        }
        
        if(editionLayer != null){
            gui_layers.setSelectedItem(editionLayer);
        }
        
        gui_layers.addItemListener(listen);
    }

    private void lock(boolean lock) {

        gui_geom.setEnabled(false);
        gui_m_line.setEnabled(false);
        gui_m_point.setEnabled(false);
        gui_m_poly.setEnabled(false);
        gui_s_line.setEnabled(false);
        gui_s_point.setEnabled(false);
        gui_s_poly.setEnabled(false);
        
        if (editionLayer != null && lock) {

            Class jtsClass = null;

            jtsClass = editionLayer.getFeatureSource().getSchema().getDefaultGeometry().getType().getBinding();

            if (jtsClass != null) {

                if (jtsClass.equals(Point.class)) {
                    gui_geom.setEnabled(true);
                    gui_s_point.setEnabled(true);
                } else if (jtsClass.equals(MultiPoint.class)) {
                    gui_geom.setEnabled(true);
                    gui_m_point.setEnabled(true);
                } else if (jtsClass.equals(LineString.class)) {
                    gui_geom.setEnabled(true);
                    gui_s_line.setEnabled(true);
                } else if (jtsClass.equals(MultiLineString.class)) {
                    gui_geom.setEnabled(true);
                    gui_m_line.setEnabled(true);
                } else if (jtsClass.equals(Polygon.class)) {
                    gui_geom.setEnabled(true);
                    gui_s_poly.setEnabled(true);
                } else if (jtsClass.equals(MultiPolygon.class)) {
                    gui_geom.setEnabled(true);
                    gui_m_poly.setEnabled(true);
                } else{
                    gui_geom.setEnabled(true);
                    gui_s_point.setEnabled(true);
                    gui_m_point.setEnabled(true);
                    gui_s_line.setEnabled(true);
                    gui_m_line.setEnabled(true);
                    gui_s_poly.setEnabled(true);
                    gui_m_poly.setEnabled(true);
                }

            }
        }
    }

    private JButton buildButton(ImageIcon img) {
        JButton but = new JButton(img);
        but.setBorder(new EmptyBorder(largeur, largeur, largeur, largeur));
        but.setBorderPainted(false);
        but.setContentAreaFilled(false);
        but.setOpaque(false);
        return but;
    }

    private JToggleButton buildToggleButton(ImageIcon img) {
        JToggleButton but = new JToggleButton(img);
        but.setBorder(new EmptyBorder(largeur, largeur, largeur, largeur));

        but.setBorderPainted(true);
        //but.setContentAreaFilled(false);
        //but.setOpaque(false);
        return but;
    }

    /**
     * set the related Map2D
     * @param map2d : related Map2D
     */
    public void setMap(Map2D map2d) {

        if (map != null) {
            map.removeMap2DListener(this);
            map.removeNavigableMap2DListener(this);
            map.removeEditableMap2DListener(this);
        }

        if (map2d instanceof EditableMap2D) {
            map = (EditableMap2D) map2d;
            editionContext = map.getRenderingStrategy().getContext();
            editionLayer = map.getEditedMapLayer();
            map.addMap2DListener(this);
            map.addNavigableMap2DListener(this);
            map.addEditableMap2DListener(this);
            gui_layers.setEnabled(true);

        } else {
            map = null;
            editionContext = null;
            editionLayer = null;
            gui_layers.setEnabled(false);
            lock(false);
        }

        initComboBox();
    }

    //-----------------------Map2DListener--------------------------------------
    public void mapAreaChanged(Map2DMapAreaEvent event) {

    }

    public void mapContextChanged(Map2DContextEvent event) {

        if (editionContext != null) {
            editionContext.removeMapLayerListListener(contextListener);
        }

        editionContext = event.getNewContext();

        if (editionContext != null) {
            editionContext.addMapLayerListListener(contextListener);
        }

        initComboBox();
    }

    //----------------------NavigableMap2DListener------------------------------
    public void mapActionStateChanged(Map2DActionStateEvent event) {
        switch (event.getNewState()) {
            case EDIT:
                gui_edit.setSelected(true);
                break;
            default:
                gui_edit.setSelected(false);
                break;
        }
    }

    //----------------------EditableMap2DListener-------------------------------
    public void mapEditStateChanged(Map2DEditStateEvent event) {
        switch (event.getNewState()) {
            case EDIT:
                gui_geom.setSelected(true);
                break;
            case POINT:
                gui_s_point.setSelected(true);
                break;
            case MULTI_POINT:
                gui_m_point.setSelected(true);
                break;
            case LINE:
                gui_s_line.setSelected(true);
                break;
            case MULTI_LINE:
                gui_m_line.setSelected(true);
                break;
            case POLYGON:
                gui_s_poly.setSelected(true);
                break;
            case MULTI_POLYGON:
                gui_m_poly.setSelected(true);
                break;
            default:
                gui_none.setSelected(true);
                break;
        }
    }

    public void mapEditLayerChanged(Map2DEditLayerEvent event) {

        MapLayer layer = event.getNewEditLayer();

        lock(false);

        if (layer != null && !editionLayer.equals(layer)) {

            editionLayer = layer;

            gui_layers.setSelectedItem(layer);

            if (isEditing) {
                lock(true);
            }


        }
    }

    //----------------private classes-------------------------------------------
    private class ContextListener implements MapLayerListListener {

        public void layerAdded(MapLayerListEvent event) {
            initComboBox();
        }

        public void layerRemoved(MapLayerListEvent event) {
            initComboBox();
        }

        public void layerChanged(MapLayerListEvent event) {
            initComboBox();
        }

        public void layerMoved(MapLayerListEvent event) {
            initComboBox();
        }
    }

    private class listRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if (value instanceof MapLayer) {
                value = ((MapLayer) value).getTitle();
            }

            return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        }
    }

    public void mapStrategyChanged(RenderingStrategy oldStrategy, RenderingStrategy newStrategy) {
        oldStrategy.removeStrategyListener(this);
        newStrategy.addStrategyListener(this);
    }

    public void setRendering(boolean rendering) {
    }
}
