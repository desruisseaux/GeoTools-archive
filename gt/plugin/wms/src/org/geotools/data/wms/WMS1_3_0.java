/*
 * Created on Sep 12, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.data.wms;

import java.net.URL;

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
			return extractIntChild(serviceElement, "LayerLimit", 0);
		}
		protected int queryMaxHeight(Element serviceElement) {
			return extractIntChild(serviceElement, "MaxHeight", 0);
		}
		protected int queryMaxWidth(Element serviceElement) {
			return extractIntChild(serviceElement, "MaxWidth", 0);
		}
		private int extractIntChild(Element element, String childName, int _default) {
			String result = element.getChildText(childName, defaultNamespace);
			if (result == null || result.length() == 0) {
				return _default;
			}
			return Integer.parseInt(result);
		}
		protected String getBBoxCRSName() {
			return "CRS";
		}
		protected void parseLatLonBoundingBox(Element layerElement,
				WMSBuilder builder) {
			Element geoBboxElement = layerElement.getChild("EX_GeographicBoundingBox", defaultNamespace);
			if (geoBboxElement == null) {
				return;
			}
			
			double minX = Double.parseDouble(geoBboxElement.getChildText("westBoundLongitude", defaultNamespace));
			double maxX = Double.parseDouble(geoBboxElement.getChildText("eastBoundLongitude", defaultNamespace));
			double minY = Double.parseDouble(geoBboxElement.getChildText("southBoundLatitude", defaultNamespace));
			double maxY = Double.parseDouble(geoBboxElement.getChildText("northBoundLatitude", defaultNamespace));
			
			builder.buildLatLonBoundingBox(minX, minY, maxX, maxY);
		}
	}
}
