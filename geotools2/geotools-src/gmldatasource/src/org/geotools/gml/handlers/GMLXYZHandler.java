/*
 * GMLXYZHandler.java
 *
 * Created on 07 March 2002, 17:56
 */

package org.geotools.gml.handlers;
import com.vividsolutions.jts.geom.*;
/**
 *
 * @author  ian
 */
public abstract class GMLXYZHandler extends org.geotools.gml.GMLHandler {

    public abstract void parseText(String s);
    
    public Geometry finish(GeometryFactory gf) {
        return null;
    }
    
    public void addGeometry(Geometry g) {
    }
    
    public void addCoordinate(Coordinate c) {
    }
    
}

