/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
 *
 */

package org.geotools.data.complex.filter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.data.complex.AttributeMapping;
import org.geotools.data.complex.FeatureTypeMapping;
import org.geotools.factory.CommonFactoryFinder;
import org.opengis.feature.type.TypeName;
import org.opengis.filter.And;
import org.opengis.filter.BinaryComparisonOperator;
import org.opengis.filter.BinaryLogicOperator;
import org.opengis.filter.ExcludeFilter;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.Id;
import org.opengis.filter.IncludeFilter;
import org.opengis.filter.Not;
import org.opengis.filter.Or;
import org.opengis.filter.PropertyIsBetween;
import org.opengis.filter.PropertyIsEqualTo;
import org.opengis.filter.PropertyIsGreaterThan;
import org.opengis.filter.PropertyIsGreaterThanOrEqualTo;
import org.opengis.filter.PropertyIsLessThan;
import org.opengis.filter.PropertyIsLessThanOrEqualTo;
import org.opengis.filter.PropertyIsLike;
import org.opengis.filter.PropertyIsNotEqualTo;
import org.opengis.filter.PropertyIsNull;
import org.opengis.filter.expression.Add;
import org.opengis.filter.expression.BinaryExpression;
import org.opengis.filter.expression.Divide;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.ExpressionVisitor;
import org.opengis.filter.expression.Function;
import org.opengis.filter.expression.Literal;
import org.opengis.filter.expression.Multiply;
import org.opengis.filter.expression.NilExpression;
import org.opengis.filter.expression.PropertyName;
import org.opengis.filter.expression.Subtract;
import org.opengis.filter.spatial.BBOX;
import org.opengis.filter.spatial.Beyond;
import org.opengis.filter.spatial.BinarySpatialOperator;
import org.opengis.filter.spatial.Contains;
import org.opengis.filter.spatial.Crosses;
import org.opengis.filter.spatial.DWithin;
import org.opengis.filter.spatial.Disjoint;
import org.opengis.filter.spatial.Equals;
import org.opengis.filter.spatial.Intersects;
import org.opengis.filter.spatial.Overlaps;
import org.opengis.filter.spatial.Touches;
import org.opengis.filter.spatial.Within;

/**
 * A Filter visitor that traverse a Filter or Expression made against a complex
 * FeatureType, and that uses the attribute and type mapping information given
 * by a {@linkplain org.geotools.data.complex.FeatureTypeMapping} object to
 * produce an equivalent Filter that operates against the original FeatureType.
 * <p>
 * Usage:
 * 
 * <pre>
 * <code>
 *                   Filter filterOnTargetType = ...
 *                   FeatureTypeMappings schemaMapping = ....
 *                   
 *                   UnMappingFilterVisitor visitor = new UnmappingFilterVisitor(schemaMapping);
 *                   visitor.visit(filterOnTargetType);
 *                   
 *                   Filter filterOnSourceType = visitor.getUnrolledFilter();
 * </code>
 * </pre>
 * 
 * </p>
 * 
 * @author Gabriel Roldan, Axios Engineering
 */
public class UnmappingFilterVisitor implements
		org.opengis.filter.FilterVisitor, ExpressionVisitor {
	private static final Logger LOGGER = Logger
			.getLogger(UnmappingFilterVisitor2.class.getPackage().getName());

	private Filter unrolled = Filter.INCLUDE;

	private Map/* <String, Expression> */idMappings;

	private Map/* <List<XPath.Step>, Expression> */attributeMappings;

	FeatureTypeMapping mappings;

	private static final FilterFactory2 ff = (FilterFactory2) CommonFactoryFinder
			.getFilterFactory(null);

	/**
	 * visit(*Expression) holds the unmapped expression here. Package visible
	 * just for unit tests
	 */
	List/* <Expression> */unrolledExpressions = new ArrayList/* <Expression> */(
			2);

	public UnmappingFilterVisitor(FeatureTypeMapping mappings) {
		this.mappings = mappings;
		this.idMappings = mappings.getIdMappings();

		this.attributeMappings = new HashMap/* <List<XPath.Step>, Expression> */();
		TypeName featureNode = mappings.getTargetFeature();

		for (Iterator itr = mappings.getAttributeMappings().iterator(); itr
				.hasNext();) {
			AttributeMapping attMapping = (AttributeMapping) itr.next();
			Expression exp = attMapping.getSourceExpression();
			String targetAttribute = attMapping.getTargetXPath();
			/*
			 * List<XPath.Step> simplifiedSteps; try{ simplifiedSteps =
			 * XPath.steps(featureNode, targetAttribute);
			 * }catch(RuntimeException e){ e.printStackTrace(); throw e; }
			 * this.attributeMappings.put(simplifiedSteps, exp);
			 */
			this.attributeMappings.put(targetAttribute, exp);
		}
	}

	public UnmappingFilterVisitor(FeatureTypeMapping mappings,
			Map/* <List<XPath.Step>, Expression> */attributeMappings) {
		this.mappings = mappings;
		this.attributeMappings = attributeMappings;
		this.idMappings = mappings.getIdMappings();
	}

	private UnmappingFilterVisitor2 copy() {
		return new UnmappingFilterVisitor2(mappings, attributeMappings);
	}

	/**
	 * Returns the Filter produced to operate over the original FeatureType.
	 * This method should be called only after {@linkplain #visit(Filter)} has
	 * been called.
	 * 
	 * @return
	 */
	public Filter getUnrolledFilter() {
		return unrolled;
	}

	/**
	 * Starts traversing the <code>filter</code>
	 */
	public void visit(Filter filter) {
		if (Filter.INCLUDE.equals(filter) || Filter.EXCLUDE.equals(filter)) {
			this.unrolled = filter;
			return;
		}
		filter.accept(this, null);
	}

	public void visit(Expression expression) {
		expression.accept(this, null);
	}

	/**
	 * Returns a CompareFilter of the same type than <code>filter</code>, but
	 * built on the unmapped expressions pointing to the surrogate type
	 * attributes.
	 */
	public Expression[] visitBinaryComparisonOperator(
			BinaryComparisonOperator filter) {
		Expression left = filter.getExpression1();
		Expression right = filter.getExpression2();

		left.accept(this, null);
		right.accept(this, null);

		if (this.unrolledExpressions.size() != 2) {
			throw new IllegalStateException("Expected 2 unrolled expressions: "
					+ this.unrolledExpressions);
		}

		left = (Expression) this.unrolledExpressions.get(0);
		right = (Expression) this.unrolledExpressions.get(1);

		return new Expression[] { left, right };
		// this.unrolled = newFilter;
	}

	public Expression[] visitBinarySpatialOp(BinarySpatialOperator filter) {
		Expression left = filter.getExpression1();
		Expression right = filter.getExpression2();

		left.accept(this, null);
		right.accept(this, null);

		if (this.unrolledExpressions.size() != 2) {
			throw new IllegalStateException("Expected 2 unrolled expressions: "
					+ this.unrolledExpressions);
		}

		left = (Expression) this.unrolledExpressions.get(0);
		right = (Expression) this.unrolledExpressions.get(1);

		return new Expression[] { left, right };
		// this.unrolled = newFilter;
	}

	public List/* <Filter> */visitBinaryLogicOp(BinaryLogicOperator filter) {

		List unrolledFilers = new ArrayList();
		try {
			for (Iterator it = filter.getChildren().iterator(); it.hasNext();) {
				UnmappingFilterVisitor2 helper = copy();
				Filter next = (Filter) it.next();
				helper.visit(next);
				Filter unrolled = helper.getUnrolledFilter();
				unrolledFilers.add(unrolled);
			}
		} catch (Exception e) {
			throw (RuntimeException) new RuntimeException().initCause(e);
		}
		return unrolledFilers;
	}

	public Expression[] visitBinaryExpression(BinaryExpression expression) {
		LOGGER.finest(expression.toString());

		Expression left = expression.getExpression1();
		Expression right = expression.getExpression2();

		UnmappingFilterVisitor2 helper = copy();
		helper.visit(left);
		left = (Expression) helper.unrolledExpressions.get(0);

		helper = copy();
		helper.visit(right);
		right = (Expression) helper.unrolledExpressions.get(0);

		return new Expression[] { left, right };
	}

	public Object visit(ExcludeFilter filter, Object arg1) {
		return this.unrolled = filter;
	}

	public Object visit(IncludeFilter filter, Object arg1) {
		return this.unrolled = filter;
	}

	public Object visit(And filter, Object arg1) {
		List list = visitBinaryLogicOp(filter);
		this.unrolled = ff.and(list);
		return this.unrolled;
	}

	public Object visit(Id filter, Object arg1) {
		Set fids = filter.getIdentifiers();

		TypeName target = mappings.getTargetFeature();
		String name = target.getLocalPart();
		Expression fidExpression = (Expression) this.idMappings.get(name);

		Filter unrolled = Filter.EXCLUDE;

		if (fidExpression instanceof Function) {
			Function fe = (Function) fidExpression;
			if ("getID".equalsIgnoreCase(fe.getName())) {
				LOGGER.finest("Fid mapping points to same ID as source");
				this.unrolled = filter;
				return unrolled;
			}
		}

		LOGGER.finest("fid mapping expression is " + fidExpression);
		try {
			if (fidExpression != null) {
				for (Iterator it = fids.iterator(); it.hasNext();) {
					Object fid = it.next();
					Filter comparison = ff.equals(fidExpression, ff
							.literal(fid));
					LOGGER.finest("Adding unmapped fid filter " + comparison);
					unrolled = ff.or(unrolled, comparison);
				}
			} else {
				throw new IllegalStateException(
						"No FID expression found for type " + target);
			}
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "unrolling " + filter, e);
			throw new RuntimeException(e.getMessage());
		}

		LOGGER.finer("unrolled fid filter is " + unrolled);
		this.unrolled = unrolled;
		return unrolled;
	}

	public Object visit(Not filter, Object arg1) {
		filter.getFilter().accept(this, null);
		this.unrolled = ff.not(this.unrolled);
		return unrolled;
	}

	public Object visit(Or filter, Object arg1) {
		List list = visitBinaryLogicOp(filter);
		this.unrolled = ff.or(list);
		return unrolled;
	}

	public Object visit(PropertyIsBetween filter, Object arg1) {

		Expression expression = filter.getExpression();
		Expression lower = filter.getLowerBoundary();
		Expression upper = filter.getUpperBoundary();

		expression.accept(this, null);
		lower.accept(this, null);
		upper.accept(this, null);

		expression = (Expression) this.unrolledExpressions.get(0);
		lower = (Expression) this.unrolledExpressions.get(1);
		upper = (Expression) this.unrolledExpressions.get(2);

		PropertyIsBetween newFilter = ff.between(expression, lower, upper);

		this.unrolled = newFilter;
		return newFilter;
	}

	public Object visit(PropertyIsEqualTo filter, Object arg1) {
		Expression[] expressions = visitBinaryComparisonOperator(filter);
		unrolled = ff.equals(expressions[0], expressions[1]);
		return unrolled;
	}

	public Object visit(PropertyIsNotEqualTo filter, Object arg1) {
		Expression[] expressions = visitBinaryComparisonOperator(filter);
		boolean matchingCase = filter.isMatchingCase();
		unrolled = ff.notEqual(expressions[0], expressions[1], matchingCase);
		return unrolled;
	}

	public Object visit(PropertyIsGreaterThan filter, Object arg1) {
		Expression[] expressions = visitBinaryComparisonOperator(filter);
		unrolled = ff.greater(expressions[0], expressions[1]);
		return unrolled;
	}

	public Object visit(PropertyIsGreaterThanOrEqualTo filter, Object arg1) {
		Expression[] expressions = visitBinaryComparisonOperator(filter);
		unrolled = ff.greaterOrEqual(expressions[0], expressions[1]);
		return unrolled;
	}

	public Object visit(PropertyIsLessThan filter, Object arg1) {
		Expression[] expressions = visitBinaryComparisonOperator(filter);
		unrolled = ff.less(expressions[0], expressions[1]);
		return unrolled;
	}

	public Object visit(PropertyIsLessThanOrEqualTo filter, Object arg1) {
		Expression[] expressions = visitBinaryComparisonOperator(filter);
		unrolled = ff.lessOrEqual(expressions[0], expressions[1]);
		return unrolled;
	}

	public Object visit(PropertyIsLike filter, Object arg1) {
		Expression value = filter.getExpression();
		value.accept(this, null);
		Expression newValue = (Expression) this.unrolledExpressions.get(0);

		PropertyIsLike newFilter = ff.like(newValue, filter.getLiteral());
		this.unrolled = newFilter;
		return newFilter;
	}

	public Object visit(PropertyIsNull filter, Object arg1) {
		Expression nullCheck = filter.getExpression();
		nullCheck.accept(this, null);

		Expression newCheckValue = (Expression) this.unrolledExpressions.get(0);

		this.unrolled = ff.isNull(newCheckValue);
		return unrolled;
	}

	public Object visit(BBOX filter, Object arg1) {
		String propertyName = filter.getPropertyName();
		Expression name = ff.property(propertyName);
		name.accept(this, null);
		name = (Expression) this.unrolledExpressions.get(0);

		unrolled = ff.bbox(name, filter.getMinX(), filter.getMinY(), filter
				.getMaxX(), filter.getMaxY(), filter.getSRS());
		return unrolled;
	}

	public Object visit(Beyond filter, Object arg1) {
		Expression[] exps = visitBinarySpatialOp(filter);

		unrolled = ff.beyond(exps[0], exps[1], filter.getDistance(), filter
				.getDistanceUnits());
		return unrolled;
	}

	public Object visit(Contains filter, Object arg1) {
		Expression[] exps = visitBinarySpatialOp(filter);
		unrolled = ff.contains(exps[0], exps[1]);
		return unrolled;
	}

	public Object visit(Crosses filter, Object arg1) {
		Expression[] exps = visitBinarySpatialOp(filter);
		unrolled = ff.crosses(exps[0], exps[1]);
		return unrolled;
	}

	public Object visit(Disjoint filter, Object arg1) {
		Expression[] exps = visitBinarySpatialOp(filter);
		unrolled = ff.disjoint(exps[0], exps[1]);
		return unrolled;
	}

	public Object visit(DWithin filter, Object arg1) {
		Expression[] exps = visitBinarySpatialOp(filter);
		unrolled = ff.dwithin(exps[0], exps[1], filter.getDistance(), filter
				.getDistanceUnits());
		return unrolled;
	}

	public Object visit(Equals filter, Object arg1) {
		Expression[] exps = visitBinarySpatialOp(filter);
		unrolled = ff.equal(exps[0], exps[1]);
		return unrolled;
	}

	public Object visit(Intersects filter, Object arg1) {
		Expression[] exps = visitBinarySpatialOp(filter);
		unrolled = ff.intersects(exps[0], exps[1]);
		return unrolled;
	}

	public Object visit(Overlaps filter, Object arg1) {
		Expression[] exps = visitBinarySpatialOp(filter);
		unrolled = ff.overlaps(exps[0], exps[1]);
		return unrolled;
	}

	public Object visit(Touches filter, Object arg1) {
		Expression[] exps = visitBinarySpatialOp(filter);
		unrolled = ff.touches(exps[0], exps[1]);
		return unrolled;
	}

	public Object visit(Within filter, Object arg1) {
		Expression[] exps = visitBinarySpatialOp(filter);
		unrolled = ff.within(exps[0], exps[1]);
		return unrolled;
	}

	public Object visitNullFilter(Object arg0) {
		return unrolled = Filter.EXCLUDE;
	}

	public Object visit(NilExpression expr, Object arg1) {
		this.unrolledExpressions.add(expr);
		return unrolledExpressions;
	}

	public Object visit(Add expr, Object arg1) {
		Expression[] expressions = visitBinaryExpression(expr);
		Expression add = ff.add(expressions[0], expressions[1]);
		unrolledExpressions.add(add);
		return unrolledExpressions;
	}

	public Object visit(Divide expr, Object arg1) {
		Expression[] expressions = visitBinaryExpression(expr);
		Expression divide = ff.divide(expressions[0], expressions[1]);
		unrolledExpressions.add(divide);
		return unrolledExpressions;
	}

	public Object visit(Function expr, Object arg1) {
		List expressions = expr.getParameters();
		List arguments = new ArrayList(expressions.size());

		for (Iterator it = expressions.iterator(); it.hasNext();) {
			Expression mappingExpression = (Expression) it.next();
			UnmappingFilterVisitor2 helper = copy();
			helper.visit(mappingExpression);
			List list = helper.unrolledExpressions;
			Expression unrolledExpression = (Expression) list.get(0);
			arguments.add(unrolledExpression);
		}

		Expression[] unmapped = new Expression[arguments.size()];
		unmapped = (Expression[]) arguments.toArray(unmapped);

		Function unmappedFunction = ff.function(expr.getName(), unmapped);

		this.unrolledExpressions.add(unmappedFunction);

		return unmappedFunction;
	}

	public Object visit(Literal expr, Object arg1) {
		this.unrolledExpressions.add(expr);
		return expr;
	}

	public Object visit(Multiply expr, Object arg1) {
		Expression[] expressions = visitBinaryExpression(expr);
		Expression multiply = ff.multiply(expressions[0], expressions[1]);
		unrolledExpressions.add(multiply);
		return unrolledExpressions;
	}

	public Object visit(PropertyName expr, Object arg1) {
		Expression sourceExpression;

		String targetXPath = expr.getPropertyName();
		sourceExpression = (Expression) this.attributeMappings.get(targetXPath);

		if (sourceExpression == null) {
			throw new IllegalArgumentException("Don't know how to map "
					+ targetXPath);
		}

		this.unrolledExpressions.add(sourceExpression);
		return sourceExpression;
	}

	public Object visit(Subtract expr, Object arg1) {
		Expression[] expressions = visitBinaryExpression(expr);
		Expression subtract = ff.subtract(expressions[0], expressions[1]);
		unrolledExpressions.add(subtract);
		return unrolledExpressions;
	}
}
