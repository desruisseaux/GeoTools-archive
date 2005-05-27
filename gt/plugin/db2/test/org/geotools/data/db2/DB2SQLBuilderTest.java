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

import com.vividsolutions.jts.geom.Point;
import org.geotools.data.db2.filter.SQLEncoderDB2;
import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;


/**
 * Exercise DB2SQLBuilder.
 *
 * @author David Adler - IBM Corporation
 */
public class DB2SQLBuilderTest extends DB2TestCase {
    private DB2SQLBuilder sqlBuilder = null;

    /**
     * Setup creates an encoder and SQLBuilder
     *
     * @throws Exception 
     */
    public void setUp() throws Exception {
        super.setUp();

        SQLEncoderDB2 encoder = new SQLEncoderDB2();
        encoder.setSqlNameEscape("\"");
        sqlBuilder = new DB2SQLBuilder(encoder, tabSchema, "Test");
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
        assertEquals("Encoding didn't match", "\"Geom\"..ST_AsText()",
            sb.toString());
    }
}
