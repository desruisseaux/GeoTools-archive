package org.geotools.data.coverage.grid.file.test;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.List;

import junit.framework.TestCase;

import org.geotools.data.coverage.grid.GridFormatFinder;
import org.geotools.data.coverage.grid.file.FileMetadataImpl;
import org.geotools.resources.TestData;

/**
 * TODO type description
 * 
 * @author jeichar
 *
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
    
    protected void setUp() throws Exception{
        super.setUp();
        resource = TestData.getResource(this,"ArcGrid.asc");
        assertNotNull(resource);
        f = TestData.file(this, "ArcGrid.asc");
        uri = f.toURI();
        assertTrue(f.exists());
    }

    private void init(){
        assertNotNull(f);
        metadata=new FileMetadataImpl( f,  GridFormatFinder.findFormat(f));    	
        assertNotNull(metadata);    }
    
    public void testFileMetadataImpl() {
        assertNotNull(f);
        assertNotNull(new FileMetadataImpl( f, GridFormatFinder.findFormat(f) ));
    }

    public void testGetName() {
    	init();

//      test begins
        assertEquals(metadata.getName(), f.getName());
        
    }

    public void testGetExtension() {
    	init();
//      test begins
   	    assertEquals(metadata.getExtension(), "asc");
    }

    public void testGetFile() {
    	init();
//      test begins
        assertEquals(metadata.getFile(), f);
    }

    public void testGetLastModified() {
    	init();

//      test begins
        assertEquals(metadata.getLastModified(), f.lastModified());
    }

    public void testGetPath() {
    	init();

//      test begins
        assertEquals(metadata.getPath(), f.getPath());
    }
    
    /*
     * Class under test for Object getElement(String)
     */
    public void testGetElementString() {
    
        StupidNestedMetadataImpl data=new StupidNestedMetadataImpl();
        String element=(String)data.getFileData().getName();
        assertEquals("Stupid",element);
    }

 }
