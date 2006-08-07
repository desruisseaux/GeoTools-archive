/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2005-2006, Geotools Project Managment Committee (PMC)
 *    (C) 2005, Institut de Recherche pour le Développement
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
package org.geotools.resources;

// J2SE dependencies
import java.util.Set;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.sql.Driver;

// Geotools dependencies
import org.geotools.resources.i18n.Logging;
import org.geotools.resources.i18n.LoggingKeys;


/**
 * A set of utilities methods related to JDBC (<cite>Java Database Connectivity</cite>).
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @todo This class may be removed when we will be allowed to compile for J2SE 1.6.
 */
public final class JDBC {
    /**
     * Lists of JDBC drivers already loaded.
     */
    private static final Set DRIVERS = new HashSet();

    /**
     * Do not allow instantiation of this class.
     */
    private JDBC() {
    }

    /**
     * Attempts to load the specified JDBC driver, if not already done. If this method has already
     * been invoked for the specified driver, then it does nothing and returns {@code null}.
     * Otherwise, it attempts to load the specified driver and returns a log record initialized
     * with a message at the {@link Level#CONFIG CONFIG} level on success, or at the
     * {@link Level#WARNING WARNING} level on failure.
     *
     * @param  driver The JDBC driver to load, as a fully qualified Java class name.
     * @return A log message with driver information, or {@code null} if the driver was already
     *         loaded.
     */
    public static LogRecord loadDriver(final String driver) {
        LogRecord log = null;
        if (driver != null) {
            synchronized (DRIVERS) {
                if (!DRIVERS.contains(driver)) {
                    try {
                        final Driver d = (Driver) Class.forName(driver).newInstance();
                        log = Logging.format(Level.CONFIG, LoggingKeys.LOADED_JDBC_DRIVER_$3,
                                             driver, new Integer(d.getMajorVersion()),
                                                     new Integer(d.getMinorVersion()));
                        DRIVERS.add(driver);
                    } catch (Exception exception) {
                        log = Utilities.getLogRecord(exception);
                    }
                }
            }
        }
        return log;
    }
}
