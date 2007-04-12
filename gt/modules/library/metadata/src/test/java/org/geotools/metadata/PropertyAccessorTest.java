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
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.opengis.metadata.citation.Citation;
import org.geotools.metadata.iso.citation.CitationImpl;
import org.geotools.metadata.iso.citation.Citations;


/**
 * Tests the {@link PropertyAccessor} class.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class PropertyAccessorTest extends TestCase {
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
        return new TestSuite(PropertyAccessorTest.class);
    }

    /**
     * Constructs a test case with the given name.
     */
    public PropertyAccessorTest(final String name) {
        super(name);
    }

    /**
     * Tests the constructor.
     */
    public void testConstructor() {
        final Citation citation = Citations.EPSG;
        PropertyAccessor accessor;
        try {
            accessor = new PropertyAccessor(citation.getClass(), "org.opengis.dummy");
            fail("No dummy interface expected.");
        } catch (ClassCastException e) {
            // This is the expected exception.
        }
        accessor = new PropertyAccessor(citation.getClass(), "org.opengis.metadata");
        assertEquals("Count of 'get' methods.", 14,  accessor.count());
    }

    /**
     * Tests the {@code indexOf} and {code name} methods.
     */
    public void testName() {
        final Citation citation = Citations.EPSG;
        final PropertyAccessor accessor =
                new PropertyAccessor(citation.getClass(), "org.opengis.metadata");
        assertEquals("Non-existent property",   -1,  accessor.indexOf("dummy"));
        assertEquals("getTitle() property", "title", accessor.name(accessor.indexOf("title")));
        assertEquals("getTitle() property", "title", accessor.name(accessor.indexOf("getTitle")));
        assertEquals("getTitle() property", "title", accessor.name(accessor.indexOf("TITLE")));
        assertEquals("getISBN() property",  "ISBN",  accessor.name(accessor.indexOf("ISBN")));
        assertNull(accessor.name(-1));
    }

    /**
     * Tests the get method.
     */
    public void testGet() {
        Citation citation = Citations.EPSG;
        final PropertyAccessor accessor =
                new PropertyAccessor(citation.getClass(), "org.opengis.metadata");
        final int index = accessor.indexOf("identifiers");
        assertTrue(String.valueOf(index), index >= 0);
        final Object identifiers = accessor.get(index, citation);
        assertNotNull(identifiers);
        assertTrue(identifiers instanceof Collection);
        assertTrue(((Collection) identifiers).contains("EPSG"));
    }

    /**
     * Tests the shallow equals and copy methods.
     */
    public void testEquals() {
        Citation citation = Citations.EPSG;
        final PropertyAccessor accessor =
                new PropertyAccessor(citation.getClass(), "org.opengis.metadata");

        assertFalse(accessor.shallowEquals(citation, Citations.GEOTIFF, true ));
        assertFalse(accessor.shallowEquals(citation, Citations.GEOTIFF, false));
        assertTrue (accessor.shallowEquals(citation, Citations.EPSG,    false));

        citation = new CitationImpl();
        assertTrue (accessor.shallowCopy  (Citations.EPSG, citation,    true ));
        assertFalse(accessor.shallowEquals(citation, Citations.GEOTIFF, true ));
        assertFalse(accessor.shallowEquals(citation, Citations.GEOTIFF, false));
        assertTrue (accessor.shallowEquals(citation, Citations.EPSG,    false));

        final int index = accessor.indexOf("identifiers");
        final Object source = accessor.get(index, Citations.EPSG);
        final Object target = accessor.get(index, citation);
        assertNotNull(source);
        assertNotNull(target);
        assertNotSame(source, target);
        assertEquals (source, target);
        assertTrue(((Collection) target).contains("EPSG"));

        assertSame(target, accessor.set(index, citation, null));
        final Object value = accessor.get(index, citation);
        assertNotNull(value);
        assertTrue(((Collection) value).isEmpty());

        try {
            accessor.shallowCopy(citation, Citations.EPSG, true);
            fail("Citations.EPSG should be unmodifiable.");
        } catch (UnmodifiableMetadataException e) {
            // This is the expected exception.
        }
    }
}
