package org.geotools.data;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequenceFactory;
import com.vividsolutions.jts.geom.DefaultCoordinateSequenceFactory;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.impl.PackedCoordinateSequenceFactory;

/**
 * Demo of Map of Hints ideas.
 * 
 * @author jgarnett
 * @since 0.6.0
 */
public class Hack {
    Map hints;
    public Hack( Map hints ){
        this.hints = hints;
        if( hints == null ){
            this.hints = new HashMap();
        }
    }
    private static Constructor constructor( Class type, Class param ){
        Class find[] =  param == null ? new Class[0] : new Class[]{ param };
        try {
            return type.getConstructor( find );
        } catch (SecurityException e) {
            return null;
        } catch (NoSuchMethodException e) {
            return null;
        }
    }
    /** Will create newInstance based on Params, or nothing 
     * @throws InstantiationException */
    private Object newInstance( Class type ) throws Exception {
        Constructor c = constructor( type, Map.class );
        if( c != null ){
            return c.newInstance( new Object[]{ hints } );            
        }
        c = constructor( type, null );
        if( c != null ){
            return c.newInstance( new Object[0] );
        }
        throw new InstantiationException( type.getCanonicalName() + " required a Map or no argument constructor ");  
    }
    /** make GeometryFactory based on hints */
    public GeometryFactory getGeometryFactory() throws Exception {
        Object hint = null;
        if( hints.containsKey( GeometryFactory.class )){
            hint = hints.get( GeometryFactory.class );
        }
        if( hint instanceof GeometryFactory ){
            return (GeometryFactory) hint; // user gave us one
        }
        
        Class type = null;
        if( hint == null ){
            type = systemDefault( GeometryFactory.class );
        }
        else if( hint instanceof Class ){
            type = (Class) hint;
        }
        if( GeometryFactory.class.isAssignableFrom( type )){
            GeometryFactory factory = null;
            Constructor make = constructor( type, Map.class );
            
            if( make != null ) { // use Map constructor first (so we can reuse hints)
                factory = (GeometryFactory) make.newInstance( new Object[]{ hints } );
            }
            else {                
                make = constructor( type, CoordinateSequenceFactory.class );
                if( hints != null ){ // check for one that needs coordinateSequence factory first
                    factory = (GeometryFactory) make.newInstance( new Object[]{getCoordinateSequenceFactory(),} );                        
                }
                else {
                    make = constructor( type, null );
                    if( make != null ){
                        factory = (GeometryFactory) make.newInstance( new Object[0] );
                    }
                    else {
                        throw new IllegalArgumentException("GeometryFactory "+type.getCanonicalName()+" needs a Map, CoordianteSequence or no argument constructor");                
                    }
                }
            }
            hints.put( GeometryFactory.class, factory );
            return factory;
        }
        throw new IllegalArgumentException("GeometryFactory hint "+hint+" is not understood");
    }
    
    public GeometryFactory getGeometryFactory2() throws Exception {
        Object hint = null;
        if( hints.containsKey( GeometryFactory.class )){
            hint = hints.get( GeometryFactory.class );
        }
        if( hint instanceof GeometryFactory ){
            return (GeometryFactory) hint; // user gave us one
        }
        
        Class type = null;
        if( hint == null ){
            type = systemDefault( GeometryFactory.class );
        }
        else if( hint instanceof Class ){
            type = (Class) hint;
        }
        if( GeometryFactory.class.isAssignableFrom( type )){
            GeometryFactory factory = null;
            Constructor make = constructor( type, Map.class );
            
            if( make != null ) { // use Map constructor first (so we can reuse hints)
                factory = (GeometryFactory) make.newInstance( new Object[]{ hints } );
            }
            else {                
                make = constructor( type, CoordinateSequenceFactory.class );
                if( hints != null ){ // check for one that needs coordinateSequence factory first
                    factory = (GeometryFactory) make.newInstance( new Object[]{get(CoordinateSequenceFactory.class),} );                        
                }
                else {
                    make = constructor( type, null );
                    if( make != null ){
                        factory = (GeometryFactory) make.newInstance( new Object[0] );
                    }
                    else {
                        throw new IllegalArgumentException("GeometryFactory "+type.getCanonicalName()+" needs a Map, CoordianteSequence or no argument constructor");                
                    }
                }
            }
            hints.put( GeometryFactory.class, factory );
            return factory;
        }
        throw new IllegalArgumentException("GeometryFactory hint "+hint+" is not understood");
    }
    /** CoordinateSequenceFactory based on hints */
    public CoordinateSequenceFactory getCoordinateSequenceFactory() throws Exception {
        Object hint = null;
        if( hints.containsKey( CoordinateSequenceFactory.class )){
            hint = hints.get( CoordinateSequenceFactory.class );
        }
        if( hint instanceof CoordinateSequenceFactory ){
            return (CoordinateSequenceFactory) hint; // user gave us one
        }
        Class type = (Class) hint;
        if( hint == null ){
            type = systemDefault( CoordinateSequenceFactory.class );
        }
        if( hint instanceof Class ){
            type = (Class) hint;
        }
        if( CoordinateSequenceFactory.class.isAssignableFrom( type )){
            CoordinateSequenceFactory factory = null;
            factory = (CoordinateSequenceFactory) newInstance( type );            
            hints.put( CoordinateSequenceFactory.class, factory );
            return factory;
        }            
        throw new IllegalArgumentException("CoordinateSequenceFactory hint "+hint+" is not understood" );                
    }
    /** Generic get by factory type */
    public Object get( Class factoryType ) throws Exception {
        if( factoryType == GeometryFactory.class ) return getGeometryFactory2();
        
        Object hint = null;
        if( hints.containsKey( factoryType )){
            hint = hints.get( factoryType );
        }
        if( factoryType.isInstance( hint ) ){
            return hint; // user gave us one
        }
        Class type = (Class) hint;
        if( hint == null ){
            type = systemDefault( factoryType );
        }
        if( hint instanceof Class ){
            type = (Class) hint;
        }
        if( factoryType.isAssignableFrom( type )){
            Object factory = null;
            factory = newInstance( type );            
            hints.put( factoryType, factory );
            return factory;
        }            
        throw new IllegalArgumentException("CoordinateSequenceFactory hint "+hint+" is not understood" );
    }
    /** Generic get default type by factory type */
    private Class systemDefault( Class factoryType ) {
        if( factoryType == CoordinateSequenceFactory.class ){
            return DefaultCoordinateSequenceFactory.class;    
        }
        if( factoryType == GeometryFactory.class ){
            return GeometryFactory.class;
        }
        return factoryType;        
    }
    
    public LineString line2( int points[] ) throws Exception {
        GeometryFactory gf = (GeometryFactory) get( GeometryFactory.class );
        return gf.createLineString( coords( points ) );
    }
    public LinearRing ring2( int points[] ) throws Exception {
        GeometryFactory gf = (GeometryFactory) get( GeometryFactory.class );
        return gf.createLinearRing( coords( points ) );
    }
    public LineString line( int points[] ) throws Exception {
        GeometryFactory gf = getGeometryFactory();
        return gf.createLineString( coords( points ) );
    }
    public LinearRing ring( int points[] ) throws Exception {
        GeometryFactory gf = getGeometryFactory();
        return gf.createLinearRing( coords( points ) );
    }
    private Coordinate[] coords( int points[] ){
        if( points == null ){
            return new Coordinate[0];
        }
        Coordinate[] array = new Coordinate[ points.length/2 ];
        for( int i=0; i<points.length; i+=2){
            array[i/2] = new Coordinate( points[i], points[i+1] );
        }
        return array;
    }
    public static void main( String args[]) throws Exception {
        System.out.println("HARDCODED EXAMPLE" );
        
        LineString defaultLine = new Hack( null ).line( new int[]{ 0,0, 1,1});
        System.out.println("Default sequence:"+ defaultLine.getCoordinateSequence().getClass() );
        System.out.println("Default line:"+ defaultLine );
        
        Map map = new HashMap();
        map.put( CoordinateSequenceFactory.class, PackedCoordinateSequenceFactory.class );
        
        LineString packedLine = new Hack( map ).line( new int[]{ 0,0, 1,1});
        System.out.println("Custom sequence:"+ packedLine.getCoordinateSequence().getClass() );
        System.out.println("Custom line:"+ packedLine );
        
        System.out.println("GENERIC EXAMPLE" );
        defaultLine = new Hack( null ).line2( new int[]{ 0,0, 1,1});
        System.out.println("Default sequence:"+ defaultLine.getCoordinateSequence().getClass() );
        System.out.println("Default line:"+ defaultLine );
        
        map = new HashMap();
        map.put( CoordinateSequenceFactory.class, PackedCoordinateSequenceFactory.class );
        
        packedLine = new Hack( map ).line2( new int[]{ 0,0, 1,1});
        System.out.println("Custom sequence:"+ packedLine.getCoordinateSequence().getClass() );
        System.out.println("Custom line:"+ packedLine );
    }
}
