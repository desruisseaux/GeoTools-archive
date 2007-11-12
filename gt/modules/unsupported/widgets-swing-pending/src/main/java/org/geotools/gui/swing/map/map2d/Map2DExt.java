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

import org.geotools.gui.swing.map.map2d.temp.SelectionManager;
import org.geotools.gui.swing.map.map2d.temp.HighlightManager;
import org.geotools.gui.swing.map.map2d.temp.StyleSetDescriptor;
import java.awt.Cursor;
import org.geotools.feature.FeatureCollection;
import org.geotools.map.MapLayer;

/**
 *
 * @author Johann Sorel
 */
public interface Map2DExt extends Map2D{

    
    public void setClickable(boolean clickable);
    public boolean isClickable();
        
    public void setCursor(Cursor cursor);
    public Cursor getCursor();
    
    //------------------------HIGHLIGHT-----------------------------------------
    public void setHighlightManager(HighlightManager highlightManager);
    public HighlightManager getHighlightManager();
        
    public void setHighlightEnabled(boolean highlight);
    public boolean isHighlightEnabled();
    public boolean isMapHighlightable();
            
    public void setHighlightLayer(MapLayer highlightLayers);
    public MapLayer getHighlightLayer();
    
    public void setHighLightDescriptor( StyleSetDescriptor descriptor);
    public StyleSetDescriptor getHighLightDescriptor();
    
            
    //---------------------SELECTION--------------------------------------------    
    public void setSelectionManager(SelectionManager selectionManager);
    public SelectionManager getSelectionManager();
    
    public void setSelectionEnabled(boolean selection);
    public boolean isSelectionEnabled();
    public boolean isMapSelectable();
    
    public void setSelectionLayer(MapLayer selectionLayers);
    public MapLayer getSelectionLayer();
    
    public void setSelectionDescriptor( StyleSetDescriptor descriptor);
    public StyleSetDescriptor getSelectionDescriptor();
    
    public void doSelection(double x, double y, MapLayer layer);
    public void doSelection(double topx, double topy, double bottomx, double bottomy );
    
    public void setSelection(FeatureCollection selection);
    public FeatureCollection getSelection();
    
    
    //--------------------EDITION-----------------------------------------------
    //TODO MUST BE COMPLETED
    
}
