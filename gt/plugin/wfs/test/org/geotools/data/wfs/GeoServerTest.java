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

import org.geotools.data.DataStore;
import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureReader;
import org.geotools.data.Transaction;
import org.geotools.factory.FactoryConfigurationError;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.filter.FidFilter;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.IllegalFilterException;
import org.xml.sax.SAXException;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

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
public class GeoServerTest extends TestCase {

    private URL url = null;
    
    public GeoServerTest() throws MalformedURLException{
//      url = new URL("http://localhost:8080/geoserver/wfs?REQUEST=GetCapabilities");
        url = new URL("http://www.refractions.net:8080/geoserver/wfs?REQUEST=GetCapabilities");
    }
    
    public void testFeatureType() throws NoSuchElementException, IOException, SAXException{
        WFSDataStoreReadTest.doFeatureType(url,true,false,1);
    }
    public void testFeatureReader() throws NoSuchElementException, IOException, IllegalAttributeException, SAXException{
        WFSDataStoreReadTest.doFeatureReader(url,true,true,1);
    }
    public void testFeatureReaderWithFilter() throws NoSuchElementException, OperationNotSupportedException, IllegalAttributeException, IOException, SAXException{
        WFSDataStoreReadTest.doFeatureReaderWithFilter(url,true,true,1);
    }
    
//    public void testWrite() throws NoSuchElementException, IllegalFilterException, FactoryConfigurationError, IOException, IllegalAttributeException{
//        DataStore post = WFSDataStoreWriteTest.getDataStore(url);
//        FeatureType ft = post.getSchema(post.getTypeNames()[0]);
//
//        GeometryFactory gf = new GeometryFactory();
//        MultiPolygon mp = gf.createMultiPolygon(new Polygon[]{gf.createPolygon(gf.createLinearRing(new Coordinate[]{new Coordinate(-88.071564,37.51099), new Coordinate(-88.467644,37.400757), new Coordinate(-90.638329,42.509361), new Coordinate(-89.834618,42.50346),new Coordinate(-88.071564,37.51099)}),new LinearRing[]{})});
//        mp.setUserData("http://www.opengis.net/gml/srs/epsg.xml#4326");
//        
//        Object[] attrs = {
//                mp,
//                "MyStateName",
//                "70",
//                "Refrac",
//                "RR",
//                new Double(180),
//                new Double(18),
//                new Double(220),
//                new Double(80),
//                new Double(20),
//                new Double(40),
//                new Double(180),
//                new Double(90),
//                new Double(100),
//                new Double(40),
//                new Double(80),
//                new Double(40),
//                new Double(180),
//                new Double(90),
//                new Double(70),
//                new Double(70),
//                new Double(60),
//                new Double(10)  
//        };
//        
//        System.out.println(attrs[0]);
//        Feature f = ft.create(attrs);
//        
//        FeatureReader inserts = DataUtilities.reader(new Feature[] {f});
//        FidFilter fp = WFSDataStoreWriteTest.doInsert(post,ft,inserts);
//        // geoserver does not return the correct fid here ... 
//        // get the 3rd feature ... and delete it?
//        
//        inserts.close();
//        inserts = post.getFeatureReader(new DefaultQuery(ft.getTypeName()),Transaction.AUTO_COMMIT);
//        int i = 0;
//        while(inserts.hasNext() && i<3){
//            f = inserts.next();i++;
//        }
//        inserts.close();
//        fp = FilterFactory.createFilterFactory().createFidFilter(f.getID());
//        
//        WFSDataStoreWriteTest.doDelete(post,ft,fp);
//        WFSDataStoreWriteTest.doUpdate(post,ft);
//    }
}
