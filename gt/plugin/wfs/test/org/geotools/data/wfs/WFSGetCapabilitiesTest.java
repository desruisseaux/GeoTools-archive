package org.geotools.data.wfs;

import java.io.File;
import java.net.URI;
import java.util.logging.Level;

import junit.framework.TestCase;

import org.geotools.resources.TestData;
import org.geotools.xml.DocumentFactory;
import org.xml.sax.SAXException;


/**
 * <p>
 * DOCUMENT ME!
 * </p>
 * @
 *
 * @author dzwiers www.refractions.net
 * @source $URL$
 */
public class WFSGetCapabilitiesTest extends TestCase {
    public void testGeomatics(){
        try {            
            String path = "geomatics-wfs-getCapabilities.xml";

            File f = TestData.file(this,path);
            URI u = f.toURI();

            Object doc = DocumentFactory.getInstance(u,null,Level.WARNING);
            
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
    public void testMapServer(){
        try {            
            String path = "mswfs_gmap-getCapabilities.xml";

            File f = TestData.file(this,path);
            URI u = f.toURI();

            Object doc = DocumentFactory.getInstance(u,null,Level.WARNING);
            
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
    public void testGaldos(){
       try {            
           String path = "galdos-http-getCapabilities.xml";

           File f = TestData.file(this,path);
           URI u = f.toURI();

           Object doc = DocumentFactory.getInstance(u,null,Level.WARNING);
           
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
    public void testIonic(){
       try {            
           String path = "ionic-wfs-getCapabilities.xml";

           File f = TestData.file(this,path);
           URI u = f.toURI();

           Object doc = DocumentFactory.getInstance(u,null,Level.WARNING);
           
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
