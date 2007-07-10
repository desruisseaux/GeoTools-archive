/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004-2006, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.data.wfs;

import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.NoRouteToHostException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.TestCase;

import org.geotools.data.DataStore;
import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultQuery;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureEvent;
import org.geotools.data.FeatureListener;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureStore;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.FeatureType;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.filter.IllegalFilterException;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.Id;
import org.opengis.filter.PropertyIsNull;
import org.opengis.filter.expression.PropertyName;
import org.opengis.filter.identity.FeatureId;
import org.xml.sax.SAXException;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;


public class GeoServerOnlineTest extends TestCase {

    public static final String SERVER_URL = "http://localhost:8080/geoserver/wfs?REQUEST=GetCapabilities";
    public static final String TO_EDIT_TYPE = "topp:states";
    public static final String ATTRIBUTE_TO_EDIT = "STATE_FIPS";
    public static final String NEW_EDIT_VALUE = "newN";
    private static final int EPSG_CODE = 4326;
    private URL url = null;
    public void setUp() throws MalformedURLException { 
       url = new URL(SERVER_URL);
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
            if( typeName.equals("topp:geometrytype"))
            	continue;
            FeatureType type = wfs.getSchema( typeName );
            type.getTypeName();
            type.getNamespace();
            
            FeatureSource source = wfs.getFeatureSource( typeName );
            source.getBounds();
            
            FeatureCollection features = source.getFeatures();
            features.getBounds();
            features.getSchema();
            features.getFeatureType();
            
            DefaultQuery query = new DefaultQuery( typeName, Filter.INCLUDE, 20, Query.ALL_NAMES, "work already" );
            features = source.getFeatures( query );
            features.size();
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
    
    public void testSingleType() throws IOException, NoSuchElementException {
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
        String typeName = "tiger:poi";        
            FeatureType type = wfs.getSchema( typeName );
            type.getTypeName();
            type.getNamespace();
            
            FeatureSource source = wfs.getFeatureSource( typeName );
            source.getBounds();
            
            FeatureCollection features = source.getFeatures();
            features.getBounds();
            features.getSchema();
            features.getFeatureType();
            
            DefaultQuery query = new DefaultQuery( typeName, Filter.INCLUDE, 20, Query.ALL_NAMES, "work already" );
            features = source.getFeatures( query );
            features.size();
            
            Iterator reader = features.iterator();
            while( reader.hasNext() ){
                Feature feature = (Feature)reader.next();
                System.out.println(feature);
            }
            features.close(reader);
            
            FeatureIterator iterator = features.features();
            while( iterator.hasNext() ){
                Feature feature = iterator.next();
            }
            features.close( iterator );
    }
    public void testFeatureType() throws NoSuchElementException, IOException, SAXException{

        WFSDataStoreReadTest.doFeatureType(url,true,true,0);
    }
    public void testFeatureReader() throws NoSuchElementException, IOException, IllegalAttributeException, SAXException{
        WFSDataStoreReadTest.doFeatureReader(url,true,true,0);
    }
    public void testFeatureReaderWithFilter() throws NoSuchElementException, IllegalAttributeException, IOException, SAXException{
        WFSDataStoreReadTest.doFeatureReaderWithQuery(url,true,true,0);
    }    
    public void testFeatureReaderWithFilterGET() throws NoSuchElementException, IllegalAttributeException, IOException, SAXException{
        WFSDataStoreReadTest.doFeatureReaderWithQuery(url,true,false,0);
    }
    public void testFeatureReaderWithFilterPOST() throws NoSuchElementException, IllegalAttributeException, IOException, SAXException{
        WFSDataStoreReadTest.doFeatureReaderWithQuery(url,false,true,0);
    } 

    // RR change the data?
    // NOPE, it's in Lat-Long for the Env, BCAlbers for the data
    public void testFeatureReaderWithFilterBBoxGET() throws NoSuchElementException, IllegalAttributeException, IOException, SAXException, IllegalFilterException{
        // minx,miny,maxx,maxy

        Map m = new HashMap();
        m.put(WFSDataStoreFactory.URL.key,url);
        m.put(WFSDataStoreFactory.TIMEOUT.key,new Integer(100000));
        DataStore post = (WFSDataStore)(new WFSDataStoreFactory()).createNewDataStore(m);  
        
        Envelope bbox = post.getFeatureSource(post.getTypeNames()[0]).getBounds();
        WFSDataStoreReadTest.doFeatureReaderWithBBox(url,true,false,0,bbox);
    }
    public void testFeatureReaderWithFilterBBoxPOST() throws NoSuchElementException, IllegalAttributeException, IOException, SAXException, IllegalFilterException{

        Map m = new HashMap();
        m.put(WFSDataStoreFactory.URL.key,url);
        m.put(WFSDataStoreFactory.TIMEOUT.key,new Integer(100000));
        DataStore post = (WFSDataStore)(new WFSDataStoreFactory()).createNewDataStore(m);  
        
        Envelope bbox = post.getFeatureSource(post.getTypeNames()[0]).getBounds();    
        
        WFSDataStoreReadTest.doFeatureReaderWithBBox(url,true,false,0,bbox);
    }    
    
    /**
     * Tests case where filter is makes use of 2 different attributes but Query object only requests 1 of the two 
     * attributes.  This is a fix for a bug that has occurred.
     */
    public void testFeatureReaderWithQuery() throws Exception{
        Map m = new HashMap();
        m.put(WFSDataStoreFactory.URL.key,url);
        m.put(WFSDataStoreFactory.TIMEOUT.key,new Integer(100000));
        WFSDataStore wfs = (WFSDataStore)(new WFSDataStoreFactory()).createNewDataStore(m);
        FilterFactory2 fac = CommonFactoryFinder.getFilterFactory2(GeoTools.getDefaultHints());

        Filter filter = fac.equals(fac.property("NAME"), fac.literal("E 58th St"));
        
        Query query=new DefaultQuery("tiger:tiger_roads", filter);
        FeatureReader reader = wfs.getFeatureReader(query, new DefaultTransaction());
        int expected=0;
        while (reader.hasNext()){
            expected++;
            reader.next();
        }
        query=new DefaultQuery("tiger:tiger_roads", filter, 100, new String[]{"CFCC"}, "");
        reader = wfs.getFeatureReader(query, new DefaultTransaction());
        int count=0;
        while (reader.hasNext()){
            count++;
            reader.next();
        }
        
        assertEquals(expected, count);
    }
    
    /**
     * Writing test that only engages against a remote geoserver.
     * <p>
     * Makes reference to the standard featureTypes that geoserver ships with.
     * </p>
     */
    public void testWrite() throws NoSuchElementException, IllegalFilterException, IOException, IllegalAttributeException{

        Map m = new HashMap();
        m.put(WFSDataStoreFactory.URL.key,url);
        m.put(WFSDataStoreFactory.TIMEOUT.key,new Integer(10000000));
        DataStore post = (WFSDataStore)(new WFSDataStoreFactory()).createNewDataStore(m);  
        String typename = TO_EDIT_TYPE;
        FeatureType ft = post.getSchema( typename );
        FeatureSource fs = post.getFeatureSource( typename );        
        class Watcher implements FeatureListener {
            public int count=0;
            public void changed( FeatureEvent featureEvent ) {
                System.out.println("Event "+featureEvent );
                count++;
            }            
        }
        Watcher watcher = new Watcher();
        fs.addFeatureListener( watcher );
        
        Id startingFeatures=createFidFilter(fs);
        FilterFactory2 filterFac = CommonFactoryFinder.getFilterFactory2(GeoTools.getDefaultHints());
        try{
        GeometryFactory gf = new GeometryFactory();
        MultiPolygon mp = gf.createMultiPolygon(new Polygon[]{gf.createPolygon(gf.createLinearRing(new Coordinate[]{new Coordinate(-88.071564,37.51099), new Coordinate(-88.467644,37.400757), new Coordinate(-90.638329,42.509361), new Coordinate(-89.834618,42.50346),new Coordinate(-88.071564,37.51099)}),new LinearRing[]{})});
        mp.setUserData("http://www.opengis.net/gml/srs/epsg.xml#"+EPSG_CODE);
      
        PropertyName geometryAttributeExpression = filterFac.property(ft.getDefaultGeometry().getName());
		PropertyIsNull geomNullCheck = filterFac.isNull(geometryAttributeExpression);
		Query query=new DefaultQuery(typename, filterFac.not(geomNullCheck), 1, Query.ALL_NAMES, null);
        FeatureIterator inStore = fs.getFeatures(query).features();
        
        Feature f,f2;
        try{
            Feature feature = inStore.next();
            f = ft.create(ft.duplicate(feature).getAttributes(new Object[ft.getAttributeCount()]));
            f2 = ft.create(ft.duplicate(feature).getAttributes(new Object[ft.getAttributeCount()]));
            assertFalse("Max Feature failed", inStore.hasNext());
        }finally{
            inStore.close();
        }
        
        Logger.getLogger("org.geotools.data.wfs").setLevel(Level.FINE);
        FeatureCollection inserts = DataUtilities.collection(new Feature[] {f,f2});
        Id fp = WFSDataStoreWriteOnlineTest.doInsert(post,ft,inserts);
        
        /// okay now count ...
        FeatureReader count = post.getFeatureReader(new DefaultQuery(ft.getTypeName()),Transaction.AUTO_COMMIT);        
        int i = 0;
        while(count.hasNext() && i<3){
            f = count.next();i++;
        }
        count.close();       

        WFSDataStoreWriteOnlineTest.doDelete(post,ft,fp);
        WFSDataStoreWriteOnlineTest.doUpdate(post,ft, ATTRIBUTE_TO_EDIT, NEW_EDIT_VALUE);
//        assertFalse("events not fired", watcher.count == 0);
        }finally{
        	try{
        	((FeatureStore)fs).removeFeatures(filterFac.not(startingFeatures));
        	}catch (Exception e) {
        		System.out.println(e);
			}
        }
    }
	private Id createFidFilter(FeatureSource fs) throws IOException {
		FeatureIterator iter = fs.getFeatures().features();
        FilterFactory2 ffac = CommonFactoryFinder.getFilterFactory2(GeoTools.getDefaultHints());
        Set fids = new HashSet();
		try{
			while(iter.hasNext()){
				String id = iter.next().getID();
                FeatureId fid = ffac.featureId(id);
                fids.add(fid);
            }
            Id filter = ffac.id(fids);
			return filter;
		}finally{
			iter.close();
		}
	}    
}
