package org.geotools.util;

import java.util.ArrayList;

public class ListOf extends ArrayList {
    Class type;
    public ListOf( Class type ){
        this.type = type;
    }
    public boolean add( Object o ) {
        if( o != null && !type.isInstance( o ) ){
            throw new IllegalArgumentException( "Set limited to contents of type "+type.getName() );
        }
        return super.add(o);
    }
}
