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
 * Genaerte a Capabilities bean from a WMS 1.1.1 complient GetCapabilities document.
 * <p>
 * Feedback:
 * <ul>
 * <li>This classname does not conform to the geotools naming conventions - it shuld end in WMSParser
 * (since it impelments that interface).
 * <li>Consider rename to one of: Spec111WMSParser, CapabilitiesDocument111WMSParser
 * <li>WIth a rename of WMSParser to CapabilitiesParser we could use WMS111CapabilitiesParser
 *     (although this woudl prevent the addition processing other documents to the WMSParser
 *      interface)
 * </ul>
 * </p>
 * @author Richard Gould, Refractions Research
 * @see OPENGIS PROJECT DOCUMENT 00-028 OpenGIS® Web Map Server Interface Implementation Specification
 */
public class Parser1_1_1 implements WMSParser {

    /**
     * Package visiable test method - not part of the public api.
     * <p>
     * Used as a storage place for Richards Code while I adjust class
     * to use of JDom
     * </p>
     */
    Capabilities parse(InputStream stream) throws ParseCapabilitiesException {
        Document document;
		try {
		    SAXBuilder builder = new SAXBuilder();
		    document = builder.build(stream);
        } catch (JDOMException badXML) {
            throw new ParseCapabilitiesException( badXML );
        } catch (IOException badIO ) {
		    throw new ParseCapabilitiesException( badIO );
		}		
		if( canProcess( document ) == NO ){
		    throw new ParseCapabilitiesException( "Not a WMS 1.1.1 Document" );
		}
		return constructCapabilities( document );				
    }
    /** Test if this WMSParser can handle the provided document.
     * <p>
     * Sample use:
     * <pre><code>
     * SAXBuilder builder = new SAXBuilder();
	 *	Document document;
	 *	try {
	 *		document = builder.build(stream);
	 *		return parser.canProcess( document );
	 *	} catch (JDOMException e) {
	 *		throw new ParseCapabilitiesException( badXML );
	 *	}
     * </code></pre>
     * </p>
     * @param document Document to test
     * @returns GENERIC for a WMS 1.1.1 GetCapabilities docuemnt
     */
	public int canProcess(Document document) {		
		Element element = document.getRootElement(); //Root = "WMT_MS_Capabilities"
			
		// String Testing Feedback:
		// - good habit to test w/ String first
		//   !"WMT_MS_Capabilities".equals( element.getName() )
		// - do you need to use element.getName().trim() or a regex for this comparison?
		//   That is - you may want to be a little bit more forgiving of whitespace issues.
		if (!element.getName().equals("WMT_MS_Capabilities")) {
			return WMSParser.NO;
		}
		String version = element.getAttributeValue("version");
		if (version == null || version.length() == 0 || !version.equals("1.1.1")) {
			return WMSParser.NO;
		}
		return WMSParser.GENERIC;
	}
	/**
	 * Ues WMSBuilder to construct a Capabilities object for the provided docuemnt.
	 * <p>
	 * Use of Builder pattern allows us to vary the Parser and isolate the complexities of
	 * Capabilities construction (especially layer objects) from Parsing code. Note the use of
	 * Builder (rather than a Factory) allows us to make the construction of layer objects order
	 * dependent.
	 * </p>
	 * @param document Document to parse
	 */
	public Capabilities constructCapabilities(Document document) throws ParseCapabilitiesException {
		Capabilities capabilities;
		Element capabilitiesElement = document.getRootElement();
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
		    throw new ParseCapabilitiesException("Unable to parse URL properly", null, exception);
		} catch (NullPointerException exception) {
		    throw new ParseCapabilitiesException("XML does not conform to the WMS Specification.", null, exception);
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
