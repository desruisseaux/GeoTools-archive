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
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.LogRecord;

// JAI dependencies
import javax.media.jai.JAI;
import javax.media.jai.OperationRegistry;
import javax.media.jai.registry.RIFRegistry;

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
     * Do not allows instantiation of this class.
     */
    private Registry() {
    }

    /**
     * Unconditionnaly registers all JAI operations provided in the {@link org.geotools.image.jai}
     * package. This method usually don't need to be invoked, since JAI should parse automatically
     * the {@code META-INF/registryFile.jai} file at startup time. However, this default mechanism
     * may fail when the geotools JAR file is unreachable from the JAI class loader, in which case
     * the {@link org.geotools.coverage.processing} package will invoke this method as a fallback.
     * <p>
     * Note to module maintainer: if this method is updated, remember to update the
     * {@code META-INF/registryFile.jai} file accordingly.
     *
     * @param  registry The operation registry to register with.
     * @return {@code true} if all registrations have been successful.
     */
    public static boolean registerServices(final OperationRegistry registry) {
        LogRecord record;
        String op = "org.geotools";
        try {
            op = CombineDescriptor.OPERATION_NAME;
            registry.registerDescriptor(new CombineDescriptor());
            RIFRegistry.register(registry, op, "org.geotools", new CombineCRIF());

            op = HysteresisDescriptor.OPERATION_NAME;
            registry.registerDescriptor(new HysteresisDescriptor());
            RIFRegistry.register(registry, op, "org.geotools", new HysteresisCRIF());

            op = NodataFilterDescriptor.OPERATION_NAME;
            registry.registerDescriptor(new NodataFilterDescriptor());
            RIFRegistry.register(registry, op, "org.geotools", new NodataFilterCRIF());

            record  = Logging.format(Level.CONFIG, LoggingKeys.REGISTERED_JAI_OPERATIONS);
            op = null;
        } catch (IllegalArgumentException exception) {
            /*
             * Logs a message with the WARNING level, because DefaultProcessing class initialization
             * is likely to fails (since it tries to load operations declared in META-INF/services,
             * and some of them depend on JAI operations).
             */
            record = Logging.getResources(null).getLogRecord(Level.WARNING,
                     LoggingKeys.CANT_REGISTER_JAI_OPERATION_$1, op);
            record.setThrown(exception);
        }
        record.setSourceClassName(Utilities.getShortClassName(Registry.class));
        record.setSourceMethodName("registerServices");
        Logger.getLogger("org.geotools.image").log(record);
        return op == null;
    }
}
