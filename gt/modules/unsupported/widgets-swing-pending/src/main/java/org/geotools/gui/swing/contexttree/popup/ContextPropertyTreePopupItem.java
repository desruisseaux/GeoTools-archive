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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JMenuItem;

import org.geotools.gui.swing.contexttree.ContextTreeNode;
import org.geotools.gui.swing.i18n.TextBundle;
import org.geotools.gui.swing.propertyedit.ContextCRSPropertyPanel;
import org.geotools.gui.swing.propertyedit.JPropertyDialog;
import org.geotools.gui.swing.propertyedit.PropertyPanel;
import org.geotools.map.MapContext;

/**
 * Default popup control for property page of MapContext, use for JContextTreePopup 
 * 
 * @author johann sorel 
 */
public class ContextPropertyTreePopupItem extends JMenuItem implements TreePopupItem{
    
    private MapContext context;
    
    /** 
     * Creates a new instance of DefaultContextPropertyPop 
     */
    public ContextPropertyTreePopupItem() {
        super( TextBundle.getResource().getString("properties")  );
        init();
    }
    
     
    public Component getComponent(Object[] obj, ContextTreeNode node[]) {
        context = (MapContext)obj[0];
        return this;
    }
    
    private void init(){
        addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ArrayList<PropertyPanel> lst = new ArrayList<PropertyPanel>();
                lst.add(new ContextCRSPropertyPanel());
                JPropertyDialog.showDialog(lst, context);
                
            }
        }
        );
    }

    public boolean isValid(Object[] objs) {
        
        if(objs.length == 1){
            return isValid(objs[0]);
        }        
        return false;        
    }
    
    private boolean isValid(Object obj) {
        return obj instanceof MapContext;
    }
    
}
