/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2006, GeoTools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.filter.text.cql2;

import java.io.StringReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.geotools.filter.FilterFactoryImpl;
import org.geotools.filter.IllegalFilterException;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.filter.BinaryComparisonOperator;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.Not;
import org.opengis.filter.PropertyIsBetween;
import org.opengis.filter.PropertyIsEqualTo;
import org.opengis.filter.PropertyIsGreaterThan;
import org.opengis.filter.PropertyIsGreaterThanOrEqualTo;
import org.opengis.filter.PropertyIsLessThan;
import org.opengis.filter.PropertyIsLessThanOrEqualTo;
import org.opengis.filter.PropertyIsLike;
import org.opengis.filter.PropertyIsNull;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.Function;
import org.opengis.filter.expression.Literal;
import org.opengis.filter.expression.PropertyName;
import org.opengis.filter.spatial.BinarySpatialOperator;
import org.opengis.filter.spatial.DistanceBufferOperator;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.WKTReader;


/**
 * CQL Compiler. This class extend  the CQLParser generated 
 * by javacc with semantic actions. 
 * The "build..." methods are associated to each syntax rules recognized.
 *
 * @author Mauricio Pazos - www.axios.es
 * 
 * @since 2.4
 * @version $Id$ 
 *
 */
final class CQLCompiler extends CQLParser implements CQLParserTreeConstants {
    private static final String ATTRIBUTE_PATH_SEPARATOR = "/";
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    private static final WKTReader WKT_READER = new WKTReader();
    private BuildResultStack resultStack;
    private CQLException exception = null;
    private String input = null;
    private FilterFactory filterFactory;

    CQLCompiler(String input, FilterFactory filterFactory) {
        super(new StringReader(input));
        this.input = input;
        this.filterFactory = filterFactory;
        this.resultStack = BuildResultStack.getInstance();
    }

    public final CQLException getException() {
        return this.exception;
    }

    /**
     * @return either an Expression or a Filter, depending on what the product
     *         of the compilation unit was
     * @throws CQLException
     */
    public final Object getResult() throws CQLException {
        Result item = resultStack.peek();
        Object result = item.getBuilt();

        return result;
    }

    /**
     * Returns the list of Filters built as the result of calling
     * {@link #MultipleCompilationUnit()}
     *
     * @return
     * @throws CQLException
     *             if a ClassCastException occurs while casting a built item to
     *             a Filter.
     */
    public final List getResults() throws CQLException {
        int size = resultStack.size();
        List results = new ArrayList(size);

        for (int i = 0; i < size; i++) {
            Result item = resultStack.popResult();
            Object result = item.getBuilt();
            results.add(0, result);
        }

        return results;
    }

    public final void jjtreeOpenNodeScope(Node n) {
    }

    public final void jjtreeCloseNodeScope(Node n) throws ParseException {
        try {
            Object built = buildObject(n);

            if (built == null) {
                throw new RuntimeException("INTERNAL ERROR : Node " + n + " resulted in null build");
            }

            resultStack.push(new Result(built, getToken(0), n.getType()));
        } finally {
            n.dispose();
        }
    }

    private org.opengis.filter.expression.BinaryExpression buildBinaryExpression(int nodeType)
        throws CQLException {
        org.opengis.filter.expression.Expression right = resultStack.popExpression();
        org.opengis.filter.expression.Expression left = resultStack.popExpression();

        org.opengis.filter.expression.BinaryExpression expr = null;

        switch (nodeType) {
        case JJTADDNODE:
            expr = filterFactory.add(left, right);

            break;

        case JJTSUBTRACTNODE:
            expr = filterFactory.subtract(left, right);

            break;

        case JJTMULNODE:
            expr = filterFactory.multiply(left, right);

            break;

        case JJTDIVNODE:
            expr = filterFactory.divide(left, right);

            break;

        default:
            break;
        }

        return expr;
    }

    private org.opengis.filter.Filter buildLogicFilter(int nodeType)
        throws CQLException {
        try {
            org.opengis.filter.Filter right = null;
            org.opengis.filter.Filter left = null;

            org.opengis.filter.Filter logicFilter;

            switch (nodeType) {
            case JJTBOOLEAN_AND_NODE:
                right = resultStack.popFilter();
                left = resultStack.popFilter();

                if (Filter.INCLUDE.equals(right)) {
                    logicFilter = left;
                } else if (Filter.INCLUDE.equals(left)) {
                    logicFilter = right;
                } else if (Filter.EXCLUDE.equals(right) || Filter.EXCLUDE.equals(left)) {
                    logicFilter = Filter.EXCLUDE;
                } else {
                    logicFilter = filterFactory.and(left, right);
                }

                break;

            case JJTBOOLEAN_OR_NODE:
                right = resultStack.popFilter();
                left = resultStack.popFilter();

                if (Filter.INCLUDE.equals(right) || Filter.INCLUDE.equals(left)) {
                    logicFilter = Filter.INCLUDE;
                } else if (Filter.EXCLUDE.equals(left)) {
                    logicFilter = right;
                } else if (Filter.EXCLUDE.equals(right)) {
                    logicFilter = left;
                } else {
                    logicFilter = filterFactory.or(left, right);
                }

                break;

            case JJTBOOLEAN_NOT_NODE:
                right = resultStack.popFilter();

                if (Filter.INCLUDE.equals(right)) {
                    logicFilter = Filter.EXCLUDE;
                } else if (Filter.EXCLUDE.equals(right)) {
                    logicFilter = Filter.INCLUDE;
                } else {
                    logicFilter = filterFactory.not(right);
                }

                break;

            default:
                throw new CQLException("Expression not supported. And, Or, Not is required",
                    getToken(0));
            }

            return logicFilter;
        } catch (IllegalFilterException ife) {
            throw new CQLException("Exception building LogicFilter", getToken(0), ife);
        }
    }

    /**
     * Bulds a like filter
     * 
     * @return a PropertyIsLike
     * @throws CQLException
     */
    private PropertyIsLike buildLikeFilter() throws CQLException {
        final String WC_MULTI = "%";
        final String WC_SINGLE = "_";
        final String ESCAPE = "\\";

        try {
            org.opengis.filter.expression.Expression pattern = resultStack.popExpression();
            org.opengis.filter.expression.Expression expr = resultStack.popExpression();

            PropertyIsLike f = filterFactory.like(expr, pattern.toString(), WC_MULTI, WC_SINGLE,
                    ESCAPE);

            return f;
        } catch (IllegalFilterException ife) {
            throw new CQLException("Exception building LikeFilter", getToken(0), ife);
        }
    }

    /**
     * Builds property is null filter
     *
     * @return PropertyIsNull
     * @throws CQLException
     */
    private PropertyIsNull buildPropertyIsNull() throws CQLException {
        try {
            org.opengis.filter.expression.Expression property = resultStack.popExpression();

            PropertyIsNull filter = filterFactory.isNull(property);

            return filter;
        } catch (CQLException e) {
            throw new CQLException("Exception building Null Predicate", getToken(0), e);
        }
    }

    /**
     * builds PropertyIsBetween filter
     *
     * @return PropertyIsBetween
     * @throws CQLException
     */
    private PropertyIsBetween buildBetween() throws CQLException {
        try {
            org.opengis.filter.expression.Expression sup = resultStack.popExpression();
            org.opengis.filter.expression.Expression inf = resultStack.popExpression();
            org.opengis.filter.expression.Expression expr = resultStack.popExpression();

            PropertyIsBetween filter = filterFactory.between(expr, inf, sup);

            return filter;
        } catch (IllegalFilterException ife) {
            throw new CQLException("Exception building CompareFilter", getToken(0), ife);
        }
    }

    /**
     * This method is called when the parser close a node. Here is built the
     * filters an expressions recognized in the parsing process.
     *
     * @param n
     *            a Node instance
     * @return Filter or Expression
     * @throws CQLException
     */
    protected Object buildObject(Node n) throws CQLException {
        switch (n.getType()) {
        // Literals
        // note, these should never throw because the parser grammar
        // constrains input before we ever reach here!
        case JJTINTEGERNODE:
            return filterFactory.literal(Integer.parseInt(getToken(0).image));

        case JJTFLOATINGNODE:
            return filterFactory.literal(Double.parseDouble(getToken(0).image));

        case JJTSTRINGNODE:
            return filterFactory.literal(n.getToken().image);

        // ----------------------------------------
        // Identifier
        // ----------------------------------------
        case JJTIDENTIFIER_NODE:
            return buildIdentifier();

        case JJTIDENTIFIER_START_NODE:
        case JJTIDENTIFIER_PART_NODE:
            return buildIdentifierPart();

        // ----------------------------------------
        // attribute
        // ----------------------------------------
        case JJTSIMPLE_ATTRIBUTE_NODE:
            return buildSimpleAttribute();

        case JJTCOMPOUND_ATTRIBUTE_NODE:
            return buildCompoundAttribute();

        // ----------------------------------------
        // function
        // ----------------------------------------
        case JJTFUNCTION_NODE:
            return buildFunction(n);

        case JJTFUNCTIONNAME_NODE:
            return n; // used as mark of function name in stack

        case JJTFUNCTIONARG_NODE:
            return n; // used as mark of args in stack

        // Math Nodes
        case JJTADDNODE:
        case JJTSUBTRACTNODE:
        case JJTMULNODE:
        case JJTDIVNODE:
            return buildBinaryExpression(n.getType());

        // Boolean expression
        case JJTBOOLEAN_AND_NODE:
            return buildLogicFilter(JJTBOOLEAN_AND_NODE);

        case JJTBOOLEAN_OR_NODE:
            return buildLogicFilter(JJTBOOLEAN_OR_NODE);

        case JJTBOOLEAN_NOT_NODE:
            return buildLogicFilter(JJTBOOLEAN_NOT_NODE);

        // ----------------------------------------
        // between predicate actions
        // ----------------------------------------
        case JJTBETWEEN_NODE:
            return buildBetween();

        case JJTNOT_BETWEEN_NODE:
            return filterFactory.not(buildBetween());

        // ----------------------------------------
        // Compare predicate actions
        // ----------------------------------------
        case JJTCOMPARISSONPREDICATE_EQ_NODE:
        case JJTCOMPARISSONPREDICATE_GT_NODE:
        case JJTCOMPARISSONPREDICATE_LT_NODE:
        case JJTCOMPARISSONPREDICATE_GTE_NODE:
        case JJTCOMPARISSONPREDICATE_LTE_NODE:
            return buildBinaryComparasionOperator(n.getType());

        case JJTCOMPARISSONPREDICATE_NOT_EQUAL_NODE:

            Filter eq = buildBinaryComparasionOperator(JJTCOMPARISSONPREDICATE_EQ_NODE);
            Not notFilter = filterFactory.not(eq);

            return notFilter;

        // ----------------------------------------
        // Text predicate (Like)
        // ----------------------------------------
        case JJTLIKE_NODE:
            return buildLikeFilter();

        case JJTNOT_LIKE_NODE:

            Not filter = filterFactory.not(buildLikeFilter());

            return filter;

        // ----------------------------------------
        // Null predicate
        // ----------------------------------------
        case JJTNULLPREDICATENODE:
            return buildPropertyIsNull();

        case JJTNOTNULLPREDICATENODE:
            return filterFactory.not(buildPropertyIsNull());

        // ----------------------------------------
        // temporal predicate actions
        // ----------------------------------------
        case JJTDATETIME_NODE:
            return buildDateTimeExpression();

        case JJTDURATION_DATE_NODE:
            return buildDurationExpression();

        case JJTPERIOD_BETWEEN_DATES_NODE:
            return buildPeriodBetweenDates();

        case JJTPERIOD_WITH_DATE_DURATION_NODE:
            return buildPeriodDateAndDuration();

        case JJTPERIOD_WITH_DURATION_DATE_NODE:
            return buildPeriodDurationAndDate();

        case JJTTPBEFORE_DATETIME_NODE:
            return buildTemporalPredicateBefore();

        case JJTTPAFTER_DATETIME_NODE:
            return buildTemporalPredicateAfter();

        case JJTTPDURING_PERIOD_NODE:
            return buildTemporalPredicateDuring();

        case JJTTPBEFORE_OR_DURING_PERIOD_NODE:
            return buildTemporalPredicateBeforeOrDuring();

        case JJTTPDURING_OR_AFTER_PERIOD_NODE:
            return buildTemporalPredicateDuringOrAfter();

        // ----------------------------------------
        // existence predicate actions
        // ----------------------------------------
        case JJTEXISTENCE_PREDICATE_EXISTS_NODE:
            return buildPropertyExists();

        case JJTEXISTENCE_PREDICATE_DOESNOTEXIST_NODE:

            org.opengis.filter.Filter filterPropNotExist = filterFactory.not(buildPropertyExists());

            return filterPropNotExist;

        // ----------------------------------------
        // routine invocation Geo Operation
        // ----------------------------------------
        case JJTROUTINEINVOCATION_GEOOP_EQUAL_NODE:
        case JJTROUTINEINVOCATION_GEOOP_DISJOINT_NODE:
        case JJTROUTINEINVOCATION_GEOOP_INTERSECT_NODE:
        case JJTROUTINEINVOCATION_GEOOP_TOUCH_NODE:
        case JJTROUTINEINVOCATION_GEOOP_CROSS_NODE:
        case JJTROUTINEINVOCATION_GEOOP_WITHIN_NODE:
        case JJTROUTINEINVOCATION_GEOOP_CONTAIN_NODE:
        case JJTROUTINEINVOCATION_GEOOP_OVERLAP_NODE:
            return buildBinarySpatialOperator(n.getType());

        case JJTROUTINEINVOCATION_GEOOP_BBOX_NODE:
        case JJTROUTINEINVOCATION_GEOOP_BBOX_SRS_NODE:
            return buildBBox(n.getType());

        case JJTROUTINEINVOCATION_GEOOP_RELATE_NODE:
            throw new CQLException("Unsupported geooperation RELATE (is not implemented by GeoTools)",
                getToken(0));

        // ----------------------------------------
        // routine invocation RelGeo Operation
        // ----------------------------------------
        case JJTTOLERANCE_NODE:
            return buildTolerance();

        case JJTDISTANCEUNITS_NODE:
            return buildDistanceUnit();

        case JJTROUTINEINVOCATION_RELOP_BEYOND_NODE:
        case JJTROUTINEINVOCATION_RELOP_DWITHIN_NODE:
            return buildDistanceBufferOperator(n.getType());

        // ----------------------------------------
        // Geometries:
        // ----------------------------------------
        case JJTWKTNODE:
            return buildGeometry(n.getToken());

        case JJTENVELOPETAGGEDTEXT_NODE:
            return buildEnvelop(n.getToken());

        case JJTINCLUDE_NODE:
            return Filter.INCLUDE;

        case JJTEXCLUDE_NODE:
            return Filter.EXCLUDE;

        case JJTTRUENODE:
            return filterFactory.literal(Boolean.TRUE);

        case JJTFALSENODE:
            return filterFactory.literal(Boolean.FALSE);
        }

        return null;
    }

    private PropertyName buildCompoundAttribute() throws CQLException {
        ArrayList arrayIdentifiers = new ArrayList();

        // precondition: stack has one or more simple attributes
        while (resultStack.size() > 0) {
            Result r = resultStack.peek();

            if (r.getNodeType() != JJTSIMPLE_ATTRIBUTE_NODE) {
                break;
            }

            PropertyName simpleAttribute = resultStack.popPropertyName();

            arrayIdentifiers.add(simpleAttribute.getPropertyName());
        }

        // postcondition: array has one or more simple attr
        StringBuffer attribute = new StringBuffer(100);
        int i = 0;

        for (i = arrayIdentifiers.size() - 1; i > 0; i--) {
            attribute.append(arrayIdentifiers.get(i));
            attribute.append(ATTRIBUTE_PATH_SEPARATOR);
        }

        attribute.append(arrayIdentifiers.get(i));

        PropertyName property = filterFactory.property(attribute.toString());

        return property;
    }

    private String buildIdentifier() throws CQLException {
        // precondition: the stack have one or more parts (string type)
        // retrieve all part of identifier from result stack
        try {
            ArrayList arrayParts = new ArrayList();

            while (resultStack.size() > 0) {
                Result r = resultStack.peek();

                if (!((r.getNodeType() == JJTIDENTIFIER_START_NODE)
                        || (r.getNodeType() == JJTIDENTIFIER_PART_NODE))) {
                    break;
                }

                String part = resultStack.popIdentifierPart();
                arrayParts.add(part);
            }

            // makes the identifier
            StringBuffer identifier = new StringBuffer(100);
            String part;

            int i = 0;

            for (i = arrayParts.size() - 1; i > 0; i--) {
                part = (String) arrayParts.get(i);
                identifier.append(part).append(":");
            } // postcondition i=0

            part = (String) arrayParts.get(i);
            identifier.append(part);

            return identifier.toString();
        } catch (CQLException e) {
            throw new CQLException("Fail builing identifier: " + e.getMessage());
        }
    }

    /**
     * Creates the identifier part. An identifier like "idpart1:idpart2:idpart3:
     * ... idpartN" has N part.
     *
     * @return Name
     */
    private String buildIdentifierPart() {
        String part = getToken(0).image;

        return part;
    }

    private PropertyName buildSimpleAttribute() throws CQLException {
        // Only retrieve the identifier built before
        String identifier = resultStack.popIdentifier();
        PropertyName property = filterFactory.property(identifier);

        return property;
    }

    private Literal buildDistanceUnit() throws CQLException {
        Literal unit = null;
        Token token = getToken(0);
        unit = filterFactory.literal(token.image);

        return unit;
    }

    private Literal buildTolerance() throws CQLException {
        Literal tolerance = null;

        try {
            tolerance = resultStack.popLiteral();

            return tolerance;
        } catch (NumberFormatException e) {
            throw new CQLException("Unsupported number format", token);
        }
    }

    /**
     * Creates Binary Spatial Operator
     *
     * @param tipeNode
     *
     * @return Filter (must be BinarySpatialOperator) // FIXME see equals
     * @throws CQLException
     */
    private BinarySpatialOperator buildBinarySpatialOperator(final int nodeType)
        throws CQLException {
        Literal geom = resultStack.popLiteral();

        org.opengis.filter.expression.Expression property = resultStack.popExpression();

        FilterFactory2 ff = (FilterFactory2) filterFactory; // TODO expecting
                                                            // implementation of
                                                            // new geoapi

        BinarySpatialOperator filter = null;

        switch (nodeType) {
        case JJTROUTINEINVOCATION_GEOOP_EQUAL_NODE:
            filter = ff.equal(property, geom);

            break;

        case JJTROUTINEINVOCATION_GEOOP_DISJOINT_NODE:
            filter = ff.disjoint(property, geom);

            break;

        case JJTROUTINEINVOCATION_GEOOP_INTERSECT_NODE:
            filter = ff.intersects(property, geom);

            break;

        case JJTROUTINEINVOCATION_GEOOP_TOUCH_NODE:
            filter = ff.touches(property, geom);

            break;

        case JJTROUTINEINVOCATION_GEOOP_CROSS_NODE:
            filter = ff.crosses(property, geom);

            break;

        case JJTROUTINEINVOCATION_GEOOP_WITHIN_NODE:

            // TODO: remove cast once http://jira.codehaus.org/browse/GEO-92
            // and
            // http://jira.codehaus.org/browse/GEOT-1028 are fixed.
            FilterFactoryImpl ffi = (FilterFactoryImpl) ff;
            filter = ffi.within(property, geom);

            break;

        case JJTROUTINEINVOCATION_GEOOP_CONTAIN_NODE:
            filter = ff.contains(property, geom);

            break;

        case JJTROUTINEINVOCATION_GEOOP_OVERLAP_NODE:
            filter = ff.overlaps(property, geom);

            break;

        default:
            throw new CQLException("Binary spatial operator unexpected");
        }

        return filter;
    }

    private org.opengis.filter.spatial.BBOX buildBBox(int nodeType)
        throws CQLException {
        try {
            String srs = "EPSG:4326"; // default

            if (nodeType == JJTROUTINEINVOCATION_GEOOP_BBOX_SRS_NODE) {
                srs = resultStack.popStringValue();
            }

            double maxY = resultStack.popDoubleValue();
            double maxX = resultStack.popDoubleValue();
            double minY = resultStack.popDoubleValue();
            double minX = resultStack.popDoubleValue();

            PropertyName property = resultStack.popPropertyName();
            String strProperty = property.getPropertyName();

            // CRS.decode(srs); FIXME bug in geotools
            org.opengis.filter.spatial.BBOX bbox = filterFactory.bbox(strProperty, minX, minY,
                    maxX, maxY, srs);

            return bbox;
        } catch (Exception e) {
            throw new CQLException("Fails building BBOX filter (" + e.getMessage() + ")");
        }
    }

    /**
     * Builds Distance Buffer Operator
     *
     * @param nodeType
     * @return DistanceBufferOperator dwithin and beyond filters
     * @throws CQLException
     */
    private DistanceBufferOperator buildDistanceBufferOperator(final int nodeType)
        throws CQLException {
        String unit = resultStack.popStringValue();

        double tolerance = resultStack.popDoubleValue();

        org.opengis.filter.expression.Expression geom = resultStack.popExpression();

        org.opengis.filter.expression.Expression property = resultStack.popExpression();

        FilterFactory2 ff = (FilterFactory2) filterFactory; // TODO expecting
                                                            // implementation of
                                                            // new geoapi

        DistanceBufferOperator filter = null;

        switch (nodeType) {
        case JJTROUTINEINVOCATION_RELOP_DWITHIN_NODE:
            filter = ff.dwithin(property, geom, tolerance, unit);

            break;

        case JJTROUTINEINVOCATION_RELOP_BEYOND_NODE:

            // filter = ff.beyond(property, geom, tolerance, unit);
            // FIXME problem with Geometry param (Expresion is Needed)
            break;

        default:
            throw new CQLException("Binary spatial operator unexpected");
        }

        return filter;
    }

    /**
     * Creates PropertyIsEqualTo with PropertyExists predicate
     *
     * @return PropertyIsEqualTo
     * @throws CQLException
     */
    private PropertyIsEqualTo buildPropertyExists() throws CQLException {
        PropertyName property = resultStack.popPropertyName();

        org.opengis.filter.expression.Expression[] args = new org.opengis.filter.expression.Expression[1];
        args[0] = filterFactory.literal(property);

        Function function = filterFactory.function("PropertyExists", args);
        Literal literalTrue = filterFactory.literal(Boolean.TRUE);

        PropertyIsEqualTo propExistsFilter = filterFactory.equals(function, literalTrue);

        return propExistsFilter;
    }

    private org.opengis.filter.Filter buildTemporalPredicateBeforeOrDuring()
        throws CQLException {
        org.opengis.filter.Filter filter = null;

        Result node = resultStack.peek();

        switch (node.getNodeType()) {
        case JJTPERIOD_BETWEEN_DATES_NODE:
        case JJTPERIOD_WITH_DATE_DURATION_NODE:
        case JJTPERIOD_WITH_DURATION_DATE_NODE:
            filter = buildPropertyIsLTELastDate();

            break;

        default:
            throw new CQLException("unexpeted date time expression in temporal predicate.",
                node.getToken());
        }

        return filter;
    }

    private org.opengis.filter.Filter buildTemporalPredicateDuringOrAfter()
        throws CQLException {
        org.opengis.filter.Filter filter = null;

        Result node = resultStack.peek();

        switch (node.getNodeType()) {
        case JJTPERIOD_BETWEEN_DATES_NODE:
        case JJTPERIOD_WITH_DATE_DURATION_NODE:
        case JJTPERIOD_WITH_DURATION_DATE_NODE:
            filter = buildPropertyIsGTEFirstDate();

            break;

        default:
            throw new CQLException("unexpeted date time expression in temporal predicate.",
                node.getToken());
        }

        return filter;
    }

    /**
     * Create a literal with date time
     *
     * @param n
     *            with date time
     * @return Literal
     * @throws CQLException
     */
    private org.opengis.filter.expression.Literal buildDateTimeExpression()
        throws CQLException {
        org.opengis.filter.expression.Literal literalDate = null;
        Token token = getToken(0);

        try {
            Date dateTime = DATE_FORMAT.parse(token.image);
            literalDate = filterFactory.literal(dateTime);

            return literalDate;
        } catch (java.text.ParseException e) {
            throw new CQLException("Unsupported date time format", token);
        }
    }

    /**
     * builds a PeriodNode (date1,date2)
     *
     * @return PeriodNode
     *
     * @throws CQLException
     */
    private PeriodNode buildPeriodBetweenDates() throws CQLException {
        org.opengis.filter.expression.Literal end = resultStack.popLiteral();

        org.opengis.filter.expression.Literal begin = resultStack.popLiteral();

        PeriodNode period = PeriodNode.createPeriodDateAndDate(begin, end);

        return period;
    }

    /**
     * builds a Period Node with (duration,date).
     *
     * @return PeriodNode
     * @throws CQLException
     */
    private PeriodNode buildPeriodDurationAndDate() throws CQLException {
        Literal date = resultStack.popLiteral();

        Literal duration = resultStack.popLiteral();

        PeriodNode period = PeriodNode.createPeriodDurationAndDate(duration, date, filterFactory);

        return period;
    }

    /**
     * builds a Period with (date,duration)
     *
     * @return PeriodNode
     * @throws CQLException
     */
    private PeriodNode buildPeriodDateAndDuration() throws CQLException {
        Literal duration = resultStack.popLiteral();

        Literal date = resultStack.popLiteral();

        PeriodNode period = PeriodNode.createPeriodDateAndDuration(date, duration, filterFactory);

        return period;
    }

    /**
     * Create an integer literal with duration value.
     *
     * @return Literal
     */
    private org.opengis.filter.expression.Literal buildDurationExpression() {
        Token token = getToken(0);
        String duration = token.image;
        org.opengis.filter.expression.Literal literalDuration = filterFactory.literal(duration);

        return literalDuration;
    }

    /**
     * Build the apropiate filter for before date and before period filters
     *
     * @param nodeType
     *
     * @return Filter
     * @throws CQLException
     */
    private org.opengis.filter.Filter buildTemporalPredicateBefore()
        throws CQLException {
        org.opengis.filter.Filter filter = null;

        // analiza si se trata de un periodo o una fecha
        Result node = resultStack.peek();

        switch (node.getNodeType()) {
        case JJTDATETIME_NODE:
            filter = buildBinaryComparasionOperator(JJTCOMPARISSONPREDICATE_LT_NODE);

            break;

        case JJTPERIOD_BETWEEN_DATES_NODE:
        case JJTPERIOD_WITH_DATE_DURATION_NODE:
        case JJTPERIOD_WITH_DURATION_DATE_NODE:
            filter = buildPropertyIsLTFirsDate();

            break;

        default:
            throw new CQLException("unexpeted date time expression in temporal predicate.",
                node.getToken());
        }

        return filter;
    }

    /**
     * Build the apropiate filter for during period filters
     *
     * @return Filter
     * @throws CQLException
     * @throws CQLException
     */
    private Object buildTemporalPredicateDuring() throws CQLException {
        org.opengis.filter.Filter filter = null;

        // determines if the node is period or date
        Result node = resultStack.peek();

        switch (node.getNodeType()) {
        case JJTPERIOD_BETWEEN_DATES_NODE:
        case JJTPERIOD_WITH_DATE_DURATION_NODE:
        case JJTPERIOD_WITH_DURATION_DATE_NODE:
            filter = buildPropertyBetweenDates();

            break;

        default:
            throw new CQLException("unexpeted period expression in temporal predicate.",
                node.getToken());
        }

        return filter;
    }

    /**
     * Create an AND filter with property between dates of period. (firstDate<=
     * property <= lastDate)
     *
     * @return And filter
     *
     * @throws CQLException
     */
    private org.opengis.filter.Filter buildPropertyBetweenDates()
        throws CQLException {
        // retrieve date and duration of expression
        Result node = resultStack.popResult();
        PeriodNode period = (PeriodNode) node.getBuilt();

        org.opengis.filter.expression.Literal begin = period.getBeginning();
        org.opengis.filter.expression.Literal end = period.getEnding();

        // create and filter firstDate<= property <= lastDate
        org.opengis.filter.expression.Expression property = resultStack.popExpression();

        org.opengis.filter.Filter filter = filterFactory.and(filterFactory.lessOrEqual(begin,
                    property), filterFactory.lessOrEqual(property, end));

        return filter;
    }

    /**
     * build filter for after date and after period
     *
     * @return a filter
     * @throws CQLException
     */
    private org.opengis.filter.Filter buildTemporalPredicateAfter()
        throws CQLException {
        org.opengis.filter.Filter filter = null;

        // analiza si se trata de un periodo o una fecha
        Result node = resultStack.peek();

        switch (node.getNodeType()) {
        case JJTDATETIME_NODE:
            filter = buildBinaryComparasionOperator(JJTCOMPARISSONPREDICATE_GT_NODE);

            break;

        case JJTPERIOD_BETWEEN_DATES_NODE:
        case JJTPERIOD_WITH_DURATION_DATE_NODE:
        case JJTPERIOD_WITH_DATE_DURATION_NODE:
            filter = buildPropertyIsGTLastDate();

            break;

        default:
            throw new CQLException("unexpeted date time expression in temporal predicate.",
                node.getToken());
        }

        return filter;
    }

    /**
     * creates PropertyIsGreaterThan end date of period
     *
     * @return PropertyIsGreaterThan
     *
     * @throws CQLException
     */
    private PropertyIsGreaterThan buildPropertyIsGTLastDate()
        throws CQLException {
        Result node = resultStack.popResult();
        PeriodNode period = (PeriodNode) node.getBuilt();

        org.opengis.filter.expression.Literal date = period.getEnding();

        org.opengis.filter.expression.Expression property = resultStack.popExpression();

        PropertyIsGreaterThan filter = filterFactory.greater(property, date);

        return filter;
    }

    /**
     * Builds PropertyIsGreaterThanOrEqualTo begin of period
     *
     * @return PropertyIsGreaterThanOrEqualTo
     * @throws CQLException
     */
    private PropertyIsGreaterThanOrEqualTo buildPropertyIsGTEFirstDate()
        throws CQLException {
        Result node = resultStack.popResult();
        PeriodNode period = (PeriodNode) node.getBuilt();

        org.opengis.filter.expression.Literal begin = period.getBeginning();

        org.opengis.filter.expression.Expression property = (org.opengis.filter.expression.Expression) resultStack
            .popExpression();

        PropertyIsGreaterThanOrEqualTo filter = filterFactory.greaterOrEqual(property, begin);

        return filter;
    }

    private PropertyIsLessThan buildPropertyIsLTFirsDate()
        throws CQLException {
        PeriodNode period = resultStack.popPeriod();

        org.opengis.filter.expression.Literal date = period.getBeginning();

        org.opengis.filter.expression.Expression property = resultStack.popExpression();

        PropertyIsLessThan filter = filterFactory.less(property, date);

        return filter;
    }

    private PropertyIsLessThanOrEqualTo buildPropertyIsLTELastDate()
        throws CQLException {
        PeriodNode period = resultStack.popPeriod();

        org.opengis.filter.expression.Literal date = period.getEnding();

        org.opengis.filter.expression.Expression property = resultStack.popExpression();

        PropertyIsLessThanOrEqualTo filter = filterFactory.lessOrEqual(property, date);

        return filter;
    }

    /**
     * Builds a compare filter
     *
     * @param filterTipa
     *
     * @return BinaryComparisonOperator
     * @throws CQLException
     */
    private BinaryComparisonOperator buildBinaryComparasionOperator(int filterType)
        throws CQLException {
        org.opengis.filter.expression.Expression right = resultStack.popExpression();
        org.opengis.filter.expression.Expression left = resultStack.popExpression();

        switch (filterType) {
        case JJTCOMPARISSONPREDICATE_EQ_NODE:
            return filterFactory.equals(left, right);

        case JJTCOMPARISSONPREDICATE_GT_NODE:
            return filterFactory.greater(left, right);

        case JJTCOMPARISSONPREDICATE_LT_NODE:
            return filterFactory.less(left, right);

        case JJTCOMPARISSONPREDICATE_GTE_NODE:
            return filterFactory.greaterOrEqual(left, right);

        case JJTCOMPARISSONPREDICATE_LTE_NODE:
            return filterFactory.lessOrEqual(left, right);

        default:
            throw new CQLException("unexpeted filter type.");
        }
    }

    /**
     * makes Binary Comparasion filter
     *
     * @param filterType
     * @param right
     * @param left
     * @return buildBinaryComparasionOperator
     */
    private BinaryComparisonOperator buildBinaryComparasion(
            final int filterType,
            final org.opengis.filter.expression.Expression right,
            final org.opengis.filter.expression.Expression left) {
        
        switch (filterType) {
        case LTE:
            return filterFactory.lessOrEqual(left, right);

        case LT:
            return filterFactory.less(left, right);

        case GTE:
            return filterFactory.greaterOrEqual(left, right);

        case GT:
            return filterFactory.greater(left, right);

        case EQ:
            return filterFactory.equals(left, right);

        default:
            return null;
        }
    }

    /**
     * Builds geometry
     * 
     * @param geometry
     * @return a geometry
     * @throws CQLException
     */
    private Literal buildGeometry(final Token geometry)
        throws CQLException {
        try {
            String wktGeom = scanExpression(geometry);

            // transforms wkt to vividsolution geometry
            String vividGeom = transformWKTGeometry(wktGeom);

            Geometry g = WKT_READER.read(vividGeom);

            Literal literal = filterFactory.literal(g);

            return literal;
        } catch (com.vividsolutions.jts.io.ParseException e) {
            throw new CQLException(e.getMessage(), geometry);
        } catch (Exception e) {
            throw new CQLException("Error building WKT Geometry", geometry, e);
        }
    }

    /**
     * Extracts expression between initial token and last token in buffer.
     *
     * @param initialTocken
     *
     * @return String
     */
    private String scanExpression(final Token initialTocken) {
        // scan geometry
        Token end = initialTocken;

        while (end.next != null) {
            end = end.next;
        }

        String wktGeom = input.substring(initialTocken.beginColumn - 1, end.endColumn);

        return wktGeom;
    }

    /**
     * This transformation is required because some geometries like
     * <b>Multipoint</b> has diferent definition in vividsolucion library.
     * <p>
     *
     * <pre>
     * Then OGC require MULTIPOINT((1 2), (3 4))
     * but vividsolunion works without point &quot;(&quot; ans &quot;)&quot;
     * MULTIPOINT(1 2, 3 4)
     * </pre>
     *
     * <p>
     *
     * @param wktGeom
     *            ogc wkt geometry
     * @return String vividsolution geometry
     */
    private String transformWKTGeometry(final String wktGeom) {
        final String MULTIPOINT_TYPE = "MULTIPOINT";

        StringBuffer transformed = new StringBuffer(30);
        StringBuffer source = new StringBuffer(wktGeom.toUpperCase());

        int cur = -1;

        if ((cur = source.indexOf(MULTIPOINT_TYPE)) != -1) {
            // extract "(" and ")" from points in arguments
            String argument = source.substring(cur + MULTIPOINT_TYPE.length() + 1,
                    source.length() - 1);

            argument = argument.replace('(', ' ');
            argument = argument.replace(')', ' ');

            transformed.append(MULTIPOINT_TYPE).append("(").append(argument).append(")");

            return transformed.toString();
        } else {
            return wktGeom;
        }
    }

    private Literal buildEnvelop(Token token) {
        String source = scanExpression(token);

        final String ENVELOPE_TYPE = "ENVELOPE";

        int cur = source.indexOf(ENVELOPE_TYPE);

        // transforms CQL envelop envelop(West,East,North,South) to
        // GS84 West=minX, East=maxX, North=maxY, South=minY
        double minX;

        // transforms CQL envelop envelop(West,East,North,South) to
        // GS84 West=minX, East=maxX, North=maxY, South=minY
        double minY;

        // transforms CQL envelop envelop(West,East,North,South) to
        // GS84 West=minX, East=maxX, North=maxY, South=minY
        double maxX;

        // transforms CQL envelop envelop(West,East,North,South) to
        // GS84 West=minX, East=maxX, North=maxY, South=minY
        double maxY;

        cur = cur + ENVELOPE_TYPE.length() + 1;

        String argument = source.substring(cur, source.length() - 1);

        final String comma = ",";
        cur = 0;

        int end = argument.indexOf(comma, cur);
        String west = argument.substring(cur, end);
        minX = Double.parseDouble(west);

        cur = end + 1;
        end = argument.indexOf(comma, cur);

        String east = argument.substring(cur, end);
        maxX = Double.parseDouble(east);

        cur = end + 1;
        end = argument.indexOf(comma, cur);

        String north = argument.substring(cur, end);
        maxY = Double.parseDouble(north);

        cur = end + 1;

        String south = argument.substring(cur);
        minY = Double.parseDouble(south);

        // ReferencedEnvelope envelope = new
        // ReferencedEnvelope(DefaultGeographicCRS.WGS84);
        // envelope.init(minX, minY, maxX, maxY);
        GeometryFactory gf = new GeometryFactory();

        Coordinate[] coords = {
                new Coordinate(minX, minY), new Coordinate(minX, maxY), new Coordinate(maxX, maxY),
                new Coordinate(maxX, minY), new Coordinate(minX, minY)
            };
        LinearRing shell = gf.createLinearRing(coords);
        Polygon bbox = gf.createPolygon(shell, null);
        bbox.setUserData(DefaultGeographicCRS.WGS84);

        Literal literal = filterFactory.literal(bbox);

        return literal;
    }

    /**
     * Builds a function expression
     *
     * @param n
     *            function node
     * @return Function
     * @throws CQLException
     */
    private Function buildFunction(Node n) throws CQLException {
        // FIXME FilterFactoryImpl cast must be precluded
        FilterFactoryImpl ff = (FilterFactoryImpl) filterFactory;

        String functionName = null; // token.image;

        // extracts the arguments from stack. Each argument in the stack
        // is preceded by an argument node. Finally extracts the function name
        List argList = new LinkedList();

        while (!resultStack.empty()) {
            Result node = resultStack.peek();

            if (node.getNodeType() == JJTFUNCTIONNAME_NODE) {
                // gets the function's name
                Result funcNameNode = resultStack.popResult();
                functionName = funcNameNode.getToken().image;

                break;
            }

            // ejects the argument node
            resultStack.popResult();

            // extracts the argument value
            Expression arg = resultStack.popExpression();
            argList.add(arg);
        }

        // Puts the argument in correct order
        Collections.reverse(argList);

        Expression[] args = (Expression[]) argList.toArray(new Expression[argList.size()]);

        Function function = ff.function(functionName, args);

        if (function == null) {
            throw new CQLException("function not found: ", token);
        }

        return function;
    }
}
