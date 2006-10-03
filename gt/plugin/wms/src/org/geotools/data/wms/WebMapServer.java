/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004-2006, Geotools Project Managment Committee (PMC)
 *    
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.data.wms;

import java.io.IOException;
import java.net.URL;
import java.util.Iterator;

import org.geotools.data.ows.AbstractOpenWebService;
import org.geotools.data.ows.CRSEnvelope;
import org.geotools.data.ows.Capabilities;
import org.geotools.data.ows.GetCapabilitiesRequest;
import org.geotools.data.ows.GetCapabilitiesResponse;
import org.geotools.data.ows.Layer;
import org.geotools.data.ows.OperationType;
import org.geotools.data.ows.Request;
import org.geotools.data.ows.Specification;
import org.geotools.data.ows.WMSCapabilities;
import org.geotools.data.ows.WMSRequest;
import org.geotools.data.wms.request.DescribeLayerRequest;
import org.geotools.data.wms.request.GetFeatureInfoRequest;
import org.geotools.data.wms.request.GetLegendGraphicRequest;
import org.geotools.data.wms.request.GetMapRequest;
import org.geotools.data.wms.request.GetStylesRequest;
import org.geotools.data.wms.request.PutStylesRequest;
import org.geotools.data.wms.response.DescribeLayerResponse;
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
public class WebMapServer extends AbstractOpenWebService {

    /**
     * Creates a new WebMapServer instance and attempts to retrieve the 
     * Capabilities document specified by serverURL. 
     * 
     * @param serverURL a URL that points to the capabilities document of a server
     * @throws IOException if there is an error communicating with the server
     * @throws ServiceException if the server responds with an error
     */
    public WebMapServer( final URL serverURL ) throws IOException, ServiceException {
    	super(serverURL);
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
    
    public GetCapabilitiesResponse issueRequest(GetCapabilitiesRequest request) throws IOException, ServiceException {
    	return (GetCapabilitiesResponse) internalIssueRequest(request);
    }
    
    public GetMapResponse issueRequest(GetMapRequest request) throws IOException, ServiceException {
        return (GetMapResponse) internalIssueRequest(request);
    }
    
    public GetFeatureInfoResponse issueRequest(GetFeatureInfoRequest request) throws IOException, ServiceException {
        return (GetFeatureInfoResponse) internalIssueRequest(request);
    }
    
    public DescribeLayerResponse issueRequest(DescribeLayerRequest request) throws IOException, ServiceException {
        return (DescribeLayerResponse) internalIssueRequest(request);
    }
    
    public GetLegendGraphicResponse issueRequest(GetLegendGraphicRequest request) throws IOException, ServiceException {
        return (GetLegendGraphicResponse) internalIssueRequest(request);
    }
    
    public GetStylesResponse issueRequest(GetStylesRequest request) throws IOException, ServiceException {
        return (GetStylesResponse) internalIssueRequest(request);
    }
    
    public PutStylesResponse issueRequest(PutStylesRequest request) throws IOException, ServiceException {
        return (PutStylesResponse) internalIssueRequest(request);
    }
    
    /**
     * Get the getCapabilities document. If there was an error parsing it
     * during creation, it will return null (and it should have thrown an
     * exception during creation).
     * 
     * @return a WMSCapabilities object, representing the Capabilities of the server
     */
    public WMSCapabilities getCapabilities() {
        return (WMSCapabilities) capabilities;
    }
    
    private WMSSpecification getSpecification() {
    	return (WMSSpecification) specification;
    }
    
    
    private URL findURL(OperationType operation) {
    	if (operation.getGet() != null) {
    		return operation.getGet();
    	}
    	return serverURL;
    }
    
    /**
     * Creates a GetMapRequest that can be configured and then passed to 
     * issueRequest(). 
     * 
     * @return a configureable GetMapRequest object
     */
    public GetMapRequest createGetMapRequest() {
        URL onlineResource = findURL(getCapabilities().getRequest().getGetMap());

        return (GetMapRequest) getSpecification().createGetMapRequest(onlineResource);
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

        URL onlineResource = findURL(getCapabilities().getRequest().getGetFeatureInfo());
        
        GetFeatureInfoRequest request = getSpecification().createGetFeatureInfoRequest(onlineResource,
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
        
        DescribeLayerRequest request = getSpecification().createDescribeLayerRequest(onlineResource);
        
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
        
        GetLegendGraphicRequest request = getSpecification().createGetLegendGraphicRequest(onlineResource);
        
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
        
        GetStylesRequest request = getSpecification().createGetStylesRequest(onlineResource);
       
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
        
        PutStylesRequest request = getSpecification().createPutStylesRequest(onlineResource);
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
                    MathTransform transform = CRS.findMathTransform(fromCRS, crs, true);
                    
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
