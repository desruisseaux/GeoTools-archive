
package org.geotools.data.wfs;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.OperationNotSupportedException;

import junit.framework.TestCase;

import org.geotools.data.DataStore;
import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureReader;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.feature.FeatureType;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.xml.DocumentWriter;
import org.geotools.xml.schema.Element;
import org.geotools.xml.wfs.WFSSchema;
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

    private DataStore getDataStore(URL server, boolean isPost) throws IOException{
        Map m = new HashMap();
        m.put(WFSDataStoreFactory.GET_CAPABILITIES_URL.key,server);
        m.put(WFSDataStoreFactory.TIMEOUT.key,new Integer(10000));
        if(isPost){
            m.put(WFSDataStoreFactory.USE_POST.key,Boolean.TRUE);
            m.put(WFSDataStoreFactory.USE_GET.key,Boolean.FALSE);
        }
        else {
            m.put(WFSDataStoreFactory.USE_GET.key,Boolean.TRUE);
            m.put(WFSDataStoreFactory.USE_POST.key,Boolean.FALSE);
        }
        return (new WFSDataStoreFactory()).createNewDataStore(m);
    }
    
    public void testPostDescribe() throws OperationNotSupportedException, IOException, SAXException{
        StringWriter w = new StringWriter();
        Map hints = new HashMap();
        hints.put(DocumentWriter.BASE_ELEMENT,WFSSchema.getInstance().getElements()[1]); // DescribeFeatureType
        Element e = WFSSchema.getInstance().getElements()[1];
        System.out.println("HINT NAME = "+e.getName());
        System.out.println("Can encode"+e.getType().canEncode(e,new String[]{"van:Airport"},hints));
        DocumentWriter.writeDocument(new String[]{"van:Airport"},WFSSchema.getInstance(),w,hints);
        System.out.print(w.getBuffer());
    }
    
    public void testPostGetFeature() throws OperationNotSupportedException, IOException, SAXException{
        StringWriter w = new StringWriter();
        Map hints = new HashMap();
        Element e = WFSSchema.getInstance().getElements()[2];
        hints.put(DocumentWriter.BASE_ELEMENT,WFSSchema.getInstance().getElements()[2]); // GetFeature
        System.out.println("HINT NAME = "+e.getName());
        System.out.println("Can encode"+e.getType().canEncode(e,new String[]{"topp:bc_roads"},hints));

        Query query = new DefaultQuery("topp:bc_roads");
        DocumentWriter.writeDocument(query,WFSSchema.getInstance(),w,hints);
        System.out.print(w.getBuffer());
    }
    
    public void testGaldos() throws NoSuchElementException, IOException, IllegalAttributeException{
        URL url = new URL("http://wfs.galdosinc.com:8680/wfs/http?Request=GetCapabilities&service=WFS");
        System.out.println("\nGaldos");
        doFeatureType(url,false,false);
        doFeatureReader(url,false,false);
        doFeatureReaderWithFilter(url,false,false);
        System.out.println("");
    }
    
    public void testGeoServer() throws NoSuchElementException, IOException, IllegalAttributeException{
        URL url = new URL("http://192.168.50.35:8080/geoserver/wfs?Request=GetCapabilities&service=WFS");
        
        System.out.println("\nGeoServer");
//        doFeatureType(url,true,true);
//        doFeatureReader(url,true,true);
//        doFeatureReaderWithFilter(url,true,true);
        System.out.println("");
    }
    
    public void testESRI() throws NoSuchElementException, IOException, IllegalAttributeException{
        URL url = new URL("http://dev.geographynetwork.ca/ogcwfs/servlet/com.esri.ogc.wfs.WFSServlet?Request=GetCapabilities");
        
        System.out.println("\nESRI");
        // TODO turn these on
        doFeatureType(url,false,false);
        doFeatureReader(url,false,false);
        doFeatureReaderWithFilter(url,false,false);
        System.out.println("");
    }
    
    public void testInterGraph() throws NoSuchElementException, IOException, IllegalAttributeException{
        URL url = new URL("http://ogc.intergraph.com/OregonDOT_wfs/request.asp?VERSION=0.0.14&request=GetCapabilities");
        
        System.out.println("\nInterGraph");
        // TODO turn these on
        // This server does not specify namespace declarations within their xml documents
        doFeatureType(url,false,false);
        doFeatureReader(url,false,false);
        doFeatureReaderWithFilter(url,false,false);
        System.out.println("");
    }
    
    
    
    public void testGeomatics() throws NoSuchElementException, IOException, IllegalAttributeException{
        URL url = new URL("http://gws2.pcigeomatics.com/wfs1.0.0/wfs?service=WFS&request=getcapabilities");
        System.out.println("\nGeomatics");
        doFeatureType(url,true,false);
        // TODO uncomment when server bug is fixed
        doFeatureReader(url,false,false);
        doFeatureReaderWithFilter(url,false,false);
        System.out.println("");
    }
    
    // Permission issues
//    public void testMapServer() throws NoSuchElementException, IOException, IllegalAttributeException{
//        URL url = new URL("http://www2.dmsolutions.ca/cgi-bin/mswfs_gmap?version=1.0.0&request=getcapabilities&service=wfs");
//        System.out.println("\nMapServer");
//        doFeatureType(url,true,false);
//        doFeatureReader(url,true,false);
//        doFeatureReaderWithFilter(url,true,false);
//        System.out.println("");
//    }
    
    public void doFeatureType(URL url,boolean get, boolean post) throws IOException{
        DataStore wfs = null;
        if(get){
        // get
        System.out.println("Get FeatureTypeTest");
        wfs = getDataStore(url,false);
        assertNotNull("No featureTypes",wfs.getTypeNames());
        assertNotNull("Null featureType in [0]",wfs.getTypeNames()[1]);
        System.out.println("FT name = "+wfs.getTypeNames()[1]);
        FeatureType ft = wfs.getSchema(wfs.getTypeNames()[1]);
        assertNotNull("FeatureType was null",ft);
        assertTrue("must have 1 geom and atleast 1 other attribute -- fair assumption",ft.getDefaultGeometry()!=null && ft.getAttributeTypes()!=null && ft.getAttributeCount()>0);
        }if(post){
        // post
        System.out.println("Post FeatureTypeTest");
        wfs = getDataStore(url,true);
        assertNotNull("No featureTypes",wfs.getTypeNames());
        assertNotNull("Null featureType in [0]",wfs.getTypeNames()[1]);
        System.out.println("FT name = "+wfs.getTypeNames()[1]);
        FeatureType ft = wfs.getSchema(wfs.getTypeNames()[1]);
        assertNotNull("FeatureType was null",ft);
        assertTrue("must have 1 geom and atleast 1 other attribute -- fair assumption",ft.getDefaultGeometry()!=null && ft.getAttributeTypes()!=null && ft.getAttributeCount()>0);
    }}
    
    public void doFeatureReader(URL url, boolean get, boolean post) throws NoSuchElementException, IOException, IllegalAttributeException{
        DataStore wfs = null;
        if(post){
        // 	post
        System.out.println("Post FeatureReaderTest");
        wfs = getDataStore(url,true);
        assertNotNull("No featureTypes",wfs.getTypeNames());
        assertNotNull("Null featureType in [0]",wfs.getTypeNames()[1]);
        Query query = new DefaultQuery(wfs.getTypeNames()[1]);
        FeatureReader ft = wfs.getFeatureReader(query,Transaction.AUTO_COMMIT);
        assertNotNull("FeatureType was null",ft);
        assertTrue("must have 1 feature -- fair assumption",ft.hasNext() && ft.getFeatureType()!=null && ft.next()!=null);
        ft.close();
        }if(get){
        // 	get
        System.out.println("Get FeatureReaderTest");
        wfs = getDataStore(url,false);
        assertNotNull("No featureTypes",wfs.getTypeNames());
        assertNotNull("Null featureType in [0]",wfs.getTypeNames()[1]);
        Query query = new DefaultQuery(wfs.getTypeNames()[1]);
        FeatureReader ft = wfs.getFeatureReader(query,Transaction.AUTO_COMMIT);
        assertNotNull("FeatureType was null",ft);
        assertTrue("must have 1 feature -- fair assumption",ft.hasNext() && ft.getFeatureType()!=null && ft.next()!=null);
        ft.close();}
    }
    
    public void doFeatureReaderWithFilter(URL url, boolean get, boolean post) throws NoSuchElementException, IOException, IllegalAttributeException{
        DataStore wfs = null;
    if(get){
        // 	get
        System.out.println("Get FeatureReaderWithFilterTest");
        wfs = getDataStore(url,false);
        assertNotNull("No featureTypes",wfs.getTypeNames());
        assertNotNull("Null featureType in [0]",wfs.getTypeNames()[1]);
        FeatureType ft = wfs.getSchema(wfs.getTypeNames()[1]);
        String[] props = new String[] {ft.getDefaultGeometry().getName(),ft.getAttributeType(0).getName().equals(ft.getDefaultGeometry().getName())?ft.getAttributeType(1).getName():ft.getAttributeType(0).getName()};
        DefaultQuery query = new DefaultQuery(ft.getTypeName());
        query.setPropertyNames(props);
        FeatureReader fr = wfs.getFeatureReader(query,Transaction.AUTO_COMMIT);
        assertNotNull("FeatureType was null",ft);
        assertTrue("must have 1 feature -- fair assumption",fr.hasNext() && fr.getFeatureType()!=null && fr.next()!=null);
    }if(post){
        // 	post
        System.out.println("Post FeatureReaderWithFilterTest");
        wfs = getDataStore(url,true);
        assertNotNull("No featureTypes",wfs.getTypeNames());
        assertNotNull("Null featureType in [0]",wfs.getTypeNames()[1]);
        FeatureType ft = wfs.getSchema(wfs.getTypeNames()[1]);
        String[] props = new String[] {ft.getDefaultGeometry().getName(),ft.getAttributeType(0).getName().equals(ft.getDefaultGeometry().getName())?ft.getAttributeType(1).getName():ft.getAttributeType(0).getName()};
        DefaultQuery query = new DefaultQuery(ft.getTypeName());
        query.setPropertyNames(props);
        FeatureReader fr = wfs.getFeatureReader(query,Transaction.AUTO_COMMIT);
        assertNotNull("FeatureType was null",ft);
        assertTrue("must have 1 feature -- fair assumption",fr.hasNext() && fr.getFeatureType()!=null && fr.next()!=null);
    }}
}
