/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2006, Geotools Project Managment Committee (PMC)
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
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.Handler;           // For javadoc
import java.util.logging.ConsoleHandler;    // For javadoc
import java.util.logging.SimpleFormatter;   // For javadoc

// Geotools dependencies
import org.geotools.resources.Utilities;


/**
 * A set of utilities method for configuring loggings in Geotools. Those methods operates on the
 * {@linkplain java.util.logging Java logging framework}, which is the framework used by Geotools.
 *
 * @since 2.4
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public final class Logging {
    /**
     * Logging configuration that applies only to Geotools packages.
     */
    public static final Logging GEOTOOLS = new Logging("org.geotools");

    /**
     * Logging configuration that applied to all packages.
     */
    public static final Logging ALL = new Logging("");

    /**
     * The root package.
     */
    private final String root;

    /**
     * {@code true} if {@link #redirectToCommonLogging} has been invoked.
     */
    private boolean redirected;

    /**
     * Creates an instance for the specified root package.
     */
    private Logging(final String root) {
        this.root = root;
    }

    /**
     * Configures the default {@linkplain ConsoleHandler console handler} in order to log records
     * on a single line instead of two lines. More specifically, for each {@link ConsoleHandler}
     * using a {@link SimpleFormatter}, this method replaces the simple formatter by an instance
     * of {@link MonolineFormatter}. If no {@link ConsoleHandler} are found, then a new one is
     * created.
     * <p>
     * Note that {@link MonolineFormatter} writes to the {@linkplain System#out standard
     * output stream} instead of the {@linkplain System#err standard error stream}.
     *
     * @throws IllegalStateException is {@link #redirectToCommonLogging} has been invoked.
     */
    public void forceMonolineConsoleOutput() throws IllegalStateException {
        forceMonolineConsoleOutput(null);
    }

    /**
     * Same as {@link #forceMonolineConsoleOutput()}, but additionnaly set an optional logging
     * level. If the specified level is non-null, then all {@link Handler}s using the monoline
     * formatter will be set to the specified level.
     * <p>
     * <b>Note:</b> Avoid this method as much as possible, since it overrides user's level
     * setting. A user trying to configure his logging properties may find confusing to see
     * his setting ignored.
     *
     * @throws IllegalStateException is {@link #redirectToCommonLogging} has been invoked.
     */
    public synchronized void forceMonolineConsoleOutput(final Level level)
            throws IllegalStateException
    {
        if (redirected) {
            throw new IllegalStateException();
        }
        final MonolineFormatter f = MonolineFormatter.init(root, level);
        // As of new MonolineFormatter.init(...) specification, 'f' should never be null.
        if (f.getSourceFormat() == null) {
            // Set the source format only if the user didn't specified
            // an explicit one in the jre/lib/logging.properties file.
            f.setSourceFormat("class:long");
        }
    }

    /**
     * Redirects {@linkplain java.util.logging Java logging} events to the Apache's
     * <A HREF="http://jakarta.apache.org/commons/logging/">commons-logging</A> framework.
     * <p>
     * This method should be invoked again when the common-logging configuration change. For
     * example if the <cite>Log4J</cite> configuration is dynamically changed at runtime, then
     * this method should be invoked again.
     * <p>
     * If this method determines that commons-logging would redirect all log events
     * to the java logging system, then this method do nothing and return {@code false}.
     *
     * @return {@code true} if the adapter has been installed or re-installed, or
     *         {@code false} if this method did nothing.
     */
    public synchronized boolean redirectToCommonLogging() {
        try {
            if (CommonHandler.install(root)) {
                redirected = true;
                return true;
            }
        } catch (NoClassDefFoundError error) {
            // May occurs if commons-logging is not in the classpath.
            Utilities.unexpectedException("org.geotools.util", "Logging",
                    "redirectToCommonLogging", error);
        }
        return false;
    }
}
