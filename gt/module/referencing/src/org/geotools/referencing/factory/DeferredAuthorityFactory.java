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
 *
 *    This package contains documentation from OpenGIS specifications.
 *    OpenGIS consortium's work is fully acknowledged here.
 */
package org.geotools.referencing.factory;

// J2SE dependencies
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.LogRecord;

// OpenGIS dependencies
import org.opengis.metadata.citation.Citation;
import org.opengis.referencing.FactoryException;

// Geotools dependencies
import org.geotools.factory.Hints;
import org.geotools.factory.OptionalFactory;
import org.geotools.referencing.factory.FactoryGroup;


/**
 * A buffered authority factory which will defer the {@linkplain #createBackingStore creation
 * of a backing store} until when first needed. This approach allow to etablish a connection to
 * a database (for example) only when first needed. In addition, the backing store can be
 * automatically disposed after a timeout and recreated when needed again.
 *
 * @since 2.1
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public abstract class DeferredAuthorityFactory extends BufferedAuthorityFactory
                                            implements OptionalFactory
{
    /**
     * The timer for {@linkplain AbstractAuthorityFactory#dispose disposing} backing stores.
     *
     * @todo Give a name to this timer when we will be allowed to compile for J2SE 1.5.
     */
    private static final Timer TIMER = new Timer(true);

    /**
     * The task for disposing the backing store, or {@code null} if none.
     * This task will be scheduled for repeated execution by {@link #setTimeout}.
     */
    private TimerTask disposer;

    /**
     * {@code true} if the backing store was used since the last time the timer task was run.
     * A value of {@code true} means that the task must wait again. A value of {@code false}
     * means that it can dispose the backing store.
     */
    private boolean used;

    /**
     * Constructs an instance without initial backing store. Subclasses are responsible for
     * creating an appropriate backing store when the {@link #createBackingStore} method is
     * invoked.
     *
     * @param factories The factories to use.
     * @param priority The priority for this factory, as a number between
     *        {@link #MINIMUM_PRIORITY MINIMUM_PRIORITY} and
     *        {@link #MAXIMUM_PRIORITY MAXIMUM_PRIORITY} inclusive.
     *
     * @see #createBackingStore
     *
     * @deprecated Use {@link #DeferredAuthorityFactory(Hints,int)} instead.
     */
    protected DeferredAuthorityFactory(final FactoryGroup factories,
                                       final int          priority)
    {
        super(new Hints(FactoryGroup.HINT_KEY, factories), priority, DEFAULT_MAX);
    }

    /**
     * Constructs an instance without initial backing store. Subclasses are responsible for
     * creating an appropriate backing store when the {@link #createBackingStore} method is
     * invoked.
     *
     * @param factories The factories to use.
     * @param priority The priority for this factory, as a number between
     *        {@link #MINIMUM_PRIORITY MINIMUM_PRIORITY} and
     *        {@link #MAXIMUM_PRIORITY MAXIMUM_PRIORITY} inclusive.
     * @param maxStrongReferences The maximum number of objects to keep by strong reference.
     *
     * @see #createBackingStore
     *
     * @deprecated Use {@link #DeferredAuthorityFactory(Hints,int,int)} instead.
     */
    protected DeferredAuthorityFactory(final FactoryGroup factories,
                                       final int          priority,
                                       final int maxStrongReferences)
    {
        super(new Hints(FactoryGroup.HINT_KEY, factories), priority, maxStrongReferences);
    }

    /**
     * Constructs an instance without initial backing store. Subclasses are responsible for
     * creating an appropriate backing store when the {@link #createBackingStore} method is
     * invoked.
     * <p>
     * This constructor recognizes the {@link Hints#CRS_FACTORY CRS}, {@link Hints#CS_FACTORY CS},
     * {@link Hints#DATUM_FACTORY DATUM} and {@link Hints#MATH_TRANSFORM_FACTORY MATH_TRANSFORM}
     * {@code FACTORY} hints. In addition, the {@link FactoryGroup#HINT_KEY} hint may be used as
     * a low-level substitute for all the above.
     *
     * @param hints The factories to use as a set of hints.
     * @param priority The priority for this factory, as a number between
     *        {@link #MINIMUM_PRIORITY MINIMUM_PRIORITY} and
     *        {@link #MAXIMUM_PRIORITY MAXIMUM_PRIORITY} inclusive.
     *
     * @see #createBackingStore
     *
     * @since 2.2
     */
    protected DeferredAuthorityFactory(final Hints hints,
                                       final int priority)
    {
        super(hints, priority, DEFAULT_MAX);
    }

    /**
     * Constructs an instance without initial backing store. Subclasses are responsible for
     * creating an appropriate backing store when the {@link #createBackingStore} method is
     * invoked.
     * <p>
     * This constructor recognizes the {@link Hints#CRS_FACTORY CRS}, {@link Hints#CS_FACTORY CS},
     * {@link Hints#DATUM_FACTORY DATUM} and {@link Hints#MATH_TRANSFORM_FACTORY MATH_TRANSFORM}
     * {@code FACTORY} hints. In addition, the {@link FactoryGroup#HINT_KEY} hint may be used as
     * a low-level substitute for all the above.
     *
     * @param hints The factories to use as a set of hints.
     * @param priority The priority for this factory, as a number between
     *        {@link #MINIMUM_PRIORITY MINIMUM_PRIORITY} and
     *        {@link #MAXIMUM_PRIORITY MAXIMUM_PRIORITY} inclusive.
     * @param maxStrongReferences The maximum number of objects to keep by strong reference.
     *
     * @see #createBackingStore
     *
     * @since 2.2
     */
    protected DeferredAuthorityFactory(final Hints hints,
                                       final int priority,
                                       final int maxStrongReferences)
    {
        super(hints, priority, maxStrongReferences);
    }

    /**
     * Returns {@code true} if this factory is ready. The default implementation returns
     * {@code false} if {@link #createBackingStore} throws an exception.
     */
    public boolean isReady() {
        return super.isReady();
    }

    /**
     * Returns the backing store authority factory.
     *
     * @return The backing store to uses in {@code createXXX(...)} methods.
     * @throws FactoryException if the creation of backing store failed.
     */
    final AbstractAuthorityFactory getBackingStore() throws FactoryException {
        assert Thread.holdsLock(this);
        if (backingStore == null) {
            backingStore = createBackingStore();
        }
        used = true; // Tell to the disposer to wait again.
        return backingStore;
    }

    /**
     * Creates the backing store authority factory. This method is invoked the first time a
     * {@code createXXX(...)} method is invoked.
     *
     * @return The backing store to uses in {@code createXXX(...)} methods.
     * @throws FactoryException if the creation of backing store failed.
     */
    protected abstract AbstractAuthorityFactory createBackingStore() throws FactoryException;

    /**
     * Returns {@code true} if this deferred factory is connected to its backing store.
     * This method returns {@code false} if no {@code createFoo} method has been invoked,
     * if the backing store has been automatically disposed after the {@linkplain #setTimeout
     * timeout} or if this factoy has been {@linkplain #dispose disposed}.
     */
    public synchronized boolean isConnected() {
        return backingStore != null;
    }

    /**
     * Set a timer for disposing the backing store after the specified amount of milliseconds of
     * inactivity. The {@link #createBackingStore} method will be responsible for creating a new
     * backing store when needed. Note that the backing store disposal can be vetoed if
     * {@link #canDisposeBackingStore} returns {@code false}.
     *
     * @param delay The minimal delay before to close the backing store. This delay is very
     *        approximative. The backing store will not be closed before, but may take as
     *        much as twice that time before to be closed.
     */
    public synchronized void setTimeout(final long delay) {
        if (disposer != null) {
            disposer.cancel();
        }
        disposer = new Disposer();
        TIMER.schedule(disposer, delay, delay);
    }

    /**
     * Returns {@code true} if the backing store can be disposed now. This method is invoked
     * automatically after the amount of time specified by {@link #setTimeout} if the factory
     * were not used during that time. The default implementation always returns {@code true}.
     * Subclasses should override this method and returns {@code false} if they want to prevent
     * the backing store disposal under some circonstances.
     *
     * @param backingStore The backing store in process of being disposed.
     */
    protected boolean canDisposeBackingStore(final AbstractAuthorityFactory backingStore) {
        return true;
    }

    /**
     * Releases resources immediately instead of waiting for the garbage collector. This
     * method disposes the backing store regardeless of {@link #canDisposeBackingStore} value.
     */
    public synchronized void dispose() throws FactoryException {
        if (disposer != null) {
            disposer.cancel();
            disposer = null;
        }
        super.dispose();
    }

    /**
     * The task for closing the backing store after the timeout.
     */
    private final class Disposer extends TimerTask {
        public void run() {
            synchronized (DeferredAuthorityFactory.this) {
                if (used || !canDisposeBackingStore(backingStore)) {
                    used = false;
                    return;
                }
                if (cancel()) {
                    disposer = null;
                    if (backingStore != null) try {
                        backingStore.dispose();
                        backingStore = null;
                    } catch (FactoryException exception) {
                        backingStore = null;
                        final LogRecord record = new LogRecord(Level.WARNING,
                                "Failed to dispose the backing store after timeout."); // TODO: localize
                        record.setSourceMethodName("run");
                        record.setSourceClassName(Disposer.class.getName());
                        record.setThrown(exception);
                        LOGGER.log(record);
                    }
                }
            }
        }
    }
}
