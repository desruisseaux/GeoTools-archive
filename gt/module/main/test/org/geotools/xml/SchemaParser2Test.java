
package org.geotools.xml;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;

import junit.framework.TestCase;

import org.geotools.resources.TestData;
import org.geotools.xml.schema.Schema;


/**
 * <p>
 * DOCUMENT ME!
 * </p>
 * @
 *
 * @author dzwiers www.refractions.net
 */
public class SchemaParser2Test extends TestCase {
    //	public void testMail(){
    //		runit("","test/mails.xsd");
    //	}
    public void testWFS() throws URISyntaxException {
        runit(new URI("http://www.opengis.net/wfs"), "wfs/WFS-basic.xsd");
    }

    public void testGMLFeature() throws URISyntaxException {
        runit(new URI("http://www.opengis.net/gml"), "gml/feature.xsd");
    }

    public void testGMLGeometry() throws URISyntaxException {
        runit(new URI("http://www.opengis.net/gml"), "gml/geometry.xsd");
    }

    public void testGMLXLinks() throws URISyntaxException {
        runit(new URI("http://www.w3.org/1999/xlink"), "gml/xlinks.xsd");
    }

    private void runit(URI targetNS, String path) {
        Schema s = null;

        try {
            File f = TestData.file(this,path);
            s = SchemaFactory.getInstance(targetNS, f.toURI(),
                    Level.INFO);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.toString());
        }

        assertNotNull("Schema missing", s);
        System.out.println(s);

        Schema s2 = null;
        s2 = SchemaFactory.getInstance(targetNS);
        assertTrue("Should be the same", s == s2);
    }
}
