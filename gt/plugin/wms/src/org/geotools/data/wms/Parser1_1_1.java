/*
 * Created on Aug 21, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.data.wms;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import org.geotools.data.wms.capabilities.Capabilities;
import org.geotools.data.wms.capabilities.Layer;
import org.geotools.data.wms.capabilities.OperationType;
import org.geotools.data.wms.capabilities.Request;
import org.geotools.data.wms.capabilities.Service;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;

/**
 * @author Kefka
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class Parser1_1_1 implements WMSParser {

	public int canProcess(InputStream stream) throws IOException {
		
		SAXBuilder builder = new SAXBuilder();
		Document document;
		try {
			document = builder.build(stream);
		} catch (JDOMException e) {
			return NO;
		}

		Element element = document.getRootElement(); //Root = "WMT_MS_Capabilities"
			
		if (!element.getName().equals("WMT_MS_Capabilities")) {
			return WMSParser.NO;
		}
		String version = element.getAttributeValue("version");
		if (version == null || version.length() == 0 || !version.equals("1.1.1")) {
			return WMSParser.NO;
		}
		return WMSParser.GENERIC;
	}

	public Capabilities constructCapabilities(Element capabilitiesElement) throws ParseCapabilitiesException {
		Capabilities capabilities;
		
		//Populate the Capabilities object with data from the XML string.

		try {
			Element serviceElement = capabilitiesElement.getChild("Service");
			Service service = parseService(serviceElement);

			Element capabilityElement = capabilitiesElement.getChild("Capability");
			
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
			
			String version = capabilitiesElement.getAttributeValue("version");
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
	protected void parseLayers(Element rootElement, List layerList) throws MalformedURLException {
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
    protected Layer parseLayer(Element layerElement) throws MalformedURLException {
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
    protected Request parseRequest(Element requestElement) throws MalformedURLException {
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

    protected OperationType parseOperationType(Element element) throws MalformedURLException {
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
    
    protected URL parseOnlineResource(Element onlineResourceElement) throws MalformedURLException {
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
    protected Service parseService(Element serviceElement) throws MalformedURLException {
        Service service;
		
        String name = serviceElement.getChildText("Name");
		String title = serviceElement.getChildText("Title");
		
		//Optional attributes finished. Can construct.
	    service = new Service();
		service.setName(name);
	    service.setTitle(title);
	    service.setOnlineResource(parseOnlineResource(serviceElement.getChild("OnlineResource")));
	    	    
		service.set_abstract(serviceElement.getChildText("Abstract"));
		
		Element keywordListElement = serviceElement.getChild("KeywordList");
		if (keywordListElement != null) {
		    String[] keywords = parseKeywordList(keywordListElement);
		    service.setKeywordList(keywords);
		}
		
		return service;
    }
    /**
     * @param keywordListElement
     * @return
     */
    protected String[] parseKeywordList(Element keywordListElement) {
        String[] keywords = new String[keywordListElement.getChildren().size()];
	    for (int i = 0; i < keywordListElement.getChildren().size();i++) {
	        Element keyword = (Element) keywordListElement.getChildren().get(i);
	        keywords[i] = (String) keyword.getText();
	    }
	    return keywords;
    }
	
}
