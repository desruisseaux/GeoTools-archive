/*
 * Created on Aug 16, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.data.wms.request;

import java.net.URL;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.geotools.data.wms.SimpleLayer;
import org.geotools.data.wms.capabilities.Layer;


/**
 * @author Richard Gould
 *
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
