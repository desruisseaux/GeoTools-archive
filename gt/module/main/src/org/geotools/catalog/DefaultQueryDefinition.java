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

import java.io.IOException;
import java.util.Iterator;

import org.opengis.catalog.CatalogEntry;
import org.opengis.catalog.MetadataEntity;
import org.opengis.catalog.QueryDefinition;

/**
 * Wraps a query and applies it to the metadata contained by catalogentries.
 *
 * @author $author$
 * @version $Revision: 1.9 $
 */
public class DefaultQueryDefinition implements QueryDefinition {
    private QueryRequest query;

    /**
     * Creates a new DefaultQueryDefinition object.
     *
     * @param query Query to be used to filter catalog entries
     */
    public DefaultQueryDefinition(QueryRequest queryRequest) {
        this.query = queryRequest;
    }

    /**
     * @see opengis.catalog.QueryDefinition#evaluate(org.geotools.catalog.CatalogEntry)
     */
    public boolean accept(CatalogEntry entry) {
        for (Iterator iter = entry.iterator(); iter.hasNext();) {
            MetadataEntity element = (MetadataEntity) iter.next();
            if (query.match(element)) {
                return true;
            }            
        }
        return false;
    }
}
