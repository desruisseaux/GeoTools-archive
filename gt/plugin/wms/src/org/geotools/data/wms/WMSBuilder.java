/*
 * Created on Aug 21, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.data.wms;

import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.geotools.data.wms.capabilities.Capabilities;
import org.geotools.data.wms.capabilities.Layer;
import org.geotools.data.wms.capabilities.OperationType;
import org.geotools.data.wms.capabilities.Request;
import org.geotools.data.wms.capabilities.Service;

/**
 * @author Kefka
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class WMSBuilder {

	private Capabilities capabilities;
	private Service service;
	private Request request;
	private OperationType getCapabilities;
	private OperationType getMap;
	private OperationType getFeatureInfo;
	private List layers;
	
	private Layer layer;
	private List srss;
	private List boundingBoxes;
	private List styles;

	public WMSBuilder() {
		layers = new ArrayList();
		request = new Request();
	}
	
	public void buildCapabilities(String version) {
		capabilities = new Capabilities();
		capabilities.setVersion(version);
	}
	
	public void buildService(String name, String title, URL onlineResource, String _abstract, String[] keywords) {
		service = new Service();
		service.setName(name);
		service.setTitle(title);
		service.setOnlineResource(onlineResource);
		service.set_abstract(_abstract);
		service.setKeywordList(keywords);
	}
		
	public void buildGetCapabilitiesOperation(String[] formats, URL get, URL post) {
		getCapabilities = new OperationType();
		buildOperationType(getCapabilities, formats, get, post);
	}
	
	public void buildGetMapOperation(String[] formats, URL get, URL post) {
		getMap = new OperationType();
		buildOperationType(getMap, formats, get, post);
	}
	
	public void buildGetFeatureInfo(String[] formats, URL get, URL post) {
		getFeatureInfo = new OperationType();
		buildOperationType(getFeatureInfo, formats, get, post);
	}
	
	private void buildOperationType(OperationType operationType, String[] formats, URL get, URL post) {
		operationType.setFormats(formats);
		operationType.setGet(get);
		operationType.setPost(post);
	}
	
	public void buildLayer(String title, String name, boolean queryable, String parentLayerTitle ) {
		if (layer != null) {
			layer.setBoundingBoxes(boundingBoxes);
			layer.setSrs(srss);
			layer.setStyles(styles);
			boundingBoxes = null;
			srss = null;
			styles = null;
		}
		
		layer = new Layer(title);
		layers.add(layer);		
		
		layer.setName(name);
		layer.setQueryable(queryable);
		if (parentLayerTitle != null && parentLayerTitle.length() != 0) {
			int index = -1;
			for (int i = 0; i < layers.size(); i++) {
				if (((Layer)layers.get(i)).getTitle().equals(parentLayerTitle) ) {
					index = i;
					break;
				}
			}
			layer.setParent((Layer) layers.get(index));
		}
	}
	
	public void buildSRS(String srs) {
		if (srss == null) {
			srss = new ArrayList();
		}
		srss.add(srs);
	}
	
	public void buildStyle(String styleName) {
		if (styles == null) {
			styles = new ArrayList();
		}
		styles.add(styles);
	}
	
	public Capabilities finish() {
		capabilities.setService(service);
		
		request.setGetCapabilities(getCapabilities);
		request.setGetMap(getMap);
		request.setGetFeatureInfo(getFeatureInfo);
		
		capabilities.setRequest(request);
		
		layer.setBoundingBoxes(boundingBoxes);
		layer.setSrs(srss);
		layer.setStyles(styles);
		
		Layer[] layerArray = new Layer[layers.size()];
		for (int i = 0; i < layers.size(); i++) {
			layerArray[i] = (Layer) layers.get(i);
		}		
		
		capabilities.setLayers(layerArray);
		
		return capabilities;
	}
}
