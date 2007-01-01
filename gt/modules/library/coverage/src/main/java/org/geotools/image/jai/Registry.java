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
package org.geotools.image.jai;

// J2SE dependencies
import java.util.List;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.LogRecord;
import java.awt.image.renderable.RenderedImageFactory;

// JAI dependencies
import javax.media.jai.JAI;
import javax.media.jai.OperationRegistry;
import javax.media.jai.registry.RIFRegistry;
import javax.media.jai.registry.RenderedRegistryMode;

// Geotools dependencies
import org.geotools.resources.Utilities;
import org.geotools.resources.i18n.Logging;
import org.geotools.resources.i18n.LoggingKeys;


/**
 * A set of static methods for managing JAI's {@linkplain OperationRegistry operation registry}.
 *
 * @since 2.2
 * @source $URL$
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
    public static boolean registerGeotoolsServices(final OperationRegistry registry) {
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
        log("registerGeotoolsServices", record);
        return op == null;
    }

    /**
     * Allows or disallow native acceleration for the specified JAI operation. By default, JAI uses
     * hardware accelerated methods when available. For example, it make use of MMX instructions on
     * Intel processors. Unfortunatly, some native method crash the Java Virtual Machine under some
     * circonstances.  For example on JAI 1.1.2, the "Affine" operation on an image with float data
     * type, bilinear interpolation and an {@link javax.media.jai.ImageLayout} rendering hint cause
     * an exception in medialib native code.  Disabling the native acceleration (i.e using the pure
     * Java version) is a convenient workaround until Sun fix the bug.
     * <p>
     * <strong>Implementation note:</strong> the current implementation assumes that factories for
     * native implementations are declared in the {@code com.sun.media.jai.mlib} package, while
     * factories for pure java implementations are declared in the {@code com.sun.media.jai.opimage}
     * package. It work for Sun's 1.1.2 implementation, but may change in future versions. If this
     * method doesn't recognize the package, it does nothing.
     *
     * @param operation The operation name (e.g. "Affine").
     * @param allowed {@code false} to disallow native acceleration.
     *
     * @see <a href="http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4906854">JAI bug report 4906854</a>
     */
    public synchronized static void setNativeAccelerationAllowed(final String operation,
                                                                 final boolean  allowed)
    {
        final String             product = "com.sun.media.jai";
        final OperationRegistry registry = JAI.getDefaultInstance().getOperationRegistry();
        final List             factories = registry.getOrderedFactoryList(
                                           RenderedRegistryMode.MODE_NAME, operation, product);
        if (factories != null) {
            RenderedImageFactory   javaFactory = null;
            RenderedImageFactory nativeFactory = null;
            Boolean               currentState = null;
            for (final Iterator it=factories.iterator(); it.hasNext();) {
                final RenderedImageFactory factory = (RenderedImageFactory) it.next();
                final String pack = factory.getClass().getPackage().getName();
                if (pack.equals("com.sun.media.jai.mlib")) {
                    nativeFactory = factory;
                    if (javaFactory != null) {
                        currentState = Boolean.FALSE;
                    }
                }
                if (pack.equals("com.sun.media.jai.opimage")) {
                    javaFactory = factory;
                    if (nativeFactory != null) {
                        currentState = Boolean.TRUE;
                    }
                }
            }
            if (currentState!=null && currentState.booleanValue()!=allowed) {
                RIFRegistry.unsetPreference(registry, operation, product,
                                            allowed ? javaFactory : nativeFactory,
                                            allowed ? nativeFactory : javaFactory);
                RIFRegistry.setPreference(registry, operation, product,
                                          allowed ? nativeFactory : javaFactory,
                                          allowed ? javaFactory : nativeFactory);
                final LogRecord record = Logging.format(Level.CONFIG,
                                                 LoggingKeys.NATIVE_ACCELERATION_STATE_$2,
                                                 operation, new Integer(allowed ? 1 : 0));
                log("setNativeAccelerationAllowed", record);
            }
        }
    }

    /**
     * Log the specified record.
     */
    private static void log(final String method, final LogRecord record) {
        record.setSourceClassName(Registry.class.getName());
        record.setSourceMethodName(method);
        Logger.getLogger("org.geotools.image").log(record);
    }
}
