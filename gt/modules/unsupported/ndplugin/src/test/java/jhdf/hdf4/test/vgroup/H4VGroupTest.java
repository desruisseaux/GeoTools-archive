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
package jhdf.hdf4.test.vgroup;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import ncsa.hdf.hdflib.HDFConstants;
import ncsa.hdf.hdflib.HDFException;
import ncsa.hdf.hdflib.HDFLibrary;

public class H4VGroupTest extends TestCase {

	private final static String SUBGROUPS_TESTFILEPATH = "E:/work/data/hdf/MISR_AM1_CGLS_WIN_2005_F04_0017.hdf";
	
	private String testFilePath;

	private final static boolean PRINT_ANY_VGROUP = false;

	protected void setUp() throws Exception {
		super.setUp();
		testFilePath = SUBGROUPS_TESTFILEPATH;
	}

	public H4VGroupTest(String name) {
		super(name);
	}

	public static void main(String[] args) {
		TestRunner.run(H4VGroupTest.class);
	}

	public static Test suite() {
		TestSuite suite = new TestSuite();
		suite.addTest(new H4VGroupTest("testGetInfo"));
		return suite;
	}

	public void testGetInfo() {
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
			assertTrue(HDFLibrary.Vstart(fileID));

			// Get and print the names and class names of all the lone vgroups.
			// First, call Vlone with bufferSize set to 0 to get the number of
			// lone vgroups in the file, but not to get their reference numbers.

			final int lonesNumber = HDFLibrary.Vlone(fileID, null, 0);
			System.out.println("This dataset contains " + lonesNumber
					+ " lone vgroups");
			System.out.println("");
			System.out.println("lone VGroups scan");
			if (lonesNumber > 0) {
				// //
				// Get their reference numbers.
				// //
				final int[] referencesArray = new int[lonesNumber];
				HDFLibrary.Vlone(fileID, referencesArray, lonesNumber);
				for (int loneVgroupIndex = 0; loneVgroupIndex < lonesNumber; loneVgroupIndex++) {
					// Attach to the current vgroup then get and display its
					// name and class. Note: the current vgroup must be detached
					// before moving to the next.
					final int vgroupID = HDFLibrary.Vattach(fileID,
							referencesArray[loneVgroupIndex], "r");
					assertNotSame(vgroupID, HDFConstants.FAIL);
					final String[] vgroupClass = { "" };
					HDFLibrary.Vgetclass(vgroupID, vgroupClass);
					if (PRINT_ANY_VGROUP || isAVGroupClass(vgroupClass))
						dumpVGroup(vgroupID, fileID, 1);
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (fileID != HDFConstants.FAIL) {
					HDFLibrary.Vend(fileID);
					System.out.println("END Of lone VGroups scan");
					System.out
							.println("==============================================================");
					System.out.println("VGroups scan by ID");

					assertTrue(HDFLibrary.Vstart(fileID));
					int lev = 1;
					int refNum = -1;
					while ((refNum = HDFLibrary.Vgetid(fileID, refNum)) != HDFConstants.FAIL) {
						final int vgroupID = HDFLibrary.Vattach(fileID, refNum,
								"r");
						final String[] vGroupClass = { "" };
						HDFLibrary.Vgetclass(vgroupID, vGroupClass);
						if (PRINT_ANY_VGROUP || isAVGroupClass(vGroupClass))
							dumpVGroup(vgroupID, fileID, lev, false);
					}
				}
			} catch (Exception e) {
			}

			try {
				if (fileID != HDFConstants.FAIL)
					HDFLibrary.Hclose(fileID);
			} catch (Exception e) {
			}
		}
	}

	private boolean isAVGroupClass(String[] vgroupClass) {
		if (vgroupClass[0].equalsIgnoreCase(HDFConstants.HDF_ATTRIBUTE)
				|| vgroupClass[0].equalsIgnoreCase(HDFConstants.HDF_VARIABLE)
				|| vgroupClass[0].equalsIgnoreCase(HDFConstants.HDF_DIMENSION)
				|| vgroupClass[0].equalsIgnoreCase(HDFConstants.HDF_UDIMENSION)
				|| vgroupClass[0].equalsIgnoreCase(HDFConstants.DIM_VALS)
				|| vgroupClass[0].equalsIgnoreCase(HDFConstants.DIM_VALS01)
				|| vgroupClass[0].equalsIgnoreCase(HDFConstants.HDF_CHK_TBL)
				|| vgroupClass[0].equalsIgnoreCase(HDFConstants.HDF_CDF)
				|| vgroupClass[0].equalsIgnoreCase(HDFConstants.GR_NAME)
				|| vgroupClass[0].equalsIgnoreCase(HDFConstants.RI_NAME)
				|| vgroupClass[0].equalsIgnoreCase(HDFConstants.RIGATTRNAME)
				|| vgroupClass[0].equalsIgnoreCase(HDFConstants.RIGATTRCLASS))
			return false;
		else
			return true;
	}

	private void dumpVGroup(final int vgroupID, int fileID, int lev)
			throws HDFException {
		dumpVGroup(vgroupID, fileID, lev, true);
	}

	private void dumpVGroup(final int vgroupID, int fileID, int lev,
			boolean recursive) throws HDFException {
		System.out.println("");
		final String[] vgroupName = { "" };
		final String[] vgroupClass = { "" };

		// //
		// Getting the vgroup name and class
		// //
		HDFLibrary.Vgetname(vgroupID, vgroupName);
		HDFLibrary.Vgetclass(vgroupID, vgroupClass);
		final StringBuffer preamble = new StringBuffer();

		for (int l = 0; l < lev; l++)
			preamble.append("\t");
		System.out.println(preamble.toString() + "<=========Vgroup Start "
				+ (recursive ? ("(level " + lev + ")") : "") + "========>");
		System.out.println(preamble.toString() + "Vgroup name " + vgroupName[0]
				+ " and class " + vgroupClass[0]);
		System.out.println(preamble.toString() + "Vgroup reference "
				+ HDFLibrary.VQueryref(vgroupID));
		System.out.println(preamble.toString() + "Vgroup tag "
				+ HDFLibrary.VQuerytag(vgroupID));
		System.out.println(preamble.toString() + "Vgroup identifier "
				+ vgroupID);
		System.out.println(preamble.toString()
				+ "Number of SDS within the VGroup "
				+ HDFLibrary.Vnrefs(vgroupID, HDFConstants.DFTAG_NDG));

		// //
		//
		// Dump vgroup info
		//
		// //

		// Get the total number of tag/reference id pairs.
		final int npairs = HDFLibrary.Vntagrefs(vgroupID);
		assertNotSame(HDFConstants.FAIL, npairs);
		System.out.println(preamble.toString() + "Vgroup # objects " + npairs);

		// //
		//
		// Getting Attribute information
		// 
		// //

		// Get and display the number of attributes attached to this vgroup.
		final int attributesNum = HDFLibrary.Vnattrs(vgroupID);
		System.out.println(preamble.toString()
				+ "Vgroup "
				+ ((attributesNum > 0 ? ("# attributes" + attributesNum)
						: "has not attributes")));

		if (attributesNum > 0) {
			// //
			// Get and display the name and the number of values of each
			// attribute.
			// //

			final String[] attribName = new String[] { " " };
			final int[] attribInfo = { 0, 0, 0 };
			for (int ii = 0; ii < attributesNum; ii++) {
				attribName[0] = "";
				// get various info about this attribute
				assertTrue(HDFLibrary.Vattrinfo(vgroupID, ii, attribName,
						attribInfo));
				final String dataTypeString = HDFConstants
						.getType(attribInfo[0]);
				System.out.println("\tVgroup Attribute " + ii + " name "
						+ attribName[0] + "dataType=" + dataTypeString
						+ " number of values=" + attribInfo[1] + " size="
						+ attribInfo[2]);

				// TODO: Find a valid sample with Vgroups having attributes to
				// test this code.
				final int size = attribInfo[2];
				byte b[] = new byte[size];
				ByteBuffer bb = null;
				Buffer attributeBuffer = null;

				assertTrue(HDFLibrary.Vgetattr(vgroupID, ii, b));
				bb = ByteBuffer.wrap(b);
				bb.order(ByteOrder.nativeOrder());

				System.out.print("\tAttribute values\n\t");
				if (dataTypeString.equals(HDFConstants.INT16)) {
					attributeBuffer = bb.asShortBuffer();
					for (int kk = 0; kk < size;)
						System.out.print(((ShortBuffer) attributeBuffer)
								.get(kk++)
								+ ((kk % 10 == 0) ? "\n\t" : " "));
				} else if (dataTypeString.equals(HDFConstants.INT32)) {
					attributeBuffer = bb.asIntBuffer();
					for (int kk = 0; kk < size;)
						System.out.print(((IntBuffer) attributeBuffer)
								.get(kk++)
								+ ((kk % 10 == 0) ? "\n\t" : " "));
				} else if (dataTypeString.equals(HDFConstants.INT64)) {
					attributeBuffer = bb.asLongBuffer();
					for (int kk = 0; kk < size;)
						System.out.print(((LongBuffer) attributeBuffer)
								.get(kk++)
								+ ((+kk % 10 == 0) ? "\n\t" : " "));
				} else if (dataTypeString.equals(HDFConstants.FLOAT32)) {
					attributeBuffer = bb.asFloatBuffer();
					for (int kk = 0; kk < size;)
						System.out.print(((FloatBuffer) attributeBuffer)
								.get(kk++)
								+ ((kk % 10 == 0) ? "\n\t" : " "));
				} else if (dataTypeString.equals(HDFConstants.FLOAT64)) {
					attributeBuffer = bb.asDoubleBuffer();
					for (int kk = 0; kk < size;)
						System.out.print(((DoubleBuffer) attributeBuffer)
								.get(kk++)
								+ ((kk % 10 == 0) ? "\n\t" : " "));
				} else
					attributeBuffer = null;
				System.out.print("\n");
			}
		}

		// //
		//
		// Getting TAG information
		// 
		// //

		// Print every tag and reference id with their corresponding position
		// within the group.
		for (int i = 0; i < npairs; i++) {
			final int tagRef[] = { 0, 0 };
			HDFLibrary.Vgettagref(vgroupID, i, tagRef);
			System.out.print(preamble.toString() + "Found tag = " + tagRef[0]
					+ ", ref = " + tagRef[1] + " at position " + i);

			final boolean isVgroup = HDFLibrary.Visvg(vgroupID, tagRef[1]);
			final boolean isVData = HDFLibrary.Visvs(vgroupID, tagRef[1]);
			if (isVData)
				System.out.println("--> Referred object is a Vdata");
			else if (isVgroup) {
				System.out.println("--> Referred object is a VGroup");
				final int subID = HDFLibrary.Vattach(fileID, tagRef[1], "r");
				assertNotSame(subID, HDFConstants.FAIL);
				final String[] vsubgroupClass = { "" };
				HDFLibrary.Vgetclass(subID, vsubgroupClass);
				if (PRINT_ANY_VGROUP
						|| (isAVGroupClass(vsubgroupClass) && recursive))
					dumpVGroup(subID, fileID, lev + 1);
			} else if (tagRef[0] == HDFConstants.DFTAG_NDG) {
				System.out.println("--> Referred object is a SDS");

			} else
				System.out
						.println("--> Referred object is neither a VGroup nor a Vdata");
		}

		System.out.println(preamble.toString() + "<=========Vgroup End "
				+ (recursive ? ("(level " + lev + ")") : "") + "========>\n");
		HDFLibrary.Vdetach(vgroupID);
	}
}
