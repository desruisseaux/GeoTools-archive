/*
 * Created on Sep 11, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.data.wms;

import java.net.URL;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.geotools.data.ows.Layer;
import org.geotools.data.wms.request.AbstractDescribeLayerRequest;
import org.geotools.data.wms.request.AbstractGetCapabilitiesRequest;
import org.geotools.data.wms.request.AbstractGetLegendGraphicRequest;
import org.geotools.data.wms.request.AbstractGetStylesRequest;
import org.geotools.data.wms.request.AbstractPutStylesRequest;
import org.geotools.data.wms.request.DescribeLayerRequest;
import org.geotools.data.wms.request.GetFeatureInfoRequest;
import org.geotools.data.wms.request.GetLegendGraphicRequest;
import org.geotools.data.wms.request.GetMapRequest;
import org.geotools.data.wms.request.GetStylesRequest;
import org.geotools.data.wms.request.PutStylesRequest;

/**
 * @author Kefka
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class WMS1_1_0 extends WMS1_0_0 {

	public WMS1_1_0 () {
		
	}
	
    public org.geotools.data.wms.request.GetMapRequest createGetMapRequest( URL get, SimpleLayer[] layers, Set availableSRSs, String[] formatStrings,
            List exceptions ) {
        return new GetMapRequest(get, layers, availableSRSs, formatStrings, exceptions);
    }
    
	/* (non-Javadoc)
	 * @see org.geotools.data.wms.Specification#getVersion()
	 */
	public String getVersion() {
		return "1.1.0";
	}

	/**
	 * @see org.geotools.data.wms.Specification#createGetCapabilitiesRequest(java.net.URL)
	 */
	public AbstractGetCapabilitiesRequest createGetCapabilitiesRequest(URL server) {
		return new GetCapsRequest(server);
	}
	
    /**
     * @see org.geotools.data.wms.WMS1_0_0#createGetFeatureInfoRequest(java.net.URL, org.geotools.data.wms.request.GetMapRequest, java.util.Set, java.lang.String[])
     */
    public org.geotools.data.wms.request.GetFeatureInfoRequest createGetFeatureInfoRequest( URL onlineResource, org.geotools.data.wms.request.GetMapRequest getMapRequest,
            Set queryableLayers, String[] infoFormats ) {
        return new GetFeatureInfoRequest(onlineResource, getMapRequest, queryableLayers, infoFormats);
    }
    
    /**
     * @see org.geotools.data.wms.WMS1_0_0#createDescribeLayerRequest(java.net.URL)
     */
    public DescribeLayerRequest createDescribeLayerRequest( URL onlineResource ) throws UnsupportedOperationException {
        return new InternalDescribeLayerRequest(onlineResource, null);
    }
    
    public GetLegendGraphicRequest createGetLegendGraphicRequest( URL onlineResource, SimpleLayer[] layers, String[] formats, String[] exceptions) {
        return new InternalGetLegendGraphicRequest(onlineResource, layers, formats, exceptions);
    }
    
    public GetStylesRequest createGetStylesRequest( URL onlineResource, Layer[] layers ) throws UnsupportedOperationException {
        return new InternalGetStylesRequest(onlineResource, layers);
    }
    
    /**
     * @see org.geotools.data.wms.WMS1_0_0#createPutStylesRequest(java.net.URL)
     */
    public PutStylesRequest createPutStylesRequest( URL onlineResource) throws UnsupportedOperationException {
        return new InternalPutStylesRequest(onlineResource);
    }
    
	public static class GetCapsRequest extends WMS1_0_0.GetCapsRequest {

		public GetCapsRequest(URL urlGetCapabilities) {
			super(urlGetCapabilities);
			// TODO Auto-generated constructor stub
		}
		
		/* (non-Javadoc)
		 * @see org.geotools.data.wms.request.AbstractGetCapabilitiesRequest#initRequest()
		 */
		protected void initRequest() {
			setProperty("REQUEST", "GetCapabilities");
		}
		/* (non-Javadoc)
		 * @see org.geotools.data.wms.request.AbstractGetCapabilitiesRequest#initService()
		 */
		protected void initService() {
			setProperty("SERVICE", "WMS");
		}
		/* (non-Javadoc)
		 * @see org.geotools.data.wms.request.AbstractGetCapabilitiesRequest#initVersion()
		 */
		protected void initVersion() {
			setProperty("VERSION", "1.1.0");
		}
	}
	
	public static class GetMapRequest extends WMS1_0_0.GetMapRequest {

        public GetMapRequest( URL onlineResource, SimpleLayer[] availableLayers, Set availableSRSs, String[] availableFormats, List availableExceptions ) {
            super(onlineResource, availableLayers, availableSRSs, availableFormats, availableExceptions);
        }
        
        protected void initRequest() {
            setProperty(REQUEST, "GetMap");
        }
        
        protected void initVersion() {
            setProperty(VERSION, "1.1.0");
        }

        protected String getRequestFormat( String format ) {
            return format;
        }
  	}
	
	public static class GetFeatureInfoRequest extends WMS1_0_0.GetFeatureInfoRequest {

        /**
         * @param onlineResource
         * @param request
         * @param queryableLayers
         * @param infoFormats
         */
        public GetFeatureInfoRequest( URL onlineResource, org.geotools.data.wms.request.GetMapRequest request, Set queryableLayers, String[] infoFormats ) {
            super(onlineResource, request, queryableLayers, infoFormats);
        }        
	    
        protected void initRequest() {
            setProperty("REQUEST", "GetFeatureInfo");
        }
        protected void initVersion() {
            setProperty("VERSION", "1.1.0");
        }
	}
	
	public static class InternalDescribeLayerRequest extends AbstractDescribeLayerRequest {

        /**
         * @param onlineResource
         * @param properties
         */
        public InternalDescribeLayerRequest( URL onlineResource, Properties properties ) {
            super(onlineResource, properties);
        }

        protected void initVersion() {
            setProperty(VERSION, "1.1.0");
        }
	}
	
	public static class InternalGetLegendGraphicRequest extends AbstractGetLegendGraphicRequest {

        public InternalGetLegendGraphicRequest( URL onlineResource, SimpleLayer[] layers, String[] formats, String[] exceptions ) {
            super(onlineResource, layers, formats, exceptions);
        }

        protected void initVersion() {
            setProperty(VERSION, "1.0.0");
        }
	    
	}
	
	public static class InternalGetStylesRequest extends AbstractGetStylesRequest {

        /**
         * @param onlineResource
         * @param layers
         */
        public InternalGetStylesRequest( URL onlineResource, Layer[] layers ) {
            super(onlineResource, layers);
        }

        /* (non-Javadoc)
         * @see org.geotools.data.wms.request.AbstractGetStylesRequest#initVersion()
         */
        protected void initVersion() {
            setProperty(VERSION, "1.1.0");
        }
	    
	}
	
	public static class InternalPutStylesRequest extends AbstractPutStylesRequest {

        public InternalPutStylesRequest( URL onlineResource ) {
            super(onlineResource, null);
        }

        protected void initVersion() {
            setProperty(VERSION, "1.0.0");            
        }
	    
	}

}
