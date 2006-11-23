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
import java.util.logging.LogRecord;
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
     * {@code true} if {@link #redirectToCommonsLogging} has been invoked.
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
     * @throws IllegalStateException is {@link #redirectToCommonsLogging} has been invoked.
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
     * @throws IllegalStateException is {@link #redirectToCommonsLogging} has been invoked.
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
    public synchronized boolean redirectToCommonsLogging() {
        try {
            if (CommonHandler.install(root)) {
                redirected = true;
                return true;
            }
        } catch (NoClassDefFoundError error) {
            // May occurs if commons-logging is not in the classpath.
            unexpectedException("org.geotools.util", "Logging", "redirectToCommonsLogging", error);
        }
        return false;
    }

    /**
     * Invoked when an unexpected error occurs. This method logs a message at the
     * {@link Level#WARNING WARNING} level to the specified logger. The originating
     * class name and method name are inferred from the error stack trace, using the
     * first {@linkplain StackTraceElement stack trace element} for which the class
     * name is inside a package or sub-package of the logger name. For example if
     * the logger name is {@code "org.geotools.image"}, then this method will uses
     * the first stack trace element where the fully qualified class name starts with
     * {@code "org.geotools.image"} or {@code "org.geotools.image.io"}, but not
     * {@code "org.geotools.imageio"}.
     *
     * @param  logger Where to log the error.
     * @param  error  The error that occured.
     * @return {@code true} if the error has been logged, or {@code false} if the logger
     *         doesn't log anything at the {@link Level#WARNING WARNING} level.
     */
    public static boolean unexpectedException(final Logger logger, final Throwable error) {
        if (logger.isLoggable(Level.WARNING)) {
            unexpectedException(logger.getName(), null, null, error);
            return true;
        }
        return false;
    }

    /**
     * Invoked when an unexpected error occurs. This method logs a message at the
     * {@link Level#WARNING WARNING} level to the logger for the specified package
     * name. The originating class name and method name can optionnaly be specified.
     * If any of them is {@code null}, then it will be inferred from the error stack
     * trace as in {@link #unexpectedException(Logger, Throwable)}.
     * <p>
     * Explicit value for class and method names are sometime preferred to automatic
     * inference for the following reasons:
     *
     * <ul>
     *   <li><p>Automatic inference is not 100% reliable, since the Java Virtual Machine
     *       is free to omit stack frame in optimized code.</p></li>
     *   <li><p>When an exception occured in a private method used internally by a public
     *       method, we sometime want to log the warning for the public method instead,
     *       since the user is not expected to know anything about the existence of the
     *       private method. If a developper really want to know about the private method,
     *       the stack trace is still available anyway.</p></li>
     * </ul>
     * 
     * @param paquet  The package where the error occurred, or {@code null}. This
     *                information is used for fetching an appropriate {@link Logger}
     *                for logging the error.
     * @param classe  The class where the error occurred, or {@code null}.
     * @param method  The method where the error occurred, or {@code null}.
     * @param error   The error.
     */
    public static void unexpectedException(String paquet, String classe, String method,
                                           final Throwable error)
    {
        final LogRecord record = Utilities.getLogRecord(error);
        if (paquet==null || classe==null || method==null) {
            final StackTraceElement[] elements = error.getStackTrace();
            for (int i=0; i<elements.length; i++) {
                final StackTraceElement e = elements[i];
                final String c = e.getClassName();
                if (paquet != null) {
                    if (!c.startsWith(paquet)) {
                        continue;
                    }
                    final int lg = paquet.length();
                    if (c.length()>lg && Character.isJavaIdentifierPart(c.charAt(lg))) {
                        continue;
                    }
                }
                if (classe != null) {
                    if (!c.endsWith(classe)) {
                        continue;
                    }
                    final int lg = c.length() - classe.length() - 1;
                    if (c.length()>=0 && Character.isJavaIdentifierPart(c.charAt(lg))) {
                        continue;
                    }
                }
                final String m = e.getMethodName();
                if (method != null) {
                    if (!m.equals(method)) {
                        continue;
                    }
                }
                final int separator = c.lastIndexOf('.');
                if (paquet == null) {
                    paquet = (separator >= 1) ? c.substring(0, separator-1) : "";
                }
                if (classe == null) {
                    classe = c.substring(separator + 1);
                }
                if (method == null) {
                    method = m;
                }
                break;
            }
        }
        record.setSourceClassName (classe);
        record.setSourceMethodName(method);
        record.setThrown          (error );
        Logger.getLogger(paquet).log(record);
    }
}
