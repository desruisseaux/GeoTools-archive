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
package org.geotools.metadata;

// J2SE dependencies
import java.util.Set;
import java.util.Map;
import java.util.List;
import java.util.Arrays;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.RandomAccess;
import java.util.Locale;
import java.util.logging.Logger;
import java.io.Serializable;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;

// OpenGIS dependencies
import org.opengis.util.Cloneable;

// Geotools dependencies
import org.geotools.resources.Utilities;
import org.geotools.resources.rsc.Resources;
import org.geotools.resources.rsc.ResourceKeys;
import org.geotools.util.CheckedArrayList;
import org.geotools.util.CheckedHashSet;
import org.geotools.xml.XPathFactory;


/**
 * A superclass for implementing ISO 19115 metadata interfaces and allowing
 * Expr based query via
 * {@link org.opengis.catalog.MetadataEntity.EntityType} and
 * {@link org.opengis.catalog.MetadataEntity.Element}.
 * 
 * A subclass <strong>must</strong> implement minimum one of the ISO MetaData interface
 * provided by <A HREF="http://geoapi.sourceforge.net">GeoAPI</A>.
 *
 * <code>MetadataEntity</code> uses BeanInfo style reflection to identify all the attributes
 * implemented by the subclass as part of a <A HREF="http://geoapi.sourceforge.net">GeoAPI</A>
 * metadata interface. The BeanInfo attributes are used to construct all the
 * {@link org.opengis.catalog.MetadataEntity.EntityType} and
 * {@link org.opengis.catalog.MetadataEntity.Element} objects.
 * 
 * The type of each attribue is used to determine whether the element is a simple
 * {@link org.opengis.catalog.MetadataEntity.Element} or
 * {@link org.opengis.catalog.MetadataEntity.EntityType}.
 *
 * <H2>Contract of the {@link #clone() clone()} method</H2>
 * <P>While {@linkplain java.lang.Cloneable cloneable}, this class do not provides the
 * {@link #clone() clone()} operation as part of the public API. The clone operation is
 * required for the internal working of the {@link #unmodifiable()} method, which expect
 * from {@link #clone() clone()} a <strong>shalow</strong> copy of this metadata entity.
 * The default implementation of {@link #clone() clone()} is suffisient for must uses.
 * However, subclasses are required to overrides the {@link #freeze} method.</P>
 *
 * @author Jody Garnett
 * @author Martin Desruisseaux
 * @since 2.1
 */
public class MetadataEntity implements org.opengis.catalog.MetadataEntity,
                                       java.lang.Cloneable, Serializable
{
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 5730550742604669102L;

    /**
     * The logger for metadata implementation.
     */
    protected static final Logger LOGGER = Logger.getLogger("org.geotools.metadata");
    
    /**
     * The entity type for this metadata. Will be constructed only when first needed.
     */
    private transient BeanEntity entity;

    /**
     * An unmodifiable copy of this metadata. Will be created only when first needed.
     * If <code>null</code>, then no unmodifiable entity is available.
     * If <code>this</code>, then this entity is itself unmodifiable.
     */
    private transient MetadataEntity unmodifiable;

    /**
     * Construct a default metadata entity.
     */
    protected MetadataEntity() {
    }

    /**
     * Access to the values associated with metadata
     * {@linkplain org.opengis.catalog.MetadataEntity.Element elements}.
     * The list is returned in the same order as described by the
     * {@link org.opengis.catalog.MetadataEntity.EntityType} schema information.
     *
     * @return List of {@linkplain org.opengis.catalog.MetadataEntity.Element elements}.
     *
     * @deprecated This method was implemented for catalog support, but the catalog API 1.0 has
     *             been withdrawn from GeoAPI. The catalog API need to be revisited in light of
     *             latest specification (2.0) before to bring it back to GeoAPI. I suspect that
     *             this method will be removed from {@code MetadataEntity}, but actually would
     *             moves in an other class (some kind of wrapper around arbitrary metadata
     *             interface, not just Geotools implementations).
     */
    public final List elements() {
        final BeanEntity entity = (BeanEntity) getEntityType();
        final List elements = new ArrayList(entity.getElements().size());
        for (final Iterator iter=entity.propertyMap.values().iterator(); iter.hasNext();) {
            final Method method = ((PropertyDescriptor) iter.next()).getReadMethod();
            final Object value;
            try {
                value = method.invoke(this, (Object[]) null);
            } catch (IllegalAccessException exception) {
                /*
                 * The method call failed because the method is not accessible.
                 * This exception should not occurs, since interface methods are public.
                 */
                throw new AssertionError(exception);
            } catch (InvocationTargetException exception) {
                /*
                 * An exception occured inside the user's method. This is not BeanEntity's fault.
                 * Rethrows the exception as an unchecked one.
                 */
                final Throwable cause = exception.getCause();
                if (cause instanceof RuntimeException) {
                    throw (RuntimeException) cause;
                }
                throw new UndeclaredThrowableException(cause);
            }
            elements.add(value);
        }
        return elements;
    }

    /**
     * Access to Metadata element values specified by the xPath expression.
     * Usually used to return individual objects, if the xPath expression
     * matches multiple elements a list will be returned.
     *
     * @param xPath XPath representation of element location.
     * @return element value, List of element value, or null if xPath did not match anything.
     *
     * @deprecated This method was implemented for catalog support, but the catalog API 1.0 has
     *             been withdrawn from GeoAPI. The catalog API need to be revisited in light of
     *             latest specification (2.0) before to bring it back to GeoAPI. I suspect that
     *             this method will be removed from {@code MetadataEntity}, but actually would
     *             moves in an other class (some kind of wrapper around arbitrary metadata
     *             interface, not just Geotools implementations).
     */
    public final Object getElement(final String xPath) {
        final List elements = XPathFactory.value(xPath, this);
        switch (elements.size()) {
            case 0:  return null;
            case 1:  return elements.get(0);
            default: return elements;
        }
    }

    /**
     * Gets the value of the provided element.
     * 
     * @param  element Element that indicates the value the caller wishes to obtain.
     * @return The value of the element the parameter represents
     *         null if the current Metadata does not contain the element.
     *
     * @deprecated This method was implemented for catalog support, but the catalog API 1.0 has
     *             been withdrawn from GeoAPI. The catalog API need to be revisited in light of
     *             latest specification (2.0) before to bring it back to GeoAPI. I suspect that
     *             this method will be removed from {@code MetadataEntity}, but actually would
     *             moves in an other class (some kind of wrapper around arbitrary metadata
     *             interface, not just Geotools implementations).
     */
    public final Object getElement(final Element element) {
        final BeanElement elemImpl;
        if (element instanceof BeanElement) {
            elemImpl = (BeanElement) element;
        } else {
            elemImpl = (BeanElement) getEntityType().getElement(element.getName());
        }        
        final Method method = elemImpl.property.getReadMethod();
        final Object value;
        try {
            value = method.invoke(this, (Object[]) null);
        } catch (IllegalAccessException exception) {
            /*
             * The method call failed because the method is not accessible.
             * This exception should not occurs, since interface methods are public.
             */
            throw new AssertionError(exception);
        } catch (InvocationTargetException exception) {
            /*
             * An exception occured inside the user's method. This is not BeanEntity's fault.
             * Rethrows the exception as an unchecked one.
             */
            final Throwable cause = exception.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            throw new UndeclaredThrowableException(cause);
        }
        return value;
    }

    /**
     * Returns the entity type that describes the schema of this Metadata.
     *
     * @return the entity type that describes the current Metadata object.
     *
     * @deprecated This method was implemented for catalog support, but the catalog API 1.0 has
     *             been withdrawn from GeoAPI. The catalog API need to be revisited in light of
     *             latest specification (2.0) before to bring it back to GeoAPI. I suspect that
     *             this method will be removed from {@code MetadataEntity}, but actually would
     *             moves in an other class (some kind of wrapper around arbitrary metadata
     *             interface, not just Geotools implementations).
     */
    public final EntityType getEntityType() {
        if (entity == null) try {
            entity = BeanEntity.getEntity(getClass());
        } catch (IntrospectionException exception) {
            final IllegalStateException e = new IllegalStateException(Resources.format(
                  ResourceKeys.ERROR_UNSUPPORTED_DATA_TYPE_$1, Utilities.getShortClassName(this)));
            e.initCause(exception);
            throw e;
        }
        return entity;
    }

    /**
     * A basic implementation of the {@link org.opengis.catalog.MetadataEntity.EntityType}
     * interface which uses uses reflection to examine the structure of a metadata. Instances
     * of this class can be created by the {@link #getEntity} method only.
     * 
     * @author Jody Garnett
     * @author Martin Desruisseaux
     *
     * @deprecated This class was implemented for catalog support, but the catalog API 1.0 has
     *             been withdrawn from GeoAPI. The catalog API need to be revisited in light of
     *             latest specification (2.0) before to bring it back to GeoAPI. I suspect that
     *             this inner class will be removed from {@code MetadataEntity}, but actually
     *             would moves in an other class (some kind of wrapper around arbitrary metadata
     *             interface, not just Geotools implementations).
     */
    private static final class BeanEntity implements EntityType {
        /**
         * A map of {@link BeanEntity} for given {@link Class} keys.
         * This map is used as a cache for the {@link #getEntity} method.
         */
        private static final HashMap entityMap = new HashMap();

        /**
         * An entity with empty {@link #propertyMap} and {@link #elements}.
         * Will be created by {@link #getEntity} only when first needed.
         */
        private static BeanEntity EMPTY;

        /**
         * An unmodifiable map of {@link PropertyDescriptor}s by name.
         */
        final Map propertyMap;

        /**
         * An unmodifiable list of {@link org.opengis.catalog.MetadataEntity.Element}
         * to be returned by {@link #elements}.
         */
        private final List elements;

        /**
         * Construct an entity for the specified type. The element are found using reflection.
         * The <code>type</code> argument should be one of GeoAPI metadata interfaces or an
         * implementation of those interfaces.
         *
         * @param  type The type to introspect.
         * @throws IntrospectionException if the introspection failed.
         */
        private BeanEntity(final Class type) throws IntrospectionException {
            final Map propertyMap = new TreeMap();
            introspect(type, propertyMap);
            final Element[] elements = new Element[propertyMap.size()];
            int index = 0;
            for (final Iterator i=propertyMap.values().iterator(); i.hasNext();) {
                PropertyDescriptor property = (PropertyDescriptor) i.next();
                elements[index++] = new BeanElement(property);
            }
            assert index == elements.length : index;
            if (index != 0) {
                this.propertyMap = Collections.unmodifiableMap(propertyMap);
                this.elements    = Collections.unmodifiableList(Arrays.asList(elements));
            } else {
                this.propertyMap = Collections.EMPTY_MAP;
                this.elements    = Collections.EMPTY_LIST;
            }
        }

        /**
         * Gets the {@link PropertyDescriptor}s for all attributes found in a metadata interface.
         * The <code>type</code> argument should be one of GeoAPI metadata interfaces or an
         * implementation of those interfaces.
         *
         * @param  type The type to introspect.
         * @param  propertyMap The map where to add attributes. Key will be attribute names
         *         as {@link String}, and values will be {@link PropertyDescriptor}s.
         * @throws IntrospectionException if the introspection failed.
         */
        private static void introspect(Class type, final Map propertyMap)
                throws IntrospectionException
        {
            while (type != null) {
                final Class[] interfaces = type.getInterfaces();
                if (interfaces != null) {
                    for (int i=0; i<interfaces.length; i++) {
                        final Class candidate = interfaces[i];
                        if (candidate.getName().startsWith("org.opengis.metadata.")) {
                            final PropertyDescriptor[] descriptors =
                                    Introspector.getBeanInfo(candidate).getPropertyDescriptors();
                            if (descriptors != null) {
                                for (int j=0; j<descriptors.length; j++) {
                                    final PropertyDescriptor descriptor = descriptors[j];
                                    if (descriptor.getReadMethod() != null) {
                                        propertyMap.put(descriptor.getName(), descriptor);
                                    }
                                }
                            }
                        } else {
                            introspect(candidate, propertyMap);
                        }
                    }
                }
                type = type.getSuperclass();
            }
        }

        /**
         * Gets or creates the {@link org.opengis.catalog.MetadataEntity.EntityType} instance
         * that describes all attributes found in a metadata interface. The <code>type</code>
         * argument should be one of GeoAPI metadata interfaces or an implementation of those
         * interfaces.
         *
         * @param type The class of a metadata to be inspected
         * @return An {@link org.opengis.catalog.MetadataEntity.EntityType} that descibes the
         *         class passed in.
         * @throws IntrospectionException if the introspection failed.
         */
        public static synchronized BeanEntity getEntity(final Class type)
                throws IntrospectionException
        {
            BeanEntity entity = (BeanEntity) entityMap.get(type);
            if (entity == null) {
                entity = new BeanEntity(type);
                if (entity.propertyMap.isEmpty()) {
                    if (EMPTY == null) {
                        EMPTY = entity;
                    } else {
                        entity = EMPTY;
                    }
                }
                entityMap.put(type, entity);
            }
            return entity;
        }   

        /**
         * The xPath is used to identify {@linkplain org.opengis.catalog.MetadataEntity.Element
         * elements} in the Metadata data hierarchy. If the xPath has wild cards a list of
         * Metadata Elements will be returned.
         * 
         * @param xpath an XPath statement that indicates 0 or more Elements.
         * @return Null if no elements are found to match the xpath 
         * 		A Element if exactly one is found to match the xpath
         * 		A List is many Elements are found to match the xpath.
         */
        public Object getElement(final String xpath) {
            final List result = XPathFactory.find(xpath, this);
            switch (result.size()) {
                case 0:  return null;
                case 1:  return result.get(0);
                default: return result;
            }
        }

        /**
         * Get a List of all the {@linkplain org.opengis.catalog.MetadataEntity.Element elements}
         * this <code>EntityType</code> contains. Only the elements contained by the current
         * <code>EntityType</code> are returned, in other words this method is not recursive,
         * elements in sub-enties are not returned.
         *
         * @return a List of all the {@linkplain org.opengis.catalog.MetadataEntity.Element
         *         elements} this <code>EntityType</code> contains.
         */
        public List getElements() {
            return elements;
        }
    }

    /**
     * A basic implementation of the {@link org.opengis.catalog.MetadataEntity.Element}
     * interface which uses uses reflection to examine the structure of a metadata.
     * 
     * @author Jody Garnett
     * @author Martin Desruisseaux
     *
     * @deprecated This class was implemented for catalog support, but the catalog API 1.0 has
     *             been withdrawn from GeoAPI. The catalog API need to be revisited in light of
     *             latest specification (2.0) before to bring it back to GeoAPI. I suspect that
     *             this inner class will be removed from {@code MetadataEntity}, but actually
     *             would moves in an other class (some kind of wrapper around arbitrary metadata
     *             interface, not just Geotools implementations).
     */
    private static final class BeanElement implements Element {
        /**
         * The Java beans property descriptor.
         * This field is read by {@link MetadataEntry#getElement(Element)} only.
         */
        final PropertyDescriptor property;

        /**
         * The entity type.
         */
        private final BeanEntity entity;

        /**
         * Construct a default element from the specified property descriptor.
         */
        public BeanElement(final PropertyDescriptor property) throws IntrospectionException {
            this.property = property;
            BeanEntity entity = BeanEntity.getEntity(property.getPropertyType());
            if (entity.propertyMap.isEmpty()) {
                // The entity doesn't contains any GeoAPI attributes.
                entity = null;
            }
            this.entity = entity;
        }

        /**
         * Gets the Type (Java Class) of this element.
         */
        public Class getType() {
            return property.getPropertyType();
        }

        /**
         * Gets the name of this element.
         */
        public String getName() {
            return property.getName();
        }

        /**
         * Returns whether nulls are allowed for this element.
         *
         * @todo Using J2SE 1.5, it would be possible to fetch this informations from annotations.
         */
        public boolean isNillable() {
            return false;
        }

        /**
         * Whether or not this element is complex in any way.  If it is
         * not nested then the code can just do the default processing, such
         * as printing the element directly, for example.  If it is nested then
         * that indicates there is more to be done, and the actual ElementType
         * should be determined and processed accordingly.
         */
        public boolean isMetadataEntity() {
            return entity != null;
        }

        /**
         * If the current element is an entity then the entity object that describes the 
         * current element is returned.
         *
         * @return Null, if not a metadata entity (isMetadataEntity returns false)
         * 		The EntityType object describing the current element if current element is
         * 		an entity 
         */
        public EntityType getEntityType() {
            return entity;
        }

        /**
         * Returns a string representation of this element.
         */
        public String toString(){
            return getName();
        }
    }

    /**
     * Returns <code>true</code> if this metadata entity is modifiable.
     * This method returns <code>false</code> if {@link #unmodifiable()}
     * has been invoked on this object.
     */
    public boolean isModifiable() {
        return unmodifiable != this;
    }

    /**
     * Returns an unmodifiable copy of this metadata. Any attempt to modify an attribute of the
     * returned object will throw an {@link UnsupportedOperationException}. If this metadata is
     * already unmodifiable, then this method returns <code>this</code>.
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
     *   <li>If the specified object is an instance of <code>MetadataEntity</code>, then
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
     * Check if changes in the metadata are allowed. All <code>setFoo(...)</code> methods in
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
