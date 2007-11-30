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
import java.awt.Component;
import org.geotools.gui.swing.map.Map;
import org.geotools.gui.swing.map.map2d.listener.Map2DListener;
import org.geotools.gui.swing.map.map2d.strategy.RenderingStrategy;
import org.geotools.map.MapContext;

/**
 *
 * @author Johann Sorel
 */
public interface Map2D extends Map{
    
    public void setContext(MapContext context);
    public MapContext getContext();
        
    public void setMapArea(Envelope mapArea);
    public Envelope getMapArea();
    
    public void setRenderingStrategy(RenderingStrategy strategy);
    public RenderingStrategy getRenderingStrategy();
                    
    public void refresh();
    
    public Component getComponent();    
    
    public void addMap2DListener(Map2DListener listener);
    public void removeMap2DListener(Map2DListener listener);
    public Map2DListener[] getMap2DListeners();
    
    
    
}
