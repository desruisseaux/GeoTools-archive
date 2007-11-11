/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, GeoTools Project Managment Committee (PMC)
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

import javax.swing.event.EventListenerList;
import org.geotools.gui.swing.map.map2d.event.SelectionChangeListener;
import org.geotools.gui.swing.map.map2d.event.SelectionChangedEvent;
import org.geotools.map.MapLayer;
import org.opengis.filter.Filter;

/**
 * default selection manager
 * @author Ian Turton, Johann Sorel
 */
public class DefaultSelectionManager implements SelectionManager{

    EventListenerList listeners = new EventListenerList();
    MapLayer selectionLayer;


    public DefaultSelectionManager(MapLayer layer) {  
        setSelectionLayer(layer);
    }

    public void addSelectionChangeListener(SelectionChangeListener l) {
        listeners.add(SelectionChangeListener.class, l);
    }

    public void removeSelectionChangeListener(SelectionChangeListener l) {
        listeners.remove(SelectionChangeListener.class, l);
    }
    
    public SelectionChangeListener[] getSelectionChangeListeners() {
        return listeners.getListeners( SelectionChangeListener.class );
    }
    
    public void fireSelectionChanged(Object source, Filter filter) {
        SelectionChangeListener[] l = (SelectionChangeListener[]) listeners.getListeners(SelectionChangeListener.class);
        SelectionChangedEvent ev = new SelectionChangedEvent(source, filter);

        for(SelectionChangeListener listen : l) {
            listen.selectionChanged(ev);
        }
    }

    public MapLayer getSelectionLayer() {
        return selectionLayer;
    }

    public void setSelectionLayer(MapLayer selectionLayer) {
        this.selectionLayer = selectionLayer;

    }

    
}
