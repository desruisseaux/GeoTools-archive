package org.geotools.referencing.factory;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Caching implementation for ReferencingObjectCache. This instance is used when
 * actual caching is desired.
 * 
 * @author Cory Horner, Refractions Research
 * 
 */
class DefaultReferencingObjectCache implements ReferencingObjectCache {

    /**
     * The pool of cached objects.
     * <p>
     * The following may be seen for a key (String key?):
     * <ul>
     * <li>Object (ie a strong reference) usually a referencing object like CoordinateReferenceSystem or Datum</li>
     * <li>WeakReference used to hold a referencing object (may be cleaned up at any time</li>
     * </ul>
     */
    private final LinkedHashMap pool = new LinkedHashMap(32, 0.75f, true);

    /**
     * The pool of objects identified by  {@link #find} .
     */
    private final Map findPool = new WeakHashMap();

    private final int maxStrongReferences;
    
    public DefaultReferencingObjectCache(int maxStrongReferences) {
        this.maxStrongReferences = maxStrongReferences;
    }
    
    public Map findPool() {
        return findPool;
    }

    public synchronized void dispose() {
        if (pool != null) {
            pool.clear();
        }
        if (findPool != null) {
            findPool.clear();
        }
        //super.dispose();
    }
    
    public Object get(final Object key) {
        //assert Thread.holdsLock(factory);
        Object object = pool.get(key);
        if (object instanceof Reference) {
            object = ((Reference) object).get();
        }
        return object;
    }

    public void put(final Object key, final Object object) {
        //assert Thread.holdsLock(factory);
        pool.put(key, object);
        int toReplace = pool.size() - maxStrongReferences;
        if (toReplace > 0) {
            for (final Iterator it=pool.entrySet().iterator(); it.hasNext();) {
                final Map.Entry entry = (Map.Entry) it.next();
                final Object value = entry.getValue();
                if (value instanceof Reference) {
                    if (((Reference) value).get() == null) {
                        it.remove();
                    }
                    continue;
                }
                entry.setValue(new WeakReference(value));
                if (--toReplace == 0) {
                    break;
                }
            }
        }
    }

}
