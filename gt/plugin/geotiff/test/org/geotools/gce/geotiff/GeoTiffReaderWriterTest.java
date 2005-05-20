/*
 * NOTICE OF RELEASE TO THE PUBLIC DOMAIN
 *
 * This work was created by employees of the USDA Forest Service's
 * Fire Science Lab for internal use.  It is therefore ineligible for
 * copyright under title 17, section 105 of the United States Code.  You
 * may treat it as you would treat any public domain work: it may be used,
 * changed, copied, or redistributed, with or without permission of the
 * authors, for free or for compensation.  You may not claim exclusive
 * ownership of this code because it is already owned by everyone.  Use this
 * software entirely at your own risk.  No warranty of any kind is given. 
 *
 * A copy of 17-USC-105 should have accompanied this distribution in the file
 * 17USC105.html.  If not, you may access the law via the US Government's
 * public websites:
 *   - http://www.copyright.gov/title17/92chap1.html#105
 *   - http://www.gpoaccess.gov/uscode/  (enter "17USC105" in the search box.)
 */
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

//import org.esa.beam.util.geotiff.GeoTIFFMetadata;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.data.coverage.grid.AbstractGridFormat;

//import org.geotools.gce.arcgrid.ArcGridReader;
import org.geotools.gce.geotiff.GeoTiffFormat;
import org.geotools.gce.geotiff.GeoTiffReader;

//import org.geotools.referencing.wkt.ParseWKT2GeoTiffMetadata;
import org.opengis.coverage.grid.GridCoverageReader;
import org.opengis.coverage.grid.GridCoverageWriter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
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
        GeoTiffReaderWriterTest.testWriter();
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
        //        File file = new File(
        //                "c:\\Programmi\\Apache Software Foundation\\Tomcat 5.0\\webapps\\geoserver\\data\\coverages\\arc_sample\\a.asc");
        //
        //        //getting a reader
        //        AbstractGridFormat format = new GeoTiffFormat();
        //
        //        if (format.accepts(file)) {
        //            GridCoverageReader reader = new ArcGridReader( file);
        //
        //            if (reader != null) {
        //                //reading the coverage
        //                GridCoverage2D gc = (GridCoverage2D) reader.read(null);
        //
        //                if (gc != null) {
        //                    GridCoverageWriter writer = new GeoTiffWriter(new File(
        //                                "c:\\a.tiff"));
        //                    writer.write(gc, null);
        //                }
        //				}
        //        }
        String wkt = "PROJCS[\"UTM_Zone_10N\", " + "GEOGCS[\"WGS84\", "
            + "DATUM[\"WGS84\", "
            + "SPHEROID[\"WGS84\", 6378137.0, 298.257223563]], "
            + "PRIMEM[\"Greenwich\", 0.0], "
            + "UNIT[\"degree\",0.017453292519943295], "
            + "AXIS[\"Longitude\",EAST], " + "AXIS[\"Latitude\",NORTH]], "
            + "PROJECTION[\"Transverse_Mercator\"], "
            + "PARAMETER[\"semi_major\", 6378137.0], "
            + "PARAMETER[\"semi_minor\", 6356752.314245179], "
            + "PARAMETER[\"central_meridian\", -123.0], "
            + "PARAMETER[\"latitude_of_origin\", 0.0], "
            + "PARAMETER[\"scale_factor\", 0.9996], "
            + "PARAMETER[\"false_easting\", 500000.0], "
            + "PARAMETER[\"false_northing\", 0.0], " + "UNIT[\"metre\",1.0], "
            + "AXIS[\"x\",EAST], " + "AXIS[\"y\",NORTH]]";

        //GeoTIFFMetadata metadata=new GeoTIFFMetadata();
        //ParseWKT2GeoTiffMetadata parser=new ParseWKT2GeoTiffMetadata(wkt,metadata);
        //    	try {
        //			parser.parseCoordinateReferenceSystem();
        //		} catch (ParseException e) {
        //			// TODO Auto-generated catch block
        //			e.printStackTrace();
        //		}
        //    	
        //    	System.out.println(metadata.getAsXML());
    }
}
