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
package org.geotools.data.wms;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.geotools.data.wms.capabilities.Layer;
import org.geotools.data.wms.capabilities.Capabilities;
import org.geotools.data.wms.request.AbstractRequest;
import org.geotools.data.wms.request.GetCapabilitiesRequest;
import org.geotools.data.wms.request.GetFeatureInfoRequest;
import org.geotools.data.wms.request.GetMapRequest;
import org.geotools.data.wms.response.AbstractResponse;
import org.geotools.data.wms.response.GetCapabilitiesResponse;
import org.geotools.data.wms.response.GetFeatureInfoResponse;
import org.geotools.data.wms.response.GetMapResponse;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

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
 * <p>
 * Version number negotiation occurs as follows (credit OGC):
 * <ul>
 * <li><b>1)</b> If the server implements the requested version number,
 *     the server shall send that version.
 * <li><b>2a)</b> If a version unknown to the server is requested, the
 *     server shall send the highest version less than the requested version.
 * <li><b>2b)</b> If the client request is for a version lower than any of
 *     those known to the server, then the server shall send the lowest version it knows.
 * <li><b>3a)</b> If the client does not understand the new version number sent by the
 *     server, it may either cease communicating with the server or send a new request
 *     with a new version number that the client does understand but which is less than
 *     that sent by the server (if the server had responded with a lower version).
 * <li><b>3b)</b> If the server had responded with a higher version (because the request
 *     was for a version lower than any known to the server), and the client does not
 *     understand the proposed higher version, then the client may send a new request with
 *     a version number higher than that sent by the server.
 * </ul>
 * </p>
 * <p>
 * The OGC tells us to repeat this process (or give up). This means we are actually going to
 * come up with a bit of setup cost in figuring out our GetCapabilities request. Initial we
 * have been using the JDOM parser and a Builder pattern to parse any getCapabilities document.
 * </p>
 * <p>
 * This has a couple of drawbacks with respect to the above negotiation:
 * <ul>
 * <li>Each of the possibly many getCapability requests would need to be relized in memory
 *     as a JDOM Document.
 * <li>We only can realize a Capabilities object for specifications for which we have a parser.
 *     Even if the Server is willing to give us a GetCapabilities docuemnt we can understand
 *     we need to know enough to ask the correct question.
 * <li>
 * </ul>
 * <p>
 * So what to do? Easy - use the JDOM root element to confirm version numbers before starting
 * the parser dance. Hard - make a custom SAX based class that grabs the version number for a
 * provided stream. Use BufferedInputStream so that if the version numbers do match we can
 * "rollback" and start the capabilities parsing off at the start of the stream.
 * </p>
 * @author Richard Gould, Refractions Research
 */
public class WebMapServer {
	
	private final URL serverURL;
	private Capabilities capabilities;
	private Exception problem;
	
	private WMSParser[] parsers;
	
	public static final int IN_PROGRESS = 1;
	public static final int NOTCONNECTED = 0;
	public static final int ERROR = -1;
	public static final int CONNECTED = 2;
	private Thread getMapRetriever;
	
	private GetMapResponse getMapResponse;
	private GetMapRequest getMapRequest;
	private AbstractRequest currentRequest;
	/** Feedback: Why only one? */
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
		
		parsers = new WMSParser[1];
		parsers[0] = new Parser1_1_1();
		
		if (wait) 
			return;
		
		issueRequest(new GetCapabilitiesRequest(serverURL), !wait);
	}
	/**
	 * Negotiate for WMS GetCapabilities Document we know how to handle.
	 * <p>
	 * Version number negotiation occurs as follows (credit OGC):
	 * <ul>
	 * <li><b>1)</b> If the server implements the requested version number,
	 *     the server shall send that version.
	 * <li><b>2a)</b> If a version unknown to the server is requested, the
	 *     server shall send the highest version less than the requested version.
	 * <li><b>2b)</b> If the client request is for a version lower than any of
	 *     those known to the server, then the server shall send the lowest version it knows.
	 * <li><b>3a)</b> If the client does not understand the new version number sent by the
	 *     server, it may either cease communicating with the server or send a new request
	 *     with a new version number that the client does understand but which is less than
	 *     that sent by the server (if the server had responded with a lower version).
	 * <li><b>3b)</b> If the server had responded with a higher version (because the request
	 *     was for a version lower than any known to the server), and the client does not
	 *     understand the proposed higher version, then the client may send a new request with
	 *     a version number higher than that sent by the server.
	 * </ul>
	 * </p>
	 * @return GetCapabilitiesRequest suitable for use with a parser
	 */
	private GetCapabilitiesRequest negotiateGetCapDocument( GetCapabilitiesRequest request ){
	    // right now this is a pass though
	    return request;
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
	public Capabilities getCapabilities() {
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
				    Document document;
					SAXBuilder builder = new SAXBuilder();
					document = builder.build( finalURL.openStream() );
			        
					WMSParser generic = null;
					WMSParser custom = null;					
					for (int i = 0; i < parsers.length; i++) {
						int canProcess = parsers[i].canProcess( document );						
						if (canProcess == WMSParser.GENERIC) {
							generic = parsers[i];
						} else if (canProcess == WMSParser.CUSTOM) {
							custom = parsers[i];
						}
					}
					WMSParser parser = generic;
					if (custom != null) {
						parser = custom;
					}
					if (parser == null) {
					    // Um can we have the name & version number please?
					    throw new RuntimeException("No parsers available to parse that GetCapabilities document");
					}
					currentResponse = new GetCapabilitiesResponse(parser, contentType, inputStream);
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
        
        Layer[] layers = capabilities.getLayers();
        for (int i = 0; i < layers.length; i++) {
        	if (layers[i].getName() != null && layers[i].getName().length() != 0) {
        		namedLayers.add(layers[i]);
        	}
        }
        
        return namedLayers;
    }

	public Exception getProblem() {
		return problem;
	}

	public GetMapRequest createGetMapRequest() {
		
		if (capabilities == null) {
			throw new RuntimeException("Unable to create a GetMapRequest when the GetCapabilities document has not been retrieved");
		}
		
		GetMapRequest request = 
			new GetMapRequest(getCapabilities().getRequest().getGetMap().getGet(),
							  getCapabilities().getVersion(),
							  Utils.findDrawableLayers(getCapabilities().getLayers()),
							  getSRSs(),
							  getCapabilities().getRequest().getGetMap().getFormats(),
							  getExceptions()
							  );
		
		return request;
	}
	
	public GetFeatureInfoRequest createGetFeatureInfoRequest(GetMapRequest getMapRequest) {
		if (capabilities == null) {
			throw new RuntimeException("Unable to create a GetFeatureInfoRequest without a GetCapabilities document");
		}
		
		if (getCapabilities().getRequest().getGetFeatureInfo() == null) {
			throw new UnsupportedOperationException("This Web Map Server does not support GetFeatureInfo requests");
		}
		
		GetFeatureInfoRequest request = 
			new GetFeatureInfoRequest(
					getCapabilities().getRequest().getGetFeatureInfo().getGet(),
					getMapRequest,
					getQueryableLayers(),
					getCapabilities().getRequest().getGetFeatureInfo().getFormats()
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
		//TODO hack - fix this later.
		return null;
		//return getCapabilities().getCapability().getException().getFormats();
	}

	private Set getSRSs() {
		Set srss = new TreeSet();
		
		Layer[] layers = getCapabilities().getLayers();
		for (int i = 0; i < layers.length; i++) {
			srss.addAll(layers[i].getSrs());
			
			Layer parentLayer = layers[i].getParent();
			while (parentLayer != null) {
				srss.addAll(parentLayer.getSrs());
				parentLayer = parentLayer.getParent();
			}
		}
		
		return srss;
	}
}
