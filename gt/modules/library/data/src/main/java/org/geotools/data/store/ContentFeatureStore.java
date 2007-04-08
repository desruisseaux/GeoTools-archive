package org.geotools.data.store;

import java.io.IOException;
import java.util.Set;

import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureStore;
import org.geotools.feature.AttributeType;
import org.geotools.feature.FeatureCollection;
import org.opengis.filter.Filter;

public class ContentFeatureStore extends ContentFeatureSource implements
		FeatureStore {

	public ContentFeatureStore(ContentEntry entry) {
		super(entry);
	}

	public Set addFeatures(FeatureCollection collection) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	public void modifyFeatures(AttributeType[] type, Object[] value,
			Filter filter) throws IOException {
		// TODO Auto-generated method stub

	}

	public void modifyFeatures(AttributeType type, Object value, Filter filter)
			throws IOException {
		// TODO Auto-generated method stub

	}

	public void removeFeatures(Filter filter) throws IOException {
		// TODO Auto-generated method stub

	}

	public void setFeatures(FeatureReader reader) throws IOException {
		// TODO Auto-generated method stub

	}

}
