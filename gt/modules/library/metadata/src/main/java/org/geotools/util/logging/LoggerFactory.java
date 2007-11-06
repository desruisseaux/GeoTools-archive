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

import java.util.logging.Logger;
import org.geotools.util.WeakValueHashMap;


/**
 * A factory for Java {@link Logger} wrapping an other logging framework.
 *
 * @since 2.4
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @see Logging
 */
public abstract class LoggerFactory {
    /**
     * The loggers created up to date.
     */
    private final WeakValueHashMap<String,Logger> loggers;

    /**
     * Creates a new factory.
     */
    protected LoggerFactory() {
        loggers = new WeakValueHashMap<String,Logger>();
    }

    /**
     * Returns the logger of the specified name. If this method has already been invoked previously
     * with the same {@code name} argument, than it may returns the same logger provided that:
     * <ul>
     *   <li>the logger has not yet been garbage collected;</li>
     *   <li>the implementation instance (Log4J, SLF4J, <cite>etc.</cite>) returned by
     *       <code>{@linkplain #getImplementation getImplementation}(name)</code> has
     *       not changed.</li>
     * </ul>
     * Otherwise this method returns a new {@code Logger} instance.
     *
     * @param  name The name of the logger.
     * @return The logger (never {@code null}).
     */
    public Logger getLogger(final String name) {
        final Object target = getImplementation(name);
        synchronized (loggers) {
            Logger logger = loggers.get(name);
            if (logger == null || !target.equals(unwrap(logger))) {
                logger = wrap(target);
                loggers.put(name, logger);
            }
            return logger;
        }
    }

    /**
     * Returns the implementation to use for the logger of the specified name. The object to be
     * returned depends on the logging framework (Log4J, SLF4J, <cite>etc.</cite>).
     *
     * @param  name The name of the logger.
     * @return The logger as an object of the target logging framework
     *         (Log4J, SLF4J, <cite>etc.</cite>). Never {@code null}.
     */
    protected abstract Object getImplementation(String name);

    /**
     * Wraps the specified {@linkplain #getImplementation implementation} in a Java logger.
     *
     * @param  implementation An implementation returned by {@link #getImplementation}.
     * @return A new logger wrapping the specified implementation.
     * @throws ClassCastException if the given implementation is not an instance
     *         of the expected class.
     */
    protected abstract Logger wrap(Object implementation) throws ClassCastException;

    /**
     * Returns the {@linkplain #getImplementation implementation} wrapped by the specified logger,
     * or {@code null} if none. If the specified logger is not an instance of the expected class,
     * then this method should returns {@code null}.
     *
     * @param  logger The logger to test.
     * @return The implementation wrapped by the specified logger, or {@code null} if none.
     */
    protected abstract Object unwrap(Logger logger);
}
