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
package org.geotools.data.store;

import org.geotools.catalog.GeoResourceInfo;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureList;
import org.opengis.feature.type.TypeName;
import org.opengis.filter.Filter;
import org.opengis.filter.sort.SortBy;

/**
 * Class is used to provide data access for ContentDataStore.
 *
 * @author Jody Garnett, Refractions Research Inc.
 */
public abstract class Content {
    
    /**
     * Summary information, providing access to such metadata as is available.
     * <p>
     * Metadata is usually available in file headers, table information
     * or data descriptors.
     */
    public abstract GeoResourceInfo info( ContentState state );
    
    /**
     * Produce an entry representing the provided typeName.
     * @param dataStore
     * @param typeName
     * @return entry representing the provided typeName
     */
    public abstract ContentEntry entry( ContentDataStore dataStore, TypeName typeName );
    
    /**
     * Track per transaction state.
     * 
     * @param entry
     * @return per transaction state.
     */
    public abstract ContentState state( ContentEntry entry );
    
    /**
     * FeatureCollection representing the entire contents.
     * <p>
     * Available via getFeatureSource():
     * <ul>
     * <li>getFeatures()
     * <li>getFeatures( Filter.INCLUDES )
     * </ul>
     * 
     * @param state
     * @return all content
     */
    public abstract FeatureCollection all( ContentState state );
    
    /**
     * FeatureCollection representing a subset of available content.
     * <p>
     * Available via getFeatureSource():
     * <ul>
     * <li>getFeatures().filter( filter )
     * <li>getFeatures( filter )
     * </ul>
     * @param state
     * @param filter
     * @return subset of content
     */
    public abstract FeatureCollection filter( ContentState state, Filter filter );
    
    /**
     * FeatureList representing sorted content.
     * <p>
     * Available via getFeatureSource():
     * <ul>
     * <li>getFeatures().sort( sort )
     * <li>getFeatures( filter ).sort( sort )
     * </ul>
     * @param state
     * @param filter
     * @return subset of content
     */
    public abstract FeatureList sorted( ContentState state, Filter filter, SortBy sort );
    
    /**
     * FeatureCollection optimized for read-only access.
     * <p>
     * Available via getView( filter ):
     * <ul>
     * <li>getFeatures().sort( sort )
     * <li>getFeatures( filter ).sort( sort )
     * </ul>
     * <p>
     * In particular this method of data access is intended for rendering and other high speed
     * operations; care should be taken to optimize the use of FeatureVisitor.
     * <p>
     * @param state
     * @param filter
     * @return readonly access
     */    
    public abstract FeatureCollection readonly( ContentState state, Filter filter );
}