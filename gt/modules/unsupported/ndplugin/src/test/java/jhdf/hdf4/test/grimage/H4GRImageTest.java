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
package jhdf.hdf4.test.grimage;

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
import ncsa.hdf.object.Dataset;
import ncsa.hdf.object.h4.H4Datatype;

public class H4GRImageTest extends TestCase {
	private String testFilePath;

	public H4GRImageTest(String string) {
		super(string);
	}

	protected void setUp() throws Exception {
		super.setUp();
		testFilePath = "E:/work/data/hdf/TOVS_BROWSE_DAILY_AM_861031_NF.HDF";
	}

	public static void main(String[] args) {
		TestRunner.run(H4GRImageTest.class);
	}

	public static Test suite() {
		TestSuite suite = new TestSuite();
		suite.addTest(new H4GRImageTest("testGetInfo"));
		return suite;
	}

	public void testGetInfo() {
		int fileID = HDFConstants.FAIL;
		int grInterfaceID = HDFConstants.FAIL;
		boolean status = false;
		int grFileIinfo[] = new int[2];
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
			// start GR interface
			//
			// /////////////////////////////////////////////////////////////////
			grInterfaceID = HDFLibrary.GRstart(fileID);
			assertNotSame(grInterfaceID, HDFConstants.FAIL);
			System.out.println("GR Interface");

			// obtain GR file information and print them out
			status = HDFLibrary.GRfileinfo(grInterfaceID, grFileIinfo);
			assertTrue(status);
			System.out.println("");
			System.out.println("Identifier is " + grInterfaceID);
			System.out.println("Number of Raster Images is " + grFileIinfo[0]);
			System.out.println("Number of Attributes is " + grFileIinfo[1]);

			// ////////////////////////////////////////////////////////////////
			//
			// GR Collection attributes for this GR
			//
			// ////////////////////////////////////////////////////////////////
			System.out.println("");
			System.out.println("");
			final int numglobGRAttributes = grFileIinfo[1];
			final String[] globGRAttrName = new String[1];
			final int[] globGRAttrInfo = { 0, 0 };
			for (int ii = 0; ii < numglobGRAttributes; ii++) {
				System.out.println("");
				globGRAttrName[0] = "";
				// get various info about this attribute
				assertTrue(HDFLibrary.GRattrinfo(grInterfaceID, ii,
						globGRAttrName, globGRAttrInfo));
				System.out.println("GR Interface Attribute " + ii + " name "
						+ globGRAttrName[0]);
				System.out.println("GR Interface Attribute " + ii + " type "
						+ HDFConstants.getType(globGRAttrInfo[0]));
				// mask off the litend bit
				globGRAttrInfo[0] = globGRAttrInfo[0]
						& (~HDFConstants.DFNT_LITEND);

				System.out.println("GR Interface Attribute " + ii + " dim "
						+ globGRAttrInfo[1]);
				Object buf = H4Datatype.allocateArray(globGRAttrInfo[0],
						globGRAttrInfo[1]);
				assertTrue(HDFLibrary.GRgetattr(grInterfaceID, ii, buf));

				if (buf != null) {
					if (globGRAttrInfo[0] == HDFConstants.DFNT_CHAR
							|| globGRAttrInfo[0] == HDFConstants.DFNT_UCHAR8) {
						System.out.println("GR Interface Attribute "
								+ ii
								+ " value "
								+ Dataset.byteToString((byte[]) buf,
										globGRAttrInfo[1])[0]);
					} else {
						System.out.println("GR Interface Attribute " + ii
								+ " value " + buf);
						Utilities.printBuffer(buf, globGRAttrInfo[0]);
					}
				}
			}
			// ////////////////////////////////////////////////////////////////
			//
			// Access every Raster Image and print its index, reference, id,
			// name, number of components, dimension lengths and number of
			// attributes.
			//
			// ////////////////////////////////////////////////////////////////
			final int numGRimages = grFileIinfo[0];
			for (int i = 0; i < numGRimages; i++) {
				// //
				//
				// select the GR image
				//
				// //
				final int grID = HDFLibrary.GRselect(grInterfaceID, i);
				assertNotSame(grID, HDFConstants.FAIL);

				// //
				//
				// get basic info like name, size etc... and print it out
				//
				// //
				final int grInfo[] = { 0, 0, 0, 0 };
				final int dimSizes[] = { 0, 0 };
				String name[] = { "" };
				assertTrue(HDFLibrary.GRgetiminfo(grID, name, grInfo, dimSizes));

				System.out.println("");
				final int index = HDFLibrary.GRreftoindex(grInterfaceID,
						HDFLibrary.GRidtoref(grID));
				assertSame(index, i);
				assertNotSame(index, HDFConstants.FAIL);
				System.out
						.println("=====================================================================");
				System.out.println("------------------------> GR image index "
						+ index + " <------------------------");
				System.out
						.println("=====================================================================");
				System.out.println("\tGR image reference "
						+ HDFLibrary.GRidtoref(grID));
				System.out.println("\tGR image identifier " + grID);
				System.out.println("\tGR image name " + name[0]);
				System.out.println("\tGR image number of components "
						+ grInfo[0]);
				for (int j = 0; j < 2; j++)
					System.out.println("\tGR image dimension length: "
							+ dimSizes[j]);
				System.out.println("\tGR image datatype "
						+ HDFConstants.getType(grInfo[1]));
				final String interlaceString;

				switch (grInfo[2]) {
				case HDFConstants.MFGR_INTERLACE_PIXEL:
					interlaceString = "MFGR_INTERLACE_PIXEL";
					break;
				case HDFConstants.MFGR_INTERLACE_LINE:
					interlaceString = "MFGR_INTERLACE_LINE";
					break;
				case HDFConstants.MFGR_INTERLACE_COMPONENT:
					interlaceString = "MFGR_INTERLACE_COMPONENT";
					break;
				default:
					interlaceString = "Unknown";
					break;
				}

				System.out.println("\tGR image interlace mode " + grInfo[2]
						+ " = " + interlaceString);
				System.out.println("\tGR image attributes " + grInfo[3]);

				// //
				//
				// get compression information
				//
				// //
				System.out.println("\t\t      ===      ");
				final HDFCompInfo compInfo = new HDFCompInfo();
				status = HDFLibrary.GRgetcompress(grID, compInfo);
				final String compression;

				if (compInfo.ctype == HDFConstants.COMP_CODE_DEFLATE)
					compression = "GZIP";
				else if (compInfo.ctype == HDFConstants.COMP_CODE_JPEG)
					compression = "JPEG";
				else if (compInfo.ctype == HDFConstants.COMP_CODE_SKPHUFF)
					compression = "SKPHUFF";
				else if (compInfo.ctype == HDFConstants.COMP_CODE_RLE)
					compression = "RLE";
				else
					compression = "NONE";
				System.out.println("\tGR image compression " + compression);

				// //
				//
				// get chunk information
				//
				// //
				System.out.println("\t\t      ===      ");
				HDFChunkInfo chunkInfo = new HDFChunkInfo();
				final int[] cflag = { HDFConstants.HDF_NONE };
				status = HDFLibrary.GRgetchunkinfo(grID, chunkInfo, cflag);
				if (cflag[0] == HDFConstants.HDF_NONE)
					System.out.println("\tGR image has no chunking");
				else {
					for (int k = 0; k < 2; k++)
						System.out.println("\tGR image dimension " + k
								+ " has chunking "
								+ (long) chunkInfo.chunk_lengths[k]);
				}

				// //
				//
				// get palette information
				//
				// //
				System.out.println("\t\t      ===      ");
				final int nPalettes = HDFLibrary.GRgetnluts(grID);

				// Getting Number of palettes
				if (nPalettes == 0)
					System.out.println("\tGR image has no palettes");
				else
					System.out.println("\tGR image has " + nPalettes
							+ " palette" + ((nPalettes > 1) ? "s" : ""));

				// Palette scan
				for (int pal = 0; pal < nPalettes; pal++) {

					// Getting palette ID
					int lutID = HDFLibrary.GRgetlutid(grID, pal);
					final int lutInfo[] = new int[] { 0, 0, 0, 0 };

					// Getting palette information
					assertTrue(HDFLibrary.GRgetlutinfo(lutID, lutInfo));
					System.out
							.println("\t\t=====================================");
					System.out.println("\t\t--------> GR Palette index " + pal
							+ " <-------");
					System.out
							.println("\t\t=====================================");

					// palette reference
					System.out.println("\t\tPalette reference "
							+ HDFLibrary.GRluttoref(lutID));

					// palette identifier
					System.out.println("\t\tPalette identifier " + lutID);

					// palette number of components
					System.out.println("\t\tPalette number of components "
							+ lutInfo[0]);

					// palette datatype
					System.out.println("\t\tPalette datatype "
							+ HDFConstants.getType(lutInfo[1]));

					// palette interlace-mode
					final String paletteInterlaceString;
					switch (lutInfo[2]) {
					case HDFConstants.MFGR_INTERLACE_PIXEL:
						paletteInterlaceString = "MFGR_INTERLACE_PIXEL";
						break;
					case HDFConstants.MFGR_INTERLACE_LINE:
						paletteInterlaceString = "MFGR_INTERLACE_LINE";
						break;
					case HDFConstants.MFGR_INTERLACE_COMPONENT:
						paletteInterlaceString = "MFGR_INTERLACE_COMPONENT";
						break;
					default:
						paletteInterlaceString = "Unknown";
						break;
					}
					System.out.println("\t\tPalette interlace mode "
							+ lutInfo[2] + " = " + paletteInterlaceString);

					// palette entries
					final int paletteEntries = lutInfo[3];
					System.out.println("\t\tPalette number of entries "
							+ paletteEntries);

					// preparing palette read by setting interlace mode
					HDFLibrary.GRreqlutil(lutID, lutInfo[2]);
					byte[] paletteValues = new byte[3 * 256];
					HDFLibrary.GRreadlut(lutID, paletteValues);
					System.out.println("\t\tPalette values");

					// palette visualization
					for (int pv = 0; pv < 768; pv++) {
						byte paletteValue = paletteValues[pv];
						if (pv % 3 == 0)
							System.out.print("     ");
						if (pv % 24 == 0)
							System.out.print("\n");
						if (paletteValue < 10 && paletteValue >= 0)
							System.out.print("    ");
						else if ((paletteValue < 100 && paletteValue >= 10)
								|| (paletteValue < 0 && paletteValue >= -10))
							System.out.print("   ");
						else if (paletteValue >= 100
								|| (paletteValue < -10 & paletteValue > -100))
							System.out.print("  ");
						else if (paletteValue <= -100)
							System.out.print(" ");
						System.out.print(paletteValue);
					}
				}

				// ////////////////////////////////////////////////////////////////
				//
				// GR attributes for this GR image
				//
				// ////////////////////////////////////////////////////////////////
				System.out.println("");
				final int numGRimageAttributes = grInfo[2];
				final String[] grImageAttrName = new String[1];
				final int[] grAttrInfo = { 0, 0 };
				for (int ii = 0; ii < numGRimageAttributes; ii++) {
					grImageAttrName[0] = "";
					// get various info about this attribute
					assertTrue(HDFLibrary.GRattrinfo(grID, ii, grImageAttrName,
							grAttrInfo));
					System.out.println("\tGR Image Attribute " + ii + " name "
							+ grImageAttrName[0]);
					// mask off the litend bit
					grAttrInfo[0] = grAttrInfo[0] & (~HDFConstants.DFNT_LITEND);
					System.out.println("\tGR Image Attribute " + ii + " dim "
							+ grAttrInfo[1]);
					Object buf = H4Datatype.allocateArray(grAttrInfo[0],
							grAttrInfo[1]);
					assertTrue(HDFLibrary.GRgetattr(grID, ii, buf));

					if (buf != null) {
						if (grAttrInfo[0] == HDFConstants.DFNT_CHAR
								|| grAttrInfo[0] == HDFConstants.DFNT_UCHAR8) {
							System.out.println("\tGR Image Attribute "
									+ ii
									+ " value "
									+ Dataset.byteToString((byte[]) buf,
											grAttrInfo[1])[0]);
						} else {
							System.out.print("\tGR Image Attribute " + ii
									+ " value ");
							Utilities.printBuffer(buf, grAttrInfo[0]);
						}
					}
				}

				// ////////////////////////////////////////////////////////////////
				//
				// GR PREDEFINED attributes for this GR image
				//
				// ////////////////////////////////////////////////////////////////
				System.out.println("\t\t      ===      ");
				printAttributeByName(grID, "FILL_ATTR",
						"\tGR Image PREDEFINED ");

				// Disposing GR Image
				HDFLibrary.GRendaccess(grID);
			}

		} catch (HDFException e) {
			e.printStackTrace();
		} finally {

			// Disposing GR Interface
			if (grInterfaceID != HDFConstants.FAIL)
				try {
					HDFLibrary.GRend(grInterfaceID);
				} catch (HDFException e) {
					e.printStackTrace();
				}
			// Disposing File
			if (fileID != HDFConstants.FAIL)
				try {
					HDFLibrary.Hclose(fileID);
				} catch (HDFException e) {
					e.printStackTrace();
				}

		}
	}

	private void printAttributeByName(int grID, String attributeName,
			String message) throws HDFException {

		final String[] grImagePredefAttrName = { attributeName };
		final int[] grPredefAttrInfo = { 0, 0 };
		// //
		//
		// Look for the attribute index in the provided interface for the
		// provided attribute name
		//
		// //
		final int attributeIndex = HDFLibrary.GRfindattr(grID,
				grImagePredefAttrName[0]);
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
		grImagePredefAttrName[0] = "";
		assertTrue(HDFLibrary.GRattrinfo(grID, attributeIndex,
				grImagePredefAttrName, grPredefAttrInfo));
		System.out.println(message + " Attribute " + attributeName + " index "
				+ attributeIndex);
		System.out.println(message + " Attribute " + attributeName + " type "
				+ HDFConstants.getType(grPredefAttrInfo[0]));
		// mask off the litend bit
		grPredefAttrInfo[0] = grPredefAttrInfo[0] & (~HDFConstants.DFNT_LITEND);

		// //
		//
		// Get the value for this attribute
		//
		// //
		Object buf = H4Datatype.allocateArray(grPredefAttrInfo[0],
				grPredefAttrInfo[1]);
		assertNotNull(buf);
		assertTrue(HDFLibrary.GRgetattr(grID, attributeIndex, buf));
		assertNotNull(buf);

		// //
		//
		// Print the value for this attribute
		//
		// //
		if (grPredefAttrInfo[0] == HDFConstants.DFNT_CHAR
				|| grPredefAttrInfo[0] == HDFConstants.DFNT_UCHAR8) {
			System.out
					.println(message
							+ " Attribute "
							+ attributeName
							+ " value "
							+ Dataset.byteToString((byte[]) buf,
									grPredefAttrInfo[1])[0]);

		} else {

		}

	}
}
