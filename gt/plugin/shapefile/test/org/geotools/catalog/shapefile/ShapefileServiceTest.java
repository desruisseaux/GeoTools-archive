/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2006-2006, Geotools Project Managment Committee (PMC)
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
 */
package org.geotools.catalog.shapefile;

import java.io.FileNotFoundException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;

import junit.framework.TestCase;

import org.geotools.catalog.Service;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;

public class ShapefileServiceTest extends TestCase {

	/**
	 * Test that shapefile geo resource throws a FileNotFoundException when 
	 * the file does not exist.
	 * 
	 */
	public void testShapefileNotExists() throws Exception {
		
		URI uri = new URI("file:///home/nouser/nofile.shp"); 
		HashMap params = new HashMap();
		params.put( ShapefileDataStoreFactory.URLP.key, uri.toURL() );
		
		Service service = new ShapefileService( null, uri , params );
		service.getInfo( null );
		
		List members = service.members( null );
		assertEquals( 1, members.size() );
		
		ShapefileGeoResource resource = (ShapefileGeoResource) members.get( 0 );
		try {
			resource.getInfo( null );
			fail( "resource should have thrown IOException" );
		}
		catch( FileNotFoundException e ) {
			//ok
		}
	}
}
