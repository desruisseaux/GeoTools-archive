
package org.geotools.data.wfs;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.TestCase;

import org.geotools.data.DataStore;
import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureReader;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.feature.FeatureType;
import org.geotools.feature.IllegalAttributeException;

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

    private DataStore getDataStore(URL server, boolean isPost) throws IOException{
        Map m = new HashMap();
        m.put(WFSDataStoreFactory.GET_CAPABILITIES_URL.key,server);
        if(isPost)
            m.put(WFSDataStoreFactory.USE_POST.key,Boolean.TRUE);
        else
            m.put(WFSDataStoreFactory.USE_GET.key,Boolean.TRUE);
        return (new WFSDataStoreFactory()).createNewDataStore(m);
    }
    
    public void testGaldos() throws NoSuchElementException, IOException, IllegalAttributeException{
        URL url = new URL("http://wfs.galdosinc.com:8680/wfs/http?Request=GetCapabilities&service=WFS");
        System.out.println("\nGaldos");
        doFeatureType(url);
        doFeatureReader(url);
        doFeatureReaderWithFilter(url);
        System.out.println("");
    }
    
    public void testGeomatics() throws NoSuchElementException, IOException, IllegalAttributeException{
        URL url = new URL("http://gws2.pcigeomatics.com/wfs1.0.0/wfs?service=WFS&request=getcapabilities");
        System.out.println("\nGeomatics");
        doFeatureType(url);
        doFeatureReader(url);
        doFeatureReaderWithFilter(url);
        System.out.println("");
    }
    
    public void testMapServer() throws NoSuchElementException, IOException, IllegalAttributeException{
        URL url = new URL("http://www2.dmsolutions.ca/cgi-bin/mswfs_gmap?version=1.0.0&request=getcapabilities&service=wfs");
        System.out.println("\nMapServer");
        doFeatureType(url);
        doFeatureReader(url);
        doFeatureReaderWithFilter(url);
        System.out.println("");
    }
    
    public void doFeatureType(URL url) throws IOException{
        DataStore wfs = null;
        
        // get
        System.out.println("Get FeatureTypeTest");
        wfs = getDataStore(url,false);
        assertNotNull("No featureTypes",wfs.getTypeNames());
        assertNotNull("Null featureType in [0]",wfs.getTypeNames()[0]);
        System.out.println("FT name = "+wfs.getTypeNames()[0]);
        FeatureType ft = wfs.getSchema(wfs.getTypeNames()[0]);
        assertNotNull("FeatureType was null",ft);
        assertTrue("must have 1 geom and atleast 1 other attribute -- fair assumption",ft.getDefaultGeometry()!=null && ft.getAttributeTypes()!=null && ft.getAttributeCount()>0);
        
        // post
        System.out.println("Post FeatureTypeTest");
        wfs = getDataStore(url,true);
        assertNotNull("No featureTypes",wfs.getTypeNames());
        assertNotNull("Null featureType in [0]",wfs.getTypeNames()[0]);
        System.out.println("FT name = "+wfs.getTypeNames()[0]);
        ft = wfs.getSchema(wfs.getTypeNames()[0]);
        assertNotNull("FeatureType was null",ft);
        assertTrue("must have 1 geom and atleast 1 other attribute -- fair assumption",ft.getDefaultGeometry()!=null && ft.getAttributeTypes()!=null && ft.getAttributeCount()>0);
    }
    
    public void doFeatureReader(URL url) throws NoSuchElementException, IOException, IllegalAttributeException{
        DataStore wfs = null;
    
        // 	get
        System.out.println("Get FeatureReaderTest");
        wfs = getDataStore(url,false);
        assertNotNull("No featureTypes",wfs.getTypeNames());
        assertNotNull("Null featureType in [0]",wfs.getTypeNames()[0]);
        Query query = new DefaultQuery(wfs.getTypeNames()[0]);
        FeatureReader ft = wfs.getFeatureReader(query,Transaction.AUTO_COMMIT);
        assertNotNull("FeatureType was null",ft);
        assertTrue("must have 1 feature -- fair assumption",ft.hasNext() && ft.getFeatureType()!=null && ft.next()!=null);
    
        // 	post
        System.out.println("Post FeatureReaderTest");
        wfs = getDataStore(url,true);
        assertNotNull("No featureTypes",wfs.getTypeNames());
        assertNotNull("Null featureType in [0]",wfs.getTypeNames()[0]);
        query = new DefaultQuery(wfs.getTypeNames()[0]);
        ft = wfs.getFeatureReader(query,Transaction.AUTO_COMMIT);
        assertNotNull("FeatureType was null",ft);
        assertTrue("must have 1 feature -- fair assumption",ft.hasNext() && ft.getFeatureType()!=null && ft.next()!=null);
    }
    
    public void doFeatureReaderWithFilter(URL url) throws NoSuchElementException, IOException, IllegalAttributeException{
        DataStore wfs = null;
    
        // 	get
        System.out.println("Get FeatureReaderWithFilterTest");
        wfs = getDataStore(url,false);
        assertNotNull("No featureTypes",wfs.getTypeNames());
        assertNotNull("Null featureType in [0]",wfs.getTypeNames()[0]);
        FeatureType ft = wfs.getSchema(wfs.getTypeNames()[0]);
        String[] props = new String[] {ft.getDefaultGeometry().getName(),ft.getAttributeType(0).getName().equals(ft.getDefaultGeometry().getName())?ft.getAttributeType(1).getName():ft.getAttributeType(0).getName()};
        DefaultQuery query = new DefaultQuery(ft.getTypeName());
        query.setPropertyNames(props);
        FeatureReader fr = wfs.getFeatureReader(query,null);
        assertNotNull("FeatureType was null",ft);
        assertTrue("must have 1 feature -- fair assumption",fr.hasNext() && fr.getFeatureType()!=null && fr.next()!=null);

        // 	post
        System.out.println("Post FeatureReaderWithFilterTest");
        wfs = getDataStore(url,true);
        assertNotNull("No featureTypes",wfs.getTypeNames());
        assertNotNull("Null featureType in [0]",wfs.getTypeNames()[0]);
        ft = wfs.getSchema(wfs.getTypeNames()[0]);
        props = new String[] {ft.getDefaultGeometry().getName(),ft.getAttributeType(0).getName().equals(ft.getDefaultGeometry().getName())?ft.getAttributeType(1).getName():ft.getAttributeType(0).getName()};
        query = new DefaultQuery(ft.getTypeName());
        query.setPropertyNames(props);
        fr = wfs.getFeatureReader(query,null);
        assertNotNull("FeatureType was null",ft);
        assertTrue("must have 1 feature -- fair assumption",fr.hasNext() && fr.getFeatureType()!=null && fr.next()!=null);
    }
}
