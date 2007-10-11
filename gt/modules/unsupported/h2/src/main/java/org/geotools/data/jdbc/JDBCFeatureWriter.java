package org.geotools.data.jdbc;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.filter.Id;

public class JDBCFeatureWriter extends JDBCFeatureIteratorSupport {

    /**
     * flag indicating if the iterator has another feature
     */
    Boolean next;
    /**
     * the last feature read 
     */
    ResultSetFeature last;
    
    public JDBCFeatureWriter( Statement st, SimpleFeatureType featureType, JDBCDataStore dataStore ) {
        super( st, featureType, dataStore );
        
    }
    
    public boolean hasNext() {
        if ( next == null ) {
            try {
                if ( last == null ) {
                    //first call
                    last = new ResultSetFeature( rs );
                }
                else {
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
        
        //reset next flag
        next = null;
        
        last.init();
        return last;
    }

    public void remove() {
        try {
            rs.deleteRow();
        } 
        catch (SQLException e) {
            throw new RuntimeException( e );
        }
    }
    
}
