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
package org.geotools.demo.data;

import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureResults;
import org.geotools.data.FeatureSource;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.feature.FeatureType;
import java.io.File;
import java.net.URL;
import javax.swing.JFileChooser;

import org.geotools.filter.FilterFactory;
import org.geotools.filter.LiteralExpression;
import org.geotools.filter.AttributeExpression;
//import org.geotools.data.DefaultQuery;
import org.geotools.filter.BetweenFilter;
import org.geotools.data.Transaction;
import org.geotools.data.FeatureStore;

import org.geotools.data.DataUtilities;
import org.geotools.data.FeatureReader;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.AttributeType;
import org.geotools.feature.FeatureTypeFactory;
import org.geotools.feature.Feature;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.geom.LineString;


/**
 * Basic reading abilities demo: open a file, get the feature type, read the
 * features and output their contents to the standard output
 *
 * @author aaime
 */
public class ShapeReaderWriter {

	private static URL getResource(String path) {
		return ShapeReaderWriter.class.getClassLoader().getResource(path);
	}

	public static void main(String[] args) {
		try {
			// get the shapefile URL by either loading it from the file system
			// or from the classpath
			URL shapeURL = null;
			JFileChooser fileChooser = new JFileChooser("C:\\projects\\Geotools2ShapefileTest\\Shapefiles");
			fileChooser.setFileFilter(new SimpleFileFilter("shp", "Shapefile"));

			int result = fileChooser.showOpenDialog(null);

			if (result == JFileChooser.APPROVE_OPTION) {
				File f = fileChooser.getSelectedFile();
				shapeURL = f.toURL();
			} else {
				shapeURL = getResource("org/geotools/sampleData/statepop.shp");
			}

			if (shapeURL == null) {
				System.err.println("Please specify a shape file.");
				System.exit(-1);
			}

			// get feature results
			ShapefileDataStore store = new ShapefileDataStore(shapeURL);
			String name = store.getTypeNames()[0];
			FeatureSource source = store.getFeatureSource(name);
			FeatureResults fsShape = source.getFeatures();

			// print out total number of features
			System.out.println(fsShape.getCount() + " features found in shapefile.");

			// get feature type to create new shapefile
			FeatureType ft = source.getSchema();

			// create filter to select only features satify 200 >= DI >= 100
			FilterFactory ff = FilterFactory.createFilterFactory();

			LiteralExpression literal200 = ff.createLiteralExpression(200.0);
			LiteralExpression literal100 = ff.createLiteralExpression(100.0);
			AttributeExpression diExpression =
				ff.createAttributeExpression(ft, "DI");

			BetweenFilter betweenFilter = ff.createBetweenFilter();

			betweenFilter.addLeftValue(literal100);
			betweenFilter.addMiddleValue(diExpression);
			betweenFilter.addRightValue(literal200);

			// perform query using the Query interface

			// DefaultQuery diQuery = new DefaultQuery(betweenFilter);
			// FeatureResults fsFilteredShape = source.getFeatures(name, diQuery);

			// alternatively,

			// perform query using the Filter interface directly
			FeatureResults fsFilteredShape = source.getFeatures(betweenFilter);

			System.out.println("Using a filter to query features with an attribute condition 200 >= DI >= 100");
			System.out.println(fsFilteredShape.getCount() + " features returned from query.");
			System.out.println();

			// now print out the feature contents (including geometric attribute)
			FeatureReader filteredReader = fsFilteredShape.reader();

			// generate new shapefile filename by prepending "new_"
			String newStoreURLString =
				shapeURL.toString().substring(0,
					shapeURL.toString().lastIndexOf("/") + 1)
				+ "new_" +
				shapeURL.toString().substring(
					shapeURL.toString().lastIndexOf("/") + 1);

			// create new shapefile data store
			ShapefileDataStore newShapefileDataStore =
				new ShapefileDataStore(new URL(newStoreURLString));

			// create the schema using from the original shapefile
			newShapefileDataStore.createSchema(ft);

			// grab the data source from the new shapefile data store
			FeatureSource newFeatureSource = newShapefileDataStore.
				getFeatureSource(name);

			// downcast FeatureSource to specific implementation of FeatureStore
			FeatureStore newFeatureStore = (FeatureStore)newFeatureSource;

			// accquire a transaction to create the shapefile from FeatureStore
			Transaction t = newFeatureStore.getTransaction();

			// add features got from the query (FeatureReader)
			newFeatureStore.addFeatures(filteredReader);

/*			// the following code appends features to the DataStore
			AttributeType geom = AttributeTypeFactory.
				newAttributeType("the_geom", LineString.class);
			AttributeType roadWidth = AttributeTypeFactory.
				newAttributeType("width", Float.class);
			FeatureType ftRoad = FeatureTypeFactory.newFeatureType(
				new AttributeType[] { geom, roadWidth }, "road");

			WKTReader wktReader = new WKTReader();
			LineString geometry =
				(LineString) wktReader.read("LINESTRING (0 0, 10 10)");
			Float width = new Float(10);
			Feature theRoad = ftRoad.create(
				new Object[] { geometry, width }, "myRoad");

			FeatureReader reader = DataUtilities.reader(
				new Feature[] { theRoad } );
			newFeatureStore.addFeatures(reader);
*/
			// filteredReader is now exhausted and closed, commit the changes
			t.commit();
			t.close();

			System.out.println("Successfully create new shapefile "
				+ newStoreURLString
				+ " with " + fsFilteredShape.getCount()
				+ " features out of " + fsShape.getCount()
				+ " features.");

		} catch (Exception e) {
			System.out.println("Ops! Something went wrong :-(");
			e.printStackTrace();
		}

		System.exit(0);
	}

}
