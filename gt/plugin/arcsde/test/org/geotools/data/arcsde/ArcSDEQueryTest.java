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
package org.geotools.data.arcsde;

import junit.framework.TestCase;


/**
 * DOCUMENT ME!
 *
 * @author $author$
 * @version $Revision: 1.9 $
 */
public class ArcSDEQueryTest extends TestCase {

	private ArcSDEQuery query;
	
	/**
     * Constructor for ArcSDEQueryTest.
     *
     * @param arg0
     */
    public ArcSDEQueryTest(String name) {
        super(name);
    }

    /**
     * DOCUMENT ME!
     *
     * @param args DOCUMENT ME!
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(ArcSDEQueryTest.class);
    }

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
        this.query = null;
    }

    /**
     * DOCUMENT ME!
     */
    public void testCalculateResultCount() {
    	throw new UnsupportedOperationException("Implement!");
    }

    /**
     * DOCUMENT ME!
     */
    public void testCalculateQueryExtent() {
    	throw new UnsupportedOperationException("Implement!");
    }

    /**
     * DOCUMENT ME!
     */
    public void testClose() {
    	throw new UnsupportedOperationException("Implement!");
    }

    /**
     * DOCUMENT ME!
     */
    public void testFetch() {
    	throw new UnsupportedOperationException("Implement!");
    }

    /**
     * DOCUMENT ME!
     */
    public void testFetchRow() {
    	throw new UnsupportedOperationException("Implement!");
    }
}
