/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004-2006, GeoTools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.referencing.crs;

// JSE dependencies
import java.util.Set;
import java.util.Iterator;
import org.geotools.referencing.FactoryFinder;

// OpenGIS dependencies
import org.opengis.metadata.citation.Citation;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

// Geotools dependencies
import org.geotools.referencing.CRS;
import org.geotools.referencing.NamedIdentifier;
import org.geotools.metadata.iso.citation.Citations;
import org.geotools.referencing.factory.esri.FactoryUsingWKT;

// JUnit dependencies
import junit.framework.TestCase;


/**
 * Test ESRI CRS support.
 * 
 * @source $URL$
 * @version $Id$
 * @author Jody Garnett
 */
public class FactoryUsingWktTest extends TestCase {
    /**
     * The factory to test.
     */
    private CRSAuthorityFactory factory;

    /**
     * Get the authority factory for ESRI.
     */
    protected void setUp() throws Exception {
        super.setUp();
        factory = FactoryFinder.getCRSAuthorityFactory("ESRI", null);
    }

    /**
     * Tests the authority code.
     */
    public void testAuthority(){
        Citation authority = factory.getAuthority();
        assertNotNull(authority);
//        assertEquals("European Petroleum Survey Group", authority.getTitle().toString());
        assertTrue(authority.getIdentifiers().contains("ESRI"));
        assertTrue(factory instanceof FactoryUsingWKT);
    }

    /**
     * Tests the vendor.
     */
    public void testVendor(){
        Citation vendor = factory.getVendor();        
        assertNotNull(vendor);
        assertEquals("Geotools", vendor.getTitle().toString());
    }

    /**
     * Tests the codes.
     */
    public void testCodes() throws FactoryException {
        Set codes = factory.getAuthorityCodes(CoordinateReferenceSystem.class);
        assertNotNull(codes);
        assertEquals(2707, codes.size());
        assertTrue(codes.contains("26910"));
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
        assertNotNull(crs.getIdentifiers());
        assertTrue(crs.getIdentifiers().size()>0);
        NamedIdentifier expected = new NamedIdentifier(Citations.EPSG, "42102");
        assertTrue( crs.getIdentifiers() .contains( expected ));
    }
    public void testSuccess() throws Exception {
        Set codes = factory.getAuthorityCodes( CoordinateReferenceSystem.class );
        int total = codes.size();
        int count = 0;
        
        for( Iterator i=codes.iterator(); i.hasNext(); ){           
            CoordinateReferenceSystem crs;
            String code = (String) i.next();
            try {
                crs = (CoordinateReferenceSystem) factory.createObject( code );
                if( crs != null ) count ++;                
            } catch (Throwable e) {
                System.err.println("WARNING (CRS: "+code+" ):"+e );
            }            
        }
        System.out.println( "success:" + count + "/" + total );                
    }
    

    
    /**
     * A random CRS for fun.
     */
    public void test26910Lower() throws Exception {
        CoordinateReferenceSystem crs = CRS.decode("epsg:26910");
        assertNotNull( crs );                
    }
    
    /**
     * A random CRS for fun.
     */
    public void test26986Lower() throws Exception {
        CoordinateReferenceSystem crs = CRS.decode("epsg:26986");
        assertNotNull( crs );                
    }
    
    /** wfs requires this to work */
    public void test4326Lower() throws Exception {
        CoordinateReferenceSystem crs = CRS.decode("epsg:4326");
        assertNotNull( crs );
    }
    /** wfs requires this to work */
    public void test26742Lower() throws Exception {
        CoordinateReferenceSystem crs = CRS.decode("epsg:26742");
        assertNotNull( crs );
    }
    /** wfs requires this to work */
    public void test4269Lower() throws Exception {
        CoordinateReferenceSystem crs = CRS.decode("epsg:4269");
        assertNotNull( crs );
    }
    /** wfs requires this to work */
    public void test42304Lower() throws Exception {
        CoordinateReferenceSystem crs = CRS.decode("epsg:42304");
        assertNotNull( crs );
    }
    /** wfs requires this to work */
    public void test42102Lower() throws Exception {
        CoordinateReferenceSystem crs = CRS.decode("epsg:42102");
        assertNotNull( crs );
        assertNotNull(crs.getIdentifiers());
        assertTrue(crs.getIdentifiers().size()>0);
        NamedIdentifier expected = new NamedIdentifier(Citations.EPSG, "42102");
        assertTrue( crs.getIdentifiers() .contains( expected ));
    }
}
