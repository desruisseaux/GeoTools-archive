package org.geotools.data.wfs;

import java.util.HashMap;
import java.util.Map;

import org.geotools.filter.BetweenFilter;
import org.geotools.filter.CompareFilter;
import org.geotools.filter.FidFilter;
import org.geotools.filter.Filter;
import org.geotools.filter.FilterCapabilitiesMask;
import org.geotools.filter.LikeFilter;
import org.geotools.filter.NullFilter;

public class WFSFilterVisitorTest extends AbstractWFSFilterVisitorTests {

	public void testVisitBetweenFilter() throws Exception {
		BetweenFilter filter = filterFactory.createBetweenFilter();
		filter.addLeftValue(filterFactory.createLiteralExpression(0));
		filter.addRightValue(filterFactory.createLiteralExpression(4));
		filter.addMiddleValue(filterFactory.createAttributeExpression(numAtt));
		
		runTest(filter, (short)FilterCapabilitiesMask.BETWEEN, numAtt);
	}

	public void testVisitLogicalANDFilter() throws Exception{
		Filter f1 = createEqualsCompareFilter(nameAtt, "david");
		Filter f2 = createEqualsCompareFilter(nameAtt, "david");

		runTest(f1.and(f2), (short)(FilterCapabilitiesMask.SIMPLE_COMPARISONS|FilterCapabilitiesMask.LOGICAL), nameAtt);
	}
	public void testVisitLogicalNOTFilter() throws Exception{
		Filter f1 = createEqualsCompareFilter(nameAtt, "david");

		runTest(f1.not(), (short)(FilterCapabilitiesMask.SIMPLE_COMPARISONS|FilterCapabilitiesMask.LOGICAL), nameAtt);
	}
	public void testVisitLogicalORFilter() throws Exception{
		Filter f1 = createEqualsCompareFilter(nameAtt, "david");
		Filter f2 = createEqualsCompareFilter(nameAtt, "david");

		runTest(f1.or(f2), (short)(FilterCapabilitiesMask.SIMPLE_COMPARISONS|FilterCapabilitiesMask.LOGICAL), nameAtt);
	}


	
	public void testVisitCompareFilter() throws Exception{
		CompareFilter f = createEqualsCompareFilter(nameAtt, "david");

		runTest(f, (short)FilterCapabilitiesMask.SIMPLE_COMPARISONS, nameAtt);
	}

	/**
	 * an update is in transaction that modifies an  attribute that NOT is referenced in the query
	 */
	public void testVisitCompareFilterWithUpdateDifferentAttribute() throws Exception {
		filterCapabilitiesMask.addType((short) FilterCapabilitiesMask.SIMPLE_COMPARISONS);
		CompareFilter f = createEqualsCompareFilter(nameAtt, "david");

		CompareFilter updateFilter = createEqualsCompareFilter(nameAtt, "jose");

		Map props = new HashMap();
		props.put(geomAtt, null);
		transactionState.addAction(new Action.UpdateAction(typeName,
				updateFilter, props));

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
		runTest(filter, (short)FilterCapabilitiesMask.LIKE, nameAtt);
	}

	public void testVisitNullFilter() throws Exception {
		NullFilter filter = filterFactory.createNullFilter();
		
		filter.nullCheckValue(filterFactory.createAttributeExpression(nameAtt));
		runTest(filter, (short)FilterCapabilitiesMask.NULL_CHECK, nameAtt);
	}

	public void testVisitFidFilter() throws Exception {
		FidFilter filter = filterFactory.createFidFilter("david");
		filter.accept(visitor);
		
		assertEquals(Filter.NONE, visitor.getFilterPost());
		assertEquals(filter, visitor.getFilterPre());
		
	}

}
