/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2006, GeoTools Project Managment Committee (PMC)
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
package org.geotools.gui.swing.map.map2d.action;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.net.URL;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import org.geotools.gui.swing.map.MapConstants;
import org.geotools.gui.swing.map.map2d.Map2D;


/**
 * Basic action for panning mappanes
 * @author Ian Turton
 *
 */
public class PanAction extends AbstractAction {
    /**
     * shut eclipse up
     */
    private static final long serialVersionUID = 2718536128821468386L;

    /**
     * the icon to display
     */
    private ImageIcon icon;

    /**
     * the mappane to pan
     */
    Map2D map;

    /**
     * constructor
     * @param map - the mappane to act on
     */
    public PanAction(Map2D map) {
        URL url = this.getClass().getResource("resources/Pan16.gif"); //$NON-NLS-1$
        icon = new ImageIcon(url);
        this.putValue(Action.SMALL_ICON, icon);
        this.putValue(Action.NAME, "pan"); //$NON-NLS-1$
        this.map = map;
    }

    /**
     * the action occurred - set the state of the map pane to pan
     */
    public void actionPerformed(ActionEvent e) {
        map.setState(MapConstants.MAP_STATE.PAN);
        map.setCursor(new Cursor(Cursor.MOVE_CURSOR));
    }

    /**
     * get the current icon
     * @return the icon
     */
    public ImageIcon getIcon() {
        return icon;
    }

    /**
     * set the icon to be displayed with the action
     * @param icon
     */
    public void setIcon(ImageIcon icon) {
        this.icon = icon;
    }
}
