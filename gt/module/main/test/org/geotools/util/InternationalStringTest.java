/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2004, Geotools Project Managment Committee (PMC)
 * (C) 2004, Institut de Recherche pour le Développement
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
package org.geotools.util;

// J2SE dependencies
import java.io.*;
import java.util.Locale;

// OpenGIS dependencies
import org.opengis.util.LocalName;
import org.opengis.util.ScopedName;
import org.opengis.util.GenericName;

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Tests the various {@link InternationalString} implementations.
 *
 * @author Martin Desruisseaux
 * @version $Id$
 */
public class InternationalStringTest extends TestCase {
    /**
     * Run the suit from the command line.
     */
    public static void main(final String[] args) {
        org.geotools.util.MonolineFormatter.initGeotools();
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
            assertEquals("CharSequence:", toTest.toString(),
                         new StringBuffer((CharSequence) toTest).toString());
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
