package org.geotools.data.wms.xml.test;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.TestCase;

import org.geotools.data.wms.xml.WMSSchema;
import org.geotools.resources.TestData;
import org.geotools.xml.DocumentFactory;
import org.geotools.xml.SchemaFactory;
import org.geotools.xml.schema.Schema;

public class WMSSchemaTest extends TestCase {
    
    public void testSchema() throws URISyntaxException{
        Schema v1 = SchemaFactory.getInstance(new URI("http://www.opengis.net/wms"));
        assertNotNull(v1);
        Schema v2 = WMSSchema.getInstance();
        assertNotNull(v2);
        assertEquals(v1,v2);
    }

	public void testInstance() throws Exception {
		
		File getCaps = TestData.file(this, "1.3.0Capabilities.xml");
        URL getCapsURL = getCaps.toURL();

		Object thing = DocumentFactory.getInstance(getCapsURL.toURI(), null, Level.FINE);

        
		Schema schema = WMSSchema.getInstance();
		SchemaFactory.getInstance(WMSSchema.NAMESPACE);
		
		
		System.out.println(thing.getClass());
	}
}
