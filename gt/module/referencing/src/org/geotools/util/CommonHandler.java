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
import java.util.Map;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

// Apache dependencies
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.impl.Jdk14Logger;

// Geotools dependencies
import org.geotools.resources.i18n.Logging;
import org.geotools.resources.i18n.LoggingKeys;


/**
 * An adapter that redirect all Java logging events to the Apache's commons logging framework.
 * This adapter is installed when the {@link #install} method is invoked.
 *
 * @since 2.4
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @see Logging
 */
final class CommonHandler extends Handler {
    /**
     * The Apache's log created up to date.
     */
    private final Map/*<String,Log>*/ loggers = new HashMap();

    /**
     * Creates a new handler.
     *
     * @param name   The name of the root logger.
     * @param logger The value of {@code LogFactory.getLog(name)}.
     */
    private CommonHandler(final String name, final Log logger) {
        /*
         * NOTE: We add the log now instead of waiting for the 'getLog' method because the
         * setting for the root logger (as performed by the 'install' method) is different
         * than the setting performed by the 'getLog' method for child loggers.
         */
        loggers.put(name, logger);
    }

    /**
     * Install or re-install the {@code CommonHandler} for the {@linkplain Logger logger} of the
     * specified name and its childs. The parent logger name is typically {@code "org.geotools"},
     * but could also be {@code ""} if events from other packages (including the log records from
     * J2SE itself) should be redirected as well.
     * <p>
     * This method should be invoked again when the logging configuration change. For example
     * if the <cite>Log4J</cite> configuration is dynamically changed at runtime, then this
     * method should be invoked again.
     * <p>
     * If this method determines that <cite>commons-logging</cite> would redirect all log events
     * to the java logging system, then this method do nothing and return {@code false}.
     *
     * @param  name The logger name, usually {@code ""} or {@code "org.geotools"}.
     * @return {@code true} if the adapter has been installed or re-installed, or
     *         {@code false} if this method did nothing.
     */
    public static synchronized boolean install(final String name) {
        final Log log = LogFactory.getLog(name);
        if (log instanceof Jdk14Logger) {
            return false;
        }
        final Logger root = Logger.getLogger(name);
        /*
         * Log to the Java logger before to redirect it,
         * so user can known where his logging are gone...
         */
        root.log(Logging.format(Level.CONFIG, LoggingKeys.REDIRECTED_TO_COMMONS_LOGGING));
        /*
         * Now process to the installation.
         */
        removeAllHandlers(root);
        root.addHandler(new CommonHandler(name, log));
        root.setLevel(Level.ALL);
        root.setUseParentHandlers(false);
        return true;
    }

    /**
     * Remove all handlers from the specified logger.
     */
    private static void removeAllHandlers(final Logger logger) {
        final Handler[] handlers = logger.getHandlers();
        for (int i=0; i<handlers.length; i++) {
            logger.removeHandler(handlers[i]);
        }
    }

    /**
     * Returns the logger (from the commons-logging package) of the specified name.
     * This method try to preserve acceptable performances in two ways:
     * <p>
     * <ul>
     *   <li>The {@linkplain Log commons-logging loggers} are cached using the name as the key.</li>
     *   <li>The {@linkplain Logger#setLevel Java logger level} are set according the
     *       commons-logging level.</li>
     * </ul>
     */
    private Log getLog(final String name) {
        synchronized (loggers) {
            Log log = (Log) loggers.get(name);
            if (log == null) {
                log = LogFactory.getLog(name);
                assert !(log instanceof Jdk14Logger);
                loggers.put(name, log);
                final Level level;
                if (log.isErrorEnabled()) {
                    level = Level.SEVERE;
                } else if (log.isWarnEnabled()) {
                    level = Level.WARNING;
                } else if (log.isInfoEnabled()) {
                    level = Level.INFO;
                } else if (log.isDebugEnabled()) {
                    level = Level.FINE;
                } else if (log.isTraceEnabled()) {
                    level = Level.FINER;
                } else {
                    level = Level.FINEST;
                }
                final Logger logger = Logger.getLogger(name);
                removeAllHandlers(logger);
                logger.setUseParentHandlers(true);
                logger.setLevel(level);
            }
            return log;
        }
    }

    /**
     * Send the specified record to Apache's commons-logging framework.
     */
    public void publish(final LogRecord record) {
        final Log       log       = getLog(record.getLoggerName());
        final int       level     = record.getLevel().intValue();
        final String    message   = record.getMessage();
        final Throwable throwable = record.getThrown();
        if (level >= Level.SEVERE.intValue()) {
            if (throwable != null) {
                log.error(message, throwable);
            } else {
                log.error(message);
            }
        } else if (level >= Level.WARNING.intValue()) {
            if (throwable != null) {
                log.warn(message, throwable);
            } else {
                log.warn(message);
            }
        } else if (level >= Level.INFO.intValue()) {
            if (throwable != null) {
                log.info(message, throwable);
            } else {
                log.info(message);
            }
        } else if (level >= Level.FINE.intValue()) {
            if (throwable != null) {
                log.debug(message, throwable);
            } else {
                log.debug(message);
            }
        } else {
            if (throwable != null) {
                log.trace(message, throwable);
            } else {
                log.trace(message);
            }
        }
    }

    /**
     * Flush this handler. The default implementation does nothing.
     */
    public void flush() {
    }

    /**
     * Closes this handler. The default implementation just invokes {@link #flush}.
     */
    public void close() {
        flush();
    }
}
