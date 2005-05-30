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
package org.geotools.coverage;

// J2SE dependencies
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

// OpenGIS dependencies
import org.opengis.coverage.Coverage;

// Geotools dependencies
import org.geotools.resources.Utilities;


/**
 * A cache for {@linkplain Coverage coverage} instances. Call to {@link #reference}
 * method returns a {@linkplain WeakReference weak} or {@linkplain SoftReference soft}
 * reference to the specified coverage. If such a reference previously existed, it is
 * returned. Otherwise a new reference is created.
 *
 * @todo This is just a first draft. The goal is to create soft reference, and transform
 *       automatically soft reference into weak ones once some amount of memory usage is
 *       reached. Do not rely on this API; it may change again.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @since 2.1
 */
public class CoverageCache {
    /**
     * The thread group for all cleaner threads.
     * Note: this field must be declared first.
     */
    private static final ThreadGroup CLEANERS = new ThreadGroup("CoverageCache cleaners");

    /**
     * The default, system-wide cache for {@linkplain Coverage coverages}.
     */
    public static final CoverageCache DEFAULT = new CoverageCache();

    /**
     * The map of cached coverages.
     * All {@link CoverageCache} operations must be synchronized on this cache.
     * By using this cache as a look instead of {@code CoverageCache} itself,
     * we make sure that the user can't hold a unwanted look that would block
     * the {@link Cleaner} thread. Furthermore, we want to performs synchronization
     * without holding any reference to {@code CoverageCache} in the {@code Cleaner}
     * thread.
     */
    private final Map cache = new HashMap();

    /**
     * The reference queue for supression of garbage-collected coverages.
     */
    private final ReferenceQueue queue = new ReferenceQueue();

    /**
     * The cleaner thread.
     */
    private final Cleaner cleaner;

    /**
     * Creates a new coverage cache.
     */
    public CoverageCache() {
        cleaner = new Cleaner(cache, queue);
        cleaner.start();
    }

    /**
     * Returns a reference to the specified coverage. If {@linkplain WeakReference weak}
     * or {@linkplain SoftReference soft} reference already exists for the specified coverage,
     * then this reference is returned. Otherwise, a {@linkplain WeakReference weak reference}
     * is created and returned.
     *
     * @param  coverage The coverage to reference.
     * @return A weak of a soft reference to the specified coverage.
     */
    public Reference reference(final Coverage coverage) {
        synchronized (cache) {
            final Ref candidate = new Ref(coverage);
            Ref ref = (Ref) cache.get(candidate);
            if (ref == null) {
                ref = candidate;
                cache.put(ref, ref);
            }
            return ref.getReference(queue);
        }
    }

    /**
     * Entries in {@link CoverageCache#cache}.
     */
    private static final class Ref {
        /**
         * The referenced coverage, as a strong reference to a {@link Coverage}
         * object or a weak or soft {@linkplan Reference reference}.
         */
        private Object referent;

        /**
         * Constructs a reference to the specified coverage.
         */
        public Ref(final Coverage referent) {
            this.referent = referent;
        }

        /**
         * Returns the referenced coverage.
         */
        public Coverage getCoverage() {
            if (referent instanceof Reference) {
                return (Coverage) ((Reference) referent).get();
            }
            return (Coverage) referent;
        }

        /**
         * Returns the weak reference to the coverage. This method will changes
         * strong reference into weak one the first time it is invoked.
         */
        public Reference getReference(final ReferenceQueue queue) {
            if (referent instanceof Coverage) {
                referent = new WeakReference(referent, queue);
            }
            return (Reference) referent;
        }

        /**
         * Returns the hash code value for the coverage.
         */
        public int hashCode() {
            return getCoverage().hashCode();
        }

        /**
         * Compares this object with the specified entry for equality.
         */
        public boolean equals(final Object object) {
            return (object instanceof Ref) &&
                    Utilities.equals(getCoverage(), ((Ref) object).getCoverage());
        }
    }

    /**
     * The thread for removing dead references. Note: because a dead {@link Ref} object
     * can't anymore returns a valid hash code or have an 'equals' method working as
     * expected, we have to iterate through all key entries in the map. This is really
     * not scalable. However, coverages are usually big objects, so this cache typically
     * contains few of them (usually less than 10).
     * <br><br>
     * <strong>NOTE: Do NOT hold any reference to {@link CoverageCache} in this class,</strong>
     * because we don't want to prevent {@code CoverageCache} to be garbage-collected. This
     * thread will stops when the underlying {@code CoverageCache} will be garbage-collected.
     */
    private static final class Cleaner extends Thread {
        /**
         * The map of cached coverages.
         */
        private final Map cache;

        /**
         * The reference queue for supression of garbage-collected coverages.
         */
        private final ReferenceQueue queue;

        /**
         * Set to {@code true} if this thread must stop its execution.
         */
        volatile boolean stop;

        /**
         * Constructs a cleaner thread with hight priority.
         * Its execution time should be short.
         */
        public Cleaner(final Map cache, final ReferenceQueue queue) {
            super(CLEANERS, "CoverageCache cleaner #"+CLEANERS.activeCount());
            setPriority(MAX_PRIORITY-2);
            setDaemon(true);
            this.cache = cache;
            this.queue = queue;
        }

        /**
         * Waits for a reference to be garbage collected and remove it from the map.
         */
        public void run() {
            while (!stop) {
                final Reference ref;
                try {
                    ref = queue.remove();
                } catch (InterruptedException exception) {
                    // Someone doesn't want to let us sleep.
                    // Ignore and go sleep again.
                    continue;
                }
                synchronized (cache) {
                    for (final Iterator it=cache.keySet().iterator(); it.hasNext();) {
                        if (it.next() == ref) {
                            it.remove();
                        }
                    }
                }
            }
        }
    }

    /**
     * Cleanup this coverage cache on garbage collection.
     */
    protected void finalize() throws Throwable {
        synchronized (cache) {
            cleaner.stop = true;
            cache.clear();
        }
        super.finalize();
    }
}
