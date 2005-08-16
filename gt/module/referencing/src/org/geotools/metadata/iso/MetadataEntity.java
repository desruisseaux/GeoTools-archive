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
package org.geotools.metadata.iso;

// J2SE dependencies
import java.util.Set;
import java.util.Map;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Logger;
import java.io.Serializable;

// OpenGIS dependencies
import org.opengis.util.Cloneable;

// Geotools dependencies
import org.geotools.util.CheckedArrayList;
import org.geotools.util.CheckedHashSet;



/**
 * A superclass for implementing ISO 19115 metadata interfaces. Subclasses must implement
 * at least one of the ISO MetaData interface provided by
 * <A HREF="http://geoapi.sourceforge.net">GeoAPI</A>.
 *
 * <H3>Contract of the {@link #clone() clone()} method</H3>
 * <P>While {@linkplain java.lang.Cloneable cloneable}, this class do not provides the
 * {@link #clone() clone()} operation as part of the public API. The clone operation is
 * required for the internal working of the {@link #unmodifiable()} method, which expect
 * from {@link #clone() clone()} a <strong>shalow</strong> copy of this metadata entity.
 * The default implementation of {@link #clone() clone()} is suffisient for must uses.
 * However, subclasses are required to overrides the {@link #freeze} method.</P>
 *
 * @since 2.1
 * @version $Id$
 * @author Jody Garnett
 * @author Martin Desruisseaux
 */
public class MetadataEntity implements java.lang.Cloneable, Serializable {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 5730550742604669102L;

    /**
     * The logger for metadata implementation.
     */
    protected static final Logger LOGGER = Logger.getLogger("org.geotools.metadata");

    /**
     * An unmodifiable copy of this metadata. Will be created only when first needed.
     * If {@code null}, then no unmodifiable entity is available.
     * If {@code this}, then this entity is itself unmodifiable.
     */
    private transient MetadataEntity unmodifiable;

    /**
     * Construct a default metadata entity.
     */
    protected MetadataEntity() {
    }

    /**
     * Returns {@code true} if this metadata entity is modifiable.
     * This method returns {@code false} if {@link #unmodifiable()}
     * has been invoked on this object.
     */
    public boolean isModifiable() {
        return unmodifiable != this;
    }

    /**
     * Returns an unmodifiable copy of this metadata. Any attempt to modify an attribute of the
     * returned object will throw an {@link UnsupportedOperationException}. If this metadata is
     * already unmodifiable, then this method returns {@code this}.
     *
     * @return An unmodifiable copy of this metadata.
     */
    public synchronized MetadataEntity unmodifiable() {
        if (unmodifiable == null) {
            try {
                /*
                 * Need a SHALOW copy of this metadata, because some attributes
                 * may already be unmodifiable and we don't want to clone them.
                 */
                unmodifiable = (MetadataEntity) clone();
            } catch (CloneNotSupportedException exception) {
                /*
                 * The metadata is not cloneable for some reason left to the user
                 * (for example it may be backed by some external database).
                 * Assumes that the metadata is unmodifiable.
                 */
                // TODO: localize the warning.
                LOGGER.warning("Cant't clone the medata. Assumes it is immutable.");
                return this;
            }
            unmodifiable.freeze();
        }
        return unmodifiable;
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
     * Declare this metadata and all its attributes as unmodifiable. This method is invoked
     * automatically by the {@link #unmodifiable()} method. Subclasses should overrides
     * this method and invokes {@link #unmodifiable(Object)} for all attributes.
     */
    protected void freeze() {
        unmodifiable = this;
    }

    /**
     * Check if changes in the metadata are allowed. All {@code setFoo(...)} methods in
     * sub-classes should invoke this method before to apply any change.
     *
     * @throws UnsupportedOperationException if this metadata is unmodifiable.
     */
    protected void checkWritePermission() throws UnsupportedOperationException {
        assert Thread.holdsLock(this);
        if (unmodifiable == this) {
            // TODO: Localize the error message.
            throw new UnsupportedOperationException("Unmodifiable metadata");
        }
        unmodifiable = null;
    }

    /**
     * Copy the content of one collection ({@code source}) into an other ({@code target}).
     * If the target collection is {@code null}, or if its type ({@link List} vs {@link Set})
     * doesn't matches the type of the source collection, a new target collection is expected.
     *
     * @param  source      The source collection.
     * @param  target      The target collection, or {@code null} if not yet created.
     * @param  elementType The base type of elements to put in the collection.
     * @return {@code target}, or a newly created collection.
     */
    protected final Collection copyCollection(final Collection source, Collection target,
                                              final Class elementType)
    {
        checkWritePermission();
        if (source == null) {
            if (target != null) {
                target.clear();
            }
        } else {
            final boolean isList = (source instanceof List);
            if (target==null || (target instanceof List)!=isList) {
                // TODO: remove the cast once we are allowed to compile for J2SE 1.5.
                target = isList ? (Collection) new CheckedArrayList(elementType)
                                : (Collection) new CheckedHashSet  (elementType);
            } else {
                target.clear();
            }
            target.addAll(source);
        }
        return target;
    }

    /**
     * Returns the specified collection, or a new one if {@code c} is null.
     * This is a convenience method for implementation of {@code getFoo()}
     * methods.
     *
     * @param  c The collection to checks.
     * @param  elementType The element type (used only if {@code c} is null).
     * @return {@code c}, or a new collection if {@code c} is null.
     */
    protected final Collection nonNullCollection(final Collection c, final Class elementType) {
        assert Thread.holdsLock(this);
        if (c != null) {
            return c;
        }
        if (isModifiable()) {
            return new CheckedHashSet(elementType);
        }
        return Collections.EMPTY_SET;
    }

    /**
     * Add a line separator to the given buffer, except if the buffer is empty.
     * This convenience method is used for {@link #toString} implementations.
     */
    protected static void appendLineSeparator(final StringBuffer buffer) {
        if (buffer.length() != 0) {
            buffer.append(System.getProperty("line.separator", "\n"));
        }
    }
}
