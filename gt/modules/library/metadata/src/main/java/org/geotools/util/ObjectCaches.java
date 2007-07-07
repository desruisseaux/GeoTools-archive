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

import org.geotools.factory.FactoryRegistryException;
import org.geotools.factory.GeoTools;
import org.geotools.factory.Hints;
import org.geotools.metadata.iso.citation.Citations;
import org.geotools.resources.Utilities;
import org.opengis.metadata.citation.Citation;
import org.opengis.util.GenericName;


/**
 * This is facade around several constructs used by GeoTools for internal caching.
 * <p>
 * This class provides the following services:
 * <ul>
 *   <li>Access to an implementation of "weak", "all" and "none" implementations of {@link ObjectCache}.</li>
 *   <li>The ability to turn a "code" into a good "key" for use with an ObjectCache.</li>
 *   <li>A Pair data object (think of C STRUCT) for use as a key when storing a value against two objects.</li>
 * </ul>
 * 
 * @author Jody Garnett
 * @author Cory Horner
 */
public final class ObjectCaches {
    /**
     * Do not allow instantiation of this class.
     */
    private ObjectCaches() {
    }

    /**
     * A pair of Codes for {@link ObjectCache) to work with.
     * Please be advised that this is a data object:
     *
     * <ul>
     *   <li>equals - is dependent on both source and target being equal.</li>
     *   <li>hashcode - is dependent on the hashCode of source and target.</li>
     * </ul>
     *
     * A Pair is considered ordered:
     * 
     * <blockquote><pre>
     * Pair pair1 = new Pair("a","b");
     * Pair pair2 = new Pair("b","a");
     * 
     * System.out.println( pair1.equals( pair2 ) ); // prints false
     * </pre></blockquote>
     * 
     * {@link #createFromCoordinateReferenceSystemCodes}.
     */
    private static final class Pair {
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
    public static ObjectCache create( Hints hints )
            throws FactoryRegistryException {
        if( hints == null ) hints = GeoTools.getDefaultHints();
        String policy = (String) hints.get(Hints.BUFFER_POLICY);
        int limit = Hints.BUFFER_LIMIT.toValue(hints);
        return create( policy, limit );
    }
    /**
     * Utility method used to produce an ObjectCache.
     * 
     * @param policy One of "weak", "all", "none"
     * @param size Used to indicate requested size, exact use depends on policy 
     * @return A new ObjectCache
     * @see Hints.BUFFER_POLICY
     */
    public static ObjectCache create( String policy, int size ){
        if ("weak".equalsIgnoreCase(policy)) {
            return new WeakObjectCache(0);
        } else if ("all".equalsIgnoreCase(policy)) {
            return new DefaultObjectCache(size);
        } else if ("none".equalsIgnoreCase(policy)) {
            return NullObjectCache.INSTANCE;
        } else if ("fixed".equalsIgnoreCase(policy)) {
            return new FixedSizeObjectCache(size);
        } else {
            return new DefaultObjectCache(size);
        }
    }

    /**
     * Produce a good key based on the privided citaiton and code.
     * You can think of the citation as being "here" and the code being the "what".
     * 
     * @param code Code
     * @return A good key for use with ObjectCache
     */
    public static String toKey( Citation citation, String code ){
        code = code.trim();
        final GenericName name = NameFactory.create(code);
        final GenericName scope = name.getScope();
        if (scope == null) {
            return code;
        }
        if (citation != null && Citations.identifierMatches( citation, scope.toString())) {
            return name.asLocalName().toString().trim();
        }
        return code;
    }

    /**
     * Produce a good key based on a pair of codes.
     * 
     * @param code1
     * @param code2
     * @return A object to use as a key
     */
    public static Object toKey( Citation citation, String code1, String code2 ){
        String key1 = toKey( citation, code1 );
        String key2 = toKey( citation, code2 );

        return new Pair( key1, key2 );
    }
}
