/*
 * Created on Sep 11, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.data.wms;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.geotools.data.wms.request.GetCapabilitiesRequest;
import org.jdom.Element;
import org.jdom.Namespace;

/**
 * @author Kefka
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class WMS1_1_0 extends WMS1_0_0 {

	public WMS1_1_0 () {
		parsers = new WMSParser[1];
		parsers[0] = new Parser();
	}
	
	/* (non-Javadoc)
	 * @see org.geotools.data.wms.Specification#getVersion()
	 */
	public String getVersion() {
		return "1.1.0";
	}

	/* (non-Javadoc)
	 * @see org.geotools.data.wms.Specification#createGetCapabilitiesRequest(java.net.URL)
	 */
	public GetCapabilitiesRequest createGetCapabilitiesRequest(URL server) {
		return new GetCapsRequest(server);
	}
	
	public static class Parser extends WMS1_0_0.Parser {

		/* (non-Javadoc)
		 * @see org.geotools.data.wms.AbstractWMSParser#getVersion()
		 */
		public String getVersion() {
			return "1.1.0";
		}

		protected URL queryDCPType(Element element, String httpType) throws MalformedURLException {
		    List dcpTypeElements = element.getChildren("DCPType", defaultNamespace); //$NON-NLS-1$
		
		    for (Iterator i = dcpTypeElements.iterator(); i.hasNext();) {
		        Element dcpTypeElement = (Element) i.next();
		        Element httpElement = dcpTypeElement.getChild("HTTP", defaultNamespace); //$NON-NLS-1$
		
		        Element httpTypeElement = httpElement.getChild(httpType, defaultNamespace);
		
		        if (httpTypeElement != null) {
		            return parseOnlineResource(httpTypeElement.getChild(
		                    "OnlineResource", defaultNamespace)); //$NON-NLS-1$
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
		    return parseOnlineResource(serviceElement.getChild("OnlineResource", defaultNamespace)); //$NON-NLS-1$
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
		    String[] keywords = new String[keywordListElement.getChildren(null, defaultNamespace).size()];
		
		    for (int i = 0; i < keywordListElement.getChildren(null, defaultNamespace).size(); i++) {
		        Element keyword = (Element) keywordListElement.getChildren(null, defaultNamespace).get(i);
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
		    Element keywordListElement = serviceElement.getChild("KeywordList", defaultNamespace); //$NON-NLS-1$
		
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
		    List formatElements = op.getChildren("Format", defaultNamespace); //$NON-NLS-1$
		    iter = formatElements.iterator();
		
		    while (iter.hasNext()) {
		        Element formatElement = (Element) iter.next();
		        formats.add(formatElement.getValue());
		    }
		
		    return formats;
		}
	}
	
	public static class GetCapsRequest extends WMS1_0_0.GetCapsRequest {

		public GetCapsRequest(URL urlGetCapabilities) {
			super(urlGetCapabilities);
			// TODO Auto-generated constructor stub
		}
		
		/* (non-Javadoc)
		 * @see org.geotools.data.wms.request.GetCapabilitiesRequest#initRequest()
		 */
		protected void initRequest() {
			setProperty("REQUEST", "GetCapabilities");
		}
		/* (non-Javadoc)
		 * @see org.geotools.data.wms.request.GetCapabilitiesRequest#initService()
		 */
		protected void initService() {
			setProperty("SERVICE", "WMS");
		}
		/* (non-Javadoc)
		 * @see org.geotools.data.wms.request.GetCapabilitiesRequest#initVersion()
		 */
		protected void initVersion() {
			setProperty("VERSION", "1.1.0");
		}
	}
}
