/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004-2006, Geotools Project Managment Committee (PMC)
 *    (C) 2004, Institut de Recherche pour le Développement
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
package org.geotools.util;

// J2SE dependencies
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Locale;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.opengis.util.GenericName;


/**
 * Tests the various {@link InternationalString} implementations.
 *
 * @author Martin Desruisseaux
 * @source $URL$
 * @version $Id$
 */
public final class InternationalStringTest extends TestCase {
    /**
     * Run the suit from the command line.
     */
    public static void main(final String[] args) {
        org.geotools.util.Logging.GEOTOOLS.forceMonolineConsoleOutput();
        junit.textui.TestRunner.run(suite());
    }

    /**
     * Returns the test suite.
     */
    public static Test suite() {
        return new TestSuite(InternationalStringTest.class);
    }

    /**
     * Constructs a test case with the given name.
     */
    public InternationalStringTest(final String name) {
        super(name);
    }

    /**
     * Tests the {@link SimpleInternationalString} implementation.
     */
    public void testSimple() throws IOException, ClassNotFoundException {
        final String message = "This is an unlocalized message";
        final SimpleInternationalString toTest = new SimpleInternationalString(message);
        assertSame("Construction:", message, toTest.toString());
        basicTests(toTest);
    }

    /**
     * Tests the {@link SimpleInternationalString} implementation.
     */
    public void testGrowable() throws IOException, ClassNotFoundException {
        final String message     = "This is an unlocalized message";
        final String messageEn   = "This is a localized message";
        final String messageFr   = "Voici un message";
        final String messageFrCa = "Caribou!";
        GrowableInternationalString toTest = new GrowableInternationalString();
        basicTests(toTest);
        toTest.add(Locale.ENGLISH, message);
        assertSame("Addition:", message, toTest.toString());
        basicTests(toTest);
        
        toTest = new GrowableInternationalString(message);
        assertSame("Construction:", message, toTest.toString());
        basicTests(toTest);
        toTest.add(Locale.ENGLISH, messageEn);
        basicTests(toTest);
        toTest.add(Locale.FRENCH,  messageFr);
        basicTests(toTest);
        assertEquals("Unlocalized message:", message,   toTest.toString(null));
        assertEquals("English message:",     messageEn, toTest.toString(Locale.ENGLISH));
        assertEquals("French message:",      messageFr, toTest.toString(Locale.FRENCH));
        assertEquals("French message:",      messageFr, toTest.toString(Locale.CANADA_FRENCH));
        assertEquals("Other language:",      message,   toTest.toString(Locale.CHINESE));
        toTest.add(Locale.CANADA_FRENCH, messageFrCa);
        basicTests(toTest);
        assertEquals("Unlocalized message:", message,     toTest.toString(null));
        assertEquals("English message:",     messageEn,   toTest.toString(Locale.ENGLISH));
        assertEquals("French message:",      messageFr,   toTest.toString(Locale.FRENCH));
        assertEquals("French message:",      messageFrCa, toTest.toString(Locale.CANADA_FRENCH));
        assertEquals("Other language:",      message,     toTest.toString(Locale.CHINESE));
    }

    /**
     * Tests the {@link GenericName} implementation.
     */
    public void testName() throws IOException, ClassNotFoundException {
        final GenericName name = NameFactory.create("codespace:subspace:name");
        basicTests(name);
        assertEquals("toString:", "codespace:subspace:name", name.toString());
        assertEquals("toString:", "codespace:subspace",      name.getScope().toString());
        assertEquals("toString:", "codespace",               name.getScope().getScope().toString());
        assertSame("asScopedName", name, name.asScopedName());
        assertSame("asLocalName",  name, name.asLocalName().asScopedName());
    }

    /**
     * Performs basic test on the given object.
     */
    private void basicTests(final Comparable toTest) throws IOException, ClassNotFoundException {
        assertEquals("CompareTo: ", 0, toTest.compareTo(toTest));
        assertEquals("Equals:", toTest, toTest);
        if (toTest instanceof CharSequence) {
            // TODO: Uncomment when we will be allowed to use J2SE 1.5
//            assertEquals("CharSequence:", toTest.toString(),
//                         new StringBuffer((CharSequence) toTest).toString());
        }
        /*
         * Tests serialization
         */
        final ByteArrayOutputStream    out = new ByteArrayOutputStream();
        final ObjectOutputStream objectOut = new ObjectOutputStream(out);
        objectOut.writeObject(toTest);
        objectOut.close();
        
        final ByteArrayInputStream    in = new ByteArrayInputStream(out.toByteArray());
        final ObjectInputStream objectIn = new ObjectInputStream(in);
        final Object              object = objectIn.readObject();
        objectIn.close();

        assertEquals("Serialization:", toTest.getClass(), object.getClass());
        assertEquals("Serialization:", toTest,            object           );
        assertEquals("Hash code:",     toTest.hashCode(), object.hashCode());
    }
}
