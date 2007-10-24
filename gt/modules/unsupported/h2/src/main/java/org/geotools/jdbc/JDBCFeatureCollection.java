package org.geotools.jdbc;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.logging.Level;

import org.geotools.data.AbstractContentFeatureCollection;
import org.geotools.data.FilteringFeatureIterator;
import org.geotools.data.store.ContentFeatureIterator;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.filter.visitor.PostPreProcessFilterSplittingVisitor;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.filter.Filter;

/**
 * Contains some common state needed for all JDBCFeatureCollections.
 * <p>
 * Please note the result does *not* implement FeatureCollection itself (
 * as you may want to choose between FeatureList and FeatureCollection).
 * 
 * @author Jody Garnett, Refractions Research
 * @author Justin Deoliveira, The Open Planning Project
 */
public class JDBCFeatureCollection extends AbstractContentFeatureCollection {

   /**
	 * filter used to filter content.
	 */
	Filter filter;
	Filter preFilter,postFilter;
	
	public JDBCFeatureCollection( JDBCFeatureStore source, JDBCState state ) {
		this( source, state, null );
	}
	
	public JDBCFeatureCollection( JDBCFeatureStore source, JDBCState state, Filter filter ) {
	    super( source, state );
		
		this.filter = filter;
		
		//split the filter
		if ( filter != null ) {
		    PostPreProcessFilterSplittingVisitor splitter = 
		        new PostPreProcessFilterSplittingVisitor( getDataStore().getFilterCapabilities(), source.getSchema(), null );
		    filter.accept( splitter, null );
		    
		    preFilter = splitter.getFilterPre();
		    postFilter = splitter.getFilterPost();
		}
		
		//ensure connection is set
		getDataStore().getConnection(state);
	}
	
	/**
	 * @return The feature source the collection originates from.
	 */
	public JDBCFeatureStore getFeatureSource() {
		return (JDBCFeatureStore) featureSource;
	}
	
	/**
     * @return The data store the collection originates from.
     */
	public JDBCDataStore getDataStore() {
        return (JDBCDataStore) dataStore;
    }
	
	/**
	 * @return The state the collection is working from.
	 */
	public JDBCState getState() {
        return (JDBCState) state;
    }
	
    protected FeatureIterator createFeatureIterator() throws Exception {
        //build up a statement for the content
        JDBCDataStore dataStore = getFeatureSource().getDataStore();
        String sql = dataStore.selectSQL( getFeatureSource().getSchema(), preFilter );
        JDBCDataStore.LOGGER.fine( sql );
        
        //create a statement and pass it off to the iterator
        Statement st = getState().getConnection()
            .createStatement(ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
    	st.execute( sql );
    	
    	FeatureIterator iterator = new JDBCFeatureIterator( st, getFeatureSource().getSchema(), this );
    	
    	//if post filter, wrap it
    	if ( postFilter != null ) {
    	    iterator = new FilteringFeatureIterator( iterator, postFilter );
    	}
    	return iterator;
    }
    
    protected FeatureIterator createFeatureWriter() throws Exception {
        //build up a statement for the content
        JDBCDataStore dataStore = getDataStore();
        String sql = dataStore.selectSQL( getFeatureSource().getSchema(), preFilter );
        JDBCDataStore.LOGGER.fine( sql );
        
        //create a statement and pass it off to the iterator
        Statement st = getState().getConnection().createStatement();
        st.execute( sql );
        
        ContentFeatureIterator iterator = new JDBCFeatureWriter( st, getFeatureSource().getSchema(), this );
        
        //if post filter, wrap it
        if ( postFilter != null ) {
            iterator = new FilteringFeatureIterator( iterator, postFilter );
        }
        return iterator;
    }
    
    protected FeatureIterator createFeatureInserter() throws Exception {
        //build up a statement for the content
        JDBCDataStore dataStore = getDataStore();
        String sql = dataStore.selectSQL( getFeatureSource().getSchema(), Filter.EXCLUDE );
        JDBCDataStore.LOGGER.fine( sql );
        
        //create a statement and pass it off to the iterator
        Statement st = getState().getConnection().createStatement();
        st.execute( sql );
        
        FeatureIterator iterator = new JDBCFeatureInserter( st, getFeatureSource().getSchema(), this );
        
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
    
   

    public ReferencedEnvelope getBounds() {
		
        JDBCDataStore dataStore = getDataStore();
        
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
                return dataStore.getBounds( getSchema(), preFilter, getState().getConnection() );
            }
            else {
                //bounds is for entire dataset, use the cached value if we can
                if (state.getBounds() == null) {
                    synchronized (state) {
                        if (state.getBounds() == null) {
                            state.setBounds(dataStore.getBounds(getSchema(), null, getState().getConnection()));
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
	    JDBCDataStore dataStore = getDataStore();
	    
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
                return dataStore.getCount(getSchema(), preFilter, getState().getConnection());
            }
            else {
                //bounds is for entire dataset, use the cached value if we can
                if (state.getCount() == -1) {
                    synchronized (state) {
                        if (state.getCount() == -1) {
                            state.setCount(dataStore.getCount(getSchema(), null, getState().getConnection()));
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
	    
	    return new JDBCFeatureCollection( getFeatureSource(), getState(), filter );
	}
	
	public boolean isEmpty() {
		return size() == 0;
	}
	
	public boolean add(Object o) {
		return addAll( Collections.singletonList(o) );
    }

	public boolean addAll(Collection c) {
        try {
            getDataStore().insert( c, getSchema(), getState().getConnection() );
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
	    JDBCDataStore dataStore = getDataStore();
	    try {
	        if ( filter == null ) {
	            dataStore.delete( getSchema(), Filter.INCLUDE, getState().getConnection() );
	            
	            //entire dataset deleted, update cache
	            state.setCount( 0 );
	            
	            ReferencedEnvelope bounds = new ReferencedEnvelope(getSchema().getCRS());
	            bounds.setToNull();
	            state.setBounds( bounds );
	        }
	        else {
	            if ( preFilter != null ) {
	                //delete what we can up front
	                dataStore.delete( getSchema(), preFilter, getState().getConnection() );    
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
	    JDBCDataStore dataStore = getDataStore();
	    try {
	        if ( filter == null ) {
                dataStore.update( getSchema(), attributes, values, null, getState().getConnection() );
            }
            else {
                if ( preFilter != null ) {
                    //delete what we can up front
                    dataStore.update( getSchema(), attributes, values, preFilter, getState().getConnection() );
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
}

