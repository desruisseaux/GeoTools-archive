/*
 * Created on Aug 18, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.data.wms.request;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.StringTokenizer;


/**
 * @author Richard Gould
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class GetCapabilitiesRequest extends AbstractRequest {

	/**
	 * @param onlineResource
	 */
	public GetCapabilitiesRequest(URL serverURL) {
		super(serverURL);

		setProperty("REQUEST", "GetCapabilities");
		setProperty("SERVICE", "WMS");
		//TODO VERSION SUPPORT HERE
		//setProperty("VERSION", "1.1.1");

		//Need to strip off the query, as getFinalURL will add it back
		//on, with all the other properties. If we don't, elements will
		//be duplicated.
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
		
		//Doing this preserves all of the query parameters while
		//enforcing the mandatory ones

		if (serverURL.getQuery() != null) {		
			StringTokenizer tokenizer = new StringTokenizer(serverURL.getQuery(), "&");
			while (tokenizer.hasMoreTokens()) {
				String token = tokenizer.nextToken();
				String[] param = token.split("=");
				setProperty(param[0], param[1]);
			}
		}
	}
}
