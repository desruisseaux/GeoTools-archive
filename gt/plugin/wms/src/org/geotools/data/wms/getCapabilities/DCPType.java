/*
 * Created on Jun 29, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.data.wms.getCapabilities;

/**
 * @author rgould
 *
 * Available Distributed Computing Platforms (DCPs) are
 * listed here. At present, only HTTP is defined.
 */
public class DCPType {
    
    /** Represents available HTTP methods */
    private HTTP http;
    
    /**
     * @param http Represents available HTTP methods
     */
    public DCPType(HTTP http) {
        super();
        this.http = http;
    }
    
    public HTTP getHttp() {
        return http;
    }
    public void setHttp(HTTP http) {
        this.http = http;
    }
}
