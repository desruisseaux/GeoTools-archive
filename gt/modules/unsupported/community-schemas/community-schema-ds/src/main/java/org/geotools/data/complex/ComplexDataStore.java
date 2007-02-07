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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.geotools.data.AbstractDataStore;
import org.geotools.data.DataSourceException;
import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureSource;
import org.geotools.data.Query;
import org.geotools.data.SchemaNotFoundException;
import org.geotools.data.complex.filter.UnmappingFilterVisitor;
import org.geotools.feature.FeatureType;
import org.geotools.feature.XPath;
import org.geotools.filter.Expression;
import org.geotools.filter.Filter;
import org.geotools.filter.FilterAttributeExtractor;
import org.opengis.feature.schema.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;

import com.vividsolutions.jts.geom.Envelope;

public class ComplexDataStore extends AbstractDataStore {

	private static final boolean IS_WRITABLE = false;

	private Map/* <String, FeatureTypeMapping> */mappings = Collections.EMPTY_MAP;

	/**
	 * 
	 * @param mappings
	 *            a Set containing a {@linkplain FeatureTypeMapping} for each
	 *            FeatureType this DataStore is going to hold.
	 */
	public ComplexDataStore(Set/*<FeatureTypeMapping>*/ mappings) {
		super(IS_WRITABLE);
		FeatureTypeMapping mapping;
		this.mappings = new HashMap();
		for (Iterator it = mappings.iterator(); it.hasNext();) {
			mapping = (FeatureTypeMapping) it.next();
			AttributeDescriptor mappedElement = mapping.getTargetFeature();
			org.opengis.feature.type.FeatureType mappedType;
			mappedType = (org.opengis.feature.type.FeatureType)mappedElement.getType();
			String typeName = mappedType.getName().getLocalPart();
			this.mappings.put(typeName, mapping);
		}
	}

	/**
	 * Returns the set of target type names this DataStore holds, where the term
	 * 'target type name' refers to the name of one of the types this datastore
	 * produces by mapping another ones through the definitions stored in its
	 * {@linkplain FeatureTypeMapping}s
	 */
	public String[] getTypeNames() throws IOException {
		String[] typeNames = new String[mappings.size()];
		this.mappings.keySet().toArray(typeNames);
		return typeNames;
	}

	/**
	 * Finds the target FeatureType named <code>typeName</code> in this
	 * ComplexDatastore's internal list of FeatureType mappings and returns it.
	 */
	public FeatureType getSchema(String typeName) throws IOException {
		FeatureTypeMapping mapping = getMapping(typeName);
		return (FeatureType) mapping.getTargetFeature().getType();
	}

	/**
	 * Returns the mapping suite for the given target type name.
	 * 
	 * @param typeName
	 * @return
	 * @throws IOException
	 */
	private FeatureTypeMapping getMapping(String typeName) throws IOException {
		FeatureTypeMapping mapping = (FeatureTypeMapping) this.mappings
				.get(typeName);
		if (mapping == null) {
			StringBuffer availables = new StringBuffer("[");
			for(Iterator it = mappings.keySet().iterator(); it.hasNext();){
				availables.append(it.next());
				availables.append(it.hasNext()? ", " : "");
			}
			availables.append("]");
			throw new DataSourceException(typeName + " not found " + availables);
		}
		return mapping;
	}

	/**
	 * GR: this method is called from inside getFeatureReader(Query ,Transaction )
	 * to allow subclasses return an optimized FeatureReader wich supports the
	 * filter and attributes truncation specified in <code>query</code>
	 * <p>
	 * A subclass that supports the creation of such an optimized FeatureReader
	 * shold override this method. Otherwise, it just returns
	 * <code>getFeatureReader(typeName)</code>
	 * <p>
	 */
	protected FeatureReader getFeatureReader(String typeName, Query query)
			throws IOException {
		FeatureTypeMapping mapping = getMapping(typeName);
		MappingFeatureReader reader = new MappingFeatureReader(mapping, query);
		return reader;
	}

	/**
	 * 
	 * @param typeName
	 */
	protected FeatureReader getFeatureReader(String typeName)
			throws IOException {

		throw new UnsupportedOperationException(
				"Not needed since we support getFeatureReader(String, Query)");
	}

	/**
	 * Computes the bounds of the features for the specified feature type that
	 * satisfy the query provided that there is a fast way to get that result.
	 * <p>
	 * Will return null if there is not fast way to compute the bounds. Since
	 * it's based on some kind of header/cached information, it's not guaranteed
	 * to be real bound of the features
	 * </p>
	 * 
	 * @param query
	 * @return the bounds, or null if too expensive
	 * @throws SchemaNotFoundException
	 * @throws IOException
	 */
	protected Envelope getBounds(Query query) throws IOException {
		String typeName = query.getTypeName();
		FeatureTypeMapping mapping = getMapping(typeName);
		Query unmappedQuery = unrollQuery(query, mapping);
		FeatureSource mappedSource = mapping.getSource();

		Envelope bounds = mappedSource.getBounds(unmappedQuery);
		return bounds;
	}

	/**
	 * Gets the number of the features that would be returned by this query for
	 * the specified feature type.
	 * <p>
	 * If getBounds(Query) returns <code>-1</code> due to expense consider
	 * using <code>getFeatures(Query).getCount()</code> as a an alternative.
	 * </p>
	 * 
	 * @param query
	 *            Contains the Filter and MaxFeatures to find the bounds for.
	 * @return The number of Features provided by the Query or <code>-1</code>
	 *         if count is too expensive to calculate or any errors or occur.
	 * @throws IOException
	 * 
	 * @throws IOException
	 *             if there are errors getting the count
	 */
	protected int getCount(Query query) throws IOException {
		String typeName = query.getTypeName();
		
		FeatureTypeMapping mapping = getMapping(typeName);
		
		Query unmappedQuery = unrollQuery(query, mapping);
		
		FeatureSource mappedSource = mapping.getSource();
		
		//@todo: limit results if its a grouping mapping
		((DefaultQuery)unmappedQuery).setMaxFeatures(query.getMaxFeatures());
		
		int count = mappedSource.getCount(unmappedQuery);
		return count;
	}
	
	/**
	 * Returns <code>Filter.NONE</code>, as the whole filter is unrolled and
	 * passed back to the underlying DataStore to be treated.
     * 
     * @return <code>Filter.NONE</code>
     */
    protected Filter getUnsupportedFilter(String typeName, Filter filter){
      return Filter.NONE;
    }	

	/**
	 * Creates a <code>org.geotools.data.Query</code> that operates over the
	 * surrogate DataStore, by unrolling the
	 * <code>org.geotools.filter.Filter</code> contained in the passed
	 * <code>query</code>, and replacing the list of required attributes by
	 * the ones of the mapped FeatureType.
	 * 
	 * @param query
	 * @param mapping
	 * @return
	 */
	public static Query unrollQuery(Query query, FeatureTypeMapping mapping) {
		Query unrolledQuery = Query.ALL;
		FeatureSource source = mapping.getSource();

		if (!Query.ALL.equals(query)) {
			Filter complexFilter = query.getFilter();
			FeatureType sourceType = source.getSchema();
			Filter unrolledFilter = unrollFilter(complexFilter, mapping);
			List propNames = getSurrogatePropertyNames(
					query.getPropertyNames(), mapping);
			
			DefaultQuery newQuery = new DefaultQuery();
			
			String name = sourceType.getName().getLocalPart();
			newQuery.setTypeName(name);
			newQuery.setFilter(unrolledFilter);
			newQuery.setPropertyNames(propNames);
			newQuery.setCoordinateSystem(query.getCoordinateSystem());
			newQuery.setCoordinateSystemReproject(query.getCoordinateSystemReproject());
			newQuery.setHandle(query.getHandle());
			newQuery.setMaxFeatures(query.getMaxFeatures());

			unrolledQuery = newQuery;
		}
		return unrolledQuery;
	}

	/**
	 * 
	 * @param mappingProperties
	 * @param mapping
	 * @return <code>null</code> if all surrogate attributes shall be queried,
	 *         else the list of needed surrogate attributes to satisfy the
	 *         mapping of prorperties in <code>mappingProperties</code>
	 */
	public static List getSurrogatePropertyNames(String[] mappingProperties,
			FeatureTypeMapping mapping) {
		List propNames = null;

		final org.opengis.feature.type.FeatureType mappedType = mapping.getSource().getSchema();
		final AttributeDescriptor target = mapping.getTargetFeature();
		final org.opengis.feature.type.FeatureType targetType = (org.opengis.feature.type.FeatureType)target.getType();

		if (mappingProperties != null && mappingProperties.length > 0) {
			Set requestedSurrogateProperties = new HashSet();
			FilterAttributeExtractor extractor = new FilterAttributeExtractor();

			// add all surrogate atts needed to recreate target schema ids
			Set/*<Expression>*/ idExpressions = new HashSet(mapping.getIdMappings().values());
			for (Iterator itr = idExpressions.iterator(); itr.hasNext();) {
				Expression e = (Expression) itr.next();
				extractor.visit(e);
				Set exprAtts = extractor.getAttributeNameSet();
				LOGGER.fine("adding atts needed for ids: " + exprAtts);
				requestedSurrogateProperties.addAll(exprAtts);
				extractor.clear();
			}

			// add all surrogate attributes involved in mapping of the requested
			// target schema attributes
			List attMappings = mapping.getAttributeMappings();
			List/*<String>*/ requestedProperties = Arrays.asList(mappingProperties);

			for (Iterator itr = requestedProperties.iterator(); itr.hasNext();) {
				String requestedPropertyXPath = (String) itr.next();
				List/*<XPath.Step>*/ requestedPropertySteps = XPath.steps(targetType, requestedPropertyXPath);

				for (Iterator aitr = attMappings.iterator(); aitr.hasNext();) {
					final AttributeMapping entry = (AttributeMapping) aitr.next();
					final String mappedPropertyXPath = entry.getTargetXPath();
					final Expression sourceExpression = entry.getSourceExpression();
					
					List/*<XPath.Step>*/ targetSteps = XPath.steps(targetType, mappedPropertyXPath);

					//i.e.: requested "measurement", found mapping of "measurement/result".
					//"result" must be included to create "measurement" 
					if (targetSteps.containsAll(requestedPropertySteps)) {
						extractor.visit(sourceExpression);
						Set/*<String>*/ exprAtts = extractor.getAttributeNameSet();
						extractor.clear();

						for(Iterator eitr = exprAtts.iterator(); eitr.hasNext();){
							String mappedAtt = (String) eitr.next();
							AttributeDescriptor mappedAttribute = (AttributeDescriptor)XPath.get(mappedType, mappedAtt);
							
							if(mappedAttribute != null){
								requestedSurrogateProperties.add(mappedAtt);
							}else{
								LOGGER.info("mapped type does not contains property " + mappedAtt);
							}
						}
						LOGGER.fine("adding atts needed for : " + exprAtts);
					}
				}
			}
			propNames = new ArrayList(requestedSurrogateProperties);
		}
		return propNames;
	}

	/**
	 * Takes a filter that operates against a {@linkplain FeatureTypeMapping}'s
	 * target FeatureType, and unrolls it creating a new Filter that operates
	 * against the mapping's source FeatureType.
	 * 
	 * @param complexFilter
	 * @return TODO: implement filter unrolling
	 */
	public static Filter unrollFilter(Filter complexFilter,
			FeatureTypeMapping mapping) {
		UnmappingFilterVisitor visitor = new UnmappingFilterVisitor(mapping);
		complexFilter.accept(visitor);
		//visitor.visit(complexFilter);
		Filter unrolledFilter = visitor.getUnrolledFilter();
		return unrolledFilter;
	}
}
