package org.geotools.jdbc;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.logging.Level;

import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureReader;
import org.geotools.factory.Hints;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.util.Converters;
import org.opengis.feature.Association;
import org.opengis.feature.FeatureFactory;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AssociationDescriptor;
import org.opengis.feature.type.AssociationType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.FeatureTypeFactory;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.Id;

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
	/**
	 * feature builder
	 */
	SimpleFeatureBuilder builder;
	
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
		
		builder = new SimpleFeatureBuilder( featureType );
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
		//List attributes = new ArrayList();
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
				}
				
				//is this an association?
				if ( collection.getDataStore().isForeignKeyAware() && 
			        Association.class.equals( type.getType().getBinding() ) && 
			        value != null ) {
				    
				    //get the associated feature type name
                    String associatedTypeName = 
                        (String) type.getUserData().get( "jdbc.associatedTypeName" );
                    SimpleFeatureType associatedType;
                    try {
                        associatedType = dataStore.getSchema(associatedTypeName);
                    } 
                    catch (IOException e) {
                        throw new RuntimeException( e );
                    }
                    
				    //set the referenced id + typeName as user data
				    builder.userData( "gml:id", value.toString() );
				    builder.userData( "gml:featureTypeName", associatedTypeName );

				    //create an association
                    FeatureTypeFactory tf = dataStore.getFeatureTypeFactory();
                    AssociationType associationType = tf.createAssociationType(
                        type.getName(), associatedType, false, Collections.EMPTY_LIST, null, null
                    );
                    AssociationDescriptor associationDescriptor = tf.createAssociationDescriptor(
                        associationType, type.getName(), 1, 1, true
                    );
                    
                    FeatureFactory f = dataStore.getFeatureFactory();
                    Association association = f.createAssociation(null, associationDescriptor);
                    association.getUserData().put( "gml:id", value.toString() );
                    
                    //check for the xlinkTraversalDepth hint, if not > 0 dont 
                    // resolve the associated feature
				    Integer depth = 
				        (Integer) collection.getHints().get( Hints.ASSOCIATION_TRAVERSAL_DEPTH );
				    if ( depth == null ) {
				        depth = new Integer(0);
				    }
				    if ( depth.intValue() > 0 ) {
				        //use the value as an the identifier in a query against the
	                    // referenced type
	                    DefaultQuery query = new DefaultQuery( associatedTypeName );

	                    Hints hints = new Hints(Hints.ASSOCIATION_TRAVERSAL_DEPTH, new Integer( depth.intValue()-1) );
	                    query.setHints( hints );
	                
	                    FilterFactory ff = collection.getDataStore().getFilterFactory();
	                    Id filter = ff.id( Collections.singleton(ff.featureId(value.toString())));
	                    query.setFilter( filter );
	                    
	                    try {
	                        //grab a reader and get the feature, there should only 
	                        // be one
	                        FeatureReader r = 
	                            collection.getDataStore().getFeatureReader(query, collection.getState().getTransaction() );
	                        try {
	                            r.hasNext();
	                            SimpleFeature associated = r.next();
	                            association.setValue(associated);
	                        }
	                        finally {
	                            r.close();
	                        }
	                        
	                    } catch (IOException e) {
	                        throw new RuntimeException( e );
	                    }
	                }
				    
	                //set the actual value to be the association				    
				    value = association;
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
				
				builder.add( value );
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
		    return builder.buildFeature( fid );
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
