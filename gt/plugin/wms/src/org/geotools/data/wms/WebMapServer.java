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

/*
 * Created on Jun 24, 2004
 *
 */
package org.geotools.data.wms;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import org.geotools.data.wms.getCapabilities.Layer;
import org.geotools.data.wms.getCapabilities.WMT_MS_Capabilities;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

/**
 * @author rgould
 * 
 * When performing the GetCapabilities request, all query parameters
 * are saved and over-ride the defaults:
 * service=WMS
 * version=1.1.1
 * request=GetCapabilities 
 * 
 * WebMapServer is a class representing a WMS.
 */
public class WebMapServer {
	
	private final URL serverURL;
	private WMT_MS_Capabilities capabilities;
	private Exception problem;
	
	private Thread retrieveCapabilities;
	public static final int IN_PROGRESS = 1;
	public static final int NOTCONNECTED = 0;
	public static final int ERROR = -1;
	public static final int CONNECTED = 2;
	
	/**
	 * Create a WebMapServer and immediately retrieve the GetCapabilities
	 * document in another thread. 
	 * 
	 * If there is an error while attempting to retrieve the GetCapabilities
	 * document, the exception will be placed in the problem field and can be
	 * retrieved using getProbelm(). An error can be detected if getStatus
	 * returns ERROR.
	 * 
	 * @param serverURL the URL that points to the WMS's GetCapabilities document
	 */
	public WebMapServer (URL serverURL) {
		this(serverURL, false);
	}
	
	/**
	 * Create a WebMapServer based on the URL located in serverURL.
	 * This will attempt to retrieve the serverURL in a thread unless
	 * wait is true, in which case it will retrieved later when requested or
	 * when needed.
	 * 
	 * If there is an error while attempting to retrieve the GetCapabilities
	 * document, the exception will be placed in the problem field and can be
	 * retrieved using getProbelm(). An error can be detected if getStatus
	 * returns ERROR.
	 * 
	 * @param serverURL the URL that points to the WMS's GetCapabilities document
	 * @param wait true if the GetCapabilities document should not be retrieved immediately
	 */
	public WebMapServer (final URL serverURL, boolean wait) {
		this.serverURL = serverURL;
		if (!wait) {
			retrieveCapabilities = new Thread(new Runnable() {
				public void run() {
					retrieveCapabilities();
				}
			});			
			retrieveCapabilities.start();
		}
	}
	
	/**
	 * Gets the current status of the GetCapabilities document.
	 * <UL>
	 * <LI>IN_PROGRESS: The thread is currently retrieving the GetCapabilities document
	 * <LI>NOTCONNECTED: Thread has not attempt to retrieve document yet
	 * <LI>CONNECTED: The GetCapabilities document has been successfully retrieved.
	 * <LI>ERROR: An error has occured. It can be retrieved using getProblem()
	 * </UL>
	 * @return the current status of the GetCapabilities document
	 */
	public int getStatus() {
		if (retrieveCapabilities != null && retrieveCapabilities.isAlive()) {
			return IN_PROGRESS;
		}

		if (problem != null) {
			return ERROR;
		}
		
		if (capabilities == null) {
			return NOTCONNECTED;
		}
		
		return CONNECTED;
	}

	/**
	 * Retrieves the serverURL request from the WMS and populates
	 * a Capabilities object with the data.
	 * Populates the problem field if there is an exception. 
	 */	
	private void retrieveCapabilities() {
		try {

			//Get the actual serverURL XML string from the server.
			URL getCapabilitiesURL = null;
			String query = "";
			int index = serverURL.toExternalForm().lastIndexOf("?");
			String urlWithoutQuery = null;
			if (index <= 0) {
				urlWithoutQuery = serverURL.toExternalForm();
			} else {
				urlWithoutQuery = serverURL.toExternalForm().substring(0, index);
			}
			
			if (serverURL.getQuery() == null || serverURL.getQuery().length() == 0) {
				query = "?request=GetCapabilities&service=WMS&version=1.1.1";
			} else {
				
				//Doing this preserves all of the query parameters while
				//enforcing the mandatory ones
				
				Properties properties = new Properties();
				properties.setProperty("request", "GetCapabilities");
				properties.setProperty("service", "WMS");
				properties.setProperty("version", "1.1.1");
				
				StringTokenizer tokenizer = new StringTokenizer(serverURL.getQuery(), "&");
				while (tokenizer.hasMoreTokens()) {
					String token = tokenizer.nextToken();
					String[] param = token.split("=");
					properties.setProperty(param[0], param[1]);
				}
				Iterator iter = properties.keySet().iterator();
				query = query+"?";
				while (iter.hasNext()) {
					String key = (String) iter.next();
					query = query + key+"="+properties.getProperty(key);
					if (iter.hasNext()) {
						query = query+"&";
					}
				}
			}
			getCapabilitiesURL = new URL(urlWithoutQuery + query);
			
			SAXBuilder builder = new SAXBuilder();
			Document document = builder.build(getCapabilitiesURL);

			Element root = document.getRootElement(); //Root = "WMT_MS_Capabilities"
			
			capabilities = CapabilitiesParser.parseCapabilities(root);
			
		} catch (JDOMException e) {
			problem = e;
			//throw new RuntimeException("Data at the given URL is not valid XML", e);
		} catch (ParseCapabilitiesException e) {
			problem = e;
			//throw new RuntimeException("XML at the given URL is not a valid serverURL document");
		} catch (IOException e) {
			problem = e;
			//throw new RuntimeException("Unable to connect to the URL", e);
		}
	}
	
	/**
	 * Get the getCapabilities document. If it is not already retrieved,
	 * then it shall be retrieved. If it returns null, there is an error
	 * which must be checked with getProblem()
	 * 
	 * @return a WMT_MS_Capabilities, or null if there was an error
	 */
	public WMT_MS_Capabilities getCapabilities() {
		if (capabilities == null) {
			if (retrieveCapabilities != null && retrieveCapabilities.isAlive()) {
				try {
					retrieveCapabilities.join();
					return capabilities;
				} catch (InterruptedException e) {
					problem = e;
					return null;
				}
			}
			retrieveCapabilities();
		}
		return capabilities;
	}

	/**
	 * Executes a GetMap request and returns the results. 
	 * 
	 * @param request a GetMapRequest containing all the valid parameters
	 * @return the response from the server as a result of the request
	 * @throws IOException if there is a network error
	 */	
	public static GetMapResponse issueGetMapRequest(GetMapRequest request) throws IOException {
	    GetMapResponse response;
		
	    URL finalURL = request.getFinalURL();

	    URLConnection connection = finalURL.openConnection();
	    InputStream inputStream = connection.getInputStream();

	    String contentType = connection.getContentType();
	    
	    response = new GetMapResponse(contentType, inputStream);
	    
	    return response;
	}
	
    /**
     * Iterate through the layers and extract and return all layers containing a <name>.
     * @param layers The list of Layers to iterate through.
     * @return A list of Layers each containing a <name>
     */
    public static List getNamedLayers(List layers) {
        List namedLayers = new ArrayList();
        
        Iterator iter = layers.iterator();
        while (iter.hasNext()) {
            Layer layer = (Layer) iter.next();
            if (layer.getName() != null && !layer.getName().equals("")) {
                namedLayers.add(layer);
            }
            if (layer.getSubLayers() != null) {
                namedLayers.add(getNamedLayers(layer.getSubLayers()));
            }
        }
        
        return namedLayers;
    }

	public Exception getProblem() {
		return problem;
	}
}
