/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2005-2006, GeoTools Project Managment Committee (PMC)
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

import org.opengis.filter.sort.SortBy;
import org.opengis.util.CodeList;
import java.util.ArrayList;
import java.util.List;


/**
 * Captures the SortBy order, ASC or DESC.
 *
 * @deprecated Please use org.opengis.filter.sort.SortOrder
 *
 * @see <a href="http://schemas.opengis.net/filter/1.1.0/sort.xsd">GeoAPI Filter sort.xsd</a>
 * @author Jody Garnett, Refractions Research.
 * @since GeoTools 2.2, Filter 1.1
 * @source $URL$
 */
public final class SortOrder extends Object {
    /** Can now use the geoapi SortOrder directly */
    public static final org.opengis.filter.sort.SortOrder ASCENDING = org.opengis.filter.sort.SortOrder.ASCENDING;

    /** Can now use the geoapi SortOrder directly */
    public static final org.opengis.filter.sort.SortOrder DESCENDING = org.opengis.filter.sort.SortOrder.DESCENDING;
}
