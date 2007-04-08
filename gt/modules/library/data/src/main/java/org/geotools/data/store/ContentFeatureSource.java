package org.geotools.data.store;

import java.io.IOException;

import org.geotools.data.DataStore;
import org.geotools.data.FeatureListener;
import org.geotools.data.FeatureSource;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureType;
import org.opengis.filter.Filter;

import com.vividsolutions.jts.geom.Envelope;

public class ContentFeatureSource implements FeatureSource {

	/**
	 * The entry for the feautre source.
	 */
	protected ContentEntry entry;
	/**
	 * The transaction to work from..
	 */
	protected Transaction transaction;
	
	public ContentFeatureSource( ContentEntry entry ) {
		this.entry = entry;
	}
	
	public void setTransaction(Transaction transaction) {
		this.transaction = transaction;
	}
	
	public Transaction getTransaction() {
		return transaction;
	}
	
	public Content getContent() {
		return entry.getDataStore().getContent();
	}
	
	public final DataStore getDataStore() {
		return entry.getDataStore();
	}
	
	public final Envelope getBounds() throws IOException {
		return getContent().all( entry.getState( transaction ) ).getBounds();
	}

	public final Envelope getBounds(Query query) throws IOException {
		return getContent().filter( entry.getState( transaction ), query.getFilter() ).getBounds();
	}

	public final int getCount(Query query) throws IOException {
		return getContent().filter( entry.getState( transaction ), query.getFilter() ).size();
	}

	public final FeatureCollection getFeatures() throws IOException {
		return getFeatures( Query.ALL );
	}
	
	public final FeatureCollection getFeatures(Query query) throws IOException {
		
		FeatureCollection features = getFeatures( query.getFilter() );
		
		if (query.getCoordinateSystemReproject() != null) {
			// features = features.reproject( query.getCoordinateSystemReproject() );
		}
		if (query.getCoordinateSystem() != null) {
			// features = features.toCRS( query.getCoordinateSystem() );
		}
		if (query.getMaxFeatures() != Integer.MAX_VALUE) {
			// features = (FeatureCollection) features.sort(
			//		SortBy.NATURAL_ORDER).subList(0, query.getMaxFeatures());
		}
		if (query.getNamespace() != null) {
			// features = features.toNamespace( query.getNamespace() );
		}
		if (query.getPropertyNames() != Query.ALL_NAMES) {
			// features = features.reType( query.getPropertyNames() );
		}
		
		return features;
	}

	public final FeatureCollection getFeatures(Filter filter) throws IOException {
	
		if ( filter == null || filter == Filter.INCLUDE ) {
			return getContent().all( entry.getState( transaction ) );
		}
		
		return getContent().filter( entry.getState( transaction ), filter );
	}

	public final FeatureType getSchema() {
		try {
			return entry.getState( transaction ).featureType();
		} 
		catch (IOException e) {
			throw new RuntimeException( e );
		}
	}

	public final void addFeatureListener(FeatureListener listener) {
		entry.getState( transaction ).addListener( listener );
	}

	public final void removeFeatureListener(FeatureListener listener) {
		entry.getState( transaction ).removeListener( listener );
	}

}
