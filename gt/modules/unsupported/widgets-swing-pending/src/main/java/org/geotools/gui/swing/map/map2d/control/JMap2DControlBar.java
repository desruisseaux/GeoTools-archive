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

import java.util.ArrayList;
import java.util.List;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.border.EmptyBorder;

import org.geotools.gui.swing.icon.IconBundle;
import org.geotools.gui.swing.map.Map;
import org.geotools.gui.swing.map.MapConstants;
import org.geotools.gui.swing.map.map2d.Map2D;
import org.geotools.gui.swing.map.map2d.NavigableMap2D;
import org.geotools.gui.swing.map.map2d.SelectableMap2D;
import org.geotools.gui.swing.map.map2d.event.Map2DActionStateEvent;
import org.geotools.gui.swing.map.map2d.event.Map2DContextEvent;
import org.geotools.gui.swing.map.map2d.listener.Map2DListener;
import org.geotools.gui.swing.map.map2d.event.Map2DMapAreaEvent;
import org.geotools.gui.swing.map.map2d.listener.NavigableMap2DListener;

/**
 * JMap2DControlBar is a JPanel to handle Navigation state for a NavigableMap2D
 * ZoomIn/Out, pan, selection, refresh ...
 * 
 * @author johann sorel
 */
public class JMap2DControlBar extends JPanel implements Map2DListener, NavigableMap2DListener {

    private final List<Envelope> mapAreas = new ArrayList<Envelope>();
    private Envelope lastMapArea = null;
    private Map2D map = null;
    private final JButton gui_zoomAll = buildButton(IconBundle.getResource().getIcon("16_zoom_all"));
    private final JButton gui_nextArea = buildButton(IconBundle.getResource().getIcon("16_next_maparea"));
    private final JButton gui_previousArea = buildButton(IconBundle.getResource().getIcon("16_previous_maparea"));
    private final JToggleButton gui_zoomIn = buildToggleButton(IconBundle.getResource().getIcon("16_zoom_in"));
    private final JToggleButton gui_zoomOut = buildToggleButton(IconBundle.getResource().getIcon("16_zoom_out"));
    private final JToggleButton gui_zoomPan = buildToggleButton(IconBundle.getResource().getIcon("16_zoom_pan"));
    private final JToggleButton gui_select = buildToggleButton(IconBundle.getResource().getIcon("16_select"));
    private final JToggleButton gui_other = new JToggleButton();
    private final JButton gui_refresh = buildButton(IconBundle.getResource().getIcon("16_data_reload"));
    private final int largeur = 2;

    /**
     * Creates a new instance of JMap2DControlBar
     */
    public JMap2DControlBar() {
        this(null);
    }

    /**
     * Creates a new instance of JMap2DControlBar
     * @param pane : related Map2D or null
     */
    public JMap2DControlBar(Map2D pane) {
        super(new FlowLayout(FlowLayout.LEFT, 0, 0));
        getInsets().set(0, 0, 0, 0);
        setMap(pane);
        init();
    }

    private void init() {

        ButtonGroup bg = new ButtonGroup();
        bg.add(gui_zoomIn);
        bg.add(gui_zoomOut);
        bg.add(gui_zoomPan);
        bg.add(gui_select);
        bg.add(gui_other);

        gui_zoomAll.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (map != null) {

                    try {
                        map.setMapArea(map.getContext().getLayerBounds());
                        map.refresh();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                }
            }
        });

        gui_nextArea.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (lastMapArea != null) {
                    int index = mapAreas.indexOf(lastMapArea);

                    index++;
                    if (index < mapAreas.size()) {
                        map.setMapArea(mapAreas.get(index));
                        map.refresh();
                    }
                    if (index == mapAreas.size() - 1) {
                        gui_nextArea.setEnabled(false);
                    }
                }
            }
        });

        gui_previousArea.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                if (lastMapArea != null) {
                    int index = mapAreas.indexOf(lastMapArea);

                    index--;
                    if (index >= 0) {
                        map.setMapArea(mapAreas.get(index));
                        map.refresh();
                    }
                    if (index == 0) {
                        gui_previousArea.setEnabled(false);
                    }
                }
            }
        });


        gui_zoomIn.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (map != null) {
                    ((NavigableMap2D) map).setActionState(MapConstants.ACTION_STATE.ZOOM_IN);
                }
            }
        });

        gui_zoomOut.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (map != null) {
                    ((NavigableMap2D) map).setActionState(MapConstants.ACTION_STATE.ZOOM_OUT);
                }
            }
        });

        gui_zoomPan.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (map != null) {
                    ((NavigableMap2D) map).setActionState(MapConstants.ACTION_STATE.PAN);
                }
            }
        });

        gui_select.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (map != null) {
                    ((SelectableMap2D) map).setActionState(MapConstants.ACTION_STATE.SELECT);
                }
            }
        });

        gui_refresh.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (map != null) {
                    map.refresh();
                }
            }
        });


        add(gui_zoomAll);
        add(gui_refresh);
        add(gui_previousArea);
        add(gui_nextArea);
        add(gui_zoomIn);
        add(gui_zoomOut);
        add(gui_zoomPan);
        add(gui_select);

    }
    
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

    /**
     * set the related Map2D
     * @param map2d : related Map2D
     */
    public void setMap(Map2D map2d) {

        if (map != null) {
            map.removeMap2DListener(this);

            if (map instanceof NavigableMap2D) {
                ((NavigableMap2D) map).removeNavigableMap2DListener(this);
            }
            lastMapArea = map.getMapArea();
        }

        if (map2d instanceof Map2D) {
            map = (Map2D) map2d;
            map.addMap2DListener(this);
            gui_refresh.setEnabled(true);

            if (map2d instanceof NavigableMap2D) {
                NavigableMap2D navigationMap = (NavigableMap2D) map2d;
                navigationMap.addNavigableMap2DListener(this);
                gui_zoomAll.setEnabled(true);
                gui_zoomIn.setEnabled(true);
                gui_zoomOut.setEnabled(true);
                gui_zoomPan.setEnabled(true);

                switch (navigationMap.getActionState()) {
                    case ZOOM_IN:
                        gui_zoomIn.setSelected(true);
                        break;
                    case ZOOM_OUT:
                        gui_zoomAll.setSelected(true);
                        break;
                    case PAN:
                        gui_zoomPan.setSelected(true);
                        break;
                    case SELECT:
                        gui_select.setSelected(true);
                        break;
                    default:
                        gui_zoomIn.setSelected(false);
                        gui_zoomAll.setSelected(false);
                        gui_zoomPan.setSelected(false);
                        gui_select.setSelected(false);
                        break;
                }

                if (map2d instanceof SelectableMap2D) {
                    gui_select.setEnabled(true);
                } else {
                    gui_select.setEnabled(false);
                }
            } else {
                gui_zoomAll.setEnabled(false);
                gui_zoomIn.setEnabled(false);
                gui_zoomOut.setEnabled(false);
                gui_zoomPan.setEnabled(false);
            }
        } else {
            map = null;
            gui_refresh.setEnabled(false);
        }

        gui_nextArea.setEnabled(false);
        gui_previousArea.setEnabled(false);

    }

    //-----------------------Map2DListener--------------------------------------
    public void mapAreaChanged(Map2DMapAreaEvent event) {

        while (mapAreas.size() > 10) {
            mapAreas.remove(0);
        }

        Envelope newMapArea = event.getNewMapArea();
        lastMapArea = newMapArea;

        if (mapAreas.contains(newMapArea)) {

            if (mapAreas.size() > 1) {

                int position = mapAreas.indexOf(newMapArea);

                if (position == 0) {
                    gui_previousArea.setEnabled(false);
                    gui_nextArea.setEnabled(true);
                } else if (position == mapAreas.size() - 1) {
                    gui_previousArea.setEnabled(true);
                    gui_nextArea.setEnabled(false);
                } else {
                    gui_previousArea.setEnabled(true);
                    gui_nextArea.setEnabled(true);
                }

            } else {
                gui_previousArea.setEnabled(false);
                gui_nextArea.setEnabled(false);
            }


        } else {
            mapAreas.add(newMapArea);

            if (mapAreas.size() > 1) {
                gui_previousArea.setEnabled(true);
                gui_nextArea.setEnabled(false);
            } else {
                gui_previousArea.setEnabled(false);
                gui_nextArea.setEnabled(false);
            }
        }




    }

    public void mapContextChanged(Map2DContextEvent event) {
        mapAreas.clear();
        gui_nextArea.setEnabled(false);
        gui_previousArea.setEnabled(false);
    }

    public void mapActionStateChanged(Map2DActionStateEvent event) {
        switch (event.getNewState()) {
            case ZOOM_IN:
                gui_zoomIn.setSelected(true);
                break;
            case ZOOM_OUT:
                gui_zoomAll.setSelected(true);
                break;
            case PAN:
                gui_zoomPan.setSelected(true);
                break;
            case SELECT:
                gui_select.setSelected(true);
                break;
            default:
                gui_other.setSelected(true);
                break;
        }
    }
}
