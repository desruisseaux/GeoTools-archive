/*
 * AutoClustMain.java
 *
 * Created on February 14, 2006, 11:19 AM
 */

package org.geotools.graph.util.delaunay;

import java.awt.Dimension;
import java.util.logging.Logger;
import javax.swing.JFrame;
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
        DelaunayNode[] nodes;
        if (useRandom){
            nodes = DelaunayTest.createRandomNodes(250, 180, 180, 100);
        } else {
            nodes = DelaunayTest.createStateNodes();
        }
        Graph triangulation = DelaunayTriangulator.triangulate(nodes);
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
    
}
