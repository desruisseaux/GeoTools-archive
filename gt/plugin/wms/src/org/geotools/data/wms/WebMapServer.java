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
import java.util.Set;
import java.util.TreeSet;

import org.geotools.data.wms.getCapabilities.DCPType;
import org.geotools.data.wms.getCapabilities.Get;
import org.geotools.data.wms.getCapabilities.Layer;
import org.geotools.data.wms.getCapabilities.WMT_MS_Capabilities;
import org.geotools.data.wms.request.AbstractRequest;
import org.geotools.data.wms.request.GetCapabilitiesRequest;
import org.geotools.data.wms.request.GetFeatureInfoRequest;
import org.geotools.data.wms.request.GetMapRequest;
import org.geotools.data.wms.response.AbstractResponse;
import org.geotools.data.wms.response.GetCapabilitiesResponse;
import org.geotools.data.wms.response.GetFeatureInfoResponse;
import org.geotools.data.wms.response.GetMapResponse;
import org.jdom.JDOMException;

/**
 * WebMapServer is a class representing a WMS.
 * 
 * <p>
 * When performing the GetCapabilities request, all query parameters
 * are saved and over-ride the defaults:
 * <pre><code>
 * service=WMS
 * version=1.1.1
 * request=GetCapabilities
 * </code></pre> 
 * </p>
 * 
 * <p>
 * The current implementation is targeted towards the 1.3.0 OGC WMS
 * Implementation Specification. There are plans to generalize this support
 * for previous revisions.
 * </p>
 * 
 * @author Richard Gould, Refractions Research
 */
public class WebMapServer {
	
	private final URL serverURL;
	private WMT_MS_Capabilities capabilities;
	private Exception problem;
	
	public static final int IN_PROGRESS = 1;
	public static final int NOTCONNECTED = 0;
	public static final int ERROR = -1;
	public static final int CONNECTED = 2;
	private Thread getMapRetriever;
	
	private GetMapResponse getMapResponse;
	private GetMapRequest getMapRequest;
	private AbstractRequest currentRequest;
	private Thread requestRetriever;
	private AbstractResponse currentResponse;
	
	/**
	 * Create a WebMapServer and immediately retrieve the GetCapabilities
	 * document in another thread. 
	 * 
	 * If there is an error while attempting to retrieve the GetCapabilities
	 * document, the exception will be placed in the problem field and can be
	 * retrieved using getProblem(). An error can be detected if getStatus
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
		
		if (wait) 
			return;
		
		issueRequest(new GetCapabilitiesRequest(serverURL), !wait);
	}
	
	/**
	 * Gets the current status of the GetCapabilities document.
	 * <UL>
	 * <LI>IN_PROGRESS: The thread is currently retrieving a request
	 * <LI>NOTCONNECTED: Thread has not attempt to retrieve document yet
	 * <LI>CONNECTED: The most recently issued request has been successfully retrieved.
	 * <LI>ERROR: An error has occured. It can be retrieved using getProblem()
	 * </UL>
	 * @return the current status of the GetCapabilities document
	 */
	public int getStatus() {
		if (requestRetriever != null && requestRetriever.isAlive()) {
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
	 * Get the getCapabilities document. If it is not already retrieved,
	 * then it shall be retrieved. If it returns null, there is an error
	 * which must be checked with getProblem()
	 * 
	 * @return a WMT_MS_Capabilities, or null if there was an error
	 */
	public WMT_MS_Capabilities getCapabilities() {
		if (capabilities == null) {
			if (requestRetriever != null && requestRetriever.isAlive()) {
				try {
					requestRetriever.join();
					if (capabilities == null) {
						issueRequest(new GetCapabilitiesRequest(serverURL), false);
					}
					return capabilities;
				} catch (InterruptedException e) {
					problem = e;
					return null;
				}
			}
			issueRequest(new GetCapabilitiesRequest(serverURL), false);
		}
		return capabilities;
	}

	public AbstractResponse issueRequest(AbstractRequest request, boolean threaded) {
		this.problem = null;
		this.currentRequest = request;
		if (threaded) {
			requestRetriever = new Thread(new Runnable() {
				public void run() {
					issueRequest();
				}
			});
			requestRetriever.start();
			return null;
		}
		issueRequest();

		return currentResponse;
	}
	
	private void issueRequest() {
		try {
			URL finalURL = currentRequest.getFinalURL();

			URLConnection connection = finalURL.openConnection();
			InputStream inputStream = connection.getInputStream();

			String contentType = connection.getContentType();
	    
			//Must check featureInfo first, as it subclasses GetMapRequest
			if (currentRequest instanceof GetFeatureInfoRequest) {
				currentResponse = new GetFeatureInfoResponse(contentType, inputStream);
			} else if (currentRequest instanceof GetMapRequest) {
				currentResponse = new GetMapResponse(contentType, inputStream);
			} else if (currentRequest instanceof GetCapabilitiesRequest) {
				try {
					currentResponse = new GetCapabilitiesResponse(contentType, inputStream);
					capabilities = ((GetCapabilitiesResponse) currentResponse).getCapabilities();
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
			} else {
				throw new RuntimeException("Request is an invalid type. I do not know it.");
			}
		} catch (IOException e) {
			problem = e;
		}
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
		
		if (capabilities == null) {
			throw new RuntimeException("Unable to create a GetMapRequest when the GetCapabilities document has not been retrieved");
		}
		
		DCPType dcpType = (DCPType) getCapabilities().getCapability().getRequest().getGetMap().getDcpTypes().get(0);
	    Get get = (Get) dcpType.getHttp().getGets().get(0);
		
		GetMapRequest request = 
			new GetMapRequest(get.getOnlineResource(),
							  getCapabilities().getVersion(),
							  Utils.findDrawableLayers(getCapabilities().getCapability().getLayer()),
							  getSRSs(),
							  getCapabilities().getCapability().getRequest().getGetMap().getFormats(),
							  getExceptions()
							  );
		
		return request;
	}
	
	public GetFeatureInfoRequest createGetFeatureInfoRequest(GetMapRequest getMapRequest) {
		if (capabilities == null) {
			throw new RuntimeException("Unable to create a GetFeatureInfoRequest without a GetCapabilities document");
		}
		
		if (getCapabilities().getCapability().getRequest().getGetFeatureInfo() == null) {
			throw new UnsupportedOperationException("This Web Map Server does not support GetFeatureInfo requests");
		}
		
		DCPType dcpType = (DCPType) getCapabilities().getCapability().getRequest().getGetFeatureInfo().getDcpTypes().get(0);
	    Get get = (Get) dcpType.getHttp().getGets().get(0);
		
		GetFeatureInfoRequest request = 
			new GetFeatureInfoRequest(
					get.getOnlineResource(),
					getMapRequest,
					getQueryableLayers(),
					getCapabilities().getCapability().getRequest().getGetFeatureInfo().getFormats()
					);
		return request;
	}
	
	private Set getQueryableLayers() {
		Set layers = new TreeSet();
		
		List namedLayers = getNamedLayers();
		for (int i = 0; i < namedLayers.size(); i++) {
			Layer layer = (Layer) namedLayers.get(i);
			if (layer.isQueryable()) {
				layers.add(layer);
			}
		}
		
		return layers;
	}


	private List getExceptions() {
		return getCapabilities().getCapability().getException().getFormats();
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
