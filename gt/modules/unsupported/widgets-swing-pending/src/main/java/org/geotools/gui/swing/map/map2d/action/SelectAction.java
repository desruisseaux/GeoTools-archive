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


public class SelectAction extends AbstractAction {
    private ImageIcon icon;
    Map2D map;

    public SelectAction(Map2D map) {
        URL url = this.getClass().getResource("resources/Add16.gif"); //$NON-NLS-1$

        icon = new ImageIcon(url);
        this.putValue(Action.SMALL_ICON, icon);
        this.putValue(Action.NAME, "select"); //$NON-NLS-1$
        this.map = map;
    }

    public void actionPerformed(ActionEvent e) {
        // TODO Auto-generated method stub
        map.setState(MapConstants.MAP_STATE.SELECT);
        map.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    public ImageIcon getIcon() {
        return icon;
    }

    public void setIcon(ImageIcon icon) {
        this.icon = icon;
    }
}
