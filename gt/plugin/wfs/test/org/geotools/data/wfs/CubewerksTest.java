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
package org.geotools.data.wfs;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.NoSuchElementException;

import org.geotools.feature.IllegalAttributeException;
import org.xml.sax.SAXException;

import junit.framework.TestCase;

/**
 *  summary sentence.
 * <p>
 * Paragraph ...
 * </p><p>
 * Responsibilities:
 * <ul>
 * <li>
 * <li>
 * </ul>
 * </p><p>
 * Example:<pre><code>
 * GeoServer x = new GeoServer( ... );
 * TODO code example
 * </code></pre>
 * </p>
 * @author dzwiers
 * @since 0.6.0
 */
public class CubewerksTest extends TestCase {

    private URL url = null;
    
    public CubewerksTest() throws MalformedURLException{

        //old server -- do not test version 0.0.16
//        url = new URL("http://ceoware2.ccrs.nrcan.gc.ca/cubewerx/cwwfs/cubeserv.cgi?datastore=CEOWARE2&version=1.0.0&service=WFS&request=GetCapabilities");
//        url = new URL("http://cgns.nrcan.gc.ca/wfs/cubeserv.cgi?DATASTORE=cgns&REQUEST=GetCapabilities&VERSION=1.0.0&SERVICE=WFS");
        url = new URL("http://demo.cubewerx.com/demo/cubeserv/cubeserv.cgi?SERVICE=wfs&request=getCapabilities&DATASTORE=Foundation");
    }
    
    public void testFeatureType() throws NoSuchElementException, IOException, SAXException{
        WFSDataStoreReadTest.doFeatureType(url,true,true,0);
    }
    public void testFeatureReader() throws NoSuchElementException, IOException, IllegalAttributeException, SAXException{
        WFSDataStoreReadTest.doFeatureReader(url,true,true,0);
    }
    public void testFeatureReaderWithFilter() throws NoSuchElementException, IllegalAttributeException, IOException, SAXException{
        WFSDataStoreReadTest.doFeatureReaderWithFilter(url,true,true,0);
    }
}
