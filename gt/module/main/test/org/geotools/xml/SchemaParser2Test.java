
package org.geotools.xml;

import junit.framework.TestCase;

import org.geotools.resources.TestData;
import org.geotools.xml.SchemaFactory;
import org.geotools.xml.schema.Schema;
import java.io.File;
import java.util.logging.Level;


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
    public void testWFS() {
        runit("http://www.opengis.net/wfs", "wfs/WFS-basic.xsd");
    }

    public void testGMLFeature() {
        runit("http://www.opengis.net/gml", "gml/feature.xsd");
    }

    public void testGMLGeometry() {
        runit("http://www.opengis.net/gml", "gml/geometry.xsd");
    }

    public void testGMLXLinks() {
        runit("http://www.w3.org/1999/xlink", "gml/xlinks.xsd");
    }

    private void runit(String targetNS, String path) {
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
