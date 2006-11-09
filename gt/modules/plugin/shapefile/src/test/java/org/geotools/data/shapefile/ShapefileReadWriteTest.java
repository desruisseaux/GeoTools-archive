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
package org.geotools.data.shapefile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

import junit.framework.AssertionFailedError;

import org.geotools.data.DataUtilities;
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
                System.out.println("File failed:"+files[i]+" "+e);
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
        Runnable reader = new Runnable() {
            public void run() {
                int cutoff = 0;
                FileInputStream fr = null;
                try {    
                    fr = new FileInputStream(file);
                    try {                        
                        fr.read();
                    } catch (IOException e1) {
                        exception = e1;
                        return;
                    }
//                    if (verbose) {
//                        System.out.println("locked");
//                    }
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
                finally {
                    if( fr != null ) {
                        try {
                            fr.close();
                        } catch (IOException e) {
                            exception = e;
                            return;
                        }
                    }
                }
            }
        };
        Thread readThread = new Thread(reader);
        readThread.start();
        while (!readStarted) {
            if (exception != null) {                
                throw exception;
            }
            Thread.sleep(100);
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
        FeatureCollection one = source.getFeatures();
        File tmp = getTempFile();

        ShapefileDataStoreFactory maker = new ShapefileDataStoreFactory();
        test(type, one, tmp, maker, true);
        
        File tmp2 = getTempFile(); // TODO consider reuse tmp results in failure
        test(type, one, tmp2, maker, false);
    }

    private void test(FeatureType type, FeatureCollection original, File tmp,
            ShapefileDataStoreFactory maker, boolean memorymapped)
            throws IOException, MalformedURLException, Exception {
        
        ShapefileDataStore shapefile;
        String typeName;
        shapefile = (ShapefileDataStore) maker.createDataStore(tmp.toURL(),
                memorymapped);       
        
        shapefile.createSchema(type);
                
        FeatureStore store = (FeatureStore) shapefile.getFeatureSource(type
                .getTypeName());
        
        store.addFeatures(original);
                    
        FeatureCollection copy = store.getFeatures();
        compare(original, copy);
        
        
        if( true ){
            // review open         
            ShapefileDataStore review = new ShapefileDataStore(tmp.toURL(), tmp.toURI(), memorymapped );        
            typeName = review.getTypeNames()[0];            
            FeatureSource featureSource = featureSource = review.getFeatureSource(typeName);
            FeatureCollection again = featureSource.getFeatures();
            
            compare(copy, again );
            compare(original, again );
        }
    }

    static void compare(FeatureCollection one, FeatureCollection two)
            throws Exception {

        if (one.size() != two.size()) {
            throw new Exception("Number of Features unequal : " + one.size()
                    + " != " + two.size());
        }

        FeatureIterator iterator1 = one.features();
        FeatureIterator iterator2 = two.features();

        int i = 0;
        while (iterator1.hasNext()) {
            Feature f1 = iterator1.next();
            Feature f2 = iterator2.next();
            compare(f1, f2);
        }
        iterator1.close();
        iterator2.close();
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
        //verbose = true;
        junit.textui.TestRunner.run(suite(ShapefileReadWriteTest.class));
    }
}
