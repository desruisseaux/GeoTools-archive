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
import org.geotools.gui.swing.contexttree.SelectionData;
import org.geotools.gui.swing.contexttree.column.OpacityComponent;

/**
 *
 * @author johann sorel
 */
public class LayerOpacityItem implements TreePopupItem{

    private final OpacityComponent comp = new OpacityComponent();
    
    /**
     * create new instance
     */
    public LayerOpacityItem(){
        comp.setOpaque(false);
    }
           
    public boolean isValid(SelectionData[] selection) {
        if (selection.length == 1) {
            return (selection[0].layer != null) ;
        }
        return false;
    }

    public Component getComponent(SelectionData[] selection) {
        comp.parse(selection[0].layer);
        return comp;
    }

}
