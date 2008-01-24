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

package org.geotools.gui.swing.contexttree.popup;

import java.awt.Component;

import javax.swing.JSeparator;
import javax.swing.SwingConstants;

import org.geotools.gui.swing.contexttree.SelectionData;

/**
 * Default popup control separator, use for JContextTreePopup
 * 
 * @author johann sorel
 * 
 */
public class SeparatorItem extends JSeparator implements TreePopupItem{
       
    
    /** 
     * Creates a new instance of separator
     */
    public SeparatorItem() {
        super();
        setOrientation(SwingConstants.HORIZONTAL);
    }
    
    public boolean isValid(SelectionData[] selection) {
        return true;
    }

    public Component getComponent(SelectionData[] selection) {
        return this;
    }
    
}
