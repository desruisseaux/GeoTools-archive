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
 *
 */
package org.geotools.metadata;

// J2SE dependencies
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.Iterator;
import java.util.Collections;
import java.util.Arrays;
import java.util.Locale;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;

// Geotools dependencies
import org.geotools.catalog.XPath;
import org.geotools.resources.Utilities;
import org.geotools.resources.rsc.Resources;
import org.geotools.resources.rsc.ResourceKeys;


/**
 * A superclass for implementing ISO 19115 MetaData interfaces and allowing
 * Expr based query via
 * {@link org.opengis.catalog.MetadataEntity.EntityType} and
 * {@link org.opengis.catalog.MetadataEntity.Element}.
 * 
 * A subclass implements *MUST* implement minimum one of the ISO MetaData interface
 * provided by GeoAPI.
 *
 * <code>MetadataEntity</code> uses BeanInfo style reflection to identify all the attributes
 * implemented by the subclass as part of a GeoAPI MetaData interface.
 * 
 * The BeanInfo attributes are used to construct all the
 * {@link org.opengis.catalog.MetadataEntity.EntityType} and
 * {@link org.opengis.catalog.MetadataEntity.Element} objects.
 * 
 * The type of each attribue is used to determine whether the element is a simple
 * {@link org.opengis.catalog.MetadataEntity.Element} or
 * {@link org.opengis.catalog.MetadataEntity.EntityType}.
 * Attributes that subclass GeoAPI ISO 19115 MetaData
 * are turned into Metadata Entities. 
 *
 * @todo The last sentence in this javadoc is not exactly true.
 *
 * @author Jody Garnett
 * @author Martin Desruisseaux
 * @since 2.1
 */
public class MetadataEntity implements org.opengis.catalog.MetadataEntity {
    /**
     * The entity type for this metadata. Will be constructed only when first needed.
     */
    private transient BeanEntity entity;

    /**
     * Construct a default metadata entity.
     */
    protected MetadataEntity() {
    }

    /**
     * Access to the values associated with metadata {@linkplain Element elements}.
     * The list is returned in the same order as described by the {@link EntityType}
     * schema information.
     *
     * @return List of {@linkplain Element elements}.
     */
    public final List elements() {
        final BeanEntity entity = (BeanEntity) getEntityType();
        final List elements = new ArrayList(entity.getElements().size());
        for (final Iterator iter=entity.propertyMap.values().iterator(); iter.hasNext();) {
            final Method method = ((PropertyDescriptor) iter.next()).getReadMethod();
            final Object value;
            try {
                value = method.invoke(this, null);
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
     */
    public final Object getElement(final String xpath) {
        final List elements = XPath.getValue(xpath, this);
        switch (elements.size()) {
            case 0:  return null;
            case 1:  return elements.get(0);
            default: return elements;
        }
    }

    /**
     * Gets the value of the provided Element.
     * 
     * @param  element Element that indicates the value the caller wishes to obtain.
     * @return The value of the element the parameter represents
     *         null if the current Metadata does not contain the Element.
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
            value = method.invoke(this, null);
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
     * A basic implementation of the {@link EntityType} interface which uses
     * uses reflection to examine the structure of a metadata. Instances of
     * this class can be created by the {@link #getEntity} method only.
     * 
     * @author Jody Garnett
     * @author Martin Desruisseaux
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
         * An unmodifiable list of {@link Element} to be returned by {@link #elements}.
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
         * Gets or creates the {@link EntityType} instance that describes all attributes found in
         * a metadata interface. The <code>type</code> argument should be one of GeoAPI metadata
         * interfaces or an implementation of those interfaces.
         *
         * @param type The class of a metadata to be inspected
         * @return An {@link EntityType} that descibes the class passed in.
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
         * The xPath is used to identify {@linkplain Element elements} in the Metadata data
         * hierarchy. If the xPath has wild cards a list of Metadata Elements will be returned.
         * 
         * @param xpath an XPath statement that indicates 0 or more Elements.
         * @return Null if no elements are found to match the xpath 
         * 		A Element if exactly one is found to match the xpath
         * 		A List is many Elements are found to match the xpath.
         */
        public Object getElement(final String xpath) {
            final List result = XPath.getElement(xpath, this);
            switch (result.size()) {
                case 0:  return null;
                case 1:  return result.get(0);
                default: return result;
            }
        }

        /**
         * Get a List of all the {@linkplain Element elements} this <code>EntityType</code> contains.
         * Only the elements contained by the current <code>EntityType</code> are returned,
         * in other words this method is not recursive, elements in sub-enties are not returned.
         *
         * @return a List of all the {@linkplain Element elements} this <code>EntityType</code> contains.
         */
        public List getElements() {
            return elements;
        }
    }

    /**
     * A basic implementation of the {@link Element} interface which uses
     * uses reflection to examine the structure of a metadata.
     * 
     * @author Jody Garnett
     * @author Martin Desruisseaux
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
}
