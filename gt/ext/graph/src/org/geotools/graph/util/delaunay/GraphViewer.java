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
import java.awt.Color;
import java.awt.Graphics;
import java.lang.RuntimeException;
import java.util.Collection;
import java.util.Iterator;
import javax.swing.JPanel;
import org.geotools.graph.structure.Edge;
import org.geotools.graph.structure.Graph;
import org.geotools.graph.structure.line.XYNode;

/**
 *
 * @author jfc173
 */
public class GraphViewer extends JPanel {
    
    Graph graph;
    Collection nodes;
    int minX, minY;
    Color[] nodeColors = new Color[]{Color.RED, Color.ORANGE, Color.YELLOW, Color.GREEN, Color.BLUE, Color.MAGENTA, Color.LIGHT_GRAY, Color.GRAY, Color.DARK_GRAY, Color.BLACK};
    
    /** Creates a new instance of GraphViewer */
    public GraphViewer() {        
    }
    
    public void setGraph(Graph gr){
        graph = gr;
        nodes = graph.getNodes();
        Iterator it = nodes.iterator();
        minX = 0;
        minY = 0;
        while (it.hasNext()){
            Object next = it.next();
            if (!(next instanceof XYNode)){
                throw new RuntimeException("I can't draw a node that doesn't have a coordinate.");
            }
            Coordinate coord = ((XYNode) next).getCoordinate();
            if (coord.x < minX){
                minX = (int) Math.round(coord.x);
            }
            if (coord.y < minY){
                minY = (int) Math.round(coord.y);
            }
        }
    }
    
    public void paintComponent(Graphics g){
        int i = 0;
        int scaling = 4;
        Iterator it = nodes.iterator();
        while (it.hasNext()){
            Object next = it.next();
            if (!(next instanceof XYNode)){
                throw new RuntimeException("I can't draw a node that doesn't have a coordinate.");
            }
            Coordinate coord = ((XYNode) next).getCoordinate();
//            g.setColor(nodeColors[i]);
//            i++; //this works if there are no more than 10 nodes.
            g.fillOval((int) Math.round(coord.x*scaling-2) - minX*scaling, (int) Math.round(coord.y*scaling-2) - minY*scaling, 4, 4);
        }
        
        g.setColor(Color.RED);        
        Collection edges = graph.getEdges();
        Iterator edgeIt = edges.iterator();
        while (edgeIt.hasNext()){
            Edge next = (Edge) edgeIt.next();
            if (!((next.getNodeA() instanceof XYNode) && (next.getNodeB() instanceof XYNode))){
                throw new RuntimeException("I can't draw an edge without endpoint coordinates.");
            }
            Coordinate coordA = ((XYNode) next.getNodeA()).getCoordinate();
            Coordinate coordB = ((XYNode) next.getNodeB()).getCoordinate();
            g.drawLine((int) Math.round(coordA.x*scaling) - minX*scaling,
                       (int) Math.round(coordA.y*scaling) - minY*scaling,
                       (int) Math.round(coordB.x*scaling) - minX*scaling,
                       (int) Math.round(coordB.y*scaling) - minY*scaling);
        }
    }
    
}
