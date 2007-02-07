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

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.geotools.data.Query;
import org.geotools.feature.XPath;
import org.geotools.filter.Expression;
import org.opengis.feature.ComplexAttribute;
import org.opengis.feature.Feature;
import org.opengis.feature.schema.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;

/**
 * A Feature iterator that operates over the FeatureSource of a
 * {@linkplain org.geotools.data.complex.FeatureTypeMapping} and produces
 * Features of the output schema by applying the mapping rules to the Features
 * of the source schema.
 * <p>
 * This iterator acts like a one-to-one mapping, producing a Feature of the
 * target type for each feature of the source type. For a one-to-many iterator
 * see {@linkplain org.geotools.data.complex.GroupingFeatureIterator}
 * </p>
 * 
 * @author Gabriel Roldan, Axios Engineering
 */
class DefaultMappingFeatureIterator extends
		AbstractMappingFeatureIterator {

	public DefaultMappingFeatureIterator(FeatureTypeMapping mapping, Query query)
			throws IOException {
		super(mapping, query);
	}

	public Object/*Feature*/ next() {
		Feature currentFeature = computeNext();
		return currentFeature;
	}

	public boolean hasNext() {
		return sourceFeatures.hasNext();
	}

	protected Query getUnrolledQuery(Query query){
		return ComplexDataStore.unrollQuery(query, mapping);
	}

	private Feature computeNext() {
		ComplexAttribute sourceInstance = (ComplexAttribute) this.sourceFeatures.next();
		final AttributeDescriptor targetNode = mapping.getTargetFeature();
		final List mappings = mapping.getAttributeMappings();

		String name = targetNode.getName().getLocalPart();
		String id = extractIdForAttribute(name, sourceInstance);

		Feature mapped = attf.createFeature(targetNode, id);

		for (Iterator itr = mappings.iterator(); itr.hasNext();) {
			AttributeMapping attMapping = (AttributeMapping)itr.next();
			Expression sourceExp = attMapping.getSourceExpression();
			String targetXpathProperty = attMapping.getTargetXPath();
			AttributeType targetNodeType = attMapping.getTargetNodeInstance();
			
			Object value = sourceExp.getValue(sourceInstance);
			id = extractIdForAttribute(targetXpathProperty, sourceInstance);
			if(targetNodeType == null){
				XPath.set(mapped, targetXpathProperty, value, id);
			}else{
				XPath.set(mapped, targetXpathProperty, value, id, targetNodeType);
			}
		}

		return mapped;
	}

}
