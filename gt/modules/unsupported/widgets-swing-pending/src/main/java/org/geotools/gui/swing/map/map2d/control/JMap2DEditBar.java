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

import com.vividsolutions.jts.geom.Envelope;
import java.awt.Dimension;
import java.awt.FlowLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.border.EmptyBorder;

import org.geotools.gui.swing.icon.IconBundle;
import org.geotools.gui.swing.map.Map;
import org.geotools.gui.swing.map.MapConstants;
import org.geotools.gui.swing.map.map2d.EditableMap2D;
import org.geotools.gui.swing.map.map2d.event.Map2DActionStateEvent;
import org.geotools.gui.swing.map.map2d.event.Map2DContextEvent;
import org.geotools.gui.swing.map.map2d.listener.Map2DListener;
import org.geotools.gui.swing.map.map2d.event.Map2DMapAreaEvent;
import org.geotools.gui.swing.map.map2d.listener.NavigableMap2DListener;
import org.geotools.map.MapContext;
import org.geotools.map.MapLayer;
import org.geotools.map.event.MapLayerListEvent;
import org.geotools.map.event.MapLayerListListener;

/**
 * @author johann sorel
 */
public class JMap2DEditBar extends JPanel implements Map2DListener, NavigableMap2DListener {

    private final ContextListener contextListener = new ContextListener();
    private EditableMap2D map = null;
    private MapContext editionContext = null;
    private MapLayer editionLayer = null;
    private final JComboBox gui_layers = new JComboBox();
    private final JToggleButton gui_edit = buildToggleButton(IconBundle.getResource().getIcon("16_edit"));

    /**
     * Creates a new instance of DefaultLightMapPaneToolBar
     */
    public JMap2DEditBar() {
        this(null);
    }

    public JMap2DEditBar(Map pane) {
        super(new FlowLayout(FlowLayout.LEFT, 0, 0));
        getInsets().set(0, 0, 0, 0);
        setMap(pane);
        init();
    }

    private void init() {
        add(gui_edit);
        add(gui_layers);

        gui_layers.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                int selected = gui_layers.getSelectedIndex();

                if (selected >= 0) {
                    editionLayer = editionContext.getLayer(selected);

                    if (map != null) {
                        map.setEditedMapLayer(editionLayer);
                    }

                } else {
                    editionLayer = null;
                }

            }
        });

        gui_edit.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (map != null) {
                    map.setActionState(MapConstants.ACTION_STATE.EDIT);
                    map.setEditedMapLayer(editionLayer);
                }
            }
        });


    }

    private void initComboBox() {
        gui_layers.removeAllItems();

        if (editionContext != null) {
            gui_layers.setEnabled(true);
            MapLayer[] layers = editionContext.getLayers();

            for (MapLayer layer : layers) {
                gui_layers.addItem(layer.getTitle());
            }

        } else {
            gui_layers.setEnabled(false);
        }
    }
    private final int largeur = 2;

    private JButton buildButton(ImageIcon img) {
        JButton but = new JButton(img);
        but.setBorder(new EmptyBorder(largeur, largeur, largeur, largeur));
        but.setBorderPainted(false);
        but.setContentAreaFilled(false);
        but.setPreferredSize(new Dimension(25, 25));
        but.setOpaque(false);
        return but;
    }

    private JToggleButton buildToggleButton(ImageIcon img) {
        JToggleButton but = new JToggleButton(img);
        but.setBorder(new EmptyBorder(largeur, largeur, largeur, largeur));
        but.setPreferredSize(new Dimension(25, 25));
        but.setBorderPainted(true);
        //but.setContentAreaFilled(false);
        //but.setOpaque(false);
        return but;
    }

    public void setMap(Map pane) {

        if (map != null) {
            map.removeMap2DListener(this);
            editionContext = map.getContext();
            
            map.removeNavigableMap2DListener(this);
            
        }

        if (pane instanceof EditableMap2D) {
            map = (EditableMap2D) pane;
            map.addMap2DListener(this);
            map.addNavigableMap2DListener(this);
            gui_layers.setEnabled(true);


        } else {
            map = null;
            editionContext = null;
            gui_layers.setEnabled(false);
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
}
