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
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.filter.AttributeExpression;
import org.geotools.filter.BBoxExpression;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.FilterFactoryFinder;
import org.geotools.filter.FilterType;
import org.geotools.filter.GeometryFilter;
import org.geotools.filter.IllegalFilterException;
import org.xml.sax.SAXException;

import com.vividsolutions.jts.geom.Envelope;

/**
 * <p> 
 * DOCUMENT ME!
 * </p>
 * @author dzwiers
 *
 * @source $URL$
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
        m.put(WFSDataStoreFactory.TIMEOUT.key,new Integer(10000)); // not debug
        m.put(WFSDataStoreFactory.TIMEOUT.key,new Integer(1000000)); //for debug
        return (WFSDataStore)(new WFSDataStoreFactory()).createNewDataStore(m);

        }catch(java.net.SocketException se){
            se.printStackTrace();
            return null;
        }
    }
        
    public static void doFeatureType(URL url,boolean get, boolean post, int i) throws IOException, SAXException{
    	if( url == null) return;
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
    	if( url == null) return;
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
    
    public static void doFeatureReaderWithQuery(URL url, boolean get, boolean post, int i) throws NoSuchElementException, IllegalAttributeException, IOException, SAXException{
    	if( url == null) return;
    	try{
        System.out.println("FeatureReaderWithFilterTest + "+url);
        WFSDataStore wfs = getDataStore(url);
        assertNotNull("No featureTypes",wfs.getTypeNames());
        assertNotNull("Null featureType in [0]",wfs.getTypeNames()[i]);
        FeatureType ft = wfs.getSchema(wfs.getTypeNames()[i]);
        // take atleast attributeType 3 to avoid the undeclared one .. inherited optional attrs
        
        String[] props;
        props = new String[] {ft.getDefaultGeometry().getName()};
        
        DefaultQuery query = new DefaultQuery(ft.getTypeName());
        query.setPropertyNames(props);
        String fid=null;
        if(get){
            // 	get
            FeatureReader fr = wfs.getFeatureReaderGet(query,Transaction.AUTO_COMMIT);
            try{
                assertNotNull("FeatureType was null",ft);
                
                FeatureType featureType = fr.getFeatureType();
                if( ft.getAttributeCount()>1 ){
                    assertEquals("Query must restrict feature type to only having 1 AttributeType", 1, featureType.getAttributeCount() );
                }
                assertTrue("must have 1 feature -- fair assumption",fr.hasNext() && featureType!=null );
                Feature feature = fr.next();
                featureType=feature.getFeatureType();
                if( ft.getAttributeCount()>1 ){
                    assertEquals("Query must restrict feature type to only having 1 AttributeType", 1, featureType.getAttributeCount() );
                }
                assertNotNull( "must have 1 feature ", feature);
                fid=feature.getID();
                int j=0;while(fr.hasNext()){ 
                    fr.next();
                    j++;
                }
                System.out.println(j+" Features");
            }finally{
                fr.close();
            }
        }if(post){
            // 	post

            FeatureReader fr = wfs.getFeatureReaderPost(query,Transaction.AUTO_COMMIT);
            try{
                assertNotNull("FeatureType was null",ft);
                FeatureType featureType = fr.getFeatureType();
                if( ft.getAttributeCount()>1 ){
                    assertEquals("Query must restrict feature type to only having 1 AttributeType", 1, featureType.getAttributeCount() );
                }
                assertTrue("must have 1 feature -- fair assumption",fr.hasNext() && featureType!=null );
                Feature feature = fr.next();
                featureType=feature.getFeatureType();
                if( ft.getAttributeCount()>1 ){
                    assertEquals("Query must restrict feature type to only having 1 AttributeType", 1, featureType.getAttributeCount() );
                }
                assertNotNull( "must have 1 feature ", feature);
                fid=feature.getID();
                int j=0;while(fr.hasNext()){ 
                    fr.next();
                    j++;
                }
                System.out.println(j+" Features");
            }finally{
                fr.close();
            }
        }

        // test fid filter 
        query.setFilter(FilterFactoryFinder.createFilterFactory().createFidFilter(fid));
        if( get ){
            FeatureReader fr = wfs.getFeatureReaderGet(query,Transaction.AUTO_COMMIT);
            try{
                assertNotNull("FeatureType was null",ft);
                int j=0;while(fr.hasNext()){ assertEquals(fid,fr.next().getID());j++;}
                assertEquals( 1,j );
            }finally{
                fr.close();
            }
        }if (post){
            FeatureReader fr = wfs.getFeatureReaderPost(query,Transaction.AUTO_COMMIT);
            try{
                assertNotNull("FeatureType was null",ft);
                int j=0;while(fr.hasNext()){ assertEquals(fid,fr.next().getID());j++;}
                assertEquals( 1,j );
            }finally{
                fr.close();
            }
        }
        }catch(java.net.SocketException se){
            se.printStackTrace();
        }
        
    }
       /** Request a subset of available properties 
     * @throws IllegalFilterException */
    public static void doFeatureReaderWithBBox(URL url, boolean get, boolean post, int i, Envelope bbox) throws NoSuchElementException, IllegalAttributeException, IOException, SAXException, IllegalFilterException{
        if( url == null ) return; // test distabled (must be site specific)                
        try{
        System.out.println("FeatureReaderWithFilterTest + "+url);
        WFSDataStore wfs = getDataStore(url);
        assertNotNull("No featureTypes",wfs.getTypeNames());
        assertNotNull("Null featureType in [0]",wfs.getTypeNames()[i]);
        FeatureType ft = wfs.getSchema(wfs.getTypeNames()[i]);
        // take atleast attributeType 3 to avoid the undeclared one .. inherited optional attrs
        
        
        FilterFactory factory = FilterFactoryFinder.createFilterFactory();
        
        DefaultQuery query = new DefaultQuery(ft.getTypeName());
        BBoxExpression theBBox = factory.createBBoxExpression( bbox );
        AttributeExpression theGeom = factory.createAttributeExpression( ft, ft.getDefaultGeometry().getName() );
        
        GeometryFilter filter = factory.createGeometryFilter( FilterType.GEOMETRY_BBOX );
        filter.addLeftGeometry( theGeom );
        filter.addRightGeometry( theBBox );
        query.setFilter( filter );
        //query.setMaxFeatures(3);
        if(get){
            //  get
            FeatureReader fr = wfs.getFeatureReaderGet(query,Transaction.AUTO_COMMIT);
            assertNotNull("FeatureType was null",ft);
            assertTrue("must have 1 feature -- fair assumption",fr.hasNext() && fr.getFeatureType()!=null && fr.next()!=null);
            int j=0;while(fr.hasNext()){fr.next();j++;}
            System.out.println("bbox selected "+j+" Features");
            fr.close();
        }if(post){
            //  post

            FeatureReader fr = wfs.getFeatureReaderPost(query,Transaction.AUTO_COMMIT);
            assertNotNull("FeatureType was null",ft);
            assertTrue("must have 1 feature -- fair assumption",fr.hasNext() && fr.getFeatureType()!=null && fr.next()!=null);
            int j=0;while(fr.hasNext()){fr.next();j++;}
            System.out.println("bbox selected "+j+" Features");
            fr.close();
        }
        }catch(java.net.SocketException se){
            se.printStackTrace();
        }
    }
}
