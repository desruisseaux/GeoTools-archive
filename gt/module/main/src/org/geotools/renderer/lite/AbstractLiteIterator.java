/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
 *
 */
package org.geotools.renderer.lite;

import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.util.NoSuchElementException;

import org.geotools.geometry.JTS;
import org.geotools.geometry.coordinatesequence.InPlaceCoordinateSequenceTransformer;
import org.geotools.geometry.jts.CoordinateSequenceTransformer;
import org.geotools.geometry.jts.DefaultCoordinateSequenceTransformer;
import org.geotools.referencing.FactoryFinder;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.NoninvertibleTransformException;
import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.CoordinateSequence;


/**
 * Subclass that provides a convenient efficient currentSegment(float[] coords) implementation
 * that reuses always the same double array. This class and the associated subclasses are not
 * thread safe.
 *
 * @author Andrea Aime
 */
public abstract class AbstractLiteIterator implements PathIterator {
    protected double[] dcoords = new double[2];
    protected static final AffineTransform NO_TRANSFORM = new AffineTransform();
    protected MathTransform mathTransform=null;
    
    /**
     * @see java.awt.geom.PathIterator#currentSegment(float[])
     */
    public int currentSegment(float[] coords) {
        int result = currentSegment(dcoords);
        coords[0] = (float) dcoords[0];
        coords[1] = (float) dcoords[1];

        return result;
    }

    /**
     * @return Returns the mathTransform.
     */
    public MathTransform getMathTransform() {
        return mathTransform;
    }
    
    public abstract void setMathTransform(MathTransform transform);
    
    /**
     * Transforms the coordinates and sets the math transform.
     */
    protected void transform( CoordinateSequence coordinates, MathTransform transform ) {
        try {
            
            MathTransform tmp=mathTransform;
            if( tmp!=null)
                tmp=FactoryFinder.getMathTransformFactory().createConcatenatedTransform(mathTransform.inverse(), transform);
            else
                tmp=transform;
            if( tmp==null )
                return;
            CoordinateSequenceTransformer transformer=new InPlaceCoordinateSequenceTransformer();
            transformer.transform(coordinates, tmp);
            mathTransform=transform;
        } catch (NoSuchElementException e) {
            // TODO Catch e
        } catch (NoninvertibleTransformException e) {
            // TODO Catch e
        } catch (FactoryException e) {
            // TODO Catch e
        } catch (TransformException e) {
            // TODO Catch e
        }
    }

    
}
