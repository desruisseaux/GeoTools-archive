/*
 * Created on Jul 5, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.data.wms.getCapabilities;

import java.net.URL;

/**
 * Represents a request type part of the HTTP element.
 * ie: <get> or <post>
 */
public class HTTPRequestType {
    /** URL prefix for each HTTP request method */
    private URL onlineResource;
    
    /**
     * @param onlineResource URL prefix for each HTTP request method
     */
    public HTTPRequestType(URL onlineResource) {
        super();
        this.onlineResource = onlineResource;
    }
    
    
    public URL getOnlineResource() {
        return onlineResource;
    }
    public void setOnlineResource(URL onlineResource) {
        this.onlineResource = onlineResource;
    }
}
