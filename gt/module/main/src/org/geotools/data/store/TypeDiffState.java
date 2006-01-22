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
package org.geotools.data.store;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.geotools.data.DataSourceException;
import org.geotools.data.DiffFeatureReader;
import org.geotools.data.DiffFeatureWriter;
import org.geotools.data.FeatureEvent;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureWriter;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.data.Transaction.State;
import org.geotools.feature.Feature;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.SimpleFeature;

import com.vividsolutions.jts.geom.Envelope;


/**
 * A Transaction.State that keeps a difference table.
 * <p>
 * This implementation is backed by memory, please feel free to make a scalable
 * implementation backed by a temporary file.
 * </p>
 *
 * @author Jody Garnett, Refractions Research
 */
public class TypeDiffState implements State {

    /** Tranasction this State is opperating against. */
    Transaction transaction;

    /**
     * Map of differences by typeName.
     * 
     * <p>
     * Differences are stored as a Map of Feature by fid, and are reset during
     * a commit() or rollback().
     * </p>
     */
    Map diffMap = new HashMap();

    private ActiveTypeEntry entry;

    public TypeDiffState(ActiveTypeEntry typeEntry) {
        entry = typeEntry;
    }

    public synchronized void setTransaction(Transaction transaction) {
        if (transaction != null) {
            // configure
            this.transaction = transaction;
        } else {
            this.transaction = null;

            if (diffMap != null) {
                diffMap.clear();                
            }
            entry = null;
        }
    }

    public Map diff() throws IOException {
        return diffMap;        
    }

    /**
     * @see org.geotools.data.Transaction.State#addAuthorization(java.lang.String)
     */
    public synchronized void addAuthorization(String AuthID)
        throws IOException {
        // not required for TransactionStateDiff
    }

    /**
     * Will apply differences to store.
     *
     * @see org.geotools.data.Transaction.State#commit()
     */
    public synchronized void commit() throws IOException {
        applyDiff( diffMap );        
    }

    /**
     * Called by commit() to apply one set of diff
     * 
     * <p>
     * diff will be modified as the differneces are applied, If the opperations
     * is successful diff will be empty at the end of this process.
     * </p>
     * 
     * <p>
     * diff can be used to represent the following operations:
     * </p>
     * 
     * <ul>
     * <li>
     * fid|null: represents a fid being removed
     * </li>
     * <li>
     * fid|feature: where fid exists, represents feature modification
     * </li>
     * <li>
     * fid|feature: where fid does not exist, represents feature being modified
     * </li>
     * </ul>
     * 
     *
     * @param typeName typeName being updated
     * @param diff differences to apply to FeatureWriter
     *
     * @throws IOException If the entire diff cannot be writen out
     * @throws DataSourceException If the entire diff cannot be writen out
     */
    void applyDiff(Map diff) throws IOException {
        if (diff.isEmpty()) {
            return;
        }

        FeatureWriter writer = entry.createWriter();
        SimpleFeature feature;
        Feature update;
        String fid;

        try {
            while (writer.hasNext()) {
                feature = (SimpleFeature)writer.next();
                fid = feature.getID();

                if (diff.containsKey(fid)) {
                    update = (Feature) diff.remove(fid);

                    if (update == null) {
                        writer.remove();

                        // notify
                        entry.listenerManager.fireFeaturesChanged( entry.getTypeName(), transaction, feature.getBounds(), true);
                    } else {
                        try {
                            feature.setAttributes(update.getAttributes(null));
                            writer.write();

                            // notify                        
                            Envelope bounds = new Envelope();
                            bounds.expandToInclude(feature.getBounds());
                            bounds.expandToInclude(update.getBounds());
                            entry.listenerManager.fireFeaturesChanged( entry.getTypeName(),
                                transaction, bounds, true);
                        } catch (IllegalAttributeException e) {
                            throw new DataSourceException("Could update " + fid,
                                e);
                        }
                    }
                }
            }

            Feature addedFeature;
            SimpleFeature nextFeature;

            for (Iterator i = diff.values().iterator(); i.hasNext();) {
                addedFeature = (Feature) i.next();
                i.remove();

                if (addedFeature != null) {
                    fid = addedFeature.getID();

                    nextFeature = (SimpleFeature)writer.next();

                    if (nextFeature == null) {
                        throw new DataSourceException("Could not add " + fid);
                    } else {
                        try {
                            nextFeature.setAttributes(addedFeature
                                .getAttributes(null));
                            writer.write();

                            // notify                        
                            entry.listenerManager.fireFeaturesAdded( entry.getTypeName(),transaction, nextFeature.getBounds(), true);
                        } catch (IllegalAttributeException e) {
                            throw new DataSourceException("Could update " + fid,
                                e);
                        }
                    }
                }
            }
        } finally {
            writer.close();
            entry.listenerManager.fireChanged( entry.getTypeName(), transaction, true);
        }
    }

    /**
     * @see org.geotools.data.Transaction.State#rollback()
     */
    public synchronized void rollback() throws IOException {
        diffMap.clear(); // rollback differences
        entry.listenerManager.fireChanged( entry.getTypeName(), transaction, false);        
    }

    /**
     * Convience Method for a Transaction based FeatureReader.
     * 
     * <p>
     * Constructs a DiffFeatureReader that works against this Transaction.
     * </p>
     *
     * @return FeatureReader the mask orgional contents with against the
     *         current Differences recorded by the Tansasction State
     *
     * @throws IOException If typeName is not Manged by this Tansaction State
     */
    public synchronized FeatureReader reader() throws IOException {
        return new DiffFeatureReader( entry.reader( Query.ALL, transaction ), diffMap );
    }

    /**
     * Convience Method for a Transaction based FeatureWriter
     * 
     * <p>
     * Constructs a DiffFeatureWriter that works against this Transaction.
     * </p>
     *
     * @param typeName Type Name to record differences against
     *
     * @return A FeatureWriter that records Differences against a FeatureReader
     *
     * @throws IOException If a FeatureRader could not be constucted to record
     *         differences against
     */
    public synchronized FeatureWriter writer()
        throws IOException {
        Map diff = null;
        FeatureReader reader = entry.createReader();

        return new DiffFeatureWriter(reader, diff) {
            protected void fireNotification(int eventType, Envelope bounds) {
                switch (eventType) {
                case FeatureEvent.FEATURES_ADDED:
                    entry.listenerManager.fireFeaturesAdded( entry.getTypeName(),
                        transaction, bounds, false);

                    break;

                case FeatureEvent.FEATURES_CHANGED:
                    entry.listenerManager.fireFeaturesChanged(entry.getTypeName(),
                        transaction, bounds, false);

                    break;

                case FeatureEvent.FEATURES_REMOVED:
                    entry.listenerManager.fireFeaturesRemoved(entry.getTypeName(),
                        transaction, bounds, false);

                    break;
                }
            }
        };
    }
}
