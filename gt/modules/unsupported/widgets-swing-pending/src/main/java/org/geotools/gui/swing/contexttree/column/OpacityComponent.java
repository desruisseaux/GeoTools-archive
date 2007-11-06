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

package org.geotools.gui.swing.contexttree.column;

import org.geotools.gui.swing.contexttree.renderer.*;
import java.awt.GridLayout;

import javax.swing.JPanel;
import javax.swing.JSlider;

import org.geotools.filter.Filters;
import org.geotools.map.MapLayer;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.LineSymbolizer;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.PolygonSymbolizer;
import org.geotools.styling.RasterSymbolizer;
import org.geotools.styling.Rule;
import org.geotools.styling.Symbolizer;

/**
 *
 * @author johann sorel
 */
public class OpacityComponent extends RendererAndEditorComponent {

    private JSlider slide = new JSlider(0, 100);
        
    public OpacityComponent() {       
        super();
        setLayout(new GridLayout(1,1));
        slide.setOpaque(false);
        slide.setPaintTicks(true);        
    }

    public void parse(Object value){
        double valeur = 1;
        boolean correct = false;

        if (value instanceof Double) {
            valeur = (Double) value;
            if (valeur > 1) {
                valeur = 1d;
            } else if (valeur < 0) {
                valeur = 0d;
            }
            correct = true;
        }

        valeur *= 100;
        slide.setValue(Double.valueOf(valeur).intValue());
        
        removeAll();
        if(correct){
           add(slide); 
        }
        
    }
    
    public Object getValue(){
        return slide.getValue() / 100d;
    }
    
    
}