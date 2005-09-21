/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2005, Geotools Project Managment Committee (PMC)
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
package org.geotools.referencing;

// J2SE dependencies
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

// OpenGIS dependencies
import org.geotools.factory.Hints;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.referencing.factory.FactoryGroup;
import org.opengis.metadata.citation.Citation;
import org.opengis.referencing.Factory;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.CoordinateOperationFactory;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.CoordinateOperation;

import com.vividsolutions.jts.geom.Coordinate;


/**
 * Simple utility class for making use of the {@link CoordinateReferenceSystem}
 * and associated {@link Factory} implementations.
 * <p>
 * Other proposals:
 * <ul>
 * <li>CoordinateReferenceSystem xml( String )
 * <li>CoordinateReferenceSystem wkt( String )
 * </ul>
 * </p>
 * <p>
 * <b>Note:</b> this utility class is made up of static final functions, this class is
 * not a Factory or a Builder. It makes use of the GeoAPI Factory interfaces
 * provided by {@link FactoryFinder} in the most direct manner possible.
 * </p>
 * @version $Id$
 * @author Jody Garnett, Refractions Research
 * @since 2.1.0
 */
public class CRS {
    /**
     * Implement this method to visit each available {@link CoordinateOperationFactory}
     * known to {@link FactoryFinder}.
     *
     * @version $Id$
     * @author Jody Garnett, Refractions Research
     * @since 2.1.0
     */
    public interface OperationVisitor {
        /**
         * Implement this method to visit each available CoordinateOperationFactory
         * known to FactoryFinder.
         * <p>
         * You may register additional Factories using META-INF/serivces
         * please see  
         * </p>
         * @param factory
         * @return Value created using the Factory, visit returns a list of these
         */
        public Object  factory( CoordinateOperationFactory factory ) throws FactoryException;
    }


    /**
     * Grab transform between two CoordianteReference Systems.
     * <p>
     * Sample use:<pre><code>
     * MathTransform transform = CRS.transform( CRS.decode("EPSG:42102"), CRS.decode("EPSG:4326") ); 
     * </code></pre>
     * </p>
     * 
     * @param from
     * @param to
     * @param lenientTransforms if true then the transforms created will not throw bursa wolf required exception during datum
     * shifts if the bursa wolf paramaters are not specified. Instead it will assume a no datum shift.
     * @return MathTransform, or null if unavailable
     * @throws FactoryException only if MathTransform is unavailable due to error
     */
    public static MathTransform transform( final CoordinateReferenceSystem from, final CoordinateReferenceSystem to, boolean lenientTransforms ) throws FactoryException {
        if( lenientTransforms )
            return FactoryFinder.getCoordinateOperationFactory(new Hints(Hints.LENIENT_DATUM_SHIFT, Boolean.TRUE)).createOperation(from, to).getMathTransform();
        else
            return transform(from, to);
    }
    /**
     * Grab transform between two CoordianteReference Systems.
     * <p>
     * Sample use:<pre><code>
     * MathTransform transform = CRS.transform( CRS.decode("EPSG:42102"), CRS.decode("EPSG:4326") ); 
     * </code></pre>
     * </p>
     * 
     * @param from
     * @param to
     * @return MathTransform, or null if unavailable
     * @throws FactoryException only if MathTransform is unavailable due to error
     */
    public static MathTransform transform( final CoordinateReferenceSystem from, final CoordinateReferenceSystem to ) throws FactoryException {
        List list = visit( new OperationVisitor() {
            public Object factory( CoordinateOperationFactory factory ) throws FactoryException {
                CoordinateOperation operation = factory.createOperation( from, to );                
                return operation.getMathTransform();                
            }
        });
        return list.isEmpty() ? null : (MathTransform) list.get(0);                         
    }
    
    /**
     * Visitor implementation is private until such time as Martin gives feedback.
     * 
     * TODO: Martin can you say aye or nay to this idea?
     */
    private static List visit(OperationVisitor visitor ) throws FactoryException {
        Throwable trouble = null;
        List list = new ArrayList();
        for( Iterator i = FactoryFinder.getCoordinateOperationFactories().iterator(); i.hasNext(); ){
            CoordinateOperationFactory factory = (CoordinateOperationFactory) i.next();
            try {
                Object value = visitor.factory( factory );
                if( value != null ) list.add( value );
            } catch (Throwable t ){                
                trouble = t;
            }            
        }        
        if( list.isEmpty()){
            if( trouble != null ) {
                // trouble is the last known cause of failure
                if( trouble instanceof FactoryException ) throw (FactoryException) trouble;            
                if( trouble instanceof Exception ) throw new FactoryException( (Exception) trouble );
                throw new FactoryException( "Trouble encountered while visiting CoordianteOpperationFactory", trouble );
            }
        }
        return list;
    }
    
    /**
     *   Get list of the codes that are supported by the authority.
     *   For example, "EPSG" -->   "EPSG:2000", "EPSG:2001", "EPSG:2002" because we know what
     *   they mean.
     */
    public static Set getSupportedCodes(String AUTHORITY)
    {
    	 TreeSet result = new TreeSet();
    	 for( Iterator i = FactoryFinder.getCRSAuthorityFactories().iterator(); i.hasNext(); )  //for each authority factory
    	 {        
            CRSAuthorityFactory factory = (CRSAuthorityFactory) i.next();  //grab a factory
            try {
                Citation authority = factory.getAuthority();  // what authorities does this factory produce?
                if( !authority.getIdentifiers().contains( AUTHORITY  ) ) continue;
                Set s = factory.getAuthorityCodes( CoordinateReferenceSystem.class );
                result.addAll(s);
            }
            catch (Exception e)
			{
            	e.printStackTrace(); // we hid errors - hopefully another factory will do the work for us.
			}
    	 }
    	 return result;
    }

    /** Parse WKT into a CRS object */
    public static CoordinateReferenceSystem parseWKT( String wkt ) throws FactoryException {
    	FactoryGroup factories = new FactoryGroup();
    	return factories.getCRSFactory().createFromWKT( wkt );
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
     * @param code
     * @return coordinate system for the provided code
     * @throws NoSuchAuthorityCodeException If the code could not be understood 
     */ 
    public static CoordinateReferenceSystem decode( String code ) throws NoSuchAuthorityCodeException {
        int split = code.indexOf(':');
        if( split == -1 ){
            throw new NoSuchAuthorityCodeException("No authority was defined - did you forget 'AUTHORITY:NUMBER'?", "unknown", code );
        }
        final String AUTHORITY = code.substring( 0, split ).trim().toUpperCase();
        Throwable trouble = null;
        
        // FIXME: FactoryFinder does not appear to work for other modules
        //
        for( Iterator i = FactoryFinder.getCRSAuthorityFactories().iterator(); i.hasNext(); ){        
            CRSAuthorityFactory factory = (CRSAuthorityFactory) i.next();
            try {
                Citation authority = factory.getAuthority();
                //System.out.println("Checking "+AUTHORITY+ " authority against "+authority );
                //System.out.println(" is "+AUTHORITY+ " in "+authority.getIdentifiers() );
                //System.out.println(" ..."+authority.getIdentifiers().contains( AUTHORITY ) );
                if( !authority.getIdentifiers().contains( AUTHORITY ) ) continue;
                
                //System.out.println("Lookup "+code+ " authority "+factory.getClass().toString() );
                CoordinateReferenceSystem crs = (CoordinateReferenceSystem) factory.createObject( code.toUpperCase() );
                if( crs != null ) return crs;
            } catch (FactoryException e) {
                trouble = e;
            }
            catch (Throwable e) {
                trouble = e;
            }            
        }
        
        if( trouble instanceof NoSuchAuthorityCodeException){
            throw (NoSuchAuthorityCodeException) trouble;
        }
        NoSuchAuthorityCodeException notFound = new NoSuchAuthorityCodeException( "Unabled to locate code", "not found", code); //$NON-NLS-1$ //$NON-NLS-2$
        if( trouble != null ) notFound.initCause( trouble );        
        throw notFound; 
    }
    
    /**
     * ESTIMATE the distance between the two points.
     *    1. transforms both points to lat/lon
     *    2. find the distance between the two points
     * 
     *  NOTE: we're using ellipsoid calculations.
     * 
     * @param p1   first point
     * @param p2   second point
     * @param crs  reference system the two points are in
     * @return approximate distance between the two points, in meters
     *
     * @todo Would like to move this method in {@link org.geotools.geometry.JTS} instead in order
     *       to avoid JTS dependency from the referencing module. Furthermore, we should also
     *       declare more specific exceptions and we may take advantage of the recent API
     *       enhancement in {@link GeodeticCalculator}. This method should also work with the
     *       CRS ellipsoid instead of the WGS84's one.
     */
    public static double distance(Coordinate p1, Coordinate p2, CoordinateReferenceSystem crs) throws Exception
	{
    	GeodeticCalculator gc = new GeodeticCalculator() ;  // WGS84
    	
    	double[] cs        = new double[4];
    	double[] csLatLong = new double[4];
    	cs[0] = p1.x;
    	cs[1] = p1.y;
    	cs[2] = p2.x;
    	cs[3] = p2.y;    	 
         
    	MathTransform transform = distanceOperationFactory.createOperation(crs,DefaultGeographicCRS.WGS84).getMathTransform();
    	transform.transform(cs, 0, csLatLong, 0, 2);
    	   //these could be backwards depending on what WSG84 you use
    	gc.setAnchorPoint(csLatLong[0],csLatLong[1]);
    	gc.setDestinationPoint(csLatLong[2],csLatLong[3]);
    
    	return gc.getOrthodromicDistance();
   }
    
    private final static CoordinateOperationFactory distanceOperationFactory;
    static {
        Hints hints=new Hints(Hints.LENIENT_DATUM_SHIFT, Boolean.TRUE);
        distanceOperationFactory=FactoryFinder.getCoordinateOperationFactory(hints);
    }
    
    
}
