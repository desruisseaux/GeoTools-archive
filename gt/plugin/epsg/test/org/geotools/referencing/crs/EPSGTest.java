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

import java.util.Iterator;
import java.util.Set;

import junit.framework.TestCase;

import org.opengis.metadata.citation.Citation;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * These EPSG support.
 * 
 * @author Jody Garnett
 * @since 2.1.M3
 * @version 2.1.M3
 */
public class EPSGTest extends TestCase {
    EPSGCRSAuthorityFactory factory;
    /*
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        factory = new EPSGCRSAuthorityFactory();
    }    
    public void testAuthority(){
        Citation authority = factory.getAuthority();
        
        assertNotNull( authority );
        assertEquals( "European Petroleum Survey Group", authority.getTitle().toString() );
        assert( authority.getIdentifiers().contains( "EPSG" ) );                        
    }
    
    public void testVendor(){
        Citation vendor = factory.getVendor();        
        assertNotNull( vendor );
        assertEquals( "Geotools", vendor.getTitle().toString() );
        assert( vendor.getIdentifiers().contains( "EPSG" ) );                        
    }
    
    public void testCodes() throws Exception {
        Set codes = factory.getAuthorityCodes( CoordinateReferenceSystem.class );
        
        assertNotNull( codes );
        assertEquals( 2704, codes.size() );                               
    }
    
    /**
     * A random CRS for fun.
     */
    public void test26910() throws Exception {
        CoordinateReferenceSystem crs = (CoordinateReferenceSystem) factory.createObject("EPSG:26910");
        assertNotNull( crs );                
    }
    
    /** UDIG requires this to work */
    public void test4326() throws Exception {
        CoordinateReferenceSystem crs = (CoordinateReferenceSystem) factory.createObject("EPSG:4326");
        assertNotNull( crs );
    }
    /** UDIG requires this to work */
    public void test4269() throws Exception {
        CoordinateReferenceSystem crs = (CoordinateReferenceSystem) factory.createObject("EPSG:4269");
        assertNotNull( crs );
    }
    /** UDIG requires this to work */
    public void test42102() throws Exception {
        CoordinateReferenceSystem crs = (CoordinateReferenceSystem) factory.createObject("EPSG:42102");
        assertNotNull( crs );
    }
    public void testSuccess() throws Exception {
        Set codes = factory.getAuthorityCodes( CoordinateReferenceSystem.class );
        int total = codes.size();
        int count = 0;
        
        for( Iterator i=codes.iterator(); i.hasNext(); ){           
            CoordinateReferenceSystem crs;
            try {
                crs = (CoordinateReferenceSystem) factory.createObject( (String) i.next() );
                if( crs != null ) count ++;                
            } catch (Throwable e) {
                System.err.println("WARNING:"+e );
            }            
        }
        System.out.println( "success:" + count + "/" + total );                
    }
}
