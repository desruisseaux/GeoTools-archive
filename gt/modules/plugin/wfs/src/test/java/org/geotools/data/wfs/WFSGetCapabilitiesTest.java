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
package org.geotools.data.wfs;

import java.io.File;
import java.net.URI;
import java.util.logging.Level;

import junit.framework.TestCase;

import org.geotools.test.TestData;
import org.geotools.xml.DocumentFactory;
import org.xml.sax.SAXException;


/**
 * <p>
 * DOCUMENT ME!
 * </p>
 * @
 *
 * @author dzwiers www.refractions.net
 * @source $URL$
 */
public class WFSGetCapabilitiesTest extends TestCase {
    public void testGeomatics(){
        try {            
            String path = "geomatics-wfs-getCapabilities.xml";

            File f = TestData.file(this,path);
            URI u = f.toURI();

            Object doc = DocumentFactory.getInstance(u,null,Level.WARNING);
            
            assertNotNull("Document missing", doc);
            System.out.println(doc);
            
        } catch (SAXException e) {
            e.printStackTrace();
            fail(e.toString());
        } catch (Throwable e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }
    public void testMapServer(){
        try {            
            String path = "mswfs_gmap-getCapabilities.xml";

            File f = TestData.file(this,path);
            URI u = f.toURI();

            Object doc = DocumentFactory.getInstance(u,null,Level.WARNING);
            
            assertNotNull("Document missing", doc);
            System.out.println(doc);
            
        } catch (SAXException e) {
            e.printStackTrace();
            fail(e.toString());
        } catch (Throwable e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }
    public void testGaldos(){
       try {            
           String path = "galdos-http-getCapabilities.xml";

           File f = TestData.file(this,path);
           URI u = f.toURI();

           Object doc = DocumentFactory.getInstance(u,null,Level.WARNING);
           
           assertNotNull("Document missing", doc);
           System.out.println(doc);
           
       } catch (SAXException e) {
           e.printStackTrace();
           fail(e.toString());
       } catch (Throwable e) {
           e.printStackTrace();
           fail(e.toString());
       }
   }
    public void testIonic(){
       try {            
           String path = "ionic-wfs-getCapabilities.xml";

           File f = TestData.file(this,path);
           URI u = f.toURI();

           Object doc = DocumentFactory.getInstance(u,null,Level.WARNING);
           
           assertNotNull("Document missing", doc);
           System.out.println(doc);
           
       } catch (SAXException e) {
           e.printStackTrace();
           fail(e.toString());
       } catch (Throwable e) {
           e.printStackTrace();
           fail(e.toString());
       }
   }
}
