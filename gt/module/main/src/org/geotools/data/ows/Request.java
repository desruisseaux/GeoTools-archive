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
package org.geotools.data.ows;

import java.net.URL;
import java.util.Properties;

/**
 * This represents a Request to be made against a Web Map Server.
 * @source $URL: http://svn.geotools.org/geotools/branches/2.2.x/plugin/wms/src/org/geotools/data/wms/request/Request.java $
 */
public interface Request {
    
    /** Represents the REQUEST parameter */
    public static final String REQUEST = "REQUEST"; //$NON-NLS-1$
    /** Represents the VERSION parameter */
    public static final String VERSION = "VERSION"; //$NON-NLS-1$
    /** Represents the WMTVER parameter */
    public static final String WMTVER = "WMTVER"; //$NON-NLS-1$
    public static final String SERVICE = "SERVICE";
    
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
     * Note that when using this method, it is up to the programmer to
     * provide their own encoding of <code>value</code> according to the
     * WMS specifications! The code will not do this for you. Please ensure
     * that you are familiar with this. See section 6.2.1 of the WMS 1.1.1 spec
     * and 6.3.2 of the WMS 1.3.0 spec. 
     * 
     * If value is null, "name" is removed from the properties table.
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
