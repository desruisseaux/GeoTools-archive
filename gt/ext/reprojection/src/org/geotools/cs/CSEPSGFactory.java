/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Managment Committee (PMC)
 * (C) 2001, Institut de Recherche pour le Développement
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
package org.geotools.cs;

// Database connection
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Properties;

import jj2000.j2k.NotImplementedError;

import org.geotools.measure.AngleFormat;
import org.geotools.cs.CoordinateSystem;
import org.geotools.units.Unit;


/**
 * Default implementation for a coordinate system factory backed
 * by the EPSG property file.
 */
public class CSEPSGFactory extends CoordinateSystemAuthorityFactory {
    /**
     * The default coordinate system authority factory.
     * Will be constructed only when first requested.
     */
    private static CSEPSGFactory DEFAULT;
    
    Properties epsg = new Properties();
    
    
    public CSEPSGFactory(final CoordinateSystemFactory factory ) {
    	this( factory, CSEPSGFactory.class.getResource("epsg.properties"));
    }
    /**
     * Construct an authority factory using the specified connection.
     *
     * @param factory    The underlying factory used for objects creation
     * @param defintion  The property file defining EPSG codes as WKT
     */
    public CSEPSGFactory(final CoordinateSystemFactory factory,
                       	 URL definition ) 
    {
        super(factory);
        Info.ensureNonNull("definition", definition );
        try {
        	epsg.load( definition.openStream() );
        }
        catch (IOException io ){
        	// could not load properties file
        }
    }    

    /**
     * Returns a default coordinate system factory backed by the EPSG property file.
     * 
     * @return The default factory.
     * @throws SQLException if the connection to the database can't be etablished.
     */
    public synchronized static CoordinateSystemAuthorityFactory getDefault() {
        if (DEFAULT == null) {        	
            DEFAULT = new CSEPSGFactory( CoordinateSystemFactory.getDefault() );
        }            
        return DEFAULT;
    }
   
    /**
     * Returns the authority name, which is <code>"EPSG"</code>.
     */
    public String getAuthority() {
        return "EPSG";
    }
	public Unit createUnit(String code) throws FactoryException {
		throw new NotImplementedError("Not implemented");
	}
	public Ellipsoid createEllipsoid(String code) throws FactoryException {
		throw new NotImplementedError("Not implemented");
	}
	public PrimeMeridian createPrimeMeridian(String code) throws FactoryException {
		throw new NotImplementedError("Not implemented");
	}
	public Datum createDatum(String code) throws FactoryException {
		throw new NotImplementedError("Not implemented");
	}
	/**
	 * Create Coordiante System from code.
	 * <p>
	 * Note the leading "EPSG:" part should of been removed by now.
	 * </p>
	 * @see org.geotools.cs.CoordinateSystemAuthorityFactory#createCoordinateSystem(java.lang.String)
	 */
	public CoordinateSystem createCoordinateSystem(String code) throws FactoryException {		
		if (code== null) {
			return null;
		}		
		if (code.startsWith("EPSG:")) { // EPSG:26907
			code = code.substring( 6 );
		}
		code = code.trim();
		String wkt = epsg.getProperty( code );
		if( wkt == null ) {
			throw new FactoryException("Unknonwn EPSG code: '"+code+"'" );
		}
		return factory.createFromWKT( code );
	}

}
