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

package org.geotools.gui.swing.toolbox;

import org.jdesktop.swingx.treetable.AbstractMutableTreeTableNode;

/**
 * 
 * @author johann sorel
 */
final class ToolPackTreeNode extends AbstractMutableTreeTableNode{
    
    private String name ="";
    
    /**
     * Creates a new instance of ContextTreeNode
     * @param model model of the tree
     */
    ToolPackTreeNode(String name) {
        super();
        this.name = name;
    }
    
    public String getTitle(){
        return name;
    }
    
    public Object getValueAt(int arg0) {
        return name;
    }

    public int getColumnCount() {
        return 1;
    }
                
        
}
