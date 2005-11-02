/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2005, Geotools Project Managment Committee (PMC)
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
package org.geotools.image.jai;

// J2SE dependencies
import java.io.InputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.LogRecord;

// JAI dependencies
import javax.media.jai.JAI;
import javax.media.jai.OperationRegistry;

// Geotools dependencies
import org.geotools.resources.Utilities;
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.i18n.Logging;
import org.geotools.resources.i18n.LoggingKeys;


/**
 * A set of static methods for managing JAI's {@linkplain OperationRegistry operation registry}
 * with Geotools operations.
 *
 * @since 2.2
 * @version $Id$
 * @author Martin Desruisseaux
 */
public final class Registry {
    /**
     * The JAI registry file for Geotools operations.
     */
    private static final String REGISTRY_FILE = "META-INF/org.geotools.registryFile.jai";

    /**
     * {@code true} if {@link #init} has been invoked at least once.
     */
    private static boolean registryFileLoaded;

    /**
     * Do not allows instantiation of this class.
     */
    private Registry() {
    }

    /**
     * Loads the Geotools's {@code registryFile.jai} file programmatically. This method is needed
     * because JAI may not find the registry file by itself if the Geotools JAR file is loaded
     * from a different class loader. Consequently, this method need to be invoked explicitly
     * at least once before the operations provided in the {@link org.geotools.image.jai} package
     * are available.
     *
     * @return {@code true} if the Geotools's {@code registryFile.jai} has been loaded, or
     *         {@code false} if it was already loaded.
     */
    public static synchronized boolean init() {
        if (registryFileLoaded) {
            return false;
        }
        LogRecord record;
        try {
            final ClassLoader loader = Registry.class.getClassLoader();
            final InputStream input  = loader.getResourceAsStream(REGISTRY_FILE);
            if (input == null) {
                // Force logging in the 'catch' block just below.
                throw new FileNotFoundException(REGISTRY_FILE);
            }
            final OperationRegistry registry = JAI.getDefaultInstance().getOperationRegistry();
            registry.updateFromStream(input);
            input.close();
            registryFileLoaded = true;
            record = Logging.format(Level.CONFIG, LoggingKeys.LOADED_JAI_REGISTRY_FILE);
        } catch (IOException exception) {
            /*
             * Logs a message with the SEVERE level, because DefaultProcessing class initialization
             * is likely to fails (since it tries to load operations declared in META-INF/services,
             * and some of them depend on JAI operations).
             */
            record = Errors.getResources(null).getLogRecord(Level.SEVERE,
                     ErrorKeys.CANT_READ_$1, REGISTRY_FILE);
            record.setThrown(exception);
        }
        record.setSourceClassName(Utilities.getShortClassName(Registry.class));
        record.setSourceMethodName("init");
        Logger.getLogger("org.geotools.image").log(record);
        return registryFileLoaded;
    }
}
