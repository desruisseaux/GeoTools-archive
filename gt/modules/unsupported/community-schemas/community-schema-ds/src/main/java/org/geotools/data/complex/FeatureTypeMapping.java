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
import java.util.LinkedList;
import java.util.List;

import org.geotools.data.feature.FeatureSource2;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.FeatureType;
import org.opengis.filter.expression.Expression;

/**
 * 
 * @author Gabriel Roldan, Axios Engineering
 */
public class FeatureTypeMapping {
    /**
     * 
     */
    private FeatureSource2 source;

    /**
     * Encapsulates the name and type of target Features
     */
    private AttributeDescriptor target;

    private List/* <String> */groupByAttNames;

    /**
     * Map of <source expression>/<target property>, where target property is
     * an XPath expression addressing the mapped property of the target schema.
     */
    List/* <AttributeMapping> */attributeMappings;

    /**
     * No parameters constructor for use by the digester configuration engine as
     * a JavaBean
     */
    public FeatureTypeMapping() {
        this.source = null;
        this.target = null;
        this.attributeMappings = new LinkedList();
        this.groupByAttNames = Collections.EMPTY_LIST;
    }

    public FeatureTypeMapping(FeatureSource2 source,
            AttributeDescriptor target, List/* <AttributeMapping> */mappings) {
        this.source = source;
        this.target = target;
        this.attributeMappings = new LinkedList/* <AttributeMapping> */(
                mappings);

        this.groupByAttNames = Collections.EMPTY_LIST;
    }

    public void addAttributeMapping(Expression idExpression,
            Expression sourceExpression, String targetXpath) {
        addAttributeMapping(idExpression, sourceExpression, targetXpath, null);
    }

    /**
     * 
     * @param sourceExpression
     * @param targetXpath
     * @param targetNodeReference
     *            if provided, instances of <code>targetXpath</code> will be
     *            created as the <code>AtrributeDescriptor</code> referenced
     *            by this name.
     */
    public void addAttributeMapping(Expression idExpression,
            Expression sourceExpression, String targetXpath,
            AttributeDescriptor targetNodeReference) {

        if (sourceExpression == null || targetXpath == null) {
            throw new NullPointerException("expression: " + sourceExpression
                    + ", target attribtue: " + targetXpath);
        }

        AttributeMapping attMapping = new AttributeMapping(idExpression,
                sourceExpression, targetXpath);
        this.attributeMappings.add(attMapping);
    }

    public List/* <AttributeMapping> */getAttributeMappings() {
        return new ArrayList(attributeMappings);
    }

    /**
     * Has to be called after {@link #setTargetType(FeatureType)}
     * 
     * @param elementName
     * @param featureTypeName
     */
    public void setTargetFeature(AttributeDescriptor featureDescriptor) {
        this.target = featureDescriptor;
    }

    public void setSource(FeatureSource2 source) {
        this.source = source;
    }

    /*
     * private void setDefaultFidMapping() { FilterFactory ff =
     * CommonFactoryFinder.getFilterFactory(null); // TODO: this could be
     * replaced by property("@id"); Expression mainFidExpression =
     * ff.function("getID", new Expression[0]); if (mainFidExpression == null) {
     * throw new IllegalStateException( "The getID function expression was not
     * found. Check the FunctionExpression SPI state"); }
     * this.fidExpressions.put(target.getName().getLocalPart(),
     * mainFidExpression); }
     */

    public AttributeDescriptor getTargetFeature() {
        return this.target;
    }

    public FeatureSource2 getSource() {
        return this.source;
    }

    public List/* <String> */getGroupByAttNames() {
        return groupByAttNames;
    }

    public void setGroupByAttNames(List/* <String> */groupByAttNames) {
        this.groupByAttNames = groupByAttNames == null ? Collections.EMPTY_LIST
                : Collections.unmodifiableList(groupByAttNames);
    }
}
