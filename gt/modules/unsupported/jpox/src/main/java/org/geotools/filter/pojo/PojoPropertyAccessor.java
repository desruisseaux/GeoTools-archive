package org.geotools.filter.pojo;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.geotools.feature.IllegalAttributeException;
import org.geotools.filter.expression.PropertyAccessor;

public class PojoPropertyAccessor implements PropertyAccessor {

    public boolean canHandle( Object object, String xpath ) {
    	// We can handle everything! ...yeah, right.. ;)
        return true;
    }

    public Object get( Object object, String xpath ) {
    	if ( object == null || xpath == null  ) return null;
    	xpath = xpath.trim();
    	if ( xpath.equals( "" ) ) return null;
    	
    	try {
			BeanInfo info = Introspector.getBeanInfo( object.getClass() );
			
			PropertyDescriptor[] descriptors = info.getPropertyDescriptors();
			for ( int i = 0; i < descriptors.length; i++ ) {
				
				if ( descriptors[i].getName().equalsIgnoreCase( xpath ) ) {
					Method getter = descriptors[i].getReadMethod();
					if ( getter != null ) {
						getter.setAccessible( true );
						Object value = getter.invoke( object, null );
						return value;
					} else {
						Field field = object.getClass().getDeclaredField( xpath );
						if ( field != null ) {
							field.setAccessible( true );
							return field.get( object );
						}
					}
				}
			}			
		} catch ( IntrospectionException e ) {
			e.printStackTrace();
		} catch ( IllegalArgumentException e ) {
			e.printStackTrace();
		} catch ( IllegalAccessException e ) {
			e.printStackTrace();
		} catch ( InvocationTargetException e ) {
			e.printStackTrace();
		} catch ( SecurityException e ) {
			e.printStackTrace();
		} catch ( NoSuchFieldException e ) {
			e.printStackTrace();
		}
        return null;
    }

    public void set( Object object, String xpath, Object value ) throws IllegalAttributeException {
        // TODO Auto-generated method stub

    }

}
