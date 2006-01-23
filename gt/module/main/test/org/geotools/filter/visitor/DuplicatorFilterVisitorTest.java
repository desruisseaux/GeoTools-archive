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
package org.geotools.filter.visitor;

import junit.framework.TestCase;

import org.geotools.filter.CompareFilter;
import org.geotools.filter.Filter;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.FilterFactoryFinder;
import org.geotools.filter.IllegalFilterException;
import org.geotools.filter.LogicFilter;


/**
 * Unit test for DuplicatorFilterVisitor.
 *
 * @author Cory Horner, Refractions Research Inc.
 * @source $URL$
 */
public class DuplicatorFilterVisitorTest extends TestCase {
    FilterFactory fac;

    public DuplicatorFilterVisitorTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        fac = FilterFactoryFinder.createFilterFactory();
    }
    
    public void testLogicFilterDuplication() throws IllegalFilterException {
    	//create a filter
    	LogicFilter oldFilter = fac.createLogicFilter(Filter.LOGIC_AND);
    	CompareFilter filter1 = fac.createCompareFilter(Filter.COMPARE_GREATER_THAN);
    	filter1.addLeftValue(fac.createLiteralExpression(2));
    	filter1.addRightValue(fac.createLiteralExpression(1));
    	oldFilter.addFilter(filter1);
    	CompareFilter filter2 = fac.createCompareFilter(Filter.COMPARE_GREATER_THAN);
    	filter2.addLeftValue(fac.createLiteralExpression(4));
    	filter2.addRightValue(fac.createLiteralExpression(3));
    	oldFilter.addFilter(filter2);
    	
    	//duplicate it
    	DuplicatorFilterVisitor visitor = new DuplicatorFilterVisitor(fac);
    	oldFilter.accept(visitor);
    	Filter newFilter = (Filter) visitor.getCopy();

    	//compare it
    	assertNotNull(newFilter);
    	//TODO: a decent comparison
    }
}
