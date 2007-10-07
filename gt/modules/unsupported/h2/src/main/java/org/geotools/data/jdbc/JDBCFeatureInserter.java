package org.geotools.data.jdbc;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.NoSuchElementException;

import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

public class JDBCFeatureInserter extends JDBCFeatureIteratorSupport {

    ResultSetFeature last;
    
    public JDBCFeatureInserter( Statement st, SimpleFeatureType featureType, JDBCDataStore dataStore ) {
        super( st, featureType, dataStore );
    }
    
    public boolean hasNext() {
        return true;
    }

    public SimpleFeature next() throws NoSuchElementException {
        try {
            if ( last != null ) {
                st.getResultSet().insertRow();
                last.close();
            }
            
            st.getResultSet().moveToInsertRow();
            
            last = new ResultSetFeature( st.getResultSet() );
        } 
        catch (SQLException e) {
            throw new RuntimeException( e );
        }
        
        return last;
    }
    
    public void close() {
        //ensure last row is written
        //TODO: make this a more robust check
        next();
        super.close();
    }
    
}
