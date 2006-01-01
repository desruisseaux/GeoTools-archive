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
import java.io.IOException;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Iterator;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.resources.TestData;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;


/**
 * Base class for test suite. This class is not abstract for the purpose of
 * {@link TestCaseSupportTest}, but should not be instantiated otherwise.
 * It should be extented (which is why the constructor is protected).
 *
 * @version $Id$
 * @author  Ian Schneider
 * @author  Martin Desruisseaux
 */
public class TestCaseSupport extends TestCase {
    /**
     * Set to {@code true} if {@code println} are wanted during normal execution.
     * It doesn't apply to message displayed in case of errors.
     */
    protected static boolean verbose = false;

    /**
     * Make sure unzipping of data only occurs once per suite.
     */
    static boolean prepared = false;

    /**
     * Stores all temporary files here - delete on tear down.
     */
    private final ArrayList tmpFiles = new ArrayList();

    /**
     * Creates a new instance of {@code TestCaseSupport} with the given name.
     */
    protected TestCaseSupport(final String name) throws IOException {
        super(name);
        if (!prepared) {
            prepared = true;
            TestData.unzipFile(TestCaseSupport.class, "data.zip");
        }
    }

    /**
     * Deletes all temporary files created by {@link #getTempFile}.
     * This method is automatically run after each test.
     */
    protected void tearDown() throws Exception {
        // it seems that not all files marked as temp will get erased, perhaps
        // this is because they have been rewritten? Don't know, don't _really_
        // care, so I'll just delete everything
        final Iterator f = tmpFiles.iterator();
        while (f.hasNext()) {
            File tf = (File) f.next();
            sibling(tf, "dbf").delete();
            sibling(tf, "shx").delete();
            tf.delete();
            f.remove();
        }
        super.tearDown();
    }

    /**
     * Helper method for {@link #tearDown}.
     */
    private static File sibling(final File f, final String ext) {
        String name = f.getName();
        name = name.substring(0, name.indexOf('.') + 1);
        return new File(f.getParent(), name + ext);
    }

    /**
     * Read a geometry of the given name.
     *
     * @param  wktResource The resource name to load, without its {@code .wkt} extension.
     * @return The geometry.
     * @throws IOException if reading failed.
     */
    protected Geometry readGeometry(final String wktResource) throws IOException {
        final BufferedReader stream = TestData.openReader(TestCaseSupport.class, wktResource + ".wkt");
        final WKTReader reader = new WKTReader();
        final Geometry geom;
        try {
            geom = reader.read(stream);
        } catch (ParseException pe) {
            IOException e = new IOException("parsing error in resource " + wktResource);
            e.initCause(pe);
            throw e;
        }
        stream.close();
        return geom;
    }

    /**
     * Returns the first feature in the given feature collection.
     */
    protected Feature firstFeature(FeatureCollection fc) {
        return fc.features().next();
    }

    /**
     * Creates a temporary file, to be automatically deleted at the end of the test suite.
     */
    protected File getTempFile() throws IOException {
        File tmpFile = File.createTempFile("test-shp", ".shp");
        // keep track of all temp files so we can delete them
        tmpFiles.add(tmpFile);
        tmpFile.createNewFile();
        if (!tmpFile.exists()) {
            throw new IOException("Couldn't setup temp file");
        }
        tmpFile.deleteOnExit();
        return tmpFile;
    }

    /**
     * Returns the test suite for the given class.
     */
    public static Test suite(Class c) {
        return new TestSuite(c);
    }
}
