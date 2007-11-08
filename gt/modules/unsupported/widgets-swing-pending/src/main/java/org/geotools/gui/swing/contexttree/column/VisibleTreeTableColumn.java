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


import org.geotools.gui.swing.contexttree.renderer.DefaultCellEditor;
import org.geotools.gui.swing.contexttree.renderer.DefaultCellRenderer;
import org.geotools.gui.swing.contexttree.renderer.DefaultHeaderRenderer;
import org.geotools.gui.swing.i18n.TextBundle;
import org.geotools.gui.swing.icon.IconBundle;
import org.geotools.map.MapLayer;

/**
 * @author johann sorel
 */
public class VisibleTreeTableColumn extends TreeTableColumn {
    
    
    /**
     * column with checkbox for jcontexttree
     */
    public VisibleTreeTableColumn() {
       
        setHeaderRenderer(new DefaultHeaderRenderer(IconBundle.getResource().getIcon("16_visible"),null,TextBundle.getResource().getString("col_visible")));        
        setCellEditor( new DefaultCellEditor(new VisibleComponent()));
        setCellRenderer( new DefaultCellRenderer(new VisibleComponent()));
        
        setTitle(TextBundle.getResource().getString("col_visible"));
        setEditable(true);
        setResizable(false);
        setMaxWidth(25);
        setMinWidth(25);
        setPreferredWidth(25);
        setWidth(25);
    }
         
    
   
    public void setValue(Object target, Object value) {
    }
    
    
    public Object getValue(Object target) {
        
        if(target instanceof MapLayer)
            return (MapLayer)target;
        else
            return "n/a";
    }
    
    
        
    public boolean isCellEditable(Object target){
        
         if(target instanceof MapLayer)
            return isEditable();
        else
            return false;
    }
    
    
    public Class getColumnClass() {
        return Boolean.class;
    }

    
    @Override
    public boolean isEditableOnMouseOver() {
        return true;
    }
    
}
