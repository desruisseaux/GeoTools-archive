
package org.geotools.xml;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.logging.Level;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import junit.framework.TestCase;

import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.resources.TestData;
import org.geotools.xml.gml.GMLFeatureCollection;
import org.geotools.xml.gml.GMLSchema;
import org.geotools.xml.schema.Schema;
import org.xml.sax.SAXException;


/**
 * <p>
 * DOCUMENT ME!
 * </p>
 * @
 *
 * @author dzwiers www.refractions.net
 */
public class GMLParserTest extends TestCase {
    public void testSchema() throws SAXException, IOException {
        Schema s = SchemaFactory.getInstance(GMLSchema.NAMESPACE);
        assertNotNull(s);
    }
    
    public void testOneFeature() throws SAXException, IOException {
        try {
            SAXParserFactory spf = SAXParserFactory.newInstance();
            spf.setNamespaceAware(true);
            spf.setValidating(false);

            SAXParser parser = spf.newSAXParser();

            String path = "geoserver/oneFeature.xml";
            File f = TestData.file(this,path);
            URI u = f.toURI();

            XMLSAXHandler xmlContentHandler = new XMLSAXHandler(u,null);
            XMLSAXHandler.setLogLevel(Level.FINEST);
            XSISAXHandler.setLogLevel(Level.FINEST);
            XMLElementHandler.setLogLevel(Level.FINEST);
            XSIElementHandler.setLogLevel(Level.FINEST);

            parser.parse(f, xmlContentHandler);

            Object doc = xmlContentHandler.getDocument();
            assertNotNull("Document missing", doc);
//            System.out.println(doc);
            
            checkFeatureCollection((FeatureCollection)doc);
            
        } catch (Throwable e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }
    public void testMoreFeatures() throws SAXException, IOException {
        try {
            SAXParserFactory spf = SAXParserFactory.newInstance();
            spf.setNamespaceAware(true);
            spf.setValidating(false);

            SAXParser parser = spf.newSAXParser();

            String path = "geoserver/roads.xml";
            File f = TestData.file(this,path);
            URI u = f.toURI();

            XMLSAXHandler xmlContentHandler = new XMLSAXHandler(u,null);
            XMLSAXHandler.setLogLevel(Level.WARNING);
            XSISAXHandler.setLogLevel(Level.WARNING);
            XMLElementHandler.setLogLevel(Level.WARNING);
            XSIElementHandler.setLogLevel(Level.WARNING);

            parser.parse(f, xmlContentHandler);

            Object doc = xmlContentHandler.getDocument();
            assertNotNull("Document missing", doc);
//            System.out.println(doc);
            
            checkFeatureCollection((FeatureCollection)doc);
            
        } catch (Throwable e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }
    
    public void testFMERoadsFeatures() throws SAXException, IOException {
        try {
            SAXParserFactory spf = SAXParserFactory.newInstance();
            spf.setNamespaceAware(true);
            spf.setValidating(false);

            SAXParser parser = spf.newSAXParser();

            String path = "fme/roads/roads.xml";
            File f = TestData.file(this,path);
            URI u = f.toURI();

            XMLSAXHandler xmlContentHandler = new XMLSAXHandler(u,null);
            XMLSAXHandler.setLogLevel(Level.WARNING);
            XSISAXHandler.setLogLevel(Level.WARNING);
            XMLElementHandler.setLogLevel(Level.WARNING);
            XSIElementHandler.setLogLevel(Level.WARNING);

            parser.parse(f, xmlContentHandler);

            Object doc = xmlContentHandler.getDocument();
            assertNotNull("Document missing", doc);
//            System.out.println(doc);

            checkFeatureCollection((FeatureCollection)doc);
            
        } catch (Throwable e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }
    
    public void testFMELakesFeatures() throws SAXException, IOException {
        try {
            SAXParserFactory spf = SAXParserFactory.newInstance();
            spf.setNamespaceAware(true);
            spf.setValidating(false);

            SAXParser parser = spf.newSAXParser();

            String path = "fme/lakes/lakes.xml";
            File f = TestData.file(this,path);
            URI u = f.toURI();

            XMLSAXHandler xmlContentHandler = new XMLSAXHandler(u,null);
            XMLSAXHandler.setLogLevel(Level.WARNING);
            XSISAXHandler.setLogLevel(Level.WARNING);
            XMLElementHandler.setLogLevel(Level.WARNING);
            XSIElementHandler.setLogLevel(Level.WARNING);

            parser.parse(f, xmlContentHandler);

            Object doc = xmlContentHandler.getDocument();
            assertNotNull("Document missing", doc);
//            System.out.println(doc);
            
            checkFeatureCollection((FeatureCollection)doc);
            
        } catch (Throwable e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }
    
    private void checkFeatureCollection(FeatureCollection doc){
               
        //remaining slot (s) should be feature(s)
        assertTrue("Requires atleast one feature",doc.size()>0);  //bbox + feature
        FeatureIterator i = doc.features();
        int j = 1;
        while(i.hasNext()){
            Feature ft = (Feature)i.next();;
            assertNotNull("Feature #"+j+" is null",ft);
//            System.out.println("Feature "+j+" : "+ft);
            j++;
        }
        System.out.println("Found "+j+" Features");
    }
    public void testOneFeatureWrite(){

        try {            
        String path = "geoserver/oneFeature.xml";

        File f = TestData.file(this,path);

        GMLFeatureCollection doc = (GMLFeatureCollection)DocumentFactory.getInstance(f.toURI(),null,Level.WARNING);
        assertNotNull("Document missing", doc);

        Schema s = SchemaFactory.getInstance(new URI("http://www.openplans.org/topp"));
                
        path = "oneFeature_out.xml";
        f = new File(f.getParentFile(),path);
        if(f.exists())
            f.delete();
        f.createNewFile();
        
        assertNotNull("Bounds exists",doc.getBounds());
        DocumentWriter.writeDocument(doc,s,f,null);
        
//        doc = (GMLFeatureCollection)DocumentFactory.getInstance(f.toURI(),null,Level.WARNING);
//        assertNotNull("New Document missing", doc);
//        
//        assertTrue("file was not created +f",f.exists());
        System.out.println(f);
        } catch (SAXException e) {
            e.printStackTrace();
            fail(e.toString());
        } catch (Throwable e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }
    public void testOneFeatureWriteWithHints(){

        try {            
        String path = "geoserver/oneFeature.xml";

        File f = TestData.file(this,path);

        GMLFeatureCollection doc = (GMLFeatureCollection)DocumentFactory.getInstance(f.toURI(),null,Level.WARNING);
        assertNotNull("Document missing", doc);

        Schema s = SchemaFactory.getInstance(new URI("http://www.openplans.org/topp"));
                
        path = "oneFeature_out_hints.xml";
        f = new File(f.getParentFile(),path);
        if(f.exists())
            f.delete();
        f.createNewFile();
        
        HashMap hints = new HashMap();
        hints.put(DocumentWriter.SCHEMA_ORDER,new String[] {"http://www.opengis.net/wfs", "http://www.openplans.org/topp"});
        assertNotNull("Bounds exists",doc.getBounds());
        DocumentWriter.writeDocument(doc,s,f,hints);
        
//        doc = (GMLFeatureCollection)DocumentFactory.getInstance(f.toURI(),null,Level.WARNING);
//        assertNotNull("New Document missing", doc);
//        
//        assertTrue("file was not created +f",f.exists());
        System.out.println(f);
        } catch (SAXException e) {
            e.printStackTrace();
            fail(e.toString());
        } catch (Throwable e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }
    
    public void testProblemFeatures() throws SAXException, IOException {
       try {
           SAXParserFactory spf = SAXParserFactory.newInstance();
           spf.setNamespaceAware(true);
           spf.setValidating(false);

           SAXParser parser = spf.newSAXParser();

           String path = "iba-gml-bad.xml";
           File f = TestData.file(this,path);
           URI u = f.toURI();

           XMLSAXHandler xmlContentHandler = new XMLSAXHandler(u,null);
           XMLSAXHandler.setLogLevel(Level.WARNING);
           XSISAXHandler.setLogLevel(Level.WARNING);
           XMLElementHandler.setLogLevel(Level.WARNING);
           XSIElementHandler.setLogLevel(Level.WARNING);

           parser.parse(f, xmlContentHandler);

           Object doc = xmlContentHandler.getDocument();
           assertNotNull("Document missing", doc);
//           System.out.println(doc);
           
           checkFeatureCollection((FeatureCollection)doc);
           fail("Didn't catch an exception :(");
       } catch (Throwable e) {
           e.printStackTrace();
       }
   }
}
