package org.geotools.demo.geometry;

import org.geotools.factory.Hints;
import org.geotools.geometry.GeometryBuilder;
import org.geotools.geometry.GeometryFactoryFinder;
import org.geotools.geometry.text.WKTParser;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.geometry.DirectPosition;
import org.opengis.geometry.PositionFactory;
import org.opengis.geometry.aggregate.AggregateFactory;
import org.opengis.geometry.coordinate.GeometryFactory;
import org.opengis.geometry.primitive.Point;
import org.opengis.geometry.primitive.PrimitiveFactory;

public class GeometryExample {

    public void createPoint(){
        GeometryBuilder builder = new GeometryBuilder( DefaultGeographicCRS.WGS84 );        
        Point point = builder.createPoint( 48.44, -123.37 );
        
        System.out.println( point );
    }
    
    public void createPointWithFactory(){
        Hints hints = new Hints( Hints.CRS, DefaultGeographicCRS.WGS84 );
        PositionFactory positionFactory = GeometryFactoryFinder.getPositionFactory( hints );
        PrimitiveFactory primitiveFactory = GeometryFactoryFinder.getPrimitiveFactory( hints );
        
        DirectPosition here = positionFactory.createDirectPosition( new double[]{48.44, -123.37} );        
        Point point1 = primitiveFactory.createPoint( here );
        
        System.out.println( point1 );
        
        Point point2 = primitiveFactory.createPoint(  new double[]{48.44, -123.37} );
        System.out.println( point2 );
    }
    
    public void createPointWithWKT() throws Exception {
        Hints hints = new Hints( Hints.CRS, DefaultGeographicCRS.WGS84 );
        
        PositionFactory positionFactory = GeometryFactoryFinder.getPositionFactory(hints);
        GeometryFactory geometryFactory = GeometryFactoryFinder.getGeometryFactory(hints);
        PrimitiveFactory primitiveFactory = GeometryFactoryFinder.getPrimitiveFactory(hints);
        AggregateFactory aggregateFactory = GeometryFactoryFinder.getAggregateFactory(hints);
        
        WKTParser parser = new WKTParser( geometryFactory, primitiveFactory, positionFactory, aggregateFactory );
        
        Point point = (Point) parser.parse("POINT( 48.44 -123.37)");
    }
    
    public static void main( String args[] ) throws Exception {
        GeometryExample example = new GeometryExample();
        
        example.createPoint();
        example.createPointWithFactory();
        example.createPointWithWKT();
    }
}
