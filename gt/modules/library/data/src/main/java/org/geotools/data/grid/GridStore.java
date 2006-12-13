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

import org.geotools.data.Store;
import org.opengis.filter.Filter;

/**
 * Allow modification to a GridCoverage.
 * @since 2.4
 * @deprecated This is a Proposal, we need your feedback!
 * @author Jody Garnett, Refractions Research Inc.
 */
public interface GridStore extends GridSource, Store {

    /**
     * This method is still read-only, use modifiableContent() for write access. 
     * 
     * @see #modifiableContent()
     */
    public Collection content();
    /**
     * This method is still read-only, use modifiableContent() for write access. 
     * 
     * @see #modifiableContent( Filter )
     */
    public Collection content( Filter filter );
    /**
     * This method is still read-only, use modifiableContent() for write access. 
     * 
     * @see #modifiableContent( String, String)
     */
    public Collection content( String query, String queryLanguage );
    
    /** Read/Write access to GridCoverage */    
    public Collection modifiableContent();
    
    /** Read/Write access to GridCoverage */    
    public Collection modifiableContent( Filter filter );
    
    /** Read/Write access to GridCoverage */
    public Collection modifiableContent( String filter, String queryLanguage );
}
