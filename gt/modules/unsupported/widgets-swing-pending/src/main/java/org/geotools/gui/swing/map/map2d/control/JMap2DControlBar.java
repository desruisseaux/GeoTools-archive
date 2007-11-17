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

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.border.EmptyBorder;

import org.geotools.gui.swing.icon.IconBundle;
import org.geotools.gui.swing.map.Map;
import org.geotools.gui.swing.map.MapConstants;
import org.geotools.gui.swing.map.map2d.NavigableMap2D;
import org.geotools.gui.swing.map.map2d.SelectableMap2D;

/**
 * @author johann sorel
 */
public class JMap2DControlBar extends JPanel {

    private NavigableMap2D navigationMap;
    private SelectableMap2D selectionMap;
    private final JToggleButton zoomin = buildToggleButton(IconBundle.getResource().getIcon("16_zoom_in"));
    private final JToggleButton zoomout = buildToggleButton(IconBundle.getResource().getIcon("16_zoom_out"));
    private final JToggleButton zoompan = buildToggleButton(IconBundle.getResource().getIcon("16_zoom_pan"));
    private final JToggleButton zoomall = buildToggleButton(IconBundle.getResource().getIcon("16_zoom_all"));
    private final JToggleButton select = buildToggleButton(IconBundle.getResource().getIcon("16_select"));

    /**
     * Creates a new instance of DefaultLightMapPaneToolBar
     */
    public JMap2DControlBar() {
        this(null);
    }

    public JMap2DControlBar(Map pane) {
        super(new FlowLayout(FlowLayout.LEFT));
        setMap(pane);
        init();
    }

    private void init() {
        
        ButtonGroup bg = new ButtonGroup();
        bg.add(zoomin);
        bg.add(zoomout);
        bg.add(zoompan);
        bg.add(zoomall);
        bg.add(select);
        
        
        zoomin.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent e) {
                        if (navigationMap != null) {
                            navigationMap.setActionState(MapConstants.ACTION_STATE.ZOOM_IN);
                        }
                    }
                });

        zoomout.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent e) {
                        if (navigationMap != null) {
                            navigationMap.setActionState(MapConstants.ACTION_STATE.ZOOM_OUT);
                        }
                    }
                });

        zoompan.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent e) {
                        if (navigationMap != null) {
                            navigationMap.setActionState(MapConstants.ACTION_STATE.PAN);
                        }
                    }
                });

        zoomall.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent e) {
                        if (navigationMap != null) {

                            try {
                                navigationMap.setMapArea(navigationMap.getContext().getLayerBounds());
                                navigationMap.refresh();
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }

                        }
                    }
                });

        select.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent e) {
                        selectionMap.setActionState(MapConstants.ACTION_STATE.SELECT);
                    }
                });

        add(zoomall);
        add(zoomin);
        add(zoomout);
        add(zoompan);
        add(select);
    }

    private JToggleButton buildToggleButton(ImageIcon img) {
        JToggleButton but = new JToggleButton(img);
        but.setBorder(new EmptyBorder(2, 2, 2, 2));
       
        //but.setBorderPainted(false);
        //but.setContentAreaFilled(false);
        //but.setOpaque(false);
        return but;
    }

    public void setMap(Map pane) {
        if (pane instanceof NavigableMap2D) {
            this.navigationMap = (NavigableMap2D) pane;
            zoomall.setEnabled(true);
            zoomin.setEnabled(true);
            zoomout.setEnabled(true);
            zoompan.setEnabled(true);
            
            switch(navigationMap.getActionState()){
                case ZOOM_IN :
                    zoomin.setSelected(true);
                    break;
                case ZOOM_OUT :
                    zoomall.setSelected(true);
                    break;
                case PAN :
                    zoompan.setSelected(true);
                    break;
                case SELECT :
                    select.setSelected(true);
                    break;
                case EDIT :
                    break;
            }
            

            if (pane instanceof SelectableMap2D) {
                this.selectionMap = (SelectableMap2D) pane;
                select.setEnabled(true);
            } else {
                select.setEnabled(false);
            }

        } else {
            zoomall.setEnabled(false);
            zoomin.setEnabled(false);
            zoomout.setEnabled(false);
            zoompan.setEnabled(false);
            select.setEnabled(false);
        }

    }
}
