/*
 * Created on Jun 29, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.data.wms.capabilities;

import java.net.URL;

/**
 * @author rgould
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class Get extends HTTPRequestType {

    /**
     * @param onlineResource URL prefix for each HTTP request method
     */
    public Get(URL onlineResource) {
        super(onlineResource);
    }
}
