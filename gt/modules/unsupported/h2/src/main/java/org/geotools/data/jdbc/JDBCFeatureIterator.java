package org.geotools.data.jdbc;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.GeometryDescriptor;

import com.vividsolutions.jts.geom.Geometry;

public class JDBCFeatureIterator extends JDBCFeatureIteratorSupport {

	/**
	 * flag indicating if the iterator has another feature
	 */
	Boolean next;
	
	public JDBCFeatureIterator( Statement st, SimpleFeatureType featureType, JDBCDataStore dataStore ) {
		super( st, featureType, dataStore );
		
		try {
            rs.beforeFirst();
        } 
		catch (SQLException e) {
		    throw new RuntimeException( e );
        }
	}
	
	public boolean hasNext() {
	    if ( next == null ) {
	    	try {
    			next = Boolean.valueOf( rs.next() );
    		} 
    		catch (SQLException e) {
    			throw new RuntimeException( e );
    		}
	    }
		return next.booleanValue();
	}

	public SimpleFeature next() throws NoSuchElementException {
	    if ( next == null ) {
	        throw new IllegalStateException("Must call hasNext before calling next");
	    }
		
		//round up attributes
		List attributes = new ArrayList();
		for ( int i = 0; i < featureType.getAttributeCount(); i++ ) {
			AttributeDescriptor type = featureType.getAttribute( i );
			try {
				Object value = rs.getObject( type.getLocalName() );
				
				//is this a geometry?
				if ( type instanceof GeometryDescriptor ) {
				    GeometryDescriptor gatt = (GeometryDescriptor) type;
				    //if the value is not of type Geometry, try to decode it
				    if ( value != null && !( value instanceof Geometry ) ) {
				        Object decoded;
                        try {
                            decoded = dataStore.getSQLDialect().decodeGeometryValue(value, gatt);
                        } 
                        catch (IOException e) {
                            throw new RuntimeException( e );
                        }
                        
				        if ( decoded != null ) {
				            value = decoded;
				        }
				    }
				    
				}
				attributes.add( value );
			}
			catch( SQLException e ) {
				throw new RuntimeException( e );
			}
		}
		
		//find the primary key
		PrimaryKey pkey;
        try {
            pkey = dataStore.getPrimaryKey(featureType);
        } 
        catch (IOException e) {
            throw new RuntimeException( e );
        }
        
        //figure out the fid
		String fid;
		try {
			fid = pkey.encode( rs );
		} 
		catch (Exception e) {
			throw new RuntimeException( "Could not determine fid from primary key", e );
		} 
		
		//reset the next flag
		next = null;
		
		//create the feature
		try {
		    
		    return SimpleFeatureBuilder.build( featureType, attributes, fid );
		} 
		catch (IllegalAttributeException e) {
			throw new RuntimeException( e );
		}
	}

	public void remove() {
	    try {
            rs.deleteRow();
        } 
	    catch (SQLException e) {
	        throw new RuntimeException ( e );
        }
	}
	
}
