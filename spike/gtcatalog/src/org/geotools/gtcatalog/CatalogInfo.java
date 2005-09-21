/*
 *    uDig - User Friendly Desktop Internet GIS client
 *    http://udig.refractions.net
 *    (C) 2004, Refractions Research Inc.
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
package org.geotools.gtcatalog;

import java.net.URI;

/**
 * Represents a bean style metadata accessor for metadata about a catalog. This may be the result of
 * a request to a metadata service. All methods within an implementation of this interface should
 * NOT block. Much of this is based on Dublin Core and the RDF application profile.
 * 
 * @author David Zwiers, Refractions Research
 * @since 0.6
 */
public class CatalogInfo {
    protected String title, description;
    protected URI source;
    protected String[] keywords;

    protected CatalogInfo() {
        // for sub-classes
    }

    public CatalogInfo( String title, String description, URI source, String[] keywords ) {
        this.title = title;
        this.description = description;
        this.source = source;
        this.keywords = keywords;
    }

    /**
     * returns the catalog title May Not Block.
     * 
     * @return
     */
    public String getTitle() {
        return title;
    }

    /**
     * returns the keywords assocaited with this catalog May Not Block. Maps to Dublin Core's
     * Subject element
     * 
     * @return
     */
    public String[] getKeywords() { // aka Subject
        return keywords;
    }

    /**
     * returns the catalog description.
     * 
     * @return
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the catalog source. May Not Block. Maps to the Dublin Core Server Element
     * 
     * @return
     */
    public URI getSource() { // aka server
        return source;
    }
}
