package org.geotools.util;

import java.util.HashMap;

public class MapOf extends HashMap {
    private Class valueType;
    private Class keyType;
    
    public MapOf( Class keyType, Class valueType ){
        this.keyType = keyType;
        this.valueType = valueType;
    }
    
    public Object put( Object key, Object value ) {
        if( key != null && !keyType.isInstance( key ) ){
            throw new IllegalArgumentException( "Key limited to type "+keyType.getName() );
        }
        if( value != null && !valueType.isInstance( value ) ){
            throw new IllegalArgumentException( "Value limited type "+valueType.getName() );
        }
        return super.put( key, value );
    }
}
