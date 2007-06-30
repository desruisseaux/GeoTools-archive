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
package org.geotools.caching.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import com.vividsolutions.jts.geom.Envelope;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.geotools.caching.FeatureCache;
import org.geotools.caching.FeatureCacheException;
import org.geotools.caching.QueryTracker;
import org.geotools.caching.spatialindex.rtree.RTree;
import org.geotools.caching.spatialindex.spatialindex.IData;
import org.geotools.caching.spatialindex.spatialindex.INode;
import org.geotools.caching.spatialindex.spatialindex.ISpatialIndex;
import org.geotools.caching.spatialindex.spatialindex.IVisitor;
import org.geotools.caching.spatialindex.spatialindex.Region;
import org.geotools.caching.spatialindex.spatialindex.SpatialIndex;
import org.geotools.caching.spatialindex.storagemanager.MemoryStorageManager;
import org.geotools.caching.spatialindex.storagemanager.PropertySet;
import org.geotools.caching.util.FilterSplitter;
import org.geotools.data.DataStore;
import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureListener;
import org.geotools.data.FeatureReader;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.data.store.EmptyFeatureCollection;
import org.geotools.feature.AttributeType;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.FeatureType;
import org.geotools.feature.SchemaException;
import org.geotools.filter.FilterFactoryImpl;
import org.geotools.filter.spatial.BBOXImpl;


/** An implementation of FeatureCache :
 *  <ul><li>with in memory storage
 *      <li>uses a RTree to index features
 *      <li>uses a SpatialQueryTracker to track query bounds
 *  </ul>
 *
 * @param ds the DataStore to cache
 * @param t the FeatureType
 *
 * TODO: add constructor InMemoryFeatureCache(FeatureStore)
 *
 * @author Christophe Rousson, SoC 2007, CRG-ULAVAL
 *
 */
public class InMemoryFeatureCache implements FeatureCache {
    protected Transaction transaction = Transaction.AUTO_COMMIT;
    protected final DataStore ds;
    protected final HashMap store;
    protected final FeatureType type;
    protected final QueryTracker tracker;
    protected final ISpatialIndex index;

    /** Create a new InMemoryFeatureCache
     *
     * @param ds the source DataStore for features
     * @param t FeatureType to cache
     * @throws FeatureCacheException if DataStore does not have type t, or if IOException occurs
     */
    public InMemoryFeatureCache(DataStore ds, FeatureType t)
        throws FeatureCacheException {
        FeatureType dstype = null;

        try {
            dstype = ds.getSchema(t.getTypeName());
        } catch (IOException e) {
            throw (FeatureCacheException) new FeatureCacheException().initCause(e);
        }

        if ((dstype == null) || !dstype.equals(t)) {
            throw new FeatureCacheException(new SchemaException("Datastore does not have type "
                    + t.getTypeName()));
        }

        this.ds = ds;
        this.type = t;
        this.store = new HashMap();
        this.tracker = new SpatialQueryTracker();

        PropertySet ps = new PropertySet();
        ps.setProperty("TreeVariant", new Integer(SpatialIndex.RtreeVariantLinear));

        MemoryStorageManager sm = new MemoryStorageManager();
        this.index = new RTree(ps, sm);
    }

    public void clear() {
        throw new UnsupportedOperationException();
    }

    public void evict() {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.geotools.caching.FeatureCache#get(java.lang.String)
     */
    public Feature get(String id) throws FeatureCacheException {
        /*SimpleFeatureCacheEntry entry = (SimpleFeatureCacheEntry) store.get(id) ;
           Feature f = null ;*/
        Feature f = (Feature) store.get(id);

        if (f == null) {
            Filter filter = new FilterFactoryImpl().createFidFilter(id);

            try {
                FeatureCollection fc = getFeatures(filter);

                if (fc.size() > 0) {
                    FeatureIterator it = fc.features();
                    f = it.next();
                }
            } catch (IOException e) {
                throw (FeatureCacheException) new FeatureCacheException().initCause(e);
            }

            /*} else {
               f = (Feature) entry.getValue() ; */
        }

        return f;
    }

    /**
     * @param id
     * @return
     */
    public Feature peek(String id) {
        /*SimpleFeatureCacheEntry entry = (SimpleFeatureCacheEntry) store.get(id) ;
           if (entry == null) {
                   return null ;
           } else {
                   return (Feature) entry.getValue() ;
           }*/
        return (Feature) store.get(id);
    }

    /** Transform a JTS Envelope to a Region
     *
     * @param e JTS Envelope
     * @return
     */
    protected static Region toRegion(final Envelope e) {
        Region r = new Region(new double[] { e.getMinX(), e.getMinY() },
                new double[] { e.getMaxX(), e.getMaxY() });

        return r;
    }

    public void put(Feature f) {
        if (store.containsKey(f.getID())) {
            return;
        }

        //store.put(f.getID(), new SimpleFeatureCacheEntry(f));
        store.put(f.getID(), f);

        Region r = toRegion(f.getBounds());
        index.insertData(f.getID().getBytes(), r, f.getID().hashCode());
    }

    public void putAll(FeatureCollection fc) {
        FeatureIterator it = fc.features();

        while (it.hasNext()) {
            put(it.next());
        }

        it.close();
    }

    public Feature remove(String id) {
        Feature f = peek(id);

        if (f == null) {
            return null;
        }

        index.deleteData(toRegion(f.getBounds()), f.getID().hashCode());
        store.remove(id);

        return f;
    }

    public int size() {
        return store.size();
    }

    public Filter[] splitFilter(Filter f) {
        // TODO really do split filter
        Filter[] filters = new Filter[3];
        FilterSplitter splitter = new FilterSplitter();
        f.accept(splitter, null);

        Filter sr = splitter.getSpatialRestriction();

        /*if (f instanceof BBOXImpl) {
           Filter missing = tracker.match(sr);
           Filter cached;
           if (missing.equals(f)) {
               cached = Filter.EXCLUDE;
           } else {
               cached = f;
           }
           filters[SPATIAL_RESTRICTION_CACHED] = cached;
           filters[SPATIAL_RESTRICTION_MISSING] = missing;
           filters[OTHER_RESTRICTIONS] = Filter.INCLUDE;
           } else {
               filters[SPATIAL_RESTRICTION_CACHED] = Filter.EXCLUDE;
               filters[SPATIAL_RESTRICTION_MISSING] = f;
               filters[OTHER_RESTRICTIONS] = Filter.INCLUDE;
           }*/
        assert (sr == Filter.INCLUDE || sr instanceof BBOXImpl);
        //System.out.println(sr.getClass()) ;

        Filter missing = tracker.match(sr);
        Filter cached;

        if (missing == sr) {
            cached = Filter.EXCLUDE;
        } else {
            cached = sr;
        }

        filters[SPATIAL_RESTRICTION_CACHED] = cached;
        filters[SPATIAL_RESTRICTION_MISSING] = missing;
        filters[OTHER_RESTRICTIONS] = splitter.getOtherRestriction();

        return filters;
    }

    public Set addFeatures(FeatureCollection collection)
        throws IOException {
        throw new UnsupportedOperationException();
    }

    public Transaction getTransaction() {
        // TODO Auto-generated method stub
        return transaction;
    }

    public void modifyFeatures(AttributeType[] type, Object[] value, Filter filter)
        throws IOException {
        throw new UnsupportedOperationException();
    }

    public void modifyFeatures(AttributeType type, Object value, Filter filter)
        throws IOException {
        throw new UnsupportedOperationException();
    }

    public void removeFeatures(Filter filter) throws IOException {
        throw new UnsupportedOperationException();
    }

    public void setFeatures(FeatureReader reader) throws IOException {
        throw new UnsupportedOperationException();
    }

    public void setTransaction(Transaction transaction) {
        if (transaction == null) {
            throw new IllegalArgumentException(
                "Transaction cannot be null, did you mean Transaction.AUTO_COMMIT?");
        }

        this.transaction = transaction;
    }

    public void addFeatureListener(FeatureListener listener) {
        // TODO Auto-generated method stub
    }

    public Envelope getBounds() throws IOException {
        // TODO Auto-generated method stub
        return getDataStore().getFeatureSource(type.getTypeName()).getBounds();
    }

    public Envelope getBounds(Query query) throws IOException {
        return getDataStore().getFeatureSource(type.getTypeName()).getBounds(query);
    }

    public int getCount(Query query) throws IOException {
        // may be we should return -1 if this is too expensive, or an estimate ?
        return getDataStore().getFeatureSource(type.getTypeName()).getCount(query);
    }

    public DataStore getDataStore() {
        return ds;
    }

    public FeatureCollection getFeatures() throws IOException {
        return getFeatures(Filter.INCLUDE);
    }

    public FeatureCollection getFeatures(Query query) throws IOException {
        if ((query.getTypeName() != null) && (query.getTypeName() != type.getTypeName())) {
            return new EmptyFeatureCollection(getSchema());
        }

        return getFeatures(query.getFilter());
    }

    public FeatureCollection getFeatures(Filter filter)
        throws IOException {
        Filter[] filters = splitFilter(filter);
        FeatureCollection fromCache = loadFromCache(filters[SPATIAL_RESTRICTION_CACHED]);

        //System.out.println("from cache = " + fromCache.size()) ;
        //fromCache.subCollection(filters[OTHER_RESTRICTIONS]) ;
        //System.out.println("from cache = " + fromCache.size()) ;
        FilterFactory ff = new FilterFactoryImpl();
        Filter missing = filters[SPATIAL_RESTRICTION_MISSING];

        //System.out.println("from store = " + fromStore.size()) ;
        if (missing != Filter.EXCLUDE) {
            FeatureCollection fromStore = loadFromStore(ff.and(missing, filters[OTHER_RESTRICTIONS]));
            tracker.register(missing);
            fromCache.addAll(fromStore);

            //System.out.println("Added data to cache") ;
        }

        return fromCache;
    }

    protected FeatureCollection loadFromStore(Filter f)
        throws IOException {
        FeatureCollection c = ds.getFeatureSource(type.getTypeName()).getFeatures(f);
        putAll(c);

        //System.out.println(index.getStatistics()) ;
        return c;
    }

    protected FeatureCollection loadFromCache(Filter f) {
        if (f == Filter.EXCLUDE) {
            return new DefaultFeatureCollection("cached", type);
        } else {
            final List features = new ArrayList();
            BBOXImpl bb = (BBOXImpl) f;
            Region r = new Region(new double[] { bb.getMinX(), bb.getMinY() },
                    new double[] { bb.getMaxX(), bb.getMaxY() });
            index.intersectionQuery(r,
                new IVisitor() {
                    public void visitData(final IData d) {
                        String id = new String(d.getData());
                        features.add(peek(id));

                        //System.out.println("Data = " + d.getIdentifier() + " fid = " + get(id)) ;
                    }

                    public void visitNode(final INode n) {
                        //System.out.println("Node = " + n.getIdentifier()) ;
                    }
                });

            return DataUtilities.collection((Feature[]) features.toArray(new Feature[1]));
        }
    }

    public FeatureType getSchema() {
        return type;
    }

    public void removeFeatureListener(FeatureListener listener) {
        throw new UnsupportedOperationException();
    }
}
