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

import org.geotools.map.MapContext;
import org.geotools.map.MapLayer;
import org.jdesktop.swingx.treetable.AbstractMutableTreeTableNode;
import org.jdesktop.swingx.treetable.TreeTableNode;

/**
 * a specific mutabletreenode for jcontexttree
 * @author johann sorel
 * Node for JXMapContextTree
 */
public class ContextTreeNode extends AbstractMutableTreeTableNode{
    
    
    private ContextTreeModel model = null;
    
    /**
     * Creates a new instance of JXMapContextTreeNode
     * @param model model of the tree
     */
    public ContextTreeNode(ContextTreeModel model) {
        super();
        this.model = model;
    }
    
    /**
     * set the model to use
     * @param model the new jcontexttreemodel
     */
    public void setModel(ContextTreeModel model){
        this.model = model;
    }
    
    
    /**
     * find if a node is an ancetor of another
     * @param anotherNode the node to compare with
     * @return true is anotherNode is an ancestor of node
     */
    public boolean isNodeAncestor(ContextTreeNode anotherNode) {
        
        if (anotherNode == null) {
            return false;
        }
        
        TreeTableNode ancestor = this;
        
        do {
            if (ancestor == anotherNode) {
                return true;
            }
        } while((ancestor = ancestor.getParent()) != null);
        
        return false;
        
    }
    
    
    
    
    /**
     * get a object at column
     * @param column number of the column
     * @return object at column
     */
    public Object getValueAt(int column) {
        
        Object res = "n/a";
        
        if(column == ContextTreeModel.TREE){
            if(getUserObject() instanceof MapContext)
                res = ((MapContext)getUserObject()).getTitle();
            else if(getUserObject() instanceof MapLayer)
                res = ((MapLayer)getUserObject()).getTitle();
            else
                res = "n/a";
        }else{
            if(column <= model.getColumnModels().size()){
                res = model.getColumnModels().get(column-1).getValue(getUserObject());
            } else{
                res = "n/a";
            }
        }
        
        
        
        return res;
        
    }
    
    
    /**
     * set a new object at specific place
     * @param aValue the new value
     * @param column column number
     */
    @Override
    public void setValueAt(Object aValue, int column){
        
        if(column == ContextTreeModel.TREE){
            if(getUserObject() instanceof MapContext)
                ((MapContext)getUserObject()).setTitle((String)aValue);
            else if(getUserObject() instanceof MapLayer)
                ((MapLayer)getUserObject()).setTitle((String)aValue);
            
        }else{
            if(column <= model.getColumnModels().size())
                model.getColumnModels().get(column-1).setValue(getUserObject(),aValue);
            
        }
        
    }

    /**
     * get the number of columns
     * @return the number of columns
     */
    public int getColumnCount() {
        return model.getColumnCount();
    }
    
    
    
    
}
