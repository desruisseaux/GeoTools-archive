/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2004, Geotools Project Managment Committee (PMC)
 * (C) 2004, Institut de Recherche pour le D�veloppement
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

import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * This is a simple utility class for making use of the
 * CoordinateReferenceSystem and assocaited Factory implementations.
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
 * provided by FactoryFinder in the most direct manner possible.
 * </p>
 * @author Jody Garnett, Refractions Research
 * @since 2.1.0
 */
public class CRS {

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
            throw new NoSuchAuthorityCodeException("No authority was defined - did you forget 'EPSG:number'?", "unknown", code );
        }
        final String AUTHORITY = code.substring( 0, split );
        Throwable trouble = null;
        for( Iterator i = .getAuthorityFactories().iterator(); i.hasNext(); ){
            AuthorityFactory factory = (AuthorityFactory) i.next();
            factory.getAuthority().getIdentifierTypes().contains( AUTHORITY );
            try {
                CoordinateReferenceSystem crs = (CoordinateReferenceSystem) factory.createObject( code );
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
