package org.geotools.filter.expression;

import org.geotools.factory.Hints;
import org.opengis.feature.Property;

/**
 * This class will *directly* access a Property with the name equal to xpath.
 * 
 * @author Jody Garnett
 */
public class DirectPropertyAccessorFactory implements PropertyAccessorFactory {

    static PropertyAccessor DIRECT = new DirectPropertyAccessor();

    public PropertyAccessor createPropertyAccessor(Class type, String xpath,
            Class target, Hints hints) {
        
        if( Property.class.isAssignableFrom( type )){
            return DIRECT;
        }
        return null;
    }

    
    /**
     * Grab a value from a Property with matching name.
     * <p>
     * This restriction is used by Types.validate to ensure
     * the provided value is good.
     * 
     * @author Jody Garnett (Refractions Research Inc)
     */
    static class DirectPropertyAccessor implements PropertyAccessor {
        
        /**
         * We can handle *one* case and one case only 
         */
        public boolean canHandle(Object object, String xpath, Class target) {
            if( object instanceof Property ){
                Property property = (Property) object;
                if( property.getName() != null ){
                    return property.getName().getLocalPart().equals( xpath );
                }
                else {
                    // a property with no name? Is this the default geometry?
                    return false;
                }
            }
            return false;
        }
        
        public Object get(Object object, String xpath, Class target)
                throws IllegalArgumentException {
            return ((Property)object).getValue();
        }

        public void set(Object object, String xpath, Object value, Class target)
                throws IllegalArgumentException {
            ((Property)object).setValue( value );            
        }        
    }    
}
