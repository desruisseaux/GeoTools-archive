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
package org.geotools.resources;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;


/**
 * A set of miscellaneous methods related to classes.
 *
 * @since 2.5
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public final class Classes {
    /**
     * Forbid object creation.
     */
    private Classes() {
    }

    /**
     * Returns the class of the specified object, or {@code null} if {@code object} is null.
     */
    @SuppressWarnings("unchecked")
    public static <T> Class<? extends T> getClass(final T object) {
        return (object != null) ? (Class) object.getClass() : null;
    }

    /**
     * Returns all classes implemented by the given set of objects.
     */
    private static Set<Class<?>> getClasses(final Collection<?> objects) {
        final Set<Class<?>> types = new LinkedHashSet<Class<?>>();
        for (final Object object : objects) {
            if (object != null) {
                types.add(object.getClass());
            }
        }
        return types;
    }

    /**
     * Returns the most specific class implemented by the objects in the given collection.
     * If no class are {@linkplain Class#isAssignableFrom assignable} to all others, then
     * this method returns the {@linkplain #commonClass most specific common super class}.
     *
     * @param  objects A collection of objects. May contains duplicated values and null values.
     * @return The most specific class.
     */
    public static Class<?> specializedClass(final Collection<?> objects) {
        final Set<Class<?>> types = getClasses(objects);
        final Class<?> type = removeAssignables(types);
        return (type != null) ? type : commonSuperClass(types);
    }

    /**
     * Returns the most specific class which is a common parent of all specified objects.
     *
     * @param  objects A collection of objects. May contains duplicated values and null values.
     * @return The most specific class common to all supplied objects.
     */
    public static Class<?> commonClass(final Collection<?> objects) {
        final Set<Class<?>> types = getClasses(objects);
        /*
         * First check if a type is assignable from all other types. At most one such
         * type can exists. We check for it first in order to avoid the creation of a
         * temporary HashSet if such type is found.
         */
search: for (final Class<?> candidate : types) {
            for (final Class<?> type : types) {
                if (!candidate.isAssignableFrom(type)) {
                    continue search;
                }
            }
            return candidate;
        }
        return commonSuperClass(types);
    }

    /**
     * Returns the most specific class which is a common parent of all the specified classes.
     * This method should be invoked when no common parent has been found in the supplied list.
     */
    private static Class<?> commonSuperClass(final Collection<Class<?>> types) {
        // Build a list of all super classes.
        final Set<Class<?>> superTypes = new LinkedHashSet<Class<?>>();
        for (Class<?> type : types) {
            while ((type = type.getSuperclass()) != null) {
                if (!superTypes.add(type)) {
                    // If the type was already in the set, then its super-types are in the set too.
                    break;
                }
            }
        }
        // Removes every elements that are not assignable from every supplied types.
        for (final Iterator<Class<?>> it=superTypes.iterator(); it.hasNext();) {
            final Class<?> candidate = it.next();
            for (final Class<?> type : types) {
                if (!candidate.isAssignableFrom(type)) {
                    it.remove();
                    break;
                }
            }
        }
        // Now removes every classes that can be assigned from an other classes.
        // We should have only one left, the most specific one in the hierarchy.
        return removeAssignables(superTypes);
    }

    /**
     * Removes every classes in the specified collection which are assignable from an other
     * class from the same collection. As a result of this method call, the given collection
     * should contains only leaf classes.
     *
     * @param  types The collection to trim.
     * @return If there is exactly one element left, that element. Otherwise {@code null}.
     */
    private static Class<?> removeAssignables(final Collection<Class<?>> types) {
        for (final Iterator<Class<?>> it=types.iterator(); it.hasNext();) {
            final Class<?> candidate = it.next();
            for (final Class<?> type : types) {
                if (candidate != type && candidate.isAssignableFrom(type)) {
                    it.remove();
                    break;
                }
            }
        }
        return (types.size() == 1) ? types.iterator().next() : null;
    }

    /**
     * Returns {@code true} if the two specified objects implements exactly the same set of
     * interfaces. Only interfaces assignable to {@code base} are compared. Declaration order
     * doesn't matter. For example in ISO 19111, different interfaces exist for different coordinate
     * system geometries ({@code CartesianCS}, {@code PolarCS}, etc.). We can check if two
     * CS implementations has the same geometry with the following code:
     *
     * <blockquote><code>
     * if (sameInterfaces(cs1, cs2, {@linkplain org.opengis.referencing.cs.CoordinateSystem}.class))
     * </code></blockquote>
     */
    public static <T> boolean sameInterfaces(final Class<? extends T> object1,
                                             final Class<? extends T> object2,
                                             final Class<T> base)
    {
        if (object1 == object2) {
            return true;
        }
        if (object1==null || object2==null) {
            return false;
        }
        final Class<?>[] c1 = object1.getInterfaces();
        final Class<?>[] c2 = object2.getInterfaces();
        /*
         * Trim all interfaces that are not assignable to 'base' in the 'c2' array.
         * Doing this once will avoid to redo the same test many time in the inner
         * loops j=[0..n].
         */
        int n = 0;
        for (int i=0; i<c2.length; i++) {
            final Class<?> c = c2[i];
            if (base.isAssignableFrom(c)) {
                c2[n++] = c;
            }
        }
        /*
         * For each interface assignable to 'base' in the 'c1' array, check if
         * this interface exists also in the 'c2' array. Order doesn't matter.
         */
compare:for (int i=0; i<c1.length; i++) {
            final Class<?> c = c1[i];
            if (base.isAssignableFrom(c)) {
                for (int j=0; j<n; j++) {
                    if (c.equals(c2[j])) {
                        System.arraycopy(c2, j+1, c2, j, --n-j);
                        continue compare;
                    }
                }
                return false; // Interface not found in 'c2'.
            }
        }
        return n == 0; // If n>0, at least one interface was not found in 'c1'.
    }

    /**
     * Returns a short class name for the specified class. This method will
     * omit the package name.  For example, it will return "String" instead
     * of "java.lang.String" for a {@link String} object. It will also name
     * array according Java language usage,  for example "double[]" instead
     * of "[D".
     *
     * @param  classe The object class (may be {@code null}).
     * @return A short class name for the specified object.
     */
    public static String getShortName(Class<?> classe) {
        if (classe == null) {
            return "<*>";
        }
        String name = classe.getSimpleName();
        Class<?> enclosing = classe.getEnclosingClass();
        if (enclosing != null) {
            final StringBuilder buffer = new StringBuilder();
            do {
                buffer.insert(0, '.').insert(0, enclosing.getSimpleName());
            } while ((enclosing = enclosing.getEnclosingClass()) != null);
            name = buffer.append(name).toString();
        }
        return name;
    }

    /**
     * Returns a short class name for the specified object. This method will
     * omit the package name. For example, it will return "String" instead
     * of "java.lang.String" for a {@link String} object.
     *
     * @param  object The object (may be {@code null}).
     * @return A short class name for the specified object.
     */
    public static String getShortClassName(final Object object) {
        return getShortName(getClass(object));
    }
}
