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

import org.geotools.gui.swing.map.map2d.listener.SelectableMap2DListener;
import org.geotools.map.MapLayer;

/**
 *
 * @author Johann Sorel
 */
public interface SelectableMap2D extends NavigableMap2D{
        
    public void addSelectableLayer(MapLayer layer);
    public void addSelectableLayer(MapLayer[] layer);
    public void removeSelectableLayer(MapLayer layer);
    public MapLayer[] getSelectableLayer();
    public boolean isLayerSelectable(MapLayer layer);
    
    public void doSelection(double x, double y);
//    public void doSelection(Envelope env);
    
//    public void setSelection(FeatureCollection selection);
//    public FeatureCollection getSelection();
    
    
    public void addSelectableMap2DListener(SelectableMap2DListener listener);
    public void removeSelectableMap2DListener(SelectableMap2DListener listener);
    public SelectableMap2DListener[] getSelectableMap2DListeners();
    
      
}
