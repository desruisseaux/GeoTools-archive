/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2007, Geotools Project Managment Committee (PMC)
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
package org.geotools.metadata;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.opengis.metadata.citation.Citation;
import org.geotools.metadata.iso.citation.CitationImpl;
import org.geotools.metadata.iso.citation.Citations;


/**
 * Tests the {@link MetadataStandard} class.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class MetadataStandardTest extends TestCase {
    /**
     * Run the suit from the command line.
     */
    public static void main(final String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    /**
     * Returns the test suite.
     */
    public static Test suite() {
        return new TestSuite(MetadataStandardTest.class);
    }

    /**
     * Constructs a test case with the given name.
     */
    public MetadataStandardTest(final String name) {
        super(name);
    }

    /**
     * Tests the shallow equals and copy methods.
     */
    public void testEquals() {
        final MetadataStandard std = MetadataStandard.ISO_19115;
        Citation citation = Citations.EPSG;
        assertFalse(std.shallowEquals(citation, Citations.GEOTIFF, true ));
        assertFalse(std.shallowEquals(citation, Citations.GEOTIFF, false));
        assertTrue (std.shallowEquals(citation, Citations.EPSG,    false));

        citation = new CitationImpl();
        std.shallowCopy(Citations.EPSG, citation, true);
        assertFalse(std.shallowEquals(citation, Citations.GEOTIFF, true ));
        assertFalse(std.shallowEquals(citation, Citations.GEOTIFF, false));
        assertTrue (std.shallowEquals(citation, Citations.EPSG,    false));

        try {
            std.shallowCopy(citation, Citations.EPSG, true);
            fail("Citations.EPSG should be unmodifiable.");
        } catch (UnmodifiableMetadataException e) {
            // This is the expected exception.
        }
    }

    /**
     * Tests the {@link PropertyMap} implementation.
     */
    public void testMap() {
        final Citation citation = new CitationImpl(Citations.EPSG);
        final Map map = MetadataStandard.ISO_19115.asMap(citation);
        assertFalse(map.isEmpty());
        assertTrue (map.size() > 1);

        final Set keys = map.keySet();
        assertTrue ("Property exists and should be defined.",            keys.contains("title"));
        assertFalse("Property exists but undefined for Citations.EPSG.", keys.contains("ISBN"));
        assertFalse("Property do not exists.",                           keys.contains("dummy"));

        final String s = keys.toString();
        assertTrue (s.indexOf("title")       >= 0);
        assertTrue (s.indexOf("identifiers") >= 0);
        assertFalse(s.indexOf("ISBN")        >= 0);

        final Object identifiers = map.get("identifiers");
        assertTrue(identifiers instanceof Collection);
        assertTrue(((Collection) identifiers).contains("EPSG"));

        final Map copy = new HashMap(map);
        assertEquals(map, copy);
        map.remove("identifiers");
        assertFalse(map.equals(copy));
    }
}
