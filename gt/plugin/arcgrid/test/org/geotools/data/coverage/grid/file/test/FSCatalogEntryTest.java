/*
 * Created on Jun 28, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.data.coverage.grid.file.test;

import java.io.File;
import java.net.URL;
import java.util.Iterator;

import org.geotools.data.arcgrid.test.TestCaseSupport;
import org.geotools.data.coverage.grid.file.FSCatalogEntry;
import org.geotools.data.coverage.grid.file.FileMetadata;



/**
 * @author jeichar
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class FSCatalogEntryTest extends TestCaseSupport {

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

	void init(){
		resource = getTestResource("ArcGrid.asc");
		f=new File(resource.getFile());
		entry=new FSCatalogEntry(f);
	}
	
	public void testFSCatalogEntry() {
		resource = getTestResource("ArcGrid.asc");
		f=new File(resource.getFile());	
		entry=new FSCatalogEntry(f);
		assertNotNull(entry);
	}

	public void testGetDataName() {
		init();
		
		assertTrue(entry.getDataName().endsWith("ArcGrid.asc"));
	}

	public void testGetMetadataNames() {
		init();
		
		assertTrue(entry.getMetadataNames()[0].endsWith("ArcGrid.asc"));
		assertEquals(1, entry.getMetadataNames().length);
	}

	/*
	 * Class under test for Metadata getMetadata(String)
	 */
	public void testGetMetadataString() {
		init();
		assertNotNull(entry.getMetadata(resource.getFile()));
	}

	public void testGetResource() {
		init();
		assertEquals(File.class, entry.getResource().getClass());
		assertEquals(resource.getFile(), ((File)entry.getResource()).getPath());
	}

	public void testIterator() {
		init();
		Iterator iter=entry.iterator();
		assertNotNull(iter);
		assertTrue(iter.hasNext());
		Object m=iter.next();
		assertNotNull(m);
		assertTrue(m instanceof FileMetadata);
	}

	public void testGetNumMetadata() {
		init();
		assertEquals(1,entry.getNumMetadata());
	}

	/*
	 * Class under test for Metadata getMetadata(int)
	 */
	public void testGetMetadataint() {
		init();
		assertNotNull(entry.getMetadata(0));
	}

}
