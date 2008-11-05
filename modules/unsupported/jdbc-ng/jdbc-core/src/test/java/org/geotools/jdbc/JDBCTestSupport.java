/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2002-2008, Open Source Geospatial Foundation (OSGeo)
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
 */
package org.geotools.jdbc;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import junit.framework.TestCase;
import junit.framework.TestResult;

import org.geotools.data.DataStoreFactorySpi;
import org.geotools.feature.LenientFeatureFactoryImpl;
import org.geotools.feature.NameImpl;
import org.geotools.feature.type.FeatureTypeFactoryImpl;
import org.geotools.filter.FilterFactoryImpl;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.Name;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;


/**
 * Test support class for jdbc test cases.
 * <p>
 * This test class fires up a live instance of an h2 database to provide a
 * live database to work with.
 * </p>
 *
 * @author Justin Deoliveira, The Open Planning Project, jdeolive@openplans.org
 *
 */
public abstract class JDBCTestSupport extends TestCase {
    /**
     * map of test setup class to boolean which tracks which 
     * setups can obtain a connection and which cannot
     */
    static Map dataSourceAvailable = new HashMap();
    
    static {
        //uncomment to turn up logging
                
        java.util.logging.ConsoleHandler handler = new java.util.logging.ConsoleHandler();
        handler.setLevel(java.util.logging.Level.FINE);
        
        org.geotools.util.logging.Logging.getLogger("org.geotools.data.jdbc").setLevel(java.util.logging.Level.FINE);
        org.geotools.util.logging.Logging.getLogger("org.geotools.data.jdbc").addHandler(handler);
        
        org.geotools.util.logging.Logging.getLogger("org.geotools.jdbc").setLevel(java.util.logging.Level.FINE);
        org.geotools.util.logging.Logging.getLogger("org.geotools.jdbc").addHandler(handler);
         
    }

    protected JDBCTestSetup setup;
    protected JDBCDataStore dataStore;
    protected SQLDialect dialect;
    
    /**
     * Override to check if a database connection can be obtained, if not
     * tests are ignored.
     */
    public void run(TestResult result) {
        JDBCTestSetup setup = createTestSetup();
        
        //check if the data source is available for this setup
        Boolean available = 
            (Boolean) dataSourceAvailable.get( setup.getClass() );
        if ( available == null || available.booleanValue() ) {
            //test the connection
            try {
                DataSource dataSource = setup.createDataSource();
                Connection cx = dataSource.getConnection();
                cx.close();
            } catch (Throwable t) {
                System.out.println("Skipping tests " + getClass().getName() + " since data souce is not available: " + t.getMessage());
                dataSourceAvailable.put( setup.getClass(), Boolean.FALSE );
                return;
            }
            
            super.run(result);
        }
    }
    
    protected void setUp() throws Exception {
        super.setUp();

        //create the test harness
        if (setup == null) {
            setup = createTestSetup();
        }

        setup.setUp();

        //initialize the database
        setup.initializeDatabase();

        //initialize the data
        setup.setUpData();

        //create the dataStore
        //TODO: replace this with call to datastore factory
        HashMap params = new HashMap();
        params.put( JDBCDataStoreFactory.NAMESPACE.key, "http://www.geotools.org/test" );
        params.put( JDBCDataStoreFactory.SCHEMA.key, "geotools" );
        params.put( JDBCDataStoreFactory.DATASOURCE.key, setup.createDataSource() );
        
        JDBCDataStoreFactory factory = setup.createDataStoreFactory();
        dataStore = factory.createDataStore( params );
        
        setup.setUpDataStore(dataStore);
        dialect = dataStore.getSQLDialect();
    }

    protected abstract JDBCTestSetup createTestSetup();

    protected void tearDown() throws Exception {
        dataStore.dispose();
        setup.tearDown();
        super.tearDown();
    }
    
    /**
     * Returns the table name as the datastore understands it (some datastore are incapable of supporting
     * mixed case names for example)
     */
    protected String tname( String raw ) {
        return setup.typeName( raw );
    }
    
    /**
     * Returns the attribute name as the datastore understands it (some datastore are incapable of supporting
     * mixed case names for example)
     */
    protected String aname( String raw ) {
        return setup.attributeName( raw );
    }
    
    /**
     * Returns the attribute name as the datastore understands it (some datastore are incapable of supporting
     * mixed case names for example)
     */
    protected Name aname( Name raw ) {
        return new NameImpl( raw.getNamespaceURI(), aname( raw.getLocalPart() ) );
    }
    
    /**
     * Checkes the two feature types are equal, taking into consideration the eventual modification
     * the datastore had to perform in order to actually manage the type (change in names case, for example)
     */
    protected void assertFeatureTypesEqual(SimpleFeatureType expected, SimpleFeatureType actual) {
        for (int i = 0; i < expected.getAttributeCount(); i++) {
            AttributeDescriptor expectedAttribute = expected.getDescriptor(i);
            AttributeDescriptor actualAttribute = actual.getDescriptor(i);

            assertAttributesEqual(expectedAttribute,actualAttribute);
        }

        // make sure the geometry is nillable and has minOccurrs to 0
        if(expected.getGeometryDescriptor() != null) {
            AttributeDescriptor dg = actual.getGeometryDescriptor();
            assertTrue(dg.isNillable());
            assertEquals(0, dg.getMinOccurs());
        }
    }

    /**
     * Checkes the two feature types are equal, taking into consideration the eventual modification
     * the datastore had to perform in order to actually manage the type (change in names case, for example)
     */
    protected void assertAttributesEqual(AttributeDescriptor expected, AttributeDescriptor actual) {
        assertEquals(aname(expected.getName()), actual.getName());
        assertEquals(expected.getMinOccurs(), actual.getMinOccurs());
        assertEquals(expected.getMaxOccurs(), actual.getMaxOccurs());
        assertEquals(expected.isNillable(), actual.isNillable());
        assertEquals(expected.getDefaultValue(), actual.getDefaultValue());

        AttributeType texpected = expected.getType();
        AttributeType tactual = actual.getType();

        if ( Number.class.isAssignableFrom( texpected.getBinding() ) ) {
            assertTrue( Number.class.isAssignableFrom( tactual.getBinding() ) );
        }
        else if ( Geometry.class.isAssignableFrom( texpected.getBinding())) {
            assertTrue( Geometry.class.isAssignableFrom( tactual.getBinding()));
        }
        else {
            assertTrue(texpected.getBinding().isAssignableFrom(tactual.getBinding()));    
        }
        
    }
    
    protected boolean areCRSEqual(CoordinateReferenceSystem crs1, CoordinateReferenceSystem crs2) {
    	
    	if (crs1==null && crs2==null)
    		return true;
    	
    	if (crs1==null ) return false;
    		
    	return crs1.equals(crs2); 
   	}

}
