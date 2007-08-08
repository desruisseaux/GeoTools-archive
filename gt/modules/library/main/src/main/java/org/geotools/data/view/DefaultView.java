/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004-2006, GeoTools Project Managment Committee (PMC)
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
package org.geotools.data.view;

import java.io.IOException;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.geotools.data.DataSourceException;
import org.geotools.data.DataStore;
import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureListener;
import org.geotools.data.FeatureLocking;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureStore;
import org.geotools.data.Query;
import org.geotools.data.crs.ForceCoordinateSystemFeatureResults;
import org.geotools.data.crs.ReprojectFeatureResults;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureType;
import org.geotools.feature.SchemaException;
import org.geotools.filter.FilterFactoryFinder;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;

/**
 * Wrapper for FeatureSource constrained by a Query.
 * 
 * <p>
 * Support FeatureSource decorator that takes care of mapping a Query &
 * FeatureSource with the schema and definition query configured for it.
 * </p>
 * 
 * <p>
 * Because GeoServer requires that attributes always be returned in the same
 * order we need a way to smoothly inforce this. Could we use this class to do
 * so?
 * </p>
 * <p>
 * WARNING: this class is a placeholder for ideas right now - it may not always
 * impement FeatureSource.
 * </p>
 * 
 * @author Gabriel Rold�n
 * @source $URL$
 */
public class DefaultView implements FeatureSource {

    /** Shared package logger */
    private static final Logger LOGGER = Logger.getLogger("org.geotools.data.view");

    /** FeatureSource being served up */
    protected FeatureSource source;

    /**
     * Schema generated by provided constraintQuery
     */
    private FeatureType schema;

    /** Query provided as a constraint */
    private Query constraintQuery;

    /**
     * Creates a new GeoServerFeatureSource object.
     * <p>
     * Grabs the following from query:
     * <ul>
     * <li>typeName - only used if client does not supply
     * <li>cs - only used if client does not supply
     * <li>csForce - only used if client does not supply
     * <li>filter - combined with client filter
     * <li>propertyNames - combined with client filter (indicate property names
     * that *must* be included)
     * </ul>
     * </p>
     * Schema is generated based on this information.
     * </p>
     * 
     * @param source
     *            a FeatureSource
     * @param query
     *            Filter used to limit results
     * @throws SchemaException
     */
    public DefaultView(FeatureSource source, Query query) throws SchemaException {
        this.source = source;
        this.constraintQuery = query;

        FeatureType origionalType = source.getSchema();

        CoordinateReferenceSystem cs = null;
        if (query.getCoordinateSystemReproject() != null) {
            cs = query.getCoordinateSystemReproject();
        } else if (query.getCoordinateSystem() != null) {
            cs = query.getCoordinateSystem();
        }
        schema = DataUtilities.createSubType(origionalType, query.getPropertyNames(), cs, query
                .getTypeName(), null);
    }

    /**
     * Factory that make the correct decorator for the provided featureSource.
     * 
     * <p>
     * This factory method is public and will be used to create all required
     * subclasses. By comparison the constructors for this class have package
     * visibiliy.
     * </p>
     * 
     * TODO: revisit this - I am not sure I want write access to views
     * (especially if they do reprojection).
     * 
     * @param source
     * @param query
     * 
     * @return @throws
     *         SchemaException
     */
    public static FeatureSource create(FeatureSource source, Query query) throws SchemaException {
        if (source instanceof FeatureLocking) {
            //  return new GeoServerFeatureLocking((FeatureLocking) source,
            // schema, definitionQuery);
        } else if (source instanceof FeatureStore) {
            //return new GeoServerFeatureStore((FeatureStore) source, schema,
            // definitionQuery);
        }
        return new DefaultView(source, query);
    }

    /**
     * Takes a query and adapts it to match re definitionQuery filter configured
     * for a feature type. It won't handle coordinate system changes
     * <p>
     * Grabs the following from query:
     * <ul>
     * <li>typeName - only used if client does not supply
     * <li>filter - combined with client filter
     * <li>propertyNames - combined with client filter (indicate property names
     * that *must* be included)
     * </ul>
     * </p>
     * 
     * @param query
     *            Query against this DataStore
     * 
     * @return Query restricted to the limits of definitionQuery
     * 
     * @throws IOException
     *             See DataSourceException
     * @throws DataSourceException
     *             If query could not meet the restrictions of definitionQuery
     */
    protected DefaultQuery makeDefinitionQuery(Query query) throws IOException {
        if ((query == Query.ALL) || query.equals(Query.ALL)) {
            return new DefaultQuery(constraintQuery);
        }

        try {
            String[] propNames = extractAllowedAttributes(query);

            String typeName = query.getTypeName();
            if (typeName == null) {
                typeName = constraintQuery.getTypeName();
            }

            URI namespace = query.getNamespace();
            if (namespace == null || namespace == Query.NO_NAMESPACE) {
                namespace = constraintQuery.getNamespace();
            }
            Filter filter = makeDefinitionFilter(query.getFilter());

            int maxFeatures = Math.min(query.getMaxFeatures(), constraintQuery.getMaxFeatures());

            String handle = query.getHandle();
            if (handle == null) {
                handle = constraintQuery.getHandle();
            } else if (constraintQuery.getHandle() != null) {
                handle = handle + "(" + constraintQuery.getHandle() + ")";
            }

            DefaultQuery defaultQuery = new DefaultQuery(typeName, namespace, filter, maxFeatures, propNames, handle);
            defaultQuery.setSortBy(query.getSortBy());
            return defaultQuery;
        } catch (Exception ex) {
            throw new DataSourceException(
                    "Could not restrict the query to the definition criteria: " + ex.getMessage(),
                    ex);
        }
    }

    /**
     * List of allowed attributes.
     * 
     * <p>
     * Creates a list of FeatureTypeInfo's attribute names based on the
     * attributes requested by <code>query</code> and making sure they not
     * contain any non exposed attribute.
     * </p>
     * 
     * <p>
     * Exposed attributes are those configured in the "attributes" element of
     * the FeatureTypeInfo's configuration
     * </p>
     * 
     * @param query
     *            User's origional query
     * 
     * @return List of allowed attribute types
     */
    private String[] extractAllowedAttributes(Query query) {
        String[] propNames = null;

        if (query.retrieveAllProperties()) {
            propNames = new String[schema.getAttributeCount()];

            for (int i = 0; i < schema.getAttributeCount(); i++) {
                propNames[i] = schema.getAttributeType(i).getLocalName();
            }
        } else {
            String[] queriedAtts = query.getPropertyNames();
            int queriedAttCount = queriedAtts.length;
            List allowedAtts = new LinkedList();

            for (int i = 0; i < queriedAttCount; i++) {
                if (schema.getAttributeType(queriedAtts[i]) != null) {
                    allowedAtts.add(queriedAtts[i]);
                } else {
                    LOGGER.info("queried a not allowed property: " + queriedAtts[i]
                            + ". Ommitting it from query");
                }
            }

            propNames = (String[]) allowedAtts.toArray(new String[allowedAtts.size()]);
        }

        return propNames;
    }

    /**
     * If a definition query has been configured for the FeatureTypeInfo, makes
     * and return a new Filter that contains both the query's filter and the
     * layer's definition one, by logic AND'ing them.
     * 
     * @param filter
     *            Origional user supplied Filter
     * 
     * @return Filter adjusted to the limitations of definitionQuery
     * 
     * @throws DataSourceException
     *             If the filter could not meet the limitations of
     *             definitionQuery
     */
    protected Filter makeDefinitionFilter(Filter filter) throws DataSourceException {
        Filter newFilter = filter;
        Filter constraintFilter = constraintQuery.getFilter();
        try {
            if (constraintFilter != Filter.INCLUDE) {
                FilterFactory ff = CommonFactoryFinder.getFilterFactory(null);
                newFilter = ff.and(constraintFilter, filter);
            }
        } catch (Exception ex) {
            throw new DataSourceException("Can't create the constraint filter", ex);
        }
        return newFilter;
    }

    /**
     * Implement getDataStore.
     * 
     * <p>
     * Description ...
     * </p>
     * 
     * @return @see org.geotools.data.FeatureSource#getDataStore()
     */
    public DataStore getDataStore() {
        return source.getDataStore();
    }

    /**
     * Implement addFeatureListener.
     * 
     * <p>
     * Description ...
     * </p>
     * 
     * @param listener
     * 
     * @see org.geotools.data.FeatureSource#addFeatureListener(org.geotools.data.FeatureListener)
     */
    public void addFeatureListener(FeatureListener listener) {
        source.addFeatureListener(listener);
    }

    /**
     * Implement removeFeatureListener.
     * 
     * <p>
     * Description ...
     * </p>
     * 
     * @param listener
     * 
     * @see org.geotools.data.FeatureSource#removeFeatureListener(org.geotools.data.FeatureListener)
     */
    public void removeFeatureListener(FeatureListener listener) {
        source.removeFeatureListener(listener);
    }

    /**
     * Implement getFeatures.
     * 
     * <p>
     * Description ...
     * </p>
     * 
     * @param query
     * 
     * @return @throws
     *         IOException
     * 
     * @see org.geotools.data.FeatureSource#getFeatures(org.geotools.data.Query)
     */
    public FeatureCollection getFeatures(Query query) throws IOException {
        DefaultQuery mergedQuery = makeDefinitionQuery(query);
        FeatureCollection results = source.getFeatures(mergedQuery);

        // Get all the coordinate systems involved in the two queries
        CoordinateReferenceSystem cCs = constraintQuery.getCoordinateSystem();
        CoordinateReferenceSystem cCsr = constraintQuery.getCoordinateSystemReproject();
        CoordinateReferenceSystem qCs = query.getCoordinateSystem();
        CoordinateReferenceSystem qCsr = query.getCoordinateSystemReproject();

        /*
         * Here we create all the needed transformations. We assume for the
         * moment that the data stores are incapable of any kind of cs
         * transformation and neither capable of forcing cs. We also assume that
         * concatenating multiple forced and reprojected wrappers is inexpensive
         * since they are optimized to recognize each other and to avoid useless
         * object creation
         */
        try {
            if (qCsr != null && cCsr != null) {
                if (cCs != null)
                    results = new ForceCoordinateSystemFeatureResults(results, cCs);
                results = new ReprojectFeatureResults(results, cCsr);
                if (qCs != null)
                    results = new ForceCoordinateSystemFeatureResults(results, qCs);
                results = new ReprojectFeatureResults(results, qCsr);
            } else if (qCs != null && cCsr != null) {
                // complex case 2, reprojected then forced
                // mergedQuery.setCoordinateSystem(cCs);
                // mergedQuery.setCoordinateSystemReproject(cCsr);
                try {
                    if (cCs != null)
                        results = new ForceCoordinateSystemFeatureResults(results, cCs);
                    results = new ReprojectFeatureResults(source.getFeatures(mergedQuery), cCsr);

                    results = new ForceCoordinateSystemFeatureResults(results, qCs);
                } catch (SchemaException e) {
                    throw new DataSourceException("This should not happen", e);
                }
            } else {
                // easy case, we can just put toghether one forced cs and one
                // reprojection cs
                // in the mixed query and let it go

                // mergedQuery.setCoordinateSystem(qCs != null ? qCs : cCs);
                // mergedQuery.setCoordinateSystemReproject(qCsr != null ? qCsr
                // : cCsr);
                CoordinateReferenceSystem forcedCS = qCs != null ? qCs : cCs;
                CoordinateReferenceSystem reprojectCS = qCsr != null ? qCsr : cCsr;

                if (forcedCS != null)
                    results = new ForceCoordinateSystemFeatureResults(results, forcedCS);
                if (reprojectCS != null)
                    results = new ReprojectFeatureResults(results, reprojectCS);
            }
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new DataSourceException("A problem occurred while handling forced "
                    + "coordinate systems and reprojection", e);
        }

        return results;
    }

    /**
     * Implement getFeatures.
     * 
     * <p>
     * Description ...
     * </p>
     * 
     * @param filter
     * 
     * @return @throws
     *         IOException
     */
    public FeatureCollection getFeatures(Filter filter) throws IOException {
        return getFeatures(new DefaultQuery(schema.getTypeName(),filter));
    }

    /**
     * Implement getFeatures.
     * 
     * <p>
     * Description ...
     * </p>
     * 
     * @return @throws
     *         IOException
     * 
     * @see org.geotools.data.FeatureSource#getFeatures()
     */
    public FeatureCollection getFeatures() throws IOException {
        return getFeatures(Query.ALL);
    }

    /**
     * Implement getSchema.
     * 
     * <p>
     * Description ...
     * </p>
     * 
     * @return @see org.geotools.data.FeatureSource#getSchema()
     */
    public FeatureType getSchema() {
        return schema;
    }

    /**
     * Retrieves the total extent of this FeatureSource.
     * 
     * <p>
     * Please note this extent will reflect the provided definitionQuery.
     * </p>
     * 
     * @return Extent of this FeatureSource, or <code>null</code> if no
     *         optimizations exist.
     * 
     * @throws IOException
     *             If bounds of definitionQuery
     */
    public Envelope getBounds() throws IOException {
        if (constraintQuery.getCoordinateSystemReproject() == null) {
            if (constraintQuery.getFilter() == null || constraintQuery.getFilter() == Filter.INCLUDE
                    || Filter.INCLUDE.equals(constraintQuery.getFilter())) {
                return source.getBounds();
            }
                return source.getBounds(constraintQuery);
            
        }
            // this will create a feature results that can reproject the
            // features, and will
            // properly compute the bouds
            return getFeatures().getBounds();
        
    }

    /**
     * Retrive the extent of the Query.
     * 
     * <p>
     * This method provides access to an optimized getBounds opperation. If no
     * optimized opperation is available <code>null</code> will be returned.
     * </p>
     * 
     * <p>
     * You may still make use of getFeatures( Query ).getCount() which will
     * return the correct answer (even if it has to itterate through all the
     * results to do so.
     * </p>
     * 
     * @param query
     *            User's query
     * 
     * @return Extend of Query or <code>null</code> if no optimization is
     *         available
     * 
     * @throws IOException
     *             If a problem is encountered with source
     */
    public Envelope getBounds(Query query) throws IOException {
        if (constraintQuery.getCoordinateSystemReproject() == null) {
            try {
                query = makeDefinitionQuery(query);
            } catch (IOException ex) {
                return null;
            }

            return source.getBounds(query);
        }
            // this will create a feature results that can reproject the
            // features, and will
            // properly compute the bouds
            return getFeatures(query).getBounds();
    }

    /**
     * Adjust query and forward to source.
     * 
     * <p>
     * This method provides access to an optimized getCount opperation. If no
     * optimized opperation is available <code>-1</code> will be returned.
     * </p>
     * 
     * <p>
     * You may still make use of getFeatures( Query ).getCount() which will
     * return the correct answer (even if it has to itterate through all the
     * results to do so).
     * </p>
     * 
     * @param query
     *            User's query.
     * 
     * @return Number of Features for Query, or -1 if no optimization is
     *         available.
     */
    public int getCount(Query query) {
        try {
            query = makeDefinitionQuery(query);
        } catch (IOException ex) {
            return -1;
        }
        try {
            return source.getCount(query);
        } catch (IOException e) {
            return 0;
        }
    }
    
    public Set getSupportedHints() {
        return source.getSupportedHints();
    }
}
