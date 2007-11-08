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

import java.util.EventListener;

/**
 * Listener for ContextTreeModel
 * 
 * @author johann sorel
 */
public interface TreeListener extends EventListener{
        
    /**
     * When a Context is added
     * 
     * @param event the event
     */
    public void ContextAdded(TreeEvent event) ;
      
    /**
     * When a Context is removed
     * 
     * @param event the event
     */
    public void ContextRemoved(TreeEvent event);
      
    /**
     * When a Context is activated
     * 
     * @param event the event
     */
    public void ContextActivated(TreeEvent event);
      
    /**
     * When a Context moved
     * 
     * @param event the event
     */
    public void ContextMoved(TreeEvent event);
        
}
