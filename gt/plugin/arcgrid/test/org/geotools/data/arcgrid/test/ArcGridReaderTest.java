/*
 * Created on Apr 23, 2004
 */
package org.geotools.data.arcgrid.test;

import java.io.InputStream;

import org.geotools.data.arcgrid.ArcGridReader;


/**
 * 
 * 
 * @author jeichar
 */
public class ArcGridReaderTest extends TestCaseSupport {

	ArcGridReader reader;
	String TESTFILE="ArcGrid.asc";
	InputStream in;

	/**
	 * @param name
	 */
	public ArcGridReaderTest(String name) {
		super(name);
	}
	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		in=getTestResourceAsStream(TESTFILE);
		reader=new ArcGridReader(in);
	}
	/*
	 * @see TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}
	public void testGetSource() {
		assertEquals(in,reader.getSource());
	}
	/*
	 * Class to test for GridCoverage read( Parameter[])
	 */
	public void testReadStringParameterArray() throws Exception{
			assertNotNull(reader.read(null));		
	}

}
