/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004, Geotools Project Managment Committee (PMC)
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

package org.geotools.demo.referencing;

//java dependancies
import java.net.URL;
import java.util.Locale;
import java.io.File;

//geotool2 dependencies
import org.geotools.data.DataStore;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureResults;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureWriter;
import org.geotools.data.Transaction;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.cs.CoordinateSystem;
import org.geotools.cs.CoordinateSystemFactory;
import org.geotools.ct.CoordinateTransformation;
import org.geotools.ct.CoordinateTransformationFactory;

//opengis dependancies
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;

//JTS dependancy
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.CoordinateFilter;

/**
 * A simple demo to transform the geometries in a shapefile from the source
 * coordinate system (CS) to a target coordinate system. Source and target
 * CS's are given here as well know text (WKT) strings. The default output path
 * is the users home directory.
 *
 * @version $Id:
 * @author rschulz
 */
public class TransformData {
    
    //hardcoded WKT strings
    private static String SOURCE_WKT = "GEOGCS[\"WGS84\", DATUM[\"WGS84\", SPHEROID[\"WGS84\", 6378137.0, 298.257223563]], PRIMEM[\"Greenwich\", 0.0], UNIT[\"degree\",0.017453292519943295], AXIS[\"Longitude\",EAST], AXIS[\"Latitude\",NORTH]]";
    private static String TARGET_WKT = "PROJCS[\"UTM Zone 14N\", GEOGCS[\"WGS84\", DATUM[\"WGS84\", SPHEROID[\"WGS84\", 6378137.0, 298.257223563]], PRIMEM[\"Greenwich\", 0.0], UNIT[\"degree\",0.017453292519943295], AXIS[\"Longitude\",EAST], AXIS[\"Latitude\",NORTH]], PROJECTION[\"Transverse_Mercator\"], PARAMETER[\"central_meridian\", -99.0], PARAMETER[\"latitude_of_origin\", 0.0], PARAMETER[\"scale_factor\", 0.9996], PARAMETER[\"false_easting\", 500000.0], PARAMETER[\"false_northing\", 0.0], UNIT[\"metre\",1.0], AXIS[\"x\",EAST], AXIS[\"y\",NORTH]]";
    //private static String TARGET_WKT = "PROJCS[\"Lambert\", GEOGCS[\"WGS84\", DATUM[\"WGS84\", SPHEROID[\"WGS84\", 6378137.0, 298.257223563]], PRIMEM[\"Greenwich\", 0.0], UNIT[\"degree\",0.017453292519943295], AXIS[\"Longitude\",EAST], AXIS[\"Latitude\",NORTH]], PROJECTION[\"Lambert_Conformal_Conic_2SP\"], PARAMETER[\"semi_major\", 6378137.0], PARAMETER[\"semi_minor\", 6356752.314245179], PARAMETER[\"central_meridian\", 0.0], PARAMETER[\"standard_parallel_1\", 20.0], PARAMETER[\"standard_parallel_2\", 20.0], PARAMETER[\"latitude_of_origin\", 0.0], PARAMETER[\"false_easting\", 100000.0], PARAMETER[\"false_northing\", 0.0], UNIT[\"metre\",1.0], AXIS[\"x\",EAST], AXIS[\"y\",NORTH]]";

    //private static String WKT_2 = "PROJCS[\"Mercator\", GEOGCS[\"WGS84\", DATUM[\"WGS84\", SPHEROID[\"WGS84\", 6378137.0, 298.257223563]], PRIMEM[\"Greenwich\", 0.0], UNIT[\"degree\",0.017453292519943295], AXIS[\"Longitude\",EAST], AXIS[\"Latitude\",NORTH]], PROJECTION[\"Mercator_2SP\"], PARAMETER[\"semi_major\", 6378137.0], PARAMETER[\"semi_minor\", 6356752.314245179], PARAMETER[\"central_meridian\", 0.0], PARAMETER[\"standard_parallel_1\", 20.0], PARAMETER[\"false_easting\", 0.0], PARAMETER[\"false_northing\", 1000000.0], UNIT[\"metre\",1.0], AXIS[\"x\",EAST], AXIS[\"y\",NORTH]]";
    //private static String WKT_3 = "GEOGCS[\"Sphere\", DATUM[\"WGS84\", SPHEROID[\"WGS84\", 6370997.0, 0],TOWGS84[0,0,0,0,0,0,0]], PRIMEM[\"Greenwich\", 0.0], UNIT[\"degree\",0.017453292519943295], AXIS[\"Longitude\",EAST], AXIS[\"Latitude\",NORTH]]";
    //private static String WKT_4 = "PROJCS[\"TransverseMercator\", GEOGCS[\"Sphere\", DATUM[\"Sphere\", SPHEROID[\"Sphere\", 6370997.0, 0],TOWGS84[0,0,0,0,0,0,0]],PRIMEM[\"Greenwich\", 0.0], UNIT[\"degree\",0.017453292519943295], AXIS[\"Longitude\",EAST], AXIS[\"Latitude\",NORTH]], PROJECTION[\"Transverse_Mercator\"], PARAMETER[\"semi_major\", 6370997], PARAMETER[\"semi_minor\", 6370997], PARAMETER[\"central_meridian\", 0.0], PARAMETER[\"latitude_of_origin\", 0.0], PARAMETER[\"scale_factor\", 1.0], PARAMETER[\"false_easting\", 0.0], PARAMETER[\"false_northing\", 0.0], UNIT[\"metre\",1.0], AXIS[\"x\",EAST], AXIS[\"y\",NORTH]]";

    /** Factory to create coordinate systems from WKT strings*/
    private CoordinateSystemFactory csFactory = CoordinateSystemFactory.getDefault();
    
    /** Factory to create transformations from a source and target CS */
    private CoordinateTransformationFactory ctFactory = CoordinateTransformationFactory.getDefault();
    
    /** Creates a new instance of TransformData */
    public TransformData(URL inURL, URL outURL, String inWKT, String outWKT) {
        try {
            //create the CS's and transformation
            CoordinateSystem inCS = csFactory.createFromWKT(inWKT);
            CoordinateSystem outCS = csFactory.createFromWKT(outWKT);
            System.out.println("source CS: " + inCS.getName(Locale.ENGLISH));
            System.out.println("target CS: " + outCS.getName(Locale.ENGLISH));
            CoordinateTransformation transformation = ctFactory.createFromCoordinateSystems(inCS, outCS);
            System.out.println("transform: " + transformation.getMathTransform().toString());
            CoordinateFilter transFilter = new TransformationCoordinateFilter(transformation.getMathTransform());
            
            //get the input shapefile
            DataStore inStore = new ShapefileDataStore(inURL);
            String name = inStore.getTypeNames()[0];
            FeatureSource inSource = inStore.getFeatureSource(name);
            FeatureResults inResults = inSource.getFeatures();
            FeatureReader inReader = inResults.reader();
            FeatureType inSchema = inSource.getSchema();

            //create the output shapefile
            DataStore outStore = new ShapefileDataStore(outURL);
            Object[] outAttributes = new Object[inSchema.getAttributeCount()];
            outStore.createSchema(inSchema);
            FeatureWriter outFeatureWriter = outStore.getFeatureWriter(outStore.getTypeNames()[0], Transaction.AUTO_COMMIT);
            
            while (inReader.hasNext()) {
                Feature inFeature = inReader.next();
                for (int i = 0; i < inFeature.getNumberOfAttributes(); i++) {
                    Object inAttribute = inFeature.getAttribute(i);
                    if (inAttribute instanceof Geometry) {
                        Geometry geom = (Geometry) inAttribute;
                        geom.apply(transFilter);
                        outAttributes[i] = geom;
                    } else {
                        outAttributes[i] = inAttribute;
                    }
                }
                // Create and write the new feature
                outFeatureWriter.next().setAttributes(outAttributes);
                outFeatureWriter.write();
            }
            
            //close stuff
            inReader.close();
            outFeatureWriter.close();
            
            System.out.println("Done");
        } catch (Exception e){
            System.out.println("Exception: " + e);
            e.printStackTrace();
        }
    }
    
    /**
     * Command line arguments are:
     *   <ul>
     *     <li>in file - input shapefile
     *     <li>out file - output shapefile
     *     <li>in WKT - WKT string for the source coordinate system
     *     <li>out WKT - WKT string for the target coordinate system
     *   </ul>
     *
     * @param args the command line arguments
     * 
     */
    public static void main(String[] args) throws Exception {
        URL inURL, outURL;
        
        if (args.length == 0) {
            inURL = TransformData.class.getClassLoader().getResource("org/geotools/sampleData/statepop.shp");
            outURL = new File(System.getProperty("user.home") + "/statepopTransform.shp").toURL();
            new TransformData(inURL, outURL, SOURCE_WKT, TARGET_WKT);
        } else if (args.length == 2) {
            inURL = new File(args[0]).toURL();
            outURL = new File(args[1]).toURL();
            new TransformData(inURL, outURL, SOURCE_WKT, TARGET_WKT);
        } else if (args.length == 4) {
            inURL = new File(args[0]).toURL();
            outURL = new File(args[1]).toURL();
            new TransformData(inURL, outURL, args[2], args[4]);
        } else {
            System.out.println("Usage: java TransformData <in file> <out file> <in WKT> <out WKT>");
        }
    }
    
}
