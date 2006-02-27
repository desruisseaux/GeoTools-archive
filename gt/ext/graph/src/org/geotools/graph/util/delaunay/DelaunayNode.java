/*
 * DelaunayNode.java
 *
 * Created on January 25, 2006, 9:59 AM
 */

package org.geotools.graph.util.delaunay;

import org.geotools.graph.structure.line.BasicXYNode;

/**
 *
 * @author jfc173
 */
public class DelaunayNode extends BasicXYNode{    
    
    /** Creates a new instance of delaunayNode */
    public DelaunayNode() {    
    }

    public boolean equals(Object o){
        return ((o instanceof DelaunayNode) &&
                (this.getCoordinate().x == ((DelaunayNode)o).getCoordinate().x) &&
                (this.getCoordinate().y == ((DelaunayNode)o).getCoordinate().y));
    }
    
    public String toString(){
        return "(" + Math.round(this.getCoordinate().x) + "," + Math.round(this.getCoordinate().y) + ")";
    }
    
}