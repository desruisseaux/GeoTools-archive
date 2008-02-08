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
import javax.swing.tree.TreePath;
import org.geotools.gui.swing.contexttree.ContextTreeNode;
import org.geotools.gui.swing.map.Map;
import org.geotools.gui.swing.map.map2d.NavigableMap2D;
import org.geotools.map.MapLayer;

/**
 * @author johann sorel
 * Default popup control for zoom on MapLayer, use for JContextTreePopup
 */
public class LayerZoomItem extends JMenuItem implements TreePopupItem {

    private MapLayer layer;
    private NavigableMap2D map;

    /** Creates a new instance
     * @param map 
     */
    public LayerZoomItem(Map map) {
        this.setText(BUNDLE.getString("zoom_to_layer"));
        setMap(map);
        init();
    }

    public void setMap(Map map) {
        if(map instanceof NavigableMap2D){
            this.map = (NavigableMap2D) map;
        }
    }

    public NavigableMap2D getMap() {
        return map;
    }
   
    private void init() {

        addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent e) {

                        if (map != null && layer != null) {
                            try {
                                map.getRenderingStrategy().setMapArea(layer.getBounds());
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                });
    }
   
    public boolean isValid(TreePath[] selection) {
        if (selection.length == 1) {
            ContextTreeNode node = (ContextTreeNode) selection[0].getLastPathComponent();            
            return ( node.getUserObject() instanceof MapLayer ) ;
        }
        return false;
    }

    public Component getComponent(TreePath[] selection) {
        ContextTreeNode node = (ContextTreeNode) selection[0].getLastPathComponent();  
        layer = (MapLayer) node.getUserObject() ;
        this.setEnabled((map != null));

        return this;
    }
}
