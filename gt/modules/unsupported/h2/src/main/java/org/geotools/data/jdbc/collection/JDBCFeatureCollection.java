package org.geotools.data.jdbc.collection;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.data.FeatureEvent;
import org.geotools.data.FeatureListener;
import org.geotools.data.FeatureSource;
import org.geotools.data.jdbc.JDBCFeatureSource;
import org.geotools.data.store.ContentState;
import org.geotools.feature.CollectionEvent;
import org.geotools.feature.CollectionListener;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.FeatureList;
import org.geotools.feature.FeatureType;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.collection.DelegateFeatureIterator;
import org.geotools.feature.visitor.FeatureVisitor;
import org.geotools.util.ProgressListener;
import org.opengis.filter.Filter;
import org.opengis.filter.sort.SortBy;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

/**
 * Contains some common state needed for all JDBCFeatureCollections.
 * <p>
 * Please note the result does *not* implement FeatureCollection itself (
 * as you may want to choose between FeatureList and FeatureCollection).
 * 
 * @author Jody
 */
public abstract class JDBCFeatureCollection implements FeatureCollection {
	JDBCFeatureSource source;

	/** logger */
	static Logger LOGGER = Logger.getLogger( "org.geotools.data.jdbc.collection" );
	
	FeatureListener listener = new FeatureListener(){
		public void changed(FeatureEvent featureEvent) {
			if( listeners.isEmpty() ) return;

			FeatureCollection collection = (FeatureCollection) JDBCFeatureCollection.this;
			CollectionEvent event = new CollectionEvent( collection, featureEvent );

			CollectionListener[] notify = (CollectionListener[]) listeners.toArray( new CollectionListener[ listeners.size() ]);
			for( int i=0; i<notify.length; i++ ){
				CollectionListener listener = notify[i];
				try {
					    listener.collectionChanged( event );
				}
				catch (Throwable t ){
					LOGGER.log( Level.WARNING, "Problem encountered during notification of "+event, t );
				}
			}
		}			
	};
	
    /** Internal listener storage list */
    private List listeners = new ArrayList(2);

    /** Set of open resource iterators */
    protected final Set open = new HashSet();

    /** A FeatureCollection backed onto the provided source */
	JDBCFeatureCollection( JDBCFeatureSource source ){
		this.source = source;
		source.addFeatureListener(listener);
	}
	
    /**
     * Adds a listener for collection events.
     *
     * @param listener The listener to add
     */
    public void addListener(CollectionListener listener) {
        listeners.add(listener);
    }

    /**
     * Removes a listener for collection events.
     *
     * @param listener The listener to remove
     */
    public void removeListener(CollectionListener listener) {
        listeners.remove(listener);
    }
    
    // Iterators
    public FeatureIterator features()(
        FeatureIterator iterator = createFeatureIterator();
        open.add( iterator ); // remember for later
        return iterator;
    }
	public void close( FeatureIterator close ) {
		close.close();
	    open.remove( close );
	}
	public Iterator iterator(){
		FeatureIterator iterator = features();
		
	}

    /** Typesafe access to contents of collection */
    protected abstract FeatureIterator createFeatureIterator();
    
    public void purge() {
        for( Iterator i = open.iterator(); i.hasNext(); ){
            Object resource = i.next();
            if( resource instanceof FeatureIterator ){
                FeatureIterator resourceIterator = (FeatureIterator) resource;
                try {
                    closeIterator( resourceIterator );
                }
                catch( Throwable e){
                    // TODO: Log e = ln
                }
                finally {
                    i.remove();
                }
            }
        }        
    }
    /**
     * Accepts a visitor, which then visits each feature in the collection.
     * @throws IOException 
     */
    public void accepts(FeatureVisitor visitor, ProgressListener progress ) throws IOException {
        Iterator iterator = null;
        // if( progress == null ) progress = new NullProgressListener();
        try{
            float size = size();
            float position = 0;            
            progress.started();
            for( iterator = iterator(); !progress.isCanceled() && iterator.hasNext();){
                if (size > 0) progress.progress( position++/size );
                try {
                    Feature feature = (Feature) iterator.next();
                    visitor.visit(feature);
                }
                catch( Exception erp ){
                    progress.exceptionOccurred( erp );
                }
            }            
        }
        finally {
            progress.complete();            
            close( iterator );
        }
    }


	public void close(Iterator close) {
		
	}

	public FeatureType getFeatureType() {
		return source.getFeatureType();
	}

	public FeatureType getSchema() {
		return source.getSchema();
	}

	public Iterator iterator() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean add(Object arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean addAll(Collection arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public void clear() {
		// TODO Auto-generated method stub
		
	}

	public boolean contains(Object o) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean containsAll(Collection arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean remove(Object o) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean removeAll(Collection arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean retainAll(Collection arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public int size() {
		// TODO Auto-generated method stub
		return 0;
	}

	public Object[] toArray() {
		// TODO Auto-generated method stub
		return null;
	}

	public Object[] toArray(Object[] arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public Object getAttribute(String xPath) {
		// TODO Auto-generated method stub
		return null;
	}

	public Object getAttribute(int index) {
		// TODO Auto-generated method stub
		return null;
	}

	public Object[] getAttributes(Object[] attributes) {
		// TODO Auto-generated method stub
		return null;
	}

	public Envelope getBounds() {
		// TODO Auto-generated method stub
		return null;
	}

	public Geometry getDefaultGeometry() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getID() {
		// TODO Auto-generated method stub
		return null;
	}

	public int getNumberOfAttributes() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void setAttribute(int position, Object val) throws IllegalAttributeException, ArrayIndexOutOfBoundsException {
		// TODO Auto-generated method stub
		
	}

	public void setAttribute(String xPath, Object attribute) throws IllegalAttributeException {
		// TODO Auto-generated method stub
		
	}

	public void setDefaultGeometry(Geometry geometry) throws IllegalAttributeException {
		// TODO Auto-generated method stub
		
	}

}
