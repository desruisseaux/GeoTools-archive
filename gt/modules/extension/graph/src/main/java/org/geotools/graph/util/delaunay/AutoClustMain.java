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
package org.geotools.graph.util.delaunay;

import java.awt.Dimension;
import java.io.FileReader;
import java.util.logging.Logger;
import javax.swing.JFrame;
import org.geotools.feature.FeatureCollection;
import org.geotools.graph.structure.Graph;

/**
 *
 * @author jfc173
 */
public class AutoClustMain {
    
    private static final Logger LOGGER = Logger.getLogger("org.geotools.graph");
    
    /** Creates a new instance of AutoClustMain */
    public AutoClustMain() {
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        boolean useRandom = true;
        DelaunayTriangulator triangulator = new DelaunayTriangulator();
//        if (useRandom){
            triangulator.setNodeArray(DelaunayTest.createRandomNodes(250, 180, 180, 100));
 //       } else {
 //           triangulator.setFeatureCollection(loadCSVData());
 //       }
        Graph triangulation = triangulator.getTriangulation();
        
        JFrame tframe = new JFrame();
        GraphViewer tviewer = new GraphViewer();
        tviewer.setGraph(triangulation);
        tframe.getContentPane().add(tviewer);
        tframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        tframe.setSize(new Dimension(800, 800));
        tframe.setTitle("Triangulation");
        tframe.setVisible(true);        

        LOGGER.fine("Triangulation is: " + triangulation);
        Graph clusters = AutoClust.runAutoClust(triangulation);        
                
        LOGGER.fine("Clusters supposedly are " + clusters);
        
        JFrame frame = new JFrame();
        GraphViewer viewer = new GraphViewer();
        viewer.setGraph(clusters);
        frame.getContentPane().add(viewer);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(new Dimension(800, 800));
        frame.setTitle("Final");
        frame.setVisible(true);
    }

/*    
    private static FeatureCollection loadCSVData(){
        FeatureCollection coll = null;
        boolean useAll = true;
        
        try{
            FileReader dataReader = new FileReader("C:/jconley/data/popset1b.csv");           
            edu.psu.geovista.io.csv.CSVParser csvp = new edu.psu.geovista.io.csv.CSVParser(dataReader);
            String[][] data = csvp.getAllValues();
            double[][] all = new double[data.length-1][data[0].length];
            for (int i = 1; i < data.length; i++){
                for (int j = 0; j < data[i].length; j++){
                    all[i-1][j] = Double.parseDouble(data[i][j]);
                }
            } 

            coll = org.geotools.feature.FeatureCollections.newCollection();

            for (int i = 0; i < all.length; i++){
                if (useAll){
                    com.vividsolutions.jts.geom.Coordinate coord = new com.vividsolutions.jts.geom.Coordinate(all[i][0]*3, all[i][1]*3);
                    com.vividsolutions.jts.geom.GeometryFactory fact = new com.vividsolutions.jts.geom.GeometryFactory();
                    com.vividsolutions.jts.geom.Point p = fact.createPoint(coord);

                    java.util.ArrayList features = new java.util.ArrayList();
                    org.geotools.feature.AttributeType[] pointAttribute = new org.geotools.feature.AttributeType[3];
                    pointAttribute[0] = org.geotools.feature.AttributeTypeFactory.newAttributeType("centre", com.vividsolutions.jts.geom.Point.class);
                    pointAttribute[1] = org.geotools.feature.AttributeTypeFactory.newAttributeType("population",Double.class);
                    pointAttribute[2] = org.geotools.feature.AttributeTypeFactory.newAttributeType("target",Double.class);

                    org.geotools.feature.FeatureType pointType = org.geotools.feature.FeatureTypeFactory.newFeatureType(pointAttribute,"testPoint");

                    org.geotools.feature.Feature pointFeature = pointType.create(new Object[]{p,
                                                                                              new Double(all[i][2]),
                                                                                              new Double(all[i][3])});
                    coll.add(pointFeature);                     
                } else {                
                    if (all[i][3] > 0){
                        com.vividsolutions.jts.geom.Coordinate coord = new com.vividsolutions.jts.geom.Coordinate(all[i][0]*3, all[i][1]*3);
                        com.vividsolutions.jts.geom.GeometryFactory fact = new com.vividsolutions.jts.geom.GeometryFactory();
                        com.vividsolutions.jts.geom.Point p = fact.createPoint(coord);

                        java.util.ArrayList features = new java.util.ArrayList();
                        org.geotools.feature.AttributeType[] pointAttribute = new org.geotools.feature.AttributeType[3];
                        pointAttribute[0] = org.geotools.feature.AttributeTypeFactory.newAttributeType("centre", com.vividsolutions.jts.geom.Point.class);
                        pointAttribute[1] = org.geotools.feature.AttributeTypeFactory.newAttributeType("population",Double.class);
                        pointAttribute[2] = org.geotools.feature.AttributeTypeFactory.newAttributeType("target",Double.class);

                        org.geotools.feature.FeatureType pointType = org.geotools.feature.FeatureTypeFactory.newFeatureType(pointAttribute,"testPoint");

                        org.geotools.feature.Feature pointFeature = pointType.create(new Object[]{p,
                                                                                                  new Double(all[i][2]),
                                                                                                  new Double(all[i][3])});
                        coll.add(pointFeature);                
                    }
                }
            }
 
        } catch (Exception e){
//            e.printStackTrace();
            System.out.println("Error message: " + e.getMessage());
        }
        return coll; 
    }
*/    
}
