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
package org.geotools.data.wms.request;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.StringTokenizer;


/**
 * DOCUMENT ME!
 *
 * @author Richard Gould
 */
public abstract class GetCapabilitiesRequest extends AbstractRequest {
    /**
     * DOCUMENT ME!
     *
     * @param serverURL
     *
     * @throws RuntimeException DOCUMENT ME!
     */
    public GetCapabilitiesRequest(URL serverURL) {
        super(serverURL, null);

        initRequest();
        initService();
        initVersion();
    }

    /**
     * Default implementation REQUEST = GetCapabilities
     * 
     * <p>
     * Subclass can override if needed.
     * </p>
     */
    protected void initRequest() {
        setProperty("REQUEST", "GetCapabilities");
    }

    /**
     * Default implementation SERVICE = WMS
     */
    protected void initService() {
        setProperty("SERVICE", "WMS");
    }

    /**
     * Sets up the version number for this request. Typically something like
     * setProperty("VERSION", "1.1.1");
     */
    protected abstract void initVersion();
}
