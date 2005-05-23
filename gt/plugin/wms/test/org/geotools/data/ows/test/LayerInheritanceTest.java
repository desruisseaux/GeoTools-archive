package org.geotools.data.ows.test;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import junit.framework.TestCase;

import org.geotools.data.ows.Layer;
import org.geotools.data.ows.WMSCapabilities;
import org.geotools.data.wms.xml.WMSSchema;
import org.geotools.resources.TestData;
import org.geotools.xml.DocumentFactory;
import org.geotools.xml.SchemaFactory;
import org.geotools.xml.schema.Schema;

public class LayerInheritanceTest extends TestCase {
    
	public void testInheritCapabilities() throws Exception {
		
		File getCaps = TestData.file(this, "inheritCap.xml");
      URL getCapsURL = getCaps.toURL();

      Map hints = new HashMap();
//      hints.put(Decoder.DEFAULT_NAMESPACE_HINT_KEY, WMSSchema.getInstance());
		Object object = DocumentFactory.getInstance(getCapsURL.openStream(),hints, Level.FINE);

      Schema schema = WMSSchema.getInstance();
		SchemaFactory.getInstance(WMSSchema.NAMESPACE);
				
		assertTrue("Capabilities failed to parse", object instanceof WMSCapabilities);
		
		WMSCapabilities capabilities = (WMSCapabilities) object;
		
		// Get first test layer, it's nested 3 deep
		Layer layer = (Layer) capabilities.getLayerList().get(2);
		assertNotNull(layer);
		assertNotNull(layer.getParent());
		
		// Should be false by default since not specified in layer or ancestors
		assertFalse(layer.isQueryable());
		assertEquals(layer.getTitle(), "Coastlines");
		
		// Should be 5 total after accumulating all ancestors
		assertEquals(layer.getSrs().size(), 5);
		assertTrue(layer.getSrs().contains("EPSG:26906"));
		assertTrue(layer.getSrs().contains("EPSG:26905"));
		assertTrue(layer.getSrs().contains("EPSG:4326"));
		assertTrue(layer.getSrs().contains("AUTO:42003"));
		assertTrue(layer.getSrs().contains("AUTO:42005"));
		
		// 2 total, this layer plus top most layer
		assertEquals(layer.getStyles().size(), 2);
		assertTrue(layer.getStyles().contains("TestStyle"));
		assertTrue(layer.getStyles().contains("default"));
		
		// Next test layer, nested 3 deep but different path
		layer = (Layer) capabilities.getLayerList().get(4);
		assertNotNull(layer);
		assertNotNull(layer.getParent());
		
		// Should be true by default since inherited from parent
		assertEquals(layer.getName(), "RTOPO");
		assertTrue(layer.isQueryable());
	}
}
