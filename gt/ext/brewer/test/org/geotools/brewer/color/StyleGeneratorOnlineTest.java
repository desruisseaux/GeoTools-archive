/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
 */
package org.geotools.brewer.color;

import java.util.HashMap;
import java.util.Map;

import org.geotools.data.DataStore;
import org.geotools.data.DataTestCase;
import org.geotools.data.DefaultQuery;
import org.geotools.data.jdbc.JDBCFeatureSource;
import org.geotools.data.postgis.PostgisDataStoreFactory;
import org.geotools.data.postgis.collection.PostgisFeatureCollection;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.FilterFactoryFinder;
import org.geotools.filter.expression.AttributeExpression;
import org.geotools.filter.function.ClassificationFunction;
import org.geotools.filter.function.UniqueIntervalFunction;
import org.geotools.styling.FeatureTypeStyle;


/**
 *
 * @source $URL: http://svn.geotools.org/geotools/branches/2.2.x/ext/brewer/test/org/geotools/brewer/color/StyleGeneratorOnlineTest.java $
 */
public class StyleGeneratorOnlineTest extends DataTestCase {

    static boolean WKB_ENABLED = true;
    static boolean CHECK_TYPE = false;

    PostgisTests.Fixture f;
    DataStore data;
    Map remote;
    PostgisFeatureCollection fc;
    
    public StyleGeneratorOnlineTest(String arg0) {
        super(arg0);
    }

    public String getFixtureFile() {
        return "fixture.properties";
    }

    protected void setUp() throws Exception {
        super.setUp();

        f = PostgisTests.newFixture(getFixtureFile());
        
        remote = new HashMap();
        remote.put("dbtype","postgis");        
        remote.put("charset", "");
        remote.put("host",f.host);
        remote.put("port", f.port);
        remote.put("database", f.database);
        remote.put("user", f.user);
        remote.put("passwd", f.password);
        remote.put("namespace", f.namespace);

        PostgisDataStoreFactory pdsf = new PostgisDataStoreFactory();
        data = pdsf.createDataStore(remote);
        JDBCFeatureSource featureSource = (JDBCFeatureSource) data.getFeatureSource("bc_hospitals");
        fc = new PostgisFeatureCollection(featureSource, DefaultQuery.ALL);
    }
    
    protected void tearDown() throws Exception {
        fc = null;
        data = null;
        super.tearDown();
    }
    
    /**
     * Simple test to ensure unique interval function works on real data
     * containing nulls.
     * 
     * @throws Exception
     */
    public void testUniqueInterval() throws Exception {

        ColorBrewer brewer = new ColorBrewer();
        brewer.loadPalettes();

        FilterFactory ff = FilterFactoryFinder.createFilterFactory();
        AttributeExpression expr = null;
        expr = ff.createAttributeExpression("authority");

        String paletteName = "YlGn";

        //create the classification function
        ClassificationFunction classifier = new UniqueIntervalFunction();
        classifier.setNumberOfClasses(7);
        classifier.setCollection(fc);
        classifier.setExpression(expr);
        classifier.getValue(0); 

        //get the fts
        StyleGenerator sg = new StyleGenerator(brewer.getPalette(paletteName)
                                                     .getColors(7), classifier,
                "myfts");
        FeatureTypeStyle fts = sg.createFeatureTypeStyle(roadFeatures[0].getFeatureType()
                                                                        .getDefaultGeometry());
        assertNotNull(fts);

    }
}
