/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
 *
 */
package org.geotools.data.wms;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.geotools.data.ows.Layer;
import org.geotools.data.ows.WMSCapabilities;


/**
 * Provides miscellaneous utility methods for use with WMSs.
 *
 * @author Richard Gould
 */
public class WMSUtils {
    /**
     * Returns a list containing multiple SimpleLayer instances, representing a
     * layer to be drawn and all available styles that can be used to draw it
     * with. The Style value can be empty.
     *
     * @param layers The list of layers to process
     *
     * @return a list of type SimpleLayer
     */
    public static SimpleLayer[] findDrawableLayers(List layers) {
        List drawableLayers = new ArrayList();

        for (int i = 0; i < layers.size(); i++) {
            Layer layer = (Layer) layers.get(i);
            if ((layer.getName() == null)
                    || (layer.getName().length() == 0)) {
                continue;
            }

            Layer parentLayer = layer.getParent();
            Set styles = new TreeSet();

            if (layer.getStyles() != null) {
                styles.addAll(layer.getStyles());
            }

            while (parentLayer != null) {
                if (layer.getStyles() != null) {
                    styles.addAll(parentLayer.getStyles());
                }

                parentLayer = parentLayer.getParent();
            }

            SimpleLayer sLayer = new SimpleLayer(layer.getName(), styles);
            drawableLayers.add(sLayer);
        }

        SimpleLayer[] simpleLayers = new SimpleLayer[drawableLayers.size()];

        for (int i = 0; i < drawableLayers.size(); i++) {
            simpleLayers[i] = (SimpleLayer) drawableLayers.get(i);
        }

        return simpleLayers;
    }

    /**
     * Utility method to return each layer that has a name. This method maintains no hierarchy at all.
     * 
     * @return An array of Layers, each value has a it's name property set or an empty array if there are none. It will return null if there is no capabilities document
     * 
     */
    public static Layer[] getNamedLayers(WMSCapabilities capabilities) {
    	
    	if (capabilities == null) {
    		return null;
    	}
    	
        List namedLayersList = new ArrayList();
        
        Layer[] layers = (Layer[]) capabilities.getLayerList().toArray(new Layer[capabilities.getLayerList().size()]);
    
        for( int i = 0; i < layers.length; i++ ) {
            if ((layers[i].getName() != null) && (layers[i].getName().length() != 0)) {
                namedLayersList.add(layers[i]);
            }
        }
    
        Layer[] namedLayers = new Layer[namedLayersList.size()];
        for (int i = 0; i < namedLayersList.size(); i++) {
            namedLayers[i] = (Layer) namedLayersList.get(i);
        }
        
        return namedLayers;
    }

    public static Set getQueryableLayers(WMSCapabilities capabilities) {
        Set layers = new TreeSet();
    
        Layer[] namedLayers = getNamedLayers(capabilities);
    
        for( int i = 0; i < namedLayers.length; i++ ) {
            Layer layer = namedLayers[i];
    
            if (layer.isQueryable()) {
                layers.add(layer);
            }
        }
    
        return layers;
    }

    public static Set getSRSs(WMSCapabilities capabilities) {
        Set srss = new TreeSet();
    
        Layer[] layers = (Layer[]) capabilities.getLayerList().toArray(new Layer[capabilities.getLayerList().size()]);
    
        for( int i = 0; i < layers.length; i++ ) {
            if (layers[i].getSrs() != null) {
                srss.addAll(layers[i].getSrs());
            }
        }
    
        return srss;
    }
}
