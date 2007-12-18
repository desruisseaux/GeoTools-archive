/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2006, GeoTools Project Managment Committee (PMC)
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

package org.geotools.gui.swing.map.map2d.listener;

import java.util.EventListener;
import org.geotools.gui.swing.map.map2d.event.Map2DEditLayerEvent;
import org.geotools.gui.swing.map.map2d.event.Map2DEditStateEvent;

/**
 * EditableMap2DListener used to listen to Map2D edition events
 * @author Johann Sorel
 */
public interface EditableMap2DListener extends EventListener{

    /**
     * called when edition state change
     * @param event : Map2DEditStateEvent
     */
    public void mapEditStateChanged(Map2DEditStateEvent event);
    
    /**
     * called when the edited layer change
     * @param event : Map2DEditLayerEvent
     */
    public void mapEditLayerChanged(Map2DEditLayerEvent event);
    
}
