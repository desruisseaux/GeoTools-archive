/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, GeoTools Project Managment Committee (PMC)
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
package org.geotools.data;

import java.util.List;

import org.opengis.feature.type.TypeName;

/**
 * <i>
 * <p>First draft of a DataService interface based on brain storming session with Jody, Thomas, 
 * Stefan and Cory in Refractions on November 24th. This could become a super set of {@link DataStore} 
 * or eventually replace it(?).</p>
 * 
 * <p>The basic idea is to have simple, general interface to access and query data that is in some way or 
 * another spatially enabled. And we don't want the restriction to {@link org.geotools.feature.Feature}, 
 * {@link org.geotools.feature.FeatureType}, {@link org.geotools.data.FeatureSource}, etc. as we have right 
 * now in {@link org.geotools.data.DataStore}.</p>
 * </i>
 * 
 * @source $URL: http://svn.geotools.org/geotools/trunk/gt/modules/library/api/src/main/java/org/geotools/data/DataStore.java $
 * @version $Id: DataStore.java 22600 2006-11-04 09:37:58Z jgarnett $
 */

public interface DataService {

	/**
	 * Names of types for which data sources are available.
	 *
	 * @return List<TypeName>, may be emtpy, but never null 
	 */
	
	List getTypes();

	/**
	 * Description of content in an appropriate format.
	 * <ul>
	 *   <li>FeatureType: when serving up features</li>
	 *   <li>Class: when providing access to a java domain model</li>
	 *   <li>URL: of XSD document when working with XML document</li>
	 *   <li>etc...</li>
	 * </ul>
	 * 
	 * @return FeatureType, ResultSetMetaData, Class, whatever?
	 */
	
	Object describe( TypeName typeName );
	
	/**
	 * Provides access to the data source for the given type name.
	 * 
	 * @return Data source, null if typeName is not available
	 */
	
	Source access( TypeName typeName );
	
}
