/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
package org.geotools.data.arcsde;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.geotools.data.DataStore;
import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureReader;
import org.geotools.data.Query;
import org.geotools.feature.FeatureType;
import org.geotools.filter.FidFilter;
import org.geotools.filter.Filter;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.FilterFactoryFinder;

import com.esri.sde.sdk.client.SeConnection;
import com.esri.sde.sdk.client.SeException;
import com.vividsolutions.jts.geom.Envelope;

import junit.framework.TestCase;


/**
 * DOCUMENT ME!
 *
 * @author $author$
 * @source $URL$
 * @version $Revision: 1.9 $
 */
public class ArcSDEQueryTest extends TestCase {

    private TestData testData;

    private ArcSDEQuery queryAll;
    private ArcSDEQuery queryFiltered;
    
    private ArcSDEDataStore dstore;
    
    private String typeName;
	
    private Query filteringQuery;
    
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
        FeatureType ftype = dstore.getSchema(typeName);
        this.queryAll = ArcSDEQuery.createQuery(dstore, ftype, Query.ALL);
        
        //grab some fids
        FeatureReader reader = dstore.getFeatureReader(typeName);
        List fids = new ArrayList();
        for(int i = 0; i < FILTERING_COUNT; i++){
        	fids.add(reader.next().getID());
        }
        reader.close();
        FidFilter filter = FilterFactoryFinder.createFilterFactory().createFidFilter();
        filter.addAllFids(fids);
        filteringQuery = new DefaultQuery(typeName, filter);
        this.queryFiltered = ArcSDEQuery.createQuery(dstore, filteringQuery);
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
    	assertNotNull(queryAll.connectionPool);
    	assertNull(queryAll.connection);
    	
    	this.queryAll.execute();
    	
    	assertNotNull(queryAll.connectionPool);
    	assertNotNull(queryAll.connection);
    	
    	//should nevel do this, just to assert it is 
    	//not closed by returned to the pool
    	SeConnection conn = queryAll.connection;
    	
    	this.queryAll.close();

    	assertNull(queryAll.connectionPool);
    	assertNull(queryAll.connection);
    	assertFalse(conn.isClosed());
    }

    /**
     * DOCUMENT ME!
     */
    public void testFetch()throws IOException {
    	try {
    		queryAll.fetch();
    		fail("fetch without calling execute");
    	}catch(IOException e){
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
    	
    	int calculated = queryAll.calculateResultCount();
    	assertEquals(readed, calculated);
    	
    	calculated = queryFiltered.calculateResultCount();
    	assertEquals(FILTERING_COUNT, calculated);
    }

    /**
     * DOCUMENT ME!
     */
    public void testCalculateQueryExtent()throws Exception {
    	FeatureReader reader = dstore.getFeatureReader(typeName);
    	Envelope real = new Envelope();
    	while(reader.hasNext()){
    		real.expandToInclude(reader.next().getBounds());
    	}
    	
    	Envelope e = queryAll.calculateQueryExtent();
    	assertNotNull(e);
    	assertEquals(real, e);
    	
    	reader.close();
    
    	reader = dstore.getFeatureReader(typeName, filteringQuery);
    	real = new Envelope();
    	while(reader.hasNext()){
    		real.expandToInclude(reader.next().getBounds());
    	}
    	
    	e = queryFiltered.calculateQueryExtent();
    	assertNotNull(e);
    	assertEquals(real, e);
    	reader.close();
    }

}
