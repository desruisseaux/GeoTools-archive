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
import java.util.ArrayList;
import java.util.List;

import org.geotools.data.wms.capabilities.Capabilities;
import org.geotools.data.wms.capabilities.Layer;
import org.geotools.data.wms.capabilities.Request;
import org.geotools.data.wms.capabilities.Service;
import org.jdom.Document;
import org.jdom.Element;

/**
 * Provides support for the Web Map Server 1.0 Specificaiton.
 * <p>
 * This class opperates as a Factory creating the following related objects.
 * <ul>
 * <li>WMS1_0.Parser - a WMSParser capable of parsing a Get Capabilities Document
 * <li>WMS1_0.Format - a WMSFormat describing required parameters
 * <li>WMS1_0.MapRequest - a MapRequest specific to WMS 1.0
 * </ul>
 * </p>
 * <p>
 * The idea is that this class opperates a Toolkit for all things assocated with
 * Web Map Server 1.0 Specification. The various objects produced by this toolkit
 * are used as stratagy objects for the top level Web Map Server objects:
 * <ul>
 * <li>Web Map Server - uses WMS1_0.Parser to derive a Get Capabilities Document
 * <li>Web Map Server - uses WMS1_0 as a WMSFormat factory to generate the correct
 *     WMS_1_0.Format.
 * </ul>
 * </p> 
 * <p>
 * WMS1_0 provides both name and version information that may be checked against
 * a GetCapabilities document during version negotiation.
 * </p> 
 * @author Jody Garnett, Refractions Research
 */
public class WMS1_0 {
    
    /** Expected name attribute for root element */
    public String getName(){
        return "WMT_MS_Capabilities";
    }
    /**
     * Expected version attribute for root element.
     */
    public String getVersion(){
        return "1.0.0";
    }
    
    static public class Parser extends AbstractWMSParser {
        public String getVersion() {
            return "1.0.0";
        }        
    }
}
