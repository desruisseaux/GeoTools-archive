package org.geotools.geometry.iso;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

import org.geotools.geometry.iso.coordinate.DirectPositionImpl;
import org.geotools.geometry.iso.coordinate.DoublePointArray;
import org.geotools.geometry.iso.coordinate.PointArrayImpl;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.spatialschema.geometry.DirectPosition;
import org.opengis.spatialschema.geometry.MismatchedDimensionException;
import org.opengis.spatialschema.geometry.PositionFactory;
import org.opengis.spatialschema.geometry.Precision;
import org.opengis.spatialschema.geometry.geometry.Position;

/**
 * Default implementation of PositionFactory..
 * <p>
 * You should be aware of the following:
 * <ul>
 * <li>createPositionList() is backed by an ArrayList
 * <li>createPositionList( double, int, int) is a custom implementation that does not support add
 * <li>createPositionList( float, int, int ) is a custom implementation that does not support add
 * </ul>
 * 
 * @author Jody Garnett
 */
public class PositionFactoryImpl implements PositionFactory {
    private Precision precision;

    public PositionFactoryImpl( CoordinateReferenceSystem crs, Precision precision ){
        this.crs = crs;      
        this.precision = precision;
    }
    
    final CoordinateReferenceSystem crs;
    
    public DirectPosition createDirectPosition( double[] coords ) throws MismatchedDimensionException {
        return new DirectPositionImpl( crs, coords ); 
    }

    public Position createPosition( Position position ) {
        DirectPosition directPosition = position.getPosition();
        return new DirectPositionImpl( directPosition );
    }

    public List<Position> createPositionList() {
        return new PointArrayImpl( crs );
    }

    public List<Position> createPositionList( final double[] array, final int start, final int end ) {        
        return new DoublePointArray( crs, array, start, end );
    }

    public List<Position> createPositionList( float[] array, int start, int end ) {
        return null; //return new FloatPointArray( crs, array, start, end );
    }

    public CoordinateReferenceSystem getCoordinateReferenceSystem() {
        return crs;
    }
    
    public Precision getPrecision() {
        return precision;
    }
    
}
