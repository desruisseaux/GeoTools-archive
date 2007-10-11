package org.geotools.data.jdbc;

import java.io.IOException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import org.geotools.data.FeatureEvent;
import org.geotools.data.FeatureListener;
import org.geotools.data.store.FeatureIteratorIterator;
import org.geotools.data.store.FilteringFeatureIterator;
import org.geotools.feature.CollectionEvent;
import org.geotools.feature.CollectionListener;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.visitor.FeatureVisitor;
import org.geotools.filter.visitor.PostPreProcessFilterSplittingVisitor;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.util.ProgressListener;
import org.opengis.feature.GeometryAttribute;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;
import org.opengis.filter.sort.SortBy;

/**
 * Contains some common state needed for all JDBCFeatureCollections.
 * <p>
 * Please note the result does *not* implement FeatureCollection itself (
 * as you may want to choose between FeatureList and FeatureCollection).
 * 
 * @author Jody Garnett, Refractions Research
 * @author Justin Deoliveira, The Open Planning Project
 */
public class JDBCFeatureCollection implements FeatureCollection {

    /**
     * data store the collection originated from
     */
    JDBCDataStore dataStore;
	/**
	 * feature store the collection originated from.
	 */
	JDBCFeatureStore featureStore;
	/**
	 * state of the feature source 
	 */
	JDBCState state;
	/**
	 * filter used to filter content.
	 */
	Filter filter;
	Filter preFilter,postFilter;
	
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

   public JDBCFeatureCollection( JDBCFeatureStore source, JDBCState state ) {
		this( source, state, null );
	}
	
	public JDBCFeatureCollection( JDBCFeatureStore source, JDBCState state, Filter filter ) {
		this.featureStore = source;
		this.dataStore = source.getDataStore();
		this.state = state;
		this.filter = filter;
		
		//add the feautre source listener
		source.addFeatureListener(listener);
		
		//split the filter
		if ( filter != null ) {
		    PostPreProcessFilterSplittingVisitor splitter = 
		        new PostPreProcessFilterSplittingVisitor( dataStore.getFilterCapabilities(), source.getSchema(), null );
		    filter.accept( splitter, null );
		    
		    preFilter = splitter.getFilterPre();
		    postFilter = splitter.getFilterPost();
		    
		}
	}
	
	/**
	 * @return The feature source the collection originates from.
	 */
	public JDBCFeatureStore getFeatureSource() {
		return featureStore;
	}
	
	/**
     * @return The data store the collection originates from.
     */
	public JDBCDataStore getDataStore() {
        return dataStore;
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
        try {
			return open( createFeatureIterator() );
		} 
		catch (Exception e) {
			throw new RuntimeException( e );
		}
    }
	
    public FeatureIterator writer() {
        try {
            return open( createFeatureWriter() );
        } 
        catch (Exception e) {
            throw new RuntimeException( e );
        }
    }
    
    public FeatureIterator inserter() {
        try {
            return open( createFeatureInserter() );
        } 
        catch (Exception e) {
            throw new RuntimeException( e );
        }
    }
    
    protected FeatureIterator open( FeatureIterator open ) {
        // keep track of the iterator
        this.open.add( open );
        return open;
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
        JDBCDataStore dataStore = featureStore.getDataStore();
        String sql = dataStore.selectSQL( featureStore.getSchema(), preFilter );
        JDBCDataStore.LOGGER.fine( sql );
        
        //create a statement and pass it off to the iterator
        Statement st = state.getConnection().createStatement();
    	st.execute( sql );
    	
    	FeatureIterator iterator = new JDBCFeatureIterator( st, featureStore.getSchema(), featureStore.getDataStore() );
    	
    	//if post filter, wrap it
    	if ( postFilter != null ) {
    	    iterator = new FilteringFeatureIterator( iterator, postFilter );
    	}
    	return iterator;
    }
    
    protected FeatureIterator createFeatureWriter() throws Exception {
        //build up a statement for the content
        JDBCDataStore dataStore = featureStore.getDataStore();
        String sql = dataStore.selectSQL( featureStore.getSchema(), preFilter );
        JDBCDataStore.LOGGER.fine( sql );
        
        //create a statement and pass it off to the iterator
        Statement st = state.getConnection().createStatement();
        st.execute( sql );
        
        FeatureIterator iterator = new JDBCFeatureWriter( st, featureStore.getSchema(), featureStore.getDataStore() );
        
        //if post filter, wrap it
        if ( postFilter != null ) {
            iterator = new FilteringFeatureIterator( iterator, postFilter );
        }
        return iterator;
    }
    
    protected FeatureIterator createFeatureInserter() throws Exception {
        //build up a statement for the content
        JDBCDataStore dataStore = featureStore.getDataStore();
        String sql = dataStore.selectSQL( featureStore.getSchema(), Filter.EXCLUDE );
        JDBCDataStore.LOGGER.fine( sql );
        
        //create a statement and pass it off to the iterator
        Statement st = state.getConnection().createStatement();
        st.execute( sql );
        
        FeatureIterator iterator = new JDBCFeatureInserter( st, featureStore.getSchema(), featureStore.getDataStore() );
        
        //TODO: should make sure that any inserted features satisfy the filter
        return iterator;
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

    public void accepts(org.opengis.feature.FeatureVisitor visitor,
            org.opengis.util.ProgressListener progress) throws IOException {
    }
    
    public SimpleFeatureType getSchema() {
		return featureStore.getSchema();
	}

    public ReferencedEnvelope getBounds() {
		
        try {
            if (postFilter != null && postFilter != Filter.INCLUDE ) {
                //calculate manually, dont use datastore optimization
                JDBCDataStore.LOGGER.fine( "Calculating bounds manually");
                
                ReferencedEnvelope bounds = 
                    new ReferencedEnvelope( getSchema().getCRS() );
                
                //grab an iterator, its already filtered for us
                Iterator i = iterator();
                try {
                    if( i.hasNext() ) {
                        SimpleFeature f = (SimpleFeature) i.next();
                        bounds.init( f.getBounds() );
                            
                        while ( i.hasNext() ) {
                            bounds.include( f.getBounds() );
                        }
                    }
                }
                finally {
                    close( i );
                }
                return bounds;

                //TODO: cache the bounds locally
            } 
            else if ( preFilter != null ) {
                //filter can be encoded, use optimization
                return dataStore.getBounds(featureStore.getSchema(), preFilter, state.getConnection() );
            }
            else {
                //bounds is for entire dataset, use the cached value if we can
                if (state.getBounds() == null) {
                    synchronized (state) {
                        if (state.getBounds() == null) {
                            state.setBounds(dataStore.getBounds(featureStore
                                    .getSchema(), null, state.getConnection()));
                        }
                    }
                }
                
                return state.getBounds();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

	public int size() {
		try {
            if (postFilter != null && postFilter != Filter.INCLUDE ) {
                //calculate manually, dont use datastore optimization
                JDBCDataStore.LOGGER.fine( "Calculating size manually");
                
                int count = 0;
                
                //grab an iterator, its already filtered for us
                Iterator i = iterator();
                try {
                    while( i.hasNext() ) {
                        i.next();
                        count++;
                    }
                }
                finally {
                    close(i);
                }
                
                return count;
                //TODO: cache the count locally
            } else if ( preFilter != null ) {
                //filter can be encoded, use optimization
                return dataStore.getCount(featureStore.getSchema(), preFilter, state.getConnection());
            }
            else {
                //bounds is for entire dataset, use the cached value if we can
                if (state.getCount() == -1) {
                    synchronized (state) {
                        if (state.getCount() == -1) {
                            state.setCount(dataStore.getCount(featureStore
                                    .getSchema(), null, state.getConnection()));
                        }
                    }
                }

                return state.getCount();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
	}
	
	public FeatureCollection subCollection(Filter filter) {
	    if ( this.filter != null ) {
	        //and the filters together
	        filter = dataStore.getFilterFactory().and(this.filter, filter );
	    }
	    
	    return new JDBCFeatureCollection( featureStore, state, filter );
	}
	
	public boolean isEmpty() {
		return size() == 0;
	}
	
	public boolean add(Object o) {
		return addAll( Collections.singletonList(o) );
    }

	public boolean addAll(Collection c) {
        try {
            dataStore.insert( c, featureStore.getSchema(), state.getConnection() );
            added( c );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return true;
    }
	
	void added( Collection features ) {
	    //update the cached values
	    if ( state.getCount() != -1 ) {
	        synchronized (state) {
                state.setCount(state.getCount()+features.size());
            }
	    }
	    
	    if ( state.getBounds() != null ) {
	        synchronized (state) {
	            for ( Iterator f = features.iterator(); f.hasNext(); ) {
	                SimpleFeature feature = (SimpleFeature) f.next();
	                state.getBounds().include( feature.getBounds() );
	            }
            }
	    }
	}
	
	public void clear() {
	    try {
	        if ( filter == null ) {
	            dataStore.delete( featureStore.getSchema(), null, state.getConnection() );
	            
	            //entire dataset deleted, update cache
	            state.setCount( 0 );
	            
	            ReferencedEnvelope bounds = new ReferencedEnvelope(getSchema().getCRS());
	            bounds.setToNull();
	            state.setBounds( bounds );
	        }
	        else {
	            if ( preFilter != null ) {
	                //delete what we can up front
	                dataStore.delete( featureStore.getSchema(), preFilter, state.getConnection() );    
	            }
	            
	            //delete any remaining
	            if ( postFilter != null ) {
	                //TODO: implement this, the feature iterator interface does not 
	                // implement remove()
	            }
	            
	            //reset cache
	            state.setCount( -1 );
	            state.setBounds( null );
	        }
	    } 
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

	public void update( AttributeDescriptor[] attributes, Object[] values ) {
	    try {
	        if ( filter == null ) {
                dataStore.update( featureStore.getSchema(), attributes, values, null, state.getConnection() );
            }
            else {
                if ( preFilter != null ) {
                    //delete what we can up front
                    dataStore.update( featureStore.getSchema(), attributes, values, preFilter, state.getConnection() );
                }
                
                //update any remaining
                if ( postFilter != null ) {
                    //TODO: implement this
                }    
            }
	        
	        //reset cache
	        //TODO: only do this if a geometric attribute changed
            state.setBounds( null );
	    }
	    catch(IOException e) {
	        throw new RuntimeException(e);
	    }
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

	public FeatureCollection sort(SortBy order) {
	    throw new UnsupportedOperationException();
    }

	public Object getAttribute(String name) {
        throw new UnsupportedOperationException();
    }

    public Object getAttribute(Name name) {
        throw new UnsupportedOperationException();
    }

    public Object getAttribute(int indedx) throws IndexOutOfBoundsException {
        throw new UnsupportedOperationException();
    }

    public int getAttributeCount() {
        throw new UnsupportedOperationException();
    }

    public List<Object> getAttributes() {
        throw new UnsupportedOperationException();
    }

    public Object getDefaultGeometry() {
        throw new UnsupportedOperationException();
    }

    public SimpleFeatureType getFeatureType() {
        throw new UnsupportedOperationException();
    }

    public SimpleFeatureType getType() {
        throw new UnsupportedOperationException();
    }

    public void setAttribute(String name, Object value) {
        throw new UnsupportedOperationException();
    }

    public void setAttribute(Name name, Object value) {
        throw new UnsupportedOperationException();
    }

    public void setAttribute(int index, Object value)
            throws IndexOutOfBoundsException {
        throw new UnsupportedOperationException();
    }

    public void setAttributes(List<Object> arg0) {
    }

    public void setAttributes(Object[] arg0) {
    }

    public void setDefaultGeometry(Object arg0) {
    }

    public GeometryAttribute getDefaultGeometryProperty() {
        return null;
    }

    public String getID() {
        return null;
    }

    public void setDefaultGeometryProperty(GeometryAttribute arg0) {
    }

    public Collection<Property> getProperties() {
        return null;
    }

    public Collection<Property> getProperties(Name arg0) {
        return null;
    }

    public Collection<Property> getProperties(String arg0) {
        return null;
    }

    public Property getProperty(Name arg0) {
        return null;
    }

    public Property getProperty(String arg0) {
        return null;
    }

    public Collection<? extends Property> getValue() {
        return null;
    }

    public void setValue(Collection<Property> arg0) {
    }

    public AttributeDescriptor getDescriptor() {
        return null;
    }

    public Name getName() {
        return null;
    }

    public Map<Object, Object> getUserData() {
        return null;
    }

    public boolean isNillable() {
        return false;
    }

    public void setValue(Object arg0) {
    }

}

