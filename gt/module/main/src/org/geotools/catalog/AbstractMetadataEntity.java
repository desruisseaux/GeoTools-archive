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
package org.geotools.catalog;

import java.beans.Introspector;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.geotools.xml.XPathFactory;
import org.opengis.catalog.MetadataEntity;

/**
 * MetadataEntity superclass that uses reflection for EntityType information.
 * <p>
 * AbstractMetadata uses reflection to identify all the getXXX() methods. The
 * getXXX() are used to construct all the Metadata.Entity and Metadata.Element
 * objects The return type of each is used to determine whether the element is a
 * simple element or an entity, if the element is not a simple entity a entity
 * for that class will can be created as well. All Metadata.Entities and
 * Elements are created when requested (Lazily)
 * </p>
 * <p>
 * We should switch this over to use BeanInfo/Introspector reflection to allow
 * for recognition of boolean isFoo as "foo", getBar as "bar" and getURL as
 * "URL".
 * </p>
 * 
 * @author jeichar
 * @since 2.1
 */
public abstract class AbstractMetadataEntity implements MetadataEntity {
    static EntityImpl entity = null;

    /**
     * @see org.geotools.metadata.Metadata#elements()
     */
    public final List elements() {
        EntityImpl type = (EntityImpl) getEntityType();

        List elements = new ArrayList(type.getElements().size());
        List methods = type.getGetMethods();

        for (Iterator iter = methods.iterator(); iter.hasNext();) {
            Method method = (Method) iter.next();

            try {
                elements.add(method.invoke(this, null));
            } catch (Exception e) {
                throw new RuntimeException(
                        "There must be a bug in the EntityImpl class during the introspection.",
                        e);
            }
        }

        return elements;
    }

    /**
     * @see org.geotools.metadata.Metadata#getElement(java.lang.String)
     */
    public final Object getElement(String xpath) {
        for( Iterator i=getEntityType().getElements().iterator(); i.hasNext();){
            Element element = (Element) i.next();
            if( xpath.equals( element.getName() )){
                return getElement( element );
            }
        }
        // Did not find a direct match 
        //
        List elements = XPathFactory.value(xpath, this);

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
            elemImpl = (ElementImpl) getEntityType().getElement(element.getName());
        }

        return invoke(elemImpl.getGetMethod());
    }

    private Object invoke(Method m) {
        try {
            return m.invoke(this, null);
        } catch (Exception e) {
            System.out.println( this.getClass().getName() + " could not invoke "+ m.getDeclaringClass().getName()+" "+m.getName());
            throw new RuntimeException(
                    "Cannot access metadata for "+m.getName()+": "+e,
                    e);
        }
    }

    /**
     * Builds EntityType based on reflection.
     * <p>
     * The EntityType will not be built until the first time it is needed.
     * </p>
     * 
     * @see org.geotools.metadata.Metadata#getEntity()
     */
    public EntityType getEntityType() {
        if (entity == null) {
            entity = EntityImpl.getEntity(getClass());
        }
        return entity;
    }

    /**
     * The EntityImpl class uses reflection to examine the structure of a
     * metadata
     * 
     * @see org.geotools.metadata.Metadata.Entity
     * 
     * @author $author$
     * @version $Revision: 1.9 $
     */
    private static class EntityImpl implements EntityType {
        static HashMap entityMap = new HashMap();

        ArrayList elemList = new ArrayList();

        HashMap elemMap = new HashMap();

        List getMethods;

        private EntityImpl(Class clazz) {
            init(clazz);
        }

        /**
         * Gets or creates the Enity instance that descibes the Class passed in
         * as an argument
         * 
         * @param clazz
         *            The class of a metadata to be inspected
         * 
         * @return A Metadata.Entity that descibes the class passed in by the
         *         class clazz
         */
        public static EntityImpl getEntity(Class clazz) {
            if (!entityMap.containsKey(clazz)) {
                entityMap.put(clazz, new EntityImpl(clazz));
            }
            return (EntityImpl) entityMap.get(clazz);
        }

        private void init(Class clazz) {
            getMethods = new ArrayList();

            List methodlist = getMethods(clazz);
            Method[] methods=new Method[methodlist.size()];
            methodlist.toArray(methods);
            
            /*
             * locate and add field that match getXXX(), or boolean isXXX()
             * indicates
             */
            for (int i = 0; i < methods.length; i++) {
                Method method = methods[i];

                if (method.getName().startsWith("get")) {
                    getMethods.add(method);

                    Class elementClass = method.getReturnType();

                    ElementImpl element = new ElementImpl(elementClass, method);
                    elemMap.put(element.getName(), element);
                    elemList.add(element);
                }
                if (method.getName().startsWith("is")
                        && method.getReturnType().equals(Boolean.TYPE)) {

                    getMethods.add(method);
                    ElementImpl element = new ElementImpl(Boolean.TYPE, method);
                    elemMap.put(element.getName(), element);
                    elemList.add(element);
                }
            } //for
        }

        private List getMethods(Class clazz){
            List result=null;
            Class sclass = clazz.getSuperclass();
            if( sclass!=null && !sclass.isAssignableFrom(AbstractMetadataEntity.class))
                result=getMethods(sclass);
            
            if(result==null)
                result=new ArrayList();
            result.addAll(Arrays.asList(clazz.getDeclaredMethods()));
            return result;
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

            if (class1 == MetadataEntity.class) {
                return;
            }

            if (!MetadataEntity.class.isAssignableFrom(class1)) {
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

        public Object getElement(String xpath) {
            // TODO: Jesse you don't have a factory for MetadataEntity.EntityTyp
            //
            List result = XPathFactory.find(xpath, this);
            if( result == null ){
                for( Iterator i=getElements().iterator(); i.hasNext(); ){
                    Element element = (Element) i.next();
                    if( xpath.equals( element.getName() )){
                        return element;
                    }
                }
                return null;
            }
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
     * 
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

        private EntityType entity;

        /**
         * @param elementClass
         */
        public ElementImpl(Class elementClass, Method method) {
            this.getMethod = method;
            type = elementClass;
            name = method.getName();
            if (name.startsWith("get")) {
                name = name.substring(3);
            } else if (name.startsWith("is")) {
                name = name.substring(2);
            }
            name = Introspector.decapitalize(name);

            if (MetadataEntity.class.isAssignableFrom(elementClass)) {
                entity = EntityImpl.getEntity(elementClass);
            }
        }

        /**
         * Returns the java.lang.reflect.Method that can access the element
         * data.
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
        public EntityType getEntityType() {
            return entity;
        }
    }
}