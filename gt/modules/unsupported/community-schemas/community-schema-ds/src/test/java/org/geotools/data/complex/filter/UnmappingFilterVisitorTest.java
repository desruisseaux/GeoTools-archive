package org.geotools.data.complex.filter;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;

import org.geotools.data.complex.AttributeMapping;
import org.geotools.data.complex.FeatureTypeMapping;
import org.geotools.data.complex.TestData;
import org.geotools.data.feature.FeatureSource2;
import org.geotools.data.feature.memory.MemoryDataAccess;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.iso.TypeBuilder;
import org.geotools.filter.FilterType;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.TypeFactory;
import org.opengis.filter.And;
import org.opengis.filter.BinaryLogicOperator;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.Id;
import org.opengis.filter.PropertyIsBetween;
import org.opengis.filter.PropertyIsEqualTo;
import org.opengis.filter.PropertyIsGreaterThan;
import org.opengis.filter.PropertyIsLike;
import org.opengis.filter.PropertyIsNull;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.Function;
import org.opengis.filter.expression.Literal;
import org.opengis.filter.expression.Multiply;
import org.opengis.filter.expression.PropertyName;
import org.opengis.filter.spatial.Intersects;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;

/**
 * 
 * 
 * @author Gabriel Roldan, Axios Engineering
 */
public class UnmappingFilterVisitorTest extends TestCase {

    private static FilterFactory2 ff = (FilterFactory2) CommonFactoryFinder
            .getFilterFactory(null);

    private UnmappingFilterVisitor visitor;

    MemoryDataAccess dataStore;

    FeatureTypeMapping mapping;

    AttributeDescriptor targetDescriptor;

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
        AttributeDescriptor targetFeature = tf.createAttributeDescriptor(
                targetType, targetType.getName(), 0, Integer.MAX_VALUE, true);

        // create the mapping definition
        List attMappings = new LinkedList();

        Function aoiExpr = ff.function("buffer", ff.property("location"), ff
                .literal(10));

        attMappings.add(new AttributeMapping(null, aoiExpr, "areaOfInfluence"));

        Function strConcat = ff.function("strConcat", ff.property("anzlic_no"),
                ff.property("project_no"));

        attMappings.add(new AttributeMapping(null, strConcat, "concatenated"));

        FeatureSource2 simpleSource = mapping.getSource();
        FeatureTypeMapping mapping = new FeatureTypeMapping(simpleSource,
                targetFeature, attMappings);
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

        AttributeMapping featureMapping = null;
        String featurePath = mapping.getTargetFeature().getName().getLocalPart();
        for(Iterator it = mapping.getAttributeMappings().iterator(); it.hasNext();){
            AttributeMapping attMapping = (AttributeMapping) it.next();
            if(featurePath.equals(attMapping.getTargetXPath())){
                featureMapping = attMapping;
                break;
            }
        }
        
        featureMapping.setIdentifierExpression(Expression.NIL);
        
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

    /**
     * 
     */
    public void testLogicFilterAnd() throws Exception {
        PropertyIsEqualTo equals = ff.equals(ff.property("measurement/result"),
                ff.literal(1.1));
        PropertyIsGreaterThan greater = ff.greater(ff
                .property("measurement/determinand_description"), ff
                .literal("desc1"));

        And logicFilter = ff.and(equals, greater);
        logicFilter.accept(visitor, null);

        Filter unrolled = visitor.getUnrolledFilter();
        assertNotNull(unrolled);
        assertTrue(unrolled instanceof And);
        assertNotSame(equals, unrolled);

        And sourceAnd = (And) unrolled;
        assertEquals(2, sourceAnd.getChildren().size());

        Filter sourceEquals = (Filter) sourceAnd.getChildren().get(0);
        assertTrue(sourceEquals instanceof PropertyIsEqualTo);

        Expression left = ((PropertyIsEqualTo) sourceEquals).getExpression1();
        Expression right = ((PropertyIsEqualTo) sourceEquals).getExpression2();
        assertTrue(left instanceof PropertyName);
        assertTrue(right instanceof Literal);

        assertEquals("results_value", ((PropertyName) left).getPropertyName());
        assertEquals(new Double(1.1), ((Literal) right).getValue());

        Filter sourceGreater = (Filter) sourceAnd.getChildren().get(1);
        assertTrue(sourceGreater instanceof PropertyIsGreaterThan);

        left = ((PropertyIsGreaterThan) sourceGreater).getExpression1();
        right = ((PropertyIsGreaterThan) sourceGreater).getExpression2();
        assertTrue(left instanceof PropertyName);
        assertTrue(right instanceof Literal);

        assertEquals("determinand_description", ((PropertyName) left)
                .getPropertyName());
        assertEquals("desc1", ((Literal) right).getValue());
    }

    public void testFunction() throws Exception {
        Function fe = ff.function("strIndexOf", ff
                .property("/measurement/determinand_description"), ff
                .literal("determinand_description_1"));

        fe.accept(visitor, null);

        Expression unmapped = (Expression) visitor.unrolledExpressions.get(0);
        assertTrue(unmapped instanceof Function);
        List params = ((Function) unmapped).getParameters();
        assertEquals(2, params.size());
        assertTrue(params.get(0) instanceof PropertyName);
        assertEquals("determinand_description", ((PropertyName) params.get(0))
                .getPropertyName());
    }

    public void testGeometryFilter() throws Exception {
        mapping = createSampleDerivedAttributeMappings();
        visitor = new UnmappingFilterVisitor(mapping);
        targetDescriptor = mapping.getTargetFeature();
        targetType = (FeatureType) targetDescriptor.getType();

        Expression literalGeom = ff.literal(new GeometryFactory()
                .createPoint(new Coordinate(1, 1)));

        Intersects gf = ff.intersects(ff.property("areaOfInfluence"),
                literalGeom);

        gf.accept(visitor, null);

        Filter unrolled = visitor.getUnrolledFilter();
        assertTrue(unrolled instanceof Intersects);
        assertNotSame(gf, unrolled);

        Intersects newFilter = (Intersects) unrolled;
        Expression left = newFilter.getExpression1();
        Expression right = newFilter.getExpression2();

        assertSame(right, literalGeom);
        assertTrue(left instanceof Function);
        Function fe = (Function) left;
        assertEquals("buffer", fe.getName());

        Expression arg0 = (Expression) fe.getParameters().get(0);
        assertTrue(arg0 instanceof PropertyName);
        assertEquals("location", ((PropertyName) arg0).getPropertyName());
    }

    public void testLikeFilter() throws Exception {
        final String wildcard = "%";
        final String single = "?";
        final String escape = "\\";
        PropertyIsLike like = ff.like(ff
                .property("/measurement/determinand_description"), "%n_1_1",
                wildcard, single, escape);

        like.accept(visitor, null);

        PropertyIsLike unmapped = (PropertyIsLike) visitor.getUnrolledFilter();
        assertEquals(like.getLiteral(), unmapped.getLiteral());
        assertEquals(like.getWildCard(), unmapped.getWildCard());
        assertEquals(like.getSingleChar(), unmapped.getSingleChar());
        assertEquals(like.getEscape(), unmapped.getEscape());

        Expression unmappedExpr = unmapped.getExpression();
        assertTrue(unmappedExpr instanceof PropertyName);
        assertEquals("determinand_description", ((PropertyName) unmappedExpr)
                .getPropertyName());
    }

    public void testLiteralExpression() throws Exception {
        Expression literal = ff.literal(new Integer(0));
        literal.accept(visitor, null);
        assertEquals(1, visitor.unrolledExpressions.size());
        assertSame(literal, visitor.unrolledExpressions.get(0));
    }

    public void testLogicFilter() throws Exception {
        testLogicFilter(FilterType.LOGIC_AND);
        testLogicFilter(FilterType.LOGIC_OR);
    }

    private void testLogicFilter(short filterType) throws Exception {
        BinaryLogicOperator complexLogicFilter;
        PropertyIsGreaterThan resultFilter = ff.greater(ff
                .property("measurement/result"), ff.literal(new Integer(5)));

        PropertyIsBetween determFilter = ff.between(ff
                .property("measurement/determinand_description"), ff
                .literal("determinand_description_1_1"), ff
                .literal("determinand_description_3_3"));

        switch (filterType) {
        case FilterType.LOGIC_AND:
            complexLogicFilter = ff.and(resultFilter, determFilter);
            break;
        case FilterType.LOGIC_OR:
            complexLogicFilter = ff.or(resultFilter, determFilter);
            break;
        default:
            throw new IllegalArgumentException();
        }

        complexLogicFilter.accept(visitor, null);

        Filter unmapped = visitor.getUnrolledFilter();
        assertNotNull(unmapped);
        assertTrue(unmapped instanceof BinaryLogicOperator);
        assertNotSame(complexLogicFilter, unmapped);

        BinaryLogicOperator logicUnmapped = (BinaryLogicOperator) unmapped;

        List children = logicUnmapped.getChildren();
        assertEquals(2, children.size());

        PropertyIsGreaterThan unmappedResult = (PropertyIsGreaterThan) children
                .get(0);
        PropertyIsBetween unmappedDeterm = (PropertyIsBetween) children.get(1);

        assertEquals("results_value", ((PropertyName) unmappedResult
                .getExpression1()).getPropertyName());

        assertEquals(new Integer(5),
                ((Literal) unmappedResult.getExpression2()).getValue());

        assertEquals("determinand_description", ((PropertyName) unmappedDeterm
                .getExpression()).getPropertyName());
        assertEquals("determinand_description_1_1", ((Literal) unmappedDeterm
                .getLowerBoundary()).getValue());
        assertEquals("determinand_description_3_3", ((Literal) unmappedDeterm
                .getUpperBoundary()).getValue());
    }

    public void testMathExpression() throws Exception {
        Literal literal = ff.literal(new Integer(2));
        Multiply mathExp = ff.multiply(ff.property("measurement/result"),
                literal);

        mathExp.accept(visitor, null);

        assertEquals(1, visitor.unrolledExpressions.size());
        Expression unmapped = (Expression) visitor.unrolledExpressions.get(0);
        assertTrue(unmapped instanceof Multiply);
        Multiply mathUnmapped = (Multiply) unmapped;

        PropertyName unmappedAttt = (PropertyName) mathUnmapped
                .getExpression1();
        assertEquals("results_value", unmappedAttt.getPropertyName());
        assertSame(literal, mathUnmapped.getExpression2());
    }

    public void testNullFilter() throws Exception {
        PropertyIsNull nullFilter = ff
                .isNull(ff.property("measurement/result"));

        nullFilter.accept(visitor, null);

        assertTrue(visitor.getUnrolledFilter() instanceof PropertyIsNull);
        assertNotSame(nullFilter, visitor.getUnrolledFilter());
        PropertyIsNull unmapped = (PropertyIsNull) visitor.getUnrolledFilter();
        Expression unmappedAtt = unmapped.getExpression();
        assertTrue(unmappedAtt instanceof PropertyName);
        assertEquals("results_value", ((PropertyName) unmappedAtt)
                .getPropertyName());
    }

}
