
package org.geotools.xml;

import junit.framework.TestCase;

import org.geotools.resources.TestData;
import org.geotools.xml.schema.Schema;
import org.xml.sax.SAXException;
import java.io.File;
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
public class XMLParserTest extends TestCase {
    public void testMail(){
        try {            
            String path = "mails.xml";

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
    
    public void testMailWrite(){

        try {            
        String path = "mails.xml";

        File f = TestData.file(this,path);

        Object doc = DocumentFactory.getInstance(f.toURI(),null,Level.WARNING);
        assertNotNull("Document missing", doc);

        Schema s = SchemaFactory.getInstance("http://mails/refractions/net");
                
        path = "mails_out.xml";
        f = new File(f.getParentFile(),path);
        if(f.exists())
            f.delete();
        f.createNewFile();
        
        DocumentWriter.writeDocument(doc,s,f,null);
        
        doc = DocumentFactory.getInstance(f.toURI(),null,Level.WARNING);
        assertNotNull("New Document missing", doc);
        
        assertTrue("file was not created +f",f.exists());
        System.out.println(f);
        } catch (SAXException e) {
            e.printStackTrace();
            fail(e.toString());
        } catch (Throwable e) {
            e.printStackTrace();
//            fail(e.toString()); Operation not supported yet
        }
    }
}
