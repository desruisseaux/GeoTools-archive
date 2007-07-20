/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2006-2007, GeoTools Project Management Committee (PMC)
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

package org.geotools.geometry.iso.coordinate;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.List;

import org.geotools.geometry.iso.util.DoubleOperation;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.geometry.DirectPosition;
import org.opengis.geometry.coordinate.PointArray;
import org.opengis.geometry.coordinate.Position;

/**
 * This implementation is a "fast" wrapper over top of a double array.
 * <p>
 * The returned DirectPositions are pure wrappers over top of the array. The
 * number of ordinates used per each DirectPosition is based on the CRS. We
 * start counting from the start position, in order to do subList efficiently.
 * </p>
 * @author Jody
 */
public class DoublePointArray extends AbstractList<Position> implements PointArray {
    
	/** This is the array we are "wrapping" */
    double[] array;
    /**
     * This is the start index into array, each DirectPosition
     * will be defined relative to start.
     */
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
    public List<Position> subList(int fromIndex, int toIndex) {
    	int subStart = start+(fromIndex*getDimension());
    	int subEnd = start+(toIndex*getDimension());
    	
    	return new DoublePointArray( crs, array, subStart, subEnd );
    	//return super.subList(fromIndex, toIndex);
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
	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = super.hashCode();
		result = PRIME * result + Arrays.hashCode(array);
		result = PRIME * result + ((crs == null) ? 0 : crs.hashCode());
		result = PRIME * result + end;
		result = PRIME * result + start;
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof DoublePointArray)
			return this.equals((DoublePointArray) obj, 0);
		else
			return false;
	}   
	
	/**
	 * Compares coodinates of DoublePointArray and allows a tolerance value in
	 * the comparison.
	 * 
	 * @param dpArray
	 *            Direct Position to compare with
	 * @param tol Epsilon tolerance value
	 * @return TRUE, if coordinates accord concording to the tolerance value, FALSE if they dont.
	 */
	public boolean equals(DoublePointArray dpArray, double tol) {
		int D = dpArray.getDimension();
		if( D != getDimension() ) return false;
		
		if (dpArray.length() != length()) return false;
		
		// only compare the positions within the start/end of the larger array
		for (int x=0; x<dpArray.length(); x++) {
			DirectPosition ddPos = dpArray.get(x);
			DirectPosition thisddPos = get(x);
			for (int i = 0; i < D; ++i) {
				if (Math.abs(DoubleOperation.subtract(ddPos.getOrdinate(i), thisddPos.getOrdinate(i))) > tol)
					return false;
			}
		}
		return true;
	}
	public DirectPosition getDirectPosition(int index, DirectPosition dest) throws IndexOutOfBoundsException {
		if (dest == null) {
				dest = new DirectPositionImpl(get(index));
		}
		else {
			assert(dest.getCoordinateReferenceSystem().equals(crs));
			DirectPosition dp = new DirectPositionImpl(get(index));
			for (int i=0; i < dp.getCoordinates().length; i++) {
				dest.setOrdinate(i, dp.getOrdinate(i));
			}
		}
		return dest;
	}
	public void setDirectPosition(int index, DirectPosition position) throws IndexOutOfBoundsException, UnsupportedOperationException {
		this.setPosition(index, position);
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