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

import java.io.File;
import java.net.URL;

import javax.swing.JFileChooser;

import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureResults;
import org.geotools.data.FeatureSource;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.feature.AttributeType;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;

import com.vividsolutions.jts.geom.Geometry;


/**
 * Basic reading abilities demo: open a file, get the feature type, read the
 * features and output their contents to the standard output
 *
 * @author aaime
 * @source $URL$
 */
public class ShapeReader {
    private static URL getResource(String path) {
        return ShapeReader.class.getClassLoader().getResource(path);
    }

    public static void main(String[] args) {
        try {
            // get the shapefile URL by either loading it from the file system
            // or from the classpath
            URL shapeURL = null;
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new SimpleFileFilter("shp", "Shapefile"));

            int result = fileChooser.showOpenDialog(null);

            if (result == JFileChooser.APPROVE_OPTION) {
                File f = fileChooser.getSelectedFile();
                shapeURL = f.toURL();
            } else {
                shapeURL = getResource("org/geotools/sampleData/statepop.shp");
            }

            // get feature results
            ShapefileDataStore store = new ShapefileDataStore(shapeURL);
            String name = store.getTypeNames()[0];
            FeatureSource source = store.getFeatureSource(name);
            FeatureResults fsShape = source.getFeatures();

            // print out a feature type header and wait for user input
            FeatureType ft = source.getSchema();
            System.out.println("FID\t");

            for (int i = 0; i < ft.getAttributeCount(); i++) {
                AttributeType at = ft.getAttributeType(i);

                if (!Geometry.class.isAssignableFrom(at.getType())) {
                    System.out.print(at.getType().getName() + "\t");
                }
            }

            System.out.println();

            for (int i = 0; i < ft.getAttributeCount(); i++) {
                AttributeType at = ft.getAttributeType(i);

                if (!Geometry.class.isAssignableFrom(at.getType())) {
                    System.out.print(at.getName() + "\t");
                }
            }

            System.out.println();

            // now print out the feature contents (every non geometric attribute)
            FeatureReader reader = fsShape.reader();

            while (reader.hasNext()) {
                Feature feature = reader.next();
                System.out.print(feature.getID() + "\t");

                for (int i = 0; i < feature.getNumberOfAttributes(); i++) {
                    Object attribute = feature.getAttribute(i);

                    if (!(attribute instanceof Geometry)) {
                        System.out.print(attribute + "\t");
                    }
                }

                System.out.println();
            }

            reader.close();
            System.out.println();
            System.out.println();
            System.out.println();

            // and finally print out every geometry in wkt format
            reader = fsShape.reader();

            while (reader.hasNext()) {
                Feature feature = reader.next();
                System.out.print(feature.getID() + "\t");
                System.out.println(feature.getDefaultGeometry());
                System.out.println();
            }

            reader.close();
        } catch (Exception e) {
            System.out.println("Ops! Something went wrong :-(");
            e.printStackTrace();
        }

        System.exit(0);
    }
}
