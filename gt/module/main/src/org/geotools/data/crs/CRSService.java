/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2004, Geotools Project Managment Committee (PMC)
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
package org.geotools.data.crs;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import org.geotools.cs.AxisInfo;
import org.geotools.cs.CoordinateSystem;
import org.geotools.cs.CoordinateSystemAuthorityFactory;
import org.geotools.cs.CoordinateSystemFactory;
import org.geotools.cs.HorizontalDatum;
import org.geotools.cs.NoSuchAuthorityCodeException;
import org.geotools.cs.PrimeMeridian;
import org.geotools.ct.CannotCreateTransformException;
import org.geotools.ct.CoordinateTransformation;
import org.geotools.ct.CoordinateTransformationFactory;
import org.geotools.ct.MathTransform;
import org.geotools.ct.MathTransform2D;
import org.geotools.ct.MathTransformFactory;
import org.geotools.data.FeatureReader;
import org.geotools.factory.FactoryFinder;
import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypeFactory;
import org.geotools.feature.GeometryAttributeType;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.SchemaException;
import org.geotools.pt.CoordinatePoint;
import org.geotools.referencing.Factory;
import org.geotools.units.Unit;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CRSFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;
import org.opengis.spatialschema.geometry.MismatchedDimensionException;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Utility method isolating data source providers from CRS production.
 * <p>
 * This should be reworked as a Martins new CoordinateReferenceSystem work
 * comes along, it is factory based an should take care of most of the functionality
 * of this module.
 * </p>
 * <p>
 * A bit of vocab:
 * <ul>
 * <li>code: a string used to identify a CoordinateReferenceSystem, a code is usually made
 * of several parts: an authority, a number separated by a colon.
 * <li>authority: used at the start of a code to indicate which group is responsible for
 * the code definition
 * <li>CRS: shortcut used to indicate coordinate reference system
 * <li>CoordinateReferenceSystem: a geoapi interface indicating where CRS information is used.
 * <li>CoordinateSystem origional geotools CRS object from org.geotools.cs slated for replacement
 * in early 2005. CoordianteSystem implements CoordianteReferenceSystem to ease the transition
 * process.
 * <li>CoordinateReferenceSystem: a geotools implementation of CoordinateReferenceSystem currently
 * under development. 
 * </ul>
 * @author Jody Garnett, Refractions Research
 */
public class CRSService {

    /**
     * GeoGraphicCoordinateSystem sutiable for distance on sphere calcualtions.
     * <p>
     * We will use a geographic coordinate system,  i.e. one that use (longitude,latitude)
     * coordinates.   Latitude values are increasing north and longitude values area
     * increasing east.  Angular units are degrees and prime meridian is Greenwich.
     * Ellipsoid is WGS 84  (a commonly used one for remote sensing data and GPS).
     * </p>
     */
    static public final CoordinateReferenceSystem GEOGRAPHIC;
    static {
        CoordinateSystemFactory csFactory = CoordinateSystemFactory.getDefault();        
        /*
         * Construct the source CoordinateSystem.           Note that the Geotools library
         * provides simpler ways to construct geographic coordinate systems using default
         * values for some arguments.  But we show here the complete way in order to show
         * the range of possibilities and to stay closer to the OpenGIS's specification.
         */
        Unit       angularUnit = Unit.DEGREE;
        HorizontalDatum  datum = HorizontalDatum.WGS84;
        PrimeMeridian meridian = PrimeMeridian.GREENWICH;
        
        CoordinateReferenceSystem geographic;
        try {
            geographic = csFactory.createGeographicCoordinateSystem(
                    "geographic", angularUnit, datum, meridian, AxisInfo.LONGITUDE, AxisInfo.LATITUDE );
        } catch (FactoryException e) {
            geographic = null;
        }
        GEOGRAPHIC = geographic;
    }
    /**
     * List of CoordinateSystemAuthorityFactory.
     * <p>
     * With a normal geotools install plugins exist for AUTO and EPSG.
     * </p>
     */ 
	List authorities = new ArrayList();
    	
	/**
	 * Construct a CRSService (will find CRSAuthoritySpi on classpath).
	 * <p>
	 * Default Geotools install provides EPSG and AUTO authorities.
	 * </p>
	 */
	public CRSService(){
		// register( CSAUTOFactory.getDefault() );
		// register( CSEPSGFactory.getDefault() );
        Set available = new HashSet();
        Iterator it = FactoryFinder.factories(CRSAuthoritySpi.class);
        CoordinateSystemAuthorityFactory factory = null;
        
        while (it.hasNext()) {
            try {
                factory = (CoordinateSystemAuthorityFactory) it.next();
                register( factory );
            }
            catch( NoClassDefFoundError notFound ){
                System.err.println("Could not locate:"+notFound );
            }
            catch( Throwable t ){
                System.err.println("Could not register "+factory+":"+t );
            }
        }            
	}
	
	/**
	 * Returns a list of codes uniquely maps to a an available CRS.
	 * <p>
     * These codes include their authority information, because that is the
     * way things work.
     * </p>
     * 
	 * @return list of codes of available CRS
	 */
	public Set getCRSNames(){
        Set set = new TreeSet();        
        for( Iterator i=authorities.iterator(); i.hasNext(); ){
            Object factory = i.next();
            if( factory instanceof CRSAuthoritySpi) {
                CRSAuthoritySpi authority = (CRSAuthoritySpi) factory;
                set.addAll( authority.getCodes() );
            }
        }
        return set;
	}

    /**
     * Procides access to the decoding process provided by known
     * CRS authorities.
     * <p>
     * </p>
     * @return Decoded CRS or null if encoding technique was not known
     * @throws IOException If a known encoding was used in an incorrect manner 
     */
    public CoordinateReferenceSystem decode(String encoding) throws IOException {
        Set set = new TreeSet();        
        for( Iterator i=authorities.iterator(); i.hasNext(); ){
            Object factory = i.next();
            if( factory instanceof CRSAuthoritySpi) {
                CRSAuthoritySpi authority = (CRSAuthoritySpi) factory;
                CoordinateReferenceSystem crs = authority.decode( encoding );
                if( crs != null ) {
                    return crs;
                }
            }
        }
        return null;
    }
    
    /**
     * Allows runtime additions to the capabilities of CRSService.
     */
	public void register( CoordinateSystemAuthorityFactory factory ){
	    authorities.add( factory );			
	}
		
	/**
	 * Locate for CoordinateSystem for specific code.
	 * <p>
	 * Note the code needs to mention the authority.
	 * <pre><code>
	 * EPSG:1234
	 * AUTO:42001, ..., ..., ...
	 * </code></pre>
	 * </p>
	 * </p>
	 * Due to common use EPSG is now assumed as the authority
	 * if not otherwise specified.
	 * @param code
	 * @return coordinate system for the provided code
	 * @throws FactoryException
	 */
	CoordinateSystem createCoordinateSystem( String code ) throws FactoryException{
		int split = code.indexOf(":");
		String authority = "EPSG";
		if( split != -1 ){
		    authority = code.substring(0,split);
			code = code.substring( split+1 );
		}
		NoSuchAuthorityCodeException noCodeException = null;
		for( Iterator i=authorities.iterator(); i.hasNext(); ){
		    CoordinateSystemAuthorityFactory factory =
		        (CoordinateSystemAuthorityFactory) i.next();
		    
		    if( !factory.getAuthority().equals( authority ) ){
		        continue;
		    }
		    try {
		        CoordinateSystem cs = factory.createCoordinateSystem( code );
		        if( cs != null ) return cs;		        
		    }
		    catch (NoSuchAuthorityCodeException notFound){
		        noCodeException = notFound;
		        continue;
		    }		    
		}
		if( noCodeException== null ){
		    noCodeException = new NoSuchAuthorityCodeException( "Unabled to locate definition of '"+code+"'");
		}
		throw noCodeException;
	}
		
	/**
	 * Locate for CoordinateReferenceSystem for specific code.
	 * <p>
	 * Note the code needs to mention the authority.
	 * <pre><code>
	 * EPSG:1234
	 * AUTO:42001, ..., ..., ...
	 * </code></pre>
	 * </p>
	 * </p>
	 * Due to common use EPSG is now assumed as the authority
	 * if not otherwise specified.
	 * @param code
	 * @return coordinate system for the provided code
	 * @throws FactoryException
	 */	
	public CoordinateReferenceSystem createCRS( String code ) throws FactoryException {
	    CRSFactory fc=org.geotools.referencing.FactoryFinder.getCRSFactory();
	    return fc.createFromWKT(code);
//	    return createCoordinateSystem( code );
	}
	/** 
	 * A "safe" cast to the old CoordinateSystem class.
	 * 
	 * @param crs CoordinateReferenceSystem
	 * @return CoordinateSystem for provided CRS, or null if this is not posssible.
	 */
	public static CoordinateSystem cs( CoordinateReferenceSystem crs ){
	    if( crs instanceof CoordinateSystem  ){
	        return (CoordinateSystem) crs;
	    }
	    String wkt = crs.toWKT();
	    CoordinateSystemFactory factory = CoordinateSystemFactory.getDefault(); 
	    try {
            return factory.createFromWKT( wkt );
        } catch (FactoryException huh) {
            huh.printStackTrace();
            return null;
        }
	}
		
	public static MathTransform reproject( CoordinateReferenceSystem from, CoordinateReferenceSystem to, boolean bidimensionalTransform) throws CannotCreateTransformException{
	    return reproject( cs( from ), cs( to ), bidimensionalTransform );
	}
	
	/**
     * Returns a math transform for the specified transformations. If no
     * transformation is available, or if it is the identity transform, then
     * this method returns <code>null</code>. This method accepts null
     * argument.
     */
    public static MathTransform2D getMathTransform2D(
        final CoordinateTransformation transformation) {
        if (transformation != null) {
            final MathTransform transform = transformation.getMathTransform();

            if (!transform.isIdentity()) {
                return (MathTransform2D) transform;
            }
        }

        return null;
    }
	
	
	public static MathTransform reproject( CoordinateSystem from, CoordinateSystem to, boolean bidimensionalTransform) throws CannotCreateTransformException{
    	CoordinateTransformationFactory factory =
    	    CoordinateTransformationFactory.getDefault();
    	
        CoordinateTransformation transformation;

        transformation = factory.createFromCoordinateSystems( from, to );
        if(!bidimensionalTransform)
        	return transformation.getMathTransform();
        else
        	return getMathTransform2D(transformation);
    }
	
	public static MathTransform concatenate(MathTransform firstTransform, MathTransform secondTransform) {
		return MathTransformFactory.getDefault()
		                                     .createConcatenatedTransform(firstTransform, secondTransform);
	}
	
	/**
     * @param transform
     * @param at
     * @return
     */
    public static MathTransform2D concatenate(MathTransform2D transform, AffineTransform at) {
        MathTransformFactory factory = MathTransformFactory.getDefault();
        return (MathTransform2D) factory.createConcatenatedTransform(transform, factory.createAffineTransform(at));
    }

	
	static FeatureType transform( FeatureType schema, CoordinateReferenceSystem crs ) throws SchemaException {
        FeatureTypeFactory factory = FeatureTypeFactory.newInstance( schema.getTypeName() );
        
        try {
            factory.setNamespace( schema.getNamespace() );
        } catch (URISyntaxException e) {
            throw new SchemaException(e);
        }
        factory.setName( schema.getTypeName() );
        
        GeometryAttributeType defaultGeometryType = null;
        for( int i=0; i<schema.getAttributeCount(); i++ ){
            AttributeType attributeType = schema.getAttributeType( i );
            if( attributeType instanceof GeometryAttributeType ){
                GeometryAttributeType geometryType = (GeometryAttributeType) attributeType;
                GeometryAttributeType geometry;
                
                geometry = (GeometryAttributeType) AttributeTypeFactory.newAttributeType(
                        geometryType.getName(),
                        geometryType.getType(),
                        geometryType.isNillable(),
                        geometryType.getFieldLength(),
                        geometryType.createDefaultValue(),
                        crs
                	);
                
                if( defaultGeometryType == null || 
                    geometryType == schema.getDefaultGeometry() ){
                    defaultGeometryType = geometry;
                }
                factory.addType( geometry );                
            }
			else {
			    factory.addType( attributeType );
			}            
		}
		factory.setDefaultGeometry( defaultGeometryType );
		return factory.getFeatureType();
	}
	
	/**
	 * Applies transform to all geometry attribute.
	 * 
	 * @param feature Feature to be transformed
	 * @param schema Schema for target transformation - transform( schema, crs )
	 * @param transform MathTransform used to transform coordinates - reproject( crs, crs )
	 * @return transformed Feature of type schema
	 * @throws TransformException
	 * @throws MismatchedDimensionException
	 * @throws IllegalAttributeException
	 */
	static Feature transform( Feature feature, FeatureType schema, MathTransform transform ) throws MismatchedDimensionException, TransformException, IllegalAttributeException{
	    feature = schema.create( feature.getAttributes( null ), feature.getID() );
	    
	    GeometryAttributeType geomType = schema.getDefaultGeometry();
	    Geometry geom = (Geometry) feature.getAttribute( geomType.getName() );
	    
	    geom = transform( geom, transform );
	    
	    try {	        
            feature.setAttribute( geomType.getName(), geom );
        } catch (IllegalAttributeException shouldNotHappen) {
            // we are expecting the transform to return the same geometry type
        }
	    return feature;
	}
	
	public static Geometry transform( Geometry geom, MathTransform transform ) throws MismatchedDimensionException, TransformException{
	    if( transform.isIdentity() ) return geom;
	    if( geom instanceof LineString ){
	        return transform( (LineString) geom, transform );	        
	    }
	    if( geom instanceof MultiLineString ){
	        return transform( (MultiLineString) geom, transform );	        
	    }
	    if( geom instanceof Polygon ){
	        return transform( (Polygon) geom, transform );	        
	    }
	    if( geom instanceof Point ){
	        return transform( (Point) geom, transform );	        
	    }
	    if( geom instanceof MultiPoint ){
	        return transform( (MultiPoint) geom, transform );	        
	    }
	    if( geom instanceof MultiPolygon ){
	        return transform( (MultiPolygon) geom, transform );	        
	    }
	    return null; // could not transform!
	}
	
    /** Reproject provided bound evelope to lat/long */
    public static Envelope toGeographic( Envelope env, CoordinateReferenceSystem crs ) throws Exception {
        MathTransform transform = reproject( crs, GEOGRAPHIC, false );
        return transform( env, transform );
        
        /*
        CoordinateSystem cs = cs( crs );         
        String wkt = cs.toWKT();
        
        CoordinateSystemFactory csFactory = CoordinateSystemFactory.getDefault();
        org.geotools.cs.CoordinateSystem cs2 = csFactory.createFromWKT( wkt );
        Unit       angularUnit = Unit.DEGREE;
        HorizontalDatum  datum = HorizontalDatum.WGS84;
        org.geotools.cs.PrimeMeridian meridian = org.geotools.cs.PrimeMeridian.GREENWICH;
        GeographicCoordinateSystem geographic =
            csFactory.createGeographicCoordinateSystem("geographic", angularUnit, datum, meridian, AxisInfo.LONGITUDE, AxisInfo.LATITUDE );
        CoordinateTransformationFactory trFactory = CoordinateTransformationFactory.getDefault();
        CoordinateTransformation transformation = trFactory.createFromCoordinateSystems(cs2, geographic );
        MathTransform transform = transformation.getMathTransform();
        CoordinatePoint p1 = new CoordinatePoint( env.getMinX(), env.getMinY());
        CoordinatePoint p2 = new CoordinatePoint( env.getMaxX(), env.getMaxY());
        transform.transform( p1, p1 );
        transform.transform( p2, p2 );
        Envelope rebounds = new Envelope();
        Point2D point = p1.toPoint2D();
        rebounds.expandToInclude( point.getX(), point.getY() );
        point = p2.toPoint2D();
        rebounds.expandToInclude( point.getX(), point.getY() );     
        return rebounds;
        */
    }
    
	public static Envelope transform( Envelope envelope, MathTransform transform ) throws MismatchedDimensionException, TransformException {
		// This code does not provide an exact transform, since the transformed envelope may not
		// be a rectangle
//	    CoordinatePoint pt;
//	    Envelope bbox = new Envelope();
//	    pt = transform.transform( new CoordinatePoint( envelope.getMinX(), envelope.getMinY() ), null );
//	    bbox.expandToInclude( pt.getOrdinate( 0 ), pt.getOrdinate( 1 ));
//	    
//	    pt = transform.transform( new CoordinatePoint( envelope.getMaxX(), envelope.getMinY() ), null );
//	    bbox.expandToInclude( pt.getOrdinate( 0 ), pt.getOrdinate( 1 ));
//	    
//	    pt = transform.transform( new CoordinatePoint( envelope.getMaxX(), envelope.getMaxY() ), null );
//	    bbox.expandToInclude( pt.getOrdinate( 0 ), pt.getOrdinate( 1 ));
//	    
//	    pt = transform.transform( new CoordinatePoint( envelope.getMinX(), envelope.getMaxY() ), null );
//	    bbox.expandToInclude( pt.getOrdinate( 0 ), pt.getOrdinate( 1 ));
//	    
//	    return bbox;	 
		
		Rectangle2D rect = new Rectangle2D.Double(envelope.getMinX(), envelope.getMinY(), envelope.getWidth(), envelope.getHeight());
        Shape s = ((MathTransform2D) transform).createTransformedShape(rect);
        Rectangle2D tb = s.getBounds2D();
        return new Envelope(tb.getMinX(), tb.getMaxX(), tb.getMinY(), tb.getMaxY());
	}
	public static Point transform( Point point, MathTransform transform ) throws MismatchedDimensionException, TransformException {
	    GeometryFactory factory = point.getFactory();
	    	    
	    Coordinate coords[] = point.getCoordinateSequence().toCoordinateArray();
	    
	    for( int i=0; i<coords.length; i++ ){
	        CoordinatePoint pt = new CoordinatePoint( coords[i].x, coords[i].y  );
            pt = transform.transform( pt, null );
            coords[i].x = pt.getOrdinate( 0 );
            coords[i].y = pt.getOrdinate( 1 );            
	    }
	    return factory.createPoint( coords[0] );	    
	}	
	public static LineString transform( LineString line, MathTransform transform ) throws MismatchedDimensionException, TransformException {
	    GeometryFactory factory = line.getFactory();
	    Coordinate coords[] = line.getCoordinateSequence().toCoordinateArray();
	    
	    for( int i=0; i<coords.length; i++ ){
	        CoordinatePoint pt = new CoordinatePoint( coords[i].x, coords[i].y  );
            pt = transform.transform( pt, null );
            coords[i].x = pt.getOrdinate( 0 );
            coords[i].y = pt.getOrdinate( 1 );            
	    }
	    return factory.createLineString( coords );	    
	}
	public static LinearRing transform( LinearRing ring, MathTransform transform ) throws MismatchedDimensionException, TransformException {
	    GeometryFactory factory = ring.getFactory();
	    Coordinate coords[] = ring.getCoordinateSequence().toCoordinateArray();
	    
	    for( int i=0; i<coords.length; i++ ){
	        CoordinatePoint pt = new CoordinatePoint( coords[i].x, coords[i].y  );
            pt = transform.transform( pt, null );
            coords[i].x = pt.getOrdinate( 0 );
            coords[i].y = pt.getOrdinate( 1 );            
	    }
	    return factory.createLinearRing( coords );	    
	}
	public static Polygon transform( Polygon polygon, MathTransform transform ) throws MismatchedDimensionException, TransformException {
	    GeometryFactory factory = polygon.getFactory();
	    
	    LinearRing ring = transform( (LinearRing) polygon.getExteriorRing(), transform );
	    LinearRing  holes[] = new LinearRing[ polygon.getNumInteriorRing() ];
	    for( int i=0; i< holes.length; i++ ){
	        holes[i] = transform( (LinearRing) polygon.getInteriorRingN( i ), transform );
	    }
	    return factory.createPolygon( ring, holes );
	}
	
	public static MultiPoint transform( MultiPoint multi, MathTransform transform ) throws MismatchedDimensionException, TransformException {
	    GeometryFactory factory = multi.getFactory();
	    	    
	    Coordinate coords[] = multi.getCoordinates();
	    
	    for( int i=0; i<coords.length; i++ ){
	        CoordinatePoint pt = new CoordinatePoint( coords[i].x, coords[i].y  );
            pt = transform.transform( pt, null );
            coords[i].x = pt.getOrdinate( 0 );
            coords[i].y = pt.getOrdinate( 1 );            
	    }
	    return factory.createMultiPoint( coords );
	}
	public static MultiLineString transform( MultiLineString multi, MathTransform transform ) throws MismatchedDimensionException, TransformException {
	    GeometryFactory factory = multi.getFactory();
	    	    
	    LineString geoms[] = new LineString[ multi.getNumGeometries() ];
	    for( int i=0; i< geoms.length; i++ ){
	        geoms[i] = transform( (LineString) multi.getGeometryN( i ), transform );
	    }
	    return factory.createMultiLineString( geoms );
	}
	public static MultiPolygon transform( MultiPolygon multi, MathTransform transform ) throws MismatchedDimensionException, TransformException {
	    GeometryFactory factory = multi.getFactory();
	    	    
	    Polygon geoms[] = new Polygon[ multi.getNumGeometries() ];
	    for( int i=0; i< geoms.length; i++ ){
	        geoms[i] = transform( (Polygon) multi.getGeometryN( i ), transform );
	    }
	    return factory.createMultiPolygon( geoms );
	}
	
	/**
	 * Force GeometryAttributes to a user supplied CoordinateReferenceSystem
	 * <p>
	 * Opperates as a FeatureReader wrapper (well Decorator pattern since the result is
	 * also a FeatureReader).
	 * </p>
	 * Example Use:
	 * <pre><code>
	 * FeatureReader reader = CRSSerivce.readerForce( origionalReader, forceCS );
	 * 
	 * CoordinateReferenceSystem orgionalCS =
	 *     origionalReader.getFeatureType().getDefaultGeometry().getCoordinateSystem();
	 * 
	 * CoordinateReferenceSystem newCS =
	 *     reader.getFeatureType().getDefaultGeometry().getCoordinateSystem();
	 * 
	 * assertEquals( forceCS, newCS );
	 * </code></pre>
	 * </p>
	 *
	 * @author jgarnett, Refractions Research, Inc.
	 * @author $Author: jive $ (last modification)
	 */
	public static FeatureReader readerForce( FeatureReader reader, CoordinateReferenceSystem crs ) throws SchemaException {
	    return new ForceCoordinateSystemFeatureReader( reader, crs );
	}
	/**
	 * Reproject GeometryAttributes to a user supplied CoordinateReferenceSystem.
	 * </p>
	 * 
	 * <p>
	 * Example Use:
	 * <pre><code>
	 * FeatureReader reader = CRSService.readerReproject( origionalReader, newCS );
	 * 
	 * CoordinateReferenceSystem orgionalCS =
	 *     origionalReader.getFeatureType().getDefaultGeometry().getCoordinateSystem();
	 * 
	 * CoordinateReferenceSystem newCS =
	 *     reader.getFeatureType().getDefaultGeometry().getCoordinateSystem();
	 * 
	 * assertEquals( forceCS, newCS );
	 * </code></pre>
	 * </p>
	 * @throws SchemaException
	 * @throws CannotCreateTransformException
	 */
	public static FeatureReader readerReproject( FeatureReader reader, CoordinateReferenceSystem crs ) throws CannotCreateTransformException, SchemaException {
	    return new ReprojectFeatureReader( reader, crs );
	}
}
