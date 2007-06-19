package org.geotools.referencing.factory;

import java.util.Map;

/**
 * Null implementation for the ReferencingObjectCache. Used for cases where
 * caching is *not* desired.
 * 
 * @author Cory Horner, Refractions Research
 * 
 */
class NullReferencingObjectCache implements ReferencingObjectCache {

    public void dispose() {
    }

    public Map findPool() {
        return null;
    }

    public Object get(Object key) {
        return null;
    }

    public void put(Object key, Object object) {
    }

}
