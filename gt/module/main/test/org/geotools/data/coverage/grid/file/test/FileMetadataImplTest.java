package org.geotools.data.coverage.grid.file.test;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

import org.opengis.catalog.MetadataEntity;
import org.opengis.catalog.MetadataEntity.EntityType;

import org.geotools.data.coverage.grid.GridFormatFinder;
import org.geotools.data.coverage.grid.file.FileMetadata;
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
     * Calls getElements(null)
     */
    public void testGetElementsPassedNull() {
    	init();

        // test begins
        List ret=metadata.elements();
        
        assertNotNull(ret);
        assertEquals(ret.size(),FileMetadata.class.getDeclaredMethods().length);
   }

    public void testGetEntity() {
    	init();
        // test begins
        assertNotNull(metadata.getEntityType());
    }

    /*
     * Class under test for Object getElement(ElementType)
     */
    public void testGetElementElementType() {
    	init();

        // test begins
    	EntityType entity=metadata.getEntityType();
        List elements=entity.getElements();
        List values=metadata.elements();
        int i=0;
        for (Iterator iter = elements.iterator(); iter.hasNext();i++) {
            MetadataEntity.Element element = (MetadataEntity.Element) iter.next();
            assertEquals(metadata.getElement(element), values.get(i));
        }
    }

    /*
     * Class under test for Object getElement(String)
     */
    public void testGetElementString() {

        StupidNestedMetadataImpl data=new StupidNestedMetadataImpl();
        String element=(String)data.getElement("fileData/name");
        assertEquals("Stupid",element);
        
        //Test xpath with wildcards
        List list=(List) data.getElement("fileData/\\w*");
        assertEquals(3,list.size());

        String name=(String) data.getElement("\\w*/name");
        assertEquals("Stupid",name);

        //Test xpath with wildcards
        list=(List) data.getElement("\\w*/\\w*");
        assertEquals(3,list.size());

    }

 }
