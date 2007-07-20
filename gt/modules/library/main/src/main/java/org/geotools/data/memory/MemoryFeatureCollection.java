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
package org.geotools.data.memory;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.TreeMap;

import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.feature.collection.AbstractFeatureCollection;
import org.geotools.feature.collection.AbstractResourceCollection;

import org.geotools.feature.collection.FeatureState;
import org.geotools.feature.collection.RandomFeatureAccess;

/**
 * Implement a FeatureCollection by burning memory!
 * <p>
 * Contents are maintained in a sorted TreeMap by FID, this serves as a reference implementation
 * when exploring the FeatureCollection api.
 * </p>
 * <p>
 * This is similar to DefaultFeatureCollection, although additional methods are
 * supported and test cases have been written. Unlike DefaultFeatureCollection
 * the type information must be known at construction time.
 * </p>
 *   
 * @author Jody Garnett, Refractions Research
 * @source $URL$
 */
public class MemoryFeatureCollection extends AbstractFeatureCollection implements RandomFeatureAccess {
    TreeMap contents = new TreeMap();
    
    public MemoryFeatureCollection( FeatureType schema ){
        super( schema );
        setResourceCollection(
    		new AbstractResourceCollection() {
    			public boolean add( Object o ) {
    		        Feature feature = (Feature) o;
    		        contents.put( feature.getID(), feature );
    		        return true;
    			}
    		    
			  public int size() {
			        return contents.size();
			  }

			  public Iterator openIterator() {
			        return new MemoryIterator( contents.values().iterator() );
			  }

			  public void closeIterator( Iterator close ) {
			        if( close == null ) return;
			        
			        MemoryIterator it = (MemoryIterator) close;
			        it.close();
			  }
    	   }
		);
    }
    
    
    class MemoryIterator implements Iterator {
        Iterator it;
        MemoryIterator( Iterator iterator ){
            it = iterator;
        }
        public void close(){
            it = null;
        }
        public boolean hasNext() {
            if( it == null ){
                throw new IllegalStateException();
            }            
            return it.hasNext();
        }
        public Object next() {
            if( it == null ){
                throw new IllegalStateException();
            }
            return it.next(); 
        }
        public void remove() {
            it.remove();
        }        
    }

    //
    // RandomFeatureAccess 
    //
    public Feature getFeatureMember( String id ) throws NoSuchElementException {
        if( contents.containsKey( id ) ){
            return (Feature) contents.get( id );
        }
        throw new NoSuchElementException( id );
    }

    public Feature removeFeatureMember( String id ) {
        if( contents.containsKey( id ) ){
            Feature old = (Feature) contents.get( id );
            contents.remove( id );
            return old;
        }
        return null;
    };
}
