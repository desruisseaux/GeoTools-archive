/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004-2006, Geotools Project Managment Committee (PMC)
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
 *    Created on Jun 29, 2004
 */
package org.geotools.data.coverage.grid.file.test;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author jeichar
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 * @source $URL$
 */
public class FileGCSuite {

    public static Test suite() {
        TestSuite suite = new TestSuite(
                "Test for org.geotools.data.coverage.grid.file.test");
        //$JUnit-BEGIN$
        suite.addTestSuite(FSCatalogEntryTest.class);
        suite.addTestSuite(FileMetadataImplTest.class);
        //$JUnit-END$
        return suite;
    }
}
