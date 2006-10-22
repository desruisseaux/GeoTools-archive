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

import org.geotools.data.DataUtilities;
import org.geotools.feature.SchemaException;
import org.geotools.filter.CompareFilter;
import org.geotools.filter.Expression;
import org.geotools.filter.FidFilter;
import org.geotools.filter.Filter;
import org.geotools.filter.FilterCapabilities;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.FilterFactoryFinder;
import org.geotools.filter.FilterType;
import org.geotools.filter.GeometryFilter;
import org.geotools.filter.IllegalFilterException;
import org.geotools.filter.function.FilterFunction_geometryType;

import com.vividsolutions.jts.geom.Envelope;

public class AbstractPostPreProcessFilterSplittingVisitorTests extends TestCase {
	public class TestAccessor implements ClientTransactionAccessor {

		private Filter updateFilter;
		private String attribute;

		public Filter getDeleteFilter() {
			return null;
		}

		public Filter getUpdateFilter(String attributePath) {
			if( attributePath.equals(attribute) )
				return updateFilter;
			else
				return null;
		}

		public void setUpdate(String attribute, Filter updateFilter) {
			this.attribute=attribute;
			this.updateFilter=updateFilter;
		}

	}

	protected FilterFactory filterFactory = FilterFactoryFinder.createFilterFactory();
	protected TestAccessor accessor;
	protected PostPreProcessFilterSplittingVisitor visitor;
	protected FilterCapabilities filterCapabilitiesMask;
	protected static final String typeName = "test";
	protected static final String geomAtt = "geom";
	protected static final String nameAtt = "name";
	protected static final String numAtt = "num";

	protected void setUp() throws Exception {
		super.setUp();
		accessor = new TestAccessor();
		filterCapabilitiesMask = new FilterCapabilities();
		visitor=newVisitor();
	}

	protected PostPreProcessFilterSplittingVisitor newVisitor() throws SchemaException {
		return new PostPreProcessFilterSplittingVisitor(filterCapabilitiesMask, DataUtilities.createType(typeName,geomAtt+":Point,"+nameAtt+":String," +
				numAtt+":int"), accessor);
	} 
	
	protected CompareFilter createEqualsCompareFilter(String attr, String value) throws IllegalFilterException {
		CompareFilter f = filterFactory.createCompareFilter(FilterType.COMPARE_EQUALS);
    	f.addLeftValue(filterFactory.createAttributeExpression(attr));
    	f.addRightValue(filterFactory.createLiteralExpression(value));
		return f;
	}

	protected GeometryFilter createGeometryFilter(short filterType) throws IllegalFilterException {
		GeometryFilter filter = filterFactory.createGeometryFilter(filterType);
		filter.addLeftGeometry(filterFactory.createAttributeExpression(geomAtt));
		filter.addRightGeometry(filterFactory.createBBoxExpression(new Envelope(10,20,10,20)));
		return filter;
	}
	
	/**
	 * Runs 3 tests.  1 with out filtercapabilities containing filter type.  1 with filter caps containing filter type
	 * 1 with an edit to the attribute being queried by filter.
	 * @param filter filter to process
	 * @param filterTypeMask the constant in {@link FilterCapabilities} that is equivalent to the FilterType used in filter
	 * @param attToEdit the attribute in filter that is queried.  If null then edit test is not ran.
	 */
	protected void runTest(Filter filter, long filterTypeMask, String attToEdit) throws SchemaException {
		// initialize fields that might be previously modified in current test
		visitor=newVisitor(); 
		filterCapabilitiesMask=new FilterCapabilities();
		if( accessor!=null )
		accessor.setUpdate("",null);

		// Testing when FilterCapabilites indicate that filter type is not supported
		filter.accept(visitor);

		assertEquals(filter, visitor.getFilterPost());
		assertEquals(Filter.NONE, visitor.getFilterPre());
		
		// now filter type is supported
		filterCapabilitiesMask.addType(filterTypeMask);
		visitor=newVisitor();
		
		filter.accept(visitor);
		
		assertEquals(Filter.NONE, visitor.getFilterPost());
		assertEquals(filter, visitor.getFilterPre());
		
		if (attToEdit != null && accessor!=null ) {
			// Test when the an update exists that affects the attribute of a
			// feature
			FidFilter updateFilter = filterFactory.createFidFilter("fid");

			accessor.setUpdate(attToEdit, updateFilter);

			visitor = newVisitor();

			filter.accept(visitor);

			assertEquals(filter, visitor.getFilterPost());
			assertEquals(filter.or(updateFilter), visitor.getFilterPre());
		}
	}

	protected CompareFilter createFunctionFilter() throws Exception {
		FilterFactory factory = FilterFactoryFinder.createFilterFactory();
		FilterFunction_geometryType geomTypeExpr = new FilterFunction_geometryType();
		geomTypeExpr.setArgs(new Expression[] { factory
				.createAttributeExpression("geom") });
	
		CompareFilter filter = factory
				.createCompareFilter(FilterType.COMPARE_EQUALS);
		filter.addLeftValue(geomTypeExpr);
		filter.addRightValue(factory.createLiteralExpression("Polygon"));
		return filter;
	}
	

}
