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
package org.geotools.gui.swing.contexttree.popup;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;
import org.geotools.gui.swing.JMapPane;
import org.geotools.gui.swing.contexttree.SelectionData;
import org.geotools.gui.swing.i18n.TextBundle;
import org.geotools.map.MapLayer;

/**
 * @author johann sorel
 * Default popup control for zoom on MapLayer, use for JContextTreePopup
 */
public class LayerZoomItem extends JMenuItem implements TreePopupItem, MapRelatedTreePopupItem {

    private MapLayer layer;
    private JMapPane map;

    /** Creates a new instance
     * @param map 
     */
    public LayerZoomItem(JMapPane map) {
        this.setText(TextBundle.getResource().getString("zoom_to_layer"));
        this.map = map;
        init();
    }

    public void setMapPane(JMapPane map) {
        this.map = map;
    }

    public JMapPane getMapPane() {
        return map;
    }
   
    private void init() {

        addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent e) {

                        if (map != null && layer != null) {
                            try {
                                map.setMapArea(layer.getBounds());
                                map.setReset(true);
                                map.revalidate();
                                map.repaint();
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                });
    }
   
    public boolean isValid(SelectionData[] selection) {
        if (selection.length == 1) {
            return (selection[0].layer != null) ;
        }
        return false;
    }

    public Component getComponent(SelectionData[] selection) {
        layer = selection[0].layer;
        this.setEnabled((map != null));

        return this;
    }
}
