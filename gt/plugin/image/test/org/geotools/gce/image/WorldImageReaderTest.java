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
package org.geotools.gce.image;

import junit.framework.TestCase;
import org.geotools.coverage.grid.*;
import org.geotools.resources.*;
import org.opengis.coverage.grid.*;
import org.opengis.parameter.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import javax.media.jai.*;
import javax.swing.*;


/**
 * DOCUMENT ME!
 *
 * @author Simone Giannecchini
 * @author Alessio Fabiani
 * @author rgould
 * @source $URL$
 */
public class WorldImageReaderTest extends TestCase {
    private static boolean verbose = false;

    WorldImageReader wiReader;
    ParameterValueGroup paramsRead = null;

    /**
     * Constructor for WorldImageReaderTest.
     *
     * @param arg0
     */
    public WorldImageReaderTest(String arg0) {
        super(arg0);
    }

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testRead() throws IOException {
        URL url = null;
        File file = null;
        InputStream in = null;

        //checking test data directory for all kind of inputs
        File test_data_dir = TestData.file(this, null);
        String[] fileList = test_data_dir.list(new MyFileFilter());

        for (int i = 0; i < fileList.length; i++) {
            //url
            url = TestData.url(this, fileList[i]);
            this.read(url);

            //file
            file = TestData.file(this, fileList[i]);
            this.read(file);

            //inputstream
            in = new FileInputStream(TestData.file(this, fileList[i]));
            this.read(in);
        }

        //checking a WMS get map
//                url = new URL("http://localhost:8080/geoserver/wms?bbox=8.284,39.347,17.221,46.43&styles=raster&Format=image/png&request=GetMap&layers=OCP_BACKSCATT_MODIS_ACQUA_20050621&width=800&height=600&srs=EPSG:4326");
//                this.read(url);
    }

    /**
     * read
     *
     * @param source Object
     *
     * @throws FileNotFoundException DOCUMENT ME!
     * @throws IOException DOCUMENT ME!
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    private void read(Object source)
        throws FileNotFoundException, IOException, IllegalArgumentException {
        wiReader = new WorldImageReader(source);

        Format readerFormat = wiReader.getFormat();
        paramsRead = readerFormat.getReadParameters();

        GridCoverage2D coverage = (GridCoverage2D) wiReader.read(null);

        //(GeneralParameterValue[]) paramsRead.values().toArray(new GeneralParameterValue[paramsRead.values().size()]));
        assertNotNull(coverage);
        assertNotNull(((GridCoverage2D) coverage).getRenderedImage());
        assertNotNull(coverage.getEnvelope());

        if (verbose) {
            System.out.println(((GridCoverage2D) coverage).getCoordinateReferenceSystem().toWKT());
        }
        if (GraphicsEnvironment.isHeadless()) {
            return;
        }
        JFrame frame = new JFrame();
        JLabel label = new JLabel(new ImageIcon(
                    ((PlanarImage) coverage.getRenderedImage())
                    .getAsBufferedImage()));
        frame.getContentPane().add(label, BorderLayout.CENTER);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.show();
    }
}
