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

import org.geotools.data.ows.WMSCapabilities;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;

/**
 * Initial start at generating a Capabilities bean from a WMS GetCapabilities document.
 * <p>
 * Web Map Server specifications known at time of writing:
 * <li>WMS 1.0.0: @link http://www.opengis.org/docs/00-028.pdf
 * <li>WMS 1.1.0: @link http://www.opengis.org/docs/01-047r2.pdf
 * <li>WMS 1.1.1: @link http://www.opengis.org/docs/01-068r3.pdf
 * <li>WMS 1.3.0: @link http://portal.opengis.org/files/?artifact_id=4756
 * </p>
 * 
 * @author Jody Garnett, Refractions Research
 */
public abstract class AbstractWMSParser implements WMSParser {
    
    /**
     * Version number understood by this parser.
     * <p>
     * The GetCapability document is epected to have this value as the
     * name attribute of the root element.
     * </p>
     * @return Name string in the format ""
     */
    public String getName(){
        return "WMT_MS_Capabilities";        
    }
    /**
     * Version number understood by this parser.
     * <p>
     * The GetCapability document is epected to have this value
     * as the version attribute of the root element.
     * </p>
     * @return <code>WMT_MS_Capabilities</code>
     */
    public abstract String getVersion();
        
    /**
     * Test if this WMSParser can handle the provided document.
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
     * <p>
     * Default implementation checks:
     * <ul>
     * <li>Root element name equals getName();
     * <li>Root element version equals = getVersion();
     * </ul>
     * </p>
     * @param document Document to test
     * @returns GENERIC for a GetCapabilities docuemnt matching getName and getVersion
     */
	public int canProcess(Document document) {		
		Element element = document.getRootElement(); //Root = "WMT_MS_Capabilities"
			
		if (!element.getName().equals( getName() )) {
			return WMSParser.NO;
		}
		String version = element.getAttributeValue("version");
		if (version == null || !version.equals( getVersion() )) {
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
	public WMSCapabilities constructCapabilities(Document document, WMSBuilder builder ) throws ParseCapabilitiesException {
	    Element capabilitiesElement = document.getRootElement();
		try {		    
			String version = capabilitiesElement.getAttributeValue("version");
			
			builder.buildCapabilities( version );
			parseService( capabilitiesElement.getChild("Service"), builder );
			
			Element capabilityElement = capabilitiesElement.getChild("Capability");
			{
			    parseRequest( capabilityElement.getChild("Request"), builder );
			    parseLayer( capabilityElement.getChild("Layer"), builder, null );
			}			
		} catch (MalformedURLException exception) {
		    throw new ParseCapabilitiesException("Unable to parse URL properly", null, exception);
		} catch (NullPointerException exception) {
		    throw new ParseCapabilitiesException("XML does not conform to the WMS Specification.", null, exception);
		}				
		return builder.finish();
	}

    
    /**
     * Parse provided layer (including any childern).
     * 
     * @param layerElement element being parsed
     * @param builder Builder used to construct
     * @param parentTitle parentTitle (or null for root)
     * @throws MalformedURLException
     */
	protected void parseLayer(Element layerElement, WMSBuilder builder, String parentTitle ) throws MalformedURLException {
		String title = layerElement.getChildText("Title");
        String name = layerElement.getChildText("Name");
        List srsElements = querySRS( layerElement );
        List styleElements = queryStyles( layerElement );
        boolean queryable = Integer.parseInt(layerElement.getAttributeValue("queryable")) == 1;
        builder.buildLayer( title, name, queryable, parentTitle, srsElements, styleElements);  
        
		List children = layerElement.getChildren( "Layer" );
		for( Iterator i=children.iterator(); i.hasNext();) {
			parseLayer( (Element) i.next(), builder, title );
		}
	}
	
	/**
	 * List of available Styles in the provided layerElement
	 * @param layerElement an element representing a layer
	 * @return a List of String containing all known styles in this layer
	 */
	protected List queryStyles(Element layerElement) {
		// TODO This is buggy. Need to extract Style.Name.value not Style.value
        List styleElements = layerElement.getChildren("Style");
        List styles = new ArrayList();
        
        if (styleElements != null) {
            
            Iterator iter = styleElements.iterator();
            while (iter.hasNext()) {
                String value = ((Element) iter.next()).getChildText("Name");
                styles.add(value);
            }
        }
        return styles;
	}
	
	/**
	 * List of available SRS for provided layerElement.
	 * <p>
	 * May need to override for WMS1.0.0.
	 * </p>
	 * @param layerElement
	 * @return
	 */
    protected List querySRS( Element layerElement ){
    	return extractStrings(layerElement, "SRS");
    }
    
    /**
     * Calls element.getChildren(childName) and iterates through all
     * the children, adding their value into a List which is returned.
     * @param element The element to extract the strings from
     * @param childName The name of the children in the element to extract values from
     * @return A List containing the String values of element's children specified by childName
     */
    protected List extractStrings(Element element, String childName) {
    	//TODO: Remove srs-nessof this.
        List srsElements = element.getChildren(childName);
        List srs = new ArrayList();
        
        if (srsElements != null) {
            
            Iterator iter = srsElements.iterator();
            while (iter.hasNext()) {
                String value = ((Element) iter.next()).getText();
                srs.add(value);
            }
        }
        return srs;
    }

    /**
     * Given an element that represents the getCapabilities request element, it will parse
     * that element and return a Request object.
     * 
     * @param requestElement an Element representing a request element
     * @return a Request object constructed from the passed-in element
     */
    protected void parseRequest(Element requestElement, WMSBuilder builder ) throws MalformedURLException {
        Element getCapabilities = requestElement.getChild("GetCapabilities"); 
        
        builder.buildGetCapabilitiesOperation(
            queryFormats( getCapabilities ),
            queryGet( getCapabilities ),
            queryPost( getCapabilities )
        );
        
        Element getMap = requestElement.getChild("GetMap");
        builder.buildGetMapOperation(
            queryFormats( getMap ),
            queryGet( getMap ),
            queryPost( getMap )
        );
        
        Element getFeatureInfo = requestElement.getChild("GetFeatureInfo");
        if( getFeatureInfo != null ){
            builder.buildGetMapOperation(
                queryFormats( getFeatureInfo ),
                queryGet( getFeatureInfo ),
                queryPost( getFeatureInfo )
            );    
        }        
    }
    
    /**
     * Parse List<String> from opperation element.
     * <p>
     * Values are taken to be mine types? We could restrict this list
     * to types we know how to deal with (incase image/svg comes up).
     * I figure we should reflect reality and leave it to
     * client code - like WMSFormat to cull this list.
     * </p>
     * <p>
     * Normal Mime Types:
     * <ul>
     * <li>image/gif
     * <li>image/jpeg
     * <li>image/png
     * <li>image/svg
     * <li>text/xml - generic XML mime type
     * <li>application/xml - generic XML mime type
     * </ul>
     * </p>
     * <p>
     * OGC-specific Mime Types:
     * <ul>
     * <li>application/vnd.ogc.wms_xml - WMS Capabilities XML
     * <li>application/vnd.ogc.gml - Geography Markup Language XML
     * <li>application/vnd.ogc.se_xml - Service Exception XML
     * </p>
     * <p>
     * Speaking of reality WMS 1.0.0 is unreal - it does not use
     * mime types (of any form) and makes use of the following
     * well known formats:
     * <pre><code>
     * GIF | JPEG | PNG | WebCGM |
     * SVG | GML.1 | GML.2 | GML.3 |
     * WBMP | WMS_XML | MIME | INIMAGE |
     * TIFF | GeoTIFF | PPM | BLANK
     * <code></pre>
     * In the interests of sanity we ask that a WMS 1.0.0 parser
     * convert these formats to mime types for the rest of the api.
     * </p>
     * @param op Opperation Element (like getMap)
     */
    protected List queryFormats( Element op ){
        Iterator iter;
        
        List formats = new ArrayList();
        List formatElements = op.getChildren("Format");
        iter = formatElements.iterator();
        while (iter.hasNext()) {
            Element formatElement = (Element) iter.next();
            formats.add(formatElement.getValue());
        }        
        return formats;
    }
    
    protected URL queryPost(Element element) throws MalformedURLException { 
        return queryDCPType(element, "Post");
    } 
    protected URL queryGet(Element element) throws MalformedURLException {
        return queryDCPType(element, "Get");
    }
    protected URL queryDCPType(Element element, String httpType) throws MalformedURLException {
        List dcpTypeElements = element.getChildren("DCPType");
        for( Iterator i = dcpTypeElements.iterator(); i.hasNext(); ) {
            Element dcpTypeElement = (Element) i.next();
            Element httpElement = dcpTypeElement.getChild("HTTP");
            
            Element httpTypeElement = httpElement.getChild(httpType);
            if (httpTypeElement != null) {
            	return parseOnlineResource(httpTypeElement.getChild("OnlineResource"));
            }
        }
        return null;        
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
    protected void parseService(Element serviceElement, WMSBuilder builder) throws MalformedURLException {
        String name = serviceElement.getChildText("Name");
		String title = serviceElement.getChildText("Title");
		
		URL onlineResource = parseOnlineResource(serviceElement.getChild("OnlineResource"));
	    String description = serviceElement.getChildText("Abstract");
	    
	    
		String keywords[] = queryKeywords(serviceElement);
		
		
		builder.buildService( name, title, onlineResource,description, keywords );				
    }
    
    /**
     * @param serviceElement
     * @return
     */
    protected String[] queryKeywords(Element serviceElement) {
        String[] keywords = null;
        Element keywordListElement = serviceElement.getChild("KeywordList");		
		if (keywordListElement != null) {
		    keywords = parseKeywordList(keywordListElement);		    
		}
        return keywords;
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
