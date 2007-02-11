package org.geotools.filter.expression;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.geotools.factory.FactoryRegistry;
import org.geotools.factory.Hints;

/**
 * Convenience class for looking up a property accessor for a particular object type.
 * 
 * @author Justin Deoliveira, The Open Planning Project
 * 
 */
public class PropertyAccessors {
    static final List FACTORY_CACHE;
    
    static {
        FACTORY_CACHE = new ArrayList();
        Iterator factories = FactoryRegistry
                .lookupProviders(PropertyAccessorFactory.class);
        while (factories.hasNext()) {
            FACTORY_CACHE.add(factories.next());
        }
    }
    
    /**
     * Make sure this class won't be instantianted
     */
    private PropertyAccessors() {}

    /**
     * Looks up a {@link PropertyAccessor} for a particular object.
     * <p>
     * This method will return the first accessor that is capabile of handling the object and xpath
     * expression provided, no order is guaranteed.
     * </p>
     * 
     * @param object
     *            The target object.
     * @param xpath
     *            An xpath expression denoting a property of the target object.
     * @param hints
     *            Hints to pass on to factories.
     * 
     * @return A property accessor, or <code>null</code> if one could not be found.
     */
    public static PropertyAccessor findPropertyAccessor(Object object, String xpath, Class target,
            Hints hints) {
        if (object == null)
            return null;

        for (Iterator it = FACTORY_CACHE.iterator(); it.hasNext();) {
            PropertyAccessorFactory factory = (PropertyAccessorFactory) it.next();
            PropertyAccessor accessor = factory.createPropertyAccessor(object.getClass(), xpath,
                    target, hints);
            if (accessor != null && accessor.canHandle(object, xpath, target)) {
                return accessor;
            }
        }
        return null;
    }
}
