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
import org.geotools.gui.swing.contexttree.ContextTreeTable;
import org.geotools.gui.swing.i18n.TextBundle;
import org.geotools.map.MapContext;


/**
 * @author johann sorel
 * Default popup control for activation of MapContext, use for JXMapContextTreePopup
 */
public class ContextActivePopupComponent extends JCheckBoxMenuItem implements PopupComponent{
    
    private MapContext context;
    private ContextTreeTable xtree ;
    
    
    /** 
     * Creates a new instance of ContextActiveControl 
     * @param tree 
     */
    public ContextActivePopupComponent(ContextTreeTable tree) {
        this.setText( TextBundle.getResource().getString("activated")  );
        xtree = tree;
        init();
    }
    
   
    
    public Component getComponent(Object obj, ContextTreeNode node) {
        context = (MapContext)obj;
        this.setSelected( context.equals(xtree.getTreeTableModel().getActiveContext()));
        
        return this;
    }
    
    private void init(){
        
        addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(isSelected()){
                    if(xtree != null && context != null)
                        xtree.getTreeTableModel().setActiveContext(context);
                } else if(xtree != null){
                    xtree.getTreeTableModel().setActiveContext(null);
                }
            }
        });
    }

    public boolean isValid(Object obj) {
        return obj instanceof MapContext;
    }
    
    
}
