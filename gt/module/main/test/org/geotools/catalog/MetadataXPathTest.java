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
import java.util.regex.Pattern;

import org.geotools.xml.XPath;
import org.geotools.xml.XPathFactory;
import org.opengis.catalog.MetadataEntity.Element;

import junit.framework.TestCase;

/**
 * TODO type description
 * 
 * @author jeichar
 *
 */
public class MetadataXPathTest extends TestCase {

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
   }

    public void testXPath() {
        XPath xpath=new MetadataXPath("google/library/\\w*");
        Pattern[] terms=xpath.getTerms();
        assertNotNull(terms);
        assertEquals(terms[0].pattern(),"google");
        assertEquals(terms[1].pattern(),"library");
        assertEquals(terms[2].pattern(),"\\w*");
    }

    /*
     * Class under test for Element match(Metadata, int)
     */
    public void testMatchMetadataint() {
        XPath xpath=new MetadataXPath("fileData/name");
        StupidNestedMetadataImpl data=new StupidNestedMetadataImpl();
        List result=xpath.find(data.getEntityType());
        assertEquals(result.size(),1);
        Element elem=(Element)result.get(0);
        assertNotNull(elem);
        assertEquals(elem.getName(),"name");
        assertEquals(elem.getType(),String.class);
        
        xpath=new MetadataXPath("fileData");
        data=new StupidNestedMetadataImpl();
        result=xpath.find(data.getEntityType());
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
        List result=XPathFactory.find("fileData/name",data);
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
        List result=XPathFactory.find("fileData/\\w*",data);
        assertEquals(result.size(),3);

        result=XPathFactory.find("\\w*/name",data);
        assertEquals(result.size(),1);
        Element element=(Element)result.get(0);
        assertEquals(element.getType(),String.class);
    
        result=XPathFactory.find("\\w*",data);
        assertEquals(result.size(),2);
        element=(Element)result.get(0);
        Element element1=(Element)result.get(1);
        if( element.getType().isAssignableFrom(String.class) )
            assertEquals(StupidFileData.class, element1.getType());
        else
            assertEquals(StupidFileData.class, element.getType());
        
        // Now test getValue
        data=new StupidNestedMetadataImpl();
        result=XPathFactory.value("fileData/\\w*",data);
        assertEquals(result.size(),3);

        result=XPathFactory.value("\\w*/name",data);
        assertEquals(result.size(),1);
        String name=(String)result.get(0);
        assertEquals(name,"Stupid");
    
        result=XPathFactory.value("\\w*",data);
        assertEquals(result.size(),2);
    }

}
