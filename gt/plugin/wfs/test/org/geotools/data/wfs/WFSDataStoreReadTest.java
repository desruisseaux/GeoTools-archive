
package org.geotools.data.wfs;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.TestCase;

import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureReader;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.feature.FeatureType;
import org.geotools.feature.IllegalAttributeException;
import org.xml.sax.SAXException;

/**
 * <p> 
 * DOCUMENT ME!
 * </p>
 * @author dzwiers
 *
 */
public class WFSDataStoreReadTest extends TestCase {
    
    public WFSDataStoreReadTest(){
        Logger.global.setLevel(Level.SEVERE);
    }
    
    public void testEmpty(){/**/}

    public static WFSDataStore getDataStore(URL server) throws IOException{
        try{
        Map m = new HashMap();
        m.put(WFSDataStoreFactory.URL.key,server);
        m.put(WFSDataStoreFactory.TIMEOUT.key,new Integer(10000)); // was 1000000 for debug
        return (WFSDataStore)(new WFSDataStoreFactory()).createNewDataStore(m);

        }catch(java.net.SocketException se){
            se.printStackTrace();
            return null;
        }
    }
        
    public static void doFeatureType(URL url,boolean get, boolean post, int i) throws IOException, SAXException{
        try{
        WFSDataStore wfs = getDataStore(url);
        System.out.println("FeatureTypeTest + "+url);
        assertNotNull("No featureTypes",wfs.getTypeNames());
        assertNotNull("Null featureType in ["+i+"]",wfs.getTypeNames()[i]);
        System.out.println("FT name = "+wfs.getTypeNames()[i]);
        if(get){
            // get
            FeatureType ft = wfs.getSchemaGet(wfs.getTypeNames()[i]);
            assertNotNull("FeatureType was null",ft);
            assertTrue(wfs.getTypeNames()[i]+" must have 1 geom and atleast 1 other attribute -- fair assumption",ft.getDefaultGeometry()!=null && ft.getAttributeTypes()!=null && ft.getAttributeCount()>0);
        }
        if(post){
            // post
            FeatureType ft = wfs.getSchemaPost(wfs.getTypeNames()[i]);
            assertNotNull("FeatureType was null",ft);
            assertTrue("must have 1 geom and atleast 1 other attribute -- fair assumption",ft.getDefaultGeometry()!=null && ft.getAttributeTypes()!=null && ft.getAttributeCount()>0);
        }
        }catch(java.net.SocketException se){
            se.printStackTrace();
        }
    }
    
    public static void doFeatureReader(URL url, boolean get, boolean post, int i) throws NoSuchElementException, IOException, IllegalAttributeException, SAXException{
        try{
        System.out.println("FeatureReaderTest + "+url);
        WFSDataStore wfs = getDataStore(url);
        assertNotNull("No featureTypes",wfs.getTypeNames());
        assertNotNull("Null featureType in [0]",wfs.getTypeNames()[i]);
        Query query = new DefaultQuery(wfs.getTypeNames()[i]);
        
        if(post){
        // 	post
            FeatureReader ft = wfs.getFeatureReaderPost(query,Transaction.AUTO_COMMIT);
            assertNotNull("FeatureType was null",ft);
            assertTrue("must have 1 feature -- fair assumption",ft.hasNext() && ft.getFeatureType()!=null && ft.next()!=null);
            // disable for now
//            assertNotNull("CRS missing ",ft.getFeatureType().getDefaultGeometry().getCoordinateSystem());
            ft.close();
        }
        if(get){
        // 	get
            FeatureReader ft = wfs.getFeatureReaderGet(query,Transaction.AUTO_COMMIT);
            assertNotNull("FeatureType was null",ft);
            assertTrue("must have 1 feature -- fair assumption",ft.hasNext() && ft.getFeatureType()!=null && ft.next()!=null);
            // disable for now
//            assertNotNull("CRS missing ",ft.getFeatureType().getDefaultGeometry().getCoordinateSystem());
            ft.close();}
        }catch(java.net.SocketException se){
            se.printStackTrace();
        }
    }
    
    public static void doFeatureReaderWithFilter(URL url, boolean get, boolean post, int i) throws NoSuchElementException, IllegalAttributeException, IOException, SAXException{
        try{
        System.out.println("FeatureReaderWithFilterTest + "+url);
        WFSDataStore wfs = getDataStore(url);
        assertNotNull("No featureTypes",wfs.getTypeNames());
        assertNotNull("Null featureType in [0]",wfs.getTypeNames()[i]);
        FeatureType ft = wfs.getSchema(wfs.getTypeNames()[i]);
        // take atleast attributeType 3 to avoid the undeclared one .. inherited optional attrs
        
        String[] props;
        if(ft.getAttributeCount()==1)
            props = new String[] {ft.getDefaultGeometry().getName()};
        else
            props = new String[] {ft.getDefaultGeometry().getName(), ft.getAttributeType(ft.getAttributeCount()-1).getName()};
        
        DefaultQuery query = new DefaultQuery(ft.getTypeName());
        query.setPropertyNames(props);
        
        if(get){
            // 	get
            FeatureReader fr = wfs.getFeatureReaderGet(query,Transaction.AUTO_COMMIT);
            assertNotNull("FeatureType was null",ft);
            assertTrue("must have 1 feature -- fair assumption",fr.hasNext() && fr.getFeatureType()!=null && fr.next()!=null);
            int j=0;while(fr.hasNext()){fr.next();j++;}
            System.out.println(j+" Features");
            fr.close();
        }if(post){
            // 	post

            FeatureReader fr = wfs.getFeatureReaderPost(query,Transaction.AUTO_COMMIT);
            assertNotNull("FeatureType was null",ft);
            assertTrue("must have 1 feature -- fair assumption",fr.hasNext() && fr.getFeatureType()!=null && fr.next()!=null);
            int j=0;while(fr.hasNext()){fr.next();j++;}
            System.out.println(j+" Features");
            fr.close();
        }
        }catch(java.net.SocketException se){
            se.printStackTrace();
        }
    }
}
