/*
 * Created on Aug 16, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.data.wms;

import java.net.URL;
import java.util.List;

/**
 * @author Richard Gould
 *
 */
public class GetFeatureInfoRequest extends GetMapRequest {

	public GetFeatureInfoRequest(URL onlineResource, GetMapRequest request) {
		super(onlineResource, "1.1.1", 
				request.getAvailableLayers(),
				request.getAvailableSRSs(), request.getAvailableFormats(),
				request.getAvailableExceptions());
		setProperty("REQUEST", "GetFeatureInfo");
	}
	
	public void setQueryLayers(String layerList) {
		setProperty("QUERY_LAYERS", layerList);
	}
	
	public void setQueryLayers(List layerList) {
		setQueryLayers(layerList);
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
	
	public void setExceptions(String exceptions) {
		setProperty("EXCEPTIONS", exceptions);
	}		
}
