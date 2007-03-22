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
package org.geotools.referencing;

// J2SE dependencies
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

// OpenGIS dependencies
import org.opengis.parameter.InvalidParameterValueException;
import org.opengis.util.InternationalString;

// Geotools dependencies
import org.geotools.metadata.iso.citation.CitationImpl;
import org.geotools.util.SimpleInternationalString;


/**
 * Tests the creation of {@link AbstractIdentifiedObject} and a few subclasses.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public final class IdentifiedObjectTest extends TestCase {
    /**
     * Run the suite from the command line.
     */
    public static void main(String[] args) {
        org.geotools.util.Logging.GEOTOOLS.forceMonolineConsoleOutput();
        junit.textui.TestRunner.run(suite());
    }

    /**
     * Returns the test suite.
     */
    public static Test suite() {
        return new TestSuite(IdentifiedObjectTest.class);
    }

    /**
     * Construct a test case.
     */
    public IdentifiedObjectTest(String testName) {
        super(testName);
    }

    /**
     * Tests {@link NamedIdentifier} attributes. Useful for making sure that the
     * hash code enumerated in the switch statement in the constructor have
     * the correct value.
     */
    public void testIdentifier() {
        final Map properties = new HashMap();
        assertNull(properties.put("code",          "This is a code"));
        assertNull(properties.put("authority",     "This is an authority"));
        assertNull(properties.put("version",       "This is a version"));
        assertNull(properties.put("dummy",         "Doesn't matter"));
        assertNull(properties.put("remarks",       "There is remarks"));
        assertNull(properties.put("remarks_fr",    "Voici des remarques"));
        assertNull(properties.put("remarks_fr_CA", "Pareil"));

        NamedIdentifier identifier = new NamedIdentifier(properties);
        assertEquals("code",          "This is a code",        identifier.getCode());
        assertEquals("authority",     "This is an authority",  identifier.getAuthority().getTitle().toString());
        //assertEquals("version",       "This is a version",     identifier.getVersion());
        assertEquals("remarks",       "There is remarks",      identifier.getRemarks().toString(Locale.ENGLISH));
        assertEquals("remarks_fr",    "Voici des remarques",   identifier.getRemarks().toString(Locale.FRENCH));
        assertEquals("remarks_fr_CA", "Pareil",                identifier.getRemarks().toString(Locale.CANADA_FRENCH));
        assertEquals("remarks_fr_BE", "Voici des remarques",   identifier.getRemarks().toString(new Locale("fr", "BE")));

        if (false) {
            // Disabled in order to avoid logging a warning (it disturb the JUnit output)
            properties.put("remarks", new SimpleInternationalString("Overrides remarks"));
            identifier = new NamedIdentifier(properties);
            assertEquals("remarks", "Overrides remarks", identifier.getRemarks().toString(Locale.ENGLISH));
        }

        assertNotNull(properties.remove("authority"));
        assertNull   (properties.put("AutHOrITY", new CitationImpl("An other authority")));
        identifier = new NamedIdentifier(properties);
        assertEquals("authority", "An other authority", identifier.getAuthority().getTitle().toString(Locale.ENGLISH));

        assertNotNull(properties.remove("AutHOrITY"));
        assertNull   (properties.put("authority", Locale.CANADA));
        try {
            identifier = new NamedIdentifier(properties);
            fail();
        } catch (InvalidParameterValueException exception) {
            // This is the expected exception
        }
    }

    /**
     * Test {@link IdentifiedObject}.
     */
    public void testIdentifiedObject() {
        final Map properties = new HashMap();
        assertNull(properties.put("name",             "This is a name"));
        assertNull(properties.put("remarks",          "There is remarks"));
        assertNull(properties.put("remarks_fr",       "Voici des remarques"));
        assertNull(properties.put("dummy",            "Doesn't matter"));
        assertNull(properties.put("dummy_fr",         "Rien d'intéressant"));
        assertNull(properties.put("local",            "A custom localized string"));
        assertNull(properties.put("local_fr",         "Une chaîne personalisée"));
        assertNull(properties.put("anchorPoint",      "Anchor point"));
        assertNull(properties.put("realizationEpoch", "Realization epoch"));
        assertNull(properties.put("validArea",        "Valid area"));

        final Map remaining = new HashMap();
        final AbstractIdentifiedObject reference = new AbstractIdentifiedObject(
                properties, remaining, new String[] {"local"});
        assertEquals("name",       "This is a name",         reference.getName().getCode());
        assertEquals("remarks",    "There is remarks",       reference.getRemarks().toString(null));
        assertEquals("remarks_fr", "Voici des remarques",    reference.getRemarks().toString(Locale.FRENCH));

        // Check extra properties
        assertEquals("Size:",    6,                    remaining.size());
        assertEquals("dummy",    "Doesn't matter",     remaining.get("dummy"));
        assertEquals("dummy_fr", "Rien d'intéressant", remaining.get("dummy_fr"));
        assertEquals("local",    "A custom localized string", ((InternationalString) remaining.get("local")).toString(null));
        assertEquals("local_fr", "Une chaîne personalisée",   ((InternationalString) remaining.get("local")).toString(Locale.FRENCH));
        assertFalse ("local_fr", remaining.containsKey("local_fr"));

        // Check the case of some special property keys
        assertEquals("anchorPoint",      "Anchor point",      remaining.get("anchorPoint"));
        assertEquals("realizationEpoch", "Realization epoch", remaining.get("realizationEpoch"));
        assertEquals("validArea",        "Valid area",        remaining.get("validArea"));
    }

    /**
     * Test {@link AbstractReferenceSystem}.
     */
    public void testReferenceSystem() {
        final Map properties = new HashMap();
        assertNull(properties.put("name",       "This is a name"));
        assertNull(properties.put("scope",      "This is a scope"));
        assertNull(properties.put("scope_fr",   "Valide dans ce domaine"));
        assertNull(properties.put("remarks",    "There is remarks"));
        assertNull(properties.put("remarks_fr", "Voici des remarques"));

        final AbstractReferenceSystem reference = new AbstractReferenceSystem(properties);
        assertEquals("name",          "This is a name",         reference.getName()   .getCode());
        assertEquals("scope",         "This is a scope",        reference.getScope()  .toString(null));
        assertEquals("scope_fr",      "Valide dans ce domaine", reference.getScope()  .toString(Locale.FRENCH));
        assertEquals("remarks",       "There is remarks",       reference.getRemarks().toString(null));
        assertEquals("remarks_fr",    "Voici des remarques",    reference.getRemarks().toString(Locale.FRENCH));
    }
}
