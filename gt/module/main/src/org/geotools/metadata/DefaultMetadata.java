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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;


/**
 * A superclass for most metadata.
 *
 * A subclass implements *MUST* implement minimum one subinterface of the Metadata interface
 *
 * DefaultMetadata uses reflection to identify all the getXXX() methods.
 * The getXXX() are used to construct all the Metadata.Entity and Metadata.Element objects
 * The return type of each is used to determine whether the element is a simple element or an entity,
 * if the element is not a simple entity a entity for that class will can be created as well.
 * All Metadata.Entities and Elements are created when requested (Lazily)
 *
 * TODO: We should switch this over to use BeanInfo/Introspector reflection to allow for
 * recognition of boolean isFoo as "foo", getBar as "bar" and getURL as "URL".
 * 
 * @author jeichar
 * @since 2.1
 */
public class DefaultMetadata implements Metadata {
    EntityImpl entity;

    /**
     * @see org.geotools.metadata.Metadata#elements()
     */
    public final List elements() {
    	Entity type = getType();
    	
        List elements = new ArrayList( type.getElements().size() );        
        List methods = getType().getGetMethods();

        for (Iterator iter = methods.iterator(); iter.hasNext();) {
            Method method = (Method) iter.next();

            try {
                elements.add(method.invoke(this, null));
            } catch (Exception e) {
                throw new RuntimeException("There must be a bug in the EntityImpl class during the introspection.",
                    e);
            }
        }

        return elements;
    }

    /**
     * @see org.geotools.metadata.Metadata#getElement(java.lang.String)
     */
    public final Object getElement(String xpath) {
        List elements = XPath.getValue(xpath, this);

        if (elements.isEmpty()) {
            return null;
        }

        if (elements.size() == 1) {
            return elements.get(0);
        }

        return elements;
    }

    /**
     * @see org.geotools.metadata.Metadata#getElement(org.geotools.metadata.ElementType)
     */
    public Object getElement(Element element) {
        ElementImpl elemImpl;

        if (element instanceof ElementImpl) {
            elemImpl = (ElementImpl) element;
        } else {
            elemImpl = (ElementImpl) getType().getElement(element.getName());
        }

        return invoke(elemImpl.getGetMethod());
    }

    private Object invoke(Method m) {
        try {
            return m.invoke(this, null);
        } catch (Exception e) {
            throw new RuntimeException("There must be a bug in the EntityImpl class during the introspection.",
                e);
        }
    }

    /**
     * @see org.geotools.metadata.Metadata#getEntity()
     */
    public Entity getEntity() {
        return getType();
    }

    private EntityImpl getType() {
        if (entity == null) {
            entity = EntityImpl.getEntity(getClass());
        }

        return entity;
    }

    /**
     * The EntityImpl class uses reflection to examine the structure of a metadata   
     *  
     * @see org.geotools.metadata.Metadata.Entity
     *
     * @author $author$
     * @version $Revision: 1.9 $
     */
    private static class EntityImpl implements Entity {
        static HashMap entityMap = new HashMap();
        ArrayList elemList = new ArrayList();
        HashMap elemMap = new HashMap();
        List getMethods;

        private EntityImpl(Class clazz) {
            init(clazz);
        }

        /**
         * Gets or creates the Enity instance that descibes the Class passed in as an argument
         *
         * @param clazz The class of a metadata to be inspected
         *
         * @return A Metadata.Entity that descibes the class passed in by the class clazz
         */
        public static EntityImpl getEntity(Class clazz) {
            if (!entityMap.containsKey(clazz)) {
                entityMap.put(clazz, new EntityImpl(clazz));
            }

            return (EntityImpl) entityMap.get(clazz);
        }
        
        private void init(Class clazz) {
            getMethods = new ArrayList();

            ArrayList ifaces = new ArrayList();
            getInterfaces(clazz, ifaces);

            /*
             * Inspects each interface. If the interface is not the
             * MetadataEntity interface then its getXXX() methods are used to
             * identify the MetadataElements of the Metadata
             */
            for (Iterator iter = ifaces.iterator(); iter.hasNext();) {
                Class iface = (Class) iter.next();

                Method[] newmethods = iface.getDeclaredMethods();

                /*
                 * locate and add field that the getXXX() indicates
                 */
                for (int j = 0; j < newmethods.length; j++) {
                    Method method = newmethods[j];

                    if (method.getName().startsWith("get")) {
                        getMethods.add(method);

                        Class elementClass = method.getReturnType();

                        ElementImpl element = new ElementImpl(elementClass,
                                method);
                        elemMap.put(method.getName().substring(3), element);
                        elemList.add(element);
                    }
                } //for
            } //for
        }

        private void getInterfaces(Class class1, List list) {
            Class[] ifaces = class1.getInterfaces();
            Class superclass = class1.getSuperclass();

            if ((superclass != null)
                    && !superclass.getClass().isAssignableFrom(Object.class)) {
                getInterfaces(superclass, list);
            }

            for (int i = 0; i < ifaces.length; i++) {
                Class iface = ifaces[i];
                getInterfaces(iface, list);
            } //for

            if (!class1.isInterface()) {
                return;
            }

            if (class1 == Metadata.class) {
                return;
            }

            if (!Metadata.class.isAssignableFrom(class1)) {
                return;
            }

            if (list.contains(class1)) {
                return;
            }

            list.add(class1);
        }

        List getGetMethods() {
            return getMethods;
        }

        /**
         * @see org.geotools.metadata.Metadata.Entity#getElement(java.lang.String)
         */
        public Object getElement(String xpath) {
            List result = XPath.getElement(xpath, this);

            if (result.isEmpty()) {
                return null;
            }

            if (result.size() == 1) {
                return result.get(0);
            }

            return result;
        }

        /**
         * @see org.geotools.metadata.Metadata.Entity#getElements()
         */
        public List getElements() {
            return elemList;
        }
    }

    /**
     * A basic implementation of the Metadata.Element class
     * @see org.geotools.metadata.Metadata.Element
     * 
     * @author $author$
     * @version $Revision: 1.9 $
     */
    private static class ElementImpl implements Element {
        private Method getMethod;
        private Class type;
        private String name;
        private boolean nillable;
        private Entity entity;

        /**
         * @param elementClass
         */
        public ElementImpl(Class elementClass, Method method) {
            this.getMethod = method;
            type = elementClass;            
            name = method.getName().substring(3);
            name = name.substring(0,1).toLowerCase() + name.substring(1);

            if (Metadata.class.isAssignableFrom(elementClass)) {
                entity = EntityImpl.getEntity(elementClass);
            }
        }

        /**
         * Returns the java.lang.reflect.Method that can access the element data.
         */
        public Method getGetMethod() {
            return getMethod;
        }

        /**
         * @see org.geotools.metadata.Metadata.Element#getType()
         */
        public Class getType() {
            return type;
        }

        /**
         * @see org.geotools.metadata.Metadata.Element#getName()
         */
        public String getName() {
            return name;
        }

        /**
         * @see org.geotools.metadata.Metadata.Element#isNillable()
         */
        public boolean isNillable() {
            return false;
        }

        /**
         * @see org.geotools.metadata.Metadata.Element#isMetadataEntity()
         */
        public boolean isMetadataEntity() {
            return (entity == null) ? false : true;
        }

        /**
         * @see org.geotools.metadata.Metadata.Element#isMetadataEntity()
         */
        public Entity getEntity() {
            return entity;
        }
    }
}
