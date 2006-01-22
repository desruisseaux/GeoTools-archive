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
package org.geotools.gce.gtopo30;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.media.jai.JAI;
import javax.media.jai.TileCache;
import com.sun.media.jai.widget.DisplayJAI;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.opengis.coverage.grid.GridCoverageReader;
import org.geotools.resources.TestData;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.data.coverage.grid.AbstractGridFormat;


/**
 * @source $URL$
 * @version $Id$
 * @author giannecchini
 */
public class GT30ReaderWriterTest extends TestCase {
    /**
     * {@code true} for displaying the image in a window. Will be set to {@code true}
     * only if executed from the main method, in order to allow quieter maven build
     * when a full build is requested.
     */
    private static boolean verbose;

    /**
     * The image to test.
     */
    private String fileName = "W020N90";

    /**
     * Constructor for GT30 tests.
     */
    public GT30ReaderWriterTest(String arg0) {
        super(arg0);
    }

    /**
	 * Unpack the test files, if not already done.
     */
    protected void setUp() throws Exception {
        super.setUp();
        TestData.unzipFile(this, fileName + ".zip");
    }

    /**
     * Testing reader and writer for gtopo.
     */
    public void testReaderWriter() throws Exception {
        // Getting a resource to test
        URL statURL = TestData.url(this, fileName + ".DEM");
        AbstractGridFormat format = (AbstractGridFormat) new GTopo30FormatFactory().createFormat();
        TileCache defaultInstance = JAI.getDefaultInstance().getTileCache();
        defaultInstance.setMemoryCapacity(1024*1024*64);
        defaultInstance.setMemoryThreshold(1.0f);
        if (format.accepts(statURL)) {
            // Get a reader
            GridCoverageReader reader = format.getReader(statURL);

            // Get a grid coverage
            GridCoverage2D gc = ((GridCoverage2D) reader.read(null));
            assertNotNull(gc);
            assertNotNull(gc.geophysics(false).getRenderedImage());

            // Show the coverage
            if (verbose) {
                DisplayJAI display = new DisplayJAI();
                display.set(gc.geophysics(false).getRenderedImage());
                JFrame frame = new JFrame();
                frame.setTitle(statURL.toString());
                frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                frame.getContentPane().add(new JScrollPane(display));
                frame.setSize(500, 500);
                frame.setVisible(true);
                Thread.sleep(5000);
            }
            //delete all files and write them again to test
//            deleteAll();

            //write it
//            File testDir = getFile("");
//            GridCoverageWriter writer = format.getWriter(testDir);
//            writer.write(gc, null);

//            //read it again
//            reader = format.getReader(statURL);
//
//            //get a grid coverage
//            gc = ((GridCoverage2D) reader.read(null)).geophysics(false);
//
//            //show the coverage again
//            display.set(gc.getRenderedImage());
//            frame = new JFrame();
//            frame.setTitle(statURL.toString());
//            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//            frame.getContentPane().add(new JScrollPane(display));
//            frame.pack();
//            frame.setVisible(true);
        }
    }
/*
    public void testReaderWriterZip() throws IOException {
        URL statURL x getTestResource(this.fileName + ".DEM");
        AbstractGridFormat format = (AbstractGridFormat) new GTopo30FormatFactory().createFormat();
        if (format.accepts(statURL)) {
            //    	get a reader
            GridCoverageReader reader = format.getReader(statURL);

            //get a grid coverage
            GridCoverage2D gc = ((GridCoverage2D) reader.read(null));

            File zipFile = getFile("test.zip");
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(
                        zipFile));

            GridCoverageWriter writer = format.getWriter(out);
            writer.write(gc, null);
            out.flush();
            out.close();
        }
    }*/

    /**
     * Returns the test suite.
     */
    public static Test suite() {
        return new TestSuite(GT30ReaderWriterTest.class);
    }

    /**
     * Run the suite from the command line.
     */
    public static final void main(final String[] args) throws Exception {
        verbose = true;
        junit.textui.TestRunner.run(suite());
    }
}
