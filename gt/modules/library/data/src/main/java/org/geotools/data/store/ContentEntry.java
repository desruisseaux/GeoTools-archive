/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2006, GeoTools Project Managment Committee (PMC)
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
package org.geotools.data.store;

import java.util.HashMap;
import java.util.Map;
import org.opengis.feature.type.Name;
import org.geotools.data.Transaction;


/**
 * An entry for a type provided by a datastore.
 * <p>
 * A content entry maintains the "state" of an entry for a particular
 * transaction.
 * </p>
 *
 * @author Jody Garnett, Refractions Research Inc.
 * @author Justin Deoliveira, The Open Planning Project
 */
public final class ContentEntry {
    /**
     * Qualified name of the entry.
     */
    Name typeName;

    /**
     * Map<Transaction,ContentState> state according to Transaction.
     */
    Map state;

    /**
     * Backpointer to datastore
     */
    ContentDataStore dataStore;

    public ContentEntry(ContentDataStore dataStore, Name typeName) {
        this.typeName = typeName;
        this.dataStore = dataStore;

        this.state = new HashMap();

        //create a state for the auto commit transaction
        ContentState autoState = dataStore.createContentState(null);
        this.state.put(Transaction.AUTO_COMMIT, autoState);
    }

    /**
     * Qualified name of the entry.
     */
    public Name getName() {
        return typeName;
    }

    /**
     * Unqualified name of the entry.
     * <p>
     * Equivalent to: <code>getName().getLocalPart()</code>.
     * </p>
     */
    public String getTypeName() {
        return typeName.getLocalPart();
    }

    /**
     * Backpointer to datastore.
     */
    public ContentDataStore getDataStore() {
        return dataStore;
    }

    /**
     * Returns state for the entry for a particular transaction.
     * <p>
     * In the event that no state exists for the supplied transaction one will
     * be created by copying the state of {@link Transaction#AUTO_COMMIT}.
     * </p>
     * @param transaction A transaction.
     *
     * @return The state for the transaction.
     */
    public ContentState getState(Transaction transaction) {
        if (state.containsKey(transaction)) {
            return (ContentState) state.get(transaction);
        } else {
            ContentState auto = (ContentState) state.get(Transaction.AUTO_COMMIT);
            ContentState copy = (ContentState) auto.copy();
            state.put(transaction, copy);

            return copy;
        }
    }

    public String toString() {
        return getTypeName();
    }
}
