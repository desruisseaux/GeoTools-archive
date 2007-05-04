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
package org.geotools.filter.visitor;

import junit.framework.TestCase;

import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.filter.IllegalFilterException;
import org.opengis.filter.And;
import org.opengis.filter.Filter;
import org.opengis.filter.PropertyIsGreaterThan;
import org.opengis.filter.PropertyIsLessThan;


/**
 * Unit test for DuplicatorFilterVisitor.
 *
 * @author Cory Horner, Refractions Research Inc.
 * @source $URL: http://svn.geotools.org/geotools/trunk/gt/modules/library/main/src/test/java/org/geotools/filter/visitor/DuplicatorFilterVisitorTest.java $
 */
public class DuplicateFilterVisitorTest extends TestCase {

    private org.opengis.filter.FilterFactory fac;

	public DuplicateFilterVisitorTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        fac = CommonFactoryFinder.getFilterFactory(GeoTools.getDefaultHints());
    }
    
    public void testLogicFilterDuplication() throws IllegalFilterException {
    	//create a filter
    	PropertyIsGreaterThan greater = fac.greater(fac.literal(2), fac.literal(1));
    	PropertyIsLessThan less = fac.less(fac.literal(3), fac.literal(4));
    	And and = fac.and(greater, less);
    	
    	//duplicate it
    	DuplicatingFilterVisitor visitor = new DuplicatingFilterVisitor();
    	Filter newFilter = (Filter) and.accept(visitor, fac);

    	//compare it
    	assertNotNull(newFilter);
    	assertEquals( and, newFilter );
    }
    
}
