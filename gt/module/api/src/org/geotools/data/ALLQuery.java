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
/*
 * Created on 15-Mar-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.data;

import org.geotools.filter.Filter;
import org.geotools.filter.SortBy;
import org.geotools.filter.SortBy2;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import java.net.URI;
import java.util.Arrays;


/**
 * Implementation of Query.ALL.
 * 
 * <p>
 * This query is used to retrive all Features. Query.ALL is the only instance
 * of this class.
 * </p>
 * 
 * <p>
 * Example:
 * </p>
 * <pre><code>
 * featureSource.getFeatures( Query.FIDS );
 * </code></pre>
 */
class ALLQuery implements Query {
    public final String[] getPropertyNames() {
        return null;
    }

    public final boolean retrieveAllProperties() {
        return true;
    }

    public final int getMaxFeatures() {
        return DEFAULT_MAX; // consider Integer.MAX_VALUE
    }

    public final Filter getFilter() {
        return Filter.NONE;
    }

    public final String getTypeName() {
        return null;
    }

    public URI getNamespace() {
        return NO_NAMESPACE;
    }

    public final String getHandle() {
        return "Request All Features";
    }

    public final String getVersion() {
        return null;
    }

    /**
     * Hashcode based on propertyName, maxFeatures and filter.
     *
     * @return hascode for filter
     */
    public int hashCode() {
        String[] n = getPropertyNames();

        return ((n == null) ? (-1)
                            : ((n.length == 0) ? 0 : (n.length
        | n[0].hashCode()))) | getMaxFeatures()
        | ((getFilter() == null) ? 0 : getFilter().hashCode())
        | ((getTypeName() == null) ? 0 : getTypeName().hashCode())
        | ((getVersion() == null) ? 0 : getVersion().hashCode())
        | ((getCoordinateSystem() == null) ? 0 : getCoordinateSystem().hashCode())
        | ((getCoordinateSystemReproject() == null) ? 0
                                                    : getCoordinateSystemReproject()
                                                          .hashCode());
    }

    /**
     * Equality based on propertyNames, maxFeatures, filter, typeName and
     * version.
     * 
     * <p>
     * Changing the handle does not change the meaning of the Query.
     * </p>
     *
     * @param obj Other object to compare against
     *
     * @return <code>true</code> if <code>obj</code> matches this filter
     */
    public boolean equals(Object obj) {
        if ((obj == null) || !(obj instanceof Query)) {
            return false;
        }

        if (this == obj) {
            return true;
        }

        Query other = (Query) obj;

        return Arrays.equals(getPropertyNames(), other.getPropertyNames())
        && (retrieveAllProperties() == other.retrieveAllProperties())
        && (getMaxFeatures() == other.getMaxFeatures())
        && ((getFilter() == null) ? (other.getFilter() == null)
                                  : getFilter().equals(other.getFilter()))
        && ((getTypeName() == null) ? (other.getTypeName() == null)
                                    : getTypeName().equals(other.getTypeName()))
        && ((getVersion() == null) ? (other.getVersion() == null)
                                   : getVersion().equals(other.getVersion()))
        && ((getCoordinateSystem() == null)
        ? (other.getCoordinateSystem() == null)
        : getCoordinateSystem().equals(other.getCoordinateSystem()))
        && ((getCoordinateSystemReproject() == null)
        ? (other.getCoordinateSystemReproject() == null)
        : getCoordinateSystemReproject()
              .equals(other.getCoordinateSystemReproject()));
    }

    public String toString() {
        return "Query.ALL";
    }

    /**
     * Return <code>null</code> as ALLQuery does not require a CS.
     *
     * @return <code>null</code> as override is not required.
     *
     * @see org.geotools.data.Query#getCoordinateSystem()
     */
    public CoordinateReferenceSystem getCoordinateSystem() {
        return null;
    }

    /**
     * Return <code>null</code> as ALLQuery does not require a CS.
     *
     * @return <code>null</code> as reprojection is not required.
     *
     * @see org.geotools.data.Query#getCoordinateSystemReproject()
     */
    public CoordinateReferenceSystem getCoordinateSystemReproject() {
        return null;
    }

	public SortBy2[] getSortyBy() {
		return SortBy.UNSORTED;
	}
}
