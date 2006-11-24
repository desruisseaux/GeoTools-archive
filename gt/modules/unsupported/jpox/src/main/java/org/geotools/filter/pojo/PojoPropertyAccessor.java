package org.geotools.filter.pojo;

import java.util.Date;

import org.geotools.feature.IllegalAttributeException;
import org.geotools.filter.expression.PropertyAccessor;

public class PojoPropertyAccessor implements PropertyAccessor {

    public boolean canHandle( Object object, String xpath ) {
        return ( object instanceof Date );
    }

    public Object get( Object object, String xpath ) {
        // TODO Auto-generated method stub
        return null;
    }

    public void set( Object object, String xpath, Object value ) throws IllegalAttributeException {
        // TODO Auto-generated method stub

    }

}
