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

import com.vividsolutions.jts.geom.Envelope;
import org.geotools.feature.FeatureCollection;
import org.geotools.gui.swing.map.MapConstants;
import org.geotools.map.MapLayer;

/**
 *
 * @author Johann Sorel
 */
public interface SelectableMap2D extends Map2D{
    
    public void setSelectionEnable(boolean enable);
    public boolean isSelectionEnabled();
    
    public void setSelectableLayer(MapLayer layer);
    public void setSelectableLayer(MapLayer[] layer);
    public MapLayer[] getSelectableLayer();
    
    public void doSelection(double x, double y);
    public void doSelection(Envelope env);
    
    public void setSelection(FeatureCollection selection);
    public FeatureCollection getSelection();
      
}
