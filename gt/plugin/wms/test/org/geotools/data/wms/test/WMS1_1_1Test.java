/*
 * Created on Sep 10, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.data.wms.test;

import java.net.URL;
import java.util.Properties;

import org.geotools.data.ows.BoundingBox;
import org.geotools.data.ows.Layer;
import org.geotools.data.ows.WMSCapabilities;
import org.geotools.data.wms.WMS1_1_1;
import org.geotools.data.wms.WMSParser;

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
		"http://www2.demis.nl/mapserver/Request.asp?wmtver=1.0.0&request=getcapabilities");
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
        
        assertEquals(capabilities.getLayers().length, 4);
        
        Layer layer = capabilities.getLayers()[0];
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
        
        layer = capabilities.getLayers()[1];
        assertEquals(layer.getName(), "DOQ");
        assertEquals(layer.getTitle(), "USGS Digital Ortho-Quadrangles");
        assertEquals(layer.getSrs().size(), 13);
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
        assertEquals(layer.getStyles().size(), 34);
        assertTrue(layer.getStyles().contains("UTMGrid"));
        assertTrue(layer.getStyles().contains("GeoGrid_Cyan"));
        assertTrue(layer.getStyles().contains("GeoGrid_Black"));
        assertTrue(layer.getStyles().contains("GeoGrid_Gray"));
        assertTrue(layer.getStyles().contains("GeoGrid_White"));
        
        assertFalse(layer.isQueryable());
        
        layer = capabilities.getLayers()[2];
        assertNotNull(layer);
        assertEquals(layer.getName(), "DRG");
        assertEquals(layer.getTitle(), "USGS Raster Graphics (Topo Maps)");
        
        layer = capabilities.getLayers()[3];
        assertNotNull(layer);
        assertEquals(layer.getName(), "UrbanArea");
        assertEquals(layer.getTitle(), "USGS Urban Areas Ortho-Imagery");
        

	}
	/* (non-Javadoc)
	 * @see org.geotools.data.wms.test.WMS1_0_0Test#checkProperties(java.util.Properties)
	 */
	protected void checkProperties(Properties properties) {
        assertEquals(properties.get("VERSION"), "1.1.1");
        assertEquals(properties.get("REQUEST"), "GetCapabilities");
        assertEquals(properties.get("SERVICE"), "WMS");
	}
	/* (non-Javadoc)
	 * @see org.geotools.data.wms.test.WMS1_0_0Test#parserCheck(org.geotools.data.wms.WMSParser)
	 */
	protected void parserCheck(WMSParser parser) {
		assertEquals(parser.getClass(), WMS1_1_1.Parser.class);
	}
}
