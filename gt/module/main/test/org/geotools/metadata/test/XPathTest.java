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
package org.geotools.metadata.test;

import java.util.List;

import org.geotools.metadata.Metadata;
import org.geotools.metadata.XPath;


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
     * Class under test for Metadata.Element match(Metadata, int)
     */
    public void testMatchMetadataint() {
        XPath xpath=new XPath("FileData/Name");
        StupidNestedMetadataImpl data=new StupidNestedMetadataImpl();
        List result=xpath.getElement(data.getEntity());
        assertEquals(result.size(),1);
        Metadata.Element elem=(Metadata.Element)result.get(0);
        assertNotNull(elem);
        assertEquals(elem.getName(),"Name");
        assertEquals(elem.getType(),String.class);
        
        xpath=new XPath("FileData");
        data=new StupidNestedMetadataImpl();
        result=xpath.getElement(data.getEntity());
        assertEquals(result.size(),1);
        elem=(Metadata.Element)result.get(0);
        assertNotNull(elem);
        assertEquals(elem.getName(),"FileData");
        assertEquals(elem.getType(),StupidFileData.class);
                
        
    }

    /*
     * Class under test for Metadata.Element match(String, Metadata)
     */
    public void testMatchStringMetadata() {
        StupidNestedMetadataImpl data=new StupidNestedMetadataImpl();
        List result=XPath.getElement("FileData/Name",data);
        assertEquals(result.size(),1);
        Metadata.Element elem=(Metadata.Element)result.get(0);
        assertNotNull(elem);
        assertEquals(elem.getName(),"Name");
        assertEquals(elem.getType(),String.class);
    }

    /*
     * Class under test for Metadata.Element match(String, Metadata)
     * Wildcards in xpath tested
     */
    public void testMatchStringMetadataWildCards() {
        StupidNestedMetadataImpl data=new StupidNestedMetadataImpl();
        List result=XPath.getElement("FileData/\\w*",data);
        assertEquals(result.size(),3);

        result=XPath.getElement("\\w*/Name",data);
        assertEquals(result.size(),1);
        Metadata.Element element=(Metadata.Element)result.get(0);
        assertEquals(element.getType(),String.class);
    
        result=XPath.getElement("\\w*",data);
        assertEquals(result.size(),2);
        element=(Metadata.Element)result.get(0);
        Metadata.Element element1=(Metadata.Element)result.get(1);
        if( element.getType().isAssignableFrom(String.class) )
            assertEquals(StupidFileData.class, element1.getType());
        else
            assertEquals(StupidFileData.class, element.getType());
        
        // Now test getValue
        data=new StupidNestedMetadataImpl();
        result=XPath.getValue("FileData/\\w*",data);
        assertEquals(result.size(),3);

        result=XPath.getValue("\\w*/Name",data);
        assertEquals(result.size(),1);
        String name=(String)result.get(0);
        assertEquals(name,"Stupid");
    
        result=XPath.getValue("\\w*",data);
        assertEquals(result.size(),2);
    }

}
