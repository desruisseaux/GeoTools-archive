
package org.geotools.xml;

import java.io.File;
import java.net.URI;
import java.util.logging.Level;

import javax.naming.OperationNotSupportedException;

import junit.framework.TestCase;

import org.geotools.TestData;
import org.geotools.xml.schema.Schema;
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
public class XMLParserTest extends TestCase {
    public void testMail(){
        try {            
            String path = "xml/mails.xml";

            File f = TestData.copy(this,path);
            TestData.copy(this,"xml/mails.xsd");
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
        String path = "xml/mails.xml";

        File f = TestData.copy(this,path);
        TestData.copy(this,"xml/mails.xsd");

        Object doc = DocumentFactory.getInstance(f.toURI(),null,Level.WARNING);
        assertNotNull("Document missing", doc);

        Schema s = SchemaFactory.getInstance(new URI("http://mails/refractions/net"));
                
        path = "mails_out.xml";
        f = TestData.temp(this, path);
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
        	assertTrue(e instanceof OperationNotSupportedException);
//            e.printStackTrace();
//            fail(e.toString()); Operation not supported yet
        }
    }
}
