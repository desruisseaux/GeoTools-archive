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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.geotools.data.wms.request.GetCapabilitiesRequest;
import org.geotools.util.InternationalString;
import org.jdom.Document;
import org.jdom.Element;


/**
 * Provides support for the Web Map Server 1.0 Specificaiton.
 * <p>
 * This class opperates as a Factory creating the following related objects.
 * <ul>
 * <li>WMS1_0_0.Parser - a WMSParser capable of parsing a Get Capabilities Document
 * <li>WMS1_0_0.Format - a WMSFormat describing required parameters
 * <li>WMS1_0_0.MapRequest - a MapRequest specific to WMS 1.0
 * </ul>
 * </p>
 * <p>
 * The idea is that this class opperates a Toolkit for all things assocated with
 * Web Map Server 1.0.0 Specification. The various objects produced by this toolkit
 * are used as stratagy objects for the top level Web Map Server objects:
 * <ul>
 * <li>Web Map Server - uses WMS1_0_0.Parser to derive a Get Capabilities Document
 * <li>Web Map Server - uses WMS1_0_0 as a WMSFormat factory to generate the correct
 *     WMS_1_0_0.Format.
 * </ul>
 * </p>
 * <p>
 * WMS1_0_0 provides both name and version information that may be checked against
 * a GetCapabilities document during version negotiation.
 * </p>
 * @author Jody Garnett, Refractions Research
 */
public class WMS1_0_0 extends Specification {
    static final Map mime = new HashMap();

    //TODO Fill out the rest of these mime types!
    static {
        mime.put("GIF", "image/gif"); //$NON-NLS-1$ //$NON-NLS-2$
        mime.put("PNG", "image/png"); //$NON-NLS-1$ //$NON-NLS-2$
        mime.put("JPEG", "image/jpeg"); //$NON-NLS-1$ //$NON-NLS-2$
        mime.put("BMP", "image/bmp"); //$NON-NLS-1$ //$NON-NLS-2$
        mime.put("WebCGM", "image/cgm;Version=4;ProfileId=WebCGM"); //$NON-NLS-1$ //$NON-NLS-2$
        mime.put("SVG", "image/svg+xml"); //$NON-NLS-1$ //$NON-NLS-2$
        mime.put("GML.1", "text/xml"); //$NON-NLS-1$ //$NON-NLS-2$
        mime.put("GML.2", "text/xml"); //$NON-NLS-1$ //$NON-NLS-2$
        mime.put("GML.3", "text/xml"); //$NON-NLS-1$ //$NON-NLS-2$
        mime.put("WBMP", "image/vnd.wap.wbmp"); //$NON-NLS-1$ //$NON-NLS-2$
        mime.put("WMS_XML", "application/vnd.ogc.wms_xml"); //$NON-NLS-1$ //$NON-NLS-2$
        mime.put("MIME", "mime"); //$NON-NLS-1$ //$NON-NLS-2$
        mime.put("INIMAGE", "application/vnd.ogc.se_inimage"); //$NON-NLS-1$ //$NON-NLS-2$
        mime.put("TIFF", "image/tiff"); //$NON-NLS-1$ //$NON-NLS-2$
        mime.put("GeoTIFF", "image/tiff"); //$NON-NLS-1$ //$NON-NLS-2$
        mime.put("PPM", "image/x-portable-pixmap"); //$NON-NLS-1$ //$NON-NLS-2$
        mime.put("BLANK", "application/vnd.ogc.se_blank"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    private WMSParser[] parsers;

    /**
     * Public constructor creates the WMS1_0_0 object.
     */
    public WMS1_0_0() {
        parsers = new WMSParser[1];
        parsers[0] = new Parser();
    }

    /** 
     * Expected name attribute for root element
     * @return the name of the root element for this capabilities specification 
     */
    public String getName() {
        return "WMT_MS_Capabilities"; //$NON-NLS-1$
    }

    /**
     * Expected version attribute for root element.
     * @return the expect version value for this specification
     */
    public String getVersion() {
        return "1.0.0"; //$NON-NLS-1$
    }

    /**
     * Provides mapping from well known format to MIME type.
     * <p>
     * WebMapServer api uses mime type internally for format information
     * (indeed WMS 1.0.0 is the only WMS specifcation not to use MIME type
     * directly).
     * </p>
     * <p>
     * @param format
     * @return MIME type for format
     */
    public static final String toMIME(String format) {
        if (mime.containsKey(format)) {
            return (String) mime.get(format);
        }

        return null;
    }

    /**
     * Provides mapping from MIME type to WMS 1.0.0 Format.
     * <p>
     * WebMapServer api uses mime type internally for format information
     * (indeed WMS 1.0.0 is the only WMS specifcation not to use MIME type
     * directly).
     * </p>
     * <p>
     * @param mimeType MIME type such as "image/gif"
     * @return Format well known WMS 1.0.0 format such as "GIF"
     */
    public static final String toFormat(String mimeType) {
        for (Iterator i = mime.entrySet().iterator(); i.hasNext();) {
            Map.Entry entry = (Map.Entry) i.next();

            if (mimeType.equals(entry.getValue())) {
                return (String) entry.getKey();
            }
        }

        return null;
    }

    /**
     * Create a WMSParser capable of parsing a 1.0.0 Capabilities document
     * @see org.geotools.data.wms.Specification#createParser(org.jdom.Document)
     * @param document a JDOM Document containing the Capabilities to be parsed
     * @return a WMSParser that is capable of parsing the provided document
     */
    public WMSParser createParser(Document document) throws IOException {
        WMSParser generic = null;
        WMSParser custom = null;

        for (int i = 0; i < parsers.length; i++) {
            int canProcess = parsers[i].canProcess(document);

            if (canProcess == WMSParser.GENERIC) {
                generic = parsers[i];
            } else if (canProcess == WMSParser.CUSTOM) {
                custom = parsers[i];
            }
        }

        WMSParser parser = generic;

        if (custom != null) {
            parser = custom;
        }

        if (parser == null) {
            // Um can we have the name & version number please?
            throw new RuntimeException(
                new InternationalString("No parsers available to parse that GetCapabilities document").toString());
        }

        return new Parser();
    }

    /**
     * Create a request for performing GetCapabilities requests on a 1.0.0 server.
     * @see org.geotools.data.wms.Specification#createGetCapabilitiesRequest(java.net.URL)
     * @param server a URL that points to the 1.0.0 server
     * @return a GetCapabilitiesRequest object that can provide a valid request
     */
    public GetCapabilitiesRequest createGetCapabilitiesRequest(URL server) {
        return new GetCapsRequest(server);
    }

    /**
     * We need a custom request object.
     * <p>
     * WMS 1.0.0 does requests a bit differently:
     * <ul>
     * <li>WMTVER=1.0.0
     * <li>
     * </p>
     */
    static public class GetCapsRequest extends GetCapabilitiesRequest {
        /**
         * Construct a Request compatable with a 1.0.0 Web Feature Server.
         *
         * @param urlGetCapabilities URL of GetCapabilities document.
         */
        public GetCapsRequest(URL urlGetCapabilities) {
            super(urlGetCapabilities);
        }

        protected void initVersion() {
            setProperty("WMTVER", "1.0.0"); //$NON-NLS-1$ //$NON-NLS-2$
        }

        protected void initRequest() {
            setProperty("REQUEST", "capabilities"); //$NON-NLS-1$ //$NON-NLS-2$
        }

        protected void initService() {
            //The 1.0.0 specification does not use the service property
        }
    }

    /**
     * A GetMapRequest for a 1.0.0 Server 
     */
    static public class GetMapRequest
        extends org.geotools.data.wms.request.GetMapRequest {
        /**
         * Constructs a GetMapRequest for use with a 1.0.0 server
         * @param onlineResource the URL for server's GetMap request
         * @param availableLayers provides information about the server's layers and styles
         * @param availableSRSs provides a Set of all known SRS on the server
         * @param availableFormats provides all known formats for the GetMap request
         * @param availableExceptions provides all known exceptions for the server
         */
        public GetMapRequest(URL onlineResource, 
            SimpleLayer[] availableLayers, Set availableSRSs,
            String[] availableFormats, List availableExceptions) {
            super(onlineResource, "1.0.0", availableLayers, availableSRSs, //$NON-NLS-1$
                availableFormats, availableExceptions);
        }

        protected void initRequest() {
            setProperty("REQUEST", "map"); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    /**
     * A GetFeatureInfoRequest for a 1.0.0 server
     */
    static public class GetFeatureInfoRequest
        extends org.geotools.data.wms.request.GetFeatureInfoRequest {
        /**
         * Constructs a GetFeatureInfoRequest for use with a 1.0.0 server
         * @param onlineResource the URL pointing to the place to execute a GetFeatureInfo request
         * @param getMapRequest a previously executed GetMapRequest that the query will be executed on
         * @param queryableLayers a Set of all the layers that can be queried
         * @param infoFormats all the known formats that can be returned by the request
         */
        public GetFeatureInfoRequest(URL onlineResource,
            GetMapRequest getMapRequest, Set queryableLayers,
            String[] infoFormats) {
            super(onlineResource, getMapRequest, queryableLayers, infoFormats);
        }

        protected void initRequest() {
            setProperty("REQUEST", "feature_info"); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    /**
     * A WMSParser capable of parsing 1.0.0 GetCapabilities document
     */
    static public class Parser extends AbstractWMSParser {
        /**
         * @return the version that this parser supports.
         */
        public String getVersion() {
            return "1.0.0"; //$NON-NLS-1$
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
            Element formatElement = op.getChild("Format"); //$NON-NLS-1$

            Iterator iter;

            List formats = new ArrayList();
            List formatElements = formatElement.getChildren();
            iter = formatElements.iterator();

            while (iter.hasNext()) {
                Element format = (Element) iter.next();
                String mimeType = toMIME(format.getName());

                if (mimeType != null) {
                    formats.add(mimeType);
                }
            }

            return formats;
        }

        protected URL queryDCPType(Element element, String httpType)
            throws MalformedURLException {
            List dcpTypeElements = element.getChildren("DCPType"); //$NON-NLS-1$

            for (Iterator i = dcpTypeElements.iterator(); i.hasNext();) {
                Element dcpTypeElement = (Element) i.next();
                Element httpElement = dcpTypeElement.getChild("HTTP"); //$NON-NLS-1$

                Element httpTypeElement = httpElement.getChild(httpType);

                if (httpTypeElement != null) {
                    return new URL((httpTypeElement.getAttributeValue(
                            "onlineResource"))); //$NON-NLS-1$
                }
            }

            return null;
        }

        protected URL queryServiceOnlineResource(Element serviceElement)
            throws MalformedURLException {
            return new URL(serviceElement.getChildText("OnlineResource")); //$NON-NLS-1$
        }

        protected String[] queryKeywords(Element serviceElement) {
            String keywords = serviceElement.getChildTextTrim("Keywords"); //$NON-NLS-1$

            return keywords.split(" "); //$NON-NLS-1$
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

        protected List querySRS(Element layerElement) {
            Element srsElement = layerElement.getChild("SRS"); //$NON-NLS-1$

            if (srsElement != null) {
                return Arrays.asList(srsElement.getTextTrim().split(" ")); //$NON-NLS-1$
            }

            return null;
        }
    }
}
