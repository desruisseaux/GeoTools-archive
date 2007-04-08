package org.geotools.data.h2;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Iterator;

import org.geotools.data.DefaultQuery;
import org.geotools.data.Query;
import org.geotools.data.store.DataFeatureCollection;
import org.geotools.feature.AttributeType;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureType;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.filter.Filter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

/**
 * H2 FeatureCollection which represents access to an entire table.
 * 
 * @author Justin Deoliveira, The Open Planning Project, jdeolive@openplans.org
 *
 */
public class H2FeatureCollectionAll extends DataFeatureCollection {

	/**
	 * state about table / feature type
	 */
	H2ContentState state;
	
	public H2FeatureCollectionAll( H2ContentState state ) {
		this.state = state;
	}
	
	public boolean add(Object object) {
		Feature feature = (Feature) object;
		
		//build up the names, values to insert
		String[] names = new String[ feature.getNumberOfAttributes() ];
		Object[] values = new Object[ names.length ];
		
		for ( int i = 0; i < names.length; i++ ) {
			names[ i ] = feature.getFeatureType().getAttributeType( i ).getName();
			values[ i ] = feature.getAttribute( i );
		}

		//build statement "INSERT INTO <featureType> ...' 
		H2SQLBuilder sqlBuilder = 
			state.getDataStore().createSQLBuilder( state );  
		String sql = sqlBuilder.insert(names, values);
		
		try {
			Connection conn = state.getDataStore().connection();
			try {
				Statement st = conn.createStatement();
				st.execute( sql );
				
				st.close();
				return true;
			}
			finally {
				conn.close();
			}
		} 
		catch (SQLException e) {
			throw new RuntimeException( e );
		}
	}
	
	public boolean addAll(Collection collection) {
		//TODO: optimize this with a batch statement!!
		
		for ( Iterator i = collection.iterator(); i.hasNext(); ) {
			add( i.next() );
		}
		
		return true;
	}
	
	public Envelope getBounds() {
		//first check the state cache
		if ( state.get( "bounds" ) == null ) {
			//calculate it
			synchronized ( state ) {
				if ( state.get( "bounds" ) == null ) {
					
					try {

						FeatureType featureType = state.featureType();
						if ( featureType.getDefaultGeometry() == null ) {
							//no geometry, return null boundsw
							ReferencedEnvelope bounds = 
								new ReferencedEnvelope( (CoordinateReferenceSystem) null );
							bounds.setToNull();
							return bounds;
						}
						
						//get the crs
						CoordinateReferenceSystem crs = null;
						if ( featureType.getDefaultGeometry() != null ) {
							crs = featureType.getDefaultGeometry().getCoordinateSystem();
						}
						
						ReferencedEnvelope bounds = new ReferencedEnvelope( crs );
						
						//build statement "SELECT <geometry> FROM <featureType>' 
						H2SQLBuilder sqlBuilder = 
							state.getDataStore().createSQLBuilder( state );  
						sqlBuilder.select( 
							new String[]{ featureType.getDefaultGeometry().getName() } 
						);
						sqlBuilder.from();
						String sql = sqlBuilder.getSQL().toString();
						
						Connection conn = state.getDataStore().connection();
						try {
							Statement st = conn.createStatement();
							ResultSet rs = st.executeQuery( sql );
							
							if ( rs.next() ) {
								Geometry geometry = (Geometry) rs.getObject( 1 );
								bounds.init( geometry.getEnvelopeInternal() );
								
								while( rs.next() ) {
									geometry = (Geometry) rs.getObject( 1 );
									bounds.expandToInclude( geometry.getEnvelopeInternal() );
								}
							}
							else {
								bounds.setToNull();
							}
							
							rs.close();
							st.close();
						}
						finally {
							conn.close();
						}
						
						//throw result in cache
						state.put( "bounds", bounds );
					} 
					
					catch (Exception e) {
						throw new RuntimeException( e );
					}
				}
			}
		}
		
		return (Envelope) state.get( "bounds" );
	}


	public int getCount() throws IOException {
		//is this cached
    	if ( state.get( "count" ) == null ) {
    		//calculate it
    		synchronized ( state ) {
    		
    			if ( state.get( "count" ) == null ) {
    			
    				try {
						//get all the data from the table
						H2SQLBuilder sqlBuilder = 
							state.getDataStore().createSQLBuilder( state );
						String sql = sqlBuilder.count( Filter.INCLUDE );
						
						Connection conn = state.getDataStore().connection();
						try {
							Statement st = conn.createStatement();
							ResultSet rs = st.executeQuery( sql );
							
							rs.next();
							int count = rs.getInt( 1 );
							
							state.put( "count", new Integer( count ) );
							
							st.close();
							rs.close();
						}
						finally {
							conn.close();
						}
					} 
    				catch (SQLException e) {
    					throw (IOException) new IOException().initCause( e );
					}
        		
    			}
    		}
    	}
    	
    	return ((Integer) state.get( "count" )).intValue();
	}


	public FeatureType getSchema() {
		try {
			return state.featureType();
		} 
		catch (IOException e) {
			throw new RuntimeException( e );
		}
	}
	
	protected Iterator openIterator() throws IOException {
		try {
			return new IteratorAll();
		} 
		catch (SQLException e) {
			throw (IOException) new IOException().initCause( e );
		}
	}
	
	protected void closeIterator(Iterator close) throws IOException {
		try {
			((IteratorAll) close).close();
		} 
		catch (SQLException e) {
			throw (IOException) new IOException().initCause( e );
		}
		
		super.closeIterator( close );
	}
	
	class IteratorAll implements java.util.Iterator {

		Connection connection;
		Statement st;
		ResultSet rs;
		
		IteratorAll() throws SQLException {
			//grab a connection
			connection = state.getDataStore().getConnectionPoolDataSource()
				.getPooledConnection().getConnection();
			
			//get all the data from the table
			H2SQLBuilder sqlBuilder = 
				state.getDataStore().createSQLBuilder( state );
			String sql = sqlBuilder.select( Query.ALL );
			
			//get the result set
			st = connection.createStatement();
			rs = st.executeQuery( sql );
		}
		
		public boolean hasNext() {
			try {
				return rs.next();
			} 
			catch (SQLException e) {
				throw new RuntimeException( e );
			}
		}

		public Object next() {
			try {
				FeatureType featureType = state.featureType();
				
				SimpleFeatureBuilder builder = 
					new SimpleFeatureBuilder( state.getDataStore().getFeatureFactory() );
				builder.setType( featureType );
				
				//build up teh attributes
				for ( int i = 0; i < featureType.getAttributeCount(); i++) {
					AttributeType attributeType = 
						featureType.getAttributeType( i );
					
					builder.add( rs.getObject( attributeType.getName() ) );
				}
				
				//get the feature id
				String fid = state.primaryKey().encode( rs );
				return builder.build( fid );
			} 
			
			catch (Exception e) {
				throw new RuntimeException( e );
			}
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}
		
		public void close() throws SQLException {
			rs.close();
			st.close();
			connection.close();
		}
		
	}

}
