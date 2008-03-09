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

package org.geotools.gui.swing.style;

import javax.swing.JComponent;

import org.geotools.map.MapLayer;
import org.geotools.styling.Style;
import org.geotools.styling.Symbolizer;

/**
 * @author Johann Sorel
 */
public interface SymbolizerPanel {

    
    public void setLayer(MapLayer layer);
    
    public MapLayer getLayer();
    
    /**
     * 
     * @return return a Point,ligne,polygon or raster symbolizer
     */
    public Symbolizer getSymbolizer();
    
    public void setSymbolizer(Symbolizer symbol);
    
    /**
     * 
     * @return return a Style with only one rule and the symbolizer
     */
    public Style getStyle();
    
    public void setStyle(Style style);
            
    /**
     * 
     * @return return the Component for symbolizer edition
     */
    public JComponent getComponent();
    
}
