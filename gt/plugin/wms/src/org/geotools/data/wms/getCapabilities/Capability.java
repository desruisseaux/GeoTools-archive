/*
 * Created on Jun 29, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.data.wms.getCapabilities;

import org.geotools.data.wms.getCapabilities.Exception;

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
