package org.geotools.data.wfs;

import java.util.HashMap;
import java.util.Map;

import org.geotools.data.DataUtilities;
import org.geotools.feature.SchemaException;
import org.geotools.filter.CompareFilter;
import org.geotools.filter.FidFilter;
import org.geotools.filter.Filter;
import org.geotools.filter.FilterCapabilitiesMask;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.FilterFactoryFinder;
import org.geotools.filter.FilterType;
import org.geotools.filter.GeometryFilter;
import org.geotools.filter.IllegalFilterException;
import org.geotools.filter.expression.Expression;
import org.geotools.filter.function.FilterFunction_geometryType;

import com.vividsolutions.jts.geom.Envelope;

import junit.framework.TestCase;

public class AbstractWFSFilterVisitorTests extends TestCase {
	protected FilterFactory filterFactory = FilterFactoryFinder.createFilterFactory();
	protected WFSTransactionState transactionState;
	protected WFSFilterVisitor visitor;
	protected FilterCapabilitiesMask filterCapabilitiesMask;
	protected static final String typeName = "test";
	protected static final String geomAtt = "geom";
	protected static final String nameAtt = "name";
	protected static final String numAtt = "num";

	protected void setUp() throws Exception {
		super.setUp();
		transactionState = new WFSTransactionState(null);
		filterCapabilitiesMask = new FilterCapabilitiesMask();
		visitor=newVisitor();
	}

	protected WFSFilterVisitor newVisitor() throws SchemaException {
		return new WFSFilterVisitor(filterCapabilitiesMask, DataUtilities.createType(typeName,geomAtt+":Point,"+nameAtt+":String," +
				numAtt+":int"), transactionState);
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
	 * @param filterTypeMask the constant in {@link FilterCapabilitiesMask} that is equivalent to the FilterType used in filter
	 * @param attToEdit the attribute in filter that is queried.  If null then edit test is not ran.
	 */
	protected void runTest(Filter filter, int filterTypeMask, String attToEdit) throws SchemaException {
		// initialize fields that might be previously modified in current test
		visitor=newVisitor(); 
		filterCapabilitiesMask=new FilterCapabilitiesMask();

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
		
		if (attToEdit != null) {
			// Test when the an update exists that affects the attribute of a
			// feature
			FidFilter updateFilter = filterFactory.createFidFilter("fid");
			Map props = new HashMap();
			props.put(attToEdit, "newValue");
			transactionState.addAction(new Action.UpdateAction(typeName,
					updateFilter, props));

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
