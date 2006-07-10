/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) Copyright IBM Corporation, 2005. All rights reserved.
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
package org.geotools.data.db2;

import com.vividsolutions.jts.geom.Envelope;
import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureSource;
import org.geotools.data.Query;
import org.geotools.feature.FeatureType;
import org.geotools.filter.AbstractFilter;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.FilterFactoryFinder;
import org.geotools.filter.GeometryFilter;
import org.geotools.filter.IllegalFilterException;
import org.geotools.filter.expression.AttributeExpression;
import org.geotools.filter.expression.LiteralExpression;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import java.io.IOException;


/**
 * Exercise DB2FeatureSource
 *
 * @author David Adler - IBM Corporation
 * @source $URL$
 */
public class DB2FeatureSourceTest extends DB2TestCase {
    private DB2DataStore dataStore = null;
    private Envelope placesEnv1 = new Envelope(-74.15, -74.1, 42.0, 42.02);
    private Envelope placesEnv2 = new Envelope(-74.15, -74.12, 42.0, 42.01);
    private Envelope roadsEnv1 = new Envelope(600000.0, 604000.0, 1160000.0,
            1162000.0);

    /**
     * Get a DB2DataStore that we will use for all the tests.
     *
     * @throws Exception
     */
    public void setUp() throws Exception {
        super.setUp();
        dataStore = getDataStore();
    }

    public void testGetBounds() throws Exception {
        FeatureSource featureSource;
        Envelope env;
        DefaultQuery query;

        //Test "Places" - all bounds
        featureSource = dataStore.getFeatureSource("Places");
        env = featureSource.getBounds();
        assertEquals("all places bounds",
            "Env[-74.160507 : -74.067637, 41.993695 : 42.05990399999999]",
            env.toString());

        env = featureSource.getBounds(Query.ALL);
        assertEquals("all places bounds",
            "Env[-74.160507 : -74.067637, 41.993695 : 42.05990399999999]",
            env.toString());

        //Test "Roads" - all bounds
        featureSource = dataStore.getFeatureSource("Roads");
        env = featureSource.getBounds();
        assertEquals("all roads bounds",
            "Env[598054.2 : 604430.47, 1158025.78 : 1165565.78]", env.toString());

        //Test "Roads" - roadsEnv1 bbox		
        query = getBBOXQuery(featureSource, roadsEnv1);
        env = featureSource.getBounds(query);
        assertEquals("all roads bounds",
            "Env[599280.58 : 604430.47, 1159468.47 : 1162830.55]",
            env.toString());
    }

    public void testGetCount() throws Exception {
        FeatureSource featureSource;
        int count;

        // Check "Roads"
        featureSource = dataStore.getFeatureSource("Roads");
        count = featureSource.getCount(Query.ALL);
        assertEquals("all roads count", 87, count);

        count = featureSource.getCount(getBBOXQuery(featureSource, roadsEnv1));
        assertEquals("all roads count", 28, count);

        // Check "Places"
        featureSource = dataStore.getFeatureSource("Places");
        count = featureSource.getCount(Query.ALL);
        assertEquals("all places count", 3, count);

        count = featureSource.getCount(getBBOXQuery(featureSource, placesEnv1));
        assertEquals("bbox1 places count", 3, count);

        count = featureSource.getCount(getBBOXQuery(featureSource, placesEnv2));
        assertEquals("bbox2 places count", 1, count);
    }

    public void testCRS() throws IOException {
        FeatureSource featureSource;
        CoordinateReferenceSystem crs;

        // Check "Roads"
        featureSource = dataStore.getFeatureSource("Roads");
        crs = featureSource.getSchema().getDefaultGeometry()
                           .getCoordinateSystem();
        assertEquals("CRS mismatch",
            "NAD_1983_StatePlane_New_York_East_FIPS_3101_Feet",
            crs.getName().toString());

        // Check "Places"
        featureSource = dataStore.getFeatureSource("Places");
        crs = featureSource.getSchema().getDefaultGeometry()
                           .getCoordinateSystem();
        assertEquals("CRS mismatch", "GCS_North_American_1983",
            crs.getName().toString());
    }

    public void testSchema() throws IOException {
        FeatureSource featureSource;
        CoordinateReferenceSystem crs;
        featureSource = dataStore.getFeatureSource("Roads");

        String schemaFound = featureSource.getSchema().toString();
        String schemaCompare = "DefaultFeatureType [name=Roads , namespace=Test , abstract=false , types=(DefaultAttributeType [name=ID , type=class java.lang.Long , nillable=true, min=1, max=1],DefaultAttributeType [name=Name , type=class java.lang.String , nillable=true, min=1, max=1],DefaultAttributeType [name=Length , type=class java.lang.Double , nillable=true, min=1, max=1],DefaultAttributeType [name=Geom , type=class com.vividsolutions.jts.geom.MultiLineString , nillable=true, min=1, max=1],)]";
        System.out.println("schema: " + schemaFound);
        assertEquals("schema mismatch", schemaCompare, schemaFound);
    }

    private GeometryFilter getBBOXFilter(FeatureSource featureSource,
        Envelope env) throws IllegalFilterException {
        FilterFactory ff = FilterFactoryFinder.createFilterFactory();
        GeometryFilter gf = ff.createGeometryFilter(AbstractFilter.GEOMETRY_BBOX);
        LiteralExpression envelope = ff.createBBoxExpression(env);
        FeatureType ft = featureSource.getSchema();
        AttributeExpression spatialColumn = ff.createAttributeExpression(ft,
                "Geom");
        gf.addLeftGeometry(spatialColumn);
        gf.addRightGeometry(envelope);

        return gf;
    }

    private DefaultQuery getBBOXQuery(FeatureSource featureSource, Envelope env)
        throws IllegalFilterException {
        GeometryFilter gf = getBBOXFilter(featureSource, env);
        FeatureType ft = featureSource.getSchema();

        return new DefaultQuery(ft.getTypeName(), gf);
    }
}
