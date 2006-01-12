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
 */
package org.geotools.data.shapefile.indexed;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import junit.framework.AssertionFailedError;

import com.vividsolutions.jts.geom.Geometry;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureResults;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureStore;
import org.geotools.data.Query;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.FeatureType;
import org.geotools.TestData;


/**
 * @version $Id: ShapefileReadWriteTest.java 17482 2006-01-09 22:10:05Z desruisseaux $
 * @author Ian Schneider
 */
public class ShapefileRTreeReadWriteTest extends TestCaseSupport {
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
    public ShapefileRTreeReadWriteTest(String name) throws IOException {
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
    	IndexedShapefileDataStore s1 = new IndexedShapefileDataStore(TestData.url(this, "shapes/stream.shp"));
        String typeName = s1.getTypeNames()[0];
        FeatureSource source = s1.getFeatureSource(typeName);
        FeatureType type = source.getSchema();
        FeatureCollection one = source.getFeatures();
        File tmp = getTempFile();

        IndexedShapefileDataStoreFactory maker = new IndexedShapefileDataStoreFactory();

        doubleWrite(type, one, tmp, maker, false);
        doubleWrite(type, one, tmp, maker, true);
	}

	private void doubleWrite(FeatureType type, FeatureCollection one, File tmp,
                             IndexedShapefileDataStoreFactory maker, boolean memorymapped)
            throws IOException, MalformedURLException {
		IndexedShapefileDataStore s;
		s = (IndexedShapefileDataStore) maker.createDataStore(tmp.toURL(),
		        memorymapped);

		s.createSchema(type);
		FeatureStore store = (FeatureStore) s.getFeatureSource(type.getTypeName());
		FeatureReader reader = one.reader();

		store.addFeatures(reader);
		reader = one.reader();
		store.addFeatures(reader);

		s = new IndexedShapefileDataStore(tmp.toURL());
		assertEquals(one.getCount()*2, store.getCount(Query.ALL));
	}
    
    public void testConcurrentReadWrite() throws Exception {
        System.gc();
        System.runFinalization(); // If some streams are still open, it may help to close them.
        final File file = getTempFile();
        Runnable reader = new Runnable() {
                public void run() {
                    int cutoff = 0;

                    try {
                        FileInputStream fr = new FileInputStream(file);

                        try {
                            fr.read();
                        } catch (IOException e1) {
                            exception = e1;

                            return;
                        }
                        if (verbose) {
                            System.out.println("locked");
                        }
                        readStarted = true;

                        while (cutoff < 10) {
                            synchronized (this) {
                                try {
                                    try {
                                        fr.read();
                                    } catch (IOException e) {
                                        exception = e;

                                        return;
                                    }

                                    wait(500);
                                    cutoff++;
                                } catch (InterruptedException e) {
                                    cutoff = 10;
                                }
                            }
                        }
                    } catch (FileNotFoundException e) {
                        assertTrue(false);
                    }
                }
            };

        Thread readThread = new Thread(reader);
        readThread.start();

        while (!readStarted) {
            if (exception != null) {
                throw exception;
            }

            Thread.yield();
        }

        test(files[0]);
    }

    void test(String f) throws Exception {
        copyShapefiles(f); // Work on File rather than URL from JAR.
        IndexedShapefileDataStore s = new IndexedShapefileDataStore(TestData.url(f));
        String typeName = s.getTypeNames()[0];
        FeatureSource source = s.getFeatureSource(typeName);
        FeatureType type = source.getSchema();
        FeatureCollection one = source.getFeatures();
        File tmp = getTempFile();

        IndexedShapefileDataStoreFactory maker = new IndexedShapefileDataStoreFactory();
        test(type, one, tmp, maker, false);
        test(type, one, tmp, maker, true);
    }

    private void test(FeatureType type, FeatureCollection one, File tmp,
        IndexedShapefileDataStoreFactory maker, boolean memorymapped)
        throws IOException, MalformedURLException, Exception {
        IndexedShapefileDataStore s;
        String typeName;
        s = (IndexedShapefileDataStore) maker.createDataStore(tmp.toURL(),
                memorymapped);


        
        s.createSchema(type);

        
        FeatureStore store = (FeatureStore) s.getFeatureSource(type.getTypeName());
        FeatureReader reader = one.reader();
        
        
        store.addFeatures(reader);

        s = new IndexedShapefileDataStore(tmp.toURL());
        typeName = s.getTypeNames()[0];

        FeatureResults two = s.getFeatureSource(typeName).getFeatures();

        compare(one.collection(), two.collection());
    }

    static void compare(FeatureCollection one, FeatureCollection two)
        throws Exception {
        if (one.size() != two.size()) {
            throw new Exception("Number of Features unequal : " + one.size()
                + " != " + two.size());
        }

        FeatureIterator fs1 = one.features();
        FeatureIterator fs2 = two.features();

        int i = 0;

        while (fs1.hasNext()) {
            Feature f1 = fs1.next();
            Feature f2 = fs2.next();

            if ((i++ % 50) == 0) {
                if (verbose) {
                    System.out.print("*");
                }
            }

            compare(f1, f2);
        }
    }

    static void compare(Feature f1, Feature f2) throws Exception {
        if (f1.getNumberOfAttributes() != f2.getNumberOfAttributes()) {
            throw new Exception("Unequal number of attributes");
        }

        for (int i = 0; i < f1.getNumberOfAttributes(); i++) {
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

    public static final void main(String[] args) throws Exception {
        verbose = true;
        junit.textui.TestRunner.run(suite(ShapefileRTreeReadWriteTest.class));
    }
}
