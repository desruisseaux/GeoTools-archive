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
package org.geotools.data;

import java.io.IOException;
import java.io.Reader;
import java.util.Set;

import org.geotools.data.memory.MemoryDataStore;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.feature.IllegalAttributeException;
import org.opengis.filter.Filter;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.FilterFactoryFinder;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

import junit.framework.TestCase;


/**
 *
 * @source $URL$
 */
public class TransactionTest extends TestCase {
    MemoryDataStore ds;
    FeatureType type;
    Geometry geom;
    
    protected void setUp() throws Exception {
        super.setUp();
        type=DataUtilities.createType("default", "name:String,*geom:Geometry");
        GeometryFactory fac=new GeometryFactory();
        geom=fac.createPoint(new Coordinate(10,10));
        Feature f1=type.create(new Object[]{ "original", geom });
        ds=new MemoryDataStore(new Feature[]{f1});
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public void testAddFeature() throws Exception{
        Feature f1=type.create(new Object[]{ "one",geom });
        Feature f2=type.create(new Object[]{ "two", geom });
        
        FeatureStore store=(FeatureStore) ds.getFeatureSource("default");
        store.setTransaction(new DefaultTransaction());
        store.addFeatures(new TestReader(type, f1));
        store.addFeatures(new TestReader(type, f2));
        
        count( store, 3);
//        assertEquals("Number of known feature as obtained from getCount",3, store.getCount(Query.ALL));
    }

    public void testRemoveFeature() throws Exception{
        Feature f1=type.create(new Object[]{ "one",geom });
        
        FeatureStore store=(FeatureStore) ds.getFeatureSource("default");
        store.setTransaction(new DefaultTransaction());
        Set fid=store.addFeatures(new TestReader(type, f1));

        count(store, 2);
        Filter f=FilterFactoryFinder.createFilterFactory().createFidFilter((String) fid.iterator().next());
        store.removeFeatures(f);
        
        count(store, 1);
//        assertEquals("Number of known feature as obtained from getCount",3, store.getCount(Query.ALL));
    }

	private void count(FeatureStore store, int expected) throws IOException, IllegalAttributeException {
		int i=0;
        for( FeatureReader reader=store.getFeatures().reader();
        reader.hasNext();){
            reader.next();
            i++;
        }
        assertEquals("Number of known feature as obtained from reader",expected, i);
	}
}
