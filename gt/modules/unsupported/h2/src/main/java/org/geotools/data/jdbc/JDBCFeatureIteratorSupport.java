package org.geotools.data.jdbc;

import java.io.IOException;
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

/**
 * Base class for jdbc based iterators.
 * <p>
 * JDBC based iterators are wrapped directly around a {@link ResultSet} object.
 * </p>
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
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
     * collection iterator originated from
     */
    protected JDBCFeatureCollection collection;
    protected JDBCDataStore dataStore;
    
    protected JDBCFeatureIteratorSupport( Statement st, SimpleFeatureType featureType, JDBCFeatureCollection collection ) {
        this.st = st;
        this.featureType = featureType;
        this.collection = collection;
        dataStore = collection.getDataStore();
        
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
         * fid
         */
        String fid;
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
        
        ResultSetFeature( ResultSet rs ) throws SQLException, IOException {
            this.rs = rs;
            
            //get the result set metadata
            ResultSetMetaData md = rs.getMetaData();
            
            //get the primary key, ensure its not contained in the values
            PrimaryKey key = dataStore.getPrimaryKey(featureType);
            int count = md.getColumnCount();
            for ( int i = 0; i < md.getColumnCount(); i++ ) {
                if ( key.getColumnName().equals( md.getColumnName(i+1)) ){
                    count--;
                }
            }
            //set up values
            values = new Object[ count ];
            dirty = new boolean[ values.length ];
            
            //set up name lookup
            index = new HashMap<String,Integer>();
            int offset = 0;
            for ( int i = 0; i < md.getColumnCount(); i++ ) {
                if ( key.getColumnName().equals( md.getColumnName(i+1)) ){
                    offset = 1;
                    continue;
                }
                
                index.put( md.getColumnName(i+1), i-offset );
            }
        }
        
        public void init( String fid ) throws Exception {
            //clear values
            for ( int i = 0; i < values.length; i++ ) {
                values[i] = null;
                dirty[i] = false;
            }
            
            this.fid = fid;
        }
        
        public void init() throws Exception {
            //get fid
            PrimaryKey pkey = dataStore.getPrimaryKey(featureType);
            
            //TODO: factory fid prefixing out
            init( featureType.getTypeName() + "." + pkey.encode(rs) );
        }
        
        public SimpleFeatureType getFeatureType() {
            return featureType;
        }

        public SimpleFeatureType getType() {
            return featureType;
        }
        
        public String getID() {
            return fid;
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
        
        public void setAttributes(List<Object> values) {
            for ( int i = 0; i < values.size(); i++ ) {
                setAttribute( i, values.get( i ) );
            }
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
