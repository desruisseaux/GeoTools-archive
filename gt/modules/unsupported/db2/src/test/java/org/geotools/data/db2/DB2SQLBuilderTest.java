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

import com.vividsolutions.jts.geom.Point;
import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureSource;
import org.geotools.data.db2.filter.SQLEncoderDB2;
import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.FeatureType;
import org.geotools.filter.AttributeExpression;
import org.geotools.filter.CompareFilter;
import org.geotools.filter.FidFilter;
import org.geotools.filter.Filter;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.FilterFactoryFinder;
import org.geotools.filter.IllegalFilterException;
import org.geotools.filter.LikeFilter;
import org.geotools.filter.LiteralExpression;
import org.geotools.filter.SQLEncoderException;
import java.io.IOException;


/**
 * Exercise DB2SQLBuilder.
 *
 * @author David Adler - IBM Corporation
 * @source $URL$
 */
public class DB2SQLBuilderTest extends DB2TestCase {
    private DB2SQLBuilder sqlBuilder = null;
    private DB2DataStore dataStore = null;

    /**
     * Setup creates an encoder and SQLBuilder
     *
     * @throws Exception
     */
    public void setUp() throws Exception {
        super.setUp();
        this.dataStore = getDataStore();

        SQLEncoderDB2 encoder = new SQLEncoderDB2();
        encoder.setSqlNameEscape("\"");
        sqlBuilder = (DB2SQLBuilder) dataStore.getSqlBuilder("Places");
    }

    public void testFidFilter()
    throws IllegalFilterException, SQLEncoderException, IOException {
    String typeName = "Places";
    FeatureSource fs = dataStore.getFeatureSource("Places");
    FeatureType ft = fs.getSchema();
    FilterFactory ff = FilterFactoryFinder.createFilterFactory();
    FidFilter fidfilter = null;
    fidfilter = ff.createFidFilter("1");
    LikeFilter lf = ff.createLikeFilter();

    DefaultQuery query = new DefaultQuery("Places", fidfilter);
    Filter preFilter = sqlBuilder.getPreQueryFilter(query.getFilter());
    Filter postFilter = sqlBuilder.getPostQueryFilter(query.getFilter());
    String[] attrNames = new String[ft.getAttributeCount()];
    AttributeType[] attrTypes = new AttributeType[ft.getAttributeCount()];

    for (int i = 0; i < ft.getAttributeCount(); i++) {
        attrNames[i] = ft.getAttributeType(i).getName();
        attrTypes[i] = ft.getAttributeType(i);
    }

    String fidQuery = this.sqlBuilder.buildSQLQuery("Places",
            this.dataStore.getFIDMapper("Places"), attrTypes, preFilter);
    assertEquals("LIKE encoding failed",
        "SELECT \"Id\", \"Name\", DB2GSE.ST_AsText(\"Geom\") FROM \"Test\".\"Places\" WHERE (\"Id\" = 1)",
        fidQuery);
}
    
    public void testCompareFilter()
    throws IllegalFilterException, SQLEncoderException, IOException {
    String typeName = "Places";
    FeatureSource fs = dataStore.getFeatureSource("Places");
    FeatureType ft = fs.getSchema();
    FilterFactory ff = FilterFactoryFinder.createFilterFactory();
    CompareFilter cf = ff.createCompareFilter(CompareFilter.COMPARE_EQUALS);
    AttributeExpression column = ff.createAttributeExpression("Name");
    LiteralExpression compareValue = ff.createLiteralExpression("Zena");  
    cf.addLeftValue(column);
    cf.addRightValue(compareValue);

    String[] attrNames = new String[ft.getAttributeCount()];
    AttributeType[] attrTypes = new AttributeType[ft.getAttributeCount()];

    for (int i = 0; i < ft.getAttributeCount(); i++) {
        attrNames[i] = ft.getAttributeType(i).getName();
        attrTypes[i] = ft.getAttributeType(i);
    }
    DefaultQuery query = new DefaultQuery("Places", cf);
    Filter preFilter = sqlBuilder.getPreQueryFilter(query.getFilter());
    Filter postFilter = sqlBuilder.getPostQueryFilter(query.getFilter());
    String compareQuery = this.sqlBuilder.buildSQLQuery("Places",
            this.dataStore.getFIDMapper("Places"), attrTypes, preFilter);
    assertEquals("compare encoding failed",
        "SELECT \"Id\", \"Name\", DB2GSE.ST_AsText(\"Geom\") FROM \"Test\".\"Places\" WHERE \"Name\" = 'Zena'",
        compareQuery);
    
    column = ff.createAttributeExpression("Id");
    compareValue = ff.createLiteralExpression(2);   
    cf.addLeftValue(column);
    cf.addRightValue(compareValue);    
    query = new DefaultQuery("Places", cf);
    preFilter = sqlBuilder.getPreQueryFilter(query.getFilter());
    postFilter = sqlBuilder.getPostQueryFilter(query.getFilter());
    compareQuery = this.sqlBuilder.buildSQLQuery("Places",
            this.dataStore.getFIDMapper("Places"), attrTypes, preFilter);
    assertEquals("compare encoding failed",
        "SELECT \"Id\", \"Name\", DB2GSE.ST_AsText(\"Geom\") FROM \"Test\".\"Places\" WHERE \"Id\" = 2",
        compareQuery);    
}    
       public void testLikeFilter()
        throws IllegalFilterException, SQLEncoderException, IOException {
        String typeName = "Places";
        FeatureSource fs = dataStore.getFeatureSource("Places");
        FeatureType ft = fs.getSchema();
        FilterFactory ff = FilterFactoryFinder.createFilterFactory();
        LikeFilter lf = ff.createLikeFilter();
        AttributeExpression nameColumn = ff.createAttributeExpression("Name");
        String pattern = "s.met*s";       
        lf.setPattern(pattern, "*", ".", "\\");
        lf.setValue(nameColumn);

        DefaultQuery query = new DefaultQuery("Places", lf);
        Filter preFilter = sqlBuilder.getPreQueryFilter(query.getFilter());
        Filter postFilter = sqlBuilder.getPostQueryFilter(query.getFilter());
        String[] attrNames = new String[ft.getAttributeCount()];
        AttributeType[] attrTypes = new AttributeType[ft.getAttributeCount()];

        for (int i = 0; i < ft.getAttributeCount(); i++) {
            attrNames[i] = ft.getAttributeType(i).getName();
            attrTypes[i] = ft.getAttributeType(i);
        }

        String likeQuery = this.sqlBuilder.buildSQLQuery("Places",
                this.dataStore.getFIDMapper("Places"), attrTypes, preFilter);
        assertEquals("LIKE encoding failed",
            "SELECT \"Id\", \"Name\", DB2GSE.ST_AsText(\"Geom\") FROM \"Test\".\"Places\" WHERE \"Name\" LIKE 's_met%s'",
            likeQuery);
    }

    public void testSqlFrom() {
        StringBuffer sb;
        sb = new StringBuffer();
        sqlBuilder.sqlFrom(sb, "Test");
        assertEquals("Encoding didn't match", " FROM \"Test\".\"Test\"",
            sb.toString());
    }

    public void testSqlGeometryColumn() {
        String columnName = "Geom";
        Class geomClass = Point.class;
        AttributeType geomAttr = AttributeTypeFactory.newAttributeType(columnName,
                geomClass);

        StringBuffer sb = new StringBuffer();
        this.sqlBuilder.sqlGeometryColumn(sb, geomAttr);
        assertEquals("Encoding didn't match", "DB2GSE.ST_AsText(\"Geom\")",
            sb.toString());
    }
}
