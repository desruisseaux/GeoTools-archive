/*
 * Created on Apr 26, 2004
 */
package org.geotools.data.coverage.grid.stream.test;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;

import org.geotools.data.arcgrid.test.TestCaseSupport;
import org.geotools.data.coverage.grid.stream.IOExchange;
/**
 * @author jeichar
 */
public class IOExchangeTest extends TestCaseSupport {
	final String TEST_FILE = "ArcGrid.asc";
	IOExchange mExchange;
	/**
	 * @param name
	 */
	public IOExchangeTest(String name) {
		super(name);
	}
	/**
	 * Test that getIOExchange() returns an object
	 *  
	 */
	public void testGetIOExchange() {
		mExchange = IOExchange.getIOExchange();
		assertNotNull(mExchange);
	}
	/**
	 * Test that various types of source objects return a reader
	 *  
	 */
	public void testGetReader() throws IOException {
		mExchange = IOExchange.getIOExchange();
		Reader reader;
		
		// Test URL
		URL fileURL = getTestResource(TEST_FILE);
		reader=mExchange.getReader(fileURL);
		assertNotNull(reader);
		assertTrue(reader.ready());
		int char1=reader.read();
		
		//Test String
		String fileString=fileURL.toString();
		reader=mExchange.getReader(fileString);
		assertNotNull(reader);
		assertTrue(reader.ready());
		assertEquals(char1,reader.read());
		
		//Test File
		File file=new File(fileURL.getFile());
		reader=mExchange.getReader(file);
		assertNotNull(reader);
		assertTrue(reader.ready());
		assertEquals(char1,reader.read());
		
		//Test InputStream
		InputStream in=fileURL.openStream();
		reader=mExchange.getReader(in);
		assertNotNull(reader);
		assertTrue(reader.ready());
		assertEquals(char1,reader.read());

		//Test Reader
		reader=mExchange.getReader(mExchange.getReader(fileURL));
		assertNotNull(reader);
		assertTrue(reader.ready());
		assertEquals(char1,reader.read());
		
		
		
		
	}
}