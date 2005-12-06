/*
 * XMLEncoderTest.java
 * JUnit based test
 *
 * Created on 01 August 2003, 16:46
 */

package org.geotools.styling;

import java.io.StringWriter;
import java.util.logging.Logger;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 *
 * @author iant
 */
public class XMLEncoderTest extends TestCase {
    protected static final Logger LOGGER = Logger.getLogger(
            "org.geotools.styling");
    public XMLEncoderTest(java.lang.String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(XMLEncoderTest.class);
        return suite;
    }
    
    // Add test methods here, they have to start with 'test' name.
    // for example:
    // public void testHello() {}
    
    public void testSimpleEncoder() throws Exception{

        StyleFactory factory = StyleFactoryFinder.createStyleFactory();
        
        java.net.URL surl = org.geotools.resources.TestData.getResource(this, "markTest.sld");
        LOGGER.info("reading "+surl);
        SLDParser stylereader = new SLDParser(factory,surl);
        Style[] style = stylereader.readXML();
        StringWriter output = new StringWriter();
//        Writer output = new PrintWriter(System.out); 
                XMLEncoder encode = new XMLEncoder(output, style[0]);
                LOGGER.info("Resulting SLD is \n" +
                    output.getBuffer().toString());
                String result = output.getBuffer().toString();
        
                result.replaceAll("\n","");
                result.replaceAll("\t","");
        assertTrue(result.indexOf("<FeatureTypeName>testPoint</FeatureTypeName>")>0);
        
    
    }
    public void testMarkDisplacment() throws Exception{

        StyleFactory factory = StyleFactoryFinder.createStyleFactory();
                                                                               
        java.net.URL surl = org.geotools.resources.TestData.getResource(this, "markDisplacementTest.sld");
        LOGGER.info("reading "+surl);
        SLDParser stylereader = new SLDParser (factory,surl);
        Style[] style = stylereader.readXML();
        StringWriter output = new StringWriter();
//        Writer output = new PrintWriter(System.out); 
                XMLEncoder encode = new XMLEncoder(output, style[0]);
                LOGGER.info("Resulting SLD is \n" +
                    output.getBuffer().toString());
                String result = output.getBuffer().toString();
        
                result.replaceAll("\n","");
                result.replaceAll("\t","");
        assertTrue(result.indexOf("<FeatureTypeName>testPoint</FeatureTypeName>")>0);
        
    
    }
    public void testComplexEncoder() throws Exception{
        
        StyleFactory factory = StyleFactoryFinder.createStyleFactory();
       
        java.net.URL surl = org.geotools.resources.TestData.getResource(this, "sample.sld");
        LOGGER.info("reading "+surl);
        SLDParser stylereader = new SLDParser(factory,surl);
        Style[] style = stylereader.readXML();
        StringWriter output = new StringWriter();
//        Writer output = new PrintWriter(System.out); 
                XMLEncoder encode = new XMLEncoder(output, style[0]);
                LOGGER.info("Resulting SLD is \n" +
                    output.getBuffer().toString());
    
    }
    public void testComplexTextEncoder() throws Exception{
        StyleFactory factory = StyleFactoryFinder.createStyleFactory();
        java.net.URL surl = org.geotools.resources.TestData.getResource(this, "textTest.sld");
        
        LOGGER.info("reading "+surl);
        SLDParser stylereader = new SLDParser(factory,surl);
        Style[] style = stylereader.readXML();
        StringWriter output = new StringWriter();
//        Writer output = new PrintWriter(System.out); 
                XMLEncoder encode = new XMLEncoder(output, style[0]);
                LOGGER.info("Resulting SLD is \n" +
                    output.getBuffer().toString());
    
    }
     
}
