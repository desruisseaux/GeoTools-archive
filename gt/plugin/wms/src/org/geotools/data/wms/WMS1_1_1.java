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

import org.geotools.data.wms.request.GetCapabilitiesRequest;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;

import java.io.IOException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;


/**
 * Provides support for the Web Map Server 1.1.1 Specificaiton.
 * <p>
 * This class opperates as a Factory creating the following related objects.
 * <ul>
 * <li>WMS1_1_1.Parser - a WMSParser capable of parsing a Get Capabilities Document
 * <li>WMS1_1_1.Format - a WMSFormat describing required parameters
 * <li>WMS1_1_1.MapRequest - a MapRequest specific to WMS 1.0
 * </ul>
 * </p>
 * <p>
 * The idea is that this class opperates a Toolkit for all things assocated with
 * Web Map Server 1.1.1 Specification. The various objects produced by this toolkit
 * are used as stratagy objects for the top level Web Map Server objects:
 * <ul>
 * <li>Web Map Server - uses WMS1_1_1.Parser to derive a Get Capabilities Document
 * <li>Web Map Server - uses WMS1_1_1 as a WMSFormat factory to generate the correct
 *     WMS_1_1_1.Format.
 * </ul>
 * </p>
 * <p>
 * WMS1_1_1 provides both name and version information that may be checked against
 * a GetCapabilities document during version negotiation.
 * </p>
 * @author Jody Garnett, Refractions Research
 */
public class WMS1_1_1 extends WMS1_1_0 {
    public WMS1_1_1() {
        parsers = new WMSParser[1];
        parsers[0] = new Parser();
    }

    /**
     * Expected version attribute for root element.
     */
    public String getVersion() {
        return "1.1.1";
    }

    /** Factory method to create WMS 1.1.1 GetCapabilities Request */
    public GetCapabilitiesRequest createGetCapabilitiesRequest(URL server) {
        return new GetCapsRequest(server);
    }

    static public class GetCapsRequest extends WMS1_1_0.GetCapsRequest {
        /**
         * Construct a Request compatable with a 1.0.0 Web Feature Server.
         *
         * @param urlGetCapabilities URL of GetCapabilities document.
         */
        public GetCapsRequest(URL urlGetCapabilities) {
            super(urlGetCapabilities);
        }

        protected void initVersion() {
            setProperty("VERSION", "1.1.1");
        }
    }

    static public class Parser extends WMS1_1_0.Parser {
        public String getVersion() {
            return "1.1.1";
        }

		protected Set querySRS(Element layerElement) {
		    return new TreeSet(extractStrings(layerElement, "SRS")); //$NON-NLS-1$
		}

		protected URL queryDCPType(Element element, String httpType) throws MalformedURLException {
		    List dcpTypeElements = element.getChildren("DCPType"); //$NON-NLS-1$
		
		    for (Iterator i = dcpTypeElements.iterator(); i.hasNext();) {
		        Element dcpTypeElement = (Element) i.next();
		        Element httpElement = dcpTypeElement.getChild("HTTP"); //$NON-NLS-1$
		
		        Element httpTypeElement = httpElement.getChild(httpType);
		
		        if (httpTypeElement != null) {
		            return parseOnlineResource(httpTypeElement.getChild(
		                    "OnlineResource")); //$NON-NLS-1$
		        }
		    }
		
		    return null;
		}

		protected URL parseOnlineResource(Element onlineResourceElement) throws MalformedURLException {
		    Namespace xlink = Namespace.getNamespace("xlink", //$NON-NLS-1$
		            "http://www.w3.org/1999/xlink"); //$NON-NLS-1$
		
		    //Element onlineResourceElement = element.getChild(ONLINE_RESOURCE);
		    String onlineResource = onlineResourceElement.getAttributeValue("href", //$NON-NLS-1$
		            xlink);
		
		    return new URL(onlineResource);
		}

		/**
		 * Takes a Service element, extracts the OnlineResource value and creates
		 * a URL from it.
		 *  
		 * @param serviceElement
		 * @throws MalformedURLException if the OnlineResource element contains an invalid URL
		 * @return a URL containing the value in the OnlineResource element
		 */
		protected URL queryServiceOnlineResource(Element serviceElement) throws MalformedURLException {
		    return parseOnlineResource(serviceElement.getChild("OnlineResource")); //$NON-NLS-1$
		}

		protected String getRequestGetFeatureInfoName() {
		    return "GetFeatureInfo"; //$NON-NLS-1$
		}

		protected String getRequestGetMapName() {
		    return "GetMap"; //$NON-NLS-1$
		}

		protected String getRequestGetCapName() {
		    return "GetCapabilities"; //$NON-NLS-1$
		}

		/**
		 * Takes a KeywordList element and extracts all the keywords from it.
		 * 
		 * This is for WMS version 1.1.0 and on.
		 * 
		 * @param keywordListElement
		 * @return A String[], each element containing a keyword
		 */
		protected String[] queryKeywordList(Element keywordListElement) {
		    String[] keywords = new String[keywordListElement.getChildren().size()];
		
		    for (int i = 0; i < keywordListElement.getChildren().size(); i++) {
		        Element keyword = (Element) keywordListElement.getChildren().get(i);
		        keywords[i] = keyword.getText();
		    }
		
		    return keywords;
		}

		/**
		 * Takes a Service element and extracts all the keywords from it.
		 * 
		 * @param serviceElement
		 * @return A String[], each element containing a keyword
		 */
		protected String[] queryKeywords(Element serviceElement) {
		    String[] keywords = null;
		    Element keywordListElement = serviceElement.getChild("KeywordList"); //$NON-NLS-1$
		
		    if (keywordListElement != null) {
		        keywords = queryKeywordList(keywordListElement);
		    }
		
		    return keywords;
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
		 * @return A List of Strings representing the formats that this operation can be performed with
		 */
		protected List queryFormats(Element op) {
		    Iterator iter;
		
		    List formats = new ArrayList();
		    List formatElements = op.getChildren("Format"); //$NON-NLS-1$
		    iter = formatElements.iterator();
		
		    while (iter.hasNext()) {
		        Element formatElement = (Element) iter.next();
		        formats.add(formatElement.getValue());
		    }
		
		    return formats;
		}
    }
}
