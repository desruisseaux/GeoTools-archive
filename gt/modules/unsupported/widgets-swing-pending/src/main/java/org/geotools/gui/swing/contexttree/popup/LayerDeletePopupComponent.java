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
import org.geotools.gui.swing.i18n.TextBundle;
import org.geotools.gui.swing.icon.IconBundle;
import org.geotools.map.MapContext;
import org.geotools.map.MapLayer;


/**
 * @author johann sorel
 * Default popup control for deletion of MapLayer, use for JXMapContextTreePopup
 */
public class LayerDeletePopupComponent extends JMenuItem implements PopupComponent{
    
    private MapContext context;
    private MapLayer layer;
    
    /** Creates a new instance of LayerDeleteControl */
    public LayerDeletePopupComponent() {
        super();
        init();
        setText( TextBundle.getResource().getString("delete")  );
        setIcon( IconBundle.getResource().getIcon("CP16_actions_fileclose")  );
    }
    
    
    
    
    
    public Component getComponent(Object obj, ContextTreeNode node) {
        
        layer = ((MapLayer)obj);
        final ContextTreeNode child = node;
        final ContextTreeNode parent = (ContextTreeNode)child.getParent();
        
        if( parent.getUserObject() instanceof MapContext){
            context = (MapContext)parent.getUserObject();
        }
        
        return this;
    }
    
    private void init(){
        
        addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                
                int answer = JOptionPane.showConfirmDialog(null, TextBundle.getResource().getString("delete_question"), TextBundle.getResource().getString("confirm"),JOptionPane.YES_NO_OPTION);                
                if(answer == JOptionPane.YES_OPTION && context != null) 
                    context.removeLayer(layer);                    
                                
            }
        });
    }
    
    
    public boolean isValid(Object obj) {
        return obj instanceof MapLayer;
    }
    
}
