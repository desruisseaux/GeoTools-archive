/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Managment Committee (PMC)
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
package org.geotools.data.shapefile;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.geotools.resources.TestData;


/**
 * Ah, nothing like a test test.
 * <p>
 * Note: a previous version was used to test unzip in a folder with spaces. We can't test that
 *       part anymore, since the default {@link TestData#unzipFile} implementation always unzip
 *       files right in the directory that contains the ZIP file. Actually, this test suite is
 *       now more a test of {@link TestData} than a test of {@link TestCaseSupport}.
 *
 * @version $Id$
 * @author  Ian Schneider
 */
public class TestCaseSupportTest extends TestCase {
    /**
     * Creates a new instance of {@code TestCaseSupportTest}.
     */
    public TestCaseSupportTest() {
        super("TestCaseSupportTest");
    }

    /**
     * Run the test suite from the command line.
     */
    public static void main(java.lang.String[] args) {
        TestCaseSupport.verbose = true;
        junit.textui.TestRunner.run(new TestSuite(TestCaseSupportTest.class));
    }

    /**
     * Creates a {@link TestCaseSupport}, which should trig an unzip of the {@code data.zip} file.
     * Then, checks that new files has been created, and delete them.
     */
    public void testUnzipping() throws IOException {
        final File dir = TestData.file(this, null);
        assertTrue(dir.exists());
        assertTrue(dir.isDirectory());
        final List        preserve = Arrays.asList(dir.listFiles());
        final TestCaseSupport test = new TestCaseSupport("TestCaseSupport");
        final List        allFiles = Arrays.asList(dir.listFiles());
        final LinkedList  deleteMe = new LinkedList();
        deleteMe.addAll(allFiles);
        deleteMe.removeAll(preserve);
        assertFalse(preserve.isEmpty());
        assertTrue(allFiles.size() >= preserve.size());
        assertTrue(allFiles.size() >= deleteMe.size());
//      TODO: Uncomment the line below when we will be allowed to compile for J2SE 1.5.
//        assertFalse(Collections.disjoint(deleteMe, preserve));
        while (!deleteMe.isEmpty()) {
            final File c = (File) deleteMe.removeFirst();
            assertTrue(c.exists());
            if (c.isDirectory()) {
                deleteMe.addAll(Arrays.asList(c.listFiles()));
                deleteMe.addLast(c);
                continue;
            } 
            c.delete();
            assertFalse(c.exists());
        }
        final List remaining = Arrays.asList(dir.listFiles());
        assertEquals(preserve, remaining);
        
        // IF YOU DON'T DO THIS, THINGS ARE BAD FOR THE REST OF THE TEST CASES
        TestCaseSupport.prepared = false;
    }

    /**
     * Makes sure {@link TestCaseSupport} is ready for the following suites.
     */
    protected void tearDown() throws Exception {
        assertFalse(TestCaseSupport.prepared);
        super.tearDown();
    }
}
