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
    
    private double roundToSigDigs(double d, int digits){
        if (d == 0){
            return 0;
        } else {
            double log = Math.log10(d);
            int digitsLeftOfDecimal = (int) Math.ceil(log);
            int digitsToMoveLeft = digits - digitsLeftOfDecimal;
            double movedD = d*Math.pow(10, digitsToMoveLeft);
            double rounded = Math.rint(movedD);
            double ret = rounded / Math.pow(10, digitsToMoveLeft);
            return ret;
        }
    }    
    
    public String toString(){
        return "(" + roundToSigDigs(this.getCoordinate().x, 5) + "," + roundToSigDigs(this.getCoordinate().y, 5) + ")";
    }
    
}