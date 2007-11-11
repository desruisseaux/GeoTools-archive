/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2003-2006, Geotools Project Managment Committee (PMC)
 *    (C) 2001, Institut de Recherche pour le Développement
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
package org.geotools.util.logging;

import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.geotools.resources.Arguments;


/**
 * Test the {@link MonolineFormatter} class.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public final class MonolineFormatterTest extends TestCase {
    /**
     * Returns the test suite.
     */
    public static Test suite() {
        return new TestSuite(MonolineFormatterTest.class);
    }

    /**
     * Constructs a test case with the given name.
     */
    public MonolineFormatterTest(final String name) {
        super(name);
    }

    /**
     * Set to {@code true} if this is run has a JUnit test
     * (i.e. not from the command line).
     */
    private static boolean runFromJUnit;

    /**
     * Set up common objects used for all tests. This initialization is performed by
     * JUnit, but is <strong>not</strong> performed when the test is run from the
     * command line. Instead, the initialization on command line is controled by
     * the optional "-init" argument.
     */
    @Override
    protected void setUp() throws Exception {
        runFromJUnit = true;
        super.setUp();
        Logging.GEOTOOLS.forceMonolineConsoleOutput();
    }

    /**
     * Run the test. This is only a visual test.
     */
    public void testInitialization() {
        if (runFromJUnit) {
            // Avoid polluting the output stream during JUnit tests.
            return;
        }
        final String[] namespaces = {
            "org.geotools.core",
            "org.geotools.resources",
            "org.geotools.referencing",
            "org.opengis.referencing"   // Non-geotools logger should not be affected.
        };
        for (int i=0; i<namespaces.length; i++) {
            System.out.println();
            System.out.print("Testing ");
            final Logger logger = Logger.getLogger(namespaces[i]);
            System.out.println(logger.getName());
            logger.severe ("Don't worry, just a test");
            logger.warning("This is an imaginary warning");
            logger.info   ("This is a pseudo-information message");
            logger.config ("Not really configuring anything...");
            logger.fine   ("This is a detailed (but useless) message\nWe log this one on two lines!");
            logger.finer  ("This is a debug message");
        }
    }

    /**
     * Run the test from the commande line.
     */
    public static void main(final String[] args) {
        final Arguments arguments = new Arguments(args);
        if (arguments.getFlag("-init")) {
            Logging.GEOTOOLS.forceMonolineConsoleOutput();
        }
        arguments.getRemainingArguments(0);
        new MonolineFormatterTest(null).testInitialization();
    }
}
