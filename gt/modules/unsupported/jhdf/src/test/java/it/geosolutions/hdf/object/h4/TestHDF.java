package it.geosolutions.hdf.object.h4;

import it.geosolutions.resources.TestData;

import java.awt.Rectangle;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BandedSampleModel;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferDouble;
import java.awt.image.DataBufferFloat;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferShort;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageReadParam;
import javax.media.jai.widget.ScrollingImagePanel;
import javax.swing.JFrame;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import ncsa.hdf.hdflib.HDFConstants;
import ncsa.hdf.hdflib.HDFException;

import com.sun.media.imageioimpl.common.ImageUtil;
import com.sun.media.jai.codecimpl.util.RasterFactory;

/**
 * Test class
 * 
 * All sample data used in these tests are available at these sites:
 * http://www.hdfgroup.uiuc.edu/UserSupport/code-examples/sample-programs/convert/Conversion.html
 * 
 * http://eosweb.larc.nasa.gov/PRODOCS/misr/level3/download_data.html
 * 
 * ftp://ftp.geo-solutions.it/incoming/MODPM2007027121858.L3_000_EAST_MED.hdf
 * (as anonymous ftp access)
 * 
 * @author Romagnoli Daniele
 * 
 */
public class TestHDF extends TestCase {

	public TestHDF(final String test) {
		super(test);
	}

	public static TestSuite suite() {
		TestSuite suite = new TestSuite();

		suite.addTest(new TestHDF("testAnnotations"));

		suite.addTest(new TestHDF("testAttributes"));

		suite.addTest(new TestHDF("testGroups"));

		suite.addTest(new TestHDF("testDimensionScales"));

		suite.addTest(new TestHDF("testVisualizeSDS"));

		suite.addTest(new TestHDF("testMisrSDS"));

		suite.addTest(new TestHDF("testVisualizePalettedGRImage"));

		return suite;
	}

	public static void main(java.lang.String[] args) throws HDFException {
		junit.textui.TestRunner.run(suite());
	}

	/**
	 * Test a MISR HDF source
	 * 
	 */
	public void testMisrSDS() throws HDFException, IOException {
		final File file = TestData.file(this,
				"MISR_AM1_CGLS_WIN_2005_F04_0017.hdf");

		final H4File myFile;
		H4SDS sds;

		System.out.println("*************************************************"
				+ "\n\t\tSDS test\n"
				+ "*************************************************");
		myFile = new H4File(file.getAbsolutePath());

		H4SDSCollection sdsColl = myFile.getH4SdsCollection();
		final int sdsNum = sdsColl.getNumSDS();

		// Simple constant to reduce the time and memory use of this test.
		int numOfSDSNeedToBeVisualized = 3;
		System.out.println("SDS num = " + sdsNum);

		// //
		//
		// SDSs scan
		//
		// //
		for (int s = 0; s < sdsNum; s++) {
			sds = sdsColl.getH4SDS(s);
			printInfo(sds);

			// Description Annotations visualization
			List annotations = sds.getAnnotations(HDFConstants.AN_DATA_DESC);
			if (annotations != null) {
				final int annSize = annotations.size();
				for (int i = 0; i < annSize; i++) {
					H4Annotation ann = (H4Annotation) annotations.get(i);
					printInfo(ann);
				}
			}

			// Label Annotations visualization
			List annotations2 = sds.getAnnotations(HDFConstants.AN_DATA_LABEL);
			if (annotations2 != null) {
				final int annSize = annotations2.size();
				for (int i = 0; i < annSize; i++) {
					H4Annotation ann = (H4Annotation) annotations2.get(i);
					printInfo(ann);
				}
			}

			System.out.println("---------------------------"
					+ "\n\tSDS Dimensions Info\n"
					+ "---------------------------");

			// SDS Dimensions management
			final List dimList = sds.getDimensions();
			if (dimList != null) {
				final int dimSizes = dimList.size();
				if (dimSizes != 0)

					// Dimensions scan
					for (int i = 0; i < dimSizes; i++) {
						H4Dimension dim = sds.getDimension(i);
						printInfo(dim);
						if (dim.isHasDimensionScaleSet())
							printDimensionScaleValues(dim);
						final int nAttrib = dim.getNumAttributes();

						// Dimension's attributes visualization
						if (nAttrib != 0) {
							Map attributes = dim.getAttributes();
							Set keys = attributes.keySet();
							Iterator attribsIt = keys.iterator();
							while (attribsIt.hasNext()) {
								H4Attribute attrib = (H4Attribute) attributes
										.get(attribsIt.next());
								printInfo(attrib);
							}
						}
					}
			}
			System.out.println("---------------------------"
					+ "\n\tSDS Attributes Info\n"
					+ "---------------------------");

			// SDS Attributes visualization
			final int attrNum = sds.getNumAttributes();
			if (attrNum != 0) {
				Map attributes = sds.getAttributes();
				Set keys = attributes.keySet();
				Iterator attribsIt = keys.iterator();
				while (attribsIt.hasNext()) {
					H4Attribute attrib = (H4Attribute) attributes.get(attribsIt
							.next());
					printInfo(attrib);
				}
			}

			// Visualize 2D datasets
			if (sds.getRank() == 2 && (numOfSDSNeedToBeVisualized--) > 0) {
				BufferedImage bimage = getBufferedImage(sds);
				visualize("", bimage, 7, 800, 600);
			}
			sds.close();
		}
		myFile.close();
	}

	/**
	 * Testing File Annotations (Label/Description), Data Object (SDS/GRImage)
	 * Annotations (Label/Description)
	 */
	public void testAnnotations() throws HDFException, IOException {
		final File file = TestData.file(this,
				"MODPM2007027121858.L3_000_EAST_MED.hdf");

		H4File myFile;
		H4SDS sds;
		H4GRImage grImage;
		List annotations = null;

		// ////////////////////////////////////////////////////////////////////
		//
		// File annotations
		//
		// ////////////////////////////////////////////////////////////////////
		final int[] anTypes = new int[] { HDFConstants.AN_FILE_DESC,
				HDFConstants.AN_FILE_LABEL };

		System.out.println("*************************************************"
				+ "\n\t\tFile Annotations test\n"
				+ "*************************************************");

		myFile = new H4File(file.getAbsolutePath());

		H4AnnotationManager annManager = myFile.getH4AnnotationManager();
		System.out
				.println("File descriptions: "
						+ annManager.getNFileDescriptions()
						+ " | File labels: " + annManager.getNFileLabels()
						+ " | Data Object descriptions: "
						+ annManager.getNDataObjectDescriptions()
						+ " | Data Object labels: "
						+ annManager.getNDataObjectLabels());

		for (int j = 0; j < 2; j++) {
			List fileAnn = myFile.getAnnotations(anTypes[j]);
			if (fileAnn != null) {
				final int fileDescNum = fileAnn.size();
				for (int i = 0; i < fileDescNum; i++) {
					H4Annotation ann = (H4Annotation) fileAnn.get(i);
					printInfo(ann);
				}
			}
		}

		// ////////////////////////////////////////////////////////////////////
		//
		// SDS annotations
		//
		// ////////////////////////////////////////////////////////////////////
		System.out.println("*************************************************"
				+ "\n\t\tSDS Annotations test\n"
				+ "*************************************************");
		final H4SDSCollection sdsCollection = myFile.getH4SdsCollection();
		final int nSDS = sdsCollection.getNumSDS();

		// SDS scan
		for (int s = 0; s < nSDS; s++) {
			sds = sdsCollection.getH4SDS(s);
			printInfo(sds);
			// Description Annotations
			annotations = sds.getAnnotations(HDFConstants.AN_DATA_DESC);
			if (annotations != null) {
				final int annSize = annotations.size();
				for (int i = 0; i < annSize; i++) {
					H4Annotation ann = (H4Annotation) annotations.get(i);
					printInfo(ann);
					ann.close();
				}
			}
			// Label Annotations
			annotations = sds.getAnnotations(HDFConstants.AN_DATA_LABEL);
			if (annotations != null) {
				final int annSize = annotations.size();
				for (int i = 0; i < annSize; i++) {
					H4Annotation ann = (H4Annotation) annotations.get(i);
					printInfo(ann);
					ann.close();
				}
			}
			sds.close();
		}
		myFile.close();

		// ////////////////////////////////////////////////////////////////////
		//
		// GRImage annotations
		//
		// ////////////////////////////////////////////////////////////////////
		System.out.println("*************************************************"
				+ "\n\t\tGRImages Annotations test\n"
				+ "*************************************************");
		final File file2 = TestData.file(this,
				"TOVS_BROWSE_MONTHLY_AM_B861001.E861031_NF.HDF");

		myFile = new H4File(file2.getAbsolutePath());

		annManager = myFile.getH4AnnotationManager();
		System.out
				.println("File descriptions: "
						+ annManager.getNFileDescriptions()
						+ " | File labels: " + annManager.getNFileLabels()
						+ " | Data Object descriptions: "
						+ annManager.getNDataObjectDescriptions()
						+ " | Data Object labels: "
						+ annManager.getNDataObjectLabels());

		final H4GRImageCollection grImageColl = myFile.getH4GRImageCollection();
		final int nImages = grImageColl.getNumImages();

		// GRImage scan
		for (int im = 0; im < nImages; im++) {
			grImage = grImageColl.getH4GRImage(im);

			// Description Annotations
			annotations = grImage.getAnnotations(HDFConstants.AN_DATA_DESC);
			if (annotations != null) {
				final int annSize = annotations.size();
				for (int i = 0; i < annSize; i++) {
					H4Annotation ann = (H4Annotation) annotations.get(i);
					printInfo(ann);
				}
			}

			// Label Annotations
			annotations = grImage.getAnnotations(HDFConstants.AN_DATA_LABEL);
			if (annotations != null) {
				final int annSize = annotations.size();
				for (int i = 0; i < annSize; i++) {
					H4Annotation ann = (H4Annotation) annotations.get(i);
					printInfo(ann);
				}
			}
		}
		myFile.close();
	}

	/**
	 * Test attributes management from various object.
	 * 
	 */
	public void testAttributes() throws HDFException, IOException {
		final File file = TestData.file(this,
				"MODPM2007027121858.L3_000_EAST_MED.hdf");
		
//		 ////////////////////////////////////////////////////////////////////
		//
		// SDS Collection attributes TEST
		//
		// ////////////////////////////////////////////////////////////////////
		System.out
				.println("\n\n\n*************************************************"
						+ "\n\t\tAttribute from SDSCollection\n"
						+ "*************************************************");
		H4File myFile = new H4File(file.getAbsolutePath());
		H4SDSCollection sdsColl = myFile.getH4SdsCollection();
		H4SDS sds;
		
		int attrNum = sdsColl.getNumAttributes();
		if (attrNum != 0) {
			Map attributes = sdsColl.getAttributes();
			Set keys = attributes.keySet();
			Iterator attribsIt = keys.iterator();
			while (attribsIt.hasNext()) {
				H4Attribute attrib = (H4Attribute) attributes.get(attribsIt
						.next());
				printInfo(attrib);
			}
		}

		System.out.println("\n\n\n======================================="
				+ "\n\t\tDatasets scan\n"
				+ "=======================================");
		final int sdsNum = sdsColl.getNumSDS();

		// ////////////////////////////////////////////////////////////////////
		// 
		// Test all SDS in the collection
		//
		// ////////////////////////////////////////////////////////////////////
		for (int i = 0; i < sdsNum; i++) {
			sds = sdsColl.getH4SDS(i);
			attrNum = sds.getNumAttributes();
			System.out.println("-----------> Dataset " + i + " has " + attrNum
					+ " attributes\n=========================================");

			// find all attributes of the current SDS
			if (attrNum != 0) {
				Map attributes = sds.getAttributes();
				Set keys = attributes.keySet();
				Iterator attribsIt = keys.iterator();
				while (attribsIt.hasNext()) {
					H4Attribute attrib = (H4Attribute) attributes.get(attribsIt
							.next());
					printInfo(attrib);
				}
			}
//			 find predefined attribute
			H4Attribute attribute = sds.getAttribute(H4SDS.PREDEF_ATTR_LONG_NAME);
			if (attribute != null)
				printInfo(attribute);
			sds.dispose();
		}
		
		myFile.close();
	}

	/**
	 * Test group Structure
	 */
	public void testGroups() throws HDFException, IOException {
		final File file = TestData.file(this,
				"MISR_AM1_CGLS_WIN_2005_F04_0017.hdf");

		System.out
				.println("\n\n\n*************************************************"
						+ "\n\t\tGroups test\n"
						+ "*************************************************");
		final H4File myFile = new H4File(file.getAbsolutePath());
		final H4VGroupCollection grColl = myFile.getH4VGroupCollection();
		final int nGroups = grColl.getNumLoneVgroups();
		for (int k = 0; k < nGroups; k++) {
			H4VGroup group = grColl.getH4VGroup(k);
			printInfo(group);
			List list = group.getTagRefList();
			if (list != null) {
				final int listSize = list.size();
				System.out
						.println("\nscanning found objects\n======================");
				for (int i = 0; i < listSize; i++) {
					int[] tagRefs = (int[]) list.get(i);
					System.out.println("INDEX  = " + i + "|---> TAG="
							+ tagRefs[0] + " REF=" + tagRefs[1]);
					if (H4VGroup.isAVGroup(group, tagRefs[1])) {
						System.out.println("is a VGroup");
						H4VGroup newGroup = new H4VGroup(group, tagRefs[1]);
						printInfo(newGroup);
					}
				}
			}
			System.out.println("");
		}
		myFile.close();
	}

	/**
	 * Test Dimension scales
	 * 
	 */
	public void testDimensionScales() throws HDFException, IOException {
		final File file = TestData.file(this,
				"TOVS_5DAYS_AM_B870511.E870515_NG.HDF");

		// //
		// 
		// Test SDS Dimension scales
		//
		// //
		System.out
				.println("\n\n\n*************************************************"
						+ "\n\t\tDimension scales test\n"
						+ "*************************************************");
		final H4File myFile = new H4File(file.getAbsolutePath());
		final H4SDSCollection sdsCollection = myFile.getH4SdsCollection();
		final int nSDSDatasets = sdsCollection.getNumSDS();

		// SDSs scan
		for (int s = 0; s < nSDSDatasets; s++) {
			H4SDS h4sds = sdsCollection.getH4SDS(s);
			final int rank = h4sds.getRank();
			printInfo(h4sds);

			// Dimensions scan
			for (int i = 0; i < rank; i++) {
				H4Dimension dimension = h4sds.getDimension(i);
				printInfo(dimension);
				if (dimension.isHasDimensionScaleSet()) {
					// Print dimension scale values
					System.out.println("Dimension " + i
							+ " has Dimension Scale set");
					printDimensionScaleValues(dimension);
				}
			}
			System.out.println("======================================");
		}
		myFile.close();
	}

	/**
	 * Test Paletted GR Images
	 * 
	 */
	public void testVisualizePalettedGRImage() throws HDFException, IOException {
		final File file = TestData.file(this,
				"TOVS_BROWSE_DAILY_AM_861031_NF.HDF");

		final H4File myFile;
		H4GRImage grImage;

		// //
		// 
		// Test GRImages
		//
		// //
		System.out.println("*************************************************"
				+ "\n\t\tGRImages ( + Palette ) test\n"
				+ "*************************************************");
		myFile = new H4File(file.getAbsolutePath());
		final H4GRImageCollection grImageCollection = myFile.getH4GRImageCollection();

		final int nImages = grImageCollection.getNumImages();

		// GRImages scan
		for (int im = 0; im < nImages; im++) {
			grImage = grImageCollection.getH4GRImage(im);
			final int palettes = grImage.getNumPalettes();
			ColorModel cm = null;
			if (palettes != 0) {
				// Getting the first palette
				H4Palette palette = grImage.getPalette(0);
				final int numEntries = palette.getNumEntries();

				// Getting palette values
				byte[] paletteData = palette.getValues();
				final int paletteInterlace = palette.getInterlaceMode();
				if (paletteData != null) {
					byte[][] myPalette = new byte[3][numEntries];
					if (paletteInterlace == HDFConstants.MFGR_INTERLACE_PIXEL) {
						// color conponents are arranged in RGB, RGB, RGB, ...
						for (int i = 0; i < numEntries; i++) {
							myPalette[0][i] = paletteData[i * 3];
							myPalette[1][i] = paletteData[i * 3 + 1];
							myPalette[2][i] = paletteData[i * 3 + 2];
						}
					} else {
						for (int i = 0; i < numEntries; i++) {
							myPalette[0][i] = paletteData[i];
							myPalette[1][i] = paletteData[256 + i];
							myPalette[2][i] = paletteData[512 + i];
						}
					}
					cm = new IndexColorModel(8, // bits - the number of bits
							// each pixel occupies
							numEntries, // size - the size of the color
							// component arrays
							myPalette[0], // the array of red color comps
							myPalette[1], // the array of green color comps
							myPalette[2]); // the array of blue color comps
				}
			}

			final int rank = 2; // Images are always 2D
			final int dimSizes[] = grImage.getDimSizes();
			final int width = dimSizes[0];
			final int height = dimSizes[1];
			final int strideX = 1; // subsamplingx
			final int strideY = 1; // subsamplingy
			final Rectangle sourceRegion = new Rectangle(0, 0, width / 1,
					height / 1);
			final Rectangle destinationRegion = new Rectangle(0, 0, width / 1,
					height / 1);
			final ImageReadParam rp = new ImageReadParam();

			rp.setSourceRegion(sourceRegion);
			rp.setSourceSubsampling(strideX, strideY, 0, 0);
			computeRegions(rp, width, height, null, sourceRegion,
					destinationRegion);

			final int[] start = new int[rank];
			final int[] stride = new int[rank];
			final int[] sizes = new int[rank];

			start[0] = sourceRegion.x;
			start[1] = sourceRegion.y;
			sizes[0] = sourceRegion.width / strideX;
			sizes[1] = sourceRegion.height / strideY;
			stride[0] = strideX;
			stride[1] = strideY;

			final int datatype = grImage.getDatatype();

			WritableRaster wr = null;
			Object data = null;
			data = grImage.read(start, stride, sizes);

			// bands variables
			final int[] banks = new int[1];
			final int[] offsets = new int[1];
			for (int band = 0; band < 1; band++) {
				banks[band] = band;
				offsets[band] = 0;
			}

			// Setting SampleModel and ColorModel
			final int bufferType = H4DatatypeUtilities
					.getBufferTypeFromDataType(datatype);
			SampleModel sm = cm.createCompatibleSampleModel(
					destinationRegion.width, destinationRegion.height);

			// ////////////////////////////////////////////////////////////////////
			//
			// DATA READ
			//
			// ////////////////////////////////////////////////////////////////////

			final int size = destinationRegion.width * destinationRegion.height;
			DataBuffer dataBuffer = null;

			switch (bufferType) {
			case DataBuffer.TYPE_BYTE:
				dataBuffer = new DataBufferByte((byte[]) data, size);
				break;
			case DataBuffer.TYPE_SHORT:
			case DataBuffer.TYPE_USHORT:
				dataBuffer = new DataBufferShort((short[]) data, size);
				break;
			case DataBuffer.TYPE_INT:
				dataBuffer = new DataBufferInt((int[]) data, size);
				break;
			case DataBuffer.TYPE_FLOAT:
				dataBuffer = new DataBufferFloat((float[]) data, size);
				break;
			case DataBuffer.TYPE_DOUBLE:
				dataBuffer = new DataBufferDouble((double[]) data, size);
				break;
			}

			// Visualize image
			wr = Raster.createWritableRaster(sm, dataBuffer, null);
			BufferedImage bimage = new BufferedImage(cm, wr, false, null);
			visualize("", bimage, im);
		}

		myFile.close();
	}

	/**
	 * SDS Visualization Test
	 * 
	 */
	public void testVisualizeSDS() throws HDFException, IOException {
		final File file = TestData.file(this,
				"TOVS_5DAYS_AM_B870511.E870515_NG.HDF");

		final H4File myFile;

		System.out.println("*************************************************"
				+ "\n\t\tVisualization test\n"
				+ "*************************************************");
		myFile = new H4File(file.getAbsolutePath());

		final H4SDSCollection sdsCollection = myFile.getH4SdsCollection();
		int nSDS = sdsCollection.getNumSDS();

		// this HDF file contains several SDS with statistic data (STD e COUNT).
		nSDS /= 3;
		for (int sd = 0; sd < nSDS; sd++) {
			H4SDS sds = sdsCollection.getH4SDS(sd);
			final int nAttributes = sds.getNumAttributes();
			String title = "";
			if (nAttributes > 0) {
				H4Attribute attrib = sds
						.getAttribute(H4SDS.PREDEF_ATTR_LONG_NAME);
				if (attrib != null)
					title = new String((byte[]) attrib.getValues());
			}
			if (title.length() == 0)
				title = "dataset " + sd;
			BufferedImage bimage = getBufferedImage(sds);
			visualize(title, bimage, sd);
		}
		myFile.close();
	}

	/**
	 * Build a BufferedImage given a H4SDS.
	 * 
	 * @param sds
	 *            the H4SDS to be read
	 * @return
	 * @throws HDFException
	 */
	private BufferedImage getBufferedImage(H4SDS sds) throws HDFException {
		final int rank = sds.getRank();
		final int dimSizes[] = sds.getDimSizes();
		final int width = dimSizes[rank - 1];
		final int height = dimSizes[rank - 2];

		final int strideX = 1; // subsamplingx
		final int strideY = 1; // subsamplingy
		final Rectangle sourceRegion = new Rectangle(0, 0, width / 1,
				height / 1);
		final Rectangle destinationRegion = new Rectangle(0, 0, width / 1,
				height / 1);
		final ImageReadParam rp = new ImageReadParam();

		rp.setSourceRegion(sourceRegion);
		rp.setSourceSubsampling(strideX, strideY, 0, 0);
		computeRegions(rp, width, height, null, sourceRegion, destinationRegion);

		final int[] start = new int[rank];
		final int[] stride = new int[rank];
		final int[] sizes = new int[rank];

		start[rank - 2] = sourceRegion.y;
		start[rank - 1] = sourceRegion.x;
		if (rank > 0) {
			for (int r = 0; r < rank - 2; r++) {
				start[r] = 0;
				sizes[r] = 1;
				stride[r] = 1;
			}
		}

		sizes[rank - 2] = sourceRegion.height / strideY;
		sizes[rank - 1] = sourceRegion.width / strideX;
		stride[rank - 2] = strideY;
		stride[rank - 1] = strideX;

		final int datatype = sds.getDatatype();

		WritableRaster wr = null;
		Object data = null;
		data = sds.read(start, stride, sizes);

		// bands variables
		final int[] banks = new int[1];
		final int[] offsets = new int[1];
		for (int band = 0; band < 1; band++) {
			banks[band] = band;
			offsets[band] = 0;
		}

		// Setting SampleModel and ColorModel
		final int bufferType = H4DatatypeUtilities
				.getBufferTypeFromDataType(datatype);
		SampleModel sm = new BandedSampleModel(bufferType,
				destinationRegion.width, destinationRegion.height,
				destinationRegion.width, banks, offsets);

		final int nBands = sm.getNumBands();
		ColorModel cm = null;
		ColorSpace cs = null;
		if (nBands > 1) {
			// Number of Bands > 1.
			// ImageUtil.createColorModel provides to Creates a
			// ColorModel that may be used with the specified
			// SampleModel
			cm = ImageUtil.createColorModel(sm);

		} else if ((bufferType == DataBuffer.TYPE_BYTE)
				|| (bufferType == DataBuffer.TYPE_USHORT)
				|| (bufferType == DataBuffer.TYPE_INT)
				|| (bufferType == DataBuffer.TYPE_FLOAT)
				|| (bufferType == DataBuffer.TYPE_DOUBLE)) {

			// Just one band. Using the built-in Gray Scale Color Space
			cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
			cm = RasterFactory.createComponentColorModel(bufferType, // dataType
					cs, // color space
					false, // has alpha
					false, // is alphaPremultiplied
					Transparency.OPAQUE); // transparency
		} else {
			if (bufferType == DataBuffer.TYPE_SHORT) {
				// Just one band. Using the built-in Gray Scale Color
				// Space
				cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
				cm = new ComponentColorModel(cs, false, false,
						Transparency.OPAQUE, DataBuffer.TYPE_SHORT);
			}
		}

		// ////////////////////////////////////////////////////////////////////
		//
		// DATA READ
		//
		// ////////////////////////////////////////////////////////////////////

		final int size = destinationRegion.width * destinationRegion.height;
		DataBuffer dataBuffer = null;

		switch (bufferType) {
		case DataBuffer.TYPE_BYTE:
			dataBuffer = new DataBufferByte((byte[]) data, size);
			break;
		case DataBuffer.TYPE_SHORT:
		case DataBuffer.TYPE_USHORT:
			dataBuffer = new DataBufferShort((short[]) data, size);
			break;
		case DataBuffer.TYPE_INT:
			dataBuffer = new DataBufferInt((int[]) data, size);
			break;
		case DataBuffer.TYPE_FLOAT:
			dataBuffer = new DataBufferFloat((float[]) data, size);
			break;
		case DataBuffer.TYPE_DOUBLE:
			dataBuffer = new DataBufferDouble((double[]) data, size);
			break;
		}

		wr = Raster.createWritableRaster(sm, dataBuffer, null);
		BufferedImage bimage = new BufferedImage(cm, wr, false, null);
		return bimage;
	}

	/**
	 * Computes the source region of interest and the destination region of
	 * interest, taking the width and height of the source image, an optional
	 * destination image, and an optional <code>ImageReadParam</code> into
	 * account. The source region begins with the entire source image. Then that
	 * is clipped to the source region specified in the
	 * <code>ImageReadParam</code>, if one is specified.
	 * 
	 * <p>
	 * If either of the destination offsets are negative, the source region is
	 * clipped so that its top left will coincide with the top left of the
	 * destination image, taking subsampling into account. Then the result is
	 * clipped to the destination image on the right and bottom, if one is
	 * specified, taking subsampling and destination offsets into account.
	 * 
	 * <p>
	 * Similarly, the destination region begins with the source image, is
	 * translated to the destination offset given in the
	 * <code>ImageReadParam</code> if there is one, and finally is clipped to
	 * the destination image, if there is one.
	 * 
	 * <p>
	 * If either the source or destination regions end up having a width or
	 * height of 0, an <code>IllegalArgumentException</code> is thrown.
	 * 
	 * <p>
	 * The {@link #getSourceRegion <code>getSourceRegion</code>} method may be
	 * used if only source clipping is desired.
	 * 
	 * @param param
	 *            an <code>ImageReadParam</code>, or <code>null</code>.
	 * @param srcWidth
	 *            the width of the source image.
	 * @param srcHeight
	 *            the height of the source image.
	 * @param image
	 *            a <code>BufferedImage</code> that will be the destination
	 *            image, or <code>null</code>.
	 * @param srcRegion
	 *            a <code>Rectangle</code> that will be filled with the source
	 *            region of interest.
	 * @param destRegion
	 *            a <code>Rectangle</code> that will be filled with the
	 *            destination region of interest.
	 * @exception IllegalArgumentException
	 *                if <code>srcRegion</code> is <code>null</code>.
	 * @exception IllegalArgumentException
	 *                if <code>dstRegion</code> is <code>null</code>.
	 * @exception IllegalArgumentException
	 *                if the resulting source or destination region is empty.
	 */
	private static void computeRegions(ImageReadParam param, int srcWidth,
			int srcHeight, BufferedImage image, Rectangle srcRegion,
			Rectangle destRegion) {
		if (srcRegion == null) {
			throw new IllegalArgumentException("srcRegion == null!");
		}
		if (destRegion == null) {
			throw new IllegalArgumentException("destRegion == null!");
		}

		// Start with the entire source image
		srcRegion.setBounds(0, 0, srcWidth, srcHeight);

		// Destination also starts with source image, as that is the
		// maximum extent if there is no subsampling
		destRegion.setBounds(0, 0, srcWidth, srcHeight);

		// Clip that to the param region, if there is one
		int periodX = 1;
		int periodY = 1;
		int gridX = 0;
		int gridY = 0;
		if (param != null) {
			Rectangle paramSrcRegion = param.getSourceRegion();
			if (paramSrcRegion != null) {
				srcRegion.setBounds(srcRegion.intersection(paramSrcRegion));
			}
			periodX = param.getSourceXSubsampling();
			periodY = param.getSourceYSubsampling();
			gridX = param.getSubsamplingXOffset();
			gridY = param.getSubsamplingYOffset();
			srcRegion.translate(gridX, gridY);
			srcRegion.width -= gridX;
			srcRegion.height -= gridY;
			destRegion.setLocation(param.getDestinationOffset());
		}

		// Now clip any negative destination offsets, i.e. clip
		// to the top and left of the destination image
		if (destRegion.x < 0) {
			int delta = -destRegion.x * periodX;
			srcRegion.x += delta;
			srcRegion.width -= delta;
			destRegion.x = 0;
		}
		if (destRegion.y < 0) {
			int delta = -destRegion.y * periodY;
			srcRegion.y += delta;
			srcRegion.height -= delta;
			destRegion.y = 0;
		}

		// Now clip the destination Region to the subsampled width and height
		int subsampledWidth = (srcRegion.width + periodX - 1) / periodX;
		int subsampledHeight = (srcRegion.height + periodY - 1) / periodY;
		destRegion.width = subsampledWidth;
		destRegion.height = subsampledHeight;

		// Now clip that to right and bottom of the destination image,
		// if there is one, taking subsampling into account
		if (image != null) {
			Rectangle destImageRect = new Rectangle(0, 0, image.getWidth(),
					image.getHeight());
			destRegion.setBounds(destRegion.intersection(destImageRect));
			if (destRegion.isEmpty()) {
				throw new IllegalArgumentException("Empty destination region!");
			}

			int deltaX = destRegion.x + subsampledWidth - image.getWidth();
			if (deltaX > 0) {
				srcRegion.width -= deltaX * periodX;
			}
			int deltaY = destRegion.y + subsampledHeight - image.getHeight();
			if (deltaY > 0) {
				srcRegion.height -= deltaY * periodY;
			}
		}
		if (srcRegion.isEmpty() || destRegion.isEmpty()) {
			throw new IllegalArgumentException("Empty region!");
		}
	}

	private void visualize(String title, BufferedImage bimage, int step) {
		visualize(title, bimage, step, 0, 0);
	}

	private void visualize(String title, BufferedImage bimage, int step,
			int width, int height) {
		if (width == 0 && height == 0) {
			width = 368;
			height = 188;
		}
		int offset = 0;
		if (title.length() == 0)
			offset += 400;
		final JFrame jf = new JFrame(title);
		jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jf.getContentPane().add(new ScrollingImagePanel(bimage, width, height));
		jf.setLocation(250 * (step % 4), offset + (100 * (step / 4)));
		jf.pack();
		jf.setVisible(true);
	}

	/**
	 * Simple utility method which display information of the provided HDF
	 * Object
	 * 
	 * @param object
	 * @throws HDFException
	 */
	private void printInfo(Object object) throws HDFException {

		if (object instanceof H4Dimension) {
			// //
			//
			// H4Dimension Information
			//
			// //
			H4Dimension dim = (H4Dimension) object;
			System.out
					.println("Dimension INFO\n--------------------------\n"
							+ "index = "
							+ dim.getIndex()
							+ "\nname = "
							+ dim.getName()
							+ "\nID = "
							+ dim.getIdentifier()
							+ "\nsize = "
							+ dim.getSize()
							+ (dim.isHasDimensionScaleSet() ? ("\ndatatype = " + HDFConstants
									.getType(dim.getDatatype()))
									: ("")));
		} else if (object instanceof H4Annotation) {
			// //
			//
			// H4Annotation Information
			//
			// //
			H4Annotation ann = (H4Annotation) object;
			System.out
					.println("ANNOTATION INFO\n--------------------------\n"
							+ "annotation Type = "
							+ ann.getAnnotationTypeString(ann.getType())
							+ "\nID = "
							+ ann.getIdentifier()
							+ "\nTAG = "
							+ ann.getTag()
							+ "\nREF = "
							+ ann.getReference()
							+ "\ncontent = "
							+ ann.getContent()
							+ "\n--------------------------------------------------------------");
		} else if (object instanceof H4Attribute) {
			// //
			//
			// H4Attribute Information
			//
			// //
			H4Attribute attr = (H4Attribute) object;
			final int datatype = attr.getDatatype();
			System.out.print("ATTRIBUTE INFO: " + "index = " + attr.getIndex()
					+ " | name = " + attr.getName() + " | size = "
					+ attr.getSize() + " | datatype = "
					+ HDFConstants.getType(datatype) + "\nvalue = ");
			Object buf = attr.getValues();
			if (buf == null) {
				System.out.println("No values found");
				return;
			}
			printBuff(datatype, buf);
			System.out.println("\n--------------------------------");
		} else if (object instanceof H4VGroup) {
			// //
			//
			// H4VGroup Information
			//
			// //
			H4VGroup vgroup = (H4VGroup) object;
			System.out.println("GROUP INFO\n--------------------------\n"
					+ "ID = " + vgroup.getIdentifier() + "\nTAG = "
					+ vgroup.getTag() + "\nREF = " + vgroup.getReference()
					+ "\nclassName = " + vgroup.getClassName() + "\nname = "
					+ vgroup.getName() + "\nnumber of attributes = "
					+ vgroup.getNumAttributes()
					+ "\nnumber of objects in the group = "
					+ vgroup.getNumObjects()
					+ "\n======================================");

		} else if (object instanceof H4SDS) {
			// //
			//
			// H4SDS Information
			//
			// //
			H4SDS sds = (H4SDS) object;
			System.out
					.println("SDS INFO\n------------------------------------------------\n"
							+ "ID = "
							+ sds.getIdentifier()
							+ " | index = "
							+ sds.getIndex()
							+ " | Dimensions number (RANK) = "
							+ sds.getRank()
							+ " | name = "
							+ sds.getName()
							+ " | label annotations = "
							+ sds.getNLabels()
							+ " | description annotations = "
							+ sds.getNDescriptions()
							+ "\n_____________________________________________________");
		}
	}

	/**
	 * Print dimension scale values of the provided {@link H4Dimension}
	 * 
	 * @param dim
	 * @throws HDFException
	 */
	private void printDimensionScaleValues(H4Dimension dim) throws HDFException {
		final int datatype = dim.getDatatype();
		System.out.println("Dimension Scale size = " + dim.getSize()
				+ " datatype " + datatype);
		Object buf = dim.getDimensionScaleValues();
		if (buf == null) {
			System.out.println("No values found");
			return;
		}
		System.out.println("Dimension Scale values:");
		printBuff(datatype, buf);
		System.out.println("\n--------------------------------");
	}

	/**
	 * Print values contained in the provided data buffer of the specified
	 * datatype.
	 * 
	 * @param datatype
	 *            the data type of values
	 * @param buf
	 *            a buffer containing data values of a specific type
	 */
	private void printBuff(int datatype, Object buf) {
		if (datatype == HDFConstants.DFNT_FLOAT32
				|| datatype == HDFConstants.DFNT_FLOAT) {
			float[] ff = (float[]) buf;
			final int size = ff.length;
			for (int i = 0; i < size; i++) {
				System.out.print(ff[i]);
				System.out.print(" ");
			}
		} else if (datatype == HDFConstants.DFNT_DOUBLE
				|| datatype == HDFConstants.DFNT_FLOAT64) {
			double[] dd = (double[]) buf;
			final int size = dd.length;
			for (int i = 0; i < size; i++) {
				System.out.print(dd[i]);
				System.out.print(" ");
			}
		} else if (datatype == HDFConstants.DFNT_INT8
				|| datatype == HDFConstants.DFNT_UINT8) {
			byte[] bb = (byte[]) buf;
			final int size = bb.length;
			for (int i = 0; i < size; i++) {
				System.out.print(bb[i]);
				System.out.print(" ");
			}
		} else if (datatype == HDFConstants.DFNT_INT16
				|| datatype == HDFConstants.DFNT_UINT16) {
			short[] ss = (short[]) buf;
			final int size = ss.length;
			for (int i = 0; i < size; i++) {
				System.out.print(ss[i]);
				System.out.print(" ");
			}
		} else if (datatype == HDFConstants.DFNT_INT32
				|| datatype == HDFConstants.DFNT_UINT32) {
			int[] ii = (int[]) buf;
			final int size = ii.length;
			for (int i = 0; i < size; i++) {
				System.out.print(ii[i]);
				System.out.print(" ");
			}
		} else if (datatype == HDFConstants.DFNT_CHAR
				|| datatype == HDFConstants.DFNT_UCHAR8) {

			byte[] bb = (byte[]) buf;
			final int size = bb.length;
			StringBuffer sb = new StringBuffer(size);
			for (int i = 0; i < size && bb[i] != 0; i++) {
				sb.append(new String(bb, i, 1));
			}
			System.out.print(sb.toString());
		}
	}
}
