/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, GeoTools Project Managment Committee (PMC)
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
package org.geotools.data.shapefile.indexed;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import junit.framework.AssertionFailedError;

import org.geotools.TestData;
import org.geotools.data.DataStore;
import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureStore;
import org.geotools.data.Query;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.filter.FidFilter;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.FilterFactoryFinder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;


/**
 * @source $URL$
 * @version $Id$
 * @author Ian Schneider
 */
public class ShapefileQuadTreeReadWriteTest extends TestCaseSupport {
    final String[] files = {
        "shapes/statepop.shp",
        "shapes/polygontest.shp",
        "shapes/pointtest.shp",
        "shapes/holeTouchEdge.shp",
        "shapes/stream.shp"
    };
    boolean readStarted = false;
    Exception exception = null;

    /**
     * Creates a new instance of ShapefileReadWriteTest
     */
    public ShapefileQuadTreeReadWriteTest(String name) throws IOException {
        super(name);
    }

    public void testAll() throws Throwable {
        StringBuffer errors = new StringBuffer();
        Exception bad = null;

        for (int i = 0, ii = files.length; i < ii; i++) {
            try {
                test(files[i]);
            } catch (Exception e) {
                e.printStackTrace();
                errors.append("\nFile " + files[i] + " : " + e.getMessage());
                bad = e;
            }
        }

        if (errors.length() > 0) {
            fail(errors.toString(), bad);
        }
    }

    public void fail(String message, Throwable cause) throws Throwable {
        Throwable fail = new AssertionFailedError(message);
        fail.initCause(cause);
        throw fail;
    }

    
    public void testWriteTwice() throws Exception {
        copyShapefiles("shapes/stream.shp");
		IndexedShapefileDataStoreFactory fac=new IndexedShapefileDataStoreFactory();
    	DataStore s1 = createDataStore(fac, TestData.url(this, "shapes/stream.shp"), true);
        String typeName = s1.getTypeNames()[0];
        FeatureSource source = s1.getFeatureSource(typeName);
        SimpleFeatureType type = source.getSchema();
        FeatureCollection one = source.getFeatures();

        IndexedShapefileDataStoreFactory maker = new IndexedShapefileDataStoreFactory();

        doubleWrite(type, one, getTempFile(), maker, false);
        doubleWrite(type, one, getTempFile(), maker, true);
	}

	private DataStore createDataStore(IndexedShapefileDataStoreFactory fac, URL url, boolean memoryMapped) throws IOException {
		Map params=new HashMap();
		params.put( IndexedShapefileDataStoreFactory.URLP.key, url);
		params.put( IndexedShapefileDataStoreFactory.CREATE_SPATIAL_INDEX.key, new Boolean(true));
		DataStore createDataStore = fac.createDataStore(params);
		return createDataStore;
	}

	private void doubleWrite(SimpleFeatureType type, FeatureCollection one, File tmp,
                             IndexedShapefileDataStoreFactory maker, boolean memorymapped)
            throws IOException, MalformedURLException {
		DataStore s;
		s =  createDataStore(maker, tmp.toURL(),
		        memorymapped);

		s.createSchema(type);
		FeatureStore store = (FeatureStore) s.getFeatureSource(type.getTypeName());
		
		store.addFeatures( one );		
		store.addFeatures( one );

		s = createDataStore(maker,tmp.toURL(), true);
		assertEquals(one.size()*2, store.getCount(Query.ALL));
	}
   
    void test(String f) throws Exception {
        File file=copyShapefiles(f); // Work on File rather than URL from JAR.
        DataStore s = createDataStore(new IndexedShapefileDataStoreFactory(),file.toURL(), true);
        String typeName = s.getTypeNames()[0];
        FeatureSource source = s.getFeatureSource(typeName);
        SimpleFeatureType type = source.getSchema();
        FeatureCollection one = source.getFeatures();

        IndexedShapefileDataStoreFactory maker = new IndexedShapefileDataStoreFactory();
        test(type, one, getTempFile(), maker, false);
        test(type, one, getTempFile(), maker, true);
    }

    private void test(SimpleFeatureType type, FeatureCollection one, File tmp,
        IndexedShapefileDataStoreFactory maker, boolean memorymapped)
        throws IOException, MalformedURLException, Exception {
        DataStore s;
        String typeName;
        s = createDataStore(maker, tmp.toURL(),
                memorymapped);


        
        s.createSchema(type);

        
        FeatureStore store = (FeatureStore) s.getFeatureSource(type.getTypeName());
        
        store.addFeatures( one );

        s = createDataStore(new IndexedShapefileDataStoreFactory(), tmp.toURL(), true);
        typeName = s.getTypeNames()[0];

        FeatureCollection two = s.getFeatureSource(typeName).getFeatures();
        
        compare(one.features(), two.features() );
    }

    static void compare(FeatureIterator fs1, FeatureIterator fs2)
        throws Exception {

        int i = 0;

        while (fs1.hasNext()) {
            SimpleFeature f1 = fs1.next();
            SimpleFeature f2 = fs2.next();

            if ((i++ % 50) == 0) {
                if (verbose) {
                    System.out.print("*");
                }
            }

            compare(f1, f2);
        }
    }

    static void compare(SimpleFeature f1, SimpleFeature f2) throws Exception {
        if (f1.getAttributeCount() != f2.getAttributeCount()) {
            throw new Exception("Unequal number of attributes");
        }

        for (int i = 0; i < f1.getAttributeCount(); i++) {
            Object att1 = f1.getAttribute(i);
            Object att2 = f2.getAttribute(i);

            if (att1 instanceof Geometry && att2 instanceof Geometry) {
                Geometry g1 = ((Geometry) att1);
                Geometry g2 = ((Geometry) att2);
                g1.normalize();
                g2.normalize();

                if (!g1.equalsExact(g2)) {
                    throw new Exception("Different geometries (" + i + "):\n"
                        + g1 + "\n" + g2);
                }
            } else {
                if (!att1.equals(att2)) {
                    throw new Exception("Different attribute (" + i + "): ["
                        + att1 + "] - [" + att2 + "]");
                }
            }
        }
    }
    
    
    /**
     * Test optimized getBounds().
     * 
     * Testing when filter is a bbox filter and a fidfilter
     * 
     * @throws Exception
     */
    public void testGetBoundsQuery() throws Exception {
        File file = copyShapefiles("shapes/streams.shp");

        IndexedShapefileDataStoreFactory fac=new IndexedShapefileDataStoreFactory();
        
        Map params=new HashMap();
        params.put( IndexedShapefileDataStoreFactory.URLP.key,  file.toURL());
        params.put( IndexedShapefileDataStoreFactory.CREATE_SPATIAL_INDEX.key, new Boolean(true));
        IndexedShapefileDataStore ds = (IndexedShapefileDataStore) fac.createDataStore(params);
        
        FilterFactory ff=FilterFactoryFinder.createFilterFactory();

        FeatureCollection features = ds.getFeatureSource().getFeatures(ff.createFidFilter("streams.84"));
        FeatureIterator iter = features.features();
        ReferencedEnvelope bounds;
        try{
            bounds = new ReferencedEnvelope(iter.next().getBounds());
        }finally{
            iter.close();
        }
        
        FidFilter filter = ff.createFidFilter("streams.84");
        Query query=new DefaultQuery(ds.getCurrentTypeName(), filter);
        
        Envelope result = ds.getFeatureSource().getBounds(query);
        
        //assertEquals(bounds, result);
        assertTrue(result.equals(bounds));
    }

    public static final void main(String[] args) throws Exception {
        verbose = true;
        junit.textui.TestRunner.run(suite(ShapefileQuadTreeReadWriteTest.class));
    }
}
