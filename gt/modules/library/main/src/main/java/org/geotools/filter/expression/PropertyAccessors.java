package org.geotools.filter.expression;

import java.util.Iterator;

import org.geotools.factory.FactoryRegistry;
import org.geotools.factory.Hints;

/**
 * Convenience class for looking up a property accessor for a particular object type.
 * 
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public class PropertyAccessors {


	/**
	 * Looks up a {@link PropertyAccessor} for a particular object.
	 * <p>
	 * This method will return the first accessor that is capabile of handling the object
	 * and xpath expression provided, no order is guaranteed.
	 * </p>
	 * @param object The target object.
	 * @param xpath An xpath expression denoting a property of the target object.
	 * @param hints Hints to pass on to factories.
	 * 
	 * @return A property accessor, or <code>null</code> if one could not be found.
	 */
	public PropertyAccessor findPropertyAccessor( Object object, String xpath, Class target, Hints hints ) {
		if ( object == null ) 
			return null;
		
		Iterator factories = FactoryRegistry.lookupProviders( PropertyAccessorFactory.class );
		while( factories.hasNext() ) {
			PropertyAccessorFactory factory = (PropertyAccessorFactory) factories.next();
			PropertyAccessor accessor = factory.createPropertyAccessor( object.getClass(), xpath, target, hints ); 
			if ( accessor != null ) { //&& accessor.canHandle( object, xpath )
				return accessor;
			}
		}		
		return null;
	}
}
