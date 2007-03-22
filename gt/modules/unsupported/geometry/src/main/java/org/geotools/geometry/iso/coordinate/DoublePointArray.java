package org.geotools.geometry.iso.coordinate;

import java.util.AbstractList;
import java.util.List;

import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.spatialschema.geometry.DirectPosition;
import org.opengis.spatialschema.geometry.geometry.PointArray;
import org.opengis.spatialschema.geometry.geometry.Position;

/**
 * This implementation is a "fast" wrapper over top of a double array.
 * <p>
 * The returned DirectPositions are pure wrappers over top of the array.
 * </p>
 * @author Jody
 */
public class DoublePointArray extends AbstractList<Position> implements PointArray {
    
    double[] array;
    int start;
    int end;
    CoordinateReferenceSystem crs;
    
    public DoublePointArray( CoordinateReferenceSystem crs, double[] array ) {
        this( crs, array, 0, array.length );
    }
    public DoublePointArray( CoordinateReferenceSystem crs, double[] array, int start, int end ) {
        this.crs = crs;
        this.array = array;
        this.start = start;
        this.end = end;
    }
    
    @Override
    public DirectPosition get( int index ) {
        int D = getDimension();
        return new DoubleDirectPosition( crs, array, start+index*D );            
    }
    @Override
    public Position set( int index, Position element ) {
        int D = getDimension();
        return new DoubleDirectPosition( crs, array, start+index*D );
    }
    public int size() {
        return end-start / crs.getCoordinateSystem().getDimension();
    }    
    public DirectPosition getPosition( int index, DirectPosition position ) throws IndexOutOfBoundsException {
        int D = getDimension();        
        if( position == null ){            
            double[] copy = new double[ D ];
            System.arraycopy( array, start+index*D, copy, 0, D );
            return new DoubleDirectPosition( crs, copy );
        }
        for( int i =0; i< D; i++){
            position.setOrdinate( i, array[ start+index*D+i]);
        }
        return position;        
    }
    public void setPosition( int index, DirectPosition position ) throws IndexOutOfBoundsException, UnsupportedOperationException {
        // note: tempting to use System arraycopy on position.getCoordiantes() but that would make an tempoaray extra array
        int D = getDimension();
        for( int i=0; i<D;i++){
            array[ start + index*D + i ] = position.getOrdinate( i );
        }
    }
    public CoordinateReferenceSystem getCoordinateReferenceSystem() {
        return crs;
    }
    public int getDimension() {
        return crs.getCoordinateSystem().getDimension();
    }
    public int length() {
        return (array.length - start )/ getDimension();
    }
    public List<Position> positions() {
        return this;
    }    
}
/**
 * Represents a DirectPosition wrapper of a secion of a double array.
 * <p>
 * This class is private, althought not an inner class of DoublePointArray, this
 * is done to allow us to clone() in a safe manner.
 * </p>
 * @author Jody Garnett
 */
class DoubleDirectPosition implements DirectPosition {
    int index;
    double array[];
    CoordinateReferenceSystem crs;
    
    public DoubleDirectPosition( DoublePointArray context, int index ){
        this( context.getCoordinateReferenceSystem(), context.array, context.start+index );
    }
    public DoubleDirectPosition( CoordinateReferenceSystem crs, double array[] ){
        this( crs, array, 0 );
    }
    public DoubleDirectPosition( CoordinateReferenceSystem crs, double array[], int index ){
        this.index = index;
        this.array = array;
        this.crs = crs;
    }    
    public CoordinateReferenceSystem getCoordinateReferenceSystem() {
        return crs;
    }
    public double[] getCoordinates() {
        double coords[] = new double[ crs.getCoordinateSystem().getDimension() ];
        System.arraycopy(array, index, coords, 0, crs.getCoordinateSystem().getDimension() );
        return coords;
    }

    public int getDimension() {
        return crs.getCoordinateSystem().getDimension();
    }

    public double getOrdinate( int dimension ) throws IndexOutOfBoundsException {
        return array[index+dimension];
    }

    public void setOrdinate( int dimension, double value ) throws IndexOutOfBoundsException {
        array[index+dimension] = value;                        
    }
    public DirectPosition getPosition() {
        return this;
    }
    public Object clone() {
        return new DoubleDirectPosition( crs, getCoordinates() );                                
    }
}