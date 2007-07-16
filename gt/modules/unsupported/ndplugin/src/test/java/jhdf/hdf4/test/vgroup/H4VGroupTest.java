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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import ncsa.hdf.hdflib.HDFConstants;
import ncsa.hdf.hdflib.HDFException;
import ncsa.hdf.hdflib.HDFLibrary;

public class H4VGroupTest extends TestCase {

	private String testFilePath;

	protected void setUp() throws Exception {
		super.setUp();
		testFilePath = "d:\\work\\data\\hdf\\MISR_AM1_CGLS_WIN_2005_F04_0017.hdf";
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
		int fid = HDFConstants.FAIL;
		try {
			// //
			// Open the HDF file for reading.
			// //
			fid = HDFLibrary.Hopen(testFilePath, HDFConstants.DFACC_READ);
			assertNotSame(fid, HDFConstants.FAIL);

			// //
			// Initialize the V interface.
			// //
			assertTrue(HDFLibrary.Vstart(fid));

			// Get and print the names and class names of all the lone vgroups.
			// First, call Vlone with num_of_lones set to 0 to get the number of
			// lone vgroups in the file, but not to get their reference numbers.

			final int num_of_lones = HDFLibrary.Vlone(fid, null, 0);
			System.out.println("This dataset contains " + num_of_lones
					+ " vgroups");
			System.out.println("");
			System.out.println("");
			if (num_of_lones > 0) {
				// //
				// Get their reference numbers.
				// //
				final int[] ref_array = new int[num_of_lones];
				HDFLibrary.Vlone(fid, ref_array, num_of_lones);
				for (int lone_vg_number = 0; lone_vg_number < num_of_lones; lone_vg_number++) {
					// Attach to the current vgroup then get and display its
					// name and class. Note: the current vgroup must be detached
					// before moving to the next.
					final int vgroup_id = HDFLibrary.Vattach(fid,
							ref_array[lone_vg_number], "r");
					assertNotSame(vgroup_id, HDFConstants.FAIL);

					dumpVGroup(vgroup_id, fid, 1);
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {

			try {
				if (fid != HDFConstants.FAIL)
					HDFLibrary.Hclose(fid);
			} catch (Exception e) {
			}

			try {
				if (fid != HDFConstants.FAIL)
					HDFLibrary.Vend(fid);
			} catch (Exception e) {
			}

		}
	}

	private void dumpVGroup(final int vgroup_id, int fid, int lev)
			throws HDFException {
		System.out.println("");
		final String[] vgroup_name = { "" };
		final String[] vgroup_class = { "" };
		HDFLibrary.Vgetname(vgroup_id, vgroup_name);
		HDFLibrary.Vgetclass(vgroup_id, vgroup_class);
		final StringBuffer preamble = new StringBuffer();

		for (int l = 0; l < lev; l++)
			preamble.append("\t");
		System.out.println(preamble.toString()
				+ "<=========Vgroup Start========>");
		System.out.println(preamble.toString() + "Vgroup name "
				+ vgroup_name[0] + " and class " + vgroup_class[0]);
		// //
		//
		// Dump vgroup info
		//
		// //

		// Get the total number of tag/reference id pairs.
		final int npairs = HDFLibrary.Vntagrefs(vgroup_id);
		assertNotSame(HDFConstants.FAIL, npairs);
		System.out.println(preamble.toString() + "Vgroup # objects " + npairs);
		if (vgroup_class[0].equalsIgnoreCase(HDFConstants.HDF_ATTRIBUTE)
				|| vgroup_class[0].equalsIgnoreCase(HDFConstants.HDF_VARIABLE)
				|| vgroup_class[0].equalsIgnoreCase(HDFConstants.HDF_DIMENSION)
				|| vgroup_class[0]
						.equalsIgnoreCase(HDFConstants.HDF_UDIMENSION)
				|| vgroup_class[0].equalsIgnoreCase(HDFConstants.DIM_VALS)
				|| vgroup_class[0].equalsIgnoreCase(HDFConstants.DIM_VALS01)
				|| vgroup_class[0].equalsIgnoreCase(HDFConstants.HDF_CHK_TBL)
				|| vgroup_class[0].equalsIgnoreCase(HDFConstants.HDF_CDF)
				|| vgroup_class[0].equalsIgnoreCase(HDFConstants.GR_NAME)
				|| vgroup_class[0].equalsIgnoreCase(HDFConstants.RI_NAME)
				|| vgroup_class[0].equalsIgnoreCase(HDFConstants.RIGATTRNAME)
				|| vgroup_class[0].equalsIgnoreCase(HDFConstants.RIGATTRCLASS)) {
			System.out.println(preamble.toString() + "Standard HDF VGroup");

		}

		// Print every tag and reference id with their corresponding file
		// position.

		for (int i = 0; i < npairs; i++) {
			final int tagRef[] = { 0, 0 };
			HDFLibrary.Vgettagref(vgroup_id, i, tagRef);
			final boolean isVgroup = HDFLibrary.Visvg(vgroup_id, tagRef[1]);

			if (isVgroup) {
				final int subID = HDFLibrary.Vattach(fid, tagRef[1], "r");
				assertNotSame(vgroup_id, HDFConstants.FAIL);
				dumpVGroup(subID, fid, lev + 1);
			}

			System.out.println(preamble.toString() + "Found tag = " + tagRef[0]
					+ ", ref = " + tagRef[1] + " at position " + i);
			System.out.println(preamble.toString()
					+ "Referred object is a VGroup? " + isVgroup);
		}
		System.out.println(preamble.toString()
				+ "<=========Vgroup End========>" + "\n");
		HDFLibrary.Vdetach(vgroup_id);
	}
}
