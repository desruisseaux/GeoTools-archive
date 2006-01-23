
package org.geotools.xml;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;

import junit.framework.TestCase;

import org.geotools.data.FeatureReader;
import org.geotools.feature.Feature;
import org.geotools.resources.TestData;
import org.geotools.xml.gml.FCBuffer;


/**
 * <p>
 * DOCUMENT ME!
 * </p>
 * @
 *
 * @author dzwiers www.refractions.net
 * @source $URL$
 */
public class GMLStreamingParserStressTest extends TestCase {

//    public void testOSDNFFeatures() throws SAXException, IOException {
//        FeatureReader fr = null;
//        try {
//
//            String path = "/home/dzwiers/testData/sample-master-map.xml";
//            File f = new File(path);
//            URI u = f.toURI();
//
//            XMLSAXHandler.setLogLevel(Level.FINEST);
//            XSISAXHandler.setLogLevel(Level.FINEST);
//            XMLElementHandler.setLogLevel(Level.FINEST);
//            XSIElementHandler.setLogLevel(Level.FINEST);
//            fr = FCBuffer.getFeatureReader(u,10,100000);
//            
//            assertNotNull("FeatureReader missing", fr);
//            
//            int i=0;
//            for(;fr.hasNext();i++){
//                System.out.println(fr.next());
//            }
//            
//            assertTrue("# features = "+i,i==70);
//            
//        } catch (Throwable e) {
//            e.printStackTrace();
//            fail(e.toString());
//        } finally {
//            if(fr!=null){
//                ((FCBuffer)fr).stop();
//            }
//        }
//    }

    public void testGTRoadsFeatures() throws IOException {
        FeatureReader fr = null;
        try {

            String path = "geoserver/roads.xml";
            File f = TestData.file(this,path);
            URI u = f.toURI();

            XMLSAXHandler.setLogLevel(Level.FINEST);
            XSISAXHandler.setLogLevel(Level.FINEST);
            XMLElementHandler.setLogLevel(Level.FINEST);
            XSIElementHandler.setLogLevel(Level.FINEST);
            fr = FCBuffer.getFeatureReader(u,10,10000);
            
            assertNotNull("FeatureReader missing", fr);
            
            int i=0;
            for(;fr.hasNext();i++){
//                System.out.println(fr.next());
                fr.next();
            }
            
            assertTrue("# features = "+i,i==70);
            
        } catch (Throwable e) {
            e.printStackTrace();
            fail(e.toString());
        } finally {
            if(fr!=null){
                ((FCBuffer)fr).close();
            }
        }
    }
    
    public void testFMERoadsFeatures() throws IOException {
        FeatureReader fr = null;
        try {
            String path = "fme/roads/roads.xml";
            File f = TestData.file(this,path);
            URI u = f.toURI();

            fr = FCBuffer.getFeatureReader(u,10,10000);
            
            assertNotNull("FeatureReader missing", fr);
            
            int i=0;
            for(;fr.hasNext();i++){
//                System.out.println(fr.next());
                fr.next();
            }
            
            assertTrue("# features "+i,i>20);
            System.out.println("\n # Features = "+i);
            
        } catch (Throwable e) {
            e.printStackTrace();
            fail(e.toString());
        } finally {
            if(fr!=null){
                ((FCBuffer)fr).close();
            }
        }
    }
    
    public void testFMELakesFeatures() throws IOException {
        FeatureReader fr = null;
        try {
            String path = "fme/lakes/lakes.xml";
            File f = TestData.file(this,path);
            URI u = f.toURI();

            fr = FCBuffer.getFeatureReader(u,10,10000);
            
            assertNotNull("FeatureReader missing", fr);
            
            int i=0;
            for(;fr.hasNext();i++){
//                System.out.println(fr.next());
                fr.next();
            }
            
            assertTrue("# features"+i,i>20);
            System.out.println("\n # Features = "+i);
            
        } catch (Throwable e) {
            e.printStackTrace();
            fail(e.toString());
        } finally {
            if(fr!=null){
                ((FCBuffer)fr).close();
            }
        }
    }
    
    public void testFME2StreamsFeatures() throws IOException {
        FeatureReader fr1 = null;
        FeatureReader fr2 = null;
        try {
            String path = "fme/lakes/lakes.xml";
            File f = TestData.file(this,path);
            URI u1 = f.toURI();

            path = "fme/roads/roads.xml";
            f = TestData.file(this,path);
            URI u2 = f.toURI();

            fr1 = FCBuffer.getFeatureReader(u1,10,10000);
            fr2 = FCBuffer.getFeatureReader(u2,10,10000);

            assertNotNull("FeatureReader missing", fr1);
            assertNotNull("FeatureReader missing", fr2);
            
            boolean cont = true;
            int count1,count2;
            count1 = count2 = 0;
            while(cont){
                cont = false;
                for(int i=0;i<10 && fr1.hasNext();i++){
                    Feature ftr = fr1.next();
                    assertTrue("Feature Null",ftr!=null);
//                    System.out.println(ftr);
                    cont = true;
                    count1++;
                }
                for(int i=0;i<10 && fr2.hasNext();i++){
                    Feature ftr = fr2.next();
                    assertTrue("Feature Null",ftr!=null);
//                    System.out.println(ftr);
                    cont = true;
                    count2++;
                }
            }
            assertTrue("Must have used both readers",(count1>20 && count2>20));
            System.out.println("\n# Features: "+count1+" , "+count2);
            
        } catch (Throwable e) {
            e.printStackTrace();
            fail(e.toString());
        } finally {
            if(fr1!=null){
                ((FCBuffer)fr1).close();
            }
            if(fr2!=null){
                ((FCBuffer)fr2).close();
            }
        }
    }
}
