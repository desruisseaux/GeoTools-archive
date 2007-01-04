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

package org.geotools.data.postgis;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.geotools.data.AbstractFeatureStore;
import org.geotools.data.DataSourceException;
import org.geotools.data.DataStore;
import org.geotools.data.DefaultQuery;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureListener;
import org.geotools.data.FeatureLocking;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureStore;
import org.geotools.data.FeatureWriter;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.data.postgis.fidmapper.VersionedFIDMapper;
import org.geotools.data.store.EmptyFeatureCollection;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.AttributeType;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureType;
import org.geotools.feature.IllegalAttributeException;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.sort.SortBy;
import org.opengis.filter.sort.SortOrder;

import com.vividsolutions.jts.geom.Envelope;

/**
 * A cheap implementation of a feature locking.
 * <p>
 * Implementation wise, for all locking needs, tries to leverage the wrapped datastore feature
 * locking. If an optimization is possible (mass updates come to mind), we try to use the feature
 * locking, otherwiser we fall back on the implementation inherited from AbstractFeatureSource.
 * <p>
 * {@link #modifyFeatures(AttributeType[], Object[], Filter)} is an example of things that cannot be
 * optimized. Theoretically, one could mass expire current feature, but he should have first read
 * into memory all of them to rewrite them as new (which may not be possible).
 * 
 * @author aaime
 * @since 2.4
 * 
 */
public class VersionedPostgisFeatureStore extends AbstractFeatureStore implements FeatureStore {

    private VersionedPostgisDataStore store;

    private FeatureLocking locking;

    private FeatureType schema;

    public VersionedPostgisFeatureStore(FeatureType schema, VersionedPostgisDataStore store)
            throws IOException {
        this.store = store;
        this.schema = schema;
        this.locking = (FeatureLocking) store.wrapped.getFeatureSource(schema.getTypeName());
    }

//    public int lockFeatures(Query query) throws IOException {
//        // check query does not need to work agains anything else than the last
//        // revision (just to avoid users being surprised the specified revision
//        // did not get locked/modified)
//        RevisionInfo ri = new RevisionInfo(query.getVersion());
//        if (ri.isLast())
//            throw new IllegalArgumentException("Cannot work against revisions but "
//                    + "the last one. Do not specify revision information in your write queries.");
//
//        DefaultQuery versionedQuery = store.buildVersionedQuery(getTypedQuery(query),
//                new RevisionInfo());
//        return locking.lockFeatures(versionedQuery);
//    }
//
//    public int lockFeatures(Filter filter) throws IOException {
//        Filter versionedFilter = (Filter) store.buildVersionedFilter(schema.getTypeName(), filter,
//                new RevisionInfo());
//        return locking.lockFeatures(versionedFilter);
//    }
//
//    public int lockFeatures() throws IOException {
//        return lockFeatures(Filter.INCLUDE);
//    }
//
//    public void setFeatureLock(FeatureLock lock) {
//        locking.setFeatureLock(lock);
//    }
//
//    public void unLockFeatures() throws IOException {
//        unLockFeatures(Filter.INCLUDE);
//    }
//
//    public void unLockFeatures(Filter filter) throws IOException {
//        Filter versionedFilter = (Filter) store.buildVersionedFilter(schema.getTypeName(), filter,
//                new RevisionInfo());
//        locking.unLockFeatures(versionedFilter);
//    }
//
//    public void unLockFeatures(Query query) throws IOException {
//        // check query does not need to work agains anything else than the last
//        // revision (just to avoid users being surprised the specified revision
//        // did not get locked/modified)
//        RevisionInfo ri = new RevisionInfo(query.getVersion());
//        if (ri.isLast())
//            throw new IllegalArgumentException("Cannot work against revisions but "
//                    + "the last one. Do not specify revision information in your write queries.");
//
//        DefaultQuery versionedQuery = store.buildVersionedQuery(getTypedQuery(query),
//                new RevisionInfo());
//        locking.unLockFeatures(versionedQuery);
//    }

    public Transaction getTransaction() {
        return locking.getTransaction();
    }

    public void setTransaction(Transaction transaction) {
        locking.setTransaction(transaction);
    }

    public Envelope getBounds() throws IOException {
        return getBounds(Query.ALL);
    }

    public Envelope getBounds(Query query) throws IOException {
        DefaultQuery versionedQuery = store.buildVersionedQuery(getTypedQuery(query),
                new RevisionInfo());
        return locking.getBounds(versionedQuery);
    }

    public int getCount(Query query) throws IOException {
        RevisionInfo ri = new RevisionInfo(query.getVersion());
        DefaultQuery versionedQuery = store.buildVersionedQuery(getTypedQuery(query), ri);
        return locking.getCount(versionedQuery);
    }

    /**
     * Clones the query and sets the proper type name into it
     * 
     * @param query
     * @return
     */
    private Query getTypedQuery(Query query) {
        DefaultQuery q = new DefaultQuery(query);
        q.setTypeName(schema.getTypeName());
        return q;
    }

    public DataStore getDataStore() {
        return store;
    }

    public void addFeatureListener(FeatureListener listener) {
        store.listenerManager.addFeatureListener(this, listener);
    }

    public FeatureType getSchema() {
        return schema;
    }

    public void removeFeatureListener(FeatureListener listener) {
        store.listenerManager.removeFeatureListener(this, listener);
    }

    public void modifyFeatures(AttributeType type, Object value, Filter filter) throws IOException {
        super.modifyFeatures(type, value, filter);
    }

    public void modifyFeatures(AttributeType[] type, Object[] value, Filter filter)
            throws IOException {
        super.modifyFeatures(type, value, filter);
    }

    public void removeFeatures(Filter filter) throws IOException {
        // this we can optimize, it's a matter of mass updating the last
        // revisions (and before that, we have to compute the modified envelope)
        Filter versionedFilter = (Filter) store.buildVersionedFilter(schema.getTypeName(), filter,
                new RevisionInfo());
        Envelope bounds = locking
                .getBounds(new DefaultQuery(schema.getTypeName(), versionedFilter));
        Transaction t = getTransaction();
        boolean autoCommit = false;
        if (Transaction.AUTO_COMMIT.equals(t)) {
            t = new DefaultTransaction();
            autoCommit = true;
        }
        VersionedJdbcTransactionState state = store.wrapped.getVersionedJdbcTransactionState(t);
        locking.modifyFeatures(locking.getSchema().getAttributeType("expired"), new Long(state
                .getRevision()), versionedFilter);
        if (autoCommit) {
            t.commit();
            t.close();
        }
        store.listenerManager.fireFeaturesRemoved(schema.getTypeName(), t, bounds, false);
    }

    public void setFeatures(FeatureReader reader) throws IOException {
        // remove everything, then add back
        removeFeatures(Filter.INCLUDE);
        addFeatures(reader);
    }

    public FeatureCollection getFeatures(Query query) throws IOException {
        // feature collection is writable unfortunately, we have to rely on the
        // default behaviour otherwise writes won't be versioned
        // TODO: builds a versioned feature collection that can do better, if possible at all
        return super.getFeatures(query);
    }

    public FeatureCollection getFeatures(Filter filter) throws IOException {
        // feature collection is writable unfortunately, we have to rely on the
        // default behaviour otherwise writes won't be versioned
        return super.getFeatures(filter);
    }

    public FeatureCollection getFeatures() throws IOException {
        // feature collection is writable unfortunately, we have to rely on the
        // default behaviour otherwise writes won't be versioned
        return super.getFeatures();
    }

    /**
     * Rolls back features matching the filter to the state they had on the specified version.
     * <p>
     * For a feature to be included into the rollback it's sufficient that one of its states between
     * <code>toVersion</code> and current matches the filter.
     * 
     * @param toVersion
     * @param filter
     * @throws IOException
     */
    public void rollback(String toVersion, Filter filter) throws IOException {
        // TODO: build an optimized version of this that can do the same work with a couple
        // of queries assuming the filter is fully encodable

        // Gather feature modified after toVersion
        ModifiedFeatureIds mfids = store.getModifiedFeatureFIDs(schema.getTypeName(), toVersion,
                null, filter, getTransaction());
        FilterFactory ff = CommonFactoryFinder.getFilterFactory(null);

        // remove all features that have been created and not deleted
        Set fidsToRemove = new HashSet(mfids.getCreated());
        fidsToRemove.removeAll(mfids.getDeleted());
        if (!fidsToRemove.isEmpty())
            removeFeatures(store.buildFidFilter(ff, fidsToRemove));

        // reinstate all features that were there before toVersion and that
        // have been deleted after it. Notice this is an insertion, so to preserve
        // the fids I have to use low level writers where I can set all attributes manually
        // (we work on the assumption the wrapped data store maps all attributes of the primary
        // key in the feature itself)
        Set fidsToRecreate = new HashSet(mfids.getDeleted());
        fidsToRecreate.removeAll(mfids.getCreated());
        if (!fidsToRecreate.isEmpty()) {
            long revision = store.wrapped.getVersionedJdbcTransactionState(getTransaction())
                    .getRevision();
            Filter recreateFilter = store.buildVersionedFilter(schema.getTypeName(), store
                    .buildFidFilter(ff, fidsToRecreate), new RevisionInfo(toVersion));
            FeatureReader fr = null;
            FeatureWriter fw = null;
            try {
                DefaultQuery q = new DefaultQuery(schema.getTypeName(), recreateFilter);
                fr = store.wrapped.getFeatureReader(q, getTransaction());
                fw = store.wrapped.getFeatureWriterAppend(schema.getTypeName(), getTransaction());
                while (fr.hasNext()) {
                    Feature original = fr.next();
                    Feature restored = fw.next();
                    for (int i = 0; i < original.getFeatureType().getAttributeCount(); i++) {
                        restored.setAttribute(i, original.getAttribute(i));
                    }
                    restored.setAttribute("revision", new Long(revision));
                    restored.setAttribute("expired", new Long(Long.MAX_VALUE));
                    fw.write();
                }
            } catch (IllegalAttributeException iae) {
                throw new DataSourceException("Unexpected error occurred while "
                        + "restoring deleted featues", iae);
            } finally {
                if (fr != null)
                    fr.close();
                if (fw != null)
                    fw.close();
            }
        }

        // Now onto the modified features, that were there, and still are there.
        // Since we cannot get a sorted writer we have to do a kind of inner loop scan
        // (note, a parellel scan of similarly sorted reader and writer would be more
        // efficient, but writer sorting is not there...)
        // Here it's possible to work against the external API, thought it would be more
        // efficient (but more complex) to work against the wrapped one.
        if (!mfids.getModified().isEmpty()) {
            Filter modifiedIdFilter = store.buildFidFilter(ff, mfids.getModified());
            Filter mifCurrent = store.buildVersionedFilter(schema.getTypeName(), modifiedIdFilter,
                    new RevisionInfo());
            FeatureReader fr = null;
            FeatureWriter fw = null;
            try {
                fw = store.getFeatureWriter(schema.getTypeName(), mifCurrent, getTransaction());
                while (fw.hasNext()) {
                    Feature current = fw.next();
                    Filter currIdFilter = ff.id(Collections
                            .singleton(ff.featureId(current.getID())));
                    Filter cidToVersion = store.buildVersionedFilter(schema.getTypeName(),
                            currIdFilter, new RevisionInfo(toVersion));
                    DefaultQuery q = new DefaultQuery(schema.getTypeName(), cidToVersion);
                    q.setVersion(toVersion);
                    fr = store.getFeatureReader(q, getTransaction());
                    Feature original = fr.next();
                    for (int i = 0; i < original.getFeatureType().getAttributeCount(); i++) {
                        current.setAttribute(i, original.getAttribute(i));
                    }
                    fr.close();
                    fw.write();
                }
            } catch (IllegalAttributeException iae) {
                throw new DataSourceException("Unexpected error occurred while "
                        + "restoring deleted featues", iae);
            } finally {
                if (fr != null)
                    fr.close();
                if (fw != null)
                    fw.close();
            }
        }

    }

    /**
     * Returns a log of changes performed between fromVersion and toVersion against the features
     * matched by the specified filter.
     * <p>
     * This is equivalent to gathering the ids of features changed between the two versions and
     * matching the filter, getting a list of revision involving those feaures between fromVersion
     * and toVersion, and then query {@link VersionedPostgisDataStore#CHANGESETS} against these
     * revision numbers.
     * 
     * @param fromVersion
     *            the start revision
     * @param toVersion
     *            the end revision, may be null to imply the latest one
     * @param filter
     *            will match features whose log will be reported
     * @return a feature collection of the logs, sorted on revision, descending
     * @throws IOException
     */
    public FeatureCollection getLog(String fromVersion, String toVersion, Filter filter)
            throws IOException {
        RevisionInfo r1 = new RevisionInfo(fromVersion);
        RevisionInfo r2 = new RevisionInfo(toVersion);

        if (r1.revision > r2.revision) {
            // swap them
            RevisionInfo tmpr = r1;
            r1 = r2;
            r1 = tmpr;
            String tmps = toVersion;
            toVersion = fromVersion;
            fromVersion = tmps;
        }

        // We implement this exactly as described. Happily, it seems Postgis does not have
        // sql lentgh limitations. Yet, if would be a lot better if we could encode this
        // as a single sql query with subqueries... (but not all filters are encodable...)
        ModifiedFeatureIds mfids = store.getModifiedFeatureFIDs(schema.getTypeName(), fromVersion,
                toVersion, filter, getTransaction());
        Set ids = new HashSet(mfids.getCreated());
        ids.addAll(mfids.getDeleted());
        ids.addAll(mfids.getModified());

        // no changes?
        if (ids.isEmpty())
            return new EmptyFeatureCollection(schema);

        // Create a filter that sounds like:
        // (revision > r1 and revision <= r2) or (expired > r1 and expired <= r2) and fid in
        // (fidlist)
        FilterFactory ff = CommonFactoryFinder.getFilterFactory(null);
        Filter fidFilter = store.buildFidFilter(ff, ids);
        Filter transformedFidFilter = store.transformFidFilter(schema.getTypeName(), fidFilter);
        Filter revGrR1 = ff.greater(ff.property("revision"), ff.literal(r1.revision));
        Filter revLeR2 = ff.lessOrEqual(ff.property("revision"), ff.literal(r2.revision));
        Filter expGrR1 = ff.greater(ff.property("expired"), ff.literal(r1.revision));
        Filter expLeR2 = ff.lessOrEqual(ff.property("expired"), ff.literal(r2.revision));
        Filter versionFilter = ff.and(transformedFidFilter, ff.or(ff.and(revGrR1, revLeR2), ff.and(
                expGrR1, expLeR2)));

        // We just want the revision and expired, build a query against the real feature type
        DefaultQuery q = new DefaultQuery(schema.getTypeName(), versionFilter, new String[] {
                "revision", "expired" });
        FeatureReader fr = null;
        SortedSet revisions = new TreeSet();
        try {
            fr = store.wrapped.getFeatureReader(q, getTransaction());
            while (fr.hasNext()) {
                Feature f = fr.next();
                Long revision = (Long) f.getAttribute(0);
                if (revision.longValue() > r1.revision)
                    revisions.add(revision);
                Long expired = (Long) f.getAttribute(1);
                if (expired.longValue() != Long.MAX_VALUE && expired.longValue() > r1.revision)
                    revisions.add(expired);
            }
        } catch (Exception e) {
            throw new DataSourceException("Error reading modified revisions from datastore", e);
        } finally {
            if (fr != null)
                fr.close();
        }

        // now, we have a list of revisions between a min and a max
        // let's try to build a fid filter with revisions from the biggest to the smallest
        Set revisionIdSet = new HashSet();
        for (Iterator it = revisions.iterator(); it.hasNext();) {
            Long rev = (Long) it.next();
            revisionIdSet.add(ff.featureId(rev.toString()));
        }
        Filter revisionFilter = ff.id(revisionIdSet);

        // return the changelog
        // TODO: sort on revision descending. Unfortunately, to do so we have to fix fid mappers,
        // so that auto-increment can return revision among the attributes, and at the same
        // time simply allow not include fid attributes in the insert queries (or provide a
        // "default"
        // value for them).
        FeatureStore changesets = (FeatureStore) store
                .getFeatureSource(VersionedPostgisDataStore.CHANGESETS);
        changesets.setTransaction(getTransaction());
        DefaultQuery sq = new DefaultQuery();
        sq.setFilter(revisionFilter);
        sq.setSortBy(new SortBy[] { ff.sort("revision", SortOrder.DESCENDING) });
        return changesets.getFeatures(sq);
    }

    public FeatureDiffReader getDifferences(String fromVersion, String toVersion, Filter filter)
            throws IOException {
        RevisionInfo r1 = new RevisionInfo(fromVersion);
        RevisionInfo r2 = new RevisionInfo(toVersion);

        boolean swap = false;
        if (r1.revision > r2.revision) {
            swap = true;
        }

        // gather modified ids
        ModifiedFeatureIds mfids = store.getModifiedFeatureFIDs(schema.getTypeName(), fromVersion,
                toVersion, filter, getTransaction());

        // build all the filters to gather created, deleted and modified features at the appropriate
        // revisions, depending also on wheter creation/deletion should be swapped or not
        FilterFactory ff = CommonFactoryFinder.getFilterFactory(null);

        // TODO: extract only pk attributes for the delete reader, no need for the others
        FeatureReader createdReader;
        FeatureReader deletedReader;
        if (swap) {
            createdReader = readerFromIdsRevision(ff, mfids.deleted, r2);
            deletedReader = readerFromIdsRevision(ff, mfids.created, r1);
        } else {
            createdReader = readerFromIdsRevision(ff, mfids.created, r2);
            deletedReader = readerFromIdsRevision(ff, mfids.deleted, r1);
        }
        FeatureReader fvReader = readerFromIdsRevision(ff, mfids.modified, r1);
        FeatureReader tvReader = readerFromIdsRevision(ff, mfids.modified, r2);

        VersionedFIDMapper mapper = (VersionedFIDMapper) store.getFIDMapper(schema.getTypeName());
        return new FeatureDiffReader(fromVersion, toVersion, schema, mapper, createdReader,
                deletedReader, fvReader, tvReader);
    }

    /**
     * Returns a feature reader for the specified fids and revision, or null if the fid set is empty
     * 
     * @param ff
     * @param fids
     * @param ri
     * @return
     * @throws IOException
     */
    FeatureReader readerFromIdsRevision(FilterFactory ff, Set fids, RevisionInfo ri)
            throws IOException {
        if (fids != null && !fids.isEmpty()) {
            Filter fidFilter = store.buildFidFilter(ff, fids);
            Filter versionFilter = store.buildVersionedFilter(schema.getTypeName(), fidFilter, ri);
            return store.wrapped.getFeatureReader(new DefaultQuery(schema.getTypeName(),
                    versionFilter), getTransaction());
        } else {
            return null;
        }
    }

}
