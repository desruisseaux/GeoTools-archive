/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004, Geotools Project Managment Committee (PMC)
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

/**
 * Provides miscellaneous utility methods for use with WMSs.
 * 
 * @author Richard Gould
 */
public class Utils {
	
	/**
	 * Returns a list containing multiple SimpleLayer instances,  
	 * representing a layer to be drawn and all available styles that
	 * can be used to draw it with. The Style value can be empty.
	 * 
	 * @param rootLayer The layer to start processing from
	 * @return a list of type SimpleLayer
	 */
	public static SimpleLayer[] findDrawableLayers(Layer[] layers) {
		List drawableLayers = new ArrayList();
		for (int i = 0; i < layers.length; i++) {
			if (layers[i].getName() == null || layers[i].getName().length() == 0){
				continue;
			}
			Layer parentLayer = layers[i].getParent();
			Set styles = new TreeSet();
			if (layers[i].getStyles() != null) {
				styles.addAll(layers[i].getStyles());
			}
			while (parentLayer != null) {
				if (layers[i].getStyles() != null) {
					styles.addAll(parentLayer.getStyles());
				}
				parentLayer = parentLayer.getParent();
			}
			SimpleLayer layer = new SimpleLayer(layers[i].getName(), styles);
			drawableLayers.add(layer);
		}
		
		SimpleLayer[] simpleLayers = new SimpleLayer[drawableLayers.size()];
		for (int i = 0; i < drawableLayers.size(); i++) {
			simpleLayers[i] = (SimpleLayer) drawableLayers.get(i);
		}

		return simpleLayers;
	}
}
