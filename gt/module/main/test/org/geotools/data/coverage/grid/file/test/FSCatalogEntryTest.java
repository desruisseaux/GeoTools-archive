/*
 * Created on Jun 28, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.data.coverage.grid.file.test;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Iterator;

import junit.framework.TestCase;

import org.geotools.data.coverage.grid.GridFormatFinder;
import org.geotools.data.coverage.grid.file.FSCatalogEntry;
import org.geotools.data.coverage.grid.file.FileMetadata;
import org.geotools.resources.TestData;



/**
 * @author jeichar
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class FSCatalogEntryTest extends TestCase {

	URL resource;
	File f;
	FSCatalogEntry entry;
	
	/**
	 * @param name
	 */
	public FSCatalogEntryTest(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	void init()throws Exception{
		resource = TestData.getResource(this,"ArcGrid.asc");
		f=new File(new URI(URLDecoder.decode(resource.toString(),"UTF-8")));
		entry=new FSCatalogEntry(f,GridFormatFinder.getFormatArray());
	}
	
	public void testFSCatalogEntry()throws Exception {
		resource = TestData.getResource(this,"ArcGrid.asc");
		f=new File(URLDecoder.decode(resource.toString(),"UTF-8"));	
		entry=new FSCatalogEntry(f, GridFormatFinder.getFormatArray());
		assertNotNull(entry);
	}

	public void testGetDataName() throws Exception{
		init();
		
		assertTrue(entry.getDataName().endsWith("ArcGrid.asc"));
	}

	public void testGetMetadataNames()throws Exception {
		init();
		
		assertTrue(entry.getMetadataNames()[0].endsWith("ArcGrid.asc"));
		assertEquals(1, entry.getMetadataNames().length);
	}

	/*
	 * Class under test for Metadata getMetadata(String)
	 */
	public void testGetMetadataString()throws Exception {
		init();
		assertNotNull(f);
		assertNotNull(entry.getMetadata(f.toString()));
	}

	public void testGetResource() throws Exception{
		init();
		assertEquals(File.class, entry.getResource().getClass());
		assertEquals(f, entry.getResource());
	}

	public void testIterator() throws Exception{
		init();
		Iterator iter=entry.iterator();
		assertNotNull(iter);
		assertTrue(iter.hasNext());
		Object m=iter.next();
		assertNotNull(m);
		assertTrue(m instanceof FileMetadata);
	}

	public void testGetNumMetadata() throws Exception{
		init();
		assertEquals(1,entry.getNumMetadata());
	}

	/*
	 * Class under test for Metadata getMetadata(int)
	 */
	public void testGetMetadataint() throws Exception{
		init();
		assertNotNull(entry.getMetadata(0));
	}

}
