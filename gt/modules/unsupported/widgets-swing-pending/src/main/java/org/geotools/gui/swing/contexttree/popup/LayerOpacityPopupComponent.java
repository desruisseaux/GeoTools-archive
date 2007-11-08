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
import java.awt.Dimension;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.plaf.DimensionUIResource;
import org.geotools.gui.swing.contexttree.ContextTreeNode;
import org.geotools.gui.swing.contexttree.column.OpacityComponent;
import org.geotools.map.MapLayer;

/**
 *
 * @author johann sorel
 */
public class LayerOpacityPopupComponent implements PopupComponent{

    private OpacityComponent comp = new OpacityComponent();
    
    public LayerOpacityPopupComponent(){
        comp.setOpaque(false);
        //comp.setPreferredSize(new Dimension(100,22));
    }
    
    public boolean isValid(Object[] objs) {
        
        if(objs.length == 1){
            return isValid(objs[0]);
        }        
        return false;        
    }
    
    public boolean isValid(Object obj) {
        return obj instanceof MapLayer;
    }

    public Component getComponent(Object[] obj, ContextTreeNode[] node) {
        comp.parse(obj[0]);
        return comp;
    }

}
