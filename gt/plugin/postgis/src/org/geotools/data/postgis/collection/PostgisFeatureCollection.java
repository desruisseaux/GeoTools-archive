package org.geotools.data.postgis.collection;

import org.geotools.data.Query;
import org.geotools.data.jdbc.JDBCFeatureCollection;
import org.geotools.data.jdbc.JDBCFeatureSource;

/**
 * FeatureCollection for PostGIS datastores. If we'd like to optimize PostGIS
 * any further than JDBCFeatureCollection, we can override methods within this
 * subclass. Even though we aren't overriding any methods, we should use this
 * class in case we do optimizations for PostGIS in the future.
 * 
 * @author Cory Horner, Refractions Research
 * 
 */
public class PostgisFeatureCollection extends JDBCFeatureCollection  {

	public PostgisFeatureCollection(JDBCFeatureSource arg0, Query arg1) {
		super(arg0, arg1);
	}
	
}
