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
import java.net.URL;

import org.geotools.data.wms.request.GetCapabilitiesRequest;
import org.jdom.Document;

/**
 * Provides support for the Web Map Server Specificaitons.
 * <p>
 * This class operates as a Factory creating the following related objects.
 * <ul>
 * <li>WMSParser - a WMSParser capable of parsing a Get Capabilities Document
 * <li>WMSFormat - a WMSFormat describing required parameters
 * <li>GetCapabilities - a GetCapabilities request
 * <li>MapRequest - a MapRequest specific to the current specification
 * </ul>
 * </p>
 * <p>
 * The idea is that this class operates a Toolkit for all things assocated with
 * a Web Map Server Specification. The various objects produced by this toolkit
 * are used as strategy objects for the top level WebMapServer object:
 * <ul>
 * <li>WebMapServer - uses a WMSParser to derive a Capabilities object
 * <li>WebMapServer - uses a GetCapabilitiesRequest during version negotiation.
 * <li>WMSGridCoverageExchange - uses a WMSFormatFactory to generate the correct WMSFormat
 * </ul>
 * </p> 
 * <p>
 * Both name and version information that may be checked against
 * a GetCapabilities document during version negotiation.
 * </p>
 * <p>
 * <b>Q:</b> Why are these not static?<br>
 * <b>A:</b> Because we want to place new specifications into a data structure for WebMapServer
 * to search through dynamically
 * </p>
 * @author Jody Garnett, Refractions Reasearch
 */
public abstract class Specification {
    /** Expected name attribute for root element */
    public String getName(){
        return "WMT_MS_Capabilities";
    }
    /**
     * Expected version attribute for root element.
     */
    public abstract String getVersion();
        
    
    /** Factory method to create WMSGetCapabilities Request */
    public abstract GetCapabilitiesRequest createGetCapabilitiesRequest( URL server );
    /**
     * Create parser given document generated from createRequest.
     * <p>
     * By allowing the specification to choose a Parser based on the document
     * we are given an oppertunity to choose a specific parser for several
     * vendors that generate unusual Capabilities documents.
     * </p>
     * <p>
     * That is even the version number negotiation worked out
     * with getVersion and createRequest above may not be sufficient to
     * narrow down our options to a specific parser. Actual document inspection
     * is required.
     * </p>
     * @param document
     * @return Parser capable of handling provided document
     * @throws IOException if there is an error reading document
     */
    public abstract WMSParser createParser( Document document ) throws IOException ;
}
