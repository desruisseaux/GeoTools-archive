package org.geotools.data.feature.memory;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import org.geotools.catalog.GeoResourceInfo;
import org.geotools.data.DataStore;
import org.geotools.data.FeatureListener;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.data.feature.FeatureSource2;
import org.geotools.data.feature.FilteringCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.Feature;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;
import org.opengis.filter.capability.FilterCapabilities;

import com.vividsolutions.jts.geom.Envelope;

public class MemorySource implements FeatureSource2 {

	private FeatureType type;

	private Collection content;

	private MemoryDataAccess dataStore;

	public MemorySource(MemoryDataAccess dataStore, FeatureType type,
			Collection collection) {
		this.dataStore = dataStore;
		this.type = type;
		this.content = collection;
	}

	public Collection content() {
		return Collections.unmodifiableCollection(content);
	}

	public Collection content(String query, String queryLanguage) {
		throw new UnsupportedOperationException();
	}

	public Collection content(Filter filter) {
		return new FilteringCollection(content(), filter);
	}

	public Object describe() {
		return dataStore.describe(type.getName());
	}

	public void dispose() {
	}

	public FilterCapabilities getFilterCapabilities() {
		throw new UnsupportedOperationException();
	}

	public GeoResourceInfo getInfo() {
		throw new UnsupportedOperationException();
	}

	public Name getName() {
		return type.getName();
	}

	public void setTransaction(Transaction t) {
		throw new UnsupportedOperationException();
	}

	public void addFeatureListener(FeatureListener listener) {
		throw new UnsupportedOperationException();
	}

	public Envelope getBounds() throws IOException {
		return getBounds(Filter.INCLUDE);
	}

	public Envelope getBounds(Query query) throws IOException {
		return getBounds(query.getFilter());
	}

	private Envelope getBounds(Filter filter) throws IOException {
		Collection collection = content(filter);
		Feature f;
		ReferencedEnvelope env = new ReferencedEnvelope(this.type.getCRS());
		for (Iterator it = collection.iterator(); it.hasNext();) {
			f = (Feature) it.next();
			env.include(f.getBounds());
		}
		return env;
	}

	public int getCount(Query query) throws IOException {
		Collection collection = content(query.getFilter());
		int count = 0;
		for (Iterator it = collection.iterator(); it.hasNext();) {
			it.next();
			count++;
		}
		return count;
	}

	public DataStore getDataStore() {
		return dataStore;
	}

	public FeatureCollection getFeatures(Query query) throws IOException {
		throw new UnsupportedOperationException();
	}

	public FeatureCollection getFeatures(Filter filter) throws IOException {
		throw new UnsupportedOperationException();
	}

	public FeatureCollection getFeatures() throws IOException {
		throw new UnsupportedOperationException();
	}

	public org.geotools.feature.FeatureType getSchema() {
		throw new UnsupportedOperationException();
	}

	public void removeFeatureListener(FeatureListener listener) {
		throw new UnsupportedOperationException();
	}

}
