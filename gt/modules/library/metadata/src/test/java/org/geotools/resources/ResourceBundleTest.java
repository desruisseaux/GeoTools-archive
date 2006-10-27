/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2005-2006, Geotools Project Managment Committee (PMC)
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

// J2SE dependencies
import java.util.Locale;

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

// Geotools dependencies
import org.geotools.resources.i18n.Vocabulary;
import org.geotools.resources.i18n.VocabularyKeys;


/**
 * Tests the {@link ResourceBundle} class, especially {@link Vocabulary}.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class ResourceBundleTest extends TestCase {
    /**
     * Run the test from the command line.
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    /**
     * Returns the test suite.
     */
    public static Test suite() {
        return new TestSuite(ResourceBundleTest.class);
    }

    /**
     * Constructs a test case with the given name.
     */
    public ResourceBundleTest(String name) {
        super(name);
    }

    /**
     * Tests some simple vocabulary words.
     */
    public void testVocabulary() {
        Vocabulary resources;

        resources = Vocabulary.getResources(Locale.ENGLISH);
        assertSame(resources, Vocabulary.getResources(Locale.US));
        assertSame(resources, Vocabulary.getResources(Locale.UK));
        assertSame(resources, Vocabulary.getResources(Locale.CANADA));
        assertEquals("North", resources.getString(VocabularyKeys.NORTH));

        resources = Vocabulary.getResources(Locale.FRENCH);
        assertSame(resources, Vocabulary.getResources(Locale.FRANCE));
        assertSame(resources, Vocabulary.getResources(Locale.CANADA_FRENCH));
        assertEquals("Nord", resources.getString(VocabularyKeys.NORTH));
    }
}
