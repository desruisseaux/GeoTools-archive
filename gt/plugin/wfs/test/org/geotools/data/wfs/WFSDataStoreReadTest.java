
package org.geotools.data.wfs;

import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.OperationNotSupportedException;

import junit.framework.TestCase;

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

    private WFSDataStore getDataStore(URL server) throws IOException{
        Map m = new HashMap();
        m.put(WFSDataStoreFactory.URL.key,server);
        m.put(WFSDataStoreFactory.TIMEOUT.key,new Integer(10000));
        return (WFSDataStore)(new WFSDataStoreFactory()).createNewDataStore(m);
    }
    
    public void testPostDescribe() throws OperationNotSupportedException, IOException{
        StringWriter w = new StringWriter();
        Map hints = new HashMap();
        hints.put(DocumentWriter.BASE_ELEMENT,WFSSchema.getInstance().getElements()[1]); // DescribeFeatureType
        Element e = WFSSchema.getInstance().getElements()[1];
//        System.out.println("HINT NAME = "+e.getName());
//        System.out.println("Can encode"+e.getType().canEncode(e,new String[]{"van:Airport"},hints));
        DocumentWriter.writeDocument(new String[]{"van:Airport"},WFSSchema.getInstance(),w,hints);
//        System.out.print(w.getBuffer());
    }
    
    public void testPostGetFeature() throws OperationNotSupportedException, IOException{
        
        Map hints = new HashMap();
        Element e = WFSSchema.getInstance().getElements()[2];
        hints.put(DocumentWriter.BASE_ELEMENT,WFSSchema.getInstance().getElements()[2]); // GetFeature
//        System.out.println("HINT NAME = "+e.getName());
//        System.out.println("Can encode"+e.getType().canEncode(e,new String[]{"topp:bc_roads"},hints));

        DefaultQuery query = new DefaultQuery("topp:bc_roads");
        query.setPropertyNames(new String[]{"geom","length"}); // no acurate ...
        StringWriter w = new StringWriter();
        DocumentWriter.writeDocument(query,WFSSchema.getInstance(),w,hints);
//        System.out.print(w.getBuffer());
    }
    
    public void testGaldos() throws NoSuchElementException, OperationNotSupportedException, IllegalAttributeException, IOException, SAXException{
        URL url = new URL("http://wfs.galdosinc.com:8680/wfs/http?Request=GetCapabilities&service=WFS");
        System.out.println("\nGaldos");
        doFeatureType(url,true,false,1);
//        doFeatureReader(url,false,false,1);
//        doFeatureReaderWithFilter(url,false,false,1);
        System.out.println("");
    }
    
    public void testGeoServer() throws NoSuchElementException, OperationNotSupportedException, IllegalAttributeException, IOException, SAXException{
        URL url = new URL("http://www.refractions.net:8080/geoserver/wfs?REQUEST=GetCapabilities");
//        
        System.out.println("\nGeoServer");
        doFeatureType(url,true,false,1);
//        doFeatureReader(url,true,true,1);
//        doFeatureReaderWithFilter(url,true,true,1);
        System.out.println("");
    }
    
//    public void testLocalGeoServer() throws NoSuchElementException, IllegalAttributeException, IOException{
//    	URL url = new URL("http://localhost:8080/geoserver/wfs?REQUEST=GetCapabilities");
//    	try{
//    		url.openConnection();
//    	}catch(Exception e){
//    		return; // server not around
//    	}
//        System.out.println("\nLocal GeoServer");
//        doFeatureType(url,true,true,1);
//        doFeatureReader(url,true,true,0);
//        doFeatureReader(url,true,true,1);
//        System.out.println("");
//    }
    
    public void testESRI() throws NoSuchElementException, OperationNotSupportedException, IllegalAttributeException, IOException, SAXException{
        URL url = new URL("http://dev.geographynetwork.ca/ogcwfs/servlet/com.esri.ogc.wfs.WFSServlet?Request=GetCapabilities");
        
        // DEAD server - need a new one
        System.out.println("\nESRI");
//        doFeatureType(url,true,false,1);
//        doFeatureReader(url,false,false,1);
//        doFeatureReaderWithFilter(url,false,false,1);
        System.out.println("");
    }
    
    public void testInterGraph() throws NoSuchElementException, OperationNotSupportedException, IllegalAttributeException, IOException, SAXException{
        URL url = new URL("http://ogc.intergraph.com/OregonDOT_wfs/request.asp?VERSION=0.0.14&request=GetCapabilities");
        
        System.out.println("\nInterGraph");

        // This server does not specify namespace declarations within their xml documents
        doFeatureType(url,true,false,1);
//        doFeatureReader(url,false,false,1);
//        doFeatureReaderWithFilter(url,false,false,1);
        System.out.println("");
    }
    
    
    
    public void testGeomatics() throws NoSuchElementException, OperationNotSupportedException, IllegalAttributeException, IOException, SAXException{
        URL url = new URL("http://gws2.pcigeomatics.com/wfs1.0.0/wfs?service=WFS&request=getcapabilities");
        System.out.println("\nGeomatics");
        doFeatureType(url,true,false,1);
//        doFeatureReader(url,false,false,1);
//        doFeatureReaderWithFilter(url,false,false,1);
        System.out.println("");
    }
    
    // Permission issues
    public void testDMSolutions() throws NoSuchElementException, IOException, SAXException{
        URL url = new URL("http://www2.dmsolutions.ca/cgi-bin/mswfs_gmap?version=1.0.0&request=getcapabilities&service=wfs");
        System.out.println("\nDMSolutions");
        doFeatureType(url,true,false,0);
//        doFeatureReader(url,true,false);
//        doFeatureReaderWithFilter(url,true,false);
        System.out.println("");
    }
    
    public void testMapServer() throws NoSuchElementException, OperationNotSupportedException, IllegalAttributeException, IOException, SAXException{
        URL url = new URL("http://map.ns.ec.gc.ca/MapServer/mapserv.exe?map=/mapserver/services/envdat/config.map&service=WFS&version=1.0.0&request=GetCapabilities");
        System.out.println("\nMapServer");
        doFeatureType(url,true,false,0);
        doFeatureReader(url,true,false,0);
        doFeatureReaderWithFilter(url,true,false,0);
        System.out.println("");
    }
    
    public void testIonic() throws NoSuchElementException, OperationNotSupportedException, IllegalAttributeException, IOException, SAXException{
       URL url = new URL("http://webservices.ionicsoft.com/ionicweb/wfs/BOSTON_ORA?version=1.0.0&request=getcapabilities&service=WFS");
       System.out.println("\nIonicWeb");
       doFeatureType(url,true,false,0);
       doFeatureReader(url,true,false,0);
       doFeatureReaderWithFilter(url,true,false,0);
       System.out.println("");
   }
    
    public void doFeatureType(URL url,boolean get, boolean post, int i) throws IOException, SAXException{
        WFSDataStore wfs = getDataStore(url);
        System.out.println("Get FeatureTypeTest + "+url);
        assertNotNull("No featureTypes",wfs.getTypeNames());
        assertNotNull("Null featureType in ["+i+"]",wfs.getTypeNames()[i]);
//        System.out.println("FT name = "+wfs.getTypeNames()[i]);
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
    }
    
    public void doFeatureReader(URL url, boolean get, boolean post, int i) throws NoSuchElementException, IOException, IllegalAttributeException, SAXException{

        System.out.println("Post FeatureReaderTest + "+url);
        WFSDataStore wfs = getDataStore(url);
        assertNotNull("No featureTypes",wfs.getTypeNames());
        assertNotNull("Null featureType in [0]",wfs.getTypeNames()[i]);
        Query query = new DefaultQuery(wfs.getTypeNames()[i]);
        
        if(post){
        // 	post
            FeatureReader ft = wfs.getFeatureReaderPost(query,Transaction.AUTO_COMMIT);
            assertNotNull("FeatureType was null",ft);
            assertTrue("must have 1 feature -- fair assumption",ft.hasNext() && ft.getFeatureType()!=null && ft.next()!=null);
            assertNotNull("CRS missing ",ft.getFeatureType().getDefaultGeometry().getCoordinateSystem());
            ft.close();
        }
        if(get){
        // 	get
            FeatureReader ft = wfs.getFeatureReaderGet(query,Transaction.AUTO_COMMIT);
            assertNotNull("FeatureType was null",ft);
            assertTrue("must have 1 feature -- fair assumption",ft.hasNext() && ft.getFeatureType()!=null && ft.next()!=null);
            assertNotNull("CRS missing ",ft.getFeatureType().getDefaultGeometry().getCoordinateSystem());
            ft.close();}
    }
    
    public void doFeatureReaderWithFilter(URL url, boolean get, boolean post, int i) throws NoSuchElementException, IllegalAttributeException, OperationNotSupportedException, IOException, SAXException{

        System.out.println("Get FeatureReaderWithFilterTest + "+url);
        WFSDataStore wfs = getDataStore(url);
        assertNotNull("No featureTypes",wfs.getTypeNames());
        assertNotNull("Null featureType in [0]",wfs.getTypeNames()[i]);
        FeatureType ft = wfs.getSchema(wfs.getTypeNames()[i]);
        // take atleast attributeType 3 to avoid the undeclared one .. inherited optional attrs
        String[] props = new String[] {ft.getDefaultGeometry().getName(),ft.getAttributeType(4).getName().equals(ft.getDefaultGeometry().getName())?ft.getAttributeType(5).getName():ft.getAttributeType(4).getName()};
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

            Map hints = new HashMap();
            hints.put(DocumentWriter.BASE_ELEMENT,WFSSchema.getInstance().getElements()[2]); // GetFeature
            StringWriter w = new StringWriter();
            DocumentWriter.writeDocument(query,WFSSchema.getInstance(),w,hints);
        //        System.out.print(w.getBuffer());
        
            FeatureReader fr = wfs.getFeatureReaderPost(query,Transaction.AUTO_COMMIT);
            assertNotNull("FeatureType was null",ft);
            assertTrue("must have 1 feature -- fair assumption",fr.hasNext() && fr.getFeatureType()!=null && fr.next()!=null);
            int j=0;while(fr.hasNext()){fr.next();j++;}
            System.out.println(j+" Features");
            fr.close();
        }
    }
}
