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
package jhdf.hdf4.test.file;

import ncsa.hdf.hdflib.HDFException;
import ncsa.hdf.hdflib.HDFLibrary;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

public class H4FileTest extends TestCase {

	public H4FileTest(String name) {
		super(name);
	}

	public static void main(String[] args) {
		TestRunner.run(H4FileTest.class);
	}

	public static Test suite() {
		TestSuite suite = new TestSuite();
		suite.addTest(new H4FileTest("testMultipleOpen"));
		return suite;
	}

	/**
	 * Note that the file identifier is different for different sessions on the
	 * same file!
	 * 
	 * <p>
	 * This means thata we cannot rely on it to spot two H4Files working on the
	 * same file, we got to rely on the file path which is obviously unique on
	 * the same machine.
	 * 
	 */
	public void testMultipleOpen() {
		int fileID1 = -1, fileID2 = -1;
		try {
			fileID1 = HDFLibrary
					.Hopen("E:/Work/data/HDF/TOVS_BROWSE_DAILY_AM_861031_NF.HDF");
			fileID2 = HDFLibrary
					.Hopen("E:/Work/data/HDF/TOVS_BROWSE_DAILY_AM_861031_NF.HDF");
			assertNotSame(fileID1, fileID2);
		} catch (HDFException e) {
			e.printStackTrace();
		} finally {
			if (fileID1 == -1)
				try {
					HDFLibrary.Hclose(fileID1);
				} catch (HDFException e) {
					e.printStackTrace();
				}
			if (fileID2 == -1)
				try {
					HDFLibrary.Hclose(fileID2);
				} catch (HDFException e) {
					e.printStackTrace();
				}
		}
	}
}
