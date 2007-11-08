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
 * Style column, glyph legend
 * 
 * @author johann sorel
 */
public class StyleTreeTableColumn extends TreeTableColumn {
    
    
    /**
     * Creates a new instance 
     */
    public StyleTreeTableColumn() {
        super();
        
        setHeaderRenderer(new DefaultHeaderRenderer(IconBundle.getResource().getIcon("16_style"),null,TextBundle.getResource().getString("col_symbol")));
        setCellEditor( new DefaultCellEditor( new StyleComponent()));
        setCellRenderer( new DefaultCellRenderer( new StyleComponent()));
        
        setTitle(TextBundle.getResource().getString("col_symbol"));
        setEditable(true);
        setResizable(false);
        setMaxWidth(25);
        setMinWidth(25);
        setPreferredWidth(25);
        setWidth(25);
    }
    
    
    
    public void setValue(Object target, Object value) {}
    
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
        return MapLayer.class;
    }
    
    
    @Override
    public boolean isEditableOnMouseOver() {
        return true;
    }
    
}

    
    

