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
package org.geotools.resources;

import org.geotools.util.*;
import java.io.*;
import java.util.Set;
import java.util.HashSet;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Tests the {@link Classes} static methods.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public final class ClassesTest extends TestCase {
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
        return new TestSuite(ClassesTest.class);
    }

    /**
     * Constructs a test case with the given name.
     */
    public ClassesTest(final String name) {
        super(name);
    }

    /**
     * Tests {@link Classes#mostSpecificClass} and {@link Classes#commonClass}.
     */
    public void testCommonParent() {
        final Set<Object> types = new HashSet<Object>();

        assertTrue(types.add(new NotSerializableException()));
        assertEquals(NotSerializableException.class, Classes.commonClass     (types));
        assertEquals(NotSerializableException.class, Classes.specializedClass(types));

        assertTrue(types.add(new InvalidObjectException(null)));
        assertEquals(ObjectStreamException.class, Classes.commonClass     (types));
        assertEquals(ObjectStreamException.class, Classes.specializedClass(types));

        assertTrue(types.add(new FileNotFoundException()));
        assertEquals(IOException.class, Classes.commonClass     (types));
        assertEquals(IOException.class, Classes.specializedClass(types));

        assertTrue(types.add(new IOException()));
        assertEquals(IOException.class, Classes.commonClass     (types));
        assertEquals(IOException.class, Classes.specializedClass(types));

        assertTrue(types.add(new Exception()));
        assertEquals(  Exception.class, Classes.commonClass     (types));
        assertEquals(IOException.class, Classes.specializedClass(types));
    }

    /**
     * Tests {@link Classes#sameInterfaces}.
     */
    @SuppressWarnings("unchecked") // We break consistency on purpose for one test.
    public void testSameInterfaces() {
        assertTrue (Classes.sameInterfaces(StringBuilder.class, String.class, CharSequence.class));
        assertTrue (Classes.sameInterfaces(StringBuilder.class, String.class, Serializable.class));
        assertFalse(Classes.sameInterfaces((Class)  File.class, String.class, CharSequence.class));
        assertTrue (Classes.sameInterfaces(         File.class, String.class, Serializable.class));
    }
}
