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

import org.geotools.data.wms.request.GetCapabilitiesRequest;
import org.geotools.data.wms.request.GetMapRequest;
import org.geotools.util.InternationalString;
import org.jdom.Document;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Set;


/**
 * Provides support for the Web Map Server Specificaitons.
 * 
 * <p>
 * This class operates as a Factory creating the following related objects.
 * 
 * <ul>
 * <li>
 * WMSParser - a WMSParser capable of parsing a Get Capabilities Document
 * </li>
 * <li>
 * WMSFormat - a WMSFormat describing required parameters
 * </li>
 * <li>
 * GetCapabilities - a GetCapabilities request
 * </li>
 * <li>
 * MapRequest - a MapRequest specific to the current specification
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
 * WebMapServer - uses a WMSParser to derive a Capabilities object
 * </li>
 * <li>
 * WebMapServer - uses a GetCapabilitiesRequest during version negotiation.
 * </li>
 * <li>
 * WMSGridCoverageExchange - uses a WMSFormatFactory to generate the correct
 * WMSFormat
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
     *
     * @return DOCUMENT ME!
     */
    public abstract String getVersion();

    /**
     * Factory method to create WMSGetCapabilities Request
     *
     * @param server DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public abstract GetCapabilitiesRequest createGetCapabilitiesRequest(
        URL server);

    public abstract GetMapRequest createGetMapRequest( URL get, SimpleLayer[] layers, Set availableSRSs, String[] formatStrings, List exceptions );
}
