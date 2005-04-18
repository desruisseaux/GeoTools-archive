/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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


/**
 * Base interface for Geotools factories (ie service discovery).
 * <p>
 * This interfaces forms the core of the Geotools plug-in system, by which capabilities
 * can be added to the library at runtime. Each sub-interface defines a <cite>service</cite>.
 * <p>
 * Most factories are set up with concrete implementation being registered for use in
 * a <cite>service registry</cite>. Others provided registration mechanism that can be called
 * by application code.
 * </p>
 * <p>
 * Service registries don't need to be a Geotools implementation. They can be (but are not limited
 * to) {@link FactoryRegistry}. The steps to follows are:
 * </p>
 * <ul>
 * <li>Provide a public no-arguments constructor.</li>
 * <li>Add the fully qualified name of the <u>implementation</u> class in the
 *    {@code META-INF/services}<var>classname</var> where <var>classname</var>
 *     is the fully qualified name of the <u>interface</u> class.</li>
 * </ul>
 * <br>
 * <p>
 * In addition, it is recommended that implementations provide a constructor expecting
 * a single {@link Hints} argument. This optional argument gives to the user some control
 * of the factory's low-level details. The amount of control is factory specific. The geotools
 * library defines a globat class called Hints that is ment as API (ie you can assume these
 * hints are supported), factories may also provide information on their own custom hints
 * as part of their javadoc class description.
 * </p>
 * <ul>
 * Examples:
 * <li>the {@linkplain org.opengis.referencing.datum.DatumFactory datum factory} backing an
 * {@linkplain org.opengis.referencing.datum.DatumAuthorityFactory datum authority factory}).
 * <li>an application supplied FeatureFactory (ensuring all constructed features support the
 * IAdatpable interface), being passed to a FeatureTypeFactory so that all FeatureTypes
 * constructed will produce features supporting the indicated interface.
 * </ul>
 * As seen in the second example this concept opf a hint becomes more interesting when
 * the opperation being controlled is discovery of other services used by the Factory.
 * By supplying appropriate hints one can chain together several factories and retarget
 * them to an application specific task.
 * <p>
 * </p>
 * @author Ian Schneider
 * @author Martin Desruisseaux
 * @version $Id$
 *
 * @see Hints
 * @see FactoryRegistry
 */
public interface Factory {
    /**
     * Map of  hints (maybe {@linkplain java.util.Collections#unmodifiableMap unmodifiable})
     * used by this factory to customize its use.
     * <p>
     * Keys are usually static constants from the {@link Hints} class, while values are instances
     * of some key-dependent class. Any hints specified as {@link Class}
     * value shall be resolved before to be included in this map, i.e. {@link Class} values shall
     * be replaced by the actual instances used by this factory. This means that the map returned
     * by this method is usually not equals to the map given by the user.
     * </p>
     * <p>
     * The map's {@linkplain Map#keySet key set} shall contains <U>all</U> hints and <U>only</U>
     * hints impacting functionality, with {@code null} values for unspecified hints. This level
     * of accuracy helps to detect hints that matter and avoid the creation of unnecessary instance
     * of this factory.
     * </p>
     * <p>
     * Implementations of this method are usually quite simple. For example if a
     * {@linkplain org.opengis.referencing.datum.DatumAuthorityFactory datum authority factory}
     * uses an ordinary {@linkplain org.opengis.referencing.datum.DatumFactory datum factory},
     * its method could be implemented as below (note that we should <U>not</U> check if the
     * datum factory is null, since key with null value is the expected behaviour in this case):
     * <p>
     * Example:<pre><code>
     * Map hints = new HashMap();
     * hints.put({@linkplain Hints#DATUM_FACTORY}, datumFactory);
     * return hints;
     * </code></pre>
     * </p>
     * @return The map of hints, or an {@linkplain java.util.Collections#EMPTY_MAP empty map}
     *         if none.
     */
    Map/*<RenderingHints.Key,Object>*/ getImplementationHints();
}
