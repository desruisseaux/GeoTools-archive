/*
 * Created on Jun 29, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.data.coverage.grid.file.test;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author jeichar
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
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
