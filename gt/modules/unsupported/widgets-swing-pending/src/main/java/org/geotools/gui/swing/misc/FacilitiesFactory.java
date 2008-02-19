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
package org.geotools.gui.swing.misc;

import org.geotools.data.FeatureSource;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.DefaultMapLayer;
import org.geotools.map.MapContext;
import org.geotools.map.MapLayer;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

/**
 *
 * @author johann sorel
 */
public class FacilitiesFactory {

    
    public MapContext[] duplicateContexts(MapContext[] contexts){
        
        if(contexts != null){
            MapContext[] copys = new MapContext[contexts.length];
            
            for(int i=0;i<contexts.length;i++){
                copys[i] = duplicateContext(contexts[i]);
            }
            
            return copys;
        }else{
            return null;
        }
        
    }
    
    
    public MapContext duplicateContext(MapContext context) {

        if (context != null) {
            DefaultMapContext copycontext = new DefaultMapContext(context.getCoordinateReferenceSystem());
            MapLayer[] layers = duplicateLayers(context.getLayers());
            copycontext.addLayers(  layers );
            copycontext.setTitle(context.getTitle());

            return copycontext;
        } else {
            return null;
        }

    }

    
    
    public MapLayer[] duplicateLayers(MapLayer[] layers){
        
        if(layers != null){
            MapLayer[] copys = new MapLayer[layers.length];
            
            for(int i=0;i<layers.length;i++){
                copys[i] = duplicateLayer(layers[i]);
            }
            
            return copys;
        }else{
            return null;
        }
        
    }
    
    public MapLayer duplicateLayer(MapLayer layer) {

        if (layer != null) {

            MapLayer copy = new DefaultMapLayer((FeatureSource<SimpleFeatureType, SimpleFeature>) layer.getFeatureSource(), layer.getStyle(), layer.getTitle());
            copy.setQuery(layer.getQuery());
            copy.setVisible(layer.isVisible());

            return copy;
        } else {
            return null;
        }

    }
}
