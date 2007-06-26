/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2005-2006, GeoTools Project Managment Committee (PMC)
 *    (C) 2005, Institut de Recherche pour le Développement
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
 *    
 *    This package contains documentation from OpenGIS specifications.
 *    OpenGIS consortium's work is fully acknowledged here.
 */
package org.geotools.referencing.factory;

// J2SE dependencies and extensions
import java.util.Set;

import org.geotools.factory.BufferedFactory;
import org.geotools.factory.FactoryRegistryException;
import org.geotools.factory.Hints;
import org.opengis.metadata.citation.Citation;
import org.opengis.referencing.AuthorityFactory;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.IdentifiedObject;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.cs.CSAuthorityFactory;
import org.opengis.referencing.datum.DatumAuthorityFactory;
import org.opengis.referencing.operation.CoordinateOperationAuthorityFactory;
import org.opengis.util.InternationalString;


/**
 * An authority factory that caches all objects created by an other factories. All
 * {@code createFoo(String)} methods first looks if a previously created object
 * exists for the given code. If such an object exists, it is returned. Otherwise,
 * the object definition is delegated to the {@linkplain an AuthorityFactory authority factory}
 * and the result is cached in this buffered factory. 
 * <p>
 * This object is responsible for owning a {{ReferencingObjectCache}}; there are several
 * implementations to choose from on construction.
 * </p>
 * @since 2.4
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public abstract class AbstractBufferedAuthorityFactory extends ReferencingFactory implements AuthorityFactory, BufferedFactory {
    /**
     * The default value for {@link #maxStrongReferences}.
     */
    static final int DEFAULT_MAX = 20;
    
    /** Cache to be used for referencing objects. */ 
    ReferencingObjectCache cache;
    
    /** The factories used to create objects */
    ReferencingFactoryContainer container;
        
    protected AbstractBufferedAuthorityFactory( int priority ){
        this( priority, new DefaultReferencingObjectCache( DEFAULT_MAX ));
    }
    /**
     * This constructor is protected because subclasses must declare which of the
     * {@link DatumAuthorityFactory}, {@link CSAuthorityFactory}, {@link CRSAuthorityFactory}
     * and {@link CoordinateOperationAuthorityFactory} interfaces they choose to implement.
     *
     * @param factory The factory to cache. Can not be {@code null}.
     */
    protected AbstractBufferedAuthorityFactory(ReferencingObjectCache cache) {
        this( DEFAULT_MAX, cache );        
    }

    protected AbstractBufferedAuthorityFactory(int priority, ReferencingObjectCache cache) {
        super( priority );
        this.cache = cache;
        this.container = ReferencingFactoryContainer.instance( null );
    }
    /**
     * Constructs an instance wrapping the specified factory. The {@code maxStrongReferences}
     * argument specify the maximum number of objects to keep by strong reference. If a greater
     * amount of objects are created, then the strong references for the oldest ones are replaced
     * by weak references.
     * <p>
     * This constructor is protected because subclasses must declare which of the
     * {@link DatumAuthorityFactory}, {@link CSAuthorityFactory}, {@link CRSAuthorityFactory}
     * and {@link CoordinateOperationAuthorityFactory} interfaces they choose to implement.
     *
     * @param factory The factory to cache. Can not be {@code null}.
     * @param maxStrongReferences The maximum number of objects to keep by strong reference.
     */
    protected AbstractBufferedAuthorityFactory( Hints hints ){
        super( DEFAULT_MAX );
        this.container = ReferencingFactoryContainer.instance( hints );
        this.cache = createCache( hints );        
    }

    protected static ReferencingObjectCache createCache(final Hints hints) throws FactoryRegistryException {
        String policy = (String) hints.get( Hints.BUFFER_POLICY );
        int limit = Hints.BUFFER_LIMIT.toValue( hints );
        
        if( "weak".equalsIgnoreCase(policy) ){
            return new DefaultReferencingObjectCache( 0 );
        }
        else if ( "all".equalsIgnoreCase(policy) ){
            return new DefaultReferencingObjectCache( limit );
        }
        else if ( "none".equalsIgnoreCase(policy )){        
            return new NullReferencingObjectCache();
        }
        else {
            return new DefaultReferencingObjectCache( limit );
        }
    }
    
    /**
     * Retrives a backing store, used to populate the cache.
     * 
     * @return AuthorityFactory
     */
    abstract protected AuthorityFactory getBackingStore();
    
    public IdentifiedObject createObject( String code ) throws FactoryException {
        IdentifiedObject obj = (IdentifiedObject) cache.get( code );
        if( obj == null ){
            obj = getBackingStore().createObject( code );
            cache.put( code, obj );
        }
        return obj;
    }

    public Citation getAuthority() {
        return getBackingStore().getAuthority();
    }

    public Set getAuthorityCodes( Class type ) throws FactoryException {
        return getBackingStore().getAuthorityCodes(type);
    }

    public InternationalString getDescriptionText( String code ) throws FactoryException {
        return getBackingStore().getDescriptionText(code);
    }    
}
