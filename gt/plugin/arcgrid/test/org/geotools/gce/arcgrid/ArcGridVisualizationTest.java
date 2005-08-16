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
package org.geotools.gce.arcgrid;

import org.geotools.coverage.Category;
import org.geotools.coverage.FactoryFinder;
import org.geotools.coverage.GridSampleDimension;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.gce.image.WorldImageWriter;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.referencing.crs.AbstractCRS;
import org.geotools.referencing.operation.transform.LinearTransform1D;
import org.geotools.resources.TestData;
import org.geotools.util.NumberRange;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.coverage.grid.GridCoverageReader;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.spatialschema.geometry.MismatchedDimensionException;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.NoSuchElementException;

import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.PropertySourceImpl;
import javax.media.jai.RenderedOp;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import java.awt.geom.Rectangle2D;

/**
 * <p>
 * Title: TestArcGridClass
 * </p>
 * 
 * <p>
 * Description: Testing ArcGrid ascii grids related classes.
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2005 Simone Giannecchini
 * </p>
 * 
 * <p>
 * Company:
 * </p>
 *
 * @author <a href="mailto:simboss_ml@tiscali.it">Simone Giannecchini
 *         (simboss)</a>
 * @version 1.0
 */
public class ArcGridVisualizationTest extends TestCaseSupport {
    /** ArcGrid files (and associated parameters) to test */
    final TestParams[] params = new TestParams[] {
//			new TestParams("a.asc", false, false)
//            new TestParams("vandem.asc.gz", true, false),
//            new TestParams("ArcGrid.asc", false, false),
//            new TestParams("spearfish_dem.asc.gz", true, true)
        };

    /**
     * Creates a new instance of ArcGridReadWriteTest
     *
     * @param name 
     */
    public ArcGridVisualizationTest(String name) {
        super(name);
    }

    public void testAll() throws Exception {
        StringBuffer errors = new StringBuffer();

        for (int i = 0; i < params.length; i++) {
            try {
                test(params[i]);
            } catch (Exception e) {
                e.printStackTrace();
                errors.append("\nFile " + params[i].fileName + " : "
                    + e.getMessage());
            }
        }

        if (errors.length() > 0) {
            fail(errors.toString());
        }
    }

    void test(TestParams testParam) throws Exception {
        //create a temporary output file
        //temporary file to use
        File tmpFile = null;

        if (testParam.compressed) {
            tmpFile = File.createTempFile("temp", ".gz");
        } else {
            tmpFile = File.createTempFile("temp", ".asc");
        }

        tmpFile.deleteOnExit();

        //file to use
        URL file = TestData.getResource(this, testParam.fileName);

        /*Step 1 read it*/

        //read in the grid coverage
        GridCoverageReader reader = new ArcGridReader((TestData.getResource(
                    this, testParam.fileName)));

        //reading the coverage
        GridCoverage2D gc = ((GridCoverage2D) reader.read(null));
        BufferedImage bufferedImage = ((PlanarImage) gc.getRenderedImage())
        .getAsBufferedImage();

		
		
		
		com.vividsolutions.jts.geom.Envelope envelope = new com.vividsolutions.jts.geom.Envelope();
		GeneralEnvelope gEnvelope = (GeneralEnvelope) gc.getEnvelope();
		envelope.init(
				gEnvelope.getLowerCorner().getOrdinate(0),
				gEnvelope.getUpperCorner().getOrdinate(0),
				gEnvelope.getLowerCorner().getOrdinate(1),
				gEnvelope.getUpperCorner().getOrdinate(1)
		);
		//com.vividsolutions.jts.geom.Envelope subEnvelope = request.getEnvelope();
		com.vividsolutions.jts.geom.Envelope subEnvelope = new com.vividsolutions.jts.geom.Envelope(
					new Coordinate(0, 42.21),
					new Coordinate(11.819999694824217, 43.0)
				); 
		GeneralEnvelope gSEnvelope = new GeneralEnvelope(new Rectangle2D.Double(
				subEnvelope.getMinX(),
				subEnvelope.getMinY(),
				subEnvelope.getWidth(),
				subEnvelope.getHeight())
		); 
		//getting raw image
		RenderedImage image = gc.getRenderedImage();
		
		//getting dimensions of the raw image to evaluate the steps
		final int nX = image.getWidth();
		final int nY = image.getHeight();
		final double lo1 = envelope.getMinX();
		final double la1 = envelope.getMinY();
		final double lo2 = envelope.getMaxX();
		final double la2 = envelope.getMaxY();
		
		final double los1 = subEnvelope.getMinX();
		final double las1 = subEnvelope.getMinY();
		final double los2 = subEnvelope.getMaxX();
		final double las2 = subEnvelope.getMaxY();
		
		final double dX = (lo2 - lo1) / nX;
		final double dY = (la2 - la1) / nY;//we have to keep into account axis directions
		//when using the image

		final double lonIndex1 = java.lang.Math.ceil((los1 - lo1) / dX); 
		final double lonIndex2 = java.lang.Math.floor((los2 - lo1) / dX); 
		final double latIndex1 = java.lang.Math.floor((la2 - las2) / dY); 
		final double latIndex2 = java.lang.Math.ceil((la2 - las1) / dY); 
		
		final int cnX = new Double(lonIndex2 - lonIndex1).intValue();
		final int cnY = new Double(latIndex2 - latIndex1).intValue();

		
//		 Create the background Image.
		Number bandValues[]=null;
		int numBands=gc.getNumSampleDimensions();
        if( bufferedImage.getSampleModel().getDataType() == DataBuffer.TYPE_FLOAT ) {
	        bandValues = new Float[numBands];  
	        // Fill the array with a constant value.  
	        for(int band=0;band<bandValues.length;band++)  
	        	bandValues[band] = new Float(Float.NaN);
        } else if( bufferedImage.getSampleModel().getDataType() == DataBuffer.TYPE_DOUBLE ) {
	        bandValues = new Double[numBands];  
	        // Fill the array with a constant value.  
	        for(int band=0;band<bandValues.length;band++)  
	        	bandValues[band] = new Double(Double.NaN);
        } else if( bufferedImage.getSampleModel().getDataType() == DataBuffer.TYPE_SHORT ) {
	        bandValues = new Short[numBands];  
	        // Fill the array with a constant value.  
	        for(int band=0;band<bandValues.length;band++)  
	        	//TODO this should be parameterized!!!!
	        	bandValues[band] = new Short((short)-9999);//quick hack for gtopo30 format!!!!!
        }			 
	
        ParameterBlock pb=new ParameterBlock();
		pb.add(new Float( subEnvelope.getWidth() / dX)).add(new Float( subEnvelope.getHeight() / dY));
        pb.add(bandValues);
		PlanarImage imgBackground = JAI.create("constant", pb, null);

		/**translating the old one*/
        pb.removeParameters();
        pb.removeSources();
		pb.addSource(image);
		pb.add((float)((lo1-los1)/dX));
		pb.add((float)((las2-la2)/dY));
		RenderedImage renderableSource=JAI.create("translate",pb);
		
		/**overlaying images*/
        pb.removeParameters();
        pb.removeSources();
        pb.addSource(imgBackground).addSource(renderableSource);
		RenderedImage destOverlayed=JAI.create("overlay", pb, null);

		//creating a copy of the given grid coverage2D
		GridCoverage2D subCoverage = (GridCoverage2D) FactoryFinder.getGridCoverageFactory(null).create(
				"s",
				destOverlayed,
				gc.getCoordinateReferenceSystem(),
				gSEnvelope,
				gc.getSampleDimensions(),
				null,
				((PropertySourceImpl)gc).getProperties());
		
		
		BufferedOutputStream out1=new BufferedOutputStream(new FileOutputStream(new File("c:\\temp\\"+ tmpFile.getName()+".png")));
		WorldImageWriter writer1 = new WorldImageWriter(out1);
		writer1.getFormat().getWriteParameters().parameter("format").setValue("png");
		writer1.write(subCoverage,null);
		
        //visualizing it
        ImageIcon icon = new ImageIcon(bufferedImage);
        JLabel label = new JLabel(icon);
        JFrame frame = new JFrame();
        frame.setTitle(testParam.fileName);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(new JScrollPane(label));
        frame.pack();
        frame.show();
    }
	 private GridCoverage createCoverage(
	    		RenderedImage image,
				CoordinateReferenceSystem crs, 
				GeneralEnvelope envelope, 
				String coverageName
		) throws MismatchedDimensionException, IOException {
	    	//building up a coverage
	    	GridCoverage coverage = null;
	    	//deciding the number range
	    	NumberRange geophysicRange=null;
	    	switch(image.getSampleModel().getTransferType()){
	    	case DataBuffer.TYPE_BYTE:
	    		geophysicRange=new NumberRange(0, 255);
	    	break;
	    	case DataBuffer.TYPE_USHORT:
	    		geophysicRange=new NumberRange(0, 65535);
	    	break;
	    	case DataBuffer.TYPE_INT:
	    		geophysicRange=new NumberRange(-Integer.MAX_VALUE,Integer.MAX_VALUE);
	    	break;	
	    	default:
	    		throw new IOException("Data buffer type not supported! Use byte, ushort or int");
	    	}
	    	try {
	    		
	    		//convenieience category in order to 
	    		Category  values = new Category("values",new Color[]{Color.BLACK},geophysicRange,LinearTransform1D.IDENTITY );
	    		
	    		//creating bands
	    		GridSampleDimension bands[]=new GridSampleDimension[image.getSampleModel().getNumBands()];
	    		for(int i=0;i<image.getSampleModel().getNumBands();i++)
	    			bands[i]=new GridSampleDimension(new Category[] {values}, null).geophysics(true);
	    		
	    		//creating coverage
	    		coverage = FactoryFinder.getGridCoverageFactory(null).create(
                        coverageName, image, crs, envelope,bands,null,null);
	    	} catch (NoSuchElementException e1) {
	    		throw new IOException("Error when creating the coverage in world image"+e1.getMessage());
	    	}
	    	
	    	return coverage;
	    }

    public static final void main(String[] args) throws Exception {
        junit.textui.TestRunner.run(suite(ArcGridVisualizationTest.class));
    }
}
