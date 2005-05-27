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
package org.geotools.data;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.geotools.data.store.DataFeatureCollection;
import org.geotools.feature.CollectionListener;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.FeatureType;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.SchemaException;
import org.geotools.geometry.JTS;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;


/**
 * Generic "results" of a query, class.
 * <p>
 * Please optimize this class when use with your own content.
 * For example a "ResultSet" make a great cache for a JDBCDataStore,
 * a temporary copy of an original file may work for shapefile etc. 
 * </p>
 *
 * @author Jody Garnett, Refractions Research
 */
public class DefaultFeatureResults extends DataFeatureCollection {
    
    /** Query used to define this subset of features from the feature source */
    protected Query query;
    
    /**
     * Feature source used to aquire features, note we are only a
     * "view" of this FeatureSource, its contents, transaction and events
     * need to be forwarded through this collection api to simplier code
     * such as renderers.
     */
    protected FeatureSource featureSource;

    /**
     * FeatureResults query against featureSource.
     * <p>
     * Please note that is object will not be valid
     * after the transaction has closed.
     * </p>
     * <p>
     * Really? I think it would be, it would just reflect the
     * same query against the featuresource using AUTO_COMMIT.
     * </p>
     * 
     * @param source
     * @param query
     */
    public DefaultFeatureResults(FeatureSource source, Query query) {
        this.featureSource = source;
        String typeName = source.getSchema().getTypeName();

        if( typeName.equals( query.getTypeName() ) ){
            this.query = query;
        }
        else {
            this.query = new DefaultQuery(
                typeName,
                query.getFilter(),
                query.getMaxFeatures(),
                query.getPropertyNames(),
                query.getHandle()
            );
            ((DefaultQuery) this.query).setCoordinateSystem(query.getCoordinateSystem());
            ((DefaultQuery) this.query).setCoordinateSystemReproject(query.getCoordinateSystemReproject());
        }
    }

    /**
     * FeatureSchema for provided query.
     *
     * <p>
     * If query.retrieveAllProperties() is <code>true</code> the FeatureSource
     * getSchema() will be returned.
     * </p>
     *
     * <p>
     * If query.getPropertyNames() is used to limit the result of the Query a
     * sub type will be returned based on FeatureSource.getSchema().
     * </p>
     *
     * @return DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     * @throws DataSourceException DOCUMENT ME!
     */
    public FeatureType getSchema() {
        if (query.retrieveAllProperties()) {
            return featureSource.getSchema();
        } else {
            try {
                return DataUtilities.createSubType(featureSource.getSchema(),
                    query.getPropertyNames());
            } catch (SchemaException e) {
                return featureSource.getSchema();
                //throw new DataSourceException("Could not create schema", e);
            }
        }
    }

    /**
     * Returns transaction from featureSource (if it is a FeatureStore), or
     * Transaction.AUTO_COMMIT if it is not.
     *
     * @return Transacstion this FeatureResults opperates against
     */
    protected Transaction getTransaction() {
        if (featureSource instanceof FeatureStore) {
            FeatureStore featureStore = (FeatureStore) featureSource;

            return featureStore.getTransaction();
        } else {
            return Transaction.AUTO_COMMIT;
        }
    }

    /**
     * Retrieve a FeatureReader for this Query
     *
     * @return FeatureReader for this Query
     *
     * @throws IOException If results could not be obtained
     */
    public FeatureReader reader() throws IOException {
        FeatureReader reader = featureSource.getDataStore().getFeatureReader(query,
                getTransaction());
        int maxFeatures = query.getMaxFeatures();

        if (maxFeatures == Integer.MAX_VALUE) {
            return reader;
        } else {
            return new MaxFeatureReader(reader, maxFeatures);
        }


    }

    /**
     * Returns the bounding box of this FeatureResults
     *
     * <p>
     * This implementation will generate the correct results from reader() if
     * the provided FeatureSource does not provide an optimized result via
     * FeatureSource.getBounds( Query ).
     * </p>
     * If the feature has no geometry, then an empty envelope is returned.
     *
     * @return
     *
     * @throws DataSourceException See IOException
     *
     * @see org.geotools.data.FeatureResults#getBounds()
     */
    public Envelope getBounds() {
        Envelope bounds;

        try {
            bounds = featureSource.getBounds(query);
        } catch (IOException e1) {
            return JTS.empty();
        }

        if (bounds != null) {
            return bounds;
        }

        try {
            Feature feature;
            bounds = new Envelope();

            FeatureReader reader = reader();

            while (reader.hasNext()) {
                feature = reader.next();
                bounds.expandToInclude(feature.getBounds());
            }

            reader.close();

            return bounds;
        } catch (IllegalAttributeException e) {
            //throw new DataSourceException("Could not read feature ", e);
            return JTS.empty();
        } catch (IOException e) {
            return JTS.empty();
        }
    }

    /**
     * Number of Features in this query.
     *
     * <p>
     * This implementation will generate the correct results from reader() if
     * the provided FeatureSource does not provide an optimized result via
     * FeatureSource.getCount( Query ).
     * </p>
     *
     * @return
     *
     * @throws IOException If feature could not be read
     * @throws DataSourceException See IOException
     *
     * @see org.geotools.data.FeatureResults#getCount()
     */
    public int getCount() throws IOException {
        int count;
        count = featureSource.getCount(query);

        if (count != -1) {
            // we have an optimization!
            return count;
        }

        // Okay lets count the FeatureReader
        try {
            count = 0;

            FeatureReader reader = reader();

            for (; reader.hasNext(); count++) {
                reader.next();
            }

            reader.close();

            return count;
        } catch (IllegalAttributeException e) {
            throw new DataSourceException("Could not read feature ", e);
        }
    }

    public FeatureCollection collection() throws IOException {
        try {
            FeatureCollection collection = FeatureCollections.newCollection();
            Feature feature;
            FeatureReader reader = reader();
            FeatureType type = reader.getFeatureType();
            while (reader.hasNext()) {
                collection.add(reader.next());
            }
            reader.close();

            return collection;
        } catch (IllegalAttributeException e) {
            throw new DataSourceException("Could not read feature ", e);
        }
    }

}
