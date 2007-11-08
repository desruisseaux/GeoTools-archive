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

import javax.swing.JCheckBoxMenuItem;

import org.geotools.gui.swing.contexttree.ContextTreeNode;
import org.geotools.gui.swing.i18n.TextBundle;
import org.geotools.map.MapLayer;



/**
 * @author johann sorel
 * Default popup control for visibility of MapLayer, use for JContextTreePopup
 */
public class LayerVisibleTreePopupItem extends JCheckBoxMenuItem implements TreePopupItem{
    
    private MapLayer layer;
    
    
    /** 
     * Creates a new instance of LayerVisibleControl 
     */
    public LayerVisibleTreePopupItem() {
        this.setText( TextBundle.getResource().getString("visible"));
        init();
    }
    
    public Component getComponent(Object[] obj, ContextTreeNode node[]) {
        layer = (MapLayer)obj[0];
        this.setSelected(layer.isVisible());
        
        return this;
    }
    
    private void init(){
        
        addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                layer.setVisible(isSelected());
            }
        });
    }
    
    public boolean isValid(Object[] objs) {
        
        if(objs.length == 1){
            return isValid(objs[0]);
        }        
        return false;        
    }
    
    private boolean isValid(Object obj) {
        return obj instanceof MapLayer;
    }
    
}
