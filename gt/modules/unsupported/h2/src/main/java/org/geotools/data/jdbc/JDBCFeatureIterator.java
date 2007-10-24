package org.geotools.data.jdbc;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.logging.Level;

import org.geotools.factory.Hints;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.util.Converters;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.GeometryDescriptor;

import com.vividsolutions.jts.geom.CoordinateSequenceFactory;
import com.vividsolutions.jts.geom.GeometryFactory;

/**
 * Iterator for read only access to a dataset.
 * 
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public class JDBCFeatureIterator extends JDBCFeatureIteratorSupport {

	/**
	 * flag indicating if the iterator has another feature
	 */
	Boolean next;
	/**
     * geometry factory used to create geometry objects
	 */
	GeometryFactory geometryFactory;
	
	public JDBCFeatureIterator( Statement st, SimpleFeatureType featureType, JDBCFeatureCollection collection ) {
		super( st, featureType, collection );
		
		try {
            rs.beforeFirst();
        } 
		catch (SQLException e) {
		    throw new RuntimeException( e );
        }
		
		//set a geometry factory, use the hints on the collection first
		geometryFactory = (GeometryFactory) collection.getHints().get( Hints.JTS_GEOMETRY_FACTORY );
		if ( geometryFactory == null ) {
		    //look for a coordinate sequence factory
		    CoordinateSequenceFactory csFactory = 
		        (CoordinateSequenceFactory) collection.getHints().get( Hints.JTS_COORDINATE_SEQUENCE_FACTORY );
		    if ( csFactory != null ) {
		        geometryFactory = new GeometryFactory( csFactory);
		    }
		}
		
		if ( geometryFactory == null ) {
		    //use the datastore provided one
	        geometryFactory = dataStore.getGeometryFactory();    
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
				    try {
				        value = dataStore.getSQLDialect()
			               .decodeGeometryValue(gatt,rs,type.getLocalName(),geometryFactory);
				    }
				    catch (IOException e) {
                        throw new RuntimeException( e );
                    }
				    
//				    //if the value is not of type Geometry, try to decode it
//				    if ( value != null && !( value instanceof Geometry ) ) {
//				        Object decoded;
//                        try {
//                            decoded = 
//                        } 
//                    
//                        
//				        if ( decoded != null ) {
//				            value = decoded;
//				        }
//				    }
				}
				
				//if the value is not of the type of the binding, try to convert
				Class binding = type.getType().getBinding();
				if ( value != null && 
				        !(type.getType().getBinding().isAssignableFrom(binding))) {
				    if ( JDBCDataStore.LOGGER.isLoggable(Level.FINER)) {
				        String msg = value + " is not of type " + 
				            binding.getName() + ", attempting conversion";
				        JDBCDataStore.LOGGER.finer( msg );
				    }
				    
				    Object converted = Converters.convert(value, binding);
				    if ( converted != null ) {
				        value = converted;
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
			
			//wrap the fid in the type name
			fid = featureType.getTypeName() + "." + fid;
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
