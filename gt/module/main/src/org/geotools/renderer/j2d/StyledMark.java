/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Management Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.geotools.renderer.j2d;

// J2SE dependencies
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;

import org.geotools.feature.Feature;
import org.geotools.renderer.style.Java2DMark;
import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.Geometry;


/**
 *
 * @author  jamesm
 */
public class StyledMark extends RenderedMarks {
    Feature f;
    /** Creates a new instance of StyledRenderedMark */
    public StyledMark(Feature f) {
        this.f = f;
    }
    
    public MarkIterator getMarkIterator() {
        return new SingleMark(f);
    }
    
    
    
    class SingleMark extends MarkIterator {
        Feature feature;
        int pos = 0;
        public SingleMark(Feature f){
            feature = f;
            
        }
        
        public int getIteratorPosition() {
            return pos;
        }
        
        public boolean next() {
            return pos++<1;
           
        }
        
        public java.awt.geom.Point2D position() throws TransformException {
          Geometry g = feature.getDefaultGeometry();
          return new Point2D.Double(g.getCoordinate().x, g.getCoordinate().y);
        }
        
        public void setIteratorPosition(int index) throws IllegalArgumentException {
            pos = index;
        }
        
        public Shape markShape(){
            GeneralPath shape = (GeneralPath)Java2DMark.getWellKnownMark("star"); 
            AffineTransform at = new AffineTransform();
            at.scale(10,10);
            return shape.createTransformedShape(at);
        }
    }
    
}
