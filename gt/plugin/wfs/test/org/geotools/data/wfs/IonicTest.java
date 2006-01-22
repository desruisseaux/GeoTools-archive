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
 * @source $URL$
 */
public class IonicTest extends TestCase {

    private URL url = null;
    
    public IonicTest() throws MalformedURLException{
        url = new URL("http://webservices.ionicsoft.com/ionicweb/wfs/BOSTON_ORA?version=1.0.0&request=getcapabilities&service=WFS");
    }
    
    public void testFeatureType() throws NoSuchElementException, IOException, SAXException{
        WFSDataStoreReadTest.doFeatureType(url,true,true,0);
    }
    public void testFeatureReader() throws NoSuchElementException, IOException, IllegalAttributeException, SAXException{
        // FAILS due to Choice !!!
        WFSDataStoreReadTest.doFeatureReader(url,true,true,0);
    }
    public void testFeatureReaderWithFilter() throws NoSuchElementException, OperationNotSupportedException, IllegalAttributeException, IOException, SAXException{
        WFSDataStoreReadTest.doFeatureReaderWithFilter(url,true,true,0);
    }
}
