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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.geotools.cs.CoordinateSystem;
import org.geotools.cs.CoordinateSystemAuthorityFactory;
import org.geotools.cs.NoSuchAuthorityCodeException;
import org.geotools.factory.FactoryFinder;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * Utility method isolating data source providers from CRS production.
 * <p>
 * This should be reworked as a Martins new CoordianteReferenceSystem work
 * comes along, it is factory based an should take care of most of the functionality
 * of this module.
 * </p>
 * @author Jody Garnett, Refractions Research
 */
public class CRSService {
    
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
	CoordinateSystem createCoordianteSystem( String code ) throws FactoryException{
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
	    return createCoordianteSystem( code );
	}
}
