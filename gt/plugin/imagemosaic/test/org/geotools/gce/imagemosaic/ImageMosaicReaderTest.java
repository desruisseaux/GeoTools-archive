/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2006, Geotools Project Managment Committee (PMC)
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
package org.geotools.gce.imagemosaic;

import java.awt.Rectangle;
import java.io.IOException;
import java.net.URL;

import javax.media.jai.JAI;

import junit.framework.TestCase;
import junit.textui.TestRunner;

import org.geotools.coverage.AbstractCoverage;
import org.geotools.coverage.grid.GeneralGridRange;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.data.coverage.grid.AbstractGridFormat;
import org.geotools.data.coverage.grid.GridFormatFinder;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.resources.TestData;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValue;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.spatialschema.geometry.MismatchedDimensionException;

/**
 * @author Simone Giannecchini
 * @since 2.3
 * 
 */
public class ImageMosaicReaderTest extends TestCase {

	private final static String TEST_FILE="mosaic.shp";
	/**
	 * 
	 */
	public ImageMosaicReaderTest() {

	}

	public void testDefaultParameterValue() throws IOException,
			MismatchedDimensionException, NoSuchAuthorityCodeException {
		//
		// /////////////////////////////////////////////////////////////////
		//
		// Get the resource.
		//
		//
		// /////////////////////////////////////////////////////////////////
		final URL testFile = TestData.getResource(this,TEST_FILE);
		assertNotNull(testFile);

		//
		// /////////////////////////////////////////////////////////////////
		//
		// Get a reader
		//
		//
		// /////////////////////////////////////////////////////////////////
		final AbstractGridFormat format = (AbstractGridFormat) GridFormatFinder
				.findFormat(testFile);
		assertNotNull(format);
		final ImageMosaicReader reader = (ImageMosaicReader) format
				.getReader(testFile);
		assertNotNull(reader);

		//
		// /////////////////////////////////////////////////////////////////
		//
		// Show the coverage
		//
		//
		// /////////////////////////////////////////////////////////////////
		final AbstractCoverage coverage = (AbstractCoverage) reader.read(null);
		assertNotNull("Null value returned instead of a coverage", coverage);
		coverage.show("Default Values");

	}

	public void testInputImageROI() throws IOException,
			MismatchedDimensionException, NoSuchAuthorityCodeException {
		//
		// /////////////////////////////////////////////////////////////////
		//
		// Get the resource.
		//
		//
		// /////////////////////////////////////////////////////////////////
		final URL testFile = TestData.getResource(this, TEST_FILE);
		assertNotNull(testFile);

		//
		// /////////////////////////////////////////////////////////////////
		//
		// Get a reader
		//
		//
		// /////////////////////////////////////////////////////////////////
		final AbstractGridFormat format = (AbstractGridFormat) GridFormatFinder
				.findFormat(testFile);
		assertNotNull(format);
		final ImageMosaicReader reader = (ImageMosaicReader) format
				.getReader(testFile);
		assertNotNull(reader);

		//
		// /////////////////////////////////////////////////////////////////
		//
		// limit yourself to reading just a bit of it
		//
		//
		// /////////////////////////////////////////////////////////////////
		final ParameterValue value = (ParameterValue) ImageMosaicFormat.INPUT_IMAGE_ROI
				.createValue();
		value.setValue(Boolean.TRUE);

		//
		// /////////////////////////////////////////////////////////////////
		//
		// Show the coverage
		//
		//
		// /////////////////////////////////////////////////////////////////
		((AbstractCoverage) reader.read(new GeneralParameterValue[] { value }))
				.show("testInputImageROI");

	}

	public void testInputROIThreshold() throws IOException,
			MismatchedDimensionException, NoSuchAuthorityCodeException {
		//
		// /////////////////////////////////////////////////////////////////
		//
		// Get the resource.
		//
		//
		// /////////////////////////////////////////////////////////////////
		final URL testFile = TestData.getResource(this, TEST_FILE);
		assertNotNull(testFile);

		//
		// /////////////////////////////////////////////////////////////////
		//
		// Get a reader
		//
		//
		// /////////////////////////////////////////////////////////////////
		final AbstractGridFormat format = (AbstractGridFormat) GridFormatFinder
				.findFormat(testFile);
		assertNotNull(format);
		final ImageMosaicReader reader = (ImageMosaicReader) format
				.getReader(testFile);
		assertNotNull(reader);

		//
		// /////////////////////////////////////////////////////////////////
		//
		// limit yourself to reading just a bit of it
		//
		//
		// /////////////////////////////////////////////////////////////////
		final ParameterValue alpha = (ParameterValue) ImageMosaicFormat.INPUT_IMAGE_ROI
				.createValue();
		alpha.setValue(Boolean.TRUE);
		final ParameterValue alphaVal = (ParameterValue) ImageMosaicFormat.INPUT_IMAGE_ROI_THRESHOLD
				.createValue();
		alphaVal.setValue(new Integer(100));

		//
		// /////////////////////////////////////////////////////////////////
		//
		// Show the coverage
		//
		//
		// /////////////////////////////////////////////////////////////////
		((AbstractCoverage) reader.read(new GeneralParameterValue[] { alpha,
				alphaVal })).show("testInputROIThreshold");

	}

	public void testFinalAlphaThreshold() throws IOException,
			MismatchedDimensionException, NoSuchAuthorityCodeException {

		//
		// /////////////////////////////////////////////////////////////////
		//
		// Get the resource.
		//
		//
		// /////////////////////////////////////////////////////////////////
		final URL testFile = TestData.getResource(this,TEST_FILE);
		assertNotNull(testFile);

		//
		// /////////////////////////////////////////////////////////////////
		//
		// Get a reader
		//
		//
		// /////////////////////////////////////////////////////////////////
		final AbstractGridFormat format = (AbstractGridFormat) GridFormatFinder
				.findFormat(testFile);
		assertNotNull(format);
		final ImageMosaicReader reader = (ImageMosaicReader) format
				.getReader(testFile);
		assertNotNull(reader);

		//
		// /////////////////////////////////////////////////////////////////
		//
		// alpha on output
		//
		//
		// /////////////////////////////////////////////////////////////////
		final ParameterValue alpha = (ParameterValue) ImageMosaicFormat.FINAL_ALPHA
				.createValue();
		alpha.setValue(Boolean.TRUE);
		final ParameterValue threshold = (ParameterValue) ImageMosaicFormat.ALPHA_THRESHOLD
				.createValue();
		threshold.setValue(100);

		//
		// /////////////////////////////////////////////////////////////////
		//
		// Show the coverage
		//
		//
		// /////////////////////////////////////////////////////////////////
		((AbstractCoverage) reader.read(new GeneralParameterValue[] { alpha,
				threshold })).show("testFinalAlphaThreshold");

	}

	public void testCrop() throws IOException, MismatchedDimensionException,
			NoSuchAuthorityCodeException {

		// /////////////////////////////////////////////////////////////////
		//
		// Get the resource.
		//
		//
		// /////////////////////////////////////////////////////////////////
		final URL testFile = TestData.getResource(this, TEST_FILE);

		//
		// /////////////////////////////////////////////////////////////////
		//
		// Get a reader
		//
		//
		// /////////////////////////////////////////////////////////////////
		final AbstractGridFormat format = (AbstractGridFormat) GridFormatFinder
				.findFormat(testFile);
		final ImageMosaicReader reader = (ImageMosaicReader) format
				.getReader(testFile);

		//
		// /////////////////////////////////////////////////////////////////
		//
		// alpha on output
		//
		//
		// /////////////////////////////////////////////////////////////////
		final ParameterValue gg = (ParameterValue) ImageMosaicFormat.READ_GRIDGEOMETRY2D
				.createValue();
		 final GeneralEnvelope cropEnvelope = new GeneralEnvelope(new double[]
		 {
				 7.71089711835763,38.4483357978537 }, new double[] {
				 13.2519216051101,43.1624043574166 });
		cropEnvelope.setCoordinateReferenceSystem(DefaultGeographicCRS.WGS84);
		gg.setValue(new GridGeometry2D(new GeneralGridRange(new Rectangle(0, 0,
				100, 80)), cropEnvelope));
		
		//
		// /////////////////////////////////////////////////////////////////
		//
		// Show the coverage
		//
		//
		// /////////////////////////////////////////////////////////////////
		((AbstractCoverage) reader.read(new GeneralParameterValue[] { gg,
				 })).show("testCrop");

	}

	public void testComplete() throws IOException,
			MismatchedDimensionException, NoSuchAuthorityCodeException {

		 //
		 ///////////////////////////////////////////////////////////////////
		 //
		 // Get the resource.
		 //
		 //
		 ///////////////////////////////////////////////////////////////////
		 final URL testFile = TestData.getResource(this, TEST_FILE);
		 assertNotNull(testFile);
		
		 //
		 ///////////////////////////////////////////////////////////////////
		 //
		 // Get a reader
		 //
		 //
		 ///////////////////////////////////////////////////////////////////
		 final AbstractGridFormat format = (AbstractGridFormat)
		 GridFormatFinder
		 .findFormat(testFile);
		 assertNotNull(format);
		 final ImageMosaicReader reader = (ImageMosaicReader) format
		 .getReader(testFile);
		 assertNotNull(reader);
		
		 //
		 // /////////////////////////////////////////////////////////////////
		 //
		 // alpha on output
		 //
		 //
		 // /////////////////////////////////////////////////////////////////
		 final ParameterValue alpha = (ParameterValue)
		 ImageMosaicFormat.FINAL_ALPHA
		 .createValue();
		 alpha.setValue(Boolean.TRUE);
		 final ParameterValue threshold = (ParameterValue)
		 ImageMosaicFormat.ALPHA_THRESHOLD
		 .createValue();
		 threshold.setValue(1);
		 final ParameterValue roi = (ParameterValue)
		 ImageMosaicFormat.INPUT_IMAGE_ROI
		 .createValue();
		 roi.setValue(Boolean.TRUE);
		 final ParameterValue roiTh = (ParameterValue)
		 ImageMosaicFormat.INPUT_IMAGE_ROI_THRESHOLD
		 .createValue();
		 roiTh.setValue(new Integer(1));
		
		 // /////////////////////////////////////////////////////////////////
		 //
		 // Show the coverage
		 //
		 //
		 // /////////////////////////////////////////////////////////////////
		 ((AbstractCoverage) reader.read(new GeneralParameterValue[] { alpha,
		 threshold, roiTh, roi })).show("testComplete");

	}

	public void testFinalAlpha() throws IOException,
			MismatchedDimensionException, NoSuchAuthorityCodeException {
		//
		// /////////////////////////////////////////////////////////////////
		//
		// Get the resource.
		//
		//
		// /////////////////////////////////////////////////////////////////
		final URL testFile = TestData.getResource(this,TEST_FILE);
		assertNotNull(testFile);

		//
		// /////////////////////////////////////////////////////////////////
		//
		// Get a reader
		//
		//
		// /////////////////////////////////////////////////////////////////
		final AbstractGridFormat format = (AbstractGridFormat) GridFormatFinder
				.findFormat(testFile);
		assertNotNull(format);
		final ImageMosaicReader reader = (ImageMosaicReader) format
				.getReader(testFile);
		assertNotNull(reader);

		//
		// /////////////////////////////////////////////////////////////////
		//
		// alpha on output
		//
		//
		// /////////////////////////////////////////////////////////////////
		final ParameterValue alpha = (ParameterValue) ImageMosaicFormat.FINAL_ALPHA
				.createValue();
		alpha.setValue(Boolean.TRUE);

		//
		// /////////////////////////////////////////////////////////////////
		//
		// Show the coverage
		//
		//
		// /////////////////////////////////////////////////////////////////
		((AbstractCoverage) reader.read(new GeneralParameterValue[] { alpha }))
				.show("testFinalAlpha");

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		TestRunner.run(ImageMosaicReaderTest.class);

	}

	protected void setUp() throws Exception {
		JAI.getDefaultInstance().getTileCache().setMemoryCapacity(
				64 * 1024 * 1024);
		JAI.getDefaultInstance().getTileScheduler().setParallelism(50);
		JAI.getDefaultInstance().getTileScheduler().setPriority(10);
		JAI.getDefaultInstance().getTileScheduler().setPrefetchParallelism(50);
		JAI.getDefaultInstance().getTileScheduler().setPrefetchPriority(10);
		
		super.setUp();
	}

}
