package org.geotools.util;

import java.util.HashSet;

public class SetOf extends HashSet {
    Class type;
    public SetOf( Class type ){
        this.type = type;
    }
    public boolean add( Object o ) {
        if( o != null && !type.isInstance( o ) ){
            throw new IllegalArgumentException( "Set limited to contents of type "+type.getName() );
        }
        return super.add(o);
    }
}
