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

package org.geotools.gui.swing.contexttree.column;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;

import org.geotools.gui.swing.contexttree.renderer.RenderAndEditComponent;
import org.geotools.gui.swing.map.map2d.SelectableMap2D;
import org.geotools.map.MapLayer;

/**
 *
 * @author johann sorel
 */
final class SelectionComponent extends RenderAndEditComponent{

    private SelectableMap2D map;
    private final JCheckBox check = new JCheckBox();
    private MapLayer layer = null;
    
    SelectionComponent(){
        super();
        setLayout(new GridLayout(1,1));
        check.setOpaque(false);
        
        check.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if(layer != null && map!= null){
                    
                    if(check.isSelected()){
                        map.addSelectableLayer(layer);
                    }else{
                        map.removeSelectableLayer(layer);
                    }
                }
            }
        });
    }
    
    public void setMap(SelectableMap2D map){
        this.map = map;
    }
    
    
    @Override
    public void parse(Object obj) {
       
        
        removeAll();
        if(obj instanceof MapLayer){
            layer = (MapLayer) obj;
            
            if(map != null){
                check.setSelected(map.isLayerSelectable(layer));
            }else{
                check.setSelected(false);
                check.setEnabled(false);
            }
            add(check);
        }else{
            layer = null;
        }
        
    }

    @Override
    public Object getValue() {
        return check.isSelected();
    }

}
