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

import java.net.URL;
import java.util.Properties;

/**
 * This represents a Request to be made against a Web Map Server.
 */
public interface Request {
    
    /** Represents the REQUEST parameter */
    public static final String REQUEST = "REQUEST"; //$NON-NLS-1$
    /** Represents the VERSION parameter */
    public static final String VERSION = "VERSION"; //$NON-NLS-1$
    /** Represents the WMTVER parameter */
    public static final String WMTVER = "WMTVER"; //$NON-NLS-1$
    
    /**
     * Once the properties of the request are configured, this will return
     * the URL that points to the server and contains all of the appropriate
     * name/value parameters. 
     * 
     * @return a URL that can be used to issue the request
     */
    public URL getFinalURL();
    
    /**
     * Sets the name/value property for this request.
     * 
     * @param name the name of the property
     * @param value the value of the property
     */
    public void setProperty(String name, String value);
    
    /**
     * @return the request's current property map
     */
    public Properties getProperties();
}
