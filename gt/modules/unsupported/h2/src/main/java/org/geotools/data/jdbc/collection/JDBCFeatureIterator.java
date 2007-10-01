package org.geotools.data.jdbc.collection;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.geotools.data.jdbc.PrimaryKey;
import org.geotools.data.jdbc.collection.JDBCFeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;

public class JDBCFeatureIterator implements FeatureIterator {

	/**
	 * sql statement
	 */
	Statement st;
	/**
	 * collection which the iterator originated from
	 */
	JDBCFeatureCollection collection;
	
	public JDBCFeatureIterator( Statement st, JDBCFeatureCollection collection ) {
		this.st = st;
		this.collection = collection;
	}
	
	public boolean hasNext() {
		try {
			return st.getResultSet().next();
		} 
		catch (SQLException e) {
			throw new RuntimeException( e );
		}
	}

	public SimpleFeature next() throws NoSuchElementException {
		SimpleFeatureType featureType = collection.getSchema();
		
		//round up attributes
		List attributes = new ArrayList();
		for ( int i = 0; i < featureType.getAttributeCount(); i++ ) {
			AttributeDescriptor type = featureType.getAttribute( i );
			try {
				Object value = st.getResultSet().getObject( type.getLocalName() );
				if ( value != null ) {
					attributes.add( value );
				}
			}
			catch( SQLException e ) {
				//log
				attributes.add( null );
			}
		}
		
		//fid / primary key
		PrimaryKey pkey = collection.getFeatureSource().getPrimaryKey();
		String fid;
		try {
			fid = pkey.encode( st.getResultSet() );
		} 
		catch (Exception e) {
			throw new RuntimeException( "Could not determine fid from primary key", e );
		} 
		
		//create the feature
		try {
		    return SimpleFeatureBuilder.build( featureType, attributes, fid );
		} 
		catch (IllegalAttributeException e) {
			throw new RuntimeException( e );
		}
	}

	public void close() {
		if ( st != null ) {
			try {
				st.close();
			} 
			catch (SQLException e) {
				
			}
		}
	}

}
