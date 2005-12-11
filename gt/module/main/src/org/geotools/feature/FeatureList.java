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
package org.geotools.feature;

import java.util.List;

import org.geotools.filter.Filter;


/**
 * An ordered List of Features. 
 * <p>
 * A FeatureList is usually retrived from a FeatureCollection with the
 * subCollection( Filter ) operation. How you ask - make use of Filter
 * 1.1 sortBy.
 * </p>
 * <p>
 * You may check to see if the result of subCollection( Filter ) is
 * a FeatureList using an instanceof check. This often the case,
 * when using not using sortBy the order is usually based on FID.
 * </p>
 *  
 * @author Jody Garnett, Refractions Research, Inc.
 */
public interface FeatureList extends List, FeatureCollection {	
	/**
	 * Similar to subCollection, explicitly constructs a ordered List.
	 * <p>
	 * The list will be ordered:
	 * <ul>
	 * <li>As indicated using Filter 1.1 sortBy
	 * <li>occuring to their appearance in this FeatureList
	 * </ul>
	 * </p>
	 * @param filter
	 * @return FeatureList based on features selected by filter
	 */
    FeatureList subList( Filter filter );    
}