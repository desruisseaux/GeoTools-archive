/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2007, Geotools Project Managment Committee (PMC)
 *    (C) 2007, Geomatys
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


/**
 * Enumeration of logging frameworks known to {@link Logging}.
 *
 * @since 2.4
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public enum LoggingFramework {
    /**
     * The {@linkplain java.util.logging Java logging} framework.
     */
    JAVA_LOGGING("java.util.logging.Logger", null),

    /**
     * The <A HREF="http://jakarta.apache.org/commons/logging/">commons-logging</A> framework.
     */
    COMMONS_LOGGING("org.apache.commons.logging.Log", "org.geotools.util.logging.CommonsLogger$Factory"),

    /**
     * The <A HREF="http://logging.apache.org/log4j/">Log4J</A> framework.
     */
    LOG4J("org.apache.log4j.Logger", "org.geotools.util.logging.Log4JLogger$Factory");

    /**
     * The fully qualified logger class name for this framework.
     */
    private final String loggerClassName;

    /**
     * The fully {@link LoggerFactory} class name for this framework.
     */
    final String factoryClassName;

    /**
     * Creates an enumeration.
     *
     * @param loggerClassName  The fully qualified logger class name for this framework.
     * @param factoryClassName The fully {@link LoggerFactory} class name for this framework.
     */
    private LoggingFramework(final String loggerClassName, final String factoryClassName) {
        this.loggerClassName  = loggerClassName;
        this.factoryClassName = factoryClassName;
    }

    /**
     * Returns {@code true} if this framework seems to be available on the classpath.
     */
    public boolean isAvailable() {
        try {
            Class.forName(loggerClassName, false, LoggingFramework.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
