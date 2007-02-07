package org.geotools.feature.iso.collection;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.TreeMap;

import org.geotools.feature.CollectionListener;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.FeatureType;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.visitor.FeatureVisitor;
import org.geotools.util.ProgressListener;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeatureCollection;
import org.opengis.feature.simple.SimpleFeatureCollectionType;
import org.opengis.feature.type.Name;

import com.vividsolutions.jts.geom.Geometry;

/**
 * Implement a SimpleFeatureCollection by burning memory!
 * <p>
 * Contents are maintained in a sorted TreeMap by FID, this serves as a
 * reference implementation when exploring the FeatureCollection api.
 * </p>
 *   
 * @author Jody Garnett, Refractions Research
 * @source $URL$
 */
public class MemorySimpleFeatureCollection extends AbstractSimpleFeatureCollection implements SimpleFeatureCollection,RandomFeatureAccess {
	
	public MemorySimpleFeatureCollection(SimpleFeatureCollectionType type, String id) {
		super(type, id);
	}

	TreeMap contents = new TreeMap();
    
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
    }

    public Object operation(Name arg0, List arg1) {
        throw new UnsupportedOperationException();
    }

   
}
