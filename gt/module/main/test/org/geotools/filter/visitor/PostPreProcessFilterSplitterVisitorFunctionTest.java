/**
 * 
 */
package org.geotools.filter.visitor;

import org.geotools.filter.CompareFilter;
import org.geotools.filter.Filter;
import org.geotools.filter.FilterCapabilities;
import org.geotools.filter.FilterType;
import org.geotools.filter.expression.Expression;
import org.geotools.filter.function.FilterFunction_geometryType;
import org.geotools.filter.function.math.FilterFunction_abs;

/**
 * Test case where only specific functions are supported.
 * 
 * @author Jesse
 *
 */
public class PostPreProcessFilterSplitterVisitorFunctionTest extends AbstractPostPreProcessFilterSplittingVisitorTests {

	public void testSupportAll() throws Exception {
		CompareFilter filter1 = createFunctionFilter();
		CompareFilter filter2 = filterFactory.createCompareFilter(FilterType.COMPARE_EQUALS);
		filter2.addLeftValue(filterFactory.createAttributeExpression("name"));
		FilterFunction_abs filterFunction_abs = new FilterFunction_abs();
		filterFunction_abs.setArgs(new Expression[]{filterFactory.createAttributeExpression("name")});
		filter2.addRightValue(filterFunction_abs);
		
		Filter filter=filter1.and(filter2);

		filter.accept(visitor);
		
		assertEquals(Filter.NONE, visitor.getFilterPre());
		assertEquals(filter, visitor.getFilterPost());

		filterCapabilitiesMask.addType(FilterFunction_geometryType.class);
		filterCapabilitiesMask.addType(FilterFunction_abs.class);
		filterCapabilitiesMask.addType(FilterCapabilities.SIMPLE_COMPARISONS|FilterCapabilities.LOGICAL);
		visitor=newVisitor();

		filter.accept(visitor);
		
		assertEquals(Filter.NONE, visitor.getFilterPost());
		assertEquals(filter, visitor.getFilterPre());
	}

	public void testSupportOnlySome() throws Exception {

		CompareFilter filter1 = createFunctionFilter();
		CompareFilter filter2 = filterFactory.createCompareFilter(FilterType.COMPARE_EQUALS);
		filter2.addLeftValue(filterFactory.createAttributeExpression("name"));
		FilterFunction_abs filterFunction_abs = new FilterFunction_abs();
		filterFunction_abs.setArgs(new Expression[]{filterFactory.createAttributeExpression("name")});
		filter2.addRightValue(filterFunction_abs);
		
		Filter filter=filter1.and(filter2);
		filterCapabilitiesMask.addType(FilterFunction_geometryType.class);
		filterCapabilitiesMask.addType(FilterCapabilities.SIMPLE_COMPARISONS|FilterCapabilities.LOGICAL);
		visitor=newVisitor();

		filter.accept(visitor);
		
		assertEquals(filter1, visitor.getFilterPre());
		assertEquals(filter2, visitor.getFilterPost());
		
	}
}
