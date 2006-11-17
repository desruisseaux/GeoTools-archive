/**
 * 
 */
package org.geotools.gce.arcgrid;

import java.io.File;
import java.io.FileFilter;
import java.io.PrintWriter;
import java.io.StringWriter;

import junit.framework.TestCase;

import org.geotools.resources.TestData;

/**
 * @author Giannecchini
 * 
 * @source $URL$
 */
public abstract class ArcGridBaseTestCase extends TestCase {

	protected File[] testFiles;

	/**
	 * 
	 */
	public ArcGridBaseTestCase() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param arg0
	 */
	public ArcGridBaseTestCase(String arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
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
		final StringBuffer errors = new StringBuffer();
		final int length = testFiles.length;
		for (int i = 0; i < length; i++) {
			try {
				test(testFiles[i]);
			} catch (Exception e) {
//				e.printStackTrace();
				final StringWriter writer= new StringWriter();
				e.printStackTrace(new PrintWriter(writer));
				errors.append("\nFile ").append(testFiles[i].getAbsolutePath())
						.append(" :\n").append(e.getMessage()).append("\n")
						.append(writer.toString());

			}
		}

		if (errors.length() > 0) {
			fail(errors.toString());
		}
	}

	public abstract void test(File file) throws Exception;

}
