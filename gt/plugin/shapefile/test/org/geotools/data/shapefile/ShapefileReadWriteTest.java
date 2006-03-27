/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Managment Committee (PMC)
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
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.geotools.data.shapefile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

import junit.framework.AssertionFailedError;

import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureResults;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureStore;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.FeatureType;
import org.geotools.TestData;

import com.vividsolutions.jts.geom.Geometry;


/**
 *
 * @source $URL$
 * @version $Id$
 * @author Ian Schneider
 */
public class ShapefileReadWriteTest extends TestCaseSupport {
    final String[] files = {
        "shapes/statepop.shp",
        "shapes/polygontest.shp",
        "shapes/pointtest.shp",
        "shapes/holeTouchEdge.shp",
        "shapes/stream.shp"
    };

    /** Creates a new instance of ShapefileReadWriteTest */
    public ShapefileReadWriteTest(String name) throws IOException {
        super(name);
    }

    public void testAll() {
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

    boolean readStarted = false;

    Exception exception = null;

    public void testConcurrentReadWrite() throws Exception {
        System.gc();
        System.runFinalization(); // If some streams are still open, it may help to close them.
        final File file = getTempFile();
        final Boolean bool;
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

    private static void fail(String message, Throwable cause)  {
        AssertionFailedError fail = new AssertionFailedError(message);
        fail.initCause(cause);
        throw fail;
    }

    private void test(String f) throws Exception {
        copyShapefiles(f); // Work on File rather than URL from JAR.
        ShapefileDataStore s = new ShapefileDataStore(TestData.url(this, f));
        String typeName = s.getTypeNames()[0];
        FeatureSource source = s.getFeatureSource(typeName);
        FeatureType type = source.getSchema();
        FeatureResults one = source.getFeatures();
        File tmp = getTempFile();

        ShapefileDataStoreFactory maker = new ShapefileDataStoreFactory();
        test(type, one, tmp, maker, true);
        test(type, one, tmp, maker, false);
    }

    private void test(FeatureType type, FeatureResults one, File tmp,
            ShapefileDataStoreFactory maker, boolean memorymapped)
            throws IOException, MalformedURLException, Exception {
        ShapefileDataStore s;
        String typeName;
        s = (ShapefileDataStore) maker.createDataStore(tmp.toURL(),
                memorymapped);

        s.createSchema(type);
        FeatureStore store = (FeatureStore) s.getFeatureSource(type
                .getTypeName());
        FeatureReader reader = one.reader();
        assertNotNull( reader );
        try {
			store.addFeatures(reader);
		} finally {
			reader.close();
		}

		try {
		} catch (Exception e) {
			reader.close();
			// ignore
		}
                    
            s = new ShapefileDataStore(tmp.toURL());
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
        junit.textui.TestRunner.run(suite(ShapefileReadWriteTest.class));
    }
}
