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
package org.geotools.data.wfs;

import org.geotools.filter.FidFilter;


/**
 * DOCUMENT ME!
 *
 * @author dzwiers
 * @source $URL$
 */
public class LockResult {
    protected String lockId;
    protected FidFilter supported;
    protected FidFilter notSupported;

    private LockResult() {
        // should not be used
    }

    /**
     * 
     * @param lockId
     * @param supported
     * @param notSupported
     */
    public LockResult(String lockId, FidFilter supported, FidFilter notSupported) {
        this.lockId = lockId;
        this.supported = supported;
        this.notSupported = notSupported;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the lockId.
     */
    public String getLockId() {
        return lockId;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the notSupported.
     */
    public FidFilter getNotSupported() {
        return notSupported;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the supported.
     */
    public FidFilter getSupported() {
        return supported;
    }
}
