/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, Geotools Project Managment Committee (PMC)
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

import java.net.URL;


/**
 * DOCUMENT ME!
 *
 * @author Richard Gould
 * @source $URL$
 */
public abstract class AbstractGetCapabilitiesRequest extends AbstractRequest implements GetCapabilitiesRequest{
    /** Represents the SERVICE parameter */
    public static final String SERVICE = "SERVICE"; //$NON-NLS-1$

    
    /**
     * Creates a AbstractGetCapabilitiesRequest and sets the REQUEST, VERSION and
     * SERVICE parameters.
     * 
     * @param serverURL
     */
    public AbstractGetCapabilitiesRequest(URL serverURL) {
        super(serverURL, null);

        initRequest();
        initService();
        initVersion();
    }

    /**
     * Sets the REQUEST parameter
     * <p>
     * Subclass can override if needed.
     * </p>
     */
    protected void initRequest() {
        setProperty(REQUEST, "GetCapabilities"); //$NON-NLS-1$
    }

    /**
     * Sets the SERVICE parameter
     */
    protected void initService() {
        setProperty(SERVICE, "WMS"); //$NON-NLS-1$
    }

    /**
     * Sets up the version number for this request. Typically something like
     * setProperty("VERSION", "1.1.1");
     */
    protected abstract void initVersion();
}
