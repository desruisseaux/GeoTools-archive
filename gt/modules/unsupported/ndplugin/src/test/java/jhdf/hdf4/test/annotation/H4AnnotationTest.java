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
package jhdf.hdf4.test.annotation;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import ncsa.hdf.hdflib.HDFConstants;
import ncsa.hdf.hdflib.HDFLibrary;

public class H4AnnotationTest extends TestCase {

	private final static String TESTPATH_FOR_OBSOLETETAGS_ANNOTATION = "E:/Work/data/HDF/TOVS_5DAYS_AM_B870511.E870515_NG.HDF";

	private final static String TESTPATH_FOR_NEWTAGS_ANNOTATION = "E:/Work/data/HDF/TOVS_BROWSE_DAILY_AM_861031_NF.HDF";

	public H4AnnotationTest(String string) {
		super(string);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	public static void main(String[] args) {
		TestRunner.run(suite());
	}

	public static Test suite() {
		TestSuite suite = new TestSuite();
		suite.addTest(new H4AnnotationTest("testObsoleteTags"));

		suite.addTest(new H4AnnotationTest("testNewTags"));

		return suite;
	}

	public void testObsoleteTags() {
		testGetInfo(TESTPATH_FOR_OBSOLETETAGS_ANNOTATION);
	}

	public void testNewTags() {
		testGetInfo(TESTPATH_FOR_NEWTAGS_ANNOTATION);
	}

	private void testGetInfo(final String testFilePath) {
		int fileID = HDFConstants.FAIL;
		try {
			// //
			// Open the HDF file for reading.
			// //
			fileID = HDFLibrary.Hopen(testFilePath, HDFConstants.DFACC_READ);
			assertNotSame(fileID, HDFConstants.FAIL);

			// //
			// Initialize the V interface.
			// //
			final int anInterfaceID = HDFLibrary.ANstart(fileID);
			int annotationsInfo[] = new int[] { 0, 0, 0, 0 };

			System.out.println("This file contains the following annotations");
			HDFLibrary.ANfileinfo(anInterfaceID, annotationsInfo);
			final String[] annotationsType = new String[] {
					"file label annotations", "file description annotations",
					"data object label annotations",
					"data object description annotations" };
			for (int i = 0; i < 4; i++) {
				System.out.println(annotationsInfo[i] + " "
						+ annotationsType[i]);
			}
			System.out.println("Retrieving Annotations info");

			// //
			// Retrieving file label annotations
			// //
			if (annotationsInfo[0] != 0) {
				final int fileLabels = annotationsInfo[0];
				System.out.println("\n===========================");
				System.out.print("file label annotations info:");
				System.out.println("Quantity = " + fileLabels);
				for (int i = 0; i < fileLabels; i++) {
					final int annID = HDFLibrary.ANselect(anInterfaceID, i,
							HDFConstants.AN_FILE_LABEL);
					final int annLength = HDFLibrary.ANannlen(annID);
					String annBuf[] = new String[] { "" };
					assertTrue(HDFLibrary.ANreadann(annID, annBuf, annLength));
					short tagRef[] = new short[] { -1, -1 };
					assertTrue(HDFLibrary.ANid2tagref(annID, tagRef));
					System.out.println("Annotation index = " + i + " ; ID = "
							+ annID + " ; TAG = " + tagRef[0] + " ; REF = "
							+ tagRef[1] + "\n" + "Annotation content text = "
							+ annBuf[0]);
					HDFLibrary.ANendaccess(annID);
				}
			}
			// //
			// Retrieving file description annotations
			// //
			if (annotationsInfo[1] != 0) {
				final int fileDescriptions = annotationsInfo[1];
				System.out.println("\n=================================");
				System.out.print("file description annotations info: ");
				System.out.println("Quantity = " + fileDescriptions);
				for (int i = 0; i < fileDescriptions; i++) {
					final int annID = HDFLibrary.ANselect(anInterfaceID, i,
							HDFConstants.AN_FILE_DESC);
					final int annLength = HDFLibrary.ANannlen(annID);
					String annBuf[] = new String[] { "" };
					assertTrue(HDFLibrary.ANreadann(annID, annBuf, annLength));
					short tagRef[] = new short[] { -1, -1 };
					assertTrue(HDFLibrary.ANid2tagref(annID, tagRef));
					System.out.println("Annotation index = " + i + " ; ID = "
							+ annID + " ; TAG = " + tagRef[0] + " ; REF = "
							+ tagRef[1] + "\n" + "Annotation content text = "
							+ annBuf[0]);
					HDFLibrary.ANendaccess(annID);
				}
			}
			// //
			// Retrieving data object label annotations
			// //
			if (annotationsInfo[2] != 0) {
				final int dataObjectLabels = annotationsInfo[2];
				System.out.println("\n==================================");
				System.out.print("data object label annotations info: ");
				System.out.println("Quantity = " + dataObjectLabels);
				for (int i = 0; i < dataObjectLabels; i++) {
					final int annID = HDFLibrary.ANselect(anInterfaceID, i,
							HDFConstants.AN_DATA_LABEL);
					final int annLength = HDFLibrary.ANannlen(annID);
					String annBuf[] = new String[] { "" };
					assertTrue(HDFLibrary.ANreadann(annID, annBuf, annLength));
					short tagRef[] = new short[] { -1, -1 };
					assertTrue(HDFLibrary.ANid2tagref(annID, tagRef));
					System.out.println("Annotation index = " + i + " ; ID = "
							+ annID + " ; TAG = " + tagRef[0] + " ; REF = "
							+ tagRef[1] + "\n" + "Annotation content text = "
							+ annBuf[0]);
					HDFLibrary.ANendaccess(annID);
				}
			}

			// //
			// Retrieving data object description annotations
			// //
			if (annotationsInfo[3] != 0) {
				final int dataObjectDescriptions = annotationsInfo[3];
				System.out
						.println("\n========================================");
				System.out.print("data object description annotations info: ");
				System.out.println("Quantity = " + dataObjectDescriptions);
				for (int i = 0; i < dataObjectDescriptions; i++) {
					final int annID = HDFLibrary.ANselect(anInterfaceID, i,
							HDFConstants.AN_DATA_DESC);
					final int annLength = HDFLibrary.ANannlen(annID);
					String annBuf[] = new String[] { "" };
					assertTrue(HDFLibrary.ANreadann(annID, annBuf, annLength));
					short tagRef[] = new short[] { -1, -1 };
					assertTrue(HDFLibrary.ANid2tagref(annID, tagRef));
					System.out.println("Annotation index = " + i + " ; ID = "
							+ annID + " ; TAG = " + tagRef[0] + " ; REF = "
							+ tagRef[1] + "\n" + "Annotation content text = "
							+ annBuf[0]);
					HDFLibrary.ANendaccess(annID);
				}
			}

			System.out
					.println("\n=========================\nAnnotation related to SDS");
			System.out
					.println("=========================\nRetrieving from obsolete TAGS: DFTAG_SDG = 700");
			final short obsoleteTag = (short) HDFConstants.DFTAG_SDG;
			for (int j = 0; j < 10000; j++) {

				final int numTag = HDFLibrary.ANnumann(anInterfaceID,
						HDFConstants.AN_DATA_DESC, obsoleteTag, (short) j);
				if (numTag > 0) {
					final int annIDs[] = new int[numTag];
					HDFLibrary.ANannlist(anInterfaceID,
							HDFConstants.AN_DATA_DESC, obsoleteTag, (short) j,
							annIDs);
					for (int k = 0; k < numTag; k++) {
						System.out.println("Found Annotation with identifier "
								+ annIDs[k] + " related to Object with TAG = "
								+ obsoleteTag + " and REF = " + j);
					}
				}
			}
			System.out
					.println("\n=========================\nRetrieving from new TAGS: DFTAG_NDG = 720");
			final short newTag = (short) HDFConstants.DFTAG_NDG;
			for (int j = 0; j < 10000; j++) {

				final int numTag = HDFLibrary.ANnumann(anInterfaceID,
						HDFConstants.AN_DATA_DESC, newTag, (short) j);
				if (numTag > 0) {
					final int annIDs[] = new int[numTag];
					HDFLibrary.ANannlist(anInterfaceID,
							HDFConstants.AN_DATA_DESC, newTag, (short) j,
							annIDs);
					for (int k = 0; k < numTag; k++) {
						System.out.println("Found Annotation with identifier "
								+ annIDs[k] + " related to Object with TAG = "
								+ newTag + " and REF = " + j);
					}
				}
			}
			System.out.println("\n=========================\n");
			if (anInterfaceID != HDFConstants.FAIL) {
				HDFLibrary.ANend(anInterfaceID);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (fileID != HDFConstants.FAIL)
					HDFLibrary.Hclose(fileID);
			} catch (Exception e) {
			}
		}

	}
}
