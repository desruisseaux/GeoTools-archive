/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 */
package org.geotools.data.wms.test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;

import org.geotools.data.ows.BoundingBox;
import org.geotools.data.ows.Layer;
import org.geotools.data.ows.WMSCapabilities;
import org.geotools.data.wms.Specification;
import org.geotools.data.wms.WMS1_1_1;
import org.geotools.data.wms.WebMapServer;
import org.xml.sax.SAXException;

/**
 * @author Kefka
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class WMS1_1_1Test extends WMS1_1_0Test {

	public WMS1_1_1Test() throws Exception {
		this.spec = new WMS1_1_1();
		this.server = new URL(
		"http://www2.dmsolutions.ca/cgi-bin/mswms_gmap?VERSION=1.1.0&REQUEST=GetCapabilities");
		
		this.getStylesURL = new URL("http://mapserv2.esrin.esa.it/cubestor/cubeserv/cubeserv.cgi?VERSION=1.1.1&REQUEST=GetCapabilities&SERVICE=WMS");
	}

	public void testGetVersion() {
		assertEquals(spec.getVersion(), "1.1.1");
	}
	public void testCreateParser() throws Exception {
        WMSCapabilities capabilities = createCapabilities("1.1.1Capabilities.xml");
        
        assertEquals(capabilities.getVersion(), "1.1.1");
        assertEquals(capabilities.getService().getName(), "OGC:WMS");
        assertEquals(capabilities.getService().getTitle(), "Microsoft TerraServer Map Server");
        assertEquals(capabilities.getService().get_abstract(), "WMT Map Server maintained by Microsoft Corporation.  Data returned provided by US Geological Survey.  Contact: tbarclay@microsoft.com or gylee@usgs.gov.");
        assertEquals(capabilities.getService().getOnlineResource(), new URL("http://terraservice.net/"));
        
        String[] keywords = { "USGS", "DOQ", "DRG", "Topographic", "UrbanArea", "Urban Areas" };
        
        for (int i = 0; i < capabilities.getService().getKeywordList().length; i++) {
        	assertEquals(capabilities.getService().getKeywordList()[i],
        			keywords[i]);
        }
        
        assertEquals(capabilities.getRequest().getGetCapabilities().getFormatStrings()[0], "application/vnd.ogc.wms_xml");
        assertEquals(capabilities.getRequest().getGetCapabilities().getGet(), new URL("http://terraservice.net/ogccapabilities.ashx"));
        assertEquals(capabilities.getRequest().getGetCapabilities().getPost(), new URL("http://terraservice.net/ogccapabilities.ashx"));
        
        assertEquals(capabilities.getRequest().getGetMap().getFormatStrings()[0], "image/jpeg");
        assertEquals(capabilities.getRequest().getGetMap().getGet(), new URL("http://terraservice.net/ogcmap.ashx"));
        
        assertNull(capabilities.getRequest().getGetFeatureInfo());
        
        assertEquals(capabilities.getLayerList().size(), 4);
        
        Layer layer = (Layer) capabilities.getLayerList().get(0);
        assertNotNull(layer);
        assertNull(layer.getName());
        assertEquals(layer.getTitle(), "Microsoft TerraServer Map Server");
        assertEquals(layer.getSrs().size(), 1);
        assertTrue(layer.getSrs().contains("EPSG:4326" ));

        validateLatLonBoundingBox(layer.getLatLonBoundingBox(), 
        		-168.67, 17.84, -65.15, 71.55);
        
        assertNull(layer.getParent());
        assertEquals(layer.getBoundingBoxes().size(), 0);
        assertEquals(layer.getStyles().size(), 0);
        
        layer = (Layer) capabilities.getLayerList().get(1);
        assertEquals(layer.getName(), "DOQ");
        assertEquals(layer.getTitle(), "USGS Digital Ortho-Quadrangles");
        // changed expected to 14 to account for inherited srs
        assertEquals(layer.getSrs().size(), 14);
        // Added additional check to test for inherited srs
        assertTrue(layer.getSrs().contains("EPSG:4326"));
        assertTrue(layer.getSrs().contains("EPSG:26905"));
        assertTrue(layer.getSrs().contains("EPSG:26920"));
        assertEquals(layer.getBoundingBoxes().size(), 13);
        BoundingBox bbox = (BoundingBox) layer.getBoundingBoxes().get("EPSG:26905");
        assertNotNull(bbox);
        assertEquals(bbox.getCrs(), "EPSG:26905");
        assertEquals(bbox.getMinX(), 552600.0, 0.0);
		assertEquals(bbox.getMinY(), 6540200.0, 0.0);
		assertEquals(bbox.getMaxX(), 670200.0, 0.0);
		assertEquals(bbox.getMaxY(), 6794800.0, 0.0);
        
		bbox = (BoundingBox) layer.getBoundingBoxes().get("EPSG:26920");
		assertNotNull(bbox);
		assertEquals(bbox.getCrs(), "EPSG:26920");
		assertEquals(bbox.getMinX(), 181800.0, 0.0);
		assertEquals(bbox.getMinY(), 1985200.0, 0.0);
		assertEquals(bbox.getMaxX(), 269400.0, 0.0);
		assertEquals(bbox.getMaxY(), 2048600.0, 0.0);
		 
		// Changed expected value, no duplicates allowed by spec
        assertEquals(layer.getStyles().size(), 18);
        assertTrue(layer.getStyles().contains("UTMGrid"));
        assertTrue(layer.getStyles().contains("GeoGrid_Cyan"));
        assertTrue(layer.getStyles().contains("GeoGrid_Black"));
        assertTrue(layer.getStyles().contains("GeoGrid_Gray"));
        assertTrue(layer.getStyles().contains("GeoGrid_White"));
        
        assertFalse(layer.isQueryable());
        
    		// Added test to verify inheritance, should be same as previous llbbox
        validateLatLonBoundingBox(layer.getLatLonBoundingBox(), 
        		-168.67, 17.84, -65.15, 71.55);
          
        
        layer = (Layer) capabilities.getLayerList().get(2);
        assertNotNull(layer);
        assertEquals(layer.getName(), "DRG");
        assertEquals(layer.getTitle(), "USGS Raster Graphics (Topo Maps)");
    		// Added test to verify inheritance, should be same as previous llbbox
        validateLatLonBoundingBox(layer.getLatLonBoundingBox(), 
        		-168.67, 17.84, -65.15, 71.55);
        
        layer = (Layer) capabilities.getLayerList().get(3);
        assertNotNull(layer);
        assertEquals(layer.getName(), "UrbanArea");
        assertEquals(layer.getTitle(), "USGS Urban Areas Ortho-Imagery");
    		// Added test to verify inheritance, should be same as previous llbbox
        validateLatLonBoundingBox(layer.getLatLonBoundingBox(), 
        		-168.67, 17.84, -65.15, 71.55);
        
	}
	
	//Don't have working server to test against yet.
//	public void testCreateGetStylesRequest() throws Exception {
//      WebMapServer wms = new CustomWMS(getStylesURL);
//      
//      GetStylesRequest request = wms.createGetStylesRequest();
//      assertNotNull(request);
//	}
	
	/* (non-Javadoc)
	 * @see org.geotools.data.wms.test.WMS1_0_0Test#checkProperties(java.util.Properties)
	 */
	protected void checkProperties(Properties properties) {
        assertEquals(properties.get("VERSION"), "1.1.1");
        assertEquals(properties.get("REQUEST"), "GetCapabilities");
        assertEquals(properties.get("SERVICE"), "WMS");
	}
	
    protected WebMapServer getCustomWMS( URL featureURL ) throws SAXException, URISyntaxException, IOException {
        return new CustomWMS(featureURL);
    }
    //forces use of 1.1.1 spec
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
            specs[0] = new WMS1_1_1();
        }
    }
}
