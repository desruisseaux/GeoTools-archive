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
package org.geotools.reprojection;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.opengis.referencing.FactoryException;

import org.geotools.cs.AxisInfo;
import org.geotools.cs.CSAUTOFactory;
import org.geotools.cs.CSEPSGFactory;
import org.geotools.cs.CoordinateSystem;
import org.geotools.cs.CoordinateSystemAuthorityFactory;
import org.geotools.cs.CoordinateSystemFactory;
import org.geotools.cs.Ellipsoid;
import org.geotools.cs.GeographicCoordinateSystem;
import org.geotools.cs.HorizontalDatum;
import org.geotools.cs.PrimeMeridian;
import org.geotools.pt.CoordinatePoint;
import org.geotools.resources.CTSUtilities;
import org.geotools.units.Unit;

/**
 * Front end for Geotools reprojection services.
 * <p>
 * This should be reworked as a Martins new CoordianteReferenceSystem work
 * comes along, it is factory based an should take care of most of the functionality
 * of this module.
 * </p>
 * @author Jody Garnett, Refractions Research
 */
public class ReprojectionService {
	Map authority = new HashMap();
	/**
	 * Default Reprojection Service using EPSG and AUTO.
	 */
	public ReprojectionService(){
		register( CSAUTOFactory.getDefault() );
		register( CSEPSGFactory.getDefault() );		
	}
	public void register( CoordinateSystemAuthorityFactory factory ){
		authority.put( factory.getAuthority(), factory );	
	}
		
	public CoordinateSystemAuthorityFactory authority( String key ){
		return (CoordinateSystemAuthorityFactory) authority.get( key );
	}
	/**
	 * Locate for CoordianteSystem for specific code.
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
	public CoordinateSystem createCoordinateSystem( String code ) throws FactoryException{
		int split = code.indexOf(":");
		String key = "EPSG";
		if( split != -1 ){
			key = code.substring(0,split);
			code = code.substring( split+1 );
		}		
		CoordinateSystemAuthorityFactory factory = authority( key );
		if( factory == null ){
			throw new FactoryException("Could not locate authority for '"+key+"'.");
		}		
		return factory.createCoordinateSystem( code );
	}  
}
