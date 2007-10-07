package org.geotools.data.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.geotools.feature.FeatureIterator;
import org.opengis.feature.GeometryAttribute;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.Name;
import org.opengis.geometry.BoundingBox;

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
        
        try {
            st.getResultSet().beforeFirst();
        } 
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    
    public boolean hasNext() {
        if ( next == null ) {
            try {
                if ( !st.getResultSet().isBeforeFirst() ) {
                    //do the write
                    st.getResultSet().updateRow();
                }
                
                next = Boolean.valueOf( st.getResultSet().next() );
            } 
            catch (SQLException e) {
                throw new RuntimeException( e );
            }
        }
        return next.booleanValue();
    }

    public SimpleFeature next() {
        if ( next == null ) {
            throw new IllegalStateException("Must call hasNext before calling next");
        }
        
        if ( last != null ) {
            last.close();
        }
        
        try {
            last = new ResultSetFeature( st.getResultSet() );
        } 
        catch (SQLException e) {
            throw new RuntimeException( e );
        }
        
        //reset next flag
        next = null;
        
        return last;
    }

    public void remove() {
        try {
            st.getResultSet().deleteRow();
        } 
        catch (SQLException e) {
            throw new RuntimeException( e );
        }
    }
    
}
