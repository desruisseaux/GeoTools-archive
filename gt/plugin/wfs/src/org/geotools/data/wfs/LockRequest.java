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
 * Created on Sep 15, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.data.wfs;

import org.geotools.data.FeatureLock;
import org.geotools.filter.Filter;
import java.util.Map;


/**
 * DOCUMENT ME!
 *
 * @author polio TODO To change the template for this generated type comment go
 *         to Window - Preferences - Java - Code Style - Code Templates
 */
public class LockRequest implements FeatureLock {
    private long duration = 0;
    private String[] types = null;
    private Filter[] filters = null;
    private String lockId = null;

    private LockRequest() {
    }

    protected LockRequest(long duration, Map dataSets) {
        this.duration = duration;
        types = (String[]) dataSets.keySet().toArray(new String[dataSets.size()]);
        filters = new Filter[types.length];

        for (int i = 0; i < types.length; i++)
            filters[i] = (Filter) dataSets.get(types[i]);
    }

    protected LockRequest(long duration, String[] types, Filter[] filters) {
        this.duration = duration;
        this.types = types;
        this.filters = filters;
    }

    /* (non-Javadoc)
     * @see org.geotools.data.FeatureLock#getAuthorization()
     */
    public String getAuthorization() {
        return lockId;
    }

    protected void setAuthorization(String auth) {
        lockId = auth;
    }

    /* (non-Javadoc)
     * @see org.geotools.data.FeatureLock#getDuration()
     */
    public long getDuration() {
        return duration;
    }

    public String[] getTypeNames() {
        return types;
    }

    public Filter[] getFilters() {
        return filters;
    }
}
