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
package org.geotools.util.logging;

import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Tests the {@link Logging} class.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class LoggingTest extends TestCase {
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
        return new TestSuite(LoggingTest.class);
    }

    /**
     * Constructs a test case with the given name.
     */
    public LoggingTest(final String name) {
        super(name);
    }

    /**
     * Checks {@link Logging#GEOTOOLS}.
     */
    public void testGeotools() {
        assertEquals("",             Logging.ALL.name);
        assertEquals("org.geotools", Logging.GEOTOOLS.name);
        assertEquals(0,              Logging.GEOTOOLS.getChildren().length);
        Logging[] children =         Logging.ALL.getChildren();
        assertEquals(1,              children.length);
        assertEquals("org",          children[0].name);
        assertSame(children[0],      Logging.getLogging("org"));
        children =                   children[0].getChildren();
        assertEquals(1,              children.length);
        assertSame(Logging.GEOTOOLS, children[0]);
        assertSame(Logging.ALL,      Logging.getLogging(""));
        assertSame(Logging.GEOTOOLS, Logging.getLogging("org.geotools"));
    }

    /**
     * Tests the redirection to Commons-logging.
     */
    public void testCommonsLogging() {
        assertTrue(LoggingFramework.COMMONS_LOGGING.isAvailable());
        try {
            Logging.GEOTOOLS.setLoggingFramework(LoggingFramework.COMMONS_LOGGING);
            Logger logger = Logging.getLogger("org.geotools");
            /*
             * 'logger' would be an instanceof from the Java logging framework if Log4J wasn't in
             * the classpath.  But because it is (with "provided" scope), Commons-logging chooses
             * it instead of the Java logging one.
             */
            assertTrue(logger instanceof CommonsLogger);
            /*
             * Tests level setting, ending with OFF in order to avoid
             * polluting the standard output stream with this test.
             */
            final org.apache.log4j.Logger log4j = org.apache.log4j.Logger.getLogger("org.geotools");
            final org.apache.log4j.Level oldLevel = log4j.getLevel();

            log4j.setLevel(org.apache.log4j.Level.WARN);
            assertEquals(Level.WARNING, logger.getLevel());
            assertTrue (logger.isLoggable(Level.WARNING));
            assertTrue (logger.isLoggable(Level.SEVERE));
            assertFalse(logger.isLoggable(Level.CONFIG));

            log4j.setLevel(org.apache.log4j.Level.DEBUG);
//          assertEquals(Level.FINER, logger.getLevel());
            // Commented-out because in older version of commons-logging, "trace" maps to "debug".
            assertTrue (logger.isLoggable(Level.FINER));
            assertTrue (logger.isLoggable(Level.SEVERE));

            log4j.setLevel(org.apache.log4j.Level.OFF);
            assertEquals(Level.OFF, logger.getLevel());

            logger.finest ("Message to Commons-logging at FINEST level.");
            logger.finer  ("Message to Commons-logging at FINER level.");
            logger.fine   ("Message to Commons-logging at FINE level.");
            logger.config ("Message to Commons-logging at CONFIG level.");
            logger.info   ("Message to Commons-logging at INFO level.");
            logger.warning("Message to Commons-logging at WARNING level.");
            logger.severe ("Message to Commons-logging at SEVERE level.");
            log4j.setLevel(oldLevel);
        } finally {
            Logging.GEOTOOLS.setLoggingFramework(LoggingFramework.JAVA_LOGGING);
            assertEquals(Logger.class, Logging.getLogger("org.geotools").getClass());
        }
    }

    /**
     * Tests the redirection to Log4J.
     */
    public void testLog4J() {
        assertTrue(LoggingFramework.LOG4J.isAvailable());
        try {
            Logging.GEOTOOLS.setLoggingFramework(LoggingFramework.LOG4J);
            Logger logger = Logging.getLogger("org.geotools");
            assertTrue(logger instanceof Log4JLogger);
            /*
             * Tests level setting, ending with OFF in order to avoid
             * polluting the standard output stream with this test.
             */
            final Level oldLevel = logger.getLevel();

            logger.setLevel(Level.WARNING);
            assertEquals(Level.WARNING, logger.getLevel());
            assertTrue (logger.isLoggable(Level.WARNING));
            assertTrue (logger.isLoggable(Level.SEVERE));
            assertFalse(logger.isLoggable(Level.CONFIG));

            logger.setLevel(Level.FINER);
            assertEquals(Level.FINER, logger.getLevel());
            assertEquals(Level.FINER, logger.getLevel());
            assertTrue (logger.isLoggable(Level.FINER));
            assertTrue (logger.isLoggable(Level.SEVERE));

            logger.setLevel(Level.OFF);
            assertEquals(Level.OFF, logger.getLevel());
            /*
             * WARNING: Don't test logging at FINEST level. For some mysterious reason,
             * Maven 2.0.6 executes the tests using Log4J 1.2.6 even if we declared and
             * compiled successfully the module with Log4J 1.2.12. Because the TRACE level
             * was not yet available in Log4J 1.2.6, attempts to test this level result in
             * a NoSuchMethodError: org.apache.log4j.Logger.trace(Ljava/lang/Object;)V
             */
            logger.finer  ("Message to Log4J at FINER level.");
            logger.fine   ("Message to Log4J at FINE level.");
            logger.config ("Message to Log4J at CONFIG level.");
            logger.info   ("Message to Log4J at INFO level.");
            logger.warning("Message to Log4J at WARNING level.");
            logger.severe ("Message to Log4J at SEVERE level.");
            logger.setLevel(oldLevel);
        } finally {
            Logging.GEOTOOLS.setLoggingFramework(LoggingFramework.JAVA_LOGGING);
            assertEquals(Logger.class, Logging.getLogger("org.geotools").getClass());
        }
    }
}
