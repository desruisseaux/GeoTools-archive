/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, GeoTools Project Managment Committee (PMC)
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
package org.geotools.factory;

// J2SE dependencies
import java.util.Arrays;
import java.util.Set;

// Geotools dependencies
import org.geotools.styling.StyleFactory;
import org.geotools.catalog.ServiceFactory;
import org.geotools.data.FeatureLock;
import org.geotools.data.FeatureLockFactory;
import org.geotools.data.FileDataStoreFactorySpi;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.DefaultFeatureTypeFactory;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.FeatureTypeFactory;
import org.geotools.filter.FilterFactoryFinder;
import org.geotools.filter.FunctionExpression;
import org.geotools.filter.FunctionImpl;
import org.geotools.resources.LazySet;

// OpenGIS dependencies
import org.opengis.filter.FilterFactory;


/**
 * Defines static methods used to access the application's default implementation for some
 * common factories. Those "common" factories comprise the {@linkplain StyleFactory style}
 * and {@linkplain FilterFactory filter} factories. Note that some specialized factories
 * finder like {@linkplain org.geotools.referencing.ReferencingFactoryFinder referencing} and
 * {@linkplain org.geotools.coverage.ReferencingFactoryFinder coverage} are defined in specialized
 * classes.
 * <p>
 * <b>Tip:</b> The {@link BasicFactories} classes provides an other way to access the various
 * factories from a central point.
 *
 * @since 2.4
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Jody Garnett
 */
public final class CommonFactoryFinder {
    /**
     * The service registry for this manager.
     * Will be initialized only when first needed.
     */
    private static FactoryRegistry registry;

    /**
     * Do not allows any instantiation of this class.
     */
    private CommonFactoryFinder() {
        // singleton
    }

    /**
     * Returns the service registry. The registry will be created the first
     * time this method is invoked.
     */
    private static FactoryRegistry getServiceRegistry() {
        assert Thread.holdsLock(CommonFactoryFinder.class);
        if (registry == null) {
            registry = new FactoryCreator(Arrays.asList(new Class[] {
                    StyleFactory.class,
                    FilterFactory.class,
                    FeatureLockFactory.class,
                    FileDataStoreFactorySpi.class,
                    FunctionImpl.class,
                    FunctionExpression.class,
                    AttributeTypeFactory.class,
                    FeatureCollections.class,
                    FeatureTypeFactory.class}));
        }
        return registry;
    }

    /**
     * Add {@linkplain GeoTools#getDefaultHints defaults hints} to the specified user hints.
     * User hints have precedence.
     * <p>
     * <b>Note:</b>
     * Revisit if this method is necessary. Default hints should already be
     * taken in account if the factory implementations get their hints with
     * {@link org.geotools.factory.AbstractFactory#getHintValue}. This method
     * is a safety for factories that are not implemented like that, but we
     * could (in theory) remove it for Geotools factories if profiling show
     * that this method has noticeable performance impact.
     */
    private static Hints addDefaultHints(final Hints hints) {
        final Hints completed = GeoTools.getDefaultHints();
        if (hints != null) {
            completed.add(hints);
        }
        return completed;
    }

    /**
     * Returns the first implementation of {@link StyleFactory} matching the specified hints.
     * If no implementation matches, a new one is created if possible or an exception is thrown
     * otherwise.
     *
     * @param  hints An optional map of hints, or {@code null} if none.
     * @return The first style factory that matches the supplied hints.
     * @throws FactoryRegistryException if no implementation was found or can be created for the
     *         {@link StyleFactory} interface.
     *
     * @see Hints#STYLE_FACTORY
     */
    public static synchronized StyleFactory getStyleFactory(Hints hints)
            throws FactoryRegistryException
    {
        hints = addDefaultHints(hints);
        return (StyleFactory) getServiceRegistry().getServiceProvider(
                StyleFactory.class, null, hints, Hints.STYLE_FACTORY);
    }

    /**
     * Returns a set of all available implementations for the {@link StyleFactory} interface.
     *
     * @param  hints An optional map of hints, or {@code null} if none.
     * @return Set of available style factory implementations.
     */
    public static synchronized Set getStyleFactories(Hints hints) {
        hints = addDefaultHints(hints);
        return new LazySet(getServiceRegistry().getServiceProviders(
                StyleFactory.class, null, hints));
    }

    /**
     * Returns a set of all available implementations for the {@link FunctionExpression} interface.
     *
     * @param  hints An optional map of hints, or {@code null} if none.
     * @return Set of available style factory implementations.
     * @deprecated Use FunctionExpression is now @deprecated
     */
    public static synchronized Set getFunctionExpressions(Hints hints) {
        hints = addDefaultHints(hints);
        return new LazySet(getServiceRegistry().getServiceProviders(
                FunctionExpression.class, null, hints));
    }


    /**
     * Returns a set of all available implementations for the {@link FunctionExpression} interface.
     * 
     * @param  hints An optional map of hints, or {@code null} if none.
     * @return Set<FunctonImpl> of available style factory implementations.
     */
    public static synchronized Set getFunctions(Hints hints) {
        hints = addDefaultHints(hints);
        return new LazySet(getServiceRegistry().getServiceProviders(
                FunctionImpl.class, null, hints));
    }    
    
    /**
     * Returns a set of all available implementations for the {@link FeatureLockFactory} interface.
     * 
     * @param  hints An optional map of hints, or {@code null} if none.
     * @return Set<FeatureLockFactory> of available style factory implementations.
     */
    public static synchronized Set getFeatureLockFactories(Hints hints) {
        hints = addDefaultHints(hints);
        return new LazySet(getServiceRegistry().getServiceProviders(
                FeatureLockFactory.class, null, hints));
    }
    
    public static synchronized FeatureLockFactory getFeatureLockFactory(Hints hints) {
        hints = addDefaultHints(hints);
        return (FeatureLockFactory) getServiceRegistry().getServiceProvider(
                FeatureLockFactory.class, null, hints, Hints.FEATURE_LOCK_FACTORY);
    }
    
    /**
     * Returns a set of all available implementations for the {@link FileDataStoreFactorySpi} interface.
     * 
     * @param  hints An optional map of hints, or {@code null} if none.
     * @return Set<FileDataStoreFactorySpi> of available style factory implementations.
     */
    public static synchronized Set getFileDataStoreFactories(Hints hints) {
        hints = addDefaultHints(hints);
        return new LazySet(getServiceRegistry().getServiceProviders(
                FileDataStoreFactorySpi.class, null, hints));
    }

    /**
     * Returns a set of all available implementations for the {@link AttributeTypeFactory} interface.
     *
     * @param  hints An optional map of hints, or {@code null} if none.
     * @return Set<AttributeTypeFactory> of available factory implementations.
     */
    public static synchronized Set getAttributeTypeFactories(Hints hints) {
        hints = addDefaultHints(hints);
        return new LazySet(getServiceRegistry().getServiceProviders(
                AttributeTypeFactory.class, null, hints));
    }
    /**
     * The default AttributeTypeFactory.
     * <p>
     * Initially set to DefaultAttributeTypeFactory.
     */
    public static synchronized AttributeTypeFactory getAttributeTypeFactory(Hints hints) {
        hints = addDefaultHints(hints);
        return (AttributeTypeFactory) getServiceRegistry().getServiceProvider(
                AttributeTypeFactory.class, null, hints, Hints.ATTRIBUTE_TYPE_FACTORY );
    }
    
    /**
     * Returns a set of all available implementations for the {@link AttributeTypeFactory} interface.
     *
     * @param  hints An optional map of hints, or {@code null} if none.
     * @return Set<AttributeTypeFactory> of available factory implementations.
     */
    public static synchronized Set getAttributeFeatureFactories(Hints hints) {
        hints = addDefaultHints(hints);
        return new LazySet(getServiceRegistry().getServiceProviders(
                FeatureTypeFactory.class, null, hints));
    }
    /**
     * The default AttributeTypeFactory.
     * <p>
     * You can use the following Hints:
     * <ul>
     * <li>FEATURE_TYPE_FACTORY - to control or reuse an implementation
     * <li>FEATURE_TYPE_FACTORY_NAME - to supply a name for the returned factory
     * </ul>
     * GeoTools ships with a DefaultAttributeTypeFactory, although you can hook up
     * your own implementation as needed.
     * @return FeatureTypeFactory using Hints.FEATURE_TYPE_FACTORY_NAME
     */
    public static synchronized FeatureTypeFactory getFeatureTypeFactory(Hints hints) {
        hints = addDefaultHints(hints);
        FeatureTypeFactory factory = new DefaultFeatureTypeFactory();        
        factory.setName( (String) hints.get( Hints.FEATURE_TYPE_FACTORY_NAME));
        return factory;
        
        //return (FeatureTypeFactory) getServiceRegistry().getServiceProvider(
        //        FeatureTypeFactory.class, null, hints, Hints.FEATURE_TYPE_FACTORY );
    }
    
    /**
     * Returns a set of all available implementations for the {@link FeatureCollections} interface.
     *
     * @param  hints An optional map of hints, or {@code null} if none.
     * @return Set<FeatureCollections> of available implementations.
     */
    public static synchronized Set getFeatureCollectionsSet(Hints hints) {
        hints = addDefaultHints(hints);
        return new LazySet(getServiceRegistry().getServiceProviders(
                FeatureCollections.class, null, hints));
    }    
    /**
     * The default FeatureCollections instance.
     * <p>
     * Access to a utility class to let you work with FeatureCollections.
     * </p>
     * @param hints
     * @return FeatureCollections utility class
     */
    public static synchronized FeatureCollections getFeatureCollections(Hints hints) {
        hints = addDefaultHints(hints);
        return (FeatureCollections) getServiceRegistry().getServiceProvider(
                FeatureCollections.class, null, hints, Hints.FEATURE_COLLECTIONS );
    }
    
    /**
     * Returns the first implementation of {@link FilterFactory} matching the specified hints.
     * If no implementation matches, a new one is created if possible or an exception is thrown
     * otherwise.
     *
     * @param  hints An optional map of hints, or {@code null} if none.
     * @return The first filter factory that matches the supplied hints.
     * @throws FactoryRegistryException if no implementation was found or can be created for the
     *         {@link FilterFactory} interface.
     *
     * @see Hints#FILTER_FACTORY
     */
    public static synchronized FilterFactory getFilterFactory(Hints hints)
            throws FactoryRegistryException
    {
        hints = addDefaultHints(hints);
        return (FilterFactory) getServiceRegistry().getServiceProvider(
                FilterFactory.class, null, hints, Hints.FILTER_FACTORY);
    }

    /**
     * Returns a set of all available implementations for the {@link FilterFactory} interface.
     *
     * @param  hints An optional map of hints, or {@code null} if none.
     * @return Set of available filter factory implementations.
     */
    public static synchronized Set getFilterFactories(Hints hints) {
        hints = addDefaultHints(hints);
        return new LazySet(getServiceRegistry().getServiceProviders(
                FilterFactory.class, null, hints));
    }

    /**
     * Scans for factory plug-ins on the application class path. This method is
     * needed because the application class path can theoretically change, or
     * additional plug-ins may become available. Rather than re-scanning the
     * classpath on every invocation of the API, the class path is scanned
     * automatically only on the first invocation. Clients can call this
     * method to prompt a re-scan. Thus this method need only be invoked by
     * sophisticated applications which dynamically make new plug-ins
     * available at runtime.
     */
    public static synchronized void scanForPlugins() {
        if (registry != null) {
            registry.scanForPlugins();
        }
    }
}
