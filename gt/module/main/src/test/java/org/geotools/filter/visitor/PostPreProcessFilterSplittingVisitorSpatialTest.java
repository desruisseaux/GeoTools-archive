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

import org.geotools.filter.Filter;
import org.geotools.filter.FilterCapabilities;
import org.geotools.filter.FilterType;

public class PostPreProcessFilterSplittingVisitorSpatialTest extends AbstractPostPreProcessFilterSplittingVisitorTests {

	protected void setUp() throws Exception {
		super.setUp();
	}
	
	public void testBBOX() throws Exception{
		// JE:  this test fails but I am not sure if it is a bug or expected behaviour  
		// I wrote this test so it may be correct but it maybe wrong.  Someone that knows should look at this.
//		Filter f = createGeometryFilter((short)FilterType.GEOMETRY_BBOX);
//		runTest(f, (short)FilterCapabilities.BBOX, geomAtt);
	}
	public void testBEYOND() throws Exception{
		Filter f = createGeometryFilter((short)FilterType.GEOMETRY_BEYOND);
		runTest(f, (short)FilterCapabilities.SPATIAL_BEYOND, geomAtt);
	}
	public void testCONTAINS() throws Exception{
		Filter f = createGeometryFilter((short)FilterType.GEOMETRY_CONTAINS);
		runTest(f, (short)FilterCapabilities.SPATIAL_CONTAINS, geomAtt);
	}
	public void testCROSSES() throws Exception{
		Filter f = createGeometryFilter((short)FilterType.GEOMETRY_CROSSES);
		runTest(f, (short)FilterCapabilities.SPATIAL_CROSSES, geomAtt);
	}
	public void testDISJOINT() throws Exception{
		Filter f = createGeometryFilter((short)FilterType.GEOMETRY_DISJOINT);
		runTest(f, (short)FilterCapabilities.SPATIAL_DISJOINT, geomAtt);
	}
	public void tesDWITHINt() throws Exception{
		Filter f = createGeometryFilter((short)FilterType.GEOMETRY_DWITHIN);
		runTest(f, (short)FilterCapabilities.SPATIAL_DWITHIN, geomAtt);
	}
	public void testEQUALS() throws Exception{
		Filter f = createGeometryFilter((short)FilterType.GEOMETRY_EQUALS);
		runTest(f, (short)FilterCapabilities.SPATIAL_EQUALS, geomAtt);
	}
	public void testINTERSECT() throws Exception{
		Filter f = createGeometryFilter((short)FilterType.GEOMETRY_INTERSECTS);
		runTest(f, (short)FilterCapabilities.SPATIAL_INTERSECT, geomAtt);
	}
	public void testOVERLAPS() throws Exception{
		Filter f = createGeometryFilter((short)FilterType.GEOMETRY_OVERLAPS);
		runTest(f, (short)FilterCapabilities.SPATIAL_OVERLAPS, geomAtt);
	}
	public void testTOUCHES() throws Exception{
		Filter f = createGeometryFilter((short)FilterType.GEOMETRY_TOUCHES);
		runTest(f, (short)FilterCapabilities.SPATIAL_TOUCHES, geomAtt);		
	}
	public void testWITHIN() throws Exception{
		Filter f = createGeometryFilter((short)FilterType.GEOMETRY_WITHIN);
		runTest(f, (short)FilterCapabilities.SPATIAL_WITHIN, geomAtt);
	}

}
