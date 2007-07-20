package org.geotools.geometry;

import java.util.Collection;
import java.util.List;

import org.geotools.factory.GeoTools;
import org.geotools.factory.Hints;
import org.geotools.referencing.CRS;
import org.opengis.geometry.DirectPosition;
import org.opengis.geometry.PositionFactory;
import org.opengis.geometry.Precision;
import org.opengis.geometry.coordinate.Position;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * A Builder to help with Geometry creation.
 * <p>
 * The factory interfaces provided by GeoAPI are hard to use in isolation (they
 * are even hard to collect a matched set in order to work on the same problem).
 * The main advantage a "builder" has over a factory is that it does not have to
 * be thread safe and can hold state in order to make your job easier.
 * <p>
 *  
 * @author Jody Garnett
 *
 */
public class GeometryBuilder /*implements PositionFactory*/ {
    /**
     * Hints used for the duration of this GeometryBuilder.
     */
    private Hints hints;
    
    /**
     * CoordinateReferenceSystem used to construct the next geometry artifact.
     * <p>
     * This forms the core state of our builds, all other factories are created
     * with this CoordinateReferenceSystem in mind.
     */
    private CoordinateReferenceSystem crs;
    
    /**
     * Precision used to construct the next direct position.
     * <p>
     * This forms the core state of our builds, all other factories are created
     * with this CoordinateReferenceSystem in mind.
     */
    private Precision precision;
    
    private PositionFactory positionFactory;
    
    public GeometryBuilder( CoordinateReferenceSystem crs ){
        this.crs = crs;
        this.hints = GeoTools.getDefaultHints();
        hints.put( Hints.CRS, crs );
    }
    
    public GeometryBuilder( String code ) throws NoSuchAuthorityCodeException, FactoryException{
        this( CRS.decode( code ));
    }
    
    public GeometryBuilder( Hints hints ){
        this.crs = (CoordinateReferenceSystem) hints.get( Hints.CRS );
        this.hints = hints;
    }
    
    public CoordinateReferenceSystem getCoordinateReferenceSystem() {
        return crs;
    }

    public void setCoordianteReferenceSystem( CoordinateReferenceSystem crs ) {
        if( this.crs != crs ){
            positionFactory = null;
        }
        this.crs = crs;        
    }

    public Precision getPrecision() {
        if( precision == null ){
            precision = GeometryFactoryFinder.getPrecision( hints );
        }
        return precision;        
    }
    
    public PositionFactory getPositionFactory() {
        if( positionFactory == null ){
            positionFactory = GeometryFactoryFinder.getPositionFactory( crs, hints);
        }
        return positionFactory;
    }

    public DirectPosition createDirectPosition( double[] ordinates ) {
        return getPositionFactory().createDirectPosition( ordinates );
    }

    public Position createPosition( Position position ) {
        return getPositionFactory().createPosition( position );
    }

    /**
     * Helper method allows you to take a raw collection of Position and
     * convert it into a PointArray.
     * @param origional
     * @return PointArray
     */
    public List createPositionList( Collection origional ) {
        List list = getPositionFactory().createPositionList();
        list.addAll( origional );
        return list;
    }
    
    public List createPositionList() {
        return getPositionFactory().createPositionList();
    }

    public List createPositionList( double[] array, int start, int end ) {
        return getPositionFactory().createPositionList(array, start, end );
    }

    public List createPositionList( float[] array, int start, int end ) {
        return getPositionFactory().createPositionList(array, start, end );
    }

}
