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
package org.geotools.gui.swing.map.map2d;

import java.util.Observable;
import javax.swing.JComponent;
import org.geotools.map.event.MapLayerListEvent;

/**
 *
 * @author Johann Sorel
 */
public abstract class RenderingStrategy extends Observable{

        
    public abstract void redraw(boolean complete);

    public abstract void layerChanged(MapLayerListEvent event);

    public abstract void layerDeleted(MapLayerListEvent event);

    public abstract void layerAdded(MapLayerListEvent event);

    public abstract void layerMoved(MapLayerListEvent event);
        
    public abstract JComponent getComponent();
    
    
    
}
