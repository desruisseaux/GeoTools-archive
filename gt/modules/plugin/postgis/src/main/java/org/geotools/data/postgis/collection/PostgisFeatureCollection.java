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
package org.geotools.data.postgis.collection;

import java.io.IOException;

import org.geotools.data.Query;
import org.geotools.data.jdbc.JDBCFeatureCollection;
import org.geotools.data.jdbc.JDBCFeatureSource;

/**
 * FeatureCollection<SimpleFeatureType, SimpleFeature> for PostGIS datastores. If we'd like to optimize PostGIS
 * any further than JDBCFeatureCollection, we can override methods within this
 * subclass. Even though we aren't overriding any methods, we should use this
 * class in case we do optimizations for PostGIS in the future.
 * 
 * @author Cory Horner, Refractions Research
 * 
 * @source $URL$
 */
public class PostgisFeatureCollection extends JDBCFeatureCollection  {

	public PostgisFeatureCollection(JDBCFeatureSource arg0, Query arg1) throws IOException  {
		super(arg0, arg1);
	}
	
}
