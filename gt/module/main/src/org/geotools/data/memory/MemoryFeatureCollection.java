package org.geotools.data.memory;

import java.util.Iterator;
import java.util.TreeMap;

import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.feature.collection.AbstractFeatureCollection;
import org.geotools.feature.collection.FeatureState;

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
 */
public class MemoryFeatureCollection extends AbstractFeatureCollection {
    TreeMap contents = new TreeMap();
    FeatureState state;
    
    public MemoryFeatureCollection( FeatureType schema ){
        super( schema );
    }
    
    public int size() {
        return contents.size();
    }

    protected Iterator openIterator() {
        return new MemoryIterator( contents.values().iterator() );
    }

    protected void closeIterator( Iterator close ) {
        if( close == null ) return;
        
        MemoryIterator it = (MemoryIterator) close;
        it.close();
    }
    
    public boolean add( Object o ) {
        Feature feature = (Feature) o;
        contents.put( feature.getID(), feature );
        return true;
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
    };
}