/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2005, Geotools Project Managment Committee (PMC)
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
package org.geotools.data.shapefile.indexed;

import java.io.IOException;
import java.nio.channels.FileChannel;

import org.geotools.data.shapefile.indexed.FidIndexer;
import org.geotools.data.shapefile.indexed.IndexedFidReader;
import org.geotools.data.shapefile.indexed.IndexedFidWriter;
import org.geotools.data.shapefile.shp.IndexFile;

public class IndexedFidReaderTest extends FIDTestCase {
	private IndexedFidReader reader;

	private IndexFile indexFile;

	protected void setUp() throws Exception {
		super.setUp();

		FidIndexer.generate(backshp.toURL());

		indexFile = new IndexFile(FidIndexer.getReadChannel(backshx.toURL()));
		reader = new IndexedFidReader(TYPE_NAME, FidIndexer
				.getReadChannel(fixFile.toURL()));
	}

	protected void tearDown() throws Exception {
		reader.close();
		indexFile.close();
		super.tearDown();
	}

	/*
	 * Test method for 'org.geotools.index.fid.IndexedFidReader.findFid(String)'
	 */
	public void testFindFid() throws Exception {
		long offset = reader.findFid("roads.4");
		assertEquals(3, offset);
		
		offset = reader.findFid("roads.1");
		assertEquals(0, offset);

		// test if the fid is too high
		offset = reader.findFid("roads.10000000");
		assertEquals(-1, offset);

		// test if the fid is negative
		offset = reader.findFid("roads.-1");
		assertEquals(-1, offset);

	}

	// test if FID no longer exists.
	public void testFindDeletedFID() throws Exception {
		reader.close();
		
		FileChannel channel = FidIndexer.getWriteChannel(fixFile.toURL());
		IndexedFidWriter writer = new IndexedFidWriter(channel,
				new IndexedFidReader(TYPE_NAME, channel));
		try {
			writer.next();
			writer.next();
			writer.next();
			writer.remove();
			while (writer.hasNext()) {
				writer.next();
			}
		} finally {
			writer.close();
			reader.close();
		}


		reader = new IndexedFidReader(TYPE_NAME, FidIndexer
				.getReadChannel(fixFile.toURL()));
		
		long offset = reader.findFid(TYPE_NAME+".11");
		assertEquals(9, offset);
		
		offset = reader.findFid(TYPE_NAME+".4");
		assertEquals(2, offset);

		offset = reader.findFid(TYPE_NAME+".3");
		assertEquals(-1, offset);

	}

	public void testHardToFindFid() throws Exception {
		long offset=reader.search(5,3,7,5);
		assertEquals(4, offset);
	}
	
	/*
	 * Test method for 'org.geotools.index.fid.IndexedFidReader.goTo(int)'
	 */
	public void testGoTo() throws IOException {
		reader.goTo(10);
		assertEquals(TYPE_NAME+".11", reader.next());
		assertTrue(reader.hasNext());
		
		reader.goTo(15);
		assertEquals(TYPE_NAME+".16", reader.next());
		assertTrue(reader.hasNext());
		

		reader.goTo(0);
		assertEquals(TYPE_NAME+".1", reader.next());
		assertTrue(reader.hasNext());
		
		reader.goTo(3);
		assertEquals(TYPE_NAME+".4", reader.next());
		assertTrue(reader.hasNext());
	}
}
