package org.geotools.data.complex.filter;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;

import org.geotools.data.FeatureSource;
import org.geotools.data.Source;
import org.geotools.data.complex.AttributeMapping;
import org.geotools.data.complex.FeatureTypeMapping;
import org.geotools.data.complex.TestData;
import org.geotools.data.feature.FeatureSource2;
import org.geotools.data.feature.memory.MemoryDataAccess;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.iso.TypeBuilder;
import org.geotools.feature.iso.Types;
import org.geotools.feature.type.TypeFactoryImpl;
import org.opengis.feature.Feature;
import org.opengis.feature.FeatureCollection;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.TypeFactory;
import org.opengis.feature.type.TypeName;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.Id;
import org.opengis.filter.PropertyIsBetween;
import org.opengis.filter.PropertyIsEqualTo;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.Function;
import org.opengis.filter.expression.Literal;
import org.opengis.filter.expression.PropertyName;
import org.opengis.filter.identity.FeatureId;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;

/**
 * 
 * 
 * @author Gabriel Roldan, Axios Engineering
 */
public class UnmappingFilterVisitorTest extends TestCase {

	private static FilterFactory ff = CommonFactoryFinder
			.getFilterFactory(null);

	private UnmappingFilterVisitor visitor;

	MemoryDataAccess dataStore;

	FeatureTypeMapping mapping;

	TypeName targetDescriptor;

	FeatureType targetType;

	protected void setUp() throws Exception {
		super.setUp();
		dataStore = TestData.createDenormalizedWaterQualityResults();
		mapping = TestData.createMappingsGroupByStation(dataStore);
		visitor = new UnmappingFilterVisitor(mapping);
		targetDescriptor = mapping.getTargetFeature();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/**
	 * Creates a mapping from the test case simple source to a complex type with
	 * an "areaOfInfluence" attribute, which is a buffer over the simple
	 * "location" attribute and another which is the concatenation of the
	 * attributes "anzlic_no" and "project_no"
	 * 
	 * @return
	 * @throws Exception
	 */
	private FeatureTypeMapping createSampleDerivedAttributeMappings()
			throws Exception {
		FeatureSource2 simpleSource = mapping.getSource();
		FeatureType sourceType = (FeatureType) simpleSource.describe();

		// create the target type
		TypeFactory tf = new org.geotools.feature.iso.type.TypeFactoryImpl();
		TypeBuilder builder = new TypeBuilder(tf);

		AttributeType areaOfInfluence = builder.name("areaOfInfluence").bind(
				Polygon.class).attribute();
		AttributeType concatType = builder.name("concatenated").bind(
				String.class).attribute();

		builder.setName("target");
		builder.addAttribute("areaOfInfluence", areaOfInfluence);
		builder.addAttribute("concatenated", concatType);

		FeatureType targetType = builder.feature();

		// create the mapping definition
		List attMappings = new LinkedList();

		Function aoiExpr = ff.function("buffer", ff.property("location"), ff
				.literal(10));
		;

		attMappings.add(new AttributeMapping(aoiExpr, "areaOfInfluence"));

		Function strConcat = ff.function("strConcat", ff.property("anzlic_no"),
				ff.property("project_no"));

		attMappings.add(new AttributeMapping(strConcat, "concatenated"));

		FeatureTypeMapping mapping = new FeatureTypeMapping(simpleSource,
				targetType.getName(), attMappings, null);
		return mapping;
	}

	/**
	 * Mapping specifies station_no --> wq_plus/@id. A FidFilter over wq_plus
	 * type should result in a compare equals filter over the station_no
	 * attribute of wq_ir_results simple type.
	 */
	public void testUnrollFidMappedToAttribute() throws Exception {
		String fid = "station_no.1";
		Id fidFilter = ff.id(Collections.singleton(ff.featureId(fid)));

		this.visitor.visit(fidFilter, null);

		Filter unrolled = this.visitor.getUnrolledFilter();
		assertNotNull(unrolled);

		Collection results = mapping.getSource().content(unrolled);
		assertEquals(1, results.size());
		Iterator features = results.iterator();
		SimpleFeature unmappedFeature = (SimpleFeature) features.next();
		assertNotNull(unmappedFeature);
		Object object = unmappedFeature.get("station_no");
		assertEquals(fid, object);
	}

	/**
	 * If no a specific mapping is defined for the feature id, the same feature
	 * id from the originating feature source is used. In such a case, an
	 * unrolled FidFilter should stay being a FidFilter.
	 */
	public void testUnrollFidToFid() throws Exception {
		FeatureTypeMapping noFidMapping;
		noFidMapping = new FeatureTypeMapping(mapping.getSource(), mapping
				.getTargetFeature(), mapping.getAttributeMappings(), null);

		this.mapping = noFidMapping;
		this.visitor = new UnmappingFilterVisitor(this.mapping);

		Feature sourceFeature = (Feature) mapping.getSource().content()
				.iterator().next();

		String fid = sourceFeature.getID();

		Id fidFilter = ff.id(Collections.singleton(ff.featureId(fid)));

		this.visitor.visit(fidFilter);

		Filter unrolled = this.visitor.getUnrolledFilter();
		assertNotNull(unrolled);
		assertTrue(unrolled instanceof Id);

		Collection results = mapping.getSource().content(unrolled);
		assertEquals(1, results.size());
		SimpleFeature unmappedFeature = (SimpleFeature) results.iterator()
				.next();
		assertEquals(fid, unmappedFeature.getID());
	}

	public void testAttributeExpression() throws Exception {
		PropertyName ae = ff.property("/wq_plus/measurement/result");
		visitor.visit(ae);
		List unrolled = visitor.unrolledExpressions;
		assertNotNull(unrolled);
		assertEquals(1, unrolled.size());

		Expression unmappedExpr = (Expression) unrolled.get(0);
		assertTrue(unmappedExpr instanceof PropertyName);
		PropertyName attExp = (PropertyName) unmappedExpr;
		assertEquals("results_value", attExp.getPropertyName());

		// now try with an AttributeExpression that is not directly mapped to an
		// attribute
		// expresion on the surrogate FeatureType, but to a composite one.
		// For example, create a mapping from the test case simple source to
		// a complex type with an "areaOfInfluence" attribute, which is a buffer
		// over the simple "location" attribute
		// and another which is the concatenation of the attributes "anzlic_no"
		// and "project_no"
		FeatureTypeMapping mapping = createSampleDerivedAttributeMappings();
		targetDescriptor = mapping.getTargetFeature();

		visitor = new UnmappingFilterVisitor(mapping);
		attExp = ff.property("areaOfInfluence");
		visitor.visit(attExp);

		assertNotNull(visitor.unrolledExpressions);
		assertEquals(1, visitor.unrolledExpressions.size());

		unmappedExpr = (Expression) visitor.unrolledExpressions.get(0);
		assertTrue(unmappedExpr instanceof Function);
		Function fe = (Function) unmappedExpr;
		assertEquals("buffer", fe.getName());

		Expression arg0 = (Expression) fe.getParameters().get(0);
		assertTrue(arg0 instanceof PropertyName);
		assertEquals("location", ((PropertyName) arg0).getPropertyName());
	}

	public void testBetweenFilter() throws Exception {
		PropertyIsBetween bf = ff.between(ff.property("measurement/result"), ff
				.literal(1), ff.literal(2));

		visitor.visit(bf);

		PropertyIsBetween unrolled = (PropertyIsBetween) visitor
				.getUnrolledFilter();
		Expression att = unrolled.getExpression();
		assertTrue(att instanceof PropertyName);
		String propertyName = ((PropertyName) att).getPropertyName();
		assertEquals("results_value", propertyName);
	}

	/**
	 * 
	 */
	public void testCompareFilter() throws Exception {
		final FeatureType simpleType = (FeatureType) mapping.getSource()
				.describe();

		PropertyIsEqualTo complexFilter = ff.equals(ff
				.property("measurement/result"), ff.literal(1.1));

		visitor.visit((Filter) complexFilter);

		Filter unrolled = visitor.getUnrolledFilter();
		assertNotNull(unrolled);
		assertTrue(unrolled instanceof PropertyIsEqualTo);
		assertNotSame(complexFilter, unrolled);

		Expression left = ((PropertyIsEqualTo) unrolled).getExpression1();
		Expression right = ((PropertyIsEqualTo) unrolled).getExpression2();

		assertTrue(left instanceof PropertyName);
		assertTrue(right instanceof Literal);
		PropertyName attExp = (PropertyName) left;
		String expectedAtt = "results_value";
		assertEquals(expectedAtt, attExp.getPropertyName());
		assertEquals(new Double(1.1), ((Literal) right).getValue());
	}
	/*
	 * public void testFunctionExpression() throws Exception {
	 * FunctionExpression fe = ff.createFunctionExpression("strIndexOf");
	 * Expression[] params = { ff.createAttributeExpression(targetType,
	 * "/measurement/determinand_description"),
	 * ff.createLiteralExpression("determinand_description_1") };
	 * fe.setArgs(params); visitor.visit(fe);
	 * 
	 * Expression unmapped = (Expression) visitor.unrolledExpressions.get(0);
	 * assertTrue(unmapped instanceof FunctionExpression); params =
	 * ((FunctionExpression) unmapped).getArgs(); assertEquals(2,
	 * params.length); assertTrue(params[0] instanceof AttributeExpression);
	 * assertEquals("determinand_description", ((AttributeExpression)
	 * params[0]).getAttributePath()); }
	 * 
	 * public void testGeometryFilter() throws Exception { mapping =
	 * createSampleDerivedAttributeMappings(); visitor = new
	 * UnmappingFilterVisitor(mapping); targetDescriptor =
	 * mapping.getTargetFeature(); targetType = (FeatureType)
	 * targetDescriptor.getType();
	 * 
	 * GeometryFilter gf = ff
	 * .createGeometryFilter(FilterType.GEOMETRY_INTERSECTS); Expression attGeom =
	 * ff.createAttributeExpression(targetType, "areaOfInfluence");
	 * gf.addLeftGeometry(attGeom); Expression literalGeom = ff
	 * .createLiteralExpression(new GeometryFactory() .createPoint(new
	 * Coordinate(1, 1))); gf.addRightGeometry(literalGeom);
	 * 
	 * visitor.visit(gf); Filter unrolled = visitor.getUnrolledFilter();
	 * assertTrue(unrolled instanceof GeometryFilter); assertNotSame(gf,
	 * unrolled);
	 * 
	 * GeometryFilter newFilter = (GeometryFilter) unrolled; Expression left =
	 * newFilter.getLeftGeometry(); Expression right =
	 * newFilter.getRightGeometry(); assertNotSame(attGeom, left);
	 * assertSame(right, literalGeom);
	 * 
	 * assertTrue(left instanceof FunctionExpression); FunctionExpression fe =
	 * (FunctionExpression) left; assertEquals("buffer", fe.getName());
	 * 
	 * Expression arg0 = fe.getArgs()[0]; assertTrue(arg0 instanceof
	 * AttributeExpression); assertEquals("location", ((AttributeExpression)
	 * arg0) .getAttributePath()); }
	 * 
	 * public void testLikeFilter() throws Exception { LikeFilter lf =
	 * ff.createLikeFilter(); Expression param =
	 * ff.createAttributeExpression(targetType,
	 * "/measurement/determinand_description"); lf.setPattern("%n_1_1", "%",
	 * "?", "\\"); lf.setValue(param);
	 * 
	 * visitor.visit(lf);
	 * 
	 * LikeFilter unmapped = (LikeFilter) visitor.getUnrolledFilter();
	 * assertEquals(lf.getPattern(), unmapped.getPattern());
	 * assertEquals(lf.getWildcardMulti(), unmapped.getWildcardMulti());
	 * assertEquals(lf.getWildcardSingle(), unmapped.getWildcardSingle());
	 * assertEquals(lf.getEscape(), unmapped.getEscape());
	 * 
	 * Expression unmappedExpr = unmapped.getValue(); assertTrue(unmappedExpr
	 * instanceof AttributeExpression); assertEquals("determinand_description",
	 * ((AttributeExpression) unmappedExpr).getAttributePath()); }
	 * 
	 * public void testLiteralExpression() throws Exception { Expression literal =
	 * ff.createLiteralExpression(new Integer(0)); visitor.visit(literal);
	 * assertEquals(1, visitor.unrolledExpressions.size()); assertSame(literal,
	 * visitor.unrolledExpressions.get(0)); }
	 * 
	 * public void testLogicFilter() throws Exception {
	 * testLogicFilter(FilterType.LOGIC_AND);
	 * testLogicFilter(FilterType.LOGIC_NOT);
	 * testLogicFilter(FilterType.LOGIC_OR); }
	 * 
	 * private void testLogicFilter(short filterType) throws Exception {
	 * LogicFilter complexLogicFilter = ff.createLogicFilter(filterType);
	 * CompareFilter resultFilter = ff
	 * .createCompareFilter(FilterType.COMPARE_GREATER_THAN);
	 * resultFilter.addLeftValue(ff.createAttributeExpression(targetType,
	 * "measurement/result"));
	 * resultFilter.addRightValue(ff.createLiteralExpression(new Integer(5)));
	 * 
	 * complexLogicFilter.addFilter(resultFilter);
	 * 
	 * if (filterType != FilterType.LOGIC_NOT) { BetweenFilter determFilter =
	 * ff.createBetweenFilter(); determFilter.addLeftValue(ff
	 * .createLiteralExpression("determinand_description_1_1"));
	 * determFilter.addMiddleValue(ff.createAttributeExpression( targetType,
	 * "measurement/determinand_description")); determFilter.addRightValue(ff
	 * .createLiteralExpression("determinand_description_3_3"));
	 * complexLogicFilter.addFilter(determFilter); }
	 * 
	 * visitor.visit(complexLogicFilter);
	 * 
	 * Filter unmapped = visitor.getUnrolledFilter(); assertNotNull(unmapped);
	 * assertTrue(unmapped instanceof LogicFilter);
	 * assertNotSame(complexLogicFilter, unmapped);
	 * 
	 * assertEquals(complexLogicFilter.getFilterType(), unmapped
	 * .getFilterType());
	 * 
	 * LogicFilter logicUnmapped = (LogicFilter) unmapped;
	 * 
	 * Iterator it = logicUnmapped.getFilterIterator();
	 * assertTrue(it.hasNext()); CompareFilter unmappedResult = (CompareFilter)
	 * it.next();
	 * 
	 * assertEquals("results_value", ((AttributeExpression) unmappedResult
	 * .getLeftValue()).getAttributePath());
	 * 
	 * assertEquals(new Integer(5), ((LiteralExpression) unmappedResult
	 * .getRightValue()).getLiteral());
	 * 
	 * if (filterType != FilterType.LOGIC_NOT) {
	 * 
	 * assertTrue(it.hasNext()); BetweenFilter unmappedDeterm = (BetweenFilter)
	 * it.next(); assertFalse(it.hasNext());
	 * 
	 * assertEquals("determinand_description_1_1", ((LiteralExpression)
	 * unmappedDeterm.getLeftValue()) .getLiteral());
	 * assertEquals("determinand_description", ((AttributeExpression)
	 * unmappedDeterm.getMiddleValue()) .getAttributePath());
	 * assertEquals("determinand_description_3_3", ((LiteralExpression)
	 * unmappedDeterm.getRightValue()) .getLiteral()); } }
	 * 
	 * public void testMathExpression() throws Exception { MathExpression
	 * mathExp = ff .createMathExpression(ExpressionType.MATH_MULTIPLY);
	 * mathExp.addLeftValue(ff.createAttributeExpression(targetType,
	 * "measurement/result")); Expression literal =
	 * ff.createLiteralExpression(new Integer(2));
	 * mathExp.addRightValue(literal);
	 * 
	 * visitor.visit(mathExp); assertEquals(1,
	 * visitor.unrolledExpressions.size()); Expression unmapped = (Expression)
	 * visitor.unrolledExpressions.get(0); assertTrue(unmapped instanceof
	 * MathExpression); MathExpression mathUnmapped = (MathExpression) unmapped;
	 * assertEquals(mathExp.getType(), mathUnmapped.getType());
	 * AttributeExpression unmappedAttt = (AttributeExpression) mathUnmapped
	 * .getLeftValue(); assertEquals("results_value",
	 * unmappedAttt.getAttributePath()); assertSame(literal,
	 * mathUnmapped.getRightValue()); }
	 * 
	 * public void testNullFilter() throws Exception { NullFilter nullFilter =
	 * ff.createNullFilter();
	 * nullFilter.nullCheckValue(ff.createAttributeExpression(targetType,
	 * "measurement/result"));
	 * 
	 * visitor.visit(nullFilter);
	 * 
	 * assertTrue(visitor.getUnrolledFilter() instanceof NullFilter);
	 * assertNotSame(nullFilter, visitor.getUnrolledFilter()); NullFilter
	 * unmapped = (NullFilter) visitor.getUnrolledFilter(); Expression
	 * unmappedAtt = unmapped.getNullCheckValue(); assertTrue(unmappedAtt
	 * instanceof AttributeExpression); assertEquals("results_value",
	 * ((AttributeExpression) unmappedAtt) .getAttributePath()); }
	 */
}
