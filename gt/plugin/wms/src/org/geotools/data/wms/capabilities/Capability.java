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
package org.geotools.data.wms.capabilities;

import org.geotools.data.wms.capabilities.Exception;

/**
 * @author rgould
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class Capability {
    
    /** Available WMS Operations are listed in a Request element. */
    private Request request;
    
    /** An Exception element indicates which error-reporting formats are supported. */
    private Exception exception;
    
    private VendorSpecificCapabilities vendorSpecificCapabilities;
    
    /** Optional user-defined symbolization (used only by SLD-enabled WMSes). */
    private UserDefinedSymbolization userDefinedSymboliazation;
    
    /** Nested list of zero or more map Layers offered by this server */
    private Layer layer;
    
    /**
     * @param request
     * @param exception
     */
    public Capability(Request request, Exception exception) {
        this.request = request;
        this.exception = exception;
    }
    
    public Exception getException() {
        return exception;
    }
    public void setException(Exception exception) {
        this.exception = exception;
    }
    public Layer getLayer() {
        return layer;
    }
    public void setLayer(Layer layer) {
        this.layer = layer;
    }
    public Request getRequest() {
        return request;
    }
    public void setRequest(Request request) {
        this.request = request;
    }
    public UserDefinedSymbolization getUserDefinedSymboliazation() {
        return userDefinedSymboliazation;
    }
    public void setUserDefinedSymboliazation(
            UserDefinedSymbolization userDefinedSymboliazation) {
        this.userDefinedSymboliazation = userDefinedSymboliazation;
    }
    public VendorSpecificCapabilities getVendorSpecificCapabilities() {
        return vendorSpecificCapabilities;
    }
    public void setVendorSpecificCapabilities(
            VendorSpecificCapabilities vendorSpecificCapabilities) {
        this.vendorSpecificCapabilities = vendorSpecificCapabilities;
    }
}
