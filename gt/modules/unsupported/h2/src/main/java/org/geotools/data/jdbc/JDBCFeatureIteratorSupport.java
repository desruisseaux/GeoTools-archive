package org.geotools.data.jdbc;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashMap;
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

public abstract class JDBCFeatureIteratorSupport implements FeatureIterator {

    /**
     * the statement + result set
     */
    protected Statement st;
    protected ResultSet rs;
    
    /**
     * feature type
     */
    protected SimpleFeatureType featureType;
    /**
     * datastore
     */
    protected JDBCDataStore dataStore;
    
    protected JDBCFeatureIteratorSupport( Statement st, SimpleFeatureType featureType, JDBCDataStore dataStore ) {
        this.st = st;
        this.featureType = featureType;
        this.dataStore = dataStore;
        
        try {
            rs = st.getResultSet();
            
            //move to before first to mark as "initialized"
            rs.beforeFirst();
        } 
        catch (SQLException e) {
            throw new RuntimeException( e );
        }
    }
    
        
    public void close() {
        dataStore.closeSafe( rs );
        dataStore.closeSafe( st );
        rs = null;
        st = null;
    }

    /**
     * Feature wrapper around a result set.
     */
    protected class ResultSetFeature implements SimpleFeature {

        /** 
         * result set 
         */
        ResultSet rs;
        /** 
         * updated values 
         * */
        Object[] values;
        /** 
         * dirty flags 
         */
        boolean[] dirty;
        /**
         * name index
         */
        HashMap<String, Integer> index;
        /**
         * user data 
         */
        HashMap<Object,Object> userData = new HashMap<Object, Object>();
        
        ResultSetFeature( ResultSet rs ) throws SQLException {
            this.rs = rs;
            
            ResultSetMetaData md = rs.getMetaData();
            
            //set up values
            values = new Object[ md.getColumnCount() ];
            dirty = new boolean[ values.length ];
            
            //set up name lookup
            index = new HashMap<String,Integer>();
            for ( int i = 0; i < values.length; i++ ) {
                index.put( md.getColumnName(i+1), i );
            }
        }
        
        public void init() {
            for ( int i = 0; i < values.length; i++ ) {
                values[i] = null;
                dirty[i] = false;
            }
        }
        public SimpleFeatureType getFeatureType() {
            return featureType;
        }

        public SimpleFeatureType getType() {
            return featureType;
        }
        
        public String getID() {
            return null;
        }
        
        public Object getAttribute(String name) {
            return values[ index.get( name ) ];
        }

        public Object getAttribute(Name name) {
            return getAttribute( name.getLocalPart() );
        }

        public Object getAttribute(int index) throws IndexOutOfBoundsException {
            return values[ index ];
        }

        public void setAttribute(String name, Object value) {
            JDBCDataStore.LOGGER.fine( "Setting " + name + " to " + value );
            int i = index.get( name );
            setAttribute( i, value );
        }

        public void setAttribute(Name name, Object value) {
            setAttribute( name.getLocalPart(), value );
        }

        public void setAttribute(int index, Object value)
                throws IndexOutOfBoundsException {
            JDBCDataStore.LOGGER.fine( "Setting " + index + " to " + value );
            values[index] = value;
            dirty[index] = true;
        }
        
        public int getAttributeCount() {
            return values.length;
        }

        public boolean isDirty( int index ) {
            return dirty[ index ];
        }
        
        public boolean isDirrty( String name ) {
            return isDirty( index.get( name ) );
        }
        
        public void close() {
            rs = null;
        }
        
        public List<Object> getAttributes() {
            throw new UnsupportedOperationException();
        }

        public Object getDefaultGeometry() {
            throw new UnsupportedOperationException();
        }

        public void setAttributes(List<Object> values) {
            throw new UnsupportedOperationException();
        }

        public void setAttributes(Object[] object) {
            throw new UnsupportedOperationException();
        }

        public void setDefaultGeometry(Object defaultGeometry) {
            throw new UnsupportedOperationException();
        }

        public BoundingBox getBounds() {
            throw new UnsupportedOperationException();
        }

        public GeometryAttribute getDefaultGeometryProperty() {
            throw new UnsupportedOperationException();
        }

        public void setDefaultGeometryProperty(GeometryAttribute defaultGeometry) {
            throw new UnsupportedOperationException();
        }

        public Collection<Property> getProperties() {
            throw new UnsupportedOperationException();
        }

        public Collection<Property> getProperties(Name name) {
            throw new UnsupportedOperationException();
        }

        public Collection<Property> getProperties(String name) {
            throw new UnsupportedOperationException();
        }

        public Property getProperty(Name name) {
            throw new UnsupportedOperationException();
        }

        public Property getProperty(String name) {
            throw new UnsupportedOperationException();
        }

        public Collection<? extends Property> getValue() {
            throw new UnsupportedOperationException();
        }

        public void setValue(Collection<Property> value) {
            throw new UnsupportedOperationException();
        }

        public AttributeDescriptor getDescriptor() {
            throw new UnsupportedOperationException();
        }

        public Name getName() {
            throw new UnsupportedOperationException();
        }

        public Map<Object, Object> getUserData() {
            return userData;
        }

        public boolean isNillable() {
            throw new UnsupportedOperationException();
        }

        public void setValue(Object value) {
            throw new UnsupportedOperationException();
        }
        
    }

}
