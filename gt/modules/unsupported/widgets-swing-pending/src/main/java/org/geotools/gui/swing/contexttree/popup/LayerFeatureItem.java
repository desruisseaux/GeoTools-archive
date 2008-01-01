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
import java.util.ArrayList;

import javax.swing.JMenuItem;

import org.geotools.gui.swing.contexttree.SelectionData;
import org.geotools.gui.swing.i18n.TextBundle;
import org.geotools.gui.swing.propertyedit.JPropertyDialog;
import org.geotools.gui.swing.propertyedit.LayerFeaturePropertyPanel;
import org.geotools.gui.swing.propertyedit.PropertyPanel;
import org.geotools.map.MapLayer;


/**
 * @author johann sorel
 * Default popup control for property page of MapLayer, use for JContextTreePopup
 */
public class LayerFeatureItem extends JMenuItem implements TreePopupItem{
    
    private MapLayer layer;
    
    /** 
     * Creates a new instance of DefaultContextPropertyPop 
     */
    public LayerFeatureItem() {
        super( TextBundle.getResource().getString("feature_table")  );
        init();
    }
        
    private void init(){
        addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ArrayList<PropertyPanel> lst = new ArrayList<PropertyPanel>();
                lst.add(new LayerFeaturePropertyPanel());
                JPropertyDialog.showDialog(lst, layer);
                
            }
        }
        );
    }
    
    public boolean isValid(SelectionData[] selection) {
        if (selection.length == 1) {
            return (selection[0].getLayer() != null && selection[0].getSubObject()== null) ;
        }
        return false;
    }

    public Component getComponent(SelectionData[] selection) {
        layer = selection[0].getLayer();
        return this;
    }
    
}
