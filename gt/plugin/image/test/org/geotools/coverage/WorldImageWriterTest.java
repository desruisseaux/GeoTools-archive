/*
 * Created on Jul 20, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package test.org.geotools.coverage;

import java.io.File;

import net.refractions.udig.TestCaseSupport;

import src.org.geotools.coverage.WorldImageWriter;

import junit.framework.TestCase;

/**
 * @author rgould
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class WorldImageWriterTest extends TestCaseSupport {


	WorldImageWriter writer;
	Object destination;
	
	public WorldImageWriterTest(String name) {
		super(name);
	}
	
	protected void setUp() throws Exception {
		super.setUp();
		destination = new File(getTestResource("etoto.png").toExternalForm());
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
