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


import org.geotools.gui.swing.i18n.TextBundle;
import org.geotools.gui.swing.icon.IconBundle;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import org.geotools.gui.swing.contexttree.renderer.ColumnHeader;
import org.geotools.gui.swing.contexttree.renderer.HeaderRenderer;
import org.geotools.gui.swing.contexttree.renderer.StyleCellProvider;
import org.geotools.gui.swing.contexttree.renderer.StyleCellRenderer;
import org.geotools.gui.swing.propertyedit.JPropertyDialog;
import org.geotools.gui.swing.propertyedit.LayerStylePropertyPanel;
import org.geotools.gui.swing.propertyedit.PropertyPanel;
import org.geotools.map.MapLayer;
import org.jdesktop.swingx.renderer.ComponentProvider;
import org.jdesktop.swingx.table.TableColumnExt;

/**
 * @author johann sorel
 */
public class StyleColumnModel implements ColumnModel{
    
    private boolean edit = true;
    private TableColumnExt col = new TableColumnExt();
    
    /**
     * Creates a new instance of JXVisibleColumn
     */
    public StyleColumnModel() {
        ColumnHeader head1 = new ColumnHeader(
                TextBundle.getResource().getString("col_symbol"),
                new JLabel( IconBundle.getResource().getIcon("16_style") )
                
                );
        
        TableCellRenderer headerRenderer = new HeaderRenderer(head1);
        
        col.setHeaderValue(head1);
        col.setHeaderRenderer(headerRenderer);
        
        ComponentProvider myProvider = new StyleCellProvider();
        col.setCellRenderer( new StyleCellRenderer(myProvider) );
        col.setCellEditor( new Editor() );
        
        col.setResizable(false);
        col.setMaxWidth(25);
        col.setMinWidth(25);
        col.setPreferredWidth(25);
        col.setWidth(25);
    }
    
    
    
    public void setValue(Object target, Object value) {}
    
    public Object getValue(Object target) {
        
        if(target instanceof MapLayer)
            return (MapLayer)target;
        else
            return "n/a";
    }
    
    public String getName() {
        return TextBundle.getResource().getString("col_symbol");
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


class Editor extends AbstractCellEditor implements TableCellEditor{
    
    private JButton component = new JButton();
    private MapLayer layer = null;
    
    public Editor(){
        super();
        component.setBorderPainted(false);
        component.setBorder(null);
        component.setOpaque(false);
        component.setIcon(IconBundle.getResource().getIcon("paint"));
        component.setContentAreaFilled(false);
        
        component.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if( layer != null ){
                    ArrayList<PropertyPanel> lst = new ArrayList<PropertyPanel>();
                    lst.add(new LayerStylePropertyPanel());
                    JPropertyDialog.showDialog(lst, layer);
                    
                    stopCellEditing();
                }
            }
        });
    }
    
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        
        layer = (value instanceof MapLayer) ? (MapLayer)value : null ;
        
        return component;
    }
    
    public Object getCellEditorValue() {
        return null;
    }
    
    
    
}
