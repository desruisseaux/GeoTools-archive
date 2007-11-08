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

import org.geotools.gui.swing.contexttree.ContextTreeNode;

/**
 * Default popup control separator, use for JContextTreePopup
 * 
 * @author johann sorel
 * 
 */
public class SeparatorTreePopupItem extends JSeparator implements TreePopupItem{
    
    private Class classe;
    
    /**
     * Constructor
     */
    public SeparatorTreePopupItem() {
        this(Object.class);
    }
    
    
    /** 
     * Creates a new instance of DefaultPopSeparator 
     * @param classe 
     */
    public SeparatorTreePopupItem(Class classe) {
        super();
        this.setOrientation(SwingConstants.HORIZONTAL);
        this.classe = classe;
    }


    public Component getComponent(Object[] obj, ContextTreeNode[] node) {
        return this;
    }
    
    public boolean isValid(Object[] objs) {
        for(Object obj : objs){            
            if( !isValid(obj) ){
                return false;
            }
        }
        
        return true;
        
    }
    
    private boolean isValid(Object obj){
        return classe.isAssignableFrom(obj.getClass())  ;
    }
    
}
