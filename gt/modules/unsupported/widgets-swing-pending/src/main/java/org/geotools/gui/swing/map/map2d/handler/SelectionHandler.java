/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2003-2006, GeoTools Project Managment Committee (PMC)
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

package org.geotools.gui.swing.map.map2d.handler;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import org.geotools.gui.swing.map.map2d.SelectableMap2D;

/**
 *
 * @author johann sorel
 */
public interface SelectionHandler {

        
    /**
     * 
     * @param map2d
     * @param mapComponent
     */
    void install(SelectableMap2D map2d);
    
    /**
     * 
     */
    void uninstall();
    
    /**
     * 
     * @return
     */
    boolean isInstalled();
    
    String getTitle();
    
    ImageIcon getIcon();
    
}
