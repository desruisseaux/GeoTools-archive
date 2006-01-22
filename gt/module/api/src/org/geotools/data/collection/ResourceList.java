package org.geotools.data.collection;

import java.util.List;

public interface ResourceList extends List, ResourceCollection {    
    void removeRange( int fromIndex, int toIndex );
}
