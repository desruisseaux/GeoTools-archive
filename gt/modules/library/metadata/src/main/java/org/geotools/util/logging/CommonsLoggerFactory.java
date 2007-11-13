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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.impl.Jdk14Logger;

/**
 * Factory for {@link CommonsLogger}.
 *
 * @since 2.4
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public final class CommonsLoggerFactory extends LoggerFactory {
    /**
     * The unique instance of this factory.
     */
    private static CommonsLoggerFactory factory;

    /**
     * Do not allows more than instantiation of this class.
     */
    private CommonsLoggerFactory() {
    }

    /**
     * Returns the unique instance of this factory.
     */
    public static synchronized CommonsLoggerFactory getInstance() {
        if (factory == null) {
            factory = new CommonsLoggerFactory();
        }
        return factory;
    }

    /**
     * Returns the implementation to use for the logger of the specified name,
     * or {@code null} if the logger would delegates to Java logging anyway.
     */
    protected Object getImplementation(final String name) {
        final Log log = LogFactory.getLog(name);
        if (log instanceof Jdk14Logger) {
            return null;
        }
        return log;
    }

    /**
     * Wraps the specified {@linkplain #getImplementation implementation} in a Java logger.
     */
    protected Logger wrap(String name, Object implementation) throws ClassCastException {
        return new CommonsLogger(name, (Log) implementation);
    }

    /**
     * Returns the {@linkplain #getImplementation implementation} wrapped by the specified logger,
     * or {@code null} if none.
     */
    protected Object unwrap(final Logger logger) {
        if (logger instanceof CommonsLogger) {
            return ((CommonsLogger) logger).logger;
        }
        return null;
    }
}