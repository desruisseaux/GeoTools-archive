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

import com.vividsolutions.jts.geom.Coordinate;
import java.awt.Dimension;
import java.io.FileReader;
import java.util.Random;
import java.util.logging.Logger;
import javax.swing.JFrame;
import org.geotools.feature.FeatureCollection;
import org.geotools.graph.structure.Graph;

/**
 *
 * @author jfc173
 */
public class DelaunayTest {
    
    private static final Logger LOGGER = Logger.getLogger("org.geotools.graph");
    
    /** Creates a new instance of DelaunayTest */
    public DelaunayTest() {
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Graph g;
        boolean debug = false;
        //for debug purposes
//        System.out.println("long edges:");
//        java.util.Iterator edgeIt = g.getEdges().iterator();
//        while (edgeIt.hasNext()){
//            DelaunayEdge next = (DelaunayEdge) edgeIt.next();
//            if (next.getEuclideanDistance() > 50){
//                System.out.println(next.toString());
//            }
//        }
        //end debug
        JFrame frame = new JFrame();
        DelaunayTriangulator triangulator = new DelaunayTriangulator();
        if (debug){
            AddAPointTriangulator aapt = new AddAPointTriangulator(AutoClustUtils.featureCollectionToNodeArray(createBrunsdonNodes()));            
            frame.getContentPane().add(aapt);
        } else {
            boolean useRandom = false;
            if (useRandom){
                triangulator.setNodeArray(createRandomNodes(250, 250, 250, 0));
            } else {
                triangulator.setNodeArray(AutoClustUtils.featureCollectionToNodeArray(createBrunsdonNodes()));
            }
            g = triangulator.getTriangulation();
            LOGGER.fine("Graph supposedly is " + g);            
            GraphViewer viewer = new GraphViewer();
            viewer.setGraph(g);
            frame.getContentPane().add(viewer);
        }
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(new Dimension(400, 400));
        frame.setVisible(true);
        
    }
    
    public static DelaunayNode[] createEdgeTestNodes(){
        DelaunayNode[] nodeArray = new DelaunayNode[10];
        for (int i = 0; i < 10; i++){
            nodeArray[i] = new DelaunayNode();
        }
        nodeArray[0].setCoordinate(new Coordinate(10,30));
        nodeArray[1].setCoordinate(new Coordinate(15,35));
        nodeArray[2].setCoordinate(new Coordinate(20,40));
        nodeArray[3].setCoordinate(new Coordinate(15,45));
        nodeArray[4].setCoordinate(new Coordinate(10,50));
        nodeArray[5].setCoordinate(new Coordinate(5,45));
        nodeArray[6].setCoordinate(new Coordinate(0,40));
        nodeArray[7].setCoordinate(new Coordinate(5,35));
        nodeArray[8].setCoordinate(new Coordinate(5,40));
        nodeArray[9].setCoordinate(new Coordinate(15,40));
        return nodeArray;        
    }
    
    public static DelaunayNode[] createStateNodes(){
        DelaunayNode[] nodeArray = new DelaunayNode[49];
        for (int i = 0; i < 49; i++){
            nodeArray[i] = new DelaunayNode();
        }
        nodeArray[0].setCoordinate(new Coordinate(-89,40));
        nodeArray[1].setCoordinate(new Coordinate(-92,38));
        nodeArray[2].setCoordinate(new Coordinate(-112,34));
        nodeArray[3].setCoordinate(new Coordinate(-98,36));
        nodeArray[4].setCoordinate(new Coordinate(-79,36));
        nodeArray[5].setCoordinate(new Coordinate(-86,36));
        nodeArray[6].setCoordinate(new Coordinate(-99,31));
        nodeArray[7].setCoordinate(new Coordinate(-106,34));
        nodeArray[8].setCoordinate(new Coordinate(-87,33));
        nodeArray[9].setCoordinate(new Coordinate(-90,33));
        nodeArray[10].setCoordinate(new Coordinate(-83,33));
        nodeArray[11].setCoordinate(new Coordinate(-77,39));
        nodeArray[12].setCoordinate(new Coordinate(-81,34));
        nodeArray[13].setCoordinate(new Coordinate(-92,35));
        nodeArray[14].setCoordinate(new Coordinate(-92,31));
        nodeArray[15].setCoordinate(new Coordinate(-82,29));
        nodeArray[16].setCoordinate(new Coordinate(-85,44));
        nodeArray[17].setCoordinate(new Coordinate(-110,47));
        nodeArray[18].setCoordinate(new Coordinate(-69,45));
        nodeArray[19].setCoordinate(new Coordinate(-100,47));
        nodeArray[20].setCoordinate(new Coordinate(-100,44));
        nodeArray[21].setCoordinate(new Coordinate(-108,43));
        nodeArray[22].setCoordinate(new Coordinate(-76,39));
        nodeArray[23].setCoordinate(new Coordinate(-90,45));
        nodeArray[24].setCoordinate(new Coordinate(-115,44));
        nodeArray[25].setCoordinate(new Coordinate(-73,44));
        nodeArray[26].setCoordinate(new Coordinate(-94,46));
        nodeArray[27].setCoordinate(new Coordinate(-121,44));
        nodeArray[28].setCoordinate(new Coordinate(-72,44));
        nodeArray[29].setCoordinate(new Coordinate(-94,42));
        nodeArray[30].setCoordinate(new Coordinate(-72,42));
        nodeArray[31].setCoordinate(new Coordinate(-100,42));
        nodeArray[32].setCoordinate(new Coordinate(-76,43));
        nodeArray[33].setCoordinate(new Coordinate(-81,39));
        nodeArray[34].setCoordinate(new Coordinate(-78,41));
        nodeArray[35].setCoordinate(new Coordinate(-73,42));
        nodeArray[36].setCoordinate(new Coordinate(-72,42));
        nodeArray[37].setCoordinate(new Coordinate(-75,40));
        nodeArray[38].setCoordinate(new Coordinate(-86,40));
        nodeArray[39].setCoordinate(new Coordinate(-117,39));
        nodeArray[40].setCoordinate(new Coordinate(-112,39));
        nodeArray[41].setCoordinate(new Coordinate(-120,37));
        nodeArray[42].setCoordinate(new Coordinate(-83,40));
        nodeArray[43].setCoordinate(new Coordinate(-120,47));
        nodeArray[44].setCoordinate(new Coordinate(-77.5,39.5));
        nodeArray[45].setCoordinate(new Coordinate(-106,39));
        nodeArray[46].setCoordinate(new Coordinate(-85,38));
        nodeArray[47].setCoordinate(new Coordinate(-98,38));
        nodeArray[48].setCoordinate(new Coordinate(-79,38));
        
        return nodeArray;
    }
    
    public static DelaunayNode[] createRandomNodes(int num, int xMax, int yMax, int clusterSize){
        int x, y;
        Random r = new Random();
        DelaunayNode[] nodeArray = new DelaunayNode[num];          
        for (int i = 0; i < num; i++){
            DelaunayNode next = new DelaunayNode();            
            do{
                if (i < clusterSize){
                    x = r.nextInt(xMax/8) + xMax/3;
                    y = r.nextInt(yMax/8) + 2*yMax/3;
                } else {
                    x = r.nextInt(xMax);
                    y = r.nextInt(yMax);
                }
                next.setCoordinate(new Coordinate(x, y));
            } while (arrayContains(next, nodeArray, i));
            nodeArray[i] = next;
        }  

        return nodeArray;
    }
    
    public static FeatureCollection createBrunsdonNodes(){
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
                    com.vividsolutions.jts.geom.Coordinate coord = new com.vividsolutions.jts.geom.Coordinate(all[i][0]*0.03, all[i][1]*0.03);
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
    
    private static boolean arrayContains(DelaunayNode node, DelaunayNode[] nodes, int index){
        boolean ret = false;
        boolean done = false;
        int i = 0;
        while (!(done)){
            if (i < index){
                done = ret = (nodes[i].equals(node));
                i++;
            } else {
                done = true;
            }
        }
        return ret;
    }    
    
}
