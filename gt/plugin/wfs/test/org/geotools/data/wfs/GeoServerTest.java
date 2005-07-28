package org.geotools.data.wfs;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.NoRouteToHostException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import junit.framework.TestCase;

import org.geotools.data.DataStore;
import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureEvent;
import org.geotools.data.FeatureListener;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureSource;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.factory.FactoryConfigurationError;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.FeatureType;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.filter.FidFilter;
import org.geotools.filter.Filter;
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

    private URL locale = null;
    private URL remote = null;
    public void setUp() throws MalformedURLException { 
         locale = new URL("http://localhost:8080/geoserver/wfs?REQUEST=GetCapabilities");
         if( locale != null && locale.toString().indexOf("localhost")!= -1 ) {
             InputStream stream = null;             
             try {
                 stream = locale.openStream();
             }
             catch( Throwable t ) {
                 System.err.println("Warning you local geoserver is not available - "+getName()+" test disabled ");
                 locale = null;
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
         remote = new URL("http://www.refractions.net:8000/geoserver/wfs?REQUEST=GetCapabilities");         
    }
    public WFSDataStore connect(URL capabilities) throws Exception {
        Map m = new HashMap();
        m.put(WFSDataStoreFactory.URL.key,capabilities);
        m.put(WFSDataStoreFactory.TIMEOUT.key,new Integer(10000)); // was 1000000 for debug
        return (WFSDataStore)(new WFSDataStoreFactory()).createNewDataStore(m);
    }   
    public void testTypes() throws Exception {
        WFSDataStore wfs; 
        try {
            wfs = connect( remote );
        } catch (ConnectException e) {
            e.printStackTrace(System.err);
            return;
        } catch (FileNotFoundException e) {
            e.printStackTrace(System.err);
            return;
        } catch (UnknownHostException e) {
            e.printStackTrace(System.err);
            return;
        } catch (NoRouteToHostException e) {
            e.printStackTrace(System.err);
            return;
        }
        String types[] = wfs.getTypeNames();
        String typeName = "unknown";        
        for( int i=0; i<types.length;i++){
            typeName = types[i];
            System.out.println("Type:"+typeName );
            try {
                FeatureType type = wfs.getSchema( typeName );
                System.out.print( " -  typeName: " + type.getTypeName() );
                System.out.println( " - namespace: "); System.out.println( type.getNamespace() );
                System.out.println( " -      geom:"); System.out.println( type.getDefaultGeometry() );
                System.out.println( " -       crs:"); System.out.println(type.getDefaultGeometry().getCoordinateSystem() );
                
                FeatureSource source = wfs.getFeatureSource( typeName );
                Envelope e = source.getBounds();
                System.out.println( "-     bounds:"); System.out.println(source.getBounds() );
                
                FeatureCollection features = source.getFeatures();
                System.out.println( "- all bounds:"); System.out.println(features.getBounds() );
                System.out.println( "- all schema:"); System.out.println(features.getSchema() );
                System.out.println( "- all   type:"); System.out.println(features.getFeatureType() );
                
                DefaultQuery query = new DefaultQuery( typeName, Filter.NONE, 20, Query.ALL_NAMES, "work already" );
                features = source.getFeatures( query );
                System.out.println( "- 20   size:"); System.out.println(features.size() );
                System.out.println( "- 20   count:"); System.out.println(features.getCount() );
                FeatureReader reader = features.reader();
                while( reader.hasNext() ){
                    Feature feature = reader.next();
                    System.out.println( feature.getID()+":"+feature );
                }
                reader.close();
                
                FeatureIterator iterator = features.features();
                while( iterator.hasNext() ){
                    Feature feature = iterator.next();
                    System.out.println( feature.getID()+":"+feature );
                }
                features.close( iterator );
            }
            catch (Throwable t ){
                System.out.println("Failure on "+typeName );
                t.printStackTrace();
            }
        }
    }
    public void testFeatureType() throws NoSuchElementException, IOException, SAXException{
        WFSDataStoreReadTest.doFeatureType(locale,true,true,0);
    }
    public void testFeatureReader() throws NoSuchElementException, IOException, IllegalAttributeException, SAXException{
        WFSDataStoreReadTest.doFeatureReader(locale,true,true,0);
    }
    public void testFeatureReaderWithFilter() throws NoSuchElementException, IllegalAttributeException, IOException, SAXException{
        WFSDataStoreReadTest.doFeatureReaderWithFilter(locale,true,true,0);
    }    
    public void testFeatureReaderWithFilterGET() throws NoSuchElementException, IllegalAttributeException, IOException, SAXException{
        WFSDataStoreReadTest.doFeatureReaderWithFilter(locale,true,false,0);
    }
    public void testFeatureReaderWithFilterPOST() throws NoSuchElementException, IllegalAttributeException, IOException, SAXException{
        WFSDataStoreReadTest.doFeatureReaderWithFilter(locale,false,true,0);
    }    
    public void testFeatureReaderWithFilterBBoxGET() throws NoSuchElementException, IllegalAttributeException, IOException, SAXException, IllegalFilterException{
        Envelope bbox = new Envelope(-75.791435,38.44949,-75.045998,39.840008);        
        WFSDataStoreReadTest.doFeatureReaderWithBBox(locale,true,false,0,bbox);
    }
    public void testFeatureReaderWithFilterBBoxPOST() throws NoSuchElementException, IllegalAttributeException, IOException, SAXException, IllegalFilterException{
        Envelope bbox = new Envelope(-75.791435,38.44949,-75.045998,39.840008);        
        WFSDataStoreReadTest.doFeatureReaderWithBBox(locale,true,false,0,bbox);
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
        if( locale == null || locale.toString().indexOf("localhost") != -1 ) {        
            return; // only test writing against developers local geoserver
        }
        DataStore post = WFSDataStoreWriteTest.getDataStore(locale);        
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
