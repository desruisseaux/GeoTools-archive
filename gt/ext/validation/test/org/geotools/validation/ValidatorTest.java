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
package org.geotools.validation;

import junit.framework.TestCase;
import org.geotools.data.DataStore;
import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultQuery;
import org.geotools.data.DefaultRepository;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureStore;
import org.geotools.feature.AttributeType;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureType;
import org.geotools.filter.Filter;

import com.vividsolutions.jts.geom.*;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


/**
 * ValidatorTest<br>
 *
 * @author bowens<br> Created Jun 28, 2004<br>
 * @version <br><b>Puropse:</b><br><p><b>Description:</b><br><p><b>Usage:</b><br><p>
 */
public class ValidatorTest extends TestCase {
    TestFixture fixture;

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        fixture = new TestFixture();
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
        fixture = null;
    }

    public void testRepositoryGeneration() throws Exception {
        //DefaultRepository dataRepository = new DefaultRepository();               
        assertNotNull(fixture.repository.datastore("LAKES"));
        assertNotNull(fixture.repository.datastore("STREAMS"));
        assertNotNull(fixture.repository.datastore("SWAMPS"));
        assertNotNull(fixture.repository.datastore("RIVERS"));

        Map types = fixture.repository.types();
        assertTrue( types.containsKey( "LAKES:lakes" ) );
        assertTrue( types.containsKey( "STREAMS:streams" ) );
        assertTrue( types.containsKey( "SWAMPS:swamps" ) );
        assertTrue( types.containsKey( "RIVERS:rivers" ) );        
    }

    public void testFeatureValidation() throws Exception {
    	FeatureSource lakes = fixture.repository.source( "LAKES", "lakes" );
    	FeatureReader reader = lakes.getFeatures().reader();
		DefaultFeatureResults results = new DefaultFeatureResults();    	
    	fixture.processor.runFeatureTests( "LAKES", lakes.getSchema(), reader, results );
    	reader.close();    	
    	assertEquals( "lakes test", 0, results.error.size() );
    	

    }
    public Feature invalidLake() throws Exception {
    	FeatureSource lakes = fixture.repository.source( "LAKES", "lakes" );
    	
    	FeatureReader reader = lakes.getFeatures( new DefaultQuery("lakes", Filter.NONE, 1, null, null) ).reader();
    	Feature feature = reader.next();
    	reader.close();
    	
    	FeatureType LAKE = lakes.getSchema();
    	Object array[] = new Object[ LAKE.getAttributeCount() ];
    	for( int i=0; i<LAKE.getAttributeCount(); i++){
    		AttributeType attr = LAKE.getAttributeType( i );
    		// System.out.println( i+" "+attr.getType()+":"+attr.getName()+"="+feature.getAttribute( i )  );
    		if( LAKE.getDefaultGeometry() == attr ){
    			GeometryFactory factory = new GeometryFactory();
    			Coordinate coords[] = new Coordinate[]{
    					new Coordinate( 1, 1 ),new Coordinate( 2, 2 ),
						new Coordinate( 2, 1 ),new Coordinate( 1, 2 ),
						new Coordinate( 1, 1 ),
    			};
    			LinearRing ring = factory.createLinearRing( coords );
    			Polygon poly = factory.createPolygon( ring, null );
    			array[i] = factory.createMultiPolygon( new Polygon[]{ poly, } ); 
    		}
    		else {
    			array[i] = feature.getAttribute( i );
    		}
    	}
    	return LAKE.create( array, "splash" );
    }
    public void testFeatureValidation2() throws Exception {
    	FeatureSource lakes = fixture.repository.source( "LAKES", "lakes" );
    	Feature newFeature = invalidLake();
    	    	
    	FeatureReader add = DataUtilities.reader( new Feature[]{ newFeature, } );
    	
    	DefaultFeatureResults results = new DefaultFeatureResults();    	
    	fixture.processor.runFeatureTests( "LAKES", lakes.getSchema(), add, results );
    	add.close();
    	assertEquals( "lakes test", 2, results.error.size() );
    }

    public void testIntegrityValidation() {
    }
}
