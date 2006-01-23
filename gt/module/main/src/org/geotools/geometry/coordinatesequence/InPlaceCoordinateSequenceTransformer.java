/*
 *    uDig - User Friendly Desktop Internet GIS client
 *    http://udig.refractions.net
 *    (C) 2004, Refractions Research Inc.
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
package org.geotools.geometry.coordinatesequence;

import org.geotools.geometry.jts.CoordinateSequenceTransformer;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.opengis.spatialschema.geometry.DirectPosition;
import org.opengis.spatialschema.geometry.MismatchedDimensionException;

import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;
import com.vividsolutions.jts.geom.impl.PackedCoordinateSequence;

/**
 *  summary sentence.
 * <p>
 * Paragraph ...
 * </p><p>
 * Responsibilities:
 * <ul>
 * <li>
 * <li>
 * </ul>
 * </p><p>
 * Example:<pre><code>
 * InPlaceCoordinateSequenceTransformer x = new InPlaceCoordinateSequenceTransformer( ... );
 * TODO code example
 * </code></pre>
 * </p>
 * @author jeichar
 * @since 0.6.0
 * @source $URL$
 */
public class InPlaceCoordinateSequenceTransformer implements CoordinateSequenceTransformer {

    /**
     * @see org.geotools.geometry.jts.CoordinateSequenceTransformer#transform(com.vividsolutions.jts.geom.CoordinateSequence, org.opengis.referencing.operation.MathTransform)
     */
    public CoordinateSequence transform( CoordinateSequence cs, MathTransform transform )
            throws TransformException {
        if( cs instanceof PackedCoordinateSequence ){
            return transformInternal( (PackedCoordinateSequence) cs, transform);
        }
        throw new TransformException(cs.getClass().getName()+" is not a implementation that is known to be transformable in place");
    }

    FlyWeightDirectPosition start=new FlyWeightDirectPosition(2);
    private CoordinateSequence transformInternal( PackedCoordinateSequence sequence, MathTransform transform ) 
    throws TransformException{
        
        start.setSequence(sequence);   
        for(int i=0; i<sequence.size();i++ ){
            start.setOffset(i);
            try {
                transform.transform(start, start);
            } catch (MismatchedDimensionException e) {
                throw new TransformException( "", e);
            } 
        }
        return sequence;
    }
    
    private class FlyWeightDirectPosition implements DirectPosition{
        PackedCoordinateSequence sequence;
        int offset=0;
        private int dimension;
        
        /**
         * Construct <code>InPlaceCoordinateSequenceTransformer.FlyWeightDirectPosition</code>.
         *
         */
        public FlyWeightDirectPosition(int dim) {
            dimension=dim;
        }
        
        /**
         * @param offset The offset to set.
         */
        public void setOffset( int offset ) {
            this.offset = offset;
        }
        
        /**
         * @param sequence The sequence to set.
         */
        public void setSequence( PackedCoordinateSequence sequence ) {
            this.sequence = sequence;
        }
        
        /**
         * @see org.opengis.spatialschema.geometry.DirectPosition#getDimension()
         */
        public int getDimension() {
            return dimension;
        }

        /**
         * @see org.opengis.spatialschema.geometry.DirectPosition#getCoordinates()
         */
        public double[] getCoordinates() {
            return new double[]{ sequence.getX(offset), sequence.getY(offset), sequence.getOrdinate(offset, CoordinateSequence.Z)};
        }

        /**
         * @see org.opengis.spatialschema.geometry.DirectPosition#getOrdinate(int)
         */
        public double getOrdinate( int arg0 ) throws IndexOutOfBoundsException {
            return sequence.getOrdinate(offset, arg0);
        }

        /**
         * @see org.opengis.spatialschema.geometry.DirectPosition#setOrdinate(int, double)
         */
        public void setOrdinate( int arg0, double arg1 ) throws IndexOutOfBoundsException {
            sequence.setOrdinate(offset, arg0, arg1);
        }

        /**
         * @see org.opengis.spatialschema.geometry.DirectPosition#getCoordinateReferenceSystem()
         */
        public CoordinateReferenceSystem getCoordinateReferenceSystem() {
            //TODO implement method body
            throw new UnsupportedOperationException();
        }

        /**
         * @see org.opengis.spatialschema.geometry.DirectPosition#clone()
         */
        public Object clone() {
            throw new UnsupportedOperationException();
        }

        /**
         * @see org.opengis.spatialschema.geometry.geometry.Position#getPosition()
         */
        public DirectPosition getPosition() {
            return this;
        }
        
    }

}
