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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.zip.GZIPInputStream;

import org.geotools.catalog.CatalogEntry;
import org.geotools.catalog.Discovery;
import org.geotools.catalog.QueryRequest;
import org.geotools.data.ows.BoundingBox;
import org.geotools.data.ows.LatLonBoundingBox;
import org.geotools.data.ows.Layer;
import org.geotools.data.ows.WMSCapabilities;
import org.geotools.data.wms.request.AbstractGetCapabilitiesRequest;
import org.geotools.data.wms.request.DescribeLayerRequest;
import org.geotools.data.wms.request.GetFeatureInfoRequest;
import org.geotools.data.wms.request.GetLegendGraphicRequest;
import org.geotools.data.wms.request.GetMapRequest;
import org.geotools.data.wms.request.GetStylesRequest;
import org.geotools.data.wms.request.PutStylesRequest;
import org.geotools.data.wms.request.Request;
import org.geotools.data.wms.response.AbstractResponse;
import org.geotools.data.wms.response.DescribeLayerResponse;
import org.geotools.data.wms.response.GetFeatureInfoResponse;
import org.geotools.data.wms.response.GetLegendGraphicResponse;
import org.geotools.data.wms.response.GetMapResponse;
import org.geotools.data.wms.response.PutStylesResponse;
import org.geotools.data.wms.xml.WMSSchema;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.xml.DocumentFactory;
import org.geotools.xml.handlers.DocumentHandler;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.spatialschema.geometry.Envelope;
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
 */
public class WebMapServer implements Discovery {
    private final URL serverURL;
    private WMSCapabilities capabilities;

    private Request currentRequest;
    private AbstractResponse currentResponse;

    protected Specification[] specs;
    private Specification specification;

    //Flag used to determine if we have checked the capabilities already,
    //So we don't check multiple times
    private boolean alreadyChecked;

    /**
     * Creates a new WebMapServer instance and attempts to retrieve the 
     * Capabilities document specified by serverURL. 
     * 
     * @param serverURL a URL that points to the capabilities document of a server
     * @throws IOException if there is an error communicating with the server
     */
    public WebMapServer( final URL serverURL ) throws IOException {
        this.serverURL = serverURL;

        this.alreadyChecked = false;

        setupSpecifications();

        getCapabilities();
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
     */
    private WMSCapabilities negotiateVersion() throws IOException {
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
            URL url = request.getFinalURL();
//                        System.out.println("URL: "+url.toExternalForm());
            WMSCapabilities tempCapabilities;
            try {
                tempCapabilities = parseCapabilities(url);
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
                // 
//                                System.out.println("Server responded with "+serverVersion+". We know it and will use it now.");

                int index = versions.indexOf(serverVersion);
                this.specification = specs[index];

                return tempCapabilities;

            } else if (compare < 0) {
                if (tempCapabilities == null) {
//                                        System.out.println("Unable to read from server at version:"+serverVersion+".");
                }
//                                System.out.println("Downgrading version.");
                // server responded lower then we asked - and we don't understand.
                maxClient = test - 1; // set current version as limit

                // lets try and go one lower?
                //	           
                clientVersion = before(versions, serverVersion);

                if (clientVersion == null) {
                    if (exception != null) {
                        IOException e = new IOException(exception.getMessage());
                        throw e;
                    }
                    return null; // do not know any lower version numbers
                }

                test = versions.indexOf(clientVersion);
            } else {
//                                System.out.println("Server responded with "+serverVersion+" after a request for "+clientVersion);
//                                System.out.println("Upgrading");
                // server responsed higher than we asked - and we don't understand
                minClient = test + 1; // set current version as lower limit

                // lets try and go one higher
                clientVersion = after(versions, serverVersion);

                if (clientVersion == null) {
                    if (exception != null) {
                        IOException e = new IOException(exception.getMessage());
                        throw e;
                    }
                    return null; // do not know any higher version numbers
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

    private static WMSCapabilities parseCapabilities( URL url ) throws SAXException, IOException {
        System.out.println(url);
        Map hints = new HashMap();
        hints.put(DocumentHandler.DEFAULT_NAMESPACE_HINT_KEY, WMSSchema.getInstance());
        hints.put(DocumentFactory.VALIDATION_HINT, Boolean.FALSE);

        URLConnection urlConnection = url.openConnection();
        //        urlConnection.setRequestProperty("accept", "application/vnd.ogc.wms+xml, text/xml, *; q=.2, */*; q=.2");
        //        urlConnection.setRequestProperty("accept-encoding", "compress; q=1.0, gzip; q=0, *");

        InputStream io = urlConnection.getInputStream();
        
        if (urlConnection.getContentEncoding() != null && urlConnection.getContentEncoding().indexOf("gzip") != -1) {
            io = new GZIPInputStream(io);
        }
        
        Object object = DocumentFactory.getInstance(io, hints, Level.WARNING);

        WMSCapabilities capabilities = (WMSCapabilities) object;
        return capabilities;
    }

    /**
     * Get the getCapabilities document. If it is not already retrieved, then 
     * it shall be retrieved.
     * 
     * @return a WMSCapabilities object, representing the Capabilities of the server
     * @throws IOException if there is an error retrieving the capabilities
     */
    public WMSCapabilities getCapabilities() throws IOException {
        if (capabilities == null && !alreadyChecked) {
            capabilities = negotiateVersion();
            alreadyChecked = true;
        }

        return capabilities;
    }

    /**
     * Issues a request to the server and returns that server's response. 
     * As of 12 Nov 2004, it only supports GetMap and GetFeatureInfo.
     * 
     * It is not capable of issuing a GetCapabilities request. If this is
     * desired, it is recommend that you subclass and over-ride
     * getCapabilities. 
     * 
     * @param request the request to be issued
     * @return a response from the server, of type GetMapResponse or GetFeatureInfoResponse
     * @throws IOException
     * @throws SAXException
     */
    public AbstractResponse issueRequest( Request request ) throws IOException, SAXException {
        this.currentRequest = request;

        issueRequest();

        return currentResponse;
    }

    private void issueRequest() throws IOException, SAXException {
        URL finalURL = currentRequest.getFinalURL();

        URLConnection connection = finalURL.openConnection();
        InputStream inputStream = connection.getInputStream();
        
        if (connection.getContentEncoding() != null && connection.getContentEncoding().indexOf("gzip") != -1) {
            inputStream = new GZIPInputStream(inputStream);
        }


        String contentType = connection.getContentType();

        if (currentRequest instanceof GetFeatureInfoRequest) {
            currentResponse = new GetFeatureInfoResponse(contentType, inputStream);
        } else if (currentRequest instanceof GetMapRequest) {
            currentResponse = new GetMapResponse(contentType, inputStream);
        } else if (currentRequest instanceof DescribeLayerRequest) {
            currentResponse = new DescribeLayerResponse(contentType, inputStream);
        } else if (currentRequest instanceof GetLegendGraphicRequest) {
            currentResponse = new GetLegendGraphicResponse(contentType, inputStream);
        } else if (currentRequest instanceof PutStylesRequest) {
            currentResponse = new PutStylesResponse(contentType, inputStream);
        } else {
            throw new RuntimeException("Request is an invalid type. I do not know it.");
        }
    }

    /**
     * Creates a GetMapRequest that can be configured and then passed to 
     * issueRequest(). It is created with the data retrieved from the
     * capabilities document.
     * 
     * @return a configureable GetMapRequest object
     * @throws IOException if there is an error while attempting to read the capabilities document
     */
    public GetMapRequest createGetMapRequest() throws IOException {
        if (capabilities == null) {
            getCapabilities();

            if (capabilities == null) {
                throw new RuntimeException(
                        "Unable to create a GetMapRequest when the GetCapabilities document is null.");
            }
        }
        
        URL onlineResource = onlineResource = getCapabilities().getRequest().getGetMap().getGet();
        if (onlineResource == null) { 
            onlineResource = serverURL;
        }

        GetMapRequest request = specification.createGetMapRequest(onlineResource,
                Utils.findDrawableLayers(getCapabilities().getLayers()), getSRSs(), getCapabilities().getRequest()
                        .getGetMap().getFormatStrings(), getExceptions());

        return request;
    }

    /**
     * Creates a GetFeatureInfoRequest that can be configured and then passed to
     * issueRequest(). It is created using the data from a previously configured
     * GetMapRequest.
     * 
     * @param getMapRequest a previous configured GetMapRequest
     * @return a GetFeatureInfoRequest
     * @throws IOException if there is an reading the capabilities file
     * @throws UnsupportedOperationException if the server does not support GetFeatureInfo
     */
    public GetFeatureInfoRequest createGetFeatureInfoRequest( GetMapRequest getMapRequest ) throws IOException {
        if (capabilities == null) {
            throw new RuntimeException("Unable to create a GetFeatureInfoRequest without a GetCapabilities document");
        }

        if (getCapabilities().getRequest().getGetFeatureInfo() == null) {
            throw new UnsupportedOperationException("This Web Map Server does not support GetFeatureInfo requests");
        }

        URL onlineResource = getCapabilities().getRequest().getGetFeatureInfo().getGet();
        if (onlineResource == null) {
            onlineResource = serverURL;
        }
        
        GetFeatureInfoRequest request = specification.createGetFeatureInfoRequest(onlineResource,
                getMapRequest, getQueryableLayers(), getCapabilities().getRequest()
                .getGetFeatureInfo().getFormatStrings());

        return request;
    }
    
    public DescribeLayerRequest createDescribeLayerRequest() throws UnsupportedOperationException, IOException {
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
    
    public GetLegendGraphicRequest createGetLegendGraphicRequest() throws UnsupportedOperationException, IOException {
        if (getCapabilities().getRequest().getGetLegendGraphic() == null) {
            throw new UnsupportedOperationException("Server does not specify a GetLegendGraphic operation. Cannot be performed");
        }
        
        URL onlineResource = getCapabilities().getRequest().getGetLegendGraphic().getGet();
        if (onlineResource == null) {
            onlineResource = serverURL;
        }
        
        GetLegendGraphicRequest request = specification.createGetLegendGraphicRequest(onlineResource, 
                Utils.findDrawableLayers(getCapabilities().getLayers()),
                getCapabilities().getRequest().getGetLegendGraphic().getFormatStrings(), 
                null);
        
        return request;        
    }
    
    public GetStylesRequest createGetStylesRequest() throws IOException, UnsupportedOperationException{
        if (getCapabilities().getRequest().getGetStyles() == null) {
            throw new UnsupportedOperationException("Server does not specify a GetStyles operation. Cannot be performed");
        }
        
        URL onlineResource = getCapabilities().getRequest().getGetStyles().getGet();
        if (onlineResource == null) {
            onlineResource = serverURL;
        }
        
        GetStylesRequest request = specification.createGetStylesRequest(onlineResource,
                getNamedLayers());
       
        return request;
    }
    
    public PutStylesRequest createPutStylesRequest() throws IOException, UnsupportedOperationException {
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
    
    /**********************************************************
     * UTILITY METHODS
     **********************************************************/
    
    /**
     * Utility method to return each layer that has a name. This method maintains no hierarchy at all.
     * 
     * @return An array of Layers, each value has a it's name property set or an empty array if there are none. It will return null if there is no capabilities document
     * 
     */
    public Layer[] getNamedLayers() {
    	
    	if (capabilities == null) {
    		return null;
    	}
    	
        List namedLayersList = new ArrayList();
        
        Layer[] layers = capabilities.getLayers();

        for( int i = 0; i < layers.length; i++ ) {
            if ((layers[i].getName() != null) && (layers[i].getName().length() != 0)) {
                namedLayersList.add(layers[i]);
            }
        }

        Layer[] namedLayers = new Layer[namedLayersList.size()];
        for (int i = 0; i < namedLayersList.size(); i++) {
            namedLayers[i] = (Layer) namedLayersList.get(i);
        }
        
        return namedLayers;
    }

    private Set getQueryableLayers() {
        Set layers = new TreeSet();

        Layer[] namedLayers = getNamedLayers();

        for( int i = 0; i < namedLayers.length; i++ ) {
            Layer layer = (Layer) namedLayers[i];

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

    private Set getSRSs() throws IOException {
        Set srss = new TreeSet();

        Layer[] layers = getCapabilities().getLayers();

        for( int i = 0; i < layers.length; i++ ) {
            if (layers[i].getSrs() != null) {
                srss.addAll(layers[i].getSrs());
            }
        }

        return srss;
    }

    /*
     * ************************************************************************* 
     * Catalog Interface Methods
     * *************************************************************************
     */

    /**
     * Metadata search through entries.
     * 
     * @see org.geotools.catalog.Discovery#search(org.geotools.catalog.QueryRequest)
     * @param queryRequest
     * @return List of matching TypeEntry
     */
    public List search( QueryRequest queryRequest ) {
        if (queryRequest == QueryRequest.ALL) {
            return entries();
        }
        List queryResults = new ArrayList();
        CATALOG: for( Iterator i = entries().iterator(); i.hasNext(); ) {
            CatalogEntry entry = (CatalogEntry) i.next();
            METADATA: for( Iterator m = entry.metadata().values().iterator(); m.hasNext(); ) {
                if (queryRequest.match(m.next())) {
                    queryResults.add(entry);
                    break METADATA;
                }
            }
        }
        return queryResults;
    }

    /**
     * Catalog of the Layers known to this Web Map Server.
     * <p>
     * It flattens the hierarchy. Each element is of type Layer.
     * </p>
     * 
     * @return an iterator that will iterate through the layers
     */
    public List entries() {
        ArrayList layers = new ArrayList();

        for( int i = 0; i < capabilities.getLayers().length; i++ ) {
            Layer layer = capabilities.getLayers()[i];

            if ((layer.getName() != null) && (layer.getName().length() != 0)) { //$NON-NLS-1$

                layers.add(new WMSLayerCatalogEntry(this, layer));
            }
        }
        return Collections.unmodifiableList(layers);
    }
    
    /**
     * Given a layer and a coordinate reference system, will locate an envelope
     * for that layer in that CRS.
     * 
     * If null is returned, no valid bounding box could be found.
     * 
     * @param layer
     * @param crs
     * @return an Envelope containing a valid bounding box, or null if none are found
     */
    public Envelope getEnvelope(Layer layer, CoordinateReferenceSystem crs) {
        
        for (int i = 0; i < crs.getIdentifiers().length; i++) {
            String epsgCode = crs.getIdentifiers()[i].toString();

            BoundingBox tempBBox = null;
            Layer parentLayer = layer;

            //Locate a BBOx if we can
            while( tempBBox == null && parentLayer != null ) {
                tempBBox = (BoundingBox) parentLayer.getBoundingBoxes().get(epsgCode);
                
                //TODO HACK 3005 and 42102 are close enough
                if (epsgCode.equals("EPSG:3005") && tempBBox == null) {
                    tempBBox = (BoundingBox) parentLayer.getBoundingBoxes().get("EPSG:42102");
                }
                parentLayer = parentLayer.getParent();
            }
    
            //Otherwise, locate a LatLon BBOX
    
            //TODO HACK 4326 is close enough to 4269
            if (tempBBox == null && ("EPSG:4326".equals(epsgCode.toUpperCase()) ||
                    "EPSG:4269".equals(epsgCode.toUpperCase()) )) { //$NON-NLS-1$
                LatLonBoundingBox latLonBBox = null;
    
                parentLayer = layer;
                while (latLonBBox == null && parentLayer != null) {
                    latLonBBox = parentLayer.getLatLonBoundingBox();
                    if (latLonBBox != null) {
                        break;
                    }
                    parentLayer = layer.getParent();
                }
                
                if (latLonBBox == null) {
                    return new GeneralEnvelope(new double[] { -180, -90 }, new double[] {180, 90});
                }
                
                return new GeneralEnvelope(new double[] {latLonBBox.getMinX(), latLonBBox.getMinY()}, 
                        new double[] { latLonBBox.getMaxX(), latLonBBox.getMaxY() });
            }
            
            //TODO Attempt to figure out the valid area of teh CRS and use that.
            
            if (tempBBox != null) {
                return new GeneralEnvelope(new double[] { tempBBox.getMinX(), tempBBox.getMinY()}, 
                        new double[] { tempBBox.getMaxX(), tempBBox.getMaxY() });
            }
    
        }
        return null;
    }
}
