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
package org.geotools.metadata.iso19115;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import org.geotools.metadata.Metadata;
import org.geotools.metadata.XPath;
import org.opengis.metadata.MetaData;

/**
 * A superclass for implementing ISO19115 MetaData interfaces and allowing
 * Expr based query via geotools Metadata.Entity and Metadata.Element.
 * 
 * A subclass implements *MUST* implement minimum one subinterface of
 * the ISO MetaData interface provided by GeoAPI.
 *
 * ISOMetadata uses BeanInfo style reflection to identify all the attributes
 * implemented by the subclass as part of a GeoAPI MetaData interface.
 * 
 * The BeanInfo attributes are used to construct all the Metadata.Entity
 * and Metadata.Element objects.
 * 
 * The type of each attribue is used to determine whether the element is a
 * simple Metadata.Element or an Metadata.Entity.
 * Attributes that subclass GeoAPI ISO19115 MetaData
 * are turned into Metadata Entities. 
 *  
 * @author jgarnett
 * @since 2.1
 */
public abstract class AbstractMetaData implements Metadata, MetaData {
    ISO19115Entity entity;
    
    /**
     * @see org.geotools.metadata.Metadata#elements()
     */
    public final List elements() {
    	ISO19115Entity entity = getType();
    	List elements = new ArrayList( entity.getElements().size() );            
        
        for (Iterator iter = entity.propertyMap().values().iterator(); iter.hasNext();) {        	
        	PropertyDescriptor descriptor =(PropertyDescriptor) iter.next();
        	Method read = descriptor.getReadMethod();        	
        	Method method = (Method) descriptor.getReadMethod();
            try {
            	Object value = read.invoke( this, null );
            	elements.add( value );                
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
        ISO19115Element elemImpl;

        if (element instanceof ISO19115Element) {
            elemImpl = (ISO19115Element) element;
        } else {
            elemImpl = (ISO19115Element) getType().getElement(element.getName());
        }        
        PropertyDescriptor descriptor = elemImpl.getProperty();
        Method read = descriptor.getReadMethod();
        try {
        	return read.invoke( this, null );        	               
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

    protected ISO19115Entity getType() {
        if (entity == null) {
            entity = ISO19115Entity.getEntity(getClass());
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
    private static class ISO19115Entity implements Metadata.Entity {
        static HashMap entityMap = new HashMap();
                
        /** Map of PropertyDescriptor by name */
        Map propertyMap;

		private ArrayList elements;

        private ISO19115Entity(Class clazz) {
            init(clazz);
        }

        /**
         * Gets or creates the Enity instance that descibes the Class passed in as an argument
         *
         * @param clazz The class of a metadata to be inspected
         *
         * @return A Metadata.Entity that descibes the class passed in by the class clazz
         */
        public static ISO19115Entity getEntity(Class clazz) {
            if (!entityMap.containsKey(clazz)) {
                entityMap.put(clazz, new ISO19115Entity(clazz));
            }
            return (ISO19115Entity) entityMap.get(clazz);
        }   
        /**
         * Returns Map of attribtues by name for all this class.
         * <p>
         * Should only be called against interfaces that extend
         * the GeoAPI ISO MetaData interface.
         * </p>
         * @param type
         * @return
         */
        private final Map introspectISO19115( Class metaDataType ){
        	BeanInfo beanInfo;
			try {
				beanInfo = Introspector.getBeanInfo( metaDataType );
				return toMap( beanInfo.getPropertyDescriptors() );
			} catch (IntrospectionException e) {
				throw new RuntimeException(
					"Could not inspect "+ metaDataType.getName() +
					" for "+this.getClass().getName()+":"+e.getMessage(),
		            e);
			}
        }
        /**
         * Convert to Map of PropertyDescriptor by name.
         * <p>
         * Not only properties that can be read will be added.
         */
        private final Map toMap( PropertyDescriptor descriptors[] ){
        	if( descriptors == null ){
        		return Collections.EMPTY_MAP;
        	}
        	Map map = new HashMap();
        	for( int i=0; i<descriptors.length; i++){
        		PropertyDescriptor descriptor = descriptors[i];
        		if( descriptor.getReadMethod() != null ){        			
        			map.put( descriptor.getName(), descriptor );
        		}
        	}
        	return map;
        }
        private final boolean isMetaDataInterface( Class type ){
        	if( type == null ){
        		return false;
        	}
        	if ( org.opengis.metadata.MetaData.class == type ){
				return true;
			}
			Class interfaces[] = type.getInterfaces();			
			for( int i=0; i<interfaces.length; i++){
				if( isMetaDataInterface( interfaces[i] )){
					return true;
				}
			}
			return isMetaDataInterface( type.getSuperclass() );			
        }
        private final Map introspect( Class baseType ){
        	Map map = new TreeMap();
        	
        	for( Class type=baseType; type != null; type = type.getSuperclass() ){
        		Class interfaces[] = type.getInterfaces();
        		if( interfaces != null){        			
        			for( int i=0; i<interfaces.length; i++){
        				//System.out.println("\t interface "+interfaces[i] + " "+isMetaDataInterface( interfaces[i] ) );        				
        				if( isMetaDataInterface( interfaces[i] ) ){
        					Map properties = introspectISO19115( interfaces[i] );							
        					map.putAll( properties );
        				}
        			}
        		}
        	}
        	return map;
        }
        private void init(Class type) {
        	propertyMap = introspect( type );
        	
        	elements = new ArrayList( propertyMap.size() );
        	for( Iterator i=propertyMap.values().iterator(); i.hasNext(); ){
        		PropertyDescriptor property = (PropertyDescriptor) i.next();
        		ISO19115Element element = new ISO19115Element( property );
        		elements.add( element );
        	}
        }
        private Map propertyMap(){
        	return propertyMap;
        }
        private PropertyDescriptor property( String name ){
        	return (PropertyDescriptor) propertyMap.get( name );
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
        	return elements;
        }

		/**
		 * @param elementClass
		 * @return Trye if type is a GeoAPI ISO Metadata class
		 */
		public static boolean isEntity(Class type) {
			return MetaData.class.isAssignableFrom(type);
		}
    }

    /**
     * A basic implementation of the Metadata.Element class
     * @see org.geotools.metadata.Metadata.Element
     * 
     * @author $author$
     * @version $Revision: 1.9 $
     */
    private static class ISO19115Element implements Metadata.Element {
        private PropertyDescriptor property;        
        private Entity entity;

        /**
         * @param elementClass
         */
        public ISO19115Element( PropertyDescriptor property) {
            this.property = property;
            Class type = property.getPropertyType();
            if( ISO19115Entity.isEntity( type )) {
                entity = ISO19115Entity.getEntity( type );
            }
            else {
            	entity = null;
            }
        }

        /**
         * Returns the java.lang.reflect.Method that can access the element data.
         */
        public PropertyDescriptor getProperty() {
            return property;
        }

        /**
         * @see org.geotools.metadata.Metadata.Element#getType()
         */
        public Class getType() {
            return property.getPropertyType();
        }

        /**
         * @see org.geotools.metadata.Metadata.Element#getName()
         */
        public String getName() {
            return property.getName();
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
            return entity != null;
        }

        /**
         * @see org.geotools.metadata.Metadata.Element#isMetadataEntity()
         */
        public Entity getEntity() {
            return entity;
        }
        public String toString(){
        	return property.getName();
        }
    }

	/* (non-Javadoc)
	 * @see org.opengis.metadata.MetaData#getFileIdentifier()
	 */
	public String getFileIdentifier() {	
		String className = getClass().getName();
		String name = className.substring( className.lastIndexOf(".") );
		return  name+".java";
	}

	/* (non-Javadoc)
	 * @see org.opengis.metadata.MetaData#getLanguage()
	 */
	public Locale getLanguage() {
		return Locale.getDefault();
	}

	public String getMetadataStandardName() {
		return "ISO19115";
	}
	public String getMetadataStandardVersion() {
		return "5.0";
	}
}