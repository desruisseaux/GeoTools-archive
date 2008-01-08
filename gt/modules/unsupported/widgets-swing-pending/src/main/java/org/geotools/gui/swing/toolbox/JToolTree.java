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

package org.geotools.gui.swing.toolbox;

import java.awt.BorderLayout;
import java.awt.ComponentOrientation;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

/**
 *
 * @author Johann Sorel
 */
public class JToolTree extends JComponent{

    private final TreeTable treetable;
    
    public JToolTree(){
        treetable = new TreeTable(this);
        
        JScrollPane pane = new JScrollPane(treetable);
        pane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        pane.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        
        setLayout(new BorderLayout());
        
        add(BorderLayout.CENTER,pane);        
    }
    
    public void addTool(TreeToolDescriptor tool){
        treetable.addTool(tool);
    }
    
    public void removeTool(TreeToolDescriptor tool){
        treetable.removeTool(tool);
    }
    
    public TreeToolDescriptor[] getTreeToolDescriptors(){
        return treetable.getTreeToolDescriptors();
    }
    
    /**
     * add ToolTreeListener
     * @param listener
     */
    public void addToolTreeListener(ToolTreeListener listener) {
        treetable.addToolTreeListener(listener);
    }

    /**
     * remove ToolTreeListener
     * @param listener to remove
     */
    public void removeToolTreeListener(ToolTreeListener listener) {
        treetable.removeToolTreeListener(listener);
    }

    /**
     * get ToolTreeListener list
     * @return the listener's table
     */
    public ToolTreeListener[] getToolTreeListeners() {
        return treetable.getToolTreeListeners();
    }
    
    
}
