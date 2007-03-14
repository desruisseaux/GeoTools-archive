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

import java.util.Collections;
import java.util.Map;

import org.geotools.feature.Feature;

/**
 * Represents the difference between two states of the same feature.
 * 
 * @author aaime
 * @since 2.4
 */
public class FeatureDiff {
    /**
     * Feature exists in both versions, but has been modified
     */
    public static final int UPDATED = 0;

    /**
     * Feature does not exists in fromVersion, has been created in the meantime
     * (change map contains all attributes in this case)
     */
    public static final int INSERTED = 1;

    /**
     * Feature existed in fromVersion, but has been deleted (change map is
     * empty)
     */
    public static final int DELETED = 2;

    String ID;

    int state;

    Map changes;
    
    Feature feature;

    /**
     * Creates a new feature difference for a deleted feature
     * 
     * @param ID
     * @param fromVersion
     * @param toVersion
     * @param state
     * @param changes
     */
    FeatureDiff(String ID) {
        super();
        this.ID = ID;
        this.state = DELETED;
        this.changes = Collections.EMPTY_MAP;
    }
    
    /**
     * Creates a new feature difference for a modified feature
     * 
     * @param ID
     * @param fromVersion
     * @param toVersion
     * @param state
     * @param changes
     */
    FeatureDiff(String ID, Map changes) {
        super();
        this.ID = ID;
        this.state = UPDATED;
        this.changes = Collections.unmodifiableMap(changes);
    }
    
    /**
     * Creates a new feature difference
     * 
     * @param ID
     * @param fromVersion
     * @param toVersion
     * @param state
     * @param changes
     */
    FeatureDiff(Feature feature) {
        super();
        this.ID = feature.getID();
        this.state = INSERTED;
        this.feature = feature;
        this.changes = Collections.EMPTY_MAP;
    }

    /**
     * Returns a map of changes, from attribute name to the value at toVersion,
     * if state is {@link #UPDATED}, an empty map otherwise
     * 
     * @return
     */
    public Map getChanges() {
        return changes;
    }

    /**
     * The feature ID
     * 
     * @return
     */
    public String getID() {
        return ID;
    }

    /**
     * The type of difference, either::
     * <ul>
     * <li>{@link #UPDATED}</li>
     * <li>{@link #INSERTED}</li>
     * <li>{@link #DELETED}</li>
     * </ul>
     * 
     * @return
     */
    public int getState() {
        return state;
    }
    
    /**
     * Returns the inserted feature, if the state is {@link #INSERTED}, null otherwise
     * @return
     */
    public Feature getFeature() {
        return feature;
    }

}
