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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.geotools.data.wms.capabilities.Layer;
import org.geotools.data.wms.capabilities.Request;
import org.geotools.data.wms.capabilities.OperationType;
import org.geotools.data.wms.capabilities.Service;

import org.geotools.data.wms.capabilities.Capabilities;
import org.jdom.Element;
import org.jdom.Namespace;


/**
 * @author rgould
 *
 * TODO Perhaps perform inheritence of layer properties - Section 7.1.4.7
 */
public class CapabilitiesParser {
    public static Capabilities parseCapabilities(Element wmt_ms_capabilities) throws ParseCapabilitiesException {
		Capabilities capabilities;
		
		//Populate the Capabilities object with data from the XML string.

		try {
			Element serviceElement = wmt_ms_capabilities.getChild("Service");
			Service service = parseService(serviceElement);

			Element capabilityElement = wmt_ms_capabilities.getChild("Capability");
			
			//Parse Request
	        Element requestElement = capabilityElement.getChild("Request");
	        Request request = parseRequest(requestElement);

			//Parse Layer
	        Element layerElement = capabilityElement.getChild("Layer");
	        
	        List layerList = new ArrayList();
	        parseLayers(layerElement, layerList);

	        Layer[] layers = new Layer[layerList.size()];
	        for (int i = 0; i < layerList.size(); i++) {
	        	layers[i] = (Layer) layerList.get(i);
	        }
	        
			capabilities = new Capabilities();
			capabilities.setService(service);
			capabilities.setRequest(request);
			capabilities.setLayers(layers);
			
			String version = wmt_ms_capabilities.getAttributeValue("version");
			capabilities.setVersion(version);
			
		} catch (MalformedURLException exception) {
		    throw new ParseCapabilitiesException("Unable to parse URL properly", exception);
		} catch (NullPointerException exception) {
		    throw new ParseCapabilitiesException("XML does not conform to the WMS Specification.", exception);
		}		
		
		return capabilities;

    }
    
    /**
	 * @param layerElement
	 * @param layerList
	 */
	private static void parseLayers(Element rootElement, List layerList) throws MalformedURLException {
		Layer layer = parseLayer(rootElement);
		layerList.add(layer);
		
		Iterator iter = rootElement.getChildren("Layer").iterator();
		while (iter.hasNext()) {
			Element layerElement = (Element) iter.next();
			parseLayers(layerElement, layerList);
		}
	}

	/**
     * @param layerElement
     * @return
     */
    private static Layer parseLayer(Element layerElement) throws MalformedURLException {
        Layer layer;
        Iterator iter;
        
        String title = layerElement.getChildText("Title");
        layer = new Layer(title);
        
        String name = layerElement.getChildText("Name");
        if (name != null) {
            layer.setName(name);
        }
        
        List srsElements = layerElement.getChildren("SRS");
        if (srsElements != null) {
            List srs = new ArrayList();
            iter = srsElements.iterator();
            while (iter.hasNext()) {
                String srsValue = ((Element) iter.next()).getText();
                srs.add(srsValue);
            }
            layer.setSrs(srs);
        }
      
        if (Integer.parseInt(layerElement.getAttributeValue("queryable")) == 1) {
            layer.setQueryable(true);
        }
        
        return layer;
    }


    /**
     * Given an element that represents the getCapabilities request element, it will parse
     * that element and return a Request object.
     * 
     * @param requestElement an Element representing a request element
     * @return a Request object constructed from the passed-in element
     */
    private static Request parseRequest(Element requestElement) throws MalformedURLException {
        Request request = new Request();
        OperationType operationType;
        
        operationType = parseOperationType(requestElement.getChild("GetCapabilities"));
        request.setGetCapabilities(operationType);
        
        operationType = parseOperationType(requestElement.getChild("GetMap"));
        request.setGetMap(operationType);
        
        if (requestElement.getChild("GetFeatureInfo") != null) {
            operationType = parseOperationType(requestElement.getChild("GetFeatureInfo"));
            request.setGetFeatureInfo(operationType);
        }
        
        return request;
    }

    private static OperationType parseOperationType(Element element) throws MalformedURLException {
        OperationType operationType = new OperationType();
        Iterator iter;
        
        List formats = new ArrayList();
        List formatElements = element.getChildren("Format");
        iter = formatElements.iterator();
        while (iter.hasNext()) {
            Element formatElement = (Element) iter.next();
            formats.add(formatElement.getValue());
        }
        
        String[] formatStrings = new String[formats.size()];
        for (int i = 0; i < formats.size(); i++) {
        	formatStrings[i] = (String) formats.get(i);
        }
        
        operationType.setFormats(formatStrings);
        
        List dcpTypes = new ArrayList();
        List dcpTypeElements = element.getChildren("DCPType");
        iter = dcpTypeElements.iterator();
        while (iter.hasNext()) {
            Element dcpTypeElement = (Element) iter.next();
            Element httpElement = dcpTypeElement.getChild("HTTP");
            
            Element get = httpElement.getChild("Get");
            if (get!= null) {
            	operationType.setGet(parseOnlineResource(get.getChild("OnlineResource")));
            }
            
            Element post = httpElement.getChild("Post");
            if (post != null) {
            	operationType.setPost(parseOnlineResource(post.getChild("OnlineResource")));
            }
        }
        
        return operationType;
    }
    
    private static URL parseOnlineResource(Element onlineResourceElement) throws MalformedURLException {
		Namespace xlink = Namespace.getNamespace("xlink", "http://www.w3.org/1999/xlink");
		
		//Element onlineResourceElement = element.getChild(ONLINE_RESOURCE);
		String onlineResource = onlineResourceElement.getAttributeValue("href", xlink);
		return new URL(onlineResource);
    }

    /**
     * Given an element that represents the getCapabilities service element, it will parse
     * that element and return a Service object.
     * 
     * @param serviceElement an Element representing a service element
     * @return a Service object constructed from the passed-in element
     */
    private static Service parseService(Element serviceElement) throws MalformedURLException {
        Service service;
		
        String name = serviceElement.getChildText("Name");
		String title = serviceElement.getChildText("Title");
		
		//Optional attributes finished. Can construct.
	    service = new Service(name, title, parseOnlineResource(serviceElement.getChild("OnlineResource")));
	    
		service.set_abstract(serviceElement.getChildText("Abstract"));
		
		Element keywordListElement = serviceElement.getChild("KeywordList");
		if (keywordListElement != null) {
		    List keywords = parseKeywordList(keywordListElement);
		    service.setKeywordList(keywords);
		}
		
		return service;
    }
    /**
     * @param keywordListElement
     * @return
     */
    private static List parseKeywordList(Element keywordListElement) {
        List keywords = new ArrayList();
	    Iterator iter = keywordListElement.getChildren().iterator();
	    while (iter.hasNext()) {
	        Element keyword = (Element) iter.next();
	        keywords.add(keyword.getText());
	    }
	    return keywords;
    }
}
