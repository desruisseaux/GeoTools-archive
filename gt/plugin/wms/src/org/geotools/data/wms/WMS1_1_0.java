/*
 * Created on Sep 11, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.data.wms;

import java.io.IOException;
import java.net.URL;

import org.geotools.data.wms.WMS1_0_0.Parser;
import org.geotools.data.wms.request.GetCapabilitiesRequest;
import org.geotools.util.InternationalString;
import org.jdom.Document;

/**
 * @author Kefka
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class WMS1_1_0 extends WMS1_0_0 {

	public WMS1_1_0 () {
		parsers = new WMSParser[1];
		parsers[0] = new Parser();
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
	
	public static class Parser extends WMS1_0_0.Parser {

		/* (non-Javadoc)
		 * @see org.geotools.data.wms.AbstractWMSParser#getVersion()
		 */
		public String getVersion() {
			return "1.1.0";
		}
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
}
