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
package org.geotools.catalog;

import org.geotools.metadata.Metadata;
import org.geotools.metadata.Query;
import java.io.IOException;
import java.util.Iterator;


/**
 * Wraps a query and applies it to the metadata contained by catalogentries.
 *
 * @author $author$
 * @version $Revision: 1.9 $
 */
public class DefaultQueryDefinition implements QueryDefinition {
    private Query query;

    /**
     * Creates a new DefaultQueryDefinition object.
     *
     * @param query Query to be used to filter catalog entries
     */
    public DefaultQueryDefinition(Query query) {
        this.query = query;
    }

    /**
     * @see org.geotools.catalog.QueryDefinition#evaluate(org.geotools.catalog.CatalogEntry)
     */
    public boolean accept(CatalogEntry entry) {
        for (Iterator iter = entry.iterator(); iter.hasNext();) {
            Metadata element = (Metadata) iter.next();

            try {
                if (query.accepts(element)) {
                    return true;
                }
            } catch (IOException ie) {
                /* future versions may actually throw exceptions but the current version doesn't so
                 * so check the method call: query.filter(fakeFeature) in the method Query.accepts()
                 * (if it still exists)
                 */
                throw new RuntimeException(
                    "See the Query.accepts()::filter.filter(fakeFeature).  It is not expected to throw an exception...but maybe will one day.");
            }
        }

        return false;
    }
}
