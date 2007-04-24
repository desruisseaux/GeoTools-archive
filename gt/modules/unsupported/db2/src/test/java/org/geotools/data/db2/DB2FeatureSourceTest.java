/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) Copyright IBM Corporation, 2005-2007. All rights reserved.
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
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureType;
import org.geotools.filter.IllegalFilterException;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.spatial.BBOX;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import java.io.IOException;
import java.util.Iterator;


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
    FilterFactory2 ff = (FilterFactory2) CommonFactoryFinder.getFilterFactory(null);

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
        String coordString = null;
        DefaultQuery query;

        //Test "Places" - all bounds
        featureSource = new DB2FeatureSource(dataStore,dataStore.getSchema("Places"));
        env = featureSource.getBounds();
        assertEquals("all places bounds",
            "[74°09.6'W 41°59.6'N , 74°04.1'W 42°03.6'N]",
            env.toString());

        env = featureSource.getBounds(Query.ALL);
        coordString = env2CoordString(env);        
        assertEquals("all places bounds",
            "Env[-74.160507 : -74.067637, 41.993695 : 42.05990399999999]", coordString);

        //Test "Roads" - all bounds
        featureSource = dataStore.getFeatureSource("Roads");
        env = featureSource.getBounds();
        coordString = env2CoordString(env);
        assertEquals("all roads bounds",
            "Env[598054.2 : 604430.47, 1158025.78 : 1165565.78]", coordString);

        //Test "Roads" - roadsEnv1 bbox		
        query = getBBOXQuery(featureSource, roadsEnv1);
        env = featureSource.getBounds(query);
        coordString = env2CoordString(env);        
        assertEquals("all roads bounds",
            "Env[599280.58 : 604430.47, 1159468.47 : 1162830.55]",
			coordString);
    }

    private void checkFidTable(String featureName, String testValue) throws IOException {
        FeatureSource featureSource;
        FeatureCollection features;
        Iterator it;

        featureSource = dataStore.getFeatureSource(featureName);
        features = featureSource.getFeatures();
        it = features.iterator();
        while (it.hasNext()) {
        	Feature f = (Feature) it.next();
        	String s = f.toString();
        	int pos = testValue.indexOf(s);
        	System.out.println(pos + s);
            assertTrue(featureName, (pos >= 0));
        }
    	
    }
    public void testFidTables() throws Exception {
    	checkFidTable("FIDMCOLPRIKEY", 
    			"Feature[ id=FIDMCOLPRIKEY.key1+++++++&1 , IDCOL1=key1        , IDCOL2=1 , GEOM=POINT (-76 42.5) ];Feature[ id=FIDMCOLPRIKEY.key2+++++++&2 , IDCOL1=key2        , IDCOL2=2 , GEOM=POINT (-76.5 42) ]");
    	checkFidTable("FIDCHARPRIKEY", 
    			"Feature[ id=FIDCHARPRIKEY.key1            , IDCOL=key1            , GEOM=POINT (-76 42.5) ];Feature[ id=FIDCHARPRIKEY.key2            , IDCOL=key2            , GEOM=POINT (-76.5 42) ]");
    	checkFidTable("FIDVCHARPRIKEY", 
    			"Feature[ id=FIDVCHARPRIKEY.key1 , IDCOL=key1 , GEOM=POINT (-76 42.5) ];Feature[ id=FIDVCHARPRIKEY.key2 , IDCOL=key2 , GEOM=POINT (-76.5 42) ]");
    	checkFidTable("FIDNOPRIKEY", 
    			"Feature[ id=FIDNOPRIKEY.2 , IDCOL=1 , GEOM=POINT (-76 42.5) ];Feature[ id=FIDNOPRIKEY.3 , IDCOL=2 , GEOM=POINT (-76.5 42) ]");
    	checkFidTable("FIDINTPRIKEY", 
    			"Feature[ id=FIDINTPRIKEY.1 , IDCOL=1 , GEOM=POINT (-76 42.5) ];Feature[ id=FIDINTPRIKEY.2 , IDCOL=2 , GEOM=POINT (-76.5 42) ]");
    	checkFidTable("FIDAUTOINC", 
    			"Feature[ id=FIDAUTOINC.1 , GEOM=POINT (-76 42.5) ];Feature[ id=FIDAUTOINC.2 , GEOM=POINT (-76.5 42) ]");
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
        String schemaCompare = "DefaultFeatureType [name=Roads , namespace=Test , abstract=false , types=(DefaultAttributeType [name=ID , type=class java.lang.Integer , nillable=true, min=1, max=1],DefaultAttributeType [name=Name , type=class java.lang.String , nillable=true, min=0, max=0],DefaultAttributeType [name=Length , type=class java.lang.Double , nillable=true, min=0, max=0],DefaultAttributeType [name=Geom , type=class com.vividsolutions.jts.geom.LineString , nillable=true, min=0, max=0],)]";
        System.out.println("schema: " + schemaFound);
        assertEquals("schema mismatch", schemaCompare, schemaFound);
    }

    private BBOX getBBOXFilter(FeatureSource featureSource,
        Envelope env) throws IllegalFilterException {
    	
    	double xmin = env.getMinX();
    	double ymin = env.getMinY();
    	double xmax = env.getMaxX();
    	double ymax = env.getMaxY();

    	BBOX bbox = ff.bbox("Geom",xmin,ymin,xmax,ymax,"");

        return bbox;
    }

    private DefaultQuery getBBOXQuery(FeatureSource featureSource, Envelope env)
        throws IllegalFilterException {
        BBOX bbox = getBBOXFilter(featureSource, env);
        FeatureType ft = featureSource.getSchema();

        return new DefaultQuery(ft.getTypeName(), bbox);
    }
        public String env2CoordString(Envelope env) {
    	String result = null;
    	result = "Env[" + env.getMinX() + " : " + env.getMaxX() 
				 + ", " + env.getMinY() + " : " + env.getMaxY() + "]";
    	return result;
    }
}
