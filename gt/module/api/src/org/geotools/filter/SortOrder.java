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

import java.util.ArrayList;
import java.util.List;

import org.opengis.util.CodeList;

/**
 * Captures the SortBy order, ASC or DESC.
 * 
 * @see http://schemas.opengis.net/filter/1.1.0/sort.xsd
 * @author Jody Garnett, Refractions Research.
 * @since GeoTools 2.2, Filter 1.1
 * @source $URL$
 */
public final class SortOrder extends CodeList {
	private static final long serialVersionUID = 7840334200112859571L;
	private static final List all = new ArrayList(2);

	/**
	 * Represents acending order.
	 * <p>
	 * Note this has the string representation of ASC to agree
	 * with the Filter 1.1 specification.
	 * </p>
	 */
	public static final SortOrder ASCENDING  = new SortOrder("ASC");
	
	/**
	 * Represents descending order.
	 * <p>
	 * Note this has the string representation of DESC to agree
	 * with the Filter 1.1 specification.
	 * </p> 
	 */	
	public static final SortOrder DESCENDING = new SortOrder("DESC");
	
	private SortOrder( String name ){
		super(name, all );
	}
			
	public CodeList[] family() {
		return (CodeList[]) all.toArray( new CodeList[ all.size()]);
	}	
}
