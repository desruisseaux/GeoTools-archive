/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
 *
 */
package org.geotools.data.oracle.sdo;

import java.sql.SQLException;

import org.geotools.data.oracle.OracleTestFixture;

import junit.framework.TestCase;

/**
 * @author jgarnett
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class SDOTest extends TestCase {
    GeometryFixture fixture;
    OracleTestFixture oracle;
	GeometryConverter converter;
    
    /**
     * Constructor for GeometryToJTSTest.
     * @param arg0
     */
    public SDOTest(String arg0) {
        super(arg0);
    }    
    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        fixture = new GeometryFixture();
        oracle = new OracleTestFixture();        
        converter = new GeometryConverter( oracle.connection );                
    }
    
    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        fixture = null;
        super.tearDown();
    }
    
    final public void testGType() throws SQLException {
        assertEquals( 2003, SDO.gType( fixture.rectangle ) );               
    }
    final public void testGTypeD() {
        assertEquals( 2, SDO.D( fixture.rectangle ) );        
    }
    final public void testGTypeL() {
        assertEquals( 0, SDO.L( fixture.rectangle ) );
    }
    final public void testGTypeTT() {
        assertEquals( 03, SDO.TT( fixture.rectangle ) );
    }
    final public void testSRID() throws SQLException {
        assertEquals( -1,  SDO.SRID( fixture.rectangle ) );
    }
    final public void testElemInfo() throws SQLException {
        int elemInfo[] = SDO.elemInfo( fixture.rectangle );
        assertEquals( 1, elemInfo[0] ); 
        assertEquals( 1003, elemInfo[1] );
        assertEquals( 3, elemInfo[2] );
    }
    final public void testElemInfoStartingOffset() {
        assertEquals( 1, SDO.elemInfoStartingOffset( fixture.rectangle ) );
    }
    final public void testElemInfoEType() {        
        assertEquals( 1003, SDO.elemInfoEType( fixture.rectangle));
    }
    final public void testGeometryElemInfoInterpretation() {
        assertEquals( 3, SDO.elemInfoInterpretation( fixture.rectangle));
    }
    final public void testOrdinates() throws SQLException {
        double ords[] = SDO.ordinates( fixture.rectangle );
        assertEquals( "length", 4, ords.length );
        assertEquals( "x1", 1, ords[0], 0.00001 );
        assertEquals( "y1", 1, ords[1], 0.00001 );
        assertEquals( "x2", 5, ords[2], 0.00001 );
        assertEquals( "y2", 7, ords[3], 0.00001 );
    }        
}