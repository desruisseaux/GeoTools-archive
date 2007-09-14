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

import org.jdesktop.swingx.table.TableColumnExt;

/**
 * @author johann sorel
 */
public interface ColumnModel {
    
    
    public void setValue(Object target,Object value);
    
    public Object getValue(Object target);
    
    public Class getColumnClass();
    
    public String getName();
    
    public boolean isEditable();
    
    public void setEditable(boolean edit);

    public boolean isCellEditable(Object target);
    
    public TableColumnExt getTableColumnExt();
            
    
    
}
