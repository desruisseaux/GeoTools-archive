/*
 * Created on Sep 12, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.data.wms.test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import org.geotools.data.ows.BoundingBox;
import org.geotools.data.ows.LatLonBoundingBox;
import org.geotools.data.ows.Layer;
import org.geotools.data.ows.WMSCapabilities;
import org.geotools.data.wms.SimpleLayer;
import org.geotools.data.wms.Specification;
import org.geotools.data.wms.WMS1_3_0;
import org.geotools.data.wms.WebMapServer;
import org.geotools.data.wms.request.GetFeatureInfoRequest;
import org.geotools.data.wms.request.GetMapRequest;
import org.xml.sax.SAXException;

/**
 * @author Kefka
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class WMS1_3_0Test extends WMS1_1_1Test{

	public WMS1_3_0Test() throws Exception {
		this.spec = new WMS1_3_0();
		this.server = new URL("http://www2.demis.nl/mapserver/Request.asp?VERSION=1.3.0&SERVICE=WMS&REQUEST=GetCapabilities");
	}

	public void testGetVersion() {
		assertEquals(spec.getVersion(), "1.3.0");
	}

	protected void checkProperties(Properties properties) {
        assertEquals(properties.get("VERSION"), "1.3.0");
        assertEquals(properties.get("REQUEST"), "GetCapabilities");
        assertEquals(properties.get("SERVICE"), "WMS");
	}
	
	public void testCreateParser() throws Exception {
		WMSCapabilities capabilities = createCapabilities("1.3.0Capabilities.xml");
		
		assertNotNull(capabilities);
		
		assertEquals(capabilities.getVersion(), "1.3.0");
		assertEquals(capabilities.getService().getName(), "WMS");
		assertEquals(capabilities.getService().getTitle(), "World Map");
		assertEquals(capabilities.getService().get_abstract(), "None");
		assertEquals(capabilities.getService().getOnlineResource(), new URL("http://www2.demis.nl"));
		
		assertEquals(capabilities.getService().getLayerLimit(), 40);
		assertEquals(capabilities.getService().getMaxWidth(), 2000);
		assertEquals(capabilities.getService().getMaxHeight(), 2000);
		
		assertEquals(capabilities.getRequest().getGetCapabilities().getFormatStrings()[0], "text/xml");
		assertEquals(capabilities.getRequest().getGetCapabilities().getGet(), new URL("http://www2.demis.nl/wms/wms.asp?wms=WorldMap&"));
		assertEquals(capabilities.getRequest().getGetCapabilities().getPost(), new URL("http://www2.demis.nl/wms/wms.asp?wms=WorldMap&"));
		
		assertEquals(capabilities.getRequest().getGetMap().getFormatStrings().length, 5);
		assertEquals(capabilities.getRequest().getGetMap().getFormatStrings()[0], "image/gif");
		assertEquals(capabilities.getRequest().getGetMap().getFormatStrings()[1], "image/png");
		assertEquals(capabilities.getRequest().getGetMap().getFormatStrings()[2], "image/jpeg");
		assertEquals(capabilities.getRequest().getGetMap().getFormatStrings()[3], "image/bmp");
		assertEquals(capabilities.getRequest().getGetMap().getFormatStrings()[4], "image/swf");
		assertEquals(capabilities.getRequest().getGetMap().getGet(), new URL("http://www2.demis.nl/wms/wms.asp?wms=WorldMap&"));
		
		assertEquals(capabilities.getRequest().getGetFeatureInfo().getFormatStrings().length, 4);
		assertEquals(capabilities.getRequest().getGetFeatureInfo().getFormatStrings()[0], "text/xml");
		assertEquals(capabilities.getRequest().getGetFeatureInfo().getFormatStrings()[1], "text/plain");
		assertEquals(capabilities.getRequest().getGetFeatureInfo().getFormatStrings()[2], "text/html");
		assertEquals(capabilities.getRequest().getGetFeatureInfo().getFormatStrings()[3], "text/swf");
		assertEquals(capabilities.getRequest().getGetFeatureInfo().getGet(), new URL("http://www2.demis.nl/wms/wms.asp?wms=WorldMap&"));
		
		Layer topLayer = capabilities.getLayers()[0];
		assertNotNull(topLayer);
		assertNull(topLayer.getParent());
		assertFalse(topLayer.isQueryable());
		assertEquals(topLayer.getTitle(), "World Map");
		assertEquals(topLayer.getSrs().size(), 1);
		assertTrue(topLayer.getSrs().contains("CRS:84"));
		
		LatLonBoundingBox llbbox = topLayer.getLatLonBoundingBox();
		assertNotNull(llbbox);
		assertEquals(llbbox.getMinX(), -180, 0.0);
		assertEquals(llbbox.getMaxX(), 180, 0.0);
		assertEquals(llbbox.getMinY(), -90, 0.0);
		assertEquals(llbbox.getMaxY(), 90, 0.0);
		
		assertEquals(topLayer.getBoundingBoxes().size(), 1);
		
		BoundingBox bbox = (BoundingBox) topLayer.getBoundingBoxes().get("CRS:84");
		assertNotNull(bbox);
		assertEquals(bbox.getCrs(), "CRS:84");
		assertEquals(bbox.getMinX(), -184, 0.0);
		assertEquals(bbox.getMaxX(), 180, 0.0);
		assertEquals(bbox.getMinY(), -90.0000000017335, 0.0);
		assertEquals(bbox.getMaxY(), 90, 0.0);
		
		Layer layer = capabilities.getLayers()[1];
		assertEquals(layer.getParent(), topLayer);
		assertTrue(layer.isQueryable());
		assertEquals(layer.getName(), "Bathymetry");
		assertEquals(layer.getTitle(), "Bathymetry");
		
		bbox = (BoundingBox) layer.getBoundingBoxes().get("CRS:84");
		assertNotNull(bbox);
		assertEquals(bbox.getCrs(), "CRS:84");
		assertEquals(bbox.getMinX(), -180, 0.0);
		assertEquals(bbox.getMaxX(), 180, 0.0);
		assertEquals(bbox.getMinY(), -90, 0.0);
		assertEquals(bbox.getMaxY(), 90, 0.0);
		
		assertEquals(capabilities.getLayers().length, 21);
		
		layer = capabilities.getLayers()[20];
		assertEquals(layer.getParent(), topLayer);
		assertTrue(layer.isQueryable());
		assertEquals(layer.getName(), "Ocean features");
		assertEquals(layer.getTitle(), "Ocean features");
		
		bbox = (BoundingBox) layer.getBoundingBoxes().get("CRS:84");
		assertNotNull(bbox);
		assertEquals(bbox.getCrs(), "CRS:84");
		assertEquals(bbox.getMinX(), -180, 0.0);
		assertEquals(bbox.getMaxX(), 179.999420166016, 0.0);
		assertEquals(bbox.getMinY(), -62.9231796264648, 0.0);
		assertEquals(bbox.getMaxY(), 68.6906585693359, 0.0);
	}
	
	
    public void testCreateGetFeatureInfoRequest() throws Exception {
        URL featureURL = new URL("http://www.demis.nl/mapserver/request.asp?Service=WMS&Version=1.3.0&Request=GetCapabilities");
        WebMapServer wms = getCustomWMS(featureURL);
        WMSCapabilities caps = wms.getCapabilities();
        assertNotNull(caps);
        assertNotNull(caps.getRequest().getGetFeatureInfo());
        
        GetMapRequest getMapRequest = wms.createGetMapRequest();

        List simpleLayers = getMapRequest.getAvailableLayers();
        Iterator iter = simpleLayers.iterator();
        while (iter.hasNext()) {
                SimpleLayer simpleLayer = (SimpleLayer) iter.next();
                Object[] styles = simpleLayer.getValidStyles().toArray();
                if (styles.length == 0) {
                        simpleLayer.setStyle("");
                        continue;
                }
                Random random = new Random();
                int randomInt = random.nextInt(styles.length);
                simpleLayer.setStyle((String) styles[randomInt]);
        }
        getMapRequest.setLayers(simpleLayers);

        getMapRequest.setSRS("EPSG:4326");
        getMapRequest.setDimensions("400", "400");
        getMapRequest.setFormat("image/png");

        getMapRequest.setBBox("-114.01268,59.4596930,-113.26043,60.0835794");
        URL url2 = getMapRequest.getFinalURL();

        GetFeatureInfoRequest request = wms.createGetFeatureInfoRequest(getMapRequest);
        request.setQueryLayers(request.getQueryableLayers());
        request.setQueryPoint(200, 200);
        request.setInfoFormat("text/swf");
        
        System.out.println(request.getFinalURL());

//     TODO   Currently this server rtreturns code 400 !?
        /*GetFeatureInfoResponse response = (GetFeatureInfoResponse) wms.issueRequest(request);
        System.out.println(response.getContentType());
        assertTrue( response.getContentType().indexOf("text/plain") != -1 );
        BufferedReader in = new BufferedReader(new InputStreamReader(response.getInputStream()));
        String line;

        boolean textFound = false;
        while ((line = in.readLine()) != null) {
            System.out.println(line);
            if (line.indexOf("Wood Buffalo National Park") != -1) {
                textFound = true;
            }
        }
        assertTrue(textFound);*/

        
    }
	
    protected WebMapServer getCustomWMS( URL featureURL ) throws SAXException, URISyntaxException, IOException {
        return new CustomWMS(featureURL);
    }
    //forces use of 1.3.0 spec
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
            specs[0] = new WMS1_3_0();
        }
    }
}
