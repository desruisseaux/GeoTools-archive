/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2006-2007, Geotools Project Managment Committee (PMC)
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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * An adapter that redirect all Java logging events to the Apache's commons logging framework.
 *
 * @since 2.4
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Saul Farber
 *
 * @see Logging
 */
final class CommonLogger extends LoggerAdapter {
    /**
     * The Apache logger to use.
     */
    private final Log logger;

    /**
     * Creates a new logger.
     *
     * @param name   The logger name.
     * @param logger The result of {@code LogFactory.getLog(name)}.
     */
    public CommonLogger(final String name, final Log logger) {
        super(name);
        this.logger = logger;
    }

    /**
     * Do nothing since Commons-Logging doesn't support programmatic change of logging level.
     */
    public void setLevel(Level level) {
    }

    /**
     * Returns the level for this logger.
     */
    public Level getLevel() {
        if (logger.isFatalEnabled()) return Level.SEVERE;
        if (logger.isErrorEnabled()) return Level.SEVERE;
        if (logger.isWarnEnabled ()) return Level.WARNING;
        if (logger.isInfoEnabled ()) return Level.CONFIG;
        if (logger.isDebugEnabled()) return getDebugLevel();
        if (logger.isTraceEnabled()) return Level.FINEST;
        return Level.OFF;
    }

    /**
     * Returns {@code true} if the specified level is loggable.
     */
    @SuppressWarnings("fallthrough")
    public boolean isLoggable(Level level) {
        final int n = level.intValue();
        switch (n / 100) {
            default: return (n>=0) ? logger.isFatalEnabled() : false;
            case 10: return logger.isErrorEnabled();    // SEVERE
            case  9: return logger.isWarnEnabled();     // WARNING
            case  8:                                    // INFO
            case  7: return logger.isInfoEnabled();     // CONFIG
            case  6:                                    // (not allocated)
            case  5:                                    // FINE
            case  4: return logger.isDebugEnabled();    // FINER
            case  3: return logger.isTraceEnabled();    // FINEST
            case  2:                                    // (not allocated)
            case  1:                                    // (not allocated)
            case  0: return false;                      // OFF
        }
    }

    /**
     * Logs a record at the specified level.
     */
    @Override
    @SuppressWarnings("fallthrough")
    public void log(final Level level, final String message, final Throwable thrown) {
        final int n = level.intValue();
        switch (n / 100) {
            default: if (n < 0)                     break;  // Fallthrough otherwise.
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
     * Factory for {@link CommonLogger}.
     */
    static final class Factory extends LoggerFactory {
        /**
         * Returns the implementation to use for the logger of the specified name.
         */
        protected Object getImplementation(final String name) {
            return LogFactory.getLog(name);
        }

        /**
         * Wraps the specified {@linkplain #getImplementation implementation} in a Java logger.
         */
        protected Logger wrap(String name, Object implementation) throws ClassCastException {
            return new CommonLogger(name, (Log) implementation);
        }

        /**
         * Returns the {@linkplain #getImplementation implementation} wrapped by the specified logger,
         * or {@code null} if none.
         */
        protected Object unwrap(final Logger logger) {
            if (logger instanceof CommonLogger) {
                return ((CommonLogger) logger).logger;
            }
            return null;
        }
    }
}
