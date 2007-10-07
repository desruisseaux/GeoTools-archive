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

package org.geotools.gui.swing.contexttree.renderer;

import java.awt.Graphics;

import org.geotools.gui.swing.misc.Render.RandomStyleFactory;
import org.geotools.map.MapLayer;
import org.geotools.styling.Symbolizer;
import org.jdesktop.swingx.renderer.JRendererLabel;

/**
 *
 * @author johann sorel
 */
public class StyleViewComponent extends JRendererLabel{
    
    private MapLayer layer = null;
    private Symbolizer symbol = null;
    
    /** Creates a new instance of StyleViewComponent */
    public StyleViewComponent() {
        super();
    }
    
    public void format(Object obj){
        layer = null;
        symbol = null;
        
        if(obj instanceof MapLayer)
            layer = (MapLayer) obj ;
        else if(obj instanceof Symbolizer){
            symbol = (Symbolizer) obj;
        }
            
    }
    
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);      
        
        if( layer != null ){
            g.drawImage( RandomStyleFactory.createGlyph(layer), (getWidth()-16)/2,(getHeight()-16)/2,this);
        }
        else if( symbol != null ){
            g.drawImage( RandomStyleFactory.createGlyph(symbol), (getWidth()-16)/2,(getHeight()-16)/2,this);
        }
    }
    
}
