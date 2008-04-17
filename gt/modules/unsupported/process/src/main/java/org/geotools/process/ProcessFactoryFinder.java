/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *    (C) 2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.process;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.geotools.factory.FactoryCreator;
import org.geotools.factory.FactoryFinder;
import org.geotools.factory.FactoryRegistry;
import org.geotools.factory.FactoryRegistryException;
import org.geotools.process.literal.BufferFactory;
import org.geotools.process.literal.IntersectionFactory;
import org.geotools.process.literal.UnionFactory;
import org.geotools.resources.LazySet;


/**
 * Defines static methods used to access the application's default process factory implementations.
 *
 * @author Graham Davis
 */
public class ProcessFactoryFinder extends FactoryFinder {
    /**
     * The service registry for this manager. Will be initialized only when first needed.
     */
    private static FactoryRegistry registry;

    /**
     * Do not allow any instantiation of this class.
     */
    private ProcessFactoryFinder() {
        // singleton
    }

    /**
     * Returns the service registry. The registry will be created the first
     * time this method is invoked.
     */
    private static FactoryRegistry getServiceRegistry() {
        assert Thread.holdsLock(ProcessFactoryFinder.class);
        if (registry == null) {
            registry = new FactoryCreator(Arrays.asList(new Class<?>[] {
            		ProcessFactory.class}));
        }
        return registry;
    }
    
    public static Set<ProcessFactory> getProcessFactories() {
    	return new LazySet<ProcessFactory>(getServiceRegistry().getServiceProviders(
        		ProcessFactory.class, null, null));
    }
    
    /**
     * Finds all implementations of BufferFactory which have registered using
     * the services mechanism, and that have the appropriate libraries on the
     * classpath.
     *
     * @return An iterator over all discovered BufferFactory
     */
    public static synchronized Iterator<BufferFactory> getBufferFactory() throws FactoryRegistryException {
        FactoryRegistry serviceRegistry = getServiceRegistry();
        return getServiceRegistry().getServiceProviders(BufferFactory.class, null, null);   
    }    

    /**
     * Finds all implementations of IntersectionFactory which have registered using
     * the services mechanism, and that have the appropriate libraries on the
     * classpath.
     *
     * @return An iterator over all discovered IntersectionFactory
     */
    public static synchronized Iterator<IntersectionFactory> getIntersectsFactory() throws FactoryRegistryException {
        FactoryRegistry serviceRegistry = getServiceRegistry();
        return getServiceRegistry().getServiceProviders(IntersectionFactory.class, null, null);   
    }
    
    /**
     * Finds all implementations of UnionFactory which have registered using
     * the services mechanism, and that have the appropriate libraries on the
     * classpath.
     *
     * @return An iterator over all discovered UnionFactory
     */
    public static synchronized Iterator<UnionFactory> getUnionFactory() throws FactoryRegistryException {
        FactoryRegistry serviceRegistry = getServiceRegistry();
        return getServiceRegistry().getServiceProviders(UnionFactory.class, null, null);   
    }    
}
