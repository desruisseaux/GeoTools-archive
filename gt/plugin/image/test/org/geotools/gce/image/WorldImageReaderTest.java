/*
 * Created on Jul 16, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.gce.image;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

import org.geotools.gc.GridCoverage;
import org.geotools.resources.TestData;

/**
 * @author rgould
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class WorldImageReaderTest extends TestCase {

	WorldImageReader wiReader;
	
	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		File imageFile = TestData.file(this, "etopo.png");
		wiReader = new WorldImageReader(imageFile);
	}

	/**
	 * Constructor for WorldImageReaderTest.
	 * @param arg0
	 */
	public WorldImageReaderTest(String arg0) {
		super(arg0);
	}

	public void testWorldImageReader() {
	}

	public void testRead() throws IOException {
		GridCoverage coverage = wiReader.read(null);
		assertNotNull(coverage);
		assertNotNull(coverage.getRenderedImage());
		assertNotNull(coverage.getEnvelope());
	}
}
