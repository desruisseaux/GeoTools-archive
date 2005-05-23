
package org.geotools.xml;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.logging.Level;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import junit.framework.TestCase;

import org.geotools.resources.TestData;
import org.xml.sax.SAXException;

import com.vividsolutions.xdo.Decoder;
import com.vividsolutions.xdo.PluginFinder;
import com.vividsolutions.xdo.xsi.Schema;


/**
 * <p>
 * DOCUMENT ME!
 * </p>
 * @
 *
 * @author dzwiers www.refractions.net
 */
public class SchemaParserTest extends TestCase {
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

    public void testMail() throws IOException, SAXException{
        runit("mails.xsd");
    }

    public void testWFS() throws IOException, SAXException{
        runit("wfs/WFS-basic.xsd");
    }

    public void testGMLFeature() throws IOException, SAXException{
        runit("gml/feature.xsd");
    }

    public void testGMLGeometry() throws IOException, SAXException{
        runit("gml/geometry.xsd");
    }

    public void testGMLXLinks() throws IOException{
        runit("gml/xlinks.xsd");
    }

    private void runit(String path) throws IOException{
        File f;

            f = TestData.file(this,path);
        URI u = f.toURI();
        Schema doc = PluginFinder.getInstance().getSchemaBuilder().find(u);
        assertNotNull(doc);
    }
}
