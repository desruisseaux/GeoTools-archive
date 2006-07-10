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
import java.util.Random;
import java.util.logging.Logger;
import javax.swing.JFrame;
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
        boolean useRandom = false;
        if (useRandom){
            g = DelaunayTriangulator.triangulate(createRandomNodes(250, 250, 250, 0));
        } else {
            g = DelaunayTriangulator.triangulate(createStateNodes());
        }
        LOGGER.fine("Graph supposedly is " + g);
        
        JFrame frame = new JFrame();
        GraphViewer viewer = new GraphViewer();
        viewer.setGraph(g);
        frame.getContentPane().add(viewer);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(new Dimension(400, 400));
        frame.setVisible(true);
        
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
