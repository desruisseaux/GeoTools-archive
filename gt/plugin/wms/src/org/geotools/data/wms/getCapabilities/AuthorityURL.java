/*
 * Created on Jun 30, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.data.wms.getCapabilities;

import java.net.URL;

/**
 * @author rgould
 *
 * A Map Server may use zero or more Identifier elements to list ID numbers
 * or labels defined by a particular Authority.  For example, the Global Change
 * Master Directory (gcmd.gsfc.nasa.gov) defines a DIF_ID label for every
 * dataset.  The authority name and explanatory URL are defined in a spearate
 * AuthorityURL element, which may be defined once and inherited by subsidiary
 * layers.  Identifiers themselves are not inherited.
 */
public class AuthorityURL {
    private URL onlineResource;
    private String name;
    
    
    /**
     * @param onlineResource
     * @param name
     */
    public AuthorityURL(URL onlineResource, String name) {
        super();
        this.onlineResource = onlineResource;
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public URL getOnlineResource() {
        return onlineResource;
    }
    public void setOnlineResource(URL onlineResource) {
        this.onlineResource = onlineResource;
    }
}
