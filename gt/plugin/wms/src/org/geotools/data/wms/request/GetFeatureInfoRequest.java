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
package org.geotools.data.wms.request;

import java.net.URL;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.geotools.data.wms.SimpleLayer;
import org.geotools.data.wms.capabilities.Layer;


/**
 * Information required for a GetFeatureInfo request.
 * <p>
 * Q: queryableLayers is a Set - is this true? Or is order important
 * Q: infoFormats - what does this do? Do these match up with
 * querableLayers? Or is it a list of formats our client is willing
 * to understand?
 * 
 * @author Richard Gould, Refractions Research
 */
public class GetFeatureInfoRequest extends GetMapRequest {

	/** 
	 * A list of type String, each representing a format for the request
	 */
	private List infoFormats;
	
	/**
	 * A list of type Layer, each of which has queryable set to true.
	 * This is instantiated usually from a GetCapabilities document
	 */
	private Set queryableLayers;
	
	/**
	 * A set of type Layer, each of which is to be queried in the request
	 */
	private Set queryLayers;

	public GetFeatureInfoRequest(
			URL onlineResource, 
			GetMapRequest request, 
			Set queryableLayers, 
			String[] infoFormats) {
		super(onlineResource, "1.1.1", 
				(SimpleLayer[]) request.getAvailableLayers().toArray(),
				request.getAvailableSRSs(), (String[]) request.getAvailableFormats().toArray(),
				request.getAvailableExceptions());
		this.properties = request.properties;
		setProperty("REQUEST", "GetFeatureInfo");
		
		this.queryableLayers = queryableLayers;
		this.infoFormats = Arrays.asList(infoFormats);
		queryLayers = new TreeSet();
	}
	
	public URL getFinalURL() {
		Iterator iter = queryLayers.iterator();
		String queryLayerString = "";
		while (iter.hasNext()) {
			Layer layer = (Layer) iter.next();
			queryLayerString = queryLayerString + layer.getName();
			if (iter.hasNext()) {
				queryLayerString = queryLayerString + ",";
			}
		}
		setProperty("QUERY_LAYERS", queryLayerString);
		return super.getFinalURL();
	}
	
	public void addQueryLayer(Layer layer) {
		queryLayers.add(layer);
	}
	
	/**
	 * An unordered set of type Layer.
	 * Each layer will be queried in the request
	 * 
	 * @param layerList A Set of type Layer, each to be queried
	 */
	public void setQueryLayers(Set layers) {
		queryLayers = layers;
	}
	
	public void setInfoFormat(String infoFormat) {
		setProperty("INFO_FORMAT", infoFormat);
	}
	
	public void setFeatureCount(String featureCount) {
		setProperty("FEATURE_COUNT", featureCount);
	}
	
	public void setFeatureCount(int featureCount) {
		setFeatureCount(Integer.toString(featureCount));
	}
	
	public void setQueryPoint(int x, int y) {
		setProperty("X", Integer.toString(x));
		setProperty("Y", Integer.toString(y));
	}
	
	public List getInfoFormats() {
		return infoFormats;
	}
	public Set getQueryableLayers() {
		return queryableLayers;
	}
}
