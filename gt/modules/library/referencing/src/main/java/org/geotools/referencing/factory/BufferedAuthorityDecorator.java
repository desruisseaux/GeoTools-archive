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
import org.opengis.util.InternationalString;

// Geotools dependencies
import org.geotools.factory.Hints;
import org.geotools.factory.BufferedFactory;
import org.geotools.resources.Utilities;
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.i18n.Logging;
import org.geotools.resources.i18n.LoggingKeys;


/**
 * A decorator that wraps an existing AuthorityFactory and caches all objects created.
 * <p>
 * All {@code createFoo(String)} methods first looks if a previously created object
 * exists for the given code. If such an object exists, it is returned. Otherwise,
 * the object creation is delegated to the {@linkplain AbstractAuthorityFactory authority factory}
 * specified at creation time, and the result is cached in this buffered factory. 
 * <p>
 * The kind of cache used internally can be specified at construction time.
 * <p>
 * For the default implementation: Objects are cached by strong references, up to the
 * amount of objects specified at construction time. If a greater amount of objects are
 * cached, the oldest ones will be retained through a {@linkplain WeakReference weak reference}
 * instead of a strong one. This means that this buffered factory will continue to returns
 * them as long as they are in use somewhere else in the Java virtual machine, but will be
 * discarted (and recreated on the fly if needed) otherwise.
 * </p>
 * @since 2.4
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class BufferedAuthorityDecorator extends AbstractBufferedAuthorityFactory {

    /**
     * The underlying authority factory. This field may be {@code null} if this object was
     * created by the {@linkplain #BufferedAuthorityFactory(AbstractAuthorityFactory,int)
     * package protected constructor}. In this case, the subclass is responsible for creating
     * the backing store when {@link DeferredAuthorityFactory#createBackingStore} is invoked.
     *
     * @see #getBackingStore
     * @see DeferredAuthorityFactory#createBackingStore
     */
    AuthorityFactory backingStore;
    

    /**
     * Constructs an instance wrapping the specified factory with a default number
     * of entries to keep by strong reference.
     * <p>
     * This constructor is protected because subclasses must declare which of the
     * {@link DatumAuthorityFactory}, {@link CSAuthorityFactory}, {@link CRSAuthorityFactory}
     * and {@link CoordinateOperationAuthorityFactory} interfaces they choose to implement.
     *
     * @param factory The factory to cache. Can not be {@code null}.
     */
    protected BufferedAuthorityDecorator(final AuthorityFactory factory) {
        super( new DefaultReferencingObjectCache( DEFAULT_MAX ) );
        backingStore = factory;
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
    protected BufferedAuthorityDecorator(AbstractAuthorityFactory factory,
                                       final int maxStrongReferences)
    {
        super( new DefaultReferencingObjectCache( maxStrongReferences ) );
        backingStore = factory;
    }

    /**
     * Constructs an instance without initial backing store. This constructor is for subclass
     * constructors only. Subclasses are responsible for creating an appropriate backing store
     * when the {@link DeferredAuthorityFactory#createBackingStore} method is invoked.
     *
     * @param priority The priority for this factory, as a number between
     *        {@link #MINIMUM_PRIORITY MINIMUM_PRIORITY} and
     *        {@link #MAXIMUM_PRIORITY MAXIMUM_PRIORITY} inclusive.
     * @param maxStrongReferences The maximum number of objects to keep by strong reference.
     *
     * @see DeferredAuthorityFactory#createBackingStore
     */
    BufferedAuthorityDecorator(final int priority, final int maxStrongReferences) {
        super( priority, new DefaultReferencingObjectCache( maxStrongReferences ));
        // completeHints() will be invoked by DeferredAuthorityFactory.getBackingStore()
    }

    protected AuthorityFactory getBackingStore() {
        return backingStore;
    }
}
