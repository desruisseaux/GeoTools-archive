package org.geotools.data;

import java.util.Collection;

import org.geotools.referencing.crs.AbstractCRS;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

/**
 * This GeometryFactory contains additional information allowing for intergration with
 * CoordinateReferenceSystem classes.
 * <p>
 * TODO: Andrea can you review and revise this class? It should declare reprojection
 * to start with.
 * </p>
 * <p>
 * This class is a builder and thus is stateful, it has the same api calls as
 * JTS Geometryfactory. You can use a subclass of this api to define you own
 * transformations 
 * </p>
 * 
 * @author Jody Garnett
 */
public class GeometryBuilder {
    
    /**
     * CoordinateReferenceSystem used to interpret the coordinates
     * used by this CoordinateSystemFactory.
     * <p>
     * This coordinate reference system will be used as the value for the
     * userData object associated with each Geometry. Note this take up
     * no additional space.
     */
    private AbstractCRS coordinateReferenceSystem;
    
    /** GeometryFactory slaved to existing this GeometryBuilder */
    private GeometryFactory geometryFactory = new ProcessedGeometryFactory() {
        protected Geometry process(Geometry geom) {
            geom = super.process(geom);            
            geom.setUserData( getCoordinateReferenceSystem() );
            return geom;
        }
    };
    
    public GeometryFactory getGeometryFactory() {
        return geometryFactory;
    }
    /**
     * @return Returns the coordinateReferenceSystem.
     */
    public AbstractCRS getCoordinateReferenceSystem() {
        return coordinateReferenceSystem;
    }
    /**
     * @param coordinateReferenceSystem The coordinateReferenceSystem to set.
     */
    public void setCoordinateReferenceSystem(
            AbstractCRS coordinateReferenceSystem) {
        this.coordinateReferenceSystem = coordinateReferenceSystem;
    }
    
}

/** Implementation class: a GeometryFactory that sets CRS information. */
class ProcessedGeometryFactory extends GeometryFactory {        
    /* (non-Javadoc)
     * @see com.vividsolutions.jts.geom.GeometryFactory#buildGeometry(java.util.Collection)
     */
    public Geometry buildGeometry(Collection arg0) {
        return process( super.buildGeometry(arg0) );        
    }
    public Geometry createGeometry(Geometry geom ) {
        return process( super.createGeometry( geom ) );
    }
    public GeometryCollection createGeometryCollection(Geometry[] arg0) {
        return process( super.createGeometryCollection(arg0) );
    }
    public LinearRing createLinearRing(Coordinate[] coordinateArray) {
        return (LinearRing) process( super.createLinearRing(coordinateArray) );
    }
    public LinearRing createLinearRing(CoordinateSequence coordinateSequence) {
        return (LinearRing) process( super.createLinearRing(coordinateSequence) );
    }
    public LineString createLineString(Coordinate[] coordinateArray) {
        return (LineString) process( super.createLineString(coordinateArray) );
    }
    public LineString createLineString(CoordinateSequence coordinateSequence) {
        return (LineString) process( super.createLineString(coordinateSequence) );
    }
    public MultiLineString createMultiLineString(LineString[] arg0) {
        return (MultiLineString) process( createMultiLineString(arg0) );
    }
    public MultiPoint createMultiPoint(Coordinate[] arg0) {
        return (MultiPoint) process( super.createMultiPoint(arg0) );
    }
    public MultiPoint createMultiPoint(CoordinateSequence arg0) {
        return (MultiPoint) process( super.createMultiPoint(arg0) );
    }
    public MultiPoint createMultiPoint(Point[] arg0) {
        return (MultiPoint) process( super.createMultiPoint(arg0) );
    }
    public MultiPolygon createMultiPolygon(Polygon[] arg0) {
        return (MultiPolygon) process( super.createMultiPolygon(arg0) );
    }
    public Point createPoint(Coordinate arg0) {
        return (Point) process( super.createPoint(arg0) );
    }
    public Point createPoint(CoordinateSequence arg0) {
        return (Point) process( super.createPoint(arg0) );
    }
    public Polygon createPolygon(LinearRing arg0, LinearRing[] arg1) {
        return (Polygon) process( super.createPolygon(arg0, arg1) );
    }
    public Geometry toGeometry(Envelope arg0) {
        return process( super.toGeometry(arg0) );
    }    
    /** Overrride for custom process (ie reprojection) */
    protected Geometry process( Geometry geom ){        
        if( geom instanceof GeometryCollection ){
            process( (GeometryCollection) geom );            
        }
        return geom;
    }
    
    /** Overrride for custom process (ie reprojection) */
    protected GeometryCollection process( GeometryCollection collection ){
        for( int i=0; i< collection.getNumGeometries(); i++){                 
            process( collection.getGeometryN( i ));
        }
        return collection;
    }
}