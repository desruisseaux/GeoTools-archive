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
 * Base interface for Geotools factories. Each sub-interface defines a <cite>service</cite>.
 * Each concrete implementation shall be registered for use in a <cite>service registry</cite>.
 * Service registries don't need to be a Geotools implementation. They can be (but are not limited
 * to) {@link FactoryRegistry}. The steps to follows are:
 * <br><br>
 * <ul>
 *   <li>Provide a public no-arguments constructor.</li>
 *   <li>Add the fully qualified name of the <u>implementation</u> class in the
 *       {@code META-INF/services}<var>classname</var> where <var>classname</var>
 *       is the fully qualified name of the <u>interface</u> class.</li>
 * </ul>
 * <br><br>
 * In addition, implementations may provide a constructor expecting a single {@link Hints} argument.
 * This optional argument gives to the user some control on factory's low-level details (for example
 * the {@linkplain org.opengis.referencing.datum.DatumFactory datum factory} backing an
 * {@linkplain org.opengis.referencing.datum.DatumAuthorityFactory datum authority factory}).
 *
 * @author Ian Schneider
 * @author Martin Desruisseaux
 * @version $Id$
 *
 * @see Hints
 * @see FactoryRegistry
 */
public interface Factory {
    /**
     * Returns a (maybe {@linkplain java.util.Collections#unmodifiableMap unmodifiable}) map of
     * hints used by this factory. Keys are usually static constants from the {@link Hints} class,
     * while values are instances of some key-dependent class. Any hints specified as {@link Class}
     * value shall be resolved before to be included in this map, i.e. {@link Class} values shall
     * be replaced by the actual instances used by this factory. This means that the map returned
     * by this method is usually not equals to the map given by the user.
     * <br><br>
     * The map's {@linkplain Map#keySet key set} shall contains <U>all</U> hints and <U>only</U>
     * hints impacting functionality, with {@code null} values for unspecified hints. This level
     * of accuracy helps to detect hints that matter and avoid the creation of unnecessary instance
     * of this factory.
     * <br><br>
     * Implementations of this method are usually quite simple. For example if a
     * {@linkplain org.opengis.referencing.datum.DatumAuthorityFactory datum authority factory}
     * uses an ordinary {@linkplain org.opengis.referencing.datum.DatumFactory datum factory},
     * its method could be implemented as below (note that we should <U>not</U> check if the
     * datum factory is null, since key with null value is the expected behaviour in this case):
     *
     * <blockquote><pre>
     * Map hints = new HashMap();
     * hints.put({@linkplain Hints#DATUM_FACTORY}, datumFactory);
     * return hints;
     * </pre></blockquote>
     *
     * @return The map of hints, or an {@linkplain java.util.Collections#EMPTY_MAP empty map}
     *         if none.
     */
    Map/*<RenderingHints.Key,Object>*/ getImplementationHints();
}
