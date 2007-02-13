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
import org.geotools.data.complex.filter.XPath;
import org.geotools.feature.iso.AttributeBuilder;
import org.opengis.feature.ComplexAttribute;
import org.opengis.feature.Feature;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Name;
import org.opengis.filter.expression.Expression;

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
class DefaultMappingFeatureIterator extends AbstractMappingFeatureIterator {

    private XPath xpathAttributeBuilder;

    public DefaultMappingFeatureIterator(ComplexDataStore store,
            FeatureTypeMapping mapping, Query query) throws IOException {
        super(store, mapping, query);
        xpathAttributeBuilder = new XPath();
        xpathAttributeBuilder.setFeatureFactory(super.attf);
    }

    public Object/* Feature */next() {
        Feature currentFeature = computeNext();
        return currentFeature;
    }

    public boolean hasNext() {
        return sourceFeatures.hasNext();
    }

    protected Query getUnrolledQuery(Query query) {
        return store.unrollQuery(query, mapping);
    }

    private Feature computeNext() {
        ComplexAttribute sourceInstance = (ComplexAttribute) this.sourceFeatures
                .next();
        final AttributeDescriptor targetNode = mapping.getTargetFeature();
        final Name targetNodeName = targetNode.getName();
        final List mappings = mapping.getAttributeMappings();
        final FeatureType targetType = (FeatureType) targetNode.getType();

        String id = super.extractIdForFeature(sourceInstance);

        AttributeBuilder builder = new AttributeBuilder(attf);
        builder.setType(targetType);

        Feature mapped = (Feature) builder.build(id);

        for (Iterator itr = mappings.iterator(); itr.hasNext();) {
            AttributeMapping attMapping = (AttributeMapping) itr.next();
            Expression sourceExp = attMapping.getSourceExpression();
            String targetXpathProperty = attMapping.getTargetXPath();
            // TODO: optimize here
            targetXpathProperty = XPath.Step.toString(XPath.steps(
                    targetNodeName, targetXpathProperty));
            AttributeType targetNodeType = attMapping.getTargetNodeInstance();

            Object value = sourceExp.evaluate(sourceInstance);
            id = extractIdForAttribute(attMapping, sourceInstance);

            xpathAttributeBuilder.set(mapped, targetXpathProperty, value, id,
                    targetNodeType);
        }

        return mapped;
    }

}
