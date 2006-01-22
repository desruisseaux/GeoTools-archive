/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) Copyright IBM Corporation, 2005. All rights reserved.
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
package org.geotools.data.db2;

import org.geotools.data.jdbc.fidmapper.NullFIDMapper;
import java.util.logging.Logger;


/**
 * Overrides NullFIDMapper methods for DB2-specific handling.
 *
 * @author David Adler - IBM Corporation
 * @source $URL$
 */
public class DB2NullFIDMapper extends NullFIDMapper {
    private static final Logger LOGGER = Logger.getLogger(
            "org.geotools.data.db2");
    private int currentFID = 1;

    /**
     * Default constructor.
     */
    public DB2NullFIDMapper() {
        super();
    }

    /**
     * @see org.geotools.data.jdbc.fidmapper.FIDMapper#getID(java.lang.Object[])
     */
    public String getID(Object[] attributes) {
        currentFID++;

        return String.valueOf(currentFID);
    }
}
