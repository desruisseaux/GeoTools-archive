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

import java.util.logging.Logger;

/**
 * Factory for {@link Log4JLogger}.
 *
 * @since 2.4
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public final class Log4JLoggerFactory extends LoggerFactory {
    /**
     * The unique instance of this factory.
     */
    private static Log4JLoggerFactory factory;

    /**
     * Do not allows more than instantiation of this class.
     */
    private Log4JLoggerFactory() {
    }

    /**
     * Returns the unique instance of this factory.
     */
    public static synchronized Log4JLoggerFactory getInstance() {
        if (factory == null) {
            factory = new Log4JLoggerFactory();
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