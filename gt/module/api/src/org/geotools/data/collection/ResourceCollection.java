package org.geotools.data.collection;

import java.util.Collection;
import java.util.Iterator;

/**
 * Collection supporting close( Iterator ), used to grok resources.
 * <p>
 * This implementation is a port of java.util.Collection with support for
 * the use of close( Iterator ). This will allow subclasses that make use of
 * resources during iterator() to be uses safely.
 * </p>
 * @author Jody Garnett, Refractions Research, Inc.
 * @since GeoTools 2.2
 */
public interface ResourceCollection extends Collection {

    /**
     * An iterator over this collection, which must be closeed after use.
     * <p>
     * Collection is not guarneteed to be ordered in any manner.
     * </p>
     * <p>
     * The implementation of Collection must adhere to the rules of
     * fail-fast concurrent modification. In addition (to allow for
     * resource backed collections, the <code>close( Iterator )</code>
     * method must be called.
     * <p>
     * </p>
     * Example (safe) use:<pre><code>
     * Iterator iterator = collection.iterator();
     * try {
     *     while( iterator.hasNext();){
     *          Feature feature = (Feature) iterator.hasNext();
     *          System.out.println( feature.getID() );
     *     }
     * }
     * finally {
     *     collection.close( iterator );
     * }
     * </code></pre>
     * </p>
     * @return Iterator
     */
    public Iterator iterator();
    
    /**
     * Clean up after any resources assocaited with this itterator in a manner similar to JDO collections.
     * </p>
     * Example (safe) use:<pre><code>
     * Iterator iterator = collection.iterator();
     * try {
     *     for( Iterator i=collection.iterator(); i.hasNext();){
     *          Feature feature = (Feature) i.hasNext();
     *          System.out.println( feature.getID() );
     *     }
     * }
     * finally {
     *     collection.close( iterator );
     * }
     * </code></pre>
     * </p>
     * @param close
     */
    public void close( Iterator close );    
    
    /**
     * Close any outstanding resources released by this resources.
     * <p>
     * This method should be used with great caution, it is however available
     * to allow the use of the ResourceCollection with algorthims that are
     * unaware of the need to close iterators after use.
     * </p>
     * <p>
     * Example of using a normal Collections utility method:<pre><code>
     * Collections.sort( collection );
     * collection.purge(); 
     * </code></pre>
     */
    public void purge();
	
}
