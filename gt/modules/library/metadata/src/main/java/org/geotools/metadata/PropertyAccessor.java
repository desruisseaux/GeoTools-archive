/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2007, GeoTools Project Managment Committee (PMC)
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
package org.geotools.metadata;

// J2SE dependencies
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

// Geotools implementation
import org.geotools.resources.XArray;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.Utilities;


/**
 * The getters declared in a GeoAPI interface, together with setters (if any)
 * declared in the Geotools implementation.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
final class PropertyAccessor {
    /**
     * The prefix for getters on boolean values.
     */
    private static final String IS = "is";

    /**
     * The prefix for getters (general case).
     */
    private static final String GET = "get";

    /**
     * The prefix for setters.
     */
    private static final String SET = "set";

    /**
     * Getters shared between many instances of this class. Two different implementations
     * may share the same getters but different setters.
     */
    private static final Map/*<Class, Method[]>*/ SHARED_GETTERS = new HashMap();

    /**
     * The implemented metadata interface.
     */
    private final Class type;

    /**
     * The implementation class. The following condition must hold:
     *
     * <blockquote><pre>
     * type.{@linkplain Class#isAssignableFrom isAssignableFrom}(implementation);
     * </pre></blockquote>
     */
    private final Class implementation;

    /**
     * The getter methods. This array should not contain any null element.
     */
    private final Method[] getters;

    /**
     * The corresponding setter methods, or {@code null} if none. This array must have
     * the same length than {@link #getters}. For every {@code getters[i]} element,
     * {@code setters[i]} is the corresponding setter or {@code null} if there is none.
     */
    private final Method[] setters;

    /**
     * Creates a new property reader for the specified metadata implementation.
     *
     * @param  metadata The metadata implementation to wrap.
     * @param  interfacePackage The root package for metadata interfaces.
     * @throws ClassCastException if the specified implementation class
     *         do not implements a metadata interface of the expected package.
     */
    PropertyAccessor(final Class implementation, final String interfacePackage)
            throws ClassCastException
    {
        this.implementation = implementation;
        type = getType(implementation, interfacePackage);
        assert type.isAssignableFrom(implementation) : implementation;
        getters = getGetters(type);
        Method[] setters = null;
        final Class[] arguments = new Class[1];
        for (int i=0; i<getters.length; i++) {
            final Method getter = getters[i];
            final Method setter; // To be determined later
            arguments[0] = getter.getReturnType();
            String name  = getter.getName();
            name = SET + name.substring(prefix(name).length());
            try {
                setter = implementation.getMethod(name, arguments);
            } catch (NoSuchMethodException e) {
                continue;
            }
            if (setters == null) {
                setters = new Method[getters.length];
            }
            setters[i] = setter;
        }
        this.setters = setters;
    }

    /**
     * Returns the metadata interface implemented by the specified implementation.
     * Only one metadata interface can be implemented.
     *
     * @param  metadata The metadata implementation to wraps.
     * @param  interfacePackage The root package for metadata interfaces.
     * @throws ClassCastException if the specified implementation class
     *         do not implements a metadata interface of the expected package.
     */
    private static Class getType(final Class implementation, final String interfacePackage)
            throws ClassCastException
    {
        if (!implementation.isInterface()) {
            final Class[] interfaces = implementation.getInterfaces();
            int count = 0;
            for (int i=0; i<interfaces.length; i++) {
                final Class candidate = interfaces[i];
                if (candidate.getName().startsWith(interfacePackage)) {
                    interfaces[count++] = candidate;
                }
            }
            if (count == 1) {
                return interfaces[0];
            }
        }
        throw new ClassCastException(Errors.format(ErrorKeys.UNKNOW_TYPE_$1,
                                     implementation.getName()));        
    }

    /**
     * Returns the getters. The returned array should never be modified,
     * since it may be shared among many instances of {@code PropertyAccessor}.
     *
     * @todo Ignore deprecated methods when we will be allowed to compile for J2SE 1.5.
     */
    private static Method[] getGetters(final Class type) {
        synchronized (SHARED_GETTERS) {
            Method[] getters = (Method[]) SHARED_GETTERS.get(type);
            if (getters == null) {
                getters = type.getMethods();
                int count = 0;
                for (int i=0; i<getters.length; i++) {
                    final Method candidate = getters[i];
                    final String name = candidate.getName();
                    if (name.startsWith(GET) || name.startsWith(IS)) {
                        getters[count++] = candidate;
                    }
                }
                getters = (Method[]) XArray.resize(getters, count);
                SHARED_GETTERS.put(type, getters);
            }
            return getters;
        }
    }

    /**
     * Returns the prefix of the specified method name.
     * We test the most common prefix first.
     */
    private static String prefix(final String name) {
        if (name.startsWith(GET)) {
            return GET;
        }
        if (name.startsWith(IS)) {
            return IS;
        }
        if (name.startsWith(SET)) {
            return SET;
        }
        // Should never happen since 'getGetters' filtered the methods.
        throw new AssertionError(name);
    }

    /**
     * Returns the number of properties that can be read.
     */
    final int count() {
        return getters.length;
    }

    /**
     * Returns the index of the specified property, or -1 if none.
     * The search is case-insensitive.
     */
    final int indexOf(String key) {
        key = key.trim();
        for (int i=0; i<getters.length; i++) {
            final String name = getters[i].getName();
            if (name.equalsIgnoreCase(key) ||
                name.regionMatches(true, prefix(name).length(), key, 0, key.length()))
            {
                return i;
            }
        }
        return -1;
    }

    /**
     * Returns {@code true} if the specified string starting at the specified index contains
     * no lower case characters. The characters don't have to be in upper case however (e.g.
     * non-alphabetic characters)
     */
    private static boolean isAcronym(final String name, int offset) {
        final int length = name.length();
        while (offset < length) {
            if (Character.isLowerCase(name.charAt(offset++))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the name of the property at the given index, or {@code null} if none.
     */
    final String name(final int index) {
        if (index >= 0 && index < getters.length) {
            String name = getters[index].getName();
            final int base = prefix(name).length();
            /*
             * Remove the "get" or "is" prefix and turn the first character after the
             * prefix into lower case. For example the method name "getTitle" will be
             * replaced by the property name "title". We will performs this operation
             * only if there is at least 1 character after the prefix.
             */
            if (name.length() > base) {
                if (isAcronym(name, base)) {
                    name = name.substring(base);
                } else {
                    name = Character.toLowerCase(name.charAt(base)) + name.substring(base + 1);
                }
            }
            return name;
        }
        return null;
    }

    /**
     * Returns the value for the specified metadata, or {@code null} if none.
     */
    final Object get(final int index, final Object metadata) {
        return (index >= 0 && index < getters.length) ? get(getters[index], metadata) : null;
    }

    /**
     * Gets a value from the specified metadata. We do not expect any checked exception to
     * be thrown, since {@code org.opengis.metadata} do not declare any.
     *
     * @param method The method to use for the query.
     * @param metadata The metadata object to query.
     */
    private static Object get(final Method method, final Object metadata) {
        assert method.getReturnType() != null;
        try {
            return method.invoke(metadata, (Object[]) null);
        } catch (IllegalAccessException e) {
            // Should never happen since 'getters' should contains only public methods.
            throw new AssertionError(e);
        } catch (InvocationTargetException e) {
            final Throwable cause = e.getTargetException();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            if (cause instanceof Error) {
                throw (Error) cause;
            }
            throw new UndeclaredThrowableException(cause);
        }
    }

    /**
     * Set a value for the specified metadata.
     *
     * @return The old value.
     * @throws IllegalArgumentException if the specified property can't be set.
     */
    final Object set(final int index, final Object metadata, final Object value)
            throws IllegalArgumentException
    {
        if (index >= 0 && index < getters.length && setters != null) {
            final Method setter = setters[index];
            if (setter != null) {
                final Object old = get(getters[index], metadata);
                set(setter, metadata, new Object[] {value});
                return old;
            }
        }
        throw new IllegalArgumentException(
                Errors.format(ErrorKeys.ILLEGAL_ARGUMENT_$1, "key"));
    }

    /**
     * Sets a value for the specified metadata. We do not expect any checked exception to
     * be thrown.
     *
     * @param method The method to use for the query.
     * @param metadata The metadata object to query.
     */
    private static void set(final Method method, final Object metadata, final Object[] arguments) {
        try {
            method.invoke(metadata, arguments);
        } catch (IllegalAccessException e) {
            // Should never happen since 'setters' should contains only public methods.
            throw new AssertionError(e);
        } catch (InvocationTargetException e) {
            final Throwable cause = e.getTargetException();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            if (cause instanceof Error) {
                throw (Error) cause;
            }
            throw new UndeclaredThrowableException(cause);
        }
    }

    /**
     * Compares the two specified metadata objects. The comparaison is <cite>shallow</cite>,
     * i.e. all metadata attributes are compared using the {@link Object#equals} method without
     * recursive call to this {@code shallowEquals} method for other metadata.
     * <p>
     * This method can optionaly excludes null values from the comparaison. In metadata,
     * null value often means "don't know", so in some occasion we want to consider two
     * metadata as different only if an attribute value is know for sure to be different.
     *
     * @param metadata1 The first metadata object to compare.
     * @param metadata2 The second metadata object to compare.
     * @param skipNulls If {@code true}, only non-null values will be compared.
     */
    public boolean shallowEquals(final Object metadata1, final Object metadata2, final boolean skipNulls) {
        assert type.isInstance(metadata1);
        assert type.isInstance(metadata2);
        for (int i=0; i<getters.length; i++) {
            final Method method = getters[i];
            final Object value1 = get(method, metadata1);
            final Object value2 = get(method, metadata2);
            if (!Utilities.equals(value1, value2)) {
                if (!skipNulls || (!isEmpty(value1) && !isEmpty(value2))) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Copies all metadata from source to target. The source can be any implementation of
     * the metadata interface, but the target must be the implementation expected by this
     * class.
     *
     * @param  source The metadata to copy.
     * @param  target The target metadata.
     * @param  skipNulls If {@code true}, only non-null values will be copied.
     * @return {@code true} in case of success, or {@code false} if at least
     *         one setter method was not found.
     * @throws UnmodifiableMetadataException if the target metadata is unmodifiable.
     */
    public boolean shallowCopy(final Object source, final Object target, final boolean skipNulls)
            throws UnmodifiableMetadataException
    {
        boolean success = true;
        assert type          .isInstance(source);
        assert implementation.isInstance(target);
        final Object[] arguments = new Object[1];
        for (int i=0; i<getters.length; i++) {
            arguments[0] = get(getters[i], source);
            if (!skipNulls || !isEmpty(arguments[0])) {
                if (setters == null) {
                    return false;
                }
                final Method setter = setters[i];
                if (setter != null) {
                    set(setter, target, arguments);
                } else {
                    success = false;
                }
            }
        }
        return success;
    }

    /**
     * Counts the number of non-null properties.
     */
    public int count(final Object metadata, final int max) {
        assert type.isInstance(metadata);
        int count = 0;
        for (int i=0; i<getters.length; i++) {
            if (!isEmpty(get(getters[i], metadata))) {
                if (++count >= max) {
                    break;
                }
            }
        }
        return count;
    }

    /**
     * Returns {@code true} if the specified object is null or an empty collection.
     */
    static boolean isEmpty(final Object value) {
        return value == null || ((value instanceof Collection) && ((Collection) value).isEmpty());
    }

    /**
     * Returns {@code true} if the specified object implements the expected interface.
     *
     * @throws ClassCastException if the specified implementation class do
     *         not implements a metadata interface of the expected package.
     */
    final boolean sameInterface(final Class implementation, final String interfacePackage)
            throws ClassCastException
    {
        return type.equals(getType(implementation, interfacePackage));
    }

    /**
     * Makes sure that the specified metadata is of the expected type.
     */
    final void ensureValidType(final Object metadata) throws ClassCastException {
        if (!type.isInstance(metadata)) {
            if (metadata == null) {
                throw new ClassCastException(Errors.format(ErrorKeys.NULL_ARGUMENT_$1, "metadata"));
            }
            throw new ClassCastException(Errors.format(ErrorKeys.ILLEGAL_CLASS_$2,
                    metadata.getClass().getName(), type.getName()));
        }
    }
}
