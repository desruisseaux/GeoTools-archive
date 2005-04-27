/*
 *    uDig - User Friendly Desktop Internet GIS client
 *    http://udig.refractions.net
 *    (C) 2004, Refractions Research Inc.
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
package org.geotools.data;

import java.io.IOException;
import java.io.Reader;
import java.util.NoSuchElementException;

import org.geotools.data.memory.MemoryDataStore;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.feature.IllegalAttributeException;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

import junit.framework.TestCase;

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
        store.addFeatures(new Reader(f1));
        store.addFeatures(new Reader(f2));
        
        int i=0;
        for( FeatureReader reader=store.getFeatures().reader();reader.hasNext();){
            reader.next();
            i++;
        }
        assertEquals("Number of known feature as obtained from reader",3, i);
//        assertEquals("Number of known feature as obtained from getCount",3, store.getCount(Query.ALL));
    }
    
    class Reader implements FeatureReader{

        private Feature feature;

        public Reader(Feature f) {
            this.feature=f;
        }
        
        public FeatureType getFeatureType() {
            return type;
        }

        public Feature next() throws IOException, IllegalAttributeException, NoSuchElementException {
            next=false;
            return feature;
        }

        boolean next=true;
        public boolean hasNext() throws IOException {
            return next;
        }

        public void close() throws IOException {
        }
        
    }
}
