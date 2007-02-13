/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2006, GeoTools Project Managment Committee (PMC)
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
package org.geotools.data.grid;

import java.util.Collection;

import org.geotools.catalog.GeoResourceInfo;
import org.geotools.data.Source;
import org.geotools.data.Transaction;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;
import org.opengis.filter.capability.FilterCapabilities;

/**
 * Allows readonly access to a single GridCoverage.
 * 
 * @since 2.4
 * @deprecated This is a Proposal, we need your feedback!
 * @author Jody Garnett, Refractions Research Inc.
 */
public interface GridSource extends Source {

    public Name getName();

    /**
     * Human readible description of content (title, icon, etc...).
     * 
     * @return Information about content
     */
    GeoResourceInfo getInfo();
    
    /**
     * Direct description about content, often from content header.
     * 
     * @see GridAccess#describe(org.opengis.feature.type.TypeName)
     * @return GridCoverageDescription
     */
    public Object describe();

    /**
     * Description of the subset of filter you can use when
     * selecting content.
     */
    public FilterCapabilities getFilterCapabilities();
    
    /**
     * Provides independence between sessions.
     * <p>
     * Even though this is a read-only interface we need the transaction
     * in order to perform read-only operations on a session that is undergoing
     * change (an example would be providing a visualization of a grid coverage
     * as it is convolved).
     */
    public void setTransaction( Transaction t );
    
    /**
     * Retrive all content, read-only.
     * <p>
     * Collection of (one or more in the case of tiles) GridCoverage. If we are cool the collection
     * can be spatial indexed as per FeatureCollection.subCollection( filter ) example.
     * </p>
     * 
     * @return Collection<GridCoverage> read-only
     */
    public Collection/** <GridCoverage> */
    content();

    /**
     * Retrive indicated content, read-only.
     * <p>
     * If we are cool the collection can be spatial indexed as per FeatureCollection.subCollection(
     * filter ) example.
     * </p>
     * 
     * @return Collection<GridCoverage> read-only
     */
    public Collection content( Filter filter );

    /**
     * Retrive indicated content, read-only.
     */
    public Collection content( String query, String queryLanguage );

    /**
     * Clean up any cached content, or if only user file channel.
     */
    public void dispose();
    
}
