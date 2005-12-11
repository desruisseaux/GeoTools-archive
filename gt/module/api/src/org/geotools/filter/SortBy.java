/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2005, Geotools Project Managment Committee (PMC)
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
package org.geotools.filter;

/**
 * Defines the sort order, based on a property and assending/desending.
 * <p>
 * Having SortBy at the Filter level is an interesting undertaking of Filter 1.1
 * support. Why you ask? It is at the Same level as Filter, it is not *used* by
 * Filter itself. The services that make use of Filter, such as WFS are starting
 * to make use of SortBy in the same breath.
 * </p>
 * <p>
 * Where is SortBy used:
 * <ul>
 * <li>WFS 1.1 Query
 * <li>CSW 2.0.1 AbstractQuery
 * </ul>
 * There may be more ...
 * </p>
 * <p>
 * What this means is that the GeoTools Query will make use of this
 * construct. As for sorting the result of an expression (where an
 * expression matches more then one element), we will splice it in to
 * AttributeExpression as an optional parameter. Note function is defined
 * to return a single value (so we don't need to worry there).
 * </p> 
 * @see http://schemas.opengis.net/filter/1.1.0/sort.xsd
 * @see http://schemas.opengis.net/wfs/1.1.0/wfs.xsd
 * 
 * @since GeoTools 2.2, Filter 1.1
 * @author Jody Garnett, Refractions Research, Inc.
 */
public interface SortBy {
	public static final SortBy2[] UNSORTED = new SortBy2[0];	
	/**
	 * Indicate property to sort by, specification is limited to PropertyName.
	 * <p>
	 * Not sure if this is allowed to be a xPath expression?
	 * <ul>
	 * <li>It would be consist with our use in GeoTools
	 * <li>It would not seem to agree with the short hand notation
	 *     used by WFS1.1 (ie. "year A, month A, day A" )
	 * </ul>
	 * </p>
	 * @return Name of property to sort by.
	 */
	public AttributeExpression getPropertyName();	
	
	/**
	 * Indicate property.
	 *   
	 * @param name
	 */
	public void setPropertyName( AttributeExpression name );
	
	/**
	 * The the sort order - one of ASC or DESC.
	 * 
	 * @return ASC or DESC.
	 */
	public SortOrder getSortOrderType();
	
	/**
	 * Set the sort order - to ASC or DESC.
	 *  
	 * @param order ASC or DESC.
	 */
	public void setSortOrder( SortOrder order);	
}
