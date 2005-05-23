
package org.geotools.xml;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.logging.Level;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.geotools.resources.TestData;
import org.geotools.xml.schema.Schema;
import org.xml.sax.SAXException;

import com.vividsolutions.xdo.Decoder;

import junit.framework.TestCase;

/**
 * @author dzwiers
 *
 */
public class SchemaMergeTest extends TestCase {

    protected SAXParser parser;

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();

        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setNamespaceAware(true);
        spf.setValidating(false);
        parser = spf.newSAXParser();
    }
    
	public void testMergeSchema() throws SAXException{
		// will load a doc that includes two schema docs which duplicate definitions
		

        File f;
        try {
            f = TestData.file(this,"merge.xsd");
	        URI u = f.toURI();

	        Schema schema = (Schema)Decoder.decode(u,new HashMap());
		        
	        assertTrue("Should only have 2 elements, had "+schema.getElements().length,schema.getElements().length == 2);
	        assertTrue("Should only have 1 complexType, had "+schema.getComplexTypes().length,schema.getComplexTypes().length == 1);
		        
        } catch (IOException e1) {
            e1.printStackTrace();
            fail(e1.toString());
        }
	}
}
