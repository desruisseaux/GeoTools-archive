/*
 * Created on Jul 20, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.gce.image;

import java.io.File;
import junit.framework.TestCase;
import org.geotools.resources.TestData;

/**
 * @author rgould
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class WorldImageWriterTest extends TestCase {


	WorldImageWriter writer;
	Object destination;
	
	public WorldImageWriterTest(String name) {
		super(name);
	}
	
	protected void setUp() throws Exception {
		super.setUp();
		destination = TestData.file(this, "etopo.png");
		writer = new WorldImageWriter ((File) destination);
	}

	public void testGetFormat() {
		assertNotNull(writer.getFormat());
	}

	public void testGetDestination() {
		assertEquals(destination, writer.getDestination());
	}

	public void testWrite() {
	}

}
