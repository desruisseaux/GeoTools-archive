/*
 * Created on Sep 12, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.data.wms;

import java.net.MalformedURLException;
import java.net.URL;

import org.geotools.data.wms.WMS1_0_0.Parser;
import org.geotools.data.wms.request.GetCapabilitiesRequest;
import org.jdom.Element;
import org.jdom.Namespace;

/**
 * @author Kefka
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class WMS1_3_0 extends WMS1_1_1 {
	
	public WMS1_3_0() {
        parsers = new WMSParser[1];
        parsers[0] = new Parser();
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
	public GetCapabilitiesRequest createGetCapabilitiesRequest(URL server) {
		return new GetCapsRequest(server);
	}
	
	public static class GetCapsRequest extends WMS1_1_1.GetCapsRequest {

		public GetCapsRequest(URL urlGetCapabilities) {
			super(urlGetCapabilities);
		}
		
		/* (non-Javadoc)
		 * @see org.geotools.data.wms.request.GetCapabilitiesRequest#initVersion()
		 */
		protected void initVersion() {
			setProperty("VERSION", "1.3.0");
		}
	}
	
	public static class Parser extends WMS1_1_1.Parser {
		
		public Parser() {
			defaultNamespace =  Namespace.getNamespace("", //$NON-NLS-1$
            	"http://www.opengis.net/wms"); //$NON-NLS-1$
		}
		
		public String getVersion() {
			return "1.3.0";
		}
		//TODO LatLonBoundingBox here - it is different in 1.3.0

		public String getName() {
			return "WMS_Capabilities";
		}
		
		protected String getCRSElementName() {
			return "CRS";
		}
		protected int queryLayerLimit(Element serviceElement) {
			return Integer.parseInt(serviceElement.getChildText("LayerLimit", defaultNamespace));
		}
		protected int queryMaxHeight(Element serviceElement) {
			return Integer.parseInt(serviceElement.getChildText("MaxHeight", defaultNamespace));
		}
		protected int queryMaxWidth(Element serviceElement) {
			return Integer.parseInt(serviceElement.getChildText("MaxWidth", defaultNamespace));
		}
		protected String getBBoxCRSName() {
			return "CRS";
		}
	}
}
