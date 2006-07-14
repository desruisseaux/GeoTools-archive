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

import java.io.File;
import java.io.FileFilter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Logger;

import junit.framework.TestCase;

import org.geotools.resources.TestData;

/**
 * @author Giannecchini
 * 
 * @source $URL:
 *         http://svn.geotools.org/geotools/branches/coverages_branch/trunk/gt/plugin/arcgrid/test/org/geotools/gce/arcgrid/ArcGridBaseTestCase.java $
 */
public abstract class ArcGridBaseTestCase extends TestCase {

	protected final static Logger LOGGER = Logger
			.getLogger(ArcGridBaseTestCase.class.toString());

	protected File[] testFiles;

	final boolean skipTest;

	/**
	 * 
	 */
	public ArcGridBaseTestCase() {
		super();
		skipTest = false;
	}

	public ArcGridBaseTestCase(String name) {

		super(name);
		skipTest = false;
	}

	public ArcGridBaseTestCase(String name, boolean skipBaseTest) {
		super(name);
		skipTest = skipBaseTest;

	}

	protected void setUp() throws Exception {
		super.setUp();
		testFiles = TestData.file(this, ".").listFiles(new FileFilter() {

			public boolean accept(File pathname) {

				return new ArcGridFormat().accepts(pathname);
			}
		});
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testAll() throws Exception {
		if (!skipTest) {
			final StringBuffer errors = new StringBuffer();
			final int length = testFiles.length;
			for (int i = 0; i < length; i++) {
				LOGGER.info(testFiles[i].getAbsolutePath());
				try {
					test(testFiles[i]);

				} catch (Exception e) {
					// e.printStackTrace();
					final StringWriter writer = new StringWriter();
					e.printStackTrace(new PrintWriter(writer));
					errors.append("\nFile ").append(
							testFiles[i].getAbsolutePath()).append(" :\n")
							.append(e.getMessage()).append("\n").append(
									writer.toString());
				}
			}

			if (errors.length() > 0) {
				fail(errors.toString());
			}
		}
	}

	public abstract void test(File file) throws Exception;
}
