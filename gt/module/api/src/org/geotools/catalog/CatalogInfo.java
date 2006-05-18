/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, Geotools Project Managment Committee (PMC)
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
package org.geotools.catalog;

import java.net.URI;


/**
 * Represents a bean style metadata accessor for metadata about a catalog. This
 * may be the result of a request to a metadata service. All methods within an
 * implementation of this interface should NOT block. Much of this is based on
 * Dublin Core and the RDF application profile.
 *
 * @author David Zwiers, Refractions Research
 * @author Justin Deoliveira, The Open Planning Project
 *
 * @since 0.6
 * @source $URL$
 */
public interface CatalogInfo {
    /**
     * returns the catalog title May Not Block.
     *
     * @return
     */
    String getTitle();

    /**
     * returns the keywords assocaited with this catalog May Not Block. Maps to
     * Dublin Core's Subject element
     *
     * @return
     */
    String[] getKeywords();

    /**
     * returns the catalog description.
     *
     * @return
     */
    String getDescription();

    /**
     * Returns the catalog source. May Not Block. Maps to the Dublin Core
     * Server Element
     *
     * @return
     */
    URI getSource();
}
