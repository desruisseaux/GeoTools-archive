/*
 * AutoClustUtils.java
 *
 * Created on February 13, 2006, 2:33 PM
 */

package org.geotools.graph.util.delaunay;

import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Logger;
import org.geotools.graph.structure.Edge;
import org.geotools.graph.structure.Graph;
import org.geotools.graph.structure.Node;
import org.geotools.graph.structure.basic.BasicGraph;

/**
 *
 * @author jfc173
 */
public class AutoClustUtils {
    
    private static final Logger LOGGER = Logger.getLogger("org.geotools.graph");
    
    /** Creates a new instance of AutoClustUtils */
    public AutoClustUtils() {
    }
    
    public static Vector findConnectedComponents(Collection nodes, Collection edges){
        Vector components = new Vector();
        Vector nodesVisited = new Vector();
        
        Iterator nodesIt = nodes.iterator();
        while (nodesIt.hasNext()){            
            Node next = (Node) nodesIt.next();
            if (!(nodesVisited.contains(next))){
                Vector componentNodes = new Vector();
                Vector componentEdges = new Vector();
                expandComponent(next, edges, componentNodes, componentEdges);
                nodesVisited.addAll(componentNodes);
                Graph component = new BasicGraph(componentNodes, componentEdges);
                components.add(component);
            }
        }
        
        return components;
    }
    
    private static void expandComponent(Node node, Collection edges, Collection componentNodes, Collection componentEdges){
        if (componentNodes.contains(node)){
            //base case.  I've already expanded on node, so no need to repeat the process.
            LOGGER.fine("I've already expanded from " + node);
        } else {
            componentNodes.add(node);
            LOGGER.finer("Adding " + node + " to component");
            Vector adjacentEdges = findAdjacentEdges(node, edges);  //yes, I know node.getEdges() should do this, but this method could be out of data by the time I use this in AutoClust
            componentEdges.addAll(adjacentEdges);  
            LOGGER.finer("Adding " + adjacentEdges + " to component");
            
            Iterator aeIt = adjacentEdges.iterator();
            while (aeIt.hasNext()){
                Edge next = (Edge) aeIt.next();
                LOGGER.finer("looking at edge " + next);
                Node additionalNode = next.getOtherNode(node);
                LOGGER.finer("its other node is " + additionalNode);
                if (additionalNode == null){
                    throw new RuntimeException("I tried to get the other node of this edge " + next + " but it doesn't have " + node);
                }
                expandComponent(additionalNode, edges, componentNodes, componentEdges);
            }
        }
    }
    
    public static Vector findAdjacentEdges(Node node, Collection edges){
        Vector ret = new Vector();
        Iterator it = edges.iterator();
        while (it.hasNext()){
            Edge next = (Edge) it.next();
            if ((next.getNodeA().equals(node)) || (next.getNodeB().equals(node))){
                ret.add(next);
            }
        }
        return ret;
    }
    
}
