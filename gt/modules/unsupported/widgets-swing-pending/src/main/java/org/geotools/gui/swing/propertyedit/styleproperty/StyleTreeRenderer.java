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

package org.geotools.gui.swing.propertyedit.styleproperty;

import java.awt.Component;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

/**
 *
 * @author johann sorel
 */
public class StyleTreeRenderer extends DefaultTreeCellRenderer{
    
    /** Creates a new instance of StyleTreeRenderer */
    public StyleTreeRenderer() {
        super();
    }
    
    @Override
    public Component getTreeCellRendererComponent(JTree tree,Object value,boolean sel,boolean expanded,boolean leaf, int row,boolean hasFocus) {
        super.getTreeCellRendererComponent(tree, value, sel,expanded, leaf, row,hasFocus);
        
        if( value instanceof StyleNode){
            setIcon(((StyleNode)value).getIcon());
            setText(((StyleNode)value).getTitle());
        }
        
        return this;
    }
    
}
