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
package org.geotools.referencing.crs;

import junit.framework.TestCase;

import org.geotools.referencing.CRS;

/**
 * Ensure that FactoryFinder can cope.
 * 
 * @author Jody Garnett
 * @since 2.1.M3
 * @version 2.1.M3
 */
public class CRSAutoFactoryTest extends TestCase {
    /** UDIG requires this to work */
    public void test4326() throws Exception {
        assertNotNull( CRS.decode("EPSG:4326") );
    }
    /** UDIG requires this to work */
    public void test4269() throws Exception {
        assertNotNull( CRS.decode("EPSG:4269") );
        
    }
    /** UDIG requires this to work */
    public void test42102() throws Exception {
        assertNotNull( CRS.decode("EPSG:42102") );        
    }
   
}
