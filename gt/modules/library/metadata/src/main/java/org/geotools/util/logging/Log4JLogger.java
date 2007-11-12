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


/**
 * An adapter that redirect all Java logging events to the Apache's Log4J framework.
 *
 * @since 2.4
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Saul Farber
 *
 * @see Logging
 */
final class Log4JLogger extends LoggerAdapter {
    /**
     * The Log4J logger to use.
     */
    private final org.apache.log4j.Logger logger;

    /**
     * Creates a new logger.
     *
     * @param name   The logger name.
     * @param logger The result of {@code Logger.getLogger(name)}.
     */
    public Log4JLogger(final String name, final org.apache.log4j.Logger logger) {
        super(name);
        this.logger = logger;
    }

    /**
     * Returns the Log4J level for the given Java level.
     */
    private static org.apache.log4j.Level toLog4JLevel(final Level level) {
        final int n = level.intValue();
        switch (n / 100) {
            default: {
                // MAX_VALUE is a special value for Level.OFF. Otherwise and
                // if positive, log to fatal since we are greater than SEVERE.
                return (n != Integer.MAX_VALUE && n >= 0) ?
                    org.apache.log4j.Level.FATAL : org.apache.log4j.Level.OFF;
            }
            case 10: return org.apache.log4j.Level.ERROR;    // SEVERE
            case  9: return org.apache.log4j.Level.WARN;     // WARNING
            case  8:                                         // INFO
            case  7: return org.apache.log4j.Level.INFO;     // CONFIG
            case  6:                                         // (not allocated)
            case  5:                                         // FINE
            case  4: return org.apache.log4j.Level.DEBUG;    // FINER
            case  3: return org.apache.log4j.Level.TRACE;    // FINEST
            case  2:                                         // (not allocated)
            case  1:                                         // (not allocated)
            case  0: return org.apache.log4j.Level.OFF;      // OFF
        }
    }

    /**
     * Returns the Java level for the given Log4J level.
     */
    private static Level toJavaLevel(final org.apache.log4j.Level level) {
        final int n = level.toInt();
        if (n != org.apache.log4j.Level.OFF_INT) {
            if (n >= org.apache.log4j.Level.ERROR_INT) return Level.SEVERE;
            if (n >= org.apache.log4j.Level.WARN_INT)  return Level.WARNING;
            if (n >= org.apache.log4j.Level.INFO_INT)  return Level.CONFIG;
            if (n >= org.apache.log4j.Level.DEBUG_INT) return Level.FINER;
            if (n >= org.apache.log4j.Level.TRACE_INT) return Level.FINEST;
            if (n == org.apache.log4j.Level.ALL_INT)   return Level.ALL; // Really ==, not >=.
        }
        return Level.OFF;
    }

    /**
     * Set the level for this logger.
     */
    public void setLevel(final Level level) {
        logger.setLevel(toLog4JLevel(level));
    }

    /**
     * Returns the level for this logger.
     */
    public Level getLevel() {
        return toJavaLevel(logger.getEffectiveLevel());
    }

    /**
     * Returns {@code true} if the specified level is loggable.
     */
    public boolean isLoggable(final Level level) {
        return logger.isEnabledFor(toLog4JLevel(level));
    }

    /**
     * Logs a record at the specified level.
     */
    @Override
    @SuppressWarnings("fallthrough")
    public void log(final Level level, final String message, final Throwable thrown) {
        final int n = level.intValue();
        switch (n / 100) {
            default: {
                // MAX_VALUE is a special value for Level.OFF. Otherwise and
                // if positive, log to fatal since we are greater than SEVERE.
                if (n != Integer.MAX_VALUE || n >= 0) {
                    logger.fatal(message, thrown);
                }
                break;
            }
            case 10: logger.error(message, thrown); break;  // SEVERE
            case  9: logger.warn (message, thrown); break;  // WARNING
            case  8:                                        // INFO
            case  7: logger.info (message, thrown); break;  // CONFIG
            case  6:                                        // (not allocated)
            case  5:                                        // FINE
            case  4: logger.debug(message, thrown); break;  // FINER
            case  3: logger.trace(message, thrown); break;  // FINEST
            case  2:                                        // (not allocated)
            case  1:                                        // (not allocated)
            case  0:                                        // OFF
        }
    }

    public void severe (String message) {logger.error(message);}
    public void warning(String message) {logger.warn (message);}
    public void info   (String message) {logger.info (message);}
    public void config (String message) {logger.info (message);}
    public void fine   (String message) {logger.debug(message);}
    public void finer  (String message) {logger.debug(message);}
    public void finest (String message) {logger.trace(message);}




    /**
     * Factory for {@link Log4JLogger}.
     *
     * @since 2.4
     * @source $URL$
     * @version $Id$
     * @author Martin Desruisseaux
     */
    static final class Factory extends LoggerFactory {
        /**
         * The unique instance of this factory.
         */
        private static Factory factory;

        /**
         * Do not allows more than instantiation of this class.
         */
        private Factory() {
        }

        /**
         * Returns the unique instance of this factory.
         */
        public static synchronized Factory getInstance() {
            if (factory == null) {
                factory = new Factory();
            }
            return factory;
        }

        /**
         * Returns the implementation to use for the logger of the specified name,
         * or {@code null} if the logger would delegates to Java logging anyway.
         */
        protected Object getImplementation(final String name) {
            return org.apache.log4j.Logger.getLogger(name);
        }

        /**
         * Wraps the specified {@linkplain #getImplementation implementation} in a Java logger.
         */
        protected Logger wrap(String name, Object implementation) throws ClassCastException {
            return new Log4JLogger(name, (org.apache.log4j.Logger) implementation);
        }

        /**
         * Returns the {@linkplain #getImplementation implementation} wrapped by the specified logger,
         * or {@code null} if none.
         */
        protected Object unwrap(final Logger logger) {
            if (logger instanceof Log4JLogger) {
                return ((Log4JLogger) logger).logger;
            }
            return null;
        }
    }
}
