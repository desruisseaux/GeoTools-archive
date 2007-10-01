package org.geotools.data.jdbc.collection;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import org.geotools.data.FeatureEvent;
import org.geotools.data.FeatureListener;
import org.geotools.data.jdbc.JDBCDataStore;
import org.geotools.data.jdbc.JDBCFeatureSource;
import org.geotools.data.jdbc.JDBCRunnable;
import org.geotools.data.jdbc.JDBCState;
import org.geotools.data.jdbc.JDBCUtils;
import org.geotools.data.jdbc.SQLBuilder;
import org.geotools.data.jdbc.SQLDialect;
import org.geotools.data.store.FeatureIteratorIterator;
import org.geotools.feature.CollectionEvent;
import org.geotools.feature.CollectionListener;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.visitor.FeatureVisitor;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.util.ProgressListener;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
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
public class JDBCFeatureCollection implements FeatureCollection {

	/**
	 * feature sourfce the collection originated from.
	 */
	JDBCFeatureSource source;
	/**
	 * state of the feature source 
	 */
	JDBCState state;
	/**
	 * filter used to filter content.
	 */
	Filter filter;
	
	/**
	 * feature listener which listens to the feautre source and 
	 * forwards events to its listeners.
	 */
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
					JDBCDataStore.LOGGER.log( Level.WARNING, "Problem encountered during notification of "+event, t );
				}
			}
		}			
	};
	
    /** Internal listener storage list */
    private List listeners = new ArrayList(2);

    /** Set of open resource iterators */
    protected final Set open = new HashSet();

   public JDBCFeatureCollection( JDBCFeatureSource source, JDBCState state ) {
		this( source, state, null );
	}
	
	public JDBCFeatureCollection( JDBCFeatureSource source, JDBCState state, Filter filter ) {
		this.source = source;
		this.state = state;
		this.filter = filter;
		
		//add the feautre source listener
		source.addFeatureListener(listener);
	
		//ensure the state has a connection
		if ( state.getConnection() == null ) {
			state.setConnection( ((JDBCDataStore) source.getDataStore()).connection() );
		}
	}
	
	/**
	 * @return The feautre source the collection originates from.
	 */
	public JDBCFeatureSource getFeatureSource() {
		return source;
	}
	
	/**
	 * Sets a filter used to "filter" the content of the collection.
	 */
	public void setFilter(Filter filter) {
		this.filter = filter;
	}
	
	/**
	 * @return The filter being used to "filter" the collection.
	 */
	public Filter getFilter() {
		return filter;
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
    public FeatureIterator features(){
        FeatureIterator iterator;
		try {
			iterator = createFeatureIterator();
		} 
		catch (Exception e) {
			throw new RuntimeException( e );
		}
        
        // keep track of the iterator
        open.add( iterator );
        
        return iterator;
    }
	
    public void close( FeatureIterator close ) {
		close.close();
	    open.remove( close );
	}
	
	public Iterator iterator() {
		FeatureIterator iterator = features();
		return new FeatureIteratorIterator( iterator );
	}
	
	public void close(Iterator close) {
		if ( close instanceof FeatureIteratorIterator ) {
			FeatureIteratorIterator iterator = (FeatureIteratorIterator) close;
			close( iterator.getDelegate() );
		}
	}

    protected FeatureIterator createFeatureIterator() throws Exception {
        //build up a statement for the content
        JDBCDataStore dataStore = source.getDataStore();
        String sql = dataStore.selectSQL( source.getSchema(), filter );
        
        Statement st = state.getConnection().createStatement();
    	st.execute( sql );
    	
    	return new JDBCFeatureIterator( st, this );
    }
    
    public void purge() {
        for( Iterator i = open.iterator(); i.hasNext(); ){
            Object resource = i.next();
            if( resource instanceof FeatureIterator ){
                FeatureIterator resourceIterator = (FeatureIterator) resource;
                try {
                    close( resourceIterator );
                }
                catch( Throwable e){
                	String msg = "Error occured closing iterator";
                	JDBCDataStore.LOGGER.log( Level.WARNING, msg, e );
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
                    SimpleFeature feature = (SimpleFeature) iterator.next();
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

    public SimpleFeatureType getSchema() {
		return source.getSchema();
	}

    public ReferencedEnvelope getBounds() {
		
		JDBCRunnable runnable = new JDBCRunnable() {

			public Object run(Statement statement) throws IOException, SQLException {
				
				Envelope bounds = new Envelope();
				
				SQLBuilder sql = 
					new SQLBuilder( (JDBCDataStore) source.getDataStore() );
				ResultSet rs = statement.executeQuery( sql.bounds( source.getSchema(), filter ) );
				
				if ( rs.next() ) {
					bounds.init( (Envelope) rs.getObject( 1 ) );
					while( rs.next() ) {
						bounds.expandToInclude( (Envelope) rs.getObject( 1 ) );
					}
				}
				else {
					bounds.setToNull();
				}
				
				rs.close();
				
				return ReferencedEnvelope.reference(bounds);
			}
			
		};
		
		try {
			return (ReferencedEnvelope) JDBCUtils.statement( state.getConnection(), runnable );
		} 
		catch (IOException e) {
			throw new RuntimeException( e );
		}
	}

	public int size() {
		JDBCRunnable runnable = new JDBCRunnable() {

			public Object run(Statement statement) throws IOException, SQLException {
				SQLBuilder sql = 
					new SQLBuilder( (JDBCDataStore) source.getDataStore() );
				
				ResultSet rs = statement.executeQuery( sql.count( source.getSchema(),filter ) );
				rs.next();
				
				Integer count = new Integer( rs.getInt( 1 ) );
				rs.close();
				
				return count;
			}
			
		};
		
		try {
			return ((Integer)JDBCUtils.statement( state.getConnection(), runnable )).intValue();
		} 
		catch (IOException e) {
			throw new RuntimeException( e );
		}
	}
	
	public boolean isEmpty() {
		return size() == 0;
	}
	
	public boolean add(Object o) {
		throw new UnsupportedOperationException();
	}

	public boolean addAll(Collection arg0) {
		throw new UnsupportedOperationException();
	}

	public void clear() {
		throw new UnsupportedOperationException();
	}

	public boolean contains(Object o) {
		throw new UnsupportedOperationException();
	}

	public boolean containsAll(Collection arg0) {
		throw new UnsupportedOperationException();
	}

	public boolean remove(Object o) {
		throw new UnsupportedOperationException();
	}

	public boolean removeAll(Collection arg0) {
		throw new UnsupportedOperationException();
	}

	public boolean retainAll(Collection arg0) {
		throw new UnsupportedOperationException();
	}

	public Object[] toArray() {
		throw new UnsupportedOperationException();
	}

	public Object[] toArray(Object[] arg0) {
		throw new UnsupportedOperationException();
	}

}

