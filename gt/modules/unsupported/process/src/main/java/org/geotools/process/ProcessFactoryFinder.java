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
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import org.geotools.factory.FactoryCreator;
import org.geotools.factory.FactoryFinder;
import org.geotools.factory.FactoryRegistry;
import org.geotools.factory.FactoryRegistryException;
import org.geotools.resources.LazySet;
import org.geotools.util.NullProgressListener;


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
     * Look up a Factory by name.
     * 
     * @param name Name of Factory
     * @return ProcessFactory with matching name
     */
    public static synchronized ProcessFactory createProcessFactory( String name){
        for( ProcessFactory factory : getProcessFactories() ){
            if( name.equals( factory.getName() )){
                return factory;
            }
        }
        return null; // go fish
    }
    
    public static synchronized Process createProcess(String name){
        ProcessFactory factory = createProcessFactory( name );
        if( factory == null ) return null;
        
        return factory.create();
    }
    
    public static synchronized Map<String,Object> execute(String name, Map<String,Object> input ){
        Process process = createProcess( name );
        if( process == null ) return null;
        
        process.setInput( input );
        process.process( new NullProgressListener() );
        
        return process.getResult();
    }
    
    public static Callable<Map<String,Object>> createCallable( final Process process, final Map<String,Object> input ){        
        return new Callable<Map<String,Object>>(){
            public Map<String, Object> call() throws Exception {
                process.setInput( input );
                process.process( new NullProgressListener() );
                return process.getResult();
            }            
        };
    }
    
    /**
     * This progress listener checks if the current Thread is interrupted, it
     * acts as a bridge between Future and ProgressListener code.
     * 
     * @author Jody
     */
    static class CallableProgressListener extends NullProgressListener {
        @Override
        public boolean isCanceled() {
            return Thread.currentThread().isInterrupted();
        }
    }
}
