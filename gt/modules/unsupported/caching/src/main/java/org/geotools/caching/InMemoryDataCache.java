/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, GeoTools Project Managment Committee (PMC)
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
package org.geotools.caching;

import java.io.IOException;
import org.opengis.filter.Filter;
import org.geotools.data.DataStore;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureWriter;
import org.geotools.data.LockingManager;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.FeatureType;
import org.geotools.feature.SchemaException;


/** Implementation of DataCache that uses in-memory storage,
 * spatial query tracker and spatial index.
 *
 *  IMPORTANT : for the time being, this class provides cache facility only
 *  when using getView(Query) method. Other methods simply delegate to source DataStore.
 *
 * @task use this class to design AbstractDataCache,
 * which would be the parent of all DataCache implementation.
 *
 * @author Christophe Rousson, SoC 2007, CRG-ULAVAL
 *
 */
public class InMemoryDataCache implements DataCache {
    /**
     * The source DataStore, from where to get original features
     */
    private final DataStore source;

    /**
     * Tracker to keep relationships between queries and features.
     */
    private final QueryTracker tracker;

    /**
     * Actual storage for features.
     */
    private final FeatureIndex index;

    /** Creates a new DataCache on top of DataStore ds.
     *
     * @param ds the DataStore to cache.
     */
    public InMemoryDataCache(DataStore ds) {
        this.source = ds;
        this.tracker = new SpatialQueryTracker();

        try {
            this.index = new MemoryFeatureIndex(ds.getSchema(ds.getTypeNames()[0]), 100);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            throw (RuntimeException) new RuntimeException().initCause(e);
        }
    }

    public void clear() {
        // TODO Auto-generated method stub
    }

    public void flush() throws IllegalStateException {
        // TODO Auto-generated method stub
    }

    public long getHits() {
        // TODO Auto-generated method stub
        return 0;
    }

    public void createSchema(FeatureType arg0) throws IOException {
        source.createSchema(arg0);
    }

    public FeatureReader getFeatureReader(Query arg0, Transaction arg1)
        throws IOException {
        return source.getFeatureReader(arg0, arg1);
    }

    public FeatureSource getFeatureSource(String arg0)
        throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    public FeatureWriter getFeatureWriter(String arg0, Transaction arg1)
        throws IOException {
        return source.getFeatureWriter(arg0, arg1);
    }

    public FeatureWriter getFeatureWriter(String arg0, Filter arg1, Transaction arg2)
        throws IOException {
        // TODO Auto-generated method stub
        return source.getFeatureWriter(arg0, arg1, arg2);
    }

    public FeatureWriter getFeatureWriterAppend(String arg0, Transaction arg1)
        throws IOException {
        return source.getFeatureWriterAppend(arg0, arg1);
    }

    public LockingManager getLockingManager() {
        return source.getLockingManager();
    }

    public FeatureType getSchema(String arg0) throws IOException {
        return source.getSchema(arg0);
    }

    public String[] getTypeNames() throws IOException {
        return source.getTypeNames();
    }

    /* (non-Javadoc)
     * @see org.geotools.data.DataStore#getView(org.geotools.data.Query)
     *
     * This is the important method :
     *
     * Sequence proposed to process user query :
     *
     * user query
     *  -> match query in tracker
     *  -> dowload missing data from source
     *  -> add new data to cache
     *  -> register query in tracker
     *  -> read cache
     *     -> anwser to query
     *
     */
    public FeatureSource getView(Query q) throws IOException, SchemaException {
        Query m = tracker.match(q);
        FeatureSource in = source.getView(m);
        FeatureCollection fc = in.getFeatures();

        // FIXME what if the query oversize the cache ?
        if (fc.size() > 0) {
            FeatureIterator i = fc.features();

            while (i.hasNext()) {
                index.add((Feature) i.next());
            }

            fc.close(i);
        }

        tracker.register(m);

        // if query q could not be turned into a "smaller" query, ie a query that yield a smaller set of features,
        // returns directly collection obtained from source, rather than reading the cache.
        if (m.equals(q)) {
            return in;
        } else {
            return index.getView(q);
        }
    }

    public void updateSchema(String arg0, FeatureType arg1)
        throws IOException {
        source.updateSchema(arg0, arg1);
    }
}
