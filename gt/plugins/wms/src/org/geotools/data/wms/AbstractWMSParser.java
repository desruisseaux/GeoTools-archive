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

import org.geotools.data.ows.WMSCapabilities;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;

import java.net.MalformedURLException;
import java.net.URL;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;


/**
 * Initial start at generating a Capabilities bean from a WMS GetCapabilities document.
 * <p>
 * Web Map Server specifications known at time of writing:
 * <li>WMS 1.0.0: @link http://www.opengis.org/docs/00-028.pdf</li>
 * <li>WMS 1.1.0: @link http://www.opengis.org/docs/01-047r2.pdf</li>
 * <li>WMS 1.1.1: @link http://www.opengis.org/docs/01-068r3.pdf</li>
 * <li>WMS 1.3.0: @link http://portal.opengis.org/files/?artifact_id=4756</li>
 * </p>
 *
 * @author Jody Garnett, Refractions Research
 */
public abstract class AbstractWMSParser implements WMSParser {
	
	protected Namespace defaultNamespace = Namespace.NO_NAMESPACE;
	
    /**
     * Version number understood by this parser.
     * <p>
     * The GetCapability document is epected to have this value as the
     * name attribute of the root element.
     * </p>
     * @return Name string in the format ""
     */
    public String getName() {
        return "WMT_MS_Capabilities"; //$NON-NLS-1$
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
         *        Document document;
         *        try {
         *                document = builder.build(stream);
         *                return parser.canProcess( document );
         *        } catch (JDOMException e) {
         *                throw new ParseCapabilitiesException( badXML );
         *        }
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
     * @return GENERIC for a GetCapabilities document matching getName and getVersion
     */
    public int canProcess(Document document) {
        Element element = document.getRootElement(); //Root = "WMT_MS_Capabilities"

        if (!element.getName().equals(getName())) {
            return WMSParser.NO;
        }

        String version = element.getAttributeValue("version"); //$NON-NLS-1$
        
        if ((version == null) || !version.equals(getVersion())) {
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
     * @param builder a WMSBuilder object which will construct the capabilities
     * @return a WMSCapabilities object containing the values of a parse capabilities document
     * @throws ParseCapabilitiesException if the document is unable to be parsed.
     */
    public WMSCapabilities constructCapabilities(Document document,
        WMSBuilder builder) throws ParseCapabilitiesException {
        Element capabilitiesElement = document.getRootElement();

        try {
            String version = capabilitiesElement.getAttributeValue("version"); //$NON-NLS-1$

            builder.buildCapabilities(version);
            parseService(capabilitiesElement.getChild("Service", defaultNamespace), builder); //$NON-NLS-1$

            Element capabilityElement = capabilitiesElement.getChild(
                    "Capability", defaultNamespace); //$NON-NLS-1$
            parseRequest(capabilityElement.getChild("Request", defaultNamespace), builder); //$NON-NLS-1$
            parseLayer(capabilityElement.getChild("Layer", defaultNamespace), builder, null); //$NON-NLS-1$
        } catch (MalformedURLException exception) {
            throw new ParseCapabilitiesException("Unable to parse URL properly",
                null, exception);
        } /*catch (NullPointerException exception) {
            throw new ParseCapabilitiesException("XML does not conform to the WMS Specification.",
                null, exception);
        }*/

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
    protected void parseLayer(Element layerElement, WMSBuilder builder,
        String parentTitle) throws MalformedURLException {
        String title = layerElement.getChildText("Title", defaultNamespace); //$NON-NLS-1$
        String name = layerElement.getChildText("Name", defaultNamespace); //$NON-NLS-1$
        Set srsElements = querySRS(layerElement);
        List styleElements = queryStyles(layerElement);
        
        String queryAttribute = layerElement.getAttributeValue(
                "queryable");

        boolean queryable = false; 
        if (queryAttribute != null && queryAttribute.length() != 0) {
        	queryable = Integer.parseInt(queryAttribute) == 1; //$NON-NLS-1$
        }

        builder.buildLayer(title, name, queryable, parentTitle, srsElements,
            styleElements);

        parseBoundingBoxes(layerElement, builder);
        
        parseLatLonBoundingBox(layerElement, builder);

        List children = layerElement.getChildren("Layer", defaultNamespace); //$NON-NLS-1$

        for (Iterator i = children.iterator(); i.hasNext();) {
            parseLayer((Element) i.next(), builder, title);
        }
    }

	protected void parseLatLonBoundingBox(Element layerElement, WMSBuilder builder) {
		Element latLonBboxElement = layerElement.getChild("LatLonBoundingBox", defaultNamespace); 
		
		if (latLonBboxElement == null) {
			return;
		}
		
		double[] coords = queryBaseBBoxAttributes(latLonBboxElement);
		
		builder.buildLatLonBoundingBox(coords[0], coords[1], coords[2], coords[3]);
	}

	/**
	 * Extracts the coordinate attributes from a generic bounding box element and returns
	 * a array of doubles containing those attributes.
	 * 
	 * @param bboxElement an element containing four attributes ('minx', 'miny', 'maxx', 'maxy')
	 * @return an array of ordered doubles. index 0 = minx, 1 = miny, 2 = maxx, 3 = maxy 
	 */
	protected double[] queryBaseBBoxAttributes(Element bboxElement) {
		double[] coords = new double[4];
		
		double minX = Double.parseDouble(bboxElement.getAttributeValue(
        	"minx")); //$NON-NLS-1$
		double minY = Double.parseDouble(bboxElement.getAttributeValue(
        	"miny")); //$NON-NLS-1$
		double maxX = Double.parseDouble(bboxElement.getAttributeValue(
        	"maxx")); //$NON-NLS-1$
		double maxY = Double.parseDouble(bboxElement.getAttributeValue(
        	"maxy")); //$NON-NLS-1$
		
		coords[0] = minX;
		coords[1] = minY;
		coords[2] = maxX;
		coords[3] = maxY;
		
		return coords;
	}

	protected void parseBoundingBoxes(Element layerElement, WMSBuilder builder) {
        List bboxElements = layerElement.getChildren("BoundingBox", defaultNamespace); //$NON-NLS-1$
        Iterator iter = bboxElements.iterator();

        while (iter.hasNext()) {
            Element bboxElement = (Element) iter.next();

            String crs = bboxElement.getAttributeValue(getBBoxCRSName());
            double[] coords = queryBaseBBoxAttributes(bboxElement);

            builder.buildBoundingBox(crs, coords[0], coords[1], coords[2], coords[3]);
        }
    }

    protected String getBBoxCRSName() {
        return "SRS"; //$NON-NLS-1$
    }

    /**
     * List of available Styles in the provided layerElement
     * @param layerElement an element representing a layer
     * @return a List of String containing all known styles in this layer
     */
    protected List queryStyles(Element layerElement) {
        // TODO This is buggy. Need to extract Style.Name.value not Style.value
        List styleElements = layerElement.getChildren("Style", defaultNamespace); //$NON-NLS-1$
        List styles = new ArrayList();

        if (styleElements != null) {
            Iterator iter = styleElements.iterator();

            while (iter.hasNext()) {
                String value = ((Element) iter.next()).getChildText("Name", defaultNamespace); //$NON-NLS-1$
                styles.add(value);
            }
        }

        return styles;
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
        List srsElements = element.getChildren(childName, defaultNamespace);
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
     * @param builder a WMSBuilder object which will construct the capabilities
     * @throws MalformedURLException if the request element contains invalid URLs 
     */
    protected void parseRequest(Element requestElement, WMSBuilder builder)
    	throws MalformedURLException {
        Element getCapabilities = requestElement.getChild(getRequestGetCapName(), defaultNamespace);

        builder.buildGetCapabilitiesOperation(queryFormats(getCapabilities),
            queryGet(getCapabilities), queryPost(getCapabilities));

        Element getMap = requestElement.getChild(getRequestGetMapName(), defaultNamespace);
        builder.buildGetMapOperation(queryFormats(getMap), queryGet(getMap),
            queryPost(getMap));

        Element getFeatureInfo = requestElement.getChild(getRequestGetFeatureInfoName(), defaultNamespace);

        if (getFeatureInfo != null) {
            builder.buildGetFeatureInfoOperation(queryFormats(getFeatureInfo),
                queryGet(getFeatureInfo), queryPost(getFeatureInfo));
        }
    }

    protected URL queryPost(Element element) throws MalformedURLException {
        return queryDCPType(element, "Post"); //$NON-NLS-1$
    }

    protected URL queryGet(Element element) throws MalformedURLException {
        return queryDCPType(element, "Get"); //$NON-NLS-1$
    }

    /**
     * Given an element that represents the getCapabilities service element, it will parse
     * that element and return a Service object.
     *
     * @param serviceElement an Element representing a service element
     * @param builder a WMSBuilder object which will construct the capabilities
     * @throws MalformedURLException if the service's OnlineResource contains an invalid URL 
     */
    protected void parseService(Element serviceElement, WMSBuilder builder)
        throws MalformedURLException {
        String name = serviceElement.getChildText("Name", defaultNamespace); //$NON-NLS-1$
        String title = serviceElement.getChildText("Title", defaultNamespace); //$NON-NLS-1$

        URL onlineResource = queryServiceOnlineResource(serviceElement);

        String description = serviceElement.getChildText("Abstract", defaultNamespace); //$NON-NLS-1$

        String[] keywords = queryKeywords(serviceElement);
        
        int layerLimit = queryLayerLimit(serviceElement);
        int maxWidth = queryMaxWidth(serviceElement);
        int maxHeight = queryMaxHeight(serviceElement);

        builder.buildService(name, title, onlineResource, description, keywords, layerLimit, maxWidth, maxHeight);
    }

	protected int queryMaxHeight(Element serviceElement) {
		return 0;
	}

	protected int queryMaxWidth(Element serviceElement) {
		return 0;
	}

	protected int queryLayerLimit(Element serviceElement) {
		return 0;
	}

	protected Set querySRS(Element layerElement) {
		Set srss = new TreeSet();
		
		List srsElements = layerElement.getChildren(getCRSElementName(), defaultNamespace);
		Iterator iter = srsElements.iterator();
		while (iter.hasNext()) {
			Element srsElement = (Element) iter.next();
	
			if (srsElement != null) {
				srss.addAll(Arrays.asList(srsElement.getTextTrim().split(" "))); //$NON-NLS-1$
			}
		}
		
	    return srss;
	}

	protected String getCRSElementName() {
		return "SRS";
	}

	protected URL queryServiceOnlineResource(Element serviceElement) throws MalformedURLException {
	    return new URL(serviceElement.getChildText("OnlineResource", defaultNamespace)); //$NON-NLS-1$
	}

	protected URL queryDCPType(Element element, String httpType) throws MalformedURLException {
	    List dcpTypeElements = element.getChildren("DCPType", defaultNamespace); //$NON-NLS-1$
	
	    for (Iterator i = dcpTypeElements.iterator(); i.hasNext();) {
	        Element dcpTypeElement = (Element) i.next();
	        Element httpElement = dcpTypeElement.getChild("HTTP", defaultNamespace); //$NON-NLS-1$
	
	        Element httpTypeElement = httpElement.getChild(httpType, defaultNamespace);
	
	        if (httpTypeElement != null) {
	            return new URL((httpTypeElement.getAttributeValue(
	                    "onlineResource", defaultNamespace))); //$NON-NLS-1$
	        }
	    }
	
	    return null;
	}

	protected String getRequestGetCapName() {
	    return "Capabilities"; //$NON-NLS-1$
	}

	protected String getRequestGetFeatureInfoName() {
	    return "FeatureInfo"; //$NON-NLS-1$
	}

	protected String getRequestGetMapName() {
	    return "Map"; //$NON-NLS-1$
	}

	protected String[] queryKeywords(Element serviceElement) {
	    String keywords = serviceElement.getChildTextTrim("Keywords", defaultNamespace); //$NON-NLS-1$
	    
	    if (keywords == null) {
	    	return null;
	    }
	
	    return keywords.split(" "); //$NON-NLS-1$
	}

	/**
	 * WMS 1.0 makes use of a different definition of format that what we would like.
	 * <p>
	 * KnownFormats for 1.0.0:
	 * <pre><code>
	 * GIF | JPEG | PNG | WebCGM |
	 * SVG | GML.1 | GML.2 | GML.3 |
	 * WBMP | WMS_XML | MIME | INIMAGE |
	 * TIFF | GeoTIFF | PPM | BLANK
	 * </code></pre>
	 * </p>
	 * <p>
	 * Our problem being that queryFormats is expected to return a list of mime types:
	 * <ul>
	 * <li>GIF mapped to "image/gif"
	 * <li>PNG mapped to "image/png"
	 * <li>JPEG mapped to "image/jpeg"
	 * </ul>
	 * </p>
	 * <p>
	 * We will need the reverse mapping when need to encode a WMS 1.0.0 request.
	 * We should probably punt this mapping off to a property file.
	 * </p>
	 * @see org.geotools.data.wms.AbstractWMSParser#queryFormats(org.jdom.Element)
	 */
	protected List queryFormats(Element op) {
	    // Example: <Format><PNG /><JPEG /><GML.1 /></Format>
	    Element formatElement = op.getChild("Format", defaultNamespace); //$NON-NLS-1$
	
	    Iterator iter;
	
	    List formats = new ArrayList();
	    List formatElements = formatElement.getChildren();
	    iter = formatElements.iterator();
	
	    while (iter.hasNext()) {
	        Element format = (Element) iter.next();
	        String mimeType = WMS1_0_0.toMIME(format.getName());
	
	        if (mimeType != null) {
	            formats.add(mimeType);
	        }
	    }
	
	    return formats;
	}
}
