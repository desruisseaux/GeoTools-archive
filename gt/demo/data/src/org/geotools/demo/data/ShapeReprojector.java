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

import org.geotools.cs.GeographicCoordinateSystem;
import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureStore;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.opengis.referencing.crs.CoordinateReferenceSystem;


/**
 * Basic reading abilities demo: open a file, get the feature type, read the
 * features and output their contents to the standard output
 *
 * @author aaime
 */
public class ShapeReprojector {
    private static URL getResource(String path) {
        return ShapeReprojector.class.getClassLoader().getResource(path);
    }

    public static void main(String[] args) {
        try {
            // get the shapefile URL by either loading it from the file system
            // or from the classpath
            URL shapeURL = null;
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Choose a shapefile, or press cancel to use the default one");
            fileChooser.setFileFilter(new SimpleFileFilter("shp", "Shapefile"));

            int result = fileChooser.showOpenDialog(null);

            if (result == JFileChooser.APPROVE_OPTION) {
                File f = fileChooser.getSelectedFile();
                shapeURL = f.toURL();
            } else {
                shapeURL = getResource("org/geotools/sampleData/statepop.shp");
            }

            
            
            // get a origin CRS, let's say that data are expressed in WGS84
            // if you want a list of codes, please look into the epsg.properties file
            // included in the EPSG module
            // Let's also create an auto crs based on the UTM with the standard parallel and meridian
            // as the equator and Greenwich 

            CoordinateReferenceSystem originCrs = GeographicCoordinateSystem.WGS84; // crsService.createCRS("EPSG:4326");
            CoordinateReferenceSystem destCrs = crsService.createCRS("AUTO:42001,0.0,0.0");
            
            System.out.println("Origin CRS: " + originCrs);
            System.out.println("Destination CRS: " + destCrs);
            
            // since we assume the data does not include a CRS, we need to force one, and
            // then ask for reprojection
            ShapefileDataStore store = new ShapefileDataStore(shapeURL);
            String name = store.getTypeNames()[0];
            DefaultQuery q = new DefaultQuery(name);
            q.setCoordinateSystem(originCrs);
            q.setCoordinateSystemReproject(destCrs);
            FeatureSource reprojectedSource = store.getView(q);
            
            // now we need to write out the reprojected features. 
            // first ask the user where to save data
            result = fileChooser.showSaveDialog(null);
            
            if (result != JFileChooser.APPROVE_OPTION)
            	return;

            File f = fileChooser.getSelectedFile();
            if(!f.getName().toLowerCase().endsWith(".shp")) {
            	f = new File(f.getAbsolutePath() + ".shp");
            }
            
            // then create the destination data store and write them all to
            // to the disk
            ShapefileDataStore dest = new ShapefileDataStore(f.toURL());
            dest.createSchema(reprojectedSource.getSchema());
            FeatureStore writer = (FeatureStore) dest.getFeatureSource();
            writer.setFeatures(reprojectedSource.getFeatures().reader());
            
            System.out.println("Reprojected shapefile " + f.getAbsolutePath() + " successfully written");
        } catch (Exception e) {
            System.out.println("Ops! Something went wrong :-(");
            e.printStackTrace();
        }

        System.exit(0);
    }
}
