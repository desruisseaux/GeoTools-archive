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
import org.geotools.data.wms.request.DescribeLayerRequest;
import org.geotools.data.wms.request.GetFeatureInfoRequest;
import org.geotools.data.wms.request.GetMapRequest;


/**
 * Provides support for the Web Map Server Specificaitons.
 * 
 * <p>
 * This class operates as a Factory creating the following related objects.
 * 
 * <ul>
 * <li>
 * AbstractGetCapabilitiesRequest
 * </li>
 * <li>
 * GetMapRequest
 * </li>
 * <li>
 * GetFeatureInfoRequest
 * </li>
 * </ul>
 * </p>
 * 
 * <p>
 * The idea is that this class operates a Toolkit for all things assocated with
 * a Web Map Server Specification. The various objects produced by this
 * toolkit are used as strategy objects for the top level WebMapServer object:
 * 
 * <ul>
 * <li>
 * WebMapServer - uses a AbstractGetCapabilitiesRequest during version negotiation.
 * </li>
 * </ul>
 * </p>
 * 
 * <p>
 * Both name and version information that may be checked against a
 * GetCapabilities document during version negotiation.
 * </p>
 * 
 * <p>
 * <b>Q:</b> Why are these not static?<br>
 * <b>A:</b> Because we want to place new specifications into a data structure
 * for WebMapServer to search through dynamically
 * </p>
 *
 * @author Jody Garnett, Refractions Reasearch
 */
public abstract class Specification {

    /**
     * Expected version attribute for root element.
     * @return the version as a String
     */
    public abstract String getVersion();

    /**
     * Factory method to create WMSGetCapabilities Request
     * @param server the URL that points to the server's getCapabilities document
     * @return a configured AbstractGetCapabilitiesRequest that can be used to access the Document 
     */
    public abstract AbstractGetCapabilitiesRequest createGetCapabilitiesRequest(
        URL server);

    /**
     * Creates a GetMapRequest for this specification, populating it with valid
     * values.
     * 
     * @param onlineResource the URL for the GetMapRequest
     * @param availableLayers valid layers for this request
     * @param availableSRSs a Set of type String representing valid SRS values for this request
     * @param formats available formats for the GetMapResponse
     * @param availableExceptions available formats for any thrown Exceptions
     * @return a GetMapRequest that can be configured and used
     */
    public abstract GetMapRequest createGetMapRequest( URL onlineResource, SimpleLayer[] availableLayers, Set availableSRSs, String[] formats, List availableExceptions );

    /**
     * Creates a GetFeatureInfoRequest for this specification, populating it 
     * with valid values.
     * 
     * @param onlineResource the URL to be executed against
     * @param getMapRequest a previously configured GetMapRequest
     * @param queryableLayers a Set of type Layer, each of which must be queryable
     * @param formats a list of known formats that are valid for GetFeatureInfoResponses 
     * @return a GetFeatureInfoRequest that can be configured and used
     */
    public abstract GetFeatureInfoRequest createGetFeatureInfoRequest( URL onlineResource, GetMapRequest getMapRequest, Set queryableLayers, String[] formats );
    
    /**
     * Creates a DescribeLayer request which can be used to retrieve
     * information about specific layers on the Web Map Server.
     * 
     * @param onlineResource the location where the request can be made
     * @return a DescribeLayerRequest to be configured and then passed to the Web Map Server
     * @throws UnsupportedOperationException if the version of the specification doesn't support this request
     */
    public abstract DescribeLayerRequest createDescribeLayerRequest( URL onlineResource ) throws UnsupportedOperationException;
}
