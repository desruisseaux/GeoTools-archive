/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2005, Geotools Project Managment Committee (PMC)
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
package org.geotools.filter;

import org.geotools.feature.Feature;
import java.util.Collection;


/**
 * Defines a feature ID filter, which holds a list of feature IDs. This filter
 * stores a series of feature IDs, which are used to distinguish features
 * uniquely.
 *
 * @author Rob Hranac, TOPP
 * @version $Id: FidFilter.java,v 1.6 2004/02/20 00:19:13 seangeo Exp $
 */
public interface FidFilter extends Filter {
    /**
     * Determines whether or not the given feature's ID matches this filter.
     *
     * @param feature Specified feature to examine.
     *
     * @return <tt>true</tt> if the feature's ID matches an fid held by this
     *         filter, <tt>false</tt> otherwise.
     */
    boolean contains(Feature feature);

    /**
     * Adds a feature ID to the filter.
     *
     * @param fid A single feature ID.
     */
    void addFid(String fid);

    /**
     * Returns all the fids in this filter.
     *
     * @return An array of all the fids in this filter.
     */
    String[] getFids();

    /**
     * Adds a collection of feature IDs to the filter.
     *
     * @param fidsToAdd A collection of feature IDs.
     */
    void addAllFids(Collection fidsToAdd);

    /**
     * Removes a collection of feature IDs from the filter.
     *
     * @param fidsToRemove A collection of feature IDs.
     */
    void removeAllFids(Collection fidsToRemove);

    /**
     * Removes a feature ID from the filter.
     *
     * @param fid A single feature ID.
     */
    void removeFid(String fid);
}
