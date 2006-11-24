package org.geotools.filter.pojo;

import org.geotools.factory.Hints;
import org.geotools.feature.Feature;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.filter.expression.PropertyAccessor;
import org.geotools.filter.expression.PropertyAccessorFactory;

/**
 * Creates a property accessor for plain old java objects features.
 * <p>
 * The created accessor handles a small subset of xpath expressions:
 * <ul>
 * <li>"name" which corresponds to a bean property
 * </ul>
 */
public class PojoPropertyAccessorFactory implements
		PropertyAccessorFactory {

    public PropertyAccessor createPropertyAccessor( Class type, String xpath, Hints hints ) {
        return null;
    }

	
}
