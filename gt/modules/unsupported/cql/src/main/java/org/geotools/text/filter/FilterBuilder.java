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

package org.geotools.text.filter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.Hints;
import org.geotools.filter.FilterFactoryImpl;
import org.geotools.filter.FilterTransformer;
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
 * FilterBuilder (the original name was ExpressionBuilder) is the main entry
 * point for parsing Filters from the language.
 * <p>
 * This class was extended to generate semantic actions for all the CQL
 * production rules.
 * </p>
 * <p>
 * Aditionaly refactoring was done in order to adapt the products to the new
 * GeoAPI filter interfaces, targeting Filter 1.1.0.
 * </p>
 * <p>
 * <b>CQL</b> is an acronym for OGC Common Query Language, a query predicate
 * language whose syntax is similar to a SQL WHERE clause, defined in clause
 * 6.2.2 of the OGC Catalog Service for Web, version 2.0.1 implementation
 * specification.
 * </p>
 * <p>
 * <h2>Usage</h2>
 * This class provides two methods, {@link #parse(String)} and
 * {@link #parse(org.opengis.filter.FilterFactory, String)},
 * 
 * </p>
 * 
 * @since 2.4
 * @author Created by: Ian Schneider
 * @author Extended by: Mauricio Pazos - Axios Engineering
 * @author Extended by: Gabriel Roldan - Axios Engineering
 * @version $Id$
 * @source $URL:
 *         http://gtsvn.refractions.net/geotools/trunk/gt/modules/unsupported/cql/src/main/java/org/geotools/text/filter/FilterBuilder.java $
 */
public class FilterBuilder {

    /**
     * Delimiter characted used for
     * {@link #parseFilterList(FilterFactory, String)} to distinguish between
     * the different filters in a list (for example:
     * <code>att > 1| att2 < 3</code>
     */
    public static final String DELIMITER = "|";

    /**
     * Parses the input string in OGC CQL format into a Filter, using the
     * provided FilterFactory.
     * 
     * @param filterFactory
     *            the {@link FilterFactory} to use for the creation of the
     *            Filter.
     * @param input
     *            a string containing a query predicate in OGC CQL format.
     * @return a {@link Filter} equivalent to the constraint specified in
     *         <code>input</code>.
     */
    public static org.opengis.filter.Filter parse(FilterFactory filterFactory, String input)
            throws ParseException {

        if (filterFactory == null) {
            filterFactory = CommonFactoryFinder.getFilterFactory((Hints) null);
        }

        CQLCompiler c = new CQLCompiler(input, filterFactory);
        try {
            c.CompilationUnit();
        } catch (TokenMgrError tme) {
            throw new FilterBuilderException(tme.getMessage(), c.getToken(0));
        }
        if (c.exception != null)
            throw c.exception;

        Object result = c.getResult();
        Filter builtFilter = (Filter) result;
        return builtFilter;
    }

    public static List parseFilterList(FilterFactory filterFactory, String input)
            throws ParseException {

        if (filterFactory == null) {
            filterFactory = CommonFactoryFinder.getFilterFactory((Hints) null);
        }

        CQLCompiler c = new CQLCompiler(input, filterFactory);
        try {
            c.MultipleCompilationUnit();
        } catch (TokenMgrError tme) {
            throw new FilterBuilderException(tme.getMessage() + ": " + input, c.getToken(0));
        }
        if (c.exception != null)
            throw c.exception;

        List results = c.getResults();
        return results;
    }

    /**
     * Parses the input string in OGC CQL format into a Filter, using the
     * systems default FilterFactory implementation.
     * 
     * @param input
     *            a string containing a query predicate in OGC CQL format.
     * @return a {@link Filter} equivalent to the constraint specified in
     *         <code>input</code>.
     */
    public static org.opengis.filter.Filter parse(String input) throws ParseException {
        return parse(null, input);
    }

    /**
     * Parses the input string in OGC CQL format into an Expression, using the
     * provided FilterFactory.
     * 
     * @param filterFactory
     *            the {@link FilterFactory} to use for the creation of the
     *            Expression.
     * @param input
     *            a string containing a OGC CQL expression.
     * @return a {@link Filter} equivalent to the constraint specified in
     *         <code>input</code>.
     */
    public static org.opengis.filter.expression.Expression parseExpression(
            FilterFactory filterFactory, String input) throws ParseException {

        if (filterFactory == null) {
            filterFactory = CommonFactoryFinder.getFilterFactory((Hints) null);
        }

        CQLCompiler c = new CQLCompiler(input, filterFactory);
        try {
            c.ExpressionCompilationUnit();
        } catch (TokenMgrError tme) {
            throw new FilterBuilderException(tme.getMessage(), c.getToken(0));
        }
        if (c.exception != null)
            throw c.exception;

        Expression builtFilter = (Expression) c.getResult();
        return builtFilter;
    }

    /**
     * Parses the input string in OGC CQL format into an Expression, using the
     * systems default FilterFactory implementation.
     * 
     * @param input
     *            a string containing an OGC CQL expression.
     * @return a {@link Expression} equivalent to the one specified in
     *         <code>input</code>.
     */
    public static org.opengis.filter.expression.Expression parseExpression(String input)
            throws ParseException {
        return parseExpression(null, input);
    }

    /**
     * Returns a formatted error string, showing the original input, along with
     * a pointer to the location of the error and the error message itself.
     */
    public static String getFormattedErrorMessage(ParseException pe, String input) {

        StringBuffer sb = new StringBuffer(input);
        sb.append('\n');
        Token t = pe.currentToken;
        while (t.next != null)
            t = t.next;
        int column = t.beginColumn - 1;
        for (int i = 0; i < column; i++) {
            sb.append(' ');
        }
        sb.append('^').append('\n');
        sb.append(pe.getMessage());
        return sb.toString();
    }

    private static class CQLCompiler extends CQLParser implements CQLParserTreeConstants {

        private static final String ATTRIBUTE_PATH_SEPARATOR = "/";

        private static final DateFormat DATE_FORMAT = new SimpleDateFormat(
                "yyyy-MM-dd'T'HH:mm:ss'Z'");

        private static final WKTReader WKT_READER = new WKTReader();

        private BuildResultStack resultStack;

        private FilterBuilderException exception = null;

        private String input = null;

        private FilterFactory filterFactory;

        CQLCompiler(String input, FilterFactory filterFactory) {
            super(new StringReader(input));
            this.input = input;
            this.filterFactory = filterFactory;
            this.resultStack = BuildResultStack.getInstance();
        }

        /**
         * @return either an Expression or a Filter, depending on what the
         *         product of the compilation unit was
         * @throws FilterBuilderException
         */
        public Object getResult() throws FilterBuilderException {
            Result item = resultStack.peek();
            Object result = item.getBuilt();
            return result;
        }

        /**
         * Returns the list of Filters built as the result of calling
         * {@link #MultipleCompilationUnit()}
         * 
         * @return
         * @throws FilterBuilderException
         *             if a ClassCastException occurs while casting a built item
         *             to a Filter.
         */
        public List getResults() throws FilterBuilderException {
            int size = resultStack.size();
            List results = new ArrayList(size);
            for (int i = 0; i < size; i++) {
                Result item = resultStack.popResult();
                Object result = item.getBuilt();
                results.add(0, result);
            }
            return results;
        }

        public void jjtreeOpenNodeScope(Node n) {
        }

        public void jjtreeCloseNodeScope(Node n) throws ParseException {
            try {
                Object built = buildObject(n);
                if (built == null)
                    throw new RuntimeException("INTERNAL ERROR : Node " + n
                            + " resulted in null build");
                resultStack.push(new Result(built, getToken(0), n.getType()));

            } finally {
                n.dispose();
            }
        }

        private org.opengis.filter.expression.BinaryExpression buildBinaryExpression(int nodeType)
                throws FilterBuilderException {

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
                throws FilterBuilderException {
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
                    throw new FilterBuilderException(
                            "Expression not supported. And, Or, Not is required", getToken(0));
                }

                return logicFilter;
            } catch (IllegalFilterException ife) {
                throw new FilterBuilderException("Exception building LogicFilter", getToken(0), ife);
            }
        }

        private PropertyIsLike buildLikeFilter() throws FilterBuilderException {

            final String WC_MULTI = "%";
            final String WC_SINGLE = "_";
            final String ESCAPE = "\\";

            try {
                org.opengis.filter.expression.Expression pattern = resultStack.popExpression();
                org.opengis.filter.expression.Expression expr = resultStack.popExpression();

                PropertyIsLike f = filterFactory.like(expr, pattern.toString(), WC_MULTI,
                        WC_SINGLE, ESCAPE);

                return f;

            } catch (IllegalFilterException ife) {
                throw new FilterBuilderException("Exception building LikeFilter", getToken(0), ife);
            }
        }

        /**
         * Builds property is null filter
         * 
         * @return PropertyIsNull
         * @throws FilterBuilderException
         */
        private PropertyIsNull buildPropertyIsNull() throws FilterBuilderException {

            try {
                org.opengis.filter.expression.Expression property = resultStack.popExpression();

                PropertyIsNull filter = filterFactory.isNull(property);

                return filter;

            } catch (FilterBuilderException e) {
                throw new FilterBuilderException("Exception building Null Predicate", getToken(0),
                        e);
            }

        }

        /**
         * builds PropertyIsBetween filter
         * 
         * @return PropertyIsBetween
         * @throws FilterBuilderException
         */
        private PropertyIsBetween buildBetween() throws FilterBuilderException {
            try {
                org.opengis.filter.expression.Expression sup = resultStack.popExpression();
                org.opengis.filter.expression.Expression inf = resultStack.popExpression();
                org.opengis.filter.expression.Expression expr = resultStack.popExpression();

                PropertyIsBetween filter = filterFactory.between(expr, inf, sup);

                return filter;

            } catch (IllegalFilterException ife) {
                throw new FilterBuilderException("Exception building CompareFilter", getToken(0),
                        ife);
            }
        }

        /**
         * This method is called when the parser close a node. Here is built the
         * filters an expressions recognized in the parsing process.
         * 
         * @param n
         *            a Node instance
         * @return Filter or Expression
         * @throws FilterBuilderException
         */
        protected Object buildObject(Node n) throws FilterBuilderException {
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
            case JJTFUNCTION_NODE:
                return buildFunction(n);

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
                org.opengis.filter.Filter filterPropNotExist = filterFactory
                        .not(buildPropertyExists());
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
                throw new FilterBuilderException(
                        "Unsupported geooperation RELATE (is not implemented by GeoTools)",
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

        private PropertyName buildCompoundAttribute() throws FilterBuilderException {

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

        private String buildIdentifier() throws FilterBuilderException {

            // precondition: the stack have one or more parts (string type)
            // retrieve all part of identifier from result stack
            try {
                ArrayList arrayParts = new ArrayList();

                while (resultStack.size() > 0) {

                    Result r = resultStack.peek();
                    if (!((r.getNodeType() == JJTIDENTIFIER_START_NODE) || (r.getNodeType() == JJTIDENTIFIER_PART_NODE))) {
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
                }// postcondition i=0

                part = (String) arrayParts.get(i);
                identifier.append(part);

                return identifier.toString();

            } catch (FilterBuilderException e) {

                throw new FilterBuilderException("Fail builing identifier: " + e.getMessage());
            }
        }

        /**
         * Creates the identifier part. An identifier like
         * "idpart1:idpart2:idpart3: ... idpartN" has N part.
         * 
         * @return Name
         */
        private String buildIdentifierPart() {

            String part = getToken(0).image;

            return part;
        }

        private PropertyName buildSimpleAttribute() throws FilterBuilderException {

            // Only retrieve the identifier built before
            String identifier = resultStack.popIdentifier();
            PropertyName property = filterFactory.property(identifier);

            return property;
        }

        private Literal buildDistanceUnit() throws FilterBuilderException {

            Literal unit = null;
            Token token = getToken(0);
            unit = filterFactory.literal(token.image);

            return unit;
        }

        private Literal buildTolerance() throws FilterBuilderException {

            Literal tolerance = null;
            try {

                tolerance = resultStack.popLiteral();
                return tolerance;
            } catch (NumberFormatException e) {
                throw new FilterBuilderException("Unsupported number format", token);
            }
        }

        /**
         * Creates Binary Spatial Operator
         * 
         * @param tipeNode
         * 
         * @return Filter (must be BinarySpatialOperator) // FIXME see equals
         * @throws FilterBuilderException
         */
        private BinarySpatialOperator buildBinarySpatialOperator(final int nodeType)
                throws FilterBuilderException {

            Literal geom = resultStack.popLiteral();

            org.opengis.filter.expression.Expression property = resultStack.popExpression();

            FilterFactory2 ff = (FilterFactory2) filterFactory;// TODO
            // expecting
            // implementation
            // of new geoapi
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
                throw new FilterBuilderException("Binary spatial operator unexpected");
            }

            return filter;
        }

        private org.opengis.filter.spatial.BBOX buildBBox(int nodeType)
                throws FilterBuilderException {

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

                throw new FilterBuilderException("Fails building BBOX filter (" + e.getMessage()
                        + ")");
            }

        }

        /**
         * Builds Distance Buffer Operator
         * 
         * @param nodeType
         * @return DistanceBufferOperator dwithin and beyond filters
         * @throws FilterBuilderException
         */
        private DistanceBufferOperator buildDistanceBufferOperator(final int nodeType)
                throws FilterBuilderException {

            String unit = resultStack.popStringValue();

            double tolerance = resultStack.popDoubleValue();

            org.opengis.filter.expression.Expression geom = resultStack.popExpression();

            org.opengis.filter.expression.Expression property = resultStack.popExpression();

            FilterFactory2 ff = (FilterFactory2) filterFactory;// TODO
            // expecting
            // implementation
            // of new geoapi

            DistanceBufferOperator filter = null;

            switch (nodeType) {

            case JJTROUTINEINVOCATION_RELOP_DWITHIN_NODE:
                filter = ff.dwithin(property, geom, tolerance, unit);
                break;

            case JJTROUTINEINVOCATION_RELOP_BEYOND_NODE:
                // filter = ff.beyond(property, geom, tolerance, unit); FIXME
                // problem with Geometry param (Expresion is Needed)
                break;

            default:
                throw new FilterBuilderException("Binary spatial operator unexpected");
            }

            return filter;
        }

        /**
         * Creates PropertyIsEqualTo with PropertyExists predicate
         * 
         * @return PropertyIsEqualTo
         * @throws FilterBuilderException
         */
        private PropertyIsEqualTo buildPropertyExists() throws FilterBuilderException {

            PropertyName property = resultStack.popPropertyName();

            org.opengis.filter.expression.Expression[] args = new org.opengis.filter.expression.Expression[1];
            args[0] = filterFactory.literal(property);

            Function function = filterFactory.function("PropertyExists", args);
            Literal literalTrue = filterFactory.literal(Boolean.TRUE);

            PropertyIsEqualTo propExistsFilter = filterFactory.equals(function, literalTrue);

            return propExistsFilter;
        }

        private org.opengis.filter.Filter buildTemporalPredicateBeforeOrDuring()
                throws FilterBuilderException {

            org.opengis.filter.Filter filter = null;

            Result node = resultStack.peek();

            switch (node.getNodeType()) {
            case JJTPERIOD_BETWEEN_DATES_NODE:
            case JJTPERIOD_WITH_DATE_DURATION_NODE:
            case JJTPERIOD_WITH_DURATION_DATE_NODE:
                filter = buildPropertyIsLTELastDate();
                break;
            default:
                throw new FilterBuilderException(
                        "unexpeted date time expression in temporal predicate.", node.getToken());
            }
            return filter;
        }

        private org.opengis.filter.Filter buildTemporalPredicateDuringOrAfter()
                throws FilterBuilderException {

            org.opengis.filter.Filter filter = null;

            Result node = resultStack.peek();

            switch (node.getNodeType()) {
            case JJTPERIOD_BETWEEN_DATES_NODE:
            case JJTPERIOD_WITH_DATE_DURATION_NODE:
            case JJTPERIOD_WITH_DURATION_DATE_NODE:
                filter = buildPropertyIsGTEFirstDate();
                break;
            default:
                throw new FilterBuilderException(
                        "unexpeted date time expression in temporal predicate.", node.getToken());
            }
            return filter;
        }

        /**
         * Create a literal with date time
         * 
         * @param n
         *            with date time
         * @return Literal
         * @throws FilterBuilderException
         */
        private org.opengis.filter.expression.Literal buildDateTimeExpression()
                throws FilterBuilderException {

            org.opengis.filter.expression.Literal literalDate = null;
            Token token = getToken(0);
            try {
                Date dateTime = DATE_FORMAT.parse(token.image);
                literalDate = filterFactory.literal(dateTime);

                return literalDate;

            } catch (java.text.ParseException e) {
                throw new FilterBuilderException("Unsupported date time format", token);
            }
        }

        /**
         * builds a PeriodNode (date1,date2)
         * 
         * @return PeriodNode
         * 
         * @throws FilterBuilderException
         */
        private PeriodNode buildPeriodBetweenDates() throws FilterBuilderException {

            org.opengis.filter.expression.Literal end = resultStack.popLiteral();

            org.opengis.filter.expression.Literal begin = resultStack.popLiteral();

            PeriodNode period = PeriodNode.createPeriodDateAndDate(begin, end);

            return period;
        }

        /**
         * builds a Period Node with (duration,date).
         * 
         * @return PeriodNode
         * @throws FilterBuilderException
         */
        private PeriodNode buildPeriodDurationAndDate() throws FilterBuilderException {

            Literal date = resultStack.popLiteral();

            Literal duration = resultStack.popLiteral();

            PeriodNode period = PeriodNode.createPeriodDurationAndDate(duration, date,
                    filterFactory);

            return period;
        }

        /**
         * builds a Period with (date,duration)
         * 
         * @return PeriodNode
         * @throws FilterBuilderException
         */
        private PeriodNode buildPeriodDateAndDuration() throws FilterBuilderException {

            Literal duration = resultStack.popLiteral();

            Literal date = resultStack.popLiteral();

            PeriodNode period = PeriodNode.createPeriodDateAndDuration(date, duration,
                    filterFactory);

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
         * @throws FilterBuilderException
         */
        private org.opengis.filter.Filter buildTemporalPredicateBefore()
                throws FilterBuilderException {

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
                throw new FilterBuilderException(
                        "unexpeted date time expression in temporal predicate.", node.getToken());
            }
            return filter;
        }

        /**
         * Build the apropiate filter for during period filters
         * 
         * @return Filter
         * @throws FilterBuilderException
         * @throws FilterBuilderException
         */
        private Object buildTemporalPredicateDuring() throws FilterBuilderException {

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
                throw new FilterBuilderException(
                        "unexpeted period expression in temporal predicate.", node.getToken());
            }
            return filter;
        }

        /**
         * Create an AND filter with property between dates of period.
         * (firstDate<= property <= lastDate)
         * 
         * @return And filter
         * 
         * @throws FilterBuilderException
         */
        private org.opengis.filter.Filter buildPropertyBetweenDates() throws FilterBuilderException {

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
         * @return
         * @throws FilterBuilderException
         * @throws FilterBuilderException
         */
        private org.opengis.filter.Filter buildTemporalPredicateAfter()
                throws FilterBuilderException {

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
                throw new FilterBuilderException(
                        "unexpeted date time expression in temporal predicate.", node.getToken());
            }
            return filter;
        }

        /**
         * creates PropertyIsGreaterThan end date of period
         * 
         * @return PropertyIsGreaterThan
         * 
         * @throws FilterBuilderException
         */
        private PropertyIsGreaterThan buildPropertyIsGTLastDate() throws FilterBuilderException {

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
         * @throws FilterBuilderException
         */
        private PropertyIsGreaterThanOrEqualTo buildPropertyIsGTEFirstDate()
                throws FilterBuilderException {

            Result node = resultStack.popResult();
            PeriodNode period = (PeriodNode) node.getBuilt();

            org.opengis.filter.expression.Literal begin = period.getBeginning();

            org.opengis.filter.expression.Expression property = (org.opengis.filter.expression.Expression) resultStack
                    .popExpression();

            PropertyIsGreaterThanOrEqualTo filter = filterFactory.greaterOrEqual(property, begin);

            return filter;

        }

        private PropertyIsLessThan buildPropertyIsLTFirsDate() throws FilterBuilderException {

            PeriodNode period = resultStack.popPeriod();

            org.opengis.filter.expression.Literal date = period.getBeginning();

            org.opengis.filter.expression.Expression property = resultStack.popExpression();

            PropertyIsLessThan filter = filterFactory.less(property, date);

            return filter;

        }

        private PropertyIsLessThanOrEqualTo buildPropertyIsLTELastDate()
                throws FilterBuilderException {

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
         * @throws FilterBuilderException
         */
        private BinaryComparisonOperator buildBinaryComparasionOperator(int filterType)
                throws FilterBuilderException {

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
                throw new FilterBuilderException("unexpeted filter type.");
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
        private BinaryComparisonOperator buildBinaryComparasion(final int filterType,
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

        private Literal buildGeometry(final Token geometry) throws FilterBuilderException {
            try {
                String wktGeom = scanExpression(geometry);

                // transforms wkt to vividsolution geometry
                String vividGeom = transformWKTGeometry(wktGeom);

                Geometry g = WKT_READER.read(vividGeom);

                Literal literal = filterFactory.literal(g);

                return literal;
            } catch (com.vividsolutions.jts.io.ParseException e) {

                throw new FilterBuilderException(e.getMessage(), geometry);

            } catch (Exception e) {
                throw new FilterBuilderException("Error building WKT Geometry", geometry, e);
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
         *                 Then OGC require MULTIPOINT((1 2), (3 4)) 
         *                 but vividsolunion works without point &quot;(&quot; ans &quot;)&quot; 
         *                 MULTIPOINT(1 2, 3 4)
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
                String argument = source.substring(cur + MULTIPOINT_TYPE.length() + 1, source
                        .length() - 1);

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

            double minX, minY, maxX, maxY;

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

            Coordinate[] coords = { new Coordinate(minX, minY), new Coordinate(minX, maxY),
                    new Coordinate(maxX, maxY), new Coordinate(maxX, minY),
                    new Coordinate(minX, minY) };
            LinearRing shell = gf.createLinearRing(coords);
            Polygon bbox = gf.createPolygon(shell, null);
            bbox.setUserData(DefaultGeographicCRS.WGS84);
            Literal literal = filterFactory.literal(bbox);

            return literal;
        }

        private Function buildFunction(Node n) throws FilterBuilderException {

            // TODO expecting implementation of Expr.function(...)
            FilterFactoryImpl ff = (FilterFactoryImpl) filterFactory;

            Token token = n.getToken();
            String functionName = token.image;

            int nArgs = n.jjtGetNumChildren();

            Expression[] args = new Expression[nArgs];
            for (int i = 0; i < nArgs; i++) {
                args[i] = (Expression) resultStack.popExpression();
            }
            Function function = ff.function(functionName, args);
            if (function == null) {
                throw new FilterBuilderException("function not found: ", token);
            }

            return function;

            // FIXME arreglarla revisar box en CQL
            // String function = n.getToken().image;
            // if ("box".equalsIgnoreCase(function)) {
            // if (n.jjtGetNumChildren() != 4) {
            // throw new FilterBuilderException(
            // "Bounding Box filter requires 4 arguments",
            // getToken(0));
            // }
            //                
            // double d4 = doubleValue();
            // double d3 = doubleValue();
            // double d2 = doubleValue();
            // double d1 = doubleValue();
            // try {
            // return factory.createBBoxExpression(new Envelope(d1, d2,
            // d3, d4));
            // } catch (IllegalFilterException ife) {
            // throw new FilterBuilderException(
            // "Exception building BBoxExpression", getToken(0),
            // ife);
            // }
            // } else if ("id".equalsIgnoreCase(function)) {
            // if (n.jjtGetNumChildren() != 1) {
            // throw new FilterBuilderException(
            // "Feature ID filter requires 1 argument",
            // getToken(0));
            // }
            // return factory.createFidFilter(stringValue());
            // } else if ("between".equalsIgnoreCase(function)) {
            // if (n.jjtGetNumChildren() != 3) {
            // throw new FilterBuilderException(
            // "Between filter requires 3 arguments", getToken(0));
            // }
            // Expression two = popExpression();
            // Expression att = popExpression();
            // Expression one = popExpression();
            // try {
            // BetweenFilter b = factory.createBetweenFilter();
            // b.addLeftValue(one);
            // b.addMiddleValue(att);
            // b.addRightValue(two);
            // return b;
            // } catch (IllegalFilterException ife) {
            // throw new FilterBuilderException(
            // "Exception building BetweenFilter", getToken(0),
            // ife);
            // }
            //                
            // } else if ("like".equalsIgnoreCase(function)) {
            // if (n.jjtGetNumChildren() != 2) {
            // throw new FilterBuilderException(
            // "Like filter requires at least two arguments",
            // getToken(0));
            // }
            // LikeFilter f = factory.createLikeFilter();
            // f.setPattern(stringValue(), "*", ".?", "//");
            // try {
            // f.setValue(popExpression());
            // } catch (IllegalFilterException ife) {
            // throw new FilterBuilderException(
            // "Exception building LikeFilter", getToken(0), ife);
            // }
            // return f;
            // } else if ("null".equalsIgnoreCase(function)
            // || "isNull".equalsIgnoreCase(function)) {
            // NullFilter nf = factory.createNullFilter();
            // Expression e = popExpression();
            //                
            // try {
            // if (e instanceof LiteralExpression) {
            // e = factory
            // .createAttributeExpression(((LiteralExpression) e)
            // .getValue(null).toString());
            // }
            // nf.nullCheckValue(e);
            // } catch (IllegalFilterException ife) {
            // throw new FilterBuilderException(
            // "Exception building NullFilter", getToken(0), ife);
            // }
            // return nf;
            // }
            //            
            // short geometryFilterType = lookupGeometryFilter(function);
            // if (geometryFilterType >= 0)
            // return buildGeometryFilter(geometryFilterType);
            //            
            // FunctionExpression func = factory
            // .createFunctionExpression(function);
            // if (func == null)
            // throw new FilterBuilderException("Could not build function : "
            // + function, getToken(0));
            //            
            // int nArgs = func.getArgCount();
            // if (n.jjtGetNumChildren() != nArgs) {
            // throw new FilterBuilderException(function + " function requires "
            // + nArgs + " arguments", getToken(0));
            // }
            //            
            // Expression[] args = new Expression[func.getArgCount()];
            // for (int i = 0; i < args.length; i++) {
            // args[i] = popExpression();
            // }
            //            
            // func.setArgs(args);
            // return func;

        }

    }

    public static final void main(String[] args) throws Exception {
        System.out.println("Expression Tester");
        BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
        FilterTransformer t = new FilterTransformer();
        t.setIndentation(4);
        while (true) {
            System.out.print(">");
            String line = r.readLine();
            if (line.equals("quit"))
                break;
            try {
                Object b = FilterBuilder.parse(line);
                t.transform(b, System.out);
                System.out.println();
            } catch (ParseException pe) {
                System.out.println(FilterBuilder.getFormattedErrorMessage(pe, line));
            }
        }
    }
}
