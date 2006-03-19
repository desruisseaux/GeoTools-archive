package org.geotools.data.wfs;

import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.NoRouteToHostException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Iterator;
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


public class GeoServerOnlineTest extends TestCase {

    protected URL url = null;
    
    public void setUp() throws MalformedURLException { 
    	url = targetCapabilities("http://www.refractions.net:8080/geoserver/wfs?REQUEST=GetCapabilities");
    }
    
    /** Subclasses may override 
     * @throws MalformedURLException
     */
    protected URL targetCapabilities(String capabilities) throws MalformedURLException{
    	URL url = new URL(capabilities);
        InputStream stream = null;             
        try {
            stream = url.openStream();
            return url;
        }
        catch( Throwable t ) {
            System.err.println( getName()+ " disabled: target unavailable "+capabilities );
            return null;
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
    public void testTypes() throws IOException, NoSuchElementException {
    	if( url == null) return;
        WFSDataStore wfs; 
        try {
            wfs = WFSDataStoreReadTest.getDataStore(url);
        } catch (ConnectException e) {
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
            FeatureType type = wfs.getSchema( typeName );
            type.getTypeName();
            type.getNamespace();
            
            FeatureSource source = wfs.getFeatureSource( typeName );
            source.getBounds();
            
            FeatureCollection features = source.getFeatures();
            features.getBounds();
            features.getSchema();
            features.getFeatureType();
            
            DefaultQuery query = new DefaultQuery( typeName, Filter.NONE, 20, Query.ALL_NAMES, "work already" );
            features = source.getFeatures( query );
            features.size();
            features.getCount();
            Iterator reader = features.iterator();
            while( reader.hasNext() ){
                Feature feature = (Feature)reader.next();
            }
            features.close(reader);
            
            FeatureIterator iterator = features.features();
            while( iterator.hasNext() ){
                Feature feature = iterator.next();
            }
            features.close( iterator );
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

    // RR change the data?
    // NOPE, it's in Lat-Long for the Env, BCAlbers for the data
    public void testFeatureReaderWithFilterBBoxGET() throws NoSuchElementException, IllegalAttributeException, IOException, SAXException, IllegalFilterException{
        // minx,miny,maxx,maxy
//        Envelope bbox = new Envelope(-75.791435,38.44949,-75.045998,39.840008); // lat long
        Envelope bbox = new Envelope(556759.0,5233034,556934, 5233040.0);  // bc albers  
        WFSDataStoreReadTest.doFeatureReaderWithBBox(url,true,false,0,bbox);
    }
    public void testFeatureReaderWithFilterBBoxPOST() throws NoSuchElementException, IllegalAttributeException, IOException, SAXException, IllegalFilterException{
        // minx,miny,maxx,maxy
//      Envelope bbox = new Envelope(-75.791435,38.44949,-75.045998,39.840008); // lat long
      Envelope bbox = new Envelope(556759.0,5233034,556934, 5233040.0);  // bc albers  
        WFSDataStoreReadTest.doFeatureReaderWithBBox(url,true,false,0,bbox);
    }    
    
    /**
     * Writing test that only engages against a remote geoserver.
     * <p>
     * Makes referece to the standard featureTypes that geoserver ships with.
     * </p>
     */
    
    // Feature Set no longer at RR.
    
//    public void testWrite() throws NoSuchElementException, IllegalFilterException, IOException, IllegalAttributeException{
//
//
//
//        Map m = new HashMap();
//        m.put(WFSDataStoreFactory.URL.key,url);
//        m.put(WFSDataStoreFactory.TIMEOUT.key,new Integer(100000));
//        DataStore post = (WFSDataStore)(new WFSDataStoreFactory()).createNewDataStore(m);  
//        FeatureType ft = post.getSchema( "gd:states" );
//        FeatureSource fs = post.getFeatureSource( "gd:states" );        
//        class Watcher implements FeatureListener {
//            public int count=0;
//            public void changed( FeatureEvent featureEvent ) {
//                System.out.println("Event "+featureEvent );
//                count++;
//            }            
//        }
//        Watcher watcher = new Watcher();
//        fs.addFeatureListener( watcher );
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
//        inserts.close();
//        
//        /// okay now count ...
//        FeatureReader count = post.getFeatureReader(new DefaultQuery(ft.getTypeName()),Transaction.AUTO_COMMIT);        
//        int i = 0;
//        while(count.hasNext() && i<3){
//            f = count.next();i++;
//        }
//        count.close();       
//        //
//        fp = FilterFactoryFinder.createFilterFactory().createFidFilter(f.getID());
//        
//        WFSDataStoreWriteTest.doDelete(post,ft,fp);
//        WFSDataStoreWriteTest.doUpdate(post,ft);
//        //assertFalse("events fired", watcher.count == 0);
//    }    
}
