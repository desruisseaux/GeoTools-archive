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
 *
 * @author johann sorel
 */
public abstract class ContextTreeColumn extends TableColumnExt{

    public abstract void setValue(Object target,Object value);
    
    public abstract Object getValue(Object target);
    
    public abstract Class getColumnClass();
    
    public abstract String getName();

    public abstract boolean isCellEditable(Object target);
    
    public abstract boolean isEditableOnMouseOver();
    
    
}
