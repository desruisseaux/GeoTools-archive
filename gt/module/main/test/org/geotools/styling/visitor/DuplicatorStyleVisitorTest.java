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
package org.geotools.styling.visitor;

import junit.framework.TestCase;

import org.geotools.filter.FilterFactory;
import org.geotools.filter.FilterFactoryFinder;
import org.geotools.filter.IllegalFilterException;
import org.geotools.styling.Style;
import org.geotools.styling.StyleBuilder;
import org.geotools.styling.StyleFactory;
import org.geotools.styling.StyleFactoryFinder;


/**
 * Unit test for DuplicatorStyleVisitor.
 *
 * @author Cory Horner, Refractions Research Inc.
 */
public class DuplicatorStyleVisitorTest extends TestCase {
    StyleBuilder sb;
    StyleFactory sf;
    FilterFactory ff;

    public DuplicatorStyleVisitorTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    	sf = StyleFactoryFinder.createStyleFactory();
        ff = FilterFactoryFinder.createFilterFactory();
        sb = new StyleBuilder(sf, ff);
    }
    
    public void testStyleDuplication() throws IllegalFilterException {
    	//create a style
    	Style oldStyle = sb.createStyle("FTSName", sf.createPolygonSymbolizer());
    	
    	//duplicate it
    	DuplicatorStyleVisitor visitor = new DuplicatorStyleVisitor(sf, ff);
    	oldStyle.accept(visitor);
    	Style newStyle = (Style) visitor.getCopy();

    	//compare it
    	assertNotNull(newStyle);
    	//TODO: actually compare it
    }
}
