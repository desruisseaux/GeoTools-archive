/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004-2006, GeoTools Project Managment Committee (PMC)
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
package org.geotools.metadata.iso;

// J2SE dependencies
import java.util.Set;
import java.util.Map;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.io.Serializable;

// OpenGIS dependencies
import org.opengis.util.Cloneable;

// Geotools dependencies
import org.geotools.metadata.MetadataStandard;
import org.geotools.metadata.ModifiableMetadata;
import org.geotools.metadata.InvalidMetadataException;
import org.geotools.metadata.UnmodifiableMetadataException;
import org.geotools.util.CheckedArrayList;
import org.geotools.util.CheckedHashSet;
import org.geotools.util.Logging;
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.i18n.ErrorKeys;


/**
 * A superclass for implementing ISO 19115 metadata interfaces. Subclasses
 * must implement at least one of the ISO MetaData interface provided by
 * <A HREF="http://geoapi.sourceforge.net">GeoAPI</A>.
 *
 * @since 2.1
 * @source $URL$
 * @version $Id$
 * @author Jody Garnett
 * @author Martin Desruisseaux
 */
public class MetadataEntity extends ModifiableMetadata implements Serializable {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 5730550742604669102L;

    /**
     * Constructs an initially empty metadata entity.
     */
    protected MetadataEntity() {
        super();
    }

    /**
     * Constructs a metadata entity initialized with the values from the specified metadata.
     * The {@code source} metadata must implements the same metadata interface than this class.
     *
     * @param  source The metadata to copy values from.
     * @throws ClassCastException if the specified metadata don't implements the expected
     *         metadata interface.
     *
     * @since 2.4
     */
    protected MetadataEntity(final Object source) throws ClassCastException {
        super(source);
    }

    /**
     * Returns the metadata standard implemented by subclasses,
     * which is {@linkplain MetadataStandard#ISO_19115 ISO 19115}.
     *
     * @since 2.4
     */
    public MetadataStandard getStandard() {
        return MetadataStandard.ISO_19115;
    }

    /**
     * Returns an unmodifiable copy of the the specified object. This method is used for
     * implementation of {@link #freeze} method by subclasses. This method performs the
     * following heuristic tests:<br>
     *
     * <ul>
     *   <li>If the specified object is an instance of {@code MetadataEntity}, then
     *       {@link #unmodifiable()} is invoked on this object.</li>
     *   <li>Otherwise, if the object is a {@linkplain Cloneable cloneable}
     *       {@linkplain Collection collection}, then the collection is cloned and all its
     *       elements are replaced by their unmodifiable variant. If the collection is not
     *       cloneable, then it is assumed immutable and returned unchanged.</li>
     *   <li>Otherwise, the object is assumed immutable and returned unchanged.</li>
     * </ul>
     *
     * @param  object The object to convert in an immutable one.
     * @return A presumed immutable view of the specified object.
     *
     * @deprecated No longuer needed, since {@link #freeze} is now implemented
     *             in the general case using Java reflections.
     */
    protected static Object unmodifiable(final Object object) {
        /*
         * CASE 1 - The object is an implementation of MetadataEntity. It may have
         *          its own algorithm for creating an unmodifiable view of metadata.
         */
        if (object instanceof MetadataEntity) {
            return ((MetadataEntity) object).unmodifiable();
        }
        /*
         * CASE 2 - The object is a collection. If the collection is not cloneable, it is assumed
         *          immutable and returned unchanged. Otherwise, the collection is cloned and all
         *          elements are replaced by their unmodifiable variant.
         */
        if (object instanceof Collection) {
            Collection collection = (Collection) object;
            if (collection.isEmpty()) {
                return null;
            }
            if (collection instanceof Cloneable) {
                collection = (Collection) ((Cloneable) collection).clone();
                final List buffer;
                if (collection instanceof List) {
                    // If the collection is an array list, we will update the element in place...
                    buffer = (List) collection;
                } else {
                    // ...otherwise, we will copy them in a temporary buffer.
                    buffer = new ArrayList(collection.size());
                }
                int index = 0;
                boolean refill = false;
                for (final Iterator it=collection.iterator(); it.hasNext(); index++) {
                    final Object  original = it.next();
                    final Object  copy     = unmodifiable(original);
                    final boolean changed  = (original != copy);
                    if (buffer == collection) {
                        if (changed) {
                            buffer.set(index, copy);
                        }
                    } else {
                        buffer.add(copy);
                        refill |= changed;
                    }
                }
                if (refill) {
                    collection.clear();
                    collection.addAll(buffer);
                }
                if (collection instanceof List) {
                    return Collections.unmodifiableList((List) collection);
                }
                if (collection instanceof Set) {
                    return Collections.unmodifiableSet((Set) collection);
                }
                return Collections.unmodifiableCollection(collection);
            }
            return collection;
        }
        /*
         * CASE 3 - The object is a map. If the map is cloneable, then copy all
         *          entries in a new map.
         */
        if (object instanceof Map) {
            final Map map = (Map) object;
            if (map.isEmpty()) {
                return null;
            }
            if (map instanceof Cloneable) {
                final Map copy = (Map) ((Cloneable) map).clone();
                copy.clear(); // The clone was for constructing an instance of the same class.
                for (final Iterator it=map.entrySet().iterator(); it.hasNext();) {
                    final Map.Entry entry = (Map.Entry) it.next();
                    copy.put(unmodifiable(entry.getKey()), unmodifiable(entry.getValue()));
                }
                return Collections.unmodifiableMap(copy);
            }
            return map;
        }
        /*
         * CASE 4 - Any other case. The object is assumed immutable and returned unchanged.
         */
        return object;
    }

    /**
     * Makes sure that an argument is non-null. This is used for checking if
     * a mandatory attribute is presents.
     *
     * @param  name   Argument name.
     * @param  object User argument.
     * @throws InvalidMetadataException if {@code object} is null.
     *
     * @since 2.4
     */
    protected static void ensureNonNull(final String name, final Object object)
            throws InvalidMetadataException
    {
        if (object == null) {
            throw new InvalidMetadataException(Errors.format(ErrorKeys.NULL_ATTRIBUTE_$1, name));
        }
    }

    /**
     * Add a line separator to the given buffer, except if the buffer is empty.
     * This convenience method is used for {@link #toString} implementations.
     *
     * @deprecated Not needed anymore since we now inherit a default {@link #toString}
     *             method for all metadata.
     */
    protected static void appendLineSeparator(final StringBuffer buffer) {
        if (buffer.length() != 0) {
            buffer.append(System.getProperty("line.separator", "\n"));
        }
    }
}
