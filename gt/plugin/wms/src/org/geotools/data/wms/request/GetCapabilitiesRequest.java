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
package org.geotools.data.wms.request;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.StringTokenizer;


/**
 * @author Richard Gould
 */
public class GetCapabilitiesRequest extends AbstractRequest {

	/**
	 * @param onlineResource
	 */
	public GetCapabilitiesRequest(URL serverURL) {
		super(serverURL);
		initRequest();
		initService();
		initVersion();	
		
		// Need to strip off the query, as getFinalURL will add it back
		// on, with all the other properties. If we don't, elements will
		// be duplicated.
		int index = serverURL.toExternalForm().lastIndexOf("?");
		String urlWithoutQuery = null;
		if (index <= 0) {
			urlWithoutQuery = serverURL.toExternalForm();
		} else {
			urlWithoutQuery = serverURL.toExternalForm().substring(0, index);
		}
		
		try {
			this.onlineResource = new URL(urlWithoutQuery);
		} catch (MalformedURLException e) {
			throw new RuntimeException("Error parsing URL");
		}
		
		// Doing this preserves all of the query parameters while
		// enforcing the mandatory ones

		if (serverURL.getQuery() != null) {		
			StringTokenizer tokenizer = new StringTokenizer(serverURL.getQuery(), "&");
			while (tokenizer.hasMoreTokens()) {
				String token = tokenizer.nextToken();
				String[] param = token.split("=");
				setProperty(param[0], param[1]);
			}
		}
	}
	/**
	 * Default implementation REQUEST = GetCapabilities
	 * <p>
	 * Subclass can override if needed.
	 * </p>
	 */
	protected void initRequest(){
	    setProperty("REQUEST", "GetCapabilities");
	}
	/**
	 * Default implementation SERVICE = WMS
	 */
	protected void initService(){
	    setProperty("SERVICE", "WMS");
	}
	/**
	 * Default implementation VERSION = 1.1.1
	 * <p>
	 * Subclass can override if needed:
	 * <pre><code>
	 * protected void initVersion(){
	 *   setProperty("WEBVER", "1.0.0");
	 * }
	 * </code></pre>
	 * </p>
	 */
	protected void initVersion(){
	    setProperty("VERSION", "1.1.1");
	}
}
