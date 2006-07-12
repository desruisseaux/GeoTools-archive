package org.geotools.data.wfs;

import java.util.HashMap;
import java.util.Map;

import org.geotools.filter.BetweenFilter;
import org.geotools.filter.CompareFilter;
import org.geotools.filter.FidFilter;
import org.geotools.filter.Filter;
import org.geotools.filter.FilterCapabilities;
import org.geotools.filter.FilterType;
import org.geotools.filter.GeometryFilter;
import org.geotools.filter.LikeFilter;
import org.geotools.filter.NullFilter;

public class PostPreProcessFilterSplittingVisitorTest extends AbstractPostPreProcessFilterSplittingVisitorTests {

	public void testVisitBetweenFilter() throws Exception {
		BetweenFilter filter = filterFactory.createBetweenFilter();
		filter.addLeftValue(filterFactory.createLiteralExpression(0));
		filter.addRightValue(filterFactory.createLiteralExpression(4));
		filter.addMiddleValue(filterFactory.createAttributeExpression(numAtt));
		
		runTest(filter, FilterCapabilities.BETWEEN, numAtt);
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
		
		filterCapabilitiesMask.addType(FilterCapabilities.FUNCTIONS);
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
		
		filterCapabilitiesMask.addType(FilterCapabilities.FUNCTIONS);
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
		
		filterCapabilitiesMask.addType(FilterCapabilities.FUNCTIONS);
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
		
		filterCapabilitiesMask.addType(FilterCapabilities.FUNCTIONS);
		visitor=newVisitor();
		
		not.accept(visitor);

		assertEquals(Filter.NONE, visitor.getFilterPost());
		assertEquals(not, visitor.getFilterPre());
				
	}
}
