package org.geotools.arcsde.data;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.geotools.arcsde.pool.ArcSDEPooledConnection;
import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureReader;
import org.geotools.data.Query;
import org.geotools.data.store.DataFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureReaderIterator;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * FeatureCollection implementation that works over an
 * {@link ArcSDEFeatureReader} or one of the decorators over it returned by
 * {@link ArcSDEDataStore#getFeatureReader(Query, ArcSDEPooledConnection, boolean)}.
 * 
 * @author Gabriel Roldan (TOPP)
 * @version $Id$
 * @since 2.5
 * @URL $URL:
 *      http://svn.geotools.org/geotools/trunk/gt/modules/plugin/arcsde/datastore/src/main/java/org/geotools/arcsde/data/ArcSdeFeatureCollection.java $
 * @see FeatureCollection
 */
public class ArcSdeFeatureCollection extends DataFeatureCollection {

    private final ArcSdeFeatureSource featureSource;
    private final Query query;
    private final Set<FeatureReaderIterator> openIterators;
    private ArcSDEPooledConnection connection;
    private SimpleFeatureType childrenSchema;

    public ArcSdeFeatureCollection(final ArcSdeFeatureSource featureSource, final Query namedQuery) {
        this.featureSource = featureSource;
        this.query = namedQuery;

        final Set<FeatureReaderIterator> iterators;
        iterators = new HashSet<FeatureReaderIterator>();
        this.openIterators = Collections.synchronizedSet(iterators);
    }

    /**
     * @see FeatureCollection#getSchema()
     */
    @Override
    public final synchronized SimpleFeatureType getSchema() {
        if (childrenSchema == null) {
            try {
                final ArcSDEPooledConnection conn = getConnection();
                final ArcSDEDataStore dataStore = featureSource.getDataStore();
                DefaultQuery query = new DefaultQuery(this.query);
                query.setFilter(Filter.EXCLUDE);
                final FeatureReader reader = dataStore.getFeatureReader(query, conn, false);
                this.childrenSchema = reader.getFeatureType();
                reader.close();
            } catch (IOException e) {
                throw new RuntimeException("Can't fetch schema for query " + query, e);
            } finally {
                closeConnection();
            }
        }
        return childrenSchema;
    }

    /**
     * @see FeatureCollection#getBounds()
     */
    @Override
    public final ReferencedEnvelope getBounds() {
        try {
            final ArcSDEPooledConnection connection = getConnection();
            return featureSource.getBounds(query, connection);
        } catch (IOException e) {
            return new ReferencedEnvelope((CoordinateReferenceSystem) null);
        } finally {
            closeConnection();
        }
    }

    @Override
    public final int getCount() throws IOException {
        final ArcSDEPooledConnection connection = getConnection();
        try {
            return featureSource.getCount(query, connection);
        } finally {
            closeConnection();
        }
    }

    @Override
    protected final void closeIterator(Iterator close) throws IOException {
        FeatureReaderIterator iterator = (FeatureReaderIterator) close;
        iterator.close(); // only needs package visability
    }

    /**
     * Returns
     */
    @Override
    protected final Iterator openIterator() throws IOException {
        final ArcSDEDataStore dataStore = featureSource.getDataStore();
        final ArcSDEPooledConnection connection = getConnection();
        
        final FeatureReader reader = dataStore.getFeatureReader(query, connection, false);
        // slight optimization here: store the child features schema if not yet
        // done
        if (this.childrenSchema == null) {
            this.childrenSchema = reader.getFeatureType();
        }

        final FeatureReaderIterator iterator = new FeatureReaderIterator(reader) {
            @Override
            public void close() {
                super.close();
                openIterators.remove(this);
                closeConnection();
            }
        };
        openIterators.add(iterator);
        return iterator;
    }

    private synchronized ArcSDEPooledConnection getConnection() throws IOException {
        if (connection == null || connection.isPassivated()) {
            connection = featureSource.getConnection();
            connection.getLock().lock();
        }
        return connection;
    }

    private synchronized void closeConnection() {
        if (openIterators.size() == 0) {
            if (!connection.isPassivated()) {
                connection.getLock().unlock();
                if (!connection.isTransactionActive()) {
                    connection.close();
                }
            }
            connection = null;
        }
    }
}
