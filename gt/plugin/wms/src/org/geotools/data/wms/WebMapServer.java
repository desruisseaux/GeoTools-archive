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
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.geotools.data.ows.WMSCapabilities;
import org.geotools.data.ows.Layer;
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
 * are saved except the following, which are over-rided.
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
	private WMSCapabilities capabilities;
	private Exception problem;
	
	//private WMSParser[] parsers;
	
	public static final int IN_PROGRESS = 1;
	public static final int NOTCONNECTED = 0;
	public static final int ERROR = -1;
	public static final int CONNECTED = 2;
	
	private AbstractRequest currentRequest;
	/** Feedback: Why only one? */
	private Thread requestRetriever;
	private AbstractResponse currentResponse;
	private Specification[] specs;
	private Specification specification;
	
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
		
		specs = new Specification[2];
		specs[0] = new WMS1_0_0();
		specs[1] = new WMS1_1_1();
		
		if (wait) {
			return;
		}
		GetCapabilitiesRequest request = negotiateVersion( serverURL );
		if (getProblem() != null) {
			return;
		}
		issueRequest( request, !wait);
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
	 * <p>
	 * Example 1:<br>
	 * Server understands versions 1, 2, 4, 5 and 8.<br>
	 * Client understands versions 1,3, 4, 6, and 7.<br>
	 * <ol>
	 * <li>Client requests version 7. Server responds with version 5.
	 * <li>Client requests version 4. Server responds with version 4
	 * <li>Client does understands, negotiation ends successfully.
	 * </ol>
	 * </p>
	 * <p>
	 * Example 2:<br>
	 * Server understands versions 4, 5 and 8.<br>
	 * Client understands version 3.<br>
	 * <ol>
	 * <li>Client requests version 3. Server responds with version 4.
	 * <li>Client does not understand that version or any higher version, so negotiation fails
	 * </ol>
	 * @return GetCapabilitiesRequest suitable for use with a parser
	 */
	private GetCapabilitiesRequest negotiateVersion( URL server ){
	    List specs = specifications();
	    List versions = new ArrayList( specs.size() );
	    for( Iterator i=specs.iterator(); i.hasNext(); ){
	        Specification specification = (Specification) i.next();
	        versions.add( specification.getVersion() );
	    }
	    int minClient = 0;
	    int maxClient = specs.size()-1;
	    
	    int test = maxClient;	    	   
	    while( minClient <= test && test <= maxClient ){
	        Specification specification = (Specification) specs.get( test );
	        String clientVersion = specification.getVersion();
	        
	        GetCapabilitiesRequest request = specification.createRequest( server );
	        String serverVersion = queryVersion( request );
	        
	        if (getProblem() != null) {
	        	/*
	        	 * There was an error accessing the server.
	        	 * 
	        	 * Not sure if there is any way at all to recover from this,
	        	 * as the WMS specification states that if a request is made
	        	 * for a higher or lower version, it should return a valid getCaps,
	        	 * but in this instance, it hasn't. 
	        	 * 
	        	 */
        		return null;
	        }
	        
	        int compare = serverVersion.compareTo( clientVersion );
	        if( compare == 0 ){
	        	this.specification = specification;
	            return request; // we have an exact match
	        }
	        if( versions.contains( serverVersion )){
               // we can communicate with this server
	           // 
	           test = versions.indexOf( serverVersion );	           
	        }
	        else if( compare < 0 ){ 
	           // server responded lower then we asked - and we don't understand.	           
	           maxClient = test-1; // set current version as limit
	           
	           // lets try and go one lower?
	           //	           
	           clientVersion = before( versions, serverVersion );
	           if( clientVersion == null ){
	               return null; // do not know any lower version numbers
	           }
	           test = versions.indexOf( clientVersion );      	            
	        }
	        else {
               // server responsed higher than we asked - and we don't understand
	           minClient = test+1; // set current version as lower limit
	           
	           // lets try and go one higher
	           clientVersion = after( versions, serverVersion );
	           if( clientVersion == null ){
	               return null; // do not know any higher version numbers
	           }
	           test = versions.indexOf( clientVersion );
	        }	        
	    }	    
	    // could not talk to this server
	    return null;
	}
	
	/**
	 * Utility method returning the known version,
	 * just before the provided version
	 */
	String before( List known, String version ){
	    if( known.isEmpty() ) {
	        return null;
	    }
	    String before = null;	    
	    for( Iterator i=known.iterator(); i.hasNext(); ){
            String test = (String) i.next();
            if( test.compareTo( version ) < 0 ){
                if( before == null || before.compareTo( test ) > 0 ){
                    before = test;
                }
            }
        }
	    return before;
	}
	/**
	 * Utility method returning the known version,
	 * just after the provided version
	 */
	String after( List known, String version ){
	    if( known.isEmpty() ) {
	        return null;
	    }
	    String after = null;	    
	    for( Iterator i=known.iterator(); i.hasNext(); ){
            String test = (String) i.next();
            if( test.compareTo( version ) > 0 ){
                if( after == null || after.compareTo( test ) < 0 ){
                    after = test;
                }
            }
        }
	    return after;
	}
	    
	/**
	 * Map of known specifications.
	 * <p>
	 * We could do the plug-in thing here to add specifications at a later date.
	 * @return Sorted Map of Specifications by version number.
	 */
	private List specifications(){
	    List specs = new ArrayList( 2 );	    
	    specs.add( new WMS1_0_0() );
	    specs.add( new WMS1_1_1() );
	    return specs;
	}
	
	private String queryVersion( GetCapabilitiesRequest request ) {
	    URL url = request.getFinalURL();
	    Document document;
		try {
		    SAXBuilder builder = new SAXBuilder();
		    URLConnection connection = url.openConnection();
		    String mimeType = connection.getContentType();
		    // should be:
		    // - application/vnd.ogc.wms_xml (Great!)
            // - application/xml
            // - text/xml		    
		    document = builder.build( connection.getInputStream() );
        } catch (JDOMException badXML) {
        	problem = badXML;
            return null;
        } catch (IOException badIO) {
        	problem = badIO;
            return null;
        }
        Element element = document.getRootElement(); //Root = 		
	    String version;
	    return element.getAttributeValue("version");	    
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
	public WMSCapabilities getCapabilities() {
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
			GetCapabilitiesRequest request = negotiateVersion(serverURL);
			if (getProblem() != null) {
				return null;
			}
			issueRequest(request, false);
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
			}, "WebMapServer Request Thread");
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
			        
					WMSParser parser = specification.createParser(document);
					
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
							  getCapabilities().getRequest().getGetMap().getFormatStrings(),
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
					getCapabilities().getRequest().getGetFeatureInfo().getFormatStrings()
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
			if (layers[i].getSrs() != null) {
				srss.addAll(layers[i].getSrs());
			}
		}
		
		return srss;
	}
}
