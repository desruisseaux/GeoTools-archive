package org.geotools.catalog;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;

import javax.swing.Icon;

import org.geotools.catalog.defaults.DefaultGeoResourceInfo;
import org.geotools.data.DataStore;
import org.geotools.data.FeatureSource;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.FeatureType;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.util.ProgressListener;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;

/**
 * Resource implementation for resources which can map or resolve to a 
 * {@link org.geotools.data.FeatureSource}.
 * <p>
 * Subclasses must implement the methods:
 * <ul>
 * 	<li>{@link #createMetaData(FeatureSource, ProgressListener)}
 * </ul>
 * 
 * In addition, subclasses may wish to  <b>extend</b> the following methods in 
 * order to support additional resolves.
 * <ul>
 * 	<li>{@link #canResolve(Class)}
 * 	<li>{@link #resolve(Class, ProgressListener)}
 * </ul>
 * 
 * </p>
 * 
 * @author Justin Deoliveira, The Open Planning Project, jdeolive@openplans.org
 *
 */
public abstract class FeatureSourceGeoResource extends AbstractGeoResource {

	/**
	 * Parent handle
	 */
	DataStoreService parent;
	/**
	 * Feature type name
	 */
	String name;
	/**
	 * Cached feature source
	 */
	FeatureSource source;
	/**
	 * metadata object 
	 */
	GeoResourceInfo info;
	
	public FeatureSourceGeoResource ( DataStoreService parent, String name ) {
		this.parent = parent;
		this.name = name;
	}
	
	/**
	 * @return The name of the feature source, feature type.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Supports the required GeoResource resolves with an additional resolves 
	 * to:
	 * <ul>
	 * 	<li>{@link FeatureSourceGeoResource}
	 * 	<li>{@link FeatureType}
	 *  <li>{@link DataStore}
	 *  </ul>
	 * <p>
	 * Subclasses may wish to extend this method.
	 * </p>
	 */
	public boolean canResolve(Class adaptee) {
		if ( adaptee == null)
			return false;
		
		return adaptee.isAssignableFrom( Service.class ) || 
			adaptee.isAssignableFrom( GeoResourceInfo.class ) || 
			adaptee.isAssignableFrom( FeatureSource.class ) || 
			adaptee.isAssignableFrom( FeatureType.class ) || 
			adaptee.isAssignableFrom( DataStore.class );
	}
	
	/**
	 * Supports the required GeoResource resolves with an additional resolves 
	 * to:
	 * <ul>
	 * 	<li>{@link FeatureSourceGeoResource}
	 * 	<li>{@link FeatureType}
	 *  <li>{@link DataStore}
	 *  </ul>
	 * <p>
	 * Subclasses may wish to extend this method.
	 * </p>
	 */
	public Object resolve(Class adaptee, ProgressListener monitor)
			throws IOException {
		
		if ( adaptee == null )
			return null;
		
		if ( adaptee.isAssignableFrom( Service.class ) )
			return parent;
		
		if ( adaptee.isAssignableFrom( GeoResourceInfo.class ) )
			return getInfo( monitor );
		
		if ( adaptee.isAssignableFrom( FeatureSource.class ) ) 
			return featureSource( monitor );
		
		if ( adaptee.isAssignableFrom( FeatureType.class ) )
			return parent.dataStore( monitor ).getSchema( name );
		
		if ( adaptee.isAssignableFrom( DataStore.class) )
			return parent.dataStore( monitor );
		
		return null;
	}

	/**
	 * Returns the status of the handle based on the following.
	 * 
	 * 1. If a non-null error has been set with {@link #setMessage(Throwable)}
	 * then the handle is {@link Status#BROKEN}.
	 * 2. If {@link #source} is non-null the handle is {@link Status#CONNECTED}.
	 * 3. The handle is {@link Status#NOTCONNECTED}.
	 */
	public Status getStatus() {
		if ( getMessage() != null )  
			return Status.BROKEN;
		
		if ( source != null )
			return Status.CONNECTED;
		
		return Status.NOTCONNECTED;
	}
	
	public GeoResourceInfo getInfo(ProgressListener monitor) throws IOException {
		if ( info == null ) {
			
			DataStore dataStore = parent.dataStore( monitor );
			
			synchronized (dataStore) {
				if ( info == null ) {
					try {
						info = createMetaData( featureSource( monitor ), monitor );
						setMessage( null );
					}
					catch( Throwable t ) {
						String msg = "unable to create metadata";
						logger.log( Level.SEVERE, msg, t );
						setMessage( t );
					}
				}
			}
		}
		
		return info;
	}

	/**
	 * Creates the resource metadata.
	 * <p>
	 * Data providers providing custom metadata need to override this method. 
	 * The default implementation provided the following metadata mappings:
	 * 
	 * <ul>
	 * 	<li>{@link FeatureSource#getBounds()} -> {@link GeoResourceInfo#getBounds()}
	 * 	<li>{@link FeatureType#getTypeName()} -> {@link GeoResourceInfo#getName()}
	 * 	<li>{@link FeatureType#getNamespace()()} -> {@link GeoResourceInfo#getSchema()()}
	 * </ul>
	 * </p>
	 * 
	 * @param featureSource The underlying feature source.
	 * @param monitor Progress monitor for blocking calls.  
	 * 
	 * @return The resource info.
	 */
	protected GeoResourceInfo createMetaData( FeatureSource source, ProgressListener monitor ) 
		throws IOException {

		//calculate bounds
		ReferencedEnvelope rBounds = null;
		Envelope bounds = source.getBounds();
		if ( bounds != null ) {
			//we have an "optmized bounds", do we have a crs?
			if ( bounds instanceof ReferencedEnvelope ) {
				rBounds = (ReferencedEnvelope) bounds;
			}
			
			if ( rBounds == null ) {
				//since we had an optimized bounds from feature source, we would
				// like to avoid accessing the data, so check the type for 
				// crs info
				FeatureType schema = source.getSchema();
				if ( schema.getDefaultGeometry() != null ) {
					CoordinateReferenceSystem crs = 
						schema.getDefaultGeometry().getCoordinateSystem();
					
					if ( crs != null ) {
						rBounds = new ReferencedEnvelope( bounds, crs );
					}
					else {
						rBounds = null;
					}
				}
			}
			
			if ( rBounds == null ) {
				rBounds = new ReferencedEnvelope( bounds, null );
			}
		}
		else {
			//manually calculate the bounds
			bounds = new Envelope();
			
			FeatureIterator itr = source.getFeatures().features();
			if ( itr.hasNext() ) {
				bounds.init( itr.next().getBounds() );
				while( itr.hasNext() ) {
					bounds.expandToInclude( itr.next().getBounds() );
				}
			}
			
			FeatureType schema = source.getSchema();
			CoordinateReferenceSystem crs = null;
			if ( schema.getDefaultGeometry() != null ) {
				crs = schema.getDefaultGeometry().getCoordinateSystem();
			}
			
			rBounds = new ReferencedEnvelope( bounds, crs );
		}
		
		String name = source.getSchema().getTypeName();
		URI schema = source.getSchema().getNamespace();
		
		return new DefaultGeoResourceInfo( null, name, null, schema, rBounds, null, null );
	}
	
	/**
	 * Uses the parent identifer, and tacks {@link #name} on as a fragment.
	 */
	public URI getIdentifier() {
		URI uri = parent.getIdentifier();
		
		try {
			return new URI( uri.getScheme(), uri.getHost(), uri.getPath(), name );
		} 
		catch (URISyntaxException e) {
			String msg = "Unable to build uri identifer";
			logger.log( Level.WARNING, msg, e );
			setMessage( e );
		}
		
		try {
			return new URI( name );
		} 
		catch (URISyntaxException e) {
			//shoult not happen
		}
		
		return null;
		
	}

	protected FeatureSource featureSource( ProgressListener monitor ) {
		
		if ( source == null ) {
			DataStore dataStore = parent.dataStore( monitor );
			try {
				synchronized ( dataStore ) {
					source = dataStore.getFeatureSource( name );
					if ( source == null )
						throw new NullPointerException();
					
					setMessage( null );
					return source;
				}
			}
			catch( Throwable t ) {
				String msg = "Unable to resolve feature source.";
				logger.log( Level.SEVERE, msg, t );
				setMessage( t );
			}
		}
		
		return source;
	}
	
	protected FeatureType featureType( ProgressListener monitor ) {
		 if ( featureSource( monitor ) != null ) 
			 return featureSource( monitor ).getSchema();
		 
		 return null;
	}
}
