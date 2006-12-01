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

import java.util.Collection;

import org.opengis.filter.Filter;
import org.opengis.filter.capability.FilterCapabilities;
import org.opengis.feature.type.TypeName;

/**
 * <i>
 * <p>First draft of a Source interface based on brain storming session with Jody, Thomas, 
 * Stefan and Cory in Refractions on November 24th.</p>
 * 
 * <p>The basic idea is to have simple, general interface to access and query data that is in some way or 
 * another spatially enabled. And we don't want the restriction to {@link org.geotools.feature.Feature}, 
 * {@link org.geotools.feature.FeatureType}, {@link org.geotools.data.FeatureSource}, etc. as we have right 
 * now in {@link org.geotools.data.DataStore}.</p>
 * </i>
 * 
 * The <code>Source</code> interface provides access to the actual data either filtered/queried or not. Access 
 * is purely <b>read-only</b> with this interface.
 * 
 * @source $URL$
 * @version $Id$
 */

public interface Source/*<Content,Description>*/ {
	
	/**
	 * Get the corresponding DataService, that created this Source.
	 */
	// Comment this out, if you think it is needed!
//	DataAccess<Content,Description> getDataAccess();
	
	/**
	 * Description of the supported filter capabilities.
	 *
	 * @return Supported filter capabilities
	 */
	
	FilterCapabilities getFilterCapabilities();


	/**
	 * Get the complete data of this <code>Source</code> implementation. No filters or 
	 * queries are applied.
	 * 
	 * @return An immutable Collection, may be empty, but never <code>null</code>
	 */
	
	Collection/*<Content>*/ content();
	
	
	/**
	 * Get the complete data of this <code>Source</code> implementation. No filters are 
	 * applied.
	 * 
	 * @return A immutable Collection, may be empty, but never <code>null</code> 
	 */
	
//	TODO check catalog service web spec. and change param types accordingly
	Collection/*<Content>*/ content( String query, String queryLanguage ); 


	/**
	 * A collection containing all the data indicated by the filter.
	 * 
	 * @return A immutable Collection, may be empty, but never <code>null</code> 
	 */
	
	Collection/*<Content>*/ content( Filter filter );


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

	Object /*Description*/ describe();

	/**
	 * Names of the type this data source provides.
	 *
	 * @return The type name 
	 */

	TypeName getName();
	
	/**
     * Provides a transaction for commit/rollback control of this <code>Source</code>.
	 *
	 * @param t The transaction
	 */
	
	void setTransaction( Transaction t );
	
    /**
     * Clean up any resources, or listeners that made use of this Source of data.
     * <p>
     * Please note this Source will not function after this method is called. Any
     * Transaction.State mementos placed on the current transaction will also be
     * cleaned up (although the transaction itself will not be canceled - as it
     * may be in use by others).
     * </p>
     */ 
	void dispose();
}
