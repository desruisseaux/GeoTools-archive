
package org.geotools.xml;

import junit.framework.TestCase;

import org.geotools.resources.TestData;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;


/**
 * <p>
 * Big dataset tests ... more than you want for regular testing
 * </p>
 * @
 *
 * @author dzwiers www.refractions.net
 */
public class GMLParserTester2 extends TestCase {
//    public void testFMEPostalFeatures() throws SAXException, IOException {
//        try {
//            SAXParserFactory spf = SAXParserFactory.newInstance();
//            spf.setNamespaceAware(true);
//            spf.setValidating(false);
//
//            SAXParser parser = spf.newSAXParser();
//
//            String path = "fme/postal/postal.gml";
//            File f = TestData.file(this,path);
//            URI u = f.toURI();
//
//            XMLSAXHandler xmlContentHandler = new XMLSAXHandler(u,null);
//            XMLSAXHandler.setLogLevel(Level.WARNING);
//            XSISAXHandler.setLogLevel(Level.WARNING);
//            XMLElementHandler.setLogLevel(Level.WARNING);
//            XSIElementHandler.setLogLevel(Level.WARNING);
//
//            parser.parse(f, xmlContentHandler);
//
//            Object doc = xmlContentHandler.getDocument();
//            assertNotNull("Document missing", doc);
//            System.out.println(doc);
//            
//            Object[] objs = (Object[])doc;
//            
//            assertTrue("Should have 95054 features + 1 bbox : "+objs.length,objs.length == 95055);
//                        
//        } catch (Throwable e) {
//            e.printStackTrace();
//            fail(e.toString());
//        }
//    }
//    
//    public void testFMEFedCenFeatures() throws SAXException, IOException {
//        try {
//            SAXParserFactory spf = SAXParserFactory.newInstance();
//            spf.setNamespaceAware(true);
//            spf.setValidating(false);
//
//            SAXParser parser = spf.newSAXParser();
//
//            String path = "fme/fed-cen/fed308_a.gml";
//            File f = TestData.file(this,path);
//            URI u = f.toURI();
//
//            XMLSAXHandler xmlContentHandler = new XMLSAXHandler(u,null);
//            XMLSAXHandler.setLogLevel(Level.WARNING);
//            XSISAXHandler.setLogLevel(Level.WARNING);
//            XMLElementHandler.setLogLevel(Level.WARNING);
//            XSIElementHandler.setLogLevel(Level.WARNING);
//
//            parser.parse(f, xmlContentHandler);
//
//            Object doc = xmlContentHandler.getDocument();
//            assertNotNull("Document missing", doc);
//            System.out.println(doc);
//            
//            Object[] objs = (Object[])doc;
//            
//            assertTrue("Should have N features + 1 bbox : "+objs.length,objs.length > 2);
//            
//        } catch (Throwable e) {
//            e.printStackTrace();
//            fail(e.toString());
//        }
//    }
//    
//    public void testFMEForestFeatures() throws SAXException, IOException {
//        try {
//            SAXParserFactory spf = SAXParserFactory.newInstance();
//            spf.setNamespaceAware(true);
//            spf.setValidating(false);
//
//            SAXParser parser = spf.newSAXParser();
//
//            String path = "fme/forest_districts/forest-districts-2003-04.gml";
//            File f = TestData.file(this,path);
//            URI u = f.toURI();
//
//            XMLSAXHandler xmlContentHandler = new XMLSAXHandler(u,null);
//            XMLSAXHandler.setLogLevel(Level.WARNING);
//            XSISAXHandler.setLogLevel(Level.WARNING);
//            XMLElementHandler.setLogLevel(Level.WARNING);
//            XSIElementHandler.setLogLevel(Level.WARNING);
//
//            parser.parse(f, xmlContentHandler);
//
//            Object doc = xmlContentHandler.getDocument();
//            assertNotNull("Document missing", doc);
//            System.out.println(doc);
//            
//            Object[] objs = (Object[])doc;
//            
//            assertTrue("Should have N features + 1 bbox : "+objs.length,objs.length > 2);
//            
//        } catch (Throwable e) {
//            e.printStackTrace();
//            fail(e.toString());
//        }
//    }
//    
//    public void testFMEVictoriaFeatures() throws SAXException, IOException {
//        try {
//            SAXParserFactory spf = SAXParserFactory.newInstance();
//            spf.setNamespaceAware(true);
//            spf.setValidating(false);
//
//            SAXParser parser = spf.newSAXParser();
//
//            String path = "fme/victoria/victoria.gml";
//            File f = TestData.file(this,path);
//            URI u = f.toURI();
//
//            XMLSAXHandler xmlContentHandler = new XMLSAXHandler(u,null);
//            XMLSAXHandler.setLogLevel(Level.WARNING);
//            XSISAXHandler.setLogLevel(Level.WARNING);
//            XMLElementHandler.setLogLevel(Level.WARNING);
//            XSIElementHandler.setLogLevel(Level.WARNING);
//
//            parser.parse(f, xmlContentHandler);
//
//            Object doc = xmlContentHandler.getDocument();
//            assertNotNull("Document missing", doc);
//            System.out.println(doc);
//            
//            Object[] objs = (Object[])doc;
//            
//            assertTrue("Should have N features + 1 bbox : "+objs.length,objs.length > 2);
//            
//        } catch (Throwable e) {
//            e.printStackTrace();
//            fail(e.toString());
//        }
//    }
}
