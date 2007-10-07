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

package org.geotools.gui.swing.contexttree.renderer;

import java.awt.Component;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;


/**
 * @author johann Sorel
 * Header Renderer for JXMapContextTree
 */
public class HeaderRenderer implements TableCellRenderer{
    
    private ColumnHeader head = null;
    
    /**
     * Creates a new instance of JXMapContextHeaderRenderer
     */
    public HeaderRenderer() {}
    
    public HeaderRenderer(ColumnHeader head) {
        this.head = head;
    }
    
    
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        
        if(head == null){
            if(value.getClass().equals(ColumnHeader.class)){
                return (Component)value;
            } else if(value.getClass().equals(JLabel.class)){
                JLabel lbl = (JLabel)value;
                return lbl;
            } else{
                JLabel lbl = new JLabel(value.toString(),JLabel.CENTER);
                lbl.setFont(new Font("Arial",Font.BOLD,10));
                lbl.setBorder(BorderFactory.createEtchedBorder());
                return lbl;
            }
        }else{
            return (Component)value;
        }
        
    }
    
}
