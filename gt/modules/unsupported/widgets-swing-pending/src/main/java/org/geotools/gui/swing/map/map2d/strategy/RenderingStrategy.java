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
package org.geotools.gui.swing.map.map2d.strategy;

import com.vividsolutions.jts.geom.Envelope;
import java.awt.image.BufferedImage;
import javax.swing.JComponent;
import org.geotools.gui.swing.map.map2d.listener.StrategyListener;
import org.geotools.map.MapContext;
import org.geotools.map.MapLayer;
import org.geotools.renderer.GTRenderer;

/**
 *
 * @author Johann Sorel
 */
public interface RenderingStrategy {

    
    public void setRenderer(GTRenderer renderer);    
    public GTRenderer getRenderer();
    
    public BufferedImage createBufferImage(MapLayer layer);
    public BufferedImage createBufferImage(MapContext context);

    public void setContext(MapContext context);
    public MapContext getContext();
    
    public void setMapArea(Envelope area);
    public Envelope getMapArea();
            
    public void reset();
        
    public JComponent getComponent();
    
    public void addStrategyListener(StrategyListener listener);
    public void removeStrategyListener(StrategyListener listener);
    public StrategyListener[] getStrategyListeners();
    
    
    
}
