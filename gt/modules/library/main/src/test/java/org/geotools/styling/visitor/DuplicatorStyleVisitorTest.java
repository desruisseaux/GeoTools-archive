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
package org.geotools.styling.visitor;

import junit.framework.TestCase;

import org.geotools.factory.CommonFactoryFinder;
import org.geotools.filter.IllegalFilterException;
import org.geotools.styling.Style;
import org.geotools.styling.StyleBuilder;
import org.geotools.styling.StyleFactory;
import org.geotools.styling.StyleFactoryFinder;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.FilterFactory2;


/**
 * Unit test for DuplicatorStyleVisitor.
 *
 * @author Cory Horner, Refractions Research Inc.
 * @source $URL$
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
        ff = CommonFactoryFinder.getFilterFactory(null);
        sb = new StyleBuilder(sf, ff);
    }
    
    public void testStyleDuplication() throws IllegalFilterException {
    	//create a style
    	Style oldStyle = sb.createStyle("FTSName", sf.createPolygonSymbolizer());
    	oldStyle.getFeatureTypeStyles()[0].setSemanticTypeIdentifiers(new String[] {"simple", "generic:geometry"});
    	//duplicate it
    	DuplicatingStyleVisitor visitor = new DuplicatingStyleVisitor(sf, (FilterFactory2) ff);
    	oldStyle.accept(visitor);
    	Style newStyle = (Style) visitor.getCopy();
    	
    	//compare it
    	assertNotNull(newStyle);
    	assertEquals(2, newStyle.getFeatureTypeStyles()[0].getSemanticTypeIdentifiers().length);
    	//TODO: actually compare it
    }
    
}
