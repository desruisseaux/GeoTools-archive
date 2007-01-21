/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, GeoTools Project Managment Committee (PMC)
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
package org.geotools.filter.v1_0;

import junit.framework.TestCase;
import org.opengis.filter.PropertyIsEqualTo;
import org.opengis.filter.expression.Literal;
import org.opengis.filter.expression.PropertyName;
import org.geotools.xml.Configuration;
import org.geotools.xml.Parser;


public class OGCFilterTest extends TestCase {
    Parser parser;

    protected void setUp() throws Exception {
        super.setUp();

        Configuration configuration = new OGCConfiguration();
        parser = new Parser(configuration);
    }

    public void testRun() throws Exception {
        Object thing = parser.parse(getClass().getResourceAsStream("test1.xml"));
        assertNotNull(thing);
        assertTrue(thing instanceof PropertyIsEqualTo);

        PropertyIsEqualTo equal = (PropertyIsEqualTo) thing;
        assertTrue(equal.getExpression1() instanceof PropertyName);
        assertTrue(equal.getExpression2() instanceof Literal);

        PropertyName name = (PropertyName) equal.getExpression1();
        assertEquals("testString", name.getPropertyName());

        Literal literal = (Literal) equal.getExpression2();
        assertEquals("2", literal.toString());
    }
}
