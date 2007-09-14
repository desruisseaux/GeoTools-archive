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

import javax.swing.ImageIcon;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 *
 * @author johann sorel
 */
public class StyleNode extends DefaultMutableTreeNode{
    
    private StylePanel pan;
    
    /** Creates a new instance of StyleNode 
     * @param panel 
     */
    public StyleNode(StylePanel panel) {
        super(panel);
        pan = panel;
    }
    
    public  ImageIcon getIcon(){
        return pan.getIcon();
    }

    public String getTitle() {
        return pan.getTitle();
    }

        
}
