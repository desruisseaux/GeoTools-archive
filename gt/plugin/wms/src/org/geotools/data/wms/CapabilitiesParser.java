/*
 * Created on Jul 8, 2004
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

import org.geotools.data.wms.getCapabilities.Attribution;
import org.geotools.data.wms.getCapabilities.AuthorityURL;
import org.geotools.data.wms.getCapabilities.BoundingBox;
import org.geotools.data.wms.getCapabilities.Capability;
import org.geotools.data.wms.getCapabilities.ContactAddress;
import org.geotools.data.wms.getCapabilities.ContactInformation;
import org.geotools.data.wms.getCapabilities.ContactPersonPrimary;
import org.geotools.data.wms.getCapabilities.DCPType;
import org.geotools.data.wms.getCapabilities.DataURL;
import org.geotools.data.wms.getCapabilities.DescribeLayer;
import org.geotools.data.wms.getCapabilities.Dimension;
import org.geotools.data.wms.getCapabilities.Exception;
import org.geotools.data.wms.getCapabilities.Extent;
import org.geotools.data.wms.getCapabilities.FeatureListURL;
import org.geotools.data.wms.getCapabilities.Get;
import org.geotools.data.wms.getCapabilities.GetCapabilities;
import org.geotools.data.wms.getCapabilities.GetFeatureInfo;
import org.geotools.data.wms.getCapabilities.GetLegendGraphic;
import org.geotools.data.wms.getCapabilities.GetMap;
import org.geotools.data.wms.getCapabilities.GetStyles;
import org.geotools.data.wms.getCapabilities.HTTP;
import org.geotools.data.wms.getCapabilities.HTTPRequestType;
import org.geotools.data.wms.getCapabilities.Identifier;
import org.geotools.data.wms.getCapabilities.LatLonBoundingBox;
import org.geotools.data.wms.getCapabilities.Layer;
import org.geotools.data.wms.getCapabilities.LegendURL;
import org.geotools.data.wms.getCapabilities.LogoURL;
import org.geotools.data.wms.getCapabilities.MetadataURL;
import org.geotools.data.wms.getCapabilities.Post;
import org.geotools.data.wms.getCapabilities.PutStyles;
import org.geotools.data.wms.getCapabilities.Request;
import org.geotools.data.wms.getCapabilities.RequestType;
import org.geotools.data.wms.getCapabilities.ScaleHint;
import org.geotools.data.wms.getCapabilities.Service;
import org.geotools.data.wms.getCapabilities.Style;
import org.geotools.data.wms.getCapabilities.StyleSheetURL;
import org.geotools.data.wms.getCapabilities.StyleURL;
import org.geotools.data.wms.getCapabilities.UserDefinedSymbolization;
import org.geotools.data.wms.getCapabilities.VendorSpecificCapabilities;
import org.geotools.data.wms.getCapabilities.WMT_MS_Capabilities;
import org.geotools.data.wms.getCapabilities.WmsURL;
import org.jdom.Element;
import org.jdom.Namespace;


/**
 * @author rgould
 *
 * TODO Perhaps perform inheritence of layer properties - Section 7.1.4.7
 */
public class CapabilitiesParser {
    public static WMT_MS_Capabilities parseCapabilities(Element wmt_ms_capabilities) throws ParseCapabilitiesException {
		WMT_MS_Capabilities capabilities;
		
		//Populate the Capabilities object with data from the XML string.
		
		//Hmm.. should I explicitly request each element as expected, or just iterate through the entire thing? 
		//Will do explicit for now. -- rgould
		
		try {
			Element serviceElement = wmt_ms_capabilities.getChild("Service");
			Service service = parseService(serviceElement);

			Element capabilityElement = wmt_ms_capabilities.getChild("Capability");
			Capability capability = parseCapability(capabilityElement);
		    
			capabilities = new WMT_MS_Capabilities(service, capability);
			
		} catch (MalformedURLException exception) {
		    throw new ParseCapabilitiesException("Unable to parse URL properly", exception);
		} catch (NullPointerException exception) {
		    throw new ParseCapabilitiesException("Element not specified.", exception);
		}		
		
		return capabilities;

    }
    
    /**
     * Given an element that represents the getCapabilities capability element, it will parse
     * that element and return a Capability object.
     * 
     * @param capabilityElement an Element representing a capability element
     * @return a Capability object constructed from the passed-in element
     */
    private static Capability parseCapability(Element capabilityElement) throws MalformedURLException {
        Capability capability;
        
        Element requestElement = capabilityElement.getChild("Request");
        Request request = parseRequest(requestElement);
        
        Element exceptionElement = capabilityElement.getChild("Exception");
        Exception exception = parseException(exceptionElement);
        
        capability = new Capability(request, exception);
        
        Element vendorSpecificCapabilitiesElement = capabilityElement.getChild("VendorSpecificCapabilities");
        if (vendorSpecificCapabilitiesElement != null) {
            VendorSpecificCapabilities vendorSpecificCapabilities = parseVendorSpecificCapabilities(vendorSpecificCapabilitiesElement);
            capability.setVendorSpecificCapabilities(vendorSpecificCapabilities);
        }
        
        Element userDefinedSymbolizationElement = capabilityElement.getChild("UserDefinedSymbolization");
        if (userDefinedSymbolizationElement != null) {
            UserDefinedSymbolization userDefinedSymbolization = parseUserDefinedSymbolization(userDefinedSymbolizationElement);
            capability.setUserDefinedSymboliazation(userDefinedSymbolization);
        }
        
        Element layerElement = capabilityElement.getChild("Layer");
        if (layerElement != null) {
            Layer layer = parseLayer(layerElement);
            capability.setLayer(layer);
        }
        
        return capability;
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
        
        String _abstract = layerElement.getChildText("Abstract");
        if (_abstract != null) {
            layer.set_abstract(_abstract);
        }
        
        Element keywordListElement = layerElement.getChild("KeywordList");
        if (keywordListElement != null) {
            List keywordList = parseKeywordList(keywordListElement);
            layer.setKeywordList(keywordList);
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
        
        Element latLonBoundingBoxElement = layerElement.getChild("LatLonBoundingBox");
        if (latLonBoundingBoxElement != null) {
            LatLonBoundingBox latLonBoundingBox = parseLatLonBoundingBox(latLonBoundingBoxElement);
            layer.setLatLonBoundingBox(latLonBoundingBox);
        }        
        
        List boundingBoxElements = layerElement.getChildren("BoundingBox");
        if (boundingBoxElements != null) {
            List boundingBoxes = new ArrayList();
            iter = boundingBoxElements.iterator();
            while (iter.hasNext()) {
                Element boundingBoxElement = (Element) iter.next();
                boundingBoxes.add(parseBoundingBox(boundingBoxElement));
            }
            layer.setBoundingBoxes(boundingBoxes);
        }
        
        List dimensionElements = layerElement.getChildren("Dimension");
        if (dimensionElements != null) {
            List dimensions = new ArrayList();
            iter = dimensionElements.iterator();
            while (iter.hasNext()) {
                Element dimensionElement = (Element) iter.next();
                dimensions.add(parseDimension(dimensionElement));
            }
            layer.setDimensions(dimensions);
        }
        
        List extentElements = layerElement.getChildren("Extent");
        if (extentElements != null) {
            List extents = new ArrayList();
            iter = extentElements.iterator();
            while (iter.hasNext()) {
                Element extentElement = (Element) iter.next();
                extents.add(parseExtent(extentElement));
            }
            layer.setExtents(extents);
        }
        
        Element attributionElement = layerElement.getChild("Attribution");
        if (attributionElement != null) {
            Attribution attribution = new Attribution();
            
            if (attributionElement.getChildText("Title") != null) {
                attribution.setTitle(attributionElement.getChildText("Title"));
            }
            
            if (attributionElement.getChild("OnlineResource") != null) {
                attribution.setOnlineResource(parseOnlineResource(attributionElement.getChild("OnlineResource")));
            }
            
            Element logoURLElement = attributionElement.getChild("LogoURL");
            if (logoURLElement != null) {
                WmsURL wmsURL = parseWmsURL(logoURLElement);
                int width = Integer.parseInt(logoURLElement.getAttributeValue("width"));
                int height = Integer.parseInt(logoURLElement.getAttributeValue("height"));
                LogoURL logoURL = new LogoURL(wmsURL.getFormat(), wmsURL.getOnlineResource(), width, height );
                attribution.setLogoURL(logoURL);
            }
            layer.setAttribution(attribution);
        }
        
        List authorityURLElements = layerElement.getChildren("AuthorityURL");
        if (authorityURLElements != null) {
            List authorityURLs = new ArrayList();
            iter = authorityURLElements.iterator();
            while (iter.hasNext()) {
                Element authorityURLElement = (Element) iter.next();
                URL onlineResource = parseOnlineResource(authorityURLElement.getChild("OnlineResource"));
                String authName = authorityURLElement.getAttributeValue("name");
                authorityURLs.add(new AuthorityURL(onlineResource, authName));
            }
            layer.setAuthorityURLs(authorityURLs);
        }
        
        List identifierElements = layerElement.getChildren("Identifier");
        if (identifierElements != null) {
            List identifiers = new ArrayList();
            iter = identifierElements.iterator();
            while (iter.hasNext()) {
                Element identifierElement = (Element) iter.next();
                String value = identifierElement.getText();
                String authority = identifierElement.getAttributeValue("authority");
                identifiers.add(new Identifier(value, authority));
            }
            layer.setIdentifiers(identifiers);
        }
        
        List metadataURLElements = layerElement.getChildren("MetadataURL");
        if (metadataURLElements != null) {
            List metadataURLs = new ArrayList();
            iter = metadataURLElements.iterator();
            while (iter.hasNext()) {
                Element metadataURLElement = (Element) iter.next();
                WmsURL wmsURL = parseWmsURL(metadataURLElement);
                String typeString = metadataURLElement.getAttributeValue("type");
                int type = -1;
                if (typeString.equals("FGDC")) {
                    type = MetadataURL.FGDC;
                } else if (typeString.equals("TC211")) {
                    type = MetadataURL.TC211;
                }
                metadataURLs.add(new MetadataURL(wmsURL.getFormat(), wmsURL.getOnlineResource(), type));
            }
            layer.setMetadataURLs(metadataURLs);
        }
        
        List dataURLElements = layerElement.getChildren("DataURL");
        if (dataURLElements != null) {
            List dataURLs = new ArrayList();
            iter = dataURLElements.iterator();
            while (iter.hasNext()) {
                Element element = (Element) iter.next();
                WmsURL wmsURL = parseWmsURL(element);
                DataURL dataURL = new DataURL(wmsURL.getFormat(), wmsURL.getOnlineResource());
                dataURLs.add(dataURL);
            }
            layer.setDataURL(dataURLs);
        }
        
        List featureListURLElements = layerElement.getChildren("FeatureListURL");
        if (featureListURLElements != null) {
            List featureListURLs = new ArrayList();
            iter = featureListURLElements.iterator();
            while (iter.hasNext()) {
                Element element = (Element) iter.next();
                WmsURL wmsURL = parseWmsURL(element);
                FeatureListURL featureListURL = new FeatureListURL(wmsURL.getFormat(), wmsURL.getOnlineResource());
                featureListURLs.add(featureListURL);
            }
            layer.setFeatureListURL(featureListURLs);
        }
        
        List styleElements = layerElement.getChildren("Style");
        if (styleElements != null) {
            List styles = new ArrayList();
            iter = styleElements.iterator();
            while (iter.hasNext()) {
                Element element = (Element) iter.next();
                styles.add(parseStyle(element));
            }
            layer.setStyles(styles);
        }
        
        Element scaleHintElement = layerElement.getChild("ScaleHint");
        if (scaleHintElement != null) {
            String min = scaleHintElement.getAttributeValue("min");
            String max = scaleHintElement.getAttributeValue("max");
            
            ScaleHint scaleHint = new ScaleHint(min, max);
            layer.setScaleHint(scaleHint);
        }
        
        List subLayerElements = layerElement.getChildren("Layer");
        if (subLayerElements != null) {
            List subLayers = new ArrayList();
            iter = subLayerElements.iterator();
            while (iter.hasNext()) {
                Element element = (Element) iter.next();
                subLayers.add(parseLayer(element));
            }
            layer.setChildLayer(subLayers);
        }
        
        /**
         * Optional attributes
         */
        
        if (Integer.parseInt(layerElement.getAttributeValue("queryable")) == 1) {
            layer.setQueryable(true);
        }
        
        String cascaded = layerElement.getAttributeValue("cascaded");
        if (cascaded != null) {
            layer.setCascaded(cascaded);
        }
        
        if (Integer.parseInt(layerElement.getAttributeValue("opaque")) == 1) {
            layer.setOpaque(true);
        }
        
        if (Integer.parseInt(layerElement.getAttributeValue("noSubsets")) == 1) {
            layer.setNoSubsets(true);
        }
        
        String fixedWidth = layerElement.getAttributeValue("fixedWidth");
        if (fixedWidth != null) {
            layer.setFixedWidth(fixedWidth);
        }
        
        String fixedHeight = layerElement.getAttributeValue("fixedHeight");
        if (fixedHeight != null) {
            layer.setFixedHeight(fixedHeight);
        }
        
        return layer;
    }

    /**
     * @param element
     * @return
     */
    private static Style parseStyle(Element element) throws MalformedURLException {
        Style style;
        
        String name = element.getChildText("Name");
        String title = element.getChildText("Title");
        
        style = new Style(name, title);
        
        String _abstract = element.getChildText("Abstract");
        if (_abstract != null) {
            style.set_abstract(_abstract);
        }
        
        List legendURLElements = element.getChildren("LegendURL");
        if (legendURLElements != null) {
            List legendURLs = new ArrayList();
            Iterator iter = legendURLElements.iterator();
            while (iter.hasNext()) {
                Element legendURLElement = (Element) iter.next();
                WmsURL wmsURL = parseWmsURL(legendURLElement);
                int width = Integer.parseInt(legendURLElement.getAttributeValue("width"));
                int height = Integer.parseInt(legendURLElement.getAttributeValue("height"));
                LegendURL legendURL = new LegendURL(wmsURL.getFormat(), wmsURL.getOnlineResource(), width, height );
                legendURLs.add(legendURL);
            }
            style.setLegendURLs(legendURLs);
        }
        
        Element styleSheetURLElement = element.getChild("StyleSheetURL");
        if (styleSheetURLElement != null) {
            WmsURL wmsURL = parseWmsURL(styleSheetURLElement);
            StyleSheetURL styleSheetURL = new StyleSheetURL(wmsURL.getFormat(), wmsURL.getOnlineResource());
            style.setStyleSheetURL(styleSheetURL);
        }
        
        Element styleURLElement = element.getChild("StyleURL");
        if (styleURLElement != null) {
            WmsURL wmsURL = parseWmsURL(styleURLElement);
            StyleURL styleURL = new StyleURL(wmsURL.getFormat(), wmsURL.getOnlineResource());
            style.setStyleURL(styleURL);
        }        
        
        return style;
    }

    /**
     * @param child
     * @return
     */
    private static WmsURL parseWmsURL(Element wmsURLElement) throws MalformedURLException {
        WmsURL wmsURL;
        String format = wmsURLElement.getChildText("Format");
        URL onlineResource = parseOnlineResource(wmsURLElement.getChild("OnlineResource"));
        wmsURL = new WmsURL(format, onlineResource);
        return wmsURL;
    }

    /**
     * @param extentElement
     * @return
     */
    private static Extent parseExtent(Element extentElement) {
        Extent extent;
        String value = extentElement.getText();
        String name = extentElement.getAttributeValue("name");
        extent = new Extent(value, name);
        
        String _default = extentElement.getAttributeValue("default");
        extent.set_default(_default);
        
        return extent;
    }

    /**
     * @param dimensionElement
     * @return
     */
    private static Dimension parseDimension(Element dimensionElement) {
        Dimension dimension;
        
        String name = dimensionElement.getAttributeValue("name");
        String units = dimensionElement.getAttributeValue("units");
                
        dimension = new Dimension(name, units);
        
        String unitSymbol = dimensionElement.getAttributeValue("unitSymbol");
        if (unitSymbol != null) {
            dimension.setUnitSymbol(unitSymbol);
        }
        
        return dimension;
    }

    /**
     * @param boundingBoxElement
     * @return
     */
    private static BoundingBox parseBoundingBox(Element boundingBoxElement) {
        BoundingBox boundingBox;
        
        String srs = boundingBoxElement.getAttributeValue("SRS");
        String minx = boundingBoxElement.getAttributeValue("minx");
        String miny = boundingBoxElement.getAttributeValue("miny");
        String maxx = boundingBoxElement.getAttributeValue("maxx");
        String maxy = boundingBoxElement.getAttributeValue("maxy");
        
        boundingBox = new BoundingBox(srs, minx, miny, maxx, maxy);
        
        String resx = boundingBoxElement.getAttributeValue("resx");
        if (resx != null) {
            boundingBox.setResX(resx);
        }
        
        String resy = boundingBoxElement.getAttributeValue("resy");
        if (resy != null) {
            boundingBox.setResY(resy);
        }
        
        return boundingBox;
    }

    /**
     * @param latLonBoundingBoxElement
     * @return
     */
    private static LatLonBoundingBox parseLatLonBoundingBox(Element latLonBoundingBoxElement) {
        LatLonBoundingBox latLonBoundingBox;
        
        String minx = latLonBoundingBoxElement.getAttributeValue("minx");
        String miny = latLonBoundingBoxElement.getAttributeValue("miny");
        String maxx = latLonBoundingBoxElement.getAttributeValue("maxx");
        String maxy = latLonBoundingBoxElement.getAttributeValue("maxy");
        
        latLonBoundingBox = new LatLonBoundingBox(minx, miny, maxx, maxy);
        
        return latLonBoundingBox;
    }

    /**
     * @param userDefinedSymbolizationElement
     * @return
     */
    private static UserDefinedSymbolization parseUserDefinedSymbolization(Element userDefinedSymbolizationElement) {
        UserDefinedSymbolization userDefinedSymbolization = new UserDefinedSymbolization();
        
        if (Integer.parseInt(userDefinedSymbolizationElement.getAttributeValue("SupportSLD")) == 1) {
            userDefinedSymbolization.setSupportSLD(true);
        }
        
        if (Integer.parseInt(userDefinedSymbolizationElement.getAttributeValue("UserLayer")) == 1) {
            userDefinedSymbolization.setUserLayer(true);
        }        
        
        if (Integer.parseInt(userDefinedSymbolizationElement.getAttributeValue("UserStyle")) == 1) {
            userDefinedSymbolization.setUserStyle(true);
        }
        
        if (Integer.parseInt(userDefinedSymbolizationElement.getAttributeValue("RemoteWFS")) == 1) {
            userDefinedSymbolization.setRemoteWFS(true);
        }
        
        return userDefinedSymbolization;
    }

    /**
     * Presently, I have no idea what to do here.
     * @param vendorSpecificCapabilitiesElement
     * @return
     */
    private static VendorSpecificCapabilities parseVendorSpecificCapabilities(Element vendorSpecificCapabilitiesElement) {
        return null;
    }

    private static Exception parseException(Element exceptionElement) {
        Exception exception;

        List formats = new ArrayList();
        List formatElements = exceptionElement.getChildren("Format");
        Iterator iter = formatElements.iterator();
        while (iter.hasNext()) {
            Element formatElement = (Element) iter.next();
            formats.add(formatElement.getText());
        }
        
        exception = new Exception(formats);
        
        return exception;
    }

    /**
     * Given an element that represents the getCapabilities request element, it will parse
     * that element and return a Request object.
     * 
     * @param requestElement an Element representing a request element
     * @return a Request object constructed from the passed-in element
     */
    private static Request parseRequest(Element requestElement) throws MalformedURLException {
        Request request;
        RequestType requestType;
        
        requestType = parseRequestType(requestElement.getChild("GetCapabilities"));
        GetCapabilities getCapabilities = new GetCapabilities(requestType.getFormats(), requestType.getDcpTypes()); 

        requestType = parseRequestType(requestElement.getChild("GetMap"));
        GetMap getMap = new GetMap(requestType.getFormats(), requestType.getDcpTypes());
        
        request = new Request(getCapabilities, getMap);
        
        
        if (requestElement.getChild("GetFeatureInfo") != null) {
            requestType = parseRequestType(requestElement.getChild("GetFeatureInfo"));
            request.setGetFeatureInfo(new GetFeatureInfo(requestType.getFormats(), requestType.getDcpTypes()));
        }
        
        if (requestElement.getChild("DescribeLayer") != null ) {
            requestType = parseRequestType(requestElement.getChild("DescribeLayer"));
            request.setDescribeLayer(new DescribeLayer(requestType.getFormats(), requestType.getDcpTypes()));
        }
        
        if (requestElement.getChild("GetLegendGraphic") != null ) {
            requestType = parseRequestType(requestElement.getChild("GetLegendGraphic"));
            request.setGetLegendGraphic(new GetLegendGraphic (requestType.getFormats(), requestType.getDcpTypes()));
        }
        
        if (requestElement.getChild("GetStyles") != null) {
            requestType = parseRequestType(requestElement.getChild("GetStyles"));
            request.setGetStyles(new GetStyles(requestType.getFormats(), requestType.getDcpTypes()));
        }
        
        if (requestElement.getChild("PutStyles") != null) {
            requestType = parseRequestType(requestElement.getChild("PutStyles"));
            request.setPutStyles(new PutStyles(requestType.getFormats(), requestType.getDcpTypes()));
        }
        
        return request;
    }

    private static RequestType parseRequestType(Element element) throws MalformedURLException {
        RequestType requestType;
        Iterator iter;
        
        List formats = new ArrayList();
        List formatElements = element.getChildren("Format");
        iter = formatElements.iterator();
        while (iter.hasNext()) {
            Element formatElement = (Element) iter.next();
            formats.add(formatElement.getValue());
        }
        
        List dcpTypes = new ArrayList();
        List dcpTypeElements = element.getChildren("DCPType");
        iter = dcpTypeElements.iterator();
        while (iter.hasNext()) {
            Element dcpTypeElement = (Element) iter.next();
            dcpTypes.add(parseDCPType(dcpTypeElement));
        }
        
        requestType = new RequestType(formats, dcpTypes);
        
        return requestType;
    }

    private static DCPType parseDCPType(Element element) throws MalformedURLException{
        DCPType dcpType;
        Iterator iter;
        
        Element httpElement = element.getChild("HTTP");
        
        List gets = new ArrayList();
        List getElements = httpElement.getChildren("Get");
        iter = getElements.iterator();
        while (iter.hasNext()) {
            Element getElement = (Element) iter.next();
            HTTPRequestType httpRequestType = parseHTTPRequestType(getElement); 
            gets.add(new Get(httpRequestType.getOnlineResource()));   
        }
        
        List posts = new ArrayList();
        List postElements = httpElement.getChildren("Post");
        iter = postElements.iterator();
        while (iter.hasNext()) {
            Element postElement = (Element) iter.next();
            HTTPRequestType httpRequestType = parseHTTPRequestType(postElement); 
            posts.add(new Post(httpRequestType.getOnlineResource()));            
        }
        
        dcpType = new DCPType(new HTTP(gets, posts));
        
        return dcpType;
    }

    /**
     * @param postElement
     * @return
     */
    private static HTTPRequestType parseHTTPRequestType(Element element) throws MalformedURLException {
        HTTPRequestType requestType;
        requestType = new HTTPRequestType(parseOnlineResource(element.getChild("OnlineResource")));
        return requestType;
    }

    /**
     * @param element
     * @return
     */
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
		service.setFees(serviceElement.getChildText("Fees"));
		service.setAccessConstraints(serviceElement.getChildText("Access Constraints"));
		
		Element keywordListElement = serviceElement.getChild("KeywordList");
		if (keywordListElement != null) {
		    List keywords = parseKeywordList(keywordListElement);
		    
		    
		    
		    service.setKeywordList(keywords);
		}
		
		Element contactInformation = serviceElement.getChild("Contact Information");
		if (contactInformation != null) {
		    service.setContactInformation(parseContactInformation(contactInformation));
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

    /**
     * Given an element that represents the getCapabilities contactInformation element, it will parse
     * that element and return a ContactInformation object.
     * 
     * @param contactInformationElement an Element representing a contactInformation element
     * @return a ContactInformation object constructed from the passed-in element
     */
    private static ContactInformation parseContactInformation(Element contactInformationElement) {
        ContactInformation contactInformation = new ContactInformation();

		Element contactPersonPrimary = contactInformationElement.getChild("ContactPersonPrimary");
		if (contactPersonPrimary != null) {
		    String contactPerson = contactPersonPrimary.getChildText("ContactPerson");
		    String contactOrganization = contactPersonPrimary.getChildText("ContactOrganization");
		    
		    ContactPersonPrimary person = new ContactPersonPrimary(contactPerson, contactOrganization);
		    contactInformation.setContactPersonPrimary(person);
		}
		
		contactInformation.setContactPosition(contactInformationElement.getChildText("ContactPosition"));
		
		Element contactAddress = contactInformationElement.getChild("ContactAddress");
		if (contactAddress != null) {
		    String addressType = contactAddress.getChildText("AddressType");
		    String address = contactAddress.getChildText("Address");
		    String city = contactAddress.getChildText("City");
		    String stateOrProvince = contactAddress.getChildText("StateOrProvince");
		    String postCode = contactAddress.getChildText("PostCode");
		    String country = contactAddress.getChildText("Country");
		    
		    ContactAddress newAddress = new ContactAddress(addressType, address, city, stateOrProvince, postCode, country);
		    contactInformation.setContactAddress(newAddress);
		}
		
		contactInformation.setContactVoiceTelephone(contactInformationElement.getChildText("ContactVoiceTelephone"));
		contactInformation.setContactFacsimileTelephone(contactInformationElement.getChildText("ContactFacsimileTelephone"));
		contactInformation.setContactElectronicMailAddress(contactInformationElement.getChildText("ContactElectronicMailAddress"));		
        
        return contactInformation;
    }    
}
