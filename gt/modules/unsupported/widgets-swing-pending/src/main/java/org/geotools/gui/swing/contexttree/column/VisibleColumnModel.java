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

import javax.swing.JLabel;
import javax.swing.table.TableCellRenderer;

import org.geotools.gui.swing.contexttree.ContextTreeRenderer;
import org.geotools.gui.swing.contexttree.renderer.ColumnHeader;
import org.geotools.gui.swing.contexttree.renderer.HeaderRenderer;
import org.geotools.gui.swing.i18n.TextBundle;
import org.geotools.gui.swing.icon.IconBundle;
import org.geotools.map.MapLayer;
import org.jdesktop.swingx.renderer.ButtonProvider;
import org.jdesktop.swingx.table.TableColumnExt;

/**
 * @author johann sorel
 */
public class VisibleColumnModel implements ColumnModel{
    
    private boolean edit = true;
    private TableColumnExt col = new TableColumnExt();

    
    public VisibleColumnModel() {
        ColumnHeader head1 = new ColumnHeader(TextBundle.getResource().getString("col_visible"),new JLabel( IconBundle.getResource().getIcon("16_visible")  ));
        TableCellRenderer headerRenderer = new HeaderRenderer(head1);
        
        TableCellRenderer cellRenderer = new ContextTreeRenderer(new ButtonProvider()) ;
        
        col.setHeaderValue(head1);
        col.setHeaderRenderer(headerRenderer);
        col.setCellRenderer(cellRenderer);
        col.setResizable(false);
        col.setMaxWidth(25);
        col.setMinWidth(25);
        col.setPreferredWidth(25);
        col.setWidth(25);
    }
         
    
    public void setValue(Object target, Object value) {
        if(target instanceof MapLayer && value instanceof Boolean)
            ((MapLayer)target).setVisible((Boolean)value);
    }
    
    public Object getValue(Object target) {
        
        if(target instanceof MapLayer)
            return ((MapLayer)target).isVisible();
        else
            return "n/a";
    }
    
    public String getName() {
        return TextBundle.getResource().getString("col_visible");
    }
    
    public boolean isEditable() {
        return edit;
    }
    
    public boolean isCellEditable(Object target){
        
         if(target instanceof MapLayer)
            return edit;
        else
            return false;
    }
    
    public void setEditable(boolean edit) {
        this.edit = edit;
    }
    
    public Class getColumnClass() {
        return Boolean.class;
    }

    public TableColumnExt getTableColumnExt() {
        return col;
    }
    
}
