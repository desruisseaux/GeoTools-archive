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
import org.geotools.gui.swing.contexttree.ContextTreeNode;
import org.geotools.gui.swing.i18n.TextBundle;
import org.geotools.map.MapLayer;



/**
 * @author johann sorel
 * Default popup control for visibility of MapLayer, use for JXMapContextTreePopup
 */
public class LayerZoomPopupComponent extends JMenuItem implements PopupComponent{
    
    private MapLayer layer;
    private JMapPane map;
    
    /** Creates a new instance of LayerVisibleControl */
    public LayerZoomPopupComponent() {
        this.setText( TextBundle.getResource().getString("zoom_to_layer"));
        init();
    }
    
    public void setMapPane(JMapPane map){
        this.map = map;
    }
    
    
    public Component getComponent(Object obj, ContextTreeNode node) {
        layer = (MapLayer)obj;
        this.setSelected(layer.isVisible());
        
        return this;
    }
    
    private void init(){
        
        addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                
                if(map!= null)
                    map.setMapArea( layer.getBounds());
            }
        });
    }
    
    public boolean isValid(Object obj) {
        return obj instanceof MapLayer;
    }
    
}
