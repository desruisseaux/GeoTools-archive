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

import org.geotools.feature.Feature;
import org.geotools.graph.structure.line.BasicXYNode;

/**
 *
 * @author jfc173
 */
public class DelaunayNode extends BasicXYNode{    
    
    private Feature feature;
    
    /** Creates a new instance of delaunayNode */
    public DelaunayNode() {    
    }

    public void setFeature(Feature f){
        feature = f;
    }
    
    public Feature getFeature(){
        return feature;
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