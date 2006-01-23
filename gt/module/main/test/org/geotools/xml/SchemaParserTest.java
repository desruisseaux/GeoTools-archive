
package org.geotools.xml;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import junit.framework.TestCase;

import org.geotools.resources.TestData;


/**
 * <p>
 * DOCUMENT ME!
 * </p>
 * @
 *
 * @author dzwiers www.refractions.net
 * @source $URL$
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

    public void testMail(){
        runit("mails.xsd");
    }

    public void testWFS(){
        runit("wfs/WFS-basic.xsd");
    }

    public void testGMLFeature(){
        runit("gml/feature.xsd");
    }

    public void testGMLGeometry(){
        runit("gml/geometry.xsd");
    }

    public void testGMLXLinks(){
        runit("gml/xlinks.xsd");
    }

    private void runit(String path){
        File f;
        try {
            f = TestData.file(this,path);
        URI u = f.toURI();
        XSISAXHandler contentHandler = new XSISAXHandler(u);
//        XSISAXHandler.setLogLevel(Level.INFO);
        XSISAXHandler.setLogLevel(Level.WARNING);

        try {
            parser.parse(f, contentHandler);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.toString());
        }

        try{
            assertNotNull("Schema missing", contentHandler.getSchema());
            System.out.println(contentHandler.getSchema());
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.toString());
        }
        } catch (IOException e1) {
            e1.printStackTrace();
            fail(e1.toString());
        }
    }
}
