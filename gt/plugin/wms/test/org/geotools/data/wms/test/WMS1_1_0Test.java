/*
 * Created on Sep 11, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.data.wms.test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;

import org.geotools.data.ows.LatLonBoundingBox;
import org.geotools.data.ows.Layer;
import org.geotools.data.ows.WMSCapabilities;
import org.geotools.data.wms.Specification;
import org.geotools.data.wms.WMS1_1_0;
import org.geotools.data.wms.WebMapServer;
import org.geotools.data.wms.request.GetMapRequest;
import org.xml.sax.SAXException;

/**
 * @author Kefka
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class WMS1_1_0Test extends WMS1_0_0Test {

	public WMS1_1_0Test() throws Exception {
		server = new URL("http://www2.dmsolutions.ca/cgi-bin/mswms_gmap?VERSION=1.1.0&REQUEST=GetCapabilities");
		spec = new WMS1_1_0();
	}
	
	public void testGetVersion() {
		assertEquals(spec.getVersion(), "1.1.0");
	}

	/* (non-Javadoc)
	 * @see org.geotools.data.wms.test.WMS1_0_0Test#checkProperties(java.util.Properties)
	 */
	protected void checkProperties(Properties properties) {
		assertEquals(properties.get("REQUEST"), "GetCapabilities");
		assertEquals(properties.get("VERSION"), "1.1.0");
		assertEquals(properties.get("SERVICE"), "WMS");
	}
	
	public void testCreateParser() throws Exception {
		WMSCapabilities capabilities = createCapabilities("1.1.0Capabilities.xml");
		
		assertEquals(capabilities.getVersion(), "1.1.0");
		assertEquals(capabilities.getService().getName(), "OGC:WMS");
		assertEquals(capabilities.getService().getTitle(), "GMap WMS Demo Server");
		assertNotNull(capabilities.getService().get_abstract());
		assertEquals(capabilities.getService().getOnlineResource(), new URL("http://dev1.dmsolutions.ca/cgi-bin/mswms_gmap?"));
		assertNull(capabilities.getService().getKeywordList());
		
		assertEquals(capabilities.getRequest().getGetCapabilities().getFormatStrings().length, 1);
		assertEquals(capabilities.getRequest().getGetCapabilities().getFormatStrings()[0], "application/vnd.ogc.wms_xml");
		assertEquals(capabilities.getRequest().getGetCapabilities().getGet(), new URL("http://dev1.dmsolutions.ca/cgi-bin/mswms_gmap?"));
		assertEquals(capabilities.getRequest().getGetCapabilities().getPost(), new URL("http://dev1.dmsolutions.ca/cgi-bin/mswms_gmap?"));
		
		assertEquals(capabilities.getRequest().getGetMap().getFormatStrings().length, 7);
		assertEquals(capabilities.getRequest().getGetMap().getFormatStrings()[0], "image/gif");
		assertEquals(capabilities.getRequest().getGetMap().getFormatStrings()[3], "image/wbmp");
		assertEquals(capabilities.getRequest().getGetMap().getFormatStrings()[6], "image/tiff");
		assertEquals(capabilities.getRequest().getGetMap().getGet(), new URL("http://dev1.dmsolutions.ca/cgi-bin/mswms_gmap?"));
		assertEquals(capabilities.getRequest().getGetMap().getPost(), new URL("http://dev1.dmsolutions.ca/cgi-bin/mswms_gmap?"));
		
		assertEquals(capabilities.getRequest().getGetFeatureInfo().getFormatStrings().length, 3);
		assertEquals(capabilities.getRequest().getGetFeatureInfo().getFormatStrings()[0], "text/plain");
		assertEquals(capabilities.getRequest().getGetFeatureInfo().getFormatStrings()[1], "text/html");
		assertEquals(capabilities.getRequest().getGetFeatureInfo().getFormatStrings()[2], "application/vnd.ogc.gml");
		
		assertEquals(capabilities.getLayers().length, 12);
		
		Layer layer = capabilities.getLayers()[0];
		assertNull(layer.getParent());
		assertEquals(layer.getName(), "DEMO");
		assertEquals(layer.getTitle(), "GMap WMS Demo Server");
		assertEquals(layer.getSrs().size(), 4);
		assertTrue(layer.getSrs().contains("EPSG:42304"));
		assertTrue(layer.getSrs().contains("EPSG:42101"));
		assertTrue(layer.getSrs().contains("EPSG:4269"));
		assertTrue(layer.getSrs().contains("EPSG:4326"));
		
		LatLonBoundingBox llbbox = layer.getLatLonBoundingBox();
		validateLatLonBoundingBox(llbbox, -172.367, 35.6673, -11.5624, 83.8293);
		
		
		assertEquals(layer.getBoundingBoxes().size(), 1);
		assertNotNull(layer.getBoundingBoxes().get("EPSG:42304"));
		
		Layer layer2 = capabilities.getLayers()[1];
		assertEquals(layer2.getParent(), layer);
		assertEquals(layer2.getName(), "bathymetry");
		assertEquals(layer2.getTitle(), "Elevation/Bathymetry");
		assertTrue(layer2.getSrs().contains("EPSG:42304"));
		assertFalse(layer2.isQueryable());
		
		layer2 = capabilities.getLayers()[2];
		assertEquals(layer2.getParent(), layer);
		assertEquals(layer2.getName(), "land_fn");
		assertEquals(layer2.getTitle(), "Foreign Lands");

		validateLatLonBoundingBox(layer2.getLatLonBoundingBox(),
				-178.838, 31.8844, 179.94, 89.8254);
		
		assertTrue(layer2.getSrs().contains("EPSG:42304"));
		assertFalse(layer2.isQueryable());
		assertNotNull(layer2.getBoundingBoxes().get("EPSG:42304"));

		layer2 = capabilities.getLayers()[3];
		assertEquals(layer2.getParent(), layer);
		assertEquals(layer2.getName(), "park");
		assertEquals(layer2.getTitle(), "Parks");
		
		validateLatLonBoundingBox(layer2.getLatLonBoundingBox(),
				-173.433, 41.4271, -13.3643, 83.7466);
		
		assertTrue(layer2.getSrs().contains("EPSG:42304"));
		assertTrue(layer2.isQueryable());
		assertNotNull(layer2.getBoundingBoxes().get("EPSG:42304"));

		layer2 = capabilities.getLayers()[11];
		assertEquals(layer2.getParent(), layer);
		assertEquals(layer2.getName(), "grid");
		assertEquals(layer2.getTitle(), "Grid");

		llbbox = layer2.getLatLonBoundingBox();
		validateLatLonBoundingBox(llbbox, -178.838, 31.8844, 179.94, 89.8254);
		
		assertTrue(layer2.getSrs().contains("EPSG:42304"));
		assertFalse(layer2.isQueryable());
		assertNotNull(layer2.getBoundingBoxes().get("EPSG:42304"));
	}

    public void testCreateGetMapRequest() throws Exception {
        WebMapServer wms = new WebMapServer(server);
        WMSCapabilities caps = wms.getCapabilities();
        GetMapRequest request = wms.createGetMapRequest();
        request.setFormat("image/jpeg");
        System.out.println(request.getFinalURL().toExternalForm());
        
        assertTrue(request.getFinalURL().toExternalForm().indexOf("image/jpeg") >= 0);
    }
    
    protected WebMapServer getCustomWMS( URL featureURL ) throws SAXException, URISyntaxException, IOException {
        return new CustomWMS(featureURL);
    }
    
	protected void validateLatLonBoundingBox(LatLonBoundingBox llbbox,
			double minX, double minY, double maxX, double maxY) {
		assertNotNull(llbbox);
		assertEquals(llbbox.getMinX(), minX, 0.0);
		assertEquals(llbbox.getMinY(), minY, 0.0);
		assertEquals(llbbox.getMaxX(), maxX, 0.0);
		assertEquals(llbbox.getMaxY(), maxY, 0.0);
	}
	
    //forces use of 1.1.0 spec
    private class CustomWMS extends WebMapServer {

        /**
         * @param serverURL
         * @param wait
         * @throws SAXException
         * @throws URISyntaxException
         * @throws IOException
         */
        public CustomWMS( URL serverURL) throws SAXException, URISyntaxException, IOException {
            super(serverURL);
            // TODO Auto-generated constructor stub
        }
        
        protected void setupSpecifications() {
            specs = new Specification[1];
            specs[0] = new WMS1_1_0();
        }
    }
}

