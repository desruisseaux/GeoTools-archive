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

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import org.geotools.gui.swing.contexttree.ContextTreeNode;
import org.geotools.gui.swing.contexttree.JContextTree_old;
import org.geotools.gui.swing.contexttree.ContextTreeTable;
import org.geotools.gui.swing.i18n.TextBundle;
import org.geotools.gui.swing.icon.IconBundle;
import org.geotools.map.MapContext;


/**
 * @author johann sorel
 * Default popup control for deletion of MapContext, use for JXMapContextTreePopup
 */
public class ContextDeletePopupComponent extends JMenuItem implements PopupComponent{
    
    private MapContext context;
    private ContextTreeTable xtree ;
    
    /** 
     * Creates a new instance of ContextDeleteControl 
     * @param tree 
     */
    public ContextDeletePopupComponent( ContextTreeTable tree ) {
        super();
        init();
        setText( TextBundle.getResource().getString("delete")  );
        setIcon( IconBundle.getResource().getIcon("CP16_actions_fileclose")  );
        xtree = tree;
    }
    
    
    public Component getComponent(Object obj, ContextTreeNode node) {
        
        ContextTreeNode child = (ContextTreeNode)node;
        
        if( child.getUserObject() instanceof MapContext){
            context = (MapContext)child.getUserObject();
        }
                
        return this;
    }
    
    private void init(){
        
        addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int answer = JOptionPane.showConfirmDialog(null, TextBundle.getResource().getString("delete_question"), TextBundle.getResource().getString("confirm"),JOptionPane.YES_NO_OPTION);
                   
                if(answer == JOptionPane.YES_OPTION && context != null && xtree != null)                 
                        xtree.getTreeTableModel().removeMapContext(context);
                                                        
            }
        });
    }

    public boolean isValid(Object obj) {
        return obj instanceof MapContext;
    }
    
    
    
}
