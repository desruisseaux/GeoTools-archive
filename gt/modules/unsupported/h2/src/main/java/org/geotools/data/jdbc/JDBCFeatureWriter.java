package org.geotools.data.jdbc;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.geotools.data.store.ContentFeatureIterator;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.filter.Id;

/**
 * Iterator which provides update access to the underlying dataset.
 * <p>
 * When a call to {@link #next()} occurs, the feature returned is "live" in that
 * all attributes set are tracked, and written to the underlying database with 
 * the next call to {@link next()}. Example:
 * <pre>
 *   <code>
 *   FeatureCollection collection = ...;
 *   FeatureIterator writer = collection.writer();
 *   
 *   try {
 *      while( writer.hasNext() ) {
 *         //grab the next feature (this will write the previous feature)
 *         SimpleFeature feature = writer.next();
 *         
 *         //set the values
 *         feature.setAttribute( 0, ... );
 *         feature.setAttribute( 1, ... );
 *         ...
 *      }
 *   }
 *   finally {
 *     //always remember to close the iterator
 *     collection.close( writer );
 *   }
 *   </code>
 * </pre>
 * 
 * </p>
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public class JDBCFeatureWriter extends JDBCFeatureIteratorSupport
    implements ContentFeatureIterator {

    /**
     * flag indicating if the iterator has another feature
     */
    Boolean next;
    /**
     * the last feature read 
     */
    ResultSetFeature last;
    /**
     * JDBCFeature
     */
    JDBCFeatureInserter inserter;
    
    public JDBCFeatureWriter( Statement st, SimpleFeatureType featureType, JDBCFeatureCollection collection ) {
        super( st, featureType, collection );
    }
    
    public boolean hasNext() {
        
        if ( next == null ) {
            try {
                if ( last == null ) {
                    //first call
                    last = new ResultSetFeature( rs );
                }

                next = Boolean.valueOf( rs.next() );
            } 
            catch (Exception e) {
                throw new RuntimeException( e );
            }
        }
        return next.booleanValue();
    }

    public SimpleFeature next() {
        if ( next == null ) {
            throw new IllegalStateException("Must call hasNext before calling next");
        }
        
        if ( !next.booleanValue() ) {
            //hasNext() == false, switch to insert mode
            if ( inserter == null ) {
                inserter = new JDBCFeatureInserter(st,featureType,collection);
            }
            return inserter.next();
        }
        
        //reset next flag
        next = null;
            
        try {
            last.init();
        } 
        catch (Exception e) {
            //TODO: remove when this method can throw exception
            throw new RuntimeException( e );
        }
        return last;
    }

    public void remove() throws IOException {
        if ( inserter != null ) {
            inserter.remove();
            return;
        }
        
        try {
            dataStore.delete(featureType, last.getID(), st.getConnection());
        } 
        catch (SQLException e) {
            throw (IOException) new IOException().initCause(e);
        }
    }
    
    public void write() throws IOException {
        if ( inserter != null ) {
            inserter.write();
            return;
        }
        
        try {
            //figure out what the fid is
            PrimaryKey key = dataStore.getPrimaryKey( featureType );
            String fid = key.encode( rs );
            
            Id filter = dataStore.getFilterFactory().id(
                Collections.singleton( dataStore.getFilterFactory().featureId( fid ) )
            );
            
            //figure out which attributes changed
            List<AttributeDescriptor> changed = new ArrayList<AttributeDescriptor>();
            List<Object> values = new ArrayList<Object>();
            
            for ( AttributeDescriptor att : featureType.getAttributes() ) {
                if ( last.isDirrty( att.getLocalName() ) ) {
                    changed.add( att );
                    values.add( last.getAttribute( att.getLocalName() ) );
                }
            }
            
            //do the write
            dataStore.update(featureType, changed, values, filter, st.getConnection());
        }   
        catch (Exception e) {
            throw (IOException) new IOException().initCause(e);
        }
    }
}
