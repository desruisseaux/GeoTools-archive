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

import java.awt.Dimension;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JToolBar;
import javax.swing.border.EmptyBorder;

import org.geotools.gui.swing.icon.IconBundle;
import org.geotools.gui.swing.map.map2d.EditableMap2D;
import org.geotools.gui.swing.map.map2d.Map2D;

/**
 * JMap2DEditBar is a JPanel to handle edition state for an EditableMap2D
 * Layer selection, edition, line, polygon, point ...
 * 
 * @author johann sorel
 */
public class JMap2DEditBar extends JToolBar {

    private static final ImageIcon ICON_EDIT = IconBundle.getResource().getIcon("16_edit");
    private static final ImageIcon ICON_EDIT_GEOM = IconBundle.getResource().getIcon("16_edit_geom");
    private static final ImageIcon ICON_EDIT_POINT = IconBundle.getResource().getIcon("16_edit_single_point");
    private static final ImageIcon ICON_EDIT_MULTIPOINT = IconBundle.getResource().getIcon("16_edit_multi_point");
    private static final ImageIcon ICON_EDIT_LINE = IconBundle.getResource().getIcon("16_edit_single_line");
    private static final ImageIcon ICON_EDIT_MULTILINE = IconBundle.getResource().getIcon("16_edit_multi_line");
    private static final ImageIcon ICON_EDIT_POLYGON = IconBundle.getResource().getIcon("16_edit_single_polygon");
    private static final ImageIcon ICON_EDIT_MULTIPOLYGON = IconBundle.getResource().getIcon("16_edit_multi_polygon");
    
    private final EditAction ACTION_EDIT = new EditAction();
    private final EditGeomAction ACTION_EDIT_GEOM = new EditGeomAction();
    private final EditPointAction ACTION_EDIT_POINT = new EditPointAction();
    private final EditMultiPointAction ACTION_EDIT_MULTIPOINT = new EditMultiPointAction();
    private final EditLineAction ACTION_EDIT_LINE = new EditLineAction();
    private final EditMultiLineAction ACTION_EDIT_MULTILINE = new EditMultiLineAction();
    private final EditPolygonAction ACTION_EDIT_POLYGON = new EditPolygonAction();
    private final EditMultiPolygonAction ACTION_EDIT_MULTIPOLYGON = new EditMultiPolygonAction();
    
    private Map2D map = null;
    private final JButton gui_edit = buildButton(ICON_EDIT, ACTION_EDIT);
    private final JButton gui_edit_geom = buildButton(ICON_EDIT_GEOM, ACTION_EDIT_GEOM);
    private final JButton gui_edit_line = buildButton(ICON_EDIT_LINE, ACTION_EDIT_LINE);
    private final JButton gui_edit_multiline = buildButton(ICON_EDIT_MULTILINE, ACTION_EDIT_MULTILINE);
    private final JButton gui_edit_multipoint = buildButton(ICON_EDIT_MULTIPOINT, ACTION_EDIT_MULTIPOINT);
    private final JButton gui_edit_multipolygon = buildButton(ICON_EDIT_MULTIPOLYGON, ACTION_EDIT_MULTIPOLYGON);
    private final JButton gui_edit_point = buildButton(ICON_EDIT_POINT, ACTION_EDIT_POINT);
    private final JButton gui_edit_polygon = buildButton(ICON_EDIT_POLYGON, ACTION_EDIT_POLYGON);
    private final EditedLayerChooser gui_chooser = new EditedLayerChooser();
    private final int largeur = 2;
    
    

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
        setMap(pane);
        init();
    }

    private void init() {
        add(gui_edit);
        add(gui_chooser);
        add(gui_edit_geom);
        add(gui_edit_point);
        add(gui_edit_multipoint);
        add(gui_edit_line);
        add(gui_edit_multiline);
        add(gui_edit_polygon);
        add(gui_edit_multipolygon);
    }


    private JButton buildButton(ImageIcon img,Action action) {
        JButton but = new JButton(action);
        but.setIcon(img);
        but.setBorder(new EmptyBorder(largeur, largeur, largeur, largeur));
        but.setBorderPainted(false);
        but.setContentAreaFilled(false);
        but.setPreferredSize(new Dimension(25, 25));
        but.setOpaque(false);
        return but;
    }
    
    
    /**
     * set the related Map2D
     * @param map2d : related Map2D
     */
    public void setMap(Map2D map2d) {

        map = map2d;
        ACTION_EDIT.setMap(map);
        ACTION_EDIT_GEOM.setMap(map);
        ACTION_EDIT_LINE.setMap(map);
        ACTION_EDIT_MULTILINE.setMap(map);
        ACTION_EDIT_MULTIPOINT.setMap(map);
        ACTION_EDIT_MULTIPOLYGON.setMap(map);
        ACTION_EDIT_POINT.setMap(map);
        ACTION_EDIT_POLYGON.setMap(map);
        gui_chooser.setMap(map2d);
    }

}
