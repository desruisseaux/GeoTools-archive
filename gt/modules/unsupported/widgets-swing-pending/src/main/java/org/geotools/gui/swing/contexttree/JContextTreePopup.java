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
import org.geotools.gui.swing.contexttree.popup.ContextActiveTreePopupItem;
import org.geotools.gui.swing.contexttree.popup.ContextPropertyTreePopupItem;
import org.geotools.gui.swing.contexttree.popup.CopyTreePopupItem;
import org.geotools.gui.swing.contexttree.popup.CutTreePopupItem;
import org.geotools.gui.swing.contexttree.popup.DeleteTreePopupItem;
import org.geotools.gui.swing.contexttree.popup.DuplicateTreePopupItem;
import org.geotools.gui.swing.contexttree.popup.LayerFeatureTreePopupItem;
import org.geotools.gui.swing.contexttree.popup.LayerPropertyTreePopupItem;
import org.geotools.gui.swing.contexttree.popup.LayerVisibleTreePopupItem2;
import org.geotools.gui.swing.contexttree.popup.LayerZoomTreePopupItem;
import org.geotools.gui.swing.contexttree.popup.MapRelatedTreePopupItem;
import org.geotools.gui.swing.contexttree.popup.PasteTreePopupItem;
import org.geotools.gui.swing.contexttree.popup.TreePopupItem;
import org.geotools.gui.swing.contexttree.popup.SeparatorTreePopupItem;

/**
 * Dynamic Popup used by JXMapContextTree
 * @author johann Sorel
 *
 */
public class JContextTreePopup extends JPopupMenu {

    private ArrayList<TreePopupItem> controls = new ArrayList<TreePopupItem>();
    private TreeTable treetable;
    private JMapPane map;

    /**
     * Creates a new instance of JXMapContextTreePopup
     * Dynamic Popup used by JXMapContextTree
     */
    public JContextTreePopup() {
        this(null,null);
    }

    /**
     *
     * Creates a new instance of JXMapContextTreePopup
     * Dynamic Popup used by JXMapContextTree
     * @param tree the tree related to the poup
     * @param treetable 
     */
    public JContextTreePopup(TreeTable treetable) {
        this(treetable,null);
    }
    
    /**
     * Creates a new instance of JXMapContextTreePopup
     * Dynamic Popup used by JXMapContextTree
     * 
     * @param map 
     * @param treetable the tree related to the popup
     */
    public JContextTreePopup(TreeTable treetable,JMapPane map) {
        super();
        this.treetable = treetable;
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
     * active the defaults Popup controls
     */
    public void activeDefaultPopups() {

        
        addPopControl(new LayerVisibleTreePopupItem2());            //layer 
        
        addSeparator(Object.class);
        
        addPopControl(new LayerZoomTreePopupItem(map));            //layer
        addPopControl(new LayerFeatureTreePopupItem());            //layer
        addPopControl(new ContextActiveTreePopupItem(treetable));  //context

        addSeparator(Object.class);

        addPopControl(new CutTreePopupItem(treetable));                 //all
        addPopControl(new CopyTreePopupItem(treetable));                //all
        addPopControl(new PasteTreePopupItem(treetable));               //all
        addPopControl(new DuplicateTreePopupItem(treetable));           //all
        
        addSeparator(Object.class);
        
        addPopControl(new DeleteTreePopupItem(treetable));              //all

        addSeparator(Object.class);

        addPopControl(new LayerPropertyTreePopupItem());           //layer
        addPopControl(new ContextPropertyTreePopupItem());         //context

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
     * Add a Separator in the popup for a specific classe
     * @param classe the classe where the separator will appear
     */
    public void addSeparator(Class classe) {
        controls.add(new SeparatorTreePopupItem(classe));

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


            Object[] nodeobjects =
                    
               
               {};
            ContextTreeNode[] nodes = {};

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

                    nodeobjects = new Object[paths.length];
                    nodes = new ContextTreeNode[paths.length];

                    for (int i = 0; i < paths.length; i++) {
                        nodeobjects[i] = ((ContextTreeNode) paths[i].getLastPathComponent()).getUserObject();
                        nodes[i] = (ContextTreeNode) paths[i].getLastPathComponent();
                    }
                }

            }


            for (TreePopupItem control : controls) {
                if (control.isValid(nodeobjects)) {
                    add(control.getComponent(nodeobjects, nodes));
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
            while (getComponent(getComponentCount() - 1) instanceof SeparatorTreePopupItem) {
                remove(getComponentCount() - 1);
            }
        }
    }

    public void setTree(TreeTable treetable) {
        this.treetable = treetable;
    }

    @Override
    public Component add(Component menuItem) {

        if (getComponentCount() > 0) {
            if (!(getComponent(getComponentCount() - 1) instanceof SeparatorTreePopupItem && menuItem instanceof SeparatorTreePopupItem)) {
                return super.add(menuItem);
            }
        }

        if (!(menuItem instanceof SeparatorTreePopupItem)) {
            return super.add(menuItem);
        }

        return null;

    }
}
