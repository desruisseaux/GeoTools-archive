/*
 * Created on Sep 12, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.data.wms;

import java.net.URL;
import java.util.List;
import java.util.Set;

import org.geotools.data.wms.request.AbstractGetCapabilitiesRequest;

/**
 * @author Kefka
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class WMS1_3_0 extends WMS1_1_1 {
	
	public WMS1_3_0() {

	}
	/* (non-Javadoc)
	 * @see org.geotools.data.wms.Specification#getVersion()
	 */
	public String getVersion() {
		return "1.3.0";
	}

	/* (non-Javadoc)
	 * @see org.geotools.data.wms.Specification#createGetCapabilitiesRequest(java.net.URL)
	 */
	public AbstractGetCapabilitiesRequest createGetCapabilitiesRequest(URL server) {
		return new GetCapsRequest(server);
	}
	
    public org.geotools.data.wms.request.GetMapRequest createGetMapRequest( URL get, SimpleLayer[] layers, Set availableSRSs, String[] formatStrings,
            List exceptions ) {
        return new GetMapRequest(get, layers, availableSRSs, formatStrings, exceptions);
    }
    
	public static class GetCapsRequest extends WMS1_1_1.GetCapsRequest {

		public GetCapsRequest(URL urlGetCapabilities) {
			super(urlGetCapabilities);
		}
		
		/* (non-Javadoc)
		 * @see org.geotools.data.wms.request.AbstractGetCapabilitiesRequest#initVersion()
		 */
		protected void initVersion() {
			setProperty("VERSION", "1.3.0");
		}
	}
	
	public static class GetMapRequest extends WMS1_1_1.GetMapRequest {

        public GetMapRequest( URL onlineResource, SimpleLayer[] availableLayers, Set availableSRSs, String[] availableFormats, List availableExceptions ) {
            super(onlineResource, availableLayers, availableSRSs, availableFormats, availableExceptions);
        }
	    
        protected void initVersion() {
            setVersion("1.3.0");
        }
	}
	
	public static class GetFeatureInfoRequest extends WMS1_1_1.GetFeatureInfoRequest {
	    
        public GetFeatureInfoRequest( URL onlineResource, org.geotools.data.wms.request.GetMapRequest request, Set queryableLayers, String[] infoFormats ) {
            super(onlineResource, request, queryableLayers, infoFormats);
        }
        
        protected void initVersion() {
            setProperty("VERSION", "1.3.0");
        }
        
        protected String getQueryX() {
            return "I";
        }
        
        protected String getQueryY() {
            return "J";
        }
	}
	
    public org.geotools.data.wms.request.GetFeatureInfoRequest createGetFeatureInfoRequest( URL onlineResource, org.geotools.data.wms.request.GetMapRequest getMapRequest,
            Set queryableLayers, String[] infoFormats ) {
        return new GetFeatureInfoRequest(onlineResource, getMapRequest, queryableLayers, infoFormats);
    }
}
