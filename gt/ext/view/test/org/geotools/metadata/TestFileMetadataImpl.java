/*
 *    uDig - User Friendly Desktop Internet GIS client
 *    http://udig.refractions.net
 *    (C) 2004, Refractions Research Inc.
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
 *
 */
package org.geotools.metadata;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
    
    protected void setUp(){
        resource = TestFileMetadataImpl.class.getResource("testdata/ArcGrid.asc");
        assertNotNull(resource);
    }

    public void testFileMetadataImpl() {
        File f=new File(resource.getFile());
        assertNotNull(f);
        assertNotNull(new FileMetadataImpl( f, "ArcGrid" ));
    }

    public void testGetName() {
        File f=new File(resource.getFile());
        assertNotNull(f);
        FileMetadataImpl metadata=new FileMetadataImpl( f, "ArcGrid" );
        assertNotNull(metadata);

//      test begins
        assertEquals(metadata.getName(), f.getName());
        
    }

    public void testGetExtension() {
        File f=new File(resource.getFile());
        assertNotNull(f);
        FileMetadataImpl metadata=new FileMetadataImpl( f, "ArcGrid" );
        assertNotNull(metadata);

//      test begins
        assertEquals(metadata.getExtension(), "asc");
    }

    public void testGetFile() {
        File f=new File(resource.getFile());
        assertNotNull(f);
        FileMetadataImpl metadata=new FileMetadataImpl( f, "ArcGrid" );
        assertNotNull(metadata);

//      test begins
        assertEquals(metadata.getFile(), f);
    }

    public void testGetFormat() {
        File f=new File(resource.getFile());
        assertNotNull(f);
        FileMetadataImpl metadata=new FileMetadataImpl( f, "ArcGrid" );
        assertNotNull(metadata);

//      test begins
        assertEquals(metadata.getFormat(), "ArcGrid");
    }

    public void testGetLastModified() {
        File f=new File(resource.getFile());
        assertNotNull(f);
        FileMetadataImpl metadata=new FileMetadataImpl( f, "ArcGrid" );
        assertNotNull(metadata);

//      test begins
        assertEquals(metadata.getLastModified(), f.lastModified());
    }

    public void testGetPath() {
        File f=new File(resource.getFile());
        assertNotNull(f);
        FileMetadataImpl metadata=new FileMetadataImpl( f, "ArcGrid" );
        assertNotNull(metadata);

//      test begins
        assertEquals(metadata.getPath(), f.getPath());
    }

    public void testSetFormat() {
        File f=new File(resource.getFile());
        assertNotNull(f);
        FileMetadataImpl metadata=new FileMetadataImpl( f, "ArcGrid" );
        assertNotNull(metadata);

//      test begins
        metadata.setFormat("Geotiff");
        assertEquals(metadata.getFormat(), "Geotiff");
    }

    /*
     * Calls getElements(null)
     */
    public void testGetElementsPassedNull() {
        File f=new File(resource.getFile());
        assertNotNull(f);
        FileMetadataImpl metadata=new FileMetadataImpl( f, "ArcGrid" );
        assertNotNull(metadata);

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
        File f=new File(resource.getFile());
        assertNotNull(f);
        FileMetadataImpl metadata=new FileMetadataImpl( f, "ArcGrid" );
        assertNotNull(metadata);

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
        File f=new File(resource.getFile());
        assertNotNull(f);
        FileMetadataImpl metadata=new FileMetadataImpl( f, "ArcGrid" );
        assertNotNull(metadata);

        // test begins
        assertNotNull(metadata.getEntity());
    }

    /*
     * Class under test for Object getElement(ElementType)
     */
    public void testGetElementElementType() {
        File f=new File(resource.getFile());
        assertNotNull(f);
        FileMetadataImpl metadata=new FileMetadataImpl( f, "ArcGrid" );
        assertNotNull(metadata);

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
        File f=new File(resource.getFile());
        assertNotNull(f);
        FileMetadataImpl metadata=new FileMetadataImpl( f, "ArcGrid" );
        assertNotNull(metadata);

        // test begins
        Entity entity=metadata.getEntity();
        List elements=entity.getElements();
        List values=metadata.getElements(null);
        int i=0;
        for (Iterator iter = elements.iterator(); iter.hasNext();i++) {
            Metadata.Element element = (Metadata.Element) iter.next();
            String name=element.getName();
            Object value=metadata.getElement(name);
            assertEquals(value, values.get(i));
        }
    }

 }
