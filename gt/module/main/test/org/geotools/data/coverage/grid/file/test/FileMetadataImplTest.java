package org.geotools.data.coverage.grid.file.test;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

import org.geotools.data.coverage.grid.GridFormatFinder;
import org.geotools.data.coverage.grid.file.FileMetadata;
import org.geotools.data.coverage.grid.file.FileMetadataImpl;
import org.geotools.metadata.Metadata;
import org.geotools.metadata.Metadata.Entity;
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
    
    protected void setUp() throws Exception{
        super.setUp();
        resource = TestData.getResource(this,"ArcGrid.asc");
        assertNotNull(resource);
    }

    private void init(){
        f=new File(resource.getFile());
        assertNotNull(f);
        metadata=new FileMetadataImpl( f,  GridFormatFinder.findFormat(f));    	
        assertNotNull(metadata);    }
    
    public void testFileMetadataImpl() {
        File f=new File(resource.getFile());
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

    public void testGetFormat() {
    	init();
//      test begins
        assertEquals(metadata.getFormat(),GridFormatFinder.findFormat(f));
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
        List ret=metadata.getElements(null);
        
        assertNotNull(ret);
        assertEquals(ret.size(),FileMetadata.class.getDeclaredMethods().length);
   }

    /*
     * calls getElements(new ArrayList())
     */
    public void testGetElementsPassedObjectArray() {
    	init();


        // test begins
        ArrayList param=new ArrayList();
        List ret=metadata.getElements(param);
        
        assertEquals(param,ret);
        
        // ensure that it can withstand a second call
        param=new ArrayList();
        ret=metadata.getElements(param);
        
        assertEquals(param,ret);
   }

    public void testGetEntity() {
    	init();
        // test begins
        assertNotNull(metadata.getEntity());
    }

    /*
     * Class under test for Object getElement(ElementType)
     */
    public void testGetElementElementType() {
    	init();

        // test begins
        Entity entity=metadata.getEntity();
        List elements=entity.getElements();
        List values=metadata.getElements(null);
        int i=0;
        for (Iterator iter = elements.iterator(); iter.hasNext();i++) {
            Metadata.Element element = (Metadata.Element) iter.next();
            assertEquals(metadata.getElement(element), values.get(i));
        }
    }

    /*
     * Class under test for Object getElement(String)
     */
    public void testGetElementString() {

        StupidNestedMetadataImpl data=new StupidNestedMetadataImpl();
        String element=(String)data.getElement("FileData/Name");
        assertEquals("Stupid",element);
        
        //Test xpath with wildcards
        List list=(List) data.getElement("FileData/\\w*");
        assertEquals(3,list.size());

        String name=(String) data.getElement("\\w*/Name");
        assertEquals("Stupid",name);

        //Test xpath with wildcards
        list=(List) data.getElement("\\w*/\\w*");
        assertEquals(3,list.size());

    }

 }
