/*
 *    JImageIO-extension - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    (C) 2007, GeoSolutions
 *    
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2005-2006, GeoTools Project Managment Committee (PMC)
 *    (C) 2005, Refractions Research Inc.
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
 */
package jhdf.hdf4.test.sds;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;

import jhdf.hdf4.test.Utilities;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import ncsa.hdf.hdflib.HDFChunkInfo;
import ncsa.hdf.hdflib.HDFCompInfo;
import ncsa.hdf.hdflib.HDFConstants;
import ncsa.hdf.hdflib.HDFException;
import ncsa.hdf.hdflib.HDFLibrary;
import ncsa.hdf.hdflib.HDFNativeData;
import ncsa.hdf.object.Dataset;
import ncsa.hdf.object.h4.H4Datatype;

public class H4SDSTest extends TestCase {
	public H4SDSTest(String string) {
		super(string);
	}

	private final static boolean VISUALIZE_FIRST_CHUNK_VALUES = true;

	private final static boolean VISUALIZE_DIMENSION_SCALES_VALUES = true;

	private final static boolean VISUALIZE_ATTRIBUTES = true;

	private final static boolean VISUALIZE_DIMENSIONS = true;

	private String testFilePath;

	private final String chunkTestFilePath = "E:/work/data/hdf/MISR_AM1_CGLS_WIN_2005_F04_0017.hdf";

	private final String dimensionScaleTestFilePath = "E:/Work/data/HDF/TOVS_5DAYS_AM_B870511.E870515_NG.HDF";

	protected void setUp() throws Exception {
		super.setUp();
		testFilePath = dimensionScaleTestFilePath;
	}

	public static void main(String[] args) {
		TestRunner.run(H4SDSTest.class);
	}

	public static Test suite() {
		TestSuite suite = new TestSuite();
		suite.addTest(new H4SDSTest("testGetInfo"));
		return suite;
	}

	public void testGetInfo() {
		int fileID = HDFConstants.FAIL;
		int sdsInterfaceID = HDFConstants.FAIL;
		boolean status = false;
		int sdsFileIinfo[] = new int[2];
		try {
			// /////////////////////////////////////////////////////////////////
			//
			// file id, useless but nice to have :-)
			//
			// /////////////////////////////////////////////////////////////////
			fileID = HDFLibrary.Hopen(testFilePath);
			assertNotSame(fileID, HDFConstants.FAIL);

			// /////////////////////////////////////////////////////////////////
			//
			// start sds interface
			//
			// /////////////////////////////////////////////////////////////////
			sdsInterfaceID = HDFLibrary.SDstart(testFilePath,
					HDFConstants.DFACC_RDONLY | HDFConstants.DFACC_PARALLEL);
			assertNotSame(sdsInterfaceID, HDFConstants.FAIL);
			final int sdsInterfaceID1 = HDFLibrary.SDstart(testFilePath,
					HDFConstants.DFACC_RDONLY | HDFConstants.DFACC_PARALLEL);
			// //
			//
			// NOTE that the SDS interface ID is not the same every time we open
			// it up
			//
			// //
			assertNotSame(sdsInterfaceID, sdsInterfaceID1);
			HDFLibrary.SDend(sdsInterfaceID1);

			// obtain sds file information and print them out
			status = HDFLibrary.SDfileinfo(sdsInterfaceID, sdsFileIinfo);
			assertTrue(status);
			System.out.println("");
			System.out.println("");
			System.out
					.println("Number of SDS Datasets is (comprehending dimension scales) "
							+ sdsFileIinfo[0]);
			System.out.println("Number of SDS Collection Attributes is "
					+ sdsFileIinfo[1]);

			// ////////////////////////////////////////////////////////////////
			//
			// SDS Collection attributes for this sds
			//
			// ////////////////////////////////////////////////////////////////
			System.out.println("");
			System.out.println("");
			final int numglobSDSAttributes = sdsFileIinfo[1];
			final String[] globSDSAttrName = new String[1];
			final int[] globSDSAttrInfo = { 0, 0 };
			for (int ii = 0; ii < numglobSDSAttributes; ii++) {
				System.out.println("");
				globSDSAttrName[0] = "";

				// get various info about this attribute
				assertTrue(HDFLibrary.SDattrinfo(sdsInterfaceID, ii,
						globSDSAttrName, globSDSAttrInfo));
				System.out.println("SDS Interface Attribute " + ii + " name "
						+ globSDSAttrName[0]);
				System.out.println("SDS Interface Attribute " + ii + " type "
						+ HDFConstants.getType(globSDSAttrInfo[0]));
				// mask off the litend bit
				globSDSAttrInfo[0] = globSDSAttrInfo[0]
						& (~HDFConstants.DFNT_LITEND);
				System.out.println("SDS Interface Attribute " + ii + " dim "
						+ globSDSAttrInfo[1]);
				Object buf = H4Datatype.allocateArray(globSDSAttrInfo[0],
						globSDSAttrInfo[1]);
				assertTrue(HDFLibrary.SDreadattr(sdsInterfaceID, ii, buf));

				if (buf != null) {
					if (globSDSAttrInfo[0] == HDFConstants.DFNT_CHAR
							|| globSDSAttrInfo[0] == HDFConstants.DFNT_UCHAR8) {
						System.out.println("SDS Interface Attribute value "
								+ Dataset.byteToString((byte[]) buf,
										globSDSAttrInfo[1])[0]);
					}

				} else {
					System.out.print("SDS Interface Attribute " + ii
							+ " value ");
					Utilities.printBuffer(buf, globSDSAttrInfo[0]);
				}
			}

			// ////////////////////////////////////////////////////////////////
			//
			// Access every data set and print its name, rank, dimension sizes,
			// data type, and number of attributes. The following information
			// should be displayed:
			// 
			// name = SDStemplate rank = 2 dimension sizes are : 16 5 data type
			// is 24 number of attributes is 0
			//
			// ////////////////////////////////////////////////////////////////
			final int numSDSDataSets = sdsFileIinfo[0];
			for (int i = 0; i < numSDSDataSets; i++) {
				// //
				//
				// select the sds
				//
				// //
				int sdsID = HDFLibrary.SDselect(sdsInterfaceID, i);
				assertNotSame(sdsID, HDFConstants.FAIL);

				// //
				//
				// get basic info like name, size etc... and print it out
				//
				// //
				final int sdInfo[] = { 0, 0, 0 };
				final int dimSizes[] = new int[HDFConstants.MAX_VAR_DIMS];
				String name[] = { "" };
				status = HDFLibrary.SDgetinfo(sdsID, name, dimSizes, sdInfo);
				assertTrue(status);

				System.out.println("");
				final int index = HDFLibrary.SDreftoindex(sdsInterfaceID,
						HDFLibrary.SDidtoref(sdsID));
				assertNotSame(index, HDFConstants.FAIL);
				System.out
						.println("=====================================================================");
				System.out
						.println("-----------------------> SDS Dataset index "
								+ index + " <----------------------");
				System.out
						.println("=====================================================================");
				System.out.println("\tSDS Dataset reference "
						+ HDFLibrary.SDidtoref(sdsID));
				System.out.println("\tSDS Dataset identifier " + sdsID);

				System.out.println("\tSDS Dataset name " + name[0]);
				System.out.println("\tSDS Dataset rank " + sdInfo[0]);
				final int rank = sdInfo[0];
				for (int j = 0; j < rank; j++)
					System.out.println("\tSDS Dataset dimension length: "
							+ dimSizes[j]);
				final int dataType = sdInfo[1];
				final int typeSize = HDFConstants.getTypeSize(dataType);
				final String dataTypeString = HDFConstants.getType(dataType);
				System.out.println("\tSDS Dataset datatype " + dataTypeString);
				System.out.println("\tSDS Dataset num attributes " + sdInfo[2]);

				// //
				// 
				// Dimension scale information
				//
				// //
				final boolean isDimensionScale = HDFLibrary.SDiscoordvar(sdsID);
				System.out.println("\tSDS Dataset is dimension scale "
						+ isDimensionScale);

				if (isDimensionScale) {
					final int dimSize = dimSizes[0];
					byte b[] = new byte[dimSize * typeSize];
					int dimID = HDFLibrary.SDgetdimid(sdsID, 0);
					assertTrue(dimID != HDFConstants.FAIL);
					HDFLibrary.SDgetdimscale(dimID, b);
					if (VISUALIZE_DIMENSION_SCALES_VALUES) {
						System.out.print("\tDimension scale values\n\t");
						printValues(b, dataTypeString, dimSize);
					}
					System.out.print("\n");
				}
				// //
				//
				// is empty?
				//
				// //
				final int empty[] = { 0 };
				assertTrue(HDFLibrary.SDcheckempty(sdsID, empty));
				if (empty[0] == 1)
					System.out.println("\tSDS dataset is empty ");

				// //
				//
				// get compression information
				//
				// //
				System.out.println("\t\t      ===      ");
				final HDFCompInfo compInfo = new HDFCompInfo();
				status = HDFLibrary.SDgetcompress(sdsID, compInfo);
				String compression = "NONE";
				// print it out
				if (compInfo.ctype == HDFConstants.COMP_CODE_DEFLATE)
					compression = "GZIP";
				else if (compInfo.ctype == HDFConstants.COMP_CODE_SZIP)
					compression = "SZIP";
				else if (compInfo.ctype == HDFConstants.COMP_CODE_JPEG)
					compression = "JPEG";
				else if (compInfo.ctype == HDFConstants.COMP_CODE_SKPHUFF)
					compression = "SKPHUFF";
				else if (compInfo.ctype == HDFConstants.COMP_CODE_RLE)
					compression = "RLE";
				else if (compInfo.ctype == HDFConstants.COMP_CODE_NBIT)
					compression = "NBIT";
				System.out.println("\tSDS dataset compression " + compression);

				// //
				//
				// get chunk information
				//
				// //
				System.out.println("\t\t      ===      ");
				HDFChunkInfo chunkInfo = new HDFChunkInfo();
				final int[] cflag = { HDFConstants.HDF_NONE };
				status = HDFLibrary.SDgetchunkinfo(sdsID, chunkInfo, cflag);
				if (cflag[0] == HDFConstants.HDF_NONE)
					System.out.println("\tSDS dataset has no chunking");
				else {
					int chunkValues = 1;
					int chunkCoordinates[] = new int[rank];
					for (int k = 0; k < rank; k++) {
						System.out.println("\tSDS dataset dimension " + k
								+ " has chunking "
								+ (long) chunkInfo.chunk_lengths[k]);
						chunkValues *= chunkInfo.chunk_lengths[k];
						chunkCoordinates[k] = 0;
					}
					if (VISUALIZE_FIRST_CHUNK_VALUES) {
						final int dimSize = chunkValues * typeSize;
						byte chunksToBread[] = new byte[dimSize];
						assertTrue(HDFLibrary.SDreadchunk(sdsID,
								chunkCoordinates, chunksToBread));
						System.out.print("\tChunk values\n\t");
						printValues(chunksToBread, dataTypeString, chunkValues);
					}
				}

				// ////////////////////////////////////////////////////////////////
				//
				// SDS attributes for this sds
				//
				// ////////////////////////////////////////////////////////////////
				System.out.println("\t\t      ===      ");
				if (VISUALIZE_ATTRIBUTES) {
					final int numSDSAttributes = sdInfo[2];
					final String[] SDSAttrName = new String[1];
					final int[] SDSAttrInfo = { 0, 0 };
					for (int ii = 0; ii < numSDSAttributes; ii++) {
						SDSAttrName[0] = "";
						// get various info about this attribute
						assertTrue(HDFLibrary.SDattrinfo(sdsID, ii,
								SDSAttrName, SDSAttrInfo));
						System.out.println("\tSDS Dataset Attribute " + ii
								+ " name " + SDSAttrName[0]);
						// mask off the litend bit
						SDSAttrInfo[0] = SDSAttrInfo[0]
								& (~HDFConstants.DFNT_LITEND);
						System.out.println("\tSDS Dataset Attribute " + ii
								+ " dim " + SDSAttrInfo[1]);
						Object buf = H4Datatype.allocateArray(SDSAttrInfo[0],
								SDSAttrInfo[1]);
						assertTrue(HDFLibrary.SDreadattr(sdsID, ii, buf));

						if (buf != null) {
							if (SDSAttrInfo[0] == HDFConstants.DFNT_CHAR
									|| SDSAttrInfo[0] == HDFConstants.DFNT_UCHAR8) {
								System.out
										.println("\tSDS Dataset Attribute value "
												+ Dataset.byteToString(
														(byte[]) buf,
														SDSAttrInfo[1])[0]);
							} else {
								System.out.print("\tSDS Dataset Attribute "
										+ ii + " value ");
								Utilities.printBuffer(buf, SDSAttrInfo[0]);
							}
						}
					}

					// ////////////////////////////////////////////////////////////////
					//
					// SDS PREDEFINED attributes for this sds
					//
					// ////////////////////////////////////////////////////////////////
					System.out.println("\t\t      ===      ");
					printAttributeByName(sdsID, "long_name",
							"\tSDS PREDEFINED ");
					printAttributeByName(sdsID, "units", "\tSDS PREDEFINED ");
					printAttributeByName(sdsID, "format", "\tSDS PREDEFINED ");
					printAttributeByName(sdsID, "coordsys", "\tSDS PREDEFINED ");
					final Object fillValue = SDgetfillvalue(sdsID);
					if (fillValue != null)
						System.out.println("\tSDS PREDEFINED _fillValue "
								+ fillValue);
					final double[] range = { 0.0, 0.0 };
					if (HDFLibrary.SDgetrange(sdsID, range))
						System.out.println("\tSDS PREDEFINED range " + range[1]
								+ " " + range[0]);
					final double calibrationParams[] = { 0.0, 0.0, 0.0, 0.0 };
					int[] NT = { 0 };
					if (HDFLibrary.SDgetcal(sdsID, calibrationParams, NT)) {
						System.out
								.println("\tSDS PREDEFINED calibration factor "
										+ calibrationParams[0]);
						System.out
								.println("\tSDS PREDEFINED calibration error "
										+ calibrationParams[1]);
						System.out.println("\tSDS PREDEFINED offset "
								+ calibrationParams[2]);
						System.out.println("\tSDS PREDEFINED offset factor "
								+ calibrationParams[3]);
					}
				}

				// ////////////////////////////////////////////////////////////////
				//
				// Dimension information
				//
				// ////////////////////////////////////////////////////////////////

				if (VISUALIZE_DIMENSIONS) {
					for (int r = 0; r < rank; r++) {
						// get the id of the first dimension for this dataset
						final int dimensionID = HDFLibrary.SDgetdimid(sdsID, r);
						assertNotSame(dimensionID, HDFConstants.FAIL);
						System.out
								.println("\t===============================================================");
						System.out
								.println("\t--------------> SDS dataset dimension index "
										+ r + " <---------------");
						System.out
								.println("\t===============================================================");
						System.out.println("\t\tSDS dataset dimension id "
								+ dimensionID);
						final String[] dimName = { "" };
						final int[] dimInfo = { 0, 0, 0 };
						assertTrue(HDFLibrary.SDdiminfo(dimensionID, dimName,
								dimInfo));
						System.out.println("\t\tSDS dataset dimension name "
								+ dimName[0]);
						System.out
								.println("\t\tSDS dataset dimension datatype "
										+ HDFConstants.getType(dimInfo[1]));
						System.out
								.println("\t\tSDS dataset dimension num attributes "
										+ dimInfo[2]);

						// dimension predefined attributes
						if (dimInfo[2] > 0) {
							final String predefinedAttributes[] = { "NONE",
									"NONE", "NONE" };
							HDFLibrary.SDgetdimstrs(dimensionID,
									predefinedAttributes,
									HDFConstants.DFS_MAXLEN);
							System.out
									.println("\t\tSDS dataset dimension long_name "
											+ predefinedAttributes[0]);
							System.out
									.println("\t\tSDS dataset dimension unit "
											+ predefinedAttributes[1]);
							System.out
									.println("\t\tSDS dataset dimension format "
											+ predefinedAttributes[2]);
						}

						// ////////////////////////////////////////////////////////////////
						//
						// SDS PREDEFINED attributes for this sds
						//
						// ////////////////////////////////////////////////////////////////
						System.out.println("\t\t\t     ===      ");
						printAttributeByName(dimensionID, "long_name",
								"\t\tSDS dataset dimension ");
						printAttributeByName(dimensionID, "units",
								"\t\tSDS dataset dimension ");
						printAttributeByName(dimensionID, "format",
								"\t\tSDS dataset dimension ");

						// //
						//
						// dimension attributes
						//
						// //
						System.out.println("\t\t\t      ===      ");
						final int numDimensionAttributes = dimInfo[2];
						final String[] dimAttrName = new String[1];
						final int[] dimAttrInfo = { 0, 0 };
						for (int ii = 0; ii < numDimensionAttributes; ii++) {
							dimAttrName[0] = "";
							// get various info about this attribute
							assertTrue(HDFLibrary.SDattrinfo(dimensionID, ii,
									dimAttrName, dimAttrInfo));
							System.out.println("\t\tSDS Dimension Attribute "
									+ ii + " name " + dimAttrName[0]);
							// mask off the litend bit
							dimAttrInfo[0] = dimAttrInfo[0]
									& (~HDFConstants.DFNT_LITEND);
							System.out.println("\t\tSDS Dimension Attribute "
									+ ii + " dim " + dimAttrInfo[1]);
							Object buf = H4Datatype.allocateArray(
									dimAttrInfo[0], dimAttrInfo[1]);
							assertTrue(HDFLibrary.SDreadattr(dimensionID, ii,
									buf));

							if (buf != null) {
								if (dimAttrInfo[0] == HDFConstants.DFNT_CHAR
										|| dimAttrInfo[0] == HDFConstants.DFNT_UCHAR8) {
									System.out
											.println("\t\tSDS Dimension Attribute value "
													+ Dataset.byteToString(
															(byte[]) buf,
															dimAttrInfo[1])[0]);
								}

								else {
									System.out
											.print("\t\tSDS Dimension Attribute "
													+ ii + " value ");
									Utilities.printBuffer(buf, dimAttrInfo[0]);
								}
							}
						}
					}

				}
				// //
				//
				// end access
				//
				// //
				HDFLibrary.SDend(sdsID);

			}
		} catch (HDFException e) {
			e.printStackTrace();
		} finally {
			if (sdsInterfaceID != HDFConstants.FAIL)
				try {
					HDFLibrary.SDend(sdsInterfaceID);
				} catch (HDFException e) {
					e.printStackTrace();
				}
			if (fileID != HDFConstants.FAIL)
				try {
					HDFLibrary.Hclose(fileID);
				} catch (HDFException e) {
					e.printStackTrace();
				}

		}
	}

	private void printValues(byte[] b, String dataTypeString, final int nValues) {
		ByteBuffer bb = null;
		Buffer scaleBuffer = null;
		bb = ByteBuffer.wrap(b);
		bb.order(ByteOrder.nativeOrder());

		if (dataTypeString.equals(HDFConstants.INT16)) {
			scaleBuffer = bb.asShortBuffer();
			for (int kk = 0; kk < nValues;)
				System.out.print(((ShortBuffer) scaleBuffer).get(kk++)
						+ ((kk % 10 == 0) ? "\n\t" : " "));
		} else if (dataTypeString.equals(HDFConstants.INT32)) {
			scaleBuffer = bb.asIntBuffer();
			for (int kk = 0; kk < nValues;)
				System.out.print(((IntBuffer) scaleBuffer).get(kk++)
						+ ((kk % 10 == 0) ? "\n\t" : " "));
		} else if (dataTypeString.equals(HDFConstants.INT64)) {
			scaleBuffer = bb.asLongBuffer();
			for (int kk = 0; kk < nValues;)
				System.out.print(((LongBuffer) scaleBuffer).get(kk++)
						+ ((+kk % 10 == 0) ? "\n\t" : " "));
		} else if (dataTypeString.equals(HDFConstants.FLOAT32)) {
			scaleBuffer = bb.asFloatBuffer();
			for (int kk = 0; kk < nValues;)
				System.out.print(((FloatBuffer) scaleBuffer).get(kk++)
						+ ((kk % 10 == 0) ? "\n\t" : " "));
		} else if (dataTypeString.equals(HDFConstants.FLOAT64)) {
			scaleBuffer = bb.asDoubleBuffer();
			for (int kk = 0; kk < nValues;)
				System.out.print(((DoubleBuffer) scaleBuffer).get(kk++)
						+ ((kk % 10 == 0) ? "\n\t" : " "));
		} else
			scaleBuffer = null;

	}

	private void printAttributeByName(int sdsID, String attributeName,
			String message) throws HDFException {

		final String[] SDSPredefAttrName = { attributeName };
		final int[] SDSPredefAttrInfo = { 0, 0 };
		// //
		//
		// Look for the attribute index in the provided interface for the
		// provided attribute name
		//
		// //
		final int attributeIndex = HDFLibrary.SDfindattr(sdsID,
				SDSPredefAttrName[0]);
		if (attributeIndex == HDFConstants.FAIL) {
			System.out
					.println(message + " " + attributeName + " not present. ");
			return;
		}

		// //
		//
		// Get the attribute info for the the attribute index we have found
		//
		// //
		SDSPredefAttrName[0] = "";
		assertTrue(HDFLibrary.SDattrinfo(sdsID, attributeIndex,
				SDSPredefAttrName, SDSPredefAttrInfo));
		System.out.println(message + " Attribute " + attributeName + " index "
				+ attributeIndex);
		System.out.println(message + " Attribute " + attributeName + " type "
				+ HDFConstants.getType(SDSPredefAttrInfo[0]));
		// mask off the litend bit
		SDSPredefAttrInfo[0] = SDSPredefAttrInfo[0]
				& (~HDFConstants.DFNT_LITEND);

		// //
		//
		// Get the value for this attribute
		//
		// //
		Object buf = H4Datatype.allocateArray(SDSPredefAttrInfo[0],
				SDSPredefAttrInfo[1]);
		assertNotNull(buf);
		assertTrue(HDFLibrary.SDreadattr(sdsID, attributeIndex, buf));
		assertNotNull(buf);

		// //
		//
		// Print the value for this attribute
		//
		// //
		if (SDSPredefAttrInfo[0] == HDFConstants.DFNT_CHAR
				|| SDSPredefAttrInfo[0] == HDFConstants.DFNT_UCHAR8) {
			System.out
					.println(message
							+ " Attribute "
							+ attributeName
							+ " value "
							+ Dataset.byteToString((byte[]) buf,
									SDSPredefAttrInfo[1])[0]);
		} else {
			System.out.print(message + " Attribute " + attributeName
					+ " value ");
			Utilities.printBuffer(buf, SDSPredefAttrInfo[0]);
		}
	}

	/**
	 * @param sdsid
	 *            <b>IN</b>: id of the SDS as returned by SDselect
	 * @param theFillValue
	 *            <b>OUT</b>: Object[1], one object of appropriate type
	 * 
	 * @exception ncsa.hdf.hdflib.HDFException
	 *                should be thrown for errors in the HDF library call, but
	 *                is not yet implemented.
	 * 
	 * 
	 * @return the the data in the Java array: theFillValue[0] = fillValue
	 * 
	 * <p>
	 * <b>Note:</b> the routine calls SDgetinfo to determine the correct type,
	 * reads the data as bytes, and converts to the appropriate Java object.
	 */
	public static Object SDgetfillvalue(int sdsid) throws HDFException {
		final int[] SDInfo = new int[3];

		final String ss[] = { "" };
		final int dimsize[] = new int[16];

		// //
		//
		// Get information about the data type of this SDS in order to return a
		// correct object back
		//
		// //
		HDFLibrary.SDgetinfo(sdsid, ss, dimsize, SDInfo);
		int numberType = SDInfo[1];
		final HDFNativeData convert = new HDFNativeData();
		final byte[] d1 = new byte[8];

		// //
		//
		// Get the fill value itself
		//
		// //
		if (!HDFLibrary.SDgetfillvalue(sdsid, d1))
			return null;

		// //
		//
		// Convert it to the right type
		//
		// //
		return convertBytesToObject(numberType, convert, d1);
	}

	private static Object convertBytesToObject(int numberType,
			final HDFNativeData convert, final byte[] d1) {
		if ((numberType & HDFConstants.DFNT_LITEND) != 0) {
			numberType -= HDFConstants.DFNT_LITEND;
		}
		if ((numberType == HDFConstants.DFNT_INT8)
				|| (numberType == HDFConstants.DFNT_CHAR8)
				|| (numberType == HDFConstants.DFNT_CHAR)) {
			return new Byte(d1[0]);
		} else if ((numberType == HDFConstants.DFNT_UINT8)
				|| (numberType == HDFConstants.DFNT_UCHAR8)
				|| (numberType == HDFConstants.DFNT_UCHAR8)) {
			Byte f = new Byte(d1[0]);

			if (f.shortValue() < 0) {
				return new Short((short) (f.intValue() + 256));
			} else {
				return new Short(f.shortValue());
			}
		} else if ((numberType == HDFConstants.DFNT_INT16)
				|| (numberType == HDFConstants.DFNT_CHAR16)) {
			short[] fx = convert.byteToShort(0, 1, d1);
			return new Short(fx[0]);
		} else if ((numberType == HDFConstants.DFNT_UINT16)
				|| (numberType == HDFConstants.DFNT_UCHAR16)) {
			short[] fmx = convert.byteToShort(0, 1, d1);
			Short f = new Short(fmx[0]);
			if (f.intValue() < 0) {
				return new Integer(f.intValue() + 65536);
			} else {
				return new Integer(f.intValue());
			}
		} else if ((numberType == HDFConstants.DFNT_INT32)) {
			int[] fx = convert.byteToInt(0, 1, d1);
			return new Integer(fx[0]);
		} else if ((numberType == HDFConstants.DFNT_UINT32)) {
			int[] fmx = convert.byteToInt(0, 1, d1);
			Integer i = new Integer(fmx[0]);
			if (i.floatValue() < 0) {
				return new Float((float) (i.floatValue() + 4294967296.0));
			} else {
				return new Float(i.floatValue());
			}
		} else if (numberType == HDFConstants.DFNT_FLOAT32) {
			float[] fx = convert.byteToFloat(0, 1, d1);
			return new Float(fx[0]);
		} else if (numberType == HDFConstants.DFNT_FLOAT64) {
			double[] fx = convert.byteToDouble(0, 1, d1);
			return new Double(fx[0]);
		} else {
			System.out.println("Error: SDgetfillvalue not converting, type "
					+ numberType);
		}
		return null;
	}
}
