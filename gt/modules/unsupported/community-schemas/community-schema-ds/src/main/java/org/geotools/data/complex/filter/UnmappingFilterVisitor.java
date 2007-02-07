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
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.data.complex.AttributeMapping;
import org.geotools.data.complex.FeatureTypeMapping;
import org.geotools.feature.XPath;
import org.geotools.filter.AttributeExpression;
import org.geotools.filter.BetweenFilter;
import org.geotools.filter.CompareFilter;
import org.geotools.filter.Expression;
import org.geotools.filter.FidFilter;
import org.geotools.filter.Filter;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.FilterType;
import org.geotools.filter.FilterVisitor;
import org.geotools.filter.FunctionExpression;
import org.geotools.filter.GeometryFilter;
import org.geotools.filter.IllegalFilterException;
import org.geotools.filter.LikeFilter;
import org.geotools.filter.LiteralExpression;
import org.geotools.filter.LogicFilter;
import org.geotools.filter.MathExpression;
import org.geotools.filter.NullFilter;
import org.opengis.feature.schema.AttributeDescriptor;

/**
 * A Filter visitor that traverse a Filter or Expression made against a complex
 * FeatureType, and that uses the attribute and type mapping information given
 * by a {@linkplain org.geotools.data.complex.FeatureTypeMapping} object to
 * produce an equivalent Filter that operates against the original FeatureType.
 * <p>
 * Usage:
 * <pre>
 * <code>
 * Filter filterOnTargetType = ...
 * FeatureTypeMappings schemaMapping = ....
 * 
 * UnMappingFilterVisitor visitor = new UnmappingFilterVisitor(schemaMapping);
 * visitor.visit(filterOnTargetType);
 * 
 * Filter filterOnSourceType = visitor.getUnrolledFilter();
 * </code>
 * </pre>
 * </p>
 * 
 * @author Gabriel Roldan, Axios Engineering
 */
public class UnmappingFilterVisitor implements FilterVisitor {
	private static final Logger LOGGER = Logger
			.getLogger(UnmappingFilterVisitor.class.getPackage().getName());

	private Filter unrolled = Filter.NONE;

	private Map/*<String, Expression>*/ idMappings;

	private Map/*<List<XPath.Step>, Expression>*/ attributeMappings;

	FeatureTypeMapping mappings;

	private static final FilterFactory ff = FilterFactory.createFilterFactory();

	/**
	 * visit(*Expression) holds the unmapped expression here. Package visible
	 * just for unit tests
	 */
	List/*<Expression>*/ unrolledExpressions = new ArrayList/*<Expression>*/(2);

	public UnmappingFilterVisitor(FeatureTypeMapping mappings) {
		this.mappings = mappings;
		this.idMappings = mappings.getIdMappings();

		this.attributeMappings = new HashMap/*<List<XPath.Step>, Expression>*/();
		AttributeDescriptor featureNode = mappings.getTargetFeature();

		for (Iterator itr = mappings.getAttributeMappings().iterator(); itr.hasNext();) {
			AttributeMapping attMapping = (AttributeMapping)itr.next();
			Expression exp = attMapping.getSourceExpression();
			String targetAttribute = attMapping.getTargetXPath();
			List/*<XPath.Step>*/ simplifiedSteps;
			try{
				simplifiedSteps = XPath.steps(featureNode, targetAttribute);
			}catch(RuntimeException e){
				e.printStackTrace();
				throw e;
			}
			this.attributeMappings.put(simplifiedSteps, exp);
		}
	}

	public UnmappingFilterVisitor(FeatureTypeMapping mappings,
			Map/*<List<XPath.Step>, Expression>*/ attributeMappings) {
		this.mappings = mappings;
		this.attributeMappings = attributeMappings;
		this.idMappings = mappings.getIdMappings();
	}

	private UnmappingFilterVisitor copy() {
		return new UnmappingFilterVisitor(mappings, attributeMappings);
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
		if(Filter.ALL.equals(filter) || Filter.NONE.equals(filter)){
			this.unrolled = filter;
			return;
		}
		filter.accept(this);
	}

	public void visit(Expression expression) {
		expression.accept(this);
	}

	public void visit(BetweenFilter filter) {
		Expression left = filter.getLeftValue();
		Expression middle = filter.getMiddleValue();
		Expression right = filter.getRightValue();

		left.accept(this);
		middle.accept(this);
		right.accept(this);

		try {
			BetweenFilter newFilter = ff.createBetweenFilter();
			newFilter.addLeftValue((Expression) this.unrolledExpressions.get(0));
			newFilter.addMiddleValue((Expression) this.unrolledExpressions.get(1));
			newFilter.addRightValue((Expression) this.unrolledExpressions.get(2));
			this.unrolled = newFilter;
		} catch (IllegalFilterException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Returns a CompareFilter of the same type than <code>filter</code>, but
	 * built on the unmapped expressions pointing to the surrogate type
	 * attributes.
	 */
	public void visit(CompareFilter filter) {
		Expression left = filter.getLeftValue();
		Expression right = filter.getRightValue();

		left.accept(this);
		right.accept(this);

		if (this.unrolledExpressions.size() != 2) {
			throw new IllegalStateException("Expected 2 unrolled expressions: "
					+ this.unrolledExpressions);
		}

		left = (Expression) this.unrolledExpressions.get(0);
		right = (Expression) this.unrolledExpressions.get(1);

		CompareFilter newFilter;
		try {
			newFilter = ff.createCompareFilter(filter.getFilterType());
			newFilter.addLeftValue(left);
			newFilter.addRightValue(right);
		} catch (IllegalFilterException e) {
			LOGGER.log(Level.SEVERE, "unrolling " + filter, e);
			throw new RuntimeException(e.getMessage());
		}

		this.unrolled = newFilter;
	}

	public void visit(GeometryFilter filter) {
		Expression left = filter.getLeftGeometry();
		Expression right = filter.getRightGeometry();

		left.accept(this);
		right.accept(this);

		if (this.unrolledExpressions.size() != 2) {
			throw new IllegalStateException("Expected 2 unrolled expressions: "
					+ this.unrolledExpressions);
		}

		left = (Expression) this.unrolledExpressions.get(0);
		right = (Expression) this.unrolledExpressions.get(1);

		GeometryFilter newFilter;
		try {
			newFilter = ff.createGeometryFilter(filter.getFilterType());
			newFilter.addLeftGeometry(left);
			newFilter.addRightGeometry(right);
		} catch (IllegalFilterException e) {
			LOGGER.log(Level.SEVERE, "unrolling " + filter, e);
			throw new RuntimeException(e.getMessage());
		}

		this.unrolled = newFilter;
	}

	public void visit(LikeFilter filter) {
		LikeFilter newFilter = ff.createLikeFilter();
		Expression value = filter.getValue();
		value.accept(this);
		Expression newValue = (Expression) this.unrolledExpressions.get(0);

		newFilter.setPattern(filter.getPattern(), filter.getWildcardMulti(),
				filter.getWildcardSingle(), filter.getEscape());
		try {
			newFilter.setValue(newValue);
		} catch (IllegalFilterException e) {
			throw new RuntimeException(e);
		}
		this.unrolled = newFilter;
	}

	public void visit(LogicFilter filter) {
		short filterType = filter.getFilterType();

		try {
			this.unrolled = FilterFactory.createFilterFactory()
					.createLogicFilter(filterType);

			for (Iterator it = filter.getFilterIterator(); it.hasNext();) {
				UnmappingFilterVisitor helper = copy();
				Filter next = (Filter) it.next();
				helper.visit(next);
				Filter unrolled = helper.getUnrolledFilter();
				((LogicFilter) this.unrolled).addFilter(unrolled);
			}
		} catch (IllegalFilterException e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	public void visit(NullFilter filter) {
		Expression nullCheck = filter.getNullCheckValue();
		nullCheck.accept(this);
		NullFilter newFilter = ff.createNullFilter();
		Expression newCheckValue = (Expression) this.unrolledExpressions.get(0);
		try {
			newFilter.nullCheckValue(newCheckValue);
		} catch (IllegalFilterException e) {
			throw new RuntimeException(e);
		}
		this.unrolled = newFilter;
	}

	public void visit(FidFilter filter) {
		String[] fids = filter.getFids();

		AttributeDescriptor target = mappings.getTargetFeature();
		String name = target.getName().getLocalPart();
		Expression fidExpression = (Expression) this.idMappings.get(name);

		Filter unrolled = Filter.ALL;

		if (fidExpression instanceof FunctionExpression) {
			FunctionExpression fe = (FunctionExpression) fidExpression;
			if ("getID".equalsIgnoreCase(fe.getName())) {
				LOGGER.finest("Fid mapping points to same ID as source");
				this.unrolled = filter;
				return;
			}
		}
		LOGGER.finest("fid mapping expression is " + fidExpression);
		try {
			if (fidExpression != null) {
				for (int i = 0; i < fids.length; i++) {
					CompareFilter comparison = ff
							.createCompareFilter(FilterType.COMPARE_EQUALS);
					comparison.addLeftValue(fidExpression);
					comparison.addRightValue(ff
							.createLiteralExpression(fids[i]));

					LOGGER.finest("Adding unmapped fid filter " + comparison);
					unrolled = unrolled.or(comparison);
				}
			} else {
				throw new IllegalStateException(
						"No FID expression found for type " + target);
			}
		} catch (IllegalFilterException e) {
			LOGGER.log(Level.SEVERE, "unrolling " + filter, e);
			throw new RuntimeException(e.getMessage());
		}

		LOGGER.finer("unrolled fid filter is " + unrolled);
		this.unrolled = unrolled;
	}

	public void visit(AttributeExpression expression) {
		Expression sourceExpression;

		String targetXPath = expression.getAttributePath();
		AttributeDescriptor featureNode = mappings.getTargetFeature();
		List/*<XPath.Step>*/ steps = XPath.steps(featureNode, targetXPath);

		sourceExpression = (Expression) this.attributeMappings.get(steps);

		if (sourceExpression == null) {
			throw new IllegalArgumentException("Don't know how to map "
					+ targetXPath);
		}

		this.unrolledExpressions.add(sourceExpression);
	}

	public void visit(LiteralExpression expression) {
		this.unrolledExpressions.add(expression);
	}

	public void visit(MathExpression expression) {
		LOGGER.finest(expression.toString());
		Expression left = expression.getLeftValue();
		Expression right = expression.getRightValue();

		UnmappingFilterVisitor helper = copy();
		helper.visit(left);
		left = (Expression) helper.unrolledExpressions.get(0);

		helper = copy();
		helper.visit(right);
		right = (Expression) helper.unrolledExpressions.get(0);

		try {
			MathExpression unmapped = ff.createMathExpression(expression
					.getType());
			unmapped.addLeftValue(left);
			unmapped.addRightValue(right);
			this.unrolledExpressions.add(unmapped);
		} catch (IllegalFilterException e) {
			throw new RuntimeException(e);
		}
	}

	public void visit(FunctionExpression expression) {
		Expression[] expressions = expression.getArgs();
		Expression[] arguments = new Expression[expressions.length];

		FunctionExpression unmappedExpression = ff
				.createFunctionExpression(expression.getName());

		for (int i = 0; i < expressions.length; i++) {
			Expression mappingExpression = expressions[i];
			UnmappingFilterVisitor helper = copy();
			helper.visit(mappingExpression);
			List/*<Expression>*/ unrolledExpressions = helper.unrolledExpressions;
			arguments[i] = (Expression) unrolledExpressions.get(0);
		}
		unmappedExpression.setArgs(arguments);
		this.unrolledExpressions.add(unmappedExpression);
	}
}
