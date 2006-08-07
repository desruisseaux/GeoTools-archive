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

import org.geotools.filter.BetweenFilter;
import org.geotools.filter.CompareFilter;
import org.geotools.filter.FidFilter;
import org.geotools.filter.Filter;
import org.geotools.filter.FilterCapabilities;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.FilterType;
import org.geotools.filter.GeometryFilter;
import org.geotools.filter.LikeFilter;
import org.geotools.filter.NullFilter;
import org.geotools.filter.function.FilterFunction_geometryType;

import com.vividsolutions.jts.geom.Envelope;

public class PostPreProcessFilterSplittingVisitorTest extends AbstractPostPreProcessFilterSplittingVisitorTests {

	public void testVisitBetweenFilter() throws Exception {
		BetweenFilter filter = filterFactory.createBetweenFilter();
		filter.addLeftValue(filterFactory.createLiteralExpression(0));
		filter.addRightValue(filterFactory.createLiteralExpression(4));
		filter.addMiddleValue(filterFactory.createAttributeExpression(numAtt));
		
		runTest(filter, FilterCapabilities.BETWEEN, numAtt);
	}


	public void testNullTransactionAccessor() throws Exception {
		accessor=null;
		Filter f1 = createEqualsCompareFilter(nameAtt, "david");
		Filter f2 = createEqualsCompareFilter(nameAtt, "david");

		runTest(f1.and(f2), (FilterCapabilities.SIMPLE_COMPARISONS|FilterCapabilities.LOGICAL), nameAtt);
	}
	
	public void testVisitLogicalANDFilter() throws Exception{
		Filter f1 = createEqualsCompareFilter(nameAtt, "david");
		Filter f2 = createEqualsCompareFilter(nameAtt, "david");

		runTest(f1.and(f2), (FilterCapabilities.SIMPLE_COMPARISONS|FilterCapabilities.LOGICAL), nameAtt);
	}
	public void testVisitLogicalNOTFilter() throws Exception{
		Filter f1 = createEqualsCompareFilter(nameAtt, "david");

		runTest(f1.not(), (FilterCapabilities.SIMPLE_COMPARISONS|FilterCapabilities.LOGICAL), nameAtt);
	}

	public void testVisitLogicalORFilter() throws Exception{
		Filter f1 = createEqualsCompareFilter(nameAtt, "david");
		Filter f2 = createEqualsCompareFilter("name", "jose");

		Filter orFilter = f1.or(f2);
		runTest(orFilter, (FilterCapabilities.SIMPLE_COMPARISONS|FilterCapabilities.LOGICAL), nameAtt);
		
		filterCapabilitiesMask=new FilterCapabilities();
		filterCapabilitiesMask.addType(FilterCapabilities.SIMPLE_COMPARISONS);
		filterCapabilitiesMask.addType(FilterCapabilities.LOGICAL);
		
		visitor=newVisitor();
		
		f2=createGeometryFilter(FilterType.GEOMETRY_BBOX);
		orFilter = f1.or(f2);
		orFilter.accept(visitor);
		
		// f1 could be pre-processed but since f2 can't all the processing has to be done on the client side :-(
		assertEquals(orFilter, visitor.getFilterPost());
		assertEquals(Filter.NONE, visitor.getFilterPre());
	}


	
	public void testVisitCompareFilter() throws Exception{
		CompareFilter f = createEqualsCompareFilter(nameAtt, "david");

		runTest(f, FilterCapabilities.SIMPLE_COMPARISONS, nameAtt);
	}

	/**
	 * an update is in transaction that modifies an  attribute that NOT is referenced in the query
	 */
	public void testVisitCompareFilterWithUpdateDifferentAttribute() throws Exception {
		filterCapabilitiesMask.addType(FilterCapabilities.SIMPLE_COMPARISONS);
		CompareFilter f = createEqualsCompareFilter(nameAtt, "david");

		CompareFilter updateFilter = createEqualsCompareFilter(nameAtt, "jose");

		accessor.setUpdate(geomAtt, updateFilter);

		f.accept(visitor);

		assertEquals(visitor.getFilterPost().toString(), Filter.NONE, visitor
				.getFilterPost());
		assertEquals(visitor.getFilterPre().toString(), f,
				visitor.getFilterPre());
	}
	
	public void testVisitLikeFilter() throws Exception {
		LikeFilter filter = filterFactory.createLikeFilter();
		filter.setValue(filterFactory.createAttributeExpression(nameAtt));
		filter.setPattern("j*", "*", "?", "\\");
		runTest(filter, FilterCapabilities.LIKE, nameAtt);
	}

	public void testVisitNullFilter() throws Exception {
		NullFilter filter = filterFactory.createNullFilter();
		
		filter.nullCheckValue(filterFactory.createAttributeExpression(nameAtt));
		runTest(filter, FilterCapabilities.NULL_CHECK, nameAtt);
	}

	public void testVisitFidFilter() throws Exception {
		FidFilter filter = filterFactory.createFidFilter("david");
		filter.accept(visitor);
		
		assertEquals(Filter.NONE, visitor.getFilterPost());
		assertEquals(filter, visitor.getFilterPre());
	}

	public void testFunctionFilter() throws Exception {
		filterCapabilitiesMask.addType(FilterCapabilities.LOGICAL);
		filterCapabilitiesMask.addType(FilterCapabilities.SIMPLE_COMPARISONS);
		filterCapabilitiesMask.addType(FilterCapabilities.SPATIAL_BBOX);
		
		CompareFilter filter = createFunctionFilter();

		filter.accept(visitor);

		assertEquals(filter, visitor.getFilterPost());
		assertEquals(Filter.NONE, visitor.getFilterPre());
		
		filterCapabilitiesMask.addType(FilterFunction_geometryType.class);
		visitor=newVisitor();
		
		filter.accept(visitor);

		assertEquals(Filter.NONE, visitor.getFilterPost());
		assertEquals(filter, visitor.getFilterPre());
	}
	
	public void testFunctionANDGeometryFilter() throws Exception{
		filterCapabilitiesMask.addType(FilterCapabilities.LOGICAL);
		filterCapabilitiesMask.addType(FilterCapabilities.SIMPLE_COMPARISONS);
		filterCapabilitiesMask.addType(FilterCapabilities.SPATIAL_BBOX);
		
		Filter funtionFilter = createFunctionFilter();
		GeometryFilter geomFilter= createGeometryFilter(FilterType.GEOMETRY_BBOX);
		
		Filter andFilter = funtionFilter.and(geomFilter);

		andFilter.accept(visitor);

		assertEquals(funtionFilter, visitor.getFilterPost());
		assertEquals(geomFilter, visitor.getFilterPre());
		
		filterCapabilitiesMask.addType(FilterFunction_geometryType.class);
		visitor=newVisitor();
		
		andFilter.accept(visitor);

		assertEquals(Filter.NONE, visitor.getFilterPost());
		assertEquals(andFilter, visitor.getFilterPre());
	}

	public void testFunctionORGeometryFilter() throws Exception{
		filterCapabilitiesMask.addType(FilterCapabilities.LOGICAL);
		filterCapabilitiesMask.addType(FilterCapabilities.SIMPLE_COMPARISONS);
		filterCapabilitiesMask.addType(FilterCapabilities.SPATIAL_BBOX);
		
		Filter funtionFilter = createFunctionFilter();
		GeometryFilter geomFilter= createGeometryFilter(FilterType.GEOMETRY_BBOX);
		
		Filter orFilter = funtionFilter.or(geomFilter);

		orFilter.accept(visitor);

		assertEquals(Filter.NONE, visitor.getFilterPre());
		assertEquals(orFilter, visitor.getFilterPost());
		
		filterCapabilitiesMask.addType(FilterFunction_geometryType.class);
		visitor=newVisitor();
		
		orFilter.accept(visitor);

		assertEquals(Filter.NONE, visitor.getFilterPost());
		assertEquals(orFilter, visitor.getFilterPre());

	}
	public void testFunctionNOTFilter() throws Exception {
		filterCapabilitiesMask.addType(FilterCapabilities.LOGICAL);
		filterCapabilitiesMask.addType(FilterCapabilities.SIMPLE_COMPARISONS);
		filterCapabilitiesMask.addType(FilterCapabilities.SPATIAL_BBOX);
		
		Filter funtionFilter = createFunctionFilter();

		Filter not = funtionFilter.not();
		not.accept(visitor);

		assertEquals(not, visitor.getFilterPost());
		assertEquals(Filter.NONE, visitor.getFilterPre());
		
		filterCapabilitiesMask.addType(FilterFunction_geometryType.class);
		visitor=newVisitor();
		
		not.accept(visitor);

		assertEquals(Filter.NONE, visitor.getFilterPost());
		assertEquals(not, visitor.getFilterPre());
	}

	public void testNullParentNullAccessor() throws Exception {
		filterCapabilitiesMask.addType(FilterCapabilities.LOGICAL);
		filterCapabilitiesMask.addType(FilterCapabilities.SIMPLE_COMPARISONS);
		filterCapabilitiesMask.addType(FilterCapabilities.SPATIAL_BBOX);
		filterCapabilitiesMask.addType(FilterFunction_geometryType.class);
		
		Filter funtionFilter = createFunctionFilter();
		GeometryFilter geomFilter= createGeometryFilter(FilterType.GEOMETRY_BBOX);
		
		Filter orFilter = funtionFilter.or(geomFilter);
		visitor=new PostPreProcessFilterSplittingVisitor(new FilterCapabilities(), null, null);
		orFilter.accept(visitor);

		assertEquals(Filter.NONE, visitor.getFilterPre());
		assertEquals(orFilter, visitor.getFilterPost());

		visitor=new PostPreProcessFilterSplittingVisitor(filterCapabilitiesMask, null, null);
		
		orFilter.accept(visitor);

		assertEquals(Filter.NONE, visitor.getFilterPost());
		assertEquals(orFilter, visitor.getFilterPre());
	}
	
	public void testComplicatedOrFilter() throws Exception {
		CompareFilter c1=filterFactory.createCompareFilter(FilterType.COMPARE_EQUALS);
		c1.addLeftValue(filterFactory.createAttributeExpression("eventstatus"));
		c1.addRightValue(filterFactory.createLiteralExpression("deleted"));
		
		CompareFilter c2 = filterFactory.createCompareFilter(FilterType.COMPARE_EQUALS);
		c2.addLeftValue(filterFactory.createAttributeExpression("eventtype"));
		c2.addRightValue(filterFactory.createLiteralExpression("road hazard"));
		
		CompareFilter c3 = filterFactory.createCompareFilter(FilterType.COMPARE_EQUALS);
		c3.addLeftValue(filterFactory.createAttributeExpression("eventtype"));
		c3.addRightValue(filterFactory.createLiteralExpression("area warning"));

		GeometryFilter g = filterFactory.createGeometryFilter(FilterType.GEOMETRY_BBOX);
		g.addLeftGeometry(filterFactory.createAttributeExpression("geom"));
		g.addRightGeometry(filterFactory.createBBoxExpression(new Envelope(0,180,0,90)));
		
		Filter f = c2.or(c3);
		f=c1.not().and(f);
		f=f.and(g);
		
		filterCapabilitiesMask.addType(FilterCapabilities.LOGICAL);
		filterCapabilitiesMask.addType(FilterCapabilities.SIMPLE_COMPARISONS);
		filterCapabilitiesMask.addType(FilterCapabilities.SPATIAL_BBOX);
		
		visitor=new PostPreProcessFilterSplittingVisitor(filterCapabilitiesMask, null, null);
		f.accept(visitor);
		
		assertEquals(f, visitor.getFilterPre());
		assertEquals(Filter.NONE, visitor.getFilterPost());
		
		visitor=new PostPreProcessFilterSplittingVisitor( filterCapabilitiesMask, null, new ClientTransactionAccessor(){

			public Filter getDeleteFilter() {
				return null;
			}

			public Filter getUpdateFilter(String attributePath) {
				if( attributePath.equals("eventtype") ){
					return filterFactory.createFidFilter("fid");
				}
				return null;
			}
			
		});

		f.accept(visitor);
		
		assertEquals(f, visitor.getFilterPost());
		assertEquals(f.or(filterFactory.createFidFilter("fid")), visitor.getFilterPre());
	}
	
}
