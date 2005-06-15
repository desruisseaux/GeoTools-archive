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
package org.geotools.data.mif;

import com.vividsolutions.jts.io.ParseException;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;


/**
 * DOCUMENT ME!
 *
 * @author Luca S. Percich, AMA-MI
 */
public class MIFFileTokenizerTest extends TestCase {
    MIFFileTokenizer tok = null;

    /**
     * DOCUMENT ME!
     *
     * @param args DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public static void main(java.lang.String[] args) throws Exception {
        junit.textui.TestRunner.run(new TestSuite(MIFFileTokenizerTest.class));
    }

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        tok = new MIFFileTokenizer(new BufferedReader(
                    new FileReader(
                        new File(MIFTestUtils.getDataPath()
                            + "MIFFileTokenizer.txt"))));
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        tok = null;
        super.tearDown();
    }

    /*
     * Class under test for boolean readLine()
     */
    public void testReadLine() {
        assertEquals(true, tok.readLine());

        try {
            assertEquals("one", tok.getToken(' '));
            assertEquals("two", tok.getToken(' '));
            assertEquals("three", tok.getToken(' ', true, false));
            assertEquals(2, tok.getLineNumber());
        } catch (ParseException e) {
            fail(e.getMessage());
        }
    }

    /**
     * DOCUMENT ME!
     */
    public void testGetLineNumber() {
        assertEquals(true, tok.readLine());
        assertEquals(true, tok.readLine());
        assertEquals(2, tok.getLineNumber());
    }
}
