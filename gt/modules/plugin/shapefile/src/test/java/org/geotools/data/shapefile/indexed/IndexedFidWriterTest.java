/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, GeoTools Project Managment Committee (PMC)
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

import org.geotools.data.shapefile.indexed.FidIndexer;
import org.geotools.data.shapefile.indexed.IndexedFidReader;
import org.geotools.data.shapefile.indexed.IndexedFidWriter;
import org.geotools.data.shapefile.shp.IndexFile;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.channels.FileChannel;


public class IndexedFidWriterTest extends FIDTestCase {
    private IndexFile indexFile;
    private IndexedFidWriter writer;

    protected void setUp() throws Exception {
        super.setUp();
    }

    private void initWriter() throws IOException, MalformedURLException {
        close();
        indexFile = new IndexFile(FidIndexer.getReadChannel(backshx.toURL()));

        FileChannel writeChannel = FidIndexer.getWriteChannel(fixFile.toURL());
        writer = new IndexedFidWriter(writeChannel,
                new IndexedFidReader(TYPE_NAME, writeChannel));
    }

    protected void tearDown() throws Exception {
        close();
        super.tearDown();
    }

    private void close() throws IOException {
        if ((writer != null) && !writer.isClosed()) {
            writer.close();
        }

        try {
            if (indexFile != null) {
                indexFile.close();
            }
        } catch (Exception e) {
            // fine if already closed
        }
    }

    /*
     * Test method for 'org.geotools.index.fid.IndexedFidWriter.hasNext()'
     */
    public void testHasNext() throws MalformedURLException, IOException {
        FidIndexer.generate(backshp.toURL());
        initWriter();

        for (int i = 1, j = indexFile.getRecordCount(); i < j; i++) {
            assertTrue(i + "th record", writer.hasNext());
            assertEquals((long) i, writer.next());
        }
    }

    /*
     * Test method for 'org.geotools.index.fid.IndexedFidWriter.remove()'
     */
    public void testRemove() throws MalformedURLException, IOException {
        FidIndexer.generate(backshp.toURL());
        initWriter();
        writer.next();
        writer.remove();

        for (int i = 2, j = indexFile.getRecordCount(); i < j; i++) {
            assertTrue(writer.hasNext());
            assertEquals((long) i, writer.next());
        }

    	writer.write();
        close();

        
        initWriter();

        for (int i = 1, j = indexFile.getRecordCount()-1; i < j; i++) {
            assertTrue(writer.hasNext());
            assertEquals((long) i + 1, writer.next());
        }
    }

    public void testRemoveCounting() throws Exception {
    	FidIndexer.generate(backshp.toURL());
        initWriter();
        writer.next();
        writer.remove();
        writer.next();
        writer.remove();
        writer.next();
        writer.remove();

        while( writer.hasNext()) {
        	writer.next();
            writer.write();
        }

        close();
        IndexedFidReader reader = new IndexedFidReader(TYPE_NAME, FidIndexer
				.getReadChannel(fixFile.toURL()));
        try{
        	assertEquals(3, reader.getRemoves());
        }finally{
        	reader.close();
        }
        
        // remove some more features
        initWriter();
        writer.next();
        writer.next();
        writer.next();
        writer.remove();
        writer.next();
        writer.remove();
        writer.next();
        writer.next();
        writer.next();
        writer.remove();
        while( writer.hasNext()) {
        	writer.next();
            writer.write();
        }

        close();
        
        reader = new IndexedFidReader(TYPE_NAME, FidIndexer
				.getReadChannel(fixFile.toURL()));
        try{
        	assertEquals(6, reader.getRemoves());
        }finally{
        	reader.close();
        }
        
        
	}
    
    /*
     * Test method for 'org.geotools.index.fid.IndexedFidWriter.write()'
     */
    public void testWrite() throws IOException {
        initWriter();

        for (int i = 0; i < 5; i++) {
            writer.next();
            writer.write();
        }

        close();
        initWriter();

        for (int i = 1; i < 5; i++) {
            assertTrue(writer.hasNext());
            assertEquals((long) i, writer.next());
        }
    }

}
