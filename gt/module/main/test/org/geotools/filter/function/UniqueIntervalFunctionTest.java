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
 *
 */
package org.geotools.filter.function;

import junit.framework.*;
import org.geotools.data.DataUtilities;
import org.geotools.data.memory.MemoryDataStore;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.FeatureType;
import org.geotools.filter.Expression;
import org.geotools.filter.ExpressionBuilder;
import org.geotools.filter.ExpressionSAXParser;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.FilterFilter;
import org.geotools.filter.FunctionExpression;
import org.geotools.filter.TestFilterHandler;
import org.geotools.filter.function.UniqueIntervalFunction;
import org.geotools.filter.parser.ParseException;
import org.geotools.gml.GMLFilterDocument;
import org.geotools.gml.GMLFilterGeometry;
import org.geotools.resources.TestData;
import org.xml.sax.helpers.ParserAdapter;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;


/**
 * DOCUMENT ME!
 *
 * @author Cory Horner
 */
public class UniqueIntervalFunctionTest extends FunctionTestSupport {
    public UniqueIntervalFunctionTest(String testName) {
        super(testName);
    }

    protected void tearDown() throws java.lang.Exception {
    }

    public static junit.framework.Test suite() {
        junit.framework.TestSuite suite = new junit.framework.TestSuite(UniqueIntervalFunctionTest.class);

        return suite;
    }

    /**
     * Test of getName method, of class
     * org.geotools.filter.functions.UniqueIntervalFunction.
     */
    public void testInstance() {
        FunctionExpression equInt = FilterFactory.createFilterFactory()
                                                 .createFunctionExpression("UniqueInterval");
        assertNotNull(equInt);
    }

    /**
     * Test of getName method, of class
     * org.geotools.filter.functions.UniqueIntervalFunction.
     */
    public void testGetName() {
        FunctionExpression equInt = FilterFactory.createFilterFactory()
                                                 .createFunctionExpression("UniqueInterval");
        System.out.println("testGetName");
        assertEquals("UniqueInterval", equInt.getName());
    }

    /**
     * Test of setNumberOfClasses method, of class
     * org.geotools.filter.function.UniqueIntervalFunction.
     *
     * @throws Exception DOCUMENT ME!
     */
    public void testSetNumberOfClasses() throws Exception {
        System.out.println("testSetNumberOfClasses");

        Expression classes = (Expression) builder.parse(dataType, "3");
        Expression exp = (Expression) builder.parse(dataType, "foo");
        UniqueIntervalFunction func = (UniqueIntervalFunction) fac
            .createFunctionExpression("UniqueInterval");
        func.setArgs(new Expression[] { exp, classes });
        assertEquals(3, func.getNumberOfClasses());
        classes = (Expression) builder.parse(dataType, "12");
        func.setArgs(new Expression[] { exp, classes });
        assertEquals(12, func.getNumberOfClasses());
    }

    /**
     * Test of getValue method, of class
     * org.geotools.filter.function.UniqueIntervalFunction.
     *
     * @throws Exception DOCUMENT ME!
     */
    public void testGetValue() throws Exception {
        System.out.println("testGetValue");

        Expression classes = (Expression) builder.parse(dataType, "2");
        Expression exp = (Expression) builder.parse(dataType, "foo");
        FunctionExpression func = fac.createFunctionExpression("UniqueInterval");
        func.setArgs(new Expression[] { exp, classes });

        Object result = func.getValue(fc);
        assertNotNull(result);
        System.out.println(result);
    }
}
