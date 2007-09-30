/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 */
package org.geotools.arcsde.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import junit.framework.TestCase;

import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureReader;
import org.geotools.data.Query;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.Id;

import com.esri.sde.sdk.client.SeConnection;
import com.vividsolutions.jts.geom.Envelope;


/**
 * DOCUMENT ME!
 *
 * @author Gabriel Roldan
 * @source $URL$
 * @version $Revision: 1.9 $
 */
public class ArcSDEQueryTest extends TestCase {

    private TestData testData;

    /**
     * do not access it directly, use {@link #getQueryAll()}
     */
    private ArcSDEQuery queryAll;

    /**
     * do not access it directly, use {@link #getQueryFiltered()}
     */
    private ArcSDEQuery queryFiltered;
    
    private ArcSDEDataStore dstore;
    
    private String typeName;
	
    private Query filteringQuery;
    
    private FilterFactory ff = CommonFactoryFinder.getFilterFactory(null);

    private SimpleFeatureType ftype;
    
    private static final int FILTERING_COUNT = 3;
	/**
     * Constructor for ArcSDEQueryTest.
     *
     * @param arg0
     */
    public ArcSDEQueryTest(String name) {
        super(name);
    }

    /**
     * loads {@code test-data/testparams.properties} into a Properties object, wich is
     * used to obtain test tables names and is used as parameter to find the DataStore
     *
     * @throws Exception DOCUMENT ME!
     */
    protected void setUp() throws Exception {
        super.setUp();
        this.testData = new TestData();
        this.testData.setUp();
        dstore = testData.getDataStore();
        typeName = testData.getLine_table();
        this.ftype = dstore.getSchema(typeName);
        
        //grab some fids
        FeatureReader reader = dstore.getFeatureReader(typeName);
        List fids = new ArrayList();
        for(int i = 0; i < FILTERING_COUNT; i++){
        	fids.add(ff.featureId(reader.next().getID()));
        }
        reader.close();
        Id filter = ff.id(new HashSet(fids));
        filteringQuery = new DefaultQuery(typeName, filter);
    }

    private ArcSDEQuery getQueryAll() throws IOException{
        this.queryAll = ArcSDEQuery.createQuery(dstore, ftype, Query.ALL);
        return this.queryAll;
    }
    
    private ArcSDEQuery getQueryFiltered() throws IOException{
        this.queryFiltered = ArcSDEQuery.createQuery(dstore, filteringQuery);
        return this.queryFiltered;
    }

    /**
     * DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    protected void tearDown() throws Exception {
        super.tearDown();
        try{
        	this.queryAll.close();
        }catch(Exception e){
        	//no-op
        }
        try{
        	this.queryFiltered.close();
        }catch(Exception e){
        	//no-op
        }
        this.queryAll = null;
        this.queryFiltered = null;
        testData.tearDown(true, true);
        testData = null;
    }

    /**
     * DOCUMENT ME!
     */
    public void testClose()throws IOException {
        ArcSDEQuery queryAll = getQueryAll();
    	assertNotNull(queryAll.connection);
    	
    	queryAll.execute();
    	
    	assertNotNull(queryAll.connection);
    	
    	//should nevel do this, just to assert it is 
    	//not closed by returned to the pool
    	SeConnection conn = queryAll.connection;
    	
    	queryAll.close();

    	assertNull(queryAll.connection);
    	assertFalse(conn.isClosed());
    }

    /**
     * DOCUMENT ME!
     */
    public void testFetch()throws IOException {
        ArcSDEQuery queryAll = getQueryAll();
    	try {
    		queryAll.fetch();
    		fail("fetch without calling execute");
    	}catch(IllegalStateException e){
    		//ok
    	}
    	
    	queryAll.execute();
    	assertNotNull(queryAll.fetch());
    	
    	queryAll.close();
    	try{
    		queryAll.fetch();
    		fail("fetch after close!");
    	}catch(IllegalStateException e){
    		//ok
    	}
    }

    /**
     * DOCUMENT ME!
     */
    public void testCalculateResultCount() throws Exception{
    	FeatureReader reader = dstore.getFeatureReader(typeName);
    	int readed = 0;
    	while(reader.hasNext()){
    		reader.next();
    		readed++;
    	}
    	
    	int calculated = getQueryAll().calculateResultCount();
    	assertEquals(readed, calculated);
    	
    	calculated = getQueryFiltered().calculateResultCount();
    	assertEquals(FILTERING_COUNT, calculated);
    }

    /**
     * DOCUMENT ME!
     */
    public void testCalculateQueryExtent()throws Exception {
    	FeatureReader reader = dstore.getFeatureReader(typeName);
    	ReferencedEnvelope real = new ReferencedEnvelope();
    	while(reader.hasNext()){
    		real.include(reader.next().getBounds());
    	}
    	
    	Envelope e = getQueryAll().calculateQueryExtent();
    	assertNotNull(e);
    	assertEquals(real, e);
    	
    	reader.close();
    
    	reader = dstore.getFeatureReader(typeName, filteringQuery);
    	real = new ReferencedEnvelope();
    	while(reader.hasNext()){
    		real.include(reader.next().getBounds());
    	}
    	
    	e = getQueryFiltered().calculateQueryExtent();
    	assertNotNull(e);
    	assertEquals(real, e);
    	reader.close();
    }

}
