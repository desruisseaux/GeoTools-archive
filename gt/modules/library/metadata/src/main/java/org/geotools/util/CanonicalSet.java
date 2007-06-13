/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2003-2007, Geotools Project Managment Committee (PMC)
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

// Collections and references
import java.util.Set;
import java.util.WeakHashMap;

/**
 * A canonical set of objects, used to optimize memory use.
 * <p>
 * The operation of this set is similar in spirit to the {@link String.intern} method.
 * The following example shows a convenient way to use {@code CanonicalSet} as an
 * internal pool of immutable objects.<pre><code>
 * public Foo create( String definition ){
 *      Foo created = new Foo( definition );
 *      return (Foo) canionicalSet.toUnqiue( created );
 * }
 * </code></pre>
 * As shown above the {@code CanonicalSet} has a  {@link #get}  method that is not part
 * of the {@link Set} interface. This {@code get} method retrieves an entry from this
 * set that is equals to the supplied object.
 * </p>
 * <p>
 * The set of objects is held by weak references. An entry in a {@code CanonicalSet}
 * will automatically be removed when it is no longer in ordinary use. More precisely,
 * the presence of a entry will not prevent the entry from being discarded by the
 * garbage collector, that is, made finalizable, finalized, and then reclaimed.
 * When an entry has been discarded it is effectively removed from the set, so
 * this class behaves somewhat differently than other {@link Set} implementations.
 * </p>
 *  
 * <p>
 * The {@code CanonicalSet} class is thread-safe.
 *
 * @since 2.1
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @see WeakHashMap
 */
public class CanonicalSet extends WeakHashSet {

    /**
     * Construct a {@code CanonicalSet}.
     */
    public CanonicalSet() {
    }

    /**
     * Returns an object equals to {@code object} if such an object already exist in this
     * {@code CanonicalSet}. Otherwise, adds {@code object} to this {@code CanonicalSet}.
     * This method is equivalents to the following code:
     */
    public synchronized Object toUnqiue(final Object object) {
        return intern(object, INTERN);
    }

}
