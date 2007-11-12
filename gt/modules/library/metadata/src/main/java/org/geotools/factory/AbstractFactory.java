/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2005-2006, GeoTools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.factory;

// J2SE dependencies
import java.util.Map;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.IdentityHashMap;
import java.util.Collections;
import java.util.Iterator;
import java.io.Writer;
import java.io.IOException;
import java.awt.RenderingHints;
import javax.imageio.spi.ServiceRegistry;
import javax.imageio.spi.RegisterableService;

// OpenGIS dependencies
import org.opengis.referencing.AuthorityFactory;

// Geotools dependencies
import org.geotools.io.TableWriter;
import org.geotools.resources.Utilities;
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.i18n.ErrorKeys;


/**
 * Skeletal implementation of factories. This base classe provides no {@code createFoo} methods,
 * (they must be provided by subclasses), but provides two convenience features:
 * <p>
 * <ul>
 *   <li>An initially empty {@linkplain #hints map of hints} to be filled by subclasses
 *       constructors. They are the hints to be returned by {@link #getImplementationHints}.</li>
 *   <li>An automatic {@linkplain ServiceRegistry#setOrdering ordering} applied
 *       on the basis of subclasses-provided {@linkplain #priority} rank.</li>
 * </ul>
 * <p>
 * When more than one factory implementation is
 * {@linkplain ServiceRegistry#registerServiceProvider registered} for the same category (i.e. they
 * implement the same {@link Factory} sub-interface), the actual instance to be used is selected
 * according their {@linkplain ServiceRegistry#setOrdering ordering} and user-supplied
 * {@linkplain Hints hints}. Hints have precedence. If more than one factory matches the hints
 * (including the common case where the user doesn't provide any hint at all), then ordering
 * matter.
 * <p>
 * The ordering is unspecified for every pairs of factories with the same {@linkplain #priority}.
 * This implies that the ordering is unspecified between all factories created with the
 * {@linkplain #AbstractFactory() default constructor}, since they all have the same
 * {@linkplain #NORMAL_PRIORITY default priority} level.
 *
 * <h3>How hints are set</h3>
 * Hints are used for two purposes. The distinction is important because the set
 * of hints may not be identical in both cases:
 * <p>
 * <ol>
 *   <li>Hints are used for creating new factories.</li>
 *   <li>Hints are used in order to check if an <em>existing</em> factory is suitable.</li>
 * </ol>
 * <p>
 * {@code AbstractFactory} do <strong>not</strong> provides any facility for the first case.
 * Factories implementations shall inspect themselves all relevant hints supplied by the user,
 * and pass them to any dependencies. Do <strong>not</strong> use the {@link #hints} field for
 * that; use the hints provided by the user in the constructor. If all dependencies are created
 * at construction time (<cite>constructor injection</cite>), there is no need to keep user's hints
 * once the construction is finished.
 * <p>
 * The {@link #hints} field is for the second case only. Implementations shall copy in this
 * field only the user's hints that are know to be relevant to this factory. If a hint is
 * relevant but the user didn't specified any value, the hint key should be added to the
 * {@link #hints} map anyway with a {@code null} value. Only direct dependencies shall be put
 * in the {@link #hints} map. Indirect dependencies (i.e. hints used by other factories used
 * by this factory) will be inspected automatically by {@link FactoryRegistry} in a recursive way.
 * <p>
 * <strong>Note:</strong> The lack of constructor expecting a {@link Map} argument is intentional.
 * This is in order to discourage blind-copy of all user-supplied hints to the {@link #hints} map.
 * <p>
 * <strong>Example:</strong> Lets two factories, A and B. Factory A need an instance of Factory B.
 * Factory A can be implemented as below:
 *
 * <table border='1'>
 * <tr><th>Code</th><th>Observations</th></tr>
 * <tr><td><blockquote><pre>
 * class FactoryA extends AbstractFactory {
 *     FactoryB fb;
 *
 *     FactoryA(Hints userHints) {
 *         fb = FactoryFinder.getFactoryB(userHints);
 *         hints.put(Hints.FACTORY_B, fb);
 *     }
 * }
 * </pre></blockquote></td>
 * <td>
 * <ul>
 *   <li>The user-supplied map ({@code userHints}) is never modified.</li>
 *   <li>All hints relevant to other factories are used in the constructor. Hints relevant to
 *       factory B are used when {@code FactoryFinder.getFactoryB(...)} is invoked.</li>
 *   <li>The {@code FactoryA} constructor stores only the hints relevant to {@code FactoryA}.
 *       Indirect dependencies (e.g. hints relevant to {@code FactoryB}) will be inspected
 *       recursively by {@link FactoryRegistry}.</li>
 *   <li>In the above example, {@link #hints} will never be used for creating new factories.</li>
 * </ul>
 * </td></tr></table>
 *
 * @since 2.1
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class AbstractFactory implements Factory, RegisterableService {
    /**
     * The minimum priority for a factory, which is {@value}. Factories with lowest priority
     * will be used only if there is no other factory in the same
     * {@linkplain ServiceRegistry#getCategories category}.
     *
     * @see #priority
     * @see #onRegistration
     */
    public static final int MINIMUM_PRIORITY = 1;

    /**
     * The default priority, which is {@value}.
     *
     * @see #priority
     * @see #onRegistration
     */
    public static final int NORMAL_PRIORITY = 50;

    /**
     * The maximum priority for a factory, which is {@value}. Factories with highest
     * priority will be preferred to any other factory in the same
     * {@linkplain ServiceRegistry#getCategories category}.
     *
     * @see #priority
     * @see #onRegistration
     */
    public static final int MAXIMUM_PRIORITY = 100;

    /**
     * The priority for this factory, as a number between {@link #MINIMUM_PRIORITY} and
     * {@link #MAXIMUM_PRIORITY} inclusive. Priorities are used by {@link FactoryRegistry}
     * for selecting a preferred factory when many are found for the same service.
     *
     * @see #getPriority
     *
     * @todo Consider deprecating this field. See
     * <A HREF="http://jira.codehaus.org/browse/GEOT-1100">GEOT-1100</A> for details.
     */
    protected final int priority;

    /**
     * The {@linkplain Factory#getImplementationHints implementation hints}. This map should be
     * filled by subclasses at construction time. If possible, constructors should not copy blindly
     * all user-provided hints. They should select only the relevant hints and resolve them as of
     * {@linkplain Factory#getImplementationHints implementation hints} contract.
     * <p>
     * <b>Note:</b> This field is not an instance of {@link Hints} because:
     * <ul>
     *   <li>The primary use of this map is to check if this factory can be reused.
     *       It is not for creating new factories.</li>
     *   <li>This map needs to allow null values, as of
     *       {@linkplain Factory#getImplementationHints implementation hints} contract.</li>
     * </ul>
     */
    protected final Map hints = new LinkedHashMap();

    /**
     * An unmodifiable view of {@link #hints}. This is the actual map to be returned
     * by {@link #getImplementationHints}. Its content reflects the {@link #hints}
     * map even if the later is modified.
     */
    private final Map/*<RenderingHints.Key,Object>*/ unmodifiableHints = Collections.unmodifiableMap(hints);
    
    /**
     * Creates a new factory with the {@linkplain #NORMAL_PRIORITY default priority}.
     */
    protected AbstractFactory() {
        this(NORMAL_PRIORITY);
    }

    /**
     * Constructs a factory with the specified priority.
     *
     * @param priority The priority for this factory, as a number between
     *        {@link #MINIMUM_PRIORITY} and {@link #MAXIMUM_PRIORITY} inclusive.
     */
    protected AbstractFactory(final int priority) {
    	this.priority = priority;
        if (priority<MINIMUM_PRIORITY || priority>MAXIMUM_PRIORITY) {
            throw new IllegalArgumentException(Errors.format(ErrorKeys.ILLEGAL_ARGUMENT_$2,
                                               "priority", priority));
        }
    }

    /**
     * Returns the priority for this factory, as a number between {@link #MINIMUM_PRIORITY} and
     * {@link #MAXIMUM_PRIORITY} inclusive. Priorities are used by {@link FactoryRegistry} for
     * selecting a preferred factory when many are found for the same service. The default
     * implementation returns {@link #priority} with no change. Subclasses should override
     * this method if they want to return a higher or lower priority.
     *
     * @since 2.3
     */
    public int getPriority() {
        return priority;
    }

    /**
     * Returns an {@linkplain Collections#unmodifiableMap unmodifiable} view of
     * {@linkplain #hints}.
     *
     * @return The map of hints, or an empty map if none.
     */
    public Map getImplementationHints() {
        return unmodifiableHints;
    }

    /**
     * Called when this factory is added to the given {@code category} of the given
     * {@code registry}. The factory may already be registered under another category
     * or categories.
     * <p>
     * This method is invoked automatically when this factory is registered as a plugin,
     * and should not be invoked directly by the user. The default implementation iterates
     * through all services under the same category that extends the {@code AbstractFactory}
     * class, and set the ordering according the priority given at construction time.
     *
     * @param registry A service registry where this factory has been registered.
     * @param category The registry category under which this object has been registered.
     *
     * @see #MINIMUM_PRIORITY
     * @see #MAXIMUM_PRIORITY
     */
    public void onRegistration(final ServiceRegistry registry, final Class category) {
        for (final Iterator it=registry.getServiceProviders(category, false); it.hasNext();) {
            final Object provider = it.next();
            if (provider!=this && provider instanceof AbstractFactory) {
                final AbstractFactory factory = (AbstractFactory) provider;
                final int priority = getPriority();
                final int compare  = factory.getPriority();
                final Object first, second;
                if (priority > compare) {
                    first  = this;
                    second = factory;
                } else if (priority < compare) {
                    first  = factory;
                    second = this;
                } else {
                    continue; // No ordering
                }
                registry.setOrdering(category, first, second);
            }
        }
    }

    /**
     * Called when this factory is removed from the given {@code category} of the given
     * {@code registry}. The object may still be registered under another category or categories.
     * <p>
     * This method is invoked automatically when this factory is no longer registered as a plugin,
     * and should not be invoked directly by the user.
     *
     * @param registry A service registry from which this object is being (wholly or partially)
     *                 deregistered.
     * @param category The registry category from which this object is being deregistered.
     */
    public void onDeregistration(final ServiceRegistry registry, final Class category) {
        // No action needed.
    }

    /**
     * Returns a hash value for this factory. The default implementation computes the hash
     * value using only immutable properties. This computation do <strong>not</strong> relies
     * on {@linkplain #getImplementationHints implementation hints}, since there is no garantee
     * that they will not change.
     *
     * @since 2.3
     */
    public final int hashCode() {
        return getClass().hashCode() + (37 * priority);
    }

    /**
     * Compares this factory with the specified object for equality.
     * The default implementation returns {@code true} if and only if:
     * <p>
     * <ul>
     *   <li>Both objects are of the exact same class
     *       (a <cite>is instance of</cite> relationship is not enough).</li>
     *   <li>{@linkplain #getImplementationHints implementation hints} are
     *       {@linkplain Map#equals equal}.</li>
     * </ul>
     * <p>
     * The requirement for the <cite>exact same class</cite> is needed for consistency with the
     * {@linkplain FactoryRegistry factory registry} working, since at most one instance of a given
     * class {@linkplain FactoryRegistry#getServiceProviderByClass) is allowed} in a registry.
     *
     * @since 2.3
     */
    public final boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (object!=null && object.getClass().equals(getClass())) {
            final AbstractFactory that = (AbstractFactory) object;
            return this.priority == that.priority &&
                   new FactoryComparator(this, that).compare(new HashSet());
        }
        return false;
    }

    /**
     * Returns a string representation of this factory. This method is mostly for debugging purpose,
     * so the string format may vary across different implementations or versions. The default
     * implementation formats all {@linkplain #getImplementationHints implementation hints} as a
     * tree. If the implementation hints include some {@linkplain Factory factory} dependencies,
     * then the implementation hints for those dependencies will appears under a tree branch.
     *
     * @since 2.3
     */
    public String toString() {
        final String name = format(this);
        final Map done = new IdentityHashMap(); // We don't want to rely on Factory.equals(...)
        done.put(this, name);
        final String tree = format(getImplementationHints(), done);
        return name + System.getProperty("line.separator", "\n") + tree;
    }

    /**
     * Returns a string representation of the specified hints. This is used by
     * {@link Hints#toString} in order to share the code provided in this class.
     */
    static String toString(final Map hints) {
        return format(hints, new IdentityHashMap());
    }

    /**
     * Formats a name for the specified factory.
     */
    private static String format(final Factory factory) {
        String name = Utilities.getShortClassName(factory);
        if (factory instanceof AuthorityFactory) {
            name = name + "[\"" + ((AuthorityFactory) factory).getAuthority().getTitle() + "\"]";
        }
        return name;
    }

    /**
     * Formats the specified hints. This method is just the starting
     * point for {@link #format(Writer, Map, String, Map)} below.
     */
    private static String format(final Map hints, final Map done) {
        final Writer table;
        try {
            table = new TableWriter(null, " ");
            format(table, hints, "  ", done);
        } catch (IOException e) {
            // Should never happen, since we are writing in a buffer.
            throw new AssertionError(e);
        }
        return table.toString();
    }

    /**
     * Formats recursively the tree. This method invoke itself.
     */
    private static void format(final Writer  table,
                               final Map     hints,
                               final String  indent,
                               final Map/*<Object,String>*/ done) throws IOException
    {
        final String[] keys   = new String[hints.size()];
        final Object[] values = new Object[keys.length];
        for (final Iterator it=hints.entrySet().iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry) it.next();
            String    key   = String.valueOf(entry.getKey());
            Object    value = entry.getValue();
            table.write(indent);
            table.write(key);
            table.write("\t= ");
            Factory recursive = null;
            if (value instanceof Factory) {
                recursive = (Factory) value;
                value = format(recursive);
                final String previous = (String) done.put(recursive, key);
                if (previous != null) {
                    done.put(recursive, previous);
                    table.write("(same as ");  // TODO: localize
                    table.write(previous);
                    value = ")";
                    recursive = null;
                }
            }
            table.write(String.valueOf(value));
            table.write('\n');
            if (recursive != null) {
                final String nextIndent = Utilities.spaces(indent.length() + 2);
                format(table, recursive.getImplementationHints(), nextIndent, done);
            }
        }
    }
}
