/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.filter.function.math;

import junit.framework.TestCase;
import org.geotools.factory.FactoryConfigurationError;
import org.geotools.filter.expression.Expression;
import org.geotools.filter.FilterFactoryFinder;
import org.geotools.filter.FilterFactoryImpl;
import org.geotools.filter.expression.FunctionExpression;
import org.geotools.filter.expression.LiteralExpression;
import org.geotools.filter.LiteralExpressionImpl;


public class FilterFunction_Test extends TestCase {
    private LiteralExpressionImpl literal_1 = null;
    private LiteralExpression literal_m1;
    private LiteralExpression literal_2;
    private LiteralExpression literal_m2;
    private LiteralExpression literal_pi;
    private LiteralExpression literal_05pi;
    private FilterFactoryImpl filterFactory;

    protected void setUp() throws Exception {
        super.setUp();
        filterFactory = (FilterFactoryImpl) FilterFactoryFinder
            .createFilterFactory();
        literal_1 = (LiteralExpressionImpl) filterFactory
            .createLiteralExpression();
        literal_pi = filterFactory.createLiteralExpression();
        literal_05pi = filterFactory.createLiteralExpression();
        literal_m1 = filterFactory.createLiteralExpression();
        literal_2 = filterFactory.createLiteralExpression();
        literal_m2 = filterFactory.createLiteralExpression();

        literal_1.setLiteral(new Double(1));
        literal_m1.setLiteral(new Double(-1));
        literal_2.setLiteral(new Double(2));
        literal_m2.setLiteral(new Double(-2));
        literal_pi.setLiteral(new Double(Math.PI));
        literal_05pi.setLiteral(new Double(0.5 * Math.PI));
        assertEquals("Literal Expression 0.0", new Double(1.0),
            literal_1.getLiteral());
        assertEquals("Literal Expression pi", new Double(Math.PI),
            literal_pi.getLiteral());
        assertEquals("Literal Expression 05pi", new Double(0.5 * Math.PI),
            literal_05pi.getLiteral());
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testsin() {
        try {
            FunctionExpression sinFunction = filterFactory
                .createFunctionExpression("sin");
            assertEquals("Name is, ", "sin", sinFunction.getName());
            assertEquals("Number of arguments, ", 1, sinFunction.getArgCount());

            Expression[] expressions = new Expression[1];
            sinFunction.setArgs(expressions);
            expressions[0] = literal_1;

            double good0 = Math.sin(1.0);

            if (Double.isNaN(good0)) {
                assertTrue("sin of (1.0):",
                    Double.isNaN(
                        ((Double) sinFunction.getValue(null)).doubleValue()));
            } else {
                assertEquals("sin of (1.0):", (double) Math.sin(1.0),
                    ((Double) sinFunction.getValue(null)).doubleValue(), 0.00001);
            }

            expressions[0] = literal_m1;

            double good1 = Math.sin(-1.0);

            if (Double.isNaN(good1)) {
                assertTrue("sin of (-1.0):",
                    Double.isNaN(
                        ((Double) sinFunction.getValue(null)).doubleValue()));
            } else {
                assertEquals("sin of (-1.0):", (double) Math.sin(-1.0),
                    ((Double) sinFunction.getValue(null)).doubleValue(), 0.00001);
            }

            expressions[0] = literal_2;

            double good2 = Math.sin(2.0);

            if (Double.isNaN(good2)) {
                assertTrue("sin of (2.0):",
                    Double.isNaN(
                        ((Double) sinFunction.getValue(null)).doubleValue()));
            } else {
                assertEquals("sin of (2.0):", (double) Math.sin(2.0),
                    ((Double) sinFunction.getValue(null)).doubleValue(), 0.00001);
            }

            expressions[0] = literal_m2;

            double good3 = Math.sin(-2.0);

            if (Double.isNaN(good3)) {
                assertTrue("sin of (-2.0):",
                    Double.isNaN(
                        ((Double) sinFunction.getValue(null)).doubleValue()));
            } else {
                assertEquals("sin of (-2.0):", (double) Math.sin(-2.0),
                    ((Double) sinFunction.getValue(null)).doubleValue(), 0.00001);
            }

            expressions[0] = literal_pi;

            double good4 = Math.sin(3.141592653589793);

            if (Double.isNaN(good4)) {
                assertTrue("sin of (3.141592653589793):",
                    Double.isNaN(
                        ((Double) sinFunction.getValue(null)).doubleValue()));
            } else {
                assertEquals("sin of (3.141592653589793):",
                    (double) Math.sin(3.141592653589793),
                    ((Double) sinFunction.getValue(null)).doubleValue(), 0.00001);
            }

            expressions[0] = literal_05pi;

            double good5 = Math.sin(1.5707963267948966);

            if (Double.isNaN(good5)) {
                assertTrue("sin of (1.5707963267948966):",
                    Double.isNaN(
                        ((Double) sinFunction.getValue(null)).doubleValue()));
            } else {
                assertEquals("sin of (1.5707963267948966):",
                    (double) Math.sin(1.5707963267948966),
                    ((Double) sinFunction.getValue(null)).doubleValue(), 0.00001);
            }
        } catch (FactoryConfigurationError e) {
            e.printStackTrace();
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    public void testcos() {
        try {
            FunctionExpression cosFunction = filterFactory
                .createFunctionExpression("cos");
            assertEquals("Name is, ", "cos", cosFunction.getName());
            assertEquals("Number of arguments, ", 1, cosFunction.getArgCount());

            Expression[] expressions = new Expression[1];
            cosFunction.setArgs(expressions);
            expressions[0] = literal_1;

            double good0 = Math.cos(1.0);

            if (Double.isNaN(good0)) {
                assertTrue("cos of (1.0):",
                    Double.isNaN(
                        ((Double) cosFunction.getValue(null)).doubleValue()));
            } else {
                assertEquals("cos of (1.0):", (double) Math.cos(1.0),
                    ((Double) cosFunction.getValue(null)).doubleValue(), 0.00001);
            }

            expressions[0] = literal_m1;

            double good1 = Math.cos(-1.0);

            if (Double.isNaN(good1)) {
                assertTrue("cos of (-1.0):",
                    Double.isNaN(
                        ((Double) cosFunction.getValue(null)).doubleValue()));
            } else {
                assertEquals("cos of (-1.0):", (double) Math.cos(-1.0),
                    ((Double) cosFunction.getValue(null)).doubleValue(), 0.00001);
            }

            expressions[0] = literal_2;

            double good2 = Math.cos(2.0);

            if (Double.isNaN(good2)) {
                assertTrue("cos of (2.0):",
                    Double.isNaN(
                        ((Double) cosFunction.getValue(null)).doubleValue()));
            } else {
                assertEquals("cos of (2.0):", (double) Math.cos(2.0),
                    ((Double) cosFunction.getValue(null)).doubleValue(), 0.00001);
            }

            expressions[0] = literal_m2;

            double good3 = Math.cos(-2.0);

            if (Double.isNaN(good3)) {
                assertTrue("cos of (-2.0):",
                    Double.isNaN(
                        ((Double) cosFunction.getValue(null)).doubleValue()));
            } else {
                assertEquals("cos of (-2.0):", (double) Math.cos(-2.0),
                    ((Double) cosFunction.getValue(null)).doubleValue(), 0.00001);
            }

            expressions[0] = literal_pi;

            double good4 = Math.cos(3.141592653589793);

            if (Double.isNaN(good4)) {
                assertTrue("cos of (3.141592653589793):",
                    Double.isNaN(
                        ((Double) cosFunction.getValue(null)).doubleValue()));
            } else {
                assertEquals("cos of (3.141592653589793):",
                    (double) Math.cos(3.141592653589793),
                    ((Double) cosFunction.getValue(null)).doubleValue(), 0.00001);
            }

            expressions[0] = literal_05pi;

            double good5 = Math.cos(1.5707963267948966);

            if (Double.isNaN(good5)) {
                assertTrue("cos of (1.5707963267948966):",
                    Double.isNaN(
                        ((Double) cosFunction.getValue(null)).doubleValue()));
            } else {
                assertEquals("cos of (1.5707963267948966):",
                    (double) Math.cos(1.5707963267948966),
                    ((Double) cosFunction.getValue(null)).doubleValue(), 0.00001);
            }
        } catch (FactoryConfigurationError e) {
            e.printStackTrace();
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    public void testtan() {
        try {
            FunctionExpression tanFunction = filterFactory
                .createFunctionExpression("tan");
            assertEquals("Name is, ", "tan", tanFunction.getName());
            assertEquals("Number of arguments, ", 1, tanFunction.getArgCount());

            Expression[] expressions = new Expression[1];
            tanFunction.setArgs(expressions);
            expressions[0] = literal_1;

            double good0 = Math.tan(1.0);

            if (Double.isNaN(good0)) {
                assertTrue("tan of (1.0):",
                    Double.isNaN(
                        ((Double) tanFunction.getValue(null)).doubleValue()));
            } else {
                assertEquals("tan of (1.0):", (double) Math.tan(1.0),
                    ((Double) tanFunction.getValue(null)).doubleValue(), 0.00001);
            }

            expressions[0] = literal_m1;

            double good1 = Math.tan(-1.0);

            if (Double.isNaN(good1)) {
                assertTrue("tan of (-1.0):",
                    Double.isNaN(
                        ((Double) tanFunction.getValue(null)).doubleValue()));
            } else {
                assertEquals("tan of (-1.0):", (double) Math.tan(-1.0),
                    ((Double) tanFunction.getValue(null)).doubleValue(), 0.00001);
            }

            expressions[0] = literal_2;

            double good2 = Math.tan(2.0);

            if (Double.isNaN(good2)) {
                assertTrue("tan of (2.0):",
                    Double.isNaN(
                        ((Double) tanFunction.getValue(null)).doubleValue()));
            } else {
                assertEquals("tan of (2.0):", (double) Math.tan(2.0),
                    ((Double) tanFunction.getValue(null)).doubleValue(), 0.00001);
            }

            expressions[0] = literal_m2;

            double good3 = Math.tan(-2.0);

            if (Double.isNaN(good3)) {
                assertTrue("tan of (-2.0):",
                    Double.isNaN(
                        ((Double) tanFunction.getValue(null)).doubleValue()));
            } else {
                assertEquals("tan of (-2.0):", (double) Math.tan(-2.0),
                    ((Double) tanFunction.getValue(null)).doubleValue(), 0.00001);
            }

            expressions[0] = literal_pi;

            double good4 = Math.tan(3.141592653589793);

            if (Double.isNaN(good4)) {
                assertTrue("tan of (3.141592653589793):",
                    Double.isNaN(
                        ((Double) tanFunction.getValue(null)).doubleValue()));
            } else {
                assertEquals("tan of (3.141592653589793):",
                    (double) Math.tan(3.141592653589793),
                    ((Double) tanFunction.getValue(null)).doubleValue(), 0.00001);
            }

            expressions[0] = literal_05pi;

            double good5 = Math.tan(1.5707963267948966);

            if (Double.isNaN(good5)) {
                assertTrue("tan of (1.5707963267948966):",
                    Double.isNaN(
                        ((Double) tanFunction.getValue(null)).doubleValue()));
            } else {
                assertEquals("tan of (1.5707963267948966):",
                    (double) Math.tan(1.5707963267948966),
                    ((Double) tanFunction.getValue(null)).doubleValue(), 0.00001);
            }
        } catch (FactoryConfigurationError e) {
            e.printStackTrace();
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    public void testatan2() {
        try {
            FunctionExpression atan2Function = filterFactory
                .createFunctionExpression("atan2");
            assertEquals("Name is, ", "atan2", atan2Function.getName());
            assertEquals("Number of arguments, ", 2, atan2Function.getArgCount());

            Expression[] expressions = new Expression[2];
            atan2Function.setArgs(expressions);
            expressions[0] = literal_1;
            expressions[1] = literal_m1;

            double good0 = Math.atan2(1.0, -1.0);

            if (Double.isNaN(good0)) {
                assertTrue("atan2 of (1.0,-1.0):",
                    Double.isNaN(
                        ((Double) atan2Function.getValue(null)).doubleValue()));
            } else {
                assertEquals("atan2 of (1.0,-1.0):",
                    (double) Math.atan2(1.0, -1.0),
                    ((Double) atan2Function.getValue(null)).doubleValue(),
                    0.00001);
            }

            expressions[0] = literal_m1;
            expressions[1] = literal_2;

            double good1 = Math.atan2(-1.0, 2.0);

            if (Double.isNaN(good1)) {
                assertTrue("atan2 of (-1.0,2.0):",
                    Double.isNaN(
                        ((Double) atan2Function.getValue(null)).doubleValue()));
            } else {
                assertEquals("atan2 of (-1.0,2.0):",
                    (double) Math.atan2(-1.0, 2.0),
                    ((Double) atan2Function.getValue(null)).doubleValue(),
                    0.00001);
            }

            expressions[0] = literal_2;
            expressions[1] = literal_m2;

            double good2 = Math.atan2(2.0, -2.0);

            if (Double.isNaN(good2)) {
                assertTrue("atan2 of (2.0,-2.0):",
                    Double.isNaN(
                        ((Double) atan2Function.getValue(null)).doubleValue()));
            } else {
                assertEquals("atan2 of (2.0,-2.0):",
                    (double) Math.atan2(2.0, -2.0),
                    ((Double) atan2Function.getValue(null)).doubleValue(),
                    0.00001);
            }

            expressions[0] = literal_m2;
            expressions[1] = literal_pi;

            double good3 = Math.atan2(-2.0, 3.141592653589793);

            if (Double.isNaN(good3)) {
                assertTrue("atan2 of (-2.0,3.141592653589793):",
                    Double.isNaN(
                        ((Double) atan2Function.getValue(null)).doubleValue()));
            } else {
                assertEquals("atan2 of (-2.0,3.141592653589793):",
                    (double) Math.atan2(-2.0, 3.141592653589793),
                    ((Double) atan2Function.getValue(null)).doubleValue(),
                    0.00001);
            }

            expressions[0] = literal_pi;
            expressions[1] = literal_05pi;

            double good4 = Math.atan2(3.141592653589793, 1.5707963267948966);

            if (Double.isNaN(good4)) {
                assertTrue("atan2 of (3.141592653589793,1.5707963267948966):",
                    Double.isNaN(
                        ((Double) atan2Function.getValue(null)).doubleValue()));
            } else {
                assertEquals("atan2 of (3.141592653589793,1.5707963267948966):",
                    (double) Math.atan2(3.141592653589793, 1.5707963267948966),
                    ((Double) atan2Function.getValue(null)).doubleValue(),
                    0.00001);
            }

            expressions[0] = literal_05pi;
            expressions[1] = literal_1;

            double good5 = Math.atan2(1.5707963267948966, 1.0);

            if (Double.isNaN(good5)) {
                assertTrue("atan2 of (1.5707963267948966,1.0):",
                    Double.isNaN(
                        ((Double) atan2Function.getValue(null)).doubleValue()));
            } else {
                assertEquals("atan2 of (1.5707963267948966,1.0):",
                    (double) Math.atan2(1.5707963267948966, 1.0),
                    ((Double) atan2Function.getValue(null)).doubleValue(),
                    0.00001);
            }
        } catch (FactoryConfigurationError e) {
            e.printStackTrace();
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    public void testsqrt() {
        try {
            FunctionExpression sqrtFunction = filterFactory
                .createFunctionExpression("sqrt");
            assertEquals("Name is, ", "sqrt", sqrtFunction.getName());
            assertEquals("Number of arguments, ", 1, sqrtFunction.getArgCount());

            Expression[] expressions = new Expression[1];
            sqrtFunction.setArgs(expressions);
            expressions[0] = literal_1;

            double good0 = Math.sqrt(1.0);

            if (Double.isNaN(good0)) {
                assertTrue("sqrt of (1.0):",
                    Double.isNaN(
                        ((Double) sqrtFunction.getValue(null)).doubleValue()));
            } else {
                assertEquals("sqrt of (1.0):", (double) Math.sqrt(1.0),
                    ((Double) sqrtFunction.getValue(null)).doubleValue(),
                    0.00001);
            }

            expressions[0] = literal_m1;

            double good1 = Math.sqrt(-1.0);

            if (Double.isNaN(good1)) {
                assertTrue("sqrt of (-1.0):",
                    Double.isNaN(
                        ((Double) sqrtFunction.getValue(null)).doubleValue()));
            } else {
                assertEquals("sqrt of (-1.0):", (double) Math.sqrt(-1.0),
                    ((Double) sqrtFunction.getValue(null)).doubleValue(),
                    0.00001);
            }

            expressions[0] = literal_2;

            double good2 = Math.sqrt(2.0);

            if (Double.isNaN(good2)) {
                assertTrue("sqrt of (2.0):",
                    Double.isNaN(
                        ((Double) sqrtFunction.getValue(null)).doubleValue()));
            } else {
                assertEquals("sqrt of (2.0):", (double) Math.sqrt(2.0),
                    ((Double) sqrtFunction.getValue(null)).doubleValue(),
                    0.00001);
            }

            expressions[0] = literal_m2;

            double good3 = Math.sqrt(-2.0);

            if (Double.isNaN(good3)) {
                assertTrue("sqrt of (-2.0):",
                    Double.isNaN(
                        ((Double) sqrtFunction.getValue(null)).doubleValue()));
            } else {
                assertEquals("sqrt of (-2.0):", (double) Math.sqrt(-2.0),
                    ((Double) sqrtFunction.getValue(null)).doubleValue(),
                    0.00001);
            }

            expressions[0] = literal_pi;

            double good4 = Math.sqrt(3.141592653589793);

            if (Double.isNaN(good4)) {
                assertTrue("sqrt of (3.141592653589793):",
                    Double.isNaN(
                        ((Double) sqrtFunction.getValue(null)).doubleValue()));
            } else {
                assertEquals("sqrt of (3.141592653589793):",
                    (double) Math.sqrt(3.141592653589793),
                    ((Double) sqrtFunction.getValue(null)).doubleValue(),
                    0.00001);
            }

            expressions[0] = literal_05pi;

            double good5 = Math.sqrt(1.5707963267948966);

            if (Double.isNaN(good5)) {
                assertTrue("sqrt of (1.5707963267948966):",
                    Double.isNaN(
                        ((Double) sqrtFunction.getValue(null)).doubleValue()));
            } else {
                assertEquals("sqrt of (1.5707963267948966):",
                    (double) Math.sqrt(1.5707963267948966),
                    ((Double) sqrtFunction.getValue(null)).doubleValue(),
                    0.00001);
            }
        } catch (FactoryConfigurationError e) {
            e.printStackTrace();
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    public void testpow() {
        try {
            FunctionExpression powFunction = filterFactory
                .createFunctionExpression("pow");
            assertEquals("Name is, ", "pow", powFunction.getName());
            assertEquals("Number of arguments, ", 2, powFunction.getArgCount());

            Expression[] expressions = new Expression[2];
            powFunction.setArgs(expressions);
            expressions[0] = literal_1;
            expressions[1] = literal_m1;

            double good0 = Math.pow(1.0, -1.0);

            if (Double.isNaN(good0)) {
                assertTrue("pow of (1.0,-1.0):",
                    Double.isNaN(
                        ((Double) powFunction.getValue(null)).doubleValue()));
            } else {
                assertEquals("pow of (1.0,-1.0):",
                    (double) Math.pow(1.0, -1.0),
                    ((Double) powFunction.getValue(null)).doubleValue(), 0.00001);
            }

            expressions[0] = literal_m1;
            expressions[1] = literal_2;

            double good1 = Math.pow(-1.0, 2.0);

            if (Double.isNaN(good1)) {
                assertTrue("pow of (-1.0,2.0):",
                    Double.isNaN(
                        ((Double) powFunction.getValue(null)).doubleValue()));
            } else {
                assertEquals("pow of (-1.0,2.0):",
                    (double) Math.pow(-1.0, 2.0),
                    ((Double) powFunction.getValue(null)).doubleValue(), 0.00001);
            }

            expressions[0] = literal_2;
            expressions[1] = literal_m2;

            double good2 = Math.pow(2.0, -2.0);

            if (Double.isNaN(good2)) {
                assertTrue("pow of (2.0,-2.0):",
                    Double.isNaN(
                        ((Double) powFunction.getValue(null)).doubleValue()));
            } else {
                assertEquals("pow of (2.0,-2.0):",
                    (double) Math.pow(2.0, -2.0),
                    ((Double) powFunction.getValue(null)).doubleValue(), 0.00001);
            }

            expressions[0] = literal_m2;
            expressions[1] = literal_pi;

            double good3 = Math.pow(-2.0, 3.141592653589793);

            if (Double.isNaN(good3)) {
                assertTrue("pow of (-2.0,3.141592653589793):",
                    Double.isNaN(
                        ((Double) powFunction.getValue(null)).doubleValue()));
            } else {
                assertEquals("pow of (-2.0,3.141592653589793):",
                    (double) Math.pow(-2.0, 3.141592653589793),
                    ((Double) powFunction.getValue(null)).doubleValue(), 0.00001);
            }

            expressions[0] = literal_pi;
            expressions[1] = literal_05pi;

            double good4 = Math.pow(3.141592653589793, 1.5707963267948966);

            if (Double.isNaN(good4)) {
                assertTrue("pow of (3.141592653589793,1.5707963267948966):",
                    Double.isNaN(
                        ((Double) powFunction.getValue(null)).doubleValue()));
            } else {
                assertEquals("pow of (3.141592653589793,1.5707963267948966):",
                    (double) Math.pow(3.141592653589793, 1.5707963267948966),
                    ((Double) powFunction.getValue(null)).doubleValue(), 0.00001);
            }

            expressions[0] = literal_05pi;
            expressions[1] = literal_1;

            double good5 = Math.pow(1.5707963267948966, 1.0);

            if (Double.isNaN(good5)) {
                assertTrue("pow of (1.5707963267948966,1.0):",
                    Double.isNaN(
                        ((Double) powFunction.getValue(null)).doubleValue()));
            } else {
                assertEquals("pow of (1.5707963267948966,1.0):",
                    (double) Math.pow(1.5707963267948966, 1.0),
                    ((Double) powFunction.getValue(null)).doubleValue(), 0.00001);
            }
        } catch (FactoryConfigurationError e) {
            e.printStackTrace();
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    public void testmin() {
        try {
            FunctionExpression minFunction = filterFactory
                .createFunctionExpression("min_4");
            assertEquals("Name is, ", "min_4", minFunction.getName());
            assertEquals("Number of arguments, ", 2, minFunction.getArgCount());

            Expression[] expressions = new Expression[2];
            minFunction.setArgs(expressions);
            expressions[0] = literal_1;
            expressions[1] = literal_m1;
            assertEquals("min of (1.0,-1.0):", (long) Math.min(1.0, -1.0),
                ((Long) minFunction.getValue(null)).longValue(), 0.00001);
            expressions[0] = literal_m1;
            expressions[1] = literal_2;
            assertEquals("min of (-1.0,2.0):", (long) Math.min(-1.0, 2.0),
                ((Long) minFunction.getValue(null)).longValue(), 0.00001);
            expressions[0] = literal_2;
            expressions[1] = literal_m2;
            assertEquals("min of (2.0,-2.0):", (long) Math.min(2.0, -2.0),
                ((Long) minFunction.getValue(null)).longValue(), 0.00001);
            expressions[0] = literal_m2;
            expressions[1] = literal_pi;
            assertEquals("min of (-2.0,3.141592653589793):",
                (long) Math.min(-2.0, 3.141592653589793),
                ((Long) minFunction.getValue(null)).longValue(), 0.00001);
            expressions[0] = literal_pi;
            expressions[1] = literal_05pi;
            assertEquals("min of (3.141592653589793,1.5707963267948966):",
                (long) Math.min(3.141592653589793, 1.5707963267948966),
                ((Long) minFunction.getValue(null)).longValue(), 0.00001);
            expressions[0] = literal_05pi;
            expressions[1] = literal_1;
            assertEquals("min of (1.5707963267948966,1.0):",
                (long) Math.min(1.5707963267948966, 1.0),
                ((Long) minFunction.getValue(null)).longValue(), 0.00001);
        } catch (FactoryConfigurationError e) {
            e.printStackTrace();
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    public void testmin_2() {
        try {
            FunctionExpression min_2Function = filterFactory
                .createFunctionExpression("min");
            assertEquals("Name is, ", "min", min_2Function.getName());
            assertEquals("Number of arguments, ", 2, min_2Function.getArgCount());

            Expression[] expressions = new Expression[2];
            min_2Function.setArgs(expressions);
            expressions[0] = literal_1;
            expressions[1] = literal_m1;

            double good0 = Math.min(1.0, -1.0);

            if (Double.isNaN(good0)) {
                assertTrue("min of (1.0,-1.0):",
                    Double.isNaN(
                        ((Double) min_2Function.getValue(null)).doubleValue()));
            } else {
                assertEquals("min of (1.0,-1.0):",
                    (double) Math.min(1.0, -1.0),
                    ((Double) min_2Function.getValue(null)).doubleValue(),
                    0.00001);
            }

            expressions[0] = literal_m1;
            expressions[1] = literal_2;

            double good1 = Math.min(-1.0, 2.0);

            if (Double.isNaN(good1)) {
                assertTrue("min of (-1.0,2.0):",
                    Double.isNaN(
                        ((Double) min_2Function.getValue(null)).doubleValue()));
            } else {
                assertEquals("min of (-1.0,2.0):",
                    (double) Math.min(-1.0, 2.0),
                    ((Double) min_2Function.getValue(null)).doubleValue(),
                    0.00001);
            }

            expressions[0] = literal_2;
            expressions[1] = literal_m2;

            double good2 = Math.min(2.0, -2.0);

            if (Double.isNaN(good2)) {
                assertTrue("min of (2.0,-2.0):",
                    Double.isNaN(
                        ((Double) min_2Function.getValue(null)).doubleValue()));
            } else {
                assertEquals("min of (2.0,-2.0):",
                    (double) Math.min(2.0, -2.0),
                    ((Double) min_2Function.getValue(null)).doubleValue(),
                    0.00001);
            }

            expressions[0] = literal_m2;
            expressions[1] = literal_pi;

            double good3 = Math.min(-2.0, 3.141592653589793);

            if (Double.isNaN(good3)) {
                assertTrue("min of (-2.0,3.141592653589793):",
                    Double.isNaN(
                        ((Double) min_2Function.getValue(null)).doubleValue()));
            } else {
                assertEquals("min of (-2.0,3.141592653589793):",
                    (double) Math.min(-2.0, 3.141592653589793),
                    ((Double) min_2Function.getValue(null)).doubleValue(),
                    0.00001);
            }

            expressions[0] = literal_pi;
            expressions[1] = literal_05pi;

            double good4 = Math.min(3.141592653589793, 1.5707963267948966);

            if (Double.isNaN(good4)) {
                assertTrue("min of (3.141592653589793,1.5707963267948966):",
                    Double.isNaN(
                        ((Double) min_2Function.getValue(null)).doubleValue()));
            } else {
                assertEquals("min of (3.141592653589793,1.5707963267948966):",
                    (double) Math.min(3.141592653589793, 1.5707963267948966),
                    ((Double) min_2Function.getValue(null)).doubleValue(),
                    0.00001);
            }

            expressions[0] = literal_05pi;
            expressions[1] = literal_1;

            double good5 = Math.min(1.5707963267948966, 1.0);

            if (Double.isNaN(good5)) {
                assertTrue("min of (1.5707963267948966,1.0):",
                    Double.isNaN(
                        ((Double) min_2Function.getValue(null)).doubleValue()));
            } else {
                assertEquals("min of (1.5707963267948966,1.0):",
                    (double) Math.min(1.5707963267948966, 1.0),
                    ((Double) min_2Function.getValue(null)).doubleValue(),
                    0.00001);
            }
        } catch (FactoryConfigurationError e) {
            e.printStackTrace();
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    public void testmin_3() {
        try {
            FunctionExpression min_3Function = filterFactory
                .createFunctionExpression("min_2");
            assertEquals("Name is, ", "min_2", min_3Function.getName());
            assertEquals("Number of arguments, ", 2, min_3Function.getArgCount());

            Expression[] expressions = new Expression[2];
            min_3Function.setArgs(expressions);
            expressions[0] = literal_1;
            expressions[1] = literal_m1;
            assertEquals("min of (1.0,-1.0):", (float) Math.min(1.0, -1.0),
                ((Float) min_3Function.getValue(null)).floatValue(), 0.00001);
            expressions[0] = literal_m1;
            expressions[1] = literal_2;
            assertEquals("min of (-1.0,2.0):", (float) Math.min(-1.0, 2.0),
                ((Float) min_3Function.getValue(null)).floatValue(), 0.00001);
            expressions[0] = literal_2;
            expressions[1] = literal_m2;
            assertEquals("min of (2.0,-2.0):", (float) Math.min(2.0, -2.0),
                ((Float) min_3Function.getValue(null)).floatValue(), 0.00001);
            expressions[0] = literal_m2;
            expressions[1] = literal_pi;
            assertEquals("min of (-2.0,3.141592653589793):",
                (float) Math.min(-2.0, 3.141592653589793),
                ((Float) min_3Function.getValue(null)).floatValue(), 0.00001);
            expressions[0] = literal_pi;
            expressions[1] = literal_05pi;
            assertEquals("min of (3.141592653589793,1.5707963267948966):",
                (float) Math.min(3.141592653589793, 1.5707963267948966),
                ((Float) min_3Function.getValue(null)).floatValue(), 0.00001);
            expressions[0] = literal_05pi;
            expressions[1] = literal_1;
            assertEquals("min of (1.5707963267948966,1.0):",
                (float) Math.min(1.5707963267948966, 1.0),
                ((Float) min_3Function.getValue(null)).floatValue(), 0.00001);
        } catch (FactoryConfigurationError e) {
            e.printStackTrace();
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    public void testmin_4() {
        try {
            FunctionExpression min_4Function = filterFactory
                .createFunctionExpression("min_3");
            assertEquals("Name is, ", "min_3", min_4Function.getName());
            assertEquals("Number of arguments, ", 2, min_4Function.getArgCount());

            Expression[] expressions = new Expression[2];
            min_4Function.setArgs(expressions);
            expressions[0] = literal_1;
            expressions[1] = literal_m1;
            assertEquals("min of (1.0,-1.0):", (int) Math.min(1.0, -1.0),
                ((Integer) min_4Function.getValue(null)).intValue(), 0.00001);
            expressions[0] = literal_m1;
            expressions[1] = literal_2;
            assertEquals("min of (-1.0,2.0):", (int) Math.min(-1.0, 2.0),
                ((Integer) min_4Function.getValue(null)).intValue(), 0.00001);
            expressions[0] = literal_2;
            expressions[1] = literal_m2;
            assertEquals("min of (2.0,-2.0):", (int) Math.min(2.0, -2.0),
                ((Integer) min_4Function.getValue(null)).intValue(), 0.00001);
            expressions[0] = literal_m2;
            expressions[1] = literal_pi;
            assertEquals("min of (-2.0,3.141592653589793):",
                (int) Math.min(-2.0, 3.141592653589793),
                ((Integer) min_4Function.getValue(null)).intValue(), 0.00001);
            expressions[0] = literal_pi;
            expressions[1] = literal_05pi;
            assertEquals("min of (3.141592653589793,1.5707963267948966):",
                (int) Math.min(3.141592653589793, 1.5707963267948966),
                ((Integer) min_4Function.getValue(null)).intValue(), 0.00001);
            expressions[0] = literal_05pi;
            expressions[1] = literal_1;
            assertEquals("min of (1.5707963267948966,1.0):",
                (int) Math.min(1.5707963267948966, 1.0),
                ((Integer) min_4Function.getValue(null)).intValue(), 0.00001);
        } catch (FactoryConfigurationError e) {
            e.printStackTrace();
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    public void testmax() {
        try {
            FunctionExpression maxFunction = filterFactory
                .createFunctionExpression("max");
            assertEquals("Name is, ", "max", maxFunction.getName());
            assertEquals("Number of arguments, ", 2, maxFunction.getArgCount());

            Expression[] expressions = new Expression[2];
            maxFunction.setArgs(expressions);
            expressions[0] = literal_1;
            expressions[1] = literal_m1;

            double good0 = Math.max(1.0, -1.0);

            if (Double.isNaN(good0)) {
                assertTrue("max of (1.0,-1.0):",
                    Double.isNaN(
                        ((Double) maxFunction.getValue(null)).doubleValue()));
            } else {
                assertEquals("max of (1.0,-1.0):",
                    (double) Math.max(1.0, -1.0),
                    ((Double) maxFunction.getValue(null)).doubleValue(), 0.00001);
            }

            expressions[0] = literal_m1;
            expressions[1] = literal_2;

            double good1 = Math.max(-1.0, 2.0);

            if (Double.isNaN(good1)) {
                assertTrue("max of (-1.0,2.0):",
                    Double.isNaN(
                        ((Double) maxFunction.getValue(null)).doubleValue()));
            } else {
                assertEquals("max of (-1.0,2.0):",
                    (double) Math.max(-1.0, 2.0),
                    ((Double) maxFunction.getValue(null)).doubleValue(), 0.00001);
            }

            expressions[0] = literal_2;
            expressions[1] = literal_m2;

            double good2 = Math.max(2.0, -2.0);

            if (Double.isNaN(good2)) {
                assertTrue("max of (2.0,-2.0):",
                    Double.isNaN(
                        ((Double) maxFunction.getValue(null)).doubleValue()));
            } else {
                assertEquals("max of (2.0,-2.0):",
                    (double) Math.max(2.0, -2.0),
                    ((Double) maxFunction.getValue(null)).doubleValue(), 0.00001);
            }

            expressions[0] = literal_m2;
            expressions[1] = literal_pi;

            double good3 = Math.max(-2.0, 3.141592653589793);

            if (Double.isNaN(good3)) {
                assertTrue("max of (-2.0,3.141592653589793):",
                    Double.isNaN(
                        ((Double) maxFunction.getValue(null)).doubleValue()));
            } else {
                assertEquals("max of (-2.0,3.141592653589793):",
                    (double) Math.max(-2.0, 3.141592653589793),
                    ((Double) maxFunction.getValue(null)).doubleValue(), 0.00001);
            }

            expressions[0] = literal_pi;
            expressions[1] = literal_05pi;

            double good4 = Math.max(3.141592653589793, 1.5707963267948966);

            if (Double.isNaN(good4)) {
                assertTrue("max of (3.141592653589793,1.5707963267948966):",
                    Double.isNaN(
                        ((Double) maxFunction.getValue(null)).doubleValue()));
            } else {
                assertEquals("max of (3.141592653589793,1.5707963267948966):",
                    (double) Math.max(3.141592653589793, 1.5707963267948966),
                    ((Double) maxFunction.getValue(null)).doubleValue(), 0.00001);
            }

            expressions[0] = literal_05pi;
            expressions[1] = literal_1;

            double good5 = Math.max(1.5707963267948966, 1.0);

            if (Double.isNaN(good5)) {
                assertTrue("max of (1.5707963267948966,1.0):",
                    Double.isNaN(
                        ((Double) maxFunction.getValue(null)).doubleValue()));
            } else {
                assertEquals("max of (1.5707963267948966,1.0):",
                    (double) Math.max(1.5707963267948966, 1.0),
                    ((Double) maxFunction.getValue(null)).doubleValue(), 0.00001);
            }
        } catch (FactoryConfigurationError e) {
            e.printStackTrace();
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    public void testmax_2() {
        try {
            FunctionExpression max_2Function = filterFactory
                .createFunctionExpression("max_2");
            assertEquals("Name is, ", "max_2", max_2Function.getName());
            assertEquals("Number of arguments, ", 2, max_2Function.getArgCount());

            Expression[] expressions = new Expression[2];
            max_2Function.setArgs(expressions);
            expressions[0] = literal_1;
            expressions[1] = literal_m1;
            assertEquals("max of (1.0,-1.0):", (float) Math.max(1.0, -1.0),
                ((Float) max_2Function.getValue(null)).floatValue(), 0.00001);
            expressions[0] = literal_m1;
            expressions[1] = literal_2;
            assertEquals("max of (-1.0,2.0):", (float) Math.max(-1.0, 2.0),
                ((Float) max_2Function.getValue(null)).floatValue(), 0.00001);
            expressions[0] = literal_2;
            expressions[1] = literal_m2;
            assertEquals("max of (2.0,-2.0):", (float) Math.max(2.0, -2.0),
                ((Float) max_2Function.getValue(null)).floatValue(), 0.00001);
            expressions[0] = literal_m2;
            expressions[1] = literal_pi;
            assertEquals("max of (-2.0,3.141592653589793):",
                (float) Math.max(-2.0, 3.141592653589793),
                ((Float) max_2Function.getValue(null)).floatValue(), 0.00001);
            expressions[0] = literal_pi;
            expressions[1] = literal_05pi;
            assertEquals("max of (3.141592653589793,1.5707963267948966):",
                (float) Math.max(3.141592653589793, 1.5707963267948966),
                ((Float) max_2Function.getValue(null)).floatValue(), 0.00001);
            expressions[0] = literal_05pi;
            expressions[1] = literal_1;
            assertEquals("max of (1.5707963267948966,1.0):",
                (float) Math.max(1.5707963267948966, 1.0),
                ((Float) max_2Function.getValue(null)).floatValue(), 0.00001);
        } catch (FactoryConfigurationError e) {
            e.printStackTrace();
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    public void testmax_3() {
        try {
            FunctionExpression max_3Function = filterFactory
                .createFunctionExpression("max_3");
            assertEquals("Name is, ", "max_3", max_3Function.getName());
            assertEquals("Number of arguments, ", 2, max_3Function.getArgCount());

            Expression[] expressions = new Expression[2];
            max_3Function.setArgs(expressions);
            expressions[0] = literal_1;
            expressions[1] = literal_m1;
            assertEquals("max of (1.0,-1.0):", (int) Math.max(1.0, -1.0),
                ((Integer) max_3Function.getValue(null)).intValue(), 0.00001);
            expressions[0] = literal_m1;
            expressions[1] = literal_2;
            assertEquals("max of (-1.0,2.0):", (int) Math.max(-1.0, 2.0),
                ((Integer) max_3Function.getValue(null)).intValue(), 0.00001);
            expressions[0] = literal_2;
            expressions[1] = literal_m2;
            assertEquals("max of (2.0,-2.0):", (int) Math.max(2.0, -2.0),
                ((Integer) max_3Function.getValue(null)).intValue(), 0.00001);
            expressions[0] = literal_m2;
            expressions[1] = literal_pi;
            assertEquals("max of (-2.0,3.141592653589793):",
                (int) Math.max(-2.0, 3.141592653589793),
                ((Integer) max_3Function.getValue(null)).intValue(), 0.00001);
            expressions[0] = literal_pi;
            expressions[1] = literal_05pi;
            assertEquals("max of (3.141592653589793,1.5707963267948966):",
                (int) Math.max(3.141592653589793, 1.5707963267948966),
                ((Integer) max_3Function.getValue(null)).intValue(), 0.00001);
            expressions[0] = literal_05pi;
            expressions[1] = literal_1;
            assertEquals("max of (1.5707963267948966,1.0):",
                (int) Math.max(1.5707963267948966, 1.0),
                ((Integer) max_3Function.getValue(null)).intValue(), 0.00001);
        } catch (FactoryConfigurationError e) {
            e.printStackTrace();
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    public void testmax_4() {
        try {
            FunctionExpression max_4Function = filterFactory
                .createFunctionExpression("max_4");
            assertEquals("Name is, ", "max_4", max_4Function.getName());
            assertEquals("Number of arguments, ", 2, max_4Function.getArgCount());

            Expression[] expressions = new Expression[2];
            max_4Function.setArgs(expressions);
            expressions[0] = literal_1;
            expressions[1] = literal_m1;
            assertEquals("max of (1.0,-1.0):", (long) Math.max(1.0, -1.0),
                ((Long) max_4Function.getValue(null)).longValue(), 0.00001);
            expressions[0] = literal_m1;
            expressions[1] = literal_2;
            assertEquals("max of (-1.0,2.0):", (long) Math.max(-1.0, 2.0),
                ((Long) max_4Function.getValue(null)).longValue(), 0.00001);
            expressions[0] = literal_2;
            expressions[1] = literal_m2;
            assertEquals("max of (2.0,-2.0):", (long) Math.max(2.0, -2.0),
                ((Long) max_4Function.getValue(null)).longValue(), 0.00001);
            expressions[0] = literal_m2;
            expressions[1] = literal_pi;
            assertEquals("max of (-2.0,3.141592653589793):",
                (long) Math.max(-2.0, 3.141592653589793),
                ((Long) max_4Function.getValue(null)).longValue(), 0.00001);
            expressions[0] = literal_pi;
            expressions[1] = literal_05pi;
            assertEquals("max of (3.141592653589793,1.5707963267948966):",
                (long) Math.max(3.141592653589793, 1.5707963267948966),
                ((Long) max_4Function.getValue(null)).longValue(), 0.00001);
            expressions[0] = literal_05pi;
            expressions[1] = literal_1;
            assertEquals("max of (1.5707963267948966,1.0):",
                (long) Math.max(1.5707963267948966, 1.0),
                ((Long) max_4Function.getValue(null)).longValue(), 0.00001);
        } catch (FactoryConfigurationError e) {
            e.printStackTrace();
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    public void testabs() {
        try {
            FunctionExpression absFunction = filterFactory
                .createFunctionExpression("abs");
            assertEquals("Name is, ", "abs", absFunction.getName());
            assertEquals("Number of arguments, ", 1, absFunction.getArgCount());

            Expression[] expressions = new Expression[1];
            absFunction.setArgs(expressions);
            expressions[0] = literal_1;
            assertEquals("abs of (1.0):", (long) Math.abs(1.0),
                ((Long) absFunction.getValue(null)).longValue(), 0.00001);
            expressions[0] = literal_m1;
            assertEquals("abs of (-1.0):", (long) Math.abs(-1.0),
                ((Long) absFunction.getValue(null)).longValue(), 0.00001);
            expressions[0] = literal_2;
            assertEquals("abs of (2.0):", (long) Math.abs(2.0),
                ((Long) absFunction.getValue(null)).longValue(), 0.00001);
            expressions[0] = literal_m2;
            assertEquals("abs of (-2.0):", (long) Math.abs(-2.0),
                ((Long) absFunction.getValue(null)).longValue(), 0.00001);
            expressions[0] = literal_pi;
            assertEquals("abs of (3.141592653589793):",
                (long) Math.abs(3.141592653589793),
                ((Long) absFunction.getValue(null)).longValue(), 0.00001);
            expressions[0] = literal_05pi;
            assertEquals("abs of (1.5707963267948966):",
                (long) Math.abs(1.5707963267948966),
                ((Long) absFunction.getValue(null)).longValue(), 0.00001);
        } catch (FactoryConfigurationError e) {
            e.printStackTrace();
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    public void testabs_2() {
        try {
            FunctionExpression abs_2Function = filterFactory
                .createFunctionExpression("abs_2");
            assertEquals("Name is, ", "abs_2", abs_2Function.getName());
            assertEquals("Number of arguments, ", 1, abs_2Function.getArgCount());

            Expression[] expressions = new Expression[1];
            abs_2Function.setArgs(expressions);
            expressions[0] = literal_1;

            double good0 = Math.abs(1.0);

            if (Double.isNaN(good0)) {
                assertTrue("abs of (1.0):",
                    Double.isNaN(
                        ((Double) abs_2Function.getValue(null)).doubleValue()));
            } else {
                assertEquals("abs of (1.0):", (double) Math.abs(1.0),
                    ((Double) abs_2Function.getValue(null)).doubleValue(),
                    0.00001);
            }

            expressions[0] = literal_m1;

            double good1 = Math.abs(-1.0);

            if (Double.isNaN(good1)) {
                assertTrue("abs of (-1.0):",
                    Double.isNaN(
                        ((Double) abs_2Function.getValue(null)).doubleValue()));
            } else {
                assertEquals("abs of (-1.0):", (double) Math.abs(-1.0),
                    ((Double) abs_2Function.getValue(null)).doubleValue(),
                    0.00001);
            }

            expressions[0] = literal_2;

            double good2 = Math.abs(2.0);

            if (Double.isNaN(good2)) {
                assertTrue("abs of (2.0):",
                    Double.isNaN(
                        ((Double) abs_2Function.getValue(null)).doubleValue()));
            } else {
                assertEquals("abs of (2.0):", (double) Math.abs(2.0),
                    ((Double) abs_2Function.getValue(null)).doubleValue(),
                    0.00001);
            }

            expressions[0] = literal_m2;

            double good3 = Math.abs(-2.0);

            if (Double.isNaN(good3)) {
                assertTrue("abs of (-2.0):",
                    Double.isNaN(
                        ((Double) abs_2Function.getValue(null)).doubleValue()));
            } else {
                assertEquals("abs of (-2.0):", (double) Math.abs(-2.0),
                    ((Double) abs_2Function.getValue(null)).doubleValue(),
                    0.00001);
            }

            expressions[0] = literal_pi;

            double good4 = Math.abs(3.141592653589793);

            if (Double.isNaN(good4)) {
                assertTrue("abs of (3.141592653589793):",
                    Double.isNaN(
                        ((Double) abs_2Function.getValue(null)).doubleValue()));
            } else {
                assertEquals("abs of (3.141592653589793):",
                    (double) Math.abs(3.141592653589793),
                    ((Double) abs_2Function.getValue(null)).doubleValue(),
                    0.00001);
            }

            expressions[0] = literal_05pi;

            double good5 = Math.abs(1.5707963267948966);

            if (Double.isNaN(good5)) {
                assertTrue("abs of (1.5707963267948966):",
                    Double.isNaN(
                        ((Double) abs_2Function.getValue(null)).doubleValue()));
            } else {
                assertEquals("abs of (1.5707963267948966):",
                    (double) Math.abs(1.5707963267948966),
                    ((Double) abs_2Function.getValue(null)).doubleValue(),
                    0.00001);
            }
        } catch (FactoryConfigurationError e) {
            e.printStackTrace();
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    public void testabs_3() {
        try {
            FunctionExpression abs_3Function = filterFactory
                .createFunctionExpression("abs_3");
            assertEquals("Name is, ", "abs_3", abs_3Function.getName());
            assertEquals("Number of arguments, ", 1, abs_3Function.getArgCount());

            Expression[] expressions = new Expression[1];
            abs_3Function.setArgs(expressions);
            expressions[0] = literal_1;
            assertEquals("abs of (1.0):", (float) Math.abs(1.0),
                ((Float) abs_3Function.getValue(null)).floatValue(), 0.00001);
            expressions[0] = literal_m1;
            assertEquals("abs of (-1.0):", (float) Math.abs(-1.0),
                ((Float) abs_3Function.getValue(null)).floatValue(), 0.00001);
            expressions[0] = literal_2;
            assertEquals("abs of (2.0):", (float) Math.abs(2.0),
                ((Float) abs_3Function.getValue(null)).floatValue(), 0.00001);
            expressions[0] = literal_m2;
            assertEquals("abs of (-2.0):", (float) Math.abs(-2.0),
                ((Float) abs_3Function.getValue(null)).floatValue(), 0.00001);
            expressions[0] = literal_pi;
            assertEquals("abs of (3.141592653589793):",
                (float) Math.abs(3.141592653589793),
                ((Float) abs_3Function.getValue(null)).floatValue(), 0.00001);
            expressions[0] = literal_05pi;
            assertEquals("abs of (1.5707963267948966):",
                (float) Math.abs(1.5707963267948966),
                ((Float) abs_3Function.getValue(null)).floatValue(), 0.00001);
        } catch (FactoryConfigurationError e) {
            e.printStackTrace();
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    public void testabs_4() {
        try {
            FunctionExpression abs_4Function = filterFactory
                .createFunctionExpression("abs_4");
            assertEquals("Name is, ", "abs_4", abs_4Function.getName());
            assertEquals("Number of arguments, ", 1, abs_4Function.getArgCount());

            Expression[] expressions = new Expression[1];
            abs_4Function.setArgs(expressions);
            expressions[0] = literal_1;
            assertEquals("abs of (1.0):", (int) Math.abs(1.0),
                ((Integer) abs_4Function.getValue(null)).intValue(), 0.00001);
            expressions[0] = literal_m1;
            assertEquals("abs of (-1.0):", (int) Math.abs(-1.0),
                ((Integer) abs_4Function.getValue(null)).intValue(), 0.00001);
            expressions[0] = literal_2;
            assertEquals("abs of (2.0):", (int) Math.abs(2.0),
                ((Integer) abs_4Function.getValue(null)).intValue(), 0.00001);
            expressions[0] = literal_m2;
            assertEquals("abs of (-2.0):", (int) Math.abs(-2.0),
                ((Integer) abs_4Function.getValue(null)).intValue(), 0.00001);
            expressions[0] = literal_pi;
            assertEquals("abs of (3.141592653589793):",
                (int) Math.abs(3.141592653589793),
                ((Integer) abs_4Function.getValue(null)).intValue(), 0.00001);
            expressions[0] = literal_05pi;
            assertEquals("abs of (1.5707963267948966):",
                (int) Math.abs(1.5707963267948966),
                ((Integer) abs_4Function.getValue(null)).intValue(), 0.00001);
        } catch (FactoryConfigurationError e) {
            e.printStackTrace();
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    public void testIEEEremainder() {
        try {
            FunctionExpression IEEEremainderFunction = filterFactory
                .createFunctionExpression("IEEEremainder");
            assertEquals("Name is, ", "IEEEremainder",
                IEEEremainderFunction.getName());
            assertEquals("Number of arguments, ", 2,
                IEEEremainderFunction.getArgCount());

            Expression[] expressions = new Expression[2];
            IEEEremainderFunction.setArgs(expressions);
            expressions[0] = literal_1;
            expressions[1] = literal_m1;

            double good0 = Math.IEEEremainder(1.0, -1.0);

            if (Double.isNaN(good0)) {
                assertTrue("IEEEremainder of (1.0,-1.0):",
                    Double.isNaN(
                        ((Double) IEEEremainderFunction.getValue(null))
                        .doubleValue()));
            } else {
                assertEquals("IEEEremainder of (1.0,-1.0):",
                    (double) Math.IEEEremainder(1.0, -1.0),
                    ((Double) IEEEremainderFunction.getValue(null)).doubleValue(),
                    0.00001);
            }

            expressions[0] = literal_m1;
            expressions[1] = literal_2;

            double good1 = Math.IEEEremainder(-1.0, 2.0);

            if (Double.isNaN(good1)) {
                assertTrue("IEEEremainder of (-1.0,2.0):",
                    Double.isNaN(
                        ((Double) IEEEremainderFunction.getValue(null))
                        .doubleValue()));
            } else {
                assertEquals("IEEEremainder of (-1.0,2.0):",
                    (double) Math.IEEEremainder(-1.0, 2.0),
                    ((Double) IEEEremainderFunction.getValue(null)).doubleValue(),
                    0.00001);
            }

            expressions[0] = literal_2;
            expressions[1] = literal_m2;

            double good2 = Math.IEEEremainder(2.0, -2.0);

            if (Double.isNaN(good2)) {
                assertTrue("IEEEremainder of (2.0,-2.0):",
                    Double.isNaN(
                        ((Double) IEEEremainderFunction.getValue(null))
                        .doubleValue()));
            } else {
                assertEquals("IEEEremainder of (2.0,-2.0):",
                    (double) Math.IEEEremainder(2.0, -2.0),
                    ((Double) IEEEremainderFunction.getValue(null)).doubleValue(),
                    0.00001);
            }

            expressions[0] = literal_m2;
            expressions[1] = literal_pi;

            double good3 = Math.IEEEremainder(-2.0, 3.141592653589793);

            if (Double.isNaN(good3)) {
                assertTrue("IEEEremainder of (-2.0,3.141592653589793):",
                    Double.isNaN(
                        ((Double) IEEEremainderFunction.getValue(null))
                        .doubleValue()));
            } else {
                assertEquals("IEEEremainder of (-2.0,3.141592653589793):",
                    (double) Math.IEEEremainder(-2.0, 3.141592653589793),
                    ((Double) IEEEremainderFunction.getValue(null)).doubleValue(),
                    0.00001);
            }

            expressions[0] = literal_pi;
            expressions[1] = literal_05pi;

            double good4 = Math.IEEEremainder(3.141592653589793,
                    1.5707963267948966);

            if (Double.isNaN(good4)) {
                assertTrue("IEEEremainder of (3.141592653589793,1.5707963267948966):",
                    Double.isNaN(
                        ((Double) IEEEremainderFunction.getValue(null))
                        .doubleValue()));
            } else {
                assertEquals("IEEEremainder of (3.141592653589793,1.5707963267948966):",
                    (double) Math.IEEEremainder(3.141592653589793,
                        1.5707963267948966),
                    ((Double) IEEEremainderFunction.getValue(null)).doubleValue(),
                    0.00001);
            }

            expressions[0] = literal_05pi;
            expressions[1] = literal_1;

            double good5 = Math.IEEEremainder(1.5707963267948966, 1.0);

            if (Double.isNaN(good5)) {
                assertTrue("IEEEremainder of (1.5707963267948966,1.0):",
                    Double.isNaN(
                        ((Double) IEEEremainderFunction.getValue(null))
                        .doubleValue()));
            } else {
                assertEquals("IEEEremainder of (1.5707963267948966,1.0):",
                    (double) Math.IEEEremainder(1.5707963267948966, 1.0),
                    ((Double) IEEEremainderFunction.getValue(null)).doubleValue(),
                    0.00001);
            }
        } catch (FactoryConfigurationError e) {
            e.printStackTrace();
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    public void testacos() {
        try {
            FunctionExpression acosFunction = filterFactory
                .createFunctionExpression("acos");
            assertEquals("Name is, ", "acos", acosFunction.getName());
            assertEquals("Number of arguments, ", 1, acosFunction.getArgCount());

            Expression[] expressions = new Expression[1];
            acosFunction.setArgs(expressions);
            expressions[0] = literal_1;

            double good0 = Math.acos(1.0);

            if (Double.isNaN(good0)) {
                assertTrue("acos of (1.0):",
                    Double.isNaN(
                        ((Double) acosFunction.getValue(null)).doubleValue()));
            } else {
                assertEquals("acos of (1.0):", (double) Math.acos(1.0),
                    ((Double) acosFunction.getValue(null)).doubleValue(),
                    0.00001);
            }

            expressions[0] = literal_m1;

            double good1 = Math.acos(-1.0);

            if (Double.isNaN(good1)) {
                assertTrue("acos of (-1.0):",
                    Double.isNaN(
                        ((Double) acosFunction.getValue(null)).doubleValue()));
            } else {
                assertEquals("acos of (-1.0):", (double) Math.acos(-1.0),
                    ((Double) acosFunction.getValue(null)).doubleValue(),
                    0.00001);
            }

            expressions[0] = literal_2;

            double good2 = Math.acos(2.0);

            if (Double.isNaN(good2)) {
                assertTrue("acos of (2.0):",
                    Double.isNaN(
                        ((Double) acosFunction.getValue(null)).doubleValue()));
            } else {
                assertEquals("acos of (2.0):", (double) Math.acos(2.0),
                    ((Double) acosFunction.getValue(null)).doubleValue(),
                    0.00001);
            }

            expressions[0] = literal_m2;

            double good3 = Math.acos(-2.0);

            if (Double.isNaN(good3)) {
                assertTrue("acos of (-2.0):",
                    Double.isNaN(
                        ((Double) acosFunction.getValue(null)).doubleValue()));
            } else {
                assertEquals("acos of (-2.0):", (double) Math.acos(-2.0),
                    ((Double) acosFunction.getValue(null)).doubleValue(),
                    0.00001);
            }

            expressions[0] = literal_pi;

            double good4 = Math.acos(3.141592653589793);

            if (Double.isNaN(good4)) {
                assertTrue("acos of (3.141592653589793):",
                    Double.isNaN(
                        ((Double) acosFunction.getValue(null)).doubleValue()));
            } else {
                assertEquals("acos of (3.141592653589793):",
                    (double) Math.acos(3.141592653589793),
                    ((Double) acosFunction.getValue(null)).doubleValue(),
                    0.00001);
            }

            expressions[0] = literal_05pi;

            double good5 = Math.acos(1.5707963267948966);

            if (Double.isNaN(good5)) {
                assertTrue("acos of (1.5707963267948966):",
                    Double.isNaN(
                        ((Double) acosFunction.getValue(null)).doubleValue()));
            } else {
                assertEquals("acos of (1.5707963267948966):",
                    (double) Math.acos(1.5707963267948966),
                    ((Double) acosFunction.getValue(null)).doubleValue(),
                    0.00001);
            }
        } catch (FactoryConfigurationError e) {
            e.printStackTrace();
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    public void testasin() {
        try {
            FunctionExpression asinFunction = filterFactory
                .createFunctionExpression("asin");
            assertEquals("Name is, ", "asin", asinFunction.getName());
            assertEquals("Number of arguments, ", 1, asinFunction.getArgCount());

            Expression[] expressions = new Expression[1];
            asinFunction.setArgs(expressions);
            expressions[0] = literal_1;

            double good0 = Math.asin(1.0);

            if (Double.isNaN(good0)) {
                assertTrue("asin of (1.0):",
                    Double.isNaN(
                        ((Double) asinFunction.getValue(null)).doubleValue()));
            } else {
                assertEquals("asin of (1.0):", (double) Math.asin(1.0),
                    ((Double) asinFunction.getValue(null)).doubleValue(),
                    0.00001);
            }

            expressions[0] = literal_m1;

            double good1 = Math.asin(-1.0);

            if (Double.isNaN(good1)) {
                assertTrue("asin of (-1.0):",
                    Double.isNaN(
                        ((Double) asinFunction.getValue(null)).doubleValue()));
            } else {
                assertEquals("asin of (-1.0):", (double) Math.asin(-1.0),
                    ((Double) asinFunction.getValue(null)).doubleValue(),
                    0.00001);
            }

            expressions[0] = literal_2;

            double good2 = Math.asin(2.0);

            if (Double.isNaN(good2)) {
                assertTrue("asin of (2.0):",
                    Double.isNaN(
                        ((Double) asinFunction.getValue(null)).doubleValue()));
            } else {
                assertEquals("asin of (2.0):", (double) Math.asin(2.0),
                    ((Double) asinFunction.getValue(null)).doubleValue(),
                    0.00001);
            }

            expressions[0] = literal_m2;

            double good3 = Math.asin(-2.0);

            if (Double.isNaN(good3)) {
                assertTrue("asin of (-2.0):",
                    Double.isNaN(
                        ((Double) asinFunction.getValue(null)).doubleValue()));
            } else {
                assertEquals("asin of (-2.0):", (double) Math.asin(-2.0),
                    ((Double) asinFunction.getValue(null)).doubleValue(),
                    0.00001);
            }

            expressions[0] = literal_pi;

            double good4 = Math.asin(3.141592653589793);

            if (Double.isNaN(good4)) {
                assertTrue("asin of (3.141592653589793):",
                    Double.isNaN(
                        ((Double) asinFunction.getValue(null)).doubleValue()));
            } else {
                assertEquals("asin of (3.141592653589793):",
                    (double) Math.asin(3.141592653589793),
                    ((Double) asinFunction.getValue(null)).doubleValue(),
                    0.00001);
            }

            expressions[0] = literal_05pi;

            double good5 = Math.asin(1.5707963267948966);

            if (Double.isNaN(good5)) {
                assertTrue("asin of (1.5707963267948966):",
                    Double.isNaN(
                        ((Double) asinFunction.getValue(null)).doubleValue()));
            } else {
                assertEquals("asin of (1.5707963267948966):",
                    (double) Math.asin(1.5707963267948966),
                    ((Double) asinFunction.getValue(null)).doubleValue(),
                    0.00001);
            }
        } catch (FactoryConfigurationError e) {
            e.printStackTrace();
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    public void testatan() {
        try {
            FunctionExpression atanFunction = filterFactory
                .createFunctionExpression("atan");
            assertEquals("Name is, ", "atan", atanFunction.getName());
            assertEquals("Number of arguments, ", 1, atanFunction.getArgCount());

            Expression[] expressions = new Expression[1];
            atanFunction.setArgs(expressions);
            expressions[0] = literal_1;

            double good0 = Math.atan(1.0);

            if (Double.isNaN(good0)) {
                assertTrue("atan of (1.0):",
                    Double.isNaN(
                        ((Double) atanFunction.getValue(null)).doubleValue()));
            } else {
                assertEquals("atan of (1.0):", (double) Math.atan(1.0),
                    ((Double) atanFunction.getValue(null)).doubleValue(),
                    0.00001);
            }

            expressions[0] = literal_m1;

            double good1 = Math.atan(-1.0);

            if (Double.isNaN(good1)) {
                assertTrue("atan of (-1.0):",
                    Double.isNaN(
                        ((Double) atanFunction.getValue(null)).doubleValue()));
            } else {
                assertEquals("atan of (-1.0):", (double) Math.atan(-1.0),
                    ((Double) atanFunction.getValue(null)).doubleValue(),
                    0.00001);
            }

            expressions[0] = literal_2;

            double good2 = Math.atan(2.0);

            if (Double.isNaN(good2)) {
                assertTrue("atan of (2.0):",
                    Double.isNaN(
                        ((Double) atanFunction.getValue(null)).doubleValue()));
            } else {
                assertEquals("atan of (2.0):", (double) Math.atan(2.0),
                    ((Double) atanFunction.getValue(null)).doubleValue(),
                    0.00001);
            }

            expressions[0] = literal_m2;

            double good3 = Math.atan(-2.0);

            if (Double.isNaN(good3)) {
                assertTrue("atan of (-2.0):",
                    Double.isNaN(
                        ((Double) atanFunction.getValue(null)).doubleValue()));
            } else {
                assertEquals("atan of (-2.0):", (double) Math.atan(-2.0),
                    ((Double) atanFunction.getValue(null)).doubleValue(),
                    0.00001);
            }

            expressions[0] = literal_pi;

            double good4 = Math.atan(3.141592653589793);

            if (Double.isNaN(good4)) {
                assertTrue("atan of (3.141592653589793):",
                    Double.isNaN(
                        ((Double) atanFunction.getValue(null)).doubleValue()));
            } else {
                assertEquals("atan of (3.141592653589793):",
                    (double) Math.atan(3.141592653589793),
                    ((Double) atanFunction.getValue(null)).doubleValue(),
                    0.00001);
            }

            expressions[0] = literal_05pi;

            double good5 = Math.atan(1.5707963267948966);

            if (Double.isNaN(good5)) {
                assertTrue("atan of (1.5707963267948966):",
                    Double.isNaN(
                        ((Double) atanFunction.getValue(null)).doubleValue()));
            } else {
                assertEquals("atan of (1.5707963267948966):",
                    (double) Math.atan(1.5707963267948966),
                    ((Double) atanFunction.getValue(null)).doubleValue(),
                    0.00001);
            }
        } catch (FactoryConfigurationError e) {
            e.printStackTrace();
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    public void testceil() {
        try {
            FunctionExpression ceilFunction = filterFactory
                .createFunctionExpression("ceil");
            assertEquals("Name is, ", "ceil", ceilFunction.getName());
            assertEquals("Number of arguments, ", 1, ceilFunction.getArgCount());

            Expression[] expressions = new Expression[1];
            ceilFunction.setArgs(expressions);
            expressions[0] = literal_1;

            double good0 = Math.ceil(1.0);

            if (Double.isNaN(good0)) {
                assertTrue("ceil of (1.0):",
                    Double.isNaN(
                        ((Double) ceilFunction.getValue(null)).doubleValue()));
            } else {
                assertEquals("ceil of (1.0):", (double) Math.ceil(1.0),
                    ((Double) ceilFunction.getValue(null)).doubleValue(),
                    0.00001);
            }

            expressions[0] = literal_m1;

            double good1 = Math.ceil(-1.0);

            if (Double.isNaN(good1)) {
                assertTrue("ceil of (-1.0):",
                    Double.isNaN(
                        ((Double) ceilFunction.getValue(null)).doubleValue()));
            } else {
                assertEquals("ceil of (-1.0):", (double) Math.ceil(-1.0),
                    ((Double) ceilFunction.getValue(null)).doubleValue(),
                    0.00001);
            }

            expressions[0] = literal_2;

            double good2 = Math.ceil(2.0);

            if (Double.isNaN(good2)) {
                assertTrue("ceil of (2.0):",
                    Double.isNaN(
                        ((Double) ceilFunction.getValue(null)).doubleValue()));
            } else {
                assertEquals("ceil of (2.0):", (double) Math.ceil(2.0),
                    ((Double) ceilFunction.getValue(null)).doubleValue(),
                    0.00001);
            }

            expressions[0] = literal_m2;

            double good3 = Math.ceil(-2.0);

            if (Double.isNaN(good3)) {
                assertTrue("ceil of (-2.0):",
                    Double.isNaN(
                        ((Double) ceilFunction.getValue(null)).doubleValue()));
            } else {
                assertEquals("ceil of (-2.0):", (double) Math.ceil(-2.0),
                    ((Double) ceilFunction.getValue(null)).doubleValue(),
                    0.00001);
            }

            expressions[0] = literal_pi;

            double good4 = Math.ceil(3.141592653589793);

            if (Double.isNaN(good4)) {
                assertTrue("ceil of (3.141592653589793):",
                    Double.isNaN(
                        ((Double) ceilFunction.getValue(null)).doubleValue()));
            } else {
                assertEquals("ceil of (3.141592653589793):",
                    (double) Math.ceil(3.141592653589793),
                    ((Double) ceilFunction.getValue(null)).doubleValue(),
                    0.00001);
            }

            expressions[0] = literal_05pi;

            double good5 = Math.ceil(1.5707963267948966);

            if (Double.isNaN(good5)) {
                assertTrue("ceil of (1.5707963267948966):",
                    Double.isNaN(
                        ((Double) ceilFunction.getValue(null)).doubleValue()));
            } else {
                assertEquals("ceil of (1.5707963267948966):",
                    (double) Math.ceil(1.5707963267948966),
                    ((Double) ceilFunction.getValue(null)).doubleValue(),
                    0.00001);
            }
        } catch (FactoryConfigurationError e) {
            e.printStackTrace();
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    public void testexp() {
        try {
            FunctionExpression expFunction = filterFactory
                .createFunctionExpression("exp");
            assertEquals("Name is, ", "exp", expFunction.getName());
            assertEquals("Number of arguments, ", 1, expFunction.getArgCount());

            Expression[] expressions = new Expression[1];
            expFunction.setArgs(expressions);
            expressions[0] = literal_1;

            double good0 = Math.exp(1.0);

            if (Double.isNaN(good0)) {
                assertTrue("exp of (1.0):",
                    Double.isNaN(
                        ((Double) expFunction.getValue(null)).doubleValue()));
            } else {
                assertEquals("exp of (1.0):", (double) Math.exp(1.0),
                    ((Double) expFunction.getValue(null)).doubleValue(), 0.00001);
            }

            expressions[0] = literal_m1;

            double good1 = Math.exp(-1.0);

            if (Double.isNaN(good1)) {
                assertTrue("exp of (-1.0):",
                    Double.isNaN(
                        ((Double) expFunction.getValue(null)).doubleValue()));
            } else {
                assertEquals("exp of (-1.0):", (double) Math.exp(-1.0),
                    ((Double) expFunction.getValue(null)).doubleValue(), 0.00001);
            }

            expressions[0] = literal_2;

            double good2 = Math.exp(2.0);

            if (Double.isNaN(good2)) {
                assertTrue("exp of (2.0):",
                    Double.isNaN(
                        ((Double) expFunction.getValue(null)).doubleValue()));
            } else {
                assertEquals("exp of (2.0):", (double) Math.exp(2.0),
                    ((Double) expFunction.getValue(null)).doubleValue(), 0.00001);
            }

            expressions[0] = literal_m2;

            double good3 = Math.exp(-2.0);

            if (Double.isNaN(good3)) {
                assertTrue("exp of (-2.0):",
                    Double.isNaN(
                        ((Double) expFunction.getValue(null)).doubleValue()));
            } else {
                assertEquals("exp of (-2.0):", (double) Math.exp(-2.0),
                    ((Double) expFunction.getValue(null)).doubleValue(), 0.00001);
            }

            expressions[0] = literal_pi;

            double good4 = Math.exp(3.141592653589793);

            if (Double.isNaN(good4)) {
                assertTrue("exp of (3.141592653589793):",
                    Double.isNaN(
                        ((Double) expFunction.getValue(null)).doubleValue()));
            } else {
                assertEquals("exp of (3.141592653589793):",
                    (double) Math.exp(3.141592653589793),
                    ((Double) expFunction.getValue(null)).doubleValue(), 0.00001);
            }

            expressions[0] = literal_05pi;

            double good5 = Math.exp(1.5707963267948966);

            if (Double.isNaN(good5)) {
                assertTrue("exp of (1.5707963267948966):",
                    Double.isNaN(
                        ((Double) expFunction.getValue(null)).doubleValue()));
            } else {
                assertEquals("exp of (1.5707963267948966):",
                    (double) Math.exp(1.5707963267948966),
                    ((Double) expFunction.getValue(null)).doubleValue(), 0.00001);
            }
        } catch (FactoryConfigurationError e) {
            e.printStackTrace();
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    public void testfloor() {
        try {
            FunctionExpression floorFunction = filterFactory
                .createFunctionExpression("floor");
            assertEquals("Name is, ", "floor", floorFunction.getName());
            assertEquals("Number of arguments, ", 1, floorFunction.getArgCount());

            Expression[] expressions = new Expression[1];
            floorFunction.setArgs(expressions);
            expressions[0] = literal_1;

            double good0 = Math.floor(1.0);

            if (Double.isNaN(good0)) {
                assertTrue("floor of (1.0):",
                    Double.isNaN(
                        ((Double) floorFunction.getValue(null)).doubleValue()));
            } else {
                assertEquals("floor of (1.0):", (double) Math.floor(1.0),
                    ((Double) floorFunction.getValue(null)).doubleValue(),
                    0.00001);
            }

            expressions[0] = literal_m1;

            double good1 = Math.floor(-1.0);

            if (Double.isNaN(good1)) {
                assertTrue("floor of (-1.0):",
                    Double.isNaN(
                        ((Double) floorFunction.getValue(null)).doubleValue()));
            } else {
                assertEquals("floor of (-1.0):", (double) Math.floor(-1.0),
                    ((Double) floorFunction.getValue(null)).doubleValue(),
                    0.00001);
            }

            expressions[0] = literal_2;

            double good2 = Math.floor(2.0);

            if (Double.isNaN(good2)) {
                assertTrue("floor of (2.0):",
                    Double.isNaN(
                        ((Double) floorFunction.getValue(null)).doubleValue()));
            } else {
                assertEquals("floor of (2.0):", (double) Math.floor(2.0),
                    ((Double) floorFunction.getValue(null)).doubleValue(),
                    0.00001);
            }

            expressions[0] = literal_m2;

            double good3 = Math.floor(-2.0);

            if (Double.isNaN(good3)) {
                assertTrue("floor of (-2.0):",
                    Double.isNaN(
                        ((Double) floorFunction.getValue(null)).doubleValue()));
            } else {
                assertEquals("floor of (-2.0):", (double) Math.floor(-2.0),
                    ((Double) floorFunction.getValue(null)).doubleValue(),
                    0.00001);
            }

            expressions[0] = literal_pi;

            double good4 = Math.floor(3.141592653589793);

            if (Double.isNaN(good4)) {
                assertTrue("floor of (3.141592653589793):",
                    Double.isNaN(
                        ((Double) floorFunction.getValue(null)).doubleValue()));
            } else {
                assertEquals("floor of (3.141592653589793):",
                    (double) Math.floor(3.141592653589793),
                    ((Double) floorFunction.getValue(null)).doubleValue(),
                    0.00001);
            }

            expressions[0] = literal_05pi;

            double good5 = Math.floor(1.5707963267948966);

            if (Double.isNaN(good5)) {
                assertTrue("floor of (1.5707963267948966):",
                    Double.isNaN(
                        ((Double) floorFunction.getValue(null)).doubleValue()));
            } else {
                assertEquals("floor of (1.5707963267948966):",
                    (double) Math.floor(1.5707963267948966),
                    ((Double) floorFunction.getValue(null)).doubleValue(),
                    0.00001);
            }
        } catch (FactoryConfigurationError e) {
            e.printStackTrace();
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    public void testlog() {
        try {
            FunctionExpression logFunction = filterFactory
                .createFunctionExpression("log");
            assertEquals("Name is, ", "log", logFunction.getName());
            assertEquals("Number of arguments, ", 1, logFunction.getArgCount());

            Expression[] expressions = new Expression[1];
            logFunction.setArgs(expressions);
            expressions[0] = literal_1;

            double good0 = Math.log(1.0);

            if (Double.isNaN(good0)) {
                assertTrue("log of (1.0):",
                    Double.isNaN(
                        ((Double) logFunction.getValue(null)).doubleValue()));
            } else {
                assertEquals("log of (1.0):", (double) Math.log(1.0),
                    ((Double) logFunction.getValue(null)).doubleValue(), 0.00001);
            }

            expressions[0] = literal_m1;

            double good1 = Math.log(-1.0);

            if (Double.isNaN(good1)) {
                assertTrue("log of (-1.0):",
                    Double.isNaN(
                        ((Double) logFunction.getValue(null)).doubleValue()));
            } else {
                assertEquals("log of (-1.0):", (double) Math.log(-1.0),
                    ((Double) logFunction.getValue(null)).doubleValue(), 0.00001);
            }

            expressions[0] = literal_2;

            double good2 = Math.log(2.0);

            if (Double.isNaN(good2)) {
                assertTrue("log of (2.0):",
                    Double.isNaN(
                        ((Double) logFunction.getValue(null)).doubleValue()));
            } else {
                assertEquals("log of (2.0):", (double) Math.log(2.0),
                    ((Double) logFunction.getValue(null)).doubleValue(), 0.00001);
            }

            expressions[0] = literal_m2;

            double good3 = Math.log(-2.0);

            if (Double.isNaN(good3)) {
                assertTrue("log of (-2.0):",
                    Double.isNaN(
                        ((Double) logFunction.getValue(null)).doubleValue()));
            } else {
                assertEquals("log of (-2.0):", (double) Math.log(-2.0),
                    ((Double) logFunction.getValue(null)).doubleValue(), 0.00001);
            }

            expressions[0] = literal_pi;

            double good4 = Math.log(3.141592653589793);

            if (Double.isNaN(good4)) {
                assertTrue("log of (3.141592653589793):",
                    Double.isNaN(
                        ((Double) logFunction.getValue(null)).doubleValue()));
            } else {
                assertEquals("log of (3.141592653589793):",
                    (double) Math.log(3.141592653589793),
                    ((Double) logFunction.getValue(null)).doubleValue(), 0.00001);
            }

            expressions[0] = literal_05pi;

            double good5 = Math.log(1.5707963267948966);

            if (Double.isNaN(good5)) {
                assertTrue("log of (1.5707963267948966):",
                    Double.isNaN(
                        ((Double) logFunction.getValue(null)).doubleValue()));
            } else {
                assertEquals("log of (1.5707963267948966):",
                    (double) Math.log(1.5707963267948966),
                    ((Double) logFunction.getValue(null)).doubleValue(), 0.00001);
            }
        } catch (FactoryConfigurationError e) {
            e.printStackTrace();
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    public void testrandom() {
        try {
            FunctionExpression randomFunction = filterFactory
                .createFunctionExpression("random");
            assertEquals("Name is, ", "random", randomFunction.getName());
            assertEquals("Number of arguments, ", 0,
                randomFunction.getArgCount());

            Expression[] expressions = new Expression[0];
            randomFunction.setArgs(expressions);
        } catch (FactoryConfigurationError e) {
            e.printStackTrace();
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    public void testrint() {
        try {
            FunctionExpression rintFunction = filterFactory
                .createFunctionExpression("rint");
            assertEquals("Name is, ", "rint", rintFunction.getName());
            assertEquals("Number of arguments, ", 1, rintFunction.getArgCount());

            Expression[] expressions = new Expression[1];
            rintFunction.setArgs(expressions);
            expressions[0] = literal_1;

            double good0 = Math.rint(1.0);

            if (Double.isNaN(good0)) {
                assertTrue("rint of (1.0):",
                    Double.isNaN(
                        ((Double) rintFunction.getValue(null)).doubleValue()));
            } else {
                assertEquals("rint of (1.0):", (double) Math.rint(1.0),
                    ((Double) rintFunction.getValue(null)).doubleValue(),
                    0.00001);
            }

            expressions[0] = literal_m1;

            double good1 = Math.rint(-1.0);

            if (Double.isNaN(good1)) {
                assertTrue("rint of (-1.0):",
                    Double.isNaN(
                        ((Double) rintFunction.getValue(null)).doubleValue()));
            } else {
                assertEquals("rint of (-1.0):", (double) Math.rint(-1.0),
                    ((Double) rintFunction.getValue(null)).doubleValue(),
                    0.00001);
            }

            expressions[0] = literal_2;

            double good2 = Math.rint(2.0);

            if (Double.isNaN(good2)) {
                assertTrue("rint of (2.0):",
                    Double.isNaN(
                        ((Double) rintFunction.getValue(null)).doubleValue()));
            } else {
                assertEquals("rint of (2.0):", (double) Math.rint(2.0),
                    ((Double) rintFunction.getValue(null)).doubleValue(),
                    0.00001);
            }

            expressions[0] = literal_m2;

            double good3 = Math.rint(-2.0);

            if (Double.isNaN(good3)) {
                assertTrue("rint of (-2.0):",
                    Double.isNaN(
                        ((Double) rintFunction.getValue(null)).doubleValue()));
            } else {
                assertEquals("rint of (-2.0):", (double) Math.rint(-2.0),
                    ((Double) rintFunction.getValue(null)).doubleValue(),
                    0.00001);
            }

            expressions[0] = literal_pi;

            double good4 = Math.rint(3.141592653589793);

            if (Double.isNaN(good4)) {
                assertTrue("rint of (3.141592653589793):",
                    Double.isNaN(
                        ((Double) rintFunction.getValue(null)).doubleValue()));
            } else {
                assertEquals("rint of (3.141592653589793):",
                    (double) Math.rint(3.141592653589793),
                    ((Double) rintFunction.getValue(null)).doubleValue(),
                    0.00001);
            }

            expressions[0] = literal_05pi;

            double good5 = Math.rint(1.5707963267948966);

            if (Double.isNaN(good5)) {
                assertTrue("rint of (1.5707963267948966):",
                    Double.isNaN(
                        ((Double) rintFunction.getValue(null)).doubleValue()));
            } else {
                assertEquals("rint of (1.5707963267948966):",
                    (double) Math.rint(1.5707963267948966),
                    ((Double) rintFunction.getValue(null)).doubleValue(),
                    0.00001);
            }
        } catch (FactoryConfigurationError e) {
            e.printStackTrace();
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    public void testround() {
        try {
            FunctionExpression roundFunction = filterFactory
                .createFunctionExpression("round");
            assertEquals("Name is, ", "round", roundFunction.getName());
            assertEquals("Number of arguments, ", 1, roundFunction.getArgCount());

            Expression[] expressions = new Expression[1];
            roundFunction.setArgs(expressions);
            expressions[0] = literal_1;
            assertEquals("round of (1.0):", (int) Math.round(1.0),
                ((Integer) roundFunction.getValue(null)).intValue(), 0.00001);
            expressions[0] = literal_m1;
            assertEquals("round of (-1.0):", (int) Math.round(-1.0),
                ((Integer) roundFunction.getValue(null)).intValue(), 0.00001);
            expressions[0] = literal_2;
            assertEquals("round of (2.0):", (int) Math.round(2.0),
                ((Integer) roundFunction.getValue(null)).intValue(), 0.00001);
            expressions[0] = literal_m2;
            assertEquals("round of (-2.0):", (int) Math.round(-2.0),
                ((Integer) roundFunction.getValue(null)).intValue(), 0.00001);
            expressions[0] = literal_pi;
            assertEquals("round of (3.141592653589793):",
                (int) Math.round(3.141592653589793),
                ((Integer) roundFunction.getValue(null)).intValue(), 0.00001);
            expressions[0] = literal_05pi;
            assertEquals("round of (1.5707963267948966):",
                (int) Math.round(1.5707963267948966),
                ((Integer) roundFunction.getValue(null)).intValue(), 0.00001);
        } catch (FactoryConfigurationError e) {
            e.printStackTrace();
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    public void testround_2() {
        try {
            FunctionExpression round_2Function = filterFactory
                .createFunctionExpression("round_2");
            assertEquals("Name is, ", "round_2", round_2Function.getName());
            assertEquals("Number of arguments, ", 1,
                round_2Function.getArgCount());

            Expression[] expressions = new Expression[1];
            round_2Function.setArgs(expressions);
            expressions[0] = literal_1;
            assertEquals("round of (1.0):", (long) Math.round(1.0),
                ((Long) round_2Function.getValue(null)).longValue(), 0.00001);
            expressions[0] = literal_m1;
            assertEquals("round of (-1.0):", (long) Math.round(-1.0),
                ((Long) round_2Function.getValue(null)).longValue(), 0.00001);
            expressions[0] = literal_2;
            assertEquals("round of (2.0):", (long) Math.round(2.0),
                ((Long) round_2Function.getValue(null)).longValue(), 0.00001);
            expressions[0] = literal_m2;
            assertEquals("round of (-2.0):", (long) Math.round(-2.0),
                ((Long) round_2Function.getValue(null)).longValue(), 0.00001);
            expressions[0] = literal_pi;
            assertEquals("round of (3.141592653589793):",
                (long) Math.round(3.141592653589793),
                ((Long) round_2Function.getValue(null)).longValue(), 0.00001);
            expressions[0] = literal_05pi;
            assertEquals("round of (1.5707963267948966):",
                (long) Math.round(1.5707963267948966),
                ((Long) round_2Function.getValue(null)).longValue(), 0.00001);
        } catch (FactoryConfigurationError e) {
            e.printStackTrace();
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    public void testtoDegrees() {
        try {
            FunctionExpression toDegreesFunction = filterFactory
                .createFunctionExpression("toDegrees");
            assertEquals("Name is, ", "toDegrees", toDegreesFunction.getName());
            assertEquals("Number of arguments, ", 1,
                toDegreesFunction.getArgCount());

            Expression[] expressions = new Expression[1];
            toDegreesFunction.setArgs(expressions);
            expressions[0] = literal_1;

            double good0 = Math.toDegrees(1.0);

            if (Double.isNaN(good0)) {
                assertTrue("toDegrees of (1.0):",
                    Double.isNaN(
                        ((Double) toDegreesFunction.getValue(null)).doubleValue()));
            } else {
                assertEquals("toDegrees of (1.0):",
                    (double) Math.toDegrees(1.0),
                    ((Double) toDegreesFunction.getValue(null)).doubleValue(),
                    0.00001);
            }

            expressions[0] = literal_m1;

            double good1 = Math.toDegrees(-1.0);

            if (Double.isNaN(good1)) {
                assertTrue("toDegrees of (-1.0):",
                    Double.isNaN(
                        ((Double) toDegreesFunction.getValue(null)).doubleValue()));
            } else {
                assertEquals("toDegrees of (-1.0):",
                    (double) Math.toDegrees(-1.0),
                    ((Double) toDegreesFunction.getValue(null)).doubleValue(),
                    0.00001);
            }

            expressions[0] = literal_2;

            double good2 = Math.toDegrees(2.0);

            if (Double.isNaN(good2)) {
                assertTrue("toDegrees of (2.0):",
                    Double.isNaN(
                        ((Double) toDegreesFunction.getValue(null)).doubleValue()));
            } else {
                assertEquals("toDegrees of (2.0):",
                    (double) Math.toDegrees(2.0),
                    ((Double) toDegreesFunction.getValue(null)).doubleValue(),
                    0.00001);
            }

            expressions[0] = literal_m2;

            double good3 = Math.toDegrees(-2.0);

            if (Double.isNaN(good3)) {
                assertTrue("toDegrees of (-2.0):",
                    Double.isNaN(
                        ((Double) toDegreesFunction.getValue(null)).doubleValue()));
            } else {
                assertEquals("toDegrees of (-2.0):",
                    (double) Math.toDegrees(-2.0),
                    ((Double) toDegreesFunction.getValue(null)).doubleValue(),
                    0.00001);
            }

            expressions[0] = literal_pi;

            double good4 = Math.toDegrees(3.141592653589793);

            if (Double.isNaN(good4)) {
                assertTrue("toDegrees of (3.141592653589793):",
                    Double.isNaN(
                        ((Double) toDegreesFunction.getValue(null)).doubleValue()));
            } else {
                assertEquals("toDegrees of (3.141592653589793):",
                    (double) Math.toDegrees(3.141592653589793),
                    ((Double) toDegreesFunction.getValue(null)).doubleValue(),
                    0.00001);
            }

            expressions[0] = literal_05pi;

            double good5 = Math.toDegrees(1.5707963267948966);

            if (Double.isNaN(good5)) {
                assertTrue("toDegrees of (1.5707963267948966):",
                    Double.isNaN(
                        ((Double) toDegreesFunction.getValue(null)).doubleValue()));
            } else {
                assertEquals("toDegrees of (1.5707963267948966):",
                    (double) Math.toDegrees(1.5707963267948966),
                    ((Double) toDegreesFunction.getValue(null)).doubleValue(),
                    0.00001);
            }
        } catch (FactoryConfigurationError e) {
            e.printStackTrace();
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    public void testtoRadians() {
        try {
            FunctionExpression toRadiansFunction = filterFactory
                .createFunctionExpression("toRadians");
            assertEquals("Name is, ", "toRadians", toRadiansFunction.getName());
            assertEquals("Number of arguments, ", 1,
                toRadiansFunction.getArgCount());

            Expression[] expressions = new Expression[1];
            toRadiansFunction.setArgs(expressions);
            expressions[0] = literal_1;

            double good0 = Math.toRadians(1.0);

            if (Double.isNaN(good0)) {
                assertTrue("toRadians of (1.0):",
                    Double.isNaN(
                        ((Double) toRadiansFunction.getValue(null)).doubleValue()));
            } else {
                assertEquals("toRadians of (1.0):",
                    (double) Math.toRadians(1.0),
                    ((Double) toRadiansFunction.getValue(null)).doubleValue(),
                    0.00001);
            }

            expressions[0] = literal_m1;

            double good1 = Math.toRadians(-1.0);

            if (Double.isNaN(good1)) {
                assertTrue("toRadians of (-1.0):",
                    Double.isNaN(
                        ((Double) toRadiansFunction.getValue(null)).doubleValue()));
            } else {
                assertEquals("toRadians of (-1.0):",
                    (double) Math.toRadians(-1.0),
                    ((Double) toRadiansFunction.getValue(null)).doubleValue(),
                    0.00001);
            }

            expressions[0] = literal_2;

            double good2 = Math.toRadians(2.0);

            if (Double.isNaN(good2)) {
                assertTrue("toRadians of (2.0):",
                    Double.isNaN(
                        ((Double) toRadiansFunction.getValue(null)).doubleValue()));
            } else {
                assertEquals("toRadians of (2.0):",
                    (double) Math.toRadians(2.0),
                    ((Double) toRadiansFunction.getValue(null)).doubleValue(),
                    0.00001);
            }

            expressions[0] = literal_m2;

            double good3 = Math.toRadians(-2.0);

            if (Double.isNaN(good3)) {
                assertTrue("toRadians of (-2.0):",
                    Double.isNaN(
                        ((Double) toRadiansFunction.getValue(null)).doubleValue()));
            } else {
                assertEquals("toRadians of (-2.0):",
                    (double) Math.toRadians(-2.0),
                    ((Double) toRadiansFunction.getValue(null)).doubleValue(),
                    0.00001);
            }

            expressions[0] = literal_pi;

            double good4 = Math.toRadians(3.141592653589793);

            if (Double.isNaN(good4)) {
                assertTrue("toRadians of (3.141592653589793):",
                    Double.isNaN(
                        ((Double) toRadiansFunction.getValue(null)).doubleValue()));
            } else {
                assertEquals("toRadians of (3.141592653589793):",
                    (double) Math.toRadians(3.141592653589793),
                    ((Double) toRadiansFunction.getValue(null)).doubleValue(),
                    0.00001);
            }

            expressions[0] = literal_05pi;

            double good5 = Math.toRadians(1.5707963267948966);

            if (Double.isNaN(good5)) {
                assertTrue("toRadians of (1.5707963267948966):",
                    Double.isNaN(
                        ((Double) toRadiansFunction.getValue(null)).doubleValue()));
            } else {
                assertEquals("toRadians of (1.5707963267948966):",
                    (double) Math.toRadians(1.5707963267948966),
                    ((Double) toRadiansFunction.getValue(null)).doubleValue(),
                    0.00001);
            }
        } catch (FactoryConfigurationError e) {
            e.printStackTrace();
            fail("Unexpected exception: " + e.getMessage());
        }
    }

}
