/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004-2006, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.data.coverage.grid.file.test;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.List;

import junit.framework.TestCase;

import org.geotools.data.coverage.grid.GridFormatFinder;
import org.geotools.data.coverage.grid.file.FileMetadataImpl;
import org.geotools.TestData;

/**
 * TODO type description
 * 
 * @author jeichar
 *
 * @source $URL$
 */
public class FileMetadataImplTest extends TestCase {

    /**
     * @param name
     */
    public FileMetadataImplTest(String name) {
        super(name);
     }

    URL resource;
    File f;
    FileMetadataImpl metadata;
    URI uri;
    
//    protected void setUp() throws Exception{
//        super.setUp();
//        resource = TestData.url("arcgrid/ArcGrid.asc");
//        assertNotNull(resource);
//        f = TestData.copy(this, "arcgrid/ArcGrid.asc");
//        uri = f.toURI();
//        assertTrue(f.exists());
//    }

    private void init(){
        assertNotNull(f);
        metadata=new FileMetadataImpl( f,  GridFormatFinder.findFormat(f));    	
        assertNotNull(metadata);    }

	public void testNone() {
		//TODO: fix tests
	}
    
//    public void testFileMetadataImpl() {
//        assertNotNull(f);
//        assertNotNull(new FileMetadataImpl( f, GridFormatFinder.findFormat(f) ));
//    }
//
//    public void testGetName() {
//    	init();
//
////      test begins
//        assertEquals(metadata.getName(), f.getName());
//        
//    }
//
//    public void testGetExtension() {
//    	init();
////      test begins
//   	    assertEquals(metadata.getExtension(), "asc");
//    }
//
//    public void testGetFile() {
//    	init();
////      test begins
//        assertEquals(metadata.getFile(), f);
//    }
//
//    public void testGetLastModified() {
//    	init();
//
////      test begins
//        assertEquals(metadata.getLastModified(), f.lastModified());
//    }
//
//    public void testGetPath() {
//    	init();
//
////      test begins
//        assertEquals(metadata.getPath(), f.getPath());
//    }
//    
//    /*
//     * Class under test for Object getElement(String)
//     */
//    public void testGetElementString() {
//    
//        StupidNestedMetadataImpl data=new StupidNestedMetadataImpl();
//        String element=(String)data.getFileData().getName();
//        assertEquals("Stupid",element);
//    }

 }
