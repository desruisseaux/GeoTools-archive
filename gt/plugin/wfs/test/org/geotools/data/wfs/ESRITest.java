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

import javax.naming.OperationNotSupportedException;

import junit.framework.TestCase;

import org.geotools.feature.IllegalAttributeException;
import org.xml.sax.SAXException;

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
public class ESRITest extends TestCase {

    private URL url = null;
    
    public ESRITest() throws MalformedURLException{
        // Too slow
        url = new URL("http://dev.geographynetwork.ca/ogcwfs/servlet/com.esri.ogc.wfs.WFSServlet?Request=GetCapabilities");
    }
    
    public void testFeatureType() throws NoSuchElementException, IOException, SAXException{
//        WFSDataStoreReadTest.doFeatureType(url,true,false,2);
    }
    public void testFeatureReader() throws NoSuchElementException, IOException, IllegalAttributeException, SAXException{
//        WFSDataStoreReadTest.doFeatureReader(url,true,false,2);
    }
    public void testFeatureReaderWithFilter() throws NoSuchElementException, OperationNotSupportedException, IllegalAttributeException, IOException, SAXException{
//        WFSDataStoreReadTest.doFeatureReaderWithFilter(url,true,false,2);
    }
}
