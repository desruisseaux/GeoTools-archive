/*
 *    Geotools2 - OpenSource mapping toolkit
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
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.WKTReader;
import org.geotools.data.db2.filter.SQLEncoderDB2;
import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypeFactory;
import org.geotools.filter.AbstractFilter;
import org.geotools.filter.FilterCapabilities;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.FilterFactoryFinder;
import org.geotools.filter.GeometryDistanceFilter;
import org.geotools.filter.GeometryFilter;
import org.geotools.filter.IllegalFilterException;
import org.geotools.filter.SQLEncoderException;
import org.geotools.filter.expression.AttributeExpression;
import org.geotools.filter.expression.Expression;
import org.geotools.filter.expression.LiteralExpression;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


/**
 * DOCUMENT ME!
 *
 * @author David Adler
 * @source $URL$
 */
public class SQLEncoderDB2Test extends DB2TestCase {
    private static Map DB2_SPATIAL_PREDICATES = new HashMap();
    private SQLEncoderDB2 encoder;
    FilterFactory ff = null;
    LiteralExpression bboxLiteral = null;
    String sqlString = null;
    AttributeType[] types = null;
    FeatureType pointType = null;
    AttributeExpression spatialColumn = null;
    LiteralExpression doubleLiteral = null;
    LiteralExpression geometryLiteral = null;

    {
        DB2_SPATIAL_PREDICATES.put("EnvelopesIntersect",
            new Integer(AbstractFilter.GEOMETRY_BBOX));
        DB2_SPATIAL_PREDICATES.put("ST_Contains",
            new Integer(AbstractFilter.GEOMETRY_CONTAINS));
        DB2_SPATIAL_PREDICATES.put("ST_Crosses",
            new Integer(AbstractFilter.GEOMETRY_CROSSES));
        DB2_SPATIAL_PREDICATES.put("ST_Disjoint",
            new Integer(AbstractFilter.GEOMETRY_DISJOINT));
        DB2_SPATIAL_PREDICATES.put("ST_Equals",
            new Integer(AbstractFilter.GEOMETRY_EQUALS));
        DB2_SPATIAL_PREDICATES.put("ST_Intersects",
            new Integer(AbstractFilter.GEOMETRY_INTERSECTS));
        DB2_SPATIAL_PREDICATES.put("ST_Overlaps",
            new Integer(AbstractFilter.GEOMETRY_OVERLAPS));
        DB2_SPATIAL_PREDICATES.put("ST_Touches",
            new Integer(AbstractFilter.GEOMETRY_TOUCHES));
        DB2_SPATIAL_PREDICATES.put("ST_Within",
            new Integer(AbstractFilter.GEOMETRY_WITHIN));
    }

    /**
     * Setup creates an encoder and an expression to encode
     *
     * @throws Exception DOCUMENT ME!
     */
    public void setUp() throws Exception {
        super.setUp();
        encoder = new SQLEncoderDB2();
        encoder.setSqlNameEscape("\"");
        ff = FilterFactoryFinder.createFilterFactory();
        bboxLiteral = null;
        types = new AttributeType[1];
        types[0] = AttributeTypeFactory.newAttributeType("Geom", Point.class);
        pointType = FeatureTypeFactory.newFeatureType(types, "testfeature");
        bboxLiteral = ff.createBBoxExpression(new Envelope(-76.0, -74.0, 41.0,
                    42.0));
        spatialColumn = ff.createAttributeExpression(pointType, "Geom");
        doubleLiteral = ff.createLiteralExpression(1);

        WKTReader reader = new WKTReader();
        LineString line = (LineString) reader.read("LINESTRING (0 0, 300 300)");
        geometryLiteral = ff.createLiteralExpression(line);
    }

    /**
     * Creates a geometry filter that uses the left and right geometries
     * created by the setup method.
     *
     * @param filterType a value defined by AbstractFilter
     *
     * @return a GeometryFilter
     *
     * @throws IllegalFilterException
     */
    private GeometryFilter createGeometryFilter(short filterType)
        throws IllegalFilterException {
        return createGeometryFilter(filterType, spatialColumn, bboxLiteral);
    }

    /**
     * Creates a geometry filter with the specified filter type, left and right
     * expressions.
     *
     * @param filterType
     * @param left
     * @param right
     *
     * @return
     *
     * @throws IllegalFilterException
     */
    private GeometryFilter createGeometryFilter(short filterType,
        Expression left, Expression right) throws IllegalFilterException {
        GeometryFilter gf = null;
        gf = ff.createGeometryFilter(filterType);
        gf.addLeftGeometry(left);
        gf.addRightGeometry(right);

        return gf;
    }

    /**
     * Creates a distance filter with the specified filter type, left and right
     * expressions and distance.
     *
     * @param filterType
     * @param left
     * @param right
     * @param distance
     *
     * @return
     *
     * @throws IllegalFilterException
     */
    private GeometryDistanceFilter createDistanceFilter(short filterType,
        Expression left, Expression right, double distance)
        throws IllegalFilterException {
        GeometryDistanceFilter gf = null;
        gf = (GeometryDistanceFilter) ff.createGeometryDistanceFilter(filterType);
        gf.addLeftGeometry(left);
        gf.addRightGeometry(right);
        gf.setDistance(distance);

        return gf;
    }

    public void testDistance()
        throws IllegalFilterException, SQLEncoderException {
        StringWriter output;
        GeometryDistanceFilter gf = null;

        encoder.setSelectivityClause(null);
        gf = createDistanceFilter(AbstractFilter.GEOMETRY_BEYOND,
                spatialColumn, geometryLiteral, 10.0);
        encoder.setSelectivityClause(null);
        output = new StringWriter();
        this.encoder.encode(output, gf);
        sqlString = output.toString();
        assertEquals("DWITHIN",
            "WHERE db2gse.ST_Distance(\"Geom\", db2gse.ST_Geometry('LINESTRING (0 0, 300 300)', 1)) > 10.0",
            sqlString);
        gf = createDistanceFilter(AbstractFilter.GEOMETRY_DWITHIN,
                spatialColumn, geometryLiteral, 10.0);
        encoder.setSelectivityClause(null);
        output = new StringWriter();
        this.encoder.encode(output, gf);
        sqlString = output.toString();
        assertEquals("DWITHIN",
            "WHERE db2gse.ST_Distance(\"Geom\", db2gse.ST_Geometry('LINESTRING (0 0, 300 300)', 1)) < 10.0",
            sqlString);
    }

    public void testDWITHIN() throws IllegalFilterException {
        StringWriter output;
        GeometryDistanceFilter gf = null;

        encoder.setSelectivityClause(null);

        gf = createDistanceFilter(AbstractFilter.GEOMETRY_DWITHIN,
                spatialColumn, geometryLiteral, 10.0);

        encoder.setSelectivityClause(null);
        output = new StringWriter();

        try {
            this.encoder.encode(output, gf);
            sqlString = output.toString();
            assertEquals("DWITHIN",
                "WHERE db2gse.ST_Distance(\"Geom\", db2gse.ST_Geometry('LINESTRING (0 0, 300 300)', 1)) < 10.0",
                sqlString);
        } catch (SQLEncoderException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }

    public void testVisit() throws SQLEncoderException, IllegalFilterException {
        // The visit method is tested by invoking "encode" in the 
        // super-class which will subsequent invoke visit in the
        // SQLEncoderDB2 object.
        StringWriter output;
        GeometryFilter gf;
        gf = createGeometryFilter(AbstractFilter.GEOMETRY_BBOX, spatialColumn,
                bboxLiteral);
        encoder.setSelectivityClause(null);
        output = new StringWriter();
        encoder.encode(output, gf);
        sqlString = output.toString();
        assertEquals("Check without selectivity",
            "WHERE db2gse.EnvelopesIntersect(\"Geom\", -76.0, 41.0, -74.0, 42.0, 1) = 1",
            sqlString);

        encoder.setSelectivityClause("SELECTIVITY 0.001");
        output = new StringWriter();
        encoder.encode(output, gf);
        sqlString = output.toString();
        assertEquals("Check with selectivity",
            "WHERE db2gse.EnvelopesIntersect(\"Geom\", -76.0, 41.0, -74.0, 42.0, 1) = 1 SELECTIVITY 0.001",
            sqlString);

        Set keys = DB2_SPATIAL_PREDICATES.keySet();
        Iterator it = keys.iterator();

        while (it.hasNext()) {
            String predicateName = (String) it.next();

            if (predicateName.equals("EnvelopesIntersect")) { // skip - already tested

                continue;
            }

            Integer type = (Integer) DB2_SPATIAL_PREDICATES.get(predicateName);
            short filterType = type.shortValue();

            //			System.out.println("Testing predicate '" + predicateName + "' type = " + filterType);
            gf = createGeometryFilter(filterType);
            encoder.setSelectivityClause(null);
            output = new StringWriter();
            encoder.encode(output, gf);
            sqlString = output.toString();

            String expected = "WHERE db2gse." + predicateName
                + "(\"Geom\", db2gse.ST_Geometry('POLYGON ((-76 41, -76 42, -74 42, -74 41, -76 41))', 1)) = 1";
            assertEquals("Testing predicate: " + predicateName, expected,
                sqlString);
        }

        gf = createGeometryFilter(AbstractFilter.GEOMETRY_BBOX, bboxLiteral,
                spatialColumn);

        encoder.setSelectivityClause(null);
        output = new StringWriter();

        try {
            encoder.encode(output, gf);
            fail("Filter with bboxLiteral on left should not be accepted");
        } catch (RuntimeException e) {
            // we should always come here
        }

        gf = createGeometryFilter(AbstractFilter.GEOMETRY_BBOX, spatialColumn,
                geometryLiteral);

        encoder.setSelectivityClause(null);
        output = new StringWriter();

        encoder.encode(output, gf);
        sqlString = output.toString();
        assertEquals("geometry literal",
            "WHERE db2gse.EnvelopesIntersect(\"Geom\", 0.0, 0.0, 300.0, 300.0, 1) = 1",
            sqlString);

        gf = createGeometryFilter(AbstractFilter.GEOMETRY_BBOX, spatialColumn,
                doubleLiteral);
        encoder.setSelectivityClause(null);
        output = new StringWriter();

        try {
            encoder.encode(output, gf);
            fail(
                "Filter with numeric literal on the right should not be accepted");
        } catch (RuntimeException e) {
            // we should always come here
        }
    }

    public void testGetCapabilities() throws IllegalFilterException {
        FilterCapabilities fc = encoder.getCapabilities();
        assertTrue("Check if BB filter supported",
            fc.fullySupports(createGeometryFilter(AbstractFilter.GEOMETRY_BBOX)));
    }
}
