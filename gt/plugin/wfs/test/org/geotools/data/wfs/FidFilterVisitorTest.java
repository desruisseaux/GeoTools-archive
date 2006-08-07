/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004-2006, Geotools Project Managment Committee (PMC)
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
package org.geotools.data.wfs;

import java.util.HashMap;
import java.util.Map;

import org.geotools.filter.BetweenFilter;
import org.geotools.filter.CompareFilter;
import org.geotools.filter.FidFilter;
import org.geotools.filter.Filter;
import org.geotools.filter.FilterCapabilities;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.FilterFactoryFinder;
import org.geotools.filter.FilterType;
import org.geotools.filter.GeometryFilter;
import org.geotools.filter.IllegalFilterException;
import org.geotools.filter.LikeFilter;
import org.geotools.filter.LogicFilter;
import org.geotools.filter.NullFilter;
import org.geotools.filter.visitor.PostPreProcessFilterSplittingVisitor;

import com.vividsolutions.jts.geom.Envelope;

import junit.framework.TestCase;

/**
 * @author Jesse
 *
 */
public class FidFilterVisitorTest extends TestCase {

	private FidFilterVisitor visitor;
	private FilterFactory fac=FilterFactoryFinder.createFilterFactory();
	private Map state;
	
	protected void setUp() throws Exception {
		state=new HashMap();
		state.put("new1","final1");
		state.put("new2","final2");
		state.put("new3","final3");
		
		visitor=new FidFilterVisitor(state); 
	}

	/**
	 * Test method for {@link org.geotools.data.wfs.FidFilterVisitor#visit(org.geotools.filter.LogicFilter)}.
	 */
	public void testVisitLogicFilterOR() throws Exception {
		LogicFilter f=fac.createLogicFilter(FilterType.LOGIC_OR);
		f.addFilter(fac.createFidFilter("new1"));
		f.addFilter(fac.createFidFilter("new2"));
		visitor.visit(f);
		
		assertEquals(fac.createFidFilter("final1").or(fac.createFidFilter("final2")), visitor.getProcessedFilter());
	}

	/**
	 * Test method for {@link org.geotools.data.wfs.FidFilterVisitor#visit(org.geotools.filter.LogicFilter)}.
	 */
	public void testVisitLogicFilterAND() throws Exception {
		LogicFilter f=fac.createLogicFilter(FilterType.LOGIC_AND);
		f.addFilter(fac.createFidFilter("new1"));
		f.addFilter(fac.createFidFilter("new2"));
		visitor.visit(f);
		
		assertEquals(fac.createFidFilter("final1").and(fac.createFidFilter("final2")), visitor.getProcessedFilter());
	}
	
	/**
	 * Test method for {@link org.geotools.data.wfs.FidFilterVisitor#visit(org.geotools.filter.FidFilter)}.
	 */
	public void testVisitLogicFilterNOT() {
		FidFilter f = fac.createFidFilter("new1");
		visitor.visit((LogicFilter)f.not());
		assertEquals(fac.createFidFilter("final1").not(), visitor.getProcessedFilter());
	}

	/**
	 * Test method for {@link org.geotools.data.wfs.FidFilterVisitor#visit(org.geotools.filter.FidFilter)}.
	 */
	public void testVisitFidFilter() {
		FidFilter f = fac.createFidFilter("new1");
		visitor.visit(f);
		assertEquals(fac.createFidFilter("final1"), visitor.getProcessedFilter());
	}
	public void testVisitFidFilter2() {
		FidFilter f = fac.createFidFilter("other");
		visitor.visit(f);
		assertEquals(fac.createFidFilter("other"), visitor.getProcessedFilter());
	}
	public void testVisitFidFilter3() {
		FidFilter f = fac.createFidFilter();
		f.addFid("new1");
		f.addFid("new2");
		f.addFid("new3");
		f.addFid("other");
		visitor.visit(f);
		
		FidFilter expected = fac.createFidFilter();
		expected.addFid("final1");
		expected.addFid("final2");
		expected.addFid("final3");
		expected.addFid("other");
		
		assertEquals(expected, visitor.getProcessedFilter());
	}

	/**
	 * Test method for {@link org.geotools.data.wfs.FidFilterVisitor#visit(org.geotools.filter.Filter)}.
	 */
	public void testVisitFilter() {
		Filter f = fac.createFidFilter("new1");
		visitor.visit(f);
		assertEquals(fac.createFidFilter("final1"), visitor.getProcessedFilter());
		
		Filter filter=fac.createFidFilter("new2");
		filter=filter.and(fac.createFidFilter("new3"));
		visitor=new FidFilterVisitor(state);
		visitor.visit(filter);
		
		assertEquals(fac.createFidFilter("final2").and(fac.createFidFilter("final3")), visitor.getProcessedFilter());
	}
	

	public void testVisitBetweenFilter() throws IllegalFilterException {
		BetweenFilter filter = fac.createBetweenFilter();
		filter.addLeftValue(fac.createLiteralExpression("1"));
		filter.addMiddleValue(fac.createLiteralExpression("1"));
		filter.addRightValue(fac.createLiteralExpression("1"));
		filter.accept(visitor);
		assertSame(filter, visitor.getProcessedFilter());
	}

	public void testVisitCompareFilter()  throws IllegalFilterException {
		CompareFilter filter = fac.createCompareFilter(FilterType.COMPARE_EQUALS);
		filter.addLeftValue(fac.createLiteralExpression("1"));
		filter.addRightValue(fac.createLiteralExpression("1"));
		filter.accept(visitor);
		assertSame(filter, visitor.getProcessedFilter());
	}

	public void testVisitGeometryFilter() throws IllegalFilterException  {
		GeometryFilter filter = fac.createGeometryFilter(FilterType.GEOMETRY_BBOX);
		filter.addLeftGeometry(fac.createBBoxExpression(new Envelope(0,10,0,10)));
		filter.addLeftGeometry(fac.createBBoxExpression(new Envelope(0,10,0,10)));
		filter.accept(visitor);
		assertSame(filter, visitor.getProcessedFilter());
	}

	public void testVisitLikeFilter() throws IllegalFilterException  {
		LikeFilter filter = fac.createLikeFilter();
		filter.setPattern("patt", "", "", "");
		filter.accept(visitor);
		assertSame(filter, visitor.getProcessedFilter());
	}

	public void testVisitNullFilter() throws IllegalFilterException  {
		NullFilter filter = fac.createNullFilter();
		filter.accept(visitor);
		assertSame(filter, visitor.getProcessedFilter());
	}
	
	public void testFilterNONE() throws Exception {
		Filter.NONE.accept(visitor);
		assertSame(Filter.NONE, visitor.getProcessedFilter());
	}
	public void testFilterALL() throws Exception {
		Filter.ALL.accept(visitor);
		assertSame(Filter.ALL, visitor.getProcessedFilter());		
	}

	public void testFilterUnchanged() throws Exception {
		CompareFilter c1=fac.createCompareFilter(FilterType.COMPARE_EQUALS);
		c1.addLeftValue(fac.createAttributeExpression("eventstatus"));
		c1.addRightValue(fac.createLiteralExpression("deleted"));
		
		CompareFilter c2 = fac.createCompareFilter(FilterType.COMPARE_EQUALS);
		c2.addLeftValue(fac.createAttributeExpression("eventtype"));
		c2.addRightValue(fac.createLiteralExpression("road hazard"));
		
		CompareFilter c3 = fac.createCompareFilter(FilterType.COMPARE_EQUALS);
		c3.addLeftValue(fac.createAttributeExpression("eventtype"));
		c3.addRightValue(fac.createLiteralExpression("area warning"));

		GeometryFilter g = fac.createGeometryFilter(FilterType.GEOMETRY_BBOX);
		g.addLeftGeometry(fac.createAttributeExpression("geom"));
		g.addRightGeometry(fac.createBBoxExpression(new Envelope(0,180,0,90)));
		
		Filter f = c2.or(c3);
		f=c1.not().and(f);
		f=f.and(g);
		
		f.accept(visitor);
		
		assertEquals(f, visitor.getProcessedFilter());

	}
}
