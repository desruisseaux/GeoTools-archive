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

import java.awt.BorderLayout;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;

import javax.media.jai.PlanarImage;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import junit.framework.TestCase;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.resources.TestData;
import org.opengis.coverage.grid.Format;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;


/**
 * Test class for WorldImageWriter. This test tries to read, writer and re-read
 * successive images checking for errors.
 *
 * @author giannecchini
 * @author rgould
 */
public class WorldImageWriterTest extends TestCase {
    WorldImageReader wiReader;
    ParameterValueGroup paramsRead = null;
    ParameterValueGroup paramsWrite = null;
    WorldImageWriter wiWriter;
    Object destination;

    public WorldImageWriterTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
//        File testDir = TestData.file(this, "");
//        String[] files = testDir.list();

        //        //removing unnecessary files
        //        if (files != null) {
        //            for (int i = 0; i < files.length; i++)
        //                if (!files[i].startsWith("etopo")
        //                        && !files[i].startsWith("usa")) {
        //                    (new File(testDir.getAbsolutePath() + "/" + files[i]))
        //                    .delete();
        //                }
        //        }
        super.tearDown();
    }

    public void testWrite()
        throws MalformedURLException, IOException, IllegalArgumentException, 
            FactoryException, TransformException, ParseException {
        URL url = null;
        File file = null;
        InputStream in = null;

        //checking test data directory for all kind of inputs
        File test_data_dir = null;
        test_data_dir = TestData.file(this, null);

        String[] fileList = test_data_dir.list(new MyFileFilter());

        for (int i = 0; i < fileList.length; i++) {
            //url
            url = TestData.url(this, fileList[i]);
            assertTrue(url != null);
            this.write(url);

            //getting file
            file = TestData.file(this, fileList[i]);
            assertTrue(file != null);            
            //starting write test            
            this.write(file);    
            
            //inputstream
            in = new FileInputStream(TestData.file(this, fileList[i]));
            this.write(in);
        }


    }

    /**
     * write
     *
     * @param source Object
     *
     * @throws IOException
     * @throws IllegalArgumentException
     * @throws FactoryException
     * @throws TransformException
     * @throws ParseException
     */
    private void write(Object source)
        throws IOException, IllegalArgumentException, FactoryException, 
            TransformException, ParseException {
        wiReader = new WorldImageReader(source);

        Format readerFormat = wiReader.getFormat();
        paramsRead = readerFormat.getReadParameters();

        //setting crs
        GridCoverage2D coverage = null;

        coverage = (GridCoverage2D) wiReader.read(null);
//        reprojectAndShow(coverage);

        assertNotNull(coverage);
        assertNotNull( coverage.getRenderedImage());
        assertNotNull(coverage.getEnvelope());

        //writing png
        File tempFile = null;

        //remember to provide a valid name, it wil be mde unique by the helper function
        //temp
        tempFile = File.createTempFile("temp", ".gif");
        tempFile.deleteOnExit();
        assertTrue(tempFile.exists());

        //writer
        wiWriter = new WorldImageWriter(tempFile);

        //writing parameters for png
        Format writerParams = wiWriter.getFormat();
        writerParams.getWriteParameters().parameter("Format").setValue("gif");

        //writing
        wiWriter.write(coverage, null);

        //reading again
        assertTrue(tempFile.exists());
        wiReader = new WorldImageReader(tempFile);

        coverage = (GridCoverage2D) wiReader.read(null);

        //displaying
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
//
//    private void reprojectAndShow(GridCoverage2D coverage)
//        throws FactoryException, TransformException, IllegalArgumentException, 
//            IOException, ParseException {
//        //crs authority
//        final CRSAuthorityFactory factory = FactoryFinder
//            .getCRSAuthorityFactory("EPSG", null);
//        final Parser parser = new Parser();
//        final AbstractCRS wgs84 = (AbstractCRS) factory
//            .createCoordinateReferenceSystem("EPSG:4326");
//
//        //mercator cs
//        //		final CSAuthorityFactory factoryCS=FactoryFinder.getCSAuthorityFactory("EPSG",null);
//        String wkt = "PROJCS[\"Mercator test\",\n" + "  GEOGCS[\"WGS84\",\n"
//            + "    DATUM[\"WGS84\",\n"
//            + "      SPHEROID[\"WGS84\", 6378137.0, 298.257223563]],\n"
//            + "    PRIMEM[\"Greenwich\", 0.0],\n"
//            + "    UNIT[\"degree\", 0.017453292519943295],\n"
//            + "    AXIS[\"Latitude\", NORTH],\n"
//            + "    AXIS[\"Longitude\", EAST]],\n"
//            + "  PROJECTION[\"Mercator_1SP\"],\n"
//            + 
//            //		 "  PARAMETER[\"latitude_of_origin\", 43.0],\n"             +         
//            "  PARAMETER[\"central_meridian\", 0.0],\n"
//            + "  PARAMETER[\"scale_factor\", 1.0],\n"
//            + "  PARAMETER[\"false_easting\", 500000.0],\n"
//            + "  PARAMETER[\"false_northing\", 0.0],\n"
//            + "  UNIT[\"m\", 1.0],\n" + "  AXIS[\"x\", EAST],\n"
//            + "  AXIS[\"y\", NORTH]]\n";
//
//        AbstractCRS mercator = (AbstractCRS) parser
//            .parseCoordinateReferenceSystem(wkt);
//
//        //getting an operation between source and destination crs
//        DefaultCoordinateOperationFactory operationFactory = new DefaultCoordinateOperationFactory(new Hints(
//                    Hints.LENIENT_DATUM_SHIFT, Boolean.TRUE));
//        AbstractCoordinateOperation operationMercator2WGS84 = (AbstractCoordinateOperation) operationFactory
//            .createOperation(mercator, wgs84);
//
//        //		AbstractCoordinateOperation operationWGS842Mercator=(AbstractCoordinateOperation) operationFactory.createOperation(wgs84,
//        //				mercator);
//        //		MathTransform transformWGS842Mercator=operationWGS842Mercator.getMathTransform();
//        //reproject the envelope
//        //        GeneralEnvelope newEnvelope=(GeneralEnvelope) coverage.getEnvelope(),
//        //		oldEnvelope=null;
//        //		oldEnvelope=new GeneralEnvelope(
//        //        		(GeneralDirectPosition )transformWGS842Mercator.transform(newEnvelope.getLowerCorner(),null),
//        //        		(GeneralDirectPosition )transformWGS842Mercator.transform(newEnvelope.getUpperCorner(),null)
//        //        		);
//        //		//construct the right coverage
//        //		GridCoverage2D gc=new GridCoverage2D(
//        //				"a",
//        //				coverage.getRenderedImage(),
//        //				mercator,
//        //				oldEnvelope);
//        //creating the new grid range keeping the old range
//        GeneralGridRange newGridrange = new GeneralGridRange(new int[] { 0, 0 },
//                new int[] {
//                    coverage.getGridGeometry().getGridRange().getLength(0),
//                    coverage.getGridGeometry().getGridRange().getLength(1)
//                });
//        GridGeometry2D newGridGeometry = new GridGeometry2D(newGridrange,
//                coverage.getEnvelope(), new boolean[] { false, true });
//
//        Operations processor2D = new Operations(new Hints(
//                    Hints.LENIENT_DATUM_SHIFT, Boolean.TRUE));
//        GridCoverage2D gcOp = (GridCoverage2D) processor2D.resample(coverage
//                .geophysics(false), wgs84, newGridGeometry, null);
//
//        //displaying
//        JFrame frame = new JFrame();
//        JPanel topPanel = new JPanel();
//        topPanel.setLayout(new BorderLayout());
//        frame.getContentPane().add(topPanel);
//
//        frame.setBackground(Color.black);
//
//        JScrollPane pane = new JScrollPane();
//        pane.getViewport().add(new JLabel(
//                new ImageIcon(
//                    ((PlanarImage) gcOp.getRenderedImage()).getAsBufferedImage())));
//        topPanel.add(pane, BorderLayout.CENTER);
//        frame.getContentPane().add(pane);
//        frame.getContentPane().add(pane, BorderLayout.CENTER);
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        frame.pack();
//        frame.show();
//
//        GridCoverageWriter writer = new WorldImageWriter(new File("c:\\a1.tif"));
//        writer.write(gcOp, null);
//
//        //        //printing
//        System.out.println(coverage.getCoordinateReferenceSystem().toWKT());
//        System.out.println(gcOp.getCoordinateReferenceSystem().toWKT());
//        System.out.println(gcOp.getEnvelope().toString());
//    }
}
