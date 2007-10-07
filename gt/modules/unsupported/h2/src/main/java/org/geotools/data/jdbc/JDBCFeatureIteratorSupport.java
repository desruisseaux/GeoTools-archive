package org.geotools.data.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
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
     * sql statement
     */
    Statement st;
    /**
     * feature type
     */
    SimpleFeatureType featureType;
    /**
     * datastore
     */
    JDBCDataStore dataStore;
    
    protected JDBCFeatureIteratorSupport( Statement st, SimpleFeatureType featureType, JDBCDataStore dataStore ) {
        this.st = st;
        this.featureType = featureType;
        this.dataStore = dataStore;
    }
    
        
    public void close() {
        dataStore.closeSafe( st );
        st = null;
    }

    protected class ResultSetFeature implements SimpleFeature {

        ResultSet rs;
        
        ResultSetFeature( ResultSet rs ) {
            this.rs = rs;
        }
        
        public SimpleFeatureType getFeatureType() {
            return featureType;
        }

        public SimpleFeatureType getType() {
            return featureType;
        }
        
        public Object getAttribute(String name) {
            try {
                return rs.getObject(name);
            } 
            catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        public Object getAttribute(Name name) {
            return getAttribute( name.getLocalPart() );
        }

        public Object getAttribute(int index) throws IndexOutOfBoundsException {
            try {
                return rs.getObject(index);
            } 
            catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        public void setAttribute(String name, Object value) {
            try {
                JDBCDataStore.LOGGER.fine( "Setting " + name + " to " + value );
                rs.updateObject( name, value );
            } 
            catch (SQLException e) {
                throw new RuntimeException( e );
            }
        }

        public void setAttribute(Name name, Object value) {
            setAttribute( name.getLocalPart(), value );
        }

        public void setAttribute(int index, Object value)
                throws IndexOutOfBoundsException {
            try {
                JDBCDataStore.LOGGER.fine( "Setting " + index + " to " + value );
                rs.updateObject( index, value );
            } 
            catch (SQLException e) {
                throw new RuntimeException( e );
            }
        }
        
        public int getAttributeCount() {
            //TODO: figure out exactly how many attributes there are
            return featureType.getAttributeCount();
        }

        public void close() {
            rs = null;
        }
        
        public List<Object> getAttributes() {
            return null;
        }

        public Object getDefaultGeometry() {
            return null;
        }

        public void setAttributes(List<Object> arg0) {
        }

        public void setAttributes(Object[] arg0) {
        }

        public void setDefaultGeometry(Object arg0) {
        }

        public BoundingBox getBounds() {
            return null;
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

}
