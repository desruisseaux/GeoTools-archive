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
/*
 * Created on May 2, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.gce.geotiff;

import junit.framework.TestCase;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.data.coverage.grid.AbstractGridFormat;
import org.geotools.gce.geotiff.GeoTiffFormat;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.opengis.coverage.grid.GridCoverageReader;
import org.opengis.coverage.grid.GridCoverageWriter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import javax.media.jai.PlanarImage;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;


/**
 * DOCUMENT ME!
 *
 * @author giannecchini TODO To change the template for this generated type
 *         comment go to Window - Preferences - Java - Code Style - Code
 *         Templates
 */
public class GeoTiffReaderWriterTest { // extends TestCase {

    /**
     * Constructor for GeoTiffReaderTest.
     *
     * @param arg0
     */
    public GeoTiffReaderWriterTest(String arg0) {
        //super(arg0);
    }

    public static void main(String[] args)
        throws IllegalArgumentException, IOException {
        //junit.textui.TestRunner.run(GeoTiffReaderTest.class);
        GeoTiffReaderWriterTest.testReader();
    }

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        //super.setUp();
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        //super.tearDown();
    }

    /**
     * testReader
     *
     * @throws IllegalArgumentException
     * @throws IOException
     */
    public static void testReader()
        throws IllegalArgumentException, IOException {
        File file = new File("c:\\a.tiff");

        //getting a reader
        AbstractGridFormat format = new GeoTiffFormat();

        if (format.accepts(file)) {
            GridCoverageReader reader = new GeoTiffReader(format, file, null);

            if (reader != null) {
                //reading the coverage
                GridCoverage2D gc = (GridCoverage2D) reader.read(null);

                if (gc != null) {
                    //displaying
                    JFrame frame = new JFrame();
                    JPanel topPanel = new JPanel();
                    topPanel.setLayout(new BorderLayout());
                    frame.getContentPane().add(topPanel);

                    frame.setBackground(Color.black);

                    JScrollPane pane = (JScrollPane) new JScrollPane();
                    pane.getViewport().add(new JLabel(
                            new ImageIcon(
                                ((PlanarImage) gc.getRenderedImage())
                                .getAsBufferedImage())));
                    topPanel.add(pane, BorderLayout.CENTER);
                    frame.getContentPane().add(pane);
                    frame.getContentPane().add(pane, BorderLayout.CENTER);
                    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    frame.pack();
                    frame.show();
                }
            }
        }
    }

    public static void testWriter()
        throws IllegalArgumentException, IOException {
        File file = new File(
                "D:\\Work\\data\\geotiff\\po_168213_0000000\\po_168213_nir_0000000.tif");

        //getting a reader
        AbstractGridFormat format = new GeoTiffFormat();

        if (format.accepts(file)) {
            GridCoverageReader reader = new GeoTiffReader(format, file, null);

            if (reader != null) {
                //reading the coverage
                GridCoverage2D gc = (GridCoverage2D) reader.read(null);

                if (gc != null) {
                    GridCoverageWriter writer = new GeoTiffWriter(new File(
                                "c:\\a.tiff"));
                    writer.write(gc, null);
                }
            }
        }
    }
}
