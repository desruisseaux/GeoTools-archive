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
import java.util.Map;

import org.geotools.data.DataUtilities;
import org.geotools.data.FeatureSource;
import org.geotools.data.Query;
import org.geotools.feature.impl.AttributeFactoryImpl;
import org.geotools.filter.Expression;
import org.opengis.feature.AttributeFactory;
import org.opengis.feature.ComplexAttribute;
import org.opengis.feature.FeatureCollection;
import org.opengis.feature.type.FeatureType;

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
 */
abstract class AbstractMappingFeatureIterator implements Iterator/*<Feature>*/ {

	/**
	 * The mappings for the source and target schemas
	 */
	protected FeatureTypeMapping mapping;

	/**
	 * Factory used to create the target feature and attributes
	 */
	protected AttributeFactory attf;

	protected FeatureCollection features;
	protected Iterator sourceFeatures;

	/**
	 * 
	 * @param mapping
	 *            place holder for the target type, the surrogate FeatureSource
	 *            and the mappings between them.
	 * @param query
	 *            the query over the target feature type, that is to be unpacked
	 *            to its equivalent over the surrogate feature type.
	 * @throws IOException
	 */
	public AbstractMappingFeatureIterator(FeatureTypeMapping mapping,
			Query query) throws IOException {
		this.mapping = mapping;
		this.attf = new AttributeFactoryImpl();

		Query unrolledQuery = getUnrolledQuery(query);
		FeatureSource mappedSource = mapping.getSource();
		
		features = mappedSource.getFeatures(unrolledQuery);
		this.sourceFeatures = features.features();		
	}
	
	/**
	 * Subclasses must override to provide a query appropiate to
	 * its underlying feature source.
	 * 
	 * @param query the original query against the output schema
	 * @return a query appropiate to be executed over the underlying
	 * feature source.
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
		features.close(this.sourceFeatures);
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
	protected String extractIdForAttribute(String attributeXPath,
			ComplexAttribute sourceInstance) {
		Map/*<String, Expression>*/ idExpressions = mapping.getIdMappings();
		Expression idExpression = (Expression) idExpressions.get(attributeXPath);
		if (idExpression == null) {
			idExpression = FeatureTypeMapping.NULL_EXPRESSION;
		}
		Object value = idExpression.getValue(sourceInstance);
		return value == null ? null : value.toString();
	}

	
}
