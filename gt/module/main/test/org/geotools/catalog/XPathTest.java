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
package org.geotools.catalog;

import java.util.List;
import org.geotools.catalog.MetadataEntity.Element;

import junit.framework.TestCase;

/**
 * TODO type description
 * 
 * @author jeichar
 *
 */
public class XPathTest extends TestCase {

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
   }

    public void testXPath() {
        XPath xpath=new XPath("google/library/*");
        String[] terms=xpath.getTerms();
        assertNotNull(terms);
        assertEquals(terms[0],"google");
        assertEquals(terms[1],"library");
        assertEquals(terms[2],"*");
    }

    /*
     * Class under test for Element match(Metadata, int)
     */
    public void testMatchMetadataint() {
        XPath xpath=new XPath("fileData/name");
        StupidNestedMetadataImpl data=new StupidNestedMetadataImpl();
        List result=xpath.getElement(data.getEntityType());
        assertEquals(result.size(),1);
        Element elem=(Element)result.get(0);
        assertNotNull(elem);
        assertEquals(elem.getName(),"name");
        assertEquals(elem.getType(),String.class);
        
        xpath=new XPath("fileData");
        data=new StupidNestedMetadataImpl();
        result=xpath.getElement(data.getEntityType());
        assertEquals(result.size(),1);
        elem=(Element)result.get(0);
        assertNotNull(elem);
        assertEquals(elem.getName(),"fileData");
        assertEquals(elem.getType(),StupidFileData.class);
                
        
    }

    /*
     * Class under test for Element match(String, Metadata)
     */
    public void testMatchStringMetadata() {
        StupidNestedMetadataImpl data=new StupidNestedMetadataImpl();
        List result=XPath.getElement("fileData/name",data);
        assertEquals(result.size(),1);
        Element elem=(Element)result.get(0);
        assertNotNull(elem);
        assertEquals(elem.getName(),"name");
        assertEquals(elem.getType(),String.class);
    }

    /*
     * Class under test for Element match(String, Metadata)
     * Wildcards in xpath tested
     */
    public void testMatchStringMetadataWildCards() {
        StupidNestedMetadataImpl data=new StupidNestedMetadataImpl();
        List result=XPath.getElement("fileData/\\w*",data);
        assertEquals(result.size(),3);

        result=XPath.getElement("\\w*/name",data);
        assertEquals(result.size(),1);
        Element element=(Element)result.get(0);
        assertEquals(element.getType(),String.class);
    
        result=XPath.getElement("\\w*",data);
        assertEquals(result.size(),2);
        element=(Element)result.get(0);
        Element element1=(Element)result.get(1);
        if( element.getType().isAssignableFrom(String.class) )
            assertEquals(StupidFileData.class, element1.getType());
        else
            assertEquals(StupidFileData.class, element.getType());
        
        // Now test getValue
        data=new StupidNestedMetadataImpl();
        result=XPath.getValue("fileData/\\w*",data);
        assertEquals(result.size(),3);

        result=XPath.getValue("\\w*/name",data);
        assertEquals(result.size(),1);
        String name=(String)result.get(0);
        assertEquals(name,"Stupid");
    
        result=XPath.getValue("\\w*",data);
        assertEquals(result.size(),2);
    }

}
