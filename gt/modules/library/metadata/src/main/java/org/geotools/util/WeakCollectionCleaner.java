/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2003-2006, Geotools Project Managment Committee (PMC)
 *    (C) 2001, Institut de Recherche pour le Développement
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
package org.geotools.util;

// J2SE dependencies
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.logging.Logger;

// Geotools dependencies
import org.geotools.util.Logging;


/**
 * A thread invoking {@link Reference#clear} on each enqueded reference.
 * This is usefull only if {@code Reference} subclasses has overridden
 * their {@code clear()} method in order to perform some cleaning.
 * This thread is used by {@link WeakHashSet} and {@link WeakValueHashMap},
 * which remove their entry from the collection when {@link Reference#clear}
 * is invoked.
 *
 * @since 2.0
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
final class WeakCollectionCleaner extends Thread {
    /**
     * The name of the logger to use.
     */
    private static final String LOGGER = "org.geotools.util";

    /**
     * The default thread.
     */
    public static final WeakCollectionCleaner DEFAULT = new WeakCollectionCleaner();

    /**
     * List of reference collected by the garbage collector.
     * Those elements must be removed from {@link #table}.
     */
    final ReferenceQueue referenceQueue = new ReferenceQueue();

    /**
     * Construct and start a new thread as a daemon. This thread will be stoped
     * most of the time.  It will run only some few nanoseconds each time a new
     * {@link WeakReference} is enqueded.
     */
    private WeakCollectionCleaner() {
        super("WeakCollectionCleaner");
        setDaemon(true);
        start();
    }

    /**
     * Loop to be run during the virtual machine lifetime.
     */
    public void run() {
        // The reference queue should never be null.  However some strange cases (maybe caused
        // by an anormal JVM state) have been reported on the mailing list. In such case, stop
        // the daemon instead of writting 50 Mb of log messages.
        while (referenceQueue != null) {
            try {
                // Block until a reference is enqueded.
                // Note: To be usefull, the clear() method must have
                //       been overridden in Reference subclasses.
                referenceQueue.remove().clear();
            } catch (InterruptedException exception) {
                // Somebody doesn't want to lets us sleep... Go back to work.
            } catch (Exception exception) {
                Logging.unexpectedException(LOGGER,
                        WeakCollectionCleaner.class, "remove", exception);
            } catch (AssertionError exception) {
                Logging.unexpectedException(LOGGER,
                        WeakCollectionCleaner.class, "remove", exception);
                // Do not kill the thread on assertion failure, in order to
                // keep the same behaviour as if assertions were turned off.
            }
        }
        Logger.getLogger(LOGGER).severe("Daemon stopped."); // Should never happen.
    }
}
