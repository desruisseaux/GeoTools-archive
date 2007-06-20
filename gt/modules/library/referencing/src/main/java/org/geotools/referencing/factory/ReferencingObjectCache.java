package org.geotools.referencing.factory;

import java.lang.ref.Reference;
import java.util.Map;

public interface ReferencingObjectCache {

    public abstract Map findPool();

    public abstract void dispose();

    /**
     * Returns an object from the pool for the specified code. If the object was retained as a
     * {@linkplain Reference weak reference}, the {@link Reference#get referent} is returned.
     * @param key TODO
     *
     * @todo Consider logging a message here to the finer or finest level.
     */
    public abstract Object get(final Object key);

    /**
     * Put an element in the pool. This method is invoked everytime a {@code createFoo(...)}
     * method is invoked, even if an object was already in the pool for the given code, for
     * the following reasons: 1) Replaces weak reference by strong reference (if applicable)
     * and 2) Alters the linked hash set order, so that this object is declared as the last
     * one used.
     * @param key TODO
     * @param object TODO
     */
    public abstract void put(final Object key, final Object object);

}