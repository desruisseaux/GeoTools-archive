/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2007, GeoTools Project Managment Committee (PMC)
 *   
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.util;

import java.lang.ref.Reference;

/**
 * A cache used by the referencing subsystem.
 * <p>
 * To use as a reader:
 * 
 * <pre><code>
 * CoordinateReferenceSystem crs = cache.get(key);
 * </code></pre>
 * 
 * To overwrite:
 * 
 * <pre><code>
 * cache.put(&quot;EPSG:4326&quot;, crs);
 * </code></pre>
 * 
 * To reserve the entry while figuring out what to write:
 * 
 * <pre><code>
 *  try {
 *      cache.writeLock( key ); // may block if another writer is working on this code
 *      value = cache.test( key );
 *      if( value == null ){
 *         // another writer got here first
 *      }
 *      else { 
 *         value = figuringOutWhatToWrite(....);
 *         cache.put( key, value );
 *      }
 *  }
 *  finally {
 *      cache.writeUnLock(&quot;EPSG:4326&quot;);
 *  }
 * </code></pre>
 * 
 * To use as a proper cache:
 * 
 * <pre><code>
 * CylindricalCS cs = (CylindricalCS) cache.get(key);
 * if (cs == null) {
 *     try {
 *         cache.writeLock(key);
 *         cs = (CylindricalCS) cache.test(key);
 *         if (cs == null) {
 *             cs = csAuthority.createCylindricalCS(code);
 *             cache.put(key, cs);
 *         }
 *     } finally {
 *         cache.writeUnLock(key);
 *     }
 * }
 * return cs;
 * </code></pre>
 * 
 * @since 2.4
 * @version $Id$
 * @source $URL:
 *         http://svn.geotools.org/geotools/trunk/gt/modules/library/referencing/src/main/java/org/geotools/referencing/factory/ReferencingObjectCache.java $
 * @todo Consider renaming as {@code ObjectCache} or {@code Cache} and move to the
 *       {@code org.geotools.util} package.
 */
public interface ObjectCache {

    /**
     * Removes all entries from this cache.
     */
    void clear();

    /**
     * Returns an object from the pool for the specified code. If the object was retained as a
     * {@linkplain Reference weak reference}, the {@link Reference#get referent} is returned.
     * 
     * @param key The authority code.
     */
    Object get( Object key );

    /**
     * Use the write lock to test the value for the provided key.
     * <p>
     * This method is used by a writer to test if someone (ie another writer) has provided the value
     * for us (while we were blocked waiting for them).
     * </p>
     * 
     * @param key
     * @return The value, may be <code>null</code>
     */
    Object test( Object key );

    /**
     * Puts an element into the cache.
     * <p>
     * You may simply use this method - it is threadsafe:
     * 
     * <pre></code>
     * cache.put(&quot;4326&quot;, crs);
     * </code></pre>
     * 
     * You may also consider reserving the entry while you work on the answer:
     * 
     * <pre></code>
     *  try {
     *     cache.writeLock( &quot;fred&quot; );
     *     ...find fred
     *     cache.put( &quot;fred&quot;, fred );
     *  }
     *  finally {
     *     cache.writeUnLock();
     *  }
     * </code></pre>
     * 
     * @param key the authority code.
     * @param object The referencing object to add in the pool.
     */
    void put( Object key, Object object );

    /**
     * Aquire a write lock on the indicated key.
     * 
     * @param key
     */
    void writeLock( Object key ); // TODO: how to indicate lock was not aquired?

    /**
     * Release write lock on the indicated key.
     * 
     * @param key
     */
    void writeUnLock( Object key );
}