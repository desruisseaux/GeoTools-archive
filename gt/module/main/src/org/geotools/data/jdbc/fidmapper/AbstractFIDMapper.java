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
 * 26-may-2005 D. Adler Move returnFIDColumnsAsAttributes here
 *                      from subclasses.
 */
package org.geotools.data.jdbc.fidmapper;

/**
 * Abstract implementation providing common methods that usually are coded the
 * same way in all fid mappers.
 *
 * @author wolf
 * @source $URL$
 */
public abstract class AbstractFIDMapper implements FIDMapper {
    /** Set if table FID columns are to be returned as business attributes. */
    protected boolean returnFIDColumnsAsAttributes = false;

    /**
     * @see org.geotools.data.jdbc.fidmapper.FIDMapper#hasAutoIncrementColumns()
     */
    public boolean hasAutoIncrementColumns() {
        for (int i = 0; i < getColumnCount(); i++) {
            if (isAutoIncrement(i)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Convenience since most FID mappers should be persistent, override on the
     * specific ones that aren't.
     *
     * @see org.geotools.data.jdbc.fidmapper.FIDMapper#isVolatile()
     */
    public boolean isVolatile() {
        return true;
    }

    /**
     * @see org.geotools.data.jdbc.fidmapper.FIDMapper#returnFIDColumnsAsAttributes()
     */
    public boolean returnFIDColumnsAsAttributes() {
        return returnFIDColumnsAsAttributes;
    }
}
