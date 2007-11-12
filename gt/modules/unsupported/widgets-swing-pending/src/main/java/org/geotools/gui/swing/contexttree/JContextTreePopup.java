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

import java.util.ArrayList;

import java.util.Collection;
import javax.swing.JPopupMenu;

import org.geotools.gui.swing.contexttree.popup.MapRelatedTreePopupItem;
import org.geotools.gui.swing.contexttree.popup.TreePopupItem;
import org.geotools.gui.swing.map.Map;
import org.geotools.gui.swing.map.map2d.Map2D;

/**
 * Dynamic Popup used by JXMapContextTree
 * @author johann Sorel
 *
 */
public final class JContextTreePopup {

    private final TreePopup popup;
    final ArrayList<TreePopupItem> controls = new ArrayList<TreePopupItem>();
    private final TreeTable treetable;
    private final JContextTree frame;
    private Map map;

    
    /**
     *
     * Creates a new instance of JXMapContextTreePopup
     * Dynamic Popup used by JXMapContextTree
     * @param tree the tree related to the poup
     * @param treetable 
     */
    JContextTreePopup(TreeTable treetable,JContextTree frame) {
        this(treetable,frame,null);
    }
    
    /**
     * Creates a new instance of JXMapContextTreePopup
     * Dynamic Popup used by JXMapContextTree
     * 
     * @param map 
     * @param treetable the tree related to the popup
     */
    JContextTreePopup(TreeTable treetable,JContextTree frame,Map2D map) {
        super();
        this.treetable = treetable;
        this.frame = frame;
        this.map = map;
        this.popup = new TreePopup(treetable, this);
    }
        
    JPopupMenu getPopupMenu(){
        return popup;
    }
       
    
    public void setMap(Map map){
        this.map = map;
        
        for(TreePopupItem pc : controls){
            
            if( pc instanceof MapRelatedTreePopupItem){
                ((MapRelatedTreePopupItem)pc).setMap(map);
            }
        }
    }
       
    /**
     * Add a Control to the PopupMenu
     * @param control the new popup
     * @return true is succesfully added
     */
    public boolean addItem(TreePopupItem control) {
        return controls.add(control);
    }
    
    /**
     * Add a Control to the PopupMenu
     * @param index 
     * @param control the new popup
     */
    public void addItem(int index, TreePopupItem control) {
        controls.add(index,control);
    }
    
    /**
     * Add a Control to the PopupMenu
     * @param index index of the first item
     * @param col Collection of TreePopupItem
     * @return true is succesfully added
     */
    public boolean addAllItem(int index, Collection <? extends TreePopupItem> col) {
        return controls.addAll(index,col);
    }
    
    /**
     * Add a Control to the PopupMenu
     * @param col Collection of TreePopupItem
     * @return true is succesfully added
     */
    public boolean addAllItem(Collection <? extends TreePopupItem> col) {
        return controls.addAll(col);
    }
    
    
    /**
     * @param control
     * @return true if TreePopupItem successfuly removed
     */
    public boolean removeItem(TreePopupItem control){
        return controls.remove(control);
    }
        
    /**
     * @param index
     * @return the removed TreePopupItem
     */
    public TreePopupItem removeItem(int index){
        return controls.remove(index);
    }
        
    /**
     * @return array of TreePopupItem or null if no TreePopupItem
     */
    public TreePopupItem[] getControls() {
        if(controls.size() > 0){
            return controls.toArray( new TreePopupItem[controls.size()]);
        }else{
            return null;
        }
    }

    
    
    
    
}
