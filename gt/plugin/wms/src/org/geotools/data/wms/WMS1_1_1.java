/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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

import java.net.URL;
import java.util.List;
import java.util.Set;

import org.geotools.data.wms.request.AbstractGetCapabilitiesRequest;

/**
 * Provides support for the Web Map Server 1.1.1 Specificaiton.
 * <p>
 * This class opperates as a Factory creating the following related objects.
 * <ul>
 * <li>WMS1_1_1.Parser - a WMSParser capable of parsing a Get Capabilities Document</li>
 * <li>WMS1_1_1.Format - a WMSFormat describing required parameters</li>
 * <li>WMS1_1_1.MapRequest - a MapRequest specific to WMS 1.0</li>
 * </ul>
 * </p>
 * <p>
 * The idea is that this class opperates a Toolkit for all things assocated with Web Map Server 1.1.1 Specification. The
 * various objects produced by this toolkit are used as stratagy objects for the top level Web Map Server objects:
 * <ul>
 * <li>Web Map Server - uses WMS1_1_1.Parser to derive a Get Capabilities Document</li>
 * <li>Web Map Server - uses WMS1_1_1 as a WMSFormat factory to generate the correct WMS_1_1_1.Format.</li>
 * </ul>
 * </p>
 * <p>
 * WMS1_1_1 provides both name and version information that may be checked against a GetCapabilities document during
 * version negotiation.
 * </p>
 * 
 * @author Jody Garnett, Refractions Research
 */
public class WMS1_1_1 extends WMS1_1_0 {
    public WMS1_1_1() {
    }

    /**
     * Expected version attribute for root element.
     * 
     * @return DOCUMENT ME!
     */
    public String getVersion() {
        return "1.1.1";
    }

    /**
     * Factory method to create WMS 1.1.1 GetCapabilities Request
     * 
     * @param server DOCUMENT ME!
     * @return DOCUMENT ME!
     */
    public AbstractGetCapabilitiesRequest createGetCapabilitiesRequest( URL server ) {
        return new GetCapsRequest(server);
    }

    static public class GetCapsRequest extends WMS1_1_0.GetCapsRequest {
        public GetCapsRequest( URL urlGetCapabilities ) {
            super(urlGetCapabilities);
        }

        protected void initVersion() {
            setProperty("VERSION", "1.1.1");
        }
    }

    static public class GetMapRequest extends WMS1_1_0.GetMapRequest {

        public GetMapRequest( URL onlineResource ) {
            super(onlineResource);
        }

        protected void initVersion() {
            setVersion("1.1.1");
        }
    }

    static public class GetFeatureInfoRequest extends WMS1_1_0.GetFeatureInfoRequest {

        public GetFeatureInfoRequest( URL onlineResource, org.geotools.data.wms.request.GetMapRequest request) {
            super(onlineResource, request);
        }

        protected void initVersion() {
            setProperty("VERSION", "1.1.1");
        }
    }

    public org.geotools.data.wms.request.GetMapRequest createGetMapRequest( URL get ) {
        return new GetMapRequest(get);
    }

    public org.geotools.data.wms.request.GetFeatureInfoRequest createGetFeatureInfoRequest( URL onlineResource, org.geotools.data.wms.request.GetMapRequest getMapRequest) {
        return new GetFeatureInfoRequest(onlineResource, getMapRequest);
    }
}
