/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2005-2006, Geotools Project Managment Committee (PMC)
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
package org.geotools.filter.function;

import org.geotools.filter.FunctionExpression;

public class ClassificationFunctionTest extends FunctionTestSupport {

    public ClassificationFunctionTest(String testName) {
        super(testName);
    }
    
    public void testDecimalPlaces() throws Exception{
    	System.out.println("testDecimalPlaces");
    	FunctionExpression func = fac.createFunctionExpression("EqualInterval");
        EqualIntervalFunction eif = (EqualIntervalFunction) func;
        int val = eif.decimalPlaces(100.0);
        System.out.println("width 100.0 truncate at "+val+" decimal place(s)");
        assertTrue(val == 0);
        val = eif.decimalPlaces(1.1);
        System.out.println("width 1.1 truncate at "+val+" decimal place(s)");
        assertTrue(val == 1);
        val = eif.decimalPlaces(0.9);
        System.out.println("width 0.9 truncate at "+val+" decimal place(s)");
        assertTrue(val == 1);
        val = eif.decimalPlaces(0.1);
        System.out.println("width 0.1 truncate at "+val+" decimal place(s)");
        assertTrue(val == 2);
        val = eif.decimalPlaces(0.01);
        System.out.println("width 0.01 truncate at "+val+" decimal place(s)");
        assertTrue(val == 3);
        val = eif.decimalPlaces(0.001);
        System.out.println("width 0.001 truncate at "+val+" decimal place(s)");
        assertTrue(val == 4);
    }
    
    public void testRound() throws Exception{
    	System.out.println("testRound");
    	FunctionExpression func = fac.createFunctionExpression("Quantile");
        QuantileFunction classifier = (QuantileFunction) func;
        double val = classifier.round(100.0, 0);
        System.out.println("round 0 digits, 100.0 --> "+val);
        assertEquals(100.0, val, 0);
        val = classifier.round(1.12, 1);
        System.out.println("round 1 digit, 1.12 --> "+val);
        assertEquals(1.1, val, 0);
        val = classifier.round(0.34523, 2);
        System.out.println("round 2 digits, 0.34523 --> "+val);
        assertEquals(0.35, val, 0);
    }
}
