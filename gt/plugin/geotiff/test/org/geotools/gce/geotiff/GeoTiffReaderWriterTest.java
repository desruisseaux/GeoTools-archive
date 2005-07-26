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
package org.geotools.gce.geotiff;

import java.awt.BorderLayout;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;

import javax.media.jai.PlanarImage;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.geotools.coverage.grid.GeneralGridRange;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.coverage.operation.Resampler2D;
import org.geotools.coverage.processing.GridCoverageProcessor2D;
import org.geotools.data.coverage.grid.AbstractGridFormat;
import org.geotools.factory.Hints;
import org.geotools.geometry.GeneralDirectPosition;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.referencing.FactoryFinder;
import org.geotools.referencing.crs.CoordinateReferenceSystem;
import org.geotools.referencing.crs.EPSGCRSAuthorityFactory;
import org.geotools.referencing.operation.CoordinateOperationFactory;
import org.opengis.coverage.grid.GridCoverageReader;
import org.opengis.coverage.grid.GridCoverageWriter;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.operation.CoordinateOperation;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.OperationNotFoundException;
import org.opengis.referencing.operation.TransformException;


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
        throws IllegalArgumentException, IOException, 
            UnsupportedOperationException, ParseException, FactoryException, TransformException {
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
     * @throws FactoryException 
     * @throws OperationNotFoundException 
     * @throws TransformException 
     */
    public static void testReader()
        throws IllegalArgumentException, IOException, OperationNotFoundException, FactoryException, TransformException {
    	
        File file = new File("C:\\temp\\po_168213_grn_0000000.tiff");

        //getting a reader
        AbstractGridFormat format = new GeoTiffFormat();

        if (format.accepts(file)) {
            GridCoverageReader reader = new GeoTiffReader(format, file, null);

            if (reader != null) {
                //reading the coverage
                GridCoverage2D gc = (GridCoverage2D) reader.read(null);
                
							
                if (gc != null) {
					PlanarImage image=((PlanarImage)gc.getRenderedImage());
			       
                    //displaying
                    JFrame frame = new JFrame();
                    JPanel topPanel = new JPanel();
                    topPanel.setLayout(new BorderLayout());
                    frame.getContentPane().add(topPanel);


					JScrollPane pane = new JScrollPane();
					pane.getViewport().add(new JLabel(
                          new ImageIcon(
								  image.getAsBufferedImage())));
					topPanel.add(pane, BorderLayout.CENTER);
                    frame.getContentPane().add(pane, BorderLayout.CENTER);
                    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    frame.pack();
                    frame.show();                	
                    /**
                     * reprojecting
                     */
//					CRSAuthorityFactory factory=FactoryFinder.getCRSAuthorityFactory("EPSG",new Hints(Hints.CRS_AUTHORITY_FACTORY,EPSGCRSAuthorityFactory.class));
//					CoordinateReferenceSystem crs=(CoordinateReferenceSystem) factory.createCoordinateReferenceSystem("EPSG:4326");
//                      //getting an operation between source and destination crs
//                    CoordinateOperationFactory operationFactory=new CoordinateOperationFactory();
//                    CoordinateOperation operation=operationFactory.createOperation(gc.getCoordinateReferenceSystem(),
//							crs);
//                    MathTransform transform=operation.getMathTransform();
//                    //reproject the envelope
//                    GeneralEnvelope oldEnvelope=(GeneralEnvelope) gc.getEnvelope(),
//                    	newEnvelope=null;
//                    newEnvelope=new GeneralEnvelope(
//                    		(GeneralDirectPosition )transform.transform(oldEnvelope.getLowerCorner(),null),
//                    		(GeneralDirectPosition )transform.transform(oldEnvelope.getUpperCorner(),null)
//                    		);
//                    
//                    //creating the new grid range keeping the old range
//                    GeneralGridRange newGridrange = new GeneralGridRange(new int[] { 0, 0 },
//                            new int[] { gc.getGridGeometry().getGridRange().getLength(0),  gc.getGridGeometry().getGridRange().getLength(1) });
//                    GridGeometry2D newGridGeometry = new GridGeometry2D(newGridrange,
//                            newEnvelope, new boolean[] { false, true });
//
//                    //getting the needed operation
//                    Resampler2D.Operation op = new Resampler2D.Operation();
//
//                    //getting parameters
//                    ParameterValueGroup group = op.getParameters();
//                    group.parameter("Source").setValue(gc.geophysics(false));
//                    group.parameter("CoordinateReferenceSystem").setValue(crs);
//                    group.parameter("GridGeometry").setValue(newGridGeometry);
//
//                    GridCoverageProcessor2D processor2D = GridCoverageProcessor2D
//                        .getDefault();
//                    GridCoverage2D gcOp = (GridCoverage2D) processor2D.doOperation(op, group);
               	
                    //displaying
//                    frame = new JFrame();
//                    topPanel = new JPanel();
//                    topPanel.setLayout(new BorderLayout());
//                    frame.getContentPane().add(topPanel);
//
//                    frame.setBackground(Color.black);
//
//                    pane = new JScrollPane();
//                    pane.getViewport().add(new JLabel(
//                            new ImageIcon(
//                                ((PlanarImage) gcOp.getRenderedImage())
//                                .getAsBufferedImage())));
//                    topPanel.add(pane, BorderLayout.CENTER);
//                    frame.getContentPane().add(pane);
//                    frame.getContentPane().add(pane, BorderLayout.CENTER);
//                    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//                    frame.pack();
//                    frame.show();
//	                GridCoverageWriter writer = new GeoTiffWriter(new File(
//                    "c:\\temp\\po_168213_pan_0000000_wgs84.tiff"));
//					writer.write(gcOp, null);

//                    //printing
                    System.out.println(gc.getCoordinateReferenceSystem().toWKT());
//                    System.out.println(gcOp.getCoordinateReferenceSystem().toWKT());
//					System.out.println(gcOp.getEnvelope().toString());
								
					
               }
            }
        }
		}
		
    public static void testWriter()
        throws IllegalArgumentException, IOException, 
            UnsupportedOperationException, ParseException, FactoryException {
//        File file = new File(
//                "c:\\Program Files\\Apache Software Foundation\\Tomcat 5.0\\webapps\\geoserver\\data\\coverages\\arc_sample\\a.asc");
//
//        //getting a reader
//
//            GridCoverageReader reader = new ArcGridReader(file);
//
//            if (reader != null) {
//                //reading the coverage
//                GridCoverage2D gc = (GridCoverage2D) reader.read(null);
//
//                                if (gc != null) {
//                                    GridCoverageWriter writer = new GeoTiffWriter(new File(
//                                                "c:\\dadas.tif"));
//                                    writer.write(gc, null);
//                                    
//                                }
//                CRSFactory crsFactory = FactoryFinder.getCRSFactory(null);
//
//                String wkt = "PROJCS[\"UTM_Zone_10N\", " + "GEOGCS[\"WGS84\", "
//                    + "DATUM[\"WGS84\", "
//                    + "SPHEROID[\"WGS84\", 6378137.0, 298.257223563]], "
//                    + "PRIMEM[\"Greenwich\", 0.0], "
//                    + "UNIT[\"degree\",0.017453292519943295], "
//                    + "AXIS[\"Longitude\",EAST], "
//                    + "AXIS[\"Latitude\",NORTH]], "
//                    + "PROJECTION[\"Transverse_Mercator\"], "
//                    + "PARAMETER[\"semi_major\", 6378137.0], "
//                    + "PARAMETER[\"semi_minor\", 6356752.314245179], "
//                    + "PARAMETER[\"central_meridian\", -123.0], "
//                    + "PARAMETER[\"latitude_of_origin\", 0.0], "
//                    + "PARAMETER[\"scale_factor\", 0.9996], "
//                    + "PARAMETER[\"false_easting\", 500000.0], "
//                    + "PARAMETER[\"false_northing\", 0.0], "
//                    + "UNIT[\"metre\",1.0], " + "AXIS[\"x\",EAST], "
//                    + "AXIS[\"y\",NORTH]]";
//                CoordinateReferenceSystem crs = (CoordinateReferenceSystem) crsFactory
//                    .createFromWKT(wkt);
//
//                
//                //new gc with the crs 
//                GridCoverage2D gc1 = new GridCoverage2D("A",
//                        gc.geophysics(false).getRenderedImage(), crs,
//                        gc.getEnvelope());
//                GridCoverageWriter writer = new GeoTiffWriter(new File(
//                            "c:\\a.tiff"));
//                writer.write(gc1, null);
//
//                //rereading
//                File file1 = new File("c:\\a.tiff");
//                reader = new GeoTiffReader(file1);
//                gc = (GridCoverage2D) reader.read(null);
//
// 
//        
//                JFrame frame = new JFrame();
//                JPanel topPanel = new JPanel();
//                topPanel.setLayout(new BorderLayout());
//                frame.getContentPane().add(topPanel);
//
//                frame.setBackground(Color.black);
//
//                JScrollPane pane = new JScrollPane();
//                pane.getViewport().add(new JLabel(
//                        new ImageIcon(
//                            ((PlanarImage) gc.getRenderedImage())
//                            .getAsBufferedImage())));
//                topPanel.add(pane, BorderLayout.CENTER);
//                frame.getContentPane().add(pane);
//                frame.getContentPane().add(pane, BorderLayout.CENTER);
//                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//                frame.pack();
//                frame.show();
//                System.out.println(gc.getCoordinateReferenceSystem().toWKT());
//
//                //
//            }
        }
   
}
