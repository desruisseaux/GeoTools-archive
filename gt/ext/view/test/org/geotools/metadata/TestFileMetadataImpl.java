package org.geotools.metadata;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.geotools.data.arcgrid.ArcGridFormat;
import org.geotools.metadata.Metadata.Entity;

import junit.framework.TestCase;

/**
 * TODO type description
 * 
 * @author jeichar
 *
 */
public class TestFileMetadataImpl extends TestCase {

    URL resource;
    File f;
    FileMetadataImpl metadata;
    
    protected void setUp() throws Exception{
        super.setUp();
        resource = TestFileMetadataImpl.class.getResource("testdata/ArcGrid.asc");
        assertNotNull(resource);
    }

    private void init(){
        f=new File(resource.getFile());
        assertNotNull(f);
        metadata=new FileMetadataImpl( f, new ArcGridFormat() );    	
        assertNotNull(metadata);    }
    
    public void testFileMetadataImpl() {
        File f=new File(resource.getFile());
        assertNotNull(f);
        assertNotNull(new FileMetadataImpl( f, new ArcGridFormat() ));
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
        assertNotNull(metadata.getFormat());
        assertEquals( metadata.getFormat().getClass(), ArcGridFormat.class);
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

    public void testSetFormat() {
    	init();

//      test begins
        metadata.setFormat(new ArcGridFormat());
        assertEquals(metadata.getFormat().getClass(), ArcGridFormat.class);
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
        for(int i=0; i<ret.size(); i++)
            assertNotNull(ret.get(i));
        
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
        for(int i=0; i<ret.size(); i++)
            assertNotNull(ret.get(i));
        
        // ensure that it can withstand a second call
        param=new ArrayList();
        ret=metadata.getElements(param);
        
        assertEquals(param,ret);
        for(int i=0; i<ret.size(); i++)
            assertNotNull(ret.get(i));
        
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
        assertEquals("ArcGrid.asc",element);
        
        //Test xpath with wildcards
        List list=(List) data.getElement("FileData/\\w*");
        assertEquals(5,list.size());

        String name=(String) data.getElement("\\w*/Name");
        assertEquals("ArcGrid.asc",name);

        //Test xpath with wildcards
        list=(List) data.getElement("\\w*/\\w*");
        assertEquals(5,list.size());

    }

 }
