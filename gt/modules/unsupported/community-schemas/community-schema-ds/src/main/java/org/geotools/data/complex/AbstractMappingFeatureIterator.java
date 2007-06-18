/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2005-2006, GeoTools Project Managment Committee (PMC)
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

package org.geotools.data.complex;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.namespace.QName;

import org.geotools.data.Query;
import org.geotools.data.Source;
import org.geotools.data.complex.filter.XPath.Step;
import org.geotools.data.complex.filter.XPath.StepList;
import org.geotools.data.feature.FeatureSource2;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.iso.AttributeFactoryImpl;
import org.geotools.feature.iso.Types;
import org.opengis.feature.Attribute;
import org.opengis.feature.ComplexAttribute;
import org.opengis.feature.FeatureCollection;
import org.opengis.feature.FeatureFactory;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.expression.Expression;

/**
 * Base class for mapping iterator strategies.
 * <p>
 * This class provides the common behavior for iterating over a mapped
 * FeatureSource and returning instances of the target FeatureType, by unpacking
 * the incoming <code>org.geotools.data.Query</code> and creating its
 * equivalent over the mapped FeatureType.
 * </p>
 * <p>
 * This way, subclasses should only worry on implementing <code>next()</code>
 * and <code>hasNext()</code> in a way according to their fetching stratagy,
 * while this superclass provides them with a FeatureIterator already made by
 * executing the unpacked Query over the source FeatureSource.
 * </p>
 * 
 * @author Gabriel Roldan, Axios Engineering
 * @version $Id$
 * @source $URL$
 * @since 2.4
 */
abstract class AbstractMappingFeatureIterator implements Iterator/* <Feature> */{

    private static final Logger LOGGER = Logger
            .getLogger(AbstractMappingFeatureIterator.class.getPackage()
                    .getName());

    /**
     * The mappings for the source and target schemas
     */
    protected FeatureTypeMapping mapping;

    /**
     * Expression to evaluate the feature id
     */
    protected Expression featureFidMapping;

    /**
     * Factory used to create the target feature and attributes
     */
    protected FeatureFactory attf;

    protected FeatureCollection features;

    protected Iterator sourceFeatures;

    protected ComplexDataStore store;

    protected Source featureSource;

    /**
     * 
     * @param store
     * @param mapping
     *            place holder for the target type, the surrogate FeatureSource
     *            and the mappings between them.
     * @param query
     *            the query over the target feature type, that is to be unpacked
     *            to its equivalent over the surrogate feature type.
     * @throws IOException
     */
    public AbstractMappingFeatureIterator(ComplexDataStore store,
            FeatureTypeMapping mapping, Query query) throws IOException {
        this.store = store;
        this.attf = new AttributeFactoryImpl();
        Name name = mapping.getTargetFeature().getName();
        this.featureSource = store.access(name);

        List attributeMappings = mapping.getAttributeMappings();

        for (Iterator it = attributeMappings.iterator(); it.hasNext();) {
            AttributeMapping attMapping = (AttributeMapping) it.next();
            StepList targetXPath = attMapping.getTargetXPath();
            if(targetXPath.size() > 1){
                continue;
            }
            Step step = (Step) targetXPath.get(0);
            QName stepName = step.getName();
            if (Types.equals(name, stepName)) {
                featureFidMapping = attMapping.getIdentifierExpression();
                break;
            }
        }

        this.mapping = mapping;

        if (featureFidMapping == null
                || Expression.NIL.equals(featureFidMapping)) {
            FilterFactory ff = CommonFactoryFinder.getFilterFactory(null);
            featureFidMapping = ff.property("@id");
        }

        Query unrolledQuery = getUnrolledQuery(query);
        Filter filter = unrolledQuery.getFilter();

        FeatureSource2 mappedSource = mapping.getSource();

        features = (FeatureCollection) mappedSource.content(filter);

        this.sourceFeatures = features.iterator();
    }

    /**
     * Subclasses must override to provide a query appropiate to its underlying
     * feature source.
     * 
     * @param query
     *            the original query against the output schema
     * @return a query appropiate to be executed over the underlying feature
     *         source.
     */
    protected abstract Query getUnrolledQuery(Query query);

    /**
     * Shall not be called, just throws an UnsupportedOperationException
     */
    public void remove() {
        throw new UnsupportedOperationException();
    }

    /**
     * Closes the underlying FeatureIterator
     */
    public void close() {
        //LOGGER.info("Closing surrogate iterator");
        features.close(sourceFeatures);
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
    protected String extractIdForAttribute(AttributeMapping attMapping,
            ComplexAttribute sourceInstance) {

        Expression idExpression = attMapping.getIdentifierExpression();
        if (idExpression == null) {
            idExpression = Expression.NIL;
        }
        String value = (String) idExpression.evaluate(sourceInstance,
                String.class);
        return value;
    }

    protected String extractIdForFeature(ComplexAttribute sourceInstance) {
        String fid = (String) featureFidMapping.evaluate(sourceInstance,
                String.class);
        return fid;
    }

    protected Object getValue(Expression expression, ComplexAttribute sourceFeature) {
        Object value;
        value = expression.evaluate(sourceFeature);
        if(value instanceof Attribute){
            value = ((Attribute)value).get();
        }
        return value;
    }
}
