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

import org.geotools.cs.CoordinateSystem;
import org.geotools.cs.CoordinateSystemAuthorityFactory;

import org.opengis.referencing.FactoryException;

import junit.framework.TestCase;

/**
 * @author Jody Garnett
 */
public class ReprojectionServiceTest extends TestCase {
	
	private ReprojectionService service;

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		service = new ReprojectionService();
	}
	public void testEPSGAuthority() throws FactoryException {	
		CoordinateSystemAuthorityFactory epsg = service.authority("EPSG");
		assertNotNull( "EPSG", epsg );
		CoordinateSystem bc = epsg.createCoordinateSystem("42102");
		assertNotNull( "bc", bc );		
	}
	public void testCreateCoodinateSystemEPSG() throws FactoryException {
		assertNotNull( "bv", service.createCoordinateSystem("EPSG:42102") );
	}
	public void testAUTOAuthority() throws FactoryException {	
		CoordinateSystemAuthorityFactory auto = service.authority("AUTO");
		assertNotNull( "AUTO", auto );
		CoordinateSystem utm =
			auto.createCoordinateSystem("42001,0.0,0.0");
		assertNotNull( "auto-utm", utm );		
	}
	public void testCreateCoodinateSystemAUTO() throws FactoryException {
		assertNotNull( "auto-utm", service.createCoordinateSystem("AUTO:42001,0.0,0.0") );
	}

}
