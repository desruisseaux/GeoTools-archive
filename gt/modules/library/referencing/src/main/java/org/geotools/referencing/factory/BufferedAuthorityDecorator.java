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
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;
import java.util.Collection;
import java.util.Collections;
import java.util.WeakHashMap;
import java.util.LinkedHashMap;
import java.util.logging.LogRecord;
import java.util.logging.Level;
import javax.units.Unit;

// OpenGIS dependencies
import org.opengis.metadata.extent.Extent;
import org.opengis.metadata.citation.Citation;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.referencing.AuthorityFactory;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.IdentifiedObject;
import org.opengis.referencing.cs.*;
import org.opengis.referencing.crs.*;
import org.opengis.referencing.datum.*;
import org.opengis.referencing.operation.*;
import org.opengis.util.GenericName;
import org.opengis.util.InternationalString;

// Geotools dependencies
import org.geotools.factory.FactoryRegistryException;
import org.geotools.factory.GeoTools;
import org.geotools.factory.Hints;
import org.geotools.factory.BufferedFactory;
import org.geotools.metadata.iso.citation.Citations;
import org.geotools.resources.Utilities;
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.i18n.Logging;
import org.geotools.resources.i18n.LoggingKeys;
import org.geotools.util.NameFactory;


/**
 * An authority factory that caches all objects created by delegate factories. This class is set up
 * to cache the full complement of referencing objects:
 * <ul>
 * <li>AuthorityFactory from getAuthorityFactory()
 * <li>CRSAuthorityFactory from getCRSAuthorityFactory()
 * <li>CSAuthorityFactory from getCSAuthorityFactory()
 * <li>DatumAuthorityFactory from getDatumAuthorityFactory()
 * <li>CoordinateOperationAuthorityFactory from getCoordinateOperationAuthorityFactory()
 * </ul>
 * In many cases a single implementation will support several of the above interfaces.
 * </p>
 * The behaviour of the {@code createFoo(String)} methods first looks if a previously created object
 * exists for the given code. If such an object exists, it is returned directly. The testing of the
 * cache is synchronized and may block if the referencing object is under construction.
 * <p>
 * If the object is not yet created, the definition is delegated to the appropratie the
 * {@linkplain an AuthorityFactory authority factory} and the result is cached for next time.
 * <p>
 * This object is responsible for owning a {{ReferencingObjectCache}}; there are several
 * implementations to choose from on construction.
 * </p>
 * 
 * @since 2.4
 * @source $URL:
 *         http://svn.geotools.org/geotools/trunk/gt/modules/library/referencing/src/main/java/org/geotools/referencing/factory/AbstractBufferedAuthorityFactory.java $
 * @version $Id$
 * @author Jody Garnett
 */
public final class BufferedAuthorityDecorator extends ReferencingFactory
        implements
            AuthorityFactory,
            CRSAuthorityFactory
// CSAuthorityFactory,
// DatumAuthorityFactory,
// CoordinateOperationAuthorityFactory,
// BufferedFactory
            {

    /** Cache to be used for referencing objects. */ 
    ReferencingObjectCache cache;

    /** The delegate authority. */
    private AuthorityFactory authority; 
    
    /** The delegate authority for coordinate reference systems. */
    private CRSAuthorityFactory crsAuthority;

    /** The delegate authority for coordinate sytems. */
    private CSAuthorityFactory csAuthority;

    /** The delegate authority for datums. */
    private DatumAuthorityFactory datumAuthority;
    
    /** The delegate authority for coordinate operations. */
    private CoordinateOperationAuthorityFactory operationAuthority;
    /**
     * Constructs an instance wrapping the specified factory with a default cache.
     * <p>
     * The provided authority factory must implement {@link DatumAuthorityFactory},
     * {@link CSAuthorityFactory}, {@link CRSAuthorityFactory} and
     * {@link CoordinateOperationAuthorityFactory} .
     * 
     * @param factory The factory to cache. Can not be {@code null}.
     */
    public BufferedAuthorityDecorator(final AuthorityFactory factory) {
        this( factory, createCache( GeoTools.getDefaultHints()) );
    }

    /**
     * Constructs an instance wrapping the specified factory. The {@code maxStrongReferences}
     * argument specify the maximum number of objects to keep by strong reference. If a greater
     * amount of objects are created, then the strong references for the oldest ones are replaced by
     * weak references.
     * <p>
     * This constructor is protected because subclasses must declare which of the
     * {@link DatumAuthorityFactory}, {@link CSAuthorityFactory}, {@link CRSAuthorityFactory} and
     * {@link CoordinateOperationAuthorityFactory} interfaces they choose to implement.
     * 
     * @param factory The factory to cache. Can not be {@code null}.
     * @param maxStrongReferences The maximum number of objects to keep by strong reference.
     */
    protected BufferedAuthorityDecorator(AuthorityFactory factory, ReferencingObjectCache cache)
    {
        this.cache = cache;
        authority = factory;
        crsAuthority = (CRSAuthorityFactory) factory;
        csAuthority = (CSAuthorityFactory) factory;
        datumAuthority = (DatumAuthorityFactory) factory;
        operationAuthority = (CoordinateOperationAuthorityFactory) factory;
    }
    
    /** Utility method used to produce cache based on hint */
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
    //
    // Utility Methods and Cache Care and Feeding
    //
    protected String trimAuthority(String code) {
        /*
         * IMPLEMENTATION NOTE: This method is overrided in PropertyAuthorityFactory. If
         * implementation below is modified, it is probably worth to revisit the overrided method as
         * well.
         */
        code = code.trim();
        final GenericName name  = NameFactory.create(code);
        final GenericName scope = name.getScope();
        if (scope == null) {
            return code;
        }
        if (Citations.identifierMatches(getAuthority(), scope.toString())) {
            return name.asLocalName().toString().trim();
        }
        return code;
    }
    //
    // AuthorityFactory
    //    
    public IdentifiedObject createObject( String code ) throws FactoryException {
        IdentifiedObject value = null;      
        final String key = trimAuthority(code);
        if (!cache.containsKey(key)) {
            try {
                cache.writeLock(key);
                if (!cache.containsKey(key)) {
                    value = authority.createObject(code);
                    cache.put(key, value);
                }
            } finally {
                cache.writeUnLock(key);
            }            
        }
        if (value == null) {
            value = (IdentifiedObject) cache.get(key);
        }
        return value;
    }

    public Citation getAuthority() {
        return authority.getAuthority();
    }

    public Set getAuthorityCodes( Class type ) throws FactoryException {
        return authority.getAuthorityCodes( type );
    }

    public InternationalString getDescriptionText( String code ) throws FactoryException {
        return authority.getDescriptionText( code );
    }
    //
    // CRSAuthority
    //
    public synchronized CompoundCRS createCompoundCRS( final String code ) throws FactoryException {
        final String key = trimAuthority(code);
        CompoundCRS crs=null;
        if (!cache.containsKey(key)) {
            try {
                cache.writeLock(key);
                if (!cache.containsKey(key)) {
                    crs = crsAuthority.createCompoundCRS(code);
                    cache.put(key, crs);
                }
            } finally {
                cache.writeUnLock(key);
            }            
        }
        if (crs == null) {
            crs = (CompoundCRS) cache.get(key);
        }
        return crs;
    }
    
    public CoordinateReferenceSystem createCoordinateReferenceSystem( String code ) throws FactoryException {
        final String key = trimAuthority(code);
        CoordinateReferenceSystem crs=null;
        if (!cache.containsKey(key)) {
            try {
                cache.writeLock(key);
                if (!cache.containsKey(key)) {
                    crs = crsAuthority.createCoordinateReferenceSystem(code);
                    cache.put(key, crs);
                }
            } finally {
                cache.writeUnLock(key);
            }            
        }
        if (crs == null) {
            crs = (CoordinateReferenceSystem) cache.get(key);
        }
        return crs;
    }

    public DerivedCRS createDerivedCRS( String code ) throws FactoryException {
        final String key = trimAuthority(code);
        DerivedCRS crs=null;
        if (!cache.containsKey(key)) {
            try {
                cache.writeLock(key);
                if (!cache.containsKey(key)) {
                    crs = crsAuthority.createDerivedCRS(code);
                    cache.put(key, crs);
                }
            } finally {
                cache.writeUnLock(key);
            }            
        }
        if (crs == null) {
            crs = (DerivedCRS) cache.get(key);
        }
        return crs;
    }

    public EngineeringCRS createEngineeringCRS( String code ) throws FactoryException {
        final String key = trimAuthority(code);
        EngineeringCRS crs=null;
        if (!cache.containsKey(key)) {
            try {
                cache.writeLock(key);
                if (!cache.containsKey(key)) {
                    crs = crsAuthority.createEngineeringCRS(code);
                    cache.put(key, crs);
                }
            } finally {
                cache.writeUnLock(key);
            }            
        }
        if (crs == null) {
            crs = (EngineeringCRS) cache.get(key);
        }
        return crs;
    }

    public GeocentricCRS createGeocentricCRS( String code ) throws FactoryException {
        final String key = trimAuthority(code);
        GeocentricCRS crs=null;
        if (!cache.containsKey(key)) {
            try {
                cache.writeLock(key);
                if (!cache.containsKey(key)) {
                    crs = crsAuthority.createGeocentricCRS(code);
                    cache.put(key, crs);
                }
            } finally {
                cache.writeUnLock(key);
            }            
        }
        if (crs == null) {
            crs = (GeocentricCRS) cache.get(key);
        }
        return crs;
    }

    public GeographicCRS createGeographicCRS( String code ) throws FactoryException {
        final String key = trimAuthority(code);
        GeographicCRS crs=null;
        if (!cache.containsKey(key)) {
            try {
                cache.writeLock(key);
                if (!cache.containsKey(key)) {
                    crs = crsAuthority.createGeographicCRS(code);
                    cache.put(key, crs);
                }
            } finally {
                cache.writeUnLock(key);
            }            
        }
        if (crs == null) {
            crs = (GeographicCRS) cache.get(key);
        }
        return crs;
    }

    public ImageCRS createImageCRS( String code ) throws FactoryException {
        final String key = trimAuthority(code);
        ImageCRS crs=null;
        if (!cache.containsKey(key)) {
            try {
                cache.writeLock(key);
                if (!cache.containsKey(key)) {
                    crs = crsAuthority.createImageCRS(code);
                    cache.put(key, crs);
                }
            } finally {
                cache.writeUnLock(key);
            }            
        }
        if (crs == null) {
            crs = (ImageCRS) cache.get(key);
        }
        return crs;
    }

    public ProjectedCRS createProjectedCRS( String code ) throws FactoryException {
        final String key = trimAuthority(code);
        ProjectedCRS crs=null;
        if (!cache.containsKey(key)) {
            try {
                cache.writeLock(key);
                if (!cache.containsKey(key)) {
                    crs = crsAuthority.createProjectedCRS(code);
                    cache.put(key, crs);
                }
            } finally {
                cache.writeUnLock(key);
            }            
        }
        if (crs == null) {
            crs = (ProjectedCRS) cache.get(key);
        }
        return crs;
    }

    public TemporalCRS createTemporalCRS( String code ) throws FactoryException {
        final String key = trimAuthority(code);
        TemporalCRS crs=null;
        if (!cache.containsKey(key)) {
            try {
                cache.writeLock(key);
                if (!cache.containsKey(key)) {
                    crs = crsAuthority.createTemporalCRS(code);
                    cache.put(key, crs);
                }
            } finally {
                cache.writeUnLock(key);
            }            
        }
        if (crs == null) {
            crs = (TemporalCRS) cache.get(key);
        }
        return crs;
    }

    public VerticalCRS createVerticalCRS( String code ) throws FactoryException {
        final String key = trimAuthority(code);
        VerticalCRS crs=null;
        if (!cache.containsKey(key)) {
            try {
                cache.writeLock(key);
                if (!cache.containsKey(key)) {
                    crs = crsAuthority.createVerticalCRS(code);
                    cache.put(key, crs);
                }
            } finally {
                cache.writeUnLock(key);
            }            
        }
        if (crs == null) {
            crs = (VerticalCRS) cache.get(key);
        }
        return crs;
    }
    

}
