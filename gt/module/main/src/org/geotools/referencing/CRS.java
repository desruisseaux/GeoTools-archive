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

// OpenGIS dependencies
import org.opengis.metadata.citation.Citation;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.CoordinateOperationFactory;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.CoordinateOperation;


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
     * @return MathTransform, or null if unavailable
     * @throws FactoryException only if MathTransform is unavailable due to error
     */
    public static MathTransform transform( final CoordinateReferenceSystem from, final CoordinateReferenceSystem to ) throws FactoryException {
        List list = visit( new OperationVisitor() {
            public Object factory( CoordinateOperationFactory factory ) throws FactoryException {
                CoordinateOperation opperation = factory.createOperation( from, to );                
                return opperation.getMathTransform();                
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
                if( t != null ){
                    // log trouble - as we can only throw the "last" cause
                    System.err.println( trouble );
                }
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
        else {
            if( trouble != null ){
                // log trouble - ie the last known cause of failure
                System.err.println( trouble );
            }
        }
        return list;
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
                System.out.println("Checking "+AUTHORITY+ " authority against "+authority );
                System.out.println(" is "+AUTHORITY+ " in "+authority.getIdentifiers() );
                System.out.println(" ..."+authority.getIdentifiers().contains( AUTHORITY ) );
                if( !authority.getIdentifiers().contains( AUTHORITY ) ) continue;
                
                System.out.println("Lookup "+code+ " authority "+factory.getClass().toString() );
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
}
