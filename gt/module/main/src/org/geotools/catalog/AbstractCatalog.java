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

import java.util.ArrayList;
import java.util.Iterator;

import org.opengis.catalog.Catalog;
import org.opengis.catalog.CatalogEntry;
import org.opengis.catalog.QueryDefinition;
import org.opengis.catalog.QueryResult;


/**
 * A general superclass that handles queries and iteration through a catalog
 *
 * @author $author$
 * @version $Revision: 1.9 $
 */
public abstract class AbstractCatalog implements Catalog {
    /** A list of CatalogEntries  */
    protected ArrayList entries = new ArrayList();

    /**
     * @see org.opengis.catalog.Catalog#query(org.opengis.catalog.QueryDefinition)
     */
    public QueryResult query(QueryDefinition arg0) {
        DefaultQueryResult result = new DefaultQueryResult();

        for (Iterator iter = iterator(); iter.hasNext();) {
            CatalogEntry element = (CatalogEntry) iter.next();

            if (arg0.accept(element)) {
                result.add(element);
            }
        }

        return result;
    }

    /**
     * @see org.opengis.catalog.Catalog#iterator()
     */
    public Iterator iterator() {
        return entries.iterator();
    }
}
