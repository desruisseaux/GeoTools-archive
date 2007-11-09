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

import javax.swing.JCheckBoxMenuItem;

import org.geotools.gui.swing.contexttree.ContextTreeNode;
import org.geotools.gui.swing.contexttree.JContextTree;
import org.geotools.gui.swing.i18n.TextBundle;
import org.geotools.map.MapContext;


/**
 * Default popup control for activation of MapContext, use for JContextTreePopup
 * 
 * @author johann sorel
 * 
 */
public class ContextActiveTreePopupItem extends JCheckBoxMenuItem implements TreePopupItem{
    
    private MapContext context;
    private JContextTree xtree ;
    
    
    /** 
     * Creates a new instance of ContextActiveControl 
     * @param tree 
     */
    public ContextActiveTreePopupItem(JContextTree tree) {
        this.setText( TextBundle.getResource().getString("activated")  );
        xtree = tree;
        init();
    }
    
   
    
    public Component getComponent(Object[] obj, ContextTreeNode node[]) {
        context = (MapContext)obj[0];
        this.setSelected( context.equals(xtree.getActiveContext()));
        
        return this;
    }
    
    private void init(){
        
        addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(isSelected()){
                    if(xtree != null && context != null)
                        xtree.setActiveContext(context);
                } else if(xtree != null){
                    xtree.setActiveContext(null);
                }
            }
        });
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
