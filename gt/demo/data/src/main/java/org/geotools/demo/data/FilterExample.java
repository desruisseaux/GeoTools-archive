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
package org.geotools.demo.data;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.factory.FactoryConfigurationError;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.filter.AttributeExpression;
import org.geotools.filter.Expression;
import org.geotools.filter.Filter;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.FilterFactoryFinder;
import org.geotools.filter.FilterType;
import org.geotools.filter.GeometryFilter;
import org.geotools.filter.LiteralExpression;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

/**
 * Examples of using filter with a provided file.
 * <p>
 * We use the bc_2m_lakes.shp included as test data, and will try out a range of
 * geospatial filters for your amusement.
 * </p>
 * 
 * @author Gearhard
 * @source $URL:
 *         http://svn.geotools.org/geotools/trunk/gt/demo/data/src/org/geotools/demo/data/ShapeReader.java $
 */
public class FilterExample {

	private static URL getResource(String path) {
		return FilterExample.class.getResource(path);
	}

	/** Just run it */
	public static void main(String[] args) {
		try {
			polygonIntersectsPoint();
		} catch (Exception e) {
			System.out.println("Ops! Something went wrong :-(");
			e.printStackTrace();
		}
		System.exit(0);
	}

	private static void polygonIntersectsPoint() throws IOException, FactoryConfigurationError {
		// locate the data
		URL url = getResource("test-data/bc_2m_lakes.shp");

		// set up connection parameters
		Map params = new HashMap();
		params.put("url", url);

		// connect to the data
		DataStore dataStore = DataStoreFinder.getDataStore(params);

		String typeName = dataStore.getTypeNames()[0];
		FeatureSource featureSource = dataStore.getFeatureSource(typeName);

		// create a filter
		FilterFactory filterFactory = FilterFactoryFinder
				.createFilterFactory();

		// look up geometry attribute name used for testing
		//
		String geometryName = featureSource.getSchema()
				.getDefaultGeometry().getName();

		// Create the attribute expression
		AttributeExpression geometryExpression = filterFactory
				.createAttributeExpression(geometryName);

		// Create the point used for testing
		//
		Coordinate pointCoordinate = new Coordinate(-116.883011765104,
				50.0001019506261);

		GeometryFactory geometryFactory = new GeometryFactory();
		Point point = geometryFactory.createPoint(pointCoordinate);

		// Create the literal expression
		LiteralExpression pointExpression = filterFactory
				.createLiteralExpression();
		pointExpression.setLiteral(point);

		// create the actual test
		//
		GeometryFilter withinFilter = filterFactory
		.createGeometryFilter(FilterType.GEOMETRY_INTERSECTS);			
		withinFilter.addLeftGeometry(geometryExpression);
		withinFilter.addRightGeometry(pointExpression);

		// grab features matching the filter
		FeatureCollection featureCollection = featureSource
				.getFeatures(withinFilter);

		// now print out the first 10 features
		//
		Iterator iterator = featureCollection.iterator();
		try {
			int count = 0;
			if (iterator.hasNext()) {
				while (iterator.hasNext()) {
					Feature feature = (Feature) iterator.next();
					System.out.println( feature );
					if ((++count) == 10)
						break; // only 10 please
				}
			} else {
				System.out.println("Point not found in any Polygons.");
			}
		} finally {
			featureCollection.close(iterator);
		}
	}
}
