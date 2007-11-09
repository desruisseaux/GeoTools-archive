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

import java.awt.Component;
import java.awt.Point;
import java.util.ArrayList;

import javax.swing.JPopupMenu;
import javax.swing.tree.TreePath;

import org.geotools.gui.swing.JMapPane;
import org.geotools.gui.swing.contexttree.popup.ContextActiveItem;
import org.geotools.gui.swing.contexttree.popup.ContextPropertyItem;
import org.geotools.gui.swing.contexttree.popup.CopyItem;
import org.geotools.gui.swing.contexttree.popup.CutItem;
import org.geotools.gui.swing.contexttree.popup.DeleteItem;
import org.geotools.gui.swing.contexttree.popup.DuplicateItem;
import org.geotools.gui.swing.contexttree.popup.LayerFeatureItem;
import org.geotools.gui.swing.contexttree.popup.LayerPropertyItem;
import org.geotools.gui.swing.contexttree.popup.LayerVisibilityItem;
import org.geotools.gui.swing.contexttree.popup.LayerZoomItem;
import org.geotools.gui.swing.contexttree.popup.MapRelatedTreePopupItem;
import org.geotools.gui.swing.contexttree.popup.PasteItem;
import org.geotools.gui.swing.contexttree.SelectionData;
import org.geotools.gui.swing.contexttree.popup.TreePopupItem;
import org.geotools.gui.swing.contexttree.popup.SeparatorItem;
import org.geotools.map.MapContext;
import org.geotools.map.MapLayer;

/**
 * Dynamic Popup used by JXMapContextTree
 * @author johann Sorel
 *
 */
public final class JContextTreePopup extends JPopupMenu {

    private final ArrayList<TreePopupItem> controls = new ArrayList<TreePopupItem>();
    private final TreeTable treetable;
    private final JContextTree frame;
    private JMapPane map;

    
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
    JContextTreePopup(TreeTable treetable,JContextTree frame,JMapPane map) {
        super();
        this.treetable = treetable;
        this.frame = frame;
        this.map = map;
    }
        
    public void setMapPane(JMapPane map){
        this.map = map;
        
        for(TreePopupItem pc : controls){
            
            if( pc instanceof MapRelatedTreePopupItem){
                ((MapRelatedTreePopupItem)pc).setMapPane(map);
            }
        }
    }
    
   
    /**
     * Add a Control to the PopupMenu
     * @param control the new popup
     */
    public void addPopControl(TreePopupItem control) {
        controls.add(control);
    }

    /**
     * get the list of controls
     * @return list of JXMapContextTreePopControl
     */
    public ArrayList<TreePopupItem> getControls() {
        return controls;
    }

    /**
     * Add a Separator in the popup
     */
    @Override
    public void addSeparator() {
        controls.add(new SeparatorItem());
    }

    /**
     * will not be set visible if nothing is in the popup
     * 
     * @param view 
     */
    @Override
    public void setVisible(boolean view) {


        if (view) {
            removeAll();

            SelectionData[] selection = {};

            if (treetable != null) {

                Point location = treetable.getMousePosition();
                if (location != null) {
                    TreePath path = treetable.getPathForLocation(location.x, location.y);

                    if (path == null) {
                        treetable.getTreeSelectionModel().clearSelection();
                    } else {
                        treetable.getTreeSelectionModel().addSelectionPath(path);
                    }
                } else {
                    treetable.getTreeSelectionModel().clearSelection();
                }

                TreePath[] paths = treetable.getTreeSelectionModel().getSelectionPaths();

                if (paths != null) {

                    selection = new SelectionData[paths.length];

                    for (int i = 0; i < paths.length; i++) {
                        
                        ContextTreeNode lastnode = (ContextTreeNode) paths[i].getLastPathComponent();
                        Object last = lastnode.getUserObject();
                        
                        if(last instanceof MapLayer){
                            MapLayer layer = (MapLayer) last;
                            MapContext context = (MapContext) ((ContextTreeNode)lastnode.getParent()).getUserObject();
                            SelectionData data = new SelectionData(context,layer);
                            selection[i] = data;
                        }
                        else{
                            MapContext context = (MapContext) last;
                            SelectionData data = new SelectionData(context,null);
                            selection[i] = data;
                        }
                        
                    }
                }

            }


            for (TreePopupItem control : controls) {
                if (control.isValid(selection)) {
                    add(control.getComponent(selection));
                }
            }
            removeLastSeparators();

            if (getComponentCount() > 0) {
                super.setVisible(view);
            }
        }else{
            super.setVisible(view);
        }


        


    }

    private void removeLastSeparators() {
        if (getComponentCount() > 0) {
            while (getComponent(getComponentCount() - 1) instanceof SeparatorItem) {
                remove(getComponentCount() - 1);
            }
        }
    }

    
    @Override
    public Component add(Component menuItem) {

        if (getComponentCount() > 0) {
            if (!(getComponent(getComponentCount() - 1) instanceof SeparatorItem && menuItem instanceof SeparatorItem)) {
                return super.add(menuItem);
            }
        }

        if (!(menuItem instanceof SeparatorItem)) {
            return super.add(menuItem);
        }

        return null;

    }
}
