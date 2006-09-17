/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2005-2006, GeoTools Project Managment Committee (PMC)
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
package org.geotools.data.postgis.collection;

import org.geotools.data.DataStoreFinder;
import org.geotools.data.DataTestCase;
import org.geotools.data.DefaultQuery;
import org.geotools.data.Query;
import org.geotools.data.jdbc.JDBCFeatureCollection;
import org.geotools.data.jdbc.JDBCFeatureSource;
import org.geotools.data.postgis.PostgisDataStore;
import org.geotools.feature.FeatureType;
import org.geotools.feature.visitor.AverageVisitor;
import org.geotools.feature.visitor.CountVisitor;
import org.geotools.feature.visitor.MaxVisitor;
import org.geotools.feature.visitor.MinVisitor;
import org.geotools.feature.visitor.SumVisitor;
import org.geotools.feature.visitor.UniqueVisitor;
import org.geotools.filter.AttributeExpression;
import org.geotools.filter.Expression;
import org.geotools.filter.ExpressionBuilder;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.FilterFactoryFinder;
import org.geotools.filter.FunctionExpression;
import org.geotools.filter.LiteralExpression;
import org.geotools.filter.MathExpression;

import java.util.HashMap;
import java.util.Map;
import java.util.PropertyResourceBundle;


/**
 * This class tests PostgisFeatureCollection, with the inherited tests from
 * JDBCFeatureCollection.
 *
 * @author Cory Horner, Refractions Research
 * @source $URL$
 */
public class PostgisFeatureCollectionOnlineTest extends DataTestCase {
    private PostgisDataStore dstore = null;
    private JDBCFeatureCollection fc = null;
    private JDBCFeatureCollection fc2 = null;
    private FeatureType featureType = null;
    private FeatureType featureType2 = null;
    private FilterFactory ff = FilterFactoryFinder.createFilterFactory();
    AttributeExpression att = null;
    AttributeExpression att2 = null;

    public PostgisFeatureCollectionOnlineTest(String test) {
        super(test);
    }

    protected void setUp() throws Exception {
        // super.setUp();
        PropertyResourceBundle resource;
        resource = new PropertyResourceBundle(this.getClass()
                                                  .getResourceAsStream("demo-bc-fixture.properties"));

        //String namespace = resource.getString("namespace");
        Map params = new HashMap();
        params.put("dbtype", "postgis");
        params.put("host", resource.getString("host"));
        params.put("port", resource.getString("port"));
        params.put("database", resource.getString("database"));
        params.put("user", resource.getString("user"));
        params.put("passwd", resource.getString("password"));

        dstore = (PostgisDataStore) DataStoreFinder.getDataStore(params);

        //String[] schemas = dstore.getTypeNames();
        featureType = dstore.getSchema("bc_voting_areas");

        JDBCFeatureSource source = new JDBCFeatureSource(dstore, featureType);
        Query query = new DefaultQuery(featureType.toString(), null);
        fc = new PostgisFeatureCollection(source, query);
        featureType2 = dstore.getSchema("bc_hospitals");

        JDBCFeatureSource source2 = new JDBCFeatureSource(dstore, featureType2);
        Query query2 = new DefaultQuery(featureType2.toString(), null);
        fc2 = new PostgisFeatureCollection(source2, query2);
        att = ff.createAttributeExpression(null, "vregist");
        att2 = ff.createAttributeExpression(null, "authority");
    }

    public void testSumCount() throws Exception {
        SumVisitor sumVisitor = new SumVisitor(att);
        fc.accepts(sumVisitor, null);
        assertTrue(fc.isOptimized); //the postgis optimization was used
        assertEquals(2209425, sumVisitor.getResult().toInt());

        //test count (we need this for the complex expression, so may as well test it here)
        CountVisitor countVisitor = new CountVisitor();
        fc.accepts(countVisitor, null);
        assertEquals(7986, countVisitor.getResult().toInt());

        //test complex expression
        LiteralExpression one = ff.createLiteralExpression(1);
        MathExpression addExpr = ff.createMathExpression(Expression.MATH_ADD);
        addExpr.addLeftValue(one);
        addExpr.addRightValue(att);

        SumVisitor sumVisitor2 = new SumVisitor(addExpr);
        fc.accepts(sumVisitor2, null);
        assertTrue(fc.isOptimized);
        assertEquals(2217411, sumVisitor2.getResult().toInt());
    }

    public void testMinMax() throws Exception {
        MinVisitor minVisitor = new MinVisitor(att);
        fc.accepts(minVisitor, null);
        assertTrue(fc.isOptimized); //the postgis optimization was used
        assertEquals(0, minVisitor.getResult().toInt());

        MaxVisitor maxVisitor = new MaxVisitor(att);
        fc.accepts(maxVisitor, null);
        assertTrue(fc.isOptimized); //the postgis optimization was used
        assertEquals(1890, maxVisitor.getResult().toInt());
    }

    // Optimization is currently unavailable for median!
    //	public void testMedian() throws Exception {
    //		AttributeExpression att = ff.createAttributeExpression(null, "vregist");
    //	    MedianVisitor medianVisitor = new MedianVisitor(att);
    //		fc.accepts(medianVisitor);
    //		assertTrue(fc.isOptimized); //the postgis optimization was used
    //		assertEquals(67, medianVisitor.getResult().toDouble(), 0);
    //	}
    
    public void testAverage() throws Exception {
        AverageVisitor averageVisitor = new AverageVisitor(att);
        fc.accepts(averageVisitor, null);
        assertTrue(fc.isOptimized); //the postgis optimization was used
        assertEquals(276, averageVisitor.getResult().toInt());
    }

    public void testUnique() throws Exception {
        UniqueVisitor uniqueVisitor = new UniqueVisitor(att2);
        fc2.accepts(uniqueVisitor, null);
        assertTrue(fc2.isOptimized); //the postgis optimization was used
        assertEquals(7, uniqueVisitor.getResult().toSet().size());
    }
    
    public void testSumExpression() throws Exception {
    	ExpressionBuilder eb = new ExpressionBuilder();
    	FunctionExpression expr = (FunctionExpression) eb.parser("Collection_Sum(vregist)");
    	int result = ((Number) expr.getValue(fc)).intValue();
    	assertTrue(fc.isOptimized);
    	assertEquals(result, 2209425);
    }
    
}
