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
 * Unit tests for transaction support
 *
 * @author $author$
 * @version $Revision: 1.9 $
 */
public class ArcSDEFeatureStoreTest extends TestCase {
	
	/**
	 * Tests the creation of new feature types, wich CRS
	 * and all.
	 */
	public void testCreateSchema(){
		throw new UnsupportedOperationException("Don't forget to implement");
	}
	
	/**
	 * Tests the writing of features with autocommit transaction
	 */
	public void testFeatureWriterAutoCommit(){
		throw new UnsupportedOperationException("Don't forget to implement");
	}

	/**
	 * Tests the writing of features with real transactions
	 */
	public void testFeatureWriterTransaction(){
		throw new UnsupportedOperationException("Don't forget to implement");
	}

	public void testFeatureWriterAppend(){
		throw new UnsupportedOperationException("Don't forget to implement");
	}
	
    /**
     * DOCUMENT ME!
     *
     * @param args DOCUMENT ME!
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(ArcSDEFeatureStoreTest.class);
    }
}
