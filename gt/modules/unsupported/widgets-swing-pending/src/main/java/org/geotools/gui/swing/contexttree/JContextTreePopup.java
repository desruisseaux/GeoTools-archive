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

package org.geotools.gui.swing.contexttree;


import java.awt.Point;
import java.util.ArrayList;

import javax.swing.JPopupMenu;
import javax.swing.tree.TreePath;

import org.geotools.gui.swing.contexttree.popup.ContextActivePopupComponent;
import org.geotools.gui.swing.contexttree.popup.ContextDeletePopupComponent;
import org.geotools.gui.swing.contexttree.popup.ContextPropertyPopupComponent;
import org.geotools.gui.swing.contexttree.popup.LayerDeletePopupComponent;
import org.geotools.gui.swing.contexttree.popup.LayerFeaturePopupComponent;
import org.geotools.gui.swing.contexttree.popup.LayerPropertyPopupComponent;
import org.geotools.gui.swing.contexttree.popup.LayerVisiblePopupComponent;
import org.geotools.gui.swing.contexttree.popup.PopupComponent;
import org.geotools.gui.swing.contexttree.popup.SeparatorPopupComponent;
import org.geotools.map.MapContext;
import org.geotools.map.MapLayer;
import org.jdesktop.swingx.JXTreeTable;

/**
 * Dynamic Popup used by JXMapContextTree
 * @author johann Sorel
 *
 */
public class JContextTreePopup extends JPopupMenu {
    
    
    private ArrayList<PopupComponent> controls = new ArrayList<PopupComponent>();
    private ContextTreeTable treetable;
    
    
    /**
     * Creates a new instance of JXMapContextTreePopup
     * Dynamic Popup used by JXMapContextTree
     */
    JContextTreePopup() {
        super();
    }
    
    /**
     *
     * Creates a new instance of JXMapContextTreePopup
     * Dynamic Popup used by JXMapContextTree
     * @param tree the tree related to the poup
     * @param treetable 
     */
    JContextTreePopup(ContextTreeTable treetable) {
        super();
        this.treetable = treetable;
    }
    
    /**
     * active the defaults Popup controls
     */
    public void activeDefaultPopups(){
        
        //layer popup
        addPopControl(new LayerVisiblePopupComponent());
        addPopControl(new LayerFeaturePopupComponent());
        addSeparator(MapLayer.class);
        addPopControl(new LayerDeletePopupComponent());
        addSeparator(MapLayer.class);
        addPopControl(new LayerPropertyPopupComponent());
        
        //context popup
        addPopControl(new ContextActivePopupComponent(treetable));
        addSeparator(MapContext.class);
        addPopControl(new ContextDeletePopupComponent(treetable));
        addSeparator(MapContext.class);
        addPopControl(new ContextPropertyPopupComponent());
        
    }
    
    
    /**
     * Add a Control to the PopupMenu
     * @param control the new popup
     */
    public void addPopControl( PopupComponent control){
        controls.add(control);
    }
    
    /**
     * get the list of controls
     * @return list of JXMapContextTreePopControl
     */
    public ArrayList<PopupComponent> getControls() {
        return controls;
    }
    
    /**
     * Add a Separator in the popup for a specific classe
     * @param classe the classe where the separator will appear
     */
    public void addSeparator(Class classe){
        controls.add(new SeparatorPopupComponent(classe));
        
    }
    
    /**
     * show or hide the popupmenu
     * @param b the visible state
     */
    @Override
    public void setVisible(boolean b) {
        removeAll();
        
        
        ContextTreeNode node = null;
        
        if(treetable != null){
            try{
                Point location = treetable.getMousePosition();
                TreePath path = treetable.getPathForLocation(location.x, location.y);
                treetable.getTreeSelectionModel().setSelectionPath(path);
                node = (ContextTreeNode)treetable.getTreeSelectionModel().getSelectionPath().getLastPathComponent();
            }catch(Exception e){
                treetable.getTreeSelectionModel().setSelectionPath(null);
            }
        }
        
        if(  node!= null){
            Object obj = node.getUserObject() ;
            
            for( PopupComponent control : controls){
                
                if ( control.isValid(obj) ){
                    add( control.getComponent(obj,node) );
                }
                
            }
            super.setVisible(b);
        } else if( b == false) {
            super.setVisible(b);
        }
        
        
    }
    
    public void setTree(ContextTreeTable treetable) {
        this.treetable = treetable;
    }
    
    
    
    
    
}
