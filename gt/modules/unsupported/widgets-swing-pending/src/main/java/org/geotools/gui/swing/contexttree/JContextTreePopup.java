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

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.tree.TreePath;

import org.geotools.gui.swing.contexttree.popup.ContextActivePopupComponent;
import org.geotools.gui.swing.contexttree.popup.ContextDeletePopupComponent;
import org.geotools.gui.swing.contexttree.popup.ContextPropertyPopupComponent;
import org.geotools.gui.swing.contexttree.popup.CopyComponent;
import org.geotools.gui.swing.contexttree.popup.CutComponent;
import org.geotools.gui.swing.contexttree.popup.DuplicateComponent;
import org.geotools.gui.swing.contexttree.popup.LayerDeletePopupComponent;
import org.geotools.gui.swing.contexttree.popup.LayerFeaturePopupComponent;
import org.geotools.gui.swing.contexttree.popup.LayerPropertyPopupComponent;
import org.geotools.gui.swing.contexttree.popup.LayerVisiblePopupComponent;
import org.geotools.gui.swing.contexttree.popup.PasteComponent;
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
    private TreeTable treetable;

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
    JContextTreePopup(TreeTable treetable) {
        super();
        this.treetable = treetable;
    }

    /**
     * active the defaults Popup controls
     */
    public void activeDefaultPopups() {

        
        addPopControl(new LayerFeaturePopupComponent());            //layer
        addPopControl(new LayerVisiblePopupComponent());            //layer        
        addPopControl(new ContextActivePopupComponent(treetable));  //context
        
        addSeparator(Object.class);
        
        addPopControl(new CutComponent(treetable));                 //all
        addPopControl(new CopyComponent(treetable));                //all
        addPopControl(new PasteComponent(treetable));               //all
        addPopControl(new DuplicateComponent(treetable));           //all
        addPopControl(new LayerDeletePopupComponent());             //layer
        addPopControl(new ContextDeletePopupComponent(treetable));  //context
        
        addSeparator(Object.class);
        
        addPopControl(new LayerPropertyPopupComponent());           //layer
        addPopControl(new ContextPropertyPopupComponent());         //context

    }

    /**
     * Add a Control to the PopupMenu
     * @param control the new popup
     */
    public void addPopControl(PopupComponent control) {
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
    public void addSeparator(Class classe) {
        controls.add(new SeparatorPopupComponent(classe));

    }

    /**
     * show or hide the popupmenu
     * @param b the visible state
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


            for (PopupComponent control : controls) {
                if (control.isValid(nodeobjects)) {
                    add(control.getComponent(nodeobjects, nodes));
                }
            }
            removeLastSeparators();

        }

        super.setVisible(view);
    }

    private void removeLastSeparators() {
        if (getComponentCount() > 0) {
            while (getComponent(getComponentCount() - 1) instanceof SeparatorPopupComponent) {
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
            if (!(getComponent(getComponentCount() - 1) instanceof SeparatorPopupComponent && menuItem instanceof SeparatorPopupComponent)) {
                return super.add(menuItem);
            }
        }

        if ( !(menuItem instanceof SeparatorPopupComponent)){
            return super.add(menuItem);
        }        
        
        return null;

    }
}
