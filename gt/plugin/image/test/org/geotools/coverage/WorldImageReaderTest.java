/*
 * Created on Jul 16, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.coverage;

import java.io.File;
import java.io.IOException;

import org.geotools.coverage.TestCaseSupport;

import org.geotools.gc.GridCoverage;
import org.geotools.gui.swing.FrameFactory;

import org.geotools.coverage.WorldImageReader;
import org.geotools.coverage.WorldImageWriter;

/**
 * @author rgould
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class WorldImageReaderTest extends TestCaseSupport {

	WorldImageReader wiReader;
	
	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		wiReader = new WorldImageReader(new File("c:\\etopo.png"));

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
	
    public static void main(String[] args) throws IOException{
    	WorldImageReader reader = new WorldImageReader(new File("c:\\etopo.png"));
		GridCoverage gc = reader.read(null);
        FrameFactory.show(gc);
        
        //Now re-write it!
        WorldImageWriter writer = new WorldImageWriter(new File("c:\\temp.png"));
        writer.write(gc, null);
    }

}
