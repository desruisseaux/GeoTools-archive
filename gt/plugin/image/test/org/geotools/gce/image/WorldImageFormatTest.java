/*
 * Created on Jul 20, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.gce.image;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import junit.framework.TestCase;


/**
 * @author rgould
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class WorldImageFormatTest extends TestCase {

	private WorldImageFormat format;


	public WorldImageFormatTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		format = new WorldImageFormat();
	}

	public void testGetReader() throws MalformedURLException {
		assertNotNull(format.getReader(new URL("http://something.com/temp.tiff")));
	}

	public void testGetWriter() throws MalformedURLException {
		assertNotNull(format.getWriter(new URL("http://something.com/temp.tiff")));
	}

	public void testAccepts() throws MalformedURLException {
		assertTrue(format.accepts(new File("c:\temp.gif")));
		assertTrue(format.accepts(new URL("http://something.com/temp.jpeg")));
	}

	public void testGetWorldExtension() {
		assertEquals(WorldImageFormat.getWorldExtension(".png"), ".pgw");
		assertEquals(WorldImageFormat.getWorldExtension(".gif"), ".gfw");
		assertEquals(WorldImageFormat.getWorldExtension(".jpg"), ".jgw");
		assertEquals(WorldImageFormat.getWorldExtension(".jpeg"), ".jgw");
		assertEquals(WorldImageFormat.getWorldExtension(".tif"), ".tfw");
		assertEquals(WorldImageFormat.getWorldExtension(".tiff"), ".tfw");
		
	}

}
