/*
 * Created on Aug 16, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.data.wms;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.geotools.data.wms.getCapabilities.Layer;
import org.geotools.data.wms.getCapabilities.Style;

/**
 * @author Richard Gould
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
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
	public static List retrieveLayers(Layer rootLayer) {
		ArrayList layers = new ArrayList();
		retrieveLayers(rootLayer, layers, null);
		return layers;
	}
	
	private static void retrieveLayers(Layer layer, List finalLayers, Set parentStyles) {
		Set layerStyles = new TreeSet();
		Iterator iterator = layer.getStyles().iterator();
		while (iterator.hasNext()) {
			Style style = (Style) iterator.next();
			layerStyles.add(style.getName());
		}
		if (parentStyles != null) {
			layerStyles.addAll(parentStyles);
		}
		
		if (layer.getName() != null && layer.getName() != "") {
			SimpleLayer simpleLayer = new SimpleLayer(layer.getName(), layerStyles);
			finalLayers.add(simpleLayer);
		}
		
		if (layer.getSubLayers() != null) {
			Iterator iter = layer.getSubLayers().iterator();
			while (iter.hasNext()) {
				retrieveLayers((Layer) iter.next(), finalLayers, layerStyles);
			}
		}
	}
	
}
