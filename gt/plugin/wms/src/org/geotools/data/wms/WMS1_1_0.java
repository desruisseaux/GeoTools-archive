/*
 * Created on Sep 11, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.data.wms;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.geotools.data.wms.WMS1_0_0.GetMapRequest;
import org.geotools.data.wms.request.GetCapabilitiesRequest;
import org.jdom.Element;
import org.jdom.Namespace;

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

	/* (non-Javadoc)
	 * @see org.geotools.data.wms.Specification#createGetCapabilitiesRequest(java.net.URL)
	 */
	public GetCapabilitiesRequest createGetCapabilitiesRequest(URL server) {
		return new GetCapsRequest(server);
	}
	
	public static class GetCapsRequest extends WMS1_0_0.GetCapsRequest {

		public GetCapsRequest(URL urlGetCapabilities) {
			super(urlGetCapabilities);
			// TODO Auto-generated constructor stub
		}
		
		/* (non-Javadoc)
		 * @see org.geotools.data.wms.request.GetCapabilitiesRequest#initRequest()
		 */
		protected void initRequest() {
			setProperty("REQUEST", "GetCapabilities");
		}
		/* (non-Javadoc)
		 * @see org.geotools.data.wms.request.GetCapabilitiesRequest#initService()
		 */
		protected void initService() {
			setProperty("SERVICE", "WMS");
		}
		/* (non-Javadoc)
		 * @see org.geotools.data.wms.request.GetCapabilitiesRequest#initVersion()
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
}
