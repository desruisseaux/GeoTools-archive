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
package org.geotools.xml;

import java.io.File;
import java.net.URI;
import java.util.logging.Level;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.resources.TestData;

import junit.framework.TestCase;

public class GMLInheritanceTest extends TestCase {

    
    public void testOneFeature(){
        try {
            SAXParserFactory spf = SAXParserFactory.newInstance();
            spf.setNamespaceAware(true);
            spf.setValidating(false);

            SAXParser parser = spf.newSAXParser();

            String path = "sample/nestedFeatures.xml";
            File f = TestData.file(this,path);
            URI u = f.toURI();

            XMLSAXHandler xmlContentHandler = new XMLSAXHandler(u,null);
            XMLSAXHandler.setLogLevel(Level.FINEST);
            XSISAXHandler.setLogLevel(Level.FINEST);
            XMLElementHandler.setLogLevel(Level.FINEST);
            XSIElementHandler.setLogLevel(Level.FINEST);

            parser.parse(f, xmlContentHandler);

            Object doc = xmlContentHandler.getDocument();
            assertNotNull("Document missing", doc);
//            System.out.println(doc);
            
            checkFeatureCollection((FeatureCollection)doc);
            
        } catch (Throwable e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }
    
    private void checkFeatureCollection(FeatureCollection doc){
               
        //remaining slot (s) should be feature(s)
        assertTrue("Requires atleast one feature",doc.size()>0);  //bbox + feature
        FeatureIterator i = doc.features();
        int j = 1;
        while(i.hasNext()){
            Feature ft = i.next();
            assertNotNull("Feature #"+j+" is null",ft);
//            assertNotNull("Feature #"+j+" missing crs ",ft.getFeatureType().getDefaultGeometry().getCoordinateSystem());
//            System.out.println("Feature "+j+" : "+ft);
            j++;
        }
        System.out.println("Found "+j+" Features");
    }
}
