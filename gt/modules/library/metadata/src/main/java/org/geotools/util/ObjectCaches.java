package org.geotools.util;

import org.geotools.factory.FactoryRegistryException;
import org.geotools.factory.Hints;
import org.geotools.resources.Utilities;

/**
 * This is facade around several constructs used by GeoTools for internal caching.
 * <p>
 * This class provides the following services:
 * <ul>
 * <li>Access to an implementation of "weak", "all" and "none" implementations of {@link ObjectCache}
 * <li>The ability to turn a "code" into a good "key" for use with an ObjectCache
 * <li>A Pair data object (think of C STRUCT) for use as a key when storing a value against two oobjects.
 * </ul>
 * 
 * @author Jody Garnett
 * @author Cory Horner
 */
public class ObjectCaches {
	
	public static interface ObjectCache {
		
	}
    /**
     * A pair of Codes for {@link ObjectCache) to work with.
     * <p>
     * Please be advised that this is a data object:
     * <ul>
     * <li>equals - is dependent on both source and target being equal
     * <li>hashcode - is dependent on the hashCode of source and target
     * </ul>
     * A Pair is considered ordered:<pre><code>
     * Pair pair1 = new Pair("a","b");
     * Pair pair2 = new Pair("b","a");
     * 
     * System.out.println( pair1.equals( pair2 ) ); // prints false
     * </code></pre>
     * 
     * {@link #createFromCoordinateReferenceSystemCodes}.
     */
    public static final class Pair {
        private final String source, target;

        public Pair(String source, String target) {
            this.source = source;
            this.target = target;
        }

        public int hashCode() {
            int code = 0;
            if (source!=null) code  = source.hashCode();
            if (target!=null) code += target.hashCode()*37;
            return code;
        }

        public boolean equals(final Object other) {
            if (other instanceof Pair) {
                final Pair that = (Pair) other;
                return Utilities.equals(this.source, that.source) &&
                       Utilities.equals(this.target, that.target);
            }
            return false;
        }

        public String toString() {
            return source + " \u21E8 " + target;
        }
    }
    
    /**
     * Utility method used to produce cache based on provide Hint
     */
    public static ObjectCache create( final Hints hints )
            throws FactoryRegistryException {
        String policy = (String) hints.get(Hints.BUFFER_POLICY);
        int limit = Hints.BUFFER_LIMIT.toValue(hints);
        return create( policy, limit );
    }
    /**
     * Utility method used to produce an Object cache.
     * @param policy One of "weak", "all", "none"
     * @param size Used to indicate requested size, exact use depends on policy 
     * @return A new ObjectCache
     * @see Hints.BUFFER_POLICY
     */
    public static ObjectCache create( String policy, int size ){
        if ("weak".equalsIgnoreCase(policy)) {
            //return new DefaultReferencingObjectCache(0);
        } else if ("all".equalsIgnoreCase(policy)) {
            //return new DefaultReferencingObjectCache(limit);
        } else if ("none".equalsIgnoreCase(policy)) {
            //return new NullReferencingObjectCache();
        } else {
            //return new DefaultReferencingObjectCache(limit);
        }
        return null;
    }
    
    
}