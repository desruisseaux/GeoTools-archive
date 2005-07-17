package org.geotools.data.wfs;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.NoSuchElementException;

import junit.framework.TestCase;

import org.geotools.data.DataStore;
import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureEvent;
import org.geotools.data.FeatureListener;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureSource;
import org.geotools.data.Transaction;
import org.geotools.factory.FactoryConfigurationError;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.filter.FidFilter;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.IllegalFilterException;
import org.xml.sax.SAXException;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;


public class GeoServerTest extends TestCase {

    private URL url = null;
    
    public void setUp() throws MalformedURLException { 
         url = new URL("http://localhost:8080/geoserver/wfs?REQUEST=GetCapabilities");
//       url = new URL("http://www.refractions.net:8080/geoserver/wfs?REQUEST=GetCapabilities");
         if( url != null && url.toString().indexOf("localhost")!= -1 ) {
             InputStream stream = null;             
             try {
                 stream = url.openStream();
             }
             catch( Throwable t ) {
                 System.err.println("Warning you local geoserver is not available - "+getName()+" test disabled ");
                 url = null;
             }
             finally {
                 if( stream != null )
                    try {
                        stream.close();
                    } catch (IOException e) {
                        // whatever
                    }
             }
         }
    }
    
    public void testFeatureType() throws NoSuchElementException, IOException, SAXException{
        WFSDataStoreReadTest.doFeatureType(url,true,true,0);
    }
    public void testFeatureReader() throws NoSuchElementException, IOException, IllegalAttributeException, SAXException{
        WFSDataStoreReadTest.doFeatureReader(url,true,true,0);
    }
    public void testFeatureReaderWithFilter() throws NoSuchElementException, IllegalAttributeException, IOException, SAXException{
        WFSDataStoreReadTest.doFeatureReaderWithFilter(url,true,true,0);
    }    
    public void testFeatureReaderWithFilterGET() throws NoSuchElementException, IllegalAttributeException, IOException, SAXException{
        WFSDataStoreReadTest.doFeatureReaderWithFilter(url,true,false,0);
    }
    public void testFeatureReaderWithFilterPOST() throws NoSuchElementException, IllegalAttributeException, IOException, SAXException{
        WFSDataStoreReadTest.doFeatureReaderWithFilter(url,false,true,0);
    }    
    public void testFeatureReaderWithFilterBBoxGET() throws NoSuchElementException, IllegalAttributeException, IOException, SAXException, IllegalFilterException{
        Envelope bbox = new Envelope(-75.791435,38.44949,-75.045998,39.840008);        
        WFSDataStoreReadTest.doFeatureReaderWithBBox(url,true,false,0,bbox);
    }
    public void testFeatureReaderWithFilterBBoxPOST() throws NoSuchElementException, IllegalAttributeException, IOException, SAXException, IllegalFilterException{
        Envelope bbox = new Envelope(-75.791435,38.44949,-75.045998,39.840008);        
        WFSDataStoreReadTest.doFeatureReaderWithBBox(url,true,false,0,bbox);
    }    
//    public void testWrite() throws NoSuchElementException, IllegalFilterException, FactoryConfigurationError, IOException, IllegalAttributeException{
//        DataStore post = WFSDataStoreWriteTest.getDataStore(url);
//        FeatureType ft = post.getSchema(post.getTypeNames()[0]);
//
//        GeometryFactory gf = new GeometryFactory();
//        MultiPolygon mp = gf.createMultiPolygon(new Polygon[]{gf.createPolygon(gf.createLinearRing(new Coordinate[]{new Coordinate(-88.071564,37.51099), new Coordinate(-88.467644,37.400757), new Coordinate(-90.638329,42.509361), new Coordinate(-89.834618,42.50346),new Coordinate(-88.071564,37.51099)}),new LinearRing[]{})});
//        mp.setUserData("http://www.opengis.net/gml/srs/epsg.xml#4326");
//        
//        Object[] attrs = {
//                mp,
//                "MyStateName",
//                "70",
//                "Refrac",
//                "RR",
//                new Double(180),
//                new Double(18),
//                new Double(220),
//                new Double(80),
//                new Double(20),
//                new Double(40),
//                new Double(180),
//                new Double(90),
//                new Double(100),
//                new Double(40),
//                new Double(80),
//                new Double(40),
//                new Double(180),
//                new Double(90),
//                new Double(70),
//                new Double(70),
//                new Double(60),
//                new Double(10)  
//        };
//        
//        System.out.println(attrs[0]);
//        Feature f = ft.create(attrs);
//        
//        FeatureReader inserts = DataUtilities.reader(new Feature[] {f});
//        FidFilter fp = WFSDataStoreWriteTest.doInsert(post,ft,inserts);
//        // geoserver does not return the correct fid here ... 
//        // get the 3rd feature ... and delete it?
//        
//        inserts.close();
//        inserts = post.getFeatureReader(new DefaultQuery(ft.getTypeName()),Transaction.AUTO_COMMIT);
//        int i = 0;
//        while(inserts.hasNext() && i<3){
//            f = inserts.next();i++;
//        }
//        inserts.close();
//        fp = FilterFactory.createFilterFactory().createFidFilter(f.getID());
//        
//        WFSDataStoreWriteTest.doDelete(post,ft,fp);
//        WFSDataStoreWriteTest.doUpdate(post,ft);
//    }
    /**
     * Writing test that only engages against your local geoserver.
     * <p>
     * Makes referece to the standard featureTypes that geoserver ships with.
     * </p>
     */
    public void testWrite() throws NoSuchElementException, IllegalFilterException, FactoryConfigurationError, IOException, IllegalAttributeException{
        if( url == null || url.toString().indexOf("localhost") != -1 ) {        
            return; // only test writing against developers local geoserver
        }
        DataStore post = WFSDataStoreWriteTest.getDataStore(url);        
        FeatureType ft = post.getSchema( "states" );
        FeatureSource fs = post.getFeatureSource( "states" );        
        class Watcher implements FeatureListener {
            public int count=0;
            public void changed( FeatureEvent featureEvent ) {
                System.out.println("Event "+featureEvent );
                count++;
            }            
        }
        Watcher watcher = new Watcher();
        fs.addFeatureListener( watcher );

        GeometryFactory gf = new GeometryFactory();
        MultiPolygon mp = gf.createMultiPolygon(new Polygon[]{gf.createPolygon(gf.createLinearRing(new Coordinate[]{new Coordinate(-88.071564,37.51099), new Coordinate(-88.467644,37.400757), new Coordinate(-90.638329,42.509361), new Coordinate(-89.834618,42.50346),new Coordinate(-88.071564,37.51099)}),new LinearRing[]{})});
        mp.setUserData("http://www.opengis.net/gml/srs/epsg.xml#4326");
        
        Object[] attrs = {
                mp,
                "MyStateName",
                "70",
                "Refrac",
                "RR",
                new Double(180),
                new Double(18),
                new Double(220),
                new Double(80),
                new Double(20),
                new Double(40),
                new Double(180),
                new Double(90),
                new Double(100),
                new Double(40),
                new Double(80),
                new Double(40),
                new Double(180),
                new Double(90),
                new Double(70),
                new Double(70),
                new Double(60),
                new Double(10)  
        };
        
        System.out.println(attrs[0]);
        Feature f = ft.create(attrs);
                
        FeatureReader inserts = DataUtilities.reader(new Feature[] {f});
        FidFilter fp = WFSDataStoreWriteTest.doInsert(post,ft,inserts);
        // geoserver does not return the correct fid here ... 
        // get the 3rd feature ... and delete it?        
        inserts.close();
        
        /// okay now count ...
        FeatureReader count = post.getFeatureReader(new DefaultQuery(ft.getTypeName()),Transaction.AUTO_COMMIT);        
        int i = 0;
        while(count.hasNext() && i<3){
            f = count.next();i++;
        }
        count.close();       
        //
        fp = FilterFactory.createFilterFactory().createFidFilter(f.getID());
        
        WFSDataStoreWriteTest.doDelete(post,ft,fp);
        WFSDataStoreWriteTest.doUpdate(post,ft);
        //assertFalse("events fired", watcher.count == 0);
    }    
}
