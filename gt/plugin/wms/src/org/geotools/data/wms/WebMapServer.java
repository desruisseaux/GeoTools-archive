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
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

import org.geotools.data.wms.getCapabilities.DCPType;
import org.geotools.data.wms.getCapabilities.Get;
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
	
	private Thread capabilitiesRetriever;
	public static final int IN_PROGRESS = 1;
	public static final int NOTCONNECTED = 0;
	public static final int ERROR = -1;
	public static final int CONNECTED = 2;
	private Thread getMapRetriever;
	private GetMapResponse getMapResponse;
	private GetMapRequest getMapRequest;
	
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
			capabilitiesRetriever = new Thread(new Runnable() {
				public void run() {
					retrieveCapabilities();
				}
			});			
			capabilitiesRetriever.start();
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
		if (capabilitiesRetriever != null && capabilitiesRetriever.isAlive()) {
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
			if (capabilitiesRetriever != null && capabilitiesRetriever.isAlive()) {
				try {
					capabilitiesRetriever.join();
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
	public GetMapResponse issueGetMapRequest(GetMapRequest request, boolean threaded) {

		this.getMapRequest = request;
		
		if (threaded) {
			getMapRetriever = new Thread(new Runnable() {
				public void run() {
					retrieveGetMap();
				}
			});			 
			getMapRetriever.start();
			return null;
		}
		retrieveGetMap();
			    
	    return getMapResponse;
	}
	
	protected void retrieveGetMap() {
		try {
			URL finalURL = getMapRequest.getFinalURL();

			URLConnection connection = finalURL.openConnection();
			InputStream inputStream = connection.getInputStream();

			String contentType = connection.getContentType();
	    
			getMapResponse = new GetMapResponse(contentType, inputStream);
		} catch (IOException e) {
			problem = e;
		}
	}
	
	public GetMapResponse getGetMapResponse() {
		if (getMapResponse == null) {
			if (getMapRetriever != null && getMapRetriever.isAlive()) {
				try {
					getMapRetriever.join();
					return getMapResponse;
				} catch (InterruptedException e) {
					problem = e;
					return null;
				}
			}
			retrieveGetMap();
		}
		return getMapResponse;
	}

	/**
     * Utility method to return each layer that has a name.
	 * This method maintains no hierarchy at all.
	 * @return A list of type Layer, each value has a it's name property set
     */
    public List getNamedLayers() {
        List namedLayers = new ArrayList();
        
        Layer root = capabilities.getCapability().getLayer();
        getNamedLayers(root, namedLayers);
                
        return namedLayers;
    }

	/**
	 * @param root
	 * @param namedLayers
	 */
	private void getNamedLayers(Layer root, List namedLayers) {
		
		if (root.getName() != null && root.getName().length() != 0) {
			namedLayers.add(root);
		}
		
		if (root.getSubLayers() == null) {
			return;
		}
		
		Iterator iter = root.getSubLayers().iterator();
        while (iter.hasNext()) {
            Layer layer = (Layer) iter.next();

            getNamedLayers(layer, namedLayers);
        }
	}

	public Exception getProblem() {
		return problem;
	}

	public GetMapRequest createGetMapRequest() {
		
		if (getStatus() != CONNECTED) {
			throw new RuntimeException("Unable to create a GetMapRequest when the GetCapabilities document has not been retrieved");
		}
		
		DCPType dcpType = (DCPType) getCapabilities().getCapability().getRequest().getGetMap().getDcpTypes().get(0);
	    Get get = (Get) dcpType.getHttp().getGets().get(0);
		
		GetMapRequest request = 
			new GetMapRequest(get.getOnlineResource(),
							  getCapabilities().getVersion(),
							  Utils.findDrawableLayers(getCapabilities().getCapability().getLayer()),
							  getSRSs(),
							  getFormats(),
							  getExceptions()
							  );
		
		return request;
	}
	private List getExceptions() {
		return getCapabilities().getCapability().getException().getFormats();
	}

	private List getFormats() {
		return getCapabilities().getCapability().getRequest().getGetMap().getFormats();
	}

	private Set getSRSs() {
		Set srss = new TreeSet();
		
		getSRSs(getCapabilities().getCapability().getLayer(), srss);
		
		return srss;
	}

	private void getSRSs(Layer rootLayer, Set srss) {
		srss.addAll(rootLayer.getSrs());
		
		Iterator iter = rootLayer.getSubLayers().iterator();
		while (iter.hasNext()) {
			Layer layer = (Layer) iter.next();
			getSRSs(layer, srss);
		}
	}
}
