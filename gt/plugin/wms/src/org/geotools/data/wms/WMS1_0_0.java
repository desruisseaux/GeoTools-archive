/*
 * Geotools2 - OpenSource mapping toolkit http://geotools.org (C) 2004, Geotools Project Managment Committee (PMC) This
 * library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; version 2.1 of the License. This library is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 */
package org.geotools.data.wms;

import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.geotools.data.ows.Layer;
import org.geotools.data.wms.request.AbstractGetFeatureInfoRequest;
import org.geotools.data.wms.request.AbstractGetMapRequest;
import org.geotools.data.wms.request.AbstractGetCapabilitiesRequest;
import org.geotools.data.wms.request.DescribeLayerRequest;
import org.geotools.data.wms.request.GetFeatureInfoRequest;
import org.geotools.data.wms.request.GetMapRequest;

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
 * The idea is that this class opperates a Toolkit for all things assocated with Web Map Server 1.0.0 Specification. The
 * various objects produced by this toolkit are used as stratagy objects for the top level Web Map Server objects:
 * <ul>
 * <li>Web Map Server - uses WMS1_0_0.Parser to derive a Get Capabilities Document
 * <li>Web Map Server - uses WMS1_0_0 as a WMSFormat factory to generate the correct WMS_1_0_0.Format.
 * </ul>
 * </p>
 * <p>
 * WMS1_0_0 provides both name and version information that may be checked against a GetCapabilities document during
 * version negotiation.
 * </p>
 * 
 * @author Jody Garnett, Refractions Research
 */
public class WMS1_0_0 extends Specification {
    static final Map mime = new HashMap();

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

    /**
     * Public constructor creates the WMS1_0_0 object.
     */
    public WMS1_0_0() {

    }

    /**
     * Expected version attribute for root element.
     * 
     * @return the expect version value for this specification
     */
    public String getVersion() {
        return "1.0.0"; //$NON-NLS-1$
    }

    /**
     * Provides mapping from well known format to MIME type.
     * <p>
     * WebMapServer api uses mime type internally for format information (indeed WMS 1.0.0 is the only WMS specifcation
     * not to use MIME type directly).
     * </p>
     * <p>
     * 
     * @param format
     * @return MIME type for format
     */
    public static final String toMIME( String format ) {
        if (mime.containsKey(format)) {
            return (String) mime.get(format);
        }

        return null;
    }

    /**
     * Provides mapping from MIME type to WMS 1.0.0 Format.
     * <p>
     * WebMapServer api uses mime type internally for format information (indeed WMS 1.0.0 is the only WMS specifcation
     * not to use MIME type directly).
     * </p>
     * <p>
     * 
     * @param mimeType MIME type such as "image/gif"
     * @return Format well known WMS 1.0.0 format such as "GIF"
     */
    public static final String toFormat( String mimeType ) {
        for( Iterator i = mime.entrySet().iterator(); i.hasNext(); ) {
            Map.Entry entry = (Map.Entry) i.next();

            if (mimeType.equals(entry.getValue())) {
                return (String) entry.getKey();
            }
        }

        return null;
    }

    /**
     * Create a request for performing GetCapabilities requests on a 1.0.0 server.
     * 
     * @see org.geotools.data.wms.Specification#createGetCapabilitiesRequest(java.net.URL)
     * @param server a URL that points to the 1.0.0 server
     * @return a AbstractGetCapabilitiesRequest object that can provide a valid request
     */
    public AbstractGetCapabilitiesRequest createGetCapabilitiesRequest( URL server ) {
        return new GetCapsRequest(server);
    }

    /**
     * We need a custom request object.
     * <p>
     * WMS 1.0.0 does requests a bit differently:
     * <ul>
     * <li>WMTVER=1.0.0</li>
     * </p>
     */
    static public class GetCapsRequest extends AbstractGetCapabilitiesRequest {
        /**
         * Construct a Request compatable with a 1.0.0 Web Feature Server.
         * 
         * @param urlGetCapabilities URL of GetCapabilities document.
         */
        public GetCapsRequest( URL urlGetCapabilities ) {
            super(urlGetCapabilities);
        }

        protected void initVersion() {
            setProperty("WMTVER", "1.0.0"); //$NON-NLS-1$ //$NON-NLS-2$
            properties.remove("VERSION");
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
    static public class GetMapRequest extends AbstractGetMapRequest {
        /**
         * Constructs a GetMapRequest for use with a 1.0.0 server
         * 
         * @param onlineResource the URL for server's GetMap request
         * @param availableLayers provides information about the server's layers and styles
         * @param availableSRSs provides a Set of all known SRS on the server
         * @param availableFormats provides all known formats for the GetMap request
         * @param availableExceptions provides all known exceptions for the server
         */
        public GetMapRequest( URL onlineResource, SimpleLayer[] availableLayers, Set availableSRSs,
                String[] availableFormats, List availableExceptions ) {
            super(onlineResource, null, availableLayers, availableSRSs, //$NON-NLS-1$
                    availableFormats, availableExceptions);
        }

        protected void initRequest() {
            setProperty("REQUEST", "map"); //$NON-NLS-1$ //$NON-NLS-2$
        }

        protected void initVersion() {
            setProperty(VERSION, "1.0.0");
        }

        public void setProperty( String name, String value ) {
            if (name.equals(FORMAT)) {
                value = getRequestFormat(value);
            }

            super.setProperty(name, value);
        }

        public void setFormat( String value ) {
            setProperty(FORMAT, value);
        }

        protected String getRequestFormat( String format ) {
            return toFormat(format);
        }
    }

    /**
     * A GetFeatureInfoRequest for a 1.0.0 server
     */

    static public class GetFeatureInfoRequest extends AbstractGetFeatureInfoRequest {
        /**
         * @param onlineResource
         * @param request
         * @param queryableLayers
         * @param infoFormats
         */
        public GetFeatureInfoRequest( URL onlineResource, org.geotools.data.wms.request.GetMapRequest request, Set queryableLayers, String[] infoFormats ) {
            super(onlineResource, request, queryableLayers, infoFormats);
        }

        protected void initVersion() {
            setProperty("WMTVER", "1.0.0");
        }
    }

    /** 
     * @see org.geotools.data.wms.Specification#createGetMapRequest(java.net.URL, java.lang.String,
     *      org.geotools.data.wms.SimpleLayer[], java.util.Set, java.lang.String[], java.util.List)
     */
    public org.geotools.data.wms.request.GetMapRequest createGetMapRequest( URL get, SimpleLayer[] layers,
            Set availableSRSs, String[] formatStrings, List exceptions ) {
        return new GetMapRequest(get, layers, availableSRSs, formatStrings, exceptions);
    }

    /**
     * @see org.geotools.data.wms.Specification#createGetFeatureInfoRequest(java.net.URL,
     *      org.geotools.data.wms.request.GetMapRequest, java.util.Set, java.lang.String[])
     */
    public org.geotools.data.wms.request.GetFeatureInfoRequest createGetFeatureInfoRequest( URL onlineResource,
            org.geotools.data.wms.request.GetMapRequest getMapRequest, Set queryableLayers, String[] infoFormats ) {
        return new GetFeatureInfoRequest(onlineResource, getMapRequest, queryableLayers, infoFormats);
    }

    /**
     * Note that WMS 1.0.0 does not support this method.
     * @see org.geotools.data.wms.Specification#createDescribeLayerRequest(java.net.URL)
     */
    public DescribeLayerRequest createDescribeLayerRequest( URL onlineResource ) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("WMS 1.0.0 does not support DescribeLayer");
    }
}
