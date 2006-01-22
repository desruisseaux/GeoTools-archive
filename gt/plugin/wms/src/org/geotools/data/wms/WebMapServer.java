/*
 * Geotools2 - OpenSource mapping toolkit http://geotools.org (C) 2002, Geotools Project Managment Committee (PMC) This
 * library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; version 2.1 of the License. This library is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 */
package org.geotools.data.wms;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.geotools.data.ows.CRSEnvelope;
import org.geotools.data.ows.Layer;
import org.geotools.data.ows.WMSCapabilities;
import org.geotools.data.wms.request.AbstractGetCapabilitiesRequest;
import org.geotools.data.wms.request.DescribeLayerRequest;
import org.geotools.data.wms.request.GetCapabilitiesRequest;
import org.geotools.data.wms.request.GetFeatureInfoRequest;
import org.geotools.data.wms.request.GetLegendGraphicRequest;
import org.geotools.data.wms.request.GetMapRequest;
import org.geotools.data.wms.request.GetStylesRequest;
import org.geotools.data.wms.request.PutStylesRequest;
import org.geotools.data.wms.request.Request;
import org.geotools.data.wms.response.AbstractResponse;
import org.geotools.data.wms.response.DescribeLayerResponse;
import org.geotools.data.wms.response.GetCapabilitiesResponse;
import org.geotools.data.wms.response.GetFeatureInfoResponse;
import org.geotools.data.wms.response.GetLegendGraphicResponse;
import org.geotools.data.wms.response.GetMapResponse;
import org.geotools.data.wms.response.GetStylesResponse;
import org.geotools.data.wms.response.PutStylesResponse;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.ows.ServiceException;
import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.opengis.spatialschema.geometry.DirectPosition;
import org.opengis.spatialschema.geometry.MismatchedDimensionException;
import org.xml.sax.SAXException;

/**
 * WebMapServer is a class representing a WMS. It is used to access the 
 * Capabilities document and perform requests. It supports multiple versions
 * and will perform version negotiation automatically and use the highest
 * known version that the server can communicate.
 * 
 * If restriction of versions to be used is desired, this class should be
 * subclassed and it's setupSpecifications() method over-ridden. It should
 * add which version/specifications are to be used to the specs array. See
 * the current implementation for an example.
 * 
 * Example usage:
 * <code><pre>
 * WebMapServer wms = new WebMapServer("http://some.example.com/wms");
 * WMSCapabilities capabilities = wms.getCapabilities();
 * GetMapRequest request = wms.getMapRequest();
 * 
 * ... //configure request
 * 
 * GetMapResponse response = (GetMapResponse) wms.issueRequest(request);
 * 
 * ... //extract image from the response
 * </pre></code>
 * 
 * @author Richard Gould, Refractions Research
 * @source $URL$
 */
public class WebMapServer {
    private final URL serverURL;
    private WMSCapabilities capabilities;

    /** Contains the specifications that are to be used with this WMS */
    protected Specification[] specs;
    private Specification specification;

    /**
     * Creates a new WebMapServer instance and attempts to retrieve the 
     * Capabilities document specified by serverURL. 
     * 
     * @param serverURL a URL that points to the capabilities document of a server
     * @throws IOException if there is an error communicating with the server
     * @throws ServiceException if the server responds with an error
     * @throws SAXException if there is an error while parsing the capabilities, such as bad XML
     */
    public WebMapServer( final URL serverURL ) throws IOException, ServiceException, SAXException {
        this.serverURL = serverURL;

        setupSpecifications();
        
        capabilities = negotiateVersion();
        if (capabilities == null) {
        	throw new ServiceException("Unable to parse capabilities.");
        }
    }

    /**
     * Sets up the specifications/versions that this server is capable of
     * communicating with.
     */
    protected void setupSpecifications() {
        specs = new Specification[4];
        specs[0] = new WMS1_0_0();
        specs[1] = new WMS1_1_0();
        specs[2] = new WMS1_1_1();
        specs[3] = new WMS1_3_0();
    }

    /**
     * <p>
     * Version number negotiation occurs as follows (credit OGC):
     * <ul>
     * <li><b>1) </b> If the server implements the requested version number, the server shall send that version.</li>
     * <li><b>2a) </b> If a version unknown to the server is requested, the server shall send the highest version less
     * than the requested version.</li>
     * <li><b>2b) </b> If the client request is for a version lower than any of those known to the server, then the
     * server shall send the lowest version it knows.</li>
     * <li><b>3a) </b> If the client does not understand the new version number sent by the server, it may either cease
     * communicating with the server or send a new request with a new version number that the client does understand but
     * which is less than that sent by the server (if the server had responded with a lower version).</li>
     * <li><b>3b) </b> If the server had responded with a higher version (because the request was for a version lower
     * than any known to the server), and the client does not understand the proposed higher version, then the client
     * may send a new request with a version number higher than that sent by the server.</li>
     * </ul>
     * </p>
     * <p>
     * The OGC tells us to repeat this process (or give up). This means we are 
     * actually going to come up with a bit of setup cost in figuring out our 
     * GetCapabilities request. This means that it is possible that we may make
     * multiple requests before being satisfied with a response. 
     * 
     * Also, if we are unable to parse a given version for some reason, 
     * for example, malformed XML, we will request a lower version until
     * we have run out of versions to request with. Thus, a server that does
     * not play nicely may take some time to parse and might not even 
     * succeed.
     * 
     * @return a capabilities object that represents the Capabilities on the server
     * @throws IOException if there is an error communicating with the server, or the XML cannot be parsed
     * @throws ServiceException if the server returns a ServiceException
     * @throws SAXException if there is an error while parsing the capabilities
     */
    private WMSCapabilities negotiateVersion() throws IOException, ServiceException, SAXException {
        List versions = new ArrayList(specs.length);
        Exception exception = null;

        for( int i = 0; i < specs.length; i++ ) {
            versions.add(i, specs[i].getVersion());
        }

        int minClient = 0;
        int maxClient = specs.length - 1;

        int test = maxClient;

        while( (minClient <= test) && (test <= maxClient) ) {
            Specification tempSpecification = specs[test];
            String clientVersion = tempSpecification.getVersion();

            AbstractGetCapabilitiesRequest request = tempSpecification.createGetCapabilitiesRequest(serverURL);

            //Grab document
            WMSCapabilities tempCapabilities;
            try {
                tempCapabilities = issueRequest(request).getCapabilities();
            } catch (ServiceException e) {
            	tempCapabilities = null;
            	exception = e;
            } catch (SAXException e) {
                tempCapabilities = null;
                exception = e;
                e.printStackTrace();
            }

            int compare = -1;
            String serverVersion = clientVersion; //Ignored if caps is null

            if (tempCapabilities != null) {

                serverVersion = tempCapabilities.getVersion();

                compare = serverVersion.compareTo(clientVersion);
            }

            if (compare == 0) {
                //we have an exact match and have capabilities as well!
                this.specification = tempSpecification;

                return tempCapabilities;
            }

            if (tempCapabilities != null && versions.contains(serverVersion)) {
                // we can communicate with this server
                int index = versions.indexOf(serverVersion);
                this.specification = specs[index];

                return tempCapabilities;

            } else if (compare < 0) {
                // server responded lower then we asked - and we don't understand.
                maxClient = test - 1; // set current version as limit

                // lets try and go one lower?
                //	           
                clientVersion = before(versions, serverVersion);

                if (clientVersion == null) {
                    if (exception != null) {
                    	if (exception instanceof ServiceException) {
                    		throw (ServiceException) exception;
                    	} else if (exception instanceof SAXException) {
                    		throw (SAXException) exception;
                    	}
                        IOException e = new IOException(exception.getMessage());
                        throw e;
                    }
                    return null; // do not know any lower version numbers
                }

                test = versions.indexOf(clientVersion);
            } else {
                // server responsed higher than we asked - and we don't understand
                minClient = test + 1; // set current version as lower limit

                // lets try and go one higher
                clientVersion = after(versions, serverVersion);

                if (clientVersion == null) {
                    if (exception != null) {
                    	if (exception instanceof ServiceException) {
                    		throw (ServiceException) exception;
                    	}
                        IOException e = new IOException(exception.getMessage());
                        throw e;
                    }
                    return null; // do not know any lower version numbers
                }

                test = versions.indexOf(clientVersion);
            }
        }

        // could not talk to this server
        if (exception != null) {
            IOException e = new IOException(exception.getMessage());
            throw e;
        }
        return null;
    }

    /**
     * Utility method returning the known version, just before the provided version
     * 
     * @param known List<String> of all known versions
     * @param version the boundary condition
     * @return the version just below the provided boundary version
     */
    String before( List known, String version ) {
        if (known.isEmpty()) {
            return null;
        }

        String before = null;

        for( Iterator i = known.iterator(); i.hasNext(); ) {
            String test = (String) i.next();

            if (test.compareTo(version) < 0) {

                if ((before == null) || (before.compareTo(test) < 0)) {
                    before = test;
                }
            }
        }

        return before;
    }

    /**
     * Utility method returning the known version, just after the provided version
     * 
     * @param known a List<String> of all known versions
     * @param version the boundary condition
     * @return a version just after the provided boundary condition
     */
    String after( List known, String version ) {
        if (known.isEmpty()) {
            return null;
        }

        String after = null;

        for( Iterator i = known.iterator(); i.hasNext(); ) {
            String test = (String) i.next();

            if (test.compareTo(version) > 0) {
                if ((after == null) || (after.compareTo(test) < 0)) {
                    after = test;
                }
            }
        }

        return after;
    }

    /**
     * Get the getCapabilities document. If there was an error parsing it
     * during creation, it will return null (and it should have thrown an
     * exception during creation).
     * 
     * @return a WMSCapabilities object, representing the Capabilities of the server
     */
    public WMSCapabilities getCapabilities() {
        return capabilities;
    }

    /**
     * Issues a request to the server and returns that server's response. 
     * 
     * @param request the request to be issued
     * @return a response from the server, of type GetMapResponse or GetFeatureInfoResponse
     * @throws IOException
     * @throws SAXException
     */
    private static AbstractResponse internalIssueRequest( Request request ) throws IOException, ServiceException, SAXException {
        URL finalURL = request.getFinalURL();

        URLConnection connection = finalURL.openConnection();
        
        connection.addRequestProperty("Accept-Encoding", "gzip");

        InputStream inputStream = connection.getInputStream();
        
        if (connection.getContentEncoding() != null && connection.getContentEncoding().indexOf("gzip") != -1) { //$NON-NLS-1$
            inputStream = new GZIPInputStream(inputStream);
        }

        String contentType = connection.getContentType();
        
        if (request instanceof GetCapabilitiesRequest) {
        	return new GetCapabilitiesResponse(contentType, inputStream);
        } else if (request instanceof GetFeatureInfoRequest) {
            return new GetFeatureInfoResponse(contentType, inputStream);
        } else if (request instanceof GetMapRequest) {
            return new GetMapResponse(contentType, inputStream);
        } else if (request instanceof DescribeLayerRequest) {
            return new DescribeLayerResponse(contentType, inputStream);
        } else if (request instanceof GetLegendGraphicRequest) {
            return new GetLegendGraphicResponse(contentType, inputStream);
        } else if (request instanceof GetStylesRequest) {
            return new GetStylesResponse(contentType, inputStream);
        } else if (request instanceof PutStylesRequest) {
            return new PutStylesResponse(contentType, inputStream);
        } else {
            throw new RuntimeException("Request is an invalid type. I do not know it.");
        }
    }
    
    public GetCapabilitiesResponse issueRequest(GetCapabilitiesRequest request) throws IOException, ServiceException, SAXException {
    	return (GetCapabilitiesResponse) internalIssueRequest(request);
    }
    
    public GetMapResponse issueRequest(GetMapRequest request) throws IOException, ServiceException, SAXException {
        return (GetMapResponse) internalIssueRequest(request);
    }
    
    public GetFeatureInfoResponse issueRequest(GetFeatureInfoRequest request) throws IOException, ServiceException, SAXException {
        return (GetFeatureInfoResponse) internalIssueRequest(request);
    }
    
    public DescribeLayerResponse issueRequest(DescribeLayerRequest request) throws IOException, ServiceException, SAXException {
        return (DescribeLayerResponse) internalIssueRequest(request);
    }
    
    public GetLegendGraphicResponse issueRequest(GetLegendGraphicRequest request) throws IOException, ServiceException, SAXException {
        return (GetLegendGraphicResponse) internalIssueRequest(request);
    }
    
    public GetStylesResponse issueRequest(GetStylesRequest request) throws IOException, ServiceException, SAXException {
        return (GetStylesResponse) internalIssueRequest(request);
    }
    
    public PutStylesResponse issueRequest(PutStylesRequest request) throws IOException, ServiceException, SAXException {
        return (PutStylesResponse) internalIssueRequest(request);
    }

    /**
     * Creates a GetMapRequest that can be configured and then passed to 
     * issueRequest(). 
     * 
     * @return a configureable GetMapRequest object
     */
    public GetMapRequest createGetMapRequest() {
        URL onlineResource = onlineResource = getCapabilities().getRequest().getGetMap().getGet();
        if (onlineResource == null) { 
            onlineResource = serverURL;
        }

        GetMapRequest request = specification.createGetMapRequest(onlineResource);

        return request;
    }

    /**
     * Creates a GetFeatureInfoRequest that can be configured and then passed to
     * issueRequest(). 
     * 
     * @param getMapRequest a previous configured GetMapRequest
     * @return a GetFeatureInfoRequest
     * @throws UnsupportedOperationException if the server does not support GetFeatureInfo
     */
    public GetFeatureInfoRequest createGetFeatureInfoRequest( GetMapRequest getMapRequest ) {
        if (getCapabilities().getRequest().getGetFeatureInfo() == null) {
            throw new UnsupportedOperationException("This Web Map Server does not support GetFeatureInfo requests");
        }

        URL onlineResource = getCapabilities().getRequest().getGetFeatureInfo().getGet();
        if (onlineResource == null) {
            onlineResource = serverURL;
        }
        
        GetFeatureInfoRequest request = specification.createGetFeatureInfoRequest(onlineResource,
                getMapRequest);

        return request;
    }
    
    public DescribeLayerRequest createDescribeLayerRequest() throws UnsupportedOperationException {
        if (getCapabilities().getRequest().getDescribeLayer() == null ) {
            throw new UnsupportedOperationException("Server does not specify a DescribeLayer operation. Cannot be performed");
        }
        
        URL onlineResource = getCapabilities().getRequest().getDescribeLayer().getGet();
        if (onlineResource == null) {
            onlineResource = serverURL;
        }
        
        DescribeLayerRequest request = specification.createDescribeLayerRequest(onlineResource);
        
        return request;
    }
    
    public GetLegendGraphicRequest createGetLegendGraphicRequest() throws UnsupportedOperationException {
        if (getCapabilities().getRequest().getGetLegendGraphic() == null) {
            throw new UnsupportedOperationException("Server does not specify a GetLegendGraphic operation. Cannot be performed");
        }
        
        URL onlineResource = getCapabilities().getRequest().getGetLegendGraphic().getGet();
        if (onlineResource == null) {
            onlineResource = serverURL;
        }
        
        GetLegendGraphicRequest request = specification.createGetLegendGraphicRequest(onlineResource);
        
        return request;        
    }
    
    public GetStylesRequest createGetStylesRequest() throws UnsupportedOperationException{
        if (getCapabilities().getRequest().getGetStyles() == null) {
            throw new UnsupportedOperationException("Server does not specify a GetStyles operation. Cannot be performed");
        }
        
        URL onlineResource = getCapabilities().getRequest().getGetStyles().getGet();
        if (onlineResource == null) {
            onlineResource = serverURL;
        }
        
        GetStylesRequest request = specification.createGetStylesRequest(onlineResource);
       
        return request;
    }
    
    public PutStylesRequest createPutStylesRequest() throws UnsupportedOperationException {
        if (getCapabilities().getRequest().getPutStyles() == null) {
            throw new UnsupportedOperationException("Server does not specify a PutStyles operation. Cannot be performed");
        }
        
        URL onlineResource = getCapabilities().getRequest().getPutStyles().getGet();
        if (onlineResource == null) {
            onlineResource = serverURL;
        }
        
        PutStylesRequest request = specification.createPutStylesRequest(onlineResource);
        return request;
    }
    
    /**
     * Given a layer and a coordinate reference system, will locate an envelope
     * for that layer in that CRS. If the layer is declared to support that CRS,
     * but no envelope can be found, it will try to calculate an appropriate 
     * bounding box.
     * 
     * If null is returned, no valid bounding box could be found and one couldn't
     * be transformed from another.
     * 
     * @param layer
     * @param crs
     * @return an Envelope containing a valid bounding box, or null if none are found
     */
    public GeneralEnvelope getEnvelope(Layer layer, CoordinateReferenceSystem crs) {
        
        for (final Iterator i=crs.getIdentifiers().iterator(); i.hasNext();) {
            String epsgCode = i.next().toString();

            CRSEnvelope tempBBox = null;
            Layer parentLayer = layer;

            //Locate a BBOx if we can
            while( tempBBox == null && parentLayer != null ) {
                tempBBox = (CRSEnvelope) parentLayer.getBoundingBoxes().get(epsgCode);
                
                parentLayer = parentLayer.getParent();
            }
    
            //Otherwise, locate a LatLon BBOX
    
            if (tempBBox == null && ("EPSG:4326".equals(epsgCode.toUpperCase()))) { //$NON-NLS-1$
                CRSEnvelope latLonBBox = null;
    
                parentLayer = layer;
                while (latLonBBox == null && parentLayer != null) {
                    latLonBBox = parentLayer.getLatLonBoundingBox();
                    if (latLonBBox != null) {
                        try {
                            new GeneralEnvelope(new double[] {latLonBBox.getMinX(), latLonBBox.getMinY()}, 
                                    new double[] { latLonBBox.getMaxX(), latLonBBox.getMaxY() });
                            break;
                        } catch (IllegalArgumentException e) {
                            //TODO LOG here
                            //log("Layer "+layer.getName()+" has invalid bbox declared: "+tempBbox.toString());
                            latLonBBox = null;
                        }
                    }
                    parentLayer = parentLayer.getParent();
                }
                
                if (latLonBBox == null) {
                    //TODO could convert another bbox to latlon?
                    tempBBox = new CRSEnvelope("EPSG:4326", -180, -90, 180, 90);
                }
                
                tempBBox = new CRSEnvelope("EPSG:4326", latLonBBox.getMinX(), latLonBBox.getMinY(), latLonBBox.getMaxX(), latLonBBox.getMaxY());
            }
            
            if (tempBBox == null) {
                //Haven't found a bbox in the requested CRS. Attempt to transform another bbox
                
                String epsg = null;
                if (layer.getLatLonBoundingBox() != null) {
                    CRSEnvelope latLonBBox = layer.getLatLonBoundingBox();
                    tempBBox = new CRSEnvelope("EPSG:4326", latLonBBox.getMinX(), latLonBBox.getMinY(), latLonBBox.getMaxX(), latLonBBox.getMaxY());
                    epsg = "EPSG:4326";
                }
                
                if (tempBBox == null && layer.getBoundingBoxes() != null && layer.getBoundingBoxes().size() > 0) {
                    tempBBox = (CRSEnvelope) layer.getBoundingBoxes().values().iterator().next();
                    epsg = tempBBox.getEPSGCode();
                }
                
                if (tempBBox == null) {
                    continue;
                }
                
                GeneralEnvelope env = new GeneralEnvelope(new double[] { tempBBox.getMinX(), tempBBox.getMinY()}, 
                        new double[] { tempBBox.getMaxX(), tempBBox.getMaxY() });
                
                CoordinateReferenceSystem fromCRS = null;
                try {
                    fromCRS = CRS.decode(epsg);
                    MathTransform transform = CRS.transform(fromCRS, crs, true);
                    
                    DirectPosition newLower = transform.transform(env.getLowerCorner(),null);
                    DirectPosition newUpper = transform.transform(env.getUpperCorner(),null);
                    
                    env = new GeneralEnvelope(newLower.getCoordinates(), newUpper.getCoordinates());
                    env.setCoordinateReferenceSystem(fromCRS);
                    
                    //success!!
                    
                    return env;
                    
                } catch (NoSuchAuthorityCodeException e) {
                    // TODO Catch e
                } catch (FactoryException e) {
                    // TODO Catch e
                } catch (MismatchedDimensionException e) {
                    // TODO Catch e
                } catch (TransformException e) {
                    // TODO Catch e
                }
            }
            
            //TODO Attempt to figure out the valid area of teh CRS and use that.
            
            if (tempBBox != null) {
                GeneralEnvelope env = new GeneralEnvelope(new double[] { tempBBox.getMinX(), tempBBox.getMinY()}, 
                        new double[] { tempBBox.getMaxX(), tempBBox.getMaxY() });
                env.setCoordinateReferenceSystem(crs);
                return env;
            }
    
        }
        return null;
    }
}
