/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2005, Geotools Project Managment Committee (PMC)
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
package org.geotools.maven.taglet;

// J2SE dependencies
import java.util.regex.Matcher;

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Tests the {@link Source} taglet.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class SourceTest extends TestCase {
    /**
     * Run the suite from the command line.
     */
    public static void main(final String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    /**
     * Returns the test suite.
     */
    public static Test suite() {
        return new TestSuite(SourceTest.class);
    }

    /**
     * Constructs a test case with the given name.
     */
    public SourceTest(final String name) {
        super(name);
    }

    /**
     * Tests the regular expression validity using the tag for this source file.
     */
    public void testCurrentTag() {
        Source  s = new Source();
        Matcher m;
        String tag, url, category, module;
        tag = "$URL$";
        m = s.findURL.matcher(tag);
        assertTrue(m.matches());

        // Try to match the URL provided by SVN.
        url = m.group(1).trim();
        m = s.findModule.matcher(url);
        assertTrue(m.matches());
        category = m.group(1);
        module   = m.group(2);
        assertEquals("maven", category);
        assertEquals("javadoc", module);

        // Try an other URL from a tag.
        url = "http://svn.geotools.org/geotools/tags/2.2.RC0/module/api/src/org/geotools/catalog/ResolveChangeListener.java";
        m = s.findModule.matcher(url);
        assertTrue(m.matches());
        category = m.group(1);
        module   = m.group(2);
        assertEquals("module", category);
        assertEquals("api", module);

        // Try an other URL from a tag.
        url = "http://svn.geotools.org/geotools/tags/2.2-RC4/module/referencing/src/org/geotools/referencing/CRS.java";
        tag = Source.SVN_KEYWORD_DELIMITER + "URL: " + url + ' ' + Source.SVN_KEYWORD_DELIMITER;
        m = s.findURL.matcher(tag);
        assertTrue(m.matches());
        assertEquals(url, m.group(1).trim());
        m = s.findModule.matcher(url);
        assertTrue(m.matches());
        category = m.group(1);
        module   = m.group(2);
        assertEquals("module", category);
        assertEquals("referencing", module);
    }
}
