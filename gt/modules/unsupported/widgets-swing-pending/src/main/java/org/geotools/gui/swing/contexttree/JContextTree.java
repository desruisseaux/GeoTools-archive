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

import java.awt.ComponentOrientation;
import java.awt.GridLayout;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.tree.TreePath;

import org.geotools.gui.swing.contexttree.column.TreeTableColumn;
import org.geotools.gui.swing.contexttree.column.OpacityTreeTableColumn;
import org.geotools.gui.swing.contexttree.column.StyleTreeTableColumn;
import org.geotools.gui.swing.contexttree.column.VisibleTreeTableColumn;
import org.geotools.map.MapContext;

/**
 * Tree Component for easy MapContext and MapLayer management
 * @author johann sorel
 */
public class JContextTree extends JPanel {

    
    private ContextTreeTable tree = null;
    private ArrayList<TreeTableColumn> columns = new ArrayList<TreeTableColumn>();

    /**
     * constructor
     */
    public JContextTree() {
        this(false);
    }

    public JContextTree(boolean complete) {
        super(new GridLayout(1, 1));
        tree = new ContextTreeTable(complete);
        init();


    }

    private void init() {        
        JScrollPane pane = new JScrollPane(getTreeTable());
        pane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        pane.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        add(pane);
    }

   
    /**
     * get the swinglabs jxtreetable. 
     * @return JXTreeTable
     */
    public ContextTreeTable getTreeTable() {
        return tree;
    }

  

}
