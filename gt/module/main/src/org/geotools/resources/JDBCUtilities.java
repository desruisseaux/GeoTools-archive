/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2005, Geotools Project Management Committee (PMC)
 * (C) 2005, Institut de Recherche pour le Développement
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
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.geotools.resources;

// J2SE dependencies
import java.sql.Driver;
import java.util.logging.Level;
import java.util.logging.LogRecord;

// Geotools dependencies
import org.geotools.resources.cts.Resources;
import org.geotools.resources.cts.ResourceKeys;


/**
 * A set of utilities methods for JDBC.
 *
 * @author Martin Desruisseaux
 */
public final class JDBCUtilities {
    
    /** Creates a new instance of JDBCUtilities */
    private JDBCUtilities() {
    }

    /**
     * Attempt to load the driver with the specified classname.
     * This method always returns a log message, either on success or on failure.
     * Logging message on success contains the driver version.
     *
     * @param  classname The class name to load.
     * @return A message to log.
     */
    public static LogRecord loadDriver(final String classname) {
        LogRecord record;
        try {
            final Driver driver = (Driver)Class.forName(classname).newInstance();
            record = Resources.getResources(null).getLogRecord(Level.CONFIG,
                                        ResourceKeys.LOADED_JDBC_DRIVER_$3);
            record.setParameters(new Object[] {
                driver.getClass().getName(),
                new Integer(driver.getMajorVersion()),
                new Integer(driver.getMinorVersion())
            });
        } catch (Exception exception) {
            record = new LogRecord(Level.WARNING, exception.getLocalizedMessage());
            record.setThrown(exception);
            // Try to connect anyway. It is possible that
            // an other driver has already been loaded...
        }
        return record;
    }
}
