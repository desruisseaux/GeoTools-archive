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

import org.geotools.gui.swing.contexttree.JContextTree;
import org.geotools.gui.swing.contexttree.SelectionData;
import org.geotools.map.MapContext;


/**
 * Default popup control for activation of MapContext, use for JContextTreePopup
 * 
 * @author johann sorel
 * 
 */
public class ContextActiveItem extends JCheckBoxMenuItem implements TreePopupItem{
    
    
    
    private MapContext context;
    private JContextTree xtree ;
    
    
    /** 
     * Creates a new instance of ContextActiveControl 
     * @param tree 
     */
    public ContextActiveItem(JContextTree tree) {
        this.setText( BUNDLE.getString("activated")  );
        xtree = tree;
        init();
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

    public boolean isValid(SelectionData[] selection) {
        if (selection.length == 1) {
            return (selection[0].getLayer() == null && selection[0].getSubObject() == null) ;
        }
        return false;
    }

    public Component getComponent(SelectionData[] selection) {
        context = selection[0].getContext();
        this.setSelected( context.equals(xtree.getActiveContext()));
        
        return this;
    }
    
    
}
