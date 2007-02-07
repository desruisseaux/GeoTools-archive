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

package org.geotools.data.complex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.geotools.data.FeatureSource;
import org.geotools.filter.Expression;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.FilterVisitor;
import org.opengis.feature.Attribute;
import org.opengis.feature.ComplexAttribute;
import org.opengis.feature.schema.AttributeDescriptor;
import org.opengis.feature.type.FeatureType;

/**
 * 
 * @author Gabriel Roldan, Axios Engineering
 */
public class FeatureTypeMapping {
	/**
	 * 
	 */
	private FeatureSource source;
	
	/**
	 * Encapsulates the name and type of target
	 * Features
	 */
	private AttributeDescriptor target;

	private List/*<String>*/ groupByAttNames;

	/**
	 * An expression whose resulting value will be used as the ID for the
	 * returned object by {@linkplain #map(ComplexAttribute)}. The key of this
	 * map is an XPath expression that adresses the attribute of the target type
	 * to which to apply the id. The values of this Map are Expressions whose
	 * evaluated values constitutes the actual ID literals to assign to the
	 * addressed attribute.
	 */
	private Map/*<String, Expression>*/ fidExpressions;

	/**
	 * Map of <source expression>/<target property>, where target property is
	 * an XPath expression addressing the mapped property of the target schema.
	 */
	List/*<AttributeMapping>*/ attributeMappings;

	/**
	 * No parameters constructor for use by the digester configuration engine as
	 * a JavaBean
	 */
	public FeatureTypeMapping() {
		this.source = null;
		this.target = null;
		this.attributeMappings = new LinkedList();
		this.fidExpressions = new HashMap/*<String, Expression>*/();
		this.groupByAttNames = Collections.EMPTY_LIST;
	}

	public FeatureTypeMapping(FeatureSource source, AttributeDescriptor target,
			List/*<AttributeMapping>*/ mappings,
			Map/*<String, Expression>*/ idExpressions) {
		this.source = source;
		this.target = target;
		this.attributeMappings = new LinkedList/*<AttributeMapping>*/(mappings);

		this.fidExpressions = new HashMap/*<String, Expression>*/();

		if (idExpressions != null) {
			this.fidExpressions.putAll(idExpressions);
		}

		if (!this.fidExpressions.containsKey(target.getName().getLocalPart())) {
			setDefaultFidMapping();
		}
		this.groupByAttNames = Collections.EMPTY_LIST;
	}

	public void addAttributeMapping(Expression sourceExpression,
			String targetXpath) {
		addAttributeMapping(sourceExpression, targetXpath, null);
	}
	/**
	 * 
	 * @param sourceExpression
	 * @param targetXpath
	 * @param targetNodeReference if provided, instances of <code>targetXpath</code>
	 * will be created as the <code>AtrributeDescriptor</code> referenced by this name.
	 */
	public void addAttributeMapping(Expression sourceExpression,
			String targetXpath,
			AttributeDescriptor targetNodeReference) {
		
		if (sourceExpression == null || targetXpath == null) {
			throw new NullPointerException("expression: " + sourceExpression
					+ ", target attribtue: " + targetXpath);
		}
		
		AttributeMapping attMapping = new AttributeMapping(sourceExpression, targetXpath);
		this.attributeMappings.add(attMapping);
	}

	public List/*<AttributeMapping>*/ getAttributeMappings() {
		return new ArrayList(attributeMappings);
	}

	public void addIdMapping(String targetAttribute, Expression sourceExpression) {
		if (sourceExpression == null || targetAttribute == null) {
			throw new NullPointerException("expression: " + sourceExpression
					+ ", target attribtue: " + targetAttribute);
		}
		this.fidExpressions.put(targetAttribute, sourceExpression);
	}

	public Map/*<String, Expression>*/ getIdMappings() {
		return this.fidExpressions;
	}

	/**
	 * Has to be called after {@link #setTargetType(FeatureType)}
	 * @param elementName
	 * @param featureTypeName
	 */
	public void setTargetFeature(AttributeDescriptor featureDescriptor){
		this.target = featureDescriptor;
	}
	
	
	/*
	public void setTargetType(FeatureType targetSchema) {
		if (targetSchema == null) {
			throw new NullPointerException("targetSchema");
		}
		this.target = targetSchema;
		if (!this.fidExpressions.containsKey(name(targetSchema))) {
			LOGGER.fine("No id mapping defined, assigning default");
			setDefaultFidMapping();
		}
	}
	*/

	public void setSource(FeatureSource source) {
		this.source = source;
	}

	private void setDefaultFidMapping() {
		FilterFactory ff = FilterFactory.createFilterFactory();
		Expression mainFidExpression = ff.createFunctionExpression("getID");
		if (mainFidExpression == null) {
			throw new IllegalStateException(
					"The getID function expression was not found. Check the FunctionExpression SPI state");
		}
		this.fidExpressions.put(target.getName().getLocalPart(), mainFidExpression);
	}


	public AttributeDescriptor getTargetFeature() {
		return this.target;
	}

	public FeatureSource getSource() {
		return this.source;
	}

	public List/*<String>*/ getGroupByAttNames() {
		return groupByAttNames;
	}

	public void setGroupByAttNames(List/*<String>*/ groupByAttNames) {
		this.groupByAttNames = groupByAttNames == null ? Collections.EMPTY_LIST
				: Collections.unmodifiableList(groupByAttNames);
	}

	/**
	 * Based on the set of xpath expression/id extracting expression, finds the
	 * ID for the attribute <code>attributeXPath</code> from the source
	 * complex attribute.
	 * 
	 * @param attributeXPath
	 *            the location path of the attribute to be created, for which to
	 *            obtain the id by evaluating the corresponding
	 *            <code>org.geotools.filter.Expression</code> from
	 *            <code>sourceInstance</code>.
	 * @param sourceInstance
	 *            a complex attribute which is the source of the mapping.
	 * @return the ID to be applied to a new attribute instance addressed by
	 *         <code>attributeXPath</code>, or <code>null</code> if there
	 *         is no an id mapping for that attribute.
	 */
	private String extractIdForAttribute(String attributeXPath,
			ComplexAttribute sourceInstance) {
		Expression idExpression = 
			(Expression) this.fidExpressions.get(attributeXPath);
		if (idExpression == null) {
			idExpression = NULL_EXPRESSION;
		}
		Object value = idExpression.getValue(sourceInstance);
		return value == null ? null : value.toString();
	}

	/**
	 * An expression that allways evaluates to <code>null</code>.
	 * <p>
	 * Used as <i>null object</i> for the cases where a target attribute is not
	 * mapped to any source Expression in particular. This situation often
	 * occurs when a complex attribute needs to be created with an id prior to
	 * the creation of its childs attributes.
	 * </p>
	 */
	public static final Expression NULL_EXPRESSION = new Expression() {
		public short getType() {
			return -1;
		}

		public Object getValue(Attribute feature) {
			return null;
		}

		public void accept(FilterVisitor visitor) {
			// no-op
		}
	};
}
