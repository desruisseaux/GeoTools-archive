package org.geotools.demo.example;

import java.net.URL;
import java.util.Iterator;

import org.geotools.data.ows.CRSEnvelope;
import org.geotools.data.ows.Layer;
import org.geotools.data.ows.WMSCapabilities;
import org.geotools.data.wms.WebMapServer;
import org.geotools.data.wms.request.GetMapRequest;
import org.geotools.data.wms.response.GetMapResponse;
import org.geotools.filter.Filter;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.FilterFactoryFinder;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.LineSymbolizer;
import org.geotools.styling.Rule;
import org.geotools.styling.SLD;
import org.geotools.styling.Stroke;
import org.geotools.styling.Style;
import org.geotools.styling.StyleFactory;
import org.geotools.styling.StyleFactoryFinder;
import org.geotools.styling.Symbolizer;

import com.vividsolutions.jts.geom.Envelope;

/**
 * This example also works against a local geoserver.
 * 
 * @author Jody Garnett
 */
public class WMSExample {

	public static void main(String args[]) {
		try {
			localWMS();
		} catch (Exception ignore) {
			ignore.printStackTrace();
		}
	}

	public static void localWMS() throws Exception {
		URL url = new URL("http://localhost:8080/geoserver/wms?REQUEST=GetCapabilities");
		WebMapServer wms = new WebMapServer(url);

		WMSCapabilities caps = wms.getCapabilities();

		Layer layer = null;
		for( Iterator i = caps.getLayerList().iterator(); i.hasNext();){
			Layer test = (Layer) i.next();
			if( test.getName() != null && test.getName().length() != 0 ){
				layer = test;
				break;
			}
		}
		GetMapRequest mapRequest = wms.createGetMapRequest();		
		mapRequest.addLayer(layer);
		
		mapRequest.setDimensions("400", "400");
		mapRequest.setFormat("image/png");
		
		CRSEnvelope bbox = new CRSEnvelope("EPSG:4326",-100.0, -70, 25, 40 );
		mapRequest.setBBox(bbox);

		System.out.println(mapRequest.getFinalURL());
		
		GetMapResponse response = wms.issueRequest( mapRequest );		
		System.out.println(response.getContentType());
	}
}
