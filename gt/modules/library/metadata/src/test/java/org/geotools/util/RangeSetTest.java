/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2003-2006, Geotools Project Managment Committee (PMC)
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
package org.geotools.util;

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Test the {@link RangeSet} implementation.
 *
 * @source $URL$
 * @version $Id$
 * @author Andrea Aime
 */
public class RangeSetTest extends TestCase {
    /**
     * Run the suit from the command line.
     */
    public static void main(final String[] args) {
        org.geotools.util.Logging.GEOTOOLS.forceMonolineConsoleOutput();
        junit.textui.TestRunner.run(suite());
    }

    /**
     * Returns the test suite.
     */
    public static Test suite() {
        return new TestSuite(RangeSetTest.class);
    }
    
    /**
     * Constructs a test case with the given name.
     */
    public RangeSetTest(final String name) {
        super(name);
    }

    /**
     * Test {@link RangeSet#remove}.
     */
    public void testRangeRemoval() {
        RangeSet rs = new RangeSet(Double.class);
        rs.add(10.0, 22.0);
        rs.remove(8.0, 12.0);
        RangeSet rsResult = new RangeSet(Double.class);
        rsResult.add(12.0, 22.0);
        assertEquals("Lower removal:", rs, rsResult);
        
        rs.remove(20.0, 30.0);
        rsResult = new RangeSet(Double.class);
        rsResult.add(12.0, 20.0);
        assertEquals("Upper removal:", rs, rsResult);
        
        rs.remove(8.0, 10.0);
        assertEquals("Inferior null removal:", rs, rsResult);
        rs.remove(8.0, 12.0);
        assertEquals("Inferior touch removal:", rs, rsResult);
        
        rs.remove(22.0, 40.0);
        assertEquals("Upper null removal:", rs, rsResult);
        rs.remove(20.0, 40.0);
        assertEquals("Upper touch removal:", rs, rsResult);
        
        
        rs.remove(14.0, 16.0);
        rsResult = new RangeSet(Double.class);
        rsResult.add(12.0, 14.0);
        rsResult.add(16.0, 20.0);
        assertEquals("Central removal:", rs, rsResult);
        
        rs.remove(15.0, 15.5);
        assertEquals("Central null removal:", rs, rsResult);
        
        rs.remove(14.0, 16.0);
        assertEquals("Central touch null removal:", rs, rsResult);
        
        rs.remove(15.0, 17.0);
        rsResult = new RangeSet(Double.class);
        rsResult.add(12.0, 14.0);
        rsResult.add(17.0, 20.0);
        assertEquals("Central right removal:", rs, rsResult);
        
        rs.remove(13.0, 15.0);
        rsResult = new RangeSet(Double.class);
        rsResult.add(12.0, 13.0);
        rsResult.add(17.0, 20.0);
        assertEquals("Central left removal:", rs, rsResult);
        
        rs.remove(12.5, 18.0);
        rsResult = new RangeSet(Double.class);
        rsResult.add(12.0, 12.5);
        rsResult.add(18.0, 20.0);
        assertEquals("Central both removal:", rs, rsResult);
        
        rs.remove(18.5, 19.0);
        rsResult = new RangeSet(Double.class);
        rsResult.add(12.0, 12.5);
        rsResult.add(18.0, 18.5);
        rsResult.add(19.0, 20.0);
        assertEquals("Central removal 2:", rs, rsResult);
        
        rs.remove(17.0, 19.0);
        rsResult = new RangeSet(Double.class);
        rsResult.add(12.0, 12.5);
        rsResult.add(19.0, 20.0);
        assertEquals("Central wipeout:", rs, rsResult);
        
        rs.remove(0.0, 25.0);
        assertEquals("Full wipeout:", 0, rs.size());
    }
}
