
package org.geotools.xml;

import junit.framework.TestCase;

import org.geotools.resources.TestData;
import org.xml.sax.SAXException;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;


/**
 * <p>
 * DOCUMENT ME!
 * </p>
 * @
 *
 * @author dzwiers www.refractions.net
 */
public class XMLParserTester extends TestCase {
    public void testMail() throws SAXException, IOException {
        try {            
            String path = "mails.xml";

            File f = TestData.file(this,path);
            URI u = f.toURI();

            Object doc = DocumentFactory.getInstance(u,null,Level.FINEST);
            
            assertNotNull("Document missing", doc);
            System.out.println(doc);
        } catch (SAXException e) {
            e.printStackTrace();
            fail(e.toString());
        } catch (Throwable e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }
}
